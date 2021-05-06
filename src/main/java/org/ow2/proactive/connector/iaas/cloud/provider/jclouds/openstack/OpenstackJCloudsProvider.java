/*
 * ProActive Parallel Suite(TM):
 * The Open Source library for parallel and distributed
 * Workflows & Scheduling, Orchestration, Cloud Automation
 * and Big Data Analysis on Enterprise Grids & Clouds.
 *
 * Copyright (c) 2007 - 2017 ActiveEon
 * Contact: contact@activeeon.com
 *
 * This library is free software: you can redistribute it and/or
 * modify it under the terms of the GNU Affero General Public License
 * as published by the Free Software Foundation: version 3 of
 * the License.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Affero General Public License for more details.
 *
 * You should have received a copy of the GNU Affero General Public License
 * along with this program. If not, see <http://www.gnu.org/licenses/>.
 *
 * If needed, contact us to obtain a release under GPL Version 2 or 3
 * or a different license than the AGPL.
 */
package org.ow2.proactive.connector.iaas.cloud.provider.jclouds.openstack;

import java.util.AbstractMap.SimpleImmutableEntry;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;
import java.util.stream.Collectors;

import javax.ws.rs.ClientErrorException;
import javax.ws.rs.NotSupportedException;
import javax.ws.rs.core.Response;

import org.apache.commons.lang3.NotImplementedException;
import org.jclouds.compute.ComputeService;
import org.jclouds.compute.options.RunScriptOptions;
import org.jclouds.domain.LoginCredentials;
import org.jclouds.net.domain.IpProtocol;
import org.jclouds.openstack.nova.v2_0.NovaApi;
import org.jclouds.openstack.nova.v2_0.domain.*;
import org.jclouds.openstack.nova.v2_0.extensions.FloatingIPApi;
import org.jclouds.openstack.nova.v2_0.extensions.SecurityGroupApi;
import org.jclouds.openstack.nova.v2_0.features.ServerApi;
import org.jclouds.openstack.nova.v2_0.options.CreateServerOptions;
import org.ow2.proactive.connector.iaas.cloud.TagManager;
import org.ow2.proactive.connector.iaas.cloud.provider.jclouds.JCloudsProvider;
import org.ow2.proactive.connector.iaas.model.Hardware;
import org.ow2.proactive.connector.iaas.model.Infrastructure;
import org.ow2.proactive.connector.iaas.model.Instance;
import org.ow2.proactive.connector.iaas.model.InstanceCredentials;
import org.ow2.proactive.connector.iaas.model.Network;
import org.ow2.proactive.connector.iaas.model.Tag;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;

import lombok.Getter;
import lombok.extern.log4j.Log4j2;


@Component
@Log4j2
public class OpenstackJCloudsProvider extends JCloudsProvider {

    @Getter
    private final String type = "openstack-nova";

    private static final String KEY_PAIR_PREFIX = "openstack-key-pair";

    private static final String SINGLE_INSTANCE = "1";

    // auto generated key pair name for each instance
    private static Map<String, String> autoCreatedKeyNamePerInstance = new HashMap<>();

    // auto generated security group name for each instance
    private static Map<String, String> autoCreatedSgPerInstance = new HashMap<>();

    private String region;

    @Autowired
    private OpenstackUtil openstackUtil;

    @Autowired
    private TagManager tagManager;

    @Override
    public Set<Instance> createInstance(Infrastructure infrastructure, Instance instance) {

        openstackUtil.validateOpenstackInfrastructureParameters(infrastructure);
        NovaApi novaApi = buildNovaApi(infrastructure);

        region = openstackUtil.getInfrastructureRegion(infrastructure);
        ServerApi serverApi = novaApi.getServerApi(region);
        com.google.common.base.Optional<SecurityGroupApi> securityGroupApi = novaApi.getSecurityGroupApi(region);
        if (!securityGroupApi.isPresent()) {
            log.warn("The support of security groups has not been found in this OpenStack instance. Therefore, the explicit configuration of security groups and the support of port opening will be disabled");
        }
        CreateServerOptions serverOptions;
        try {
            serverOptions = createOptions(infrastructure, instance, securityGroupApi);
        } catch (Exception e) {
            log.error("Exception during create instance options.", e);
            throw e;
        }
        log.info("Openstack instance will use options: " + serverOptions.toString());

        Set<Instance> createdInstances = new HashSet<>();
        for (int i = 0; i < Integer.parseInt(instance.getNumber()); i++) {
            Server server = createOpenstackInstance(instance, serverApi, serverOptions);
            Instance createdInstance = this.createInstanceFromNode(server);
            createdInstances.add(createdInstance);
            if (isPublicKeyNameNotSet(instance)) {
                autoCreatedKeyNamePerInstance.put(createdInstance.getId(), serverOptions.getKeyPairName());
            }
            if (useAutoGeneratedSecurityGroup(instance)) {
                serverOptions.getSecurityGroupNames()
                             .stream()
                             .findFirst()
                             .ifPresent(sg -> autoCreatedSgPerInstance.put(createdInstance.getId(), sg));
            }
        }

        return createdInstances;

    }

    private CreateServerOptions createOptions(Infrastructure infrastructure, Instance instance,
            com.google.common.base.Optional<SecurityGroupApi> securityGroupApiOptional) {

        // Acquire or generate KeyPair name
        String publicKeyName;
        if (isPublicKeyNameNotSet(instance)) {
            publicKeyName = createKeyPair(infrastructure, instance).getKey();
            log.info("Openstack instance will use generated key-pair: " + publicKeyName);
        } else {
            publicKeyName = instance.getCredentials().getPublicKeyName();
        }

        // Create options
        CreateServerOptions createServerOptions = new CreateServerOptions().keyPairName(publicKeyName)
                                                                           .userData(buildScriptToExecuteString(instance.getInitScript()).getBytes());
        if (isNetworkIdSet(instance.getNetwork())) {
            log.info("Networking: using specified network");
            createServerOptions = createServerOptions.networks(instance.getNetwork().getNetworkIds());
        }

        // Open port and support security group.
        if (securityGroupApiOptional.isPresent() && instance.getOptions() != null) {
            List<String> securityGroupName = instance.getOptions().getSecurityGroupNames();
            int[] inboundPort = instance.getOptions().getPortsToOpen();
            if (!StringUtils.isEmpty(securityGroupName)) {
                // Have we an explicit security group requested ?
                log.info("Security Group: Using supplied security group");
                createServerOptions.securityGroupNames(securityGroupName);
            } else if (inboundPort != null && inboundPort.length > 0) {
                // Or do we have to create a new one w/ the created request port to be opened ?
                log.info("Security group: Configuring new security group to allow inbound connections to the specified ports");
                String sgName = "SG-" + System.currentTimeMillis();
                SecurityGroupApi sgApi = securityGroupApiOptional.get();
                SecurityGroup sg = sgApi.createWithDescription(sgName, "Auto-generated security group");
                for (int port : inboundPort) {
                    if (port == -1) {
                        // Port -1 means we open the host to ICMP, as spec by Amazon
                        sgApi.createRuleAllowingCidrBlock(sg.getId(),
                                                          Ingress.builder()
                                                                 .fromPort(port)
                                                                 .toPort(port)
                                                                 .ipProtocol(IpProtocol.ICMP)
                                                                 .build(),
                                                          "0.0.0.0/0");
                    } else if (port > 1 && port < 65536) { // 65536 == 8^16 => It is the upperband of adressable port
                        sgApi.createRuleAllowingCidrBlock(sg.getId(),
                                                          Ingress.builder()
                                                                 .fromPort(port)
                                                                 .toPort(port)
                                                                 .ipProtocol(IpProtocol.TCP)
                                                                 .build(),
                                                          "0.0.0.0/0");
                        sgApi.createRuleAllowingCidrBlock(sg.getId(),
                                                          Ingress.builder()
                                                                 .fromPort(port)
                                                                 .toPort(port)
                                                                 .ipProtocol(IpProtocol.UDP)
                                                                 .build(),
                                                          "0.0.0.0/0");
                    }
                    // Otherwise, the provided port value is invalide
                }
                createServerOptions.securityGroupNames(sgName);
            }
        }

        // Set tags before returning options
        return createServerOptions.metadata(tagManager.retrieveAllTags(infrastructure.getId(), instance.getOptions())
                                                      .stream()
                                                      .collect(Collectors.toMap(Tag::getKey, Tag::getValue)));
    }

    private boolean isPublicKeyNameNotSet(Instance instance) {
        return (instance.getCredentials() == null) || (instance.getCredentials().getPublicKeyName() == null) ||
               (instance.getCredentials().getPublicKeyName().isEmpty());
    }

    private boolean useAutoGeneratedSecurityGroup(Instance instance) {
        return instance.getOptions() != null && StringUtils.isEmpty(instance.getOptions().getSecurityGroupNames()) &&
               instance.getOptions().getPortsToOpen() != null && instance.getOptions().getPortsToOpen().length > 0;
    }

    private boolean isNetworkIdSet(Network network) {
        return network != null && network.getNetworkIds() != null;
    }

    @Override
    public String addToInstancePublicIp(Infrastructure infrastructure, String instanceId, String optionalDesiredIp) {

        NovaApi novaApi = buildNovaApi(infrastructure);
        validatePlateformOperation(novaApi);

        FloatingIPApi api = novaApi.getFloatingIPApi(region).get();

        // Try to retrieve a floatingIP that match with the provided IP address, otherwise get the first available
        List<FloatingIP> floatingIPs = api.list().toList();
        Optional<FloatingIP> optionalDesiredFloatingIp = Optional.ofNullable(optionalDesiredIp)
                                                                 .map(desiredIp -> floatingIPs.stream()
                                                                                              .filter(floatingIP -> floatingIP.getFixedIp() == null &&
                                                                                                                    floatingIP.getIp()
                                                                                                                              .equals(desiredIp))
                                                                                              .findAny()
                                                                                              .orElse(null));
        FloatingIP ip = optionalDesiredFloatingIp.orElseGet(() -> floatingIPs.stream()
                                                                             .filter(floatingIP -> floatingIP.getFixedIp() == null)
                                                                             .filter(floatingIP -> !Optional.ofNullable(optionalDesiredIp)
                                                                                                            .isPresent() ||
                                                                                                   floatingIP.getIp()
                                                                                                             .equals(optionalDesiredIp))
                                                                             .findFirst()
                                                                             .orElseThrow(() -> new ClientErrorException("No floating IP available in the floating IP pool",
                                                                                                                         Response.Status.BAD_REQUEST)));

        api.addToServer(ip.getIp(), instanceId);

        return ip.getIp();
    }

    @Override
    public void removeInstancePublicIp(Infrastructure infrastructure, String instanceId, String optionalDesiredIp) {

        NovaApi novaApi = buildNovaApi(infrastructure);
        FloatingIPApi api = novaApi.getFloatingIPApi(region).get();

        // Try to retrieve a floatingIP that match with the provided IP address, otherwise get the first available
        List<FloatingIP> floatingIPs = api.list().toList();
        Optional<FloatingIP> optionalDesiredFloatingIp = Optional.ofNullable(optionalDesiredIp)
                                                                 .map(desiredIp -> floatingIPs.stream()
                                                                                              .filter(floatingIP -> instanceId.equals(floatingIP.getInstanceId()))
                                                                                              .filter(floatingIP -> floatingIP.getIp()
                                                                                                                              .equals(desiredIp))
                                                                                              .findAny()
                                                                                              .orElse(null));

        FloatingIP ip = optionalDesiredFloatingIp.orElseGet(() -> floatingIPs.stream()
                                                                             .filter(floatingIP -> instanceId.equals(floatingIP.getInstanceId()))
                                                                             .findFirst()
                                                                             .orElseThrow(() -> new ClientErrorException("No floating IP associated with this instance",
                                                                                                                         Response.Status.BAD_REQUEST)));

        api.removeFromServer(ip.getIp(), instanceId);
    }

    @Override
    public SimpleImmutableEntry<String, String> createKeyPair(Infrastructure infrastructure, Instance instance) {
        String keyPairName = KEY_PAIR_PREFIX + "-" + UUID.randomUUID();
        NovaApi novaApi = buildNovaApi(infrastructure);
        KeyPair keyPair = novaApi.getKeyPairApi(region).get().create(keyPairName);

        log.info("Openstack key-pair created: " + keyPair.getName() + " [" + keyPair.toString() + "]");
        return new SimpleImmutableEntry<>(keyPair.getName(), keyPair.toString());
    }

    @Override
    public void deleteKeyPair(Infrastructure infrastructure, String keyPairName, String region) {
        throw new NotImplementedException("This method is not yet implemented.");
    }

    public void deleteKeyPair(Infrastructure infrastructure, String keyPairName) {
        NovaApi novaApi = buildNovaApi(infrastructure);
        novaApi.getKeyPairApi(region).get().delete(keyPairName);
        log.info("Remove the auto-generated openstack key-pair: " + keyPairName);
    }

    private void validatePlateformOperation(NovaApi novaApi) {
        if (!novaApi.getFloatingIPApi(region).isPresent()) {
            throw new NotSupportedException("Operation not supported by the targeted Openstack version");
        }
    }

    private Server createOpenstackInstance(Instance instance, ServerApi serverApi, CreateServerOptions serverOptions) {
        openstackUtil.validateOpenstackInstanceParameters(instance);
        ServerCreated serverCreated = serverApi.create(instance.getTag(),
                                                       instance.getImage(),
                                                       instance.getHardware().getType(),
                                                       serverOptions);

        log.info("Server instance created: " + serverCreated.toString());

        return serverApi.get(serverCreated.getId());
    }

    private final Instance createInstanceFromNode(Server server) {
        Instance instance = Instance.builder()
                                    .id(region + "/" + server.getId())
                                    .tag(server.getName())
                                    .image(server.getImage().getName())
                                    .number(SINGLE_INSTANCE)
                                    .hardware(Hardware.builder().type(server.getFlavor().getName()).build())
                                    .status(server.getStatus().name())
                                    .build();

        log.info("Created instance: " + instance.toString());

        return instance;
    }

    @Override
    public RunScriptOptions getRunScriptOptionsWithCredentials(InstanceCredentials credentials) {
        log.info("Credentials used to execute script on instance: [username=" + credentials.getUsername() + "]");
        return RunScriptOptions.Builder.runAsRoot(false)
                                       .overrideLoginCredentials(new LoginCredentials.Builder().user(credentials.getUsername())
                                                                                               .password(credentials.getPassword())
                                                                                               .authenticateSudo(false)
                                                                                               .build());
    }

    @Override
    public void deleteInstance(Infrastructure infrastructure, String instanceId) {
        super.deleteInstance(infrastructure, instanceId);
        String keyPairName = autoCreatedKeyNamePerInstance.get(instanceId);
        if (keyPairName != null) {
            deleteKeyPair(infrastructure, keyPairName);
            autoCreatedKeyNamePerInstance.remove(instanceId);
        }
        String securityGroupName = autoCreatedSgPerInstance.get(instanceId);
        if (securityGroupName != null) {
            deleteSecurityGroup(infrastructure, securityGroupName);
            autoCreatedSgPerInstance.remove(instanceId);
        }
    }

    private void deleteSecurityGroup(Infrastructure infrastructure, String securityGroupName) {
        NovaApi novaApi = buildNovaApi(infrastructure);
        com.google.common.base.Optional<SecurityGroupApi> securityGroupApiOptional = novaApi.getSecurityGroupApi(region);
        if (!securityGroupApiOptional.isPresent()) {
            log.warn("The support of security groups has not been found in this OpenStack instance. Therefore, the explicit configuration of security groups and the support of port opening will be disabled");
        } else {
            SecurityGroupApi sgApi = securityGroupApiOptional.get();
            String securityGroupId = sgApi.list()
                                          .firstMatch(sg -> sg.getName().equals(securityGroupName))
                                          .get()
                                          .getId();
            sgApi.delete(securityGroupId);
            log.info("Remove the auto-generated openstack security group: " + securityGroupName);
        }
    }

    protected NovaApi buildNovaApi(Infrastructure infrastructure) {
        ComputeService computeService = getComputeServiceFromInfastructure(infrastructure);
        return computeService.getContext().unwrapApi(NovaApi.class);
    }

}

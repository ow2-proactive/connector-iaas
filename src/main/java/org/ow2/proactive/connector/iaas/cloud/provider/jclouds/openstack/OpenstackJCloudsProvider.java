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

import java.util.Set;
import java.util.function.BiFunction;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

import javax.ws.rs.ClientErrorException;
import javax.ws.rs.NotSupportedException;
import javax.ws.rs.core.Response;

import org.jclouds.compute.ComputeService;
import org.jclouds.openstack.nova.v2_0.NovaApi;
import org.jclouds.openstack.nova.v2_0.domain.FloatingIP;
import org.jclouds.openstack.nova.v2_0.domain.Server;
import org.jclouds.openstack.nova.v2_0.domain.ServerCreated;
import org.jclouds.openstack.nova.v2_0.extensions.FloatingIPApi;
import org.jclouds.openstack.nova.v2_0.features.ServerApi;
import org.jclouds.openstack.nova.v2_0.options.CreateServerOptions;
import org.ow2.proactive.connector.iaas.cloud.provider.jclouds.JCloudsProvider;
import org.ow2.proactive.connector.iaas.model.Hardware;
import org.ow2.proactive.connector.iaas.model.Infrastructure;
import org.ow2.proactive.connector.iaas.model.Instance;
import org.springframework.stereotype.Component;

import lombok.Getter;


@Component
public class OpenstackJCloudsProvider extends JCloudsProvider {

    private final static String region = "RegionOne";

    @Getter
    private final String type = "openstack-nova";

    @Override
    public Set<Instance> createInstance(Infrastructure infrastructure, Instance instance) {

        ComputeService computeService = getComputeServiceFromInfastructure(infrastructure);

        NovaApi novaApi = computeService.getContext().unwrapApi((NovaApi.class));

        ServerApi serverApi = novaApi.getServerApi(region);

        String script = buildScriptToExecuteString(instance.getInitScript());

        CreateServerOptions serverOptions = new CreateServerOptions().keyPairName(instance.getCredentials()
                                                                                          .getPublicKeyName())
                                                                     .userData(script.getBytes());

        return IntStream.rangeClosed(1, Integer.valueOf(instance.getNumber()))
                        .mapToObj(i -> createOpenstackInstance(instance, serverApi, serverOptions))
                        .map(server -> instanceCreatorFromNodeMetadata.apply(server, infrastructure.getId()))
                        .collect(Collectors.toSet());

    }

    @Override
    public String addToInstancePublicIp(Infrastructure infrastructure, String instanceId) {

        ComputeService computeService = getComputeServiceFromInfastructure(infrastructure);

        NovaApi novaApi = computeService.getContext().unwrapApi(NovaApi.class);

        validatePlateformOperation(novaApi);

        FloatingIPApi api = novaApi.getFloatingIPApi(region).get();

        FloatingIP ip = api.list()
                           .toList()
                           .stream()
                           .filter(floatingIP -> floatingIP.getFixedIp() == null)
                           .findFirst()
                           .orElseThrow(() -> new ClientErrorException("No floating IP available in the floating IP pool",
                                                                       Response.Status.BAD_REQUEST));

        try {
            api.addToServer(ip.getIp(), instanceId);
        } catch (Exception e) {
            if (e.getMessage().contains("Unable to associate floating ip")) {
                throw new ClientErrorException("A floating IP is already associated to the instance " + instanceId,
                                               Response.Status.BAD_REQUEST);
            } else {
                throw new RuntimeException(e);
            }
        }

        return ip.getIp();

    }

    @Override
    public void removeInstancePublicIp(Infrastructure infrastructure, String instanceId) {
        ComputeService computeService = getComputeServiceFromInfastructure(infrastructure);

        NovaApi novaApi = computeService.getContext().unwrapApi(NovaApi.class);

        FloatingIPApi api = novaApi.getFloatingIPApi(region).get();

        FloatingIP ip = api.list()
                           .toList()
                           .stream()
                           .filter(floatingIP -> instanceId.equals(floatingIP.getInstanceId()))
                           .findFirst()
                           .orElseThrow(() -> new ClientErrorException("No floating IP associated with this instance",
                                                                       Response.Status.BAD_REQUEST));

        try {
            api.removeFromServer(ip.getIp(), instanceId);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    private void validatePlateformOperation(NovaApi novaApi) {
        if (!novaApi.getFloatingIPApi(region).isPresent()) {
            throw new NotSupportedException("Operation not supported for this Openstack cloud");
        }
    }

    private Server createOpenstackInstance(Instance instance, ServerApi serverApi, CreateServerOptions serverOptions) {
        ServerCreated serverCreated = serverApi.create(instance.getTag(),
                                                       instance.getImage(),
                                                       instance.getHardware().getType(),
                                                       serverOptions);

        return serverApi.get(serverCreated.getId());
    }

    protected final BiFunction<Server, String, Instance> instanceCreatorFromNodeMetadata = (server,
            infrastructureId) -> {

        return Instance.builder()
                       .id(server.getId())
                       .tag(server.getName())
                       .image(server.getImage().getName())
                       .number("1")
                       .hardware(Hardware.builder().type(server.getFlavor().getName()).build())
                       .status(server.getStatus().name())
                       .build();
    };

}

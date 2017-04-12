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
package org.ow2.proactive.connector.iaas.cloud.provider.jclouds.aws;

import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;

import org.jclouds.aws.ec2.AWSEC2Api;
import org.jclouds.aws.ec2.compute.AWSEC2TemplateOptions;
import org.jclouds.compute.ComputeService;
import org.jclouds.compute.domain.NodeMetadata;
import org.jclouds.compute.domain.Template;
import org.jclouds.compute.domain.TemplateBuilder;
import org.jclouds.compute.domain.internal.NodeMetadataImpl;
import org.jclouds.domain.Location;
import org.jclouds.ec2.domain.PublicIpInstanceIdPair;
import org.jclouds.ec2.features.ElasticIPAddressApi;
import org.ow2.proactive.connector.iaas.cloud.provider.jclouds.JCloudsProvider;
import org.ow2.proactive.connector.iaas.model.Infrastructure;
import org.ow2.proactive.connector.iaas.model.Instance;
import org.ow2.proactive.connector.iaas.model.InstanceCredentials;
import org.ow2.proactive.connector.iaas.model.Options;
import org.springframework.stereotype.Component;

import com.google.common.collect.Sets;

import lombok.Getter;


@Component
public class AWSEC2JCloudsProvider extends JCloudsProvider {

    @Getter
    private final String type = "aws-ec2";

    private final static String INSTANCE_ID_REGION_SEPARATOR = "/";

    @Override
    public Set<Instance> createInstance(Infrastructure infrastructure, Instance instance) {

        ComputeService computeService = getComputeServiceFromInfastructure(infrastructure);

        TemplateBuilder templateBuilder = computeService.templateBuilder()
                                                        .minRam(Integer.parseInt(instance.getHardware().getMinRam()))
                                                        .minCores(Double.parseDouble(instance.getHardware()
                                                                                             .getMinCores()))
                                                        .imageId(instance.getImage());

        Template template = templateBuilder.build();

        Optional.ofNullable(instance.getOptions()).ifPresent(options -> addOptions(template, options));

        Optional.ofNullable(instance.getCredentials()).ifPresent(options -> addCredential(template, options));

        Set<? extends NodeMetadata> createdNodeMetaData = Sets.newHashSet();

        try {
            createdNodeMetaData = computeService.createNodesInGroup(instance.getTag(),
                                                                    Integer.parseInt(instance.getNumber()),
                                                                    template);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }

        return createdNodeMetaData.stream()
                                  .map(computeMetadata -> (NodeMetadataImpl) computeMetadata)
                                  .map(nodeMetadataImpl -> instanceCreatorFromNodeMetadata.apply(nodeMetadataImpl,
                                                                                                 infrastructure.getId()))
                                  .collect(Collectors.toSet());

    }

    private void addCredential(Template template, InstanceCredentials credentials) {
        Optional.ofNullable(credentials.getPublicKeyName())
                .filter(keyName -> !keyName.isEmpty())
                .ifPresent(keyName -> template.getOptions()
                                              .as(AWSEC2TemplateOptions.class)
                                              .keyPair(credentials.getPublicKeyName()));
    }

    private void addOptions(Template template, Options options) {
        Optional.ofNullable(options.getSpotPrice())
                .filter(spotPrice -> !spotPrice.isEmpty())
                .ifPresent(spotPrice -> template.getOptions()
                                                .as(AWSEC2TemplateOptions.class)
                                                .spotPrice(Float.valueOf(options.getSpotPrice())));

        Optional.ofNullable(options.getSecurityGroupNames())
                .filter(securityGroupName -> !securityGroupName.isEmpty())
                .ifPresent(securityGroupName -> template.getOptions()
                                                        .as(AWSEC2TemplateOptions.class)
                                                        .securityGroups(options.getSecurityGroupNames()));

        Optional.ofNullable(options.getSubnetId())
                .filter(subnetId -> !subnetId.isEmpty())
                .ifPresent(subnetId -> template.getOptions()
                                               .as(AWSEC2TemplateOptions.class)
                                               .subnetId(options.getSubnetId()));

    }

    private String getRegionFromNode(ComputeService computeService, NodeMetadata node) {
        Location nodeLocation = node.getLocation();
        Set<? extends Location> assignableLocations = computeService.listAssignableLocations();
        while (!assignableLocations.contains(nodeLocation)) {
            nodeLocation = nodeLocation.getParent();
        }
        return nodeLocation.getId();
    }

    @Override
    public String addToInstancePublicIp(Infrastructure infrastructure, String instanceId, String optionalDesiredIp) {

        ComputeService computeService = getComputeServiceFromInfastructure(infrastructure);
        NodeMetadata node = computeService.getNodeMetadata(instanceId);
        ElasticIPAddressApi elasticIPAddressApi = computeService.getContext()
                                                                .unwrapApi(AWSEC2Api.class)
                                                                .getElasticIPAddressApi()
                                                                .get();

        // Get the region
        String region;
        if (instanceId.contains(INSTANCE_ID_REGION_SEPARATOR)) {
            region = instanceId.split(INSTANCE_ID_REGION_SEPARATOR)[0];
        } else {
            region = getRegionFromNode(computeService, node);
        }

        String id = node.getProviderId();

        // Try to assign existing IP
        if (Optional.ofNullable(optionalDesiredIp).isPresent()) {
            elasticIPAddressApi.associateAddressInRegion(region, optionalDesiredIp, id);
            return optionalDesiredIp;
        }

        // Try to associate to an existing IP
        String ip = null;
        Set<PublicIpInstanceIdPair> unassignedIps = elasticIPAddressApi.describeAddressesInRegion(region)
                                                                       .stream()
                                                                       .filter(address -> address.getInstanceId() == null)
                                                                       .collect(Collectors.toSet());
        for (PublicIpInstanceIdPair unassignedIp : unassignedIps) {
            try {
                elasticIPAddressApi.associateAddressInRegion(region, unassignedIp.getPublicIp(), id);
            } catch (RuntimeException e) {
                continue;
            }
            ip = unassignedIp.getPublicIp();
            break;
        }
        // Allocate a new IP otherwise
        if (ip == null) {
            try {
                ip = elasticIPAddressApi.allocateAddressInRegion(region);
            } catch (Exception e) {
                throw new RuntimeException("Failed to allocate a new IP address. All IP addresses are in use.", e);
            }
            elasticIPAddressApi.associateAddressInRegion(region, ip, id);
        }
        return ip;
    }

    @Override
    public void removeInstancePublicIp(Infrastructure infrastructure, String instanceId, String optionalDesiredIp) {

        ComputeService computeService = getComputeServiceFromInfastructure(infrastructure);
        NodeMetadata node = computeService.getNodeMetadata(instanceId);
        String region = node.getLocation().getId();
        ElasticIPAddressApi elasticIPAddressApi = computeService.getContext()
                                                                .unwrapApi(AWSEC2Api.class)
                                                                .getElasticIPAddressApi()
                                                                .get();
        // Try to dissociate the specified IP
        if (Optional.ofNullable(optionalDesiredIp).isPresent()) {
            elasticIPAddressApi.disassociateAddressInRegion(region, optionalDesiredIp);
            return;
        }
        // Dissociate one of the IP associated to the instance
        node.getPublicAddresses().stream().findAny().ifPresent(ip -> {
            elasticIPAddressApi.disassociateAddressInRegion(region, ip);
            //elasticIPAddressApi.releaseAddressInRegion(region, ip);
        });
    }

}

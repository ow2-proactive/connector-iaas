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

import javax.ws.rs.NotSupportedException;

import org.jclouds.aws.ec2.compute.AWSEC2TemplateOptions;
import org.jclouds.compute.ComputeService;
import org.jclouds.compute.domain.NodeMetadata;
import org.jclouds.compute.domain.Template;
import org.jclouds.compute.domain.TemplateBuilder;
import org.jclouds.compute.domain.internal.NodeMetadataImpl;
import org.ow2.proactive.connector.iaas.cloud.provider.jclouds.JCloudsProvider;
import org.ow2.proactive.connector.iaas.model.Credentials;
import org.ow2.proactive.connector.iaas.model.Infrastructure;
import org.ow2.proactive.connector.iaas.model.Instance;
import org.ow2.proactive.connector.iaas.model.Options;
import org.springframework.stereotype.Component;

import com.google.common.collect.Sets;

import lombok.Getter;


@Component
public class AWSEC2JCloudsProvider extends JCloudsProvider {

    @Getter
    private final String type = "aws-ec2";

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

    private void addCredential(Template template, Credentials credentials) {
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

    @Override
    public String addToInstancePublicIp(Infrastructure infrastructure, String instanceId) {
        throw new NotSupportedException("Operation not supported for AWS EC2");
    }

    @Override
    public void removeInstancePublicIp(Infrastructure infrastructure, String instanceId) {
        throw new NotSupportedException("Operation not supported for AWS EC2");
    }

}

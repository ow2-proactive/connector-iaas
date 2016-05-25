package org.ow2.proactive.connector.iaas.cloud.provider.jclouds.aws;

import java.util.Set;
import java.util.stream.Collectors;

import org.jclouds.compute.ComputeService;
import org.jclouds.compute.domain.NodeMetadata;
import org.jclouds.compute.domain.Template;
import org.jclouds.compute.domain.TemplateBuilder;
import org.jclouds.compute.domain.internal.NodeMetadataImpl;
import org.jclouds.ec2.compute.options.EC2TemplateOptions;
import org.ow2.proactive.connector.iaas.cloud.provider.jclouds.JCloudsProvider;
import org.ow2.proactive.connector.iaas.model.Infrastructure;
import org.ow2.proactive.connector.iaas.model.Instance;
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
                .minCores(Double.parseDouble(instance.getHardware().getMinCores()))
                .imageId(instance.getImage());

        Set<? extends NodeMetadata> createdNodeMetaData = Sets.newHashSet();

        Template template = templateBuilder.build();

        template.getOptions().as(EC2TemplateOptions.class).securityGroups(instance.getSecurityGroup());
        try {
            createdNodeMetaData = computeService.createNodesInGroup(instance.getTag(),
                    Integer.parseInt(instance.getNumber()), template);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }

        return createdNodeMetaData.stream().map(computeMetadata -> (NodeMetadataImpl) computeMetadata)
                .map(nodeMetadataImpl -> instanceCreatorFromNodeMetadata.apply(nodeMetadataImpl,
                        infrastructure.getId()))
                .collect(Collectors.toSet());

    }

}

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
                .minCores(Double.parseDouble(instance.getHardware().getMinCores()))
                .imageId(instance.getImage());

        Template template = templateBuilder.build();

        Optional.ofNullable(instance.getOptions()).ifPresent(options -> addOptions(template, options));

        Set<? extends NodeMetadata> createdNodeMetaData = Sets.newHashSet();

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

    private void addOptions(Template template, Options options) {
        Optional.ofNullable(options.getSpotPrice()).filter(spotPrice -> !spotPrice.isEmpty())
                .ifPresent(spotPrice -> template.getOptions().as(AWSEC2TemplateOptions.class)
                        .spotPrice(Float.valueOf(options.getSpotPrice())));
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

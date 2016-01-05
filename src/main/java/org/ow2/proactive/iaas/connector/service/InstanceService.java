package org.ow2.proactive.iaas.connector.service;

import java.util.Set;
import java.util.function.BiFunction;
import java.util.stream.Collectors;

import org.jclouds.compute.ComputeService;
import org.jclouds.compute.domain.NodeMetadata;
import org.jclouds.compute.domain.Template;
import org.jclouds.compute.domain.internal.NodeMetadataImpl;
import org.ow2.proactive.iaas.connector.cache.ComputeServiceCache;
import org.ow2.proactive.iaas.connector.model.Instance;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.google.common.collect.Sets;


@Service
public class InstanceService {

    @Autowired
    private InfrastructureService infrastructureService;

    @Autowired
    private ComputeServiceCache computeServiceCache;

    public Set<Instance> createInstance(Instance instance) {
        ComputeService computeService = getComputeServiceFromInfastructureName(
                instance.getInfrastructureName());

        Template template = computeService.templateBuilder().minRam(Integer.parseInt(instance.getRam()))
                .imageId(instance.getImage()).build();

        Set<? extends NodeMetadata> createdNodeMetaData = Sets.newHashSet();

        try {
            createdNodeMetaData = computeService.createNodesInGroup(instance.getName(),
                    Integer.parseInt(instance.getNumber()), template);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }

        return createdNodeMetaData.stream().map(computeMetadata -> (NodeMetadataImpl) computeMetadata)
                .map(nodeMetadataImpl -> instanceCreatorFromNodeMetadata.apply(nodeMetadataImpl,
                        instance.getInfrastructureName()))
                .collect(Collectors.toSet());

    }

    public void deleteInstance(String infrastructureName, String instanceId) {
        getComputeServiceFromInfastructureName(infrastructureName).destroyNode(instanceId);
    }

    public Set<Instance> getAllInstances(String infrastructureName) {

        return getComputeServiceFromInfastructureName(infrastructureName).listNodes().stream()
                .map(computeMetadata -> (NodeMetadataImpl) computeMetadata)
                .map(nodeMetadataImpl -> instanceCreatorFromNodeMetadata.apply(nodeMetadataImpl,
                        infrastructureName))
                .collect(Collectors.toSet());
    }

    private ComputeService getComputeServiceFromInfastructureName(String infrastructureName) {
        return computeServiceCache
                .getComputeService(infrastructureService.getInfrastructurebyName(infrastructureName));
    }

    private final BiFunction<NodeMetadataImpl, String, Instance> instanceCreatorFromNodeMetadata = (
            nodeMetadataImpl, infrastructureName) -> {
        return Instance.builder().id(nodeMetadataImpl.getId()).name(nodeMetadataImpl.getName())
                .image(nodeMetadataImpl.getImageId()).number("1")
                .ram(String.valueOf(nodeMetadataImpl.getHardware().getRam()))
                .cpu(String.valueOf(nodeMetadataImpl.getHardware().getProcessors().size()))
                .status(nodeMetadataImpl.getStatus().name()).infrastructureName(infrastructureName).build();
    };

}

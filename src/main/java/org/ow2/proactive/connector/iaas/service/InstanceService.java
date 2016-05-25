package org.ow2.proactive.connector.iaas.service;

import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;

import javax.ws.rs.NotFoundException;

import org.ow2.proactive.connector.iaas.cloud.CloudManager;
import org.ow2.proactive.connector.iaas.model.Instance;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.google.common.collect.Sets;


@Service
public class InstanceService {

    @Autowired
    private InfrastructureService infrastructureService;

    @Autowired
    private CloudManager cloudManager;

    public Set<Instance> createInstance(String infrastructureId, Instance instance) {

        return Optional.ofNullable(infrastructureService.getInfrastructure(infrastructureId))
                .map(infrastructure -> cloudManager.createInstance(infrastructure, instance))
                .orElseThrow(() -> new NotFoundException(
                    "infrastructure id  : " + infrastructureId + " does not exists"));

    }

    public Set<Instance> createInstanceAndWaitForCompletion(String infrastructureId, Instance instance) {

        Set<Instance> createdInstances = createInstance(infrastructureId, instance);
        Set<Instance> completedInstances = Sets.newHashSet();

        while (completedInstances.size() != createdInstances.size()) {
            completedInstances
                    .addAll(createdInstances.stream()
                            .filter(createdInstance -> !completedInstances.contains(completedInstances))
                            .map(createdInstance -> getInstanceById(infrastructureId,
                                    createdInstance.getId()))
                    .filter(createdInstance -> !createdInstance.getNetwork().getPublicAddresses().isEmpty())
                    .filter(createdInstance -> createdInstance.getNetwork().getPublicAddresses().iterator()
                            .next() != null).collect(Collectors.toSet()));

            try {
                Thread.sleep(1000);
                System.out.println("sleeeping");
            } catch (InterruptedException e) {

            }

        }

        return completedInstances;
    }

    public void deleteInstance(String infrastructureId, String instanceId) {
        Optional.ofNullable(infrastructureService.getInfrastructure(infrastructureId))
                .ifPresent(infrastructure -> cloudManager.deleteInstance(infrastructure, instanceId));

    }

    public void deleteInstanceByTag(String infrastructureId, String instanceTag) {
        Set<Instance> instances = getAllInstances(infrastructureId);
        instances.stream().filter(instance -> instance.getTag().equals(instanceTag))
                .forEach(instance -> cloudManager.deleteInstance(
                        infrastructureService.getInfrastructure(infrastructureId), instance.getId()));
    }

    public Set<Instance> getInstanceByTag(String infrastructureId, String instanceTag) {
        Set<Instance> instances = getAllInstances(infrastructureId);
        return instances.stream().filter(instance -> instance.getTag().equals(instanceTag))
                .collect(Collectors.toSet());
    }

    public Instance getInstanceById(String infrastructureId, String instanceId) {
        Set<Instance> instances = getAllInstances(infrastructureId);
        return instances.stream().filter(instance -> instance.getId().equals(instanceId)).findFirst()
                .orElseThrow(() -> new RuntimeException("Instance not found"));
    }

    public Set<Instance> getAllInstances(String infrastructureId) {
        return Optional.ofNullable(infrastructureService.getInfrastructure(infrastructureId))
                .map(infrastructure -> cloudManager.getAllInfrastructureInstances(infrastructure))
                .orElseThrow(() -> new NotFoundException(
                    "infrastructure id  : " + infrastructureId + " does not exists"));

    }

}

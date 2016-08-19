package org.ow2.proactive.connector.iaas.service;

import org.ow2.proactive.connector.iaas.cloud.CloudManager;
import org.ow2.proactive.connector.iaas.model.Infrastructure;
import org.ow2.proactive.connector.iaas.model.Instance;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import javax.ws.rs.NotFoundException;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;

@Service
public class InstanceService {

    @Autowired
    private InfrastructureService infrastructureService;

    @Autowired
    private CloudManager cloudManager;

    public Set<Instance> createInstance(String infrastructureId, Instance instance) {

        return Optional.ofNullable(infrastructureService.getInfrastructure(infrastructureId))
                .map(infrastructure -> cloudManager.createInstance(infrastructure, instance)).orElseThrow(
                        () -> new NotFoundException("infrastructure id  : " + infrastructureId + " does not exists"));

    }

    public void deleteInstance(String infrastructureId, String instanceId) {
        Optional.ofNullable(infrastructureService.getInfrastructure(infrastructureId))
                .ifPresent(infrastructure -> cloudManager.deleteInstance(infrastructure, instanceId));

    }

    public void deleteInstanceByTag(String infrastructureId, String instanceTag) {
        Set<Instance> instances = getAllInstances(infrastructureId);
        instances.stream().filter(instance -> instance.getTag().equals(instanceTag)).forEach(instance -> cloudManager
                .deleteInstance(infrastructureService.getInfrastructure(infrastructureId), instance.getId()));
    }

    public Set<Instance> getInstanceByTag(String infrastructureId, String instanceTag) {
        Set<Instance> instances = getAllInstances(infrastructureId);
        return instances.stream().filter(instance -> instance.getTag().equals(instanceTag)).collect(Collectors.toSet());
    }

    public Instance getInstanceById(String infrastructureId, String instanceId) {
        Set<Instance> instances = getAllInstances(infrastructureId);
        return instances.stream().filter(instance -> instance.getId().equals(instanceId)).findFirst()
                .orElseThrow(() -> new RuntimeException("Instance not found"));
    }

    public Set<Instance> getAllInstances(String infrastructureId) {
        return Optional.ofNullable(infrastructureService.getInfrastructure(infrastructureId))
                .map(infrastructure -> cloudManager.getAllInfrastructureInstances(infrastructure)).orElseThrow(
                        () -> new NotFoundException("infrastructure id  : " + infrastructureId + " does not exists"));

    }

    public String addToInstancePublicIp(String infrastructureId, String instanceId) {
        return Optional.ofNullable(infrastructureService.getInfrastructure(infrastructureId))
                .map(infrastructure -> {
                    String ip = cloudManager.addToInstancePublicIp(infrastructure, instanceId);
                    return ip;
                }).orElseThrow(
                        () -> new NotFoundException("infrastructure id  : " + infrastructureId + "does not exists")
                );
    }

    public void addInstancePublicIpByTag(String infrastructureId, String instanceTag) {
        getInstanceByTag(infrastructureId,instanceTag).forEach(
                instance -> addToInstancePublicIp(infrastructureId,instance.getId())
        );
    }

    public void removeInstancePublicIp(String infrastructureId, String instanceId) {
        Infrastructure infrastructure = Optional.ofNullable(infrastructureService.getInfrastructure(infrastructureId))
                .orElseThrow(
                        () -> new NotFoundException("infrastructure id  : " + infrastructureId + "does not exists")
        );
        cloudManager.removeInstancePublicIp(infrastructure, instanceId);
    }

    public void removeInstancePublicIpByTag(String infrastructureId, String instanceTag) {
        getInstanceByTag(infrastructureId,instanceTag).forEach(
                instance -> removeInstancePublicIp(infrastructureId,instance.getId())
        );
    }
}

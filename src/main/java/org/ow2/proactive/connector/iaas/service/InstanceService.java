package org.ow2.proactive.connector.iaas.service;

import java.util.Set;

import org.ow2.proactive.connector.iaas.cloud.CloudManager;
import org.ow2.proactive.connector.iaas.model.Instance;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;


@Service
public class InstanceService {

    @Autowired
    private InfrastructureService infrastructureService;

    @Autowired
    private CloudManager cloudManager;

    public Set<Instance> createInstance(String infrastructureId, Instance instance) {
        return cloudManager.createInstance(infrastructureService.getInfrastructure(infrastructureId),
                instance);
    }

    public void deleteInstance(String infrastructureId, String instanceId) {
        cloudManager.deleteInstance(infrastructureService.getInfrastructure(infrastructureId), instanceId);
    }

    public void deleteInstanceByTag(String infrastructureId, String instanceTag) {
        Set<Instance> instances = getAllInstances(infrastructureId);
        instances.stream().filter(instance -> instance.getTag().equals(instanceTag))
                .forEach(instance -> cloudManager.deleteInstance(
                        infrastructureService.getInfrastructure(infrastructureId), instance.getId()));
    }

    public Set<Instance> getAllInstances(String infrastructureId) {
        return cloudManager
                .getAllInfrastructureInstances(infrastructureService.getInfrastructure(infrastructureId));
    }

}

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

    public Set<Instance> createInstance(Instance instance) {
        return cloudManager.createInstance(
                infrastructureService.getInfrastructurebyName(instance.getInfrastructureName()), instance);
    }

    public void deleteInstance(String infrastructureName, String instanceId) {
        cloudManager.deleteInstance(infrastructureService.getInfrastructurebyName(infrastructureName),
                instanceId);
    }

    public Set<Instance> getAllInstances(String infrastructureName) {
        return cloudManager.getAllInfrastructureInstances(
                infrastructureService.getInfrastructurebyName(infrastructureName));
    }

}

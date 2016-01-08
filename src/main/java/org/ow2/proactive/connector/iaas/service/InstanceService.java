package org.ow2.proactive.connector.iaas.service;

import java.util.Set;

import org.ow2.proactive.connector.iaas.cache.InstanceCache;
import org.ow2.proactive.connector.iaas.cloud.CloudManager;
import org.ow2.proactive.connector.iaas.model.Infrastructure;
import org.ow2.proactive.connector.iaas.model.Instance;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;


@Service
public class InstanceService {

    @Autowired
    private InstanceCache instanceCache;

    @Autowired
    private InfrastructureService infrastructureService;

    @Autowired
    private CloudManager cloudManager;

    public Set<Instance> createInstance(String infrastructureId, Instance instance) {

        Infrastructure infrastructure = infrastructureService.getInfrastructure(infrastructureId);

        Set<Instance> instancesCreated = cloudManager.createInstance(infrastructure, instance);

        instancesCreated.stream()
                .forEach(instanceCreated -> instanceCache.registerInstance(infrastructure, instanceCreated));

        return instancesCreated;
    }

    public void deleteInstance(String infrastructureId, String instanceId) {
        Infrastructure infrastructure = infrastructureService.getInfrastructure(infrastructureId);
        Instance instance = getInstance(infrastructureId, instanceId);
        cloudManager.deleteInstance(infrastructure, instance);
        instanceCache.deleteInstance(infrastructure, instance);

    }

    public Instance getInstance(String infrastructureId, String instanceId) {
        return instanceCache.getSupportedInstancePerInfrastructure().get(infrastructureId, instanceId);
    }

    public Set<Instance> getAllInstances(String infrastructureId) {
        return cloudManager
                .getAllInfrastructureInstances(infrastructureService.getInfrastructure(infrastructureId));
    }

}

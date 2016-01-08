package org.ow2.proactive.connector.iaas.service;

import java.util.Map;

import org.ow2.proactive.connector.iaas.cache.InfrastructureCache;
import org.ow2.proactive.connector.iaas.cache.InstanceCache;
import org.ow2.proactive.connector.iaas.cloud.CloudManager;
import org.ow2.proactive.connector.iaas.model.Infrastructure;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;


@Service
public class InfrastructureService {

    @Autowired
    private InfrastructureCache infrastructureCache;

    @Autowired
    private InstanceCache instanceCache;

    @Autowired
    private CloudManager cloudManager;

    public Map<String, Infrastructure> getAllSupportedInfrastructure() {
        return infrastructureCache.getSupportedInfrastructures();
    }

    public Infrastructure registerInfrastructure(Infrastructure infrastructure) {
        infrastructureCache.registerInfrastructure(infrastructure);
        return infrastructure;
    }

    public void deleteInfrastructure(Infrastructure infrastructure) {
        cloudManager.deleteInfrastructure(infrastructure);
        instanceCache.deleteInstancesByInfrastructure(infrastructure);
        infrastructureCache.deleteInfrastructure(infrastructure);
    }

    public Infrastructure getInfrastructure(String infrastructureId) {
        return infrastructureCache.getSupportedInfrastructures().get(infrastructureId);
    }

}

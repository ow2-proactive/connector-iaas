package org.ow2.proactive.iaas.connector.service;

import java.util.Map;

import org.ow2.proactive.iaas.connector.cache.InfrastructureCache;
import org.ow2.proactive.iaas.connector.cloud.CloudManager;
import org.ow2.proactive.iaas.connector.model.Infrastructure;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;


@Service
public class InfrastructureService {

    @Autowired
    private InfrastructureCache infrastructureCache;

    @Autowired
    private CloudManager cloudManager;

    public Map<String, Infrastructure> getAllSupportedInfrastructure() {
        return infrastructureCache.getSupportedInfrastructures();
    }

    public Infrastructure registerInfrastructure(Infrastructure infrastructure) {
        infrastructureCache.registerInfrastructure(infrastructure);
        return infrastructure;
    }

    public void deleteInfrastructure(String infrastructureName) {
        cloudManager.deleteInfrastructure(getInfrastructurebyName(infrastructureName));
        infrastructureCache.deleteInfrastructure(getInfrastructurebyName(infrastructureName));
    }

    public Infrastructure getInfrastructurebyName(String infrastructureName) {
        return infrastructureCache.getSupportedInfrastructures().get(infrastructureName);
    }

    public void updateInfrastructure(String infrastructureName, Infrastructure infrastructure) {
        deleteInfrastructure(infrastructureName);
        registerInfrastructure(infrastructure);
    }

}

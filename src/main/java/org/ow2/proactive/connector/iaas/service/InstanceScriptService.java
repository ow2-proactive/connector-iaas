package org.ow2.proactive.connector.iaas.service;

import org.ow2.proactive.connector.iaas.cloud.CloudManager;
import org.ow2.proactive.connector.iaas.model.InstanceScript;
import org.ow2.proactive.connector.iaas.model.ScriptResult;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;


@Service
public class InstanceScriptService {

    @Autowired
    private InfrastructureService infrastructureService;

    @Autowired
    private CloudManager cloudManager;

    public ScriptResult executeScriptOnInstance(String infrastructureName, InstanceScript instanceScript) {
        return cloudManager.executeScript(infrastructureService.getInfrastructurebyName(infrastructureName),
                instanceScript);

    }

}

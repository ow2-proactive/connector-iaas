package org.ow2.proactive.iaas.connector.service;

import org.ow2.proactive.iaas.connector.cloud.CloudManager;
import org.ow2.proactive.iaas.connector.model.InstanceScript;
import org.ow2.proactive.iaas.connector.model.ScriptResult;
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

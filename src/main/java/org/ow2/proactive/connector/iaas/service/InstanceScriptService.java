package org.ow2.proactive.connector.iaas.service;

import java.util.List;

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

    public ScriptResult executeScriptOnInstance(String infrastructureId, String instanceId,
            InstanceScript instanceScript) {
        return cloudManager.executeScriptOnInstanceId(infrastructureService.getInfrastructure(infrastructureId),
                instanceId, instanceScript);

    }

    public List<ScriptResult> executeScriptOnInstanceTag(String infrastructureId, String instanceTag,
            InstanceScript instanceScript) {
        return cloudManager.executeScriptOnInstanceTag(
                infrastructureService.getInfrastructure(infrastructureId), instanceTag, instanceScript);

    }

}

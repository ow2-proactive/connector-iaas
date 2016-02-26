package org.ow2.proactive.connector.iaas.service;

import java.util.List;
import java.util.Optional;

import javax.ws.rs.NotFoundException;

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

        return Optional.ofNullable(infrastructureService.getInfrastructure(infrastructureId))
                .map(infrastructure -> cloudManager.executeScriptOnInstanceId(infrastructure, instanceId,
                        instanceScript))
                .orElseThrow(() -> new NotFoundException(
                    "infrastructure id  : " + infrastructureId + " does not exists"));

    }

    public List<ScriptResult> executeScriptOnInstanceTag(String infrastructureId, String instanceTag,
            InstanceScript instanceScript) {

        return Optional.ofNullable(infrastructureService.getInfrastructure(infrastructureId))
                .map(infrastructure -> cloudManager.executeScriptOnInstanceTag(infrastructure, instanceTag,
                        instanceScript))
                .orElseThrow(() -> new NotFoundException(
                    "infrastructure id  : " + infrastructureId + " does not exists"));

    }

}

package org.ow2.proactive.iaas.connector.service;

import static org.jclouds.scriptbuilder.domain.Statements.exec;

import java.util.Arrays;

import org.jclouds.compute.ComputeService;
import org.jclouds.compute.domain.ExecResponse;
import org.jclouds.scriptbuilder.ScriptBuilder;
import org.jclouds.scriptbuilder.domain.OsFamily;
import org.ow2.proactive.iaas.connector.cache.ComputeServiceCache;
import org.ow2.proactive.iaas.connector.model.InstanceScript;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;


@Service
public class InstanceScriptService {

    @Autowired
    private InfrastructureService infrastructureService;

    @Autowired
    private ComputeServiceCache computeServiceCache;

    public String runScriptOnInstance(String infrastructureName, InstanceScript instanceScript) {
        ComputeService computeService = getComputeServiceFromInfastructureName(infrastructureName);

        ScriptBuilder scriptBuilder = new ScriptBuilder();

        Arrays.stream(instanceScript.getScripts())
                .forEachOrdered(script -> scriptBuilder.addStatement(exec(script)));

        String allScriptsToExecute = scriptBuilder.render(OsFamily.UNIX);

        ExecResponse execResponse;

        try {
            execResponse = computeService.runScriptOnNode(instanceScript.getInstanceId(),
                    allScriptsToExecute);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }

        return execResponse.getOutput();

    }

    private ComputeService getComputeServiceFromInfastructureName(String infrastructureName) {
        return computeServiceCache
                .getComputeService(infrastructureService.getInfrastructurebyName(infrastructureName));
    }

}

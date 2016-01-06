package org.ow2.proactive.iaas.connector.cloud;

import org.ow2.proactive.iaas.connector.model.Instance;
import org.ow2.proactive.iaas.connector.model.InstanceScript;
import org.ow2.proactive.iaas.connector.model.ScriptResult;


public interface CloudProvider {

    public ScriptResult executeScript(String infrastructureName, InstanceScript instanceScript);

    public ScriptResult createInstance(Instance instance);

    public ScriptResult deleteInstance(String infrastructureName, String instanceId);

    public ScriptResult getAllInfrastructureInstances(String infrastructureName);

}

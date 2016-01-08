package org.ow2.proactive.connector.iaas.cloud.provider;

import java.util.Set;

import org.ow2.proactive.connector.iaas.model.Image;
import org.ow2.proactive.connector.iaas.model.Infrastructure;
import org.ow2.proactive.connector.iaas.model.Instance;
import org.ow2.proactive.connector.iaas.model.InstanceScript;
import org.ow2.proactive.connector.iaas.model.ScriptResult;


public interface CloudProvider {

    public Set<Instance> createInstance(Infrastructure infrastructure, Instance instance);

    public void deleteInstance(Infrastructure infrastructure, String instanceId);

    public Set<Instance> getAllInfrastructureInstances(Infrastructure infrastructure);

    public ScriptResult executeScript(Infrastructure infrastructure, String instanceId,
            InstanceScript instanceScript);

    public Set<Image> getAllImages(Infrastructure infrastructure);

    public void deleteInfrastructure(Infrastructure infrastructure);

}

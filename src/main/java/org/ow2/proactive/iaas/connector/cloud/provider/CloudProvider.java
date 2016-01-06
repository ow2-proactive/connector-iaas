package org.ow2.proactive.iaas.connector.cloud.provider;

import java.util.Set;

import org.ow2.proactive.iaas.connector.model.Image;
import org.ow2.proactive.iaas.connector.model.Infrastructure;
import org.ow2.proactive.iaas.connector.model.Instance;
import org.ow2.proactive.iaas.connector.model.InstanceScript;
import org.ow2.proactive.iaas.connector.model.ScriptResult;


public interface CloudProvider {

    public Set<Instance> createInstance(Infrastructure infrastructure, Instance instance);

    public void deleteInstance(Infrastructure infrastructure, String instanceId);

    public Set<Instance> getAllInfrastructureInstances(Infrastructure infrastructure);

    public ScriptResult executeScript(Infrastructure infrastructure, InstanceScript instanceScript);

    public Set<Image> getAllImages(Infrastructure infrastructure);

    public void deleteInfrastructure(Infrastructure infrastructure);

}

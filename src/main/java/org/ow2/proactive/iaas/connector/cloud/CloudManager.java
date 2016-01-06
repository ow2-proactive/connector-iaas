package org.ow2.proactive.iaas.connector.cloud;

import java.util.Set;

import org.ow2.proactive.iaas.connector.cloud.provider.CloudProvider;
import org.ow2.proactive.iaas.connector.model.Image;
import org.ow2.proactive.iaas.connector.model.Infrastructure;
import org.ow2.proactive.iaas.connector.model.Instance;
import org.ow2.proactive.iaas.connector.model.InstanceScript;
import org.ow2.proactive.iaas.connector.model.ScriptResult;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;


@Service
public class CloudManager {

    @Autowired
    private CloudProvider defaultCloudProvider;

    public Set<Instance> createInstance(Infrastructure infrastructure, Instance instance) {
        return defaultCloudProvider.createInstance(infrastructure, instance);
    }

    public void deleteInstance(Infrastructure infrastructure, String instanceId) {
        defaultCloudProvider.deleteInstance(infrastructure, instanceId);
    }

    public void deleteInfrastructure(Infrastructure infrastructure) {
        defaultCloudProvider.deleteInfrastructure(infrastructure);
    }

    public Set<Instance> getAllInfrastructureInstances(Infrastructure infrastructure) {
        return defaultCloudProvider.getAllInfrastructureInstances(infrastructure);
    }

    public ScriptResult executeScript(Infrastructure infrastructure, InstanceScript instanceScript) {
        return defaultCloudProvider.executeScript(infrastructure, instanceScript);
    }

    public Set<Image> getAllImages(Infrastructure infrastructure) {
        return defaultCloudProvider.getAllImages(infrastructure);
    }

}

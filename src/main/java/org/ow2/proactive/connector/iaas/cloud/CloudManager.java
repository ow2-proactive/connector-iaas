package org.ow2.proactive.connector.iaas.cloud;

import java.util.Set;

import org.ow2.proactive.connector.iaas.cloud.provider.CloudProvider;
import org.ow2.proactive.connector.iaas.model.Image;
import org.ow2.proactive.connector.iaas.model.Infrastructure;
import org.ow2.proactive.connector.iaas.model.Instance;
import org.ow2.proactive.connector.iaas.model.InstanceScript;
import org.ow2.proactive.connector.iaas.model.ScriptResult;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;


@Service
public class CloudManager {

    @Autowired
    private CloudProvider defaultCloudProvider;

    public Set<Instance> createInstance(Infrastructure infrastructure, Instance instance) {
        return defaultCloudProvider.createInstance(infrastructure, instance);
    }

    public void deleteInstance(Infrastructure infrastructure, Instance instance) {
        defaultCloudProvider.deleteInstance(infrastructure, instance);
    }

    public void deleteInfrastructure(Infrastructure infrastructure) {
        defaultCloudProvider.deleteInfrastructure(infrastructure);
    }

    public Set<Instance> getAllInfrastructureInstances(Infrastructure infrastructure) {
        return defaultCloudProvider.getAllInfrastructureInstances(infrastructure);
    }

    public ScriptResult executeScript(Infrastructure infrastructure, Instance instance,
            InstanceScript instanceScript) {
        return defaultCloudProvider.executeScript(infrastructure, instance, instanceScript);
    }

    public Set<Image> getAllImages(Infrastructure infrastructure) {
        return defaultCloudProvider.getAllImages(infrastructure);
    }

}

package org.ow2.proactive.connector.iaas.cloud;

import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.function.Function;
import java.util.stream.Collectors;

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

    private Map<String, CloudProvider> cloudProviderPerType;

    @Autowired
    public CloudManager(List<CloudProvider> cloudProviders) {
        cloudProviderPerType = cloudProviders.stream()
                .collect(Collectors.toMap(CloudProvider::getType, Function.identity()));
    }

    public Set<Instance> createInstance(Infrastructure infrastructure, Instance instance) {
        return cloudProviderPerType.get(infrastructure.getType()).createInstance(infrastructure, instance);
    }

    public void deleteInstance(Infrastructure infrastructure, String instanceId) {
        cloudProviderPerType.get(infrastructure.getType()).deleteInstance(infrastructure, instanceId);
    }

    public void deleteInfrastructure(Infrastructure infrastructure) {
        cloudProviderPerType.get(infrastructure.getType()).deleteInfrastructure(infrastructure);
    }

    public Set<Instance> getAllInfrastructureInstances(Infrastructure infrastructure) {
        return cloudProviderPerType.get(infrastructure.getType())
                .getAllInfrastructureInstances(infrastructure);
    }

    public ScriptResult executeScriptOnInstanceId(Infrastructure infrastructure, String instanceId,
            InstanceScript instanceScript) {
        return cloudProviderPerType.get(infrastructure.getType()).executeScriptOnInstanceId(infrastructure,
                instanceId, instanceScript);
    }

    public List<ScriptResult> executeScriptOnInstanceTag(Infrastructure infrastructure, String instanceTag,
            InstanceScript instanceScript) {
        return cloudProviderPerType.get(infrastructure.getType()).executeScriptOnInstanceTag(infrastructure,
                instanceTag, instanceScript);
    }

    public Set<Image> getAllImages(Infrastructure infrastructure) {
        return cloudProviderPerType.get(infrastructure.getType()).getAllImages(infrastructure);
    }

    public String addToInstancePublicIp(Infrastructure infrastructure, String instanceId){
        return cloudProviderPerType.get(infrastructure.getType()).addToInstancePublicIp(infrastructure,instanceId);
    }
}

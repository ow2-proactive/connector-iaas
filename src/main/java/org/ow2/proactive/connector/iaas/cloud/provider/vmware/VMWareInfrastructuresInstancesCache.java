package org.ow2.proactive.connector.iaas.cloud.provider.vmware;

import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

import org.springframework.stereotype.Component;

import com.google.common.collect.Sets;


@Component
public class VMWareInfrastructuresInstancesCache {

    private final Map<String, Set<String>> instancesIdPerInfrastructureId;

    public VMWareInfrastructuresInstancesCache() {
        instancesIdPerInfrastructureId = new ConcurrentHashMap<String, Set<String>>();
    }

    public boolean infrastructureContainsInstanceId(String infrastructureId, String instanceId) {
        Set<String> instancesIds = instancesIdPerInfrastructureId.getOrDefault(infrastructureId,
                Sets.newHashSet());
        return instancesIds.contains(instanceId);
    }

    public void removeInfrastructure(String infrastructureId) {
        instancesIdPerInfrastructureId.remove(infrastructureId);
    }

    public void removeInstanceIdFromInfrastructure(String infrastructureId, String instanceId) {
        Set<String> instancesIds = instancesIdPerInfrastructureId.getOrDefault(infrastructureId,
                Sets.newHashSet());
        instancesIds.remove(instanceId);
    }

    public void addInstanceIdToInfrastructure(String infrastructureId, String instanceId) {
        Set<String> instancesIds = instancesIdPerInfrastructureId.getOrDefault(infrastructureId,
                Sets.newHashSet());
        instancesIds.add(instanceId);
        instancesIdPerInfrastructureId.put(infrastructureId, instancesIds);
    }

}

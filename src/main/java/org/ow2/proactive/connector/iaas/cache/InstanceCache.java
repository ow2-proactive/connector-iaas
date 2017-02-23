/*
 * ProActive Parallel Suite(TM):
 * The Open Source library for parallel and distributed
 * Workflows & Scheduling, Orchestration, Cloud Automation
 * and Big Data Analysis on Enterprise Grids & Clouds.
 *
 * Copyright (c) 2007 - 2017 ActiveEon
 * Contact: contact@activeeon.com
 *
 * This library is free software: you can redistribute it and/or
 * modify it under the terms of the GNU Affero General Public License
 * as published by the Free Software Foundation: version 3 of
 * the License.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Affero General Public License for more details.
 *
 * You should have received a copy of the GNU Affero General Public License
 * along with this program. If not, see <http://www.gnu.org/licenses/>.
 *
 * If needed, contact us to obtain a release under GPL Version 2 or 3
 * or a different license than the AGPL.
 */
package org.ow2.proactive.connector.iaas.cache;

import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

import org.ow2.proactive.connector.iaas.model.Infrastructure;
import org.ow2.proactive.connector.iaas.model.Instance;
import org.springframework.stereotype.Component;

import com.google.common.collect.Sets;

import jersey.repackaged.com.google.common.collect.ImmutableMap;
import lombok.Getter;


@Component
public class InstanceCache {
    @Getter
    private volatile ImmutableMap<String, Set<Instance>> createdInstances;

    public InstanceCache() {
        this.createdInstances = ImmutableMap.of();
    }

    public void registerInfrastructureInstances(Infrastructure infrastructure, Set<Instance> instances) {
        Map<String, Set<Instance>> tempInstances = cloneCreatedInstances();
        tempInstances.putIfAbsent(infrastructure.getId(), Sets.newHashSet());
        Set<Instance> cachedInstances = tempInstances.get(infrastructure.getId());
        cachedInstances.addAll(instances);
        tempInstances.put(infrastructure.getId(), cachedInstances);
        createdInstances = ImmutableMap.copyOf(tempInstances);
    }

    public void deleteInfrastructureInstance(Infrastructure infrastructure, Instance instance) {
        Map<String, Set<Instance>> tempInstances = cloneCreatedInstances();
        tempInstances.get(infrastructure.getId()).remove(instance);
        createdInstances = ImmutableMap.copyOf(tempInstances);
    }

    public void deleteAllInfrastructureInstances(Infrastructure infrastructure) {
        Map<String, Set<Instance>> tempInstances = cloneCreatedInstances();
        tempInstances.put(infrastructure.getId(), Sets.newHashSet());
        createdInstances = ImmutableMap.copyOf(tempInstances);
    }

    public void deleteInfrastructure(Infrastructure infrastructure) {
        Map<String, Set<Instance>> tempInstances = cloneCreatedInstances();
        tempInstances.remove(infrastructure.getId());
        createdInstances = ImmutableMap.copyOf(tempInstances);
    }

    private Map<String, Set<Instance>> cloneCreatedInstances() {
        return createdInstances.entrySet().stream().collect(Collectors.toMap(Map.Entry::getKey, Map.Entry::getValue));
    }
}

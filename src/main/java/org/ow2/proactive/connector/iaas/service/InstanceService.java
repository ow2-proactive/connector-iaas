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
package org.ow2.proactive.connector.iaas.service;

import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;

import javax.ws.rs.NotFoundException;

import org.ow2.proactive.connector.iaas.cache.InstanceCache;
import org.ow2.proactive.connector.iaas.cloud.CloudManager;
import org.ow2.proactive.connector.iaas.model.Infrastructure;
import org.ow2.proactive.connector.iaas.model.Instance;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;


@Service
public class InstanceService {

    @Autowired
    private InstanceCache instanceCache;

    @Autowired
    private InfrastructureService infrastructureService;

    @Autowired
    private CloudManager cloudManager;

    public Set<Instance> createInstance(String infrastructureId, Instance instance) {

        Set<Instance> instancesCreated = Optional.ofNullable(infrastructureService.getInfrastructure(infrastructureId))
                                                 .map(infrastructure -> cloudManager.createInstance(infrastructure,
                                                                                                    instance))
                                                 .orElseThrow(() -> new NotFoundException("infrastructure id : " +
                                                                                          infrastructureId +
                                                                                          " does not exists"));

        Optional.ofNullable(infrastructureService.getInfrastructure(infrastructureId))
                .ifPresent(infrastructure -> instanceCache.registerInfrastructureInstances(infrastructure,
                                                                                           instancesCreated));

        return instancesCreated;
    }

    public void deleteCreatedInstances(String infrastructureId) {
        Optional.ofNullable(infrastructureService.getInfrastructure(infrastructureId)).ifPresent(infrastructure -> {
            instanceCache.getCreatedInstances()
                         .get(infrastructure.getId())
                         .forEach(instance -> cloudManager.deleteInstance(infrastructure, instance.getId()));
            instanceCache.deleteAllInfrastructureInstances(infrastructure);
        });
    }

    public void deleteInstance(String infrastructureId, String instanceId) {
        Optional.ofNullable(infrastructureService.getInfrastructure(infrastructureId)).ifPresent(infrastructure -> {
            cloudManager.deleteInstance(infrastructure, instanceId);
            instanceCache.getCreatedInstances()
                         .get(infrastructure.getId())
                         .stream()
                         .filter(instance -> instance.getId().equals(instanceId))
                         .findAny()
                         .ifPresent(instance -> instanceCache.deleteInfrastructureInstance(infrastructure, instance));
        });
    }

    public void deleteInstanceByTag(String infrastructureId, String instanceTag) {
        getAllInstances(infrastructureId).stream()
                                         .filter(instance -> instance.getTag().equals(instanceTag))
                                         .forEach(instance -> {
                                             Infrastructure infrastructure = infrastructureService.getInfrastructure(infrastructureId);
                                             cloudManager.deleteInstance(infrastructure, instance.getId());
                                             instanceCache.getCreatedInstances()
                                                          .get(infrastructure.getId())
                                                          .stream()
                                                          .filter(createdInstance -> instance.getId()
                                                                                             .equals(createdInstance.getId()))
                                                          .findAny()
                                                          .ifPresent(createdInstance -> instanceCache.deleteInfrastructureInstance(infrastructure,
                                                                                                                                   instance));
                                         });
    }

    public Set<Instance> getInstanceByTag(String infrastructureId, String instanceTag) {
        return getAllInstances(infrastructureId).stream()
                                                .filter(instance -> instance.getTag().equals(instanceTag))
                                                .collect(Collectors.toSet());
    }

    public Instance getInstanceById(String infrastructureId, String instanceId) {
        Set<Instance> instances = getAllInstances(infrastructureId);
        return instances.stream()
                        .filter(instance -> instance.getId().equals(instanceId))
                        .findFirst()
                        .orElseThrow(() -> new RuntimeException("Instance not found"));
    }

    public Set<Instance> getAllInstances(String infrastructureId) {
        return Optional.ofNullable(infrastructureService.getInfrastructure(infrastructureId))
                       .map(infrastructure -> cloudManager.getAllInfrastructureInstances(infrastructure))
                       .orElseThrow(() -> new NotFoundException("infrastructure id  : " + infrastructureId +
                                                                " does not exists"));

    }

    public Set<Instance> getCreatedInstances(String infrastructureId) {
        return instanceCache.getCreatedInstances().get(infrastructureId);
    }

    public String addToInstancePublicIp(String infrastructureId, String instanceId) {
        return Optional.ofNullable(infrastructureService.getInfrastructure(infrastructureId))
                       .map(infrastructure -> cloudManager.addToInstancePublicIp(infrastructure, instanceId))
                       .orElseThrow(() -> new NotFoundException("infrastructure id  : " + infrastructureId +
                                                                "does not exists"));
    }

    public void addInstancePublicIpByTag(String infrastructureId, String instanceTag) {
        getInstanceByTag(infrastructureId,
                         instanceTag).forEach(instance -> addToInstancePublicIp(infrastructureId, instance.getId()));
    }

    public void removeInstancePublicIp(String infrastructureId, String instanceId) {
        Infrastructure infrastructure = Optional.ofNullable(infrastructureService.getInfrastructure(infrastructureId))
                                                .orElseThrow(() -> new NotFoundException("infrastructure id  : " +
                                                                                         infrastructureId +
                                                                                         "does not exists"));
        cloudManager.removeInstancePublicIp(infrastructure, instanceId);
    }

    public void removeInstancePublicIpByTag(String infrastructureId, String instanceTag) {
        getInstanceByTag(infrastructureId,
                         instanceTag).forEach(instance -> removeInstancePublicIp(infrastructureId, instance.getId()));
    }
}

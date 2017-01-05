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

import org.ow2.proactive.connector.iaas.cloud.CloudManager;
import org.ow2.proactive.connector.iaas.model.Infrastructure;
import org.ow2.proactive.connector.iaas.model.Instance;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;


@Service
public class InstanceService {

    @Autowired
    private InfrastructureService infrastructureService;

    @Autowired
    private CloudManager cloudManager;

    public Set<Instance> createInstance(String infrastructureId, Instance instance) {

        return Optional.ofNullable(infrastructureService.getInfrastructure(infrastructureId))
                       .map(infrastructure -> cloudManager.createInstance(infrastructure, instance))
                       .orElseThrow(() -> new NotFoundException("infrastructure id  : " + infrastructureId +
                                                                " does not exists"));

    }

    public void deleteInstance(String infrastructureId, String instanceId) {
        Optional.ofNullable(infrastructureService.getInfrastructure(infrastructureId))
                .ifPresent(infrastructure -> cloudManager.deleteInstance(infrastructure, instanceId));

    }

    public void deleteInstanceByTag(String infrastructureId, String instanceTag) {
        Set<Instance> instances = getAllInstances(infrastructureId);
        instances.stream()
                 .filter(instance -> instance.getTag().equals(instanceTag))
                 .forEach(instance -> cloudManager.deleteInstance(infrastructureService.getInfrastructure(infrastructureId),
                                                                  instance.getId()));
    }

    public Set<Instance> getInstanceByTag(String infrastructureId, String instanceTag) {
        Set<Instance> instances = getAllInstances(infrastructureId);
        return instances.stream().filter(instance -> instance.getTag().equals(instanceTag)).collect(Collectors.toSet());
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

    public String addToInstancePublicIp(String infrastructureId, String instanceId) {
        return Optional.ofNullable(infrastructureService.getInfrastructure(infrastructureId)).map(infrastructure -> {
            String ip = cloudManager.addToInstancePublicIp(infrastructure, instanceId);
            return ip;
        }).orElseThrow(() -> new NotFoundException("infrastructure id  : " + infrastructureId + "does not exists"));
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

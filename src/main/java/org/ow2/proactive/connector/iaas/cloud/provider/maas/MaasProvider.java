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
package org.ow2.proactive.connector.iaas.cloud.provider.maas;

import java.util.List;
import java.util.Set;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Future;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

import javax.ws.rs.NotSupportedException;

import org.apache.log4j.Logger;
import org.ow2.proactive.connector.iaas.cloud.TagManager;
import org.ow2.proactive.connector.iaas.cloud.provider.CloudProvider;
import org.ow2.proactive.connector.iaas.model.Hardware;
import org.ow2.proactive.connector.iaas.model.Image;
import org.ow2.proactive.connector.iaas.model.Infrastructure;
import org.ow2.proactive.connector.iaas.model.Instance;
import org.ow2.proactive.connector.iaas.model.InstanceScript;
import org.ow2.proactive.connector.iaas.model.Network;
import org.ow2.proactive.connector.iaas.model.ScriptResult;
import org.ow2.proactive.connector.iaas.model.Tag;
import org.ow2.proactive.connector.maas.MaasClient;
import org.ow2.proactive.connector.maas.data.CommissioningScript;
import org.ow2.proactive.connector.maas.data.Machine;
import org.ow2.proactive.connector.maas.polling.MaasClientPollingService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import com.google.common.collect.Lists;

import lombok.Getter;

/**
 * @author Vicent Kherbache
 * @since 09/01/17
 */
@Component
public class MaasProvider implements CloudProvider {

    private final Logger logger = Logger.getLogger(MaasProvider.class);

    @Getter
    private final String type = "maas";

    @Autowired
    private TagManager tagManager;

    @Autowired
    private MaasProviderClientCache maasProviderClientCache;

    @Override
    public Set<Instance> createInstance(Infrastructure infrastructure, Instance instance) {

        MaasClient maasClient = maasProviderClientCache.getMaasClient(infrastructure);

        // Retrieve and convert the list of tags
        List<org.ow2.proactive.connector.maas.data.Tag> tags = convertIaasTagsToMaasTags(tagManager.retrieveAllTags(instance.getOptions()));

        // Initialize MAAS deployment polling
        MaasClientPollingService maasPollingService = new MaasClientPollingService(maasClient, Integer.valueOf(instance.getNumber()));

        // Start deployment(s) by ID or by resources
        List<Future<Machine>> futureMachines;
        if (instance.getId() != null) {
            futureMachines = IntStream.rangeClosed(1, Integer.valueOf(instance.getNumber()))
                    .mapToObj(instanceIndexStartAt1 -> maasPollingService.deployMachine(instance.getId(), instance.getInitScript().getScripts()[0], tags))
                    .collect(Collectors.toList());
        }
        else {
            futureMachines = IntStream.rangeClosed(1, Integer.valueOf(instance.getNumber()))
                    .mapToObj(instanceIndexStartAt1 -> maasPollingService.deployMachine(Integer.valueOf(instance.getHardware().getMinCores()), Integer.valueOf(instance.getHardware().getMinRam()), "", tags))
                    .collect(Collectors.toList());
        }

        // Retrieve futures (blocking calls)
        Set<Instance> instances = futureMachines.stream().map(futureMachine -> {
            try {
                return futureMachine.get();
            } catch (InterruptedException | ExecutionException e) {
                e.printStackTrace();
                return null;
            }
        })
        .map(this::getInstanceFromMachine)
        .collect(Collectors.toSet());

        // Kill polling timeout tasks
        maasPollingService.shutdown();

        return instances;
    }

    @Override
    public void deleteInstance(Infrastructure infrastructure, String instanceId) {
        if (!maasProviderClientCache.getMaasClient(infrastructure).releaseMachineById(instanceId)) {
            throw new RuntimeException("ERROR when deleting MAAS instance : " + instanceId);
        }
    }

    private Instance getInstanceFromMachine(Infrastructure infrastructure, Machine machine) {

        return Instance.builder().id(machine.getSystemId()).tag(machine.getHostname())
                .number("1").hardware(Hardware.builder().minCores(machine.getCpuCount().toString())
                                .minRam(machine.getMemory().toString()).type(machine.getNodeTypeName()).build())
                .network(Network.builder().publicAddresses(Sets.newHashSet(machine.getIpAddresses())).build())
                .status(machine.getStatusMessage()).build();
    }

    @Override
    public ScriptResult executeScriptOnInstanceId(Infrastructure infrastructure, String instanceId, InstanceScript instanceScript) {

        ScriptResult scriptResult = new ScriptResult(instanceId, "", "");

        StringBuilder script = new StringBuilder();
        script.append("#!/bin/bash\n");
        for (int i = 0; i < instanceScript.getScripts().length; i++) {
            script.append("\n");
            script.append(instanceScript.getScripts()[i]);
        }

        CommissioningScript maasScript = maasProviderClientCache.getMaasClient(infrastructure)
                .postCommissioningScript(script.toString().getBytes(), instanceScript.getName());

        if (maasScript == null) {
            return scriptResult.withError("Unable to upload script " +  instanceScript.getName());
        }
        return scriptResult.withOutput(maasScript.getResourceUri());
    }

    @Override
    public List<ScriptResult> executeScriptOnInstanceTag(Infrastructure infrastructure, String instanceTag, InstanceScript instanceScript) {

        return maasProviderClientCache.getMaasClient(infrastructure).getMachines().stream()
                .filter(machine -> machine.getHostname().equals(instanceTag))
                .map(machine -> executeScriptOnInstanceId(infrastructure, machine.getSystemId(), instanceScript))
                .collect(Collectors.toList());
    }

    @Override
    public Set<Image> getAllImages(Infrastructure infrastructure) {
        throw new NotSupportedException("Operation not supported for MAAS");
    }

    @Override
    public void deleteInfrastructure(Infrastructure infrastructure) {
        // Remove only DESIRED instances
        infrastructure.getInstances().stream()
                .map(Instance::getId).forEach(id -> deleteInstance(infrastructure, id));
        maasProviderClientCache.removeMaasClient(infrastructure);
    }

    @Override
    public String addToInstancePublicIp(Infrastructure infrastructure, String instanceId) {
        throw new NotSupportedException("Operation not supported for MAAS");
    }

    @Override
    public void removeInstancePublicIp(Infrastructure infrastructure, String instanceId) {
        throw new NotSupportedException("Operation not supported for MAAS");
    }
}

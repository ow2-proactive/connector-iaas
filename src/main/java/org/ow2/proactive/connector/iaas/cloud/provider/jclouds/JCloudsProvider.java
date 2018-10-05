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
package org.ow2.proactive.connector.iaas.cloud.provider.jclouds;

import static org.jclouds.compute.predicates.NodePredicates.runningInGroup;
import static org.jclouds.scriptbuilder.domain.Statements.exec;

import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;

import org.jclouds.compute.ComputeService;
import org.jclouds.compute.domain.ComputeMetadata;
import org.jclouds.compute.domain.ExecResponse;
import org.jclouds.compute.domain.NodeMetadata;
import org.jclouds.compute.domain.internal.NodeMetadataImpl;
import org.jclouds.compute.options.RunScriptOptions;
import org.jclouds.scriptbuilder.ScriptBuilder;
import org.jclouds.scriptbuilder.domain.OsFamily;
import org.ow2.proactive.connector.iaas.cloud.TagManager;
import org.ow2.proactive.connector.iaas.cloud.provider.CloudProvider;
import org.ow2.proactive.connector.iaas.model.Hardware;
import org.ow2.proactive.connector.iaas.model.Image;
import org.ow2.proactive.connector.iaas.model.Infrastructure;
import org.ow2.proactive.connector.iaas.model.Instance;
import org.ow2.proactive.connector.iaas.model.InstanceCredentials;
import org.ow2.proactive.connector.iaas.model.InstanceScript;
import org.ow2.proactive.connector.iaas.model.Network;
import org.ow2.proactive.connector.iaas.model.ScriptResult;
import org.ow2.proactive.connector.iaas.model.Tag;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import com.google.common.collect.Lists;

import lombok.Getter;
import lombok.Setter;
import lombok.extern.log4j.Log4j2;


@Component
@Log4j2
public abstract class JCloudsProvider implements CloudProvider {

    @Autowired
    private JCloudsComputeServiceCache jCloudsComputeServiceCache;

    @Autowired
    private TagManager tagManager;

    /**
     * By default, the login that will be used to connect to the instances
     * and launch the script will be 'admin'. This default can be overriden
     * in the application.properties file.
     */
    @Getter
    @Setter
    @Value("${connector-iaas.vm-user-login:admin}")
    private String vmUserLogin;

    protected abstract RunScriptOptions getRunScriptOptionsWithCredentials(InstanceCredentials credentials);

    @Override
    public void deleteInstance(Infrastructure infrastructure, String instanceId) {
        getComputeServiceFromInfastructure(infrastructure).destroyNode(instanceId);

    }

    @Override
    public Set<Instance> getAllInfrastructureInstances(Infrastructure infrastructure) {
        return createInstancesFromNodes(getAllNodes(infrastructure));
    }

    private Set<Instance> createInstancesFromNodes(Set<? extends ComputeMetadata> nodes) {
        return nodes.stream()
                    .map(computeMetadata -> (NodeMetadataImpl) computeMetadata)
                    .map(this::createInstanceFromNode)
                    .collect(Collectors.toSet());
    }

    @Override
    public Set<Instance> getCreatedInfrastructureInstances(Infrastructure infrastructure) {
        Tag connectorIaasTag = tagManager.getConnectorIaasTag();
        return createInstancesFromNodes(getAllNodes(infrastructure).stream()
                                                                   .filter(node -> node.getUserMetadata()
                                                                                       .keySet()
                                                                                       .contains(connectorIaasTag.getKey()) &&
                                                                                   node.getUserMetadata()
                                                                                       .get(connectorIaasTag.getKey())
                                                                                       .equals(connectorIaasTag.getValue()))
                                                                   .collect(Collectors.toSet()));
    }

    private Set<? extends ComputeMetadata> getAllNodes(Infrastructure infrastructure) {
        return getComputeServiceFromInfastructure(infrastructure).listNodes();
    }

    @Override
    public List<ScriptResult> executeScriptOnInstanceId(Infrastructure infrastructure, String instanceId,
            InstanceScript instanceScript) {
        ExecResponse execResponse;
        RunScriptOptions runScriptOptions = null;
        try {
            String scriptToExecuteString = buildScriptToExecuteString(instanceScript);
            runScriptOptions = buildScriptOptionsWithInstanceId(instanceScript, instanceId, infrastructure);
            execResponse = getComputeServiceFromInfastructure(infrastructure).runScriptOnNode(instanceId,
                                                                                              scriptToExecuteString,
                                                                                              runScriptOptions);
        } catch (Exception e) {
            log.error("Script cannot be run on instance with id: " + instanceId + ". RunScriptOptions=" +
                      runScriptOptions, e);
            throw new RuntimeException("Script cannot be run on instance with id: " + instanceId, e);
        }

        return Lists.newArrayList(new ScriptResult(instanceId, execResponse.getOutput(), execResponse.getError()));
    }

    @Override
    public List<ScriptResult> executeScriptOnInstanceTag(Infrastructure infrastructure, String instanceTag,
            InstanceScript instanceScript) {

        Map<? extends NodeMetadata, ExecResponse> execResponses;
        RunScriptOptions runScriptOptions = null;
        try {
            String scriptToExecuteString = buildScriptToExecuteString(instanceScript);
            runScriptOptions = buildScriptOptionsWithInstanceTag(instanceScript, instanceTag, infrastructure);
            execResponses = getComputeServiceFromInfastructure(infrastructure).runScriptOnNodesMatching(runningInGroup(instanceTag),
                                                                                                        scriptToExecuteString,
                                                                                                        runScriptOptions);
        } catch (Exception e) {
            log.error("Script cannot be run on instance with tag: " + instanceTag + ". RunScriptOptions=" +
                      runScriptOptions, e);
            throw new RuntimeException("Script cannot be run on instances with tag: " + instanceTag, e);
        }

        return execResponses.entrySet()
                            .stream()
                            .map(entry -> new ScriptResult(entry.getKey().getId(),
                                                           entry.getValue().getOutput(),
                                                           entry.getValue().getError()))
                            .collect(Collectors.toList());

    }

    @Override
    public Set<Image> getAllImages(Infrastructure infrastructure) {
        return getComputeServiceFromInfastructure(infrastructure).listImages()
                                                                 .stream()
                                                                 .map(it -> Image.builder()
                                                                                 .id(it.getId())
                                                                                 .name(it.getName())
                                                                                 .build())
                                                                 .collect(Collectors.toSet());

    }

    @Override
    public void deleteInfrastructure(Infrastructure infrastructure) {
        jCloudsComputeServiceCache.removeComputeService(infrastructure);
    }

    protected final Instance createInstanceFromNode(NodeMetadataImpl nodeMetadataImpl) {
        return Instance.builder()
                       .id(nodeMetadataImpl.getId())
                       .tag(Optional.ofNullable(nodeMetadataImpl.getName()).orElse(""))
                       .image(nodeMetadataImpl.getImageId())
                       .number("1")
                       .hardware(Optional.ofNullable(nodeMetadataImpl.getHardware())
                                         .map(hardware -> Hardware.builder()
                                                                  .minRam(String.valueOf(nodeMetadataImpl.getHardware()
                                                                                                         .getRam()))
                                                                  .minCores(String.valueOf(nodeMetadataImpl.getHardware()
                                                                                                           .getProcessors()
                                                                                                           .size()))
                                                                  .type(nodeMetadataImpl.getHardware().getType().name())
                                                                  .build())
                                         .orElse(new Hardware()))
                       .network(Network.builder()
                                       .publicAddresses(Lists.newArrayList(nodeMetadataImpl.getPublicAddresses()))
                                       .privateAddresses(Lists.newArrayList(nodeMetadataImpl.getPrivateAddresses()))
                                       .build())
                       .status(nodeMetadataImpl.getStatus().name())
                       .build();
    }

    protected ComputeService getComputeServiceFromInfastructure(Infrastructure infrastructure) {
        return jCloudsComputeServiceCache.getComputeService(infrastructure);
    }

    protected String buildScriptToExecuteString(InstanceScript instanceScript) {

        return Optional.ofNullable(instanceScript).map(scriptToRun -> {
            ScriptBuilder scriptBuilder = new ScriptBuilder();
            Arrays.stream(scriptToRun.getScripts()).forEachOrdered(script -> scriptBuilder.addStatement(exec(script)));
            return scriptBuilder.render(OsFamily.UNIX);
        }).orElse("");

    }

    private RunScriptOptions buildScriptOptionsWithInstanceId(InstanceScript instanceScript, String instanceId,
            Infrastructure infrastructure) {
        return Optional.ofNullable(instanceScript.getCredentials())
                       .map(this::getRunScriptOptionsWithCredentials)
                       .orElseGet(() -> getDefaultRunScriptOptionsUsingInstanceId(instanceId, infrastructure));
    }

    private RunScriptOptions buildScriptOptionsWithInstanceTag(InstanceScript instanceScript, String instanceTag,
            Infrastructure infrastructure) {
        return Optional.ofNullable(instanceScript.getCredentials())
                       .map(this::getRunScriptOptionsWithCredentials)
                       .orElseGet(() -> getDefaultRunScriptOptionsUsingInstanceTag(instanceTag, infrastructure));
    }

    /**
     * @return scripts options, based on the instance identifier, that can be
     * specific to concrete implementations
     */
    protected RunScriptOptions getDefaultRunScriptOptionsUsingInstanceId(String instanceId,
            Infrastructure infrastructure) {
        return RunScriptOptions.NONE;
    }

    /**
     * @return scripts options, based on instances tag, that can be specific
     * to concrete implementations
     */
    protected RunScriptOptions getDefaultRunScriptOptionsUsingInstanceTag(String instanceTag,
            Infrastructure infrastructure) {
        return RunScriptOptions.NONE;
    }

}

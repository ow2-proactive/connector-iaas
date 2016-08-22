package org.ow2.proactive.connector.iaas.cloud.provider.jclouds;

import static org.jclouds.compute.predicates.NodePredicates.runningInGroup;
import static org.jclouds.scriptbuilder.domain.Statements.exec;

import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.function.BiFunction;
import java.util.stream.Collectors;

import org.jclouds.compute.ComputeService;
import org.jclouds.compute.domain.ExecResponse;
import org.jclouds.compute.domain.NodeMetadata;
import org.jclouds.compute.domain.internal.NodeMetadataImpl;
import org.jclouds.compute.options.RunScriptOptions;
import org.jclouds.domain.LoginCredentials;
import org.jclouds.scriptbuilder.ScriptBuilder;
import org.jclouds.scriptbuilder.domain.OsFamily;
import org.ow2.proactive.connector.iaas.cloud.provider.CloudProvider;
import org.ow2.proactive.connector.iaas.model.Hardware;
import org.ow2.proactive.connector.iaas.model.Image;
import org.ow2.proactive.connector.iaas.model.Infrastructure;
import org.ow2.proactive.connector.iaas.model.Instance;
import org.ow2.proactive.connector.iaas.model.InstanceScript;
import org.ow2.proactive.connector.iaas.model.Network;
import org.ow2.proactive.connector.iaas.model.ScriptResult;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;


@Component
public abstract class JCloudsProvider implements CloudProvider {

    @Autowired
    private JCloudsComputeServiceCache jCloudsComputeServiceCache;

    @Override
    public void deleteInstance(Infrastructure infrastructure, String instanceId) {
        getComputeServiceFromInfastructure(infrastructure).destroyNode(instanceId);

    }

    @Override
    public Set<Instance> getAllInfrastructureInstances(Infrastructure infrastructure) {
        return getComputeServiceFromInfastructure(infrastructure).listNodes().stream()
                .map(computeMetadata -> (NodeMetadataImpl) computeMetadata)
                .map(nodeMetadataImpl -> instanceCreatorFromNodeMetadata.apply(nodeMetadataImpl,
                        infrastructure.getId()))
                .collect(Collectors.toSet());
    }

    @Override
    public ScriptResult executeScriptOnInstanceId(Infrastructure infrastructure, String instanceId,
            InstanceScript instanceScript) {
        ExecResponse execResponse;

        try {
            execResponse = getComputeServiceFromInfastructure(infrastructure).runScriptOnNode(instanceId,
                    buildScriptToExecuteString(instanceScript), buildScriptOptions(instanceScript));
        } catch (Exception e) {
            throw new RuntimeException(e);
        }

        return new ScriptResult(instanceId, execResponse.getOutput(), execResponse.getError());
    }

    @Override
    public List<ScriptResult> executeScriptOnInstanceTag(Infrastructure infrastructure, String instanceTag,
            InstanceScript instanceScript) {

        Map<? extends NodeMetadata, ExecResponse> execResponses;

        try {
            execResponses = getComputeServiceFromInfastructure(infrastructure).runScriptOnNodesMatching(
                    runningInGroup(instanceTag), buildScriptToExecuteString(instanceScript),
                    buildScriptOptions(instanceScript));
        } catch (Exception e) {
            throw new RuntimeException(e);
        }

        return execResponses.entrySet().stream().map(entry -> new ScriptResult(entry.getKey().getId(),
            entry.getValue().getOutput(), entry.getValue().getError())).collect(Collectors.toList());

    }

    @Override
    public Set<Image> getAllImages(Infrastructure infrastructure) {
        return getComputeServiceFromInfastructure(infrastructure).listImages().stream()
                .map(it -> Image.builder().id(it.getId()).name(it.getName()).build())
                .collect(Collectors.toSet());

    }

    @Override
    public void deleteInfrastructure(Infrastructure infrastructure) {
        getAllInfrastructureInstances(infrastructure).stream().forEach(instance -> {
            deleteInstance(infrastructure, instance.getId());
        });
        jCloudsComputeServiceCache.removeComputeService(infrastructure);

    }

    protected final BiFunction<NodeMetadataImpl, String, Instance> instanceCreatorFromNodeMetadata = (

    nodeMetadataImpl, infrastructureId) -> {
        return Instance.builder().id(nodeMetadataImpl.getId())
                .tag(Optional.ofNullable(nodeMetadataImpl.getName()).orElse(""))
                .image(nodeMetadataImpl.getImageId()).number("1")
                .hardware(
                        Optional.ofNullable(nodeMetadataImpl.getHardware())
                                .map(hardware -> Hardware.builder()
                                        .minRam(String.valueOf(nodeMetadataImpl.getHardware().getRam()))
                                        .minCores(String.valueOf(
                                                nodeMetadataImpl.getHardware().getProcessors().size()))
                                .type(nodeMetadataImpl.getHardware().getType().name()).build())
                        .orElse(new Hardware()))
                .network(Network.builder().publicAddresses(nodeMetadataImpl.getPublicAddresses())
                        .privateAddresses(nodeMetadataImpl.getPrivateAddresses()).build())
                .status(nodeMetadataImpl.getStatus().name()).build();
    };

    protected ComputeService getComputeServiceFromInfastructure(Infrastructure infrastructure) {
        return jCloudsComputeServiceCache.getComputeService(infrastructure);
    }

    protected String buildScriptToExecuteString(InstanceScript instanceScript) {

        return Optional.ofNullable(instanceScript).map(scriptToRun -> {
            ScriptBuilder scriptBuilder = new ScriptBuilder();
            Arrays.stream(scriptToRun.getScripts())
                    .forEachOrdered(script -> scriptBuilder.addStatement(exec(script)));
            return scriptBuilder.render(OsFamily.UNIX);
        }).orElse("");

    }

    private RunScriptOptions buildScriptOptions(InstanceScript instanceScript) {
        return Optional.ofNullable(instanceScript.getCredentials())
                .map(credentials -> RunScriptOptions.Builder.runAsRoot(false).overrideLoginCredentials(
                        new LoginCredentials.Builder().user(credentials.getUsername())
                                .password(credentials.getPassword()).authenticateSudo(false).build()))
                .orElse(RunScriptOptions.NONE);
    }

}

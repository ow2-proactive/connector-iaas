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
import org.jclouds.compute.domain.Template;
import org.jclouds.compute.domain.internal.NodeMetadataImpl;
import org.jclouds.compute.options.RunScriptOptions;
import org.jclouds.scriptbuilder.ScriptBuilder;
import org.jclouds.scriptbuilder.domain.OsFamily;
import org.ow2.proactive.connector.iaas.cloud.provider.CloudProvider;
import org.ow2.proactive.connector.iaas.model.Image;
import org.ow2.proactive.connector.iaas.model.Infrastructure;
import org.ow2.proactive.connector.iaas.model.Instance;
import org.ow2.proactive.connector.iaas.model.InstanceScript;
import org.ow2.proactive.connector.iaas.model.ScriptResult;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import com.google.common.collect.Sets;

@Component("defaultCloudProvider")
public class JCloudsProvider implements CloudProvider {

	@Autowired
	private JCloudsComputeServiceCache jCloudsComputeServiceCache;

	@Override
	public Set<Instance> createInstance(Infrastructure infrastructure, Instance instance) {
		ComputeService computeService = getComputeServiceFromInfastructure(infrastructure);
		Template template = computeService.templateBuilder().minRam(Integer.parseInt(instance.getRam()))
				.imageId(instance.getImage()).build();

		Set<? extends NodeMetadata> createdNodeMetaData = Sets.newHashSet();

		Optional.ofNullable(instance.getPostBootScript())
				.ifPresent(script -> template.getOptions().runScript(buildScriptToExecuteString(script)));

		try {
			createdNodeMetaData = computeService.createNodesInGroup(instance.getTag(),
					Integer.parseInt(instance.getNumber()), template);
		} catch (Exception e) {
			throw new RuntimeException(e);
		}

		return createdNodeMetaData.stream().map(computeMetadata -> (NodeMetadataImpl) computeMetadata).map(
				nodeMetadataImpl -> instanceCreatorFromNodeMetadata.apply(nodeMetadataImpl, infrastructure.getId()))
				.collect(Collectors.toSet());

	}

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
				.map(it -> Image.builder().id(it.getId()).name(it.getName()).build()).collect(Collectors.toSet());

	}

	@Override
	public void deleteInfrastructure(Infrastructure infrastructure) {
		getAllInfrastructureInstances(infrastructure).stream().forEach(instance -> {
			deleteInstance(infrastructure, instance.getId());
		});
		jCloudsComputeServiceCache.removeComputeService(infrastructure);

	}

	private ComputeService getComputeServiceFromInfastructure(Infrastructure infrastructure) {
		return jCloudsComputeServiceCache.getComputeService(infrastructure);
	}

	private final BiFunction<NodeMetadataImpl, String, Instance> instanceCreatorFromNodeMetadata = (nodeMetadataImpl,
			infrastructureId) -> {
		return Instance.builder().id(nodeMetadataImpl.getId()).tag(nodeMetadataImpl.getName())
				.image(nodeMetadataImpl.getImageId()).number("1")
				.ram(String.valueOf(nodeMetadataImpl.getHardware().getRam()))
				.cpu(String.valueOf(nodeMetadataImpl.getHardware().getProcessors().size()))
				.status(nodeMetadataImpl.getStatus().name()).build();
	};

	private String buildScriptToExecuteString(InstanceScript instanceScript) {
		ScriptBuilder scriptBuilder = new ScriptBuilder();

		Arrays.stream(instanceScript.getScripts()).forEachOrdered(script -> scriptBuilder.addStatement(exec(script)));

		String allScriptsToExecute = scriptBuilder.render(OsFamily.UNIX);
		return allScriptsToExecute;
	}

	private RunScriptOptions buildScriptOptions(InstanceScript instanceScript) {
		return Optional.ofNullable(instanceScript.getCredentials())
				.map(credentials -> RunScriptOptions.Builder.runAsRoot(false)
						.overrideLoginUser(credentials.getUsername()).overrideLoginPassword(credentials.getPassword()))
				.orElse(RunScriptOptions.Builder.runAsRoot(true));
	}

}

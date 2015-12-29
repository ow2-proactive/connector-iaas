package org.ow2.proactive.iaas.connector.service;

import java.util.Set;
import java.util.stream.Collectors;

import org.jclouds.compute.ComputeService;
import org.jclouds.compute.RunNodesException;
import org.jclouds.compute.domain.Template;
import org.ow2.proactive.iaas.connector.cache.ComputeServiceCache;
import org.ow2.proactive.iaas.connector.model.Instance;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class InstanceService {

	@Autowired
	private InfrastructureService infrastructureService;

	@Autowired
	private ComputeServiceCache computeServiceCache;

	public void createInstance(Instance instance) {
		ComputeService computeService = getComputeServiceFromInfastructureName(instance.getInfrastructure());

		Template template = computeService.templateBuilder().minRam(Integer.parseInt(instance.getRam()))
				.imageId(instance.getImage()).build();

		try {
			computeService.createNodesInGroup(instance.getName(), Integer.parseInt(instance.getNumber()), template);
		} catch (RunNodesException e) {
			throw new RuntimeException(e);
		}

	}

	public void deleteInstance(String infrastructureName, String instanceID) {
		getComputeServiceFromInfastructureName(infrastructureName).destroyNode(instanceID);
	}

	public Set<String> getAllInstances(String infrastructureName) {
		return getComputeServiceFromInfastructureName(infrastructureName).listNodes().stream().map(it -> it.getId())
				.collect(Collectors.toSet());
	}

	private ComputeService getComputeServiceFromInfastructureName(String infrastructureName) {
		return computeServiceCache.getComputeService(infrastructureService.getInfrastructurebyName(infrastructureName));
	}

}

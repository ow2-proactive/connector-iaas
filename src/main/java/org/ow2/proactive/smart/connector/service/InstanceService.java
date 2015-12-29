package org.ow2.proactive.smart.connector.service;

import java.util.Iterator;
import java.util.LinkedList;

import org.jclouds.compute.ComputeService;
import org.jclouds.compute.RunNodesException;
import org.jclouds.compute.domain.ComputeMetadata;
import org.jclouds.compute.domain.TemplateBuilder;
import org.ow2.proactive.smart.connector.cache.ComputeServiceCache;
import org.ow2.proactive.smart.connector.model.Infrastructure;
import org.ow2.proactive.smart.connector.model.Instance;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class InstanceService {

	@Autowired
	private InfrastructureService infrastructureService;

	@Autowired
	private ComputeServiceCache computeServiceCache;

	public void createInstance(Instance instance) {
		Infrastructure infrastructure = infrastructureService.getInfrastructurebyName(instance.getInfrastructure());
		ComputeService computeService = computeServiceCache.getComputeService(infrastructure);

		TemplateBuilder tb = computeService.templateBuilder();
		tb.minRam(Integer.parseInt(instance.getRam()));
		tb.imageId(instance.getImage());

		try {
			computeService.createNodesInGroup(instance.getName(), Integer.parseInt(instance.getNumber()), tb.build());
		} catch (RunNodesException e) {
			e.printStackTrace();
		}

	}

	public void deleteInstance(String infrastructureName, String instanceID) {
		ComputeService computeService = computeServiceCache
				.getComputeService(infrastructureService.getInfrastructurebyName(infrastructureName));
		computeService.destroyNode(instanceID);
	}

	public LinkedList<String> getAllInstances(String infrastructureName) {
		ComputeService computeService = computeServiceCache
				.getComputeService(infrastructureService.getInfrastructurebyName(infrastructureName));
		Iterator<? extends ComputeMetadata> it = computeService.listNodes().iterator();

		LinkedList<String> allRunningInstances = new LinkedList<String>();
		while (it.hasNext()) {
			allRunningInstances.add(it.next().getId());
		}
		return allRunningInstances;
	}

}

package org.ow2.proactive.iaas.connector.service;

import java.util.Set;
import java.util.stream.Collectors;

import org.jclouds.compute.ComputeService;
import org.ow2.proactive.iaas.connector.cache.ComputeServiceCache;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class ImageService {

	@Autowired
	private InfrastructureService infrastructureService;

	@Autowired
	private ComputeServiceCache computeServiceCache;

	public Set<String> getAllImages(String infrastructureName) {

		ComputeService computeService = computeServiceCache
				.getComputeService(infrastructureService.getInfrastructurebyName(infrastructureName));

		return computeService.listImages().stream().map(it -> it.getId()).collect(Collectors.toSet());

	}

}

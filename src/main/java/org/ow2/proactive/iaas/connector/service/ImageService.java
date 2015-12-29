package org.ow2.proactive.iaas.connector.service;

import java.util.Iterator;
import java.util.LinkedList;

import org.jclouds.compute.ComputeService;
import org.jclouds.compute.domain.Image;
import org.ow2.proactive.iaas.connector.cache.ComputeServiceCache;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class ImageService {

	@Autowired
	private InfrastructureService infrastructureService;

	@Autowired
	private ComputeServiceCache computeServiceCache;

	public LinkedList<String> getAllImages(String infrastructureName) {
		ComputeService computeService = computeServiceCache
				.getComputeService(infrastructureService.getInfrastructurebyName(infrastructureName));
		Iterator<? extends Image> it = computeService.listImages().iterator();

		LinkedList<String> allSupportedImages = new LinkedList<String>();
		while (it.hasNext()) {
			allSupportedImages.add(it.next().getId());
		}
		return allSupportedImages;
	}

}

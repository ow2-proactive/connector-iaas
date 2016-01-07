package org.ow2.proactive.connector.iaas.cloud.provider.jclouds;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.function.Function;

import org.jclouds.compute.ComputeService;
import org.ow2.proactive.connector.iaas.model.Infrastructure;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Component
public class JCloudsComputeServiceCache {

	@Autowired
	private JCloudsComputeServiceBuilder computeServiceBuilder;

	private Map<Infrastructure, ComputeService> computeServiceCache;

	public JCloudsComputeServiceCache() {
		computeServiceCache = new ConcurrentHashMap<Infrastructure, ComputeService>();
	}

	public ComputeService getComputeService(Infrastructure infratructure) {
		return buildComputeService.apply(infratructure);
	}

	public void removeComputeService(Infrastructure infratructure) {
		computeServiceCache.remove(infratructure);
	}

	private Function<Infrastructure, ComputeService> buildComputeService = memoise(infrastructure -> {
		return computeServiceBuilder.buildComputeServiceFromInfrastructure(infrastructure);
	});

	private Function<Infrastructure, ComputeService> memoise(Function<Infrastructure, ComputeService> fn) {
		return (a) -> computeServiceCache.computeIfAbsent(a, fn);
	}

}

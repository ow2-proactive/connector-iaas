package org.ow2.proactive.iaas.connector.cache;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.function.Function;

import org.jclouds.ContextBuilder;
import org.jclouds.compute.ComputeService;
import org.jclouds.compute.ComputeServiceContext;
import org.ow2.proactive.iaas.connector.model.Infrastructure;
import org.springframework.stereotype.Component;

@Component
public class ComputeServiceCache {

	private Map<Infrastructure, ComputeService> computeServiceCache;

	public ComputeServiceCache() {
		computeServiceCache = new ConcurrentHashMap<Infrastructure, ComputeService>();
	}

	private Function<Infrastructure, ComputeService> buildComputeService = memoise(infrastructure -> {
		ComputeServiceContext context;
		if (infrastructure.getName().equalsIgnoreCase("aws-ec2")) {
			context = ContextBuilder.newBuilder(infrastructure.getName())
					.credentials(infrastructure.getUserName(), infrastructure.getCredential())
					.buildView(ComputeServiceContext.class);
		} else {
			context = ContextBuilder.newBuilder(infrastructure.getName()).endpoint(infrastructure.getEndPoint())
					.credentials(infrastructure.getUserName(), infrastructure.getCredential())
					.buildView(ComputeServiceContext.class);
		}

		return context.getComputeService();
	});

	private Function<Infrastructure, ComputeService> memoise(Function<Infrastructure, ComputeService> fn) {
		return (a) -> computeServiceCache.computeIfAbsent(a, fn);
	}

	public ComputeService getComputeService(Infrastructure infratructure) {
		return buildComputeService.apply(infratructure);
	}

	public void removeComputeService(Infrastructure infratructure) {
		computeServiceCache.remove(infratructure);
	}
}

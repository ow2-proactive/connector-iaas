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

	private static final String AWS_INFASTRUCTURE_NAME = "aws-ec2";

	private Map<Infrastructure, ComputeService> computeServiceCache;

	public ComputeServiceCache() {
		computeServiceCache = new ConcurrentHashMap<Infrastructure, ComputeService>();
	}

	public ComputeService getComputeService(Infrastructure infratructure) {
		return buildComputeService.apply(infratructure);
	}

	public void removeComputeService(Infrastructure infratructure) {
		computeServiceCache.remove(infratructure);
	}

	private Function<Infrastructure, ComputeService> buildComputeService = memoise(infrastructure -> {

		ContextBuilder contextBuilder = ContextBuilder.newBuilder(infrastructure.getName())
				.credentials(infrastructure.getUserName(), infrastructure.getCredential());

		if (!infrastructure.getName().equalsIgnoreCase(AWS_INFASTRUCTURE_NAME)) {
			contextBuilder.endpoint(infrastructure.getEndPoint());
		}

		return contextBuilder.buildView(ComputeServiceContext.class).getComputeService();
	});

	private Function<Infrastructure, ComputeService> memoise(Function<Infrastructure, ComputeService> fn) {
		return (a) -> computeServiceCache.computeIfAbsent(a, fn);
	}

}

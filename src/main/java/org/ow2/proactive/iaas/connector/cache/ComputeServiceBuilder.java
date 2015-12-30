package org.ow2.proactive.iaas.connector.cache;

import java.util.Optional;

import org.jclouds.ContextBuilder;
import org.jclouds.compute.ComputeService;
import org.jclouds.compute.ComputeServiceContext;
import org.ow2.proactive.iaas.connector.model.Infrastructure;
import org.springframework.stereotype.Component;

@Component
public class ComputeServiceBuilder {

	public ComputeService buildComputeServiceFromInfrastructure(Infrastructure infrastructure) {
		ContextBuilder contextBuilder = ContextBuilder.newBuilder(infrastructure.getName())
				.credentials(infrastructure.getUserName(), infrastructure.getCredential());

		Optional.ofNullable(infrastructure.getEndPoint()).filter(endPoint -> !endPoint.isEmpty()).ifPresent(endPoint -> contextBuilder.endpoint(endPoint));

		return contextBuilder.buildView(ComputeServiceContext.class).getComputeService();
	}

}

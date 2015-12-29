package org.ow2.proactive.iaas.connector.cache;

import org.jclouds.ContextBuilder;
import org.jclouds.compute.ComputeService;
import org.jclouds.compute.ComputeServiceContext;
import org.ow2.proactive.iaas.connector.model.Infrastructure;
import org.springframework.stereotype.Component;

@Component
public class ComputeServiceBuilder {

	public static final String AWS_INFASTRUCTURE_NAME = "aws-ec2";

	public ComputeService buildComputeServiceFromInfrastructure(Infrastructure infrastructure) {
		ContextBuilder contextBuilder = ContextBuilder.newBuilder(infrastructure.getName())
				.credentials(infrastructure.getUserName(), infrastructure.getCredential());

		if (!infrastructure.getName().equalsIgnoreCase(AWS_INFASTRUCTURE_NAME)) {
			contextBuilder.endpoint(infrastructure.getEndPoint());
		}

		return contextBuilder.buildView(ComputeServiceContext.class).getComputeService();
	}

}

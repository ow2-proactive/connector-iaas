package org.ow2.proactive.connector.iaas.cloud.provider.jclouds;

import static org.jclouds.compute.config.ComputeServiceProperties.TIMEOUT_PORT_OPEN;
import static org.jclouds.compute.config.ComputeServiceProperties.TIMEOUT_SCRIPT_COMPLETE;

import java.util.Optional;
import java.util.Properties;
import java.util.concurrent.TimeUnit;

import org.jclouds.ContextBuilder;
import org.jclouds.compute.ComputeService;
import org.jclouds.compute.ComputeServiceContext;
import org.jclouds.sshj.config.SshjSshClientModule;
import org.ow2.proactive.connector.iaas.model.Infrastructure;
import org.springframework.stereotype.Component;

import com.google.common.collect.ImmutableSet;
import com.google.inject.Module;

@Component
public class JCloudsComputeServiceBuilder {

	public ComputeService buildComputeServiceFromInfrastructure(Infrastructure infrastructure) {

		Iterable<Module> modules = ImmutableSet.<Module> of(new SshjSshClientModule());
		ContextBuilder contextBuilder = ContextBuilder.newBuilder(infrastructure.getType())

		.credentials(infrastructure.getUserName(), infrastructure.getCredential()).modules(modules)
				.overrides(getTimeoutPolicy());

		Optional.ofNullable(infrastructure.getEndPoint()).filter(endPoint -> !endPoint.isEmpty())
				.ifPresent(endPoint -> contextBuilder.endpoint(endPoint));

		return contextBuilder.buildView(ComputeServiceContext.class).getComputeService();
	}

	/**
	 * Sets the timeouts for the deployment.
	 * 
	 * @return Properties object with the timeout policy.
	 */
	private Properties getTimeoutPolicy() {
		Properties properties = new Properties();
		long scriptTimeout = TimeUnit.MILLISECONDS.convert(1, TimeUnit.MINUTES);
		properties.setProperty("jclouds.ssh.max-retries", "100");
		properties.setProperty("jclouds.max-retries", "1000");
		properties.setProperty("jclouds.request-timeout", "10000");
		properties.setProperty("jclouds.connection-timeout", "18000");

		properties.setProperty(TIMEOUT_PORT_OPEN, scriptTimeout + "");
		properties.setProperty(TIMEOUT_SCRIPT_COMPLETE, scriptTimeout + "");

		return properties;
	}

}

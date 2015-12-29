package org.ow2.proactive.iaas.connector.cache;

import static org.hamcrest.CoreMatchers.instanceOf;
import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.CoreMatchers.not;
import static org.junit.Assert.assertThat;

import org.jclouds.aws.ec2.compute.AWSEC2ComputeService;
import org.jclouds.compute.ComputeService;
import org.junit.Before;
import org.junit.Test;
import org.ow2.proactive.iaas.connector.fixtures.InfrastructureFixtures;

public class ComputeServiceBuilderTest {

	private ComputeServiceBuilder computeServiceBuilder;

	@Before
	public void init() {
		this.computeServiceBuilder = new ComputeServiceBuilder();
	}

	@Test
	public void testBuildComputeServiceFromInfrastructureAWS() {
		ComputeService computerService = computeServiceBuilder.buildComputeServiceFromInfrastructure(
				InfrastructureFixtures.getInfrastructure(ComputeServiceBuilder.AWS_INFASTRUCTURE_NAME, "endPoint",
						"userName", "credential"));

		assertThat(computerService, is(instanceOf(AWSEC2ComputeService.class)));
	}

	@Test
	public void testBuildComputeServiceFromInfrastructureOpenstack() {
		ComputeService computerService = computeServiceBuilder.buildComputeServiceFromInfrastructure(
				InfrastructureFixtures.getInfrastructure("openstack-nova", "endPoint", "userName", "credential"));

		assertThat(computerService, is(not(instanceOf(AWSEC2ComputeService.class))));
	}

}

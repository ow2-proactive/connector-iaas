package org.ow2.proactive.connector.iaas.cloud.provider.jclouds;

import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.not;
import static org.hamcrest.Matchers.nullValue;
import static org.junit.Assert.assertThat;
import static org.mockito.Matchers.any;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import org.jclouds.compute.ComputeService;
import org.junit.Before;
import org.junit.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.ow2.proactive.connector.iaas.cloud.provider.jclouds.JCloudsComputeServiceBuilder;
import org.ow2.proactive.connector.iaas.cloud.provider.jclouds.JCloudsComputeServiceCache;
import org.ow2.proactive.connector.iaas.fixtures.InfrastructureFixture;
import org.ow2.proactive.connector.iaas.model.Infrastructure;

public class JCloudsComputeServiceCacheTest {

	@InjectMocks
	private JCloudsComputeServiceCache computeServiceCache;

	@Mock
	private JCloudsComputeServiceBuilder computeServiceBuilder;

	@Mock
	private ComputeService computeService;

	@Before
	public void init() {
		MockitoAnnotations.initMocks(this);
		when(computeServiceBuilder.buildComputeServiceFromInfrastructure(any(Infrastructure.class)))
				.thenReturn(computeService);
	}

	@Test
	public void testGetComputeServiceFirstTime() {
		ComputeService computeService = computeServiceCache.getComputeService(
				InfrastructureFixture.getInfrastructure("id-aws-ec2","aws-ec2", "endPoint", "userName", "credential"));
		assertThat(computeService, is(not(nullValue())));
		verify(computeServiceBuilder, times(1)).buildComputeServiceFromInfrastructure(
				InfrastructureFixture.getInfrastructure("id-aws-ec2","aws-ec2", "endPoint", "userName", "credential"));
	}

	@Test
	public void testGetComputeServiceManyTimeSameInfrastructure() {
		computeServiceCache.getComputeService(
				InfrastructureFixture.getInfrastructure("id-aws-ec2","aws-ec2", "endPoint", "userName", "credential"));
		computeServiceCache.getComputeService(
				InfrastructureFixture.getInfrastructure("id-aws-ec2","aws-ec2", "endPoint", "userName", "credential"));
		computeServiceCache.getComputeService(
				InfrastructureFixture.getInfrastructure("id-aws-ec2","aws-ec2", "endPoint", "userName", "credential"));
		computeServiceCache.getComputeService(
				InfrastructureFixture.getInfrastructure("id-aws-ec2","aws-ec2", "endPoint", "userName", "credential"));
		computeServiceCache.getComputeService(
				InfrastructureFixture.getInfrastructure("id-aws-ec2","aws-ec2", "endPoint", "userName", "credential"));

		verify(computeServiceBuilder, times(1)).buildComputeServiceFromInfrastructure(
				InfrastructureFixture.getInfrastructure("id-aws-ec2","aws-ec2", "endPoint", "userName", "credential"));
	}

	@Test
	public void testGetComputeServiceDifferentInfrastructure() {
		computeServiceCache.getComputeService(
				InfrastructureFixture.getInfrastructure("id-aws-ec2","aws-ec2", "endPoint", "userName", "credential"));
		computeServiceCache.getComputeService(
				InfrastructureFixture.getInfrastructure("id-openstack","openstack", "endPoint", "userName", "credential"));

		verify(computeServiceBuilder, times(2)).buildComputeServiceFromInfrastructure(any(Infrastructure.class));
	}

	@Test
	public void testRemoveComputeService() {
		computeServiceCache.getComputeService(
				InfrastructureFixture.getInfrastructure("id-aws-ec2","aws-ec2", "endPoint", "userName", "credential"));

		computeServiceCache.removeComputeService(
				InfrastructureFixture.getInfrastructure("id-aws-ec2","aws-ec2", "endPoint", "userName", "credential"));

		computeServiceCache.getComputeService(
				InfrastructureFixture.getInfrastructure("id-aws-ec2","aws-ec2", "endPoint", "userName", "credential"));

		verify(computeServiceBuilder, times(2)).buildComputeServiceFromInfrastructure(
				InfrastructureFixture.getInfrastructure("id-aws-ec2","aws-ec2", "endPoint", "userName", "credential"));
	}

}

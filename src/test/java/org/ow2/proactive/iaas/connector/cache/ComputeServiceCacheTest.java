package org.ow2.proactive.iaas.connector.cache;

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
import org.ow2.proactive.iaas.connector.fixtures.InfrastructureFixture;
import org.ow2.proactive.iaas.connector.model.Infrastructure;

public class ComputeServiceCacheTest {

	@InjectMocks
	private ComputeServiceCache computeServiceCache;

	@Mock
	private ComputeServiceBuilder computeServiceBuilder;

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
				InfrastructureFixture.getInfrastructure("aws-ec2", "endPoint", "userName", "credential"));
		assertThat(computeService, is(not(nullValue())));
		verify(computeServiceBuilder, times(1)).buildComputeServiceFromInfrastructure(
				InfrastructureFixture.getInfrastructure("aws-ec2", "endPoint", "userName", "credential"));
	}

	@Test
	public void testGetComputeServiceManyTimeSameInfrastructure() {
		computeServiceCache.getComputeService(
				InfrastructureFixture.getInfrastructure("aws-ec2", "endPoint", "userName", "credential"));
		computeServiceCache.getComputeService(
				InfrastructureFixture.getInfrastructure("aws-ec2", "endPoint", "userName", "credential"));
		computeServiceCache.getComputeService(
				InfrastructureFixture.getInfrastructure("aws-ec2", "endPoint", "userName", "credential"));
		computeServiceCache.getComputeService(
				InfrastructureFixture.getInfrastructure("aws-ec2", "endPoint", "userName", "credential"));
		computeServiceCache.getComputeService(
				InfrastructureFixture.getInfrastructure("aws-ec2", "endPoint", "userName", "credential"));

		verify(computeServiceBuilder, times(1)).buildComputeServiceFromInfrastructure(
				InfrastructureFixture.getInfrastructure("aws-ec2", "endPoint", "userName", "credential"));
	}

	@Test
	public void testGetComputeServiceDifferentInfrastructure() {
		computeServiceCache.getComputeService(
				InfrastructureFixture.getInfrastructure("aws-ec2", "endPoint", "userName", "credential"));
		computeServiceCache.getComputeService(
				InfrastructureFixture.getInfrastructure("openstack", "endPoint", "userName", "credential"));

		verify(computeServiceBuilder, times(2)).buildComputeServiceFromInfrastructure(any(Infrastructure.class));
	}

	@Test
	public void testRemoveComputeService() {
		computeServiceCache.getComputeService(
				InfrastructureFixture.getInfrastructure("aws-ec2", "endPoint", "userName", "credential"));

		computeServiceCache.removeComputeService(
				InfrastructureFixture.getInfrastructure("aws-ec2", "endPoint", "userName", "credential"));

		computeServiceCache.getComputeService(
				InfrastructureFixture.getInfrastructure("aws-ec2", "endPoint", "userName", "credential"));

		verify(computeServiceBuilder, times(2)).buildComputeServiceFromInfrastructure(
				InfrastructureFixture.getInfrastructure("aws-ec2", "endPoint", "userName", "credential"));
	}

}

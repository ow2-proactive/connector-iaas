package org.ow2.proactive.iaas.connector.service;

import static org.hamcrest.Matchers.is;
import static org.junit.Assert.assertThat;
import static org.mockito.Mockito.inOrder;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import org.jclouds.compute.ComputeService;
import org.junit.Before;
import org.junit.Test;
import org.mockito.InOrder;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.ow2.proactive.iaas.connector.cache.InfrastructureCache;
import org.ow2.proactive.iaas.connector.cloud.provider.jcloud.JCloudComputeServiceCache;
import org.ow2.proactive.iaas.connector.fixtures.InfrastructureFixture;
import org.ow2.proactive.iaas.connector.model.Infrastructure;

import jersey.repackaged.com.google.common.collect.ImmutableMap;

public class InfrastructureServiceTest {

	@InjectMocks
	private InfrastructureService infrastructureService;

	@Mock
	private InfrastructureCache infrastructureCache;

	@Mock
	private JCloudComputeServiceCache computeServiceCache;

	@Mock
	private ComputeService infrastructure;

	private ImmutableMap<String, Infrastructure> mockSupportedInfrastructures;

	@Before
	public void init() {
		MockitoAnnotations.initMocks(this);
	}

	@Test
	public void testRegisterInfrastructure() {
		Infrastructure infrastructure = InfrastructureFixture.getInfrastructure("id-aws", "aws", "endPoint", "userName",
				"credential");
		infrastructureService.registerInfrastructure(infrastructure);
		verify(infrastructureCache, times(1)).registerInfrastructure(infrastructure);
	}

	@Test
	public void testGetInfrastructureByName() {
		Infrastructure infrastructure = InfrastructureFixture.getInfrastructure("id-aws","aws", "endPoint", "userName",
				"credential");
		mockSupportedInfrastructures = ImmutableMap.of("aws", infrastructure);
		when(infrastructureCache.getSupportedInfrastructures()).thenReturn(mockSupportedInfrastructures);
		infrastructureService.registerInfrastructure(infrastructure);
		assertThat(infrastructureCache.getSupportedInfrastructures().get("aws"), is(infrastructure));
	}

	@Test
	public void testDeleteInfrastructure() {
		Infrastructure infrastructure = InfrastructureFixture.getInfrastructure("id-aws","aws", "endPoint", "userName",
				"credential");
		mockSupportedInfrastructures = ImmutableMap.of("aws", infrastructure);
		when(infrastructureCache.getSupportedInfrastructures()).thenReturn(mockSupportedInfrastructures);
		infrastructureService.deleteInfrastructure("aws");

		InOrder inOrder = inOrder(computeServiceCache,infrastructureCache);
		inOrder.verify(computeServiceCache, times(1)).removeComputeService(infrastructure);
		inOrder.verify(infrastructureCache, times(1)).deleteInfrastructure(infrastructure);
	}

	@Test
	public void testUpdateInfrastructure() {
		Infrastructure infrastructure = InfrastructureFixture.getInfrastructure("id-aws","aws", "endPoint", "userName",
				"credential");
		mockSupportedInfrastructures = ImmutableMap.of("aws", infrastructure);
		when(infrastructureCache.getSupportedInfrastructures()).thenReturn(mockSupportedInfrastructures);
		infrastructureService.updateInfrastructure("aws", infrastructure);

		InOrder inOrder = inOrder(infrastructureCache, computeServiceCache);
		inOrder.verify(infrastructureCache, times(1)).deleteInfrastructure(infrastructure);
		inOrder.verify(infrastructureCache, times(1)).registerInfrastructure(infrastructure);
	}
}

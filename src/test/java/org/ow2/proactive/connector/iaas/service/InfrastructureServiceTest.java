package org.ow2.proactive.connector.iaas.service;

import static org.hamcrest.Matchers.is;
import static org.junit.Assert.assertThat;
import static org.mockito.Mockito.inOrder;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import org.junit.Before;
import org.junit.Test;
import org.mockito.InOrder;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.ow2.proactive.connector.iaas.cache.InfrastructureCache;
import org.ow2.proactive.connector.iaas.cloud.CloudManager;
import org.ow2.proactive.connector.iaas.fixtures.InfrastructureFixture;
import org.ow2.proactive.connector.iaas.model.Infrastructure;

import jersey.repackaged.com.google.common.collect.ImmutableMap;

public class InfrastructureServiceTest {

	@InjectMocks
	private InfrastructureService infrastructureService;

	@Mock
	private InfrastructureCache infrastructureCache;

	@Mock
	private CloudManager cloudManager;

	private ImmutableMap<String, Infrastructure> mockSupportedInfrastructures;

	@Before
	public void init() {
		MockitoAnnotations.initMocks(this);
	}

	@Test
	public void testRegisterInfrastructure() {
		Infrastructure infrastructure = InfrastructureFixture.getInfrastructure("id-aws", "aws", "endPoint", "userName",
				"password");
		infrastructureService.registerInfrastructure(infrastructure);
		verify(infrastructureCache, times(1)).registerInfrastructure(infrastructure);
	}

	@Test
	public void testGetInfrastructureByName() {
		Infrastructure infrastructure = InfrastructureFixture.getInfrastructure("id-aws", "aws", "endPoint", "userName",
				"password");
		mockSupportedInfrastructures = ImmutableMap.of("aws", infrastructure);
		when(infrastructureCache.getSupportedInfrastructures()).thenReturn(mockSupportedInfrastructures);
		infrastructureService.registerInfrastructure(infrastructure);
		assertThat(infrastructureCache.getSupportedInfrastructures().get("aws"), is(infrastructure));
	}

	@Test
	public void testDeleteInfrastructure() {
		Infrastructure infrastructure = InfrastructureFixture.getInfrastructure("id-aws", "aws", "endPoint", "userName",
				"password");
		mockSupportedInfrastructures = ImmutableMap.of("aws", infrastructure);
		when(infrastructureCache.getSupportedInfrastructures()).thenReturn(mockSupportedInfrastructures);
		infrastructureService.deleteInfrastructure(infrastructure);

		InOrder inOrder = inOrder(cloudManager, infrastructureCache);
		inOrder.verify(cloudManager, times(1)).deleteInfrastructure(infrastructure);
		inOrder.verify(infrastructureCache, times(1)).deleteInfrastructure(infrastructure);
	}

	@Test
	public void testGetAllSupportedInfrastructure() {
		Infrastructure infrastructure = InfrastructureFixture.getInfrastructure("id-aws", "aws", "endPoint", "userName",
				"password");
		mockSupportedInfrastructures = ImmutableMap.of("aws", infrastructure);
		when(infrastructureCache.getSupportedInfrastructures()).thenReturn(mockSupportedInfrastructures);
		assertThat(infrastructureService.getAllSupportedInfrastructure().get("aws"), is(infrastructure));
		assertThat(infrastructureService.getAllSupportedInfrastructure().size(), is(1));
	}
}

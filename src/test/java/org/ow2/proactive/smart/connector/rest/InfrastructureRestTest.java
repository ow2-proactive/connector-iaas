package org.ow2.proactive.smart.connector.rest;

import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertThat;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import javax.ws.rs.core.Response;

import org.junit.Before;
import org.junit.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.ow2.proactive.smart.connector.fixtures.InfrastructureFixtures;
import org.ow2.proactive.smart.connector.service.InfrastructureService;

import jersey.repackaged.com.google.common.collect.Maps;

public class InfrastructureRestTest {
	@InjectMocks
	private InfrastructureRest infrastructureRest;

	@Mock
	private InfrastructureService infrastructureService;

	@Before
	public void init() {
		MockitoAnnotations.initMocks(this);
	}

	@Test
	public void testGetAllSupportedInfrastructure() {
		when(infrastructureService.getAllSupportedInfrastructure()).thenReturn(Maps.newHashMap());
		assertThat(infrastructureRest.getAllSupportedInfrastructure().getStatus(),
				is(Response.Status.OK.getStatusCode()));
		verify(infrastructureService, times(1)).getAllSupportedInfrastructure();
	}

	@Test
	public void testRegisterInfrastructure() {
		when(infrastructureService.getAllSupportedInfrastructure()).thenReturn(Maps.newHashMap());
		assertThat(infrastructureRest.registerInfrastructure(
				InfrastructureFixtures.getInfrastructureAsaString("openstack", "endPoint", "userName", "credential"))
				.getStatus(), is(Response.Status.OK.getStatusCode()));
		verify(infrastructureService, times(1)).registerInfrastructure(
				InfrastructureFixtures.getInfrastructure("openstack", "endPoint", "userName", "credential"));
		verify(infrastructureService, times(1)).getAllSupportedInfrastructure();
	}

}

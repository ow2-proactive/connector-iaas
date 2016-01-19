package org.ow2.proactive.connector.iaas.rest;

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
import org.mockito.Mockito;
import org.mockito.MockitoAnnotations;
import org.ow2.proactive.connector.iaas.fixtures.InfrastructureFixture;
import org.ow2.proactive.connector.iaas.model.Infrastructure;
import org.ow2.proactive.connector.iaas.service.InfrastructureService;

import jersey.repackaged.com.google.common.collect.Maps;

public class InfrastructureRestTest {
	@InjectMocks
	private InfrastructureRest infrastructureRest;

	@Mock
	private InfrastructureService infrastructureService;

	private String infrastructureStringFixture;

	private Infrastructure infrastructureFixture;

	@Before
	public void init() {
		MockitoAnnotations.initMocks(this);
		infrastructureStringFixture = InfrastructureFixture.getInfrastructureAsaString("id-openstack", "openstack",
				"endPoint", "userName", "password", "privateKey", "publicKey");
		infrastructureFixture = InfrastructureFixture.getInfrastructure("id-openstack", "openstack", "endPoint",
				"userName", "password", "privateKey", "publicKey");
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
		assertThat(infrastructureRest.registerInfrastructure(infrastructureStringFixture).getStatus(),
				is(Response.Status.OK.getStatusCode()));
		verify(infrastructureService, times(1)).registerInfrastructure(infrastructureFixture);
	}

	@Test
	public void testDeleteInfrastructureByName() {
		assertThat(infrastructureRest.deleteInfrastructureByName("openstack").getStatus(),
				is(Response.Status.OK.getStatusCode()));
		verify(infrastructureService, times(1)).deleteInfrastructure(Mockito.any(Infrastructure.class));
		verify(infrastructureService, times(1)).getAllSupportedInfrastructure();
	}

	@Test
	public void testGetInfrastructureByName() {
		assertThat(infrastructureRest.getInfrastructure("openstack").getStatus(),
				is(Response.Status.OK.getStatusCode()));
		verify(infrastructureService, times(1)).getInfrastructure(("openstack"));
	}

}

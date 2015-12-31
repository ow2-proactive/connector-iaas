package org.ow2.proactive.iaas.connector.rest;

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
import org.ow2.proactive.iaas.connector.fixtures.InstanceFixture;
import org.ow2.proactive.iaas.connector.model.Instance;
import org.ow2.proactive.iaas.connector.service.InstanceService;

import com.google.common.collect.Sets;

public class InstanceRestTest {
	@InjectMocks
	private InstanceRest instanceRest;

	@Mock
	private InstanceService instanceService;

	private String instanceStringFixture;
	private Instance instanceFixture;

	@Before
	public void init() {
		MockitoAnnotations.initMocks(this);
		instanceStringFixture = InstanceFixture.getInstanceAsaString("instance-id", "name", "image", "number",
				"cpu", "ram", "running", "infrastructure-id");
		instanceFixture = InstanceFixture.getInstance("instance-id", "name", "image", "number",
				"cpu", "ram", "running", "infrastructure-id");
	}

	@Test
	public void testCreateInstance() {
		assertThat(instanceRest.createInstance(instanceStringFixture).getStatus(),
				is(Response.Status.OK.getStatusCode()));
		verify(instanceService, times(1)).createInstance(instanceFixture);
	}

	@Test
	public void testListAllInstance() {
		when(instanceService.getAllInstances("infrastructureName")).thenReturn(Sets.newHashSet());
		assertThat(instanceRest.listAllInstance("infrastructureName").getStatus(),
				is(Response.Status.OK.getStatusCode()));
		verify(instanceService, times(1)).getAllInstances("infrastructureName");
	}

	@Test
	public void testDeleteInstance() {
		assertThat(instanceRest.deleteInstance("infrastructureName", "instanceID").getStatus(),
				is(Response.Status.OK.getStatusCode()));
		verify(instanceService, times(1)).deleteInstance("infrastructureName", "instanceID");
	}

}

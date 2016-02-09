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
import org.mockito.MockitoAnnotations;
import org.ow2.proactive.connector.iaas.fixtures.InstanceFixture;
import org.ow2.proactive.connector.iaas.model.Instance;
import org.ow2.proactive.connector.iaas.service.InstanceService;

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
                "cpu", "ram", "running");
        instanceFixture = InstanceFixture.getInstance("instance-id", "name", "image", "number", "cpu", "ram",
                "running");
    }

    @Test
    public void testCreateInstance() {
        assertThat(instanceRest.createInstance("infrastructureId", instanceStringFixture).getStatus(),
                is(Response.Status.OK.getStatusCode()));
        verify(instanceService, times(1)).createInstance("infrastructureId", instanceFixture);
    }

    @Test
    public void testListAllInstance() {
        when(instanceService.getAllInstances("infrastructureId")).thenReturn(Sets.newHashSet());
        assertThat(instanceRest.listAllInstance("infrastructureId").getStatus(),
                is(Response.Status.OK.getStatusCode()));
        verify(instanceService, times(1)).getAllInstances("infrastructureId");
    }

    @Test
    public void testDeleteInstance() {
        assertThat(instanceRest.deleteInstance("infrastructureId", "instanceID").getStatus(),
                is(Response.Status.OK.getStatusCode()));
        verify(instanceService, times(1)).deleteInstance("infrastructureId", "instanceID");
    }

    @Test
    public void testDeleteInstanceByTag() {
        assertThat(instanceRest.deleteInstanceByTag("infrastructureId", "instanceTag").getStatus(),
                is(Response.Status.OK.getStatusCode()));
        verify(instanceService, times(1)).deleteInstanceByTag("infrastructureId", "instanceTag");
    }

}
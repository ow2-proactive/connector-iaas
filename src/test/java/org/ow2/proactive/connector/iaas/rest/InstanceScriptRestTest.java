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
import org.ow2.proactive.connector.iaas.fixtures.InstanceScriptFixture;
import org.ow2.proactive.connector.iaas.model.InstanceScript;
import org.ow2.proactive.connector.iaas.model.ScriptResult;
import org.ow2.proactive.connector.iaas.service.InstanceScriptService;

import com.google.common.collect.Lists;


public class InstanceScriptRestTest {
    @InjectMocks
    private InstanceScriptRest instanceScriptRest;

    @Mock
    private InstanceScriptService instanceScriptService;

    @Before
    public void init() {
        MockitoAnnotations.initMocks(this);
    }

    @Test
    public void testExecuteScriptByInstanceId() {
        ScriptResult scriptResult = new ScriptResult("instanceId", "output", "error");
        when(instanceScriptService.executeScriptOnInstance(Mockito.anyString(), Mockito.anyString(),
                Mockito.any(InstanceScript.class))).thenReturn(scriptResult);
        assertThat(
                instanceScriptRest
                        .executeScriptByInstanceId("infrastructureId", "instanceId",
                                InstanceScriptFixture.getInstanceScriptAsaString(new String[] {}))
                        .getStatus(),
                is(Response.Status.OK.getStatusCode()));
        verify(instanceScriptService, times(1)).executeScriptOnInstance(Mockito.anyString(),
                Mockito.anyString(), Mockito.any(InstanceScript.class));
    }

    @Test
    public void testExecuteScriptByInstanceTag() {
        ScriptResult scriptResult = new ScriptResult("instanceId", "output", "error");
        when(instanceScriptService.executeScriptOnInstanceTag(Mockito.anyString(), Mockito.anyString(),
                Mockito.any(InstanceScript.class))).thenReturn(Lists.newArrayList(scriptResult));
        assertThat(
                instanceScriptRest
                        .executeScriptByInstanceTag("infrastructureId", "instanceTag",
                                InstanceScriptFixture.getInstanceScriptAsaString(new String[] {}))
                        .getStatus(),
                is(Response.Status.OK.getStatusCode()));
        verify(instanceScriptService, times(1)).executeScriptOnInstanceTag(Mockito.anyString(),
                Mockito.anyString(), Mockito.any(InstanceScript.class));
    }
}

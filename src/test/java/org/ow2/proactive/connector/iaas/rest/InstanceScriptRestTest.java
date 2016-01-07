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
import org.ow2.proactive.connector.iaas.rest.InstanceScriptRest;
import org.ow2.proactive.connector.iaas.service.InstanceScriptService;


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
    public void testExecuteScript() {
        ScriptResult scriptResult = new ScriptResult("output", "error");
        when(instanceScriptService.executeScriptOnInstance(Mockito.anyString(),
                Mockito.any(InstanceScript.class))).thenReturn(scriptResult);
        assertThat(
                instanceScriptRest
                        .executeScript("infrastructureId",
                                InstanceScriptFixture.getInstanceScriptAsaString("id", new String[] {}))
                        .getStatus(),
                is(Response.Status.OK.getStatusCode()));
        verify(instanceScriptService, times(1)).executeScriptOnInstance(Mockito.anyString(),
                Mockito.any(InstanceScript.class));
    }
}

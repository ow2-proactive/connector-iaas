package org.ow2.proactive.connector.iaas.service;

import static org.hamcrest.Matchers.is;
import static org.junit.Assert.assertThat;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.List;

import org.junit.Before;
import org.junit.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.ow2.proactive.connector.iaas.cloud.CloudManager;
import org.ow2.proactive.connector.iaas.fixtures.InfrastructureFixture;
import org.ow2.proactive.connector.iaas.fixtures.InstanceScriptFixture;
import org.ow2.proactive.connector.iaas.model.Infrastructure;
import org.ow2.proactive.connector.iaas.model.InstanceScript;
import org.ow2.proactive.connector.iaas.model.ScriptResult;

import com.google.common.collect.Lists;


public class InstanceScriptServiceTest {

    @InjectMocks
    private InstanceScriptService instanceScriptService;

    @Mock
    private InfrastructureService infrastructureService;

    @Mock
    private CloudManager cloudManager;

    @Before
    public void init() {
        MockitoAnnotations.initMocks(this);
    }

    @Test
    public void testExecuteScriptOnInstance() {
        Infrastructure infrastructure = InfrastructureFixture.getInfrastructure("id-aws", "aws", "endPoint",
                "userName", "password");
        when(infrastructureService.getInfrastructure(infrastructure.getId())).thenReturn(infrastructure);

        InstanceScript instanceScript = InstanceScriptFixture.simpleInstanceScriptNoscripts();

        when(cloudManager.executeScriptOnInstanceId(infrastructure, "instanceId", instanceScript))
                .thenReturn(new ScriptResult("instanceId", "output", "error"));

        ScriptResult scriptResult = instanceScriptService.executeScriptOnInstance(infrastructure.getId(),
                "instanceId", instanceScript);

        assertThat(scriptResult.getOutput(), is(scriptResult.getOutput()));
        assertThat(scriptResult.getError(), is(scriptResult.getError()));

        verify(cloudManager, times(1)).executeScriptOnInstanceId(infrastructure, "instanceId",
                instanceScript);

    }

    @Test
    public void testExecuteScriptOnInstanceTag() {
        Infrastructure infrastructure = InfrastructureFixture.getInfrastructure("id-aws", "aws", "endPoint",
                "userName", "password");
        when(infrastructureService.getInfrastructure(infrastructure.getId())).thenReturn(infrastructure);

        InstanceScript instanceScript = InstanceScriptFixture.simpleInstanceScriptNoscripts();

        when(cloudManager.executeScriptOnInstanceTag(infrastructure, "instanceTag", instanceScript))
                .thenReturn(Lists.newArrayList(new ScriptResult("instanceId", "output", "error")));

        List<ScriptResult> scriptResults = instanceScriptService
                .executeScriptOnInstanceTag(infrastructure.getId(), "instanceTag", instanceScript);

        assertThat(scriptResults.get(0).getInstanceId(), is("instanceId"));
        assertThat(scriptResults.get(0).getOutput(), is("output"));
        assertThat(scriptResults.get(0).getError(), is("error"));

        verify(cloudManager, times(1)).executeScriptOnInstanceTag(infrastructure, "instanceTag",
                instanceScript);

    }

    @Test(expected = javax.ws.rs.NotFoundException.class)
    public void testExecuteScriptOnInstanceException() {

        InstanceScript instanceScript = InstanceScriptFixture.simpleInstanceScriptNoscripts();

        Infrastructure infrastructure = InfrastructureFixture.getInfrastructure("id-aws", "aws", "endPoint",
                "userName", "password");
        when(infrastructureService.getInfrastructure(infrastructure.getId())).thenReturn(null);

        instanceScriptService.executeScriptOnInstance(infrastructure.getId(), "instanceId", instanceScript);

        verify(cloudManager, times(0)).executeScriptOnInstanceId(infrastructure, "instanceId",
                instanceScript);

    }

    @Test(expected = javax.ws.rs.NotFoundException.class)
    public void testExecuteScriptOnInstanceTagException() {

        InstanceScript instanceScript = InstanceScriptFixture.simpleInstanceScriptNoscripts();

        Infrastructure infrastructure = InfrastructureFixture.getInfrastructure("id-aws", "aws", "endPoint",
                "userName", "password");
        when(infrastructureService.getInfrastructure(infrastructure.getId())).thenReturn(null);

        instanceScriptService.executeScriptOnInstanceTag(infrastructure.getId(), "instanceTag",
                instanceScript);

        verify(cloudManager, times(0)).executeScriptOnInstanceTag(infrastructure, "instanceTag",
                instanceScript);

    }

}

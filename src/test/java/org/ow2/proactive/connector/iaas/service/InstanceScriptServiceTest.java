/*
 * ProActive Parallel Suite(TM):
 * The Open Source library for parallel and distributed
 * Workflows & Scheduling, Orchestration, Cloud Automation
 * and Big Data Analysis on Enterprise Grids & Clouds.
 *
 * Copyright (c) 2007 - 2017 ActiveEon
 * Contact: contact@activeeon.com
 *
 * This library is free software: you can redistribute it and/or
 * modify it under the terms of the GNU Affero General Public License
 * as published by the Free Software Foundation: version 3 of
 * the License.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Affero General Public License for more details.
 *
 * You should have received a copy of the GNU Affero General Public License
 * along with this program. If not, see <http://www.gnu.org/licenses/>.
 *
 * If needed, contact us to obtain a release under GPL Version 2 or 3
 * or a different license than the AGPL.
 */
package org.ow2.proactive.connector.iaas.service;

import static org.hamcrest.Matchers.is;
import static org.junit.Assert.assertThat;
import static org.junit.Assert.assertTrue;
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
        Infrastructure infrastructure = InfrastructureFixture.getInfrastructure("id-aws",
                                                                                "aws",
                                                                                "endPoint",
                                                                                "userName",
                                                                                "password");
        when(infrastructureService.getInfrastructure(infrastructure.getId())).thenReturn(infrastructure);

        InstanceScript instanceScript = InstanceScriptFixture.simpleInstanceScriptNoscripts();

        when(cloudManager.executeScriptOnInstanceId(infrastructure,
                                                    "instanceId",
                                                    instanceScript)).thenReturn(Lists.newArrayList(new ScriptResult("instanceId",
                                                                                                                    "output",
                                                                                                                    "error")));

        List<ScriptResult> scriptResults = instanceScriptService.executeScriptOnInstance(infrastructure.getId(),
                                                                                         "instanceId",
                                                                                         instanceScript);

        assertTrue(scriptResults.size() == 1);
        assertThat(scriptResults.get(0).getOutput(), is("output"));
        assertThat(scriptResults.get(0).getError(), is("error"));

        verify(cloudManager, times(1)).executeScriptOnInstanceId(infrastructure, "instanceId", instanceScript);

    }

    @Test
    public void testExecuteScriptOnInstanceTag() {
        Infrastructure infrastructure = InfrastructureFixture.getInfrastructure("id-aws",
                                                                                "aws",
                                                                                "endPoint",
                                                                                "userName",
                                                                                "password");
        when(infrastructureService.getInfrastructure(infrastructure.getId())).thenReturn(infrastructure);

        InstanceScript instanceScript = InstanceScriptFixture.simpleInstanceScriptNoscripts();

        when(cloudManager.executeScriptOnInstanceTag(infrastructure,
                                                     "instanceTag",
                                                     instanceScript)).thenReturn(Lists.newArrayList(new ScriptResult("instanceId",
                                                                                                                     "output",
                                                                                                                     "error")));

        List<ScriptResult> scriptResults = instanceScriptService.executeScriptOnInstanceTag(infrastructure.getId(),
                                                                                            "instanceTag",
                                                                                            instanceScript);

        assertThat(scriptResults.get(0).getInstanceId(), is("instanceId"));
        assertThat(scriptResults.get(0).getOutput(), is("output"));
        assertThat(scriptResults.get(0).getError(), is("error"));

        verify(cloudManager, times(1)).executeScriptOnInstanceTag(infrastructure, "instanceTag", instanceScript);

    }

    @Test(expected = javax.ws.rs.NotFoundException.class)
    public void testExecuteScriptOnInstanceException() {

        InstanceScript instanceScript = InstanceScriptFixture.simpleInstanceScriptNoscripts();

        Infrastructure infrastructure = InfrastructureFixture.getInfrastructure("id-aws",
                                                                                "aws",
                                                                                "endPoint",
                                                                                "userName",
                                                                                "password");
        when(infrastructureService.getInfrastructure(infrastructure.getId())).thenReturn(null);

        instanceScriptService.executeScriptOnInstance(infrastructure.getId(), "instanceId", instanceScript);

        verify(cloudManager, times(0)).executeScriptOnInstanceId(infrastructure, "instanceId", instanceScript);

    }

    @Test(expected = javax.ws.rs.NotFoundException.class)
    public void testExecuteScriptOnInstanceTagException() {

        InstanceScript instanceScript = InstanceScriptFixture.simpleInstanceScriptNoscripts();

        Infrastructure infrastructure = InfrastructureFixture.getInfrastructure("id-aws",
                                                                                "aws",
                                                                                "endPoint",
                                                                                "userName",
                                                                                "password");
        when(infrastructureService.getInfrastructure(infrastructure.getId())).thenReturn(null);

        instanceScriptService.executeScriptOnInstanceTag(infrastructure.getId(), "instanceTag", instanceScript);

        verify(cloudManager, times(0)).executeScriptOnInstanceTag(infrastructure, "instanceTag", instanceScript);

    }

}

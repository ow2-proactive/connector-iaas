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

        when(instanceScriptService.executeScriptOnInstance(Mockito.anyString(),
                                                           Mockito.anyString(),
                                                           Mockito.any(InstanceScript.class))).thenReturn(Lists.newArrayList(scriptResult));

        assertThat(instanceScriptRest.executeScript("infrastructureId",
                                                    "instanceId",
                                                    "tag",
                                                    InstanceScriptFixture.getInstanceScriptAsaString(new String[] {}))
                                     .getStatus(),
                   is(Response.Status.OK.getStatusCode()));

        verify(instanceScriptService, times(1)).executeScriptOnInstance(Mockito.anyString(),
                                                                        Mockito.anyString(),
                                                                        Mockito.any(InstanceScript.class));

        verify(instanceScriptService, times(0)).executeScriptOnInstanceTag(Mockito.anyString(),
                                                                           Mockito.anyString(),
                                                                           Mockito.any(InstanceScript.class));
    }

    @Test
    public void testExecuteScriptByInstanceTag() {
        ScriptResult scriptResult = new ScriptResult("instanceId", "output", "error");
        when(instanceScriptService.executeScriptOnInstanceTag(Mockito.anyString(),
                                                              Mockito.anyString(),
                                                              Mockito.any(InstanceScript.class))).thenReturn(Lists.newArrayList(scriptResult));
        assertThat(instanceScriptRest.executeScript("infrastructureId",
                                                    null,
                                                    "instanceTag",
                                                    InstanceScriptFixture.getInstanceScriptAsaString(new String[] {}))
                                     .getStatus(),
                   is(Response.Status.OK.getStatusCode()));
        verify(instanceScriptService, times(1)).executeScriptOnInstanceTag(Mockito.anyString(),
                                                                           Mockito.anyString(),
                                                                           Mockito.any(InstanceScript.class));

        verify(instanceScriptService, times(0)).executeScriptOnInstance(Mockito.anyString(),
                                                                        Mockito.anyString(),
                                                                        Mockito.any(InstanceScript.class));
    }
}

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
        instanceStringFixture = InstanceFixture.getInstanceAsaString("instance-id",
                                                                     "name",
                                                                     "image",
                                                                     "number",
                                                                     "cpu",
                                                                     "ram",
                                                                     "publicIP",
                                                                     "privateIP",
                                                                     "running");
        instanceFixture = InstanceFixture.getInstance("instance-id",
                                                      "name",
                                                      "image",
                                                      "number",
                                                      "cpu",
                                                      "ram",
                                                      "publicIP",
                                                      "privateIP",
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
        assertThat(instanceRest.getInstances("infrastructureId", null, null).getStatus(),
                   is(Response.Status.OK.getStatusCode()));
        verify(instanceService, times(1)).getAllInstances("infrastructureId");
    }

    @Test
    public void testGetInstanceById() {
        assertThat(instanceRest.getInstances("infrastructureId", "instanceID", null).getStatus(),
                   is(Response.Status.OK.getStatusCode()));
        verify(instanceService, times(1)).getInstanceById("infrastructureId", "instanceID");
    }

    @Test
    public void testGetInstanceByTag() {
        assertThat(instanceRest.getInstances("infrastructureId", null, "instanceTAG").getStatus(),
                   is(Response.Status.OK.getStatusCode()));
        verify(instanceService, times(1)).getInstanceByTag("infrastructureId", "instanceTAG");
    }

    @Test
    public void testDeleteInstance() {
        assertThat(instanceRest.deleteInstance("infrastructureId", "instanceID", "instanceTAG").getStatus(),
                   is(Response.Status.OK.getStatusCode()));
        verify(instanceService, times(1)).deleteInstance("infrastructureId", "instanceID");
    }

    @Test
    public void testDeleteInstanceByTag() {
        assertThat(instanceRest.deleteInstance("infrastructureId", null, "instanceTAG").getStatus(),
                   is(Response.Status.OK.getStatusCode()));
        verify(instanceService, times(1)).deleteInstanceByTag("infrastructureId", "instanceTAG");
    }

}

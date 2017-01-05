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
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.Set;

import org.jclouds.compute.RunNodesException;
import org.junit.Before;
import org.junit.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.ow2.proactive.connector.iaas.cloud.CloudManager;
import org.ow2.proactive.connector.iaas.fixtures.InfrastructureFixture;
import org.ow2.proactive.connector.iaas.fixtures.InstanceFixture;
import org.ow2.proactive.connector.iaas.model.Infrastructure;
import org.ow2.proactive.connector.iaas.model.Instance;

import jersey.repackaged.com.google.common.collect.Sets;


public class InstanceServiceTest {

    @InjectMocks
    private InstanceService instanceService;

    @Mock
    private InfrastructureService infrastructureService;

    @Mock
    private CloudManager cloudManager;

    @Before
    public void init() {
        MockitoAnnotations.initMocks(this);

    }

    @Test
    public void testCreateInstance() throws NumberFormatException, RunNodesException {

        Infrastructure infratructure = InfrastructureFixture.getInfrastructure("id-aws",
                                                                               "aws",
                                                                               "endPoint",
                                                                               "userName",
                                                                               "password");
        when(infrastructureService.getInfrastructure(infratructure.getId())).thenReturn(infratructure);

        Instance instance = InstanceFixture.getInstance("instance-id",
                                                        "instance-name",
                                                        "image",
                                                        "2",
                                                        "512",
                                                        "cpu",
                                                        "publicIP",
                                                        "privateIP",
                                                        "running");

        when(cloudManager.createInstance(infratructure,
                                         instance)).thenReturn(Sets.newHashSet(InstanceFixture.simpleInstance("id")));

        Set<Instance> created = instanceService.createInstance("id-aws", instance);

        assertThat(created.size(), is(1));

        verify(cloudManager, times(1)).createInstance(infratructure, instance);

    }

    @Test(expected = javax.ws.rs.NotFoundException.class)
    public void testCreateInstanceException() throws NumberFormatException, RunNodesException {

        Infrastructure infrastructure = InfrastructureFixture.getInfrastructure("id-aws",
                                                                                "aws",
                                                                                "endPoint",
                                                                                "userName",
                                                                                "password");
        Instance instance = InstanceFixture.getInstance("instance-id",
                                                        "instance-name",
                                                        "image",
                                                        "2",
                                                        "512",
                                                        "cpu",
                                                        "publicIP",
                                                        "privateIP",
                                                        "running");

        when(infrastructureService.getInfrastructure(infrastructure.getId())).thenReturn(null);

        instanceService.createInstance("id-aws", instance);

        verify(cloudManager, times(0)).createInstance(infrastructure, instance);

    }

    @Test
    public void testDeleteInstance() throws NumberFormatException, RunNodesException {

        Infrastructure infratructure = InfrastructureFixture.getInfrastructure("id-aws",
                                                                               "aws",
                                                                               "endPoint",
                                                                               "userName",
                                                                               "password");
        when(infrastructureService.getInfrastructure(infratructure.getId())).thenReturn(infratructure);

        instanceService.deleteInstance(infratructure.getId(), "instanceID");

        verify(cloudManager, times(1)).deleteInstance(infratructure, "instanceID");

    }

    @Test
    public void testDeleteInstanceInfrastructureIdDoesNotExists() throws NumberFormatException, RunNodesException {

        Infrastructure infratructure = InfrastructureFixture.getInfrastructure("id-aws",
                                                                               "aws",
                                                                               "endPoint",
                                                                               "userName",
                                                                               "password");
        when(infrastructureService.getInfrastructure(infratructure.getId())).thenReturn(null);

        instanceService.deleteInstance(infratructure.getId(), "instanceID");

        verify(cloudManager, times(0)).deleteInstance(infratructure, "instanceID");

    }

    @Test
    public void testDeleteInstanceByTag() throws NumberFormatException, RunNodesException {

        Infrastructure infratructure = InfrastructureFixture.getInfrastructure("id-aws",
                                                                               "aws",
                                                                               "endPoint",
                                                                               "userName",
                                                                               "password");
        when(infrastructureService.getInfrastructure(infratructure.getId())).thenReturn(infratructure);

        when(cloudManager.getAllInfrastructureInstances(infratructure)).thenReturn(Sets.newHashSet(InstanceFixture.simpleInstanceWithTag("id111",
                                                                                                                                         "someTag"),
                                                                                                   InstanceFixture.simpleInstanceWithTag("id112",
                                                                                                                                         "someTag2")));

        instanceService.deleteInstanceByTag(infratructure.getId(), "someTag");

        verify(cloudManager, times(1)).deleteInstance(infratructure, "id111");
        verify(cloudManager, times(0)).deleteInstance(infratructure, "id112");
    }

    @Test(expected = javax.ws.rs.NotFoundException.class)
    public void testDeleteInstanceByTagInfrastructureIdDoesNotExists() throws NumberFormatException, RunNodesException {

        Infrastructure infratructure = InfrastructureFixture.getInfrastructure("id-aws",
                                                                               "aws",
                                                                               "endPoint",
                                                                               "userName",
                                                                               "password");
        when(infrastructureService.getInfrastructure(infratructure.getId())).thenReturn(null);

        instanceService.deleteInstanceByTag(infratructure.getId(), "someTag");

        verify(cloudManager, times(0)).deleteInstance(infratructure, "id111");
        verify(cloudManager, times(0)).deleteInstance(infratructure, "id112");
    }

    @Test
    public void testGetAllInstances() throws NumberFormatException, RunNodesException {

        Infrastructure infratructure = InfrastructureFixture.getInfrastructure("id-aws",
                                                                               "aws",
                                                                               "endPoint",
                                                                               "userName",
                                                                               "password");
        when(infrastructureService.getInfrastructure(infratructure.getId())).thenReturn(infratructure);

        when(cloudManager.getAllInfrastructureInstances(infratructure)).thenReturn(Sets.newHashSet(InstanceFixture.simpleInstance("id")));

        Set<Instance> created = instanceService.getAllInstances(infratructure.getId());

        assertThat(created.size(), is(1));

        verify(cloudManager, times(1)).getAllInfrastructureInstances(infratructure);

    }

    @Test(expected = javax.ws.rs.NotFoundException.class)
    public void testGetAllInstancesInfrastructureIdDoesNotExists() throws NumberFormatException, RunNodesException {

        Infrastructure infratructure = InfrastructureFixture.getInfrastructure("id-aws",
                                                                               "aws",
                                                                               "endPoint",
                                                                               "userName",
                                                                               "password");
        when(infrastructureService.getInfrastructure(infratructure.getId())).thenReturn(null);

        instanceService.getAllInstances(infratructure.getId());

        verify(cloudManager, times(0)).getAllInfrastructureInstances(infratructure);

    }

    @Test
    public void testGetInstanceByTag() throws NumberFormatException, RunNodesException {

        Infrastructure infratructure = InfrastructureFixture.getInfrastructure("id-aws",
                                                                               "aws",
                                                                               "endPoint",
                                                                               "userName",
                                                                               "password");
        when(infrastructureService.getInfrastructure(infratructure.getId())).thenReturn(infratructure);

        when(cloudManager.getAllInfrastructureInstances(infratructure)).thenReturn(Sets.newHashSet(InstanceFixture.simpleInstance("id")));

        instanceService.getInstanceByTag(infratructure.getId(), "instanceTAG");

        verify(cloudManager, times(1)).getAllInfrastructureInstances(infratructure);

    }

    @Test
    public void testGetInstanceById() throws NumberFormatException, RunNodesException {

        Infrastructure infratructure = InfrastructureFixture.getInfrastructure("id-aws",
                                                                               "aws",
                                                                               "endPoint",
                                                                               "userName",
                                                                               "password");
        when(infrastructureService.getInfrastructure(infratructure.getId())).thenReturn(infratructure);

        when(cloudManager.getAllInfrastructureInstances(infratructure)).thenReturn(Sets.newHashSet(InstanceFixture.simpleInstance("id")));

        instanceService.getInstanceById(infratructure.getId(), "id");

        verify(cloudManager, times(1)).getAllInfrastructureInstances(infratructure);

    }
}

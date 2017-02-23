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
import static org.mockito.Mockito.inOrder;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import org.junit.Before;
import org.junit.Test;
import org.mockito.InOrder;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.ow2.proactive.connector.iaas.cache.InfrastructureCache;
import org.ow2.proactive.connector.iaas.cache.InstanceCache;
import org.ow2.proactive.connector.iaas.cloud.CloudManager;
import org.ow2.proactive.connector.iaas.fixtures.InfrastructureFixture;
import org.ow2.proactive.connector.iaas.model.Infrastructure;

import jersey.repackaged.com.google.common.collect.ImmutableMap;


public class InfrastructureServiceTest {

    @InjectMocks
    private InfrastructureService infrastructureService;

    @Mock
    private InstanceService instanceService;

    @Mock
    private InfrastructureCache infrastructureCache;

    @Mock
    private InstanceCache instanceCache;

    @Mock
    private CloudManager cloudManager;

    private ImmutableMap<String, Infrastructure> mockSupportedInfrastructures;

    @Before
    public void init() {
        MockitoAnnotations.initMocks(this);
    }

    @Test
    public void testRegisterInfrastructure() {
        Infrastructure infrastructure = InfrastructureFixture.getInfrastructure("id-aws",
                                                                                "aws",
                                                                                "endPoint",
                                                                                "userName",
                                                                                "password");
        infrastructureService.registerInfrastructure(infrastructure);
        verify(infrastructureCache, times(1)).registerInfrastructure(infrastructure);
    }

    @Test
    public void testGetInfrastructureByName() {
        Infrastructure infrastructure = InfrastructureFixture.getInfrastructure("id-aws",
                                                                                "aws",
                                                                                "endPoint",
                                                                                "userName",
                                                                                "password");
        mockSupportedInfrastructures = ImmutableMap.of("aws", infrastructure);
        when(infrastructureCache.getSupportedInfrastructures()).thenReturn(mockSupportedInfrastructures);
        infrastructureService.registerInfrastructure(infrastructure);
        assertThat(infrastructureCache.getSupportedInfrastructures().get("aws"), is(infrastructure));
    }

    @Test
    public void testDeleteInfrastructure() {
        Infrastructure infrastructure = InfrastructureFixture.getInfrastructure("id-aws",
                                                                                "aws",
                                                                                "endPoint",
                                                                                "userName",
                                                                                "password");
        mockSupportedInfrastructures = ImmutableMap.of("id-aws", infrastructure);
        when(infrastructureCache.getSupportedInfrastructures()).thenReturn(mockSupportedInfrastructures);
        infrastructureService.deleteInfrastructure(infrastructure, false);

        InOrder inOrder = inOrder(cloudManager, infrastructureCache);
        inOrder.verify(cloudManager, times(1)).deleteInfrastructure(infrastructure);
        inOrder.verify(infrastructureCache, times(1)).deleteInfrastructure(infrastructure);
    }

    @Test
    public void testDeleteInfrastructureWithInstances() {
        Infrastructure infrastructure = InfrastructureFixture.getInfrastructure("id-aws",
                                                                                "aws",
                                                                                "endPoint",
                                                                                "userName",
                                                                                "password");
        mockSupportedInfrastructures = ImmutableMap.of("id-aws", infrastructure);
        when(infrastructureCache.getSupportedInfrastructures()).thenReturn(mockSupportedInfrastructures);
        infrastructureService.deleteInfrastructure(infrastructure, true);

        InOrder inOrder = inOrder(cloudManager, infrastructureCache, instanceCache);
        inOrder.verify(cloudManager, times(1)).deleteInfrastructure(infrastructure);
        inOrder.verify(infrastructureCache, times(1)).deleteInfrastructure(infrastructure);
        inOrder.verify(instanceCache, times(1)).deleteInfrastructure(infrastructure);

        verify(instanceService, times(1)).deleteCreatedInstances(infrastructure.getId());
    }

    @Test
    public void testGetAllSupportedInfrastructure() {
        Infrastructure infrastructure = InfrastructureFixture.getInfrastructure("id-aws",
                                                                                "aws",
                                                                                "endPoint",
                                                                                "userName",
                                                                                "password");
        mockSupportedInfrastructures = ImmutableMap.of("aws", infrastructure);
        when(infrastructureCache.getSupportedInfrastructures()).thenReturn(mockSupportedInfrastructures);
        assertThat(infrastructureService.getAllSupportedInfrastructure().get("aws"), is(infrastructure));
        assertThat(infrastructureService.getAllSupportedInfrastructure().size(), is(1));
    }
}

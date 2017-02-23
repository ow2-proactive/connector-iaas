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
import org.ow2.proactive.connector.iaas.fixtures.InfrastructureFixture;
import org.ow2.proactive.connector.iaas.fixtures.InstanceFixture;
import org.ow2.proactive.connector.iaas.model.Infrastructure;
import org.ow2.proactive.connector.iaas.model.Instance;
import org.ow2.proactive.connector.iaas.service.InfrastructureService;
import org.ow2.proactive.connector.iaas.service.InstanceService;

import jersey.repackaged.com.google.common.collect.Maps;


public class InfrastructureRestTest {
    @InjectMocks
    private InfrastructureRest infrastructureRest;

    @Mock
    private InfrastructureService infrastructureService;

    @Mock
    private InstanceService instanceService;

    private String infrastructureStringFixture;

    private Infrastructure infrastructureFixture;

    private Instance instanceFixture;

    @Before
    public void init() {
        MockitoAnnotations.initMocks(this);
        infrastructureStringFixture = InfrastructureFixture.getInfrastructureAsaString("id-openstack",
                                                                                       "openstack",
                                                                                       "endPoint",
                                                                                       "userName",
                                                                                       "password");
        infrastructureFixture = InfrastructureFixture.getInfrastructure("id-openstack",
                                                                        "openstack",
                                                                        "endPoint",
                                                                        "userName",
                                                                        "password");

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
    public void testGetAllSupportedInfrastructure() {
        when(infrastructureService.getAllSupportedInfrastructure()).thenReturn(Maps.newHashMap());
        assertThat(infrastructureRest.getAllSupportedInfrastructure().getStatus(),
                   is(Response.Status.OK.getStatusCode()));
        verify(infrastructureService, times(1)).getAllSupportedInfrastructure();
    }

    @Test
    public void testRegisterInfrastructure() {
        assertThat(infrastructureRest.registerInfrastructure(infrastructureStringFixture).getStatus(),
                   is(Response.Status.OK.getStatusCode()));
        verify(infrastructureService, times(1)).registerInfrastructure(infrastructureFixture);
    }

    @Test
    public void testDeleteInfrastructureById() {
        when(infrastructureService.getInfrastructure(InfrastructureFixture.getSimpleInfrastructure("sometype")
                                                                          .getId())).thenReturn(InfrastructureFixture.getSimpleInfrastructure("sometype"));

        assertThat(infrastructureRest.deleteInfrastructureById(InfrastructureFixture.getSimpleInfrastructure("sometype")
                                                                                    .getId(),
                                                               null)
                                     .getStatus(),
                   is(Response.Status.OK.getStatusCode()));
        verify(infrastructureService, times(1)).deleteInfrastructure(
                                                                     InfrastructureFixture.getSimpleInfrastructure("sometype"),
                                                                     false);
        verify(infrastructureService, times(1)).getAllSupportedInfrastructure();
    }

    @Test
    public void testDeleteInfrastructureByIdNotInCache() {
        assertThat(infrastructureRest.deleteInfrastructureById("openstack", null).getStatus(),
                   is(Response.Status.OK.getStatusCode()));
        verify(infrastructureService, times(0)).deleteInfrastructure(Mockito.any(Infrastructure.class),
                                                                     Mockito.anyBoolean());
        verify(infrastructureService, times(1)).getAllSupportedInfrastructure();
    }

    @Test
    public void testDeleteInfrastructureWithInstances() {
        Infrastructure infra = InfrastructureFixture.getSimpleInfrastructure("sometype");

        when(infrastructureService.getInfrastructure(InfrastructureFixture.getSimpleInfrastructure("sometype")
                                                                          .getId())).thenReturn(infra);

        assertThat(infrastructureRest.deleteInfrastructureById(InfrastructureFixture.getSimpleInfrastructure("sometype")
                                                                                    .getId(),
                                                               true)
                                     .getStatus(),
                   is(Response.Status.OK.getStatusCode()));

        verify(infrastructureService, times(1)).deleteInfrastructure(
                                                                     InfrastructureFixture.getSimpleInfrastructure("sometype"),
                                                                     true);
        verify(infrastructureService, times(1)).getAllSupportedInfrastructure();
    }

    @Test
    public void testGetInfrastructureById() {
        assertThat(infrastructureRest.getInfrastructure("openstack").getStatus(),
                   is(Response.Status.OK.getStatusCode()));
        verify(infrastructureService, times(1)).getInfrastructure(("openstack"));
    }

}

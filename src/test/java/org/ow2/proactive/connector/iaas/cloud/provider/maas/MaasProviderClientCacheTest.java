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
package org.ow2.proactive.connector.iaas.cloud.provider.maas;

import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.not;
import static org.hamcrest.Matchers.nullValue;
import static org.junit.Assert.assertThat;
import static org.mockito.Matchers.any;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import org.junit.Before;
import org.junit.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.ow2.proactive.connector.iaas.cloud.provider.azure.AzureServiceBuilder;
import org.ow2.proactive.connector.iaas.cloud.provider.azure.AzureServiceCache;
import org.ow2.proactive.connector.iaas.fixtures.InfrastructureFixture;
import org.ow2.proactive.connector.iaas.model.Infrastructure;
import org.ow2.proactive.connector.maas.MaasClient;

import com.microsoft.azure.management.Azure;


/**
 * @author ActiveEon Team
 * @since 12/03/17
 */
public class MaasProviderClientCacheTest {

    @InjectMocks
    private MaasProviderClientCache maasProviderClientCache;

    @Mock
    private MaasProviderClientBuilder maasProviderClientBuilder;

    @Mock
    private MaasClient maasClient;

    @Before
    public void init() {
        MockitoAnnotations.initMocks(this);
        when(maasProviderClientBuilder.buildMaasClientFromInfrastructure(any(Infrastructure.class))).thenReturn(maasClient);
    }

    @Test
    public void testGetServiceInstanceFirstTime() {
        MaasClient maasClient = maasProviderClientCache.getMaasClient(InfrastructureFixture.getInfrastructure("id-maas",
                                                                                                              "maas",
                                                                                                              "endpoint",
                                                                                                              null,
                                                                                                              "apiToken"));
        assertThat(maasClient, is(not(nullValue())));
        verify(maasProviderClientBuilder,
               times(1)).buildMaasClientFromInfrastructure(InfrastructureFixture.getInfrastructure("id-maas",
                                                                                                   "maas",
                                                                                                   "endpoint",
                                                                                                   null,
                                                                                                   "apiToken"));
    }

    @Test
    public void testGetServiceInstanceManyTimeSameInfrastructure() {
        maasProviderClientCache.getMaasClient(InfrastructureFixture.getInfrastructure("id-maas",
                                                                                      "maas",
                                                                                      "endpoint",
                                                                                      null,
                                                                                      "apiToken"));
        maasProviderClientCache.getMaasClient(InfrastructureFixture.getInfrastructure("id-maas",
                                                                                      "maas",
                                                                                      "endpoint",
                                                                                      null,
                                                                                      "apiToken"));
        maasProviderClientCache.getMaasClient(InfrastructureFixture.getInfrastructure("id-maas",
                                                                                      "maas",
                                                                                      "endpoint",
                                                                                      null,
                                                                                      "apiToken"));

        verify(maasProviderClientBuilder,
               times(1)).buildMaasClientFromInfrastructure(InfrastructureFixture.getInfrastructure("id-maas",
                                                                                                   "maas",
                                                                                                   "endpoint",
                                                                                                   null,
                                                                                                   "apiToken"));
    }

    @Test
    public void testGetServiceInstanceDifferentInfrastructure() {
        maasProviderClientCache.getMaasClient(InfrastructureFixture.getInfrastructure("id-maas",
                                                                                      "maas",
                                                                                      "endpoint",
                                                                                      null,
                                                                                      "apiToken"));
        maasProviderClientCache.getMaasClient(InfrastructureFixture.getInfrastructure("id-maas-2",
                                                                                      "maas",
                                                                                      "endpoint",
                                                                                      null,
                                                                                      "apiToken"));

        verify(maasProviderClientBuilder, times(2)).buildMaasClientFromInfrastructure(any(Infrastructure.class));
    }

    @Test
    public void testRemoveServiceInstance() {
        maasProviderClientCache.getMaasClient(InfrastructureFixture.getInfrastructure("id-maas",
                                                                                      "maas",
                                                                                      "endpoint",
                                                                                      null,
                                                                                      "apiToken"));

        maasProviderClientCache.removeMaasClient(InfrastructureFixture.getInfrastructure("id-maas",
                                                                                         "maas",
                                                                                         "endpoint",
                                                                                         null,
                                                                                         "apiToken"));

        maasProviderClientCache.getMaasClient(InfrastructureFixture.getInfrastructure("id-maas",
                                                                                      "maas",
                                                                                      "endpoint",
                                                                                      null,
                                                                                      "apiToken"));

        verify(maasProviderClientBuilder,
               times(2)).buildMaasClientFromInfrastructure(InfrastructureFixture.getInfrastructure("id-maas",
                                                                                                   "maas",
                                                                                                   "endpoint",
                                                                                                   null,
                                                                                                   "apiToken"));
    }
}

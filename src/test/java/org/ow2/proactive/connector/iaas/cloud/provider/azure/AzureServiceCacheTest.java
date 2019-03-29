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
package org.ow2.proactive.connector.iaas.cloud.provider.azure;

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
import org.ow2.proactive.connector.iaas.fixtures.InfrastructureFixture;
import org.ow2.proactive.connector.iaas.model.Infrastructure;

import com.microsoft.azure.management.Azure;


/**
 * @author ActiveEon Team
 * @since 12/03/17
 */
public class AzureServiceCacheTest {

    @InjectMocks
    private AzureServiceCache azureServiceCache;

    @Mock
    private AzureServiceBuilder azureServiceBuilder;

    @Mock
    private Azure azureService;

    @Before
    public void init() {
        MockitoAnnotations.initMocks(this);
        when(azureServiceBuilder.buildServiceFromInfrastructure(any(Infrastructure.class))).thenReturn(azureService);
    }

    @Test
    public void testGetServiceInstanceFirstTime() {
        Azure azureService = azureServiceCache.getService(InfrastructureFixture.getInfrastructure("id-azure",
                                                                                                  "azure",
                                                                                                  null,
                                                                                                  "userName",
                                                                                                  "password",
                                                                                                  null,
                                                                                                  null,
                                                                                                  null));
        assertThat(azureService, is(not(nullValue())));
        verify(azureServiceBuilder,
               times(1)).buildServiceFromInfrastructure(InfrastructureFixture.getInfrastructure("id-azure",
                                                                                                "azure",
                                                                                                "endPoint",
                                                                                                "userName",
                                                                                                "password",
                                                                                                null,
                                                                                                null,
                                                                                                null));
    }

    @Test
    public void testGetServiceInstanceManyTimeSameInfrastructure() {
        azureServiceCache.getService(InfrastructureFixture.getInfrastructure("id-azure",
                                                                             "azure",
                                                                             "endPoint",
                                                                             "userName",
                                                                             "password",
                                                                             null,
                                                                             null,
                                                                             null));
        azureServiceCache.getService(InfrastructureFixture.getInfrastructure("id-azure",
                                                                             "azure",
                                                                             "endPoint",
                                                                             "userName",
                                                                             "password",
                                                                             null,
                                                                             null,
                                                                             null));
        azureServiceCache.getService(InfrastructureFixture.getInfrastructure("id-azure",
                                                                             "azure",
                                                                             "endPoint",
                                                                             "userName",
                                                                             "password",
                                                                             null,
                                                                             null,
                                                                             null));

        verify(azureServiceBuilder,
               times(1)).buildServiceFromInfrastructure(InfrastructureFixture.getInfrastructure("id-azure",
                                                                                                "azure",
                                                                                                "endPoint",
                                                                                                "userName",
                                                                                                "password",
                                                                                                null,
                                                                                                null,
                                                                                                null));
    }

    @Test
    public void testGetServiceInstanceDifferentInfrastructure() {
        azureServiceCache.getService(InfrastructureFixture.getInfrastructure("id-azure",
                                                                             "azure",
                                                                             "endPoint",
                                                                             "userName",
                                                                             "password",
                                                                             null,
                                                                             null,
                                                                             null));
        azureServiceCache.getService(InfrastructureFixture.getInfrastructure("id-azure2",
                                                                             "azure",
                                                                             "endPoint",
                                                                             "userName",
                                                                             "password",
                                                                             null,
                                                                             null,
                                                                             null));

        verify(azureServiceBuilder, times(2)).buildServiceFromInfrastructure(any(Infrastructure.class));
    }

    @Test
    public void testRemoveServiceInstance() {
        azureServiceCache.getService(InfrastructureFixture.getInfrastructure("id-azure",
                                                                             "azure",
                                                                             "endPoint",
                                                                             "userName",
                                                                             "password",
                                                                             null,
                                                                             null,
                                                                             null));

        azureServiceCache.removeService(InfrastructureFixture.getInfrastructure("id-azure",
                                                                                "azure",
                                                                                "endPoint",
                                                                                "userName",
                                                                                "password",
                                                                                null,
                                                                                null,
                                                                                null));

        azureServiceCache.getService(InfrastructureFixture.getInfrastructure("id-azure",
                                                                             "azure",
                                                                             "endPoint",
                                                                             "userName",
                                                                             "password",
                                                                             null,
                                                                             null,
                                                                             null));

        verify(azureServiceBuilder,
               times(2)).buildServiceFromInfrastructure(InfrastructureFixture.getInfrastructure("id-azure",
                                                                                                "azure",
                                                                                                "endPoint",
                                                                                                "userName",
                                                                                                "password",
                                                                                                null,
                                                                                                null,
                                                                                                null));
    }
}

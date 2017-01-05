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
package org.ow2.proactive.connector.iaas;

import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.Map;

import org.junit.Before;
import org.junit.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.ow2.proactive.connector.iaas.fixtures.InfrastructureFixture;
import org.ow2.proactive.connector.iaas.model.Infrastructure;
import org.ow2.proactive.connector.iaas.service.InfrastructureService;

import com.google.common.collect.Maps;


public class ShutdownHandlerTest {

    @InjectMocks
    private ShutdownHandler shutdownHandler;

    @Mock
    private InfrastructureService infrastructureService;

    @Before
    public void init() {
        MockitoAnnotations.initMocks(this);

    }

    @Test
    public void testRemoveAllInfrastructures() {
        Map<String, Infrastructure> supportedInfrastructureMap = Maps.newHashMap();
        supportedInfrastructureMap.put(InfrastructureFixture.getSimpleInfrastructure("type1", true).getId(),
                                       InfrastructureFixture.getSimpleInfrastructure("type1", true));
        supportedInfrastructureMap.put(InfrastructureFixture.getSimpleInfrastructure("type2", true).getId(),
                                       InfrastructureFixture.getSimpleInfrastructure("type2", true));
        supportedInfrastructureMap.put(InfrastructureFixture.getSimpleInfrastructure("type3", false).getId(),
                                       InfrastructureFixture.getSimpleInfrastructure("type3", false));

        when(infrastructureService.getAllSupportedInfrastructure()).thenReturn(supportedInfrastructureMap);

        shutdownHandler.removeAllInfrastructures();

        verify(infrastructureService,
               times(1)).deleteInfrastructure(InfrastructureFixture.getSimpleInfrastructure("type1"));
        verify(infrastructureService,
               times(1)).deleteInfrastructure(InfrastructureFixture.getSimpleInfrastructure("type2"));
        verify(infrastructureService,
               times(0)).deleteInfrastructure(InfrastructureFixture.getSimpleInfrastructure("type3"));

    }

}

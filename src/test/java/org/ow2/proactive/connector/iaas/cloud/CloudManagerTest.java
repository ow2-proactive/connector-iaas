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
package org.ow2.proactive.connector.iaas.cloud;

import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import org.junit.Before;
import org.junit.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.ow2.proactive.connector.iaas.cloud.provider.CloudProvider;
import org.ow2.proactive.connector.iaas.fixtures.InfrastructureFixture;
import org.ow2.proactive.connector.iaas.fixtures.InstanceFixture;
import org.ow2.proactive.connector.iaas.fixtures.InstanceScriptFixture;
import org.ow2.proactive.connector.iaas.model.Infrastructure;
import org.ow2.proactive.connector.iaas.model.Instance;
import org.ow2.proactive.connector.iaas.model.InstanceScript;
import org.ow2.proactive.connector.iaas.model.Tag;

import com.google.common.collect.Lists;


public class CloudManagerTest {

    private CloudManager cloudManager;

    @Mock
    private CloudProvider defaultCloudProvider;

    @Mock
    private CloudProvider anotheroneCloudProvider;

    private final Tag connectorIaasTag = Tag.builder()
                                            .key(CloudManager.DEFAULT_CONNECTOR_IAAS_TAG_KEY)
                                            .value(CloudManager.DEFAULT_CONNECTOR_IAAS_TAG_VALUE)
                                            .build();

    @Before
    public void init() {
        MockitoAnnotations.initMocks(this);
        when(defaultCloudProvider.getType()).thenReturn("sometype");
        when(anotheroneCloudProvider.getType()).thenReturn("anothertype");
        cloudManager = new CloudManager(Lists.newArrayList(defaultCloudProvider, anotheroneCloudProvider));

    }

    @Test
    public void testCreateInstance() {
        when(defaultCloudProvider.getType()).thenReturn("sometype");
        Infrastructure infrastructure = InfrastructureFixture.getSimpleInfrastructure("sometype");
        Instance instance = InstanceFixture.simpleInstance("id");
        cloudManager.createInstance(infrastructure, instance);
        verify(defaultCloudProvider, times(1)).createInstance(infrastructure, instance, connectorIaasTag);
    }

    @Test
    public void testDeleteInstance() {
        Infrastructure infrastructure = InfrastructureFixture.getSimpleInfrastructure("anothertype");
        cloudManager.deleteInstance(infrastructure, "instanceID");
        verify(anotheroneCloudProvider, times(1)).deleteInstance(infrastructure, "instanceID");
    }

    @Test
    public void testDeleteInfrastructure() {
        Infrastructure infrastructure = InfrastructureFixture.getSimpleInfrastructure("sometype");
        cloudManager.deleteInfrastructure(infrastructure);
        verify(defaultCloudProvider, times(1)).deleteInfrastructure(infrastructure);
    }

    @Test
    public void testGetAllInfrastructureInstances() {
        Infrastructure infrastructure = InfrastructureFixture.getSimpleInfrastructure("anothertype");
        cloudManager.getAllInfrastructureInstances(infrastructure);
        verify(anotheroneCloudProvider, times(1)).getAllInfrastructureInstances(infrastructure);
    }

    @Test
    public void testExecuteScript() {
        Infrastructure infrastructure = InfrastructureFixture.getSimpleInfrastructure("sometype");
        InstanceScript instanceScript = InstanceScriptFixture.getInstanceScript(new String[] { "", "" });
        cloudManager.executeScriptOnInstanceId(infrastructure, "instanceId", instanceScript);
        verify(defaultCloudProvider, times(1)).executeScriptOnInstanceId(infrastructure, "instanceId", instanceScript);
    }

    @Test
    public void testExecuteScriptOnInstanceTag() {
        Infrastructure infrastructure = InfrastructureFixture.getSimpleInfrastructure("sometype");
        InstanceScript instanceScript = InstanceScriptFixture.getInstanceScript(new String[] { "", "" });
        cloudManager.executeScriptOnInstanceTag(infrastructure, "instanceTag", instanceScript);
        verify(defaultCloudProvider, times(1)).executeScriptOnInstanceTag(infrastructure,
                                                                          "instanceTag",
                                                                          instanceScript);
    }

    @Test
    public void testGetAllImages() {
        Infrastructure infrastructure = InfrastructureFixture.getSimpleInfrastructure("sometype");
        cloudManager.getAllImages(infrastructure);
        verify(defaultCloudProvider, times(1)).getAllImages(infrastructure);
    }

}

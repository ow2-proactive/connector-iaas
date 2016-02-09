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

import com.google.common.collect.Lists;


public class CloudManagerTest {

    private CloudManager cloudManager;

    @Mock
    private CloudProvider defaultCloudProvider;

    @Mock
    private CloudProvider anotheroneCloudProvider;

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
        verify(defaultCloudProvider, times(1)).createInstance(infrastructure, instance);
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
        verify(defaultCloudProvider, times(1)).executeScriptOnInstanceId(infrastructure, "instanceId",
                instanceScript);
    }

    @Test
    public void testExecuteScriptOnInstanceTag() {
        Infrastructure infrastructure = InfrastructureFixture.getSimpleInfrastructure("sometype");
        InstanceScript instanceScript = InstanceScriptFixture.getInstanceScript(new String[] { "", "" });
        cloudManager.executeScriptOnInstanceTag(infrastructure, "instanceTag", instanceScript);
        verify(defaultCloudProvider, times(1)).executeScriptOnInstanceTag(infrastructure, "instanceTag",
                instanceScript);
    }

    @Test
    public void testGetAllImages() {
        Infrastructure infrastructure = InfrastructureFixture.getSimpleInfrastructure("sometype");
        cloudManager.getAllImages(infrastructure);
        verify(defaultCloudProvider, times(1)).getAllImages(infrastructure);
    }

}

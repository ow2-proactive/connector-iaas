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
import org.ow2.proactive.connector.iaas.cache.InstanceCache;
import org.ow2.proactive.connector.iaas.cloud.CloudManager;
import org.ow2.proactive.connector.iaas.fixtures.InfrastructureFixture;
import org.ow2.proactive.connector.iaas.fixtures.InstanceFixture;
import org.ow2.proactive.connector.iaas.model.Infrastructure;
import org.ow2.proactive.connector.iaas.model.Instance;

import com.google.common.collect.ImmutableTable;

import jersey.repackaged.com.google.common.collect.Sets;


public class InstanceServiceTest {

    @InjectMocks
    private InstanceService instanceService;

    @Mock
    private InstanceCache instanceCache;

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

        Infrastructure infrastructure = InfrastructureFixture.getInfrastructure("id-aws", "aws", "endPoint",
                "userName", "credential");
        when(infrastructureService.getInfrastructure(infrastructure.getId())).thenReturn(infrastructure);

        Instance instance = InstanceFixture.getInstance("instance-id", "instance-name", "image", "2", "512",
                "cpu", "running");
        ImmutableTable<String, String, Instance> instancesTable = ImmutableTable
                .<String, String, Instance> builder().put(infrastructure.getId(), instance.getId(), instance)
                .build();
        when(instanceCache.getSupportedInstancePerInfrastructure()).thenReturn(instancesTable);

        when(cloudManager.createInstance(infrastructure, instance))
                .thenReturn(Sets.newHashSet(InstanceFixture.simpleInstance("id")));

        Set<Instance> created = instanceService.createInstance("id-aws", instance);

        assertThat(created.size(), is(1));

        verify(cloudManager, times(1)).createInstance(infrastructure, instance);

    }

    @Test
    public void testDeleteInstance() throws NumberFormatException, RunNodesException {

        Infrastructure infrastructure = InfrastructureFixture.getInfrastructure("id-aws", "aws", "endPoint",
                "userName", "credential");

        Instance instance = InstanceFixture.simpleInstance("instanceID");

        ImmutableTable<String, String, Instance> instancesTable = ImmutableTable
                .<String, String, Instance> builder().put(infrastructure.getId(), instance.getId(), instance)
                .build();
        when(instanceCache.getSupportedInstancePerInfrastructure()).thenReturn(instancesTable);

        when(infrastructureService.getInfrastructure(infrastructure.getId())).thenReturn(infrastructure);

        instanceService.deleteInstance(infrastructure.getId(), "instanceID");

        verify(cloudManager, times(1)).deleteInstance(infrastructure, instance);

    }

    @Test
    public void testGetAllInstances() throws NumberFormatException, RunNodesException {

        Infrastructure infrastructure = InfrastructureFixture.getInfrastructure("id-aws", "aws", "endPoint",
                "userName", "credential");
        when(infrastructureService.getInfrastructure(infrastructure.getId())).thenReturn(infrastructure);

        Instance instance = InstanceFixture.simpleInstance("id");
        ImmutableTable<String, String, Instance> instancesTable = ImmutableTable
                .<String, String, Instance> builder().put(infrastructure.getId(), instance.getId(), instance)
                .build();
        when(instanceCache.getSupportedInstancePerInfrastructure()).thenReturn(instancesTable);

        when(cloudManager.getAllInfrastructureInstances(infrastructure))
                .thenReturn(Sets.newHashSet(InstanceFixture.simpleInstance("id")));

        Set<Instance> created = instanceService.getAllInstances(infrastructure.getId());

        assertThat(created.size(), is(1));

        verify(cloudManager, times(1)).getAllInfrastructureInstances(infrastructure);

    }
}

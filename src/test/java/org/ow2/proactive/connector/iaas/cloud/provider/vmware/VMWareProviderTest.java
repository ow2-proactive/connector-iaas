package org.ow2.proactive.connector.iaas.cloud.provider.vmware;

import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertThat;
import static org.mockito.Matchers.any;
import static org.mockito.Matchers.anyString;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.rmi.RemoteException;
import java.util.List;
import java.util.Set;

import org.junit.Before;
import org.junit.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.ow2.proactive.connector.iaas.fixtures.InfrastructureFixture;
import org.ow2.proactive.connector.iaas.fixtures.InstanceFixture;
import org.ow2.proactive.connector.iaas.fixtures.InstanceScriptFixture;
import org.ow2.proactive.connector.iaas.model.Infrastructure;
import org.ow2.proactive.connector.iaas.model.Instance;
import org.ow2.proactive.connector.iaas.model.InstanceScript;
import org.ow2.proactive.connector.iaas.model.ScriptResult;

import com.vmware.vim25.FileFault;
import com.vmware.vim25.GuestInfo;
import com.vmware.vim25.GuestOperationsFault;
import com.vmware.vim25.GuestProgramSpec;
import com.vmware.vim25.InvalidProperty;
import com.vmware.vim25.InvalidState;
import com.vmware.vim25.ManagedEntityStatus;
import com.vmware.vim25.NamePasswordAuthentication;
import com.vmware.vim25.RuntimeFault;
import com.vmware.vim25.TaskInProgress;
import com.vmware.vim25.VirtualHardware;
import com.vmware.vim25.VirtualMachineCloneSpec;
import com.vmware.vim25.VirtualMachineConfigInfo;
import com.vmware.vim25.VirtualMachineSummary;
import com.vmware.vim25.mo.Folder;
import com.vmware.vim25.mo.GuestOperationsManager;
import com.vmware.vim25.mo.GuestProcessManager;
import com.vmware.vim25.mo.ServiceInstance;
import com.vmware.vim25.mo.Task;
import com.vmware.vim25.mo.VirtualMachine;

import jersey.repackaged.com.google.common.collect.Sets;


public class VMWareProviderTest {

    @InjectMocks
    private VMWareProvider vmWareProvider;

    @Mock
    private VMWareServiceInstanceCache vmWareServiceInstanceCache;

    @Mock
    private VMWareProviderVirualMachineUtil vmWareProviderVirualMachineUtil;

    @Mock
    private ServiceInstance serviceInstance;

    @Mock
    private Folder rootFolder;

    @Mock
    private Folder instanceFolder;

    @Mock
    private VirtualMachine virtualMachine;

    @Mock
    private VirtualMachine createdVirtualMachine;

    @Mock
    private Task task;

    @Mock
    private VirtualMachineConfigInfo virtualMachineConfigInfo;

    @Mock
    private VirtualHardware hardware;

    @Mock
    private GuestInfo guestInfo;

    @Mock
    private VirtualMachineSummary virtualMachineSummary;

    @Mock
    private GuestOperationsManager gom;

    @Mock
    private GuestProcessManager gpm;

    @Before
    public void init() {
        MockitoAnnotations.initMocks(this);
        when(vmWareServiceInstanceCache.getServiceInstance(any(Infrastructure.class)))
                .thenReturn(serviceInstance);
        when(serviceInstance.getRootFolder()).thenReturn(rootFolder);

    }

    @Test
    public void testCreateInstance()
            throws InvalidProperty, RuntimeFault, RemoteException, InterruptedException {
        Infrastructure infrastructure = InfrastructureFixture.getSimpleInfrastructure("vmware-type");
        Instance instance = InstanceFixture.simpleInstanceWithTagAndImage("marco-tag",
                "activeeon/RoboconfAgent180116");

        when(vmWareProviderVirualMachineUtil.searchFolderByName("activeeon", rootFolder))
                .thenReturn(instanceFolder);

        when(vmWareProviderVirualMachineUtil.searchVirtualMachineByName("RoboconfAgent180116", rootFolder))
                .thenReturn(virtualMachine);

        when(vmWareProviderVirualMachineUtil.searchVirtualMachineByName("marco-tag", rootFolder))
                .thenReturn(createdVirtualMachine);

        when(createdVirtualMachine.getConfig()).thenReturn(virtualMachineConfigInfo);

        when(virtualMachineConfigInfo.getUuid()).thenReturn("some-generated-virtual-machine-id");

        when(virtualMachine.cloneVM_Task(any(Folder.class), anyString(), any(VirtualMachineCloneSpec.class)))
                .thenReturn(task);

        when(task.waitForTask()).thenReturn(Task.SUCCESS.toString());

        Set<Instance> createdInstances = vmWareProvider.createInstance(infrastructure, instance);

        assertThat(createdInstances.size(), is(1));

        assertThat(createdInstances.iterator().next().getId(), is("some-generated-virtual-machine-id"));

    }

    @Test
    public void testDeleteInstance()
            throws TaskInProgress, InvalidState, RuntimeFault, RemoteException, InterruptedException {
        Infrastructure infrastructure = InfrastructureFixture.getSimpleInfrastructure("vmware-type");

        when(vmWareProviderVirualMachineUtil.getAllVirtualMachinesByInfrastructure(rootFolder,
                infrastructure)).thenReturn(Sets.newHashSet(createdVirtualMachine));

        when(createdVirtualMachine.getConfig()).thenReturn(virtualMachineConfigInfo);

        when(virtualMachineConfigInfo.getUuid()).thenReturn("some-generated-virtual-machine-id");

        when(createdVirtualMachine.powerOffVM_Task()).thenReturn(task);

        when(task.waitForTask()).thenReturn(Task.SUCCESS.toString());

        when(createdVirtualMachine.destroy_Task()).thenReturn(task);

        vmWareProvider.deleteInstance(infrastructure, "some-generated-virtual-machine-id");

        verify(createdVirtualMachine).powerOffVM_Task();
        verify(createdVirtualMachine).destroy_Task();

    }

    @Test
    public void testDeleteInstanceNotExist()
            throws TaskInProgress, InvalidState, RuntimeFault, RemoteException, InterruptedException {
        Infrastructure infrastructure = InfrastructureFixture.getSimpleInfrastructure("vmware-type");

        when(vmWareProviderVirualMachineUtil.getAllVirtualMachinesByInfrastructure(rootFolder,
                infrastructure)).thenReturn(Sets.newHashSet(createdVirtualMachine));

        when(createdVirtualMachine.getConfig()).thenReturn(virtualMachineConfigInfo);

        when(virtualMachineConfigInfo.getUuid()).thenReturn("some-generated-virtual-machine-id");

        when(createdVirtualMachine.powerOffVM_Task()).thenReturn(task);

        when(task.waitForTask()).thenReturn(Task.SUCCESS.toString());

        when(createdVirtualMachine.destroy_Task()).thenReturn(task);

        vmWareProvider.deleteInstance(infrastructure, "some-other-virtual-machine-id");

        verify(createdVirtualMachine, times(0)).powerOffVM_Task();
        verify(createdVirtualMachine, times(0)).destroy_Task();

    }

    @Test
    public void testGetAllInfrastructureInstances()
            throws TaskInProgress, InvalidState, RuntimeFault, RemoteException, InterruptedException {
        Infrastructure infrastructure = InfrastructureFixture.getSimpleInfrastructure("vmware-type");

        when(vmWareProviderVirualMachineUtil.getAllVirtualMachinesByInfrastructure(rootFolder,
                infrastructure)).thenReturn(Sets.newHashSet(createdVirtualMachine));

        when(createdVirtualMachine.getConfig()).thenReturn(virtualMachineConfigInfo);

        when(virtualMachineConfigInfo.getUuid()).thenReturn("some-generated-virtual-machine-id");

        when(createdVirtualMachine.getGuest()).thenReturn(guestInfo);

        when(guestInfo.getIpAddress()).thenReturn("77.154.227.148");

        when(virtualMachineConfigInfo.getHardware()).thenReturn(hardware);

        when(hardware.getNumCPU()).thenReturn(8);

        when(hardware.getMemoryMB()).thenReturn(2048);

        when(createdVirtualMachine.getSummary()).thenReturn(virtualMachineSummary);

        when(virtualMachineSummary.getOverallStatus()).thenReturn(ManagedEntityStatus.green);

        Set<Instance> createdInstances = vmWareProvider.getAllInfrastructureInstances(infrastructure);

        assertThat(createdInstances.size(), is(1));

        assertThat(createdInstances.iterator().next().getId(), is("some-generated-virtual-machine-id"));
        assertThat(createdInstances.iterator().next().getHardware().getMinCores(), is("8"));
        assertThat(createdInstances.iterator().next().getHardware().getMinRam(), is("2048"));
        assertThat(createdInstances.iterator().next().getNetwork().getPublicAddresses().iterator().next(),
                is("77.154.227.148"));
        assertThat(createdInstances.iterator().next().getStatus(), is(ManagedEntityStatus.green.toString()));

    }

    @Test
    public void testExecuteScriptOnInstanceId() throws GuestOperationsFault, InvalidState, TaskInProgress,
            FileFault, RuntimeFault, RemoteException {
        Infrastructure infrastructure = InfrastructureFixture.getSimpleInfrastructure("vmware-type");

        InstanceScript instanceScript = InstanceScriptFixture.getInstanceScriptUserAndPassword("username",
                "pasword", new String[] { "wget node.jar", "java -jar node.jar" });

        when(vmWareProviderVirualMachineUtil.getAllVirtualMachinesByInfrastructure(rootFolder,
                infrastructure)).thenReturn(Sets.newHashSet(createdVirtualMachine));

        when(createdVirtualMachine.getConfig()).thenReturn(virtualMachineConfigInfo);

        when(virtualMachineConfigInfo.getUuid()).thenReturn("some-generated-virtual-machine-id");

        when(serviceInstance.getGuestOperationsManager()).thenReturn(gom);

        when(gom.getProcessManager(createdVirtualMachine)).thenReturn(gpm);

        when(gpm.startProgramInGuest(any(NamePasswordAuthentication.class), any(GuestProgramSpec.class)))
                .thenReturn(132L);

        ScriptResult scriptResult = vmWareProvider.executeScriptOnInstanceId(infrastructure,
                "some-generated-virtual-machine-id", instanceScript);

        assertThat(scriptResult.getOutput(), is("132 132"));

    }

    @Test
    public void testExecuteScriptOnInstanceTag() throws GuestOperationsFault, InvalidState, TaskInProgress,
            FileFault, RuntimeFault, RemoteException {
        Infrastructure infrastructure = InfrastructureFixture.getSimpleInfrastructure("vmware-type");

        InstanceScript instanceScript = InstanceScriptFixture.getInstanceScriptUserAndPassword("username",
                "pasword", new String[] { "wget node.jar", "java -jar node.jar" });

        when(vmWareProviderVirualMachineUtil.getAllVirtualMachinesByInfrastructure(rootFolder,
                infrastructure)).thenReturn(Sets.newHashSet(createdVirtualMachine));

        when(createdVirtualMachine.getConfig()).thenReturn(virtualMachineConfigInfo);

        when(virtualMachineConfigInfo.getUuid()).thenReturn("some-generated-virtual-machine-id");

        when(createdVirtualMachine.getName()).thenReturn("some-virtual-machine-tag");

        when(serviceInstance.getGuestOperationsManager()).thenReturn(gom);

        when(gom.getProcessManager(createdVirtualMachine)).thenReturn(gpm);

        when(gpm.startProgramInGuest(any(NamePasswordAuthentication.class), any(GuestProgramSpec.class)))
                .thenReturn(132L);

        List<ScriptResult> scriptResults = vmWareProvider.executeScriptOnInstanceTag(infrastructure,
                "some-virtual-machine-tag", instanceScript);

        assertThat(scriptResults.get(0).getOutput(), is("132 132"));

    }

    @Test
    public void testDeleteInfrastructure()
            throws TaskInProgress, InvalidState, RuntimeFault, RemoteException, InterruptedException {
        Infrastructure infrastructure = InfrastructureFixture.getSimpleInfrastructure("vmware-type");

        when(vmWareProviderVirualMachineUtil.getAllVirtualMachinesByInfrastructure(rootFolder,
                infrastructure)).thenReturn(Sets.newHashSet(createdVirtualMachine));

        when(createdVirtualMachine.getConfig()).thenReturn(virtualMachineConfigInfo);

        when(virtualMachineConfigInfo.getUuid()).thenReturn("some-generated-virtual-machine-id");

        when(createdVirtualMachine.powerOffVM_Task()).thenReturn(task);

        when(task.waitForTask()).thenReturn(Task.SUCCESS.toString());

        when(createdVirtualMachine.destroy_Task()).thenReturn(task);

        vmWareProvider.deleteInfrastructure(infrastructure);

        verify(createdVirtualMachine).powerOffVM_Task();
        verify(createdVirtualMachine).destroy_Task();
        verify(vmWareServiceInstanceCache).removeServiceInstance(infrastructure);

    }

}

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
package org.ow2.proactive.connector.iaas.cloud.provider.vmware;

import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertThat;
import static org.junit.Assert.assertTrue;
import static org.mockito.Matchers.any;
import static org.mockito.Matchers.anyString;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.rmi.RemoteException;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;
import java.util.Set;

import org.junit.Before;
import org.junit.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.ow2.proactive.connector.iaas.cloud.TagManager;
import org.ow2.proactive.connector.iaas.fixtures.InfrastructureFixture;
import org.ow2.proactive.connector.iaas.fixtures.InstanceFixture;
import org.ow2.proactive.connector.iaas.fixtures.InstanceScriptFixture;
import org.ow2.proactive.connector.iaas.model.Infrastructure;
import org.ow2.proactive.connector.iaas.model.Instance;
import org.ow2.proactive.connector.iaas.model.InstanceScript;
import org.ow2.proactive.connector.iaas.model.Options;
import org.ow2.proactive.connector.iaas.model.ScriptResult;
import org.ow2.proactive.connector.iaas.model.Tag;

import com.google.common.collect.Lists;
import com.vmware.vim25.FileFault;
import com.vmware.vim25.GuestInfo;
import com.vmware.vim25.GuestOperationsFault;
import com.vmware.vim25.GuestProgramSpec;
import com.vmware.vim25.InvalidState;
import com.vmware.vim25.ManagedEntityStatus;
import com.vmware.vim25.ManagedObjectReference;
import com.vmware.vim25.NamePasswordAuthentication;
import com.vmware.vim25.RuntimeFault;
import com.vmware.vim25.TaskInProgress;
import com.vmware.vim25.VirtualDevice;
import com.vmware.vim25.VirtualDeviceConfigSpec;
import com.vmware.vim25.VirtualEthernetCard;
import com.vmware.vim25.VirtualHardware;
import com.vmware.vim25.VirtualMachineCloneSpec;
import com.vmware.vim25.VirtualMachineConfigInfo;
import com.vmware.vim25.VirtualMachineSummary;
import com.vmware.vim25.mo.Datastore;
import com.vmware.vim25.mo.Folder;
import com.vmware.vim25.mo.GuestOperationsManager;
import com.vmware.vim25.mo.GuestProcessManager;
import com.vmware.vim25.mo.HostSystem;
import com.vmware.vim25.mo.ResourcePool;
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
    private VMWareProviderVirtualMachineUtil vmWareProviderVirtualMachineUtil;

    @Mock
    private VMWareProviderMacAddressHandler vMWareProviderMacAddressHandler;

    @Mock
    private VirtualDeviceConfigSpec virtDevConfSpec;

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

    @Mock
    private ResourcePool resourcePool;

    @Mock
    private ManagedObjectReference resourcePoolMOR;

    @Mock
    private HostSystem destinationHost;

    @Mock
    private ManagedObjectReference hostMOR;

    @Mock
    private Datastore destinationDatastore;

    @Mock
    private ManagedObjectReference datastoreMOR;

    @Mock
    private TagManager tagManager;

    private Tag connectorIaasTag = Tag.builder().key("connector-iaas-tag-key").value("default-value").build();

    @Before
    public void init() {
        MockitoAnnotations.initMocks(this);
        when(vmWareServiceInstanceCache.getServiceInstance(any(Infrastructure.class))).thenReturn(serviceInstance);
        when(serviceInstance.getRootFolder()).thenReturn(rootFolder);

    }

    @Test
    public void testCreateInstance() throws RemoteException, InterruptedException {
        Infrastructure infrastructure = InfrastructureFixture.getSimpleInfrastructure("vmware-type");
        Instance instance = InstanceFixture.simpleInstanceWithTagAndImage("marco-tag", "RoboconfAgent180116");

        when(vmWareProviderVirtualMachineUtil.searchVirtualMachineByName("RoboconfAgent180116",
                                                                         rootFolder)).thenReturn(Optional.of(virtualMachine));

        when(vmWareProviderVirtualMachineUtil.searchVirtualMachineByUUID("RoboconfAgent180116",
                                                                         rootFolder)).thenReturn(Optional.empty());

        when(vmWareProviderVirtualMachineUtil.searchVirtualMachineByName("marco-tag",
                                                                         rootFolder)).thenReturn(Optional.of(createdVirtualMachine));

        // The destination resource pool and folder are the same than the VM to clone
        when(virtualMachine.getResourcePool()).thenReturn(resourcePool);
        when(vmWareProviderVirtualMachineUtil.searchVMFolderFromVMName("RoboconfAgent180116",
                                                                       rootFolder)).thenReturn(Optional.of(instanceFolder));

        when(createdVirtualMachine.getConfig()).thenReturn(virtualMachineConfigInfo);

        when(virtualMachineConfigInfo.getUuid()).thenReturn("some-generated-virtual-machine-id");

        when(virtualMachine.cloneVM_Task(any(Folder.class),
                                         anyString(),
                                         any(VirtualMachineCloneSpec.class))).thenReturn(task);

        when(task.waitForTask()).thenReturn(Task.SUCCESS);

        // Tags
        when(tagManager.retrieveAllTags(anyString(),
                                        any(Options.class))).thenReturn(Lists.newArrayList(connectorIaasTag));

        Set<Instance> createdInstances = vmWareProvider.createInstance(infrastructure, instance);

        assertThat(createdInstances.size(), is(1));

        assertThat(createdInstances.iterator().next().getId(), is("some-generated-virtual-machine-id"));
    }

    @Test
    public void testCreateInstanceWithMacAddress() throws RemoteException, InterruptedException {

        Infrastructure infrastructure = InfrastructureFixture.getSimpleInfrastructure("vmware-type");
        Instance instance = InstanceFixture.simpleInstanceWithMacAddress("cloned-tag",
                                                                         "vm-to-clone",
                                                                         Arrays.asList("00:50:56:11:11:11"));

        // Ensure that we can find the original and the new VMs
        when(vmWareProviderVirtualMachineUtil.searchVirtualMachineByName("vm-to-clone",
                                                                         rootFolder)).thenReturn(Optional.ofNullable(virtualMachine));
        when(vmWareProviderVirtualMachineUtil.searchVirtualMachineByName("cloned-tag",
                                                                         rootFolder)).thenReturn(Optional.ofNullable(createdVirtualMachine));

        when(vmWareProviderVirtualMachineUtil.searchVMFolderFromVMName("vm-to-clone",
                                                                       rootFolder)).thenReturn(Optional.empty());
        when(vmWareProviderVirtualMachineUtil.searchFolderByName("VM",
                                                                 rootFolder)).thenReturn(Optional.of(instanceFolder));

        // Ensure that VM's config, UUID and hardware are set for both VMs
        when(virtualMachine.getConfig()).thenReturn(virtualMachineConfigInfo);
        when(createdVirtualMachine.getConfig()).thenReturn(virtualMachineConfigInfo);
        when(virtualMachineConfigInfo.getUuid()).thenReturn("some-generated-virtual-machine-id");
        when(virtualMachineConfigInfo.getHardware()).thenReturn(hardware);

        // Add a Virtual Ethernet Card with an automatically generated MAC address
        VirtualEthernetCard virtEthCard = new VirtualEthernetCard();
        virtEthCard.setAddressType("Generated");
        when(hardware.getDevice()).thenReturn(new VirtualDevice[] { virtEthCard });

        // Emulate vMWareProviderMacAddressHandler behavior
        when(vMWareProviderMacAddressHandler.getVirtualDeviceConfigWithMacAddress("00:50:56:11:11:11",
                                                                                  virtualMachine)).thenReturn(Optional.of(new VirtualDeviceConfigSpec[] { virtDevConfSpec }));
        VirtualEthernetCard newVirtEthCard = new VirtualEthernetCard();
        newVirtEthCard.setAddressType("Manual");
        newVirtEthCard.setMacAddress("00:50:56:11:11:11");
        when(virtDevConfSpec.getDevice()).thenReturn(newVirtEthCard);

        // The destination resource pool is the same than the VM to clone
        when(virtualMachine.getResourcePool()).thenReturn(resourcePool);

        // Emulate the call to 'cloneVM_Task' (VMWare API)
        when(virtualMachine.cloneVM_Task(any(Folder.class),
                                         anyString(),
                                         any(VirtualMachineCloneSpec.class))).thenReturn(task);
        when(task.waitForTask()).thenReturn(Task.SUCCESS);

        // Tags
        when(tagManager.retrieveAllTags(anyString(),
                                        any(Options.class))).thenReturn(Lists.newArrayList(connectorIaasTag));

        // Create the new instance and check if the MAC address is set as option
        Set<Instance> createdInstances = vmWareProvider.createInstance(infrastructure, instance);
        assertThat(createdInstances.size(), is(1));
        assertThat(createdInstances.iterator().next().getOptions().getMacAddresses().iterator().next(),
                   is("00:50:56:11:11:11"));

        // TODO: Find a way to check the VirtualMachineCloneSpec object used to clone the VM
    }

    @Test
    public void testCreateInstanceIntoSpecificHost() throws RemoteException, InterruptedException {
        Infrastructure infrastructure = InfrastructureFixture.getSimpleInfrastructure("vmware-type");
        Instance instance = InstanceFixture.simpleInstanceWithTagAndImage("marco-tag",
                                                                          "RoboconfAgent180116/destinationHost");

        //when(vmWareProviderVirtualMachineUtil.searchFolderByName("activeeon", rootFolder)).thenReturn(instanceFolder);

        when(vmWareProviderVirtualMachineUtil.searchVirtualMachineByName("RoboconfAgent180116",
                                                                         rootFolder)).thenReturn(Optional.of(virtualMachine));

        when(vmWareProviderVirtualMachineUtil.searchVirtualMachineByName("marco-tag",
                                                                         rootFolder)).thenReturn(Optional.of(createdVirtualMachine));

        // Ensure to retrieve destination pool, datastore, folder, and host
        when(vmWareProviderVirtualMachineUtil.searchHostByName("destinationHost",
                                                               rootFolder)).thenReturn(Optional.of(destinationHost));
        when(vmWareProviderVirtualMachineUtil.searchResourcePoolByHostname("destinationHost",
                                                                           rootFolder)).thenReturn(Optional.of(resourcePool));
        when(vmWareProviderVirtualMachineUtil.getDatastoreWithMostSpaceFromHost(destinationHost)).thenReturn(Optional.of(destinationDatastore));
        when(vmWareProviderVirtualMachineUtil.searchVMFolderByHostname("destinationHost",
                                                                       rootFolder)).thenReturn(Optional.of(instanceFolder));

        when(createdVirtualMachine.getConfig()).thenReturn(virtualMachineConfigInfo);

        when(virtualMachineConfigInfo.getUuid()).thenReturn("some-generated-virtual-machine-id");

        when(virtualMachine.getResourcePool()).thenReturn(resourcePool);

        when(virtualMachine.cloneVM_Task(any(Folder.class),
                                         anyString(),
                                         any(VirtualMachineCloneSpec.class))).thenReturn(task);

        when(task.waitForTask()).thenReturn(Task.SUCCESS);

        // Tags
        when(tagManager.retrieveAllTags(anyString(),
                                        any(Options.class))).thenReturn(Lists.newArrayList(connectorIaasTag));

        Set<Instance> createdInstances = vmWareProvider.createInstance(infrastructure, instance);

        assertThat(createdInstances.size(), is(1));

        assertThat(createdInstances.iterator().next().getId(), is("some-generated-virtual-machine-id"));
    }

    @Test
    public void testCreateInstanceIntoSpecificHostWithoutDedicatedVMFolder()
            throws RemoteException, InterruptedException {

        Infrastructure infrastructure = InfrastructureFixture.getSimpleInfrastructure("vmware-type");
        Instance instance = InstanceFixture.simpleInstanceWithTagAndImage("marco-tag",
                                                                          "RoboconfAgent180116/destinationHost");

        when(vmWareProviderVirtualMachineUtil.searchVirtualMachineByName("RoboconfAgent180116",
                                                                         rootFolder)).thenReturn(Optional.of(virtualMachine));

        when(vmWareProviderVirtualMachineUtil.searchVirtualMachineByName("marco-tag",
                                                                         rootFolder)).thenReturn(Optional.of(createdVirtualMachine));

        // Ensure to retrieve destination pool, datastore, and host
        when(vmWareProviderVirtualMachineUtil.searchHostByName("destinationHost",
                                                               rootFolder)).thenReturn(Optional.of(destinationHost));
        when(vmWareProviderVirtualMachineUtil.searchResourcePoolByHostname("destinationHost",
                                                                           rootFolder)).thenReturn(Optional.of(resourcePool));
        when(vmWareProviderVirtualMachineUtil.getDatastoreWithMostSpaceFromHost(destinationHost)).thenReturn(Optional.of(destinationDatastore));

        // A dedicated VM folder is not found on destination host, therefore the generic VM folder is used (must be present in all vCenter)
        when(vmWareProviderVirtualMachineUtil.searchVMFolderByHostname("destinationHost",
                                                                       rootFolder)).thenReturn(Optional.empty());
        when(vmWareProviderVirtualMachineUtil.searchFolderByName("VM",
                                                                 rootFolder)).thenReturn(Optional.of(instanceFolder));

        when(createdVirtualMachine.getConfig()).thenReturn(virtualMachineConfigInfo);

        when(virtualMachineConfigInfo.getUuid()).thenReturn("some-generated-virtual-machine-id");

        when(virtualMachine.cloneVM_Task(any(Folder.class),
                                         anyString(),
                                         any(VirtualMachineCloneSpec.class))).thenReturn(task);

        when(task.waitForTask()).thenReturn(Task.SUCCESS);

        Set<Instance> createdInstances = vmWareProvider.createInstance(infrastructure, instance);

        assertThat(createdInstances.size(), is(1));

        assertThat(createdInstances.iterator().next().getId(), is("some-generated-virtual-machine-id"));
    }

    @Test
    public void testCreateInstanceIntoRandomHost() throws RemoteException, InterruptedException {
        Infrastructure infrastructure = InfrastructureFixture.getSimpleInfrastructure("vmware-type");
        Instance instance = InstanceFixture.simpleInstanceWithTagAndImage("marco-tag", "RoboconfAgent180116/*");

        when(vmWareProviderVirtualMachineUtil.searchFolderByName("activeeon",
                                                                 rootFolder)).thenReturn(Optional.of(instanceFolder));

        when(vmWareProviderVirtualMachineUtil.searchVirtualMachineByName("RoboconfAgent180116",
                                                                         rootFolder)).thenReturn(Optional.of(virtualMachine));

        when(vmWareProviderVirtualMachineUtil.searchVirtualMachineByName("marco-tag",
                                                                         rootFolder)).thenReturn(Optional.of(createdVirtualMachine));

        // Ensure to retrieve a (random) resource pool and a datastore (with most space available)
        when(vmWareProviderVirtualMachineUtil.getRandomResourcePool(rootFolder)).thenReturn(Optional.of(resourcePool));
        when(vmWareProviderVirtualMachineUtil.getDatastoreWithMostSpaceFromPool(resourcePool)).thenReturn(Optional.of(destinationDatastore));

        // Get the generic VM folder
        when(vmWareProviderVirtualMachineUtil.searchVMFolderFromVMName("vm-to-clone",
                                                                       rootFolder)).thenReturn(Optional.empty());
        when(vmWareProviderVirtualMachineUtil.searchFolderByName("VM",
                                                                 rootFolder)).thenReturn(Optional.of(instanceFolder));

        when(createdVirtualMachine.getConfig()).thenReturn(virtualMachineConfigInfo);

        when(virtualMachineConfigInfo.getUuid()).thenReturn("some-generated-virtual-machine-id");

        when(virtualMachine.cloneVM_Task(any(Folder.class),
                                         anyString(),
                                         any(VirtualMachineCloneSpec.class))).thenReturn(task);

        when(task.waitForTask()).thenReturn(Task.SUCCESS);

        Set<Instance> createdInstances = vmWareProvider.createInstance(infrastructure, instance);

        assertThat(createdInstances.size(), is(1));

        assertThat(createdInstances.iterator().next().getId(), is("some-generated-virtual-machine-id"));
    }

    @Test
    public void testDeleteInstance()
            throws TaskInProgress, InvalidState, RuntimeFault, RemoteException, InterruptedException {
        Infrastructure infrastructure = InfrastructureFixture.getSimpleInfrastructure("vmware-type");

        when(vmWareProviderVirtualMachineUtil.getAllVirtualMachines(rootFolder)).thenReturn(Sets.newHashSet(createdVirtualMachine));

        when(createdVirtualMachine.getConfig()).thenReturn(virtualMachineConfigInfo);

        when(virtualMachineConfigInfo.getUuid()).thenReturn("some-generated-virtual-machine-id");

        when(createdVirtualMachine.powerOffVM_Task()).thenReturn(task);

        when(task.waitForTask()).thenReturn(Task.SUCCESS);

        when(createdVirtualMachine.destroy_Task()).thenReturn(task);

        vmWareProvider.deleteInstance(infrastructure, "some-generated-virtual-machine-id");

        verify(createdVirtualMachine).powerOffVM_Task();
        verify(createdVirtualMachine).destroy_Task();

    }

    @Test
    public void testDeleteInstanceNotExist() throws RemoteException, InterruptedException {
        Infrastructure infrastructure = InfrastructureFixture.getSimpleInfrastructure("vmware-type");

        when(vmWareProviderVirtualMachineUtil.getAllVirtualMachines(rootFolder)).thenReturn(Sets.newHashSet(createdVirtualMachine));
        vmWareProvider.deleteInfrastructure(infrastructure);
        verify(vmWareServiceInstanceCache).removeServiceInstance(infrastructure);

    }

    @Test
    public void testDeleteAllInstances() throws RemoteException, InterruptedException {
        Infrastructure infrastructure = InfrastructureFixture.getSimpleInfrastructure("vmware-type");

        when(vmWareProviderVirtualMachineUtil.getAllVirtualMachines(rootFolder)).thenReturn(Sets.newHashSet(createdVirtualMachine));

        when(createdVirtualMachine.getConfig()).thenReturn(virtualMachineConfigInfo);

        when(virtualMachineConfigInfo.getUuid()).thenReturn("some-generated-virtual-machine-id");

        when(createdVirtualMachine.powerOffVM_Task()).thenReturn(task);

        when(task.waitForTask()).thenReturn(Task.SUCCESS);

        when(createdVirtualMachine.destroy_Task()).thenReturn(task);

        vmWareProvider.deleteInstance(infrastructure, "some-other-virtual-machine-id");

        verify(createdVirtualMachine, times(0)).powerOffVM_Task();
        verify(createdVirtualMachine, times(0)).destroy_Task();

    }

    @Test
    public void testGetAllInfrastructureInstances()
            throws TaskInProgress, InvalidState, RuntimeFault, RemoteException, InterruptedException {
        Infrastructure infrastructure = InfrastructureFixture.getSimpleInfrastructure("vmware-type");

        when(vmWareProviderVirtualMachineUtil.getAllVirtualMachines(rootFolder)).thenReturn(Sets.newHashSet(createdVirtualMachine));

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
    public void testGetAllInfrastructureInstancesWithBadConfigVM()
            throws TaskInProgress, InvalidState, RuntimeFault, RemoteException, InterruptedException {
        Infrastructure infrastructure = InfrastructureFixture.getSimpleInfrastructure("vmware-type");

        when(vmWareProviderVirtualMachineUtil.getAllVirtualMachines(rootFolder)).thenReturn(Sets.newHashSet(createdVirtualMachine));

        when(createdVirtualMachine.getConfig()).thenReturn(null);

        Set<Instance> createdInstances = vmWareProvider.getAllInfrastructureInstances(infrastructure);

        assertThat(createdInstances.size(), is(0));

    }

    @Test
    public void testExecuteScriptOnInstanceId()
            throws GuestOperationsFault, InvalidState, TaskInProgress, FileFault, RuntimeFault, RemoteException {
        Infrastructure infrastructure = InfrastructureFixture.getSimpleInfrastructure("vmware-type");

        InstanceScript instanceScript = InstanceScriptFixture.getInstanceScriptUserAndPassword("username",
                                                                                               "pasword",
                                                                                               new String[] { "wget node.jar",
                                                                                                              "java -jar node.jar" });

        when(vmWareProviderVirtualMachineUtil.getAllVirtualMachines(rootFolder)).thenReturn(Sets.newHashSet(createdVirtualMachine));

        when(createdVirtualMachine.getConfig()).thenReturn(virtualMachineConfigInfo);

        when(virtualMachineConfigInfo.getUuid()).thenReturn("some-generated-virtual-machine-id");

        when(vmWareProviderVirtualMachineUtil.searchVirtualMachineByUUID("some-generated-virtual-machine-id",
                                                                         rootFolder)).thenReturn(Optional.of(createdVirtualMachine));

        when(serviceInstance.getGuestOperationsManager()).thenReturn(gom);

        when(gom.getProcessManager(createdVirtualMachine)).thenReturn(gpm);

        when(gpm.startProgramInGuest(any(NamePasswordAuthentication.class),
                                     any(GuestProgramSpec.class))).thenReturn(132L);

        List<ScriptResult> scriptResults = vmWareProvider.executeScriptOnInstanceId(infrastructure,
                                                                                    "some-generated-virtual-machine-id",
                                                                                    instanceScript);

        assertTrue(scriptResults.size() == 2);
        assertThat(scriptResults.get(0).getOutput(), is("132"));
        assertThat(scriptResults.get(1).getOutput(), is("132"));
    }

    @Test
    public void testExecuteScriptOnInstanceTag()
            throws GuestOperationsFault, InvalidState, TaskInProgress, FileFault, RuntimeFault, RemoteException {
        Infrastructure infrastructure = InfrastructureFixture.getSimpleInfrastructure("vmware-type");

        InstanceScript instanceScript = InstanceScriptFixture.getInstanceScriptUserAndPassword("username",
                                                                                               "pasword",
                                                                                               new String[] { "wget node.jar",
                                                                                                              "java -jar node.jar" });

        when(vmWareProviderVirtualMachineUtil.getAllVirtualMachines(rootFolder)).thenReturn(Sets.newHashSet(createdVirtualMachine));

        when(createdVirtualMachine.getConfig()).thenReturn(virtualMachineConfigInfo);

        when(virtualMachineConfigInfo.getUuid()).thenReturn("some-generated-virtual-machine-id");

        when(createdVirtualMachine.getName()).thenReturn("some-virtual-machine-tag");

        when(vmWareProviderVirtualMachineUtil.searchVirtualMachineByName("some-virtual-machine-tag",
                                                                         rootFolder)).thenReturn(Optional.of(createdVirtualMachine));

        when(vmWareProviderVirtualMachineUtil.searchVirtualMachineByUUID("some-generated-virtual-machine-id",
                                                                         rootFolder)).thenReturn(Optional.of(createdVirtualMachine));

        when(serviceInstance.getGuestOperationsManager()).thenReturn(gom);

        when(gom.getProcessManager(createdVirtualMachine)).thenReturn(gpm);

        when(gpm.startProgramInGuest(any(NamePasswordAuthentication.class),
                                     any(GuestProgramSpec.class))).thenReturn(132L);

        List<ScriptResult> scriptResults = vmWareProvider.executeScriptOnInstanceTag(infrastructure,
                                                                                     "some-virtual-machine-tag",
                                                                                     instanceScript);

        assertTrue(scriptResults.size() == 2);
        assertThat(scriptResults.get(0).getOutput(), is("132"));
        assertThat(scriptResults.get(1).getOutput(), is("132"));
    }

    @Test
    public void testDeleteInfrastructure()
            throws TaskInProgress, InvalidState, RuntimeFault, RemoteException, InterruptedException {
        Infrastructure infrastructure = InfrastructureFixture.getSimpleInfrastructure("vmware-type");

        when(vmWareProviderVirtualMachineUtil.getAllVirtualMachines(rootFolder)).thenReturn(Sets.newHashSet(createdVirtualMachine));
        vmWareProvider.deleteInfrastructure(infrastructure);
        verify(vmWareServiceInstanceCache).removeServiceInstance(infrastructure);

    }
}

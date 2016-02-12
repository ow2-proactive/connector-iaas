package org.ow2.proactive.connector.iaas.cloud.provider.vmware;

import java.rmi.RemoteException;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

import org.apache.log4j.Logger;
import org.apache.log4j.Logger;
import org.ow2.proactive.connector.iaas.cloud.provider.CloudProvider;
import org.ow2.proactive.connector.iaas.model.Hardware;
import org.ow2.proactive.connector.iaas.model.Image;
import org.ow2.proactive.connector.iaas.model.Infrastructure;
import org.ow2.proactive.connector.iaas.model.Instance;
import org.ow2.proactive.connector.iaas.model.InstanceScript;
import org.ow2.proactive.connector.iaas.model.ScriptResult;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import com.google.common.collect.Sets;
import com.vmware.vim25.GuestProgramSpec;
import com.vmware.vim25.InvalidProperty;
import com.vmware.vim25.ManagedObjectReference;
import com.vmware.vim25.NamePasswordAuthentication;
import com.vmware.vim25.RuntimeFault;
import com.vmware.vim25.VirtualMachineCloneSpec;
import com.vmware.vim25.VirtualMachineConfigSpec;
import com.vmware.vim25.VirtualMachineRelocateSpec;
import com.vmware.vim25.mo.Folder;
import com.vmware.vim25.mo.GuestOperationsManager;
import com.vmware.vim25.mo.GuestProcessManager;
import com.vmware.vim25.mo.InventoryNavigator;
import com.vmware.vim25.mo.ManagedEntity;
import com.vmware.vim25.mo.ResourcePool;
import com.vmware.vim25.mo.Task;
import com.vmware.vim25.mo.VirtualMachine;

import lombok.Getter;


@Component("vmWareProvider")
public class VMWareProvider implements CloudProvider {

    final static Logger logger = Logger.getLogger(VMWareProvider.class);

    @Getter
    private final String type = "vmware";

    @Autowired
    private VMWareServiceInstanceCache vmWareServiceInstanceCache;

    @Override
    public Set<Instance> createInstance(Infrastructure infrastructure, Instance instance) {

        try {

            String instanceFolder = instance.getImage().split("/")[0];
            String instanceImageId = instance.getImage().split("/")[1];

            Folder rootFolder = vmWareServiceInstanceCache.getServiceInstance(infrastructure).getRootFolder();

            VirtualMachineCloneSpec vmcs = new VirtualMachineCloneSpec();

            vmcs.setLocation(getVirtualMachineRelocateSpec(rootFolder));
            vmcs.setPowerOn(true);
            vmcs.setTemplate(false);
            vmcs.setConfig(getVirtualMachineConfigSpec(instance));

            Folder vmFolder = (Folder) new InventoryNavigator(rootFolder).searchManagedEntity("Folder",
                    instanceFolder);

            return IntStream.rangeClosed(1, Integer.valueOf(instance.getNumber()))
                    .mapToObj(i -> cloneVM(instanceImageId, instance, rootFolder, vmcs, vmFolder))
                    .map(vm -> instance.withId(vm.getConfig().getUuid())).collect(Collectors.toSet());

        } catch (RemoteException e) {
            logger.error("ERROR when creating VMWare istance with : " + instance, e);
            throw new RuntimeException("ERROR when creating VMWare istance with : " + instance, e);
        }

    }

    @Override
    public void deleteInstance(Infrastructure infrastructure, String instanceId) {

        getAllVirtualMachinesByInfrastructure(infrastructure).stream()
                .filter(vm -> vm.getConfig().getUuid().equals(instanceId)).findFirst().ifPresent(vm -> {
                    try {

                        if (Task.SUCCESS == vm.powerOffVM_Task().waitForTask()) {
                            Task task = vm.destroy_Task();
                            String result = task.waitForTask();
                            if (result != Task.SUCCESS) {

                                throw new RuntimeException("Unable to delete VMWare istance : " + instanceId +
                                    " Task result = " + result);
                            }
                        } else {
                            throw new RuntimeException("ERROR when powering OFF the istance : " + instanceId);
                        }

                    } catch (RemoteException | InterruptedException e) {
                        throw new RuntimeException("ERROR when deleting VMWare istance : " + instanceId, e);
                    }

                });

    }

    @Override
    public Set<Instance> getAllInfrastructureInstances(Infrastructure infrastructure) {

        return getAllVirtualMachinesByInfrastructure(
                infrastructure)
                        .stream().map(
                                vm -> Instance.builder().id(vm.getConfig().getUuid()).tag(vm.getName())
                                        .number("1")
                                        .hardware(Hardware.builder()
                                                .minCores(String.valueOf(
                                                        vm.getConfig().getHardware().getNumCPU()))
                                        .minRam((String.valueOf(vm.getConfig().getHardware().getMemoryMB())))
                                        .build()).status(String.valueOf(vm.getSummary().getOverallStatus()))
                .build()).collect(Collectors.toSet());

    }

    @Override
    public ScriptResult executeScriptOnInstanceId(Infrastructure infrastructure, String instanceId,
            InstanceScript instanceScript) {

        ScriptResult scriptResult = new ScriptResult(instanceId, "", "");

        try {

            VirtualMachine vm = getAllVirtualMachinesByInfrastructure(infrastructure).stream()
                    .filter(virtualMachine -> virtualMachine.getConfig().getUuid().equals(instanceId))
                    .findFirst().get();

            GuestOperationsManager gom = vmWareServiceInstanceCache.getServiceInstance(infrastructure)
                    .getGuestOperationsManager();

            NamePasswordAuthentication npa = new NamePasswordAuthentication();
            npa.username = instanceScript.getCredentials().getUsername();
            npa.password = instanceScript.getCredentials().getPassword();
            npa.interactiveSession = false;

            for (int i = 0; i < instanceScript.getScripts().length; i++) {
                GuestProgramSpec gps = new GuestProgramSpec();
                gps.workingDirectory = "/root";

                gps.programPath = "/bin/bash \n";
                gps.arguments = instanceScript.getScripts()[i];

                GuestProcessManager gpm = gom.getProcessManager(vm);
                System.out.println(gpm.startProgramInGuest(npa, gps));
                //scriptResult = scriptResult.withOutput(String.valueOf(gpm.startProgramInGuest(npa, gps)));
            }

        } catch (RemoteException e) {
            throw new RuntimeException(
                "ERROR when executing the script : " + instanceScript + " against instanceid : " + instanceId,
                e);
        }

        return scriptResult;
    }

    @Override
    public List<ScriptResult> executeScriptOnInstanceTag(Infrastructure infrastructure, String instanceTag,
            InstanceScript instanceScript) {

        return getAllVirtualMachinesByInfrastructure(infrastructure).stream()
                .filter(vm -> vm.getName().equals(instanceTag))
                .map(vm -> executeScriptOnInstanceId(infrastructure, vm.getConfig().getUuid(),
                        instanceScript))
                .collect(Collectors.toList());
    }

    @Override
    public Set<Image> getAllImages(Infrastructure infrastructure) {
        throw new RuntimeException("Operation not supported for VMWare");
    }

    @Override
    public void deleteInfrastructure(Infrastructure infrastructure) {
        getAllVirtualMachinesByInfrastructure(infrastructure).stream()
                .forEach(vm -> deleteInstance(infrastructure, vm.getConfig().getUuid()));
        vmWareServiceInstanceCache.removeServiceInstance(infrastructure);

    }

    private VirtualMachine cloneVM(String instanceImageId, Instance instance, Folder rootFolder,
            VirtualMachineCloneSpec vmcs, Folder vmFolder) {
        try {
            Task task = searchVirtualMachineByName(instanceImageId, rootFolder).cloneVM_Task(vmFolder,
                    instance.getTag(), vmcs);

            String result = task.waitForTask();
            if (Task.SUCCESS != result) {
                throw new RuntimeException(
                    "Unable to create VMWare istance with : " + instance + " Task result = " + result);
            }

            return searchVirtualMachineByName(instance.getTag(), rootFolder);
        } catch (RemoteException | InterruptedException e) {
            throw new RuntimeException("ERROR when creating VMWare istance with : " + instance, e);
        }
    }

    private VirtualMachineConfigSpec getVirtualMachineConfigSpec(Instance instance) {
        VirtualMachineConfigSpec vmconfigspec = new VirtualMachineConfigSpec();
        vmconfigspec.setMemoryMB(Long.valueOf(instance.getHardware().getMinRam()));
        vmconfigspec.setNumCPUs(Integer.valueOf(instance.getHardware().getMinCores()));
        return vmconfigspec;
    }

    private VirtualMachineRelocateSpec getVirtualMachineRelocateSpec(Folder rootFolder)
            throws InvalidProperty, RuntimeFault, RemoteException {
        VirtualMachineRelocateSpec vmrs = new VirtualMachineRelocateSpec();
        vmrs.setPool(getManagedObjectReference(rootFolder));
        return vmrs;
    }

    private VirtualMachine searchVirtualMachineByName(String name, Folder rootFolder)
            throws InvalidProperty, RuntimeFault, RemoteException {
        return (VirtualMachine) new InventoryNavigator(rootFolder).searchManagedEntity("VirtualMachine",
                name);
    }

    private ManagedObjectReference getManagedObjectReference(Folder rootFolder)
            throws InvalidProperty, RuntimeFault, RemoteException {
        ResourcePool rp = (ResourcePool) new InventoryNavigator(rootFolder)
                .searchManagedEntities("ResourcePool")[0];
        return rp.getMOR();
    }

    private Set<VirtualMachine> getAllVirtualMachinesByInfrastructure(Infrastructure infrastructure) {
        Set<VirtualMachine> virtualMachines = Sets.newHashSet();

        try {

            Folder rootFolder = vmWareServiceInstanceCache.getServiceInstance(infrastructure).getRootFolder();
            ManagedEntity[] managedEntities = new InventoryNavigator(rootFolder)
                    .searchManagedEntities("VirtualMachine");
            for (int i = 0; i < managedEntities.length; i++) {

                VirtualMachine vm = (VirtualMachine) managedEntities[i];

                virtualMachines.add(vm);

            }
        } catch (RemoteException e) {
            throw new RuntimeException(
                "ERROR when retrieving VMWare istances for infrastructure : " + infrastructure, e);
        }

        return virtualMachines;
    }

}

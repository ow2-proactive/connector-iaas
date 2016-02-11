package org.ow2.proactive.connector.iaas.cloud.provider.vmware;

import java.rmi.RemoteException;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

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
import com.vmware.vim25.CustomizationFixedName;
import com.vmware.vim25.CustomizationSpec;
import com.vmware.vim25.CustomizationSysprep;
import com.vmware.vim25.CustomizationUserData;
import com.vmware.vim25.GuestProgramSpec;
import com.vmware.vim25.ManagedObjectReference;
import com.vmware.vim25.NamePasswordAuthentication;
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

    private final static String FOLDER_WHERE_TO_DEPLOY_VM = "Activeeon"; //Set folder name where to deploy VM

    @Getter
    private final String type = "vmware";

    @Autowired
    private VMWareServiceInstanceCache vmWareServiceInstanceCache;

    @Autowired
    private VMWareInfrastructuresInstancesCache vmWareInfrastructuresInstancesCache;

    @Override
    public Set<Instance> createInstance(Infrastructure infrastructure, Instance instance) {

        Set<Instance> instancesCreated = Sets.newHashSet();
        try {
            Folder rootFolder = vmWareServiceInstanceCache.getServiceInstance(infrastructure).getRootFolder();

            VirtualMachine vmTemplate = (VirtualMachine) new InventoryNavigator(rootFolder)
                    .searchManagedEntity("VirtualMachine", instance.getImage());
            VirtualMachineCloneSpec vmcs = new VirtualMachineCloneSpec();
            CustomizationSpec customSpec = new CustomizationSpec();

            ResourcePool rp = (ResourcePool) new InventoryNavigator(rootFolder)
                    .searchManagedEntities("ResourcePool")[0];
            ManagedObjectReference mor = new ManagedObjectReference();
            mor = rp.getMOR();

            VirtualMachineRelocateSpec vmrs = new VirtualMachineRelocateSpec();
            vmrs.setPool(mor);

            vmcs.setLocation(vmrs);
            vmcs.setPowerOn(false);
            vmcs.setTemplate(false);

            VirtualMachineConfigSpec vmconfigspec = new VirtualMachineConfigSpec();
            vmconfigspec.setMemoryMB(Long.valueOf(instance.getHardware().getMinRam()));
            vmconfigspec.setNumCPUs(Integer.valueOf(instance.getHardware().getMinCores()));

            vmcs.setConfig(vmconfigspec);

            CustomizationFixedName fixedName = new CustomizationFixedName();
            fixedName.setName(instance.getImage());

            CustomizationSysprep sprep = new CustomizationSysprep();
            CustomizationUserData custUserData = new CustomizationUserData();
            custUserData.setComputerName(fixedName);

            sprep.setUserData(custUserData);

            customSpec.setIdentity(sprep);

            Folder vmFolder = (Folder) new InventoryNavigator(rootFolder).searchManagedEntity("Folder",
                    FOLDER_WHERE_TO_DEPLOY_VM);

            Task task = vmTemplate.cloneVM_Task(vmFolder, instance.getTag(), vmcs);

            String result = task.waitForTask();
            if (Task.SUCCESS != result) {
                throw new RuntimeException(
                    "Unable to create VMWare istance with : " + instance + " Task result = " + result);
            }

            VirtualMachine vm = (VirtualMachine) new InventoryNavigator(rootFolder)
                    .searchManagedEntity("VirtualMachine", instance.getTag());

            instancesCreated.add(instance.withId(vm.getConfig().getUuid()));

            vmWareInfrastructuresInstancesCache.addInstanceIdToInfrastructure(infrastructure.getId(),
                    vm.getConfig().getUuid());

        } catch (RemoteException | InterruptedException e) {
            throw new RuntimeException("ERROR when creating VMWare istance with : " + instance, e);
        }
        return instancesCreated;

    }

    @Override
    public void deleteInstance(Infrastructure infrastructure, String instanceId) {

        getAllVirtualMachinesByInfrastructure(infrastructure).stream()
                .filter(vm -> vm.getConfig().getUuid().equals(instanceId)).findFirst().ifPresent(vm -> {
                    try {
                        Task task = vm.destroy_Task();
                        String result = task.waitForTask();
                        if (result == Task.SUCCESS) {
                            vmWareInfrastructuresInstancesCache
                                    .removeInstanceIdFromInfrastructure(infrastructure.getId(), instanceId);
                        } else {
                            throw new RuntimeException("Unable to delete VMWare istance : " + instanceId +
                                " Task result = " + result);
                        }
                    } catch (RemoteException | InterruptedException e) {
                        throw new RuntimeException("ERROR when deleting VMWare istance : " + instanceId, e);
                    }

                });

    }

    @Override
    public Set<Instance> getAllInfrastructureInstances(Infrastructure infrastructure) {

        return getAllVirtualMachinesByInfrastructure(infrastructure).stream()
                .filter(vm -> vmWareInfrastructuresInstancesCache.infrastructureContainsInstanceId(
                        infrastructure.getId(), vm.getConfig().getUuid()))
                .map(vm -> Instance.builder().id(vm.getConfig().getUuid()).tag(vm.getName()).number("1")
                        .hardware(Hardware.builder()
                                .minCores(String.valueOf(vm.getConfig().getHardware().getNumCPU()))
                                .minRam((String.valueOf(vm.getConfig().getHardware().getMemoryMB()))).build())
                        .status(String.valueOf(vm.getSummary().getOverallStatus())).build())
                .collect(Collectors.toSet());

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

                gps.programPath = "/bin/bash";
                gps.arguments = instanceScript.getScripts()[i];

                GuestProcessManager gpm = gom.getProcessManager(vm);
                scriptResult = scriptResult.withOutput(String.valueOf(gpm.startProgramInGuest(npa, gps)));
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
        vmWareInfrastructuresInstancesCache.removeInfrastructure(infrastructure.getId());
        vmWareServiceInstanceCache.removeServiceInstance(infrastructure);

    }

}

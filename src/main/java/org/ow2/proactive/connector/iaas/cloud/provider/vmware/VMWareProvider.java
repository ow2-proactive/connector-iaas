package org.ow2.proactive.connector.iaas.cloud.provider.vmware;

import java.rmi.RemoteException;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

import javax.ws.rs.NotSupportedException;

import org.apache.log4j.Logger;
import org.ow2.proactive.connector.iaas.cloud.provider.CloudProvider;
import org.ow2.proactive.connector.iaas.model.Hardware;
import org.ow2.proactive.connector.iaas.model.Image;
import org.ow2.proactive.connector.iaas.model.Infrastructure;
import org.ow2.proactive.connector.iaas.model.Instance;
import org.ow2.proactive.connector.iaas.model.InstanceScript;
import org.ow2.proactive.connector.iaas.model.Network;
import org.ow2.proactive.connector.iaas.model.ScriptResult;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import com.google.common.collect.Sets;
import com.vmware.vim25.GuestProgramSpec;
import com.vmware.vim25.NamePasswordAuthentication;
import com.vmware.vim25.VirtualMachineCloneSpec;
import com.vmware.vim25.VirtualMachineConfigSpec;
import com.vmware.vim25.mo.Folder;
import com.vmware.vim25.mo.GuestOperationsManager;
import com.vmware.vim25.mo.GuestProcessManager;
import com.vmware.vim25.mo.Task;
import com.vmware.vim25.mo.VirtualMachine;

import lombok.Getter;


@Component
public class VMWareProvider implements CloudProvider {

    private final Logger logger = Logger.getLogger(VMWareProvider.class);

    @Getter
    private final String type = "vmware";

    @Autowired
    private VMWareServiceInstanceCache vmWareServiceInstanceCache;

    @Autowired
    private VMWareProviderVirualMachineUtil vmWareProviderVirualMachineUtil;

    @Override
    public Set<Instance> createInstance(Infrastructure infrastructure, Instance instance) {

        try {

            String instanceFolder = instance.getImage().split("/")[0];
            String instanceImageId = instance.getImage().split("/")[1];

            Folder rootFolder = vmWareServiceInstanceCache.getServiceInstance(infrastructure).getRootFolder();

            VirtualMachineCloneSpec vmcs = new VirtualMachineCloneSpec();

            vmcs.setLocation(vmWareProviderVirualMachineUtil.getVirtualMachineRelocateSpec(rootFolder));
            vmcs.setPowerOn(true);
            vmcs.setTemplate(false);
            vmcs.setConfig(getVirtualMachineConfigSpec(instance));

            Folder vmFolder = vmWareProviderVirualMachineUtil.searchFolderByName(instanceFolder, rootFolder);

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

        vmWareProviderVirualMachineUtil
                .getAllVirtualMachinesByInfrastructure(
                        vmWareServiceInstanceCache.getServiceInstance(infrastructure).getRootFolder(),
                        infrastructure)
                .stream().filter(vm -> vm.getConfig().getUuid().equals(instanceId)).findFirst()
                .ifPresent(vm -> {
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

        return vmWareProviderVirualMachineUtil
                .getAllVirtualMachinesByInfrastructure(
                        vmWareServiceInstanceCache.getServiceInstance(infrastructure).getRootFolder(),
                        infrastructure)
                .stream()
                .map(vm -> Instance.builder().id(vm.getConfig().getUuid()).tag(vm.getName()).number("1")
                        .hardware(Hardware.builder()
                                .minCores(String.valueOf(vm.getConfig().getHardware().getNumCPU()))
                                .minRam((String.valueOf(vm.getConfig().getHardware().getMemoryMB()))).build())
                        
                        .network(Network.builder().publicAddresses(Sets.newHashSet(vm.getGuest().getIpAddress())).build())
                        
                        .status(String.valueOf(vm.getSummary().getOverallStatus())).build())
                .collect(Collectors.toSet());

    }

   


	@Override
    public ScriptResult executeScriptOnInstanceId(Infrastructure infrastructure, String instanceId,
            InstanceScript instanceScript) {

        ScriptResult scriptResult = new ScriptResult(instanceId, "", "");

        try {

            VirtualMachine vm = vmWareProviderVirualMachineUtil
                    .getAllVirtualMachinesByInfrastructure(
                            vmWareServiceInstanceCache.getServiceInstance(infrastructure).getRootFolder(),
                            infrastructure)
                    .stream()
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

                gps.programPath = "/bin/bash";
                gps.arguments = instanceScript.getScripts()[i];

                GuestProcessManager gpm = gom.getProcessManager(vm);

                scriptResult = scriptResult.withOutput(
                        (scriptResult.getOutput() + " " + String.valueOf(gpm.startProgramInGuest(npa, gps)))
                                .trim());
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

        return vmWareProviderVirualMachineUtil
                .getAllVirtualMachinesByInfrastructure(
                        vmWareServiceInstanceCache.getServiceInstance(infrastructure).getRootFolder(),
                        infrastructure)
                .stream().filter(vm -> vm.getName().equals(instanceTag))
                .map(vm -> executeScriptOnInstanceId(infrastructure, vm.getConfig().getUuid(),
                        instanceScript))
                .collect(Collectors.toList());
    }

    @Override
    public Set<Image> getAllImages(Infrastructure infrastructure) {
        throw new NotSupportedException("Operation not supported for VMWare");
    }

    @Override
    public void deleteInfrastructure(Infrastructure infrastructure) {
        vmWareProviderVirualMachineUtil
                .getAllVirtualMachinesByInfrastructure(
                        vmWareServiceInstanceCache.getServiceInstance(infrastructure).getRootFolder(),
                        infrastructure)
                .stream().forEach(vm -> deleteInstance(infrastructure, vm.getConfig().getUuid()));
        vmWareServiceInstanceCache.removeServiceInstance(infrastructure);

    }

    @Override
    public String addToInstancePublicIp(Infrastructure infrastructure, String  instanceId) {
        throw new NotSupportedException("Operation not supported for VMWare");
    }

    @Override
    public void removeInstancePublicIp(Infrastructure infrastructure, String instanceId) {
        throw new NotSupportedException("Operation not supported for VMWare");
    }

    private VirtualMachine cloneVM(VirtualMachine vm, String newVMname, Instance instance, Folder rootFolder,
            VirtualMachineCloneSpec vmcs, Folder vmFolder) {
        try {
            // Clone the VM : call to VMWare API
            Task task = vm.cloneVM_Task(vmFolder, newVMname, vmcs);

            String result = task.waitForTask();
            if (Task.SUCCESS != result) {
                throw new RuntimeException(
                    "Unable to create VMWare instance with : " + instance + " Task result = " + result);
            }

            return vmWareProviderVirualMachineUtil.searchVirtualMachineByName(newVMname, rootFolder);
        } catch (RemoteException | InterruptedException e) {
            throw new RuntimeException("ERROR when creating VMWare instance with : " + instance, e);
        }
    }

    private VirtualMachineConfigSpec getVirtualMachineConfigSpec(Instance instance) {
        VirtualMachineConfigSpec vmconfigspec = new VirtualMachineConfigSpec();
        vmconfigspec.setMemoryMB(Long.valueOf(instance.getHardware().getMinRam()));
        vmconfigspec.setNumCPUs(Integer.valueOf(instance.getHardware().getMinCores()));
        return vmconfigspec;
    }

    /**
     * Assign a single MAC address per VM
     *
     * @param vm            the original VM to clone
     * @param vmConfigSpecs VirtualMachineConfigSpec objects' array, one for each VM to start
     * @param macAddresses  set of MAC addresses to assign to the VMs (one for each)
     */
    private void assignMacAddresses(VirtualMachine vm, VirtualMachineConfigSpec[] vmConfigSpecs, Set<String> macAddresses) {

        Iterator<String> macIt = macAddresses.iterator();

        // Customize each VirtualMachineConfigSpec
        for (int i=0; i<vmConfigSpecs.length; i++) {

            // Assign MAC addresses as long as remaining
            if (macIt.hasNext()) {
                VirtualDevice[] virtualDevices = vm.getConfig().getHardware().getDevice();
                VirtualEthernetCard virtEthCard = new VirtualEthernetCard();

                // Look for an Ethernet card
                for (int j = 0; j < virtualDevices.length; j++) {
                    if (virtualDevices[j] instanceof VirtualEthernetCard) {

                        // Take the first Ethernet card found (change MAC address on the first adapter only)
                        virtEthCard = (VirtualEthernetCard) virtualDevices[j];
                        virtEthCard.setAddressType("Manual");
                        virtEthCard.setMacAddress(macIt.next());
                        break;
                    }
                }

                // Set the change
                VirtualDeviceConfigSpec virtDevConfSpec = new VirtualDeviceConfigSpec();
                virtDevConfSpec.setDevice(virtEthCard);
                virtDevConfSpec.setOperation(VirtualDeviceConfigSpecOperation.edit);
                vmConfigSpecs[i].setDeviceChange(new VirtualDeviceConfigSpec[]{virtDevConfSpec});
            }
            else {
                return;
            }
        }
    }
}

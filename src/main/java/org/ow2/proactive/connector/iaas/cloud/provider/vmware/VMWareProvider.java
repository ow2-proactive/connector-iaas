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

import java.rmi.RemoteException;
import java.util.List;
import java.util.Optional;
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
    private VMWareProviderVirtualMachineUtil vmWareProviderVirtualMachineUtil;

    @Autowired
    private VMWareProviderMacAddressHandler vmWareProviderMacAddressHandler;

    @Override
    public Set<Instance> createInstance(Infrastructure infrastructure, Instance instance) {

        try {

            String instanceFolder = instance.getImage().split("/")[0];
            String instanceImageId = instance.getImage().split("/")[1];

            Folder rootFolder = vmWareServiceInstanceCache.getServiceInstance(infrastructure).getRootFolder();
            Folder vmFolder = vmWareProviderVirtualMachineUtil.searchFolderByName(instanceFolder, rootFolder);
            VirtualMachine vmToClone = vmWareProviderVirtualMachineUtil.searchVirtualMachineByName(instanceImageId,
                                                                                                   rootFolder);

            return IntStream.rangeClosed(1, Integer.valueOf(instance.getNumber()))
                            .mapToObj(instanceIndexStartAt1 -> cloneVM(vmToClone,
                                                                       createUniqInstanceTag(instance.getTag(),
                                                                                             instanceIndexStartAt1),
                                                                       instance,
                                                                       rootFolder,
                                                                       createVirtualMachineCloneSpec(instanceIndexStartAt1,
                                                                                                     vmToClone,
                                                                                                     instance),
                                                                       vmFolder))
                            .map(vm -> instance.withId(vm.getConfig().getUuid()))
                            .collect(Collectors.toSet());

        } catch (RemoteException e) {
            logger.error("ERROR when creating VMWare instance with : " + instance, e);
            throw new RuntimeException("ERROR when creating VMWare instance with : " + instance, e);
        }

    }

    /**
     * Create a uniq tag for a VM based on the original tag provided and the instance index
     *
     * @param tagBase       the tag base
     * @param instanceIndex the instance index
     * @return a uniq VM tag
     */
    private String createUniqInstanceTag(String tagBase, int instanceIndex) {
        if (instanceIndex > 1) {
            return tagBase + "_" + String.valueOf(instanceIndex);
        }
        return tagBase;
    }

    /**
     * Create a new VirtualMachineCloneSpec based on the specified VM to clone.
     * Customize it with a specific MAC address if defined in the instance's options parameter at the specified index
     *
     * @param instanceIndexStartAt1 the index to look for a MAC address
     * @param vmToClone             the intial VM to clone
     * @param instance              the instance to rely on
     * @return a new VirtualMachineCloneSpec that may be customized with the desired MAC address' index
     */
    private VirtualMachineCloneSpec createVirtualMachineCloneSpec(int instanceIndexStartAt1, VirtualMachine vmToClone,
            Instance instance) {

        // Create a new VirtualMachineCloneSpec based on the specified VM to clone.
        VirtualMachineCloneSpec vmCloneSpecs = generateDefaultVirtualMachineCloneSpec(vmToClone, instance);

        // Customize it with a manual MAC address, if specified
        getMacAddressIfPresent(instanceIndexStartAt1,
                               instance).ifPresent(macAddress -> vmWareProviderMacAddressHandler.getVirtualDeviceConfigWithMacAddress(macAddress,
                                                                                                                                      vmToClone)
                                                                                                .ifPresent(virtDevConfSpec -> vmCloneSpecs.getConfig()
                                                                                                                                          .setDeviceChange(virtDevConfSpec)));
        return vmCloneSpecs;
    }

    /**
     * Retrieve an optional MAC address from the Instance 'options' parameter and the specified index
     *
     * @param instanceIndexStartAt1 the index of the MAC address to retrieve
     * @param instance              the instance to rely on
     * @return the desired MAC address (if it exists)
     */
    private Optional<String> getMacAddressIfPresent(int instanceIndexStartAt1, Instance instance) {
        Optional<String> macAddress = Optional.empty();
        if (instance.getOptions() != null && instance.getOptions().getMacAddresses() != null) {
            macAddress = Optional.ofNullable(instance.getOptions().getMacAddresses().get(instanceIndexStartAt1 - 1));
        }
        return macAddress;
    }

    /**
     * Generate a new VirtualMachineCloneSpec with parameters by default and based on the provided VM to clone
     *
     * @param vmToClone the source VM to rely on
     * @param instance  the current instance to rely on
     * @return a new customized VirtualMachineCloneSpec
     */
    private VirtualMachineCloneSpec generateDefaultVirtualMachineCloneSpec(VirtualMachine vmToClone,
            Instance instance) {
        VirtualMachineCloneSpec vmCloneSpecs = new VirtualMachineCloneSpec();
        vmCloneSpecs.setLocation(vmWareProviderVirtualMachineUtil.getVirtualMachineRelocateSpec(vmToClone));
        vmCloneSpecs.setPowerOn(true);
        vmCloneSpecs.setTemplate(false);
        vmCloneSpecs.setConfig(getVirtualMachineConfigSpec(instance));
        return vmCloneSpecs;
    }

    @Override
    public void deleteInstance(Infrastructure infrastructure, String instanceId) {

        vmWareProviderVirtualMachineUtil.getAllVirtualMachinesByInfrastructure(vmWareServiceInstanceCache.getServiceInstance(infrastructure)
                                                                                                         .getRootFolder(),
                                                                               infrastructure)
                                        .stream()
                                        .filter(vm -> vm.getConfig().getUuid().equals(instanceId))
                                        .findFirst()
                                        .ifPresent(vm -> {
                                            try {

                                                if (Task.SUCCESS == vm.powerOffVM_Task().waitForTask()) {
                                                    Task task = vm.destroy_Task();
                                                    String result = task.waitForTask();
                                                    if (result != Task.SUCCESS) {

                                                        throw new RuntimeException("Unable to delete VMWare instance : " +
                                                                                   instanceId + " Task result = " +
                                                                                   result);
                                                    }
                                                } else {
                                                    throw new RuntimeException("ERROR when powering OFF the instance : " +
                                                                               instanceId);
                                                }

                                            } catch (RemoteException | InterruptedException e) {
                                                throw new RuntimeException("ERROR when deleting VMWare instance : " +
                                                                           instanceId, e);
                                            }

                                        });

    }

    @Override
    public Set<Instance> getAllInfrastructureInstances(Infrastructure infrastructure) {

        return vmWareProviderVirtualMachineUtil.getAllVirtualMachinesByInfrastructure(vmWareServiceInstanceCache.getServiceInstance(infrastructure)
                                                                                                                .getRootFolder(),
                                                                                      infrastructure)
                                               .stream()
                                               .filter(vm -> vm.getConfig() != null)
                                               .map(vm -> Instance.builder()
                                                                  .id(vm.getConfig().getUuid())
                                                                  .tag(vm.getName())
                                                                  .number("1")
                                                                  .hardware(Hardware.builder()
                                                                                    .minCores(String.valueOf(vm.getConfig()
                                                                                                               .getHardware()
                                                                                                               .getNumCPU()))
                                                                                    .minRam((String.valueOf(vm.getConfig()
                                                                                                              .getHardware()
                                                                                                              .getMemoryMB())))
                                                                                    .build())

                                                                  .network(Network.builder()
                                                                                  .publicAddresses(Sets.newHashSet(vm.getGuest()
                                                                                                                     .getIpAddress()))
                                                                                  .build())

                                                                  .status(String.valueOf(vm.getSummary()
                                                                                           .getOverallStatus()))
                                                                  .build())
                                               .collect(Collectors.toSet());

    }

    @Override
    public ScriptResult executeScriptOnInstanceId(Infrastructure infrastructure, String instanceId,
            InstanceScript instanceScript) {

        ScriptResult scriptResult = new ScriptResult(instanceId, "", "");

        try {

            VirtualMachine vm = vmWareProviderVirtualMachineUtil.getAllVirtualMachinesByInfrastructure(vmWareServiceInstanceCache.getServiceInstance(infrastructure)
                                                                                                                                 .getRootFolder(),
                                                                                                       infrastructure)
                                                                .stream()
                                                                .filter(virtualMachine -> virtualMachine.getConfig()
                                                                                                        .getUuid()
                                                                                                        .equals(instanceId))
                                                                .findFirst()
                                                                .get();

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

                scriptResult = scriptResult.withOutput((scriptResult.getOutput() + " " +
                                                        String.valueOf(gpm.startProgramInGuest(npa, gps))).trim());
            }

        } catch (RemoteException e) {
            throw new RuntimeException("ERROR when executing the script : " + instanceScript +
                                       " against instance id : " + instanceId, e);
        }

        return scriptResult;
    }

    @Override
    public List<ScriptResult> executeScriptOnInstanceTag(Infrastructure infrastructure, String instanceTag,
            InstanceScript instanceScript) {

        return vmWareProviderVirtualMachineUtil.getAllVirtualMachinesByInfrastructure(vmWareServiceInstanceCache.getServiceInstance(infrastructure)
                                                                                                                .getRootFolder(),
                                                                                      infrastructure)
                                               .stream()
                                               .filter(vm -> vm.getName().equals(instanceTag))
                                               .map(vm -> executeScriptOnInstanceId(infrastructure,
                                                                                    vm.getConfig().getUuid(),
                                                                                    instanceScript))
                                               .collect(Collectors.toList());
    }

    @Override
    public Set<Image> getAllImages(Infrastructure infrastructure) {
        throw new NotSupportedException("Operation not supported for VMWare");
    }

    @Override
    public void deleteInfrastructure(Infrastructure infrastructure) {
        vmWareProviderVirtualMachineUtil.getAllVirtualMachinesByInfrastructure(vmWareServiceInstanceCache.getServiceInstance(infrastructure)
                                                                                                         .getRootFolder(),
                                                                               infrastructure)
                                        .stream()
                                        .forEach(vm -> deleteInstance(infrastructure, vm.getConfig().getUuid()));
        vmWareServiceInstanceCache.removeServiceInstance(infrastructure);

    }

    @Override
    public String addToInstancePublicIp(Infrastructure infrastructure, String instanceId) {
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
                throw new RuntimeException("Unable to create VMWare instance with : " + instance + " Task result = " +
                                           result);
            }

            return vmWareProviderVirtualMachineUtil.searchVirtualMachineByName(newVMname, rootFolder);
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

}

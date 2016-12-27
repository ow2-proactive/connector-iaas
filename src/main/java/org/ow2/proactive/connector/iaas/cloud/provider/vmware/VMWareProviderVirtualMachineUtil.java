package org.ow2.proactive.connector.iaas.cloud.provider.vmware;

import java.rmi.RemoteException;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

import org.ow2.proactive.connector.iaas.model.Infrastructure;
import org.springframework.stereotype.Component;

import com.vmware.vim25.InvalidProperty;
import com.vmware.vim25.RuntimeFault;
import com.vmware.vim25.VirtualMachineRelocateSpec;
import com.vmware.vim25.mo.Folder;
import com.vmware.vim25.mo.InventoryNavigator;
import com.vmware.vim25.mo.ManagedEntity;
import com.vmware.vim25.mo.VirtualMachine;


@Component
public class VMWareProviderVirtualMachineUtil {

    /**
     * Create a new VirtualMachineRelocateSpec based on the VM to clone
     *
     * @param vmSource  The source VM to rely on
     * @return  a new customized VirtualMachineRelocateSpec
     */
    public VirtualMachineRelocateSpec getVirtualMachineRelocateSpec(VirtualMachine vmSource) {
        try {
            VirtualMachineRelocateSpec vmrs = new VirtualMachineRelocateSpec();
            vmrs.setPool(vmSource.getResourcePool().getMOR());
            return vmrs;
        } catch (RemoteException e) {
            throw new RuntimeException(e);
        }

    }

    public VirtualMachine searchVirtualMachineByName(String name, Folder rootFolder)
            throws InvalidProperty, RuntimeFault, RemoteException {
        return (VirtualMachine) new InventoryNavigator(rootFolder).searchManagedEntity("VirtualMachine",
                name);
    }

    public Set<VirtualMachine> getAllVirtualMachinesByInfrastructure(Folder rootFolder,
            Infrastructure infrastructure) {

        try {

            ManagedEntity[] managedEntities = new InventoryNavigator(rootFolder)
                    .searchManagedEntities("VirtualMachine");

            return IntStream.range(0, managedEntities.length)
                    .mapToObj(i -> (VirtualMachine) managedEntities[i]).collect(Collectors.toSet());

        } catch (RemoteException e) {
            throw new RuntimeException(
                "ERROR when retrieving VMWare istances for infrastructure : " + infrastructure, e);
        }

    }

    public Folder searchFolderByName(String name, Folder rootFolder)
            throws InvalidProperty, RuntimeFault, RemoteException {
        return (Folder) new InventoryNavigator(rootFolder).searchManagedEntity("Folder", name);
    }

}

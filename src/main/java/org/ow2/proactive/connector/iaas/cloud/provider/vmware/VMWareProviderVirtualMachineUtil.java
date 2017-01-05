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
        return (VirtualMachine) new InventoryNavigator(rootFolder).searchManagedEntity("VirtualMachine", name);
    }

    public Set<VirtualMachine> getAllVirtualMachinesByInfrastructure(Folder rootFolder, Infrastructure infrastructure) {

        try {

            ManagedEntity[] managedEntities = new InventoryNavigator(rootFolder).searchManagedEntities("VirtualMachine");

            return IntStream.range(0, managedEntities.length)
                            .mapToObj(i -> (VirtualMachine) managedEntities[i])
                            .collect(Collectors.toSet());

        } catch (RemoteException e) {
            throw new RuntimeException("ERROR when retrieving VMWare istances for infrastructure : " + infrastructure,
                                       e);
        }

    }

    public Folder searchFolderByName(String name, Folder rootFolder)
            throws InvalidProperty, RuntimeFault, RemoteException {
        return (Folder) new InventoryNavigator(rootFolder).searchManagedEntity("Folder", name);
    }

}

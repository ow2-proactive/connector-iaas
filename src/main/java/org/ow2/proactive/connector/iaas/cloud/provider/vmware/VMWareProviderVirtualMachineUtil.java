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
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Optional;
import java.util.Random;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

import org.springframework.stereotype.Component;

import com.google.common.collect.Lists;
import com.vmware.vim25.mo.Datastore;
import com.vmware.vim25.mo.Folder;
import com.vmware.vim25.mo.HostSystem;
import com.vmware.vim25.mo.InventoryNavigator;
import com.vmware.vim25.mo.ManagedEntity;
import com.vmware.vim25.mo.ResourcePool;
import com.vmware.vim25.mo.VirtualMachine;

import lombok.Getter;
import lombok.RequiredArgsConstructor;


@Component
public class VMWareProviderVirtualMachineUtil {

    @RequiredArgsConstructor
    enum EntityType {
        VM("VirtualMachine"),
        FOLDER("Folder"),
        HOST("HostSystem"),
        POOL("ResourcePool");

        @Getter
        private final String value;
    }

    public Optional<VirtualMachine> searchVirtualMachineByName(String name, Folder rootFolder) {
        try {
            return Optional.ofNullable((VirtualMachine) new InventoryNavigator(rootFolder).searchManagedEntity(EntityType.VM.getValue(),
                                                                                                               name));
        } catch (RemoteException e) {
            throw new RuntimeException("ERROR when retrieving VMWare virtual machine with name: " + name, e);
        }
    }

    public Optional<VirtualMachine> searchVirtualMachineByUUID(String uuid, Folder rootFolder) {
        try {
            return Lists.newArrayList(new InventoryNavigator(rootFolder).searchManagedEntities(EntityType.VM.getValue()))
                        .stream()
                        .map(virtualMachine -> (VirtualMachine) virtualMachine)
                        .filter(virtualMachine -> virtualMachine.getConfig() != null)
                        .filter(virtualMachine -> virtualMachine.getConfig().getUuid().equals(uuid))
                        .findFirst();
        } catch (RemoteException e) {
            throw new RuntimeException("ERROR when retrieving VMWare virtual machines" + uuid, e);
        }
    }

    public Set<VirtualMachine> getAllVirtualMachines(Folder rootFolder) {
        try {
            ManagedEntity[] managedEntities = new InventoryNavigator(rootFolder).searchManagedEntities(EntityType.VM.getValue());

            return IntStream.range(0, managedEntities.length)
                            .mapToObj(i -> (VirtualMachine) managedEntities[i])
                            .collect(Collectors.toSet());

        } catch (RemoteException e) {
            throw new RuntimeException("ERROR when retrieving VMWare virtual machines", e);
        }
    }

    public Optional<HostSystem> searchHostByName(String name, Folder rootFolder) {
        try {
            return Optional.ofNullable((HostSystem) new InventoryNavigator(rootFolder).searchManagedEntity(EntityType.HOST.getValue(),
                                                                                                           name));
        } catch (RemoteException e) {
            throw new RuntimeException("ERROR when retrieving VMWare host with name: " + name, e);
        }
    }

    public Optional<Folder> searchFolderByName(String name, Folder rootFolder) {
        try {
            return Optional.ofNullable((Folder) new InventoryNavigator(rootFolder).searchManagedEntity(EntityType.FOLDER.getValue(),
                                                                                                       name));
        } catch (RemoteException e) {
            throw new RuntimeException("ERROR when retrieving VMWare folder with name: " + name, e);
        }
    }

    public Optional<Folder> searchVMFolderFromVMName(String name, Folder rootFolder) {
        ManagedEntity vmFolder = null;
        Optional<VirtualMachine> vm = searchVirtualMachineByName(name, rootFolder);
        if (vm.isPresent()) {
            ManagedEntity current = vm.get().getParent();
            while (current != null && !(current instanceof Folder)) {
                current = current.getParent();
            }
            vmFolder = current;
        }
        return Optional.ofNullable((Folder) vmFolder);
    }

    public Optional<Folder> searchVMFolderByHostname(String hostname, Folder rootFolder) {
        try {
            return Lists.newArrayList(new InventoryNavigator(rootFolder).searchManagedEntities(EntityType.FOLDER.getValue()))
                        .stream()
                        .map(folder -> (Folder) folder)
                        .filter(folder -> folder.getName().toLowerCase().contains("vm") &&
                                          folder.getName().toLowerCase().contains(hostname.toLowerCase()))
                        .findAny();
        } catch (RemoteException e) {
            throw new RuntimeException("ERROR when retrieving VMWare folders", e);
        }
    }

    public Optional<ResourcePool> searchResourcePoolByHostname(String hostname, Folder rootFolder) {
        try {
            return Lists.newArrayList(new InventoryNavigator(rootFolder).searchManagedEntities(EntityType.POOL.getValue()))
                        .stream()
                        .map(resourcePool -> (ResourcePool) resourcePool)
                        .filter(resourcePool -> resourcePool.getParent().getName().equals(hostname))
                        .findAny();
        } catch (RemoteException e) {
            throw new RuntimeException("ERROR when retrieving VMWare resource pools", e);
        }
    }

    public Optional<Datastore> getDatastoreWithMostSpaceFromHost(HostSystem host) {
        try {
            return Lists.newArrayList(host.getDatastores())
                        .stream()
                        .filter(datastore -> datastore.getSummary().isAccessible())
                        .max(Comparator.comparing(datastore -> (int) datastore.getSummary().getFreeSpace()));
        } catch (RemoteException e) {
            throw new RuntimeException("ERROR when retrieving VMWare datastores from host: " + host.getName(), e);
        }
    }

    public Optional<Datastore> getDatastoreWithMostSpaceFromPool(ResourcePool resourcePool) {
        try {
            return Lists.newArrayList(resourcePool.getOwner().getDatastores())
                        .stream()
                        .filter(datastore -> datastore.getSummary().isAccessible())
                        .max(Comparator.comparing(datastore -> (int) datastore.getSummary().getFreeSpace()));
        } catch (RemoteException e) {
            throw new RuntimeException("ERROR when retrieving VMWare resource pool's owner from: " +
                                       resourcePool.getName(), e);
        }
    }

    public Optional<ResourcePool> getRandomResourcePool(Folder rootFolder) {
        List<ResourcePool> resourcePools = null;
        try {
            resourcePools = Lists.newArrayList(new InventoryNavigator(rootFolder).searchManagedEntities(EntityType.POOL.getValue()))
                                 .stream()
                                 .map(resourcePool -> (ResourcePool) resourcePool)
                                 .collect(Collectors.toCollection(ArrayList::new));
        } catch (RemoteException e) {
            throw new RuntimeException("ERROR when retrieving VMWare resource pool", e);
        }
        return Optional.ofNullable(resourcePools.get(new Random().nextInt(resourcePools.size())));
    }
}

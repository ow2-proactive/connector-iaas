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
package org.ow2.proactive.connector.iaas.cloud.provider.azure;

import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;

import org.springframework.stereotype.Component;

import com.microsoft.azure.management.Azure;
import com.microsoft.azure.management.compute.VirtualMachine;
import com.microsoft.azure.management.network.Network;
import com.microsoft.azure.management.network.NetworkSecurityGroup;
import com.microsoft.azure.management.network.PublicIpAddress;
import com.microsoft.azure.management.resources.ResourceGroup;


@Component
public class AzureProviderUtils {

    public Optional<VirtualMachine> searchVirtualMachineByName(Azure azureService, String name) {

        return azureService.virtualMachines()
                           .list()
                           .stream()
                           .filter(availableVM -> availableVM.name().equals(name))
                           .findAny();
    }

    public Optional<VirtualMachine> searchVirtualMachineByID(Azure azureService, String id) {

        // Get VM by 'relative' ID instead of absolute ID (default Azure's getById method)
        return azureService.virtualMachines()
                           .list()
                           .stream()
                           .filter(availableVM -> availableVM.vmId().equals(id))
                           .findAny();
    }

    public Optional<ResourceGroup> searchResourceGroupByName(Azure azureService, String name) {
        return azureService.resourceGroups()
                           .list()
                           .stream()
                           .filter(resourceGroup -> resourceGroup.name().equals(name))
                           .findAny();
    }

    public Optional<NetworkSecurityGroup> searchNetworkSecurityGroupByName(Azure azureService, String name) {
        return azureService.networkSecurityGroups()
                           .list()
                           .stream()
                           .filter(networkSecurityGroups -> networkSecurityGroups.name().equals(name))
                           .findAny();
    }

    public Optional<Network> searchVirtualNetworkByName(Azure azureService, String name) {
        return azureService.networks()
                           .list()
                           .stream()
                           .filter(virtualNetwork -> virtualNetwork.name().equals(name))
                           .findAny();
    }

    public Optional<PublicIpAddress> searchPublicIpAddressByIp(Azure azureService, String ip) {
        return azureService.publicIpAddresses()
                           .list()
                           .stream()
                           .filter(publicIpAddress -> publicIpAddress.ipAddress().equals(ip))
                           .findAny();
    }

    public Set<VirtualMachine> getAllVirtualMachines(Azure azureService) {

        return azureService.virtualMachines().list().stream().collect(Collectors.toSet());
    }

}

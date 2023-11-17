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

import java.util.HashSet;
import java.util.Optional;
import java.util.Set;

import org.springframework.stereotype.Component;

import com.microsoft.azure.management.Azure;
import com.microsoft.azure.management.compute.VirtualMachine;
import com.microsoft.azure.management.compute.VirtualMachineScaleSet;
import com.microsoft.azure.management.network.LoadBalancer;
import com.microsoft.azure.management.network.Network;
import com.microsoft.azure.management.network.NetworkSecurityGroup;
import com.microsoft.azure.management.network.PublicIPAddress;
import com.microsoft.azure.management.resources.ResourceGroup;

import lombok.extern.log4j.Log4j2;


@Component
@Log4j2
public class AzureProviderUtils {

    public Optional<VirtualMachine> searchVirtualMachineByName(Azure azureService, String name) {
        return azureService.virtualMachines()
                           .list()
                           .stream()
                           .filter(availableVM -> availableVM.name().equals(name))
                           .findAny();
    }

    public Optional<VirtualMachine> searchVirtualMachineByID(Azure azureService, String id) {
        Optional<VirtualMachine> answer = azureService.virtualMachines()
                                                      .list()
                                                      .stream()
                                                      .filter(availableVM -> availableVM.vmId().equals(id))
                                                      .findAny();
        if (!answer.isPresent()) {
            log.error("Cannot find instance with id {}", id);
            log.error("Existing instances:");
            azureService.virtualMachines()
                        .list()
                        .stream()
                        .forEach(availableVM -> log.error("name: {} computerName: {} vmId: {} powerState: {}",
                                                          availableVM.name(),
                                                          availableVM.computerName(),
                                                          availableVM.vmId(),
                                                          availableVM.powerState()));
        }

        return answer;
    }

    public Optional<ResourceGroup> searchResourceGroupByName(Azure azureService, String name) {
        return Optional.ofNullable(azureService.resourceGroups().getByName(name));
    }

    public Optional<NetworkSecurityGroup> searchNetworkSecurityGroupByName(Azure azureService, String resourceGroup,
            String name) {
        return Optional.ofNullable(azureService.networkSecurityGroups().getByResourceGroup(resourceGroup, name));
    }

    public Optional<Network> searchVirtualNetworkByName(Azure azureService, String resourceGroup, String name) {
        return Optional.ofNullable(azureService.networks().getByResourceGroup(resourceGroup, name));
    }

    public Optional<PublicIPAddress> searchPublicIpAddressByIp(Azure azureService, String ip) {
        return azureService.publicIPAddresses()
                           .list()
                           .stream()
                           .filter(publicIpAddress -> publicIpAddress.ipAddress().equals(ip))
                           .findAny();
    }

    public Set<VirtualMachine> getAllVirtualMachines(Azure azureService) {
        return new HashSet<>(azureService.virtualMachines().list());
    }

    public Optional<LoadBalancer> searchLoadBalancerByName(Azure azureService, String resourceGroup, String name) {
        return Optional.ofNullable(azureService.loadBalancers().getByResourceGroup(resourceGroup, name));
    }

    public Optional<VirtualMachineScaleSet> searchVirtualMachineScaleSetByName(Azure azureService, String resourceGroup,
            String name) {
        return Optional.ofNullable(azureService.virtualMachineScaleSets().getByResourceGroup(resourceGroup, name));
    }

    public Optional<PublicIPAddress> searchPublicIpAddressByName(Azure azureService, String resourceGroup,
            String name) {
        return Optional.ofNullable(azureService.publicIPAddresses().getByResourceGroup(resourceGroup, name));
    }
}

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
import com.microsoft.azure.management.network.NetworkInterface;
import com.microsoft.azure.management.network.NetworkSecurityGroup;
import com.microsoft.azure.management.network.PublicIpAddress;
import com.microsoft.azure.management.network.SecurityRuleProtocol;
import com.microsoft.azure.management.resources.ResourceGroup;
import com.microsoft.azure.management.resources.fluentcore.arm.Region;
import com.microsoft.azure.management.resources.fluentcore.model.Creatable;


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

    public Set<VirtualMachine> getAllVirtualMachines(Azure azureService) {

        return azureService.virtualMachines().list().stream().collect(Collectors.toSet());
    }

    public Creatable<Network> prepareVirtualNetwork(Azure azureService, Region region, ResourceGroup resourceGroup,
            String name, String cidr) {
        return azureService.networks()
                           .define(name)
                           .withRegion(region)
                           .withExistingResourceGroup(resourceGroup)
                           .withAddressSpace(cidr);
    }

    public Creatable<PublicIpAddress> preparePublicIPAddress(Azure azureService, Region region,
            ResourceGroup resourceGroup, String name, Boolean isStatic) {
        if (isStatic) {
            return azureService.publicIpAddresses()
                               .define(name)
                               .withRegion(region)
                               .withExistingResourceGroup(resourceGroup)
                               .withStaticIp();
        } else {
            return azureService.publicIpAddresses()
                               .define(name)
                               .withRegion(region)
                               .withExistingResourceGroup(resourceGroup)
                               .withDynamicIp();
        }
    }

    public Creatable<NetworkSecurityGroup> prepareOpenNetworkSecurityGroup(Azure azureService, Region region,
            ResourceGroup resourceGroup, String name) {

        return azureService.networkSecurityGroups()
                           .define(name)
                           .withRegion(region)
                           .withExistingResourceGroup(resourceGroup)
                           .defineRule("All")
                           .allowInbound()
                           .fromAnyAddress()
                           .fromAnyPort()
                           .toAnyAddress()
                           .toAnyPort()
                           .withAnyProtocol()
                           .attach()
                           .defineRule("All")
                           .allowOutbound()
                           .fromAnyAddress()
                           .fromAnyPort()
                           .toAnyAddress()
                           .toAnyPort()
                           .withAnyProtocol()
                           .attach();
    }

    public Creatable<NetworkSecurityGroup> prepareSSHNetworkSecurityGroup(Azure azureService, Region region,
            ResourceGroup resourceGroup, String name) {

        return azureService.networkSecurityGroups()
                           .define(name)
                           .withRegion(region)
                           .withExistingResourceGroup(resourceGroup)
                           .defineRule("SSH")
                           .allowInbound()
                           .fromAnyAddress()
                           .fromAnyPort()
                           .toAnyAddress()
                           .toPort(22)
                           .withProtocol(SecurityRuleProtocol.TCP)
                           .attach()
                           .defineRule("All")
                           .allowOutbound()
                           .fromAnyAddress()
                           .fromAnyPort()
                           .toAnyAddress()
                           .toAnyPort()
                           .withAnyProtocol()
                           .attach();
    }

    public Creatable<NetworkSecurityGroup> prepareProactiveNetworkSecurityGroup(Azure azureService, Region region,
            ResourceGroup resourceGroup, String name) {

        return azureService.networkSecurityGroups()
                           .define(name)
                           .withRegion(region)
                           .withExistingResourceGroup(resourceGroup)
                           .defineRule("SSH")
                           .allowInbound()
                           .fromAnyAddress()
                           .fromAnyPort()
                           .toAnyAddress()
                           .toPort(22)
                           .withProtocol(SecurityRuleProtocol.TCP)
                           .attach()
                           .defineRule("ProActive portal")
                           .allowInbound()
                           .fromAnyAddress()
                           .fromAnyPort()
                           .toAnyAddress()
                           .toPort(8080)
                           .withProtocol(SecurityRuleProtocol.TCP)
                           .attach()
                           .defineRule("PNP")
                           .allowInbound()
                           .fromAnyAddress()
                           .fromAnyPort()
                           .toAnyAddress()
                           .toPort(64738)
                           .withAnyProtocol()
                           .attach()
                           .defineRule("PNPS")
                           .allowInbound()
                           .fromAnyAddress()
                           .fromAnyPort()
                           .toAnyAddress()
                           .toPort(64739)
                           .withAnyProtocol()
                           .attach()
                           .defineRule("PAMR")
                           .allowInbound()
                           .fromAnyAddress()
                           .fromAnyPort()
                           .toAnyAddress()
                           .toPort(33647)
                           .withAnyProtocol()
                           .attach()
                           .defineRule("All")
                           .allowOutbound()
                           .fromAnyAddress()
                           .fromAnyPort()
                           .toAnyAddress()
                           .toAnyPort()
                           .withAnyProtocol()
                           .attach();
    }

    public Creatable<NetworkInterface> prepareNetworkInterface(Azure azureService, Region region,
            ResourceGroup resourceGroup, String name, Network virtualNetwork, NetworkSecurityGroup networkSecurityGroup,
            PublicIpAddress publicIpAddress) {
        return azureService.networkInterfaces()
                           .define(name)
                           .withRegion(region)
                           .withExistingResourceGroup(resourceGroup)
                           .withExistingPrimaryNetwork(virtualNetwork)
                           .withSubnet(virtualNetwork.subnets()
                                                     .values()
                                                     .stream()
                                                     .findAny()
                                                     .orElseThrow(() -> new RuntimeException("ERROR no subnet found in virtual network: '" +
                                                                                             virtualNetwork.name() +
                                                                                             "'"))
                                                     .name())
                           .withPrimaryPrivateIpAddressDynamic()
                           .withExistingNetworkSecurityGroup(networkSecurityGroup)
                           .withExistingPrimaryPublicIpAddress(publicIpAddress);
    }

    public Creatable<NetworkInterface> prepareNetworkInterfaceFromScratch(Azure azureService, Region region,
            ResourceGroup resourceGroup, String name, Creatable<Network> creatableVirtualNetwork,
            Creatable<NetworkSecurityGroup> creatableNetworkSecurityGroup,
            Creatable<PublicIpAddress> creatablePublicIpAddress) {
        return azureService.networkInterfaces()
                           .define(name)
                           .withRegion(region)
                           .withExistingResourceGroup(resourceGroup)
                           .withNewPrimaryNetwork(creatableVirtualNetwork)
                           .withPrimaryPrivateIpAddressDynamic()
                           .withNewNetworkSecurityGroup(creatableNetworkSecurityGroup)
                           .withNewPrimaryPublicIpAddress(creatablePublicIpAddress);
    }
}

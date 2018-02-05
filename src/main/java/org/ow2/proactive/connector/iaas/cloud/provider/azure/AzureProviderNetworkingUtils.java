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

import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.stream.Collectors;

import org.springframework.stereotype.Component;

import com.microsoft.azure.management.Azure;
import com.microsoft.azure.management.compute.VirtualMachine;
import com.microsoft.azure.management.network.*;
import com.microsoft.azure.management.resources.ResourceGroup;
import com.microsoft.azure.management.resources.fluentcore.arm.Region;
import com.microsoft.azure.management.resources.fluentcore.model.Creatable;


/**
 * @author ActiveEon Team
 * @since 19/04/17
 */
@Component
public class AzureProviderNetworkingUtils {

    public Creatable<Network> prepareVirtualNetwork(Azure azureService, Region region, ResourceGroup resourceGroup,
            String name, String cidr) {
        return azureService.networks()
                           .define(name)
                           .withRegion(region)
                           .withExistingResourceGroup(resourceGroup)
                           .withAddressSpace(cidr);
    }

    public Creatable<PublicIPAddress> preparePublicIPAddress(Azure azureService, Region region,
            ResourceGroup resourceGroup, String name, Boolean isStatic) {
        if (isStatic) {
            return azureService.publicIPAddresses()
                               .define(name)
                               .withRegion(region)
                               .withExistingResourceGroup(resourceGroup)
                               .withStaticIP();
        } else {
            return azureService.publicIPAddresses()
                               .define(name)
                               .withRegion(region)
                               .withExistingResourceGroup(resourceGroup)
                               .withDynamicIP();
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
                           .withPriority(101)
                           .attach()
                           .defineRule("ProActive_portal")
                           .allowInbound()
                           .fromAnyAddress()
                           .fromAnyPort()
                           .toAnyAddress()
                           .toPort(8080)
                           .withProtocol(SecurityRuleProtocol.TCP)
                           .withPriority(102)
                           .attach()
                           .defineRule("PNP")
                           .allowInbound()
                           .fromAnyAddress()
                           .fromAnyPort()
                           .toAnyAddress()
                           .toPort(64738)
                           .withAnyProtocol()
                           .withPriority(103)
                           .attach()
                           .defineRule("PNPS")
                           .allowInbound()
                           .fromAnyAddress()
                           .fromAnyPort()
                           .toAnyAddress()
                           .toPort(64739)
                           .withAnyProtocol()
                           .withPriority(104)
                           .attach()
                           .defineRule("PAMR")
                           .allowInbound()
                           .fromAnyAddress()
                           .fromAnyPort()
                           .toAnyAddress()
                           .toPort(33647)
                           .withAnyProtocol()
                           .withPriority(105)
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
            PublicIPAddress publicIpAddress) {
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
                           .withPrimaryPrivateIPAddressDynamic()
                           .withExistingNetworkSecurityGroup(networkSecurityGroup)
                           .withExistingPrimaryPublicIPAddress(publicIpAddress);
    }

    public Creatable<NetworkInterface> prepareNetworkInterface(Azure azureService, Region region,
            ResourceGroup resourceGroup, String name, Creatable<Network> creatableVirtualNetwork,
            Network optionalVirtualNetwork, Creatable<NetworkSecurityGroup> creatableNetworkSecurityGroup,
            NetworkSecurityGroup optionalNetworkSecurityGroup, Creatable<PublicIPAddress> creatablePublicIpAddress,
            PublicIPAddress optionalPublicIpAddress) {

        // Initialize configuration
        NetworkInterface.DefinitionStages.WithPrimaryNetwork networkInterfaceCreationStage1 = azureService.networkInterfaces()
                                                                                                          .define(name)
                                                                                                          .withRegion(region)
                                                                                                          .withExistingResourceGroup(resourceGroup);

        // Configure virtual network
        NetworkInterface.DefinitionStages.WithCreate networkInterfaceCreationStage2 = Optional.ofNullable(optionalVirtualNetwork)
                                                                                              .map(networkInterfaceCreationStage1::withExistingPrimaryNetwork)
                                                                                              .map(existingNetwork -> existingNetwork.withSubnet(optionalVirtualNetwork.subnets()
                                                                                                                                                                       .keySet()
                                                                                                                                                                       .stream()
                                                                                                                                                                       .findFirst()
                                                                                                                                                                       .get()))
                                                                                              .orElseGet(() -> networkInterfaceCreationStage1.withNewPrimaryNetwork(creatableVirtualNetwork))
                                                                                              .withPrimaryPrivateIPAddressDynamic();

        // Configure network security group
        NetworkInterface.DefinitionStages.WithCreate networkInterfaceCreationStage3 = Optional.ofNullable(optionalNetworkSecurityGroup)
                                                                                              .map(networkInterfaceCreationStage2::withExistingNetworkSecurityGroup)
                                                                                              .orElseGet(() -> networkInterfaceCreationStage2.withNewNetworkSecurityGroup(creatableNetworkSecurityGroup));

        // Configure public IP address
        NetworkInterface.DefinitionStages.WithCreate networkInterfaceCreationStage4 = Optional.ofNullable(optionalPublicIpAddress)
                                                                                              .map(networkInterfaceCreationStage3::withExistingPrimaryPublicIPAddress)
                                                                                              .orElseGet(() -> networkInterfaceCreationStage3.withNewPrimaryPublicIPAddress(creatablePublicIpAddress));

        return networkInterfaceCreationStage4;
    }

    public List<NetworkInterface> getVMNetworkInterfaces(Azure azureService, VirtualMachine vm) {
        return vm.networkInterfaceIds()
                 .stream()
                 .map(id -> azureService.networkInterfaces().getById(id))
                 .collect(Collectors.toList());
    }

    public List<Network> getVMNetworks(Azure azureService, VirtualMachine vm) {
        return getVMNetworkInterfaces(azureService, vm).stream()
                                                       .flatMap(networkInterface -> networkInterface.ipConfigurations()
                                                                                                    .values()
                                                                                                    .stream())
                                                       .map(NicIPConfigurationBase::getNetwork)
                                                       .filter(Objects::nonNull)
                                                       .distinct()
                                                       .collect(Collectors.toList());
    }

    public List<NetworkSecurityGroup> getVMSecurityGroups(Azure azureService, VirtualMachine vm) {
        return getVMNetworkInterfaces(azureService, vm).stream()
                                                       .map(NetworkInterfaceBase::getNetworkSecurityGroup)
                                                       .filter(Objects::nonNull)
                                                       .distinct()
                                                       .collect(Collectors.toList());
    }

    public List<PublicIPAddress> getVMPublicIPAddresses(Azure azureService, VirtualMachine vm) {
        return getVMNetworkInterfaces(azureService, vm).stream()
                                                       .map(NetworkInterface::primaryIPConfiguration)
                                                       .filter(Objects::nonNull)
                                                       .map(NicIPConfiguration::getPublicIPAddress)
                                                       .filter(Objects::nonNull)
                                                       .collect(Collectors.toList());
    }
}

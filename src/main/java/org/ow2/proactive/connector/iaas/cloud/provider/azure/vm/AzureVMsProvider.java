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
package org.ow2.proactive.connector.iaas.cloud.provider.azure.vm;

import java.util.*;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

import org.apache.log4j.Logger;
import org.ow2.proactive.connector.iaas.cloud.TagManager;
import org.ow2.proactive.connector.iaas.cloud.provider.CloudProvider;
import org.ow2.proactive.connector.iaas.cloud.provider.azure.AzureProvider;
import org.ow2.proactive.connector.iaas.cloud.provider.azure.AzureProviderNetworkingUtils;
import org.ow2.proactive.connector.iaas.cloud.provider.azure.AzureProviderUtils;
import org.ow2.proactive.connector.iaas.cloud.provider.azure.AzureServiceCache;
import org.ow2.proactive.connector.iaas.model.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import com.google.common.collect.Lists;
import com.microsoft.azure.management.Azure;
import com.microsoft.azure.management.compute.*;
import com.microsoft.azure.management.network.*;
import com.microsoft.azure.management.network.Network;
import com.microsoft.azure.management.network.model.HasPrivateIpAddress;
import com.microsoft.azure.management.network.model.HasPublicIpAddress;
import com.microsoft.azure.management.resources.ResourceGroup;
import com.microsoft.azure.management.resources.fluentcore.arm.Region;
import com.microsoft.azure.management.resources.fluentcore.model.Creatable;
import com.microsoft.azure.management.resources.fluentcore.utils.SdkContext;

import lombok.Getter;


/**
 * Provides Microsoft Azure clouds' management using the official java SDK.
 *
 * This class has been tested by ActiveEon to be thread safe (using Azure SDK release version 1.0.0-beta5).
 * However this need to be carefully double-checked after every SDK upgrades, as mentioned by Microsoft:
 * ------------------------------------------------------------------------------------------------------------
 * We do not make any thread-safety guarantees about our libraries. We also do not test them for thread-safety.
 * Methods that are currently thread-safe may be thread-unsafe in future versions.
 * ------------------------------------------------------------------------------------------------------------
 *
 * @author ActiveEon Team
 * @since 01/03/17
 */
@Component
public class AzureVMsProvider extends AzureProvider {

    @Getter
    protected final String type = "azure";

    private final Logger logger = Logger.getLogger(AzureVMsProvider.class);

    @Override
    public Set<Instance> createInstance(Infrastructure infrastructure, Instance instance) {

        Azure azureService = azureServiceCache.getService(infrastructure);
        String instanceTag = Optional.ofNullable(instance.getTag())
                                     .orElseThrow(() -> new RuntimeException("ERROR missing instance tag/name from instance: '" +
                                                                             instance + "'"));

        // Check for Image by name first and then by id
        String imageNameOrId = Optional.ofNullable(instance.getImage())
                                       .orElseThrow(() -> new RuntimeException("ERROR missing Image name/id from instance: '" +
                                                                               instance + "'"));

        // TODO: no custom image allowed currently
        VirtualMachineCustomImage image = getImageByName(azureService,
                                                         imageNameOrId).orElseGet(() -> getImageById(azureService,
                                                                                                     imageNameOrId).orElseThrow(() -> new RuntimeException("ERROR unable to find custom Image: '" +
                                                                                                                                                           instance.getImage() +
                                                                                                                                                           "'")));

        // Retrieve tags
        List<Tag> tags = tagManager.retrieveAllTags(instance.getOptions());

        // Get the options (Optional by design)
        Optional<Options> options = Optional.ofNullable(instance.getOptions());

        // Try to retrieve the resourceGroup from provided name, otherwise get it from image
        ResourceGroup resourceGroup = azureProviderUtils.searchResourceGroupByName(azureService,
                                                                                   options.map(Options::getResourceGroup)
                                                                                          .orElseGet(image::resourceGroupName))
                                                        .orElseThrow(() -> new RuntimeException("ERROR unable to find a suitable resourceGroup from instance: '" +
                                                                                                instance + "'"));

        // Try to get region from provided name, otherwise get it from image
        Region region = options.map(presentOptions -> Region.findByLabelOrName(presentOptions.getRegion()))
                               .orElseGet(image::region);

        // Prepare a new virtual private network (same for all VMs)
        Optional<String> optionalPrivateNetworkCIDR = options.map(Options::getPrivateNetworkCIDR);
        Creatable<Network> creatableVirtualNetwork = azureProviderNetworkingUtils.prepareVirtualNetwork(azureService,
                                                                                                        region,
                                                                                                        resourceGroup,
                                                                                                        createUniqueVirtualNetworkName(instanceTag),
                                                                                                        optionalPrivateNetworkCIDR.orElse(defaultPrivateNetworkCidr));

        // Get existing virtual private network if specified
        Optional<Network> optionalVirtualNetwork = options.map(Options::getSubnetId)
                                                          .map(subnetId -> azureProviderUtils.searchVirtualNetworkByName(azureService,
                                                                                                                         subnetId)
                                                                                             .get());

        // Prepare a new  security group (same for all VMs)
        Creatable<NetworkSecurityGroup> creatableNetworkSecurityGroup = azureProviderNetworkingUtils.prepareProactiveNetworkSecurityGroup(azureService,
                                                                                                                                          region,
                                                                                                                                          resourceGroup,
                                                                                                                                          createUniqueSecurityGroupName(instance.getTag()));

        // Get existing security group if specified
        Optional<NetworkSecurityGroup> optionalNetworkSecurityGroup = options.map(Options::getSecurityGroupNames)
                                                                             .map(secGrpNames -> secGrpNames.get(0))
                                                                             .map(secGrpName -> azureProviderUtils.searchNetworkSecurityGroupByName(azureService,
                                                                                                                                                    secGrpName)
                                                                                                                  .get());

        // Get existing public IP address if specified
        Optional<PublicIpAddress> optionalPublicIpAddress = options.map(Options::getPublicIpAddress)
                                                                   .map(publicIpAddress -> azureProviderUtils.searchPublicIpAddressByIp(azureService,
                                                                                                                                        publicIpAddress)
                                                                                                             .get());

        // Prepare the VM(s)
        Optional<Boolean> optionalStaticPublicIP = options.map(Options::getStaticPublicIP);
        List<Creatable<VirtualMachine>> creatableVirtualMachines = IntStream.rangeClosed(1,
                                                                                         Integer.valueOf(Optional.ofNullable(instance.getNumber())
                                                                                                                 .orElse(SINGLE_INSTANCE_NUMBER)))
                                                                            .mapToObj(instanceNumber -> {
                                                                                // Create a new public IP address (one per VM)
                                                                                String publicIPAddressName = createUniquePublicIPName(createUniqueInstanceTag(instanceTag,
                                                                                                                                                              instanceNumber));
                                                                                Creatable<PublicIpAddress> creatablePublicIpAddress = azureProviderNetworkingUtils.preparePublicIPAddress(azureService,
                                                                                                                                                                                          region,
                                                                                                                                                                                          resourceGroup,
                                                                                                                                                                                          publicIPAddressName,
                                                                                                                                                                                          optionalStaticPublicIP.orElse(DEFAULT_STATIC_PUBLIC_IP));

                                                                                // Prepare a new network interface (one per VM)
                                                                                String networkInterfaceName = createUniqueNetworkInterfaceName(createUniqueInstanceTag(instanceTag,
                                                                                                                                                                       instanceNumber));
                                                                                Creatable<NetworkInterface> creatableNetworkInterface = azureProviderNetworkingUtils.prepareNetworkInterface(azureService,
                                                                                                                                                                                             region,
                                                                                                                                                                                             resourceGroup,
                                                                                                                                                                                             networkInterfaceName,
                                                                                                                                                                                             creatableVirtualNetwork,
                                                                                                                                                                                             optionalVirtualNetwork.orElse(null),
                                                                                                                                                                                             creatableNetworkSecurityGroup,
                                                                                                                                                                                             optionalNetworkSecurityGroup.orElse(null),
                                                                                                                                                                                             creatablePublicIpAddress,
                                                                                                                                                                                             instanceNumber == 1 ? optionalPublicIpAddress.orElse(null)
                                                                                                                                                                                                                 : null);

                                                                                return prepareVirtualMachine(instance,
                                                                                                             azureService,
                                                                                                             resourceGroup,
                                                                                                             region,
                                                                                                             createUniqueInstanceTag(instanceTag,
                                                                                                                                     instanceNumber),
                                                                                                             image,
                                                                                                             creatableNetworkInterface);
                                                                            })
                                                                            .collect(Collectors.toList());

        // Create all VMs in parallel and collect IDs
        return azureService.virtualMachines()
                           .create(creatableVirtualMachines)
                           .values()
                           .stream()
                           .map(vm -> instance.withTag(vm.name()).withId(vm.vmId()).withNumber(SINGLE_INSTANCE_NUMBER))
                           .collect(Collectors.toSet());
    }

    private Creatable<VirtualMachine> prepareVitualMachine(Instance instance, Azure azureService,
            ResourceGroup resourceGroup, Region region, String instanceTag, VirtualMachineCustomImage image,
            Creatable<NetworkInterface> creatableNetworkInterface) {

        // Configure the VM depending on the OS type
        VirtualMachine.DefinitionStages.WithFromImageCreateOptionsManaged creatableVirtualMachineWithImage;
        OperatingSystemTypes operatingSystemType = image.osDiskImage().osType();
        if (operatingSystemType.equals(OperatingSystemTypes.LINUX)) {
            creatableVirtualMachineWithImage = configureLinuxVirtualMachine(azureService,
                                                                            instanceTag,
                                                                            region,
                                                                            resourceGroup,
                                                                            instance.getCredentials(),
                                                                            image,
                                                                            creatableNetworkInterface);
        } else if (operatingSystemType.equals(OperatingSystemTypes.WINDOWS)) {
            creatableVirtualMachineWithImage = configureWindowsVirtualMachine(azureService,
                                                                              instanceTag,
                                                                              region,
                                                                              resourceGroup,
                                                                              instance.getCredentials(),
                                                                              image,
                                                                              creatableNetworkInterface);
        } else {
            throw new RuntimeException("ERROR Operating System of type '" + operatingSystemType.toString() +
                                       "' is not yet supported");
        }

        // Set VM size (or type) and name of OS' disk
        Optional<String> optionalHardwareType = Optional.ofNullable(instance.getHardware()).map(Hardware::getType);
        VirtualMachine.DefinitionStages.WithCreate creatableVMWithSize = creatableVirtualMachineWithImage.withSize(new VirtualMachineSizeTypes(optionalHardwareType.orElse(DEFAULT_VM_SIZE.toString())))
                                                                                                         .withOsDiskName(createUniqOSDiskName(instanceTag));

        // Add init script(s) using dedicated Microsoft extension
        Optional.ofNullable(instance.getInitScript()).map(InstanceScript::getScripts).ifPresent(scripts -> {
            if (scripts.length > 0) {
                StringBuilder concatenatedScripts = new StringBuilder();
                Lists.newArrayList(scripts)
                     .forEach(script -> concatenatedScripts.append(script).append(SCRIPT_SEPARATOR));
                creatableVMWithSize.defineNewExtension(createUniqueScriptName(instanceTag))
                                   .withPublisher(SCRIPT_EXTENSION_PUBLISHER)
                                   .withType(SCRIPT_EXTENSION_TYPE)
                                   .withVersion(SCRIPT_EXTENSION_VERSION)
                                   .withMinorVersionAutoUpgrade()
                                   .withPublicSetting(SCRIPT_EXTENSION_CMD_KEY, concatenatedScripts.toString())
                                   .attach();
            }
        });

        // Set tags
        return creatableVMWithSize.withTags(tagManager.retrieveAllTags(instance.getOptions())
                                                      .stream()
                                                      .collect(Collectors.toMap(Tag::getKey, Tag::getValue)));
    }

    @Override
    public void deleteInstance(Infrastructure infrastructure, String instanceId) {
        Azure azureService = azureServiceCache.getService(infrastructure);

        VirtualMachine vm = azureProviderUtils.searchVirtualMachineByID(azureService, instanceId)
                                              .orElseThrow(() -> new RuntimeException(INSTANCE_NOT_FOUND_ERROR + "'" +
                                                                                      instanceId + "'"));

        logger.info("Deletion of all Azure resources of instance " + instanceId +
                    " is being requested to the provider (infrastructure: " + infrastructure.getId() + ")");

        // Retrieve all resources attached to the instance
        List<com.microsoft.azure.management.network.Network> networks = azureProviderNetworkingUtils.getVMNetworks(azureService,
                                                                                                                   vm);
        List<NetworkSecurityGroup> networkSecurityGroups = azureProviderNetworkingUtils.getVMSecurityGroups(azureService,
                                                                                                            vm);
        List<PublicIpAddress> publicIPAddresses = azureProviderNetworkingUtils.getVMPublicIPAddresses(azureService, vm);
        String osDiskID = vm.osDiskId();

        // Delete the VM first
        azureService.virtualMachines().deleteById(vm.id());

        // Then delete all network interfaces attached
        vm.networkInterfaceIds().forEach(id -> azureService.networkInterfaces().deleteById(id));

        // Delete all public IP addresses
        publicIPAddresses.stream()
                         .map(PublicIpAddress::id)
                         .forEach(id -> azureService.publicIpAddresses().deleteById(id));

        // Delete its main disk (OS), *and keep data disks*
        azureService.disks().deleteById(osDiskID);

        // Delete the security groups if not attached to any remaining network interface
        deleteSecurityGroups(azureService, networkSecurityGroups);

        // Delete the virtual networks if not attached to any remaining network interface
        deleteNetworks(azureService, networks);

        logger.info("Deletion of all Azure resources of instance " + instanceId + " has been executed.");
    }
}

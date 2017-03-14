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

import java.util.Collection;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

import org.apache.log4j.Logger;
import org.ow2.proactive.connector.iaas.cloud.provider.CloudProvider;
import org.ow2.proactive.connector.iaas.model.Hardware;
import org.ow2.proactive.connector.iaas.model.Image;
import org.ow2.proactive.connector.iaas.model.Infrastructure;
import org.ow2.proactive.connector.iaas.model.Instance;
import org.ow2.proactive.connector.iaas.model.InstanceCredentials;
import org.ow2.proactive.connector.iaas.model.InstanceScript;
import org.ow2.proactive.connector.iaas.model.Options;
import org.ow2.proactive.connector.iaas.model.ScriptResult;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import com.google.common.collect.Lists;
import com.microsoft.azure.management.Azure;
import com.microsoft.azure.management.compute.OperatingSystemTypes;
import com.microsoft.azure.management.compute.VirtualMachine;
import com.microsoft.azure.management.compute.VirtualMachineCustomImage;
import com.microsoft.azure.management.compute.VirtualMachineDataDisk;
import com.microsoft.azure.management.compute.VirtualMachineExtension;
import com.microsoft.azure.management.compute.VirtualMachineSizeTypes;
import com.microsoft.azure.management.network.Network;
import com.microsoft.azure.management.network.NetworkInterface;
import com.microsoft.azure.management.network.NetworkSecurityGroup;
import com.microsoft.azure.management.network.PublicIpAddress;
import com.microsoft.azure.management.resources.ResourceGroup;
import com.microsoft.azure.management.resources.fluentcore.arm.Region;
import com.microsoft.azure.management.resources.fluentcore.model.Creatable;
import com.microsoft.azure.management.resources.fluentcore.utils.SdkContext;

import lombok.Getter;


/**
 * @author ActiveEon Team
 * @since 01/03/17
 */
@Component
public class AzureProvider implements CloudProvider {

    private final Logger logger = Logger.getLogger(AzureProvider.class);

    @Getter
    private final String type = "azure";

    private static final Region defaultRegion = Region.US_EAST;

    private static final VirtualMachineSizeTypes defaultVMSize = VirtualMachineSizeTypes.STANDARD_D1_V2;

    private static final int resourcesNameExtraChars = 5;

    private static final String virtualNetworkNameBase = "vnet";

    private static final String publicIPAddressNameBase = "ip";

    private static final String networkSecurityGroupNameBase = "sg";

    private static final String networkInterfaceNameBase = "if";

    private static final String osDiskNameBase = "os";

    private static final String defaultUsername = "activeeon";

    private static final String defaultPassword = "Act1v€0N";

    private static final String defaultPrivateNetworkCIDR = "10.0.0.0/24";

    private static final Boolean defaultStaticPublicIP = true;

    private static final String scriptExtensionPublisher = "Microsoft.Azure.Extensions";

    private static final String scriptExtensionType = "CustomScript";

    private static final String scriptExtensionVersion = "2.0";

    private static final String scriptExtensionCmdKey = "commandToExecute";

    @Autowired
    private AzureServiceCache azureServiceCache;

    @Autowired
    private AzureProviderUtils azureProviderUtils;

    @Override
    public Set<Instance> createInstance(Infrastructure infrastructure, Instance instance) {

        Azure azureService = azureServiceCache.getService(infrastructure);
        String instanceTag = Optional.ofNullable(instance.getTag())
                                     .orElseThrow(() -> new RuntimeException("ERROR missing instance tag/name from instance: '" +
                                                                             instance + "'"));
        ResourceGroup resourceGroup = azureService.resourceGroups()
                                                  .getByName(Optional.ofNullable(infrastructure.getResourceGroup())
                                                                     .orElseThrow(() -> new RuntimeException("ERROR missing resourceGroup from infrastructure: '" +
                                                                                                             infrastructure +
                                                                                                             "'")));

        // Chek for Image by name first and then by key
        String imageNameOrKey = Optional.ofNullable(instance.getImage())
                                        .orElseThrow(() -> new RuntimeException("ERROR missing Image name (or key) from instance: '" +
                                                                                instance + "'"));
        VirtualMachineCustomImage image = getImageByNameOrKey(azureService,
                                                              imageNameOrKey).orElseThrow(() -> new RuntimeException("ERROR unable to find custom Image: '" +
                                                                                                                     instance.getImage() +
                                                                                                                     "'"));

        // Get options (Optional by design)
        Optional<Options> options = Optional.ofNullable(instance.getOptions());

        // Get region
        Region region = Optional.ofNullable(Region.findByLabelOrName(instance.getRegion())).orElse(defaultRegion);

        // Prepare a new virtual private network (same for all VMs)
        Optional<String> optionalPrivateNetworkCIDR = options.isPresent() ? Optional.ofNullable(options.get()
                                                                                                       .getPrivateNetworkCIDR())
                                                                          : Optional.empty();
        Creatable<Network> creatableVirtualNetwork = azureProviderUtils.prepareVirtualNetwork(azureService,
                                                                                              region,
                                                                                              resourceGroup,
                                                                                              createUniqVirtualNetworkName(instanceTag),
                                                                                              optionalPrivateNetworkCIDR.orElse(defaultPrivateNetworkCIDR));

        // Prepare a new  security group (same for all VMs)
        Creatable<NetworkSecurityGroup> creatableNetworkSecurityGroup = azureProviderUtils.prepareSSHNetworkSecurityGroup(azureService,
                                                                                                                          region,
                                                                                                                          resourceGroup,
                                                                                                                          createUniqSecurityGroupName(instance.getTag()));

        // Prepare the VM(s)
        Optional<Boolean> optionalStaticPublicIP = options.isPresent() ? Optional.ofNullable(instance.getOptions()
                                                                                                     .getStaticPublicIP())
                                                                       : Optional.empty();
        List<Creatable<VirtualMachine>> creatableVirtualMachines = IntStream.rangeClosed(1,
                                                                                         Integer.valueOf(Optional.ofNullable(instance.getNumber())
                                                                                                                 .orElse("1")))
                                                                            .mapToObj(instanceNumber -> {
                                                                                // Create a new public IP address (one per VM)
                                                                                String publicIPAddressName = createUniqPublicIPName(createUniqInstanceTag(instanceTag,
                                                                                                                                                          instanceNumber));
                                                                                Creatable<PublicIpAddress> creatablePublicIPAddress = azureProviderUtils.preparePublicIPAddress(azureService,
                                                                                                                                                                                region,
                                                                                                                                                                                resourceGroup,
                                                                                                                                                                                publicIPAddressName,
                                                                                                                                                                                optionalStaticPublicIP.orElse(defaultStaticPublicIP));

                                                                                // Prepare a new network interface (one per VM)
                                                                                String networkInterfaceName = createUniqNetworkInterfaceName(createUniqInstanceTag(instanceTag,
                                                                                                                                                                   instanceNumber));
                                                                                Creatable<NetworkInterface> creatableNetworkInterface = azureProviderUtils.prepareNetworkInterfaceFromScratch(azureService,
                                                                                                                                                                                              region,
                                                                                                                                                                                              resourceGroup,
                                                                                                                                                                                              networkInterfaceName,
                                                                                                                                                                                              creatableVirtualNetwork,
                                                                                                                                                                                              creatableNetworkSecurityGroup,
                                                                                                                                                                                              creatablePublicIPAddress);

                                                                                return prepareVirtualMachine(instance,
                                                                                                             azureService,
                                                                                                             resourceGroup,
                                                                                                             region,
                                                                                                             createUniqInstanceTag(instanceTag,
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
                           .map(vm -> instance.withTag(vm.name()).withId(vm.vmId()).withNumber("1"))
                           .collect(Collectors.toSet());
    }

    private Optional<VirtualMachineCustomImage> getImageByNameOrKey(Azure azureService, String nameOrKey) {
        return Optional.ofNullable(azureService.virtualMachineCustomImages()
                                               .list()
                                               .stream()
                                               .filter(customImage -> customImage.name().equals(nameOrKey))
                                               .findAny()
                                               .orElseGet(() -> azureService.virtualMachineCustomImages()
                                                                            .list()
                                                                            .stream()
                                                                            .filter(customImage -> customImage.key()
                                                                                                              .equals(nameOrKey))
                                                                            .findAny()
                                                                            .get()));
    }

    private Creatable<VirtualMachine> prepareVirtualMachine(Instance instance, Azure azureService,
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
        Optional<String> optionalHardwareType = Optional.ofNullable(instance.getHardware())
                                                        .isPresent() ? Optional.ofNullable(instance.getHardware()
                                                                                                   .getType())
                                                                     : Optional.empty();
        VirtualMachine.DefinitionStages.WithCreate creatableVMWithSize = creatableVirtualMachineWithImage.withSize(new VirtualMachineSizeTypes(optionalHardwareType.orElse(defaultVMSize.toString())))
                                                                                                         .withOsDiskName(createUniqOSDiskName(instanceTag));

        // Add init script(s) using dedicated Microsoft extension
        Optional.ofNullable(instance.getInitScript()).ifPresent(initScript -> {
            Optional.ofNullable(initScript.getScripts()).ifPresent(scripts -> {
                if (scripts.length > 0) {
                    StringBuilder concatenatedScripts = new StringBuilder();
                    Lists.newArrayList(scripts).forEach(script -> concatenatedScripts.append(script).append(';'));
                    creatableVMWithSize.defineNewExtension(createUniqScriptName(instanceTag))
                                       .withPublisher(scriptExtensionPublisher)
                                       .withType(scriptExtensionType)
                                       .withVersion(scriptExtensionVersion)
                                       .withMinorVersionAutoUpgrade()
                                       .withPublicSetting(scriptExtensionCmdKey, concatenatedScripts.toString())
                                       .attach();
                }
            });
        });
        return creatableVMWithSize;
    }

    private VirtualMachine.DefinitionStages.WithLinuxCreateManaged configureLinuxVirtualMachine(Azure azureService,
            String instanceTag, Region region, ResourceGroup resourceGroup, InstanceCredentials instanceCredentials,
            VirtualMachineCustomImage image, Creatable<NetworkInterface> creatableNetworkInterface) {
        // Retrieve optional credentials
        Optional<InstanceCredentials> optionalInstanceCredentials = Optional.ofNullable(instanceCredentials);
        Optional<String> optionalUsername = optionalInstanceCredentials.isPresent() ? Optional.ofNullable(optionalInstanceCredentials.get()
                                                                                                                                     .getUsername())
                                                                                    : Optional.empty();
        Optional<String> optionalPassword = optionalInstanceCredentials.isPresent() ? Optional.ofNullable(optionalInstanceCredentials.get()
                                                                                                                                     .getPassword())
                                                                                    : Optional.empty();
        Optional<String> optionalPublicKey = optionalInstanceCredentials.isPresent() ? Optional.ofNullable(optionalInstanceCredentials.get()
                                                                                                                                      .getPublicKey())
                                                                                     : Optional.empty();

        // Prepare the VM without credentials
        VirtualMachine.DefinitionStages.WithLinuxRootPasswordOrPublicKeyManaged creatableVMWithoutCredentials = azureService.virtualMachines()
                                                                                                                            .define(instanceTag)
                                                                                                                            .withRegion(region)
                                                                                                                            .withExistingResourceGroup(resourceGroup)
                                                                                                                            .withNewPrimaryNetworkInterface(creatableNetworkInterface)
                                                                                                                            .withLinuxCustomImage(image.id())
                                                                                                                            .withRootUsername(optionalUsername.orElse(defaultUsername));

        // Set the credentials (whether password or SSH key)
        return optionalPublicKey.map(creatableVMWithoutCredentials::withSsh)
                                .orElseGet(() -> creatableVMWithoutCredentials.withRootPassword(optionalPassword.orElse(defaultPassword)));
    }

    private VirtualMachine.DefinitionStages.WithWindowsCreateManaged configureWindowsVirtualMachine(Azure azureService,
            String instanceTag, Region region, ResourceGroup resourceGroup, InstanceCredentials instanceCredentials,
            VirtualMachineCustomImage image, Creatable<NetworkInterface> creatableNetworkInterface) {
        // Retrieve optional credentials
        Optional<InstanceCredentials> optionalInstanceCredentials = Optional.ofNullable(instanceCredentials);
        Optional<String> optionalUsername = optionalInstanceCredentials.isPresent() ? Optional.ofNullable(optionalInstanceCredentials.get()
                                                                                                                                     .getUsername())
                                                                                    : Optional.empty();
        Optional<String> optionalPassword = optionalInstanceCredentials.isPresent() ? Optional.ofNullable(optionalInstanceCredentials.get()
                                                                                                                                     .getPassword())
                                                                                    : Optional.empty();

        // Prepare the VM with credentials
        return azureService.virtualMachines()
                           .define(instanceTag)
                           .withRegion(region)
                           .withExistingResourceGroup(resourceGroup)
                           .withNewPrimaryNetworkInterface(creatableNetworkInterface)
                           .withWindowsCustomImage(image.id())
                           .withAdminUsername(optionalUsername.orElse(defaultUsername))
                           .withAdminPassword(optionalPassword.orElse(defaultPassword));
    }

    /**
     * Create a uniq tag for a VM based on the original tag provided and the instance index
     *
     * @param tagBase       the tag base
     * @param instanceIndex the instance index
     * @return a uniq VM tag
     */
    private static String createUniqInstanceTag(String tagBase, int instanceIndex) {
        if (instanceIndex > 1) {
            return tagBase + String.valueOf(instanceIndex);
        }
        return tagBase;
    }

    private static String createUniqSecurityGroupName(String instanceTag) {
        return SdkContext.randomResourceName(instanceTag + '-' + networkSecurityGroupNameBase,
                                             instanceTag.length() + networkSecurityGroupNameBase.length() + 1 +
                                                                                               resourcesNameExtraChars);
    }

    private static String createUniqVirtualNetworkName(String instanceTag) {
        return SdkContext.randomResourceName(instanceTag + '-' + virtualNetworkNameBase,
                                             instanceTag.length() + virtualNetworkNameBase.length() + 1 +
                                                                                         resourcesNameExtraChars);
    }

    private static String createUniqNetworkInterfaceName(String instanceTag) {
        return SdkContext.randomResourceName(instanceTag + '-' + networkInterfaceNameBase,
                                             instanceTag.length() + networkInterfaceNameBase.length() + 1 +
                                                                                           resourcesNameExtraChars);
    }

    private static String createUniqPublicIPName(String instanceTag) {
        return SdkContext.randomResourceName(instanceTag + '-' + publicIPAddressNameBase,
                                             instanceTag.length() + publicIPAddressNameBase.length() + 1 +
                                                                                          resourcesNameExtraChars);
    }

    private static String createUniqOSDiskName(String instanceTag) {
        return SdkContext.randomResourceName(instanceTag + '-' + osDiskNameBase,
                                             instanceTag.length() + osDiskNameBase.length() + 1 +
                                                                                 resourcesNameExtraChars);
    }

    private static String createUniqScriptName(String instanceTag) {
        return SdkContext.randomResourceName(instanceTag, instanceTag.length() + resourcesNameExtraChars);
    }

    @Override
    public void deleteInstance(Infrastructure infrastructure, String instanceId) {
        Azure azureService = azureServiceCache.getService(infrastructure);

        VirtualMachine vm = azureProviderUtils.searchVirtualMachineByID(azureService, instanceId)
                                              .orElseThrow(() -> new RuntimeException("ERROR unable to find instance with ID: '" +
                                                                                      instanceId + "'"));

        // Retrieve all resources attached to the instance
        NetworkInterface networkInterface = vm.getPrimaryNetworkInterface();
        com.microsoft.azure.management.network.Network network = networkInterface.primaryIpConfiguration().getNetwork();
        NetworkSecurityGroup networkSecurityGroup = networkInterface.getNetworkSecurityGroup();
        Optional<PublicIpAddress> optionalPublicIPAddress = Optional.ofNullable(vm.getPrimaryPublicIpAddress());
        Collection<VirtualMachineDataDisk> dataDisks = vm.dataDisks().values();
        String osDiskID = vm.osDiskId();

        // Delete the VM first
        azureService.virtualMachines().deleteById(vm.id());

        // Then delete its network interface
        azureService.networkInterfaces().deleteById(networkInterface.id());

        // Delete its public IP address if present
        optionalPublicIPAddress.ifPresent(pubIPAddr -> azureService.publicIpAddresses().deleteById(pubIPAddr.id()));

        // Delete its main disk (OS), and keep data disks
        azureService.disks().deleteById(osDiskID);
        //dataDisks.forEach(disk -> azureService.disks().deleteById(disk.id()));

        // Delete the security group if present and not attached to any network interface
        if (azureService.networkInterfaces()
                        .list()
                        .stream()
                        .noneMatch(netIf -> netIf.getNetworkSecurityGroup().id().equals(networkSecurityGroup.id()))) {
            azureService.networkSecurityGroups().deleteById(networkSecurityGroup.id());
        }

        // Delete the virtual network if not attached to any network interface
        if (azureService.networkInterfaces()
                        .list()
                        .stream()
                        .noneMatch(netIf -> netIf.primaryIpConfiguration().getNetwork().id().equals(network.id()))) {
            azureService.networks().deleteById(network.id());
        }
    }

    @Override
    public Set<Instance> getAllInfrastructureInstances(Infrastructure infrastructure) {
        Azure azureService = azureServiceCache.getService(infrastructure);
        return azureProviderUtils.getAllVirtualMachines(azureService)
                                 .stream()
                                 .map(vm -> Instance.builder()
                                                    .id(vm.vmId())
                                                    .tag(vm.name())
                                                    .number("1")
                                                    .hardware(Hardware.builder().type(vm.size().toString()).build())
                                                    .network(org.ow2.proactive.connector.iaas.model.Network.builder()
                                                                                                           .publicAddresses(vm.networkInterfaceIds()
                                                                                                                              .stream()
                                                                                                                              .map(networkInterfaceId -> azureService.networkInterfaces()
                                                                                                                                                                     .getById(networkInterfaceId))
                                                                                                                              //.flatMap(networkInterface -> networkInterface.ipConfigurations().values().stream())
                                                                                                                              .map(NetworkInterface::primaryIpConfiguration)
                                                                                                                              .filter(nicIpConfiguration -> Optional.ofNullable(nicIpConfiguration.getPublicIpAddress())
                                                                                                                                                                    .isPresent())
                                                                                                                              .map(nicIpConfiguration -> nicIpConfiguration.getPublicIpAddress()
                                                                                                                                                                           .ipAddress())
                                                                                                                              .collect(Collectors.toList()))
                                                                                                           .build())
                                                    .status(String.valueOf(vm.powerState().toString()))
                                                    .build())
                                 .collect(Collectors.toSet());
    }

    @Override
    public List<ScriptResult> executeScriptOnInstanceId(Infrastructure infrastructure, String instanceId,
            InstanceScript instanceScript) {
        VirtualMachine vm = azureProviderUtils.searchVirtualMachineByID(azureServiceCache.getService(infrastructure),
                                                                        instanceId)
                                              .orElseThrow(() -> new RuntimeException("ERROR unable to find instance with ID: '" +
                                                                                      instanceId + "'"));
        return executeScriptOnVM(vm, instanceScript);
    }

    @Override
    public List<ScriptResult> executeScriptOnInstanceTag(Infrastructure infrastructure, String instanceTag,
            InstanceScript instanceScript) {
        VirtualMachine vm = azureProviderUtils.searchVirtualMachineByName(azureServiceCache.getService(infrastructure),
                                                                          instanceTag)
                                              .orElseThrow(() -> new RuntimeException("ERROR unable to find instance with name: '" +
                                                                                      instanceTag + "'"));
        return executeScriptOnVM(vm, instanceScript);
    }

    private List<ScriptResult> executeScriptOnVM(VirtualMachine vm, InstanceScript instanceScript) {

        // Concatenate all provided scripts in one (Multiple VMExtensions per handler not supported)
        StringBuilder concatenatedScripts = new StringBuilder();
        Lists.newArrayList(instanceScript.getScripts()).forEach(script -> {
            concatenatedScripts.append(script).append(';');
        });

        Optional<VirtualMachineExtension> vmExtension = vm.extensions()
                                                          .values()
                                                          .stream()
                                                          .filter(extension -> extension.publisherName()
                                                                                        .equals(scriptExtensionPublisher) &&
                                                                               extension.typeName()
                                                                                        .equals(scriptExtensionType))
                                                          .findAny();
        if (vmExtension.isPresent()) {
            vm.update()
              .updateExtension(vmExtension.get().name())
              .withPublicSetting(scriptExtensionCmdKey, concatenatedScripts.toString())
              .parent()
              .apply();
        } else {
            vm.update()
              .defineNewExtension(createUniqScriptName(vm.name()))
              .withPublisher(scriptExtensionPublisher)
              .withType(scriptExtensionType)
              .withVersion(scriptExtensionVersion)
              .withMinorVersionAutoUpgrade()
              .withPublicSetting(scriptExtensionCmdKey, concatenatedScripts.toString())
              .attach()
              .apply();
        }

        // Unable to retrieve scripts output, returns empty results instead
        return IntStream.rangeClosed(1, instanceScript.getScripts().length)
                        .mapToObj(scriptNumber -> new ScriptResult(vm.vmId(), "", ""))
                        .collect(Collectors.toList());
    }

    @Override
    public Set<Image> getAllImages(Infrastructure infrastructure) {
        return azureServiceCache.getService(infrastructure)
                                .virtualMachineCustomImages()
                                .list()
                                .stream()
                                .map(azureImage -> Image.builder().id(azureImage.id()).name(azureImage.name()).build())
                                .collect(Collectors.toSet());
    }

    @Override
    public void deleteInfrastructure(Infrastructure infrastructure) {
        azureServiceCache.removeService(infrastructure);
    }

    @Override
    public String addToInstancePublicIp(Infrastructure infrastructure, String instanceId) {
        Azure azureService = azureServiceCache.getService(infrastructure);
        VirtualMachine vm = azureProviderUtils.searchVirtualMachineByID(azureService, instanceId)
                                              .orElseThrow(() -> new RuntimeException("ERROR unable to find instance with ID: '" +
                                                                                      instanceId + "'"));
        ResourceGroup resourceGroup = azureService.resourceGroups().getByName(vm.resourceGroupName());

        // Retrieve all resources attached to the instance
        NetworkInterface networkInterface = vm.getPrimaryNetworkInterface();
        com.microsoft.azure.management.network.Network network = networkInterface.primaryIpConfiguration().getNetwork();
        NetworkSecurityGroup networkSecurityGroup = networkInterface.getNetworkSecurityGroup();
        Optional<PublicIpAddress> optionalPublicIPAddress = Optional.ofNullable(vm.getPrimaryPublicIpAddress());

        // Create a new public IP address
        PublicIpAddress newPublicIPAddress = azureProviderUtils.preparePublicIPAddress(azureService,
                                                                                       vm.region(),
                                                                                       resourceGroup,
                                                                                       createUniqPublicIPName(vm.name()),
                                                                                       defaultStaticPublicIP)
                                                               .create();

        // If primary network interface already has a public IP, then create a new/secondary network interface
        if (optionalPublicIPAddress.isPresent()) {
            vm.update()
              .withNewSecondaryNetworkInterface(azureProviderUtils.prepareNetworkInterface(azureService,
                                                                                           vm.region(),
                                                                                           resourceGroup,
                                                                                           createUniqNetworkInterfaceName(vm.name()),
                                                                                           network,
                                                                                           networkSecurityGroup,
                                                                                           newPublicIPAddress))
              .apply();
        } else {
            vm.getPrimaryNetworkInterface().update().withExistingPrimaryPublicIpAddress(newPublicIPAddress).apply();
        }

        return newPublicIPAddress.ipAddress();
    }

    @Override
    public void removeInstancePublicIp(Infrastructure infrastructure, String instanceId) {
        Azure azureService = azureServiceCache.getService(infrastructure);
        VirtualMachine vm = azureProviderUtils.searchVirtualMachineByID(azureService, instanceId)
                                              .orElseThrow(() -> new RuntimeException("ERROR unable to find instance with ID: '" +
                                                                                      instanceId + "'"));

        // If there are secondary interfaces then remove one of them including its public IP address
        Optional<NetworkInterface> optionalSecondaryNetworkInterface = vm.networkInterfaceIds()
                                                                         .stream()
                                                                         .map(networkInterfaceId -> azureService.networkInterfaces()
                                                                                                                .getById(networkInterfaceId))
                                                                         .filter(networkInterface -> Optional.ofNullable(networkInterface.primaryIpConfiguration()
                                                                                                                                         .getPublicIpAddress())
                                                                                                             .isPresent())
                                                                         .filter(networkInterface -> !networkInterface.id()
                                                                                                                      .equals(vm.getPrimaryPublicIpAddressId()))
                                                                         .findAny();
        if (optionalSecondaryNetworkInterface.isPresent()) {
            PublicIpAddress publicIPAddress = optionalSecondaryNetworkInterface.get()
                                                                               .primaryIpConfiguration()
                                                                               .getPublicIpAddress();
            optionalSecondaryNetworkInterface.get().update().withoutPrimaryPublicIpAddress().apply();
            azureService.networkInterfaces().deleteById(optionalSecondaryNetworkInterface.get().id());
            azureService.publicIpAddresses().deleteById(publicIPAddress.id());
        }
        // Otherwise remove the public IP address from the primary interface, if present
        else if (Optional.ofNullable(vm.getPrimaryPublicIpAddress()).isPresent()) {
            PublicIpAddress publicIPAddress = vm.getPrimaryPublicIpAddress();
            vm.getPrimaryNetworkInterface().update().withoutPrimaryPublicIpAddress().apply();
            azureService.publicIpAddresses().deleteById(publicIPAddress.id());
        }
    }
}

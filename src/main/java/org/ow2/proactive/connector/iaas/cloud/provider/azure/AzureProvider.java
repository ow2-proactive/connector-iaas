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

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.*;
import java.util.AbstractMap.SimpleImmutableEntry;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

import org.apache.commons.lang3.NotImplementedException;
import org.apache.commons.lang3.tuple.Pair;
import org.json.JSONArray;
import org.json.JSONObject;
import org.ow2.proactive.connector.iaas.cloud.TagManager;
import org.ow2.proactive.connector.iaas.cloud.provider.CloudProvider;
import org.ow2.proactive.connector.iaas.model.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import com.google.common.collect.Lists;
import com.google.common.util.concurrent.AbstractFuture;
import com.microsoft.azure.PagedList;
import com.microsoft.azure.management.Azure;
import com.microsoft.azure.management.compute.*;
import com.microsoft.azure.management.network.Network;
import com.microsoft.azure.management.network.NetworkInterface;
import com.microsoft.azure.management.network.NetworkSecurityGroup;
import com.microsoft.azure.management.network.NicIPConfiguration;
import com.microsoft.azure.management.network.PublicIPAddress;
import com.microsoft.azure.management.network.model.HasPrivateIPAddress;
import com.microsoft.azure.management.network.model.HasPublicIPAddress;
import com.microsoft.azure.management.resources.ResourceGroup;
import com.microsoft.azure.management.resources.fluentcore.arm.Region;
import com.microsoft.azure.management.resources.fluentcore.model.Creatable;
import com.microsoft.azure.management.resources.fluentcore.utils.SdkContext;

import lombok.Getter;
import lombok.extern.log4j.Log4j2;


/**
 * Provides Microsoft Azure clouds' management using the official java SDK.
 *
 * This class has been tested by ActiveEon to be thread safe.
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
@Log4j2
public class AzureProvider implements CloudProvider {

    @Getter
    protected final String type = "azure";

    protected static final VirtualMachineSizeTypes DEFAULT_VM_SIZE = VirtualMachineSizeTypes.STANDARD_D1_V2;

    protected static final int RESOURCES_NAME_EXTRA_CHARS = 10;

    // Timeout in minutes
    protected static final int TIMEOUT_SCRIPT_EXECUTION = 5;

    protected static final String VIRTUAL_NETWORK_NAME_BASE = "vnet";

    protected static final String PUBLIC_IP_ADDRESS_NAME_BASE = "ip";

    protected static final String NETWORK_SECURITY_GROUP_NAME_BASE = "sg";

    protected static final String NETWORK_INTERFACE_NAME_BASE = "if";

    protected static final String OS_DISK_NAME_BASE = "os";

    protected static final Boolean DEFAULT_STATIC_PUBLIC_IP = true;

    protected static final String SCRIPT_EXTENSION_PUBLISHER_LINUX = "Microsoft.Azure.Extensions";

    protected static final String SCRIPT_EXTENSION_PUBLISHER_WINDOWS = "Microsoft.Compute";

    protected static final String SCRIPT_EXTENSION_TYPE_LINUX = "CustomScript";

    protected static final String SCRIPT_EXTENSION_TYPE_WINDOWS = "CustomScriptExtension";

    protected static final String SCRIPT_EXTENSION_VERSION_LINUX = "2.0";

    protected static final String SCRIPT_EXTENSION_VERSION_WINDOWS = "1.9";

    protected static final String SCRIPT_EXTENSION_CMD_KEY = "commandToExecute";

    protected static final String SCRIPT_SEPARATOR = ";";

    protected static final String SINGLE_INSTANCE_NUMBER = "1";

    protected static final String INSTANCE_NOT_FOUND_ERROR = "ERROR unable to find instance with ID: ";

    protected static final String CLOUD_OFFERS_CURRENCY = "USD";

    protected static final String CLOUD_OFFERS_LOCAL = "en-US";

    protected static final String CLOUD_OFFERS_REGION_INFO = "US";

    protected static final String CLOUD_OFFERS_PAYASYOUGO = "MS-AZR-0003p";

    private static Map<String, Map<String, AzureKnownCost>> knownCostPerMeterIdPerApiKey = new HashMap<>();

    @Autowired
    protected AzureServiceCache azureServiceCache;

    @Autowired
    protected AzureProviderUtils azureProviderUtils;

    @Autowired
    protected AzureProviderNetworkingUtils azureProviderNetworkingUtils;

    @Autowired
    protected TagManager tagManager;

    @Value("${connector-iaas.azure.default-username:activeeon}")
    protected String defaultUsername;

    @Value("${connector-iaas.azure.default-password:Act1v€0N}")
    protected String defaultPassword;

    @Value("${connector-iaas.azure.default-private-network-cidr:10.0.0.0/24}")
    protected String defaultPrivateNetworkCidr;

    @Override
    public Set<String> listAvailableRegions(Infrastructure infrastructure) {
        return Arrays.stream(Region.values()).map(Region::name).collect(Collectors.toSet());
    }

    @Override
    public Set<Instance> createInstance(Infrastructure infrastructure, Instance instance) {

        Azure azureService = azureServiceCache.getService(infrastructure);
        String instanceTag = Optional.ofNullable(instance.getTag())
                                     .orElseThrow(() -> new RuntimeException("ERROR missing instance tag/name from instance: '" +
                                                                             instance + "'"));

        // Get the options (Optional by design)
        Optional<Options> options = Optional.ofNullable(instance.getOptions());

        // Retrieve the image name/ID (mandatory parameter)
        String imageNameOrId = Optional.ofNullable(instance.getImage())
                                       .orElseThrow(() -> new RuntimeException("ERROR missing Image name/id from instance: '" +
                                                                               instance + "'"));

        // Try to retrieve the resource group from provided name, otherwise get it from image ID
        ResourceGroup resourceGroup = azureProviderUtils.searchResourceGroupByName(azureService,
                                                                                   options.map(Options::getResourceGroup)
                                                                                          .orElseGet(() -> getImageById(azureService,
                                                                                                                        imageNameOrId).map(VirtualMachineCustomImage::resourceGroupName)
                                                                                                                                      .orElseThrow(() -> new RuntimeException("ERROR a resource group and/or an image ID must be specified from instance: '" +
                                                                                                                                                                              instance +
                                                                                                                                                                              "'"))))
                                                        .orElseThrow(() -> new RuntimeException("ERROR unable to find a suitable resource group from instance: '" +
                                                                                                instance + "'"));

        // Check for Image by name first and then by id
        VirtualMachineCustomImage image = getImageByName(azureService,
                                                         resourceGroup.name(),
                                                         imageNameOrId).orElseGet(() -> getImageById(azureService,
                                                                                                     imageNameOrId).orElseThrow(() -> new RuntimeException("ERROR unable to find custom Image: '" +
                                                                                                                                                           instance.getImage() +
                                                                                                                                                           "'")));

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
                                                                                                                         resourceGroup.name(),
                                                                                                                         subnetId)
                                                                                             .get());

        // Prepare a new  security group (same for all VMs) - we take into account the ports the user want to be opened, if specified
        Creatable<NetworkSecurityGroup> creatableNetworkSecurityGroup = options.map(Options::getPortsToOpen)
                                                                               .map(ports -> azureProviderNetworkingUtils.prepareProactiveNetworkSecurityGroup(azureService,
                                                                                                                                                               region,
                                                                                                                                                               resourceGroup,
                                                                                                                                                               createUniqueSecurityGroupName(instance.getTag()),
                                                                                                                                                               ports))
                                                                               .orElse(azureProviderNetworkingUtils.prepareProactiveNetworkSecurityGroup(azureService,
                                                                                                                                                         region,
                                                                                                                                                         resourceGroup,
                                                                                                                                                         createUniqueSecurityGroupName(instance.getTag())));

        // Get existing security group if specified
        Optional<NetworkSecurityGroup> optionalNetworkSecurityGroup = options.map(Options::getSecurityGroupNames)
                                                                             .map(secGrpNames -> secGrpNames.get(0))
                                                                             .map(secGrpName -> azureProviderUtils.searchNetworkSecurityGroupByName(azureService,
                                                                                                                                                    resourceGroup.name(),
                                                                                                                                                    secGrpName)
                                                                                                                  .get());

        // Get existing public IP address if specified
        Optional<PublicIPAddress> optionalPublicIPAddress = options.map(Options::getPublicIpAddress)
                                                                   .map(PublicIPAddress -> azureProviderUtils.searchPublicIpAddressByIp(azureService,
                                                                                                                                        PublicIPAddress)
                                                                                                             .get());

        // Prepare the VM(s)
        Optional<Boolean> optionalStaticPublicIP = options.map(Options::getStaticPublicIP);

        AzureNetworkOptions networkOptions = new AzureNetworkOptions(creatableVirtualNetwork,
                                                                     optionalVirtualNetwork,
                                                                     creatableNetworkSecurityGroup,
                                                                     optionalNetworkSecurityGroup,
                                                                     optionalPublicIPAddress,
                                                                     optionalStaticPublicIP);

        List<Creatable<VirtualMachine>> creatableVirtualMachines = IntStream.rangeClosed(1,
                                                                                         Integer.valueOf(Optional.ofNullable(instance.getNumber())
                                                                                                                 .orElse(SINGLE_INSTANCE_NUMBER)))
                                                                            .mapToObj(instanceNumber -> {
                                                                                Creatable<NetworkInterface> creatableNetworkInterface = createPublicAddressAndNetworkInterface(azureService,
                                                                                                                                                                               instanceTag,
                                                                                                                                                                               resourceGroup,
                                                                                                                                                                               region,
                                                                                                                                                                               networkOptions,
                                                                                                                                                                               instanceNumber);
                                                                                return prepareVirtualMachine(infrastructure.getId(),
                                                                                                             instance,
                                                                                                             azureService,
                                                                                                             resourceGroup,
                                                                                                             region,
                                                                                                             createUniqueInstanceTag(instanceTag,
                                                                                                                                     instanceNumber),
                                                                                                             image,
                                                                                                             creatableNetworkInterface);
                                                                            })
                                                                            .collect(Collectors.toList());

        // Execute init scripts asynchronously on VMs
        //vms.forEach(vm -> executeScriptOnVM(vm, instance.getInitScript()));

        // Create all VMs in parallel
        return azureService.virtualMachines()
                           .create(creatableVirtualMachines)
                           .values()
                           .stream()
                           // Use vmId() instead of id() for clarity (id() contains full resource path)
                           .map(vm -> instance.withTag(vm.name()).withId(vm.vmId()).withNumber(SINGLE_INSTANCE_NUMBER))
                           .collect(Collectors.toSet());
    }

    protected Creatable<NetworkInterface> createPublicAddressAndNetworkInterface(Azure azureService, String instanceTag,
            ResourceGroup resourceGroup, Region region, AzureNetworkOptions networkOptions, int instanceNumber) {
        // Create a new public IP address (one per VM)
        String publicIPAddressName = createUniquePublicIPName(createUniqueInstanceTag(instanceTag, instanceNumber));
        Creatable<PublicIPAddress> creatablePublicIPAddress = azureProviderNetworkingUtils.preparePublicIPAddress(azureService,
                                                                                                                  region,
                                                                                                                  resourceGroup,
                                                                                                                  publicIPAddressName,
                                                                                                                  networkOptions.getOptionalStaticPublicIP()
                                                                                                                                .orElse(DEFAULT_STATIC_PUBLIC_IP));

        // Prepare a new network interface (one per VM)
        String networkInterfaceName = createUniqueNetworkInterfaceName(createUniqueInstanceTag(instanceTag,
                                                                                               instanceNumber));
        return azureProviderNetworkingUtils.prepareNetworkInterface(azureService,
                                                                    region,
                                                                    resourceGroup,
                                                                    networkInterfaceName,
                                                                    networkOptions.getCreatableVirtualNetwork(),
                                                                    networkOptions.getOptionalVirtualNetwork()
                                                                                  .orElse(null),
                                                                    networkOptions.getCreatableNetworkSecurityGroup(),
                                                                    networkOptions.getOptionalNetworkSecurityGroup()
                                                                                  .orElse(null),
                                                                    creatablePublicIPAddress,
                                                                    instanceNumber == 1 ? networkOptions.getOptionalPublicIpAddress()
                                                                                                        .orElse(null)
                                                                                        : null);
    }

    protected Optional<VirtualMachineCustomImage> getImageByName(Azure azureService, String resourceGroup,
            String name) {
        return Optional.ofNullable(azureService.virtualMachineCustomImages().getByResourceGroup(resourceGroup, name));
    }

    protected Optional<VirtualMachineCustomImage> getImageById(Azure azureService, String id) {
        return Optional.ofNullable(azureService.virtualMachineCustomImages().getById(id));
    }

    protected Creatable<VirtualMachine> prepareVirtualMachine(String infrastructureId, Instance instance,
            Azure azureService, ResourceGroup resourceGroup, Region region, String instanceTag,
            VirtualMachineCustomImage image, Creatable<NetworkInterface> creatableNetworkInterface) {

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
            throw new RuntimeException(unsupportedOperatingSystemError(operatingSystemType.toString()));
        }

        // Set VM size (or type) and name of OS' disk
        Optional<VirtualMachineSizeTypes> optionalHardwareType = Optional.ofNullable(instance.getHardware())
                                                                         .map(Hardware::getType)
                                                                         .map(VirtualMachineSizeTypes::fromString);
        VirtualMachine.DefinitionStages.WithCreate creatableVMWithSize = creatableVirtualMachineWithImage.withSize(optionalHardwareType.orElse(DEFAULT_VM_SIZE))
                                                                                                         .withOSDiskName(createUniqOSDiskName(instanceTag));

        // Add init script(s) using dedicated Microsoft extension
        Optional.ofNullable(instance.getInitScript()).map(InstanceScript::getScripts).ifPresent(scripts -> {
            if (scripts.length > 0) {
                StringJoiner concatenatedScripts = new StringJoiner(SCRIPT_SEPARATOR);
                Lists.newArrayList(scripts).forEach(script -> concatenatedScripts.add(script));
                if (operatingSystemType.equals(OperatingSystemTypes.LINUX)) {
                    creatableVMWithSize.defineNewExtension(createUniqueScriptName(instanceTag))
                                       .withPublisher(SCRIPT_EXTENSION_PUBLISHER_LINUX)
                                       .withType(SCRIPT_EXTENSION_TYPE_LINUX)
                                       .withVersion(SCRIPT_EXTENSION_VERSION_LINUX)
                                       .withMinorVersionAutoUpgrade()
                                       .withPublicSetting(SCRIPT_EXTENSION_CMD_KEY, concatenatedScripts.toString())
                                       .attach();
                } else if (operatingSystemType.equals(OperatingSystemTypes.WINDOWS)) {
                    creatableVMWithSize.defineNewExtension(createUniqueScriptName(instanceTag))
                                       .withPublisher(SCRIPT_EXTENSION_PUBLISHER_WINDOWS)
                                       .withType(SCRIPT_EXTENSION_TYPE_WINDOWS)
                                       .withVersion(SCRIPT_EXTENSION_VERSION_WINDOWS)
                                       .withMinorVersionAutoUpgrade()
                                       .withPublicSetting(SCRIPT_EXTENSION_CMD_KEY, concatenatedScripts.toString())
                                       .attach();
                } else {
                    throw new RuntimeException(unsupportedOperatingSystemError(operatingSystemType.toString()));
                }
            }
        });

        // Set tags
        return creatableVMWithSize.withTags(tagManager.retrieveAllTags(infrastructureId, instance.getOptions())
                                                      .stream()
                                                      .collect(Collectors.toMap(Tag::getKey, Tag::getValue)));
    }

    protected VirtualMachine.DefinitionStages.WithLinuxCreateManaged configureLinuxVirtualMachine(Azure azureService,
            String instanceTag, Region region, ResourceGroup resourceGroup, InstanceCredentials instanceCredentials,
            VirtualMachineCustomImage image, Creatable<NetworkInterface> creatableNetworkInterface) {
        // Retrieve optional credentials
        Optional<String> optionalUsername = Optional.ofNullable(instanceCredentials)
                                                    .map(InstanceCredentials::getUsername);
        Optional<String> optionalPassword = Optional.ofNullable(instanceCredentials)
                                                    .map(InstanceCredentials::getPassword);
        Optional<String> optionalPublicKey = Optional.ofNullable(instanceCredentials)
                                                     .map(InstanceCredentials::getPublicKey);

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

    protected VirtualMachine.DefinitionStages.WithWindowsCreateManaged configureWindowsVirtualMachine(
            Azure azureService, String instanceTag, Region region, ResourceGroup resourceGroup,
            InstanceCredentials instanceCredentials, VirtualMachineCustomImage image,
            Creatable<NetworkInterface> creatableNetworkInterface) {
        // Retrieve optional credentials
        Optional<String> optionalUsername = Optional.ofNullable(instanceCredentials)
                                                    .map(InstanceCredentials::getUsername);
        Optional<String> optionalPassword = Optional.ofNullable(instanceCredentials)
                                                    .map(InstanceCredentials::getPassword);

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
     * Create a unique tag for a VM based on the original tag provided and the instance index
     *
     * @param tagBase       the tag base
     * @param instanceIndex the instance index
     * @return a unique VM tag
     */
    protected static String createUniqueInstanceTag(String tagBase, int instanceIndex) {
        if (instanceIndex > 1) {
            return tagBase + String.valueOf(instanceIndex);
        }
        return tagBase;
    }

    protected static String createUniqueSecurityGroupName(String instanceTag) {
        return createUniqueName(instanceTag, NETWORK_SECURITY_GROUP_NAME_BASE);
    }

    protected static String createUniqueVirtualNetworkName(String instanceTag) {
        return createUniqueName(instanceTag, VIRTUAL_NETWORK_NAME_BASE);
    }

    protected static String createUniqueNetworkInterfaceName(String instanceTag) {
        return createUniqueName(instanceTag, NETWORK_INTERFACE_NAME_BASE);
    }

    protected static String createUniquePublicIPName(String instanceTag) {
        return createUniqueName(instanceTag, PUBLIC_IP_ADDRESS_NAME_BASE);
    }

    protected static String createUniqOSDiskName(String instanceTag) {
        return createUniqueName(instanceTag, OS_DISK_NAME_BASE);
    }

    protected static String createUniqueScriptName(String instanceTag) {
        return createUniqueName(instanceTag, "");
    }

    protected static String createUniqueName(String customPart, String basePart) {
        return SdkContext.randomResourceName(customPart + '-' + basePart,
                                             customPart.length() + basePart.length() + 1 + RESOURCES_NAME_EXTRA_CHARS);
    }

    @Override
    public void deleteInstance(Infrastructure infrastructure, String instanceId) {
        Azure azureService = azureServiceCache.getService(infrastructure);

        VirtualMachine vm = azureProviderUtils.searchVirtualMachineByID(azureService, instanceId)
                                              .orElseThrow(() -> new RuntimeException(INSTANCE_NOT_FOUND_ERROR + "'" +
                                                                                      instanceId + "'"));

        log.info("Deletion of all Azure resources of instance " + instanceId +
                 " is being requested to the provider (infrastructure: " + infrastructure.getId() + ")");

        // Retrieve all resources attached to the instance
        List<com.microsoft.azure.management.network.Network> networks = azureProviderNetworkingUtils.getVMNetworks(azureService,
                                                                                                                   vm);
        List<NetworkSecurityGroup> networkSecurityGroups = azureProviderNetworkingUtils.getVMSecurityGroups(azureService,
                                                                                                            vm);
        List<PublicIPAddress> PublicIPAddresses = azureProviderNetworkingUtils.getVMPublicIPAddresses(azureService, vm);
        String osDiskID = vm.osDiskId();

        // Delete the VM first
        azureService.virtualMachines().deleteById(vm.id());

        // Then delete all network interfaces attached
        vm.networkInterfaceIds().forEach(id -> azureService.networkInterfaces().deleteById(id));

        // Delete all public IP addresses
        PublicIPAddresses.stream()
                         .map(PublicIPAddress::id)
                         .forEach(id -> azureService.publicIPAddresses().deleteById(id));

        // Delete its main disk (OS), *and keep data disks*
        azureService.disks().deleteById(osDiskID);

        // Delete the security groups if not attached to any remaining network interface
        deleteSecurityGroups(azureService, networkSecurityGroups);

        // Delete the virtual networks if not attached to any remaining network interface
        deleteNetworks(azureService, networks);

        log.info("Deletion of all Azure resources of instance " + instanceId + " has been executed.");
    }

    protected void deleteSecurityGroups(Azure azureService, List<NetworkSecurityGroup> networkSecurityGroups) {
        // Delete the security groups if not attached to any remaining network interface
        networkSecurityGroups.stream()
                             .map(NetworkSecurityGroup::id)
                             .filter(id -> azureService.networkInterfaces()
                                                       .list()
                                                       .stream()
                                                       .map(NetworkInterface::getNetworkSecurityGroup)
                                                       .filter(Objects::nonNull)
                                                       .noneMatch(networkSecurityGroup -> networkSecurityGroup.id()
                                                                                                              .equals(id)))
                             .forEach(id -> azureService.networkSecurityGroups().deleteById(id));
    }

    protected void deleteNetworks(Azure azureService, List<com.microsoft.azure.management.network.Network> networks) {
        networks.stream()
                .map(Network::id)
                .filter(id -> azureService.networkInterfaces()
                                          .list()
                                          .stream()
                                          .flatMap(networkInterface -> networkInterface.ipConfigurations()
                                                                                       .values()
                                                                                       .stream())
                                          .filter(Objects::nonNull)
                                          .map(NicIPConfiguration::getNetwork)
                                          .filter(Objects::nonNull)
                                          .noneMatch(network -> network.id().equals(id)))
                .forEach(id -> azureService.networks().deleteById(id));
    }

    @Override
    public Set<Instance> getAllInfrastructureInstances(Infrastructure infrastructure) {
        Azure azureService = azureServiceCache.getService(infrastructure);
        return getInstancesFromVMs(azureService, azureProviderUtils.getAllVirtualMachines(azureService));
    }

    @Override
    public Set<Instance> getCreatedInfrastructureInstances(Infrastructure infrastructure) {
        Azure azureService = azureServiceCache.getService(infrastructure);
        Tag connectorIaasTag = tagManager.getConnectorIaasTag();
        return getInstancesFromVMs(azureService, azureProviderUtils.getAllVirtualMachines(azureService)
                                                                   .stream()
                                                                   .filter(vm -> vm.tags().keySet().contains(
                                                                                                             connectorIaasTag.getKey()) &&
                                                                                 vm.tags()
                                                                                   .get(connectorIaasTag.getKey())
                                                                                   .equals(connectorIaasTag.getValue()))
                                                                   .collect(Collectors.toSet()));
    }

    protected Set<Instance> getInstancesFromVMs(Azure azureService, Set<VirtualMachine> vms) {
        return vms.stream()
                  .map(vm -> Instance.builder()
                                     .id(vm.vmId())
                                     .tag(vm.name())
                                     .number(SINGLE_INSTANCE_NUMBER)
                                     .hardware(Hardware.builder().type(String.valueOf(vm.size())).build())
                                     .network(org.ow2.proactive.connector.iaas.model.Network.builder()
                                                                                            .publicAddresses(buildPublicAddressList(azureService,
                                                                                                                                    vm))
                                                                                            .privateAddresses(buildPrivateAddressList(azureService,
                                                                                                                                      vm))
                                                                                            .build())
                                     .status(String.valueOf(vm.powerState()))
                                     .build())
                  .collect(Collectors.toSet());
    }

    protected List<String> buildPrivateAddressList(Azure azureService, VirtualMachine vm) {
        return vm.networkInterfaceIds()
                 .stream()
                 .map(networkInterfaceId -> azureService.networkInterfaces().getById(networkInterfaceId))
                 .flatMap(networkInterface -> networkInterface.ipConfigurations().values().stream())
                 .filter(Objects::nonNull)
                 .map(HasPrivateIPAddress::privateIPAddress)
                 .collect(Collectors.toList());
    }

    protected List<String> buildPublicAddressList(Azure azureService, VirtualMachine vm) {
        return vm.networkInterfaceIds()
                 .stream()
                 .map(networkInterfaceId -> azureService.networkInterfaces().getById(networkInterfaceId))
                 .map(NetworkInterface::primaryIPConfiguration)
                 .filter(Objects::nonNull)
                 .map(HasPublicIPAddress::getPublicIPAddress)
                 .filter(Objects::nonNull)
                 .map(PublicIPAddress::ipAddress)
                 .collect(Collectors.toList());
    }

    @Override
    public List<ScriptResult> executeScriptOnInstanceId(Infrastructure infrastructure, String instanceId,
            InstanceScript instanceScript) {
        VirtualMachine vm = azureProviderUtils.searchVirtualMachineByID(azureServiceCache.getService(infrastructure),
                                                                        instanceId)
                                              .orElseThrow(() -> new RuntimeException(INSTANCE_NOT_FOUND_ERROR + "'" +
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

    private Optional<VirtualMachineExtension> retrieveExistingExtension(VirtualMachine vm) {

        if (vm.osType().equals(OperatingSystemTypes.LINUX)) {
            return vm.listExtensions()
                     .values()
                     .stream()
                     .filter(extension -> extension.publisherName().equals(SCRIPT_EXTENSION_PUBLISHER_LINUX) &&
                                          extension.typeName().equals(SCRIPT_EXTENSION_TYPE_LINUX))
                     .findAny();
        } else if (vm.osType().equals(OperatingSystemTypes.WINDOWS)) {
            return vm.listExtensions()
                     .values()
                     .stream()
                     .filter(extension -> extension.publisherName().equals(SCRIPT_EXTENSION_PUBLISHER_WINDOWS) &&
                                          extension.typeName().equals(SCRIPT_EXTENSION_TYPE_WINDOWS))
                     .findAny();
        } else {
            throw new RuntimeException(unsupportedOperatingSystemError(vm.osType().toString()));
        }
    }

    private void updateExistingExtension(VirtualMachine vm, VirtualMachineExtension extension, String script) {
        log.info("Request Azure provider to execute script: " + script);
        AbstractFuture<VirtualMachine> vmfuture = vm.update()
                                                    .updateExtension(extension.name())
                                                    .withPublicSetting(SCRIPT_EXTENSION_CMD_KEY, script)
                                                    .parent()
                                                    .applyAsync(null);
        try {
            vmfuture.get(TIMEOUT_SCRIPT_EXECUTION, TimeUnit.MINUTES);
        } catch (InterruptedException e) {
            log.info("An interruption occurred when updating extension to execute script on a VM.");
            Thread.currentThread().interrupt();
            e.printStackTrace();
        } catch (ExecutionException e) {
            log.info("An error occurred when updating extension to execute script on a VM.");
            e.printStackTrace();
        } catch (TimeoutException e) {
            log.info("Timeout reached when updating extension to execute script on a VM.");
        }
        log.debug("Execution of script has been requested.");
    }

    private void installNewExtension(VirtualMachine vm, String script) {
        log.info("Request Azure provider to install script extension and to execute script: " + script);
        if (vm.osType().equals(OperatingSystemTypes.LINUX)) {
            AbstractFuture<VirtualMachine> vmfuture = vm.update()
                                                        .defineNewExtension(createUniqueScriptName(vm.name()))
                                                        .withPublisher(SCRIPT_EXTENSION_PUBLISHER_LINUX)
                                                        .withType(SCRIPT_EXTENSION_TYPE_LINUX)
                                                        .withVersion(SCRIPT_EXTENSION_VERSION_LINUX)
                                                        .withMinorVersionAutoUpgrade()
                                                        .withPublicSetting(SCRIPT_EXTENSION_CMD_KEY, script)
                                                        .attach()
                                                        .applyAsync(null);
            try {
                vmfuture.get(TIMEOUT_SCRIPT_EXECUTION, TimeUnit.MINUTES);
            } catch (InterruptedException e) {
                log.error("An interruption occurred while installing extension and executing script to a Linux VM.");
                Thread.currentThread().interrupt();
                e.printStackTrace();
            } catch (ExecutionException e) {
                log.error("An error occurred when installing extension and executing script to a Linux VM.");
                e.printStackTrace();
            } catch (TimeoutException e) {
                log.info("Timeout reached when installing extension and executing script to a Linux VM.");
            }

        } else if (vm.osType().equals(OperatingSystemTypes.WINDOWS)) {
            AbstractFuture<VirtualMachine> vmfuture = vm.update()
                                                        .defineNewExtension(createUniqueScriptName(vm.name()))
                                                        .withPublisher(SCRIPT_EXTENSION_PUBLISHER_WINDOWS)
                                                        .withType(SCRIPT_EXTENSION_TYPE_WINDOWS)
                                                        .withVersion(SCRIPT_EXTENSION_VERSION_WINDOWS)
                                                        .withMinorVersionAutoUpgrade()
                                                        .withPublicSetting(SCRIPT_EXTENSION_CMD_KEY, script)
                                                        .attach()
                                                        .applyAsync(null);
            try {
                vmfuture.get(TIMEOUT_SCRIPT_EXECUTION, TimeUnit.MINUTES);
            } catch (InterruptedException e) {
                log.error("An interruption occurred while installing extension and executing script to a Windows VM.");
                Thread.currentThread().interrupt();
            } catch (ExecutionException e) {
                log.info("An error occurred when installing extension and executing script to a Windows VM.");
                e.printStackTrace();
            } catch (TimeoutException e) {
                log.info("Timeout reached when installing extension and executing script to a Windows VM.");
            }
        } else {
            throw new RuntimeException(unsupportedOperatingSystemError(vm.osType().toString()));
        }
        log.debug("Installation of script extension and execution of script is terminated.");
    }

    protected List<ScriptResult> executeScriptOnVM(VirtualMachine vm, InstanceScript instanceScript) {

        // Concatenate all provided scripts in one (Multiple VMExtensions per handler not supported)
        StringJoiner concatenatedScripts = new StringJoiner(SCRIPT_SEPARATOR);
        Arrays.stream(instanceScript.getScripts()).forEach(script -> {
            concatenatedScripts.add(script);
        });

        // Update existing or install new extension
        Optional<VirtualMachineExtension> vmExtension = retrieveExistingExtension(vm);
        if (vmExtension.isPresent()) {
            updateExistingExtension(vm, vmExtension.get(), concatenatedScripts.toString());
        } else {
            installNewExtension(vm, concatenatedScripts.toString());
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
                                .map(azureImage -> Image.builder()
                                                        .id(azureImage.id())
                                                        .name(azureImage.name())
                                                        .operatingSystem(OperatingSystem.builder()
                                                                                        .arch("AMD64")
                                                                                        .description(azureImage.osDiskImage()
                                                                                                               .toString())
                                                                                        .family(azureImage.osDiskImage()
                                                                                                          .osType()
                                                                                                          .toString())
                                                                                        .is64Bit(true)
                                                                                        .build())
                                                        .build())
                                .collect(Collectors.toSet());
    }

    @Override
    public Set<Hardware> getAllHardwares(Infrastructure infrastructure) {
        return azureServiceCache.getService(infrastructure)
                                .virtualMachines()
                                .sizes()
                                .listByRegion(infrastructure.getRegion())
                                .parallelStream()
                                .map(vms -> Hardware.builder()
                                                    .type(vms.name())
                                                    .minCores("" + vms.numberOfCores())
                                                    .minFreq("-1")
                                                    .minRam("" + vms.memoryInMB())
                                                    .build())
                                .collect(Collectors.toSet());
    }

    @Override
    public void deleteInfrastructure(Infrastructure infrastructure) {
        azureServiceCache.removeService(infrastructure);
    }

    @Override
    public String addToInstancePublicIp(Infrastructure infrastructure, String instanceId, String optionalDesiredIp) {
        Azure azureService = azureServiceCache.getService(infrastructure);
        VirtualMachine vm = azureProviderUtils.searchVirtualMachineByID(azureService, instanceId)
                                              .orElseThrow(() -> new RuntimeException(INSTANCE_NOT_FOUND_ERROR + "'" +
                                                                                      instanceId + "'"));
        ResourceGroup resourceGroup = azureService.resourceGroups().getByName(vm.resourceGroupName());

        // Try to retrieve the desired public IP address or create a new one
        PublicIPAddress publicIPAddress = Optional.ofNullable(optionalDesiredIp)
                                                  .map(opt -> azureService.publicIPAddresses()
                                                                          .list()
                                                                          .stream()
                                                                          .filter(availablePublicIP -> availablePublicIP.ipAddress()
                                                                                                                        .equals(opt))
                                                                          .findAny()
                                                                          .get())
                                                  .orElseGet(() -> azureProviderNetworkingUtils.preparePublicIPAddress(azureService,
                                                                                                                       vm.region(),
                                                                                                                       resourceGroup,
                                                                                                                       createUniquePublicIPName(vm.name()),
                                                                                                                       DEFAULT_STATIC_PUBLIC_IP)
                                                                                               .create());

        List<NetworkInterface> networkInterfaces = vm.networkInterfaceIds()
                                                     .stream()
                                                     .map(id -> azureService.networkInterfaces().getById(id))
                                                     .collect(Collectors.toList());

        // If all existing network interfaces already have a public IP, then create a new/secondary network interface
        if (networkInterfaces.stream()
                             .allMatch(netIf -> Optional.ofNullable(netIf.primaryIPConfiguration()).isPresent() &&
                                                Optional.ofNullable(netIf.primaryIPConfiguration().getPublicIPAddress())
                                                        .isPresent())) {
            // Reuse the network configuration (virtual private network & security group) of the primary network interface
            addPublicIpWithNewSecondaryNetworkInterface(azureService, vm, resourceGroup, publicIPAddress);
            // Otherwise add the public address to the first network interface without public IP
        } else {
            networkInterfaces.stream()
                             .filter(netIf -> !Optional.ofNullable(netIf.primaryIPConfiguration()).isPresent() ||
                                              !Optional.ofNullable(netIf.primaryIPConfiguration().getPublicIPAddress())
                                                       .isPresent())
                             .findFirst()
                             .ifPresent(pubIpAddr -> pubIpAddr.update()
                                                              .withExistingPrimaryPublicIPAddress(publicIPAddress)
                                                              .apply());
        }

        return publicIPAddress.ipAddress();
    }

    protected void addPublicIpWithNewSecondaryNetworkInterface(Azure azureService, VirtualMachine vm,
            ResourceGroup resourceGroup, PublicIPAddress PublicIPAddress) {
        // Reuse the network configuration (virtual private network & security group) of the primary network interface
        NetworkInterface networkInterface = vm.getPrimaryNetworkInterface();
        NetworkSecurityGroup networkSecurityGroup = networkInterface.getNetworkSecurityGroup();
        com.microsoft.azure.management.network.Network network = networkInterface.primaryIPConfiguration().getNetwork();
        NetworkInterface newSecondaryNetworkInterface = azureProviderNetworkingUtils.prepareNetworkInterface(azureService,
                                                                                                             vm.region(),
                                                                                                             resourceGroup,
                                                                                                             createUniqueNetworkInterfaceName(vm.name()),
                                                                                                             network,
                                                                                                             networkSecurityGroup,
                                                                                                             PublicIPAddress)
                                                                                    .create();
        try {
            vm.update().withExistingSecondaryNetworkInterface(newSecondaryNetworkInterface).apply();
        } catch (RuntimeException ex) {
            log.warn("Cannot add new network interface. Remove it and modify the primary network interface instead",
                     ex);
            handleAddPublicIpWithNewSecondaryNetworkInterfaceException(azureService,
                                                                       vm,
                                                                       PublicIPAddress,
                                                                       newSecondaryNetworkInterface);
        }
    }

    protected void handleAddPublicIpWithNewSecondaryNetworkInterfaceException(Azure azureService, VirtualMachine vm,
            PublicIPAddress PublicIPAddress, NetworkInterface newSecondaryNetworkInterface) {
        removePublicIpFromNetworkInterface(azureService, newSecondaryNetworkInterface);
        replaceVMPrimaryPublicIPAddress(azureService, vm, PublicIPAddress);
    }

    protected void removePublicIpFromNetworkInterface(Azure azureService, NetworkInterface networkInterface) {
        networkInterface.update().withoutPrimaryPublicIPAddress().apply();
        azureService.networkInterfaces().deleteById(networkInterface.id());
    }

    protected void replaceVMPrimaryPublicIPAddress(Azure azureService, VirtualMachine vm,
            PublicIPAddress newPublicIPAddress) {
        PublicIPAddress existingPublicIPAddress = vm.getPrimaryPublicIPAddress();
        vm.getPrimaryNetworkInterface().update().withoutPrimaryPublicIPAddress().apply();
        azureService.publicIPAddresses().deleteById(existingPublicIPAddress.id());
        vm.getPrimaryNetworkInterface().update().withExistingPrimaryPublicIPAddress(newPublicIPAddress).apply();
    }

    @Override
    public void removeInstancePublicIp(Infrastructure infrastructure, String instanceId, String optionalDesiredIp) {
        Azure azureService = azureServiceCache.getService(infrastructure);
        VirtualMachine vm = azureProviderUtils.searchVirtualMachineByID(azureService, instanceId)
                                              .orElseThrow(() -> new RuntimeException(INSTANCE_NOT_FOUND_ERROR + "'" +
                                                                                      instanceId + "'"));
        Optional<PublicIPAddress> optionalPublicIPAddress = Optional.ofNullable(optionalDesiredIp)
                                                                    .map(opt -> azureService.publicIPAddresses()
                                                                                            .list()
                                                                                            .stream()
                                                                                            .filter(availablePublicIP -> availablePublicIP.ipAddress()
                                                                                                                                          .equals(opt))
                                                                                            .findAny()
                                                                                            .get());

        // Delete the desired IP address if present
        if (optionalPublicIPAddress.isPresent()) {
            azureService.publicIPAddresses().deleteById(optionalPublicIPAddress.get().id());
            return;
        }

        // If there is a secondary interface with a public IP then remove it in prior
        Optional<NetworkInterface> optionalSecondaryNetworkInterface = vm.networkInterfaceIds()
                                                                         .stream()
                                                                         .map(networkInterfaceId -> azureService.networkInterfaces()
                                                                                                                .getById(networkInterfaceId))
                                                                         .filter(networkInterface -> Optional.ofNullable(networkInterface.primaryIPConfiguration()
                                                                                                                                         .getPublicIPAddress())
                                                                                                             .isPresent())
                                                                         .filter(networkInterface -> !networkInterface.id()
                                                                                                                      .equals(vm.getPrimaryPublicIPAddressId()))
                                                                         .findAny();
        if (optionalSecondaryNetworkInterface.isPresent()) {
            PublicIPAddress publicIPAddress = optionalSecondaryNetworkInterface.get()
                                                                               .primaryIPConfiguration()
                                                                               .getPublicIPAddress();
            optionalSecondaryNetworkInterface.get().update().withoutPrimaryPublicIPAddress().apply();
            azureService.publicIPAddresses().deleteById(publicIPAddress.id());
        }
        // Otherwise remove the public IP address from the primary interface if present
        else if (Optional.ofNullable(vm.getPrimaryPublicIPAddress()).isPresent()) {
            PublicIPAddress publicIPAddress = vm.getPrimaryPublicIPAddress();
            vm.getPrimaryNetworkInterface().update().withoutPrimaryPublicIPAddress().apply();
            azureService.publicIPAddresses().deleteById(publicIPAddress.id());
        }
    }

    @Override
    public SimpleImmutableEntry<String, String> createKeyPair(Infrastructure infrastructure, Instance instance) {
        throw new UnsupportedOperationException();
    }

    @Override
    public void deleteKeyPair(Infrastructure infrastructure, String keyPairName, String region) {
        throw new NotImplementedException("This method is not yet implemented.");
    }

    @Override
    public Pair<String, Set<NodeCandidate>> getNodeCandidate(Infrastructure infra, String region, String imageReq,
            String token) {
        try {
            // We Connect to Azure
            Azure service = azureServiceCache.getService(infra);
            //PagedList<VirtualMachineSize> vmTypeInRegion = service.virtualMachines().sizes().listByRegion(region);
            // We retrieve the Sku from compute resource type
            PagedList<ComputeSku> sku = service.computeSkus()
                                               .listbyRegionAndResourceType(Region.fromName(region),
                                                                            ComputeResourceType.VIRTUALMACHINES);
            String id = infra.getCredentials().getUsername();
            if (!knownCostPerMeterIdPerApiKey.containsKey(id)) {
                // We need to initiate the cost structure if none is already present.
                // We try to do this once since the ratecard structure is heavy (~19MB)
                String rateCard = getRateCard(infra);
                knownCostPerMeterIdPerApiKey.put(id, parseVmRateCard(rateCard));
            }
            Set<NodeCandidate> result = new HashSet<>();
            for (ComputeSku csku : sku) {
                for (ResourceSkuCosts cost : csku.costs()) {
                    // Retreving info for node candidate
                    String memoryGB = csku.capabilities()
                                          .stream()
                                          .filter(cap -> cap.name().equals("MemoryGB"))
                                          .map(res -> res.value())
                                          .findAny()
                                          .orElse("0 GB");
                    String memoryMB = Double.parseDouble(memoryGB.split(" ")[0]) * 1024 + "";
                    String vCpu = csku.capabilities()
                                      .stream()
                                      .filter(cap -> cap.name().equals("vCPUsAvailable"))
                                      .map(res -> res.value())
                                      .findAny()
                                      .orElse("0");
                    String type = csku.name().toString();
                    // The Azure API doesn't provide any mean to access the freq of VMs

                    // We build up the resulting structure.
                    result.add(NodeCandidate.builder()
                                            .img(Image.builder().name("Unspecified").build())
                                            .region(region)
                                            .cloud(this.getType())
                                            .price(knownCostPerMeterIdPerApiKey.get(id)
                                                                               .get(cost.meterID()).meterRatesZero)
                                            .hw(Hardware.builder()
                                                        .minRam(memoryMB)
                                                        .minCores(vCpu)
                                                        .type(type)
                                                        .minFreq("0")
                                                        .build())
                                            .build());
                }
            }
            return Pair.of("", result);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    // This method is used to retrieve the rateCard info from Azure API.
    private String queryRateCard(Infrastructure infra, String accessToken) throws IOException {
        String endpoint = String.format("https://management.azure.com/subscriptions/%s/providers/Microsoft.Commerce/RateCard?api-version=%s&$filter=OfferDurableId eq '%s' and Currency eq '%s' and Locale eq '%s' and RegionInfo eq '%s'",
                                        infra.getCredentials().getSubscriptionId(),
                                        "2016-08-31-preview",
                                        this.CLOUD_OFFERS_PAYASYOUGO,
                                        this.CLOUD_OFFERS_CURRENCY,
                                        this.CLOUD_OFFERS_LOCAL,
                                        this.CLOUD_OFFERS_REGION_INFO)
                                .replaceAll(" ", "%20");
        HttpURLConnection conn = (HttpURLConnection) new URL(endpoint).openConnection();
        conn.setRequestMethod("GET");
        conn.addRequestProperty("Authorization", "Bearer " + accessToken);
        conn.addRequestProperty("Content-Type", "application/json");
        conn.connect();

        // getInputStream() works only if Http returns a code between 200 and 299
        BufferedReader reader = new BufferedReader(new InputStreamReader(conn.getResponseCode() / 100 == 2
                                                                                                           ? conn.getInputStream()
                                                                                                           : conn.getErrorStream(),
                                                                         "UTF-8"));

        StringBuilder builder = new StringBuilder();
        String line = null;
        while ((line = reader.readLine()) != null) {
            builder.append(line);
        }
        reader.close();
        return builder.toString();
    }

    // This method download the rateCard of the Azure subscription.
    private String getRateCard(Infrastructure infrastructure) throws IOException {
        Azure azureService = azureServiceCache.getService(infrastructure);
        String token = azureServiceCache.getInfrastructureToken(infrastructure);
        // Get a new rate card
        String queryResult = queryRateCard(infrastructure, token);
        JSONObject parsedQueryResult = new JSONObject(queryResult);
        if (parsedQueryResult.keySet().contains("Meters")) {
            return queryResult;
        } else {
            throw new RuntimeException("Unable to parse ratecard: " + queryResult);
        }
    }

    // This method parses the content of the VmRateCard answer.
    private Map<String, AzureKnownCost> parseVmRateCard(String queryResult) {
        Map<String, AzureKnownCost> result = new HashMap<>();
        JSONObject parsedQueryResult = new JSONObject(queryResult);
        Optional<JSONArray> meters = Optional.ofNullable(parsedQueryResult.optJSONArray("Meters"));
        String vmCatergory = "Virtual Machines";
        if (meters.isPresent()) {
            for (Object meterObj : meters.get()) {
                JSONObject meter = (JSONObject) meterObj;
                if (!vmCatergory.equals(meter.optString("MeterCategory"))) {
                    // If we are analyzing the price for a resource that is not a VM, we skip it.
                    continue;
                }
                JSONObject meterRates = meter.getJSONObject("MeterRates");
                result.put(meter.getString("MeterId"),
                           new AzureKnownCost(meter.getString("MeterRegion"),
                                              meter.getString("MeterCategory"),
                                              meterRates.getDouble("0")));
            }
        } else {
            throw new RuntimeException("Unable to find VmRateCard from Azure API");
        }
        return result;
    }

    private static String unsupportedOperatingSystemError(String operatingSystem) {
        return "ERROR Operating System of type '" + operatingSystem + "' is not yet supported";
    }
}

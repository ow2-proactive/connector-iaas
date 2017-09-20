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
package org.ow2.proactive.connector.iaas.cloud.provider.azure.scaleset;

import java.util.*;
import java.util.List;
import java.util.stream.Collectors;

import org.apache.log4j.Logger;
import org.ow2.proactive.connector.iaas.cloud.provider.azure.AzureProvider;
import org.ow2.proactive.connector.iaas.model.*;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import com.microsoft.azure.management.Azure;
import com.microsoft.azure.management.compute.*;
import com.microsoft.azure.management.network.*;
import com.microsoft.azure.management.network.Network;
import com.microsoft.azure.management.resources.ResourceGroup;
import com.microsoft.azure.management.resources.fluentcore.arm.Region;

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
public class AzureScaleSetProvider extends AzureProvider {

    /*
     * Assumptions for the following code:
     *
     * 1 NodeSource (infra) <-> 1 scale set </-> 1 resource group
     *
     */

    @Getter
    private final String type = "azureScaleSet";

    private static final Logger logger = Logger.getLogger(AzureScaleSetProvider.class);

    private int azureResourcesUUID;

    private String azureLoadBalancerName;

    private String azureFrontendName;

    private String azureBackendPoolName;

    private String azureTCPProbeName;

    private String azureHTTPLoadBalancingRule;

    private String azureNATPoolSSHName;

    private String azureNATPoolPNPName;

    private String azureNATPoolPNPSName;

    private String azureVNetName;

    private String azureSubnetName;

    private String azureIPName;

    private String azureScaleSetName;

    private int azureVMDiskSize;

    @Value("${connector-iaas.azure.vmss.default-private-network-cidr:172.16.0.0/16}")
    protected String vmssNetAddressSpace;

    @Value("${connector-iaas.azure.vmss.default-private-network-prefix:172.16.1.0/24}")
    protected String vmssNetAddressPrefix;

    @Override
    public Set<Instance> createInstance(Infrastructure infrastructure, Instance instance) {

        // Initialize Azure service connector
        Azure azureService = azureServiceCache.getService(infrastructure);

        // Retrieve options and prepare Azure resources creation
        Optional<Options> options = Optional.ofNullable(instance.getOptions());
        genAzureResourcesNames(infrastructure);

        logger.info("Starting creation of Azure Scale set '" + azureScaleSetName + "'.");

        // Retrieve Linux image to be used (but have to comply with the Azure VMSS policy and supported images)
        // see selectLinuxImage() below.
        String imageNameOrId = Optional.ofNullable(instance.getImage())
                                       .orElseThrow(() -> new RuntimeException("ERROR missing Image name/id from instance: '" +
                                                                               instance + "'"));

        // Retrieve resource group
        String rgName = options.map(Options::getResourceGroup)
                               .orElseThrow(() -> new RuntimeException("ERROR unable to find a suitable resourceGroup from instance: '" +
                                                                       instance + "'"));
        ResourceGroup resourceGroup = azureProviderUtils.searchResourceGroupByName(azureService, rgName)
                                                        .orElseThrow(() -> new RuntimeException("ERROR unable to find a suitable resourceGroup from instance: '" +
                                                                                                instance + "'"));

        // Try to get region from provided name, otherwise get it from image
        Region region = options.map(presentOptions -> Region.findByLabelOrName(presentOptions.getRegion()))
                               .orElseGet(resourceGroup::region);

        // Get existing virtual private network if specified
        Optional<Network> optionalVirtualNetwork = options.map(Options::getSubnetId)
                                                          .map(subnetId -> azureProviderUtils.searchVirtualNetworkByName(azureService,
                                                                                                                         subnetId)
                                                                                             .get());

        // Get VM admin user credentials
        String vmAdminUsername = Optional.ofNullable(instance.getCredentials())
                                         .map(InstanceCredentials::getUsername)
                                         .orElse(defaultUsername);
        String vmAdminPassword = Optional.ofNullable(instance.getCredentials())
                                         .map(InstanceCredentials::getPassword)
                                         .orElse(defaultPassword);
        Optional<String> vmAdminSSHPubKey = Optional.ofNullable(instance.getCredentials())
                                                    .map(InstanceCredentials::getPublicKey);

        // Retrieve number of instances within the Scale Set
        int vmssNbOfInstances = Integer.valueOf(Optional.ofNullable(instance.getNumber())
                                                        .orElse(SINGLE_INSTANCE_NUMBER));

        // Retrieve the customScript URL provided by the node source or throw an Exception otherwise.
        String customScriptUrl = Optional.ofNullable(instance.getCustomScriptUrl())
                                         .orElseThrow(() -> new RuntimeException("ERROR missing customScript URL."));
        final String scriptName = customScriptUrl.substring(customScriptUrl.lastIndexOf('/') + 1,
                                                            customScriptUrl.length());
        final String installCommand = "bash " + scriptName;
        List<String> fileUris = new ArrayList<>();
        fileUris.add(customScriptUrl);

        // Retrieve the provided VNET or create a new one
        Network network = optionalVirtualNetwork.orElse(azureService.networks()
                                                                    .define(azureVNetName)
                                                                    .withRegion(region)
                                                                    .withExistingResourceGroup(resourceGroup)
                                                                    .withAddressSpace(vmssNetAddressSpace)
                                                                    .defineSubnet(azureSubnetName)
                                                                    .withAddressPrefix(vmssNetAddressPrefix)
                                                                    .attach()
                                                                    .create());

        // Retrieve the provided public IP address or create a new one
        PublicIpAddress publicIPAddress = options.map(Options::getPublicIpAddress)
                                                 .map(publicIpAddresses -> azureProviderUtils.searchPublicIpAddressByIp(azureService,
                                                                                                                        publicIpAddresses)
                                                                                             .get())
                                                 .orElse(azureService.publicIpAddresses()
                                                                     .define(azureIPName)
                                                                     .withRegion(region)
                                                                     .withExistingResourceGroup(resourceGroup)
                                                                     .withLeafDomainLabel(azureIPName)
                                                                     .create());

        // Create a dedicated LB with the required rules
        LoadBalancer lb = createLoadBalancer(azureService, region, resourceGroup, publicIPAddress);

        // Create the Scale Set (multi-stages)
        VirtualMachineScaleSet virtualMachineScaleSet = createVMSS(azureService,
                                                                   region,
                                                                   resourceGroup,
                                                                   instance,
                                                                   network,
                                                                   lb,
                                                                   imageNameOrId,
                                                                   vmAdminUsername,
                                                                   vmAdminSSHPubKey,
                                                                   vmAdminPassword,
                                                                   vmssNbOfInstances,
                                                                   fileUris,
                                                                   installCommand);

        logger.info("Azure Scale set '" + azureScaleSetName + "'" + " created inside resource group " + resourceGroup);

        // Return the list of VMs of the Scale Set
        return virtualMachineScaleSet.virtualMachines()
                                     .list()
                                     .stream()
                                     .map(vm -> instance.withTag(vm.name())
                                                        .withId(vm.id())
                                                        .withNumber(SINGLE_INSTANCE_NUMBER))
                                     .collect(Collectors.toSet());
    }

    private VirtualMachineScaleSet createVMSS(Azure azureService, Region region, ResourceGroup resourceGroup,
            Instance instance, Network network, LoadBalancer lb, String imageNameOrId, String vmAdminUsername,
            Optional<String> vmAdminSSHPubKey, String vmAdminPassword, int vmssNbOfInstances, List<String> fileUris,
            String installCommand) {

        VirtualMachineScaleSet.DefinitionStages.WithLinuxRootPasswordOrPublicKeyManagedOrUnmanaged virtualMachineScaleSetStage1 = azureService.virtualMachineScaleSets()
                                                                                                                                              .define(azureScaleSetName)

                                                                                                                                              .withRegion(region)
                                                                                                                                              .withExistingResourceGroup(resourceGroup)
                                                                                                                                              .withSku(selectScaleSetSku(instance.getHardware()
                                                                                                                                                                                 .getType()))
                                                                                                                                              .withExistingPrimaryNetworkSubnet(network,
                                                                                                                                                                                azureSubnetName)
                                                                                                                                              .withExistingPrimaryInternetFacingLoadBalancer(lb)
                                                                                                                                              .withPrimaryInternetFacingLoadBalancerBackends(azureBackendPoolName)
                                                                                                                                              .withPrimaryInternetFacingLoadBalancerInboundNatPools(azureNATPoolSSHName,
                                                                                                                                                                                                    azureNATPoolPNPName,
                                                                                                                                                                                                    azureNATPoolPNPSName)
                                                                                                                                              .withoutPrimaryInternalLoadBalancer()
                                                                                                                                              .withPopularLinuxImage(selectScaleSetLinuxImage(imageNameOrId))
                                                                                                                                              .withRootUsername(vmAdminUsername);
        VirtualMachineScaleSet.DefinitionStages.WithLinuxCreateManagedOrUnmanaged virtualMachineScaleSetStage2;

        virtualMachineScaleSetStage2 = vmAdminSSHPubKey.map(virtualMachineScaleSetStage1::withSsh)
                                                       .orElseGet(() -> virtualMachineScaleSetStage1.withRootPassword(vmAdminPassword));

        return virtualMachineScaleSetStage2

                                           .withNewDataDisk(azureVMDiskSize)
                                           .withCapacity(vmssNbOfInstances)

                                           .defineNewExtension("CustomScriptForLinux")
                                           .withPublisher("Microsoft.OSTCExtensions")
                                           .withType("CustomScriptForLinux")
                                           .withVersion("1.4")
                                           .withMinorVersionAutoUpgrade()
                                           .withPublicSetting("fileUris", fileUris)
                                           .withPublicSetting("commandToExecute", installCommand)

                                           .attach()
                                           .create();
    }

    private void genAzureResourcesNames(Infrastructure infrastructure) {
        azureResourcesUUID = Math.abs(infrastructure.hashCode()); // Make it distinguable and findable (OK since 1 NS <-> 1 Scale Set)
        azureLoadBalancerName = "LB-" + azureResourcesUUID;
        azureFrontendName = "FE-" + azureResourcesUUID;
        azureBackendPoolName = "BE-SSH-" + azureResourcesUUID;
        azureTCPProbeName = "PTCP-" + azureResourcesUUID; // useless but required
        azureHTTPLoadBalancingRule = "RHTTP-" + azureResourcesUUID; // useless but required
        azureNATPoolSSHName = "NATSSH-" + azureResourcesUUID;
        azureNATPoolPNPName = "NATPNP-" + azureResourcesUUID;
        azureNATPoolPNPSName = "NATPNPS-" + azureResourcesUUID;
        azureVNetName = "VNET-" + azureResourcesUUID;
        azureSubnetName = "Front-end";
        azureIPName = "IP-" + azureResourcesUUID;
        azureScaleSetName = "aeSS-" + azureResourcesUUID;
        azureVMDiskSize = 3;
    }

    private LoadBalancer createLoadBalancer(Azure azureService, Region region, ResourceGroup resourceGroup,
            PublicIpAddress publicIPAddress) {
        return azureService.loadBalancers()
                           .define(azureLoadBalancerName)
                           .withRegion(region)
                           .withExistingResourceGroup(resourceGroup)
                           // assign a public IP address to the load balancer
                           .definePublicFrontend(azureFrontendName)
                           .withExistingPublicIpAddress(publicIPAddress)
                           .attach()
                           // Add one backend address pools
                           .defineBackend(azureBackendPoolName)
                           .attach()
                           // Define mock TCP probe and HTTP rule
                           .defineTcpProbe(azureTCPProbeName)
                           .withPort(22)
                           .withNumberOfProbes(1)
                           .attach()
                           .defineLoadBalancingRule(azureHTTPLoadBalancingRule)
                           .withProtocol(TransportProtocol.TCP)
                           .withFrontend(azureFrontendName)
                           .withFrontendPort(81)
                           .withProbe(azureTCPProbeName)
                           .withBackend(azureBackendPoolName)
                           .attach()
                           // Define NAT pools for SSH, PNP and PNPS
                           .defineInboundNatPool(azureNATPoolSSHName)
                           .withProtocol(TransportProtocol.TCP)
                           .withFrontend(azureFrontendName)
                           .withFrontendPortRange(50000, 50099)
                           .withBackendPort(22) // SSH
                           .attach()

                           .defineInboundNatPool(azureNATPoolPNPName)
                           .withProtocol(TransportProtocol.TCP)
                           .withFrontend(azureFrontendName)
                           .withFrontendPortRange(30000, 30099)
                           .withBackendPort(64738) // PNP
                           .attach()
                           .defineInboundNatPool(azureNATPoolPNPSName)
                           .withProtocol(TransportProtocol.TCP)
                           .withFrontend(azureFrontendName)
                           .withFrontendPortRange(40000, 40099)
                           .withBackendPort(64739) // PNPS
                           .attach()
                           .create();
    }

    private VirtualMachineScaleSetSkuTypes selectScaleSetSku(String instanceType) {
        if (instanceType == null)
            return VirtualMachineScaleSetSkuTypes.STANDARD_D1_V2;
        if (instanceType.contains("STANDARD_D1_V2"))
            return VirtualMachineScaleSetSkuTypes.STANDARD_D1_V2;
        //TODO: add support for more SKU
        return VirtualMachineScaleSetSkuTypes.STANDARD_D1_V2;
    }

    private KnownLinuxVirtualMachineImage selectScaleSetLinuxImage(String imageNameOrId) {
        if (imageNameOrId == null)
            return KnownLinuxVirtualMachineImage.UBUNTU_SERVER_16_04_LTS;
        if (imageNameOrId.contains("Debian"))
            return KnownLinuxVirtualMachineImage.DEBIAN_8;
        if (imageNameOrId.contains("CentOS"))
            return KnownLinuxVirtualMachineImage.CENTOS_7_2;
        if (imageNameOrId.contains("Ubuntu"))
            return KnownLinuxVirtualMachineImage.UBUNTU_SERVER_16_04_LTS;
        return KnownLinuxVirtualMachineImage.UBUNTU_SERVER_16_04_LTS; // default
    }

    @Override
    public void deleteInstance(Infrastructure infrastructure, String instanceId) {
        Azure azureService = azureServiceCache.getService(infrastructure);

        azureResourcesUUID = Math.abs(infrastructure.hashCode()); // Make it distinguable and findable (OK since 1 NS <-> 1 Scale Set)
        azureLoadBalancerName = "LB-" + azureResourcesUUID;
        azureVNetName = "VNET-" + azureResourcesUUID;
        azureIPName = "IP-" + azureResourcesUUID;
        azureScaleSetName = "aeSS-" + azureResourcesUUID;

        VirtualMachineScaleSet vmss = azureProviderUtils.searchVirtualMachineScaleSetByName(azureService,
                                                                                            azureScaleSetName)
                                                        .orElseThrow(() -> new RuntimeException("Could not find the VMSS to be removed."));
        azureService.virtualMachineScaleSets().deleteById(vmss.id());

        LoadBalancer lb = azureProviderUtils.searchLoadBalancerByName(azureService, azureLoadBalancerName)
                                            .orElseThrow(() -> new RuntimeException("Could not find the LB to be removed."));
        azureService.loadBalancers().deleteById(lb.id());

        Optional<Network> net = azureProviderUtils.searchVirtualNetworkByName(azureService, azureVNetName);
        if (net.isPresent())
            azureService.networks().deleteById(net.get().id());

        Optional<PublicIpAddress> ip = azureProviderUtils.searchPublicIpAddressByName(azureService, azureIPName);
        if (ip.isPresent())
            azureService.publicIpAddresses().deleteById(ip.get().id());
    }
}

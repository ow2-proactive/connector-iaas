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

import static org.hamcrest.CoreMatchers.*;
import static org.junit.Assert.assertThat;
import static org.mockito.ArgumentMatchers.nullable;
import static org.mockito.Matchers.any;
import static org.mockito.Matchers.anyBoolean;
import static org.mockito.Matchers.anyString;
import static org.mockito.Mockito.*;

import java.io.IOException;
import java.rmi.RemoteException;
import java.util.*;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

import org.junit.Before;
import org.junit.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.ow2.proactive.connector.iaas.cloud.TagManager;
import org.ow2.proactive.connector.iaas.fixtures.InfrastructureFixture;
import org.ow2.proactive.connector.iaas.fixtures.InstanceFixture;
import org.ow2.proactive.connector.iaas.fixtures.InstanceScriptFixture;
import org.ow2.proactive.connector.iaas.model.*;
import org.springframework.test.util.ReflectionTestUtils;

import com.google.common.collect.Lists;
import com.google.common.collect.Sets;
import com.microsoft.azure.Page;
import com.microsoft.azure.PagedList;
import com.microsoft.azure.management.Azure;
import com.microsoft.azure.management.compute.*;
import com.microsoft.azure.management.network.*;
import com.microsoft.azure.management.network.Network;
import com.microsoft.azure.management.resources.ResourceGroup;
import com.microsoft.azure.management.resources.ResourceGroups;
import com.microsoft.azure.management.resources.fluentcore.arm.Region;
import com.microsoft.azure.management.resources.fluentcore.model.Creatable;
import com.microsoft.azure.management.resources.fluentcore.model.CreatedResources;
import com.microsoft.rest.RestException;
import com.microsoft.rest.ServiceFuture;


/**
 * @author ActiveEon Team
 * @since 12/03/17
 */
public class AzureProviderTest {

    @InjectMocks
    private AzureProvider azureProvider;

    @Mock
    private AzureServiceCache azureServiceCache;

    @Mock
    private AzureProviderUtils azureProviderUtils;

    @Mock
    private AzureProviderNetworkingUtils azureProviderNetworkingUtils;

    @Mock
    private Azure azureService;

    @Mock
    private ResourceGroup resourceGroup;

    @Mock
    private ResourceGroups resourceGroups;

    @Mock
    private VirtualMachine.DefinitionStages.WithLinuxRootPasswordOrPublicKeyManaged creatableLinuxVMWithoutCredentials;

    @Mock
    private VirtualMachine.DefinitionStages.WithWindowsAdminPasswordManaged creatableWindowsVMWithoutCredentials;

    @Mock
    private VirtualMachine.DefinitionStages.WithLinuxCreateManaged creatableLinuxVMWithImage;

    @Mock
    private VirtualMachine.DefinitionStages.WithWindowsCreateManaged creatableWindowsVMWithImage;

    @Mock
    private VirtualMachine.DefinitionStages.WithCreate creatableVMWithSize;

    @Mock
    private VirtualMachine.DefinitionStages.Blank defineVirtualMachine;

    @Mock
    private VirtualMachine.DefinitionStages.WithGroup virtualMachineWithGroup;

    @Mock
    private VirtualMachine.DefinitionStages.WithNetwork virtualMachineWithNetwork;

    @Mock
    private VirtualMachine.DefinitionStages.WithProximityPlacementGroup virtualMachineWithPlacementGroup;

    @Mock
    private VirtualMachine.DefinitionStages.WithLinuxRootUsernameManaged virtualMachineWithLinuxRootUsername;

    @Mock
    private VirtualMachine.DefinitionStages.WithWindowsAdminUsernameManaged virtualMachineWithWindowsAdminUsername;

    @Mock
    private VirtualMachineExtension.DefinitionStages.Blank<VirtualMachine.DefinitionStages.WithCreate> virtualMachineCreateExtension;

    @Mock
    private VirtualMachineExtension.DefinitionStages.WithType<VirtualMachine.DefinitionStages.WithCreate> virtualMachineCreateExtensionWithType;

    @Mock
    private VirtualMachineExtension.DefinitionStages.WithVersion<VirtualMachine.DefinitionStages.WithCreate> virtualMachineCreateExtensionWithVersion;

    @Mock
    private VirtualMachineExtension.DefinitionStages.WithAttach<VirtualMachine.DefinitionStages.WithCreate> virtualMachineCreateExtensionWithAttach;

    @Mock
    private VirtualMachine.Update virtualMachineUpdate;

    @Mock
    private ServiceFuture<VirtualMachine> virtualMachineUpdateFuture;

    @Mock
    private VirtualMachineExtension.UpdateDefinitionStages.Blank<VirtualMachine.Update> virtualMachineUpdateBlank;

    @Mock
    private VirtualMachineExtension.UpdateDefinitionStages.WithType<VirtualMachine.Update> virtualMachineUpdateWithType;

    @Mock
    private VirtualMachineExtension.UpdateDefinitionStages.WithVersion<VirtualMachine.Update> virtualMachineUpdateWithVersion;

    @Mock
    private VirtualMachineExtension.UpdateDefinitionStages.WithAttach<VirtualMachine.Update> virtualMachineUpdateWithAttach;

    @Mock
    private VirtualMachine virtualMachine, virtualMachine2;

    @Mock
    private VirtualMachines virtualMachines;

    @Mock
    private CreatedResources<VirtualMachine> virtualMachineCreatedResources;

    @Mock
    private Creatable<Network> creatableVirtualNetwork;

    @Mock
    private Network virtualNetwork;

    @Mock
    private Networks virtualNetworks;

    @Mock
    private Creatable<NetworkSecurityGroup> creatableNetworkSecurityGroup;

    @Mock
    private NetworkSecurityGroup networkSecurityGroup;

    @Mock
    private NetworkSecurityGroups networkSecurityGroups;

    @Mock
    private Creatable<PublicIPAddress> creatablePublicIPAddress;

    @Mock
    private PublicIPAddress publicIPAddress;

    @Mock
    private PublicIPAddresses publicIPAddresses;

    @Mock
    private NicIPConfiguration nicIPConfiguration;

    @Mock
    private ImageOSDisk imageOSDisk;

    @Mock
    private VirtualMachineCustomImage virtualMachineCustomImage;

    @Mock
    private VirtualMachineCustomImages virtualMachineCustomImages;

    @Mock
    private Map<Integer, VirtualMachineDataDisk> dataDisksMap;

    @Mock
    private Collection<VirtualMachineDataDisk> dataDisks;

    @Mock
    private Disks disks;

    @Mock
    private Creatable<NetworkInterface> creatableNetworkInterface;

    @Mock
    private NetworkInterface networkInterface;

    @Mock
    private NetworkInterface secondaryNetworkInterface;

    @Mock
    private NetworkInterface.Update networkInterfaceUpdate;

    @Mock
    private NetworkInterfaces networkInterfaces;

    @Mock
    private NetworkInterface.DefinitionStages.Blank defineNetworkInterface;

    @Mock
    private NetworkInterface.DefinitionStages.WithGroup networkInterfaceWithRegion;

    @Mock
    private NetworkInterface.DefinitionStages.WithPrimaryNetwork networkInterfaceWithResourceGroup;

    @Mock
    private NetworkInterface.DefinitionStages.WithPrimaryPrivateIP networkInterfaceWithPrimaryPrivateIP;

    @Mock
    private NetworkInterface.DefinitionStages.WithCreate networkInterfaceWithCreate;

    @Mock
    private VirtualMachineExtension virtualMachineExtension;

    @Mock
    private VirtualMachineExtension.Update virtualMachineExtensionUpdate;

    @Mock
    private Map<String, VirtualMachineExtension> virtualMachineExtensionsMap;

    @Mock
    private TagManager tagManager;

    private Tag connectorIaasTag = Tag.builder().key("connector-iaas-tag-key").value("default-value").build();

    @Before
    public void init() throws Exception {
        MockitoAnnotations.initMocks(this);
        when(azureServiceCache.getService(any(Infrastructure.class))).thenReturn(azureService);
        ReflectionTestUtils.setField(azureProvider, "defaultUsername", "activeeon", String.class);
        ReflectionTestUtils.setField(azureProvider, "defaultPassword", "Act1v€0N", String.class);
        ReflectionTestUtils.setField(azureProvider, "defaultPrivateNetworkCidr", "10.0.0.0/24", String.class);

    }

    @Test
    public void testCreateInstance() throws RemoteException, InterruptedException {

        when(virtualMachine.name()).thenReturn("vmTag");
        when(virtualMachine.vmId()).thenReturn("vmId");
        when(resourceGroup.name()).thenReturn("resourceGroup");
        when(virtualMachineCustomImage.resourceGroupName()).thenReturn("resourceGroup");
        when(virtualMachineCustomImage.region()).thenReturn(Region.US_EAST);
        when(virtualMachineCustomImage.name()).thenReturn("imageName");
        when(virtualMachineCustomImage.id()).thenReturn("imageId");
        when(virtualMachineCustomImage.osDiskImage()).thenReturn(imageOSDisk);
        when(imageOSDisk.osType()).thenReturn(OperatingSystemTypes.LINUX);

        // Utils methods
        when(azureProviderUtils.searchVirtualMachineByName(azureService,
                                                           "vmTag")).thenReturn(Optional.of(virtualMachine));
        when(azureProviderNetworkingUtils.prepareVirtualNetwork(any(Azure.class),
                                                                any(Region.class),
                                                                any(ResourceGroup.class),
                                                                anyString(),
                                                                anyString())).thenReturn(creatableVirtualNetwork);
        when(azureProviderNetworkingUtils.preparePublicIPAddress(any(Azure.class),
                                                                 any(Region.class),
                                                                 any(ResourceGroup.class),
                                                                 anyString(),
                                                                 anyBoolean())).thenReturn(creatablePublicIPAddress);
        when(azureProviderNetworkingUtils.prepareNetworkInterface(any(Azure.class),
                                                                  any(Region.class),
                                                                  any(ResourceGroup.class),
                                                                  anyString(),
                                                                  any(Creatable.class),
                                                                  nullable(Network.class),
                                                                  any(Creatable.class),
                                                                  nullable(NetworkSecurityGroup.class),
                                                                  any(Creatable.class),
                                                                  nullable(PublicIPAddress.class))).thenReturn(creatableNetworkInterface);
        when(azureProviderNetworkingUtils.prepareProactiveNetworkSecurityGroup(any(Azure.class),
                                                                               any(Region.class),
                                                                               any(ResourceGroup.class),
                                                                               anyString())).thenReturn(creatableNetworkSecurityGroup);
        when(azureProviderUtils.searchResourceGroupByName(azureService,
                                                          "resourceGroup")).thenReturn(Optional.of(resourceGroup));
        when(azureProviderUtils.searchNetworkSecurityGroupByName(any(Azure.class),
                                                                 anyString(),
                                                                 anyString())).thenReturn(Optional.of(networkSecurityGroup));

        // Images
        PagedList<VirtualMachineCustomImage> pagedListCustomImage = getPagedList();
        pagedListCustomImage.add(virtualMachineCustomImage);
        when(virtualMachineCustomImages.list()).thenReturn(pagedListCustomImage);
        when(azureService.virtualMachineCustomImages()).thenReturn(virtualMachineCustomImages);
        // Consider that the imageName is matching with ID to cover the whole lambda behaviour
        when(virtualMachineCustomImages.getById("imageName")).thenReturn(virtualMachineCustomImage);
        when(virtualMachineCustomImage.resourceGroupName()).thenReturn("resourceGroup");

        // VirtualMachines
        PagedList<VirtualMachine> pagedListVirtualMachine = getPagedList();
        pagedListVirtualMachine.add(virtualMachine);
        when(virtualMachines.list()).thenReturn(pagedListVirtualMachine);
        when(azureService.virtualMachines()).thenReturn(virtualMachines);

        // ResourceGroups
        PagedList<ResourceGroup> pagedListResourceGroup = getPagedList();
        pagedListResourceGroup.add(resourceGroup);
        when(resourceGroups.list()).thenReturn(pagedListResourceGroup);
        when(azureService.resourceGroups()).thenReturn(resourceGroups);
        when(resourceGroups.getByName("resourceGroup")).thenReturn(resourceGroup);

        // Prepare Linux VirtualMachine
        when(virtualMachines.define(anyString())).thenReturn(defineVirtualMachine);
        when(defineVirtualMachine.withRegion(any(Region.class))).thenReturn(virtualMachineWithGroup);
        when(virtualMachineWithGroup.withExistingResourceGroup(resourceGroup)).thenReturn(virtualMachineWithNetwork);
        when(virtualMachineWithNetwork.withNewPrimaryNetworkInterface(creatableNetworkInterface)).thenReturn(virtualMachineWithPlacementGroup);
        when(virtualMachineWithPlacementGroup.withLinuxCustomImage(virtualMachineCustomImage.id())).thenReturn(virtualMachineWithLinuxRootUsername);
        when(virtualMachineWithLinuxRootUsername.withRootUsername(anyString())).thenReturn(creatableLinuxVMWithoutCredentials);
        when(creatableLinuxVMWithoutCredentials.withRootPassword(anyString())).thenReturn(creatableLinuxVMWithImage);
        when(creatableLinuxVMWithoutCredentials.withSsh(anyString())).thenReturn(creatableLinuxVMWithImage);
        when(creatableLinuxVMWithImage.withSize(any(VirtualMachineSizeTypes.class))).thenReturn(creatableVMWithSize);
        when(creatableVMWithSize.withOSDiskName(anyString())).thenReturn(creatableVMWithSize);

        // Add extension for initScripts
        when(creatableVMWithSize.defineNewExtension(anyString())).thenReturn(virtualMachineCreateExtension);
        when(virtualMachineCreateExtension.withPublisher(anyString())).thenReturn(virtualMachineCreateExtensionWithType);
        when(virtualMachineCreateExtensionWithType.withType(anyString())).thenReturn(virtualMachineCreateExtensionWithVersion);
        when(virtualMachineCreateExtensionWithVersion.withVersion(anyString())).thenReturn(virtualMachineCreateExtensionWithAttach);
        when(virtualMachineCreateExtensionWithAttach.withMinorVersionAutoUpgrade()).thenReturn(virtualMachineCreateExtensionWithAttach);
        when(virtualMachineCreateExtensionWithAttach.withPublicSetting(anyString(),
                                                                       anyString())).thenReturn(virtualMachineCreateExtensionWithAttach);
        when(virtualMachineCreateExtensionWithAttach.attach()).thenReturn(creatableVMWithSize);

        // Create VMs
        Collection<VirtualMachine> createdVirtualMachines = Lists.newArrayList(virtualMachine);
        when(virtualMachines.create(any(List.class))).thenReturn(virtualMachineCreatedResources);
        when(virtualMachineCreatedResources.values()).thenReturn(createdVirtualMachines);

        // Tags
        when(tagManager.retrieveAllTags(anyString(),
                                        any(Options.class))).thenReturn(Lists.newArrayList(connectorIaasTag));

        // Tests
        Infrastructure infrastructure;
        Instance instance;
        List<Instance> createdInstances;

        infrastructure = InfrastructureFixture.getAzureInfrastructure("id-azure",
                                                                      "azure",
                                                                      "clientId",
                                                                      "secret",
                                                                      "domain",
                                                                      "subscriptionId");

        // Simple instance creation
        instance = InstanceFixture.simpleInstanceWithTagAndImage("vmTag", "imageName");
        createdInstances = new ArrayList<Instance>(azureProvider.createInstance(infrastructure, instance));
        assertThat(createdInstances.size(), is(1));
        assertThat(createdInstances.get(0).getId(), is("vmId"));
        assertThat(createdInstances.get(0).getTag(), is("vmTag"));
        assertThat(createdInstances.get(0).getImage(), is("imageName"));

        // Instance with public key
        instance = InstanceFixture.simpleInstanceWithPublicKey("vmTag", "imageName", "ssh-public-key");
        createdInstances = new ArrayList<Instance>(azureProvider.createInstance(infrastructure, instance));
        assertThat(createdInstances.size(), is(1));
        assertThat(createdInstances.get(0).getCredentials().getPublicKey(), is("ssh-public-key"));

        // Instance with initScript
        instance = InstanceFixture.simpleInstanceWithInitScripts("vmTag", "imageName", new String[] { "id", "pwd" });
        createdInstances = new ArrayList<Instance>(azureProvider.createInstance(infrastructure, instance));
        assertThat(createdInstances.size(), is(1));
        assertThat(createdInstances.get(0).getInitScript().getScripts().length, is(2));

        // Instance with resourceGroup and region
        instance = InstanceFixture.getInstanceWithResourceGroupAndRegion("vmId",
                                                                         "vmTag",
                                                                         "imageName",
                                                                         "1",
                                                                         null,
                                                                         null,
                                                                         null,
                                                                         null,
                                                                         null,
                                                                         "resourceGroup",
                                                                         "eastus");
        createdInstances = new ArrayList<Instance>(azureProvider.createInstance(infrastructure, instance));
        assertThat(createdInstances.size(), is(1));
        assertThat(createdInstances.get(0).getOptions().getResourceGroup(), is("resourceGroup"));
        assertThat(createdInstances.get(0).getOptions().getRegion(), is("eastus"));

        // Instance with security group
        instance = InstanceFixture.getInstanceWithSecurityGroup("vmId",
                                                                "vmTag",
                                                                "imageName",
                                                                "1",
                                                                null,
                                                                null,
                                                                null,
                                                                null,
                                                                null,
                                                                "securityGroup");
        createdInstances = new ArrayList<Instance>(azureProvider.createInstance(infrastructure, instance));
        assertThat(createdInstances.size(), is(1));
        assertThat(createdInstances.get(0).getOptions().getSecurityGroupNames().get(0), is("securityGroup"));
    }

    @Test
    public void testCreateWindowsInstances() throws RemoteException, InterruptedException {

        when(virtualMachine.name()).thenReturn("vmTag");
        when(virtualMachine.vmId()).thenReturn("vmId");
        when(virtualMachine2.name()).thenReturn("vmTag2");
        when(virtualMachine2.vmId()).thenReturn("vmId2");
        when(resourceGroup.name()).thenReturn("resourceGroup");
        when(virtualMachineCustomImage.resourceGroupName()).thenReturn("resourceGroup");
        when(virtualMachineCustomImage.region()).thenReturn(Region.US_EAST);
        when(virtualMachineCustomImage.name()).thenReturn("imageName");
        when(virtualMachineCustomImage.id()).thenReturn("imageId");
        when(virtualMachineCustomImage.osDiskImage()).thenReturn(imageOSDisk);
        when(imageOSDisk.osType()).thenReturn(OperatingSystemTypes.WINDOWS);

        // Utils methods
        when(azureProviderUtils.searchVirtualMachineByName(azureService,
                                                           "vmTag")).thenReturn(Optional.of(virtualMachine));
        when(azureProviderUtils.searchVirtualMachineByName(azureService,
                                                           "vmTag2")).thenReturn(Optional.of(virtualMachine2));
        when(azureProviderNetworkingUtils.prepareVirtualNetwork(any(Azure.class),
                                                                any(Region.class),
                                                                any(ResourceGroup.class),
                                                                anyString(),
                                                                anyString())).thenReturn(creatableVirtualNetwork);
        when(azureProviderNetworkingUtils.preparePublicIPAddress(any(Azure.class),
                                                                 any(Region.class),
                                                                 any(ResourceGroup.class),
                                                                 anyString(),
                                                                 anyBoolean())).thenReturn(creatablePublicIPAddress);
        when(azureProviderNetworkingUtils.prepareNetworkInterface(any(Azure.class),
                                                                  any(Region.class),
                                                                  any(ResourceGroup.class),
                                                                  anyString(),
                                                                  any(Creatable.class),
                                                                  nullable(Network.class),
                                                                  any(Creatable.class),
                                                                  nullable(NetworkSecurityGroup.class),
                                                                  any(Creatable.class),
                                                                  nullable(PublicIPAddress.class))).thenReturn(creatableNetworkInterface);
        when(azureProviderNetworkingUtils.prepareProactiveNetworkSecurityGroup(any(Azure.class),
                                                                               any(Region.class),
                                                                               any(ResourceGroup.class),
                                                                               anyString())).thenReturn(creatableNetworkSecurityGroup);
        when(azureProviderUtils.searchResourceGroupByName(azureService,
                                                          "resourceGroup")).thenReturn(Optional.of(resourceGroup));

        // Images
        PagedList<VirtualMachineCustomImage> pagedListCustomImage = getPagedList();
        pagedListCustomImage.add(virtualMachineCustomImage);
        when(virtualMachineCustomImages.list()).thenReturn(pagedListCustomImage);
        when(azureService.virtualMachineCustomImages()).thenReturn(virtualMachineCustomImages);
        // Consider that the imageName is matching with ID to cover the whole lambda behaviour
        when(virtualMachineCustomImages.getById("imageName")).thenReturn(virtualMachineCustomImage);
        when(virtualMachineCustomImage.resourceGroupName()).thenReturn("resourceGroup");

        // VirtualMachines
        PagedList<VirtualMachine> pagedListVirtualMachine = getPagedList();
        pagedListVirtualMachine.add(virtualMachine);
        pagedListVirtualMachine.add(virtualMachine2);
        when(virtualMachines.list()).thenReturn(pagedListVirtualMachine);
        when(azureService.virtualMachines()).thenReturn(virtualMachines);

        // ResourceGroups
        PagedList<ResourceGroup> pagedListResourceGroup = getPagedList();
        pagedListResourceGroup.add(resourceGroup);
        when(resourceGroups.list()).thenReturn(pagedListResourceGroup);
        when(azureService.resourceGroups()).thenReturn(resourceGroups);
        when(resourceGroups.getByName("resourceGroup")).thenReturn(resourceGroup);

        // Prepare Windows VirtualMachine
        when(virtualMachines.define(anyString())).thenReturn(defineVirtualMachine);
        when(defineVirtualMachine.withRegion(any(Region.class))).thenReturn(virtualMachineWithGroup);
        when(virtualMachineWithGroup.withExistingResourceGroup(resourceGroup)).thenReturn(virtualMachineWithNetwork);
        when(virtualMachineWithNetwork.withNewPrimaryNetworkInterface(creatableNetworkInterface)).thenReturn(virtualMachineWithPlacementGroup);
        when(virtualMachineWithPlacementGroup.withWindowsCustomImage(virtualMachineCustomImage.id())).thenReturn(virtualMachineWithWindowsAdminUsername);
        when(virtualMachineWithWindowsAdminUsername.withAdminUsername(anyString())).thenReturn(creatableWindowsVMWithoutCredentials);
        when(creatableWindowsVMWithoutCredentials.withAdminPassword(anyString())).thenReturn(creatableWindowsVMWithImage);
        when(creatableWindowsVMWithImage.withSize(any(VirtualMachineSizeTypes.class))).thenReturn(creatableVMWithSize);
        when(creatableVMWithSize.withOSDiskName(anyString())).thenReturn(creatableVMWithSize);

        // Create VMs
        Collection<VirtualMachine> createdVirtualMachines = Lists.newArrayList(virtualMachine, virtualMachine2);
        when(virtualMachines.create(any(List.class))).thenReturn(virtualMachineCreatedResources);
        when(virtualMachineCreatedResources.values()).thenReturn(createdVirtualMachines);

        // Tags
        when(tagManager.retrieveAllTags(anyString(),
                                        any(Options.class))).thenReturn(Lists.newArrayList(connectorIaasTag));

        // Tests
        Infrastructure infrastructure;
        Instance instance;
        List<Instance> createdInstances;

        // Simple instance creation
        infrastructure = InfrastructureFixture.getAzureInfrastructure("id-azure",
                                                                      "azure",
                                                                      "clientId",
                                                                      "secret",
                                                                      "domain",
                                                                      "subscriptionId");
        instance = InstanceFixture.simpleInstanceWithTagAndImage("vmTag", "imageName");
        createdInstances = new ArrayList<Instance>(azureProvider.createInstance(infrastructure, instance));
        assertThat(createdInstances.size(), is(2));
        assertThat(createdInstances.get(0).getId(), anyOf(is("vmId"), is("vmId2")));
        assertThat(createdInstances.get(1).getId(), anyOf(is("vmId"), is("vmId2")));
        assertThat(createdInstances.get(0).getId(), is(not(createdInstances.get(1).getId())));
    }

    @Test
    public void testDeleteInstance() {
        Infrastructure infrastructure = InfrastructureFixture.getAzureInfrastructure("id-azure",
                                                                                     "azure",
                                                                                     "clientId",
                                                                                     "secret",
                                                                                     "domain",
                                                                                     "subscriptionId");

        when(azureProviderUtils.searchVirtualMachineByID(azureService, "vmId")).thenReturn(Optional.of(virtualMachine));
        when(virtualMachine.id()).thenReturn("vmId");
        when(virtualMachine.osDiskId()).thenReturn("diskId");
        when(publicIPAddress.id()).thenReturn("pubIP-id");
        when(networkInterface.id()).thenReturn("netIf-id");
        when(networkSecurityGroup.id()).thenReturn("nsg-id");
        when(virtualNetwork.id()).thenReturn("vnet-id");

        when(virtualMachine.getPrimaryNetworkInterface()).thenReturn(networkInterface);
        when(networkInterface.primaryIPConfiguration()).thenReturn(nicIPConfiguration);
        when(nicIPConfiguration.getNetwork()).thenReturn(virtualNetwork);
        when(networkInterface.getNetworkSecurityGroup()).thenReturn(networkSecurityGroup);
        when(virtualMachine.getPrimaryPublicIPAddress()).thenReturn(publicIPAddress);
        when(virtualMachine.dataDisks()).thenReturn(dataDisksMap);
        when(dataDisksMap.values()).thenReturn(dataDisks);

        // VirtualMachines
        PagedList<VirtualMachine> pagedListVirtualMachine = getPagedList();
        pagedListVirtualMachine.add(virtualMachine);
        when(virtualMachines.list()).thenReturn(pagedListVirtualMachine);
        when(azureService.virtualMachines()).thenReturn(virtualMachines);

        // NetworkInterfaces
        PagedList<NetworkInterface> pagedListNetworkInterface = getPagedList();
        when(networkInterfaces.list()).thenReturn(pagedListNetworkInterface);
        when(azureService.networkInterfaces()).thenReturn(networkInterfaces);
        when(networkInterfaces.getById("netIf-id")).thenReturn(networkInterface);
        when(virtualMachine.networkInterfaceIds()).thenReturn(Collections.singletonList("netIf-id"));

        // PublicIPAddresses
        when(azureService.publicIPAddresses()).thenReturn(publicIPAddresses);
        when(networkInterface.primaryIPConfiguration()).thenReturn(nicIPConfiguration);
        when(nicIPConfiguration.getPublicIPAddress()).thenReturn(publicIPAddress);
        when(azureProviderNetworkingUtils.getVMPublicIPAddresses(azureService,
                                                                 virtualMachine)).thenReturn(Lists.newArrayList(publicIPAddress));

        // Disks
        when(azureService.disks()).thenReturn(disks);

        // NetworkSecurityGroups
        when(azureService.networkSecurityGroups()).thenReturn(networkSecurityGroups);
        when(azureProviderNetworkingUtils.getVMSecurityGroups(azureService,
                                                              virtualMachine)).thenReturn(Lists.newArrayList(networkSecurityGroup));

        // Networks
        Map<String, NicIPConfiguration> mapIPConfiguration = new HashMap<>();
        mapIPConfiguration.put("ipConf", nicIPConfiguration);
        when(networkInterface.ipConfigurations()).thenReturn(mapIPConfiguration);
        when(nicIPConfiguration.getNetwork()).thenReturn(virtualNetwork);
        when(azureService.networks()).thenReturn(virtualNetworks);
        when(azureProviderNetworkingUtils.getVMNetworks(azureService,
                                                        virtualMachine)).thenReturn(Lists.newArrayList(virtualNetwork));

        // Trigger deleteInstance with full erasing
        azureProvider.deleteInstance(infrastructure, "vmId");
        verify(virtualMachines).deleteById("vmId");
        verify(networkInterfaces).deleteById("netIf-id");
        verify(publicIPAddresses).deleteById("pubIP-id");
        verify(disks).deleteById("diskId");
        verify(networkSecurityGroups).deleteById("nsg-id");
        verify(virtualNetworks).deleteById("vnet-id");
    }

    @Test
    public void testDeleteInstanceWithoutCommonResources() {
        Infrastructure infrastructure = InfrastructureFixture.getAzureInfrastructure("id-azure",
                                                                                     "azure",
                                                                                     "clientId",
                                                                                     "secret",
                                                                                     "domain",
                                                                                     "subscriptionId");

        when(azureProviderUtils.searchVirtualMachineByID(azureService, "vmId")).thenReturn(Optional.of(virtualMachine));
        when(virtualMachine.id()).thenReturn("vmId");
        when(virtualMachine.osDiskId()).thenReturn("diskId");
        when(publicIPAddress.id()).thenReturn("pubIP-id");
        when(networkInterface.id()).thenReturn("netIf-id");
        when(networkSecurityGroup.id()).thenReturn("nsg-id");
        when(virtualNetwork.id()).thenReturn("vnet-id");

        when(virtualMachine.getPrimaryNetworkInterface()).thenReturn(networkInterface);
        when(networkInterface.primaryIPConfiguration()).thenReturn(nicIPConfiguration);
        when(nicIPConfiguration.getNetwork()).thenReturn(virtualNetwork);
        when(networkInterface.getNetworkSecurityGroup()).thenReturn(networkSecurityGroup);
        when(virtualMachine.getPrimaryPublicIPAddress()).thenReturn(publicIPAddress);
        when(virtualMachine.dataDisks()).thenReturn(dataDisksMap);
        when(dataDisksMap.values()).thenReturn(dataDisks);

        // VirtualMachines
        PagedList<VirtualMachine> pagedListVirtualMachine = getPagedList();
        pagedListVirtualMachine.add(virtualMachine);
        pagedListVirtualMachine.add(virtualMachine2);
        when(virtualMachines.list()).thenReturn(pagedListVirtualMachine);
        when(azureService.virtualMachines()).thenReturn(virtualMachines);

        // NetworkInterfaces
        PagedList<NetworkInterface> pagedListNetworkInterface = getPagedList();
        pagedListNetworkInterface.add(networkInterface);
        pagedListNetworkInterface.add(secondaryNetworkInterface);
        when(networkInterfaces.list()).thenReturn(pagedListNetworkInterface);
        when(azureService.networkInterfaces()).thenReturn(networkInterfaces);
        when(networkInterfaces.getById("netIf-id")).thenReturn(networkInterface);
        when(virtualMachine.networkInterfaceIds()).thenReturn(Collections.singletonList("netIf-id"));
        when(secondaryNetworkInterface.getNetworkSecurityGroup()).thenReturn(networkSecurityGroup);
        Map<String, NicIPConfiguration> mapIPConfiguration = new HashMap<>();
        mapIPConfiguration.put("ipConf", nicIPConfiguration);
        when(secondaryNetworkInterface.ipConfigurations()).thenReturn(mapIPConfiguration);
        when(nicIPConfiguration.getNetwork()).thenReturn(virtualNetwork);

        // PublicIPAddresses
        when(azureService.publicIPAddresses()).thenReturn(publicIPAddresses);
        when(networkInterface.primaryIPConfiguration()).thenReturn(nicIPConfiguration);
        when(nicIPConfiguration.getPublicIPAddress()).thenReturn(publicIPAddress);
        when(azureProviderNetworkingUtils.getVMPublicIPAddresses(azureService,
                                                                 virtualMachine)).thenReturn(Lists.newArrayList(publicIPAddress));

        // Disks
        when(azureService.disks()).thenReturn(disks);

        // NetworkSecurityGroups
        when(azureService.networkSecurityGroups()).thenReturn(networkSecurityGroups);
        when(azureProviderNetworkingUtils.getVMSecurityGroups(azureService,
                                                              virtualMachine)).thenReturn(Lists.newArrayList(networkSecurityGroup));

        // Networks
        when(azureService.networks()).thenReturn(virtualNetworks);
        when(azureProviderNetworkingUtils.getVMNetworks(azureService,
                                                        virtualMachine)).thenReturn(Lists.newArrayList(virtualNetwork));

        // Trigger deleteInstance with busy securityGroup and virtualNetwork
        azureProvider.deleteInstance(infrastructure, "vmId");
        verify(virtualMachines).deleteById("vmId");
        verify(networkInterfaces).deleteById("netIf-id");
        verify(publicIPAddresses).deleteById("pubIP-id");
        verify(disks).deleteById("diskId");
        verify(networkSecurityGroups, times(0)).deleteById("nsg-id");
        verify(virtualNetworks, times(0)).deleteById("vnet-id");
    }

    @Test
    public void testExecuteScriptOnInstanceTag() throws InterruptedException, ExecutionException, TimeoutException {
        Infrastructure infrastructure = InfrastructureFixture.getAzureInfrastructure("id-azure",
                                                                                     "azure",
                                                                                     "clientId",
                                                                                     "secret",
                                                                                     "domain",
                                                                                     "subscriptionId");

        when(azureProviderUtils.searchVirtualMachineByName(azureService,
                                                           "vmTag")).thenReturn(Optional.of(virtualMachine));
        when(virtualMachine.name()).thenReturn("vmTag");
        when(virtualMachine.listExtensions()).thenReturn(virtualMachineExtensionsMap);
        when(virtualMachineExtensionsMap.values()).thenReturn(Lists.newArrayList());

        // Mock new extension definition
        when(virtualMachine.update()).thenReturn(virtualMachineUpdate);
        when(virtualMachineUpdate.defineNewExtension(anyString())).thenReturn(virtualMachineUpdateBlank);
        when(virtualMachineUpdateBlank.withPublisher(anyString())).thenReturn(virtualMachineUpdateWithType);
        when(virtualMachineUpdateWithType.withType(anyString())).thenReturn(virtualMachineUpdateWithVersion);
        when(virtualMachineUpdateWithVersion.withVersion(anyString())).thenReturn(virtualMachineUpdateWithAttach);
        when(virtualMachineUpdateWithAttach.withMinorVersionAutoUpgrade()).thenReturn(virtualMachineUpdateWithAttach);
        when(virtualMachineUpdateWithAttach.withPublicSetting(anyString(),
                                                              anyString())).thenReturn(virtualMachineUpdateWithAttach);
        when(virtualMachineUpdateWithAttach.attach()).thenReturn(virtualMachineUpdate);
        when(virtualMachineUpdate.applyAsync(null)).thenReturn(virtualMachineUpdateFuture);
        when(virtualMachineUpdateFuture.get(anyLong(), any(TimeUnit.class))).thenReturn(virtualMachine);

        when(virtualMachine.osType()).thenReturn(OperatingSystemTypes.LINUX);

        // Trigger executeScript
        List<ScriptResult> scriptsResult = azureProvider.executeScriptOnInstanceTag(infrastructure,
                                                                                    "vmTag",
                                                                                    InstanceScriptFixture.getInstanceScript(new String[] { "id",
                                                                                                                                           "pwd" }));
        verify(virtualMachineUpdate).defineNewExtension(anyString());
        assertThat(scriptsResult.size(), is(2));
    }

    @Test
    public void testExecuteScriptOnInstanceId() throws InterruptedException, ExecutionException, TimeoutException {
        Infrastructure infrastructure = InfrastructureFixture.getAzureInfrastructure("id-azure",
                                                                                     "azure",
                                                                                     "clientId",
                                                                                     "secret",
                                                                                     "domain",
                                                                                     "subscriptionId");

        when(azureProviderUtils.searchVirtualMachineByID(azureService, "vmId")).thenReturn(Optional.of(virtualMachine));
        when(virtualMachine.id()).thenReturn("vmId");
        when(virtualMachine.name()).thenReturn("vmTag");
        when(virtualMachine.osType()).thenReturn(OperatingSystemTypes.LINUX);
        when(virtualMachine.listExtensions()).thenReturn(virtualMachineExtensionsMap);
        when(virtualMachineExtensionsMap.values()).thenReturn(Lists.newArrayList());

        // Mock new extension definition
        when(virtualMachine.update()).thenReturn(virtualMachineUpdate);
        when(virtualMachineUpdate.defineNewExtension(anyString())).thenReturn(virtualMachineUpdateBlank);
        when(virtualMachineUpdateBlank.withPublisher(anyString())).thenReturn(virtualMachineUpdateWithType);
        when(virtualMachineUpdateWithType.withType(anyString())).thenReturn(virtualMachineUpdateWithVersion);
        when(virtualMachineUpdateWithVersion.withVersion(anyString())).thenReturn(virtualMachineUpdateWithAttach);
        when(virtualMachineUpdateWithAttach.withMinorVersionAutoUpgrade()).thenReturn(virtualMachineUpdateWithAttach);
        when(virtualMachineUpdateWithAttach.withPublicSetting(anyString(),
                                                              anyString())).thenReturn(virtualMachineUpdateWithAttach);
        when(virtualMachineUpdateWithAttach.attach()).thenReturn(virtualMachineUpdate);
        when(virtualMachineUpdate.applyAsync(null)).thenReturn(virtualMachineUpdateFuture);
        when(virtualMachineUpdateFuture.get(anyLong(), any(TimeUnit.class))).thenReturn(virtualMachine);

        // Trigger executeScript
        List<ScriptResult> scriptsResult = azureProvider.executeScriptOnInstanceId(infrastructure,
                                                                                   "vmId",
                                                                                   InstanceScriptFixture.getInstanceScript(new String[] { "id",
                                                                                                                                          "pwd" }));
        verify(virtualMachineUpdate).defineNewExtension(anyString());
        assertThat(scriptsResult.size(), is(2));
    }

    @Test
    public void testExecuteScriptOnInstanceIdWithExistingExtension()
            throws InterruptedException, ExecutionException, TimeoutException {
        Infrastructure infrastructure = InfrastructureFixture.getAzureInfrastructure("id-azure",
                                                                                     "azure",
                                                                                     "clientId",
                                                                                     "secret",
                                                                                     "domain",
                                                                                     "subscriptionId");

        when(azureProviderUtils.searchVirtualMachineByID(azureService, "vmId")).thenReturn(Optional.of(virtualMachine));
        when(virtualMachine.id()).thenReturn("vmId");
        when(virtualMachine.name()).thenReturn("vmTag");
        when(virtualMachine.osType()).thenReturn(OperatingSystemTypes.LINUX);

        when(virtualMachineExtension.name()).thenReturn("extensionName");
        when(virtualMachineExtension.publisherName()).thenReturn("Microsoft.Azure.Extensions");
        when(virtualMachineExtension.typeName()).thenReturn("CustomScript");
        Collection<VirtualMachineExtension> virtualMachineExtensions = Lists.newArrayList(virtualMachineExtension);
        when(virtualMachine.listExtensions()).thenReturn(virtualMachineExtensionsMap);
        when(virtualMachineExtensionsMap.values()).thenReturn(virtualMachineExtensions);

        // Mock extension update
        when(virtualMachine.update()).thenReturn(virtualMachineUpdate);
        when(virtualMachineUpdate.updateExtension(anyString())).thenReturn(virtualMachineExtensionUpdate);
        when(virtualMachineExtensionUpdate.withPublicSetting(anyString(),
                                                             anyString())).thenReturn(virtualMachineExtensionUpdate);
        when(virtualMachineExtensionUpdate.parent()).thenReturn(virtualMachineUpdate);
        when(virtualMachineUpdate.applyAsync(null)).thenReturn(virtualMachineUpdateFuture);
        when(virtualMachineUpdateFuture.get(anyLong(), any(TimeUnit.class))).thenReturn(virtualMachine);

        // Trigger executeScript
        List<ScriptResult> scriptsResult = azureProvider.executeScriptOnInstanceId(infrastructure,
                                                                                   "vmId",
                                                                                   InstanceScriptFixture.getInstanceScript(new String[] { "id",
                                                                                                                                          "pwd" }));
        verify(virtualMachineUpdate).updateExtension("extensionName");
        assertThat(scriptsResult.size(), is(2));
    }

    @Test
    public void testAddToInstancePublicIp() {
        Infrastructure infrastructure = InfrastructureFixture.getAzureInfrastructure("id-azure",
                                                                                     "azure",
                                                                                     "clientId",
                                                                                     "secret",
                                                                                     "domain",
                                                                                     "subscriptionId");

        when(azureProviderUtils.searchVirtualMachineByID(azureService, "vmId")).thenReturn(Optional.of(virtualMachine));
        when(virtualMachine.name()).thenReturn("vmTag");
        when(virtualMachine.region()).thenReturn(Region.US_EAST);
        when(publicIPAddress.ipAddress()).thenReturn("0.0.0.0");
        when(azureService.resourceGroups()).thenReturn(resourceGroups);
        when(resourceGroups.getByName("resourceGroup")).thenReturn(resourceGroup);
        when(virtualMachine.resourceGroupName()).thenReturn("resourceGroup");

        when(virtualMachine.getPrimaryNetworkInterface()).thenReturn(networkInterface);
        when(networkInterface.primaryIPConfiguration()).thenReturn(nicIPConfiguration);
        when(nicIPConfiguration.getNetwork()).thenReturn(virtualNetwork);
        when(networkInterface.getNetworkSecurityGroup()).thenReturn(networkSecurityGroup);
        when(virtualMachine.getPrimaryPublicIPAddress()).thenReturn(null);

        when(virtualMachine.networkInterfaceIds()).thenReturn(Collections.singletonList("netIf-id"));
        when(azureService.networkInterfaces()).thenReturn(networkInterfaces);
        when(networkInterfaces.getById("netIf-id")).thenReturn(networkInterface);
        when(networkInterface.primaryIPConfiguration()).thenReturn(nicIPConfiguration);
        when(nicIPConfiguration.getPublicIPAddress()).thenReturn(null);

        when(azureProviderNetworkingUtils.preparePublicIPAddress(any(Azure.class),
                                                                 any(Region.class),
                                                                 any(ResourceGroup.class),
                                                                 anyString(),
                                                                 anyBoolean())).thenReturn(creatablePublicIPAddress);
        when(creatablePublicIPAddress.create()).thenReturn(publicIPAddress);

        when(networkInterface.update()).thenReturn(networkInterfaceUpdate);
        when(networkInterfaceUpdate.withExistingPrimaryPublicIPAddress(publicIPAddress)).thenReturn(networkInterfaceUpdate);
        when(networkInterfaceUpdate.apply()).thenReturn(networkInterface);

        // Trigger addPublicIP
        String ipAddress = azureProvider.addToInstancePublicIp(infrastructure, "vmId", null);
        verify(azureProviderNetworkingUtils).preparePublicIPAddress(any(Azure.class),
                                                                    any(Region.class),
                                                                    any(ResourceGroup.class),
                                                                    anyString(),
                                                                    anyBoolean());
        verify(networkInterface).update();
        assertThat(ipAddress, is("0.0.0.0"));
    }

    @Test
    public void testAddToInstancePublicIpWithNewInterface() {
        Infrastructure infrastructure = InfrastructureFixture.getAzureInfrastructure("id-azure",
                                                                                     "azure",
                                                                                     "clientId",
                                                                                     "secret",
                                                                                     "domain",
                                                                                     "subscriptionId");

        when(azureProviderUtils.searchVirtualMachineByID(azureService, "vmId")).thenReturn(Optional.of(virtualMachine));
        when(virtualMachine.name()).thenReturn("vmTag");
        when(virtualMachine.region()).thenReturn(Region.US_EAST);
        when(publicIPAddress.ipAddress()).thenReturn("0.0.0.0");
        when(azureService.resourceGroups()).thenReturn(resourceGroups);
        when(resourceGroups.getByName("resourceGroup")).thenReturn(resourceGroup);
        when(virtualMachine.resourceGroupName()).thenReturn("resourceGroup");

        when(virtualMachine.getPrimaryNetworkInterface()).thenReturn(networkInterface);
        when(networkInterface.primaryIPConfiguration()).thenReturn(nicIPConfiguration);
        when(nicIPConfiguration.getNetwork()).thenReturn(virtualNetwork);
        when(networkInterface.getNetworkSecurityGroup()).thenReturn(networkSecurityGroup);
        when(virtualMachine.getPrimaryPublicIPAddress()).thenReturn(publicIPAddress);

        when(azureProviderNetworkingUtils.preparePublicIPAddress(any(Azure.class),
                                                                 any(Region.class),
                                                                 any(ResourceGroup.class),
                                                                 anyString(),
                                                                 anyBoolean())).thenReturn(creatablePublicIPAddress);
        when(creatablePublicIPAddress.create()).thenReturn(publicIPAddress);

        when(virtualMachine.update()).thenReturn(virtualMachineUpdate);
        when(virtualMachineUpdate.withExistingSecondaryNetworkInterface(networkInterface)).thenReturn(virtualMachineUpdate);
        when(virtualMachineUpdate.withNewSecondaryNetworkInterface(creatableNetworkInterface)).thenReturn(virtualMachineUpdate);
        when(azureProviderNetworkingUtils.prepareNetworkInterface(any(Azure.class),
                                                                  any(Region.class),
                                                                  any(ResourceGroup.class),
                                                                  anyString(),
                                                                  any(Network.class),
                                                                  any(NetworkSecurityGroup.class),
                                                                  any(PublicIPAddress.class))).thenReturn(creatableNetworkInterface);
        when(creatableNetworkInterface.create()).thenReturn(networkInterface);
        when(creatablePublicIPAddress.create()).thenReturn(publicIPAddress);
        when(virtualMachineUpdate.apply()).thenReturn(virtualMachine);

        // Trigger addPublicIP
        String ipAddress = azureProvider.addToInstancePublicIp(infrastructure, "vmId", null);
        verify(virtualMachine).update();
        verify(azureProviderNetworkingUtils).preparePublicIPAddress(any(Azure.class),
                                                                    any(Region.class),
                                                                    any(ResourceGroup.class),
                                                                    anyString(),
                                                                    anyBoolean());
        verify(azureProviderNetworkingUtils).prepareNetworkInterface(any(Azure.class),
                                                                     any(Region.class),
                                                                     any(ResourceGroup.class),
                                                                     anyString(),
                                                                     any(Network.class),
                                                                     any(NetworkSecurityGroup.class),
                                                                     any(PublicIPAddress.class));
        assertThat(ipAddress, is("0.0.0.0"));
    }

    @Test
    public void testRemoveInstancePublicIp() {
        Infrastructure infrastructure = InfrastructureFixture.getAzureInfrastructure("id-azure",
                                                                                     "azure",
                                                                                     "clientId",
                                                                                     "secret",
                                                                                     "domain",
                                                                                     "subscriptionId");

        when(azureProviderUtils.searchVirtualMachineByID(azureService, "vmId")).thenReturn(Optional.of(virtualMachine));
        when(virtualMachine.name()).thenReturn("vmTag");

        List<String> interfaceIds = Lists.newArrayList();
        when(networkInterface.id()).thenReturn("netIf-id");
        when(virtualMachine.getPrimaryPublicIPAddressId()).thenReturn("netIf-id");
        when(virtualMachine.networkInterfaceIds()).thenReturn(interfaceIds);
        when(virtualMachine.getPrimaryPublicIPAddress()).thenReturn(publicIPAddress);
        when(virtualMachine.getPrimaryNetworkInterface()).thenReturn(networkInterface);

        when(azureService.networkInterfaces()).thenReturn(networkInterfaces);
        when(networkInterfaces.getById(anyString())).thenReturn(networkInterface);
        when(networkInterface.primaryIPConfiguration()).thenReturn(nicIPConfiguration);
        when(nicIPConfiguration.getPublicIPAddress()).thenReturn(publicIPAddress);
        when(azureService.publicIPAddresses()).thenReturn(publicIPAddresses);
        when(publicIPAddress.id()).thenReturn("pubIP-id");

        when(networkInterface.update()).thenReturn(networkInterfaceUpdate);
        when(networkInterfaceUpdate.withoutPrimaryPublicIPAddress()).thenReturn(networkInterfaceUpdate);
        when(networkInterfaceUpdate.apply()).thenReturn(networkInterface);

        // Trigger remove public IP
        azureProvider.removeInstancePublicIp(infrastructure, "vmId", null);

        verify(networkInterface).update();
        verify(nicIPConfiguration, times(0)).getPublicIPAddress();
        verify(publicIPAddresses).deleteById("pubIP-id");
    }

    @Test
    public void testRemoveInstancePublicIpWithSecondaryNetworkInterface() {
        Infrastructure infrastructure = InfrastructureFixture.getAzureInfrastructure("id-azure",
                                                                                     "azure",
                                                                                     "clientId",
                                                                                     "secret",
                                                                                     "domain",
                                                                                     "subscriptionId");

        when(azureProviderUtils.searchVirtualMachineByID(azureService, "vmId")).thenReturn(Optional.of(virtualMachine));
        when(virtualMachine.name()).thenReturn("vmTag");

        List<String> interfaceIds = Lists.newArrayList("netIf-id");
        when(networkInterface.id()).thenReturn("netIf-id");
        when(virtualMachine.getPrimaryPublicIPAddressId()).thenReturn("netIf-id2");
        when(virtualMachine.getPrimaryPublicIPAddress()).thenReturn(publicIPAddress);
        when(virtualMachine.networkInterfaceIds()).thenReturn(interfaceIds);
        when(virtualMachine.getPrimaryNetworkInterface()).thenReturn(networkInterface);

        when(azureService.networkInterfaces()).thenReturn(networkInterfaces);
        when(networkInterfaces.getById(anyString())).thenReturn(networkInterface);
        when(networkInterface.primaryIPConfiguration()).thenReturn(nicIPConfiguration);
        when(nicIPConfiguration.getPublicIPAddress()).thenReturn(publicIPAddress);
        when(azureService.publicIPAddresses()).thenReturn(publicIPAddresses);
        when(publicIPAddress.id()).thenReturn("pubIP-id");

        when(networkInterface.update()).thenReturn(networkInterfaceUpdate);
        when(networkInterfaceUpdate.withoutPrimaryPublicIPAddress()).thenReturn(networkInterfaceUpdate);
        when(networkInterfaceUpdate.apply()).thenReturn(networkInterface);

        // Trigger remove public IP
        azureProvider.removeInstancePublicIp(infrastructure, "vmId", null);

        verify(networkInterface).update();
        verify(nicIPConfiguration, times(2)).getPublicIPAddress();
        verify(publicIPAddresses).deleteById("pubIP-id");
    }

    @Test
    public void testDeleteInfrastructure() {
        Infrastructure infrastructure = InfrastructureFixture.getSimpleInfrastructure("azure");
        when(azureProviderUtils.getAllVirtualMachines(azureService)).thenReturn(Sets.newHashSet(virtualMachine));
        azureProvider.deleteInfrastructure(infrastructure);
        verify(azureServiceCache).removeService(infrastructure);
    }

    private <T> PagedList<T> getPagedList() {
        return new PagedList<T>() {
            @Override
            public Page<T> nextPage(String nextPageLink) throws RestException, IOException {
                return null;
            }
        };
    }
}

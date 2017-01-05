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

import static org.hamcrest.CoreMatchers.is;
import static org.mockito.Matchers.any;
import static org.mockito.Mockito.when;

import java.rmi.RemoteException;
import java.util.Arrays;
import java.util.Optional;

import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.ow2.proactive.connector.iaas.model.Infrastructure;

import com.vmware.vim25.VirtualDevice;
import com.vmware.vim25.VirtualDeviceConfigSpec;
import com.vmware.vim25.VirtualDisk;
import com.vmware.vim25.VirtualEthernetCard;
import com.vmware.vim25.VirtualHardware;
import com.vmware.vim25.VirtualMachineConfigInfo;
import com.vmware.vim25.mo.Folder;
import com.vmware.vim25.mo.ServiceInstance;
import com.vmware.vim25.mo.VirtualMachine;


public class VMWareProviderMacAddressHandlerTest {

    @InjectMocks
    private VMWareProviderMacAddressHandler vMWareProviderMacAddressHandler;

    @Mock
    private VMWareServiceInstanceCache vmWareServiceInstanceCache;

    @Mock
    private ServiceInstance serviceInstance;

    @Mock
    private Folder rootFolder;

    @Mock
    private VirtualMachine virtualMachine;

    @Mock
    private VirtualMachineConfigInfo virtualMachineConfigInfo;

    @Mock
    private VirtualHardware hardware;

    @Before
    public void init() {
        MockitoAnnotations.initMocks(this);
        when(vmWareServiceInstanceCache.getServiceInstance(any(Infrastructure.class))).thenReturn(serviceInstance);
        when(serviceInstance.getRootFolder()).thenReturn(rootFolder);
    }

    @Test
    public void testGetVirtualDeviceConfigWithMacAddressOK() throws RemoteException, InterruptedException {

        // Add a Virtual Ethernet Card with automatically generated MAC address on the VM to clone
        when(virtualMachine.getConfig()).thenReturn(virtualMachineConfigInfo);
        when(virtualMachineConfigInfo.getHardware()).thenReturn(hardware);
        VirtualEthernetCard virtEthCard = new VirtualEthernetCard();
        virtEthCard.setAddressType("Generated");
        when(virtualMachine.getConfig().getHardware().getDevice()).thenReturn(new VirtualDevice[] { virtEthCard });

        Optional<VirtualDeviceConfigSpec[]> virtDevConfSpec = vMWareProviderMacAddressHandler.getVirtualDeviceConfigWithMacAddress("00:50:56:11:11:11",
                                                                                                                                   virtualMachine);

        Assert.assertTrue(virtDevConfSpec.isPresent());

        Assert.assertTrue(Arrays.stream(virtDevConfSpec.get()).findFirst().isPresent());

        VirtualDevice virtDev = Arrays.stream(virtDevConfSpec.get()).findFirst().get().getDevice();
        Assert.assertNotNull(virtDev);

        Assert.assertTrue(virtDev instanceof VirtualEthernetCard);

        Assert.assertThat(((VirtualEthernetCard) virtDev).getMacAddress(), is("00:50:56:11:11:11"));
    }

    @Test
    public void testGetVirtualDeviceConfigWithMacAddressKO() throws RemoteException, InterruptedException {

        // Add a virtual disk of 8G (instead of an ethernet card)
        when(virtualMachine.getConfig()).thenReturn(virtualMachineConfigInfo);
        when(virtualMachineConfigInfo.getHardware()).thenReturn(hardware);
        VirtualDisk virtDisk = new VirtualDisk();
        virtDisk.setCapacityInBytes((long) 8000000);
        when(virtualMachine.getConfig().getHardware().getDevice()).thenReturn(new VirtualDevice[] { virtDisk });

        Assert.assertFalse(vMWareProviderMacAddressHandler.getVirtualDeviceConfigWithMacAddress("00:50:56:11:11:11",
                                                                                                virtualMachine)
                                                          .isPresent());
    }
}

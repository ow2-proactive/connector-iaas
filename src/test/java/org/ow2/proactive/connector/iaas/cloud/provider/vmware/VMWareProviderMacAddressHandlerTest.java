package org.ow2.proactive.connector.iaas.cloud.provider.vmware;

import com.vmware.vim25.VirtualDevice;
import com.vmware.vim25.VirtualDeviceConfigSpec;
import com.vmware.vim25.VirtualEthernetCard;
import com.vmware.vim25.VirtualHardware;
import com.vmware.vim25.VirtualMachineConfigInfo;
import com.vmware.vim25.mo.Folder;
import com.vmware.vim25.mo.ServiceInstance;
import com.vmware.vim25.mo.VirtualMachine;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.ow2.proactive.connector.iaas.model.Infrastructure;

import java.rmi.RemoteException;
import java.util.Arrays;

import static org.hamcrest.CoreMatchers.is;
import static org.mockito.Matchers.any;
import static org.mockito.Mockito.when;

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
        when(vmWareServiceInstanceCache.getServiceInstance(any(Infrastructure.class)))
                .thenReturn(serviceInstance);
        when(serviceInstance.getRootFolder()).thenReturn(rootFolder);
    }

    @Test
    public void testGetVirtualDeviceConfigWithMacAddress() throws RemoteException, InterruptedException {

        // Add a Virtual Ethernet Card with automatically generated MAC address on the VM to clone
        when(virtualMachine.getConfig()).thenReturn(virtualMachineConfigInfo);
        when(virtualMachineConfigInfo.getHardware()).thenReturn(hardware);
        VirtualEthernetCard virtEthCard = new VirtualEthernetCard();
        virtEthCard.setAddressType("Generated");
        when(virtualMachine.getConfig().getHardware().getDevice()).thenReturn(new VirtualDevice[]{virtEthCard});

        Assert.assertTrue(vMWareProviderMacAddressHandler.getVirtualDeviceConfigWithMacAddress("00:50:56:11:11:11",
                virtualMachine).isPresent());

        Assert.assertTrue(Arrays.stream(vMWareProviderMacAddressHandler.getVirtualDeviceConfigWithMacAddress("00:50:56:11:11:11",
                virtualMachine).get()).findFirst().isPresent());

        Assert.assertNotNull(Arrays.stream(vMWareProviderMacAddressHandler.getVirtualDeviceConfigWithMacAddress
                ("00:50:56:11:11:11", virtualMachine).get()).findFirst().get().getDevice());

        Assert.assertTrue(Arrays.stream(vMWareProviderMacAddressHandler.getVirtualDeviceConfigWithMacAddress
                ("00:50:56:11:11:11", virtualMachine).get()).findFirst().get().getDevice() instanceof VirtualEthernetCard);

        Assert.assertThat(Arrays.stream(vMWareProviderMacAddressHandler.getVirtualDeviceConfigWithMacAddress
                ("00:50:56:11:11:11",
                virtualMachine).get()).findFirst().map(VirtualDeviceConfigSpec::getDevice)
                .map(virtDevice -> (VirtualEthernetCard) virtDevice).get().getMacAddress(), is("00:50:56:11:11:11"));
    }
}

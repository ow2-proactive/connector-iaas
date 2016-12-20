package org.ow2.proactive.connector.iaas.cloud.provider.vmware;

import java.util.Arrays;
import java.util.Optional;

import org.springframework.stereotype.Component;

import com.vmware.vim25.VirtualDevice;
import com.vmware.vim25.VirtualDeviceConfigSpec;
import com.vmware.vim25.VirtualDeviceConfigSpecOperation;
import com.vmware.vim25.VirtualEthernetCard;
import com.vmware.vim25.mo.VirtualMachine;


@Component
public class VMWareProviderMacAddressHandler {

    private static final String ADDRESS_TYPE = "Manual";

    /**
     * Assign a single MAC address per VM
     * @param macAddress
     * @param vm
     * @return
     */
    public Optional<VirtualDeviceConfigSpec[]> getVirtualDeviceConfigWithMacAddress(String macAddress,
            VirtualMachine vm) {

        return Arrays.stream(vm.getConfig().getHardware().getDevice())
                .filter(virtualDevice -> virtualDevice instanceof VirtualEthernetCard).findFirst()
                .map(virtualDevice -> getVirtualEthernetCard(virtualDevice, macAddress))
                .map(virtEthCard -> getVirtualDeviceConfigSpec(virtEthCard))
                .map(virtDevConfSpec -> new VirtualDeviceConfigSpec[] { virtDevConfSpec });

    }

    private VirtualDeviceConfigSpec getVirtualDeviceConfigSpec(VirtualEthernetCard virtEthCard) {
        VirtualDeviceConfigSpec virtDevConfSpec = new VirtualDeviceConfigSpec();
        virtDevConfSpec.setDevice(virtEthCard);
        virtDevConfSpec.setOperation(VirtualDeviceConfigSpecOperation.edit);
        return virtDevConfSpec;
    }

    private VirtualEthernetCard getVirtualEthernetCard(VirtualDevice virtualDevice, String macAddress) {
        VirtualEthernetCard virtEthCard = new VirtualEthernetCard();
        virtEthCard = (VirtualEthernetCard) virtualDevice;
        virtEthCard.setAddressType(ADDRESS_TYPE);
        virtEthCard.setMacAddress(macAddress);
        return virtEthCard;
    }

}

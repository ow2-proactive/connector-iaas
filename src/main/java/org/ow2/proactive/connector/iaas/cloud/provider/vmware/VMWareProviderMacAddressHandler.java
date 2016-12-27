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
/**
 * Handle MAC address assignment by creating customized VirtualDeviceConfigSpec.
 */
public class VMWareProviderMacAddressHandler {

    private static final String ADDRESS_TYPE = "Manual";

    /**
     * Retrieve a new VirtualDeviceConfigSpec with a customized VirtualEthernetCard's MAC address cloned from the
     * specified VM
     *
     * @param macAddress    the MAC address to assign
     * @param vm            the original VM to clone (containing a VirtualEthernetCard)
     * @return  an optional VirtualDeviceConfigSpec[] (single object's array) with a customized VirtualEthernetCard
     */
    Optional<VirtualDeviceConfigSpec[]> getVirtualDeviceConfigWithMacAddress(String macAddress, VirtualMachine vm) {

        return Arrays.stream(vm.getConfig().getHardware().getDevice())
                .filter(virtualDevice -> virtualDevice instanceof VirtualEthernetCard).findFirst()
                .map(virtualDevice -> getCustomizedVirtualEthernetCard(virtualDevice, macAddress))
                .map(this::getVirtualDeviceConfigSpec)
                .map(virtDevConfSpec -> new VirtualDeviceConfigSpec[] { virtDevConfSpec });

    }

    private VirtualEthernetCard getCustomizedVirtualEthernetCard(VirtualDevice virtualDevice, String macAddress) {
        VirtualEthernetCard virtEthCard = (VirtualEthernetCard) virtualDevice;
        virtEthCard.setAddressType(ADDRESS_TYPE);
        virtEthCard.setMacAddress(macAddress);
        return virtEthCard;
    }

    private VirtualDeviceConfigSpec getVirtualDeviceConfigSpec(VirtualEthernetCard virtEthCard) {
        VirtualDeviceConfigSpec virtDevConfSpec = new VirtualDeviceConfigSpec();
        virtDevConfSpec.setDevice(virtEthCard);
        virtDevConfSpec.setOperation(VirtualDeviceConfigSpecOperation.edit);
        return virtDevConfSpec;
    }

}

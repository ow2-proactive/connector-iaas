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
                     .filter(virtualDevice -> virtualDevice instanceof VirtualEthernetCard)
                     .findFirst()
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

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

import com.microsoft.azure.management.compute.ImageReference;


public enum KnownLinuxVirtualMachineImageCustom {
    /** UbuntuServer 16.04LTS. */
    UBUNTU_SERVER_16_04_LTS("Canonical", "UbuntuServer", "16.04.0-LTS"),
    /** UbuntuServer 18.04LTS. */
    UBUNTU_SERVER_18_04_LTS("Canonical", "UbuntuServer", "18.04-LTS"),

    /** Debian 9. */
    DEBIAN_9("credativ", "Debian", "9"),
    /** Debian 10. */
    DEBIAN_10("Debian", "debian-10", "10"),

    /** CentOS 8.1. */
    CENTOS_8_1("OpenLogic", "CentOS", "8_1"),
    /** CentOS 8.3. */
    CENTOS_8_3("OpenLogic", "CentOS", "8_3"),

    /**
     * OpenSUSE-Leap 15.1.
     * @deprecated use OPENSUSE_LEAP_15.
     */
    @Deprecated
    OPENSUSE_LEAP_15_1("SUSE", "openSUSE-Leap-15-3", "gen1"),
    /** OpenSUSE-Leap 15. */
    OPENSUSE_LEAP_15("SUSE", "opensuse-leap-15-5", "gen1"),

    /**
     * SLES 15-SP1-gen1.
     * @deprecated use SLES_15.
     */
    @Deprecated
    SLES_15_SP1("SUSE", "sles-15-sp2", "gen1"),
    /** SLES 15. */
    SLES_15("SUSE", "sles-15-sp5-basic", "gen1"),

    /** RedHat RHEL 8.2. */
    REDHAT_RHEL_8_2("RedHat", "RHEL", "8.2"),

    /** Oracle Linux 8.1. */
    ORACLE_LINUX_8_1("Oracle", "Oracle-Linux", "81"),

    /** UbuntuServer 18.04LTS Gen2. */
    UBUNTU_SERVER_18_04_LTS_GEN2("Canonical", "UbuntuServer", "18_04-lts-gen2"),
    /** UbuntuServer 20.04LTS. */
    UBUNTU_SERVER_20_04_LTS("Canonical", "0001-com-ubuntu-server-focal", "20_04-lts"),
    /** UbuntuServer 20.04LTS Gen2. */
    UBUNTU_SERVER_20_04_LTS_GEN2("Canonical", "0001-com-ubuntu-server-focal", "20_04-lts-gen2"),

    /** UbuntuServer 22.04LTS. */
    UBUNTU_SERVER_22_04_LTS("Canonical", "0001-com-ubuntu-server-jammy", "22_04-lts"),
    /** UbuntuServer 22.04LTS Gen2. */
    UBUNTU_SERVER_22_04_LTS_GEN2("Canonical", "0001-com-ubuntu-server-jammy", "22_04-lts-gen2"),

    /** UbuntuServer 24.04LTS. */
    UBUNTU_SERVER_24_04_LTS("Canonical", "ubuntu-24_04-lts", "server-gen1"),
    /** UbuntuServer 24.04LTS Gen2. */
    UBUNTU_SERVER_24_04_LTS_GEN2("Canonical", "ubuntu-24_04-lts", "server");

    private final String publisher;

    private final String offer;

    private final String sku;

    KnownLinuxVirtualMachineImageCustom(String publisher, String offer, String sku) {
        this.publisher = publisher;
        this.offer = offer;
        this.sku = sku;
    }

    /** @return the name of the image publisher */
    public String publisher() {
        return this.publisher;
    }

    /** @return the name of the image offer */
    public String offer() {
        return this.offer;
    }

    /** @return the name of the image SKU */
    public String sku() {
        return this.sku;
    }

    /** @return the image reference */
    public ImageReference imageReference() {
        return new ImageReference().withPublisher(publisher()).withOffer(offer()).withSku(sku()).withVersion("latest");
    }
}

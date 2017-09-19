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

import java.util.Optional;

import com.microsoft.azure.management.network.Network;
import com.microsoft.azure.management.network.NetworkSecurityGroup;
import com.microsoft.azure.management.network.PublicIpAddress;
import com.microsoft.azure.management.resources.fluentcore.model.Creatable;

import lombok.Getter;


/**
 * @author ActiveEon Team
 * @since 19/09/17
 */
public class AzureNetworkOptions {

    @Getter
    private Creatable<Network> creatableVirtualNetwork;

    @Getter
    private Optional<Network> optionalVirtualNetwork;

    @Getter
    private Creatable<NetworkSecurityGroup> creatableNetworkSecurityGroup;

    @Getter
    private Optional<NetworkSecurityGroup> optionalNetworkSecurityGroup;

    @Getter
    private Optional<PublicIpAddress> optionalPublicIpAddress;

    @Getter
    private Optional<Boolean> optionalStaticPublicIP;

    public AzureNetworkOptions(Creatable<Network> creatableVirtualNetwork, Optional<Network> optionalVirtualNetwork,
            Creatable<NetworkSecurityGroup> creatableNetworkSecurityGroup,
            Optional<NetworkSecurityGroup> optionalNetworkSecurityGroup,
            Optional<PublicIpAddress> optionalPublicIpAddress, Optional<Boolean> optionalStaticPublicIP) {
        this.creatableVirtualNetwork = creatableVirtualNetwork;
        this.optionalVirtualNetwork = optionalVirtualNetwork;
        this.creatableNetworkSecurityGroup = creatableNetworkSecurityGroup;
        this.optionalNetworkSecurityGroup = optionalNetworkSecurityGroup;
        this.optionalPublicIpAddress = optionalPublicIpAddress;
        this.optionalStaticPublicIP = optionalStaticPublicIP;
    }

}

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
package org.ow2.proactive.connector.iaas.fixtures;

import java.util.ArrayList;
import java.util.List;

import org.json.JSONObject;
import org.ow2.proactive.connector.iaas.model.Instance;
import org.ow2.proactive.connector.iaas.model.Options;


public class InstanceFixture {

    public static Instance simpleInstance(String id) {

        return getInstance(id, id, id, "1", "512", "1", "172.168.1.248", "1.0.0.2", "RUNNING");
    }

    public static Instance simpleInstanceWithTag(String id, String tag) {

        return getInstance(id, tag, id, "1", "512", "1", "172.168.1.248", "1.0.0.2", "RUNNING");
    }

    public static Instance simpleInstanceWithTagAndImage(String tag, String image) {

        return getInstance("", tag, image, "1", "512", "1", "172.168.1.248", "1.0.0.2", "RUNNING");
    }

    public static Instance simpleInstanceWithPublicKey(String tag, String image, String publicKey) {
        return new Instance("instanceId",
                            tag,
                            image,
                            "1",
                            null,
                            HardwareFixtures.getHardware("type"),
                            null,
                            CredentialsFixtures.getInstanceCredentialsWithKey(publicKey),
                            null,
                            null);
    }

    public static Instance simpleInstanceWithInitScripts(String tag, String image, String[] scripts) {
        return new Instance("instanceId",
                            tag,
                            image,
                            "1",
                            null,
                            HardwareFixtures.getHardware("type"),
                            null,
                            CredentialsFixtures.getInstanceCredentials("username", "password"),
                            null,
                            InstanceScriptFixture.getInstanceScript(scripts));
    }

    public static Instance simpleInstanceWithMacAddress(String tag, String image, List<String> macAddresses) {

        return getInstanceWithMacAddress(tag,
                                         tag,
                                         image,
                                         "1",
                                         "512",
                                         "1",
                                         "172.168.1.248",
                                         "1.0.0.2",
                                         "RUNNING",
                                         macAddresses);
    }

    public static String getInstanceAsaString(String id, String tag, String image, String number, String minRam,
            String minCores, String publicAddress, String privateAddress, String status) {
        JSONObject jsonObject = new JSONObject(getInstance(id,
                                                           tag,
                                                           image,
                                                           number,
                                                           minRam,
                                                           minCores,
                                                           publicAddress,
                                                           privateAddress,
                                                           status));
        return jsonObject.toString();
    }

    public static Instance getInstance(String id, String tag, String image, String number, String minRam,
            String minCores, String publicAddress, String privateAddress, String status) {
        return new Instance(id,
                            tag,
                            image,
                            number,
                            status,
                            HardwareFixtures.getHardware(minRam, minCores),
                            NetworkFixtures.getNetwork(publicAddress, privateAddress),
                            CredentialsFixtures.getInstanceCredentials("username", "password"),
                            null,
                            InstanceScriptFixture.simpleInstanceScriptNoscripts());
    }

    public static Instance getInstance(String id, String tag, String image, String number, String minRam,
            String minCores, String networkId, String publicAddress, String privateAddress, String status) {
        return new Instance(id,
                            tag,
                            image,
                            number,
                            status,
                            HardwareFixtures.getHardware(minRam, minCores),
                            NetworkFixtures.getNetwork(networkId, publicAddress, privateAddress),
                            CredentialsFixtures.getInstanceCredentials("username", "password"),
                            null,
                            InstanceScriptFixture.simpleInstanceScriptNoscripts());
    }

    public static Instance getInstanceWithSpotPrice(String id, String tag, String image, String number, String minRam,
            String minCores, String publicAddress, String privateAddress, String status, String spotPrice) {
        return new Instance(id,
                            tag,
                            image,
                            number,
                            status,
                            HardwareFixtures.getHardware(minRam, minCores),
                            NetworkFixtures.getNetwork(publicAddress, privateAddress),
                            CredentialsFixtures.getInstanceCredentials("username", "password"),
                            Options.builder().spotPrice("0.05").build(),
                            InstanceScriptFixture.simpleInstanceScriptNoscripts());
    }

    public static Instance getInstanceWithSecurityGroup(String id, String tag, String image, String number,
            String minRam, String minCores, String publicAddress, String privateAddress, String status,
            String securityGroup) {

        List<String> securityGroupNames = new ArrayList<String>();
        securityGroupNames.add("default1");
        securityGroupNames.add("default2");
        return new Instance(id,
                            tag,
                            image,
                            number,
                            status,
                            HardwareFixtures.getHardware(minRam, minCores),
                            NetworkFixtures.getNetwork(publicAddress, privateAddress),
                            CredentialsFixtures.getInstanceCredentials("username", "password"),
                            Options.builder().securityGroupNames(securityGroupNames).build(),
                            InstanceScriptFixture.simpleInstanceScriptNoscripts());
    }

    public static Instance getInstanceWithSubnetId(String id, String tag, String image, String number, String minRam,
            String minCores, String publicAddress, String privateAddress, String status, String subnetId) {
        return new Instance(id,
                            tag,
                            image,
                            number,
                            status,
                            HardwareFixtures.getHardware(minRam, minCores),
                            NetworkFixtures.getNetwork(publicAddress, privateAddress),
                            CredentialsFixtures.getInstanceCredentials("username", "password"),
                            Options.builder().subnetId("127.0.0.1").build(),
                            InstanceScriptFixture.simpleInstanceScriptNoscripts());
    }

    public static Instance getInstanceWithMacAddress(String id, String tag, String image, String number, String minRam,
            String minCores, String publicAddress, String privateAddress, String status, List<String> macAddresses) {
        return new Instance(id,
                            tag,
                            image,
                            number,
                            status,
                            HardwareFixtures.getHardware(minRam, minCores),
                            NetworkFixtures.getNetwork(publicAddress, privateAddress),
                            CredentialsFixtures.getInstanceCredentials("username", "password"),
                            Options.builder().macAddresses(macAddresses).build(),
                            InstanceScriptFixture.simpleInstanceScriptNoscripts());
    }

    public static Instance getInstanceWithResourceGroupAndRegion(String id, String tag, String image, String number,
            String minRam, String minCores, String publicAddress, String privateAddress, String status,
            String resourceGroup, String region) {
        return new Instance(id,
                            tag,
                            image,
                            number,
                            status,
                            HardwareFixtures.getHardware(minRam, minCores),
                            NetworkFixtures.getNetwork(publicAddress, privateAddress),
                            CredentialsFixtures.getInstanceCredentials("username", "password"),
                            Options.builder().resourceGroup(resourceGroup).region(region).build(),
                            InstanceScriptFixture.simpleInstanceScriptNoscripts());
    }
}

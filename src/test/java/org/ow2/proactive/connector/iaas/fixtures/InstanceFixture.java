package org.ow2.proactive.connector.iaas.fixtures;

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

    public static String getInstanceAsaString(String id, String tag, String image, String number,
            String minRam, String minCores, String publicAddress, String privateAddress, String status) {
        JSONObject jsonObject = new JSONObject(
            getInstance(id, tag, image, number, minRam, minCores, publicAddress, privateAddress, status));
        return jsonObject.toString();
    }

    public static Instance getInstance(String id, String tag, String image, String number, String minRam,
            String minCores, String publicAddress, String privateAddress, String status) {
        return new Instance(id, tag, image, number, status, HardwareFixtures.getHardware(minRam, minCores),
            NetworkFixtures.getNetwork(publicAddress, privateAddress),
            CredentialsFixtures.getCredentials("username", "password"), null,
            InstanceScriptFixture.simpleInstanceScriptNoscripts());
    }

    public static Instance getInstanceWithSpotPrice(String id, String tag, String image, String number,
            String minRam, String minCores, String publicAddress, String privateAddress, String status,
            String spotPrice) {
        return new Instance(id, tag, image, number, status, HardwareFixtures.getHardware(minRam, minCores),
            NetworkFixtures.getNetwork(publicAddress, privateAddress),
            CredentialsFixtures.getCredentials("username", "password"),
            Options.builder().spotPrice("0.05").build(),
            InstanceScriptFixture.simpleInstanceScriptNoscripts());
    }

}

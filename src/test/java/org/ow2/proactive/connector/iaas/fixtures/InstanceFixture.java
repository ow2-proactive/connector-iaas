package org.ow2.proactive.connector.iaas.fixtures;

import org.json.JSONObject;
import org.ow2.proactive.connector.iaas.model.Instance;


public class InstanceFixture {

    public static Instance simpleInstance(String id) {

        return getInstance(id, id, id, "1", "512", "1", "172.168.1.248", "1.0.0.2", "RUNNING",
                "securityGroup");
    }

    public static Instance simpleInstanceWithTag(String id, String tag) {

        return getInstance(id, tag, id, "1", "512", "1", "172.168.1.248", "1.0.0.2", "RUNNING",
                "securityGroup");
    }

    public static Instance simpleInstanceWithTagAndImage(String tag, String image) {

        return getInstance("", tag, image, "1", "512", "1", "172.168.1.248", "1.0.0.2", "RUNNING",
                "securityGroup");
    }

    public static String getInstanceAsaString(String id, String tag, String image, String number,
            String minRam, String minCores, String publicAddress, String privateAddress, String status,
            String securityGroup) {
        JSONObject jsonObject = new JSONObject(getInstance(id, tag, image, number, minRam, minCores,
                publicAddress, privateAddress, status, securityGroup));
        return jsonObject.toString();
    }

    public static Instance getInstance(String id, String tag, String image, String number, String minRam,
            String minCores, String publicAddress, String privateAddress, String status,
            String securityGroup) {
        return new Instance(id, tag, image, number, status, securityGroup,
            HardwareFixtures.getHardware(minRam, minCores),
            NetworkFixtures.getNetwork(publicAddress, privateAddress),
            CredentialsFixtures.getCredentials("username", "password"),
            InstanceScriptFixture.simpleInstanceScriptNoscripts());
    }

}

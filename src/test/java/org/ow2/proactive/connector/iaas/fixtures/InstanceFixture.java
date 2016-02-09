package org.ow2.proactive.connector.iaas.fixtures;

import org.json.JSONObject;
import org.ow2.proactive.connector.iaas.model.Instance;


public class InstanceFixture {

    public static Instance simpleInstance(String id) {

        return getInstance(id, id, id, "1", "512", "1", "RUNNING");
    }

    public static Instance simpleInstanceWithTag(String id, String tag) {

        return getInstance(id, tag, id, "1", "512", "1", "RUNNING");
    }

    public static String getInstanceAsaString(String id, String tag, String image, String number,
            String minRam, String minCores, String status) {
        JSONObject jsonObject = new JSONObject(getInstance(id, tag, image, number, minRam, minCores, status));
        return jsonObject.toString();
    }

    public static Instance getInstance(String id, String tag, String image, String number, String minRam,
            String minCores, String status) {
        return new Instance(id, tag, image, number, status, HardwareFixtures.getHardware(minRam, minCores),
            CredentialsFixtures.getCredentials("username", "password"),
            InstanceScriptFixture.simpleInstanceScriptNoscripts());
    }

}

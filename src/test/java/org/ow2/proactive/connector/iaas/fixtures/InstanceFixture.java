package org.ow2.proactive.connector.iaas.fixtures;

import org.json.JSONObject;
import org.ow2.proactive.connector.iaas.model.Instance;


public class InstanceFixture {

    public static Instance simpleInstance(String id) {

        return getInstance(id, id, id, "1", "512", "1", "RUNNING");
    }

    public static String getInstanceAsaString(String id, String name, String image, String number, String ram,
            String cpu, String status) {
        JSONObject jsonObject = new JSONObject(getInstance(id, name, image, number, ram, cpu, status));
        return jsonObject.toString();
    }

    public static Instance getInstance(String id, String name, String image, String number, String ram,
            String cpu, String status) {
        return new Instance(id, name, image, number, ram, cpu, status);
    }

}

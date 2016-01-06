package org.ow2.proactive.iaas.connector.fixtures;

import org.json.JSONObject;
import org.ow2.proactive.iaas.connector.model.InstanceScript;


public class InstanceScriptFixture {

    public static InstanceScript simpleInstanceScriptNoscripts(String id) {

        return getInstanceScript(id, new String[] {});
    }

    public static String getInstanceScriptAsaString(String id, String[] scripts) {
        JSONObject jsonObject = new JSONObject(getInstanceScript(id, scripts));
        return jsonObject.toString();
    }

    public static InstanceScript getInstanceScript(String id, String[] scripts) {
        return new InstanceScript(id, scripts);
    }

}

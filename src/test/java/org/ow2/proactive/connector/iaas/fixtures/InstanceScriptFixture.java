package org.ow2.proactive.connector.iaas.fixtures;

import org.json.JSONObject;
import org.ow2.proactive.connector.iaas.model.Credentials;
import org.ow2.proactive.connector.iaas.model.InstanceScript;

public class InstanceScriptFixture {

	public static InstanceScript simpleInstanceScriptNoscripts() {

		return getInstanceScript(new String[] {});
	}

	public static String getInstanceScriptAsaString(String[] scripts) {
		JSONObject jsonObject = new JSONObject(getInstanceScript(scripts));
		return jsonObject.toString();
	}

	public static InstanceScript getInstanceScript(String[] scripts) {
		return getInstanceScriptUserAndPassword("", "", scripts);
	}

	public static InstanceScript getInstanceScriptUserAndPassword(String username, String pasword, String[] scripts) {
		return new InstanceScript(new Credentials(username, pasword), scripts);
	}

}

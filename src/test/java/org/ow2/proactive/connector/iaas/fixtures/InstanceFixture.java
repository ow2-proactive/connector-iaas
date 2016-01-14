package org.ow2.proactive.connector.iaas.fixtures;

import org.json.JSONObject;
import org.ow2.proactive.connector.iaas.model.Instance;
import org.ow2.proactive.connector.iaas.model.InstanceScript;

public class InstanceFixture {

	public static Instance simpleInstance(String id) {

		return getInstance(id, id, id, "1", "512", "1", "RUNNING",
				InstanceScriptFixture.simpleInstanceScriptNoscripts());
	}

	public static String getInstanceAsaString(String id, String tag, String image, String number, String ram,
			String cpu, String status, InstanceScript instanceScript) {
		JSONObject jsonObject = new JSONObject(getInstance(id, tag, image, number, ram, cpu, status, instanceScript));
		return jsonObject.toString();
	}

	public static Instance getInstance(String id, String tag, String image, String number, String ram, String cpu,
			String status, InstanceScript instanceScript) {
		return new Instance(id, tag, image, number, ram, cpu, status, instanceScript);
	}

}

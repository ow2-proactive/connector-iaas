package org.ow2.proactive.iaas.connector.fixtures;

import org.json.JSONObject;
import org.ow2.proactive.iaas.connector.model.Instance;

public class InstanceFixtures {

	public static String getInstanceAsaString(String infrastructure, String name, String image, String number,
			String cpu, String ram) {
		JSONObject jsonObject = new JSONObject(getInstance(infrastructure, name, image, number, cpu, ram));
		return jsonObject.toString();
	}

	public static Instance getInstance(String infrastructure, String name, String image, String number, String cpu,
			String ram) {
		return new Instance(infrastructure, name, image, number, cpu, ram);
	}

}

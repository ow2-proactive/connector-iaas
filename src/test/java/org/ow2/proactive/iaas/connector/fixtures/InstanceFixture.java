package org.ow2.proactive.iaas.connector.fixtures;

import org.json.JSONObject;
import org.ow2.proactive.iaas.connector.model.Instance;

public class InstanceFixture {

	public static String getInstanceAsaString(String infrastructure, String name, String image, String number,
			String ram, String cpu) {
		JSONObject jsonObject = new JSONObject(getInstanceAsaString(infrastructure, name, image, number, ram, cpu));
		return jsonObject.toString();
	}

	public static Instance getInstance(String infrastructure, String name, String image, String number, String ram,
			String cpu) {
		return new Instance(infrastructure, name, image, number, ram, cpu);
	}

}

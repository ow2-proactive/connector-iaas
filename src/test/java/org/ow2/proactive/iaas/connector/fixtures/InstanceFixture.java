package org.ow2.proactive.iaas.connector.fixtures;

import org.json.JSONObject;
import org.ow2.proactive.iaas.connector.model.Instance;

public class InstanceFixture {

	public static String getInstanceAsaString(String id, String name, String image, String number,
			String ram, String cpu,String status, String infrastructureId) {
		JSONObject jsonObject = new JSONObject(getInstance(id, name, image, number, ram, cpu, status, infrastructureId));
		return jsonObject.toString();
	}

	public static Instance getInstance( String id, String name, String image, String number, String ram,
			String cpu,String status, String infrastructureId) {
		return new Instance(id, name, image, number, ram, cpu, status, infrastructureId);
	}

}

package org.ow2.proactive.iaas.connector.fixtures;

import org.json.JSONObject;
import org.ow2.proactive.iaas.connector.model.Infrastructure;

public class InfrastructureFixtures {

	public static String getInfrastructureAsaString(String name, String endPoint, String userName, String credential) {
		JSONObject jsonObject = new JSONObject(getInfrastructure(name, endPoint, userName, credential));
		return jsonObject.toString();
	}

	public static Infrastructure getInfrastructure(String name, String endPoint, String userName, String credential) {
		return new Infrastructure(name, endPoint, userName, credential);
	}

}

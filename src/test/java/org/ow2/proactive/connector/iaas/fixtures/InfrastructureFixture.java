package org.ow2.proactive.connector.iaas.fixtures;

import org.json.JSONObject;
import org.ow2.proactive.connector.iaas.model.Infrastructure;

public class InfrastructureFixture {

	public static String getInfrastructureAsaString(String name, String type, String endPoint, String userName, String credential) {
		JSONObject jsonObject = new JSONObject(getInfrastructure(name, type, endPoint, userName, credential));
		return jsonObject.toString();
	}

	public static Infrastructure getInfrastructure(String name, String type, String endPoint, String userName, String credential) {
		return new Infrastructure(name, type, endPoint, userName, credential);
	}
	
	public static Infrastructure getSimpleInfrastructure( String type) {
		return new Infrastructure("id-"+type, type,"endPoint", "userName", "credential");
	}

}

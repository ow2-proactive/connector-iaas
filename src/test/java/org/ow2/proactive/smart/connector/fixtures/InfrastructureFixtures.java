package org.ow2.proactive.smart.connector.fixtures;

import org.ow2.proactive.smart.connector.model.Infrastructure;

public class InfrastructureFixtures {

	public static String getInfrastructureAsaString(String name, String endPoint, String userName, String credential) {
		StringBuilder infrastructure = new StringBuilder();
		infrastructure.append("{");
		infrastructure.append("\"name\": \"" + name + "\",");
		infrastructure.append("\"endPoint\": \"" + endPoint + "\",");
		infrastructure.append("\"userName\": \"" + userName + "\",");
		infrastructure.append("\"credential\": \"" + credential + "\"");
		infrastructure.append("}");
		return infrastructure.toString();
	}

	public static Infrastructure getInfrastructure(String name, String endPoint, String username, String credential) {
		Infrastructure infrastructure = new Infrastructure();
		return infrastructure;
	}

}

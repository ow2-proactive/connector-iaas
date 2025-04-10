/*
 * ProActive Parallel Suite(TM):
 * The Open Source library for parallel and distributed
 * Workflows & Scheduling, Orchestration, Cloud Automation
 * and Big Data Analysis on Enterprise Grids & Clouds.
 *
 * Copyright (c) 2007 - 2017 ActiveEon
 * Contact: contact@activeeon.com
 *
 * This library is free software: you can redistribute it and/or
 * modify it under the terms of the GNU Affero General Public License
 * as published by the Free Software Foundation: version 3 of
 * the License.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Affero General Public License for more details.
 *
 * You should have received a copy of the GNU Affero General Public License
 * along with this program. If not, see <http://www.gnu.org/licenses/>.
 *
 * If needed, contact us to obtain a release under GPL Version 2 or 3
 * or a different license than the AGPL.
 */
package org.ow2.proactive.connector.iaas.fixtures;

import org.json.JSONObject;
import org.ow2.proactive.connector.iaas.model.Infrastructure;
import org.ow2.proactive.connector.iaas.model.InfrastructureScope;


public class InfrastructureFixture {

    public static String getInfrastructureAsString(String name, String type, String endPoint, String userName,
            String password, InfrastructureScope scope, String region, String identityVersion) {
        JSONObject jsonObject = new JSONObject(getInfrastructure(name,
                                                                 type,
                                                                 endPoint,
                                                                 userName,
                                                                 password,
                                                                 scope,
                                                                 region,
                                                                 identityVersion));
        return jsonObject.toString();
    }

    public static String getInfrastructureAsString(String name, String type, String endPoint, String userName,
            String password) {
        return getInfrastructureAsString(name, type, endPoint, userName, password, null, null, null);
    }

    public static Infrastructure getInfrastructure(String name, String type, String endPoint, String username,
            String password, InfrastructureScope scope, String region, String identityVersion) {
        return new Infrastructure(name,
                                  type,
                                  endPoint,
                                  CredentialsFixtures.getInfrastructureCredentials(username, password),
                                  null,
                                  null,
                                  null,
                                  null,
                                  false,
                                  scope,
                                  region,
                                  identityVersion);
    }

    public static Infrastructure getInfrastructure(String name, String type, String endPoint, String username,
            String password) {

        return getInfrastructure(name, type, endPoint, username, password, null, null, null);

    }

    public static Infrastructure getAzureInfrastructureWithEnvironment(String name, String type, String clientId,
            String projectId, String secret, String domain, String subscriptionId, String authenticationEndpoint,
            String managementEndpoint, String resourceManagerEndpoint, String graphEndpoint) {
        return new Infrastructure(name,
                                  type,
                                  null,
                                  CredentialsFixtures.getInfrastructureCredentials(clientId,
                                                                                   projectId,
                                                                                   secret,
                                                                                   domain,
                                                                                   subscriptionId),
                                  authenticationEndpoint,
                                  managementEndpoint,
                                  resourceManagerEndpoint,
                                  graphEndpoint,
                                  false,
                                  null,
                                  null,
                                  null);
    }

    public static Infrastructure getAzureInfrastructure(String name, String type, String clientId, String projectId,
            String secret, String domain, String subscriptionId) {
        return new Infrastructure(name,
                                  type,
                                  null,
                                  CredentialsFixtures.getInfrastructureCredentials(clientId,
                                                                                   projectId,
                                                                                   secret,
                                                                                   domain,
                                                                                   subscriptionId),
                                  null,
                                  null,
                                  null,
                                  null,
                                  false,
                                  null,
                                  null,
                                  null);
    }

    public static Infrastructure getSimpleInfrastructure(String type) {
        return new Infrastructure("id-" + type,
                                  type,
                                  "endPoint",
                                  CredentialsFixtures.getInfrastructureCredentials("userName", "password"),
                                  null,
                                  null,
                                  null,
                                  null,
                                  false,
                                  null,
                                  null,
                                  null);
    }

    public static Infrastructure getSimpleInfrastructure(String id, String type) {
        return new Infrastructure(id,
                                  type,
                                  "endPoint",
                                  CredentialsFixtures.getInfrastructureCredentials("userName", "password"),
                                  null,
                                  null,
                                  null,
                                  null,
                                  false,
                                  null,
                                  null,
                                  null);
    }

    public static Infrastructure getSimpleInfrastructure(String type, boolean removeOnShutdown) {
        return new Infrastructure("id-" + type,
                                  type,
                                  "endPoint",
                                  CredentialsFixtures.getInfrastructureCredentials("userName", "password"),
                                  null,
                                  null,
                                  null,
                                  null,
                                  removeOnShutdown,
                                  null,
                                  null,
                                  null);
    }

}

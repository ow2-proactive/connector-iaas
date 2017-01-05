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


public class InfrastructureFixture {

    public static String getInfrastructureAsaString(String name, String type, String endPoint, String userName,
            String password) {
        JSONObject jsonObject = new JSONObject(getInfrastructure(name, type, endPoint, userName, password));
        return jsonObject.toString();
    }

    public static Infrastructure getInfrastructure(String name, String type, String endPoint, String username,
            String password) {
        return new Infrastructure(name, type, endPoint, CredentialsFixtures.getCredentials(username, password), false);
    }

    public static Infrastructure getSimpleInfrastructure(String type) {
        return new Infrastructure("id-" + type,
                                  type,
                                  "endPoint",
                                  CredentialsFixtures.getCredentials("userName", "password"),
                                  false);
    }

    public static Infrastructure getSimpleInfrastructure(String type, boolean removeOnShutdown) {
        return new Infrastructure("id-" + type,
                                  type,
                                  "endPoint",
                                  CredentialsFixtures.getCredentials("userName", "password"),
                                  removeOnShutdown);
    }

}

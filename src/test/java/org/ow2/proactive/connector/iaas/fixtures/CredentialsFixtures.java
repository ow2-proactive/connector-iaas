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

import org.ow2.proactive.connector.iaas.model.InfrastructureCredentials;
import org.ow2.proactive.connector.iaas.model.InstanceCredentials;


public class CredentialsFixtures {

    public static InstanceCredentials getInstanceCredentials(String username, String password) {
        return new InstanceCredentials(username, password, null, null, null);
    }

    public static InstanceCredentials getInstanceCredentialsWithKeyName(String publicKeyName) {
        return new InstanceCredentials(null, null, publicKeyName, null, null);
    }

    public static InstanceCredentials getInstanceCredentialsWithKey(String publicKey) {
        return new InstanceCredentials(null, null, null, publicKey, null);
    }

    public static InfrastructureCredentials getInfrastructureCredentials(String username, String password) {
        return new InfrastructureCredentials(username, password, null, null);
    }

    public static InfrastructureCredentials getInfrastructureCredentials(String clientId, String secret, String domain,
            String subscriptionId) {
        return new InfrastructureCredentials(clientId, secret, domain, subscriptionId);
    }
}

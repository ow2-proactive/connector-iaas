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
package org.ow2.proactive.connector.iaas.cloud.provider.maas;

import org.ow2.proactive.connector.iaas.model.Infrastructure;
import org.ow2.proactive.connector.maas.MaasClient;
import org.springframework.remoting.RemoteConnectFailureException;
import org.springframework.stereotype.Component;

/**
 * @author ActiveEon Team
 * @since 12/01/17
 */
@Component
public class MaasProviderClientBuilder {

    public MaasClient buildMaasClientFromInfrastructure(Infrastructure infrastructure) {

        // TODO: add ignoreCert boolean !!
        try {
            return new MaasClient(infrastructure.getEndpoint(), infrastructure.getCredentials().getPassword());
        } catch(RemoteConnectFailureException e) {
            throw new RuntimeException(
                    "ERROR trying to create MaasClient with infrastructure : " + infrastructure, e);
        }
    }
}

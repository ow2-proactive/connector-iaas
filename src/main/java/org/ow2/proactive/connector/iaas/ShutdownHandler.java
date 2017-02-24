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
package org.ow2.proactive.connector.iaas;

import javax.annotation.PreDestroy;

import org.apache.log4j.Logger;
import org.ow2.proactive.connector.iaas.service.InfrastructureService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;


@Component
public class ShutdownHandler {

    private final Logger logger = Logger.getLogger(ShutdownHandler.class);

    @Autowired
    private InfrastructureService infrastructureService;

    @PreDestroy
    public synchronized void removeAllInfrastructures() {
        infrastructureService.getAllSupportedInfrastructure().values().forEach(infrastructure -> {
            try {
                if (infrastructure.isToBeRemovedOnShutdown()) {
                    infrastructureService.deleteInfrastructureWithCreatedInstances(infrastructure);
                } else {
                    infrastructureService.deleteInfrastructure(infrastructure);
                }
            } catch (Exception e) {
                logger.error("Shutdown ERROR when trying to delete infrastructure : " + infrastructure, e);
            }
        });
    }

}

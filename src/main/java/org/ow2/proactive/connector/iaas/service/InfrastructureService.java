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
package org.ow2.proactive.connector.iaas.service;

import java.util.HashSet;
import java.util.Map;

import org.ow2.proactive.connector.iaas.cache.InfrastructureCache;
import org.ow2.proactive.connector.iaas.cache.InstanceCache;
import org.ow2.proactive.connector.iaas.cloud.CloudManager;
import org.ow2.proactive.connector.iaas.model.Infrastructure;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;


@Service
public class InfrastructureService {

    @Autowired
    private InfrastructureCache infrastructureCache;

    @Autowired
    private InstanceCache instanceCache;

    @Autowired
    private CloudManager cloudManager;

    @Autowired
    private InstanceService instanceService;

    public Map<String, Infrastructure> getAllSupportedInfrastructure() {
        return infrastructureCache.getSupportedInfrastructures();
    }

    public Infrastructure registerInfrastructure(Infrastructure infrastructure) {
        infrastructureCache.registerInfrastructure(infrastructure);
        instanceCache.registerInfrastructureInstances(infrastructure, new HashSet<>());
        return infrastructure;
    }

    public void deleteInfrastructure(Infrastructure infrastructure) {
        cloudManager.deleteInfrastructure(infrastructure);
        infrastructureCache.deleteInfrastructure(infrastructure);
        instanceCache.deleteInfrastructure(infrastructure);
    }

    public void deleteInfrastructureWithCreatedInstances(Infrastructure infrastructure) {
        instanceService.deleteCreatedInstances(infrastructure.getId());
        cloudManager.deleteInfrastructure(infrastructure);
        infrastructureCache.deleteInfrastructure(infrastructure);
        instanceCache.deleteInfrastructure(infrastructure);
    }

    public Infrastructure getInfrastructure(String infrastructureId) {
        return infrastructureCache.getSupportedInfrastructures().get(infrastructureId);
    }
}

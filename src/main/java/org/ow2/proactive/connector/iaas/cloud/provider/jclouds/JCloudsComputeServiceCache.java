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
package org.ow2.proactive.connector.iaas.cloud.provider.jclouds;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.function.Function;

import org.jclouds.compute.ComputeService;
import org.ow2.proactive.connector.iaas.model.Infrastructure;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;


@Component
public class JCloudsComputeServiceCache {

    @Autowired
    private JCloudsComputeServiceBuilder computeServiceBuilder;

    private Map<Infrastructure, ComputeService> computeServiceCache;

    public JCloudsComputeServiceCache() {
        computeServiceCache = new ConcurrentHashMap<Infrastructure, ComputeService>();
    }

    public ComputeService getComputeService(Infrastructure infrastructure) {
        return buildComputeService.apply(infrastructure);
    }

    public void removeComputeService(Infrastructure infrastructure) {
        computeServiceCache.remove(infrastructure);
    }

    private Function<Infrastructure, ComputeService> buildComputeService = memoise(infrastructure -> {
        return computeServiceBuilder.buildComputeServiceFromInfrastructure(infrastructure);
    });

    private Function<Infrastructure, ComputeService> memoise(Function<Infrastructure, ComputeService> fn) {
        return (a) -> computeServiceCache.computeIfAbsent(a, fn);
    }

}

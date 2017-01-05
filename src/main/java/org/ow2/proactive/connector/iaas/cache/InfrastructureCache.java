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
package org.ow2.proactive.connector.iaas.cache;

import java.util.Map;
import java.util.stream.Collectors;

import org.ow2.proactive.connector.iaas.model.Infrastructure;
import org.springframework.stereotype.Component;

import jersey.repackaged.com.google.common.collect.ImmutableMap;
import lombok.Getter;


@Component
public class InfrastructureCache {
    @Getter
    private volatile ImmutableMap<String, Infrastructure> supportedInfrastructures;

    public InfrastructureCache() {
        this.supportedInfrastructures = ImmutableMap.of();
    }

    public void registerInfrastructure(Infrastructure infrastructure) {
        Map<String, Infrastructure> tempInfrastructures = cloneSupportedInfrastructures();

        tempInfrastructures.put(infrastructure.getId(), infrastructure);
        supportedInfrastructures = ImmutableMap.copyOf(tempInfrastructures);
    }

    public void deleteInfrastructure(Infrastructure infrastructure) {
        Map<String, Infrastructure> tempInfrastructures = cloneSupportedInfrastructures();

        tempInfrastructures.remove(infrastructure.getId());
        supportedInfrastructures = ImmutableMap.copyOf(tempInfrastructures);
    }

    private Map<String, Infrastructure> cloneSupportedInfrastructures() {
        return supportedInfrastructures.entrySet()
                                       .stream()
                                       .collect(Collectors.toMap(Map.Entry::getKey, e -> e.getValue()));
    }
}

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

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import java.util.Set;

import javax.ws.rs.NotFoundException;

import org.apache.commons.lang3.tuple.Pair;
import org.ow2.proactive.connector.iaas.cloud.CloudManager;
import org.ow2.proactive.connector.iaas.model.NodeCandidate;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;


@Service
public class NodeCandidateService {

    @Autowired
    private InfrastructureService infrastructureService;

    @Autowired
    private CloudManager cloudManager;

    public Map<String, Object> getNodeCandidate(String infrastructureId, String region, String imageReq, String token) {
        return Optional.ofNullable(infrastructureService.getInfrastructure(infrastructureId)).map(infra -> {
            Pair<String, Set<NodeCandidate>> result = cloudManager.getNodeCandidate(infra, region, imageReq, token);

            Map<String, Object> mappedResults = new HashMap<>();
            mappedResults.put("nextToken", result.getLeft());
            mappedResults.put("nodeCandidates", result.getRight());
            return mappedResults;
        }).orElseThrow(() -> new NotFoundException("infrastructure id  : " + infrastructureId + " does not exists"));
    }

}

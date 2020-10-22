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
package org.ow2.proactive.connector.iaas.rest;

import java.util.Set;

import javax.ws.rs.*;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;

import org.ow2.proactive.connector.iaas.model.NodeCandidate;
import org.ow2.proactive.connector.iaas.service.NodeCandidateService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import lombok.extern.log4j.Log4j2;


@Path("/infrastructures")
@Component
@Log4j2
public class NodeCandidateRest {

    @Autowired
    public NodeCandidateService nodeCandidateService;

    @POST
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces("application/json")
    @Path("{infrastructureId}/nodecandidates")
    public Response getNodeCandidate(@PathParam("infrastructureId") String infrastructureId,
            @QueryParam("region") String region, @QueryParam("imageReq") String imageReq) {
        log.info("Receive getNodeCandidate request for imageReq [{}] under infrastructure [{}] in region [{}] ",
                 imageReq,
                 infrastructureId,
                 region);
        try {
            Set<NodeCandidate> result = nodeCandidateService.getNodeCandidate(infrastructureId, region, imageReq);
            return Response.ok(result).build();
        } catch (Exception e) {
            log.error("An error occurred while retrieving node candidate infrastructureId={}: {}", infrastructureId, e);
            return Response.serverError().build();
        }
    }
}

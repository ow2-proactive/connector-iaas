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

import javax.ws.rs.GET;
import javax.ws.rs.NotFoundException;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;

import org.ow2.proactive.connector.iaas.service.RegionService;
import org.ow2.proactive.connector.iaas.util.ErrorResponse;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import lombok.extern.log4j.Log4j2;


@Path("/infrastructures")
@Component
@Log4j2
public class RegionRest {

    @Autowired
    private RegionService regionService;

    @GET
    @Path("{infrastructureId}/regions")
    @Produces(MediaType.APPLICATION_JSON)
    public Response listAllRegions(@PathParam("infrastructureId") String infrastructureId) {
        try {
            log.info("Received get all regions request for infrastructureID " + infrastructureId);
            return Response.ok(regionService.getAllRegions(infrastructureId)).build();
        } catch (IllegalArgumentException e) {
            return ErrorResponse.handleIllegalArgument("For infrastructureID " + infrastructureId + ": " +
                                                       e.getMessage(), e);
        } catch (NotFoundException e) {
            return ErrorResponse.handleNotFound("For infrastructureID " + infrastructureId + ": " + e.getMessage(), e);
        } catch (Exception e) {
            return ErrorResponse.handleServerError("While retrieving all regions for infrastructureID " +
                                                   infrastructureId + ": " + e.getMessage(), e);
        }
    }
}

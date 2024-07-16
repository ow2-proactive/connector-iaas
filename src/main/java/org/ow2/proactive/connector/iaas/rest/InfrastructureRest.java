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

import java.util.Optional;

import javax.ws.rs.Consumes;
import javax.ws.rs.DELETE;
import javax.ws.rs.GET;
import javax.ws.rs.NotFoundException;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.QueryParam;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;

import org.ow2.proactive.connector.iaas.model.Infrastructure;
import org.ow2.proactive.connector.iaas.service.InfrastructureService;
import org.ow2.proactive.connector.iaas.util.ErrorResponse;
import org.ow2.proactive.connector.iaas.util.JacksonUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import lombok.extern.log4j.Log4j2;


@Path("/infrastructures")
@Component
@Log4j2
public class InfrastructureRest {

    @Autowired
    private InfrastructureService infrastructureService;

    @GET
    @Produces(MediaType.APPLICATION_JSON)
    public Response getAllSupportedInfrastructure() {
        try {
            log.info("Received get all request");
            return Response.ok(infrastructureService.getAllSupportedInfrastructure()).build();
        } catch (Exception e) {
            return ErrorResponse.handleServerError("While executing get all request for infrastructures: " +
                                                   e.getMessage(), e);
        }
    }

    @GET
    @Path("/{infrastructureId}")
    @Produces(MediaType.APPLICATION_JSON)
    public Response getInfrastructure(@PathParam("infrastructureId") String infrastructureId) {
        try {
            log.info("Received get request for infrastructureID: " + infrastructureId);
            return Response.ok(infrastructureService.getInfrastructure(infrastructureId)).build();
        } catch (IllegalArgumentException e) {
            return ErrorResponse.handleIllegalArgument("For infrastructureID " + infrastructureId + ": " +
                                                       e.getMessage(), e);
        } catch (NotFoundException e) {
            return ErrorResponse.handleNotFound("For infrastructureID " + infrastructureId + ": " + e.getMessage(), e);
        } catch (Exception e) {
            return ErrorResponse.handleServerError("While retrieving infrastructureID " + infrastructureId + ": " +
                                                   e.getMessage(), e);
        }

    }

    @DELETE
    @Path("/{infrastructureId}")
    @Produces(MediaType.APPLICATION_JSON)
    public Response deleteInfrastructureById(@PathParam("infrastructureId") String infrastructureId,
            @QueryParam("deleteInstances") Boolean deleteInstances) {
        try {
            log.info("Received delete request for infrastructureID: " + infrastructureId + " with delete instances = " +
                     deleteInstances);
            Optional.ofNullable(infrastructureService.getInfrastructure(infrastructureId)).ifPresent(infrastructure -> {
                if (Optional.ofNullable(deleteInstances).orElse(false)) {
                    infrastructureService.deleteInfrastructureWithCreatedInstances(infrastructure);
                } else {
                    infrastructureService.deleteInfrastructure(infrastructure);
                }
            });
            return Response.ok(infrastructureService.getAllSupportedInfrastructure()).build();
        } catch (IllegalArgumentException e) {
            return ErrorResponse.handleIllegalArgument("For infrastructureID " + infrastructureId + ": " +
                                                       e.getMessage(), e);
        } catch (NotFoundException e) {
            return ErrorResponse.handleNotFound("For infrastructureID " + infrastructureId + ": " + e.getMessage(), e);
        } catch (Exception e) {
            return ErrorResponse.handleServerError("While deleting infrastructureID " + infrastructureId + ": " +
                                                   e.getMessage(), e);
        }

    }

    @POST
    @Consumes("application/json")
    @Produces(MediaType.APPLICATION_JSON)
    public Response registerInfrastructure(final String infrastructureJson) {
        Infrastructure infrastructure = null;
        try {
            // Validate input
            if (infrastructureJson == null || infrastructureJson.isEmpty()) {
                String errorMessage = "Input JSON is null or empty";
                log.error(errorMessage);
                return Response.status(Response.Status.BAD_REQUEST)
                               .entity(new ErrorResponse("400", errorMessage))
                               .build();
            }

            // Convert JSON to Infrastructure object
            infrastructure = JacksonUtil.convertFromJson(infrastructureJson, Infrastructure.class);
            log.info("Received create infrastructure request with parameters " + infrastructure);

            // Call the service layer
            Infrastructure result = infrastructureService.registerInfrastructure(infrastructure);

            // Return success response
            return Response.ok(result).build();
        } catch (IllegalArgumentException e) {
            return ErrorResponse.handleIllegalArgument("For infrastructure " + infrastructure + ": " + e.getMessage(),
                                                       e);
        } catch (NotFoundException e) {
            return ErrorResponse.handleNotFound("For infrastructure " + infrastructure + ": " + e.getMessage(), e);
        } catch (Exception e) {
            return ErrorResponse.handleServerError("While creating infrastructure " + infrastructure + ": " +
                                                   e.getMessage(), e);
        }
    }
}

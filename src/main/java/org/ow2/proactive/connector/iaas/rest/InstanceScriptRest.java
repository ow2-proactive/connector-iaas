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

import java.util.Arrays;
import java.util.List;
import java.util.Optional;

import javax.ws.rs.Consumes;
import javax.ws.rs.NotFoundException;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.QueryParam;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;

import org.ow2.proactive.connector.iaas.model.InstanceScript;
import org.ow2.proactive.connector.iaas.model.ScriptResult;
import org.ow2.proactive.connector.iaas.service.InstanceScriptService;
import org.ow2.proactive.connector.iaas.util.ErrorResponse;
import org.ow2.proactive.connector.iaas.util.JacksonUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import com.google.common.collect.Lists;

import lombok.extern.log4j.Log4j2;


@Path("/infrastructures")
@Component
@Log4j2
public class InstanceScriptRest {

    @Autowired
    private InstanceScriptService instanceScriptService;

    @POST
    @Path("{infrastructureId}/instances/scripts")
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON)
    public Response executeScript(@PathParam("infrastructureId") String infrastructureId,
            @QueryParam("instanceId") String instanceId, @QueryParam("instanceTag") String instanceTag,
            final String instanceScriptJson) {
        try {
            log.info("Received request to execute script on infrastructure id " + infrastructureId +
                     " and instance id " + instanceId + " and instance tag " + instanceTag);
            InstanceScript instanceScript = JacksonUtil.convertFromJson(instanceScriptJson, InstanceScript.class);
            final List<ScriptResult> scriptResults = Optional.ofNullable(instanceId)
                                                             .map(i -> Lists.newArrayList(instanceScriptService.executeScriptOnInstance(infrastructureId,
                                                                                                                                        instanceId,
                                                                                                                                        instanceScript)))
                                                             .orElseGet(() -> Lists.newArrayList(instanceScriptService.executeScriptOnInstanceTag(infrastructureId,
                                                                                                                                                  instanceTag,
                                                                                                                                                  instanceScript)));
            log.info("Script results " + Arrays.toString(scriptResults.toArray()));
            return Response.ok(scriptResults).build();
        } catch (IllegalArgumentException e) {
            // Handle invalid arguments
            String errorMessage = "Invalid argument for executing script for infrastructureID " + infrastructureId +
                                  " and instance id " + instanceId + " and instance tag " + instanceTag + ": " +
                                  e.getMessage();
            log.error(errorMessage, e);
            return Response.status(Response.Status.BAD_REQUEST).entity(new ErrorResponse("400", errorMessage)).build();

        } catch (NotFoundException e) {
            // Handle not found exceptions
            String errorMessage = "Resource not found for executing script for infrastructureID " + infrastructureId +
                                  " and instance id " + instanceId + " and instance tag " + instanceTag + ": " +
                                  e.getMessage();
            log.error(errorMessage, e);
            return Response.status(Response.Status.NOT_FOUND).entity(new ErrorResponse("404", errorMessage)).build();

        } catch (Exception e) {
            // Handle any other unexpected exceptions
            String errorMessage = "An unexpected error occurred while executing script for infrastructureID " +
                                  infrastructureId + " and instance id " + instanceId + " and instance tag " +
                                  instanceTag + ": " + e.getMessage();
            log.error(errorMessage, e);
            return Response.status(Response.Status.INTERNAL_SERVER_ERROR)
                           .entity(new ErrorResponse("500", errorMessage))
                           .build();
        }
    }

}

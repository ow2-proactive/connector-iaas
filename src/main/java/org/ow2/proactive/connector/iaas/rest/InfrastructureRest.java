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
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.QueryParam;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;

import org.ow2.proactive.connector.iaas.model.Infrastructure;
import org.ow2.proactive.connector.iaas.service.InfrastructureService;
import org.ow2.proactive.connector.iaas.util.JacksonUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;


@Path("/infrastructures")
@Component
public class InfrastructureRest {

    @Autowired
    private InfrastructureService infrastructureService;

    @GET
    @Produces(MediaType.APPLICATION_JSON)
    public Response getAllSupportedInfrastructure() {
        return Response.ok(infrastructureService.getAllSupportedInfrastructure()).build();
    }

    @GET
    @Path("/{infrastructureId}")
    @Produces(MediaType.APPLICATION_JSON)
    public Response getInfrastructure(@PathParam("infrastructureId") String infrastructureId) {
        return Response.ok(infrastructureService.getInfrastructure(infrastructureId)).build();
    }

    @DELETE
    @Path("/{infrastructureId}")
    @Produces(MediaType.APPLICATION_JSON)
    public Response deleteInfrastructureById(@PathParam("infrastructureId") String infrastructureId,
            @QueryParam("deleteInstances") Boolean deleteInstances) {
        Optional.ofNullable(infrastructureService.getInfrastructure(infrastructureId)).ifPresent(infrastructure -> {
            if (Optional.ofNullable(deleteInstances).orElse(false)) {
                infrastructureService.deleteInfrastructureWithCreatedInstances(infrastructure);
            } else {
                infrastructureService.deleteInfrastructure(infrastructure);
            }
        });
        return Response.ok(infrastructureService.getAllSupportedInfrastructure()).build();
    }

    @POST
    @Consumes("application/json")
    @Produces(MediaType.APPLICATION_JSON)
    public Response registerInfrastructure(final String infrastructureJson) {
        Infrastructure infrastructure = JacksonUtil.convertFromJson(infrastructureJson, Infrastructure.class);
        return Response.ok(infrastructureService.registerInfrastructure(infrastructure)).build();
    }

}

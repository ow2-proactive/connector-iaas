package org.ow2.proactive.connector.iaas.rest;

import javax.ws.rs.Consumes;
import javax.ws.rs.DELETE;
import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.core.Response;

import org.ow2.proactive.connector.iaas.model.Infrastructure;
import org.ow2.proactive.connector.iaas.service.InfrastructureService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import com.aol.micro.server.auto.discovery.Rest;
import com.aol.micro.server.rest.jackson.JacksonUtil;


@Path("/infrastructures")
@Component
@Rest(isSingleton = true)
public class InfrastructureRest {

    @Autowired
    private InfrastructureService infrastructureService;

    @GET
    @Produces("application/json")
    public Response getAllSupportedInfrastructure() {
        return Response.ok(infrastructureService.getAllSupportedInfrastructure()).build();
    }

    @GET
    @Path("/{infrastructureId}")
    @Produces("application/json")
    public Response getInfrastructure(@PathParam("infrastructureId") String infrastructureId) {
        return Response.ok(infrastructureService.getInfrastructure(infrastructureId)).build();
    }

    @DELETE
    @Path("/{infrastructureId}")
    @Produces("application/json")
    public Response deleteInfrastructureByName(@PathParam("infrastructureId") String infrastructureId) {
        infrastructureService.deleteInfrastructure(infrastructureService.getInfrastructure(infrastructureId));
        return Response.ok(infrastructureService.getAllSupportedInfrastructure()).build();
    }

    @POST
    @Consumes("application/json")
    @Produces("application/json")
    public Response registerInfrastructure(final String infrastructureJson) {
        Infrastructure infrastructure = JacksonUtil.convertFromJson(infrastructureJson, Infrastructure.class);
        return Response.ok(infrastructureService.registerInfrastructure(infrastructure)).build();
    }

}

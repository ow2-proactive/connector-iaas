package org.ow2.proactive.connector.iaas.rest;

import javax.ws.rs.Consumes;
import javax.ws.rs.DELETE;
import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.QueryParam;
import javax.ws.rs.core.Response;

import org.ow2.proactive.connector.iaas.model.Instance;
import org.ow2.proactive.connector.iaas.service.InstanceService;
import com.aol.micro.server.rest.jackson.JacksonUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.json.JacksonJsonParser;
import org.springframework.stereotype.Component;


@Path("/infrastructures")
@Component
public class InstanceRest {

    @Autowired
    private InstanceService instanceService;

    @POST
    @Consumes("application/json")
    @Produces("application/json")
    @Path("{infrastructureId}/instances")
    public Response createInstance(@PathParam("infrastructureId") String infrastructureId,
            final String instanceJson) {
        Instance instance = JacksonUtil.convertFromJson(instanceJson, Instance.class);
        return Response.ok(instanceService.createInstance(infrastructureId, instance)).build();
    }

    @GET
    @Path("{infrastructureId}/instances")
    @Produces("application/json")
    public Response listAllInstance(@PathParam("infrastructureId") String infrastructureId) {
        return Response.ok(instanceService.getAllInstances(infrastructureId)).build();
    }

    @DELETE
    @Path("{infrastructureId}/instances")
    @Produces("application/json")
    public Response deleteInstance(@PathParam("infrastructureId") String infrastructureId,
            @QueryParam("instanceId") String instanceId) {
        instanceService.deleteInstance(infrastructureId, instanceId);
        return Response.ok().build();
    }

}

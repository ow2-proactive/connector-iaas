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
import javax.ws.rs.core.Response;

import org.ow2.proactive.connector.iaas.model.Instance;
import org.ow2.proactive.connector.iaas.service.InstanceService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import com.aol.micro.server.rest.jackson.JacksonUtil;


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
            @QueryParam("waitForCompletion") Boolean waitForCompletion, final String instanceJson) {
        Instance instance = JacksonUtil.convertFromJson(instanceJson, Instance.class);

        if (Optional.ofNullable(waitForCompletion).isPresent() && waitForCompletion) {
            return Response.ok(instanceService.createInstanceAndWaitForCompletion(infrastructureId, instance))
                    .build();
        } else {
            return Response.ok(instanceService.createInstance(infrastructureId, instance)).build();
        }

    }

    @GET
    @Path("{infrastructureId}/instances")
    @Produces("application/json")
    public Response getInstances(@PathParam("infrastructureId") String infrastructureId,
            @QueryParam("instanceId") String instanceId, @QueryParam("instanceTag") String instanceTag) {

        if (Optional.ofNullable(instanceId).isPresent()) {
            return Response.ok(instanceService.getInstanceById(infrastructureId, instanceId)).build();
        } else if (Optional.ofNullable(instanceTag).isPresent()) {
            return Response.ok(instanceService.getInstanceByTag(infrastructureId, instanceTag)).build();
        } else {
            return Response.ok(instanceService.getAllInstances(infrastructureId)).build();
        }
    }

    @DELETE
    @Path("{infrastructureId}/instances")
    @Produces("application/json")
    public Response deleteInstance(@PathParam("infrastructureId") String infrastructureId,
            @QueryParam("instanceId") String instanceId, @QueryParam("instanceTag") String instanceTag) {

        if (Optional.ofNullable(instanceId).isPresent()) {
            instanceService.deleteInstance(infrastructureId, instanceId);
        } else {
            instanceService.deleteInstanceByTag(infrastructureId, instanceTag);
        }

        return Response.ok().build();
    }

}

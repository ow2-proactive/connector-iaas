package org.ow2.proactive.connector.iaas.rest;

import javax.ws.rs.Consumes;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.QueryParam;
import javax.ws.rs.core.Response;

import org.ow2.proactive.connector.iaas.model.InstanceScript;
import org.ow2.proactive.connector.iaas.service.InstanceScriptService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import com.aol.micro.server.auto.discovery.Rest;
import com.aol.micro.server.rest.jackson.JacksonUtil;


@Path("/infrastructures")
@Component
@Rest(isSingleton = true)
public class InstanceScriptRest {

    @Autowired
    private InstanceScriptService instanceScriptService;

    @POST
    @Path("{infrastructureId}/instance/scripts")
    @Consumes("application/json")
    @Produces("application/json")
    public Response executeScriptByInstanceId(@PathParam("infrastructureId") String infrastructureId,
            @QueryParam("instanceId") String instanceId, final String instanceScriptJson) {
        InstanceScript instanceScript = JacksonUtil.convertFromJson(instanceScriptJson, InstanceScript.class);
        return Response.ok(
                instanceScriptService.executeScriptOnInstance(infrastructureId, instanceId, instanceScript))
                .build();
    }

    @POST
    @Path("{infrastructureId}/tag/scripts")
    @Consumes("application/json")
    @Produces("application/json")
    public Response executeScriptByInstanceTag(@PathParam("infrastructureId") String infrastructureId,
            @QueryParam("instanceTag") String instanceTag, final String instanceScriptJson) {
        InstanceScript instanceScript = JacksonUtil.convertFromJson(instanceScriptJson, InstanceScript.class);
        return Response.ok(instanceScriptService.executeScriptOnInstanceTag(infrastructureId, instanceTag,
                instanceScript)).build();
    }

}

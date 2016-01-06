package org.ow2.proactive.iaas.connector.rest;

import javax.ws.rs.Consumes;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.core.Response;

import org.ow2.proactive.iaas.connector.model.InstanceScript;
import org.ow2.proactive.iaas.connector.service.InstanceScriptService;
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
    @Path("{infrastructureName}/scripts")
    @Consumes("application/json")
    @Produces("application/json")
    public Response executeScript(@PathParam("infrastructureName") String infrastructureName,
            final String instanceScriptJson) {
        InstanceScript instanceScript = JacksonUtil.convertFromJson(instanceScriptJson, InstanceScript.class);
        return Response.ok(instanceScriptService.executeScriptOnInstance(infrastructureName, instanceScript))
                .build();
    }

}

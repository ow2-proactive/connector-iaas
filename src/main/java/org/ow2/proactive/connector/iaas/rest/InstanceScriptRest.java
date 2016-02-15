package org.ow2.proactive.connector.iaas.rest;

import java.util.List;
import java.util.Optional;

import javax.ws.rs.Consumes;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.QueryParam;
import javax.ws.rs.core.Response;

import org.ow2.proactive.connector.iaas.model.InstanceScript;
import org.ow2.proactive.connector.iaas.model.ScriptResult;
import org.ow2.proactive.connector.iaas.service.InstanceScriptService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import com.aol.micro.server.rest.jackson.JacksonUtil;
import com.google.common.collect.Lists;


@Path("/infrastructures")
@Component
public class InstanceScriptRest {

    @Autowired
    private InstanceScriptService instanceScriptService;

    @POST
    @Path("{infrastructureId}/instances/scripts")
    @Consumes("application/json")
    @Produces("application/json")
    public Response executeScript(@PathParam("infrastructureId") String infrastructureId,
            @QueryParam("instanceId") String instanceId, @QueryParam("instanceTag") String instanceTag,
            final String instanceScriptJson) {

        InstanceScript instanceScript = JacksonUtil.convertFromJson(instanceScriptJson, InstanceScript.class);

        final List<ScriptResult> scriptResults = Optional.ofNullable(instanceId)
                .map(i -> Lists.newArrayList(instanceScriptService.executeScriptOnInstance(infrastructureId,
                        instanceId, instanceScript)))
                .orElse(Lists.newArrayList(instanceScriptService.executeScriptOnInstanceTag(infrastructureId,
                        instanceTag, instanceScript)));

        return Response.ok(scriptResults).build();
    }

}

package org.ow2.proactive.iaas.connector.rest;

import javax.ws.rs.Consumes;
import javax.ws.rs.DELETE;
import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.QueryParam;
import javax.ws.rs.core.Response;

import org.ow2.proactive.iaas.connector.model.Instance;
import org.ow2.proactive.iaas.connector.service.InstanceService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import com.aol.micro.server.auto.discovery.Rest;
import com.aol.micro.server.rest.jackson.JacksonUtil;

@Path("/instance")
@Component
@Rest(isSingleton = true)
public class InstanceRest {

	@Autowired
	private InstanceService instanceService;

	@POST
	@Consumes("application/json")
	@Produces("application/json")
	public Response createInstance(final String instanceJson) {
		Instance instance = JacksonUtil.convertFromJson(instanceJson, Instance.class);
		return Response.ok(instanceService.createInstance(instance)).build();
	}

	@GET
	@Path("of/{infrastructureName}/")
	@Produces("application/json")
	public Response listAllInstance(@PathParam("infrastructureName") String infrastructureName) {
		return Response.ok(instanceService.getAllInstances(infrastructureName)).build();
	}

	@DELETE
	@Path("of/{infrastructureName}/")
	@Produces("application/json")
	public Response deleteInstance(@PathParam("infrastructureName") String infrastructureName, @QueryParam("instanceId") String instanceId) {
		instanceService.deleteInstance(infrastructureName, instanceId);
		return Response.ok().build();
	}

}

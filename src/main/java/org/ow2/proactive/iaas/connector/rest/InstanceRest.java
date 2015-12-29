package org.ow2.proactive.iaas.connector.rest;

import javax.ws.rs.Consumes;
import javax.ws.rs.DELETE;
import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.HttpHeaders;
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
	public Response createServer(final String instanceJson) {
		Instance instance = JacksonUtil.convertFromJson(instanceJson, Instance.class);
		instanceService.createInstance(instance);
		return Response.ok(instanceJson).build();
	}

	@GET
	@Produces("application/json")
	public Response listAllServer(@Context HttpHeaders headers) {
		String infrastructureName = headers.getRequestHeader("name").get(0);
		return Response.ok(instanceService.getAllInstances(infrastructureName)).build();
	}

	@GET
	@Path("/{infrastructureName}/")
	@Produces("application/json")
	public Response listAllServer(@PathParam("infrastructureName") String infrastructureName) {
		return Response.ok(instanceService.getAllInstances(infrastructureName)).build();
	}

	@DELETE
	@Path("/{infrastructureName}/{instanceID}")
	@Produces("application/json")
	public Response deleteServer(@PathParam("infrastructureName") String infrastructureName,
			@PathParam("instanceID") String instanceID) {
		instanceService.deleteInstance(infrastructureName, instanceID);
		return Response.ok(instanceService.getAllInstances(infrastructureName)).build();
	}

}

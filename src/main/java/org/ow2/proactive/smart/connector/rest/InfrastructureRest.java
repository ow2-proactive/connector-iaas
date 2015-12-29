package org.ow2.proactive.smart.connector.rest;

import javax.ws.rs.Consumes;
import javax.ws.rs.DELETE;
import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.PUT;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.core.Response;

import org.ow2.proactive.smart.connector.model.Infrastructure;
import org.ow2.proactive.smart.connector.service.InfrastructureService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import com.aol.micro.server.auto.discovery.Rest;
import com.aol.micro.server.rest.jackson.JacksonUtil;

@Path("/infrastructure")
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
	@Path("/{infrastructureName}")
	@Produces("application/json")
	public Response getInfrastructureByName(@PathParam("infrastructureName") String infrastructureName) {
		return Response.ok(infrastructureService.getInfrastructurebyName(infrastructureName)).build();
	}

	@DELETE
	@Path("/{infrastructureName}")
	@Produces("application/json")
	public Response deleteInfrastructureByName(@PathParam("infrastructureName") String infrastructureName) {
		infrastructureService.deleteInfrastructure(infrastructureName);
		return Response.ok(infrastructureService.getAllSupportedInfrastructure()).build();
	}

	@POST
	@Consumes("application/json")
	@Produces("application/json")
	public Response registerInfrastructure(final String infrastructureJson) {
		Infrastructure infrastructure = JacksonUtil.convertFromJson(infrastructureJson, Infrastructure.class);
		infrastructureService.registerInfrastructure(infrastructure);
		return Response.ok(infrastructureService.getAllSupportedInfrastructure()).build();
	}

	@PUT
	@Consumes("application/json")
	@Produces("application/json")
	public Response updateInfrastructure(final String infrastructureJson) {
		Infrastructure infrastructure = JacksonUtil.convertFromJson(infrastructureJson, Infrastructure.class);
		infrastructureService.updateInfrastructure(infrastructure);
		return Response.ok(infrastructureService.getAllSupportedInfrastructure()).build();
	}

}

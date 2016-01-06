package org.ow2.proactive.iaas.connector.rest;

import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.core.Response;

import org.ow2.proactive.iaas.connector.service.ImageService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import com.aol.micro.server.auto.discovery.Rest;


@Path("/infrastructures")
@Component
@Rest(isSingleton = true)
public class ImageRest {

    @Autowired
    private ImageService imageService;

    @GET
    @Path("{infrastructureName}/images")
    @Produces("application/json")
    public Response listAllImage(@PathParam("infrastructureName") String infrastructureName) {
        return Response.ok(imageService.getAllImages(infrastructureName)).build();
    }

}

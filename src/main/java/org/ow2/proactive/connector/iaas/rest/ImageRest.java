package org.ow2.proactive.connector.iaas.rest;

import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.core.Response;

import org.ow2.proactive.connector.iaas.service.ImageService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import com.aol.micro.server.auto.discovery.Rest;


@Path("/infrastructures")
@Component
public class ImageRest {

    @Autowired
    private ImageService imageService;

    @GET
    @Path("{infrastructureId}/images")
    @Produces("application/json")
    public Response listAllImage(@PathParam("infrastructureId") String infrastructureId) {
        return Response.ok(imageService.getAllImages(infrastructureId)).build();
    }

}

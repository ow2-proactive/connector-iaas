package org.ow2.proactive.connector.iaas.app.exception;

import javax.ws.rs.core.Response;
import javax.ws.rs.ext.ExceptionMapper;
import javax.ws.rs.ext.Provider;

import org.json.JSONObject;


@Provider
public class ServerErrorExceptionMapper implements ExceptionMapper<RuntimeException> {
    @Override
    public Response toResponse(RuntimeException ex) {

        JSONObject errorEntity = new JSONObject();
        errorEntity.put("error", ex.getMessage());
        errorEntity.put("cause", ex.getCause());

        return Response.status(Response.Status.INTERNAL_SERVER_ERROR).entity(errorEntity)
                .type("application/json").build();

    }
}

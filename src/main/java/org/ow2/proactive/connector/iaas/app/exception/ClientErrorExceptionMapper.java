package org.ow2.proactive.connector.iaas.app.exception;

import javax.ws.rs.ClientErrorException;
import javax.ws.rs.core.Response;
import javax.ws.rs.ext.ExceptionMapper;
import javax.ws.rs.ext.Provider;

import org.json.JSONObject;


@Provider
public class ClientErrorExceptionMapper implements ExceptionMapper<ClientErrorException> {
    @Override
    public Response toResponse(ClientErrorException ex) {

        JSONObject errorEntity = new JSONObject();
        errorEntity.put("error", ex.getMessage());
        errorEntity.put("cause", ex.getCause());

        return Response.status(Response.Status.BAD_REQUEST).entity(errorEntity).type("application/json")
                .build();
    }
}

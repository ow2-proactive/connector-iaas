package org.ow2.proactive.connector.iaas.app.exception;

import static org.hamcrest.Matchers.is;
import static org.junit.Assert.assertThat;

import javax.ws.rs.core.Response;

import org.json.JSONObject;
import org.junit.Test;


public class ServerErrorExceptionMapperTest {

    @Test
    public void testToResponseRuntimeException() {
        Exception e = new Exception("Original Exception cause");
        e.setStackTrace(new StackTraceElement[] {
                new StackTraceElement("declaringClass", "methodName", "fileName", 123) });
        Response response = new ServerErrorExceptionMapper()
                .toResponse(new RuntimeException("Error message", e));

        assertThat(response.getStatus(), is(Response.Status.INTERNAL_SERVER_ERROR.getStatusCode()));

        JSONObject errorEntity = (JSONObject) response.getEntity();

        assertThat(errorEntity.get("error"), is("Error message"));
        assertThat(
                errorEntity.get("cause").toString().contains("java.lang.Exception: Original Exception cause"),
                is(true));

    }

}

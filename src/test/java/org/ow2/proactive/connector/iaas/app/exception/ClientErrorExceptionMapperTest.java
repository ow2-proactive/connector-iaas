/*
 * ProActive Parallel Suite(TM):
 * The Open Source library for parallel and distributed
 * Workflows & Scheduling, Orchestration, Cloud Automation
 * and Big Data Analysis on Enterprise Grids & Clouds.
 *
 * Copyright (c) 2007 - 2017 ActiveEon
 * Contact: contact@activeeon.com
 *
 * This library is free software: you can redistribute it and/or
 * modify it under the terms of the GNU Affero General Public License
 * as published by the Free Software Foundation: version 3 of
 * the License.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Affero General Public License for more details.
 *
 * You should have received a copy of the GNU Affero General Public License
 * along with this program. If not, see <http://www.gnu.org/licenses/>.
 *
 * If needed, contact us to obtain a release under GPL Version 2 or 3
 * or a different license than the AGPL.
 */
package org.ow2.proactive.connector.iaas.app.exception;

import static org.hamcrest.Matchers.is;
import static org.junit.Assert.assertThat;

import javax.ws.rs.BadRequestException;
import javax.ws.rs.NotFoundException;
import javax.ws.rs.NotSupportedException;
import javax.ws.rs.core.Response;

import org.junit.Test;


public class ClientErrorExceptionMapperTest {

    @Test
    public void testToResponseBadRequestException() {
        Exception e = new Exception("Original Exception cause");
        e.setStackTrace(new StackTraceElement[] { new StackTraceElement("declaringClass",
                                                                        "methodName",
                                                                        "fileName",
                                                                        123) });
        Response response = new ClientErrorExceptionMapper().toResponse(new BadRequestException("Error message", e));

        assertThat(response.getStatus(), is(Response.Status.BAD_REQUEST.getStatusCode()));

        String errorEntity = (String) response.getEntity();

        assertThat(errorEntity.contains("Error message"), is(true));

    }

    @Test
    public void testToResponseNotFoundException() {
        Exception e = new Exception("Original Exception cause");
        e.setStackTrace(new StackTraceElement[] { new StackTraceElement("declaringClass",
                                                                        "methodName",
                                                                        "fileName",
                                                                        123) });
        Response response = new ClientErrorExceptionMapper().toResponse(new NotFoundException("Error message", e));

        assertThat(response.getStatus(), is(Response.Status.BAD_REQUEST.getStatusCode()));

        String errorEntity = (String) response.getEntity();

        assertThat(errorEntity.contains("Error message"), is(true));

    }

    @Test
    public void testToResponseNotSupportedException() {
        Exception e = new Exception("Original Exception cause");
        e.setStackTrace(new StackTraceElement[] { new StackTraceElement("declaringClass",
                                                                        "methodName",
                                                                        "fileName",
                                                                        123) });
        Response response = new ClientErrorExceptionMapper().toResponse(new NotSupportedException("Error message", e));

        assertThat(response.getStatus(), is(Response.Status.BAD_REQUEST.getStatusCode()));

        String errorEntity = (String) response.getEntity();

        assertThat(errorEntity.contains("Error message"), is(true));

    }

}

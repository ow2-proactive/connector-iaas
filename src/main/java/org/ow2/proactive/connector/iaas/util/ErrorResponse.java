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
package org.ow2.proactive.connector.iaas.util;

import javax.ws.rs.core.Response;

import lombok.Getter;
import lombok.extern.log4j.Log4j2;


/**
 * Utility class to handle error responses for RESTful services.
 */
@Log4j2
@Getter
public class ErrorResponse {

    private final String statusCode;

    private final String errorMessage;

    // Constructor for ErrorResponse
    public ErrorResponse(String statusCode, String errorMessage) {
        this.statusCode = statusCode;
        this.errorMessage = errorMessage;
    }

    /**
     * Handle IllegalArgumentException and generate a BAD_REQUEST response.
     * @param message Error message
     * @param e Exception object
     * @return Response object with BAD_REQUEST status and error message
     */
    public static Response handleIllegalArgument(String message, Exception e) {
        message = "Invalid argument: " + message;
        log.error(message, e);
        return Response.status(Response.Status.BAD_REQUEST)
                       .entity(new ErrorResponse(String.valueOf(Response.Status.BAD_REQUEST.getStatusCode()), message))
                       .build();
    }

    /**
     * Handle NotFoundException and generate a NOT_FOUND response.
     * @param message Error message
     * @param e Exception object
     * @return Response object with NOT_FOUND status and error message
     */
    public static Response handleNotFound(String message, Exception e) {
        message = "Resource not found: " + message;
        log.error(message, e);
        return Response.status(Response.Status.NOT_FOUND)
                       .entity(new ErrorResponse(String.valueOf(Response.Status.NOT_FOUND.getStatusCode()), message))
                       .build();
    }

    /**
     * Handle any other Exception and generate an INTERNAL_SERVER_ERROR response.
     * @param message Error message
     * @param e Exception object
     * @return Response object with INTERNAL_SERVER_ERROR status and error message
     */
    public static Response handleServerError(String message, Exception e) {
        message = "An unexpected error occurred: " + message;
        log.error(message, e);
        return Response.status(Response.Status.INTERNAL_SERVER_ERROR)
                       .entity(new ErrorResponse(String.valueOf(Response.Status.INTERNAL_SERVER_ERROR.getStatusCode()),
                                                 message))
                       .build();
    }
}

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
package org.ow2.proactive.connector.iaas.rest;

import java.util.AbstractMap.SimpleImmutableEntry;
import java.util.Optional;

import javax.ws.rs.Consumes;
import javax.ws.rs.DELETE;
import javax.ws.rs.NotFoundException;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.QueryParam;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;

import org.ow2.proactive.connector.iaas.model.Instance;
import org.ow2.proactive.connector.iaas.service.KeyPairService;
import org.ow2.proactive.connector.iaas.util.ErrorResponse;
import org.ow2.proactive.connector.iaas.util.JacksonUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import lombok.extern.log4j.Log4j2;


/**
 * @author ActiveEon Team
 * @since 07/09/17
 */
@Path("/infrastructures")
@Component
@Log4j2
public class KeyPairRest {

    @Autowired
    private KeyPairService keyPairService;

    @POST
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces("application/json")
    @Path("{infrastructureId}/keypairs")
    public Response createKeyPair(@PathParam("infrastructureId") String infrastructureId, final String instanceJson) {
        Instance instance = null;
        try {
            instance = JacksonUtil.convertFromJson(instanceJson, Instance.class);
            log.info("Receive keypair create request for infrastructure id " + infrastructureId + " with parameter " +
                     instance);
            SimpleImmutableEntry<String, String> privateKey = keyPairService.createKeyPair(infrastructureId, instance);
            return Optional.ofNullable(privateKey)
                           .map(privateKeyResponse -> Response.ok(privateKeyResponse).build())
                           .orElse(Response.serverError().build());
        } catch (IllegalArgumentException e) {
            return ErrorResponse.handleIllegalArgument("For infrastructureID " + infrastructureId + " with parameter " +
                                                       instance + ": " + e.getMessage(), e);
        } catch (NotFoundException e) {
            return ErrorResponse.handleNotFound("For infrastructureID " + infrastructureId + " with parameter " +
                                                instance + ": " + e.getMessage(), e);
        } catch (Exception e) {
            return ErrorResponse.handleServerError("While creating key pair for infrastructureID " + infrastructureId +
                                                   " with parameter " + instance + " :" + e.getMessage(), e);
        }
    }

    @DELETE
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON)
    @Path("{infrastructureId}/keypairs")
    public Response deleteKeyPair(@PathParam("infrastructureId") String infrastructureId,
            @QueryParam("keyPairName") String keyPairName, @QueryParam("region") String region) {
        try {
            log.info("Receive delete request for key pair [{}] under infrastructureID [{}] in region [{}]",
                     keyPairName,
                     infrastructureId,
                     region);
            keyPairService.deleteKeyPair(infrastructureId, keyPairName, region);
            return Response.ok().build();
        } catch (IllegalArgumentException e) {
            return ErrorResponse.handleIllegalArgument("For key pair '" + keyPairName + "' under infrastructureID " +
                                                       infrastructureId + " in region '" + region + "': " +
                                                       e.getMessage(), e);
        } catch (NotFoundException e) {
            return ErrorResponse.handleNotFound("For key pair '" + keyPairName + "' under infrastructureID " +
                                                infrastructureId + " in region '" + region + "': " + e.getMessage(), e);
        } catch (Exception e) {
            return ErrorResponse.handleServerError("While deleting key pair '" + keyPairName +
                                                   "' under infrastructureID " + infrastructureId + " in region '" +
                                                   region + "': " + e.getMessage(), e);
        }
    }
}

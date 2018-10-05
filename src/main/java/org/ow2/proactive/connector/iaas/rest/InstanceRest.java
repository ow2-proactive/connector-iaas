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

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

import javax.ws.rs.ClientErrorException;
import javax.ws.rs.Consumes;
import javax.ws.rs.DELETE;
import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.QueryParam;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;

import org.ow2.proactive.connector.iaas.model.Instance;
import org.ow2.proactive.connector.iaas.service.InstanceService;
import org.ow2.proactive.connector.iaas.util.JacksonUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import lombok.extern.log4j.Log4j2;


@Path("/infrastructures")
@Component
@Log4j2
public class InstanceRest {

    @Autowired
    private InstanceService instanceService;

    @POST
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces("application/json")
    @Path("{infrastructureId}/instances")
    public Response createInstance(@PathParam("infrastructureId") String infrastructureId, final String instanceJson) {
        log.info("Received create request for infrastructure " + infrastructureId + " with parameters " + instanceJson);
        Instance instance = JacksonUtil.convertFromJson(instanceJson, Instance.class);
        return Response.ok(instanceService.createInstance(infrastructureId, instance)).build();
    }

    @GET
    @Path("{infrastructureId}/instances")
    @Produces(MediaType.APPLICATION_JSON)
    public Response getInstances(@PathParam("infrastructureId") String infrastructureId,
            @QueryParam("instanceId") String instanceId, @QueryParam("instanceTag") String instanceTag,
            @QueryParam("allInstances") Boolean allInstances) {

        if (Optional.ofNullable(instanceId).isPresent()) {
            log.debug("Received get request for infrastructure " + infrastructureId + " and instance id " + instanceId);
            return Response.ok(instanceService.getInstanceById(infrastructureId, instanceId)).build();
        } else if (Optional.ofNullable(instanceTag).isPresent()) {
            log.debug("Received get request for infrastructure " + infrastructureId + " and instance tag " +
                      instanceTag);
            return Response.ok(instanceService.getInstanceByTag(infrastructureId, instanceTag)).build();
        } else if (Optional.ofNullable(allInstances).isPresent() && allInstances) {
            log.debug("Received get all request for infrastructure " + infrastructureId);
            return Response.ok(instanceService.getAllInstances(infrastructureId)).build();
        } else {
            log.debug("Received get all created request for infrastructure " + infrastructureId);
            return Response.ok(instanceService.getCreatedInstances(infrastructureId)).build();
        }
    }

    @DELETE
    @Path("{infrastructureId}/instances")
    @Produces(MediaType.APPLICATION_JSON)
    public Response deleteInstance(@PathParam("infrastructureId") String infrastructureId,
            @QueryParam("instanceId") String instanceId, @QueryParam("instanceTag") String instanceTag,
            @QueryParam("allCreatedInstances") Boolean allCreatedInstances) {

        if (Optional.ofNullable(instanceId).isPresent()) {
            log.info("Received delete request for infrastructure " + infrastructureId + " and instance id " +
                     instanceId);
            instanceService.deleteInstance(infrastructureId, instanceId);
        } else if (Optional.ofNullable(instanceTag).isPresent()) {
            log.info("Received delete request for infrastructure " + infrastructureId + " and instance tag " +
                     instanceTag);
            instanceService.deleteInstanceByTag(infrastructureId, instanceTag);
        } else if (Optional.ofNullable(allCreatedInstances).isPresent() && allCreatedInstances) {
            log.info("Received delete all request for infrastructure " + infrastructureId);
            instanceService.deleteCreatedInstances(infrastructureId);
        }

        return Response.ok().build();
    }

    @POST
    @Path("{infrastructureId}/instances/publicIp")
    @Produces(MediaType.APPLICATION_JSON)
    public Response createPublicIp(@PathParam("infrastructureId") String infrastructureId,
            @QueryParam("instanceId") String instanceId, @QueryParam("instanceTag") String instanceTag,
            @QueryParam("desiredIp") String optionalDesiredIp) {
        Map<String, String> response = new HashMap<>();
        if (Optional.ofNullable(instanceId).isPresent()) {
            log.info("Received create public IP request for infrastructure " + infrastructureId + " and instance id " +
                     instanceId);
            response.put("publicIp",
                         instanceService.addToInstancePublicIp(infrastructureId, instanceId, optionalDesiredIp));
        } else if (Optional.ofNullable(instanceTag).isPresent()) {
            log.info("Received create public IP request for infrastructure " + infrastructureId + " and instance tag " +
                     instanceTag);
            instanceService.addInstancePublicIpByTag(infrastructureId, instanceTag, optionalDesiredIp);
        } else {
            throw new ClientErrorException("The parameter \"instanceId\" and \"instanceTag\" are  missing.",
                                           Response.Status.BAD_REQUEST);
        }
        return Response.ok(response).build();
    }

    @DELETE
    @Path("{infrastructureId}/instances/publicIp")
    @Produces(MediaType.APPLICATION_JSON)
    public Response removePublicIp(@PathParam("infrastructureId") String infrastructureId,
            @QueryParam("instanceId") String instanceId, @QueryParam("instanceTag") String instanceTag,
            @QueryParam("desiredIp") String optionalDesiredIp) {

        if (Optional.ofNullable(instanceId).isPresent()) {
            log.info("Received delete public IP request for infrastructure " + infrastructureId + " and instance id " +
                     instanceId);
            instanceService.removeInstancePublicIp(infrastructureId, instanceId, optionalDesiredIp);
        } else if (Optional.ofNullable(instanceTag).isPresent()) {
            log.info("Received delete public IP request for infrastructure " + infrastructureId + " and instance tag " +
                     instanceTag);
            instanceService.removeInstancePublicIpByTag(infrastructureId, instanceTag, optionalDesiredIp);
        } else {
            throw new ClientErrorException("The parameters \"instanceId\" and \"instanceTag\" are missing.",
                                           Response.Status.BAD_REQUEST);
        }

        return Response.ok().build();
    }

}

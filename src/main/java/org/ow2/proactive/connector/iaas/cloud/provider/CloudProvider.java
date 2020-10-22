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
package org.ow2.proactive.connector.iaas.cloud.provider;

import java.util.AbstractMap.SimpleImmutableEntry;
import java.util.List;
import java.util.Set;

import org.ow2.proactive.connector.iaas.model.*;


public interface CloudProvider {

    /**
     * Indicate the type of infrastructure managed by the connector.
     * @return A string stating which infrastructure type is managed by the connector
     */
    public String getType();

    /**
     * Identify all cloud regions supported by an infrastructure.
     * @param infrastructure The infrastructure whose supported regions list is to be retrieved
     * @return A set of string listing the regions supported by the infrastructure
     */
    public Set<String> listAvailableRegions(Infrastructure infrastructure);

    /**
     * Create a new instance on an infrastructure.
     * @param infrastructure The infrastructure where the new instance is to be allocated to
     * @param instance The specification of the instance to be created
     * @return The list of the newly registered instances
     */
    public Set<Instance> createInstance(Infrastructure infrastructure, Instance instance);

    /**
     * Terminate an instance from an infrastructure.
     * @param infrastructure The infrastructure hosting the instance to be removed
     * @param instanceId The infrastructure to be removed.
     */
    public void deleteInstance(Infrastructure infrastructure, String instanceId);

    /**
     * List all instances hosted on an infrastructure.
     * @param infrastructure The infrastructure to be exposed
     * @return The list of instances of the hosted instances
     */
    public Set<Instance> getAllInfrastructureInstances(Infrastructure infrastructure);

    /**
     * List all instances deployed on an infrastructure.
     * @param infrastructure The infrastructure to be exposed
     * @return The list of instances of the deployed instances
     */
    public Set<Instance> getCreatedInfrastructureInstances(Infrastructure infrastructure);

    /**
     * Execute a script on an instance identified by an id.
     * @param infrastructure The infrastructure hosting the instance to be controlled
     * @param instanceId The id of the instance the script should be launched on
     * @param instanceScript The script to be run on the instance
     * @return The result of the script execution
     */
    public List<ScriptResult> executeScriptOnInstanceId(Infrastructure infrastructure, String instanceId,
            InstanceScript instanceScript);

    /**
     * Execute a script on instances identified by a tag.
     * @param infrastructure The infrastructure hosting the instance to be controlled
     * @param instanceTag The tag of the instances the script should be launched on
     * @param instanceScript The script to be run on the instance
     * @return The result of the script execution
     */
    public List<ScriptResult> executeScriptOnInstanceTag(Infrastructure infrastructure, String instanceTag,
            InstanceScript instanceScript);

    /**
     * List the images an instance can be booted from.
     * @param infrastructure The infrastructure whose images are to expose
     * @return The set of bootable system images on the infrastructure
     */
    public Set<Image> getAllImages(Infrastructure infrastructure);

    /**
     * List all hardware profiles of an infrastructure an instances can be booted from.
     * @param infrastructure The infrastructure whose hardware profiles are to be exposed
     * @return The set of bootable hardware profiles on the infrastructure
     */
    public Set<Hardware> getAllHardwares(Infrastructure infrastructure);

    /**
     * Remove an infrastructure.
     * @param infrastructure The infrastructure to be unregistered
     */
    public void deleteInfrastructure(Infrastructure infrastructure);

    /**
     * Associate a public IP address to an instance.
     * @param infrastructure The infrastructure hosting the instance to receive a public IP address
     * @param instanceId ID of the the instance to receive the public IP address
     * @param desiredIp IP address to be preferably associated to the instance
     * @return The IP address effectively allocated to the instance
     */
    public String addToInstancePublicIp(Infrastructure infrastructure, String instanceId, String desiredIp);

    /**
     * Dissociate a public IP address from an instance.
     * @param infrastructure The infrastructure hosting the instance to be altered
     * @param instanceId The instance to be dissociated from the public IP address
     * @param desiredIp The public IP address to be dissociated from the instance
     */
    public void removeInstancePublicIp(Infrastructure infrastructure, String instanceId, String desiredIp);

    /**
     * Create a key pair on an infrastructure.
     * @param infrastructure The infrastructure to receive the new key pair
     * @param instance An instance hosted on the infrastructure
     * @return The name and the content of the newly created keypair
     */
    public SimpleImmutableEntry<String, String> createKeyPair(Infrastructure infrastructure, Instance instance);

    /**
     * Remove a key pair from an infrastructure.
     * @param infrastructure The infrastructure hosting the key pair to be removed from
     * @param keyPairName The name of the key pair to be removed
     * @param region
     */
    public void deleteKeyPair(Infrastructure infrastructure, String keyPairName, String region);

    /**
     * List the node candidate results matching a specified image requirements for a specific region of an infrastructure. A node candidate describes the type of operating system, the hardware profile a instance can be allocated from, and its pricing.
     * @param infra The infrastructure whose node candidates are to be exposed
     * @param region The infrastructure region to be examined.
     * @param imageReq The requirements of the system image the node candidates should match
     * @return A list of node candidates for the specified infrastructure and region whose system images match the requirements
     */
    public Set<NodeCandidate> getNodeCandidate(Infrastructure infra, String region, String imageReq);
}

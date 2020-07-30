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

    public String getType();

    public Set<String> listAvailableRegions(Infrastructure infrastructure);

    public Set<Instance> createInstance(Infrastructure infrastructure, Instance instance);

    public void deleteInstance(Infrastructure infrastructure, String instanceId);

    public Set<Instance> getAllInfrastructureInstances(Infrastructure infrastructure);

    public Set<Instance> getCreatedInfrastructureInstances(Infrastructure infrastructure);

    public List<ScriptResult> executeScriptOnInstanceId(Infrastructure infrastructure, String instanceId,
            InstanceScript instanceScript);

    public List<ScriptResult> executeScriptOnInstanceTag(Infrastructure infrastructure, String instanceTag,
            InstanceScript instanceScript);

    public Set<Image> getAllImages(Infrastructure infrastructure);

    public Set<Hardware> getAllHardwares(Infrastructure infrastructure);

    public void deleteInfrastructure(Infrastructure infrastructure);

    public String addToInstancePublicIp(Infrastructure infrastructure, String instanceId, String desiredIp);

    public void removeInstancePublicIp(Infrastructure infrastructure, String instanceId, String desiredIp);

    public SimpleImmutableEntry<String, String> createKeyPair(Infrastructure infrastructure, Instance instance);

    public void deleteKeyPair(Infrastructure infrastructure, String keyPairName, String region);
}

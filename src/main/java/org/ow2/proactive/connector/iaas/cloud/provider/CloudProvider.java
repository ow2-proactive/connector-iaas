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

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;

import org.ow2.proactive.connector.iaas.model.Image;
import org.ow2.proactive.connector.iaas.model.Infrastructure;
import org.ow2.proactive.connector.iaas.model.Instance;
import org.ow2.proactive.connector.iaas.model.InstanceScript;
import org.ow2.proactive.connector.iaas.model.Options;
import org.ow2.proactive.connector.iaas.model.ScriptResult;
import org.ow2.proactive.connector.iaas.model.Tag;


public interface CloudProvider {

    public String getType();

    public Set<Instance> createInstance(Infrastructure infrastructure, Instance instance, Tag connectorIaasTag);

    public void deleteInstance(Infrastructure infrastructure, String instanceId);

    public Set<Instance> getAllInfrastructureInstances(Infrastructure infrastructure);

    public Set<Instance> getCreatedInfrastructureInstances(Infrastructure infrastructure, Tag connectorIaasTag);

    public List<ScriptResult> executeScriptOnInstanceId(Infrastructure infrastructure, String instanceId,
            InstanceScript instanceScript);

    public List<ScriptResult> executeScriptOnInstanceTag(Infrastructure infrastructure, String instanceTag,
            InstanceScript instanceScript);

    public Set<Image> getAllImages(Infrastructure infrastructure);

    public void deleteInfrastructure(Infrastructure infrastructure);

    public String addToInstancePublicIp(Infrastructure infrastructure, String instanceId, String desiredIp);

    public void removeInstancePublicIp(Infrastructure infrastructure, String instanceId, String desiredIp);

    /**
     * Collect tags and ensure that mandatory connector-iaas tag key is not duplicated
     *
     * @param connectorIaasTag  mandatory connector-iaas tag
     * @param options           instance's options that may contain tags
     * @return  the list of all tags
     */
    public default List<Tag> retrieveAllTags(Tag connectorIaasTag, Options options) {
        List<Tag> tags = new ArrayList<>();
        tags.add(connectorIaasTag);
        Optional.ofNullable(options).map(Options::getTags).ifPresent(optionalTags -> {
            tags.addAll(optionalTags.stream()
                                    .filter(optionalTag -> !optionalTag.getKey().equals(connectorIaasTag.getKey()))
                                    .collect(Collectors.toList()));
        });
        return tags;
    }
}

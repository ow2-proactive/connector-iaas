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
package org.ow2.proactive.connector.iaas.cloud;

import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.function.Function;
import java.util.stream.Collectors;

import org.ow2.proactive.connector.iaas.cloud.provider.CloudProvider;
import org.ow2.proactive.connector.iaas.model.Image;
import org.ow2.proactive.connector.iaas.model.Infrastructure;
import org.ow2.proactive.connector.iaas.model.Instance;
import org.ow2.proactive.connector.iaas.model.InstanceScript;
import org.ow2.proactive.connector.iaas.model.ScriptResult;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;


@Service
public class CloudManager {

    private Map<String, CloudProvider> cloudProviderPerType;

    @Autowired
    public CloudManager(List<CloudProvider> cloudProviders) {
        cloudProviderPerType = cloudProviders.stream()
                                             .collect(Collectors.toMap(CloudProvider::getType, Function.identity()));
    }

    public Set<Instance> createInstance(Infrastructure infrastructure, Instance instance) {
        return cloudProviderPerType.get(infrastructure.getType()).createInstance(infrastructure, instance);
    }

    public void deleteInstance(Infrastructure infrastructure, String instanceId) {
        cloudProviderPerType.get(infrastructure.getType()).deleteInstance(infrastructure, instanceId);
    }

    public void deleteInfrastructure(Infrastructure infrastructure) {
        cloudProviderPerType.get(infrastructure.getType()).deleteInfrastructure(infrastructure);
    }

    public Set<Instance> getAllInfrastructureInstances(Infrastructure infrastructure) {
        return cloudProviderPerType.get(infrastructure.getType()).getAllInfrastructureInstances(infrastructure);
    }

    public List<ScriptResult> executeScriptOnInstanceId(Infrastructure infrastructure, String instanceId,
            InstanceScript instanceScript) {
        return cloudProviderPerType.get(infrastructure.getType()).executeScriptOnInstanceId(infrastructure,
                                                                                            instanceId,
                                                                                            instanceScript);
    }

    public List<ScriptResult> executeScriptOnInstanceTag(Infrastructure infrastructure, String instanceTag,
            InstanceScript instanceScript) {
        return cloudProviderPerType.get(infrastructure.getType())
                                   .executeScriptOnInstanceTag(infrastructure, instanceTag, instanceScript);
    }

    public Set<Image> getAllImages(Infrastructure infrastructure) {
        return cloudProviderPerType.get(infrastructure.getType()).getAllImages(infrastructure);
    }

    public String addToInstancePublicIp(Infrastructure infrastructure, String instanceId, String optionalDesiredIp) {
        return cloudProviderPerType.get(infrastructure.getType()).addToInstancePublicIp(infrastructure,
                                                                                        instanceId,
                                                                                        optionalDesiredIp);
    }

    public void removeInstancePublicIp(Infrastructure infrastructure, String instanceId, String optionalDesiredIp) {
        cloudProviderPerType.get(infrastructure.getType()).removeInstancePublicIp(infrastructure,
                                                                                  instanceId,
                                                                                  optionalDesiredIp);
    }
}

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
package org.ow2.proactive.connector.iaas.cloud.provider.jclouds.google;

import java.util.AbstractMap.SimpleImmutableEntry;
import java.util.Set;

import org.apache.commons.lang3.NotImplementedException;
import org.jclouds.compute.options.RunScriptOptions;
import org.ow2.proactive.connector.iaas.cloud.provider.jclouds.JCloudsProvider;
import org.ow2.proactive.connector.iaas.model.Infrastructure;
import org.ow2.proactive.connector.iaas.model.Instance;
import org.ow2.proactive.connector.iaas.model.InstanceCredentials;
import org.springframework.stereotype.Component;

import lombok.Getter;
import lombok.extern.log4j.Log4j2;


@Component
@Log4j2
public class GCEJCloudsProvider extends JCloudsProvider {

    @Getter
    private final String type = "google-compute-engine";

    @Override
    public Set<Instance> createInstance(Infrastructure infrastructure, Instance instance) {
        throw new NotImplementedException("This method is not yet implemented.");
    }

    @Override
    public String addToInstancePublicIp(Infrastructure infrastructure, String instanceId, String desiredIp) {
        throw new NotImplementedException("This method is not yet implemented.");
    }

    @Override
    public void removeInstancePublicIp(Infrastructure infrastructure, String instanceId, String desiredIp) {
        throw new NotImplementedException("This method is not yet implemented.");
    }

    @Override
    public SimpleImmutableEntry<String, String> createKeyPair(Infrastructure infrastructure, Instance instance) {
        throw new NotImplementedException("This method is not yet implemented.");
    }

    @Override
    protected RunScriptOptions getRunScriptOptionsWithCredentials(InstanceCredentials credentials) {
        throw new NotImplementedException("This method is not yet implemented.");
    }

}

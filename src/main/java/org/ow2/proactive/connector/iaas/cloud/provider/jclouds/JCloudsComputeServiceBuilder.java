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
package org.ow2.proactive.connector.iaas.cloud.provider.jclouds;

import static org.jclouds.compute.config.ComputeServiceProperties.TIMEOUT_PORT_OPEN;
import static org.jclouds.compute.config.ComputeServiceProperties.TIMEOUT_SCRIPT_COMPLETE;

import java.util.Optional;
import java.util.Properties;
import java.util.concurrent.TimeUnit;

import org.jclouds.ContextBuilder;
import org.jclouds.compute.ComputeService;
import org.jclouds.compute.ComputeServiceContext;
import org.jclouds.sshj.config.SshjSshClientModule;
import org.ow2.proactive.connector.iaas.model.Infrastructure;
import org.springframework.stereotype.Component;

import com.google.common.collect.ImmutableSet;
import com.google.inject.Module;


@Component
public class JCloudsComputeServiceBuilder {

    public ComputeService buildComputeServiceFromInfrastructure(Infrastructure infrastructure) {
        Iterable<Module> modules = ImmutableSet.of(new SshjSshClientModule());
        ContextBuilder contextBuilder = ContextBuilder.newBuilder(infrastructure.getType())

                                                      .credentials(infrastructure.getCredentials().getUsername(),
                                                                   infrastructure.getCredentials().getPassword())
                                                      .modules(modules)
                                                      .overrides(getTimeoutPolicy());

        Optional.ofNullable(infrastructure.getEndpoint())
                .filter(endPoint -> !endPoint.isEmpty())
                .ifPresent(endPoint -> contextBuilder.endpoint(endPoint));

        return contextBuilder.buildView(ComputeServiceContext.class).getComputeService();
    }

    /**
     * Sets the timeouts for the deployment.
     * 
     * @return Properties object with the timeout policy.
     */
    private Properties getTimeoutPolicy() {
        Properties properties = new Properties();
        long scriptTimeout = TimeUnit.MILLISECONDS.convert(1, TimeUnit.MINUTES);
        properties.setProperty("jclouds.ssh.max-retries", "100");
        properties.setProperty("jclouds.max-retries", "1000");
        properties.setProperty("jclouds.request-timeout", "10000");
        properties.setProperty("jclouds.connection-timeout", "18000");

        properties.setProperty(TIMEOUT_PORT_OPEN, scriptTimeout + "");
        properties.setProperty(TIMEOUT_SCRIPT_COMPLETE, scriptTimeout + "");

        return properties;
    }

}

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

import java.util.Optional;
import java.util.Properties;

import org.apache.commons.lang3.StringUtils;
import org.jclouds.Constants;
import org.jclouds.ContextBuilder;
import org.jclouds.aws.ec2.reference.AWSEC2Constants;
import org.jclouds.compute.ComputeService;
import org.jclouds.compute.ComputeServiceContext;
import org.jclouds.compute.config.ComputeServiceProperties;
import org.jclouds.sshj.config.SshjSshClientModule;
import org.ow2.proactive.connector.iaas.cloud.provider.jclouds.openstack.OpenstackUtil;
import org.ow2.proactive.connector.iaas.model.Infrastructure;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import com.google.common.collect.ImmutableSet;
import com.google.inject.Module;

import lombok.extern.log4j.Log4j2;


@Component
@Log4j2
public class JCloudsComputeServiceBuilder {

    private static final String SSH_MAX_RETRIES = "jclouds.ssh.max-retries";

    private static final String MAX_RETRIES = "jclouds.max-retries";

    @Value("${connector-iaas.jclouds.request-timeout:10000}")
    private String requestTimeout;

    @Value("${connector-iaas.jclouds.connection-timeout:18000}")
    private String connectionTimeout;

    @Value("${connector-iaas.openstack.jclouds.compute.timeout.port-open:60000}")
    private String timeoutPortOpen;

    @Value("${connector-iaas.openstack.jclouds.compute.timeout.script-complete:60000}")
    private String timeoutScriptComplete;

    @Value("${connector-iaas.jclouds.compute.timeout.node-running:1200000}")
    private String timeoutNodeRunning;

    @Value("${connector-iaas.aws.jclouds.ssh.max-retries:7}")
    private String sshMaxRetries;

    @Value("${connector-iaas.aws.jclouds.max-retries:5}")
    private String maxRetries;

    @Value("${connector-iaas.aws.jclouds.list-tag}")
    private String awsImagesListTag;

    @Autowired
    private OpenstackUtil openstackUtil;

    private Properties properties;

    public ComputeService buildComputeServiceFromInfrastructure(Infrastructure infrastructure) {
        Iterable<Module> modules = ImmutableSet.of(new SshjSshClientModule());

        String domain = infrastructure.getCredentials().getDomain();

        String identityPrefix = StringUtils.isNotBlank(domain) ? (domain + ":") : "";

        ContextBuilder contextBuilder = ContextBuilder.newBuilder(infrastructure.getType())
                                                      .credentials(identityPrefix +
                                                                   infrastructure.getCredentials().getUsername(),
                                                                   infrastructure.getCredentials().getPassword())
                                                      .modules(modules)
                                                      .overrides(getDefinedProperties(infrastructure));

        Optional.ofNullable(infrastructure.getEndpoint())
                .filter(endPoint -> !endPoint.isEmpty())
                .ifPresent(endPoint -> contextBuilder.endpoint(endPoint));

        ComputeServiceContext context = contextBuilder.buildView(ComputeServiceContext.class);
        return context.getComputeService();
    }

    public Properties getDefinedProperties(Infrastructure infrastructure) {
        if (properties == null) {
            properties = loadDefinedProperties(infrastructure);
        }
        return properties;
    }

    /**
     * Sets the timeouts for the deployment.
     * 
     * @return Properties object with the timeout policy.
     */
    private Properties loadDefinedProperties(Infrastructure infrastructure) {
        Properties properties = new Properties();

        // Add custom properties for Openstack with identity version 3
        if (infrastructure.getType().equals(OpenstackUtil.OPENSTACK_TYPE)) {
            openstackUtil.addCustomProperties(infrastructure, properties);
        }

        properties.setProperty(Constants.PROPERTY_REQUEST_TIMEOUT, requestTimeout);
        properties.setProperty(Constants.PROPERTY_CONNECTION_TIMEOUT, connectionTimeout);

        properties.put(ComputeServiceProperties.TIMEOUT_PORT_OPEN, timeoutPortOpen);
        properties.put(ComputeServiceProperties.TIMEOUT_SCRIPT_COMPLETE, timeoutScriptComplete);
        properties.put(ComputeServiceProperties.TIMEOUT_NODE_RUNNING, timeoutNodeRunning);

        properties.setProperty(SSH_MAX_RETRIES, sshMaxRetries);
        properties.setProperty(MAX_RETRIES, maxRetries);

        // set AMI queries to filter private AMI (self), API from Amazon (137112412989) & Canonical (099720109477). Doc: https://docs.aws.amazon.com/AWSEC2/latest/APIReference/API_DescribeImages.html
        //properties.setProperty(AWSEC2Constants.PROPERTY_EC2_AMI_QUERY,"owner-id=137112412989,099720109477,self;state=available;image-type=machine;hypervisor=xen;virtualization-type=hvm" );
        properties.setProperty(AWSEC2Constants.PROPERTY_EC2_AMI_QUERY,
                               "state=available;image-type=machine;hypervisor=xen;virtualization-type=hvm;tag:proactive-list-label=" +
                                                                       awsImagesListTag);
        properties.setProperty(AWSEC2Constants.PROPERTY_EC2_CC_AMI_QUERY, "");

        log.info("Infrastructure properties: " + properties.toString());

        return properties;
    }

}

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

import static org.hamcrest.CoreMatchers.instanceOf;
import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.CoreMatchers.not;
import static org.junit.Assert.assertThat;

import org.jclouds.aws.ec2.compute.AWSEC2ComputeService;
import org.jclouds.compute.ComputeService;
import org.junit.Before;
import org.junit.Test;
import org.ow2.proactive.connector.iaas.fixtures.InfrastructureFixture;
import org.springframework.test.util.ReflectionTestUtils;


public class JCloudsComputeServiceBuilderTest {

    private JCloudsComputeServiceBuilder computeServiceBuilder;

    @Before
    public void init() throws Exception {
        computeServiceBuilder = new JCloudsComputeServiceBuilder();
        ReflectionTestUtils.setField(computeServiceBuilder, "defaultProject", "admin", String.class);
        ReflectionTestUtils.setField(computeServiceBuilder, "defaultDomain", "Default", String.class);
        ReflectionTestUtils.setField(computeServiceBuilder, "defaultRegion", "RegionOne", String.class);
        ReflectionTestUtils.setField(computeServiceBuilder, "keystoneVersion", "3", String.class);
        ReflectionTestUtils.setField(computeServiceBuilder, "timeoutPortOpen", "60000", String.class);
        ReflectionTestUtils.setField(computeServiceBuilder, "timeoutScriptComplete", "60000", String.class);
        ReflectionTestUtils.setField(computeServiceBuilder, "requestTimeout", "10000", String.class);
        ReflectionTestUtils.setField(computeServiceBuilder, "connectionTimeout", "18000", String.class);
        ReflectionTestUtils.setField(computeServiceBuilder, "sshMaxRetries", "100", String.class);
        ReflectionTestUtils.setField(computeServiceBuilder, "maxRetries", "1000", String.class);
    }

    @Test
    public void testBuildComputeServiceFromInfrastructureAWS() {
        ComputeService computerService = computeServiceBuilder.buildComputeServiceFromInfrastructure(InfrastructureFixture.getInfrastructure("id-aws-ec2",
                                                                                                                                             "aws-ec2",
                                                                                                                                             "",
                                                                                                                                             "userName",
                                                                                                                                             "password"));

        assertThat(computerService, is(instanceOf(AWSEC2ComputeService.class)));
    }

    @Test
    public void testBuildComputeServiceFromInfrastructureAWSNullEndPoint() {
        ComputeService computerService = computeServiceBuilder.buildComputeServiceFromInfrastructure(InfrastructureFixture.getInfrastructure("id-aws-ec2",
                                                                                                                                             "aws-ec2",
                                                                                                                                             null,
                                                                                                                                             "userName",
                                                                                                                                             "password"));

        assertThat(computerService, is(instanceOf(AWSEC2ComputeService.class)));
    }

    @Test
    public void testBuildComputeServiceFromInfrastructureOpenstack() {
        ComputeService computerService = computeServiceBuilder.buildComputeServiceFromInfrastructure(InfrastructureFixture.getInfrastructure("id-openstack-nova",
                                                                                                                                             "openstack-nova",
                                                                                                                                             "endPoint",
                                                                                                                                             "userName",
                                                                                                                                             "password"));

        assertThat(computerService, is(not(instanceOf(AWSEC2ComputeService.class))));
    }

}

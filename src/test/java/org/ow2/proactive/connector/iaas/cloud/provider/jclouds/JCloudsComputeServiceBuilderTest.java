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


public class JCloudsComputeServiceBuilderTest {

    private JCloudsComputeServiceBuilder computeServiceBuilder;

    @Before
    public void init() {
        this.computeServiceBuilder = new JCloudsComputeServiceBuilder();
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

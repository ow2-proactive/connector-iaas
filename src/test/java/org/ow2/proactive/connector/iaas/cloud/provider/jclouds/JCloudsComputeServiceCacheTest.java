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

import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.not;
import static org.hamcrest.Matchers.nullValue;
import static org.junit.Assert.assertThat;
import static org.mockito.Matchers.any;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import org.jclouds.compute.ComputeService;
import org.junit.Before;
import org.junit.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.ow2.proactive.connector.iaas.fixtures.InfrastructureFixture;
import org.ow2.proactive.connector.iaas.model.Infrastructure;


public class JCloudsComputeServiceCacheTest {

    @InjectMocks
    private JCloudsComputeServiceCache computeServiceCache;

    @Mock
    private JCloudsComputeServiceBuilder computeServiceBuilder;

    @Mock
    private ComputeService computeService;

    @Before
    public void init() {
        MockitoAnnotations.initMocks(this);
        when(computeServiceBuilder.buildComputeServiceFromInfrastructure(any(Infrastructure.class))).thenReturn(computeService);
    }

    @Test
    public void testGetComputeServiceFirstTime() {
        ComputeService computeService = computeServiceCache.getComputeService(InfrastructureFixture.getInfrastructure("id-aws-ec2",
                                                                                                                      "aws-ec2",
                                                                                                                      "endPoint",
                                                                                                                      "userName",
                                                                                                                      "password",
                                                                                                                      null,
                                                                                                                      null,
                                                                                                                      null));
        assertThat(computeService, is(not(nullValue())));
        verify(computeServiceBuilder,
               times(1)).buildComputeServiceFromInfrastructure(InfrastructureFixture.getInfrastructure("id-aws-ec2",
                                                                                                       "aws-ec2",
                                                                                                       "endPoint",
                                                                                                       "userName",
                                                                                                       "password",
                                                                                                       null,
                                                                                                       null,
                                                                                                       null));
    }

    @Test
    public void testGetComputeServiceManyTimeSameInfrastructure() {
        computeServiceCache.getComputeService(InfrastructureFixture.getInfrastructure("id-aws-ec2",
                                                                                      "aws-ec2",
                                                                                      "endPoint",
                                                                                      "userName",
                                                                                      "password",
                                                                                      null,
                                                                                      null,
                                                                                      null));
        computeServiceCache.getComputeService(InfrastructureFixture.getInfrastructure("id-aws-ec2",
                                                                                      "aws-ec2",
                                                                                      "endPoint",
                                                                                      "userName",
                                                                                      "password",
                                                                                      null,
                                                                                      null,
                                                                                      null));
        computeServiceCache.getComputeService(InfrastructureFixture.getInfrastructure("id-aws-ec2",
                                                                                      "aws-ec2",
                                                                                      "endPoint",
                                                                                      "userName",
                                                                                      "password",
                                                                                      null,
                                                                                      null,
                                                                                      null));
        computeServiceCache.getComputeService(InfrastructureFixture.getInfrastructure("id-aws-ec2",
                                                                                      "aws-ec2",
                                                                                      "endPoint",
                                                                                      "userName",
                                                                                      "password",
                                                                                      null,
                                                                                      null,
                                                                                      null));
        computeServiceCache.getComputeService(InfrastructureFixture.getInfrastructure("id-aws-ec2",
                                                                                      "aws-ec2",
                                                                                      "endPoint",
                                                                                      "userName",
                                                                                      "password",
                                                                                      null,
                                                                                      null,
                                                                                      null));

        verify(computeServiceBuilder,
               times(1)).buildComputeServiceFromInfrastructure(InfrastructureFixture.getInfrastructure("id-aws-ec2",
                                                                                                       "aws-ec2",
                                                                                                       "endPoint",
                                                                                                       "userName",
                                                                                                       "password",
                                                                                                       null,
                                                                                                       null,
                                                                                                       null));
    }

    @Test
    public void testGetComputeServiceDifferentInfrastructure() {
        computeServiceCache.getComputeService(InfrastructureFixture.getInfrastructure("id-aws-ec2",
                                                                                      "aws-ec2",
                                                                                      "endPoint",
                                                                                      "userName",
                                                                                      "password",
                                                                                      null,
                                                                                      null,
                                                                                      null));
        computeServiceCache.getComputeService(InfrastructureFixture.getInfrastructure("id-openstack",
                                                                                      "openstack",
                                                                                      "endPoint",
                                                                                      "userName",
                                                                                      "password",
                                                                                      null,
                                                                                      null,
                                                                                      null));

        verify(computeServiceBuilder, times(2)).buildComputeServiceFromInfrastructure(any(Infrastructure.class));
    }

    @Test
    public void testRemoveComputeService() {
        computeServiceCache.getComputeService(InfrastructureFixture.getInfrastructure("id-aws-ec2",
                                                                                      "aws-ec2",
                                                                                      "endPoint",
                                                                                      "userName",
                                                                                      "password",
                                                                                      null,
                                                                                      null,
                                                                                      null));

        computeServiceCache.removeComputeService(InfrastructureFixture.getInfrastructure("id-aws-ec2",
                                                                                         "aws-ec2",
                                                                                         "endPoint",
                                                                                         "userName",
                                                                                         "password",
                                                                                         null,
                                                                                         null,
                                                                                         null));

        computeServiceCache.getComputeService(InfrastructureFixture.getInfrastructure("id-aws-ec2",
                                                                                      "aws-ec2",
                                                                                      "endPoint",
                                                                                      "userName",
                                                                                      "password",
                                                                                      null,
                                                                                      null,
                                                                                      null));

        verify(computeServiceBuilder,
               times(2)).buildComputeServiceFromInfrastructure(InfrastructureFixture.getInfrastructure("id-aws-ec2",
                                                                                                       "aws-ec2",
                                                                                                       "endPoint",
                                                                                                       "userName",
                                                                                                       "password",
                                                                                                       null,
                                                                                                       null,
                                                                                                       null));
    }

}

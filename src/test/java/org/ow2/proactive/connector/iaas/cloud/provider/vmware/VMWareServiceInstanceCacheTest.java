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
package org.ow2.proactive.connector.iaas.cloud.provider.vmware;

import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.not;
import static org.hamcrest.Matchers.nullValue;
import static org.junit.Assert.assertThat;
import static org.mockito.Matchers.any;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import org.junit.Before;
import org.junit.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.ow2.proactive.connector.iaas.fixtures.InfrastructureFixture;
import org.ow2.proactive.connector.iaas.model.Infrastructure;

import com.vmware.vim25.mo.ServiceInstance;


public class VMWareServiceInstanceCacheTest {

    @InjectMocks
    private VMWareServiceInstanceCache serviceInstanceCache;

    @Mock
    private VMWareServiceInstanceBuilder serviceInstanceBuilder;

    @Mock
    private ServiceInstance serviceInstance;

    @Before
    public void init() {
        MockitoAnnotations.initMocks(this);
        when(serviceInstanceBuilder.buildServiceInstanceFromInfrastructure(any(Infrastructure.class))).thenReturn(serviceInstance);
    }

    @Test
    public void testGetServiceInstanceFirstTime() {
        ServiceInstance serviceInstance = serviceInstanceCache.getServiceInstance(InfrastructureFixture.getInfrastructure("id-aws-ec2",
                                                                                                                          "aws-ec2",
                                                                                                                          "endPoint",
                                                                                                                          "userName",
                                                                                                                          "password"));
        assertThat(serviceInstance, is(not(nullValue())));
        verify(serviceInstanceBuilder,
               times(1)).buildServiceInstanceFromInfrastructure(InfrastructureFixture.getInfrastructure("id-aws-ec2",
                                                                                                        "aws-ec2",
                                                                                                        "endPoint",
                                                                                                        "userName",
                                                                                                        "password"));
    }

    @Test
    public void testGetServiceInstanceManyTimeSameInfrastructure() {
        serviceInstanceCache.getServiceInstance(InfrastructureFixture.getInfrastructure("id-aws-ec2",
                                                                                        "aws-ec2",
                                                                                        "endPoint",
                                                                                        "userName",
                                                                                        "password"));
        serviceInstanceCache.getServiceInstance(InfrastructureFixture.getInfrastructure("id-aws-ec2",
                                                                                        "aws-ec2",
                                                                                        "endPoint",
                                                                                        "userName",
                                                                                        "password"));
        serviceInstanceCache.getServiceInstance(InfrastructureFixture.getInfrastructure("id-aws-ec2",
                                                                                        "aws-ec2",
                                                                                        "endPoint",
                                                                                        "userName",
                                                                                        "password"));
        serviceInstanceCache.getServiceInstance(InfrastructureFixture.getInfrastructure("id-aws-ec2",
                                                                                        "aws-ec2",
                                                                                        "endPoint",
                                                                                        "userName",
                                                                                        "password"));
        serviceInstanceCache.getServiceInstance(InfrastructureFixture.getInfrastructure("id-aws-ec2",
                                                                                        "aws-ec2",
                                                                                        "endPoint",
                                                                                        "userName",
                                                                                        "password"));

        verify(serviceInstanceBuilder,
               times(1)).buildServiceInstanceFromInfrastructure(InfrastructureFixture.getInfrastructure("id-aws-ec2",
                                                                                                        "aws-ec2",
                                                                                                        "endPoint",
                                                                                                        "userName",
                                                                                                        "password"));
    }

    @Test
    public void testGetServiceInstanceDifferentInfrastructure() {
        serviceInstanceCache.getServiceInstance(InfrastructureFixture.getInfrastructure("id-aws-ec2",
                                                                                        "aws-ec2",
                                                                                        "endPoint",
                                                                                        "userName",
                                                                                        "password"));
        serviceInstanceCache.getServiceInstance(InfrastructureFixture.getInfrastructure("id-openstack",
                                                                                        "openstack",
                                                                                        "endPoint",
                                                                                        "userName",
                                                                                        "password"));

        verify(serviceInstanceBuilder, times(2)).buildServiceInstanceFromInfrastructure(any(Infrastructure.class));
    }

    @Test
    public void testRemoveServiceInstance() {
        serviceInstanceCache.getServiceInstance(InfrastructureFixture.getInfrastructure("id-aws-ec2",
                                                                                        "aws-ec2",
                                                                                        "endPoint",
                                                                                        "userName",
                                                                                        "password"));

        serviceInstanceCache.removeServiceInstance(InfrastructureFixture.getInfrastructure("id-aws-ec2",
                                                                                           "aws-ec2",
                                                                                           "endPoint",
                                                                                           "userName",
                                                                                           "password"));

        serviceInstanceCache.getServiceInstance(InfrastructureFixture.getInfrastructure("id-aws-ec2",
                                                                                        "aws-ec2",
                                                                                        "endPoint",
                                                                                        "userName",
                                                                                        "password"));

        verify(serviceInstanceBuilder,
               times(2)).buildServiceInstanceFromInfrastructure(InfrastructureFixture.getInfrastructure("id-aws-ec2",
                                                                                                        "aws-ec2",
                                                                                                        "endPoint",
                                                                                                        "userName",
                                                                                                        "password"));
    }

}

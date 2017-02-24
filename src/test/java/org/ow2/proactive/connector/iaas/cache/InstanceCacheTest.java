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
package org.ow2.proactive.connector.iaas.cache;

import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.Matchers.not;
import static org.hamcrest.Matchers.nullValue;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertThat;

import java.util.Map;
import java.util.Set;

import org.junit.Before;
import org.junit.Test;
import org.ow2.proactive.connector.iaas.fixtures.InfrastructureFixture;
import org.ow2.proactive.connector.iaas.fixtures.InstanceFixture;
import org.ow2.proactive.connector.iaas.model.Infrastructure;
import org.ow2.proactive.connector.iaas.model.Instance;

import com.google.common.collect.Sets;


public class InstanceCacheTest {
    private InstanceCache instanceCache;

    @Before
    public void init() {
        instanceCache = new InstanceCache();
    }

    @Test
    public void testConstructor() {
        assertThat(instanceCache.getCreatedInstances(), is(not(nullValue())));
        assertThat(instanceCache.getCreatedInstances().isEmpty(), is(true));
    }

    @Test(expected = UnsupportedOperationException.class)
    public void testImmutability() {
        ((Map<String, Set<Instance>>) instanceCache.getCreatedInstances()).put("infra-id",
                                                                               Sets.newHashSet(InstanceFixture.getInstance("instance-id",
                                                                                                                           "instance-name",
                                                                                                                           "image",
                                                                                                                           "2",
                                                                                                                           "512",
                                                                                                                           "cpu",
                                                                                                                           "publicIP",
                                                                                                                           "privateIP",
                                                                                                                           "running")));
    }

    @Test
    public void testRegisterInfrastructureInstance() {
        instanceCache.registerInfrastructureInstances(InfrastructureFixture.getInfrastructure("id-openstack",
                                                                                              "openstack",
                                                                                              "endPoint",
                                                                                              "userName",
                                                                                              "password"),
                                                      Sets.newHashSet(InstanceFixture.simpleInstance("instance-id")));

        assertThat(instanceCache.getCreatedInstances().size(), is(1));
        assertThat(instanceCache.getCreatedInstances().get("id-openstack"),
                   is(Sets.newHashSet(InstanceFixture.simpleInstance("instance-id"))));
    }

    @Test
    public void testDeleteInfrastructureInstance() {
        Infrastructure infrastructure = InfrastructureFixture.getInfrastructure("id-openstack",
                                                                                "openstack",
                                                                                "endPoint",
                                                                                "userName",
                                                                                "password");

        instanceCache.registerInfrastructureInstances(infrastructure,
                                                      Sets.newHashSet(InstanceFixture.simpleInstance("instance-id")));

        instanceCache.deleteInfrastructureInstance(infrastructure, InstanceFixture.simpleInstance("instance-id"));

        assertThat(instanceCache.getCreatedInstances().get(infrastructure.getId()), is(not(nullValue())));
        assertThat(instanceCache.getCreatedInstances().get(infrastructure.getId()).isEmpty(), is(true));
    }

    @Test
    public void testDeleteAllInfrastructureInstances() {
        Infrastructure infrastructure = InfrastructureFixture.getInfrastructure("id-openstack",
                                                                                "openstack",
                                                                                "endPoint",
                                                                                "userName",
                                                                                "password");

        instanceCache.registerInfrastructureInstances(infrastructure,
                                                      Sets.newHashSet(InstanceFixture.simpleInstance("instance-id1"),
                                                                      InstanceFixture.simpleInstance("instance-id2")));

        instanceCache.deleteAllInfrastructureInstances(infrastructure);

        assertThat(instanceCache.getCreatedInstances().get(infrastructure.getId()), is(not(nullValue())));
        assertThat(instanceCache.getCreatedInstances().get(infrastructure.getId()).isEmpty(), is(true));
    }

    @Test
    public void testDeleteInfrastructure() {
        Infrastructure infrastructure = InfrastructureFixture.getInfrastructure("id-openstack",
                                                                                "openstack",
                                                                                "endPoint",
                                                                                "userName",
                                                                                "password");

        instanceCache.registerInfrastructureInstances(infrastructure,
                                                      Sets.newHashSet(InstanceFixture.simpleInstance("instance-id")));

        instanceCache.deleteInfrastructure(infrastructure);

        assertThat(instanceCache.getCreatedInstances(), is(not(nullValue())));
        assertNull(instanceCache.getCreatedInstances().get(infrastructure.getId()));
    }

}

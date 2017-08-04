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
package org.ow2.proactive.connector.iaas.model;

import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.Matchers.nullValue;
import static org.junit.Assert.assertThat;

import java.util.Set;

import org.junit.Test;
import org.ow2.proactive.connector.iaas.fixtures.CredentialsFixtures;

import jersey.repackaged.com.google.common.collect.Sets;


public class InfrastructureTest {
    @Test
    public void testEmptyConstructor() {
        Infrastructure infrastructure = new Infrastructure();
        assertThat(infrastructure.getId(), is(nullValue()));
    }

    @Test
    public void testConstructor() {
        Infrastructure infrastructure = new Infrastructure("id-openstack",
                                                           "openstack",
                                                           "openstackEndpoint",
                                                           CredentialsFixtures.getInfrastructureCredentials("openstackUserName",
                                                                                                            "openstackCredential"),
                                                           null,
                                                           null,
                                                           null,
                                                           null,
                                                           false,
                                                           false);
        assertThat(infrastructure.getId(), is("id-openstack"));
    }

    @Test
    public void testEqualsAndHashcode() {
        Infrastructure infrastructure1 = new Infrastructure("id-openstack",
                                                            "openstack",
                                                            "openstackEndpoint",
                                                            CredentialsFixtures.getInfrastructureCredentials("openstackUserName",
                                                                                                             "openstackCredential1"),
                                                            null,
                                                            null,
                                                            null,
                                                            null,
                                                            false,
                                                            false);
        Infrastructure infrastructure2 = new Infrastructure("id-openstack",
                                                            "openstack",
                                                            "openstackEndpoint",
                                                            CredentialsFixtures.getInfrastructureCredentials("openstackUserName",
                                                                                                             "openstackCredential2"),
                                                            null,
                                                            null,
                                                            null,
                                                            null,
                                                            false,
                                                            false);

        Set<Infrastructure> infrastructures = Sets.newHashSet(infrastructure1, infrastructure2);

        assertThat(infrastructures.size(), is(1));
        assertThat(infrastructure1.equals(infrastructure2), is(true));
    }

}

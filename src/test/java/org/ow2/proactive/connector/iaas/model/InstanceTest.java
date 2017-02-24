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

import java.util.ArrayList;
import java.util.List;
import java.util.Set;

import org.junit.Test;
import org.ow2.proactive.connector.iaas.fixtures.CredentialsFixtures;
import org.ow2.proactive.connector.iaas.fixtures.HardwareFixtures;
import org.ow2.proactive.connector.iaas.fixtures.InstanceScriptFixture;
import org.ow2.proactive.connector.iaas.fixtures.NetworkFixtures;

import jersey.repackaged.com.google.common.collect.Sets;


public class InstanceTest {
    @Test
    public void testEmptyConstructor() {
        Instance instance = new Instance();
        assertThat(instance.getTag(), is(nullValue()));
    }

    @Test
    public void testConstructor() {
        List<String> securityGroupNames = new ArrayList<String>();
        securityGroupNames.add("default1");
        securityGroupNames.add("default2");
        Instance instance = new Instance("instance-id",
                                         "new-vm",
                                         "ubuntu",
                                         "1",
                                         "running",
                                         HardwareFixtures.getHardware("1", "512"),
                                         NetworkFixtures.getNetwork("77.154.227.148", "1.0.0.2"),
                                         CredentialsFixtures.getCredentials("publicKeyName"),
                                         Options.builder()
                                                .spotPrice("0.05f")
                                                .securityGroupNames(securityGroupNames)
                                                .subnetId("127.0.0.1")
                                                .build(),
                                         InstanceScriptFixture.simpleInstanceScriptNoscripts());

        assertThat(instance.getTag(), is("new-vm"));
    }

    @Test
    public void testEqualsAndHashcode() {
        List<String> securityGroupNames = new ArrayList<String>();
        securityGroupNames.add("default1");
        securityGroupNames.add("default2");
        Instance instance1 = new Instance("instance-id",
                                          "new-vm",
                                          "ubuntu",
                                          "1",
                                          "running",
                                          HardwareFixtures.getHardware("1", "512"),
                                          NetworkFixtures.getNetwork("77.154.227.148", "1.0.0.2"),
                                          CredentialsFixtures.getCredentials("publicKeyName"),
                                          Options.builder()
                                                 .spotPrice("0.05f")
                                                 .securityGroupNames(securityGroupNames)
                                                 .subnetId("127.0.0.1")
                                                 .build(),
                                          InstanceScriptFixture.simpleInstanceScriptNoscripts());
        Instance instance2 = new Instance("instance-id",
                                          "new-vm",
                                          "ubuntu",
                                          "1",
                                          "running",
                                          HardwareFixtures.getHardware("1", "512"),
                                          NetworkFixtures.getNetwork("77.154.227.148", "1.0.0.2"),
                                          CredentialsFixtures.getCredentials("publicKeyName"),
                                          Options.builder()
                                                 .spotPrice("0.05f")
                                                 .securityGroupNames(securityGroupNames)
                                                 .subnetId("127.0.0.1")
                                                 .build(),
                                          InstanceScriptFixture.simpleInstanceScriptNoscripts());

        Set<Instance> instances = Sets.newHashSet(instance1, instance2);

        assertThat(instances.size(), is(1));
        assertThat(instance1.equals(instance2), is(true));
    }

}

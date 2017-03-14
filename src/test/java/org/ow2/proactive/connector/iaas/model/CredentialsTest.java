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

import jersey.repackaged.com.google.common.collect.Sets;


public class CredentialsTest {
    @Test
    public void testEmptyConstructor() {
        InstanceCredentials credentials = new InstanceCredentials();
        assertThat(credentials.getUsername(), is(nullValue()));
        assertThat(credentials.getPassword(), is(nullValue()));
    }

    @Test
    public void testConstructor() {
        InstanceCredentials credentials = new InstanceCredentials("username", "password", "publicKeyName", "publicKey");
        assertThat(credentials.getUsername(), is("username"));
        assertThat(credentials.getPassword(), is("password"));
        assertThat(credentials.getPublicKeyName(), is("publicKeyName"));
        assertThat(credentials.getPublicKey(), is("publicKey"));
    }

    @Test
    public void testEqualsAndHashcode() {
        InstanceCredentials credentials1 = new InstanceCredentials("username",
                                                                   "password",
                                                                   "publicKeyName",
                                                                   "publicKey");
        InstanceCredentials credentials2 = new InstanceCredentials("username",
                                                                   "password",
                                                                   "publicKeyName",
                                                                   "publicKey");

        Set<InstanceCredentials> credentialss = Sets.newHashSet(credentials1, credentials2);

        assertThat(credentialss.size(), is(1));
        assertThat(credentials1.equals(credentials2), is(true));
    }

}

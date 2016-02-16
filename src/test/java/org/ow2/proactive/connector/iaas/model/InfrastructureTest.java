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
        Infrastructure infrastructure = new Infrastructure("id-openstack", "openstack", "openstackEndpoint",
            CredentialsFixtures.getCredentials("openstackUserName", "openstackCredential"), false);
        assertThat(infrastructure.getId(), is("id-openstack"));
    }

    @Test
    public void testEqualsAndHashcode() {
        Infrastructure infrastructure1 = new Infrastructure("id-openstack", "openstack", "openstackEndpoint",
            CredentialsFixtures.getCredentials("openstackUserName", "openstackCredential1"), false);
        Infrastructure infrastructure2 = new Infrastructure("id-openstack", "openstack", "openstackEndpoint",
            CredentialsFixtures.getCredentials("openstackUserName", "openstackCredential2"), false);

        Set<Infrastructure> infrastructures = Sets.newHashSet(infrastructure1, infrastructure2);

        assertThat(infrastructures.size(), is(1));
        assertThat(infrastructure1.equals(infrastructure2), is(true));
    }

}

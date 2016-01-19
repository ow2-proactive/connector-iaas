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
		Credentials credentials = new Credentials();
		assertThat(credentials.getUsername(), is(nullValue()));
		assertThat(credentials.getPassword(), is(nullValue()));
	}

	@Test
	public void testConstructor() {
		Credentials credentials = new Credentials("username", "password", "privateKey");
		assertThat(credentials.getUsername(), is("username"));
		assertThat(credentials.getPassword(), is("password"));
		assertThat(credentials.getPrivateKey(), is("privateKey"));
	}

	@Test
	public void testEqualsAndHashcode() {
		Credentials credentials1 = new Credentials("username", "password", "privateKey");
		Credentials credentials2 = new Credentials("username", "password", "privateKey");

		Set<Credentials> credentialss = Sets.newHashSet(credentials1, credentials2);

		assertThat(credentialss.size(), is(1));
		assertThat(credentials1.equals(credentials2), is(true));
	}

}

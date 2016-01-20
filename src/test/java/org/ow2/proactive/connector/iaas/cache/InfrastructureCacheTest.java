package org.ow2.proactive.connector.iaas.cache;

import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.Matchers.not;
import static org.hamcrest.Matchers.nullValue;
import static org.junit.Assert.assertThat;

import java.util.Map;

import org.junit.Before;
import org.junit.Test;
import org.ow2.proactive.connector.iaas.fixtures.InfrastructureFixture;
import org.ow2.proactive.connector.iaas.model.Infrastructure;

public class InfrastructureCacheTest {
	private InfrastructureCache infrastructureCache;

	@Before
	public void init() {
		infrastructureCache = new InfrastructureCache();
	}

	@Test
	public void testConstructor() {
		assertThat(infrastructureCache.getSupportedInfrastructures(), is(not(nullValue())));
		assertThat(infrastructureCache.getSupportedInfrastructures().isEmpty(), is(true));
	}

	@Test(expected = UnsupportedOperationException.class)
	public void testImmutability() {
		((Map<String, Infrastructure>) infrastructureCache.getSupportedInfrastructures()).put("openstack",
				InfrastructureFixture.getInfrastructure("id-openstack", "openstack", "endPoint", "userName",
						"password"));
	}

	@Test
	public void testRegisterInfrastructure() {
		infrastructureCache.registerInfrastructure(InfrastructureFixture.getInfrastructure("id-openstack", "openstack",
				"endPoint", "userName", "password"));
		assertThat(infrastructureCache.getSupportedInfrastructures().size(), is(1));
		assertThat(infrastructureCache.getSupportedInfrastructures().get("id-openstack"), is(InfrastructureFixture
				.getInfrastructure("id-openstack", "openstack", "endPoint", "userName", "password")));
	}

	@Test
	public void testDeleteInfrastructure() {
		infrastructureCache.registerInfrastructure(InfrastructureFixture.getInfrastructure("id-openstack", "openstack",
				"endPoint", "userName", "password"));

		infrastructureCache.deleteInfrastructure(InfrastructureFixture.getInfrastructure("id-openstack", "openstack",
				"endPoint", "userName", "password"));

		assertThat(infrastructureCache.getSupportedInfrastructures(), is(not(nullValue())));
		assertThat(infrastructureCache.getSupportedInfrastructures().isEmpty(), is(true));
	}

}

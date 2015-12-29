package org.ow2.proactive.iaas.connector.model;

import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.Matchers.nullValue;
import static org.junit.Assert.assertThat;

import java.util.Set;

import org.junit.Test;
import org.ow2.proactive.iaas.connector.model.Instance;

import jersey.repackaged.com.google.common.collect.Sets;

public class InstanceTest {
	@Test
	public void testEmptyConstructor() {
		Instance instance = new Instance();
		assertThat(instance.getName(), is(nullValue()));
	}

	@Test
	public void testConstructor() {
		Instance instance = new Instance("openstack", "vm", "ubuntu", "1", "1", "512");
		assertThat(instance.getName(), is("openstack"));
	}

	@Test
	public void testEqualsAndHashcode() {
		Instance instance1 = new Instance("openstack", "vm", "ubuntu", "1", "1", "512");
		Instance instance2 = new Instance("openstack", "vm", "ubuntu", "1", "1", "512");

		Set<Instance> instances = Sets.newHashSet(instance1, instance2);

		assertThat(instances.size(), is(1));
		assertThat(instance1.equals(instance2), is(true));
	}

}

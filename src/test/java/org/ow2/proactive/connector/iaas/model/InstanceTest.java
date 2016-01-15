package org.ow2.proactive.connector.iaas.model;

import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.Matchers.nullValue;
import static org.junit.Assert.assertThat;

import java.util.Set;

import org.junit.Test;

import jersey.repackaged.com.google.common.collect.Sets;

public class InstanceTest {
	@Test
	public void testEmptyConstructor() {
		Instance instance = new Instance();
		assertThat(instance.getTag(), is(nullValue()));
	}

	@Test
	public void testConstructor() {
		Instance instance = new Instance("instance-id", "new-vm", "ubuntu", "1", "1", "512", "running");
		assertThat(instance.getTag(), is("new-vm"));
	}

	@Test
	public void testEqualsAndHashcode() {
		Instance instance1 = new Instance("instance-id", "new-vm", "ubuntu", "1", "1", "512", "running");
		Instance instance2 = new Instance("instance-id", "new-vm", "ubuntu", "1", "1", "512", "running");

		Set<Instance> instances = Sets.newHashSet(instance1, instance2);

		assertThat(instances.size(), is(1));
		assertThat(instance1.equals(instance2), is(true));
	}

}
package org.ow2.proactive.connector.iaas.model;

import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.Matchers.nullValue;
import static org.junit.Assert.assertThat;

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
        Instance instance = new Instance("instance-id", "new-vm", "ubuntu", "1", "running",
            HardwareFixtures.getHardware("1", "512"), NetworkFixtures.getNetwork("77.154.227.148", "1.0.0.2"),
            CredentialsFixtures.getCredentials("publicKeyName"), Options.builder().spotPrice("0.05f").build(),
            InstanceScriptFixture.simpleInstanceScriptNoscripts());

        assertThat(instance.getTag(), is("new-vm"));
    }

    @Test
    public void testEqualsAndHashcode() {
        Instance instance1 = new Instance("instance-id", "new-vm", "ubuntu", "1", "running",
            HardwareFixtures.getHardware("1", "512"), NetworkFixtures.getNetwork("77.154.227.148", "1.0.0.2"),
            CredentialsFixtures.getCredentials("publicKeyName"), Options.builder().spotPrice("0.05f").build(),
            InstanceScriptFixture.simpleInstanceScriptNoscripts());
        Instance instance2 = new Instance("instance-id", "new-vm", "ubuntu", "1", "running",
            HardwareFixtures.getHardware("1", "512"), NetworkFixtures.getNetwork("77.154.227.148", "1.0.0.2"),
            CredentialsFixtures.getCredentials("publicKeyName"), Options.builder().spotPrice("0.05f").build(),
            InstanceScriptFixture.simpleInstanceScriptNoscripts());

        Set<Instance> instances = Sets.newHashSet(instance1, instance2);

        assertThat(instances.size(), is(1));
        assertThat(instance1.equals(instance2), is(true));
    }

}
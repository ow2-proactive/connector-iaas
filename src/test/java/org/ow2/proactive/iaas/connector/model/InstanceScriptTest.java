package org.ow2.proactive.iaas.connector.model;

import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.Matchers.nullValue;
import static org.junit.Assert.assertThat;

import java.util.Set;

import org.junit.Test;

import jersey.repackaged.com.google.common.collect.Sets;


public class InstanceScriptTest {
    @Test
    public void testEmptyConstructor() {
        InstanceScript instanceScript = new InstanceScript();
        assertThat(instanceScript.getInstanceId(), is(nullValue()));
        assertThat(instanceScript.getScripts(), is(nullValue()));
    }

    @Test
    public void testConstructor() {
        String[] scripts = new String[] { "script 1", "script 2" };
        InstanceScript instanceScript = new InstanceScript("instance-id", scripts);
        assertThat(instanceScript.getInstanceId(), is("instance-id"));
        assertThat(instanceScript.getScripts(), is(scripts));
    }

    @Test
    public void testEqualsAndHashcode() {
        String[] scripts = new String[] { "script 1", "script 2" };

        InstanceScript instanceScript1 = new InstanceScript("instance-id", scripts);
        InstanceScript instanceScript2 = new InstanceScript("instance-id", scripts);

        Set<InstanceScript> instanceScripts = Sets.newHashSet(instanceScript1, instanceScript2);

        assertThat(instanceScripts.size(), is(2));
        assertThat(instanceScript1.equals(instanceScript2), is(false));
    }

}

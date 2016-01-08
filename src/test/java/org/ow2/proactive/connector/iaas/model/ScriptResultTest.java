package org.ow2.proactive.connector.iaas.model;

import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.Matchers.nullValue;
import static org.junit.Assert.assertThat;

import java.util.Set;

import org.junit.Test;

import jersey.repackaged.com.google.common.collect.Sets;


public class ScriptResultTest {

    @Test
    public void testEmptyConstructor() {
        ScriptResult scriptResult = new ScriptResult();
        assertThat(scriptResult.getOutput(), is(nullValue()));
        assertThat(scriptResult.getError(), is(nullValue()));

    }

    @Test
    public void testConstructor() {
        ScriptResult scriptResult = new ScriptResult("instanceId", "some output", "some error");
        assertThat(scriptResult.getOutput(), is("some output"));
        assertThat(scriptResult.getError(), is("some error"));

    }

    @Test
    public void testEqualsAndHashcode() {
        ScriptResult scriptResult1 = new ScriptResult("instanceId", "some output", "some error");
        ScriptResult scriptResult2 = new ScriptResult("instanceId", "some output", "some error");

        Set<ScriptResult> scriptResults = Sets.newHashSet(scriptResult1, scriptResult2);

        assertThat(scriptResults.size(), is(2));
        assertThat(scriptResult1.equals(scriptResult2), is(false));
    }

}

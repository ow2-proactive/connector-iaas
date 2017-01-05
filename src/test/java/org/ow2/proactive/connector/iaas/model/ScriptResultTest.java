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

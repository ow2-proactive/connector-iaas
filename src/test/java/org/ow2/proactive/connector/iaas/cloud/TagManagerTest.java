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
package org.ow2.proactive.connector.iaas.cloud;

import static org.hamcrest.Matchers.is;
import static org.junit.Assert.assertThat;
import static org.junit.Assert.assertTrue;

import java.util.ArrayList;
import java.util.List;

import org.junit.Before;
import org.junit.Test;
import org.ow2.proactive.connector.iaas.model.Options;
import org.ow2.proactive.connector.iaas.model.Tag;


public class TagManagerTest {

    private TagManager tagManager;

    @Before
    public void init() {
        tagManager = new TagManager("connector-iaas-tag", "default-value");
    }

    @Test
    public void testRetrieveTagsWithoutOptions() {
        List<Tag> tags = tagManager.retrieveAllTags(null);
        assertTrue(tags.size() == 1);
        assertThat(tags.get(0).getKey(), is("connector-iaas-tag"));
        assertThat(tags.get(0).getValue(), is("default-value"));
    }

    @Test
    public void testRetrieveTagsWithUniqueOptions() {
        List<Tag> optionsTags = new ArrayList<>();
        optionsTags.add(Tag.builder().key("random-tag1").value("random-value1").build());
        optionsTags.add(Tag.builder().key("random-tag2").value("random-value2").build());
        optionsTags.add(Tag.builder().key("random-tag3").value("random-value3").build());
        Options options = Options.builder().tags(optionsTags).build();
        List<Tag> tags = tagManager.retrieveAllTags(options);
        assertTrue(tags.size() == 4);
    }

    @Test
    public void testRetrieveTagsWithDuplicatedMandatoryKey() {
        List<Tag> optionsTags = new ArrayList<>();
        optionsTags.add(Tag.builder().key("connector-iaas-tag").value("new-default-value").build());
        optionsTags.add(Tag.builder().key("random-tag").value("random-value").build());
        Options options = Options.builder().tags(optionsTags).build();
        List<Tag> tags = tagManager.retrieveAllTags(options);
        assertTrue(tags.size() == 2);
        assertThat(tags.stream().filter(tag -> tag.getKey().equals("connector-iaas-tag")).findAny().get().getValue(),
                   is("default-value"));
    }

}

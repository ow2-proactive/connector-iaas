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
import org.ow2.proactive.connector.iaas.model.Image;

import jersey.repackaged.com.google.common.collect.Sets;


public class ImageTest {
    @Test
    public void testEmptyConstructor() {
        Image image = new Image();
        assertThat(image.getName(), is(nullValue()));
    }

    @Test
    public void testConstructor() {
        Image image = new Image("image-id",
                                "image-name",
                                OperatingSystem.builder().family("image-os").build(),
                                "image-location",
                                "image-version");
        assertThat(image.getName(), is("image-name"));
    }

    @Test
    public void testEqualsAndHashcode() {
        Image image1 = new Image("image-id",
                                 "image-name",
                                 OperatingSystem.builder().family("image-os").build(),
                                 "image-location",
                                 "image-version");
        Image image2 = new Image("image-id",
                                 "image-name",
                                 OperatingSystem.builder().family("image-os").build(),
                                 "image-location",
                                 "image-version");

        Set<Image> images = Sets.newHashSet(image1, image2);

        assertThat(images.size(), is(1));
        assertThat(image1.equals(image2), is(true));
    }

}

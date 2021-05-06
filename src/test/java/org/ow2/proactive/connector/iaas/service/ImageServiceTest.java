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
package org.ow2.proactive.connector.iaas.service;

import static org.hamcrest.Matchers.is;
import static org.junit.Assert.assertThat;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.Set;

import org.junit.Before;
import org.junit.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.ow2.proactive.connector.iaas.cloud.CloudManager;
import org.ow2.proactive.connector.iaas.fixtures.InfrastructureFixture;
import org.ow2.proactive.connector.iaas.model.Image;
import org.ow2.proactive.connector.iaas.model.Infrastructure;
import org.ow2.proactive.connector.iaas.model.OperatingSystem;

import jersey.repackaged.com.google.common.collect.Sets;


public class ImageServiceTest {

    @InjectMocks
    private ImageService imageService;

    @Mock
    private InfrastructureService infrastructureService;

    @Mock
    private CloudManager cloudManager;

    @Before
    public void init() {
        MockitoAnnotations.initMocks(this);
    }

    @Test
    public void testGetAllImages() {
        Infrastructure infrastructure = InfrastructureFixture.getInfrastructure("id-aws",
                                                                                "aws",
                                                                                "endPoint",
                                                                                "userName",
                                                                                "password",
                                                                                null,
                                                                                null,
                                                                                null);
        when(infrastructureService.getInfrastructure(infrastructure.getId())).thenReturn(infrastructure);

        Set<Image> images = Sets.newHashSet();

        images.add(new Image("id", "name", OperatingSystem.builder().family("os").build(), "location"));

        when(cloudManager.getAllImages(infrastructure)).thenReturn(images);

        Set<Image> allImages = imageService.getAllImages(infrastructure.getId());

        assertThat(allImages.iterator().next().getId(), is("id"));
        assertThat(allImages.iterator().next().getName(), is("name"));

        verify(cloudManager, times(1)).getAllImages(infrastructure);

    }

    @Test(expected = javax.ws.rs.NotFoundException.class)
    public void testGetAllImagesException() {

        Infrastructure infrastructure = InfrastructureFixture.getInfrastructure("id-aws",
                                                                                "aws",
                                                                                "endPoint",
                                                                                "userName",
                                                                                "password",
                                                                                null,
                                                                                null,
                                                                                null);
        when(infrastructureService.getInfrastructure(infrastructure.getId())).thenReturn(null);

        imageService.getAllImages(infrastructure.getId());

        verify(cloudManager, times(0)).getAllImages(infrastructure);

    }

}

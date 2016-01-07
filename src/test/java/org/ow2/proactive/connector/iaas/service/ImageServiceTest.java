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
import org.ow2.proactive.connector.iaas.service.ImageService;
import org.ow2.proactive.connector.iaas.service.InfrastructureService;

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
        Infrastructure infrastructure = InfrastructureFixture.getInfrastructure("id-aws", "aws", "endPoint",
                "userName", "credential");
        when(infrastructureService.getInfrastructurebyName(infrastructure.getName()))
                .thenReturn(infrastructure);

        Set<Image> images = Sets.newHashSet();

        images.add(new Image("id", "name"));

        when(cloudManager.getAllImages(infrastructure)).thenReturn(images);

        Set<Image> allImages = imageService.getAllImages(infrastructure.getName());

        assertThat(allImages.iterator().next().getId(), is("id"));
        assertThat(allImages.iterator().next().getName(), is("name"));

        verify(cloudManager, times(1)).getAllImages(infrastructure);

    }

}

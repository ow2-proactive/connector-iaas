package org.ow2.proactive.iaas.connector.service;

import static org.hamcrest.Matchers.is;
import static org.junit.Assert.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.util.Set;

import org.jclouds.compute.ComputeService;
import org.jclouds.compute.domain.internal.ImageImpl;
import org.junit.Before;
import org.junit.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.ow2.proactive.iaas.connector.cache.ComputeServiceCache;
import org.ow2.proactive.iaas.connector.fixtures.InfrastructureFixture;
import org.ow2.proactive.iaas.connector.model.Infrastructure;

import jersey.repackaged.com.google.common.collect.Sets;

public class ImageServiceTest {

	@InjectMocks
	private ImageService imageService;

	@Mock
	private InfrastructureService infrastructureService;

	@Mock
	private ComputeServiceCache computeServiceCache;

	@Mock
	private ComputeService computeService;

	@Before
	public void init() {
		MockitoAnnotations.initMocks(this);
	}

	@Test
	public void testGetAllImages() {
		Infrastructure infratructure = InfrastructureFixture.getInfrastructure("aws", "endPoint", "userName",
				"credential");
		when(infrastructureService.getInfrastructurebyName(infratructure.getName())).thenReturn(infratructure);

		when(computeServiceCache.getComputeService(infratructure)).thenReturn(computeService);

		Set images = Sets.newHashSet();
		ImageImpl image = mock(ImageImpl.class);
		when(image.getId()).thenReturn("someId");
		images.add(image);
		when(computeService.listImages()).thenReturn(images);

		Set<String> allImages = imageService.getAllImages(infratructure.getName());

		assertThat(allImages.iterator().next(), is("someId"));

	}

	@Test
	public void testGetAllImagesEmptySet() {
		Infrastructure infratructure = InfrastructureFixture.getInfrastructure("aws", "endPoint", "userName",
				"credential");
		when(infrastructureService.getInfrastructurebyName(infratructure.getName())).thenReturn(infratructure);

		when(computeServiceCache.getComputeService(infratructure)).thenReturn(computeService);

		Set images = Sets.newHashSet();
		when(computeService.listImages()).thenReturn(images);

		Set<String> allImages = imageService.getAllImages(infratructure.getName());

		assertThat(allImages.isEmpty(), is(true));

	}

}

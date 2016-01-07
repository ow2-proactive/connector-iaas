package org.ow2.proactive.connector.iaas.rest;

import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertThat;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import javax.ws.rs.core.Response;

import org.junit.Before;
import org.junit.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.ow2.proactive.connector.iaas.rest.ImageRest;
import org.ow2.proactive.connector.iaas.service.ImageService;

import com.google.common.collect.Sets;

public class ImageRestTest {
	@InjectMocks
	private ImageRest imageRest;

	@Mock
	private ImageService imageService;

	@Before
	public void init() {
		MockitoAnnotations.initMocks(this);
	}

	@Test
	public void testListAllImage() {
		when(imageService.getAllImages("infrastructureName")).thenReturn(Sets.newHashSet());
		assertThat(imageRest.listAllImage("infrastructureName").getStatus(), is(Response.Status.OK.getStatusCode()));
		verify(imageService, times(1)).getAllImages("infrastructureName");
	}
}

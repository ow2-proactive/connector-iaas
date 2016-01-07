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
        Image image = new Image("image-id", "image-name");
        assertThat(image.getName(), is("image-name"));
    }

    @Test
    public void testEqualsAndHashcode() {
        Image image1 = new Image("image-id", "image-name");
        Image image2 = new Image("image-id", "image-name");

        Set<Image> images = Sets.newHashSet(image1, image2);

        assertThat(images.size(), is(1));
        assertThat(image1.equals(image2), is(true));
    }

}

package org.jahia.se.modules.dam.cloudinary.edp;

import org.jahia.services.content.JCRNodeWrapper;
import org.jahia.services.content.JCRPropertyWrapper;
import org.junit.Before;
import org.junit.Test;

import javax.jcr.RepositoryException;

import static org.jahia.se.modules.dam.cloudinary.Constants.CONTENT_TYPE_IMAGE;
import static org.junit.Assert.assertEquals;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

public class CloudinaryDecoratorTest {

    private static final String NODE_URL = "/files/default/sites/digitall/files/report.pdf";

    private JCRNodeWrapper node;

    @Before
    public void setUp() throws RepositoryException {
        node = mock(JCRNodeWrapper.class);
        when(node.isNodeType(CONTENT_TYPE_IMAGE)).thenReturn(false);
        when(node.getUrl()).thenReturn(NODE_URL);
    }

    private String urlFor(String storedUrl) throws RepositoryException {
        JCRPropertyWrapper property = mock(JCRPropertyWrapper.class);
        when(property.getString()).thenReturn(storedUrl);
        when(node.hasProperty("cloudy:url")).thenReturn(true);
        when(node.getProperty("cloudy:url")).thenReturn(property);
        return new CloudinaryDecorator(node).getUrl();
    }

    @Test
    public void keepsHttpsUrl() throws RepositoryException {
        String url = "https://res.cloudinary.com/demo/image/upload/v1/report%20Q3.pdf?a=1&b=2";
        assertEquals(url, urlFor(url));
    }

    @Test
    public void keepsHttpUrlWhateverTheCaseOfTheScheme() throws RepositoryException {
        assertEquals("http://res.cloudinary.com/demo/raw/upload/a.pdf", urlFor("http://res.cloudinary.com/demo/raw/upload/a.pdf"));
        assertEquals("HTTPS://res.cloudinary.com/demo/raw/upload/a.pdf", urlFor("HTTPS://res.cloudinary.com/demo/raw/upload/a.pdf"));
    }

    @Test
    public void usesNodeUrlForAnotherScheme() throws RepositoryException {
        assertEquals(NODE_URL, urlFor("ftp://files.example.com/report.pdf"));
    }

    @Test
    public void usesNodeUrlForAUrlWithNoScheme() throws RepositoryException {
        assertEquals(NODE_URL, urlFor("res.cloudinary.com/demo/raw/upload/a.pdf"));
    }

    private String imageUrlFor(String baseUrl) throws RepositoryException {
        when(node.isNodeType(CONTENT_TYPE_IMAGE)).thenReturn(true);
        JCRPropertyWrapper base = mock(JCRPropertyWrapper.class);
        when(base.getString()).thenReturn(baseUrl);
        JCRPropertyWrapper end = mock(JCRPropertyWrapper.class);
        when(end.getString()).thenReturn("v1/sample.jpg");
        when(node.getProperty("cloudy:baseUrl")).thenReturn(base);
        when(node.getProperty("cloudy:endUrl")).thenReturn(end);
        return new CloudinaryDecorator(node).getUrl();
    }

    @Test
    public void buildsImageUrlFromHttpsBaseUrl() throws RepositoryException {
        assertEquals("https://res.cloudinary.com/demo/image/upload/v1/sample.jpg",
                imageUrlFor("https://res.cloudinary.com/demo/image/upload"));
    }

    @Test
    public void usesNodeUrlForAnImageWithAnotherScheme() throws RepositoryException {
        assertEquals(NODE_URL, imageUrlFor("ftp://files.example.com/image/upload"));
    }

    @Test
    public void usesNodeUrlWithoutStoredUrl() {
        assertEquals(NODE_URL, new CloudinaryDecorator(node).getUrl());
    }
}

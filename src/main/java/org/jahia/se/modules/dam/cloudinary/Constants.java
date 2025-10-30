package org.jahia.se.modules.dam.cloudinary;

/**
 * Utility class for Cloudinary edp constants.
 */
public interface Constants {

    /**
     * The configuration PID for the Cloudinary provider.
     */
    String PROVIDER_CONFIGURATION_PID = "org.jahia.se.modules.dam.cloudinary.provider.config";
    String CACHE_CONFIGURATION_PID = "org.jahia.se.modules.dam.cloudinary.cache.config";

    String CONTENT_TYPE_IMAGE = "cloudynt:image";
    String CONTENT_TYPE_VIDEO = "cloudynt:video";
    String CONTENT_TYPE_PDF = "cloudynt:pdf";
    String CONTENT_TYPE_DOC = "cloudynt:document";
}

package org.jahia.se.modules.dam.cloudinary.service;


import org.jahia.se.modules.dam.cloudinary.CloudinaryProviderConfig;

public interface CloudinaryProviderService {

    /**
     * Get the Cloudinary provider configuration
     * @return the Cloudinary provider configuration
     */
    CloudinaryProviderConfig getConfig();
}

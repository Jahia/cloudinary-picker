package org.jahia.se.modules.dam.cloudinary.service;

import org.jahia.exceptions.JahiaInitializationException;

/**
 * Service to handle cloudinary mount point
 */
public interface CloudinaryMountPointService {
    /**
     * Start and mount the cloudinary EDP implementation
     *
     * @param cloudinaryProviderService the provider service
     * @throws JahiaInitializationException
     */
    void start(CloudinaryProviderService cloudinaryProviderService) throws JahiaInitializationException;

    /**
     * Stop and unmount the cloudinary EDP implementation
     */
    void stop();
}

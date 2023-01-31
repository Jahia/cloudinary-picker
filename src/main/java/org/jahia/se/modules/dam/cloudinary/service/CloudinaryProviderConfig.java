package org.jahia.se.modules.dam.cloudinary.service;

public interface CloudinaryProviderConfig {
    /**
     * The name of the cloudinary cloud you want to connect to
     * @return the Cloudinary cloud name
     */
    String getCloudName();

    /**
     * The API key
     * @return the Cloudinary api key
     */
    String getApiKey();

    /**
     * The Cloudinary API secret;
     * @return the Cloudinary api secret
     */
    String getApiSecret();
}

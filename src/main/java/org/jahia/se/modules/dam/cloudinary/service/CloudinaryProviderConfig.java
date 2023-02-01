package org.jahia.se.modules.dam.cloudinary.service;

public interface CloudinaryProviderConfig {
    /**
     * The http schema used to execute the request; default is 'https'
     * @return the Cloudinary api schema
     */
    String getApiSchema();

    /**
     * The API endpoint; default is 'api.cloudinary.com'
     * @return the Cloudinary api endpoint
     */
    String getApiEndPoint();

    /**
     * The Cloudinary API version; default is 'v1_1'
     * @return the Cloudinary api version
     */
    String getApiVersion();

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

    /**
     * The name of the cloudinary cloud you want to connect to
     * @return the Cloudinary cloud name
     */
    String getCloudName();
}

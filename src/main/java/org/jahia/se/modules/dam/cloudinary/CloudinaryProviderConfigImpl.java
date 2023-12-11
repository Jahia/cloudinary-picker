package org.jahia.se.modules.dam.cloudinary;

import org.apache.commons.lang3.StringUtils;
import org.jahia.se.modules.dam.cloudinary.service.CloudinaryMountPointService;
import org.jahia.se.modules.dam.cloudinary.service.CloudinaryProviderConfig;
import org.osgi.framework.Bundle;
import org.osgi.framework.BundleContext;
import org.osgi.service.cm.ConfigurationException;
import org.osgi.service.cm.ManagedService;
import org.osgi.service.component.annotations.Activate;
import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Deactivate;
import org.osgi.service.component.annotations.Reference;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Dictionary;
import java.util.Hashtable;

@Component(service = {CloudinaryProviderConfig.class,
        ManagedService.class}, property = "service.pid=org.jahia.se.modules.cloudinary_picker_credentials", immediate = true)
public class CloudinaryProviderConfigImpl implements ManagedService, CloudinaryProviderConfig {

    public static final Logger logger = LoggerFactory.getLogger(CloudinaryProviderConfigImpl.class);

    private Dictionary<String, ?> properties = new Hashtable<>();
    private BundleContext bundleContext;
    private CloudinaryMountPointService cloudinaryMountPointService;

    @Reference
    public void setCloudinaryMountPointService(CloudinaryMountPointService cloudinaryMountPointService) {
        this.cloudinaryMountPointService = cloudinaryMountPointService;
    }

    @Override
    public void updated(Dictionary<String, ?> properties) throws ConfigurationException {
        if (properties != null) {
            this.properties = properties;
            logger.info("Cloudinary provider configuration reloaded");
            if (isConfigurationReady() && bundleContext.getBundle().getState() == Bundle.ACTIVE) {
                logger.info("Cloudinary provider configuration is ready");
                startServices();
            } else {
                logger.warn("Cloudinary provider configuration is incomplete, please check your configuration");
                stopServices();
            }
        } else {
            this.properties = new Hashtable<>();
            logger.info("Cloudinary provider configuration removed");
            stopServices();
        }
    }

    @Activate
    public void activate(BundleContext bundleContext) {
        this.bundleContext = bundleContext;
    }

    @Deactivate
    public void deactivate() throws ConfigurationException {
        stopServices();
    }

    private void startServices() throws ConfigurationException {
        try {
            cloudinaryMountPointService.start(this);
        } catch (Exception e) {
            throw new ConfigurationException("Global config", "Error starting Cloudinary Provider services", e);
        }
    }

    private void stopServices() throws ConfigurationException {
        try {
            cloudinaryMountPointService.stop();
        } catch (Exception e) {
            throw new ConfigurationException("Global config", "Error stopping Cloudinary Provider services", e);
        }
    }

    private boolean isConfigurationReady() {
        return StringUtils.isNotEmpty(getApiSchema()) &&
                StringUtils.isNotEmpty(getApiEndPoint()) &&
                StringUtils.isNotEmpty(getApiVersion()) &&
                StringUtils.isNotEmpty(getApiKey()) &&
                StringUtils.isNotEmpty(getApiSecret()) &&
                StringUtils.isNotEmpty(getCloudName());
    }

    @Override
    public String getApiSchema() {
        return (String) properties.get("cloudinary_provider.apiSchema");
    }

    @Override
    public String getApiEndPoint() {
        return (String) properties.get("cloudinary_provider.apiEndPoint");
    }

    @Override
    public String getApiVersion() {
        return (String) properties.get("cloudinary_provider.apiVersion");
    }

    @Override
    public String getApiKey() {
        return (String) properties.get("cloudinary_provider.apiKey");
    }

    @Override
    public String getApiSecret() {
        return (String) properties.get("cloudinary_provider.apiSecret");
    }

    @Override
    public String getCloudName() {
        return (String) properties.get("cloudinary_provider.cloudName");
    }

}

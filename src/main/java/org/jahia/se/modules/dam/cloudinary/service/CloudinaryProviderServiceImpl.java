package org.jahia.se.modules.dam.cloudinary.service;

import org.apache.commons.lang3.StringUtils;
import org.jahia.se.modules.dam.cloudinary.CloudinaryProviderConfig;
import org.jahia.se.modules.dam.cloudinary.Constants;
import org.osgi.framework.Bundle;
import org.osgi.framework.BundleContext;
import org.osgi.service.cm.ConfigurationException;
import org.osgi.service.component.annotations.*;
import org.osgi.service.metatype.annotations.Designate;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.Base64;

@Component(
        service = {CloudinaryProviderService.class},
        configurationPid = Constants.PROVIDER_CONFIGURATION_PID,
        immediate = true
)
@Designate(ocd = CloudinaryProviderConfig.class)
public class CloudinaryProviderServiceImpl implements CloudinaryProviderService {

    public static final Logger LOGGER = LoggerFactory.getLogger(CloudinaryProviderServiceImpl.class);

    private CloudinaryProviderConfig config;
    private BundleContext bundleContext;
    private CloudinaryMountPointService cloudinaryMountPointService;
    private String configHash;

    @Reference
    public void setCloudinaryMountPointService(CloudinaryMountPointService cloudinaryMountPointService) {
        this.cloudinaryMountPointService = cloudinaryMountPointService;
    }

    @Activate
    public void activate(BundleContext bundleContext, CloudinaryProviderConfig config) throws ConfigurationException {
        this.bundleContext = bundleContext;
        if (config != null) {
            this.config = config;
            this.configHash = hash(config);
            if (isConfigurationReady() && bundleContext.getBundle().getState() == Bundle.ACTIVE) {
                LOGGER.info("Cloudinary provider configuration is ready");
                startServices();
            } else {
                LOGGER.warn("Cloudinary provider configuration is incomplete, please check your configuration");
                stopServices();
            }
        } else {
            LOGGER.error("Cloudinary provider configuration is missing");
            stopServices();
        }
    }

    @Modified
    protected void modified(CloudinaryProviderConfig config) throws Exception {
        if (!hash(config).equals(this.configHash)) {
            LOGGER.info("Changes detected in provider config. Cloudinary provider config would be recreated");
            deactivate();
            activate(bundleContext, config);
        }
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
        return StringUtils.isNotEmpty(config.apiSchema()) &&
                StringUtils.isNotEmpty(config.apiEndPoint()) &&
                StringUtils.isNotEmpty(config.apiVersion()) &&
                StringUtils.isNotEmpty(config.apiKey()) &&
                StringUtils.isNotEmpty(config.apiSecret()) &&
                StringUtils.isNotEmpty(config.cloudName());
    }

    @Override
    public CloudinaryProviderConfig getConfig() {
        return config;
    }

    private String hash(CloudinaryProviderConfig config) {
        try {
            MessageDigest digest = MessageDigest.getInstance("SHA-256");
            digest.update(config.apiSchema().getBytes(StandardCharsets.UTF_8));
            digest.update(config.apiEndPoint().getBytes(StandardCharsets.UTF_8));
            digest.update(config.apiVersion().getBytes(StandardCharsets.UTF_8));
            digest.update(config.apiKey().getBytes(StandardCharsets.UTF_8));
            digest.update(config.apiSecret().getBytes(StandardCharsets.UTF_8));
            digest.update(config.cloudName().getBytes(StandardCharsets.UTF_8));
            byte[] hashBytes = digest.digest();
            return Base64.getEncoder().encodeToString(hashBytes);
        } catch (NoSuchAlgorithmException e) {
            throw new RuntimeException("Unable to find SHA-256 algorithm", e);
        }
    }
}


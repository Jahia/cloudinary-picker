package org.jahia.se.modules.dam.cloudinary.edp;
import org.jahia.exceptions.JahiaInitializationException;
import org.jahia.modules.external.ExternalContentStoreProvider;
import org.jahia.modules.external.ExternalProviderInitializerService;
import org.jahia.se.modules.dam.cloudinary.service.CloudinaryMountPointService;
import org.jahia.se.modules.dam.cloudinary.service.CloudinaryProviderConfig;
import org.jahia.services.content.JCRSessionFactory;
import org.jahia.services.content.JCRStoreService;
import org.jahia.services.sites.JahiaSitesService;
import org.jahia.services.usermanager.JahiaGroupManagerService;
import org.jahia.services.usermanager.JahiaUserManagerService;
import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Reference;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

//import java.util.Arrays;


/**
 * Factory for the EDP cloudinary mount point
 */
@Component(service = {CloudinaryMountPointService.class}, immediate = true)
public class CloudinaryMountPointServiceImpl implements  CloudinaryMountPointService{
    private static final Logger logger = LoggerFactory.getLogger(CloudinaryDataSource.class);

//    public static final String WIDEN_NODETYPE = "kibanant:dashboard";
//    private static final List<String> EXTENDABLE_TYPES = Arrays.asList(DASHBOARD_NODETYPE);
//    private static final List<String> OVERRIDABLE_ITEMS = Collections.singletonList("*.*");

    private ExternalContentStoreProvider cloudinaryProvider;

    // Core deps
    private JahiaUserManagerService userManagerService;
    private JahiaGroupManagerService groupManagerService;
    private JahiaSitesService sitesService;
    private JCRStoreService jcrStoreService;
    private JCRSessionFactory sessionFactory;

    // EDP deps
    private ExternalProviderInitializerService externalProviderInitializerService;

    // internal deps
    private CloudinaryCacheManager cloudinaryCacheManager;

    @Reference
    public void setUserManagerService(JahiaUserManagerService userManagerService) {
        this.userManagerService = userManagerService;
    }

    @Reference
    public void setGroupManagerService(JahiaGroupManagerService groupManagerService) {
        this.groupManagerService = groupManagerService;
    }

    @Reference
    public void setSitesService(JahiaSitesService sitesService) {
        this.sitesService = sitesService;
    }

    @Reference
    public void setJcrStoreService(JCRStoreService jcrStoreService) {
        this.jcrStoreService = jcrStoreService;
    }

    @Reference
    public void setSessionFactory(JCRSessionFactory sessionFactory) {
        this.sessionFactory = sessionFactory;
    }

    @Reference
    public void setExternalProviderInitializerService(ExternalProviderInitializerService externalProviderInitializerService) {
        this.externalProviderInitializerService = externalProviderInitializerService;
    }

    @Reference
    public void setCloudinaryCacheManager(CloudinaryCacheManager cloudinaryCacheManager) {
        this.cloudinaryCacheManager = cloudinaryCacheManager;
    }

    @Override
    public void start(CloudinaryProviderConfig cloudinaryProviderConfig) throws JahiaInitializationException {
        logger.info("Starting Cloudinary mount point service");
        cloudinaryProvider = new ExternalContentStoreProvider();
        cloudinaryProvider.setUserManagerService(userManagerService);
        cloudinaryProvider.setGroupManagerService(groupManagerService);
        cloudinaryProvider.setSitesService(sitesService);
        cloudinaryProvider.setService(jcrStoreService);
        cloudinaryProvider.setSessionFactory(sessionFactory);
        cloudinaryProvider.setExternalProviderInitializerService(externalProviderInitializerService);

        cloudinaryProvider.setDataSource(new CloudinaryDataSource(cloudinaryProviderConfig, cloudinaryCacheManager));
//        cloudinaryProvider.setExtendableTypes(EXTENDABLE_TYPES);
//        cloudinaryProvider.setOverridableItems(OVERRIDABLE_ITEMS);
        cloudinaryProvider.setDynamicallyMounted(false);
        cloudinaryProvider.setMountPoint("/sites/systemsite/contents/dam-cloudinary");
        cloudinaryProvider.setKey("cloudinary");
        cloudinaryProvider.start();
        logger.info("Cloudinary mount point service started");
    }

    @Override
    public void stop() {
        if (cloudinaryProvider != null) {
            logger.info("Stopping Cloudinary mount point service");
            cloudinaryProvider.stop();
            cloudinaryProvider = null;
            logger.info("Cloudinary mount point service stopped");
        }
    }
}

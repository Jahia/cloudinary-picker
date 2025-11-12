package org.jahia.se.modules.dam.cloudinary.edp;

import net.sf.ehcache.Cache;
import net.sf.ehcache.CacheManager;
import net.sf.ehcache.Ehcache;
import net.sf.ehcache.Element;
import net.sf.ehcache.config.CacheConfiguration;
import net.sf.ehcache.store.MemoryStoreEvictionPolicy;
import org.jahia.se.modules.dam.cloudinary.Constants;
import org.jahia.se.modules.dam.cloudinary.model.CloudinaryAsset;
import org.jahia.services.SpringContextSingleton;
import org.jahia.services.cache.CacheHelper;
import org.jahia.services.cache.ModuleClassLoaderAwareCacheEntry;
import org.jahia.services.cache.ehcache.EhCacheProvider;
import org.osgi.service.component.annotations.Activate;
import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Deactivate;
import org.osgi.service.component.annotations.Modified;
import org.osgi.service.metatype.annotations.Designate;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.nio.ByteBuffer;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.Base64;

@Component(
        service = CloudinaryCacheManager.class,
        configurationPid = Constants.CACHE_CONFIGURATION_PID,
        immediate = true
)
@Designate(ocd = CloudinaryCacheConfig.class)
public class CloudinaryCacheManager {
    public static final Logger LOGGER = LoggerFactory.getLogger(CloudinaryCacheManager.class);

    private Ehcache cache;
    private CloudinaryCacheConfig config;
    private String configHash;

    private static Ehcache initCache(EhCacheProvider cacheProvider, CloudinaryCacheConfig config) {
        CacheManager cacheManager = cacheProvider.getCacheManager();
        Ehcache cache = cacheManager.getCache(config.edpCacheName());
        if (cache == null) {
            cache = createCloudinaryCache(cacheManager, config);
        } else {
            cache.removeAll();
        }
        return cache;
    }

    private static Ehcache createCloudinaryCache(CacheManager cacheManager, CloudinaryCacheConfig config) {
        CacheConfiguration cacheConfiguration = new CacheConfiguration();
        cacheConfiguration.setName(config.edpCacheName());
        cacheConfiguration.memoryStoreEvictionPolicy(MemoryStoreEvictionPolicy.LFU);
        cacheConfiguration.setEternal(false);
        cacheConfiguration.timeToLiveSeconds(config.edpCacheTtl());
        cacheConfiguration.setTimeToIdleSeconds(config.edpCacheTti());
        // Create a new cache with the configuration
        Ehcache cache = new Cache(cacheConfiguration);
        cache.setName(config.edpCacheName());
        // Cache name has been set now we can initialize it by putting it in the manager.
        // Only Cache manager is initializing caches.
        return cacheManager.addCacheIfAbsent(cache);
    }

    @Activate
    public void activate(CloudinaryCacheConfig config) {
        if (config != null) {
            this.config = config;
            this.configHash = hash(config);

            EhCacheProvider cacheProvider = (EhCacheProvider) SpringContextSingleton.getBean("ehCacheProvider");
            this.cache = initCache(cacheProvider, config);
            LOGGER.info("Cloudinary cache initialized: {} (TTL: {}s, TTI: {}s)",
                config.edpCacheName(), config.edpCacheTtl(), config.edpCacheTti());
        } else {
            LOGGER.error("Cloudinary cache configuration is missing");
        }
    }

    @Modified
    protected void modified(CloudinaryCacheConfig config) throws Exception {
        if (!hash(config).equals(this.configHash)) {
            LOGGER.info("Changes detected in cache config. Cloudinary cache will be recreated");
            deactivate();
            activate(config);
        }
    }

    @Deactivate
    public void deactivate() {
        flush();
        if (config != null) {
            EhCacheProvider cacheProvider = (EhCacheProvider) SpringContextSingleton.getBean("ehCacheProvider");
            CacheManager cacheManager = cacheProvider.getCacheManager();
            cacheManager.removeCache(config.edpCacheName());
            LOGGER.info("Cloudinary cache removed: {}", config.edpCacheName());
        }
    }

    /**
     * This method flushes the Cloudinary cache
     */
    public void flush() {
        // Flush cache
        if (cache != null) {
            cache.removeAll();
            LOGGER.debug("Cloudinary cache flushed");
        }
    }

    public CloudinaryAsset getCloudinaryAsset(String cacheKey) {
        return (CloudinaryAsset) CacheHelper.getObjectValue(cache, cacheKey);
    }

    public void cacheCloudinaryAsset(String cacheKey, CloudinaryAsset cloudinaryAsset) {
        if (config != null) {
            cache.put(new Element(cacheKey, new ModuleClassLoaderAwareCacheEntry(cloudinaryAsset, config.edpCacheName())));
        }
    }

    private String hash(CloudinaryCacheConfig config) {
        try {
            // Only hash relevant cache configuration fields
            MessageDigest digest = MessageDigest.getInstance("SHA-256");
            digest.update(config.edpCacheName().getBytes(StandardCharsets.UTF_8));
            digest.update(ByteBuffer.allocate(Integer.BYTES).putInt(config.edpCacheTtl()).array());
            digest.update(ByteBuffer.allocate(Integer.BYTES).putInt(config.edpCacheTti()).array());
            byte[] hashBytes = digest.digest();
            return Base64.getEncoder().encodeToString(hashBytes);
        } catch (NoSuchAlgorithmException e) {
            throw new RuntimeException("Unable to find SHA-256 algorithm", e);
        }
    }
}

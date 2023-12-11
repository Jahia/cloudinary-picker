package org.jahia.se.modules.dam.cloudinary;

import org.osgi.framework.BundleContext;
import org.osgi.framework.FrameworkUtil;
import org.osgi.framework.ServiceReference;
import org.osgi.service.cm.Configuration;
import org.osgi.service.cm.ConfigurationAdmin;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.util.Collections;
import java.util.Dictionary;
import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;

public final class Functions {
    private static final Logger logger = LoggerFactory.getLogger(Functions.class);

    private Functions() {
        //
    }

    public static Map<String, Object> getProviderConfig() {
        Map<String, Object> properties = null;

        BundleContext bundleContext = FrameworkUtil.getBundle(Functions.class).getBundleContext();
        ServiceReference<ConfigurationAdmin> cmRef = bundleContext.getServiceReference(ConfigurationAdmin.class);
        ConfigurationAdmin configAdmin = bundleContext.getService(cmRef);
        try {
            Configuration config = configAdmin.getConfiguration("org.jahia.se.modules.cloudinary_picker_credentials");
            Dictionary<String, ?> dict = config.getProperties();
            List<String> keys = Collections.list(dict.keys());
            properties = keys.stream()
                    .collect(Collectors.toMap(Function.identity(), dict::get));

        } catch (IOException e) {
            logger.error("Error reading cloudinary config file e: " + e);
        }
        return properties;
    }
}

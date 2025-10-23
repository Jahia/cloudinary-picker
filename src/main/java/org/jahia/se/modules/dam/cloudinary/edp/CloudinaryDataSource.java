package org.jahia.se.modules.dam.cloudinary.edp;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.common.collect.Sets;
import org.apache.commons.lang.StringUtils;
import org.apache.http.HttpHeaders;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.client.utils.URIBuilder;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.util.EntityUtils;
import org.jahia.modules.external.ExternalData;
import org.jahia.modules.external.ExternalDataSource;
import org.jahia.se.modules.dam.cloudinary.model.CloudinaryAsset;
import org.jahia.se.modules.dam.cloudinary.service.CloudinaryProviderConfig;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.jcr.ItemNotFoundException;
import javax.jcr.PathNotFoundException;
import javax.jcr.RepositoryException;
import java.net.URI;
import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class CloudinaryDataSource implements ExternalDataSource {
    private static final Logger LOGGER = LoggerFactory.getLogger(CloudinaryDataSource.class);

    private final ObjectMapper mapper = new ObjectMapper();
    private final CloudinaryProviderConfig cloudinaryProviderConfig;
    private final CloudinaryCacheManager cloudinaryCacheManager;
    private final CloseableHttpClient httpClient;
    private final Pattern DERIVED_PATTERN = Pattern.compile("^(.+?)_(.+)$");

    // Reverse mappings for decoding base36 parameters
    private static final String[] CROP_MODES = {"", "scale", "fit", "limit", "mfit", "fill", "lfill", "pad", "lpad", "mpad", "crop", "thumb", "imagga_crop", "imagga_scale"};
    private static final String[] GRAVITY_VALUES = {"", "center", "north", "south", "east", "west", "north_east", "north_west", "south_east", "south_west", "face", "faces", "auto", "auto_face", "auto_faces"};
    private static final String[] FORMATS = {"", "webp", "jpg", "png", "gif", "auto"};

    public CloudinaryDataSource(CloudinaryProviderConfig cloudinaryProviderConfig, CloudinaryCacheManager cloudinaryCacheManager) {
        this.cloudinaryProviderConfig = cloudinaryProviderConfig;
        this.cloudinaryCacheManager = cloudinaryCacheManager;
        this.httpClient = HttpClients.createDefault();
    }

    @Override
    public List<String> getChildren(String s) throws RepositoryException {
        List<String> child = new ArrayList<String>();
        return child;
    }

    @Override
    public ExternalData getItemByIdentifier(String identifier) throws ItemNotFoundException {
        try {
            if (identifier.equals("root")) {
                return new ExternalData(identifier, "/", "jnt:folder", new HashMap<>());
            } else {
                String cloudyId = identifier;
                String derivedParams = null;
                boolean isContent = false;

                // Check for jcr:content suffix
                if (identifier.endsWith("/jcr:content")) {
                    cloudyId = StringUtils.substringBefore(identifier, "/jcr:content");
                    isContent = true;
                }

                // Extract derived parameters if present (base36 encoded params)
                Matcher derivedMatcher = DERIVED_PATTERN.matcher(cloudyId);
                if (derivedMatcher.matches()) {
                    cloudyId = derivedMatcher.group(1);
                    derivedParams = derivedMatcher.group(2);
                }

                synchronized (this) {
                    // Use full identifier (including derived params) as cache key
                    String cacheKey = derivedParams != null ? cloudyId + "_" + derivedParams : cloudyId;
                    CloudinaryAsset cloudinaryAsset = cloudinaryCacheManager.getCloudinaryAsset(cacheKey);

                    if (cloudinaryAsset == null) {
                        LOGGER.debug("no cacheEntry for : " + identifier);
                        //search returns more metadata than details direct ressources call
                        final String path = "/" + cloudinaryProviderConfig.getApiVersion() + "/" + cloudinaryProviderConfig.getCloudName() + "/resources/search";
                        final StringEntity jsonEntity = new StringEntity("{\"expression\": \"asset_id = " + cloudyId + "\"}");
                        cloudinaryAsset = queryCloudinary(path, jsonEntity, derivedParams);

                        cloudinaryCacheManager.cacheCloudinaryAsset(cacheKey, cloudinaryAsset);
                    }
                    ExternalData data = new ExternalData(identifier, "/" + identifier, isContent ? "jnt:resource" : cloudinaryAsset.getJahiaNodeType(), cloudinaryAsset.getProperties());
                    if (isContent) {
                        data.setBinaryProperties(cloudinaryAsset.getBinaryProperties());
                    }
                    return data;
                }
            }
        } catch (Exception e) {
            LOGGER.error("", e);
            throw new ItemNotFoundException(e);
        }
    }

    @Override
    public ExternalData getItemByPath(String path) throws PathNotFoundException {
        String[] splitPath = path.split("/");
        try {
            if (path.endsWith("j:acl")) {
                throw new PathNotFoundException(path);
            }

            if (splitPath.length <= 1) {
                return getItemByIdentifier("root");
            } else if (splitPath.length == 2) {
                return getItemByIdentifier(splitPath[1]);
            } else if (splitPath.length == 3 && splitPath[2].equals("jcr:content")) {
                return getItemByIdentifier(splitPath[1] + "/jcr:content");
            }
        } catch (ItemNotFoundException e) {
            throw new PathNotFoundException(e);
        }
        throw new PathNotFoundException();
    }

    @Override
    public Set<String> getSupportedNodeTypes() {
        return Sets.newHashSet("jnt:folder", "cloudynt:image", "cloudynt:video", "cloudynt:pdf", "cloudynt:document", "jnt:resource");
    }

    @Override
    public boolean isSupportsHierarchicalIdentifiers() {
        return false;
    }

    @Override
    public boolean isSupportsUuid() {
        return false;
    }

    @Override
    public boolean itemExists(String s) {
        try {
            getItemByPath(s);
            return true;
        } catch (PathNotFoundException e) {
            return false;
        }
    }

    private CloudinaryAsset queryCloudinary(String path, StringEntity jsonEntity, String derivedParams) throws RepositoryException {
        LOGGER.debug("Query Cloudinary with path : {} and derived params: {}", path, derivedParams);
        try {
            String schema = cloudinaryProviderConfig.getApiSchema();
            String endpoint = cloudinaryProviderConfig.getApiEndPoint();
            String apiKey = cloudinaryProviderConfig.getApiKey();
            String apiSecret = cloudinaryProviderConfig.getApiSecret();

            URIBuilder builder = new URIBuilder().setScheme(schema).setHost(endpoint).setPath(path);

            URI uri = builder.build();

            long l = System.currentTimeMillis();
            final HttpPost postMethod = new HttpPost(uri);
            postMethod.setEntity(jsonEntity);

            //NOTE Cloudinary return content in ISO-8859-1 even if Accept-Charset = UTF-8 is set.
            //Need to use appropriate charset later to read the inputstream response.
            String encoding = Base64.getEncoder().encodeToString((apiKey + ":" + apiSecret).getBytes("UTF-8"));
            postMethod.setHeader(HttpHeaders.AUTHORIZATION, "Basic " + encoding);
            postMethod.setHeader("Content-Type", "application/json");

            try (CloseableHttpResponse resp = httpClient.execute(postMethod)) {
                CloudinaryAsset cloudinaryAsset = mapper.readValue(EntityUtils.toString(resp.getEntity()), CloudinaryAsset.class);

                // If derived params exist, decode them from base36
                if (derivedParams != null) {
                    String decodedParams = decodeBase36Params(derivedParams);
                    if (decodedParams != null && !decodedParams.isEmpty()) {
                        cloudinaryAsset.addProperty("cloudy:derivedTransformation", decodedParams);
                    }
                }
                return cloudinaryAsset;
            } finally {
                LOGGER.debug("Request {} executed in {} ms", uri, (System.currentTimeMillis() - l));
            }
        } catch (Exception e) {
            LOGGER.error("Error while querying Cloudinary", e);
            throw new RepositoryException(e);
        }
    }

    // Method to decode base36 encoded parameters
    private String decodeBase36Params(String encoded) {
        if (encoded == null || encoded.isEmpty()) {
            return "";
        }

        try {
            StringBuilder transformation = new StringBuilder();
            String[] parts = encoded.split("\\.");

            for (int i = 0; i < parts.length; i += 2) {
                if (i + 1 >= parts.length) break;

                String key = parts[i];
                String value = parts[i + 1];

                if (transformation.length() > 0) {
                    transformation.append(",");
                }

                switch (key) {
                    case "c":
                        int cropMode = Integer.parseInt(value, 36);
                        transformation.append("c_").append(cropMode < CROP_MODES.length ? CROP_MODES[cropMode] : value);
                        break;
                    case "g":
                        int gravity = Integer.parseInt(value, 36);
                        transformation.append("g_").append(gravity < GRAVITY_VALUES.length ? GRAVITY_VALUES[gravity] : value);
                        break;
                    case "f":
                        int format = Integer.parseInt(value, 36);
                        transformation.append("f_").append(format < FORMATS.length ? FORMATS[format] : value);
                        break;
                    case "w":
                    case "h":
                    case "x":
                    case "y":
                    case "a":
                        transformation.append(key).append("_").append(Integer.parseInt(value, 36));
                        break;
                    case "z":
                    case "dpr":
                        double decimalValue = Integer.parseInt(value, 36) / 100.0;
                        transformation.append(key).append("_").append(decimalValue);
                        break;
                    case "ar":
                        // Handle aspect ratio (e.g., "g:5" -> "16:9")
                        if (value.contains(":")) {
                            String[] ratio = value.split(":");
                            transformation.append("ar_").append(Integer.parseInt(ratio[0], 36))
                                    .append(":").append(Integer.parseInt(ratio[1], 36));
                        } else {
                            transformation.append("ar_").append(Integer.parseInt(value, 36));
                        }
                        break;
                }
            }

            return transformation.toString();
        } catch (NumberFormatException e) {
            LOGGER.warn("Failed to decode base36 params: {}", encoded, e);
        }

        return "";
    }
}

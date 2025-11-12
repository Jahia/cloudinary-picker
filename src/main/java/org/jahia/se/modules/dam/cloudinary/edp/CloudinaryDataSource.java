package org.jahia.se.modules.dam.cloudinary.edp;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.common.collect.Sets;
import org.apache.commons.lang.StringUtils;
import org.apache.http.HttpHeaders;
import org.apache.http.client.config.RequestConfig;
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
import org.jahia.se.modules.dam.cloudinary.service.CloudinaryProviderService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.jcr.ItemNotFoundException;
import javax.jcr.PathNotFoundException;
import javax.jcr.RepositoryException;
import java.io.IOException;
import java.net.URI;
import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import static org.jahia.se.modules.dam.cloudinary.Constants.*;

/**
 * External Data Source for Cloudinary assets integration in Jahia.
 *
 * This class provides JCR-like access to Cloudinary assets through Jahia's
 * External Data Provider (EDP) framework.
 *
 * Key features:
 * - Fetches asset metadata from Cloudinary API
 * - Decodes base36-encoded transformation parameters from asset paths
 * - Caches asset data for performance
 * - Maps Cloudinary assets to Jahia node types
 *
 * Path format: /assetId or /assetId_base36params
 * Example: /abc123_c.9.w.5t represents asset abc123 with crop and width transformations
 */
public class CloudinaryDataSource implements ExternalDataSource {
    private static final Logger LOGGER = LoggerFactory.getLogger(CloudinaryDataSource.class);

    private final ObjectMapper mapper = new ObjectMapper();
    private final CloudinaryProviderService cloudinaryProviderService;
    private final CloudinaryCacheManager cloudinaryCacheManager;
    private CloseableHttpClient httpClient;

    // Pattern to extract derived parameters from identifier (assetId_params)
    private final Pattern DERIVED_PATTERN = Pattern.compile("^(.+?)_(.+)$");

    // Pre-compiled pattern for splitting base36 parameter string
    private static final Pattern PARAMS_SPLIT_PATTERN = Pattern.compile("\\.");

    // Reverse mappings for decoding base36 parameters (0-indexed)
    // These arrays must match the encoding mappings in base36.js
    private static final String[] CROP_MODES = {"scale", "fit", "limit", "mfit", "fill", "lfill", "pad", "lpad", "mpad", "crop", "thumb", "imagga_crop", "imagga_scale"};
    private static final String[] GRAVITY_VALUES = {"center", "north", "south", "east", "west", "north_east", "north_west", "south_east", "south_west", "face", "faces", "auto", "auto_face", "auto_faces"};
    private static final String[] FORMATS = {"webp", "jpg", "png", "gif", "auto"};

    public CloudinaryDataSource(CloudinaryProviderService cloudinaryProviderService, CloudinaryCacheManager cloudinaryCacheManager) {
        this.cloudinaryProviderService = cloudinaryProviderService;
        this.cloudinaryCacheManager = cloudinaryCacheManager;
        this.httpClient = createHttpClient();
    }

    /**
     * Creates HTTP client with configured timeouts from provider configuration.
     *
     * @return Configured CloseableHttpClient
     */
    private CloseableHttpClient createHttpClient() {
        RequestConfig requestConfig = RequestConfig.custom()
                .setConnectTimeout(cloudinaryProviderService.getConfig().connectionTimeout())
                .setSocketTimeout(cloudinaryProviderService.getConfig().socketTimeout())
                .setConnectionRequestTimeout(cloudinaryProviderService.getConfig().connectionRequestTimeout())
                .build();

        return HttpClients.custom()
                .setDefaultRequestConfig(requestConfig)
                .build();
    }

    /**
     * Closes the HTTP client and releases resources.
     * Should be called when the data source is being destroyed.
     */
    public void destroy() {
        if (httpClient != null) {
            try {
                httpClient.close();
            } catch (IOException e) {
                LOGGER.warn("Error closing HTTP client", e);
            }
        }
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

                // Check for jcr:content suffix (binary content node)
                if (identifier.endsWith("/jcr:content")) {
                    cloudyId = StringUtils.substringBefore(identifier, "/jcr:content");
                    isContent = true;
                }

                // Extract derived parameters if present (base36 encoded params)
                // Format: assetId_c.9.w.5t.x.11
                Matcher derivedMatcher = DERIVED_PATTERN.matcher(cloudyId);
                if (derivedMatcher.matches()) {
                    cloudyId = derivedMatcher.group(1);
                    derivedParams = derivedMatcher.group(2);
                }

                synchronized (this) {
                    // Use full identifier (including derived params) as cache key
                    // This ensures different transformations are cached separately
                    String cacheKey = derivedParams != null ? cloudyId + "_" + derivedParams : cloudyId;
                    CloudinaryAsset cloudinaryAsset = cloudinaryCacheManager.getCloudinaryAsset(cacheKey);

                    if (cloudinaryAsset == null) {
                        LOGGER.debug("no cacheEntry for : " + identifier);

                        // Query Cloudinary API for asset metadata
                        // Search API returns more metadata than direct resource call
                        final String path = "/" + cloudinaryProviderService.getConfig().apiVersion() + "/" + cloudinaryProviderService.getConfig().cloudName() + "/resources/search";
                        final StringEntity jsonEntity = new StringEntity("{\"expression\": \"asset_id = " + cloudyId + "\"}");
                        cloudinaryAsset = queryCloudinary(path, jsonEntity, derivedParams);

                        cloudinaryCacheManager.cacheCloudinaryAsset(cacheKey, cloudinaryAsset);
                    }

                    // Create ExternalData with appropriate node type
                    ExternalData data = new ExternalData(
                        identifier,
                        "/" + identifier,
                        isContent ? "jnt:resource" : cloudinaryAsset.getJahiaNodeType(),
                        cloudinaryAsset.getProperties()
                    );

                    // Add binary properties for content nodes
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
        return Sets.newHashSet("jnt:folder", CONTENT_TYPE_IMAGE, CONTENT_TYPE_VIDEO, CONTENT_TYPE_PDF, CONTENT_TYPE_DOC, "jnt:resource");
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

    /**
     * Queries Cloudinary API and processes the response.
     *
     * Uses timeout configuration from CloudinaryProviderConfig to prevent hanging on unresponsive service.
     *
     * @param path API endpoint path
     * @param jsonEntity Request body with search expression
     * @param derivedParams Base36-encoded transformation parameters
     * @return CloudinaryAsset with metadata and decoded transformations
     * @throws RepositoryException if query fails or times out
     */
    private CloudinaryAsset queryCloudinary(String path, StringEntity jsonEntity, String derivedParams) throws RepositoryException {
        LOGGER.debug("Query Cloudinary with path : {} and derived params: {}", path, derivedParams);
        try {
            String schema = cloudinaryProviderService.getConfig().apiSchema();
            String endpoint = cloudinaryProviderService.getConfig().apiEndPoint();
            String apiKey = cloudinaryProviderService.getConfig().apiKey();
            String apiSecret = cloudinaryProviderService.getConfig().apiSecret();

            URIBuilder builder = new URIBuilder().setScheme(schema).setHost(endpoint).setPath(path);
            URI uri = builder.build();

            long startTime = System.currentTimeMillis();
            final HttpPost postMethod = new HttpPost(uri);
            postMethod.setEntity(jsonEntity);

            // Set up Basic Authentication
            String encoding = Base64.getEncoder().encodeToString((apiKey + ":" + apiSecret).getBytes("UTF-8"));
            postMethod.setHeader(HttpHeaders.AUTHORIZATION, "Basic " + encoding);
            postMethod.setHeader("Content-Type", "application/json");

            try (CloseableHttpResponse resp = httpClient.execute(postMethod)) {
                int statusCode = resp.getStatusLine().getStatusCode();
                if (statusCode != 200) {
                    throw new RepositoryException("Cloudinary API returned status code: " + statusCode);
                }

                CloudinaryAsset cloudinaryAsset = mapper.readValue(EntityUtils.toString(resp.getEntity()), CloudinaryAsset.class);

                // If derived params exist, decode them from base36 and add to asset properties
                if (derivedParams != null) {
                    String decodedParams = decodeBase36Params(derivedParams);
                    if (decodedParams != null && !decodedParams.isEmpty()) {
                        cloudinaryAsset.addProperty("cloudy:derivedTransformation", decodedParams);
                    }
                }

                LOGGER.debug("Request {} executed in {} ms", uri, (System.currentTimeMillis() - startTime));
                return cloudinaryAsset;
            }
        } catch (Exception e) {
            LOGGER.error("Error while querying Cloudinary", e);
            throw new RepositoryException(e);
        }
    }

    /**
     * Decodes base36-encoded transformation parameters back to Cloudinary format.
     *
     * Input format: "c.9.w.5t.x.11.y.3n"
     * Output format: "c_crop,w_209,x_37,y_133"
     *
     * Decoding steps:
     * 1. Split by dot separator
     * 2. Process key-value pairs
     * 3. Convert base36 numbers back to decimal
     * 4. Map indices back to string values (crop modes, gravity, formats)
     * 5. Rebuild Cloudinary transformation string
     *
     * @param encoded Base36-encoded parameter string
     * @return Decoded Cloudinary transformation string
     */
    private String decodeBase36Params(String encoded) {
        if (encoded == null || encoded.isEmpty()) {
            return "";
        }

        try {
            StringBuilder transformation = new StringBuilder();
            String[] parts = PARAMS_SPLIT_PATTERN.split(encoded);

            // Process pairs of key-value
            for (int i = 0; i < parts.length; i += 2) {
                if (i + 1 >= parts.length) break;

                String key = parts[i];
                String value = parts[i + 1];

                if (transformation.length() > 0) {
                    transformation.append(",");
                }

                switch (key) {
                    case "c":
                        // Decode crop mode index to string
                        int cropMode = Integer.parseInt(value, 36);
                        transformation.append("c_").append(cropMode < CROP_MODES.length ? CROP_MODES[cropMode] : value);
                        break;
                    case "g":
                        // Decode gravity index to string
                        int gravity = Integer.parseInt(value, 36);
                        transformation.append("g_").append(gravity < GRAVITY_VALUES.length ? GRAVITY_VALUES[gravity] : value);
                        break;
                    case "f":
                        // Decode format index to string
                        int format = Integer.parseInt(value, 36);
                        transformation.append("f_").append(format < FORMATS.length ? FORMATS[format] : value);
                        break;
                    case "w":
                    case "h":
                    case "x":
                    case "y":
                    case "a":
                        // Simple base36 to decimal conversion
                        transformation.append(key).append("_").append(Integer.parseInt(value, 36));
                        break;
                    case "z":
                    case "dpr":
                        // Decode decimal values (divide by 100)
                        double decimalValue = Integer.parseInt(value, 36) / 100.0;
                        transformation.append(key).append("_").append(decimalValue);
                        break;
                    case "ar":
                        // Handle aspect ratio (e.g., "g_5" -> "16:9")
                        if (value.contains("_")) {
                            String[] ratio = value.split("_");
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
            // Log warning without full stacktrace to avoid log pollution
            LOGGER.warn("Failed to decode base36 params ({}): {}", encoded, e.getMessage());
            LOGGER.debug("Details", e);
        }

        return "";
    }
}

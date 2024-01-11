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

public class CloudinaryDataSource implements ExternalDataSource {
    private static final Logger LOGGER = LoggerFactory.getLogger(CloudinaryDataSource.class);

    private final ObjectMapper mapper = new ObjectMapper();
    private final CloudinaryProviderConfig cloudinaryProviderConfig;
    private final CloudinaryCacheManager cloudinaryCacheManager;
    private final CloseableHttpClient httpClient;

    public CloudinaryDataSource(CloudinaryProviderConfig cloudinaryProviderConfig, CloudinaryCacheManager cloudinaryCacheManager) {
        this.cloudinaryProviderConfig = cloudinaryProviderConfig;
        this.cloudinaryCacheManager = cloudinaryCacheManager;
        // instantiate HttpClient
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
                boolean isContent = false;
                if (identifier.endsWith("/jcr:content")) {
                    cloudyId = StringUtils.substringBefore(identifier, "/jcr:content");
                    isContent = true;
                }

                //TODO
                synchronized (this) {
                    CloudinaryAsset cloudinaryAsset = cloudinaryCacheManager.getCloudinaryAsset(cloudyId);
                    if (cloudinaryAsset == null) {
                        LOGGER.debug("no cacheEntry for : " + identifier);
                        final String path = "/" + cloudinaryProviderConfig.getApiVersion() + "/" + cloudinaryProviderConfig.getCloudName() + "/resources/search";
                        final StringEntity jsonEntity = new StringEntity("{\"expression\": \"asset_id = " + identifier + "\"}");
                        cloudinaryAsset = queryCloudinary(path, jsonEntity);
                        cloudinaryCacheManager.cacheCloudinaryAsset(cloudinaryAsset);
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

    private CloudinaryAsset queryCloudinary(String path, StringEntity jsonEntity) throws RepositoryException {
        LOGGER.debug("Query Cloudinary with path : {} and jsonEntity : {}", path, jsonEntity);
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
            CloseableHttpResponse resp = null;
            try {
                resp = httpClient.execute(postMethod);
                CloudinaryAsset cloudinaryAsset = mapper.readValue(EntityUtils.toString(resp.getEntity()), CloudinaryAsset.class);
                return cloudinaryAsset;

            } finally {
                if (resp != null) {
                    resp.close();
                }
                LOGGER.debug("Request {} executed in {} ms", uri, (System.currentTimeMillis() - l));
            }
        } catch (Exception e) {
            LOGGER.error("Error while querying Cloudinary", e);
            throw new RepositoryException(e);
        }
    }
}

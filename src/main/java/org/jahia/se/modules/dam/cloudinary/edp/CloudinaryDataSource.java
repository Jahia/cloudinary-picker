package org.jahia.se.modules.dam.cloudinary.edp;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.common.collect.Sets;
import org.apache.http.HttpHeaders;
import org.apache.http.NameValuePair;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.utils.URIBuilder;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.message.BasicNameValuePair;
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

//,ExternalDataSource.Searchable.class not used for now, needed if you want to use AugSearch with external asset
public class CloudinaryDataSource implements ExternalDataSource{
    private static final Logger LOGGER = LoggerFactory.getLogger(CloudinaryDataSource.class);

//    private static final String ASSET_ENTRY = "assets";
//    private static final String ASSET_ENTRY_EXPAND = "embeds,thumbnails,file_properties";

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
                return new ExternalData(identifier, "/", "jnt:contentFolder", new HashMap<String, String[]>());
            }else{
                //TODO
                synchronized (this){
                    CloudinaryAsset cloudinaryAsset = cloudinaryCacheManager.getCloudinaryAsset(identifier);
                    if(cloudinaryAsset == null){
                        LOGGER.debug("no cacheEntry for : "+identifier);
                        String path = "/"+cloudinaryProviderConfig.getApiVersion()+"/"+ASSET_ENTRY+"/"+identifier;
                        Map<String, String> query = new LinkedHashMap<String, String>();
                        query.put("expand",ASSET_ENTRY_EXPAND);
                        cloudinaryAsset = queryCloudinary(path,query);
                        cloudinaryCacheManager.cacheCloudinaryAsset(cloudinaryAsset);
                    }
                    ExternalData data = new ExternalData(identifier, "/"+identifier, cloudinaryAsset.getJahiaNodeType(), cloudinaryAsset.getProperties());
                    return data;
                }
            }
        } catch (Exception e) {
            LOGGER.error("",e);
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

            }
        } catch (ItemNotFoundException e) {
            throw new PathNotFoundException(e);
        }
        throw new PathNotFoundException();
    }

    @Override
    public Set<String> getSupportedNodeTypes() {
        return Sets.newHashSet(
                "jnt:contentFolder",
                "cloudynt:image",
                "cloudynt:video",
                "cloudynt:pdf",
                "cloudynt:document"
        );
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
        return false;
    }

    private CloudinaryAsset queryCloudinary(String path, Map<String, String> query) throws RepositoryException {
        LOGGER.debug("Query Cloudinary with path : {} and query : {}",path,query);
        try {
            String cloudName = cloudinaryProviderConfig.getCloudName();
            String apiKey = cloudinaryProviderConfig.getApiKey();
            String apiSecret = cloudinaryProviderConfig.getApiSecret();
            List<NameValuePair> parameters = new ArrayList<>(query.size());

            for (Map.Entry<String, String> entry : query.entrySet()) {
                parameters.add(new BasicNameValuePair(entry.getKey(), entry.getValue()));
            }

            URIBuilder builder = new URIBuilder()
                    .setScheme("https")
                    .setHost(endpoint)
                    .setPath(path)
                    .setParameters(parameters);

            URI uri = builder.build();

            long l = System.currentTimeMillis();
            HttpGet getMethod = new HttpGet(uri);

            //NOTE Cloudinary return content in ISO-8859-1 even if Accept-Charset = UTF-8 is set.
            //Need to use appropriate charset later to read the inputstream response.
            getMethod.setHeader(HttpHeaders.AUTHORIZATION,"Bearer "+cloudinarySite+"/"+cloudinaryToken);
//            getMethod.setRequestHeader("Content-Type","application/json");
//            getMethod.setRequestHeader("Accept-Charset","ISO-8859-1");
//            getMethod.setRequestHeader("Accept-Charset","UTF-8");
            CloseableHttpResponse resp = null;
            try {
                resp = httpClient.execute(getMethod);
                CloudinaryAsset cloudinaryAsset = mapper.readValue(EntityUtils.toString(resp.getEntity()),CloudinaryAsset.class);
                return cloudinaryAsset;

            } finally {
                if (resp != null) {
                    resp.close();
                }
                LOGGER.debug("Request {} executed in {} ms",uri, (System.currentTimeMillis() - l));
            }
        } catch (Exception e) {
            LOGGER.error("Error while querying Cloudinary", e);
            throw new RepositoryException(e);
        }
    }
}

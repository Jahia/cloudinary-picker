package org.jahia.se.modules.dam.cloudinary.model;

import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.DeserializationContext;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.deser.std.StdDeserializer;
import org.apache.jackrabbit.value.BinaryImpl;
import org.joda.time.format.ISODateTimeFormat;

import javax.jcr.RepositoryException;
import java.io.IOException;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashSet;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import static org.jahia.se.modules.dam.cloudinary.Constants.*;

public class CloudinaryAssetDeserializer extends StdDeserializer<CloudinaryAsset> {

    private static final String RESOURCE_TYPE_IMAGE = "image";
    private static final String RESOURCE_TYPE_VIDEO = "video";
    private static final String FORMAT_PDF = "pdf";

    // Cloudinary stores 3D models under the "image" resource type, so the format is what tells them apart.
    // Cloudinary also lists "bw" among the 3D upload formats; it is left out here because it is an SGI
    // raster format too, and classifying a picture as a 3D model is the more damaging mistake.
    private static final Set<String> MODEL_3D_FORMATS = Collections.unmodifiableSet(new HashSet<>(
            Arrays.asList("3ds", "fbx", "glb", "gltf", "obj", "ply", "u3ma", "usdz")));



    private class Urls {
        private String baseUrl;
        private String endUrl;

        public Urls(String url) {
            String regex = "(?<baseUrl>.*upload)/(?<endUrl>.*)";
            Pattern urlPattern = Pattern.compile(regex);
            Matcher matcher = urlPattern.matcher(url);

            if (matcher.find()) {
                baseUrl = matcher.group("baseUrl");
                endUrl = matcher.group("endUrl");
            }
        }

        public String getBaseUrl() {
            return baseUrl;
        }

        public String getEndUrl() {
            return endUrl;
        }
    }


    public CloudinaryAssetDeserializer() {
        this(null);
    }

    public CloudinaryAssetDeserializer(Class<?> vc) {
        super(vc);
    }

    @Override
    public CloudinaryAsset deserialize(JsonParser jsonParser, DeserializationContext deserializationContext) throws IOException, JsonProcessingException {
        JsonNode response = jsonParser.getCodec().readTree(jsonParser);
        JsonNode cloudinaryNode = response.get("resources").get(0);
        CloudinaryAsset cloudinaryAsset = new CloudinaryAsset();

        String resourceType = cloudinaryNode.get("resource_type").textValue();
        String format = cloudinaryNode.get("format").textValue();
        String url = cloudinaryNode.get("secure_url").textValue();
        Urls urls = new Urls(url);
        boolean model3d = RESOURCE_TYPE_IMAGE.equals(resourceType) && MODEL_3D_FORMATS.contains(format);

        // Handle both Cloudinary configuration modes:
        // - Static folder mode: uses "folder" property
        // - Dynamic folder mode: uses "asset_folder" property
        String folder = cloudinaryNode.has("folder")
                ? cloudinaryNode.get("folder").textValue()
                : (cloudinaryNode.has("asset_folder") ? cloudinaryNode.get("asset_folder").textValue() : "");


        // Use display_name if available (dynamic mode), otherwise use filename
        String title = cloudinaryNode.has("display_name")
                ? cloudinaryNode.get("display_name").textValue()
                : cloudinaryNode.get("filename").textValue();

        cloudinaryAsset.setId(cloudinaryNode.get("asset_id").textValue());
        cloudinaryAsset.addProperty("cloudy:assetId", cloudinaryNode.get("asset_id").textValue());
        cloudinaryAsset.addProperty("cloudy:publicId", cloudinaryNode.get("public_id").textValue());
        cloudinaryAsset.addProperty("cloudy:folder", folder);
        cloudinaryAsset.addProperty("jcr:title", title);
        cloudinaryAsset.addProperty("cloudy:format", format);
        cloudinaryAsset.addProperty("cloudy:version", cloudinaryNode.get("version").longValue());
        cloudinaryAsset.addProperty("cloudy:resourceType", resourceType);
        cloudinaryAsset.addProperty("jcr:mimeType", resourceType + "/" + format);
        cloudinaryAsset.addBinaryProperty("jcr:data", new BinaryImpl(new byte[0]) {
            @Override
            public long getSize() throws RepositoryException {
                return cloudinaryNode.get("bytes").longValue();
            }
        });
        cloudinaryAsset.addProperty("cloudy:type", cloudinaryNode.get("type").textValue());
        cloudinaryAsset.addProperty("cloudy:createdAt", cloudinaryNode.get("created_at").textValue());
        cloudinaryAsset.addProperty("cloudy:uploadedAt", cloudinaryNode.get("uploaded_at").textValue());
        cloudinaryAsset.addProperty("jcr:lastModified", ISODateTimeFormat.dateTimeNoMillis().parseDateTime(cloudinaryNode.get("uploaded_at").textValue()).toString());
        cloudinaryAsset.addProperty("cloudy:bytes", cloudinaryNode.get("bytes").longValue());
        addNumberIfPresent(cloudinaryNode, "width", cloudinaryAsset, "j:width");
        addNumberIfPresent(cloudinaryNode, "height", cloudinaryAsset, "j:height");
        addNumberIfPresent(cloudinaryNode, "aspect_ratio", cloudinaryAsset, "cloudy:aspectRatio");
        cloudinaryAsset.addProperty("cloudy:url", url);
        cloudinaryAsset.addProperty("cloudy:status", cloudinaryNode.get("status").textValue());
        cloudinaryAsset.addProperty("cloudy:accessMode", cloudinaryNode.get("access_mode").textValue());
        cloudinaryAsset.addProperty("cloudy:accessControl", cloudinaryNode.get("access_control").textValue());
        cloudinaryAsset.addProperty("cloudy:baseUrl", urls.getBaseUrl());
        cloudinaryAsset.addProperty("cloudy:endUrl", urls.getEndUrl());
//        splitURL(cloudinaryNode.get("secure_url").textValue(),cloudinaryAsset);

        switch (resourceType) {
            case RESOURCE_TYPE_IMAGE:
                if (FORMAT_PDF.equals(format)) {
                    cloudinaryAsset.setJahiaNodeType(CONTENT_TYPE_PDF);
                    addPoster(urls.getEndUrl(), cloudinaryAsset);
                } else if (model3d) {
                    cloudinaryAsset.setJahiaNodeType(CONTENT_TYPE_MODEL3D);
                    addPoster(urls.getEndUrl(), cloudinaryAsset);
                } else {
                    cloudinaryAsset.setJahiaNodeType(CONTENT_TYPE_IMAGE);
                }
                break;
            case RESOURCE_TYPE_VIDEO:
                cloudinaryAsset.setJahiaNodeType(CONTENT_TYPE_VIDEO);
                cloudinaryAsset.addProperty("cloudy:duration", cloudinaryNode.get("duration").doubleValue());
                addPoster(urls.getEndUrl(), cloudinaryAsset);
                break;
            default:
                cloudinaryAsset.setJahiaNodeType(CONTENT_TYPE_DOC);
                break;
        }
        return cloudinaryAsset;
    }

    /**
     * Copies a numeric field of the Cloudinary response onto the asset, when the response carries it.
     *
     * An asset that carries no raster dimensions, a 3D model for one, can come back without width,
     * height or aspect_ratio.
     */
    private void addNumberIfPresent(JsonNode source, String field, CloudinaryAsset cloudinaryAsset, String property) {
        JsonNode value = source.get(field);
        if (value != null && value.isNumber()) {
            cloudinaryAsset.addProperty(property, value.numberValue());
        }
    }

    private void addPoster(String url, CloudinaryAsset cloudinaryAsset) {
        url = url.substring(0, url.lastIndexOf('.')).concat(".jpg");
        cloudinaryAsset.addProperty("cloudy:poster", url);
    }
}

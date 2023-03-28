package org.jahia.se.modules.dam.cloudinary.model;

import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.DeserializationContext;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.deser.std.StdDeserializer;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class CloudinaryAssetDeserializer extends StdDeserializer<CloudinaryAsset> {
    private static final String PREFIX = "cloudy:";

    private static final String RESOURCE_TYPE_IMAGE = "image";
    private static final String RESOURCE_TYPE_VIDEO = "video";

    private static final String FORMAT_PDF = "pdf";

    private static final String CONTENT_TYPE_IMAGE = "cloudynt:image";
    private static final String CONTENT_TYPE_VIDEO = "cloudynt:video";
    private static final String CONTENT_TYPE_PDF = "cloudynt:pdf";
    private static final String CONTENT_TYPE_DOC = "cloudynt:document";

    private class Urls {
        private String baseUrl;
        private String endUrl;

        public Urls(String url){
            String regex = "(?<baseUrl>.*upload)/(?<endUrl>.*)";
            Pattern urlPattern = Pattern.compile(regex);
            Matcher matcher = urlPattern.matcher(url);

            if(matcher.find()){
                baseUrl=matcher.group("baseUrl");
                endUrl=matcher.group("endUrl");
            }
        }
        public String getBaseUrl(){return baseUrl;}
        public String getEndUrl(){return endUrl;}
    }


    public CloudinaryAssetDeserializer() {
        this(null);
    }

    public CloudinaryAssetDeserializer(Class<?> vc) {
        super(vc);
    }

    @Override
    public CloudinaryAsset deserialize(JsonParser jsonParser, DeserializationContext deserializationContext)
        throws IOException, JsonProcessingException {
        JsonNode response = jsonParser.getCodec().readTree(jsonParser);
        JsonNode cloudinaryNode = response.get("resources").get(0);
        CloudinaryAsset cloudinaryAsset = new CloudinaryAsset();

//        List<String> mixinTypes = new ArrayList<String>();
//        "cloudymix:cloudyAsset"
//        cloudinaryAsset.addProperty("jcr:mixinTypes","cloudymix:cloudyAsset");

        String resourceType = cloudinaryNode.get("resource_type").textValue();
        String format = cloudinaryNode.get("format").textValue();
        String url = cloudinaryNode.get("secure_url").textValue();
        Urls urls = new Urls(url);

        cloudinaryAsset.setId(cloudinaryNode.get("asset_id").textValue());
        cloudinaryAsset.addProperty(PREFIX+"assetId",cloudinaryNode.get("asset_id").textValue());
        cloudinaryAsset.addProperty(PREFIX+"publicId",cloudinaryNode.get("public_id").textValue());
        cloudinaryAsset.addProperty(PREFIX+"folder",cloudinaryNode.get("folder").textValue());
        cloudinaryAsset.addProperty("jcr:title",cloudinaryNode.get("filename").textValue());
        cloudinaryAsset.addProperty(PREFIX+"format",format);
        cloudinaryAsset.addProperty(PREFIX+"version",cloudinaryNode.get("version").longValue());
        cloudinaryAsset.addProperty(PREFIX+"resourceType",resourceType);
        cloudinaryAsset.addProperty(PREFIX+"type",cloudinaryNode.get("type").textValue());
        cloudinaryAsset.addProperty(PREFIX+"createdAt",cloudinaryNode.get("created_at").textValue());
        cloudinaryAsset.addProperty(PREFIX+"uploadedAt",cloudinaryNode.get("uploaded_at").textValue());
        cloudinaryAsset.addProperty(PREFIX+"bytes",cloudinaryNode.get("bytes").longValue());
        cloudinaryAsset.addProperty(PREFIX+"width",cloudinaryNode.get("width").longValue());
        cloudinaryAsset.addProperty(PREFIX+"height",cloudinaryNode.get("height").longValue());
        cloudinaryAsset.addProperty(PREFIX+"aspectRatio",cloudinaryNode.get("aspect_ratio").doubleValue());
        cloudinaryAsset.addProperty(PREFIX+"url",url);
        cloudinaryAsset.addProperty(PREFIX+"status",cloudinaryNode.get("status").textValue());
        cloudinaryAsset.addProperty(PREFIX+"accessMode",cloudinaryNode.get("access_mode").textValue());
        cloudinaryAsset.addProperty(PREFIX+"accessControl",cloudinaryNode.get("access_control").textValue());

        cloudinaryAsset.addProperty(PREFIX+"baseUrl",urls.getBaseUrl());
        cloudinaryAsset.addProperty(PREFIX+"endUrl",urls.getEndUrl());
//        splitURL(cloudinaryNode.get("secure_url").textValue(),cloudinaryAsset);

        switch (resourceType){
            case RESOURCE_TYPE_IMAGE :
                if( FORMAT_PDF.equals(format) ){
                    cloudinaryAsset.setJahiaNodeType(CONTENT_TYPE_PDF);
                    addPoster(urls.getEndUrl(),cloudinaryAsset);
                }else{
                    cloudinaryAsset.setJahiaNodeType(CONTENT_TYPE_IMAGE);
                }
                break;

            case RESOURCE_TYPE_VIDEO:
                cloudinaryAsset.setJahiaNodeType(CONTENT_TYPE_VIDEO);
                cloudinaryAsset.addProperty(PREFIX+"duration",cloudinaryNode.get("duration").doubleValue());
                addPoster(urls.getEndUrl(),cloudinaryAsset);
                break;

            default:
                cloudinaryAsset.setJahiaNodeType(CONTENT_TYPE_DOC);
                break;
        }
        return cloudinaryAsset;
    }

    private void addPoster(String url, CloudinaryAsset cloudinaryAsset){
        url = url.substring(0, url.lastIndexOf('.')).concat(".jpg") ;
        cloudinaryAsset.addProperty(PREFIX+"poster",url);
    }

//    private void splitURL(String url, CloudinaryAsset cloudinaryAsset){
//        String regex = "(?<baseUrl>.*upload)/(?<endUrl>.*)";
//        Pattern urlPattern = Pattern.compile(regex);
//        Matcher matcher = urlPattern.matcher(url);
//
//        if(matcher.find()){
//            cloudinaryAsset.addProperty(PREFIX+"baseUrl",matcher.group("baseUrl"));
//            cloudinaryAsset.addProperty(PREFIX+"endUrl",matcher.group("endUrl"));
//        }
//    }
}

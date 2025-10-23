package org.jahia.se.modules.dam.cloudinary.edp;

import org.apache.commons.lang.StringUtils;
import org.jahia.services.content.JCRNodeWrapper;
import org.jahia.services.content.decorator.JCRNodeDecorator;

import javax.jcr.RepositoryException;
import java.util.ArrayList;
import java.util.List;

import static org.jahia.se.modules.dam.cloudinary.ContentTypesConstants.*;

public class CloudinaryDecorator extends JCRNodeDecorator {

    private final String THUMBNAIL_WIDTH = "200";
    private final String URL_WIDTH = "1024";

    public CloudinaryDecorator(JCRNodeWrapper node) {
        super(node);
    }

    @Override
    public String getDisplayableName() {
        try {
            return node.getProperty("jcr:title").getString();
        } catch (RepositoryException e) {
            return super.getDisplayableName();
        }
    }

    @Override
    public String getUrl() {
        return buildCloudinaryUrl(null);
    }

    @Override
    public String getUrl(List<String> params) {
        return buildCloudinaryUrl(params);
    }

    private String buildCloudinaryUrl(List<String> params) {
        try {
            String baseUrl = node.getProperty("cloudy:baseUrl").getString();
            String endUrl = node.getProperty("cloudy:endUrl").getString();

            // Check if we have derived transformation from path
            String derivedTransformation = null;
            if (node.hasProperty("cloudy:derivedTransformation")) {
                derivedTransformation = node.getProperty("cloudy:derivedTransformation").getString();
            }

            // Build transformations
            List<String> transformations = new ArrayList<>();

            // Add derived transformation if present (takes priority)
            if (derivedTransformation != null && !derivedTransformation.isEmpty()) {
                transformations.add(derivedTransformation);
            } else if (params != null && !params.isEmpty()) {
                // Build transformations from params
                transformations.addAll(buildTransformationsFromParams(params));
            }

            // Build final URL
            StringBuilder sb = new StringBuilder();
            sb.append(baseUrl).append("/");

            if (!transformations.isEmpty()) {
                sb.append(String.join(",", transformations)).append("/");
            }

            sb.append(endUrl);

            return sb.toString();
        } catch (RepositoryException e) {
            return super.getUrl();
        }
    }

    private List<String> buildTransformationsFromParams(List<String> params) {
        List<String> transformations = new ArrayList<>();
        transformations.add("f_auto"); // Always add auto format

        for (String param : params) {
            if (param.startsWith("width:")) {
                String width = StringUtils.substringAfter(param, "width:");
                if (width.trim().isEmpty()) {
                    width = URL_WIDTH;
                }
                transformations.add("w_" + width);
            } else if (param.startsWith("height:")) {
                String height = StringUtils.substringAfter(param, "height:");
                if (!height.trim().isEmpty()) {
                    transformations.add("h_" + height);
                }
            } else if (param.startsWith("crop:")) {
                String crop = StringUtils.substringAfter(param, "crop:");
                if (!crop.trim().isEmpty()) {
                    transformations.add("c_" + crop);
                }
            } else if (param.startsWith("gravity:")) {
                String gravity = StringUtils.substringAfter(param, "gravity:");
                if (!gravity.trim().isEmpty()) {
                    transformations.add("g_" + gravity);
                }
            }
        }

        return transformations;
    }

    @Override
    public String getThumbnailUrl(String name) {
        String width = THUMBNAIL_WIDTH;
        try {
            StringBuilder sb = new StringBuilder();
            sb.append(node.getProperty("cloudy:baseUrl").getString());

            if ("poster".equals(name)) {
                width = URL_WIDTH;
            }

            sb.append("/f_auto,w_").append(width).append("/");

            if (node.hasProperty("cloudy:poster")) {
                sb.append(node.getProperty("cloudy:poster").getString());
            } else if (node.hasProperty("cloudy:endUrl")) {
                sb.append(node.getProperty("cloudy:endUrl").getString());
            }

            return sb.toString();
        } catch (RepositoryException e) {
            return getUrl();
        }
    }
}

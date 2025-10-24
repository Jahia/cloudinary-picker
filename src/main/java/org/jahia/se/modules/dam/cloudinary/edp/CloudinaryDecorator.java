package org.jahia.se.modules.dam.cloudinary.edp;

import org.apache.commons.lang.StringUtils;
import org.jahia.services.content.JCRNodeWrapper;
import org.jahia.services.content.decorator.JCRNodeDecorator;

import javax.jcr.RepositoryException;
import java.util.ArrayList;
import java.util.List;

import static org.jahia.se.modules.dam.cloudinary.ContentTypesConstants.*;

/**
 * JCR Node Decorator for Cloudinary assets.
 *
 * Enhances Cloudinary asset nodes with URL generation capabilities.
 * Constructs Cloudinary URLs with transformations from:
 * - Derived transformations (from picker selection)
 * - Dynamic parameters (from template calls)
 *
 * URL structure: baseUrl/transformations/endUrl
 * Example: https://res.cloudinary.com/demo/image/upload/c_crop,w_200/v123/sample.jpg
 */
public class CloudinaryDecorator extends JCRNodeDecorator {

    private final String THUMBNAIL_SIZE = "150";
    private final String THUMBNAIL2_SIZE = "350";
    private final String URL_SIZE = "1024";

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

    /**
     * Builds a Cloudinary URL with optional transformations.
     *
     * Priority order:
     * 1. Derived transformations (from picker selection with crops/edits)
     * 2. Dynamic parameters (from template render calls)
     * 3. No transformations (original asset)
     *
     * @param params Optional list of transformation parameters
     * @return Full Cloudinary URL with transformations
     */
    private String buildCloudinaryUrl(List<String> params) {
        try {
            String baseUrl = node.getProperty("cloudy:baseUrl").getString();
            String endUrl = node.getProperty("cloudy:endUrl").getString();

            // Check if we have derived transformation from path
            // This comes from assets selected with transformations in the picker
            String derivedTransformation = null;
            if (node.hasProperty("cloudy:derivedTransformation")) {
                derivedTransformation = node.getProperty("cloudy:derivedTransformation").getString();
            }

            // Build transformations list
            List<String> transformations = new ArrayList<>();

            // Add derived transformation if present (takes priority)
            if (derivedTransformation != null && !derivedTransformation.isEmpty()) {
                transformations.add(derivedTransformation);
            } else if (params != null && !params.isEmpty()) {
                // Build transformations from dynamic params
                transformations.addAll(buildTransformationsFromParams(params));
            }

            // Build final URL: baseUrl/transformations/endUrl
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

    /**
     * Builds transformation list from template parameters.
     *
     * Supported parameters:
     * - width: Target width
     * - height: Target height
     * - crop: Crop mode (scale, fit, fill, etc.)
     * - gravity: Focal point (center, face, auto, etc.)
     *
     * @param params List of "key:value" parameter strings
     * @return List of Cloudinary transformation strings
     */
    private List<String> buildTransformationsFromParams(List<String> params) {
        List<String> transformations = new ArrayList<>();
        transformations.add("f_auto"); // Always add auto format for optimization

        for (String param : params) {
            if (param.startsWith("width:")) {
                String width = StringUtils.substringAfter(param, "width:");
                if (width.trim().isEmpty()) {
                    width = URL_SIZE;
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

    /**
     * Generates thumbnail URL with automatic optimizations.
     *
     * Thumbnail types:
     * - "thumbnail": 150px on the largest dimension (width or height)
     * - "thumbnail2": 350px on the largest dimension (width or height)
     * - other names: 1024px on the largest dimension
     *
     * Always includes:
     * - f_auto: Automatic format selection
     * - c_limit: Resize only if larger, maintaining aspect ratio
     * - w_X,h_X: Both dimensions to ensure the largest side is resized
     *
     * @param name Thumbnail type ("thumbnail", "thumbnail2", or other)
     * @return Thumbnail URL with appropriate transformations
     */
    @Override
    public String getThumbnailUrl(String name) {
        String size = URL_SIZE;

        // Determine size based on name
        if ("thumbnail".equals(name)) {
            size = THUMBNAIL_SIZE;
        } else if ("thumbnail2".equals(name)) {
            size = THUMBNAIL2_SIZE;
        }

        try {
            StringBuilder sb = new StringBuilder();
            sb.append(node.getProperty("cloudy:baseUrl").getString());

            // Use c_limit with both w and h set to the same value
            // This ensures the largest dimension is resized to the specified size
            // while maintaining aspect ratio
            sb.append("/f_auto,c_limit,w_").append(size).append(",h_").append(size).append("/");

            // Use poster image for videos, or main image for other types
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

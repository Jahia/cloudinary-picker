package org.jahia.se.modules.dam.cloudinary.edp;

import org.apache.commons.lang.StringUtils;
import org.jahia.services.content.JCRNodeWrapper;
import org.jahia.services.content.decorator.JCRNodeDecorator;

import javax.jcr.RepositoryException;
import java.util.ArrayList;
import java.util.List;

import static org.jahia.se.modules.dam.cloudinary.Constants.CONTENT_TYPE_IMAGE;
import static org.jahia.se.modules.dam.cloudinary.Constants.CONTENT_TYPE_MODEL3D;

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

    /**
     * Turns a 3D model into a square picture, ahead of the thumbnail resize.
     *
     * A 3D model only renders as a picture through the camera effect; without it Cloudinary still
     * answers with an image, but an unlit, near-black one. The camera renders into a fixed 640x640
     * frame the model rarely fills, hence e_trim, which drops the transparent margin whatever the
     * model's shape, where a fixed zoom would crop one shaped differently. Trimming leaves the
     * picture as tall or as wide as the model, so it is padded back to a square: a viewer that
     * crops a thumbnail to a square, the Content Editor's reference card for one, would otherwise
     * cut into the model. The model is scaled below the canvas to keep a margin around it.
     */
    private static final String MODEL_3D_RENDER =
            "/e_camera/e_trim/c_limit,w_340,h_340/c_lpad,w_400,h_400,b_transparent";

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
     * Only applies transformations to images (cloudynt:image).
     * For other content types (videos, raw files), returns the direct URL.
     *
     * Priority order for images:
     * 1. Derived transformations (from picker selection with crops/edits)
     * 2. Dynamic parameters (from template render calls)
     * 3. No transformations (original asset)
     *
     * @param params Optional list of transformation parameters
     * @return Full Cloudinary URL with or without transformations
     */
    private String buildCloudinaryUrl(List<String> params) {
        try {
            // For non-image content types, return the direct URL
            if (!this.isNodeType(CONTENT_TYPE_IMAGE)) {
                if (node.hasProperty("cloudy:url")) {
                    return node.getProperty("cloudy:url").getString();
                }
                return super.getUrl();
            }

            // For images, build URL with transformations
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
     * Supported parameters (long and short formats):
     * - width:/w: Target width
     * - height:/h: Target height
     * - crop:/c: Crop mode (scale, fit, fill, etc.)
     * - gravity:/g: Focal point (center, face, auto, etc.)
     *
     * @param params List of "key:value" parameter strings
     * @return List of Cloudinary transformation strings
     */
    private List<String> buildTransformationsFromParams(List<String> params) {
        List<String> transformations = new ArrayList<>();
        transformations.add("f_auto"); // Always add auto format for optimization

        for (String param : params) {
            if (param.startsWith("width:") || param.startsWith("w:")) {
                String width = param.startsWith("width:")
                    ? StringUtils.substringAfter(param, "width:")
                    : StringUtils.substringAfter(param, "w:");
                if (width.trim().isEmpty()) {
                    width = URL_SIZE;
                }
                transformations.add("w_" + width);
            } else if (param.startsWith("height:") || param.startsWith("h:")) {
                String height = param.startsWith("height:")
                    ? StringUtils.substringAfter(param, "height:")
                    : StringUtils.substringAfter(param, "h:");
                if (!height.trim().isEmpty()) {
                    transformations.add("h_" + height);
                }
            } else if (param.startsWith("crop:") || param.startsWith("c:")) {
                String crop = param.startsWith("crop:")
                    ? StringUtils.substringAfter(param, "crop:")
                    : StringUtils.substringAfter(param, "c:");
                if (!crop.trim().isEmpty()) {
                    transformations.add("c_" + crop);
                }
            } else if (param.startsWith("gravity:") || param.startsWith("g:")) {
                String gravity = param.startsWith("gravity:")
                    ? StringUtils.substringAfter(param, "gravity:")
                    : StringUtils.substringAfter(param, "g:");
                if (!gravity.trim().isEmpty()) {
                    transformations.add("g_" + gravity);
                }
            }
        }

        return transformations;
    }

    /**
     * Checks if a child node exists with the given name.
     *
     * This override was added to fix GraphQL queries using `thumbnailUrl(name: "thumbnail2", checkIfExists:true)`,
     * which was introduced in jContent 3.4.1 for displaying thumbnails in the UI.
     *
     * Without this method, GraphQL queries with checkIfExists would fail because the thumbnail nodes
     * don't actually exist as physical child nodes - they are virtual URLs generated on demand.
     *
     * @param s The node name to check ("thumbnail", "thumbnail2", or other)
     * @return true if the name is "thumbnail" or "thumbnail2", otherwise delegates to parent implementation
     * @throws RepositoryException If checking for node existence fails
     * @since 4.2.0
     */
    @Override
    public boolean hasNode(String s) throws RepositoryException {
        if ("thumbnail".equals(s) || "thumbnail2".equals(s)) {
            return true;
        }
        return super.hasNode(s);
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

            if (this.isNodeType(CONTENT_TYPE_MODEL3D)) {
                sb.append(MODEL_3D_RENDER);
            }

            sb.append("/f_auto");

            // Use c_limit with both w and h set to the same value
            // This ensures the largest dimension is resized to the specified size
            // while maintaining aspect ratio
            sb.append(",c_limit,w_").append(size).append(",h_").append(size).append("/");

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

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
        try {
            return node.getProperty("cloudy:url").getString();
        } catch (RepositoryException e) {
            return super.getUrl();
        }
    }

    @Override
    public String getUrl(List<String> params) {
        try {
            if (this.isNodeType(CONTENT_TYPE_IMAGE)) {
                List<String> cloudyParams = new ArrayList<>();
                cloudyParams.add("f_auto");
                for (String param : params) {
                    if (param.startsWith("width:")) {
                        String width = StringUtils.substringAfter(param, "width:");
                        if (width.trim().isEmpty()) {
                            width = URL_WIDTH; //default width
                        }
                        cloudyParams.add("w_" + width);
                    } else if (param.startsWith("height:")) {
                        String height = StringUtils.substringAfter(param, "height:");
                        if (!height.trim().isEmpty())
                            cloudyParams.add("h_" + height);
                    }
                }
                try {
                    StringBuilder sb = new StringBuilder();
                    sb.append(node.getProperty("cloudy:baseUrl").getString());
                    sb.append("/").append(StringUtils.join(cloudyParams, ",")).append("/");
                    if (node.hasProperty("cloudy:poster")) {
                        sb.append(node.getProperty("cloudy:poster").getString());
                    } else if (node.hasProperty("cloudy:endUrl")) {
                        sb.append(node.getProperty("cloudy:endUrl").getString());
                    }

                    return sb.toString();
                } catch (RepositoryException e) {
                    return super.getUrl();
                }
            } else {
                return this.getUrl();
            }
        }catch (RepositoryException e) {
            return this.getUrl();
        }
    }

    @Override
    public String getThumbnailUrl(String name) {
        String width = THUMBNAIL_WIDTH;
        try {
            StringBuilder sb = new StringBuilder();
            sb.append(node.getProperty("cloudy:baseUrl").getString());
            if("poster".equals(name))
                width= URL_WIDTH;

            sb.append("/f_auto,w_"+width+"/");

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

package org.jahia.se.modules.dam.cloudinary.edp;

import org.apache.commons.lang.StringUtils;
import org.jahia.services.content.JCRNodeWrapper;
import org.jahia.services.content.decorator.JCRNodeDecorator;

import javax.jcr.RepositoryException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

public class CloudinaryDecorator extends JCRNodeDecorator {
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

    public String getUrl(List<String> params) {
        List<String> cloudyParams = new ArrayList<>();
        for (String param : params) {
            if (param.startsWith("width:")) {
                cloudyParams.add("w_" + StringUtils.substringAfter(param, "width:"));
            } else if (param.startsWith("height:")) {
                cloudyParams.add("h_" + StringUtils.substringAfter(param, "width:"));
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
    }

    @Override
    public String getThumbnailUrl(String name) {
        try {
            StringBuilder sb = new StringBuilder();
            sb.append(node.getProperty("cloudy:baseUrl").getString());
            sb.append("/f_auto,w_200/");
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

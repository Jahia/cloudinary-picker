package org.jahia.se.modules.dam.cloudinary.service;

import org.jahia.se.modules.dam.cloudinary.edp.CloudinaryDecorator;
import org.jahia.services.content.decorator.JCRNodeDecoratorDefinition;
import org.osgi.service.component.annotations.Component;

import java.util.HashMap;
import java.util.Map;

import static org.jahia.se.modules.dam.cloudinary.Constants.*;

@Component(immediate = true, service = JCRNodeDecoratorDefinition.class)
public class CloudinaryDecoratorDefinition extends JCRNodeDecoratorDefinition {
    private Map<String, Class> decorators = new HashMap<>();

    public CloudinaryDecoratorDefinition() {
        decorators.put(CONTENT_TYPE_IMAGE, CloudinaryDecorator.class);
        decorators.put(CONTENT_TYPE_DOC, CloudinaryDecorator.class);
        decorators.put(CONTENT_TYPE_PDF, CloudinaryDecorator.class);
        decorators.put(CONTENT_TYPE_VIDEO, CloudinaryDecorator.class);
    }

    @Override
    public Map<String, Class> getDecorators() {
        return decorators;
    }
}

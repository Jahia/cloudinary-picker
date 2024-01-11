package org.jahia.se.modules.dam.cloudinary.service;

import org.jahia.se.modules.dam.cloudinary.edp.CloudinaryDecorator;
import org.jahia.services.content.decorator.JCRNodeDecoratorDefinition;
import org.osgi.service.component.annotations.Component;

import java.util.HashMap;
import java.util.Map;

@Component(immediate = true, service = JCRNodeDecoratorDefinition.class)
public class CloudinaryDecoratorDefinition extends JCRNodeDecoratorDefinition {
    private Map<String, Class> decorators = new HashMap<>();

    public CloudinaryDecoratorDefinition() {
        decorators.put("cloudynt:image", CloudinaryDecorator.class);
        decorators.put("cloudynt:document", CloudinaryDecorator.class);
        decorators.put("cloudynt:pdf", CloudinaryDecorator.class);
        decorators.put("cloudynt:video", CloudinaryDecorator.class);
    }

    @Override
    public Map<String, Class> getDecorators() {
        return decorators;
    }
}

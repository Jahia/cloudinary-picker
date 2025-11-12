package org.jahia.se.modules.dam.cloudinary.edp;

import org.osgi.service.metatype.annotations.AttributeDefinition;
import org.osgi.service.metatype.annotations.AttributeType;
import org.osgi.service.metatype.annotations.ObjectClassDefinition;

@ObjectClassDefinition(
        name = "Cloudinary Cache",
        description = "Cloudinary cache configuration to use Cloudinary in Jahia"
)
public @interface CloudinaryCacheConfig {
    @AttributeDefinition(
            name = "EDP Cache name",
            description = "Specifies the name of the ehcache used to store the Cloudinary assets metadata into Jahia.",
            defaultValue = "EDPCloudinary",
            type = AttributeType.STRING
    )
    String edpCacheName() default "EDPCloudinary";

    @AttributeDefinition(
            name = "EDP Cache TTL",
            description = "Specifies the time in seconds for the Cloudinary asset to live in the EDP cache.",
            defaultValue = "28800",
            type = AttributeType.INTEGER
    )
    int edpCacheTtl() default 28800;

    @AttributeDefinition(
            name = "EDP Cache TTI",
            description = "Specifies the time in seconds the Cloudinary asset is idle in the EDP cache.",
            defaultValue = "3600",
            type = AttributeType.INTEGER
    )
    int edpCacheTti() default 3600;
}

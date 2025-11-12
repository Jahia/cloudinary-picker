package org.jahia.se.modules.dam.cloudinary;

import org.apache.commons.lang3.StringUtils;
import org.osgi.service.metatype.annotations.AttributeDefinition;
import org.osgi.service.metatype.annotations.AttributeType;
import org.osgi.service.metatype.annotations.ObjectClassDefinition;

@ObjectClassDefinition(
        name = "Cloudinary Provider",
        description = "Cloudinary provider configuration to use Cloudinary in Jahia"
)
public @interface CloudinaryProviderConfig {

    @AttributeDefinition(
            name = "API Schema",
            description = "The http schema used to execute the request",
            defaultValue = "https",
            type = AttributeType.STRING
    )
    String apiSchema() default "https";

    @AttributeDefinition(
            name = "API EndPoint",
            description = "The API endpoint",
            defaultValue = "api.cloudinary.com",
            type = AttributeType.STRING
    )
    String apiEndPoint() default "api.cloudinary.com";

    @AttributeDefinition(
            name = "API Version",
            description = "The Cloudinary API version",
            defaultValue = "v1_1",
            type = AttributeType.STRING
    )
    String apiVersion() default "v1_1";

    @AttributeDefinition(
            name = "API Key",
            description = "The API key",
            type = AttributeType.STRING
    )
    String apiKey() default StringUtils.EMPTY;

    @AttributeDefinition(
            name = "API Secret",
            description = "The Cloudinary API secret",
            type = AttributeType.PASSWORD
    )
    String apiSecret() default StringUtils.EMPTY;

    @AttributeDefinition(
            name = "Cloud Name",
            description = "The name of the cloudinary cloud you want to connect to",
            type = AttributeType.STRING
    )
    String cloudName() default StringUtils.EMPTY;

    @AttributeDefinition(
            name="Front Key URL Pattern",
            description = "Specifies the string used to identify Cloudinary URLs within a rich text editor. This string is used to trigger the appropriate picker modal when a user edits an image in the rich text editor.",
            defaultValue = "cloudinary",
            type = AttributeType.STRING
    )
    String keyUrlPattern() default "cloudinary";

    @AttributeDefinition(
            name = "Front Apply On Pickers",
            description = "Specifies the picker types that allows the Cloudinary picker.",
            defaultValue = "image,file,video",
            type = AttributeType.STRING
    )
    String applyOnPickers() default "image,file,video";

    @AttributeDefinition(
            name = "EDP Mount Path",
            description = "Specifies the Mount Path used to reference cloudinary assets picked by a user",
            defaultValue = "/sites/systemsite/contents/dam-cloudinary",
            type = AttributeType.STRING
    )
    String edpMountPath() default "/sites/systemsite/contents/dam-cloudinary";

    /**
     * HTTP connection timeout in milliseconds.
     * Time to establish a connection with the server.
     *
     * @return Connection timeout (default: 10000ms = 10 seconds)
     */
    @AttributeDefinition(
            name = "Connection Timeout (ms)",
            description = "HTTP connection timeout in milliseconds - time to establish a connection with the server",
            defaultValue = "10000",
            type = AttributeType.INTEGER
    )
    int connectionTimeout() default 10000;

    /**
     * HTTP socket timeout in milliseconds.
     * Time to wait for data after connection is established.
     *
     * @return Socket timeout (default: 30000ms = 30 seconds)
     */
    @AttributeDefinition(
            name = "Socket Timeout (ms)",
            description = "HTTP socket timeout in milliseconds - time to wait for data after connection is established",
            defaultValue = "30000",
            type = AttributeType.INTEGER
    )
    int socketTimeout() default 30000;

    /**
     * HTTP connection request timeout in milliseconds.
     * Time to wait to get a connection from the connection pool.
     *
     * @return Connection request timeout (default: 10000ms = 10 seconds)
     */
    @AttributeDefinition(
            name = "Connection Request Timeout (ms)",
            description = "HTTP connection request timeout in milliseconds - time to wait to get a connection from the connection pool",
            defaultValue = "10000",
            type = AttributeType.INTEGER
    )
    int connectionRequestTimeout() default 10000;
}

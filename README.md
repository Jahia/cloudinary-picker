# cloudinary-picker

This module contains the implementation of the Cloudinary Content Picker for Jahia 8.2.x.x

With this module, a contributor can easily add a Cloudinary media asset to a Jahia page with advanced image transformation capabilities (crop, resize, aspect ratio).

![](./doc/images/main.png)

- [Module content](#module-content)
- [Quick Start](#quick-start)
    - [Deploy the module](#deploy-the-module)
      - [From the source](#from-the-source)
      - [From the store](#from-the-store)
    - [Post Install Configuration](#post-install-configuration)
- [Module details](#module-details)
    - [Architecture Overview](#architecture-overview)
    - [Data flow](#data-flow)
    - [Image Transformations](#image-transformations)
    - [URL Encoding](#url-encoding)
    - [Caching Strategy](#caching-strategy)

## Module content

This module contains:
* The definition of a `Cloudinary Asset Content Reference` content ([definition.cnd][definition.cnd]).
* A React application : `Cloudinary Content Picker` ([CloudinaryPicker.jsx][react:index.js]).
  This application is a custom jContent SelectorType (aka picker) and is used to pick a Cloudinary asset with crop and resize capabilities.
* A proxy servlet filter used to call the Cloudinary Admin API from the frontend
    `Cloudinary Proxy Servlet` ([CloudinaryProxyServlet.java]).
* A *light* External Data Provider (EDP) implementation named
    `Cloudinary Asset Provider` ([CloudinaryDataSource.java]) with URL transformation support.
* An OSGI-based configuration service ([CloudinaryProviderService]) for managing Cloudinary credentials and settings.
* A decorator ([CloudinaryDecorator.java]) that handles dynamic URL generation with transformations.
* A caching layer ([CloudinaryCacheManager]) for optimizing asset retrieval and transformed URLs.

Not covered in this module:
* CKEditor Cloudinary media picker

## Quick Start

### Deploy the module
The module can be installed in 2 ways, from the source or from the store.

#### From the source
1. Download the zip archive of the latest release.
2. If you already know your Cloudinary configuration (cloud name and API key) you can update the default
   configuration. Update properties in the [org.jahia.se.modules.dam.cloudinary.provider.config.cfg][mount.cfg] file.
3. Go to the root of the repository.
4. Run the `mvn clean install` command. This creates a jar file in the *target* repository.
   > you must have a **java sdk** and **maven** installed
5. In jContent, go to `Administration` panel.
6. In the `Server` section, expand `Modules and Extensions` and click `Modules`.
7. From the right panel, click `SELECT MODULE` and select the jar file in the *target* repository.
8. Finally, click `UPLOAD`.

#### From the store
1. In jContent, navigate to `Administration`.
2. In the `Server` section, expand the `Modules and Extensions` entry and click `Modules`.
3. From the right panel, click `Available modules` and search for **cloudinary**.
4. Click the install icon ![201] in the right of the package to download and install the module.
5. Wait until the module is loading.

#### Check install
If the module is properly deployed you should find it in the `Installed modules` section.

If you have installed the module from the store or if you didn't configure the properties
in the [org.jahia.se.modules.dam.cloudinary.provider.config.cfg][mount.cfg] file before building the module,
you must complete the post-install configuration to start the Cloudinary provider.

>Don't forget to enable the module for one of your projects

### Post Install Configuration
>Skip this section if you have already configured the configuration file during the *install from the source* process.
> This configuration doesn't require a server restart.

To request the Cloudinary server, you must configure the module with your Cloudinary API access information.

#### Provider Configuration

To set up your Cloudinary API access:
1. Go to jahia tools (*https://\<jahia host\>/tools*).
2. From the tools UI, click `OSGI console` under **Administration and Guidance**.

   ![][0070]

3. In the top menu expand the entry **OSGI** and click **Configuration**.

   ![][0072]

4. Look for `Cloudinary Provider` or filter by PID `org.jahia.se.modules.dam.cloudinary.provider.config` and click on it.

5. Update the **required** properties:
   - **Cloud Name**: Your Cloudinary cloud name (e.g., `demo-account`)
   - **API Key**: Your Cloudinary API key (e.g., `123456789012345`)
   - **API Secret**: Your Cloudinary API secret - stored securely as password

   You can find these credentials in your [Cloudinary Console Dashboard](https://cloudinary.com/console).

6. Optionally, adjust these settings if needed:
   
   **API Configuration:**
   - **API Schema**: The HTTP schema for API calls (default: `https`)
   - **API EndPoint**: The Cloudinary API endpoint (default: `api.cloudinary.com`)
   - **API Version**: The Cloudinary API version (default: `v1_1`)
   
   **Frontend Settings:**
   - **Front Apply On Pickers**: Picker types where Cloudinary is available (default: `image,file,video`)
   
   **Backend Settings:**
   - **EDP Mount Path**: JCR path for Cloudinary assets (default: `/sites/systemsite/contents/dam-cloudinary`)
   - **Connection Timeout (ms)**: Time to establish connection (default: 10000)
   - **Socket Timeout (ms)**: Time to wait for data (default: 30000)
   - **Connection Request Timeout (ms)**: Time to get connection from pool (default: 10000)

   ![][0071]

7. Click **Save**. 
   
   **Important:** All three required properties (Cloud Name, API Key, API Secret) must be set for the provider to start.
   
   The system will automatically:
   - Validate the configuration
   - Start the Cloudinary provider if configuration is complete
   - Create the EDP mount point at the specified path

#### Cache Configuration (Optional)

You can also configure the cache behavior for Cloudinary assets:

1. In the OSGI Configuration page, look for `Cloudinary Cache` or filter by PID `org.jahia.se.modules.dam.cloudinary.cache.config` and click on it.

2. Adjust the cache settings if needed:
   - **EDP Cache name**: Name of the Ehcache instance (default: `cacheCloudinary`)
   - **EDP Cache TTL**: Time To Live in seconds - how long entries stay in cache (default: 28800s = 8h)
   - **EDP Cache TTI**: Time To Idle in seconds - entries are removed if not accessed (default: 3600s = 1h)

3. Click **Save**.

> **Note:** The cache configuration affects performance:
> - Higher TTL means fewer API calls to Cloudinary but potentially stale data
> - Lower TTL means more up-to-date data but more API calls
> - The TTI helps remove unused entries to save memory

#### Verify Installation

8. Verify the provider is running by checking that `cloudinary` appears in the list of External Data Providers in jContent.

   ![][031]

9. You're all set! You can now:
   - Add Cloudinary assets in CKEditor
   - Create Cloudinary Asset content references
   - Use crop and resize capabilities for images

> **Troubleshooting:** If the provider doesn't start, check the Jahia logs for configuration errors. Common issues include:
> - Missing required properties (cloudName, apiKey, apiSecret)
> - Invalid credentials
> - Network connectivity issues with Cloudinary API

## Module details

### Architecture Overview

The module is built with a modern OSGI architecture:

1. **CloudinaryProviderConfig**: OSGI configuration interface with `@AttributeDefinition` annotations
2. **CloudinaryProviderService**: Service interface exposing the configuration
3. **CloudinaryProviderServiceImpl**: Service implementation managing lifecycle and mount point
4. **CloudinaryMountPointService**: Manages the EDP mount/unmount operations
5. **CloudinaryDataSource**: EDP implementation handling asset queries and URL transformations
6. **CloudinaryDecorator**: JCR node decorator providing dynamic URL generation
7. **CloudinaryCacheManager**: Caching layer using Ehcache for performance optimization

### Data flow

![][010]

The data flow includes the following steps:

1. **Content Creation**: From a website page, the user creates or updates a `Cloudinary Content Reference`.
   The React application `Cloudinary Content Picker` is launched, displaying a placeholder.

   ![][002]

2. **Asset Selection**: When the user clicks the **Cloudinary Content** field, the Cloudinary Media Library web application launches in a modal.
   Users can:
   - Browse and search Cloudinary assets
   - Crop images using the built-in crop tool
   - Resize images with aspect ratio support
   - Preview transformations in real-time

   > Learn more about the [Media Library][cloudinary:MediaLib].

3. **Asset Information Retrieval**: When a user selects an asset, the picker:
   - Receives the asset's `public_id` from the Media Library
   - Uses the Cloudinary Admin API (via proxy) to get the permanent `asset_id`
   - Encodes transformation parameters (crop, resize) in base36 format for JCR compatibility

   > The proxy uses the Cloudinary [Admin API][cloudinary:AdminAPI].

4. **Node Creation**: The picker creates a content path using:
   - Static base: `/sites/systemsite/contents/dam-cloudinary`
   - Asset ID and encoded transformation parameters
   
   A GraphQL call creates the JCR node via the `Cloudinary Asset Provider`.

5. **Asset Metadata Retrieval**: If the asset is not cached, the provider:
   - Calls the Cloudinary Admin API
   - Retrieves asset metadata (dimensions, format, resource type, etc.)
   - Stores transformation parameters in node properties

   > Uses the Cloudinary [Admin API][cloudinary:AdminAPI].

6. **Caching**: The JSON response is mapped to a Jahia node and cached in `cacheCloudinary`.
   - Default TTL: 8 hours
   - Idle timeout: 1 hour
   - Transformed URLs are cached separately for performance

7. **Content Rendering**: The provider returns a reference node that can be saved and used in pages.
   The module provides jContent views for different Cloudinary asset types (images, videos, raw files).

8. **URL Generation**: When rendering content:
   - The decorator generates Cloudinary transformation URLs
   - Supports crop (c_crop), resize (c_fill, c_fit), quality, and format parameters
   - Uses Cloudinary CDN for optimal performance and analytics

The complete flow:

![][003]

### Image Transformations

The module supports advanced image transformations through Cloudinary's URL-based API:

#### Crop Transformations
When a user crops an image in the picker, the following parameters are encoded:
- `x`, `y`: Crop starting coordinates
- `cw`, `ch`: Crop width and height
- `ar`: Aspect ratio (e.g., "16_9", "4_3", "1_1")

Example: `/c_crop,x_100,y_50,w_800,h_600,ar_16:9/`

#### Resize Transformations
Resize operations support various modes:
- `c_fill`: Fill specified dimensions, may crop
- `c_fit`: Fit within dimensions, preserves aspect ratio
- `c_scale`: Scale to exact dimensions
- `c_limit`: Limit maximum dimensions

Parameters:
- `w`: Width
- `h`: Height
- `c`: Crop/resize mode
- `g`: Gravity for positioning (auto, center, north, etc.)

Example: `/c_fill,w_800,h_600,g_auto/`

#### Parameter Shortcuts
The module supports both long and short parameter names:
- `width:` or `w:`
- `height:` or `h:`
- `crop:` or `c:`
- `gravity:` or `g:`

### URL Encoding

Transformation parameters are encoded in base36 format to ensure JCR path compatibility:

**Why base36?**
- JCR restricts certain characters in node names (`:` is treated as namespace prefix)
- Base36 uses only alphanumeric characters (0-9, a-z)
- Compact representation suitable for URLs

**Encoding format:**
```
{width}w{height}h{crop}c{gravity}g{aspectRatio}ar
```

**Example:**
```
Original: w=800, h=600, c=fill, ar=16:9
Encoded:  m0w6ighcfillgar16_9
```

The aspect ratio separator `:` is replaced with `_` for JCR compatibility.

### Caching Strategy

The module implements a two-level caching strategy managed by `CloudinaryCacheManager`:

#### Configuration

The cache behavior is configurable via OSGI configuration (`org.jahia.se.modules.dam.cloudinary.cache.config`):

- **Cache Name**: Name of the Ehcache instance (default: `cacheCloudinary`)
- **TTL (Time To Live)**: How long entries remain in cache (default: 8 hours / 28800 seconds)
- **TTI (Time To Idle)**: Entries are removed if not accessed within this time (default: 1 hour / 3600 seconds)

#### Cache Levels

1. **Asset Metadata Cache**:
   - Caches full asset information from Cloudinary API
   - Key: asset_id
   - Reduces API calls to Cloudinary
   - Configurable TTL and TTI

2. **Transformed URL Cache**:
   - Caches generated Cloudinary URLs with transformations
   - Key: asset_id + transformation parameters hash
   - Prevents redundant URL generation
   - Improves rendering performance

#### Cache Invalidation

The cache automatically:
- Removes entries after TTL expires (default: 8 hours)
- Removes idle entries after TTI expires (default: 1 hour)
- Can be manually flushed via the cache manager
- Is recreated when configuration changes

Both caches use Ehcache with LFU (Least Frequently Used) eviction policy for optimal memory management.

[031]: ./doc/images/031_install_completed.png

[010]: ./doc/images/CloudyArchi.gif
[002]: ./doc/images/CloudyContentRef.png
[003]: ./doc/images/CloudyDemo.gif

[0070]: ./doc/images/0070_OSGIConfig.png
[0071]: ./doc/images/0071_OSGIConfig.png
[0072]: ./doc/images/0072_OSGIConfig.png
[201]: ./doc/images/201_modules_download_icon.png

[mount.cfg]: ./src/main/resources/META-INF/configurations/org.jahia.se.modules.dam.cloudinary.provider.config.cfg
[definition.cnd]: ./src/main/resources/META-INF/definitions.cnd
[react:index.js]: ./src/javascript/CloudinaryPicker/CloudinaryPicker.jsx
[CloudinaryDataSource.java]: ./src/main/java/org/jahia/se/modules/dam/cloudinary/edp/CloudinaryDataSource.java
[CloudinaryDecorator.java]: ./src/main/java/org/jahia/se/modules/dam/cloudinary/edp/CloudinaryDecorator.java
[CloudinaryProxyServlet.java]: ./src/main/java/org/jahia/se/modules/dam/cloudinary/CloudinaryProxyServlet.java
[CloudinaryProviderService]: ./src/main/java/org/jahia/se/modules/dam/cloudinary/service/CloudinaryProviderService.java
[CloudinaryCacheManager]: ./src/main/java/org/jahia/se/modules/dam/cloudinary/edp/CloudinaryCacheManager.java
[cloudinary:AdminAPI]: https://cloudinary.com/documentation/admin_api
[cloudinary:MediaLib]: https://cloudinary.com/documentation/media_library_widget

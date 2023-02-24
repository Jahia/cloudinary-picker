# cloudinary-picker

This module contains the implementation of the Cloudinary Content Picker for Jahia 8.1.x.x

With this module, a contributor can easily add a Cloudinary media asset to a Jahia page.

![](./doc/images/main.png)


- [Module content](#module-content)
- [Quick Start](#quick-start)
    - [Deploy the module](#deploy-the-module)
      - [From the source](#from-the-source)
      - [From the store](#from-the-store)
    - [Post Install (optional)](#post-install-optional)
- [Module details](#module-details)
    - [Data flow](#data-flow)

## Module content

This module contains:
* The definition of a `Cloudinary Asset Content Reference` content ([definition.cnd][definition.cnd]).
* A React application : `Cloudinary Content Picker` ([CloudinaryPicker.jsx][react:index.js]).
  This application is a custom jContent SelectorType (aka picker) and is used to pick a Cloudinary asset.
* A proxy servlet filter used to call the Cloudinary Admin API from the frontend
    `Cloudinary Proxy Servlet` ([CloudinaryProxyServlet.java]).
* A *light* implementation of an External Data Provider (EDP) named
    `Cloudinary Asset Provider` ([CloudinaryDataSource.java]).

Not covered in this module:
* CKEditor Cloudinary media picker

## Quick Start

### Deploy the module
The module can be installed in 2 ways, from the source or from the store.
#### From the source
1. Download the zip archive of the latest release.
2. If you already know your Cloudinary configuration (API key, site, and host) you can update the default
   configuration about Cloudinary. Update properties in the [cloudinary_picker_credentials.cfg][mount.cfg] file.
1. Go to the root of the repository.
1. Run the `mvn clean install` command. This creates a jar file in the *target* repository.
   > you must have a **java sdk** and **maven** installed
1. In jContent, go to `Administration` panel.
1. In the `Server` section, expand `Modules and Extensions` and click `Modules`.
1. From the right panel, click `SELECT MODULE` and select the jar file in the *target* repository.
1. Finaly click `UPLOAD`.

#### From the store
1. In jContent, navigate to `Administration`.
2. In the `Server` section, expand the `Modules and Extensions` entry and click `Modules`.
3. From the right panel, click `Available modules` and search for **cloudinary**.

4. Click the install icon ![201] in the right of the package to download and install the module.
5. Wait until the module is loading.

#### Check install
If the module is properly deployed you should find it in the `Installed modules` section.

If you have installed the module from the store or if you didn't configure the properties
in the [cloudinary_picker_credentials.cfg][mount.cfg] file before to build the module, you must do the post install
to have the Cloudinary provider starting.

>Don't forget to enable the module for one of your project

### Post Install (optional)
>Skip this section if you have already configured the [cloudinary_picker_credentials.cfg][mount.cfg] file during the *install from the source*
process.
> This configuration doesn't require a server restart.

To request the Cloudinary server, you have to configure the module with your Cloudinary API access information.

To set up your Cloudinary API access:
1. Go to  jahia tools (*https://\<jahia host\>/tools*).
2. From the tools UI, click `OSGI console` under **Administration and Guidance**.

   ![][0070]

3. In the top menu expand the entry **OSGI** and click **Configuration**.

   ![][0072]

4. Look for `org.jahia.se.modules.cloudinary_picker_credentials` and click on it.

5. Finally, update the appropriate properties and save your changes.

   ![][0071]

6. If all the properties are set correctly, the provider should start,
   and you should see the `cloudinary` key in the list of External providers.

   ![][031]

7. Now, you should be able to create a new `Cloudinary Content Reference` content.
More details at the end of this doc.

## Module details

To pick a cloudinary asset (for example a video, image, or PDF) from a Cloudinary Cloud instance, you need to implement:
1. A React application named `Cloudinary Content Picker`, and used as a `selectorType` in jContent.
   This picker is a user interface (UI) from which a jContent user can query a Cloudinary server to find and
   select the media asset they want to use on the website.
2. A proxy servlet filter used to call the Cloudinary Admin API from the frontend :
     `Cloudinary Proxy Servlet`.
3. A *light* External Data Provider (EDP), named `Cloudinary Asset Provider`,
      that maps the JSON returned by the cloudinary Admin API and represents the Cloudinary asset into a Jahia node.

### Data flow

![][010]

The data flow is composed of 8 actions of which 2 are optional and depend on the cache.

1. From a website page, the user creates or updates a `content reference` to a Cloudinary asset content (aka `Cloudinary Content Reference`).
   Then, jContent displays a user form with a **Cloudinary Content** field. The React application `Cloudinary Content Picker`
   used as a `selectorType` is launched and displays a placeholder.

   >A selectorType has 2 main component :
   one used to present the selected content (*or a placeholder if no content is selected*),
   and one used to pick a content. In this module this picker is the cloudinary Media library app

   ![][002]

2. When user clicks the **Cloudinary Content** field, the Media library web application provided by Cloudinary is launched into a modal.
   This media library interact directly with the cloudinary backend.

   > To know more about the [Media Library][cloudinary:MediaLib].

3. When user insert a new asset, the media library returns a JSON object containing the public_id of the asset. This id can evolve.
   Thus, the content picker uses it immediately to search the asset with the Cloudinary Admin API through a proxy and get the fixed asset_id

   > The proxy uses the Cloudinary [Search API][cloudinary:SearchAPI].

4. When the user saves their choice from the picker, a content path is created. This path is built with
   a static part `/sites/systemsite/contents/dam-cloudinary`, and the `asset_id` of the Cloudinary asset.

   jContent cannot use this path directly as it expects to receive a jahia node id.
   Therefore, the content picker executes a GraphQL call to create the node and get its id back.
   During this call, the path is resolved and mapped to a Jahia node
   with the help of the `Cloudinary Asset Provider`.

5. If the selected asset is not in the jContent cache,
   the provider calls the Cloudinary Admin API endpoint to get the relevant properties
   about the selected asset.

   > The provider uses the Cloudinary [Search API][cloudinary:SearchAPI].

6. The JSON response returned by the API is mapped to a Jahia node and cached into an Ehcache instance named `cacheCloudinary`.
   By default, the cache is configured to keep the content for a maximum of 8 hours and to drop the content if it is idle for more than 1 hour.

7. If the path provided in step 4 is correct, the provider return a Jahia reference node, and the user can save their `Cloudinary Content Reference`
   content.
   The content can be used by a jContent Page. This module provides jContent views for different type of cloudinary assets (for example images and videos).

8. The jContent views use the cloudinary content URL to get and render the content in a webpage.
   The Cloudinary CDN improves performance when assets load and allows Cloudinary to collect statistics.
   The Cloudinary asset is rendered into the website.

    > you can select the view of the content in reference.
    You can also select the width and the height, if your asset is an image.

The complete flow is the following :

![][003]

[031]: ./doc/images/031_install_completed.png

[010]: ./doc/images/CloudyArchi.gif
[002]: ./doc/images/CloudyContentRef.png
[003]: ./doc/images/CloudyDemo.gif

[0070]: ./doc/images/0070_OSGIConfig.png
[0071]: ./doc/images/0071_OSGIConfig.png
[0072]: ./doc/images/0072_OSGIConfig.png
[201]: ./doc/images/201_modules_download_icon.png

[mount.cfg]: ./src/main/resources/META-INF/configurations/org.jahia.se.modules.cloudinary_picker_credentials.cfg
[definition.cnd]: ./src/main/resources/META-INF/definitions.cnd
[react:index.js]: ./src/javascript/CloudinaryPicker/CloudinaryPicker.jsx
[CloudinaryDataSource.java]: ./src/main/java/org/jahia/se/modules/dam/cloudinary/edp/CloudinaryDataSource.java
[CloudinaryProxyServlet.java]: ./src/main/java/org/jahia/se/modules/dam/cloudinary/CloudinaryProxyServlet.java
[cloudinary:SearchAPI]: https://cloudinary.com/documentation/search_api
[cloudinary:MediaLib]: https://cloudinary.com/documentation/media_library_widget




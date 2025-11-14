---
page:
  $path: /sites/academy/home/documentation/jahia/8_2/end-user/optional-features/using-cloudinary-assets
  'jcr:title': Using Cloudinary Assets in Jahia
  'j:templateName': documentation
content:
  $subpath: document-area/content
---

This guide explains how to select and display media assets from your Cloudinary Digital Asset Management (DAM) system within Jahia content.

**What you'll learn:**
- How to select Cloudinary assets in your content
- How to display Cloudinary assets on your website
- How to work with images and videos
- How to apply transformations

**Prerequisites:**
- The Cloudinary Content Picker module is installed and configured (see [Administrator Guide][admin-guide])
- Basic knowledge of jContent

:::warning
**Version Requirements:**  
Before using Cloudinary assets, verify you have compatible versions:
- **Jahia:** 8.2.0.0+
- **jContent:** 3.5.0+ (for thumbnail display - upcoming version)
- **CKEditor:** CKEditor 4 only (CKE5 support coming in module v1.1.0)
- **JavaScript Modules Library:** 1.1.0+ (for React/JSX rendering - upcoming version)
- **Cloudinary Module:** 4.0.0+ (current version supports crop & resize for images in weak reference fields)

See [Known Limitations][admin-guide] in the Administrator Guide for details.
:::
---

## Selecting Cloudinary Assets

### Using the Cloudinary Picker in Content Forms

When creating or editing content that supports media references, you can select Cloudinary assets directly from your content form.

**Note:** The Cloudinary picker is only available for picker types configured in the `applyOnPickers` setting (default: `image,file,video`). If you don't see the Cloudinary option in a picker, verify that the picker type is enabled in the module configuration. See the [Administrator Guide][admin-guide] for configuration details.

#### Adding a Hero Image

Let's add a hero image to a banner component:

1. **Open your content** in jContent
   - Create or edit a "Hero Banner" component

![C001]

2. **Click the image field** (e.g., "Image")
   
3. **Open the Cloudinary picker**
   - A modal appears showing Jahia assets
   - **Select "Cloudinary"** from the Source provider dropdown to access your Cloudinary Media Library

![C002]

4. **Authenticate if needed**
    - Enter your Cloudinary credentials when prompted

![C003]

5. **Select your asset**
   - Browse through folders in your Cloudinary Media Library
   - Search for assets by name or tags
   - Filter by type (images, videos)
   - Click an image to select it

![C004]

6. **[Optional] Transform the image**
   - **Crop:** Use the crop tool to focus on a specific area
   - **Resize:** Adjust dimensions
   - Preview updates in real-time
   - Transformations don't modify the original in Cloudinary

![C005]

7. **Insert and save**
   - Click **"Insert"** to add the image
   - Click **"Save"** to save your content
   ![C006]
   The image is now referenced in your content. The file remains in Cloudinary and is not duplicated in Jahia.

### Using Cloudinary Assets in CKEditor

You can also insert Cloudinary images directly into rich text fields:

1. **Click inside a rich text field** (e.g., article body)
2. **Click the "Image" icon** in the CKEditor toolbar
3. **Select "Cloudinary"** as the source (if you have multiple DAM providers)
4. **Browse and select your image** using the Cloudinary Media Library
5. **Configure display settings** (alt text, alignment, size)
6. **Insert the image**

The image appears inline in your text editor.

![C007]

### Adding Cloudinary Assets Directly to Pages

#### Adding Images with Default Jahia Content

You can add Cloudinary images directly to a page using Jahia's default media content:

1. **In Page Composer**, click **"Add content"** in the target area
2. Select **"Jahia - Media → Image (Internationalized)"**
3. In the image field, click to open the picker
4. Select **"Cloudinary"** from the Source provider dropdown
5. Choose your image and optionally apply transformations
6. Click **"Insert"** and **"Save"**

This approach is useful for adding standalone images to pages without creating custom content types.

#### Adding Videos with Default Jahia Content

You can add Cloudinary videos to pages in multiple ways:

**Option 1: File Reference in Pages**

1. **In Page Composer**, click **"Add content"** in the target area
2. Select **"Jahia - Media → File reference"** (Internationalized or Shared by all languages)
3. In the file field, click to open the picker
4. Select **"Cloudinary"** from the Source provider dropdown
5. Choose your video
6. Click **"Insert"** and **"Save"**

**Option 2: Video via Weak Reference in Custom Content**

For custom content types (e.g., Article with video field):

1. **Create or edit your content** (e.g., Article)
2. In the video field (weak reference), click to open the picker
3. Select **"Cloudinary"** as the source
4. Choose your video
5. Click **"Insert"** and **"Save"**

These methods work for any Cloudinary video format and automatically handle video rendering on your site.

---

## Displaying Cloudinary Assets

### Displaying Images in JSP Templates

#### Basic Image Display

```jsp
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<%@ taglib prefix="fn" uri="http://java.sun.com/jsp/jstl/functions" %>

<%-- Get the Cloudinary asset reference --%>
<c:set var="Image" value="${currentNode.properties['image']}"/>

<%-- Display the image --%>
<c:if test="${not empty Image.node}">
    <c:set var="image" value="${Image.node}"/>
    <img src="${image.url}" 
         alt="${fn:escapeXml(image.displayableName)}"
         class="hero-image"/>
</c:if>
```

#### Image with Custom Size

```jsp
<%-- Display at 800px width --%>
<img src="${image.getUrl(['w:800'])}" 
     alt="${fn:escapeXml(image.displayableName)}"/>

<%-- Display at 800x600 --%>
<img src="${image.getUrl(['w:800', 'h:600'])}" 
     alt="${fn:escapeXml(image.displayableName)}"/>

<%-- With crop mode and gravity --%>
<img src="${image.getUrl(['w:800', 'h:600', 'c:fill', 'g:face'])}" 
     alt="${fn:escapeXml(image.displayableName)}"/>
```

**Supported parameters:**
- `w:` or `width:` - Width in pixels
- `h:` or `height:` - Height in pixels
- `c:` or `crop:` - Crop mode (scale, fit, fill, crop, thumb, etc.)
- `g:` or `gravity:` - Focal point (center, face, auto, north, south, etc.)

### Displaying Images in React/JSX Templates

#### Basic Image Display

```jsx
import { buildNodeUrl, jahiaComponent } from "@jahia/javascript-modules-library";

jahiaComponent(
  {...},({image}) => {
    if (!image) return null;
    
    const imageUrl = buildNodeUrl(image);
    
    return (
        <img 
            src={imageUrl} 
            alt={image.getDisplayableName()} 
        />
    );
});
```

#### Image with Custom Size and Transformations

```jsx
import { buildNodeUrl, jahiaComponent } from "@jahia/javascript-modules-library";

jahiaComponent(
  {...},({image}) => {
    if (!image) return null;
    
    // Build URL with width parameter
    const imageUrl = buildNodeUrl(image, { args: { w: 800 } });

    return (
      <img
        src={imageUrl}
        alt={image.getDisplayableName()}
      />
    );
  });

// With width, height, crop mode, and gravity
const imageUrl = buildNodeUrl(image, { 
  args: { 
    w: 800, 
    h: 600, 
    c: 'fill', 
    g: 'face' 
  } 
});
```

**Parameter format:**
```jsx
buildNodeUrl(imageNode, { args: { w: 800 } })                    // Width only
buildNodeUrl(imageNode, { args: { h: 600 } })                    // Height only
buildNodeUrl(imageNode, { args: { w: 800, h: 600 } })            // Both
buildNodeUrl(imageNode, { args: { w: 800, h: 600, c: 'fill' } }) // With crop
buildNodeUrl(imageNode, { args: { w: 800, c: 'fill', g: 'face' } }) // With gravity
```

### Displaying Videos

#### Video in JSP

```jsp
<c:set var="video" value="${currentNode.properties['videoAsset'].node}"/>

<c:if test="${not empty video}">
    <video controls>
        <source src="${video.url}" type="video/mp4">
        Your browser does not support the video tag.
    </video>
</c:if>
```

#### Video in React

```jsx
import { buildNodeUrl, jahiaComponent } from "@jahia/javascript-modules-library";

jahiaComponent(
  {...},({videoNode}) => {
    if (!videoNode) return null;
    const videoUrl = buildNodeUrl(videoNode);
    
    return (
        <video controls>
            <source src={videoUrl} type="video/mp4" />
            Your browser does not support the video tag.
        </video>
    );
});
```

**Note:** Cloudinary automatically handles video format optimization and adaptive streaming when configured.

---

## Retrieving Asset URLs via GraphQL

You can also retrieve Cloudinary asset URLs programmatically using Jahia's GraphQL API. This is useful for headless CMS implementations or custom applications.

### GraphQL Query

```graphql
query GetCloudinaryAssetUrl(
    $workspace: Workspace!,
    $uuid: String!
) {
    jcr(workspace: $workspace) {
        nodeById(uuid: $uuid) {
            title:displayName
            name
            url
        }
    }
}
```

### Query Variables

```json
{
  "workspace": "EDIT",
  "uuid": "fffffff2-6800-40cf-9b88-562d41ada4b4"
}
```

### Example Response

```json
{
  "data": {
    "jcr": {
      "nodeById": {
        "title": "alpine-supercar-concept_d5buh5",
        "name": "b46e97d7820542cddb8942405ff9ece9_c.a.w.15o",
        "url": "https://res.cloudinary.com/demvmlgq7/image/upload/c_thumb,w_1500/v1740057988/alpine-supercar-concept_d5buh5.webp"
      }
    }
  }
}
```

**Note:** The returned URL includes any transformations that were applied during asset selection in the picker.

**Important limitation:** Unlike JSP (`getUrl(['w:800'])`) or React (`buildNodeUrl(image, { args: { w: 800 } })`), GraphQL does **not support** dynamic transformation parameters. The `url` field returns the asset URL with only the transformations that were applied during asset selection. To get different image sizes via GraphQL, you would need to:
- Select and save multiple versions with different transformations, or
- Apply transformations client-side after fetching the URL
- 

### Use Cases

- **Headless CMS:** Fetch asset URLs for React, Vue, or Angular applications
- **Custom integrations:** Build custom galleries or sliders
- **API consumers:** Provide asset URLs to third-party services
- **Mobile apps:** Deliver optimized images to native applications

---

## Understanding Cloudinary Transformations

Cloudinary provides powerful transformation capabilities. The module supports basic transformations through template parameters:

### Crop Modes (`c:` parameter)

- **`scale`** - Resize to exact dimensions, may distort aspect ratio
- **`fit`** - Resize to fit within dimensions, maintains aspect ratio
- **`fill`** - Resize and crop to exact dimensions, maintains aspect ratio
- **`crop`** - Crop to exact dimensions
- **`thumb`** - Generate thumbnail with face detection
- **`limit`** - Resize only if larger than specified dimensions

### Gravity (`g:` parameter)

Controls focal point for cropping and resizing:
- **`auto`** - Automatic focal point detection
- **`face`** - Focus on detected faces
- **`center`** - Center of the image
- **`north`, `south`, `east`, `west`** - Cardinal directions
- **`north_east`, `south_east`, etc.** - Corner positions

### Automatic Optimizations

Cloudinary automatically applies:
- **Format optimization** (`f_auto`) - Delivers best format (WebP, AVIF, etc.)
- **Quality optimization** (`q_auto`) - Balances quality and file size
- **DPR scaling** - Adapts to device pixel ratio

---

## Summary

**Key takeaways:**
- Cloudinary assets are **referenced, not duplicated** in Jahia
- Transformations are **non-destructive** and applied on-the-fly
- Use `image.url` for original, `image.getUrl(['w:800'])` for transformed
- In React: `buildNodeUrl(imageNode, { args: { w: 800 } })`
- Cloudinary's CDN handles global delivery and automatic optimization
- Basic transformations (width, height, crop, gravity) are supported via template parameters

For installation and configuration instructions, see the [Administrator Guide][admin-guide].

[C001]: ./C001.png
[C002]: ./C002.png
[C003]: ./C003.png
[C004]: ./C004.png
[C005]: ./C005.png
[C006]: ./C006.png
[C007]: ./C007.png

[admin-guide]: /cms/{mode}/{lang}/sites/academy/home/documentation/jahia/8_2/end-user/optional-features/cloudinary-administrator-guide.html

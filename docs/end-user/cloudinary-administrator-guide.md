---
page:
  $path: /sites/academy/home/documentation/jahia/8_2/end-user/optional-features/cloudinary-administrator-guide
  'jcr:title': Cloudinary Picker - Administrator Guide
  'j:templateName': documentation
content:
  $subpath: document-area/content
---
# Installing and Configuring the Cloudinary Picker Module

This guide explains how to install and configure the Cloudinary Content Picker module in Jahia.

**What you'll learn:**
- How to install the Cloudinary module
- How to configure the connection to Cloudinary
- How the integration works behind the scenes
- How to troubleshoot common issues

**Prerequisites:**
- Jahia 8.2.x or higher
- Active Cloudinary account with API access
- Administrator access to Jahia

---

## Overview

The Cloudinary Content Picker module allows you to browse, select, and display media assets from your Cloudinary Digital Asset Management (DAM) system directly within Jahia content. This integration enables you to reference Cloudinary images and videos without duplicating files, keeping your content lightweight and always up-to-date with your DAM.

---

## Installation

### Installing from the Jahia Store

1. Log in to jContent as an administrator
2. Navigate to **Administration → Server → Modules and Extensions → Modules**
3. Click the **Available modules** tab
4. Search for "**Cloudinary Content Picker**"
5. Click the **install** icon next to the module
6. Wait for the installation to complete (you'll see a confirmation message)

---

## Configuration

After installation, you need to configure the connection to your Cloudinary account.

### Step 1: Access the Configuration Interface

1. Open Jahia Tools in your browser: `https://your-jahia-domain.com/tools`
2. Navigate to **Administration and Guidance → OSGi Console**
3. Click **OSGi → Configuration** in the left menu

### Step 2: Configure Cloudinary Provider

1. Find "**Cloudinary Provider**" in the configuration list
   - You can use the search box to filter by typing "cloudinary"
   - The PID (identifier) is: `org.jahia.se.modules.dam.cloudinary.provider.config`

2. Enter your Cloudinary API credentials (available in your Cloudinary Console dashboard):
   ```
   cloudName = your-cloud-name
   apiKey = your-api-key
   apiSecret = your-api-secret
   ```

3. Verify the following settings match your Cloudinary instance (these are the default values, typically no changes needed):
   ```
   apiSchema = https
   apiEndPoint = api.cloudinary.com
   apiVersion = v1_1
   ```

4. Configure picker availability (optional):
   ```
   applyOnPickers = image,file,video
   ```
   
   This setting controls which Jahia picker types will show the Cloudinary option:
   - **`image`** - Cloudinary appears in image pickers
   - **`file`** - Cloudinary appears in file pickers
   - **`video`** - Cloudinary appears in video pickers
   
   **Example scenarios:**
   - `applyOnPickers = image,video` → Cloudinary available for images and videos only
   - `applyOnPickers = image` → Cloudinary available for images only
   - `applyOnPickers = file` → Cloudinary available for file pickers only
   
   If a Jahia property uses an "Image" picker and `applyOnPickers` contains `image`, the Cloudinary picker will be accessible. Otherwise, it won't appear as an option.

5. Review the mount point (this is where Cloudinary assets appear in Jahia's content tree):
   ```
   edpMountPath = /sites/systemsite/contents/dam-cloudinary
   ```
   
   ⚠️ **Important:** This path must be unique across all external providers in your Jahia installation.

6. Configure HTTP timeouts (optional, adjust if you experience slow API responses):
   ```
   connectionTimeout = 10000      # 10 seconds
   socketTimeout = 30000          # 30 seconds
   connectionRequestTimeout = 10000  # 10 seconds
   ```

7. Click **Save**

### Step 3: Verify the Configuration

The provider will automatically start if all required settings are correctly configured:
- `apiSchema`
- `apiEndPoint`
- `apiVersion`
- `cloudName`
- `apiKey`
- `apiSecret`
- `edpMountPath`

You can verify the provider started successfully in two ways:

**Option A: Check the Logs**

Look for this message in the Jahia logs (`digital-factory-data/logs/jahia.log`):
```
INFO  [CloudinaryDataSource] - Cloudinary mount point service started
```

**Option B: Check the Cache Management**

1. Navigate to **Administration → Server → System → Cache Management**
2. Search for "**EDPCloudinary**"
3. If you see this cache, the provider has started successfully

If the provider didn't start, check that all required fields are filled in, especially `cloudName`, `apiKey`, and `apiSecret`.

### Step 4: Enable for Your Site

1. Navigate to **Administration → Server → Modules and Extensions → Modules**
2. Search for "**cloudinary**" in the module list
3. Find the **Cloudinary Content Picker** module
4. Toggle the switch to **enable** it for your site (e.g., "digitall")
5. Click **Save**
6. **Reload your browser page** to apply the changes

The Cloudinary picker is now ready to use in your site's content.

---

## Understanding the Integration

### No Binary Duplication

When you select a Cloudinary asset in Jahia, **the actual file is not copied to Jahia**. Instead:

1. **Only a reference is stored** in Jahia content
   - Contains the asset ID and optional transformation parameters

2. **The file stays in Cloudinary**
   - Your Cloudinary DAM remains the single source of truth
   - Updates to the asset in Cloudinary are reflected in Jahia (subject to cache duration)

3. **URLs are generated on-demand**
   - When a page is rendered, Jahia generates the asset URL
   - The URL points directly to the Cloudinary CDN

**Benefits:**
- **Storage efficiency:** No duplicate files in Jahia
- **Up-to-date content:** Asset updates propagate to all sites (after cache refresh)
- **Centralized management:** All assets managed from your DAM
- **Performance:** Cloudinary's global CDN delivers assets with automatic optimization

### Caching Strategy

The module uses caching to optimize performance and reduce API calls to Cloudinary.

#### Asset Metadata Cache

**What is cached:**
- Asset properties (title, dimensions, format, resource type)
- Transformation parameters (crop, resize settings from picker)
- Base URL components

**Cache duration:**
- **Time to Live (TTL):** 8 hours (default)
- **Time to Idle (TTI):** 1 hour (default)

**How it works:**
1. First request → Fetches from Cloudinary API → Stores in cache
2. Subsequent requests → Served from cache
3. Cache expires after 8 hours or 1 hour of inactivity

**Note:** If an asset is updated in Cloudinary, the change will appear in Jahia after the cache expires (up to 8 hours by default).

#### Cache Management

**Viewing the cache:**
1. Navigate to **Administration → Server → System → Cache Management**
2. Search for "**EDPCloudinary**"
3. View statistics (size, hit rate, misses)

**Clearing the cache:**
- **Manual:** Restart Jahia
- **Automatic:** Cache entries expire after TTL/TTI

**Adjusting cache settings:**
1. Navigate to OSGi Configuration
2. Find Cloudinary Cache, PID: `org.jahia.se.modules.dam.cloudinary.cache.config`
3. Adjust values:
   ```
   edpCacheTtl = 28800    # 8 hours (in seconds)
   edpCacheTti = 3600     # 1 hour (in seconds)
   ```

---

## Known Limitations

### Compatibility Issues

**jContent Thumbnails**
- Thumbnails may not display correctly in jContent versions prior to **3.5.0**
- **Impact:** Assets can still be selected and displayed on pages; only the thumbnail preview is unavailable in the form card view after a Cloudinary asset is selected
- **Workaround:** No workaround available, update to 3.5.0 is required
- **Fixed in:** 3.5.0 (release pending)

**CKEditor 5 Support**
- The Cloudinary picker is **not compatible** with CKEditor 5 (CKE5) until the upcoming **module version 1.1.0**
- **Current support:** CKEditor 4 only
- **Expected release:** Version 1.1.0
- **Impact:** Cannot insert Cloudinary assets into CKE5 rich text fields

**JavaScript Modules (NPM)**
- Not compatible with `@jahia/javascript-modules-library` versions prior to **1.1.0**
- **Required version:** 1.1.0+
- **Expected release:** Upcoming version 1.1.0
- **Impact:** React/JSX rendering may fail with older versions

### Functional Limitations

**"Open in New Tab" Not Supported**
- When a Cloudinary asset is selected for a weak reference field, the **"Open in new tab"** context menu option does not work
- **Reason:** External assets are not directly browsable in jContent
- **Workaround:** View the asset in the Cloudinary Media Library directly

**No Direct Asset Editing**
- Assets cannot be edited directly from Jahia (transformations must be applied during selection or via template parameters)
- **Workaround:** Re-select the asset with new transformations, or edit in Cloudinary

**Cache Delay for Updates**
- Asset updates in Cloudinary are not immediately reflected in Jahia due to caching (default: 8 hours)
- **Workaround:** Flush Cloudinary cache or wait for cache expiration
- See "Cache Management" section above for cache configuration

**Limited Video Format Support**
- Only formats supported by Cloudinary are available
- **Common formats:** MP4, WebM, MOV, AVI
- **Unsupported:** Proprietary codecs may require conversion in Cloudinary

**No Batch Operations**
- Cannot bulk-select or bulk-transform multiple Cloudinary assets at once
- **Workaround:** Select and configure assets individually

**Advanced Transformations Limitation**
- Only basic transformations (crop, resize, gravity) are supported through the picker
- **Advanced features** like overlays, effects, or layered transformations require manual URL construction
- **Workaround:** Use Cloudinary's upload presets or named transformations, then reference via URL

---

## Troubleshooting

### Assets Not Appearing in the Picker

**Check these items:**
- Verify `cloudName`, `apiKey`, and `apiSecret` are configured in OSGi
- Check logs for: `INFO [CloudinaryDataSource] - Cloudinary mount point service started`
- Open browser console (F12) and look for errors
- Verify your Cloudinary account is active and not rate-limited

### Images Not Loading on the Website

**Check these items:**
- Verify property names match your content definition
- Check browser console for CORS or network errors
- Verify `cloudName` in OSGi configuration matches your Cloudinary account
- Check that assets are publicly accessible (not restricted by privacy settings)

### Slow Performance

**Solutions:**
- Increase timeout values in OSGi Configuration (Cloudinary Provider):
  ```
  connectionTimeout = 20000
  socketTimeout = 60000
  connectionRequestTimeout = 20000
  ```
- Check cache hit rate in **Cache Management**
- Use Cloudinary's automatic format (`f_auto`) and quality (`q_auto`) optimizations
- Consider using responsive breakpoints for images

### API Rate Limiting

**Symptoms:**
- Slow loading or failed requests
- Error messages in logs about rate limits

**Solutions:**
- Upgrade your Cloudinary plan for higher API limits
- Increase cache TTL to reduce API calls
- Use Cloudinary's SDK caching features

### Need More Help?

1. Check Jahia logs
2. Enable DEBUG logging: OSGi Console → Log Service → `org.jahia.se.modules.dam.cloudinary`
3. Contact Jahia Support: https://jira.jahia.org/

---

## Summary

**Key takeaways:**
- The module must be installed and configured before use
- API credentials (cloudName, apiKey, apiSecret) are required to connect to Cloudinary
- The provider starts automatically when all required settings are configured
- Caching optimizes performance (8-hour default TTL)
- Assets are referenced, not duplicated
- Cloudinary's CDN handles automatic format optimization and delivery

For usage instructions, see the [Using Cloudinary Assets Guide][user-guide].

[user-guide]: /cms/{mode}/{lang}/sites/academy/home/documentation/jahia/8_2/end-user/optional-features/using-cloudinary-assets.html

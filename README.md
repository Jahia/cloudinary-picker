# Cloudinary Picker for Jahia

Cloudinary Picker is a Jahia (8.2+) module that lets contributors select media from Cloudinary, apply optional transformations (crop, resize, aspect ratio), and reference those assets in Jahia without duplicating binaries locally. Assets remain served through Cloudinary’s CDN for optimal delivery.

## Table of Contents
1. [Introduction](#1-introduction)
2. [Quick Start](#2-quick-start)
   - [Install from Jahia Store](#21-install-from-jahia-store)
   - [Configure OSGI (Cloudinary Provider)](#22-configure-osgi-cloudinary-provider)
   - [Cache Configuration](#23-optional-cache-configuration)
   - [Enable the Module on a Site](#24-enable-the-module-on-a-site)
   - [Use in Content](#25-use-in-content)
3. [Technical Overview](#3-technical-overview)
   - [Architecture](#31-architecture-simplified)
   - [Functional Flow](#32-functional-flow)
   - [Transformation Encoding](#33-transformation-encoding)
   - [Caching Strategy](#34-caching-strategy)
   - [Security](#35-security)
   - [Performance](#36-performance)
   - [JCR Types](#37-jcr-types-see-definitionscnd)
   - [OSGI Parameters](#38-osgi-parameters-summary)
4. [Troubleshooting](#4-troubleshooting)
5. [Build from Source](#5-build-from-source)
6. [Resources](#6-resources)
7. [License / Support](#7-license--support)
8. [Revision Notes](#8-revision-notes)

---

## 1. Introduction

Key Features:
- Visual picker powered by Cloudinary Media Library
- On-the-fly crop & resize before inserting
- Lightweight JCR references (no file copy)
- Automatic delivery optimization (e.g. `f_auto`)
- Supports images, videos, and documents (PDF/other)

Main JCR node types provided:
- `cloudynt:image`
- `cloudynt:video`
- `cloudynt:pdf`
- `cloudynt:document`

---

## 2. Quick Start

### 2.1 Install from Jahia Store
1. Go to: jContent → Administration → Server → Modules and Extensions → Modules  
2. Open the “Available modules” tab  
3. Search for “cloudinary”  
4. Click the install icon  
5. Wait until it appears under “Installed modules”  

### 2.2 Configure OSGI (Cloudinary Provider)
Open: `https://<your-jahia-host>/tools` → OSGI Console → OSGI → Configuration  
Find: Cloudinary Provider (PID: `org.jahia.se.modules.dam.cloudinary.provider.config`)

Mandatory:
- Cloud Name (`cloudName`)
- API Key (`apiKey`)
- API Secret (`apiSecret` – password field)

Optional (with defaults):
- API Schema (`apiSchema`) = https  
- API EndPoint (`apiEndPoint`) = api.cloudinary.com  
- API Version (`apiVersion`) = v1_1  
- Front Key URL Pattern (`keyUrlPattern`) = cloudinary
- Front Apply On Pickers (`applyOnPickers`) = image,file,video  
- EDP Mount Path (`edpMountPath`) = /sites/systemsite/contents/dam-cloudinary  
- Connection Timeout (ms) (`connectionTimeout`) = 10000  
- Socket Timeout (ms) (`socketTimeout`) = 30000  
- Connection Request Timeout (ms) (`connectionRequestTimeout`) = 10000  

Save. The provider starts only if the 3 mandatory fields are filled.

### 2.3 (Optional) Cache Configuration
PID: `org.jahia.se.modules.dam.cloudinary.cache.config`  
Parameters (if implemented):
- `edpCacheName` (e.g. `EDPCloudinary`)
- `edpCacheTtl` (time-to-live seconds, default 28800 = 8h)
- `edpCacheTti` (time-to-idle seconds, default 3600 = 1h)

### 2.4 Enable the Module on a Site
jContent → Administration → Site Settings → Modules  
Activate “Cloudinary Picker” for the target site → Save.

### 2.5 Use in Content
1. Create or Edit a content in your jahia web project
2. Add a “Cloudinary Asset” as image, video or file weak-reference
3. Click the field to open the picker
4. Select an asset (optionally crop/resize)
5. Insert
6. Save content

Supported transformations in picker workflow: crop modes, resize (e.g. `c_fill`, `c_fit`, `c_limit`), aspect ratios, gravity, plus automatic format (`f_auto`).

> A “Cloudinary Asset” can also be added inside RichText editor (image insertion)
---

## 3. Technical Overview

### 3.1 Architecture (Simplified)
```
React Picker → Provider Service + DataSource (EDP) → Cache (Ehcache) → JCR Decorator → Cloudinary CDN
```

Core Java Components:
- [`CloudinaryProviderConfig`][CloudinaryProviderConfig]  (OSGI @ObjectClassDefinition)
- [`CloudinaryProviderService`][CloudinaryProviderService] / Impl (lifecycle + validation)
- [`CloudinaryMountPointService`][CloudinaryMountPointService] (EDP mount management)
- [`CloudinaryDataSource`][CloudinaryDataSource] (fetch, mapping, transform decoding)
- [`CloudinaryDecorator`][CloudinaryDecorator] (URL assembly)
- [`CloudinaryCacheManager`][CloudinaryCacheManager] (TTL / TTI metadata & derived URLs)

### 3.2 Functional Flow
1. User selects an asset in the React picker (widget returns cloudinary `id`)
2. The picker builds a JCR path (optional base36-encoded transformation suffix)
3. A GraphQL mutation triggers creation or retrieval of the external node (EDP)
4. CloudinaryDataSource queries Cloudinary’s Search API
5. Metadata is mapped to a CloudinaryAsset and cached
6. Base36 transformation segment (if any) is decoded and stored as `cloudy:derivedTransformation`
7. Subsequent accesses served from cache until TTL/TTI eviction
8. CloudinaryDecorator assembles final delivery URL (with transformations if any)

### 3.3 Transformation Encoding
Path format: `/assetId` or `/assetId_base36params`  
Example raw transformation: `c_crop,w_800,h_600,g_auto` → encoded into a compact base36 sequence.  
Decoded server-side → stored in property `cloudy:derivedTransformation`.

Supported (long & short) param keys when building dynamic URLs:
- `width:` / `w:`
- `height:` / `h:`
- `crop:` / `c:`
- `gravity:` / `g:`

In your jsp code, you provide these params to the node.getUrl() method, e.g.:
```jsp
//short version
<c:set var="imageURL" value="${imageNode.getUrl(['w:400','h:300','g:auto'])}"/>

//long version
<c:set var="imageURL" value="${imageNode.getUrl(['width:400','height:300','gravity:auto'])}"/>
```
In your jsx code, you provide these params to the buildNodeUrl() method, e.g.:
```jsx
const url = buildNodeUrl(imageNode,{args: {width:400, height:300, gravity:'auto'}});
```

### 3.4 Caching Strategy
Two layers:
1. Asset Metadata (`assetId`) → avoids repeated Admin API calls
2. Derived URLs (`assetId + transformHash`) → avoids recomputing repeated transforms

Configurable TTL (total lifetime) + TTI (idle eviction). Eviction policy: typically LFU (as configured via Ehcache).

### 3.5 Security
- `apiSecret` is only used server-side
- `apiKey` is also exposed client-side to initialize the Cloudinary widget
- All backend API calls use HTTPS with Basic Auth
- No secret appears in delivery URLs
- Timeouts are configurable to prevent hanging requests

### 3.6 Performance
- Lazy loading (no prefetch of entire library)
- `f_auto` + CDN ensures best format per browser
- Cache reduces latency for repeated assets
- Decorator only applies transformations when needed

### 3.7 JCR Types (see [`definitions.cnd`][definitions.cnd])
Mixin: `cloudymix:cloudyAsset` shared properties (excerpt):
- `cloudy:assetId`
- `cloudy:folder`
- `cloudy:format`
- `cloudy:version`
- `cloudy:resourceType`
- `cloudy:type`
- `cloudy:bytes`
- `cloudy:width`
- `cloudy:height`
- `cloudy:aspectRatio`
- `cloudy:url`
- `cloudy:baseUrl`
- `cloudy:endUrl`
- `cloudy:status`
- `cloudy:accessMode`
- `cloudy:accessControl`
- `cloudy:derivedTransformation`
- (plus optional `cloudy:poster`, `cloudy:duration` on specific types)

### 3.8 OSGI Parameters (Summary)
Provider PID: `org.jahia.se.modules.dam.cloudinary.provider.config`
- `apiSchema`, `apiEndPoint`, `apiVersion`
- `apiKey`, `apiSecret`, `cloudName`
- `applyOnPickers`
- `edpMountPath`
- `connectionTimeout`, `socketTimeout`, `connectionRequestTimeout`

Cache PID: `org.jahia.se.modules.dam.cloudinary.cache.config`
- `edpCacheName`
- `edpCacheTtl`
- `edpCacheTti`

---

## 4. Troubleshooting

| Issue | Likely Cause | Action |
|-------|--------------|--------|
| Provider not visible | Missing credentials | Fill `cloudName`, `apiKey`, `apiSecret` |
| Asset not found | Invalid `asset_id` / expired cache | Re-select or reduce TTL |
| Transform ignored | Invalid syntax | Check `c_`, `w_`, `h_`, `g_` formatting |
| API timeouts | Network latency | Increase timeouts in OSGI config |
| Stale data | TTL too high | Lower `edpCacheTtl` |

Check logs for classes: [`CloudinaryDataSource`][CloudinaryDataSource], [`CloudinaryProviderServiceImpl`][CloudinaryProviderServiceImpl].

---

## 5. Build from Source
```bash
mvn clean install
```
Deploy the generated JAR (in `target/`) through Jahia Module Manager.

---

## 6. Resources
- Cloudinary Admin API: https://cloudinary.com/documentation/admin_api
- Transformation Reference: https://cloudinary.com/documentation/transformation_reference
- Media Library Widget: https://cloudinary.com/documentation/media_library_widget
- Jahia External Data Provider (EDP): Jahia developer docs
- CDN & Optimization: Cloudinary documentation

---

## 7. License / Support
Check the [licence]

---

[CloudinaryProviderConfig]: ./src/main/java/org/jahia/se/modules/dam/cloudinary/provider/CloudinaryProviderConfig.java
[CloudinaryProviderService]: ./src/main/java/org/jahia/se/modules/dam/cloudinary/provider/CloudinaryProviderService.java
[CloudinaryMountPointService]: ./src/main/java/org/jahia/se/modules/dam/cloudinary/provider/CloudinaryMountPointService.java
[CloudinaryDataSource]: ./src/main/java/org/jahia/se/modules/dam/cloudinary/provider/CloudinaryDataSource.java
[CloudinaryDecorator]: ./src/main/java/org/jahia/se/modules/dam/cloudinary/provider/CloudinaryDecorator.java
[CloudinaryCacheManager]: ./src/main/java/org/jahia/se/modules/dam/cloudinary/provider/CloudinaryCacheManager.java
[CloudinaryProviderServiceImpl]: ./src/main/java/org/jahia/se/modules/dam/cloudinary/provider/CloudinaryProviderServiceImpl.java
[definitions.cnd]: ./src/main/resources/META-INF/jahia/definition.cnd
[licence]: ./LICENSE.md

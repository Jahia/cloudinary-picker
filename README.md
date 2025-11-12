# Cloudinary Picker - Cloudinary Content Picker for Jahia

This module provides seamless integration between Jahia 8.2.x+ and Cloudinary DAM,
allowing contributors to browse, select, and reference Cloudinary media assets (images and videos) directly from jContent.

**Key Features:**
- 🎨 Native jContent picker for Cloudinary assets
- 🖼️ Support for images and videos
- ⚡ Compact base36 encoding for transformations
- 🔄 Automatic thumbnail generation with optimization
- ⏱️ Configurable timeouts to prevent UI blocking
- 💾 Intelligent caching for optimal performance
- 🛡️ Graceful error handling with fallbacks
- 🌐 Cloudinary CDN integration with automatic format optimization

---

## 📚 Documentation

This module provides two comprehensive guides:

### For Administrators

**[Cloudinary Picker - Administrator Guide](./docs/end-user/cloudinary-administrator-guide.md)**

Learn how to install and configure the module:
- Installation from Jahia Store or source
- OSGi configuration (API credentials, endpoints, timeouts)
- Enabling the module for your site
- Understanding the integration architecture
- Cache management
- **Known limitations** (version compatibility, functional constraints)
- Troubleshooting common issues

👉 **Start here** if you're setting up Cloudinary integration for the first time.

### For Content Contributors & Developers

**[Using Cloudinary Assets in Jahia](./docs/end-user/using-cloudinary-assets.md)**

Learn how to work with Cloudinary assets:
- Version requirements and compatibility notes
- Selecting assets with the Cloudinary Media Library
- Applying transformations (crop/resize)
- Displaying assets in JSP templates
- Displaying assets in React/JSX templates
- Retrieving asset URLs via GraphQL
- Working with images and videos
- Understanding Cloudinary transformations

👉 **Start here** if you're creating content or developing templates.

---

## ⚡ Quick Start

1. **Install** the module from Jahia Store or deploy the JAR
2. **Configure** your Cloudinary API credentials (cloudName, apiKey, apiSecret) in OSGi Console
3. **Verify compatibility** - Check [Known Limitations](./docs/end-user/cloudinary-administrator-guide.md#known-limitations) for version requirements
4. **Enable** the module for your site
5. **Select** Cloudinary assets using the integrated Media Library picker
6. **Display** assets using provided JSP/React APIs

For detailed instructions, see the [Administrator Guide](./docs/end-user/cloudinary-administrator-guide.md).

---

## 📋 Requirements

- **Jahia:** 8.2.0.0+
- **jContent:** 3.5.0+ (for thumbnail support - upcoming)
- **CKEditor:** CKEditor 4 (CKE5 support in v1.1.0)
- **JavaScript Modules Library:** 1.1.0+ (upcoming)
- **Cloudinary Account:** Active account with API access
- **Java:** 8 or higher
- **Maven:** 3.6+ (for building from source)

⚠️ See [Known Limitations](./docs/end-user/cloudinary-administrator-guide.md#known-limitations) for detailed compatibility information.

---

## 🔧 Technical Overview

**Architecture:**
```
jContent UI (React Picker → Cloudinary Media Library)
    ↓
Jahia External Provider (EDP)
    ├─ CloudinaryDataSource (metadata & path resolution)
    ├─ CloudinaryDecorator (URL generation & transformations)
    └─ CloudinaryCacheManager (performance optimization)
    ↓
Cloudinary API (assets & CDN delivery)
```

**Asset References:**
- Assets are **referenced**, not duplicated
- Transformations encoded in compact base36 format
- URLs generated on-demand with Cloudinary CDN

**Example Identifier:**
```
b46e97_r.5k.5k.1
└─┬──┘ │ └──┬──┘
  │    │    └─ Transformation parameters (base36 encoded)
  │    └─ Transformation type (r=resize)
  └─ Cloudinary asset_id
```

**Supported Transformations:**
- Width (`w:` or `width:`) - Resize width
- Height (`h:` or `height:`) - Resize height
- Crop mode (`c:` or `crop:`) - scale, fit, fill, crop, thumb, limit
- Gravity (`g:` or `gravity:`) - auto, face, center, north, south, etc.

For technical details, see the [Administrator Guide](./docs/end-user/cloudinary-administrator-guide.md).

---

## 🤝 Support

- **Documentation:** See guides above
- **Issues:** [Jahia JIRA](https://jira.jahia.org/)
- **Jahia Academy:** [academy.jahia.com](https://academy.jahia.com/)

---

## 📄 License

This module is licensed under MIT License.

---

**Ready to get started?** Head to the [Administrator Guide](./docs/end-user/cloudinary-administrator-guide.md) to begin installation and configuration.

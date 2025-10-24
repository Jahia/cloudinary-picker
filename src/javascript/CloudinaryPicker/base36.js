/**
 * Base36 encoding system for Cloudinary transformation parameters
 *
 * This module provides efficient encoding of Cloudinary transformation parameters
 * into a compact base36 string format. This reduces URL length and improves caching.
 *
 * Encoding format: key.value.key.value...
 * Example: "c.9.w.5t.x.11.y.3n" represents "c_crop,w_209,x_37,y_133"
 *
 * Benefits:
 * - Shorter URLs (base36 vs decimal)
 * - Standardized parameter order
 * - Efficient cache key generation
 */

// Format mappings (0-indexed)
// Maps format names to integer indices for compact encoding
const FORMAT_MAP = {webp: 0, jpg: 1, jpeg: 1, png: 2, gif: 3, auto: 4};

// Crop mode mappings (c_*) - 0-indexed
// All Cloudinary crop modes with their corresponding indices
const CROP_MODE_MAP = {
    scale: 0, // Simple resize
    fit: 1, // Fit within dimensions
    limit: 2, // Resize only if larger
    mfit: 3, // Fit for multiple images
    fill: 4, // Fill dimensions with crop
    lfill: 5, // Fill with auto gravity
    pad: 6, // Fill with padding
    lpad: 7, // Pad with auto gravity
    mpad: 8, // Pad for multiple images
    crop: 9, // Custom crop
    thumb: 10, // Thumbnail with face detection
    imagga_crop: 11, // Intelligent crop via Imagga
    imagga_scale: 12 // Intelligent scale via Imagga
};

// Gravity mappings (g_*) - 0-indexed
// Defines focal points for cropping and transformations
const GRAVITY_MAP = {
    center: 0, // Center of image
    north: 1, // Top
    south: 2, // Bottom
    east: 3, // Right
    west: 4, // Left
    north_east: 5, // Top-right corner
    north_west: 6, // Top-left corner
    south_east: 7, // Bottom-right corner
    south_west: 8, // Bottom-left corner
    face: 9, // Detected face
    faces: 10, // All detected faces
    auto: 11, // Automatic focal point
    auto_face: 12, // Auto with face priority
    auto_faces: 13 // Auto with all faces priority
};

/**
 * Encodes decimal values for compact representation
 * Multiplies by 100 to preserve 2 decimal places as integers
 *
 * @param {string|number} value - The decimal value to encode
 * @param {number} multiplier - Multiplier for precision (default: 100)
 * @returns {number} The encoded integer value
 *
 * @example
 * encodeDecimal(0.5) // returns 50
 * encodeDecimal(1.25) // returns 125
 */
const encodeDecimal = (value, multiplier = 100) => {
    return Math.round(parseFloat(value || 0) * multiplier);
};

/**
 * Encodes aspect ratio into compact base36 format
 * Handles both ratio format (16:9) and single values
 *
 * @param {string} ar - The aspect ratio (e.g., "16:9" or "1.5")
 * @returns {string|null} Encoded aspect ratio in base36
 *
 * @example
 * encodeAspectRatio("16:9") // returns "g:5" (16 and 9 in base36)
 * encodeAspectRatio("1.5") // returns "1" (1 in base36)
 */
const encodeAspectRatio = ar => {
    if (!ar) {
        return null;
    }

    const parts = ar.split(':');
    if (parts.length === 2) {
        return `${parseInt(parts[0], 10).toString(36)}:${parseInt(parts[1], 10).toString(36)}`;
    }

    return parseInt(ar, 10).toString(36);
};

/**
 * Parses Cloudinary transformation string into parameter object
 * Handles comma-separated transformations with underscore-separated key-value pairs
 *
 * @param {string} transformation - Cloudinary transformation string
 * @returns {Object} Object with transformation parameters
 *
 * @example
 * parseTransformation("c_crop,w_209,x_37,y_133")
 * // returns { c: "crop", w: "209", x: "37", y: "133" }
 */
const parseTransformation = transformation => {
    if (!transformation) {
        return {};
    }

    const params = {};
    const parts = transformation.split(',');

    parts.forEach(part => {
        const [key, value] = part.split('_');
        if (key && value) {
            params[key] = value;
        }
    });

    return params;
};

/**
 * Main encoding function: converts Cloudinary transformations to compact base36 string
 *
 * Process:
 * 1. Parse input (either raw transformation string or URL)
 * 2. Extract and encode all transformation parameters
 * 3. Build ordered base36 string
 *
 * @param {string} input - Either a transformation string or a full Cloudinary URL
 * @returns {string} Compact base36 encoded string
 *
 * @example
 * // From transformation string
 * getQueryParamsBase36("c_crop,w_209,x_37,y_133")
 * // returns "c.9.w.5t.x.11.y.3n"
 *
 * // From URL
 * getQueryParamsBase36("https://res.cloudinary.com/.../c_crop,w_209/...")
 * // returns "c.9.w.5t"
 */
export const getQueryParamsBase36 = input => {
    if (!input) {
        return '';
    }

    let rawParams = {};

    // Check if input is a transformation string (e.g., "c_crop,w_209,x_37")
    if (typeof input === 'string' && !input.startsWith('http')) {
        rawParams = parseTransformation(input);
    } else if (typeof input === 'string' && input.includes('/')) {
        // Extract transformation from URL path
        // URL format: .../upload/transformations/version/...
        const match = input.match(/\/([^/]+)\/v\d+\//);
        if (match && match[1]) {
            rawParams = parseTransformation(match[1]);
        }
    }

    const params = {};

    // Encode crop mode (c)
    if (rawParams.c) {
        params.c = CROP_MODE_MAP[rawParams.c] || rawParams.c;
    }

    // Encode dimensions (width, height, aspect ratio)
    if (rawParams.w) {
        params.w = parseInt(rawParams.w, 10);
    }

    if (rawParams.h) {
        params.h = parseInt(rawParams.h, 10);
    }

    if (rawParams.ar) {
        params.ar = rawParams.ar;
    }

    // Encode position and gravity
    if (rawParams.g) {
        params.g = GRAVITY_MAP[rawParams.g] || rawParams.g;
    }

    if (rawParams.x) {
        params.x = parseInt(rawParams.x);
    }

    if (rawParams.y) {
        params.y = parseInt(rawParams.y);
    }

    // Encode zoom, rotation, and device pixel ratio
    // These are encoded as decimals (multiplied by 100)
    if (rawParams.z) {
        params.z = encodeDecimal(rawParams.z);
    }

    if (rawParams.a) {
        params.a = parseInt(rawParams.a);
    }

    if (rawParams.dpr) {
        params.dpr = encodeDecimal(rawParams.dpr);
    }

    // Encode format
    if (rawParams.f) {
        params.f = FORMAT_MAP[rawParams.f] || rawParams.f;
    }

    // Build base36 string with standardized parameter order
    // Order ensures consistent cache keys for identical transformations
    const parts = [];
    const order = ['c', 'w', 'h', 'ar', 'g', 'x', 'y', 'z', 'a', 'dpr', 'f'];

    order.forEach(key => {
        if (params[key] !== undefined) {
            const value = params[key];
            if (key === 'ar') {
                // Special handling for aspect ratio
                parts.push(`${key}.${encodeAspectRatio(value)}`);
            } else if (typeof value === 'number') {
                // Convert numbers to base36
                parts.push(`${key}.${value.toString(36)}`);
            } else {
                // Use value as-is (already encoded)
                parts.push(`${key}.${value}`);
            }
        }
    });

    return parts.length > 0 ? parts.join('.') : '';
};

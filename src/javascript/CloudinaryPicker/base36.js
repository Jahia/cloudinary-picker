// Format mappings
const FORMAT_MAP = {webp: 1, jpg: 2, jpeg: 2, png: 3, gif: 4, auto: 5};

// Crop mode mappings (c_*)
const CROP_MODE_MAP = {
    scale: 1,
    fit: 2,
    limit: 3,
    mfit: 4,
    fill: 5,
    lfill: 6,
    pad: 7,
    lpad: 8,
    mpad: 9,
    crop: 10,
    thumb: 11,
    imagga_crop: 12,
    imagga_scale: 13
};

// Gravity mappings (g_*)
const GRAVITY_MAP = {
    center: 1,
    north: 2,
    south: 3,
    east: 4,
    west: 5,
    north_east: 6,
    north_west: 7,
    south_east: 8,
    south_west: 9,
    face: 10,
    faces: 11,
    auto: 12,
    auto_face: 13,
    auto_faces: 14
};

// Helper to encode decimal numbers (e.g., 0.5 -> 50)
const encodeDecimal = (value, multiplier = 100) => {
    return Math.round(parseFloat(value || 0) * multiplier);
};

// Helper to encode aspect ratio (e.g., "16:9" -> "g:5")
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

// Parse Cloudinary transformation string (e.g., "c_crop,w_209,x_37,y_133")
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

// Cloudinary parameters encoding from raw transformation or URL
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
        const match = input.match(/\/([^/]+)\/v\d+\//);
        if (match && match[1]) {
            rawParams = parseTransformation(match[1]);
        }
    }

    const params = {};

    // Crop mode (c)
    if (rawParams.c) {
        params.c = CROP_MODE_MAP[rawParams.c] || rawParams.c;
    }

    // Dimensions
    if (rawParams.w) {
        params.w = parseInt(rawParams.w, 10);
    }

    if (rawParams.h) {
        params.h = parseInt(rawParams.h, 10);
    }

    if (rawParams.ar) {
        params.ar = rawParams.ar;
    }

    // Position and gravity
    if (rawParams.g) {
        params.g = GRAVITY_MAP[rawParams.g] || rawParams.g;
    }

    if (rawParams.x) {
        params.x = parseInt(rawParams.x);
    }

    if (rawParams.y) {
        params.y = parseInt(rawParams.y);
    }

    // Zoom, rotation, DPR
    if (rawParams.z) {
        params.z = encodeDecimal(rawParams.z);
    }

    if (rawParams.a) {
        params.a = parseInt(rawParams.a);
    }

    if (rawParams.dpr) {
        params.dpr = encodeDecimal(rawParams.dpr);
    }

    // Format
    if (rawParams.f) {
        params.f = FORMAT_MAP[rawParams.f] || rawParams.f;
    }

    // Build base36 string
    const parts = [];
    const order = ['c', 'w', 'h', 'ar', 'g', 'x', 'y', 'z', 'a', 'dpr', 'f'];

    order.forEach(key => {
        if (params[key] !== undefined) {
            const value = params[key];
            if (key === 'ar') {
                parts.push(`${key}.${encodeAspectRatio(value)}`);
            } else if (typeof value === 'number') {
                parts.push(`${key}.${value.toString(36)}`);
            } else {
                parts.push(`${key}.${value}`);
            }
        }
    });

    return parts.length > 0 ? parts.join('.') : '';
};

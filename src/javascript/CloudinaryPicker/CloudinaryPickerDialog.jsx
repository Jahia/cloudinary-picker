/* eslint camelcase: 0 */
import React, {useEffect, useState} from 'react';
import PropTypes from 'prop-types';
import {LoaderOverlay} from '../DesignSystem/LoaderOverlay';
import {useTranslation} from 'react-i18next';
import {useQuery} from '@apollo/react-hooks';
import {edpCoudinaryContentUUIDQuery} from './edpCoudinaryContentUUID.gql-queries';
import {getQueryParamsBase36} from './base36';

/**
 * CloudinaryPickerDialog Component
 *
 * Integrates Cloudinary Media Library widget for asset selection in Jahia.
 *
 * Flow:
 * 1. Initialize Cloudinary Media Library widget
 * 2. User selects assets (with optional transformations)
 * 3. Encode transformation parameters to base36
 * 4. Build content paths with encoded parameters
 * 5. Query Jahia to get JCR node UUIDs
 * 6. Return selected assets with UUIDs to parent
 *
 * @param {string} className - CSS class for styling
 * @param {function} onItemSelection - Callback when assets are selected
 * @param {boolean} isMultiple - Allow multiple asset selection
 * @param {array} initialSelectedItem - Previously selected items
 */
export const CloudinaryPickerDialog = ({className, onItemSelection, isMultiple, initialSelectedItem}) => {
    const {t} = useTranslation();
    const [cloudinaryData, setCloudinaryData] = useState();

    // Get Cloudinary configuration from Jahia context
    const config = window.contextJsParameters.config?.cloudinary;

    // Query Jahia to get UUIDs for selected assets
    // This creates the JCR nodes if they don't exist
    const {data, loading, error} = useQuery(edpCoudinaryContentUUIDQuery, {
        variables: {
            edpContentPaths: cloudinaryData && cloudinaryData.assets.map(asset => {
                // Encode transformation parameters from derived asset
                const paramsHash = asset.derived && asset.derived.length > 0 ?
                    getQueryParamsBase36(asset.derived[0].raw_transformation) :
                    '';

                // Build content path: mountPoint/assetId_encodedParams
                const path = config.mountPoint + '/' + asset.id;
                return paramsHash ? path + '_' + paramsHash : path;
            })
        },
        skip: !cloudinaryData // Only run query when assets are selected
    });

    // Initialize Cloudinary Media Library widget
    React.useEffect(() => {
        if (!config?.cloudName || !config?.apiKey) {
            console.error('oups... cloudinary cloudName and apiKey are not configured! Please fill the cloudinary_picker_credentials.cfg file.');
        } else if (window.cloudinary) {
            // Configure and open Cloudinary Media Library
            window.cloudinary.openMediaLibrary({
                cloud_name: config.cloudName,
                api_key: config.apiKey,
                inline_container: '#CloudinaryWebHookElement', // Render inside this div
                multiple: Boolean(isMultiple), // Single or multiple selection
                remove_header: true // Hide Cloudinary header
            }, {
                // Callback when user selects assets
                insertHandler: data => {
                    setCloudinaryData(data);
                }
            });
        } else {
            console.debug('oups... no window.cloudinary available !');
        }
    }, [config, isMultiple]);

    // Process selected assets once UUIDs are available
    useEffect(() => {
        if (!error && !loading && data?.jcr?.result) {
            // Extract from Cloudinary data exts data potentially used by CKEditor
            const exts = cloudinaryData.assets.map(({url, derived, public_id: name}) => ({
                name,
                url: derived && derived.length > 0 ? derived[0].url : url
            }));

            // Merge data from Jahia with exts data
            const currentSelection = data.jcr.result.map((m, i) => ({...m, ...exts[i]}));

            // Return selection to parent component
            if (isMultiple) {
                // Append to existing selection
                onItemSelection([...initialSelectedItem, ...currentSelection]);
            } else {
                // Replace existing selection
                onItemSelection(currentSelection);
            }
        }
    }, [cloudinaryData, data, error, loading, onItemSelection, isMultiple, initialSelectedItem]);

    // Handle errors
    if (error) {
        const message = t(
            'jcontent:label.jcontent.error.queryingContent',
            {details: error.message ? error.message : ''}
        );

        console.warn(message);
    }

    // Show loader while querying Jahia
    if (loading) {
        return <LoaderOverlay/>;
    }

    // Render Cloudinary Media Library container
    return (<div id="CloudinaryWebHookElement" className={className}/>);
};

CloudinaryPickerDialog.propTypes = {
    className: PropTypes.string,
    onItemSelection: PropTypes.func.isRequired,
    isMultiple: PropTypes.bool,
    initialSelectedItem: PropTypes.arrayOf(PropTypes.object)
};

/* eslint camelcase: 0 */

import React, {useEffect, useState} from 'react';
import PropTypes from 'prop-types';
import {useTranslation} from 'react-i18next';
import {useQuery} from '@apollo/react-hooks';
import {edpCoudinaryContentUUIDQuery} from './edpCoudinaryContentUUID.gql-queries';
import {LoaderOverlay} from '../DesignSystem/LoaderOverlay';

export const CloudinaryPickerDialog = ({className, onItemSelection, isMultiple}) => {
    const {t} = useTranslation();
    const [cloudinaryData, setCloudinaryData] = useState();

    const config = window.contextJsParameters.config?.cloudinary;

    const {data, loading, error} = useQuery(edpCoudinaryContentUUIDQuery, {
        variables: {
            edpContentPaths: cloudinaryData && cloudinaryData.assets.map(asset => config.mountPoint + '/' + asset.id)
        },
        skip: !cloudinaryData
    });

    React.useEffect(() => {
        if (!config?.cloudName || !config?.apiKey) {
            console.error('oups... cloudinary cloudName and apiKey are not configured! Please fill the cloudinary_picker_credentials.cfg file.');
        } else if (window.cloudinary) {
            // #0 Prepare the cloudinary media lib
            window.cloudinary.openMediaLibrary({
                cloud_name: config.cloudName,
                api_key: config.apiKey,
                inline_container: '#CloudinaryWebHookElement',
                multiple: Boolean(isMultiple), // Cannot select more than one asset
                remove_header: true
            }, {
                insertHandler: data => {
                    setCloudinaryData(data);
                }
            });
        } else {
            console.debug('oups... no window.cloudinary available !');
        }
    }, [config, isMultiple]);

    useEffect(() => {
        if (!error && !loading && data?.jcr?.result) {
            const urls = cloudinaryData.assets.map(({url, derived}) => {
                if (derived && derived.length > 0) {
                    return derived[0].url;
                }

                return url;
            });

            onItemSelection(data?.jcr?.result.map((m, i) => ({...m, url: urls[i]})));
        }
    }, [cloudinaryData, data, error, loading, onItemSelection]);

    if (error) {
        const message = t(
            'jcontent:label.jcontent.error.queryingContent',
            {details: error.message ? error.message : ''}
        );

        console.warn(message);
    }

    if (loading) {
        return <LoaderOverlay/>;
    }

    return (<div id="CloudinaryWebHookElement" className={className}/>);
};

CloudinaryPickerDialog.propTypes = {
    className: PropTypes.string,
    onItemSelection: PropTypes.func.isRequired,
    isMultiple: PropTypes.bool
};
// CloudinaryPickerDialog.propTypes = {
//     editorContext: PropTypes.object.isRequired,
//     value: PropTypes.string,
//     field: PropTypes.object.isRequired,
//     inputContext: PropTypes.object.isRequired,
//     onChange: PropTypes.func.isRequired,
//     onBlur: PropTypes.func.isRequired
// };

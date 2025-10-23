/* eslint camelcase: 0 */
import React, {useEffect, useState} from 'react';
import PropTypes from 'prop-types';
import {LoaderOverlay} from '../DesignSystem/LoaderOverlay';
import {useTranslation} from 'react-i18next';
import {useQuery} from '@apollo/react-hooks';
import {edpCoudinaryContentUUIDQuery} from './edpCoudinaryContentUUID.gql-queries';
import {getQueryParamsBase36} from './base36';

export const CloudinaryPickerDialog = ({className, onItemSelection, isMultiple, initialSelectedItem}) => {
    const {t} = useTranslation();
    const [cloudinaryData, setCloudinaryData] = useState();

    const config = window.contextJsParameters.config?.cloudinary;

    const {data, loading, error} = useQuery(edpCoudinaryContentUUIDQuery, {
        variables: {
            edpContentPaths: cloudinaryData && cloudinaryData.assets.map(asset => {
                const paramsHash = asset.derived && asset.derived.length > 0 ?
                    getQueryParamsBase36(asset.derived[0].raw_transformation) :
                    '';
                const path = config.mountPoint + '/' + asset.id;
                return paramsHash ? path + '_' + paramsHash : path;
            })
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
            const exts = cloudinaryData.assets.map(({url, derived, public_id: name}) => ({
                name,
                url: derived && derived.length > 0 ? derived[0].url : url
            }));
            const currentSelection = data.jcr.result.map((m, i) => ({...m, ...exts[i]}));
            if (isMultiple) {
                onItemSelection([...initialSelectedItem, ...currentSelection]);
            } else {
                onItemSelection(currentSelection);
            }
        }
    }, [cloudinaryData, data, error, loading, onItemSelection, isMultiple, initialSelectedItem]);

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
    isMultiple: PropTypes.bool,
    initialSelectedItem: PropTypes.arrayOf(PropTypes.object)
};

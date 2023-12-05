import React from 'react';
import PropTypes from 'prop-types';
import {useTranslation} from 'react-i18next';
import {useLazyQuery} from "@apollo/react-hooks";
import {edpCoudinaryContentUUIDQuery} from "./edpCoudinaryContentUUID.gql-queries";
import {LoaderOverlay} from "../DesignSystem/LoaderOverlay";

export const CloudinaryPickerDialog = ({className, onItemSelection,isCkEditor, isMultiple}) => {
    const {t} = useTranslation();
    const [loadEdp4UUID, selectedNodeUUID] = useLazyQuery(edpCoudinaryContentUUIDQuery);

    const config = window.contextJsParameters.config?.cloudinary;

    React.useEffect( () => {
        if(!config?.cloudName || !config?.apiKey){
            console.error("oups... cloudinary cloudName and apiKey are not configured! Please fill the cloudinary_picker_credentials.cfg file.")
        }else{
            if(window.cloudinary){
                //#0 Prepare the cloudinary media lib
                window.cloudinary.openMediaLibrary({
                    cloud_name: config.cloudName,
                    api_key: config.apiKey,
                    inline_container:"#CloudinaryWebHookElement",
                    multiple: isMultiple, //cannot select more than one asset
                    remove_header:true
                }, {
                    insertHandler: (data) => {
                        //Get url or create record and get uuid
                        if(isCkEditor){
                            const urls = data.assets.map(({url,derived}) => {
                                if(derived && derived.length > 0)
                                    return derived[0].url;
                                return url;
                            });
                            onItemSelection(urls);
                        }else{
                            const asset_id = data.assets[0].id;
                            const edpContentPath = config.mountPoint + "/" + asset_id
                            loadEdp4UUID({
                                variables: {
                                    edpContentPath
                                }
                            })
                        }
                    }
                } );
            }else{
                console.debug("oups... no window.cloudinary available !")
            }
        }
    },[]);

    const error = selectedNodeUUID?.error;
    const loading = selectedNodeUUID?.loading;

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

    if(selectedNodeUUID?.data?.jcr?.result?.uuid){
        onItemSelection([{uuid:selectedNodeUUID.data.jcr.result.uuid}]);
    }

    return (<div id="CloudinaryWebHookElement" className={className}></div>);
}
CloudinaryPickerDialog.propTypes = {
    className: PropTypes.object,
    onItemSelection: PropTypes.func.isRequired,
    isCkEditor: PropTypes.bool,
    isMultiple: PropTypes.bool
}
// CloudinaryPickerDialog.propTypes = {
//     editorContext: PropTypes.object.isRequired,
//     value: PropTypes.string,
//     field: PropTypes.object.isRequired,
//     inputContext: PropTypes.object.isRequired,
//     onChange: PropTypes.func.isRequired,
//     onBlur: PropTypes.func.isRequired
// };

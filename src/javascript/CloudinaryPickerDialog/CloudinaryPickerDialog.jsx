import React from 'react';
import PropTypes from 'prop-types';
import {useTranslation} from 'react-i18next';
import {postData} from "../CloudinaryPicker/engine";
import {useLazyQuery} from "@apollo/react-hooks";
import {edpCoudinaryContentUUIDQuery} from "./edpCoudinaryContentUUID.gql-queries";
import {LoaderOverlay} from "../DesignSystem/LoaderOverlay";
// import svgCloudyLogo from "../asset/logo.svg";
// import {Button, toIconComponent} from "@jahia/moonstone";
// import {getButtonRenderer} from '../utils';

// const ButtonRenderer = getButtonRenderer({labelStyle: 'none', defaultButtonProps: {variant: 'ghost'}});

export const CloudinaryPickerDialog = ({className, onItemSelection}) => {
    const [widget,setWidget] = React.useState(null);
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
                    multiple: false, //cannot select more than one asset
                    remove_header:true
                }, {
                    insertHandler: (data) => {
                        // console.debug("cloudinary selected content : ",data);
                        //#1 fetch asset_id now id is there
                        // postData(
                        //     "/resources/search",
                        //     {expression: `public_id=${data.assets[0].public_id} && resource_type=${data.assets[0].resource_type}`}
                        // ).then( apiData => {
                        //     const asset_id = apiData?.resources[0]?.asset_id;
                            const asset_id = data.assets[0].id;
                            const edpContentPath = config.mountPoint + "/" + asset_id
                            //#2 create record and get uuid
                            loadEdp4UUID({
                                variables: {
                                    edpContentPath
                                }
                            })
                        // });
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
    onItemSelection: PropTypes.func.isRequired
}
// CloudinaryPickerDialog.propTypes = {
//     editorContext: PropTypes.object.isRequired,
//     value: PropTypes.string,
//     field: PropTypes.object.isRequired,
//     inputContext: PropTypes.object.isRequired,
//     onChange: PropTypes.func.isRequired,
//     onBlur: PropTypes.func.isRequired
// };

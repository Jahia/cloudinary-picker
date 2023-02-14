import React from 'react'
import {FileImage} from '@jahia/moonstone';
import {LoaderOverlay} from '../../DesignSystem/LoaderOverlay';
import {useTranslation} from 'react-i18next';
import {postData} from "../engine";
import {useQuery,useLazyQuery} from "@apollo/react-hooks";
import {edpCoudinaryContentUUIDQuery} from "./edpCoudinaryContentUUID.gql-queries";
import {edpCoudinaryContentPropsQuery} from "./edpCoudinaryContentProps.gql-queries";
import {ReferenceCard} from "./Viewer";

export const CloudinaryPickerCmp = ({field,value,editorContext,onChange}) => {
    const [widget,setWidget] = React.useState(null);
    const {t} = useTranslation();

    const [loadEdp4UUID, { loading: lazyLoading, data : lazyData }] = useLazyQuery(edpCoudinaryContentUUIDQuery);

    const config = window.contextJsParameters.config?.cloudinary;

    React.useEffect( () => {
        if(!config?.cloudName || !config?.apiKey){
            console.error("oups... cloudinary cloudName and apiKey are not configured! Please fill the cloudinary_picker_credentials.cfg file.")
        }else{
            if(window.cloudinary){
                //#0 Prepare the cloudinary media lib
                setWidget(window.cloudinary.createMediaLibrary({
                    cloud_name: config.cloudName,
                    api_key: config.apiKey,
                    multiple: false //cannot select more than one asset
                }, {
                    insertHandler: (data) => {
                        console.debug("cloudinary selected content : ",data);
                        //#1 fetch asset_id
                        postData(
                            "/resources/search",
                            {expression: `public_id=${data.assets[0].public_id} && resource_type=${data.assets[0].resource_type}`}
                        ).then( apiData => {
                            const asset_id = apiData?.resources[0]?.asset_id;
                            const edpContentPath = config.mountPoint + "/" + asset_id
                            //#2 create record and get uuid
                            loadEdp4UUID({
                                variables: {
                                    edpContentPath,
                                    skip: !asset_id
                                }
                            })
                        });
                    }
                } ));
            }else{
                console.debug("oups... no window.cloudinary available !")
            }
        }
    },[]);

// console.log("value : ",value)
    const variables = {
        uuid : value,
        language: editorContext.lang,
        skip: !value
    };
    // Console.log("[WidenPicker] variables for WidenPickerFilledQuery : ",variables);

    const {loading, error, data} = useQuery(edpCoudinaryContentPropsQuery, {
        variables
    });

    if (error) {
        const message = t(
            'jcontent:label.jcontent.error.queryingContent',
            {details: error.message ? error.message : ''}
        );

        console.warn(message);
    }

    if (loading || lazyLoading) {
        return <LoaderOverlay/>;
    }
console.log("lazyData : ",lazyData);
    if(lazyData){
        onChange(lazyData.jcr?.result?.uuid)
    }

    let fieldData = null;
    const cloudinaryJcrProps = data?.jcr?.result;

    if(cloudinaryJcrProps)
        fieldData = {
            name : cloudinaryJcrProps.displayName,
            resourceType: cloudinaryJcrProps.resourceType?.value,
            format: cloudinaryJcrProps.format?.value,
            url: cloudinaryJcrProps.url?.value,
            baseUrl: cloudinaryJcrProps.baseUrl?.value,
            endUrl: cloudinaryJcrProps.endUrl?.value,
            poster: cloudinaryJcrProps.poster?.value,
            width: cloudinaryJcrProps.width?.value,
            height: cloudinaryJcrProps.height?.value,
            bytes: cloudinaryJcrProps.bytes?.value,
            aspectRatio: cloudinaryJcrProps.aspectRatio?.value,
        }


    const handleShow = () =>
        widget.show();

    return (
        <>
            {widget &&
            <ReferenceCard
                isReadOnly={field.readOnly}
                emptyLabel="Add Cloudinary Asset"
                emptyIcon={<FileImage/>}
                labelledBy={`${field.name}-label`}
                fieldData={fieldData}
                onClick={handleShow}
            />}
        </>

    )
}

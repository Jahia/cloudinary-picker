import React from 'react'
import {FileImage} from '@jahia/moonstone';
import {LoaderOverlay} from '../../DesignSystem/LoaderOverlay';
import {useTranslation} from 'react-i18next';
import {postData} from "../engine";
import {useQuery} from "@apollo/react-hooks";
import {edpCoudinaryContentUUIDQuery} from "./edpCoudinaryContentUUID.gql-queries";
import {edpCoudinaryContentPropsQuery} from "./edpCoudinaryContentProps.gql-queries";
import {ReferenceCard} from "./Viewer";

const _GetUuid = (edpContentPath,uuidHandler) => {
    const variables = {
        edpContentPath,
        skip: !edpContentPath
    };

    // Call the EDP to get uuid
    const {loading, error, data} = useQuery(edpCoudinaryContentUUIDQuery, {
        variables
    });

    if (loading || error || !data || !edpContentPath) {
        console.log("[_GetUuid] loading, error, !data, !edpContentPath",loading, error, !data, !edpContentPath)
        return; // {error, loading, notFound: Boolean(path)};
    }
    uuidHandler(data.jcr?.result?.uuid);

    return (<></>);
};


export const CloudinaryPickerCmp = ({field,value,editorContext,onChange}) => {
    // const [open,setOpen] = React.useState(false);
    const [widget,setWidget] = React.useState(null);
    const [uuid,setUuid] = React.useState(value);
    const {t} = useTranslation();

    const config = window.contextJsParameters.config?.cloudinary;
    React.useEffect( () => {
        if(!config?.cloudName || !config?.apiKey){
            console.error("oups... cloudinary cloudName and apiKey are not configured! Please fill the cloudinary_picker_credentials.cfg file.")
        }else{
            if(window.cloudinary){
                setWidget(window.cloudinary.createMediaLibrary({
                    cloud_name: config.cloudName,
                    api_key: config.apiKey,
                }, {
                    insertHandler: (data) => {
                        console.log("cloudinary selected content : ",data);
                        //#1 fetch asset_id
                        postData(
                            "/resources/search",
                            {expression: `public_id=${data.assets[0].public_id} && resource_type=${data.assets[0].resource_type}`}
                        ).then(
                            //#2 create record and get uuid
                            apiData => _GetUuid(apiData?.resources[0]?.asset_id,setUuid)
                        );
                        //#3 query content data ?
                    }
                } ));
            }else{
                console.debug("oups... no window.cloudinary available !")
            }
        }
    },[])
console.log("uuid : ",uuid)
    const variables = {
        uuid,
        language: editorContext.lang,
        skip: !uuid
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

    if (loading) {
        return <LoaderOverlay/>;
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

import {useQuery} from '@apollo/react-hooks';
import {edpCoudinaryContentPropsQuery} from "./edpCoudinaryContentProps.gql-queries";
import {useContentEditorContext} from '@jahia/jcontent'
export const useCloudinaryPickerInputData = uuids => {
    const {lang} = useContentEditorContext();

    const {data, error, loading} = useQuery(edpCoudinaryContentPropsQuery, {
        variables: {
            uuids: uuids || [],
            language: lang
        },
        skip: !uuids,
        errorPolicy: 'ignore'
    });

    if (loading || error || !data || !data.jcr || !uuids || (data.jcr.result.length === 0 && uuids.length > 0)) {
        return {error, loading, notFound: Boolean(uuids)};
    }

    const getImgUrl = ({poster,baseUrl,endUrl}) => `${baseUrl}/w_200/${poster || endUrl}`;

    const fieldData = data.jcr.result.map(cloudyContentData => {
        const sizeInfo = (cloudyContentData.height && cloudyContentData.width) ? ` - ${parseInt(cloudyContentData.width.value, 10)}x${parseInt(cloudyContentData.height.value, 10)}px` : '';
        const mime = `${cloudyContentData.resourceType?.value}/${cloudyContentData.format?.value}`;
        return {
            uuid: cloudyContentData.uuid,
            url: getImgUrl({
                poster:cloudyContentData.poster?.value,
                baseUrl:cloudyContentData.baseUrl?.value,
                endUrl:cloudyContentData.endUrl?.value
            }),
            name: cloudyContentData.displayName,
            path: cloudyContentData.path,
            info: `${mime}${sizeInfo} (r:${cloudyContentData.aspectRatio?.value} )`
        };
    });

    return {fieldData, error, loading};
};

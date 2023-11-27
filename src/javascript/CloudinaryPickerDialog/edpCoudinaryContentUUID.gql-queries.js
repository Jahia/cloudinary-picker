import {gql} from 'graphql-tag';
import {PredefinedFragments} from '@jahia/data-helper';

export const edpCoudinaryContentUUIDQuery = gql`
    query edpCoudinaryContentUUIDQuery($edpContentPath: String!) {
        jcr{
            result: nodeByPath(path: $edpContentPath) {
                ...NodeCacheRequiredFields
            }
        }
    }
    ${PredefinedFragments.nodeCacheRequiredFields.gql}
`;

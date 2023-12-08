import {gql} from 'graphql-tag';
import {PredefinedFragments} from '@jahia/data-helper';

export const edpCoudinaryContentUUIDQuery = gql`
    query edpCoudinaryContentUUIDQuery($edpContentPaths: [String!]!) {
        jcr{
            result: nodesByPath(paths: $edpContentPaths) {
                ...NodeCacheRequiredFields
            }
        }
    }
    ${PredefinedFragments.nodeCacheRequiredFields.gql}
`;

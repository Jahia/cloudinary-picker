import {gql} from 'graphql-tag';
import {PredefinedFragments} from '@jahia/data-helper';

export const edpCoudinaryContentPropsQuery = gql`
    query edpCoudinaryContentPropsQuery($uuid: String!,$language: String!) {
        jcr{
            result: nodeById(uuid: $uuid) {
                displayName(language: $language)
                resourceType: property(name: "cloudy:resourceType") {value}
                format: property(name: "cloudy:format") {value}
                url: property(name: "cloudy:url") {value}
                baseUrl: property(name: "cloudy:baseUrl") {value}
                endUrl: property(name: "cloudy:endUrl") {value}
                poster: property(name: "cloudy:poster") {value}
                width: property(name: "cloudy:width") {value}
                height: property(name: "cloudy:height") {value}
                bytes: property(name: "cloudy:bytes") {value}
                aspectRatio: property(name: "cloudy:aspectRatio") {value}
                ...NodeCacheRequiredFields
            }
        }
    }
    ${PredefinedFragments.nodeCacheRequiredFields.gql}
`;

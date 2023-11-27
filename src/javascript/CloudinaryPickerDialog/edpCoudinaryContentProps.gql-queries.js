import {gql} from 'graphql-tag';
import {PredefinedFragments} from '@jahia/data-helper';

export const edpCoudinaryContentPropsQuery = gql`
    query edpCoudinaryContentPropsQuery($uuids: [String!]!,$language: String!) {
        jcr{
            result: nodesById(uuids: $uuids) {
                displayName(language: $language)
                width: property(name: "cloudy:width") {value}
                height: property(name: "cloudy:height") {value}
                resourceType: property(name: "cloudy:resourceType") {value}
                format: property(name: "cloudy:format") {value}
                url: property(name: "cloudy:url") {value}
                baseUrl: property(name: "cloudy:baseUrl") {value}
                endUrl: property(name: "cloudy:endUrl") {value}
                poster: property(name: "cloudy:poster") {value}
                bytes: property(name: "cloudy:bytes") {value}
                aspectRatio: property(name: "cloudy:aspectRatio") {value}
                ...NodeCacheRequiredFields
            }
        }
    }
    ${PredefinedFragments.nodeCacheRequiredFields.gql}
`;

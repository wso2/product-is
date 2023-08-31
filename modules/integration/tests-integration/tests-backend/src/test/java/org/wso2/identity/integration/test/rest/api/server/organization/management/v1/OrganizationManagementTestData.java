/*
 * Copyright (c) 2023, WSO2 LLC. (http://www.wso2.com).
 *
 * WSO2 LLC. licenses this file to you under the Apache License,
 * Version 2.0 (the "License"); you may not use this file except
 * in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */

package org.wso2.identity.integration.test.rest.api.server.organization.management.v1;

/**
 * Contains test data for organization management REST API tests.
 */
public class OrganizationManagementTestData {

    public static final String APPLICATION_PAYLOAD = "{" +
            "\"name\": \"pickup-dispatch\"," +
            "\"description\": \"This is the configuration for Pickup-dispatch application.\"," +
            "\"imageUrl\": \"https://example.com/logo/my-logo.png\"," +
            "\"accessUrl\": \"https://example.com/login\"," +
            "\"templateId\": \"b9c5e11e-fc78-484b-9bec-015d247561b8\"," +
            "\"isManagementApp\": false," +
            "\"claimConfiguration\": {" +
            "  \"dialect\": \"LOCAL\"," +
            "  \"claimMappings\": [" +
            "    {" +
            "      \"applicationClaim\": \"firstname\"," +
            "      \"localClaim\": {" +
            "        \"uri\": \"http://wso2.org/claims/username\"" +
            "      }" +
            "    }" +
            "  ]," +
            "  \"requestedClaims\": [" +
            "    {" +
            "      \"claim\": {" +
            "        \"uri\": \"http://wso2.org/claims/username\"" +
            "      }," +
            "      \"mandatory\": false" +
            "    }" +
            "  ]," +
            "  \"subject\": {" +
            "    \"claim\": {" +
            "      \"uri\": \"http://wso2.org/claims/username\"" +
            "    }," +
            "    \"includeUserDomain\": false," +
            "    \"includeTenantDomain\": false," +
            "    \"useMappedLocalSubject\": false" +
            "  }," +
            "  \"role\": {" +
            "    \"mappings\": [" +
            "      {" +
            "        \"localRole\": \"admin\"," +
            "        \"applicationRole\": \"Administrator\"" +
            "      }" +
            "    ]," +
            "    \"includeUserDomain\": true," +
            "    \"claim\": {" +
            "      \"uri\": \"http://wso2.org/claims/username\"" +
            "    }" +
            "  }" +
            "}," +
            "\"inboundProtocolConfiguration\": {" +
            "  \"oidc\": {" +
            "    \"clientId\": \"rMfbPgCi5oWljNhv8c4Pugfuo8Aa\"," +
            "    \"clientSecret\": \"MkHGGiTdAPfTyUKfXLdyOwelMywt\"," +
            "    \"grantTypes\": [" +
            "      \"authorization_code\"," +
            "      \"password\"" +
            "    ]," +
            "    \"callbackURLs\": [" +
            "      \"regexp=(https://app.example.com/callback1|https://app.example.com/callback2)\"" +
            "    ]," +
            "    \"allowedOrigins\": [" +
            "      \"https://app.example.com\"" +
            "    ]," +
            "    \"publicClient\": false," +
            "    \"pkce\": {" +
            "      \"mandatory\": false," +
            "      \"supportPlainTransformAlgorithm\": true" +
            "    }," +
            "    \"accessToken\": {" +
            "      \"type\": \"JWT\"," +
            "      \"userAccessTokenExpiryInSeconds\": 3600," +
            "      \"applicationAccessTokenExpiryInSeconds\": 3600," +
            "      \"bindingType\": \"cookie\"," +
            "      \"revokeTokensWhenIDPSessionTerminated\": true," +
            "      \"validateTokenBinding\": true" +
            "    }," +
            "    \"refreshToken\": {" +
            "      \"expiryInSeconds\": 86400," +
            "      \"renewRefreshToken\": true" +
            "    }," +
            "    \"idToken\": {" +
            "      \"expiryInSeconds\": 3600," +
            "      \"audience\": [" +
            "        \"http://idp.xyz.com\"," +
            "        \"http://idp.abc.com\"" +
            "      ]," +
            "      \"encryption\": {" +
            "        \"enabled\": false," +
            "        \"algorithm\": \"RSA-OAEP\"," +
            "        \"method\": \"A128CBC+HS256\"" +
            "      }" +
            "    }," +
            "    \"logout\": {" +
            "      \"backChannelLogoutUrl\": \"https://app.example.com/backchannel/callback\"," +
            "      \"frontChannelLogoutUrl\": \"https://app.example.com/frontchannel/callback\"" +
            "    }," +
            "    \"validateRequestObjectSignature\": false," +
            "    \"scopeValidators\": [" +
            "      \"Role based scope validator\"" +
            "    ]" +
            "  }" +
            "}," +
            "\"authenticationSequence\": {" +
            "  \"type\": \"DEFAULT\"," +
            "  \"steps\": [" +
            "    {" +
            "      \"id\": 1," +
            "      \"options\": [" +
            "        {" +
            "          \"idp\": \"LOCAL\"," +
            "          \"authenticator\": \"basic\"" +
            "        }" +
            "      ]" +
            "    }" +
            "  ]," +
            "  \"script\": \"string\"," +
            "  \"subjectStepId\": 1," +
            "  \"attributeStepId\": 1" +
            "}," +
            "\"advancedConfigurations\": {" +
            "  \"saas\": false," +
            "  \"discoverableByEndUsers\": false," +
            "  \"certificate\": {" +
            "    \"type\": \"string\"," +
            "    \"value\": \"string\"" +
            "  }," +
            "  \"skipLoginConsent\": false," +
            "  \"skipLogoutConsent\": false," +
            "  \"useExternalConsentPage\": false," +
            "  \"returnAuthenticatedIdpList\": false," +
            "  \"enableAuthorization\": true" +
            "}" +
            "}";
}


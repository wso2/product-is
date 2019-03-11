/*
 *  Copyright (c)  WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
 *
 *  WSO2 Inc. licenses this file to you under the Apache License,
 *  Version 2.0 (the "License"); you may not use this file except
 *  in compliance with the License.
 *  You may obtain a copy of the License at
 *
 *    http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */

package org.wso2.identity.integration.test.utils;

/**
 * OAuth2 constant
 */
public final class OAuth2Constant {

    public static final String OAUTH2_GRANT_TYPE_IMPLICIT = "token";
    public static final String OAUTH2_GRANT_TYPE_CODE = "code";
    public static final String OAUTH2_GRANT_TYPE_CLIENT_CREDENTIALS = "client_credentials";
    public static final String OAUTH2_GRANT_TYPE_RESOURCE_OWNER = "password";
    public static final String OAUTH2_GRANT_TYPE_REFRESH_TOKEN = "refresh_token";

    public static final String OAUTH2_GRANT_TYPE_AUTHORIZATION_CODE = "authorization_code";
    public static final String AUTHORIZATION_HEADER = "Authorization";

    public final static String ACCESS_TOKEN = "access_token";
    public static final String REFRESH_TOKEN = "refresh_token";
    public static final String ID_TOKEN = "id_token";
    public final static String ACCESS_TOKEN_TYPE = "bearer";
    public final static String OAUTH_VERSION_2 = "OAuth-2.0";
    public static final String OAUTH_2 = "oauth2";
    public final static String REDIRECT_LOCATIONS = "http.protocol.redirect-locations";
    public final static String ACCESS_TOKEN_ENDPOINT = "https://localhost:9853/oauth2/token";
    public final static String TOKEN_REVOKE_ENDPOINT = "https://localhost:9853/oauth2/revoke";
    public final static String USER_INFO_ENDPOINT = "https://localhost:9853/oauth2/userinfo?schema=openid";
    public final static String AUTHTOKEN_VALIDATE_SERVICE = CommonConstants.DEFAULT_SERVICE_URL + "OAuth2TokenValidationService";
    public final static String COMMON_AUTH_URL = "https://localhost:9853/commonauth";
    public final static String USER_AGENT = "Apache-HttpClient/4.2.5 (java 1.6)";
    public final static String AUTHORIZE_ENDPOINT_URL = "https://localhost:9853/oauth2/authorize";
    public final static String APPROVAL_URL = "https://localhost:9853/oauth2/authorize";
    public final static String AUTHORIZE_PARAM = "Authorize";
    public final static String TOKEN_VALIDATION_SERVICE_URL = CommonConstants.DEFAULT_SERVICE_URL + "OAuth2TokenValidationService";
    public final static String HTTP_RESPONSE_HEADER_LOCATION = "location";
    public final static String OAUTH2_SCOPE_OPENID = "openid";
    public final static String OAUTH2_SCOPE_EMAIL = "email";
    public final static String OAUTH2_SCOPE_DEFAULT = "";
    public final static String OAUTH_APPLICATION_NAME = "oauthTestApplication";
    public static final String UNSUPPORTED_GRANT_TYPE = "unsupported_grant_type";
    public static final String UNAUTHORIZED_CLIENT = "unauthorized_client";
    public final static String OAUTH_APPLICATION_STATE_REVOKED = "REVOKED";
    public static final String OAUTH_CONSUMER_SECRET = "oauthConsumerSecret";
    public static final String OAUTH_OIDC_REQUEST = "request";

    public final static String CALLBACK_URL = "http://localhost:8490/playground2/oauth2client";
    public final static String CALLBACK_URL_REGEXP = "regexp=http:\\/\\/localhost:8490\\/playground2\\/oauth2client[\\?]?((\\w+)=(\\w+)&?)+";
    public final static String CALLBACK_REQUEST_URL_WITH_PARAMS = "http://localhost:8490/playground2/oauth2client?param=value&param2=value2";
    public final static String AUTHORIZED_USER_URL = "http://localhost:8490/playground2/oauth2-authorize-user.jsp";
    public final static String AUTHORIZED_URL = "http://localhost:8490/playground2/oauth2.jsp";
    public final static String GET_ACCESS_TOKEN_URL = "http://localhost:8490/playground2/oauth2-get-access-token.jsp";
    public final static String ACCESS_RESOURCES_URL = "http://localhost:8490/playground2/oauth2-access-resource.jsp";
    public final static String PLAYGROUND_APP_CONTEXT_ROOT = "/playground2";
    public static final String GRANT_TYPE_PLAYGROUND_NAME = "grantType";
    public static final String CONSUMER_KEY_PLAYGROUND_NAME = "consumerKey";
    public static final String CALLBACKURL_PLAYGROUND_NAME = "callbackurl";
    public static final String AUTHORIZE_ENDPOINT_PLAYGROUND_NAME = "authorizeEndpoint";
    public static final String AUTHORIZE_PLAYGROUND_NAME = "authorize";
    public static final String SCOPE_PLAYGROUND_NAME = "scope";

    public final static String WSO2_CLAIM_DIALECT_ROLE="http://wso2.org/claims/role";
    public static final String GRANT_TYPE_NAME = "grant_type";
    public static final String AUTHORIZATION_CODE_NAME = "code";
    public static final String REDIRECT_URI_NAME = "redirect_uri";
    public static final String BASIC_HEADER = "Basic";
    public static final String INVALID_GRANT_ERROR = "invalid_grant";
    public static final String SESSION_DATA_KEY_CONSENT = "sessionDataKeyConsent";
    public static final String SESSION_DATA_KEY = "sessionDataKey";
    public static final String INVALID_CLIENT = "invalid_client";
    public static final String TRAVELOCITY_APP_CONTEXT_ROOT = "/travelocity.com";
    public static final String OAUTH2_GRANT_TYPE_SAML2_BEARER = "urn:ietf:params:oauth:grant-type:saml2-bearer";

    // OAuth 2 request parameters.
    public static final String OAUTH2_RESPONSE_TYPE = "response_type";
    public static final String OAUTH2_CLIENT_ID = "client_id";
    public static final String OAUTH2_REDIRECT_URI = "redirect_uri";
    public static final String OAUTH2_SCOPE = "scope";
    public static final String OAUTH2_NONCE = "nonce";
    public static final String INTRO_SPEC_ENDPOINT = "https://localhost:9853/oauth2/introspect";

    public static final class PlaygroundAppPaths {

        public static final String callBackPath = "/oauth2client";
        public static final String homePagePath = "/index.jsp";
        public static final String appResetPath = "/oauth2.jsp?reset=true";
        public static final String appAuthorizePath = "/oauth2.jsp";
        public static final String appUserAuthorizePath = "/oauth2-authorize-user.jsp";
        public static final String accessTokenRequestPath = "/oauth2-get-access-token.jsp";
        public static final String resourceAccessPath = "/oauth2-access-resource.jsp";
    }

    private OAuth2Constant() {
	}
}

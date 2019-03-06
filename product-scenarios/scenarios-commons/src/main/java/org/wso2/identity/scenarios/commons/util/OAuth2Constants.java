/*
 * Copyright (c) 2019, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.wso2.identity.scenarios.commons.util;

public class OAuth2Constants {

    private OAuth2Constants() {

    }

    public static class GrantTypes {

        public static final String AUTHORIZATION_CODE = "authorization_code";

        public static final String IMPLICIT = "implicit";

        public static final String PASSWORD = "password";

        public static final String CLIENT_CREDENTIALS = "client_credentials";

        public static final String REFRESH_TOKEN = "refresh_token";
    }

    public static class ResponseTypes {

        public static final String CODE = "code";

        public static final String TOKEN = "token";
    }

    public static class TokenTypes {

        public static final String BEARER = "Bearer";
    }

    public static class IntrospectResponseElements {

        public static final String ACTIVE = "active";
    }

    public static class TokenResponseElements {

        public static final String ACCESS_TOKEN = "access_token";

        public static final String REFRESH_TOKEN = "refresh_token";

        public static final String TOKEN_TYPE = "token_type";

        public static final String EXPIRES_IN = "expires_in";
    }

    public static class RequestParams {

        public static final String CLIENT_ID = "client_id";

        public static final String REDIRECT_URI = "redirect_uri";

        public static final String RESPONSE_TYPE = "response_type";

        public static final String SCOPE = "scope";

        public static final String GRANT_TYPE = "grant_type";

        public static final String CODE = "code";

        public static final String CONSENT = "consent";

        public static final String TOKEN = "token";

        public static final String USERNAME = "username";

        public static final String PASSWORD = "password";

        public static final String REFRESH_TOKEN = "refresh_token";
    }

    public static class DCRRequestElements {

        public static final String REDIRECT_URIS = "redirect_uris";

        public static final String CLIENT_NAME = "client_name";

        public static final String GRANT_TYPES = "grant_types";

        public static final String APPLICATION_TYPE = "application_type";

        public static final String JWKS_URI = "jwks_uri";

        public static final String URL = "url";

        public static final String CLIENT_ID = "ext_param_client_id";

        public static final String CLIENT_SECRET = "ext_param_client_secret";

        public static final String CONTACTS = "contacts";

        public static final String POST_LOGOUT_REDIRECT_URIS = "post_logout_redirect_uris";

        public static final String REQUEST_URIS = "request_uris";

        public static final String RESPONSE_TYPES = "response_types";

        public static final String TOKEN_TYPE = "token_type_extension";

        public static final String SP_TEMPLATE_NAME = "ext_param_sp_template";
    }

    public static class DCRUpdateRequestElements {

        public static final String REDIRECT_URIS = "redirect_uris";

        public static final String CLIENT_NAME = "client_name";

        public static final String GRANT_TYPES = "grant_types";

        public static final String TOKEN_TYPE = "token_type_extension";

        public static final String CLIENT_ID = "client_id";

        public static final String CLIENT_SECRET = "client_secret";
    }

    public static class DCRResponseElements {

        public static final String CLIENT_NAME = "client_name";

        public static final String CLIENT_ID = "client_id";

        public static final String CLIENT_SECRET = "client_secret";

        public static final String REDIRECT_URIS = "redirect_uris";
    }

    public static class PKCERequestElements {

        public static final String CODE_VERIFIER = "code_verifier";

        public static final String CODE_CHALLENGE = "code_challenge";

        public static final String CODE_CHALLENGE_METHOD = "code_challenge_method";
    }
}

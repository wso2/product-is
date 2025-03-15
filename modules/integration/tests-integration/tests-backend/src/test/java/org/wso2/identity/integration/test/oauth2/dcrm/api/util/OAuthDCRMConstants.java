/*
 * Copyright (c) 2017, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
 *
 * WSO2 Inc. licenses this file to you under the Apache License,
 * Version 2.0 (the "License"); you may not use this file except
 * in compliance with the License.
 * you may obtain a copy of the License at
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */
package org.wso2.identity.integration.test.oauth2.dcrm.api.util;

public class OAuthDCRMConstants {
    public static final String DCR_ENDPOINT_HOST_PART = "https://localhost:9853/api/identity/oauth2/dcr/v1.1/register/";
    public static final String DCR_ENDPOINT_PATH_PART = "/api/identity/oauth2/dcr/v1.1/register/";
    public static final String CLIENT_NAME = "client_name";
    public static final String GRANT_TYPES = "grant_types";
    public static final String REDIRECT_URIS = "redirect_uris";
    public static final String CLIENT_ID = "client_id";
    public static final String CLIENT_SECRET = "client_secret";
    public static final String ERROR = "error";
    public static final String ERROR_DESCRIPTION = "error_description";

    public static final String BACKEND_FAILED = "backend_failed";

    public static final String AUTHORIZATION = "Basic YWRtaW46YWRtaW4=";
    public static final String CONTENT_TYPE = "application/json";
    public static final String ACCEPT = "application/json";

    public static final String INVALID_CLIENT_ID = "invalid_client_id";
    public static final String INVALID_CLIENT_SECRET = "invalid_client_secret";

    public static final String APPLICATION_NAME = "TestApp";
    public static final String REDIRECT_URI = "http://TestApp.com";

    public static final String OAUTH_VERSION = "OAuth-2.0";

    public static final String GRANT_TYPE_IMPLICIT = "implicit";
    public static final String GRANT_TYPE_AUTHORIZATION_CODE = "authorization_code";
    public static final String GRANT_TYPE_PASSWORD = "password";

    public static final String TOKEN_AUTH_METHOD =  "token_endpoint_auth_method";
    public static final String TOKEN_AUTH_SIGNATURE_ALGORITHM = "token_endpoint_auth_signing_alg";
    public static final String SECTOR_IDENTIFIER_URI = "sector_identifier_uri";
    public static final String ID_TOKEN_SIGNATURE_ALGORITHM = "id_token_signed_response_alg";
    public static final String ID_TOKEN_ENCRYPTION_ALGORITHM = "id_token_encrypted_response_alg";
    public static final String ID_TOKEN_ENCRYPTION_METHOD = "id_token_encrypted_response_enc";
    public static final String REQUEST_OBJECT_SIGNATURE_ALGORITHM = "request_object_signing_alg";
    public static final String TLS_SUBJECT_DN = "tls_client_auth_subject_dn";
    public static final String IS_PUSH_AUTH = "require_pushed_authorization_requests";
    public static final String IS_SIGNED_REQUEST_OBJECT = "require_signed_request_object";
    public static final String IS_CERTIFICATE_BOUND_ACCESS_TOKEN = "tls_client_certificate_bound_access_tokens";
    public static final String SUBJECT_TYPE = "subject_type";
    public static final String REQUEST_OBJECT_ENCRYPTION_ALGORITHM = "request_object_encryption_alg";
    public static final String REQUEST_OBJECT_ENCRYPTION_METHOD = "request_object_encryption_enc";
    public static final String JWKS_URI = "jwks_uri";
}

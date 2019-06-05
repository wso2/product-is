/*
 * Copyright (c) 2018, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
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

/**
 * Common constance holder class.
 */
public class Constants {

    private Constants() {

    }


    public static final String IS_HTTPS_URL = "ISHttpsUrl";
    public static final String IS_SAMPLES_HTTP_URL = "ISSamplesHttpUrl";
    public static final String MGT_CONSOLE_URL = "MgtConsoleUrl";
    public static final String CARBON_SERVER_URL = "CarbonServerUrl";
    public static final String IS_HTTP_URL = "ISHttpUrl";

    public static final String APPROVE_ONCE = "approve";
    public static final String APPROVE_ALWAYS = "approveAlways";
    public static final String BASIC = "Basic";
    public static final String CLIENT_NAME = "client_name";
    public static final String CONTENT_TYPE_APPLICATION_JSON = "application/json";
    public static final String CONTENT_TYPE_APPLICATION_FORM = "application/x-www-form-urlencoded";
    public static final String DENY = "deny";
    public static final String GRANT_TYPE_AUTHORIZATION_CODE = "authorization_code";
    public static final String PARAM_CLIENT_ID = "client_id";
    public static final String PARAM_CLIENT_SECRET = "client_secret";
    public static final String PARAM_CODE = "code";
    public static final String PARAM_CONSENT = "consent";
    public static final String PARAM_GRANT_TYPE = "grant_type";
    public static final String PARAM_GRANT_TYPES = "grant_types";
    public static final String PARAM_PASSWORD = "password";
    public static final String PARAM_REDIRECT_URI = "redirect_uri";
    public static final String PARAM_REDIRECT_URIS = "redirect_uris";
    public static final String PARAM_RESPONSE_TYPE = "response_type";
    public static final String PARAM_MANDATORY_CLAIMS = "mandatoryClaims";
    public static final String PARAM_REQUESTED_CLAIMS = "requestedClaims";
    public static final String PARAM_SCOPE = "scope";
    public static final String PARAM_SESSION_DATA_KEY = "sessionDataKey";
    public static final String PARAM_SESSION_DATA_KEY_CONSENT = "sessionDataKeyConsent";
    public static final String PARAM_ACCESS_TOKEN = "access_token";
    public static final String PARAM_USERNAME = "username";
    public static final String HTTP_RESPONSE_HEADER_LOCATION = "location";
    public static final String DCR_REGISTER_URI_CONTEXT = "/api/identity/oauth2/dcr/v1.1/register";
    public static final String OAUTH_AUTHORIZE_URI_CONTEXT = "/oauth2/authorize";
    public static final String OAUTH_TOKEN_URI_CONTEXT = "/oauth2/token";
    public static final String COMMONAUTH_URI_CONTEXT = "/commonauth";
    public static final String SCIM1_USERS_ENDPOINT = "wso2/scim";
    public static final String SCIM2_USERS_ENDPOINT = "scim2";
    public static final String SCIM_ENDPOINT_USER = "Users";
    public static final String SAML_REQUEST_PARAM = "SAMLRequest";
    public static final String SAML_RESPONSE_PARAM = "SAMLResponse";
    public static final String TOCOMMONAUTH = "tocommonauth";
    public static final String COOKIE = "Cookie";
    public static final String HEADER_SET_COOKIE = "Set-Cookie";
    public static final String HEADER_USER_AGENT = "User-Agent";
    public static final String RESPONSE_TAG_NAME = "Response";
    public static final String ASSERTION_TAG_NAME = "Assertion";
    public static final String AUTHN_CONTEXT_CLASS_REF = "AuthnContextClassRef";
    public static final String AUTHN_REQUEST = "AuthnRequest";
    public static final String INBOUND_AUTH_TYPE_SAML = "samlsso";
    public static final String INBOUND_AUTH_TYPE_OAUTH2 = "oauth2";
    public static final String ISSUER = "Issuer";
    public static final String NAMESPACE_PREFIX = "samlp";
    public static final String PASSWORD_PROTECTED_TRANSPORT_CLASS =
            "urn:oasis:names:tc:SAML:2.0:ac:classes:PasswordProtectedTransport";
    public static final String SAML_ASSERTION_URN = "urn:oasis:names:tc:SAML:2.0:assertion";
    public static final String SAML_PROTOCOL_URN = "urn:oasis:names:tc:SAML:2.0:protocol";
    public static final String XML_DOCUMENT_BUILDER_FACTORY =
            "javax.xml.parsers.DocumentBuilderFactory";
    public static final String XML_DOCUMENT_BUILDER_FACTORY_IMPL =
            "org.apache.xerces.jaxp.DocumentBuilderFactoryImpl";
    public static final String MULTI_ATTRIBUTE_SEPARATOR = ",";
    public static final String TENANT_DOMAIN = "tenantDomain";
    public static final String PASSWORD = "password";
    public static final String DEFAULT_PROFILE_NAME = "default";
    public static final int DEFAULT_SOCKET_TIMEOUT_IN_SECONDS = 30;

    /**
     * SCIM Endpoints.
     */
    public static class SCIMEndpoints {

        public static final String SCIM1_ENDPOINT = "wso2/scim";
        public static final String SCIM2_ENDPOINT = "scim2";
        public static final String SCIM_ENDPOINT_BULK = "Bulk";
        public static final String SCIM_ENDPOINT_USER = "Users";
        public static final String SCIM_ANONYMOUS_USER = "Me";

    }

    public static class ClaimURIs {

        public static final String EMAIL_CLAIM_URI = "http://wso2.org/claims/emailaddress";
        public static final String FIRST_NAME_CLAIM_URI = "http://wso2.org/claims/givenname";
        public static final String LAST_NAME_CLAIM_URI = "http://wso2.org/claims/lastname";
        public static final String USER_NAME_CLAIM_URI = "http://wso2.org/claims/username";
        public static final String ROLE_CLAIM_URI = "http://wso2.org/claims/role";
    }

    public static class SystemProperties {
        public static final String TEST_USERS_LOCATION = "test.users.location";
    }

    public static class SigningProperties {
        public static final String XML_DIGEST_ALGORITHM_SHA1 = "http://www.w3.org/2000/09/xmldsig#sha1";
        public static final String SIGNATURE_ALGORITHM_SHA1_RSA = "SHA1withRSA";

    }

    public static class HttpBinding {

        public static final String HTTP_REDIRECT = "HTTP-Redirect";
        public static final String HTTP_POST = "HTTP-POST";

    }

}

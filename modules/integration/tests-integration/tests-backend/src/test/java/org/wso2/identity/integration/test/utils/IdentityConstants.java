package org.wso2.identity.integration.test.utils;

public class IdentityConstants {

    public static class Authenticator {
        public static class SAML2SSO {
            public static final String NAME = "samlsso";
            public static final String FED_AUTH_NAME = "SAMLSSOAuthenticator";
            public static final String IDP_ENTITY_ID = "IdPEntityId";
            public static final String SP_ENTITY_ID = "SPEntityId";
            public static final String SSO_URL = "SSOUrl";
            public static final String IS_AUTHN_REQ_SIGNED = "ISAuthnReqSigned";
            public static final String IS_ENABLE_ASSERTION_ENCRYPTION = "IsAssertionEncrypted";
            public static final String IS_ENABLE_ASSERTION_SIGNING = "isAssertionSigned";
            public static final String IS_LOGOUT_ENABLED = "IsLogoutEnabled";
            public static final String LOGOUT_REQ_URL = "LogoutReqUrl";
            public static final String IS_LOGOUT_REQ_SIGNED = "IsLogoutReqSigned";
            public static final String IS_AUTHN_RESP_SIGNED = "IsAuthnRespSigned";
            public static final String IS_USER_ID_IN_CLAIMS = "IsUserIdInClaims";
            public static final String RESPONSE_AUTHN_CONTEXT_CLASS_REF = "ResponseAuthnContextClassRef";
        }

         public static class OAuth2 {

            public static final String NAME = "oauth2";
            public static final String CLIENT_ID = "ClientId";
            public static final String CLIENT_SECRET = "ClientSecret";
            public static final String OAUTH2_AUTHZ_URL = "OAuth2AuthzEPUrl";
            public static final String OAUTH2_TOKEN_URL = "OAuth2TokenEPUrl";
            public static final String OAUTH2_REVOKE_URL = "OAuth2RevokeEPUrl";
            public static final String OAUTH2_INTROSPECT_URL = "OAuth2IntrospectEPUrl";
            public static final String OAUTH2_USER_INFO_EP_URL = "OAuth2UserInfoEPUrl";
            public static final String CALLBACK_URL = "callbackUrl";
            public static final String OAUTH_CONSUMER_SECRET = "oauthConsumerSecret";
            public static final String OIDC_WEB_FINGER_EP_URL = "OIDCWebFingerEPUrl";
            public static final String OAUTH2_DCR_EP_URL = "OAuth2DCREPUrl";
            public static final String OAUTH2_JWKS_EP_URL = "OAuth2JWKSPage";
            public static final String OIDC_DISCOVERY_EP_URL = "OIDCDiscoveryEPUrl";
        }

        public static class OIDC extends OAuth2 {

            public static final String IDP_NAME = "IdPName";
            public static final String NAME = "openidconnect";
            public static final String USER_INFO_URL = "UserInfoUrl";
            public static final String OIDC_CHECK_SESSION_URL = "OIDCCheckSessionEPUrl";
            public static final String OIDC_LOGOUT_URL = "OIDCLogoutEPUrl";
            public static final String IS_USER_ID_IN_CLAIMS = "IsUserIdInClaims";
            public static final String IS_BASIC_AUTH_ENABLED = "IsBasicAuthEnabled";
        }
    }

    public static enum ServiceClientType {
        APPLICATION_MANAGEMENT,
        IDENTITY_PROVIDER_MGT,
        SAML_SSO_CONFIG,
        OAUTH_ADMIN,
    }
}

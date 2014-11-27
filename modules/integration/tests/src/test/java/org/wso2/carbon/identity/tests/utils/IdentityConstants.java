package org.wso2.carbon.identity.tests.utils;

public class IdentityConstants {

    public static class Authenticator {
        public static class SAML2SSO {
            public static final java.lang.String NAME = "samlsso";
            public static final java.lang.String FED_AUTH_NAME = "SAMLSSOAuthenticator";
            public static final java.lang.String IDP_ENTITY_ID = "IdPEntityId";
            public static final java.lang.String SP_ENTITY_ID = "SPEntityId";
            public static final java.lang.String SSO_URL = "SSOUrl";
            public static final java.lang.String IS_AUTHN_REQ_SIGNED = "ISAuthnReqSigned";
            public static final java.lang.String IS_ENABLE_ASSERTION_ENCRYPTION = "IsAssertionEncrypted";
            public static final java.lang.String IS_ENABLE_ASSERTION_SIGNING = "isAssertionSigned";
            public static final java.lang.String IS_LOGOUT_ENABLED = "IsLogoutEnabled";
            public static final java.lang.String LOGOUT_REQ_URL = "LogoutReqUrl";
            public static final java.lang.String IS_LOGOUT_REQ_SIGNED = "IsLogoutReqSigned";
            public static final java.lang.String IS_AUTHN_RESP_SIGNED = "IsAuthnRespSigned";
            public static final java.lang.String IS_USER_ID_IN_CLAIMS = "IsUserIdInClaims";
        }
    }

    public static enum ServiceClientType {
        APPLICATION_MANAGEMENT,
        IDENTITY_PROVIDER_MGT,
        SAML_SSO_CONFIG,
    }
}

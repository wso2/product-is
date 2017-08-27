package org.wso2.sample.identity.oauth2;

public final class OAuth2Constants {

    // Oauth response parameters and session attributes
    public static final String SCOPE = "scope";
    public static final String ERROR = "error";
    public static final String ACCESS_TOKEN = "access_token";
    public static final String SESSION_STATE = "session_state";

    // oauth scopes
    public static final String SCOPE_OPENID = "openid";

    // oauth grant type constants
    public static final String OAUTH2_GRANT_TYPE_CODE = "code";
    public static final String OAUTH2_GRANT_TYPE_IMPLICIT = "token";
    public static final String OAUTH2_GRANT_TYPE_RESOURCE_OWNER = "password";
    public static final String OAUTH2_GRANT_TYPE_CLIENT_CREDENTIALS = "client_credentials";

    // application specific request parameters
    public static final String RESET_PARAM = "reset";
    public static final String RESOURCE_OWNER_PARAM = "recowner";
    public static final String RESOURCE_OWNER_PASSWORD_PARAM = "recpassword";

    // application specific request parameters and session attributes
    public static final String CONSUMER_KEY = "consumerKey";
    public static final String CONSUMER_SECRET = "consumerSecret";
    public static final String CALL_BACK_URL = "callbackurl";
    public static final String OAUTH2_GRANT_TYPE = "grantType";
    public static final String OAUTH2_AUTHZ_ENDPOINT = "authorizeEndpoint";
    public static final String OAUTH2_ACCESS_ENDPOINT = "accessEndpoint";
    public static final String OIDC_LOGOUT_ENDPOINT = "logoutEndpoint";
    public static final String OIDC_SESSION_IFRAME_ENDPOINT = "sessionIFrameEndpoint";

    // application specific session attributes
    public static final String CODE = "code";
    public static final String ID_TOKEN = "id_token";
    public static final String RESULT = "result";
    public static final String TOKEN_VALIDATION = "valid";

    // request headers
    public static final String REFERER = "referer";

    //OAuth 2.0 PKCE Constants
    public static final String OAUTH2_PKCE_CODE_VERIFIER = "code_verifier";
    public static final String OAUTH2_PKCE_CODE_CHALLENGE = "code_challenge";
    public static final String OAUTH2_PKCE_CODE_CHALLENGE_METHOD = "code_challenge_method";
    public static final String OAUTH2_USE_PKCE = "use_pkce";

    // Form post constants
    public static final String OAUTH2_FORM_POST = "form_post";
    public static final String OAUTH2_RESPONSE_MODE = "response_mode";

    //OAuth DCRM specific constants
    public static final String CREATE = "create";
    public static final String READ = "read";
    public static final String UPDATE = "update";
    public static final String DELETE = "delete";
    public static final String DCRM_METHOD = "dcrmMethod";
    public static final String CLIENT_ID = "clientId";
    public static final String CLIENT_SECRET = "clientSecret";
    public static final String CLIENT_NAME = "clientName";
    public static final String GRANT_TYPES = "grantTypes";
    public static final String REDIRECT_URIS = "redirectUris";
    public static final String CLIENT_CONFIGURATION_ENDPOINT = "https://localhost:9443/identity/register/";
    public static final String AUTHORIZATION = "Basic YWRtaW46YWRtaW4=";
    public static final String CONTENT_TYPE = "application/json";

    public static class DCRMMetaData {
        public static final String CLIENT_NAME = "client_name";
        public static final String CLIENT_ID = "client_id";
        public static final String CLIENT_SECRET = "client_secret";
        public static final String GRANT_TYPES = "grant_types";
        public static final String REDIRECT_URIS = "redirect_uris";
    }
}

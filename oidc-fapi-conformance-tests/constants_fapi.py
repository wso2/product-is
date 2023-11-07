
ALIAS = "fapi-wso2is"

BASE_URL = "https://iam:9443"
DCR_ENDPOINT = BASE_URL + "/api/identity/oauth2/dcr/v1.1/register"
TOKEN_ENDPOINT = BASE_URL + "/oauth2/token"
APPLICATION_ENDPOINT = BASE_URL + "/api/server/v1/applications"

SCOPES = "internal_user_mgt_update internal_application_mgt_create internal_application_mgt_view internal_login " \
         "internal_claim_meta_update internal_application_mgt_update internal_scope_mgt_create"

HEADERS_WITH_AUTH = {'Content-Type': 'application/json', 'Connection': 'keep-alive',
               'Authorization': 'Basic YWRtaW46YWRtaW4='}

DCR_BODY = {
    "grant_types": ["client_credentials", "authorization_code", "refresh_token"],
    "backchannel_logout_uri": "https://www.google.com",
    "backchannel_logout_session_required": "true",
    "token_endpoint_auth_signing_alg" : "PS256",
    "id_token_signed_response_alg" : "PS256",
    "id_token_encrypted_response_alg" : "RSA-OAEP",
    "id_token_encrypted_response_enc" : "A128GCM",
    "request_object_signing_alg" : "PS256",
    "require_signed_request_object" : "true",
    "require_pushed_authorization_requests" : "false",
    "tls_client_certificate_bound_access_tokens":"true",
    "request_object_encryption_alg" : "RSA-OAEP",
    "request_object_encryption_enc" : "A128GCM",
}

APP_1_CLIENT_NAME = "a_fapi1"
APP_1_REDIRECT_URIS = ["https://localhost.emobix.co.uk:8443/test/a/fapi-wso2is/callback"]
APP_1_JWKS_URI = "https://www.jsonkeeper.com/b/VEF7"
APP_1_AUTH_METHOD = "private_key_jwt"
# If MTLS APP, use > tls_client_auth
APP_1_CLIENT_ID = "a_fapi1_client_id"
APP_1_CLIENT_SECRET = "a_fapi1_client_secret"

APP_2_CLIENT_NAME = "a_fapi2"
APP_2_REDIRECT_URIS = ["https://localhost.emobix.co.uk:8443/test/a/fapi-wso2is/callback?dummy1=lorem&dummy2=ipsum"]
APP_2_JWKS_URI = "https://www.jsonkeeper.com/b/F8CW"
APP_2_AUTH_METHOD = "private_key_jwt"
APP_2_CLIENT_ID = "a_fapi2_client_id"
APP_2_CLIENT_SECRET = "a_fapi2_client_secret"

ACR = {
        "authenticationSequence": {
            "attributeStepId": 1,
            "steps": [
                {
                    "id": 1,
                    "options": [
                        {
                            "authenticator": "BasicAuthenticator",
                            "idp": "LOCAL"
                        }
                    ]
                }
            ],
            "subjectStepId": 1,
            "type": "USER_DEFINED",
            "script": "var supportedAcrValues = ['acr1', 'urn:mace:incommon:iap:silver',];\n\nvar onLoginRequest = function(context) {\n    var selectedAcr = selectAcrFrom(context, supportedAcrValues);\n    Log.info('--------------- ACR selected: ' + selectedAcr);\n    context.selectedAcr = selectedAcr;\n    executeStep(1);\n};\n"
        }
}
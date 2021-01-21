ALIAS = "test"
DCR_ENDPOINT = "https://localhost:9443/api/identity/oauth2/dcr/v1.1/register"
TOKEN_ENDPOINT = "https://localhost:9443/oauth2/token"
DCR_CLIENT_ID = "oidc_test_clientid001"
DCR_CLIENT_SECRET = "oidc_test_client_secret001"
APPLICATION_ENDPOINT = "https://localhost:9443/api/server/v1/applications"
SCOPES = "internal_user_mgt_update internal_application_mgt_create internal_application_mgt_view internal_login " \
         "internal_claim_meta_update internal_application_mgt_update internal_scope_mgt_create"

DCR_HEADERS = {'Content-Type': 'application/json', 'Connection': 'keep-alive',
               'Authorization': 'Basic YWRtaW46YWRtaW4='}
DCR_BODY = {
    'client_name': 'python_script',
    "grant_types": ["password"],
    "ext_param_client_id": DCR_CLIENT_ID,
    "ext_param_client_secret": DCR_CLIENT_SECRET
}


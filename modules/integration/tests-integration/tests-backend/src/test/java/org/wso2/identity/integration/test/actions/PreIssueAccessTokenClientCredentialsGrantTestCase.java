package org.wso2.identity.integration.test.actions;

import com.nimbusds.jwt.JWTClaimsSet;
import com.nimbusds.jwt.SignedJWT;
import org.apache.commons.lang.ArrayUtils;
import org.json.JSONException;
import org.testng.Assert;
import org.testng.annotations.AfterClass;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;
import org.wso2.carbon.automation.engine.context.TestUserMode;
import org.wso2.identity.integration.test.mocks.MockServer;
import org.wso2.identity.integration.test.rest.api.server.action.management.v1.model.ActionModel;
import org.wso2.identity.integration.test.rest.api.server.action.management.v1.model.AuthenticationType;
import org.wso2.identity.integration.test.rest.api.server.action.management.v1.model.Endpoint;
import org.wso2.identity.integration.test.rest.api.server.application.management.v1.model.ApplicationResponseModel;
import org.wso2.identity.integration.test.rest.api.server.application.management.v1.model.OpenIDConnectConfiguration;
import org.wso2.identity.integration.test.rest.api.server.roles.v2.model.Audience;
import org.wso2.identity.integration.test.rest.api.server.roles.v2.model.Permission;
import org.wso2.identity.integration.test.rest.api.server.roles.v2.model.RoleV2;
import org.wso2.identity.integration.test.rest.api.user.common.model.Email;
import org.wso2.identity.integration.test.rest.api.user.common.model.ListObject;
import org.wso2.identity.integration.test.rest.api.user.common.model.Name;
import org.wso2.identity.integration.test.rest.api.user.common.model.PatchOperationRequestObject;
import org.wso2.identity.integration.test.rest.api.user.common.model.RoleItemAddGroupobj;
import org.wso2.identity.integration.test.rest.api.user.common.model.UserObject;
import org.wso2.identity.integration.test.restclients.ActionsRestClient;
import org.wso2.identity.integration.test.restclients.SCIM2RestClient;
import org.wso2.identity.integration.test.utils.CarbonUtils;
import org.wso2.identity.integration.test.utils.OAuth2Constant;

import java.io.IOException;
import java.text.ParseException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Integration test class for testing the pre issue access token flow with client credentials grant.
 * This test case extends {@link ActionsBaseTestCase} and focuses on scenarios related
 * to scopes and claims modifications through an external service.
 */
public class PreIssueAccessTokenClientCredentialsGrantTestCase extends ActionsBaseTestCase {

    private static final String USERS = "users";
    private static final String USERNAME_PROPERTY = "username";
    private static final String PASSWORD_PROPERTY = "password";
    private static final String TEST_USER = "test_user";
    private static final String ADMIN_WSO2 = "Admin@wso2";
    private static final String TEST_USER_GIVEN = "test_user_given";
    private static final String TEST_USER_GMAIL_COM = "test.user@gmail.com";
    private static final String EXTERNAL_SERVICE_NAME = "TestExternalService";
    private static final String EXTERNAL_SERVICE_URI = "http://localhost:8587/test/action";
    private static final String PRE_ISSUE_ACCESS_TOKEN_API_PATH = "preIssueAccessToken";

    private static final String CLIENT_CREDENTIALS_GRANT_TYPE = "client_credentials";
    private static final String APPLICATION_AUDIENCE = "APPLICATION";
    private static final String TEST_ROLE_APPLICATION = "test_role_application";

    private static final String INTERNAL_ACTION_MANAGEMENT_VIEW = "internal_action_mgt_view";
    private static final String INTERNAL_ACTION_MANAGEMENT_CREATE = "internal_action_mgt_create";
    private static final String INTERNAL_ACTION_MANAGEMENT_UPDATE = "internal_action_mgt_update";
    private static final String INTERNAL_ACTION_MANAGEMENT_DELETE = "internal_action_mgt_delete";
    private static final String INTERNAL_ORG_USER_MANAGEMENT_LIST = "internal_org_user_mgt_list";
    private static final String INTERNAL_ORG_USER_MANAGEMENT_VIEW = "internal_org_user_mgt_view";
    private static final String INTERNAL_ORG_USER_MANAGEMENT_CREATE = "internal_org_user_mgt_create";
    private static final String INTERNAL_ORG_USER_MANAGEMENT_UPDATE = "internal_org_user_mgt_update";
    private static final String INTERNAL_ORG_USER_MANAGEMENT_DELETE = "internal_org_user_mgt_delete";
    private static final String INTERNAL_APPLICATION_MANAGEMENT_VIEW = "internal_application_mgt_view";
    private static final String INTERNAL_APPLICATION_MANAGEMENT_UPDATE = "internal_application_mgt_update";
    private static final String INTERNAL_API_RESOURCE_VIEW = "internal_api_resource_view";
    private static final String INTERNAL_API_RESOURCE_CREATE = "internal_api_resource_create";
    private static final String CUSTOM_SCOPE_1 = "test_custom_scope_1";
    private static final String CUSTOM_SCOPE_2 = "test_custom_scope_2";
    private static final String CUSTOM_SCOPE_3 = "test_custom_scope_3";
    private static final String NEW_SCOPE_1 = "new_test_custom_scope_1";
    private static final String NEW_SCOPE_2 = "new_test_custom_scope_2";
    private static final String NEW_SCOPE_3 = "new_test_custom_scope_3";
    private static final String NEW_SCOPE_4 = "replaced_scope";

    private static final String SCIM2_USERS_API = "/o/scim2/Users";
    private static final String ACTIONS_API = "/api/server/v1/actions";
    private static final String APPLICATION_MANAGEMENT_API = "/api/server/v1/applications";
    private static final String API_RESOURCE_MANAGEMENT_API = "/api/server/v1/api-resources";
    private static final String MOCK_SERVER_ENDPOINT = "/test/action";

    private SCIM2RestClient scim2RestClient;
    private String accessToken;
    private String clientId;
    private String actionId;
    private String applicationId;
    private String domainAPIId;
    private String userId;
    private String roleId;
    private JWTClaimsSet jwtClaims;

    /**
     * Initializes Test environment and sets up necessary configurations.
     *
     * @throws Exception If an error occurs during initialization
     */
    @BeforeClass(alwaysRun = true)
    public void testInit() throws Exception {

        super.init(TestUserMode.TENANT_USER);

        scim2RestClient = new SCIM2RestClient(serverURL, tenantInfo);
        restClient = new ActionsRestClient(serverURL, tenantInfo);
        // TODO: Review if ActionsRestClient should be instantiated, or if the superclass initialization is sufficient

        List<String> customScopes = Arrays.asList(CUSTOM_SCOPE_1, CUSTOM_SCOPE_2, CUSTOM_SCOPE_3);

        ApplicationResponseModel application = addApplicationWithGrantType(CLIENT_CREDENTIALS_GRANT_TYPE);
        applicationId = application.getId();
        if (!CarbonUtils.isLegacyAuthzRuntimeEnabled()) {
            authorizeSystemAPIs(applicationId, new ArrayList<>(Arrays.asList(SCIM2_USERS_API, ACTIONS_API,
                    APPLICATION_MANAGEMENT_API, API_RESOURCE_MANAGEMENT_API)));
        }
        domainAPIId = createDomainAPI(EXTERNAL_SERVICE_NAME, EXTERNAL_SERVICE_URI, customScopes);
        authorizeDomainAPIs(applicationId, domainAPIId, customScopes);

        addUserWithRole(applicationId, customScopes);

        MockServer.createMockServer(MOCK_SERVER_ENDPOINT);
        actionId = createPreIssueAccessTokenAction();

        accessToken = retrieveAccessToken(application.getId(), customScopes);
        jwtClaims = extractJwtClaims(accessToken);
    }

    @AfterClass(alwaysRun = true)
    public void atEnd() throws Exception {

        restClient = null;
        deleteApp(applicationId);
        deleteDomainAPI(domainAPIId);
        scim2RestClient.deleteUser(userId);
        deleteRole(roleId);
        scim2RestClient = null;
        MockServer.shutDownMockServer();
        deleteAction(PRE_ISSUE_ACCESS_TOKEN_API_PATH, actionId);
        accessToken = null;
        jwtClaims = null;
    }

    @Test(groups = "wso2.is", description = "Verify the presence of the updated scopes in the access token")
    public void testTokenScopeOperations() throws Exception {

        String[] scopes = jwtClaims.getStringClaim("scope").split("\\s+");

        Assert.assertTrue(ArrayUtils.contains(scopes, NEW_SCOPE_1));
        Assert.assertTrue(ArrayUtils.contains(scopes, NEW_SCOPE_2));
        Assert.assertTrue(ArrayUtils.contains(scopes, NEW_SCOPE_3));
        Assert.assertTrue(ArrayUtils.contains(scopes, NEW_SCOPE_4));
        Assert.assertFalse(ArrayUtils.contains(scopes, CUSTOM_SCOPE_3));
        Assert.assertFalse(ArrayUtils.contains(scopes, CUSTOM_SCOPE_2));
    }

    @Test(groups = "wso2.is", description = "Verify the presence of the updated aud claims in the access token")
    public void testTokenAUDClaimOperations() throws Exception {

        String[] audValueArray = jwtClaims.getStringArrayClaim("aud");

        Assert.assertTrue(ArrayUtils.contains(audValueArray, "zzz1.com"));
        Assert.assertTrue(ArrayUtils.contains(audValueArray, "zzz2.com"));
        Assert.assertTrue(ArrayUtils.contains(audValueArray, "zzz3.com"));
        Assert.assertTrue(ArrayUtils.contains(audValueArray, "zzzR.com"));
        Assert.assertFalse(ArrayUtils.contains(audValueArray, clientId));
    }

    @Test(groups = "wso2.is", description = "Verify the presence of the specified custom string claim in the access " +
            "token")
    public void testTokenStringClaimAddOperation() throws Exception {

        String claimStr = jwtClaims.getStringClaim("custom_claim_string_1");
        Assert.assertEquals(claimStr, "testCustomClaim1");

    }

    @Test(groups = "wso2.is", description = "Verify the presence of the specified custom number claim in the access " +
            "token")
    public void testTokenNumberClaimAddOperation() throws Exception {

        Number claimValue = jwtClaims.getIntegerClaim("custom_claim_number_1");
        Assert.assertEquals(claimValue, 78);

    }

    @Test(groups = "wso2.is", description = "Verify the presence of the specified custom boolean claim in the access " +
            "token")
    public void testTokenBooleanClaimAddOperation() throws Exception {

        Boolean claimValue = jwtClaims.getBooleanClaim("custom_claim_boolean_1");
        Assert.assertTrue(claimValue);
    }

    @Test(groups = "wso2.is", description = "Verify the presence of the specified custom string array claim in the " +
            "access token")
    public void testTokenStringArrayClaimAddOperation()
            throws Exception {

        String[] claimArray1 = {"TestCustomClaim1", "TestCustomClaim2", "TestCustomClaim3"};

        String[] claimArray = jwtClaims.getStringArrayClaim("custom_claim_string_array_1");
        Assert.assertEquals(claimArray, claimArray1);
    }

    @Test(groups = "wso2.is", description = "Verify the replacement of the 'expires_in' claim in the access token")
    public void testTokenExpiresInClaimReplaceOperation() throws Exception {

        if (jwtClaims.getClaim("expires_in") != null) {
            Object expValue = jwtClaims.getLongClaim("expires_in");
            Assert.assertEquals(expValue, 7200);
        }
    }

    /**
     * Creates an action for pre-issuing an access token with basic authentication.
     */
    private String createPreIssueAccessTokenAction() {

        AuthenticationType authenticationType = new AuthenticationType();
        authenticationType.setType(AuthenticationType.TypeEnum.BASIC); // todo handle mock server authorization
        Map<String, Object> authProperties = new HashMap<>();
        authProperties.put(USERNAME_PROPERTY, TEST_USER);
        authProperties.put(PASSWORD_PROPERTY, ADMIN_WSO2);
        authenticationType.setProperties(authProperties);

        Endpoint endpoint = new Endpoint();
        endpoint.setUri(EXTERNAL_SERVICE_URI);
        endpoint.setAuthentication(authenticationType);

        ActionModel actionModel = new ActionModel();
        actionModel.setName("Access Token Pre Issue");
        actionModel.setDescription("This is a test pre issue access token type");
        actionModel.setEndpoint(endpoint);

        try {
            return createAction(PRE_ISSUE_ACCESS_TOKEN_API_PATH, actionModel);
        } catch (IOException e) {
            throw new RuntimeException("Error while creating pre issue access token " + actionModel.getName());
        }
    }

    /**
     * Retrieves an access token for the application.
     *
     * @param applicationId ID of the application
     * @param customScopes  Custom scopes related to the integrated domain APIs
     * @return Access token
     * @throws Exception If error occurred wile requesting access token
     */
    private String retrieveAccessToken(String applicationId, List<String> customScopes) throws Exception {

        OpenIDConnectConfiguration oidcConfig = getOIDCInboundDetailsOfApplication(applicationId);
        clientId = oidcConfig.getClientId();
        String tenantedTokenURI = getTenantQualifiedURL(OAuth2Constant.ACCESS_TOKEN_ENDPOINT, tenantInfo.getDomain());

        List<Permission> permissions = new ArrayList<>();
        Collections.addAll(permissions,
                new Permission(INTERNAL_ORG_USER_MANAGEMENT_LIST),
                new Permission(INTERNAL_ORG_USER_MANAGEMENT_VIEW),
                new Permission(INTERNAL_ORG_USER_MANAGEMENT_CREATE),
                new Permission(INTERNAL_ORG_USER_MANAGEMENT_UPDATE),
                new Permission(INTERNAL_ORG_USER_MANAGEMENT_DELETE)
                          );
        customScopes.forEach(scope -> permissions.add(new Permission(scope)));

        return requestAccessToken(clientId, oidcConfig.getClientSecret(), tenantedTokenURI,
                TEST_USER, ADMIN_WSO2, permissions);
    }

    /**
     * Extracts the JWT claims set from a given JWT token.
     *
     * @param jwtToken JWT token from which claims are to be extracted
     * @return JWTClaimsSet extracted from the provided JWT token
     * @throws ParseException If there is an error in parsing the JWT token
     */
    private JWTClaimsSet extractJwtClaims(String jwtToken) throws ParseException {

        SignedJWT signedJWT = SignedJWT.parse(jwtToken);
        return signedJWT.getJWTClaimsSet();
    }

    /**
     * Adds a user with a role and specific permissions based on custom scopes.
     *
     * @param appID        Application ID to which the role is associated
     * @param customScopes The custom scopes based on which permissions are added
     * @return A list of permissions that were added to the role
     * @throws JSONException If there is an error in processing JSON
     * @throws IOException   If there is an IO exception during user or role creation
     */
    private void addUserWithRole(String appID, List<String> customScopes) throws Exception {
        // Creates roles
        List<Permission> permissions = addPermissions(customScopes);
        Audience roleAudience = new Audience(APPLICATION_AUDIENCE, appID);
        RoleV2 role = new RoleV2(roleAudience, TEST_ROLE_APPLICATION, permissions, Collections.emptyList());
        roleId = addRole(role);

        // Creates user
        UserObject userInfo = new UserObject();
        userInfo.setUserName(TEST_USER);
        userInfo.setPassword(ADMIN_WSO2);
        userInfo.setName(new Name().givenName(TEST_USER_GIVEN));
        userInfo.addEmail(new Email().value(TEST_USER_GMAIL_COM));
        userId = scim2RestClient.createUser(userInfo);

        // Assigns role to the created user
        RoleItemAddGroupobj rolePatchReqObject = new RoleItemAddGroupobj();
        rolePatchReqObject.setOp(RoleItemAddGroupobj.OpEnum.ADD);
        rolePatchReqObject.setPath(USERS);
        rolePatchReqObject.addValue(new ListObject().value(userId));
        scim2RestClient.updateUserRole(new PatchOperationRequestObject().addOperations(rolePatchReqObject), roleId);
    }

    /**
     * Adds permissions based on the provided custom scopes.
     *
     * @param customScopes A list of custom scopes to add as permissions
     * @return A list of permissions including both predefined and custom scope-based permissions
     */
    private List<Permission> addPermissions(List<String> customScopes) {

        List<Permission> userPermissions = new ArrayList<>();

        Collections.addAll(userPermissions,
                new Permission(INTERNAL_ACTION_MANAGEMENT_VIEW),
                new Permission(INTERNAL_ACTION_MANAGEMENT_CREATE),
                new Permission(INTERNAL_ACTION_MANAGEMENT_UPDATE),
                new Permission(INTERNAL_ACTION_MANAGEMENT_DELETE),

                new Permission(INTERNAL_ORG_USER_MANAGEMENT_LIST),
                new Permission(INTERNAL_ORG_USER_MANAGEMENT_VIEW),
                new Permission(INTERNAL_ORG_USER_MANAGEMENT_CREATE),
                new Permission(INTERNAL_ORG_USER_MANAGEMENT_UPDATE),
                new Permission(INTERNAL_ORG_USER_MANAGEMENT_DELETE),

                new Permission(INTERNAL_APPLICATION_MANAGEMENT_VIEW),
                new Permission(INTERNAL_APPLICATION_MANAGEMENT_UPDATE),

                new Permission(INTERNAL_API_RESOURCE_VIEW),
                new Permission(INTERNAL_API_RESOURCE_CREATE)
                          );

        customScopes.forEach(scope -> userPermissions.add(new Permission(scope)));

        return userPermissions;
    }
}

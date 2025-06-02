/*
 * Copyright (c) 2024, WSO2 LLC. (http://www.wso2.com).
 *
 * WSO2 LLC. licenses this file to you under the Apache License,
 * Version 2.0 (the "License"); you may not use this file except
 * in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */

package org.wso2.identity.integration.test.apiAuthorization;

import com.nimbusds.jwt.JWTClaimsSet;
import com.nimbusds.jwt.SignedJWT;

import org.json.JSONException;
import org.testng.Assert;
import org.testng.annotations.AfterClass;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.DataProvider;
import org.testng.annotations.Test;
import org.wso2.carbon.automation.engine.context.TestUserMode;
import org.wso2.carbon.identity.application.common.model.xsd.AssociatedRolesConfig;
import org.wso2.carbon.identity.application.common.model.xsd.ServiceProvider;
import org.wso2.carbon.identity.oauth.stub.dto.OAuthConsumerAppDTO;
import org.wso2.identity.integration.common.clients.oauth.OauthAdminClient;
import org.wso2.identity.integration.test.oauth2.OAuth2ServiceAbstractIntegrationTest;

import org.wso2.identity.integration.test.rest.api.server.application.management.v1.model.ApplicationPatchModel;
import org.wso2.identity.integration.test.rest.api.server.application.management.v1.model.Role;
import org.wso2.identity.integration.test.rest.api.server.roles.v2.model.Permission;
import org.wso2.identity.integration.test.rest.api.server.roles.v2.model.RoleV2;
import org.wso2.identity.integration.test.rest.api.server.roles.v2.model.*;
import org.wso2.identity.integration.test.rest.api.user.common.model.Email;
import org.wso2.identity.integration.test.rest.api.user.common.model.ListObject;
import org.wso2.identity.integration.test.rest.api.user.common.model.Name;
import org.wso2.identity.integration.test.rest.api.user.common.model.PatchOperationRequestObject;
import org.wso2.identity.integration.test.rest.api.user.common.model.RoleItemAddGroupobj;
import org.wso2.identity.integration.test.rest.api.user.common.model.UserObject;
import org.wso2.identity.integration.test.restclients.SCIM2RestClient;
import org.wso2.identity.integration.test.utils.CarbonUtils;

import java.io.IOException;
import java.text.ParseException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

/**
 * Integration test class for testing the API authorization flow in applications.
 * This test case extends {@link OAuth2ServiceAbstractIntegrationTest} and focuses on scenarios related
 * to API authorization, covering the association of API resources with applications, users, and roles.
 * It encompasses API authorization with both organization audience roles and application audience roles.
 */
public class RBACWithAPIAuthorizationTestCase extends OAuth2ServiceAbstractIntegrationTest {

    private static final String CALLBACK_URL = "https://localhost/callback";
    private static final String USERS = "users";
    private static final String TEST_USER = "test_user";
    private static final String ADMIN_WSO2 = "Admin@wso2";
    private static final String TEST_USER_GIVEN = "test_user_given";
    private static final String TEST_USER_GMAIL_COM = "test.user@gmail.com";
    private static final String TEST_ROLE_APPLICATION = "test_role_application";
    private static final String TEST_ROLE_ORGANIZATION = "test_role_organization";
    private static final String INTERNAL_OFFLINE_INVITE = "internal_offline_invite";
    private static final String INTERNAL_BULK_RESOURCE_CREATE = "internal_bulk_resource_create";
    private static final String PASSWORD = "password";
    private static final String JWT = "JWT";
    private static final String API_USERS_V1_OFFLINE_INVITE_LINK = "/api/users/v1/offline-invite-link/";
    public static final String SCIM2_BULK = "/scim2/Bulk";
    public static final String OAUTH2_INTROSPECT = "/oauth2/introspect";
    private static final String SCOPE = "scope";
    private String appId;
    private SCIM2RestClient scim2RestClient;
    private String roleID;
    private String orgRoleID;
    private String orgAppID;
    private String applicationAppID;
    private String userID;
    private String tokenURL = "https://localhost:9853/oauth2/token";
    private List<String> consumerKeys = new ArrayList<>();
    private List<String> consumerSecrets = new ArrayList<>();

    @BeforeClass(alwaysRun = true)
    public void testInit() throws Exception {

        super.init(TestUserMode.SUPER_TENANT_USER);
        scim2RestClient = new SCIM2RestClient(serverURL, tenantInfo);
        adminClient = new OauthAdminClient(backendURL, sessionCookie);
        setSystemproperties();
    }

    @AfterClass(alwaysRun = true)
    public void atEnd() throws Exception {

        restClient.deleteV2Role(orgRoleID);
        deleteApp(orgAppID);
        deleteApp(applicationAppID);
        scim2RestClient.deleteUser(userID);
        consumerKey = null;
        consumerSecret = null;
        appId = null;
        restClient.closeHttpClient();
    }

    /**
     * Provides data for testing registration pre-requisites.
     * Each dataset consists of an application name and its audience type.
     *
     * @return Two-dimensional array containing pairs of application names and audience types.
     */
    @DataProvider(name = "testRegisterPreRequisites")
    public Object[][] getAppNamesAndAudiences() {

        return new Object[][] {
                { "SampleApp1", "APPLICATION" },
                { "SampleApp2", "ORGANIZATION" },
        };
    }

    @Test(groups = "wso2.is", description = "Registers an application with specified audience type, creates a role, adds users, " +
            "and associates users with roles.", dataProvider = "testRegisterPreRequisites")
    private void testRegisterPreRequisites(String appName, String allowedAudience) throws Exception {

        appId = registerApplication(appName, allowedAudience);
        if ("APPLICATION".equals(allowedAudience)) {
            applicationAppID = appId;
        } else {
            orgAppID = appId;
        }
        roleID = createRoles(appId, allowedAudience);
        if ("APPLICATION".equals(allowedAudience)) {
            createUser();
        }
        RoleItemAddGroupobj rolePatchReqObject = new RoleItemAddGroupobj();
        rolePatchReqObject.setOp(RoleItemAddGroupobj.OpEnum.ADD);
        rolePatchReqObject.setPath(USERS);
        rolePatchReqObject.addValue(new ListObject().value(userID));
        scim2RestClient.updateUserRole(new PatchOperationRequestObject().addOperations(rolePatchReqObject),
                roleID);
    }

    private void createUser() throws Exception {

        UserObject userInfo = new UserObject();
        userInfo.setUserName(TEST_USER);
        userInfo.setPassword(ADMIN_WSO2);
        userInfo.setName(new Name().givenName(TEST_USER_GIVEN));
        userInfo.addEmail(new Email().value(TEST_USER_GMAIL_COM));
        userID = scim2RestClient.createUser(userInfo);
    }

    private String createRoles(String appID , String audience) throws JSONException, IOException {

        List<Permission> permissions = new ArrayList<>();
        permissions.add(new Permission(INTERNAL_OFFLINE_INVITE));
        permissions.add(new Permission(INTERNAL_BULK_RESOURCE_CREATE));
        String displayName;
        RoleV2 role;
        List<String> schemas = Collections.emptyList();
        if ("APPLICATION".equals(audience)) {
            displayName = TEST_ROLE_APPLICATION;
            Audience roleAudience = new Audience(audience, appID);
            role = new RoleV2(roleAudience, displayName, permissions, schemas);
        } else {
            displayName = TEST_ROLE_ORGANIZATION;
            role = new RoleV2(displayName, permissions, schemas);
        }
        roleID = addRole(role);
        if ("ORGANIZATION".equals(audience)) {
            orgRoleID = roleID;
        }
        return roleID;
    }

    private String registerApplication(String appName, String allowedAudience) throws Exception {

        OAuthConsumerAppDTO oAuthConsumerAppDTO = getBasicOAuthApp(CALLBACK_URL);
        oAuthConsumerAppDTO.setGrantTypes(PASSWORD);
        oAuthConsumerAppDTO.setApplicationName(appName);
        oAuthConsumerAppDTO.setTokenType(JWT);

        AssociatedRolesConfig associatedRolesConfig = new AssociatedRolesConfig();
        associatedRolesConfig.setAllowedAudience(allowedAudience);
        ServiceProvider serviceProvider = registerApplicationAudienceServiceProvider(oAuthConsumerAppDTO ,
                associatedRolesConfig);

        consumerKeys.add(consumerKey);
        consumerSecrets.add(consumerSecret);
        String applicationId = serviceProvider.getApplicationResourceId();
            if (!CarbonUtils.isLegacyAuthzRuntimeEnabled()) {
                // Authorize few system APIs.
                authorizeSystemAPIs(applicationId,
                        new ArrayList<>(Arrays.asList(API_USERS_V1_OFFLINE_INVITE_LINK, SCIM2_BULK,
                                OAUTH2_INTROSPECT)));
            }

        return applicationId;
    }

    /**
     * Provides consumer keys and secrets for testing purposes.
     * Each dataset consists of a consumer key and its corresponding consumer secret.
     *
     * @return Two-dimensional array containing pairs of consumer keys and secrets.
     */
    @DataProvider(name = "consumerKeysAndSecrets")
    public Object[][] getConsumerKeysAndSecrets() {

        Object[][] keysAndSecrets = new Object[consumerKeys.size()][2];
        for (int i = 0; i < consumerKeys.size(); i++) {
            keysAndSecrets[i][0] = consumerKeys.get(i);
            keysAndSecrets[i][1] = consumerSecrets.get(i);
        }
        return keysAndSecrets;
    }

    @Test(groups = "wso2.is", description = "Check whether the authorizes scopes are available in the token.",
            dataProvider = "consumerKeysAndSecrets" , dependsOnMethods = "testRegisterPreRequisites")
    public void testGetToken(String consumerKey, String consumerSecret) throws Exception {

        List<Permission>
                permissions = new ArrayList<>();
        permissions.add(new Permission(INTERNAL_OFFLINE_INVITE));
        permissions.add(new Permission(INTERNAL_BULK_RESOURCE_CREATE));
        String token = requestAccessToken(consumerKey, consumerSecret, tokenURL,
                TEST_USER, ADMIN_WSO2, permissions);
        JWTClaimsSet jwtClaimsSet = extractJwt(token);
        String validScopes = jwtClaimsSet.getStringClaim(SCOPE);
        Assert.assertEquals(validScopes, INTERNAL_BULK_RESOURCE_CREATE + " " + INTERNAL_OFFLINE_INVITE);
    }

    private JWTClaimsSet extractJwt(String jwtToken) throws ParseException {

        SignedJWT signedJWT = SignedJWT.parse(jwtToken);
        return signedJWT.getJWTClaimsSet();
    }
}

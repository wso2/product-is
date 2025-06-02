/*
 * Copyright (c) 2017, WSO2 LLC. (https://www.wso2.com).
 *
 * WSO2 LLC. licenses this file to you under the Apache License,
 * Version 2.0 (the "License"); you may not use this file except
 * in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied. See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */

package org.wso2.identity.integration.test.oauth2;

import org.apache.commons.codec.binary.Base64;
import org.apache.http.HttpResponse;
import org.apache.http.NameValuePair;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.message.BasicNameValuePair;
import org.apache.http.util.EntityUtils;
import org.json.simple.JSONObject;
import org.json.simple.JSONValue;
import org.testng.Assert;
import org.testng.annotations.AfterClass;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;
import org.wso2.carbon.automation.engine.context.TestUserMode;
import org.wso2.identity.integration.test.rest.api.server.application.management.v1.model.ApplicationResponseModel;
import org.wso2.identity.integration.test.rest.api.server.application.management.v1.model.OpenIDConnectConfiguration;
import org.wso2.identity.integration.test.rest.api.user.common.model.*;
import org.wso2.identity.integration.test.restclients.SCIM2RestClient;
import org.wso2.identity.integration.test.utils.OAuth2Constant;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.List;

public class OAuth2RoleClaimTestCase extends OAuth2ServiceAbstractIntegrationTest {

    private static final String OAUTH_ROLE = "oauthRole";
    private static final String USERS_PATH = "users";
    private static final String OIDC_ROLES_CLAIM_URI = "roles";
    private static final String GIVEN_NAME = "testUser";
    private static final String FAMILY_NAME = "test";
    private static final String HOME_ATTRIBUTE = "home";
    private static final String HOME_EMAIL = "testuser11@gmail.com";
    private static final String WORK_ATTRIBUTE = "work";
    private static final String WORK_EMAIL = "testuser99@wso2.com";
    private static final String USER_USERNAME = "testuser99";
    private static final String USER_PASSWORD = "Testuser@123";
    private static final String EMPLOYEE_NUMBER = "Abc123";
    private static final String MANAGER_NAME = "wso2TestManage";


    private String consumerKey;
    private String consumerSecret;
    private String applicationId;
    private String roleId;
    private String userId;

    private CloseableHttpClient client;
    private SCIM2RestClient scim2RestClient;

    private String USERNAME;
    private String PASSWORD;

    @BeforeClass(alwaysRun = true)
    public void testInit() throws Exception {

        super.init(TestUserMode.TENANT_USER);

        this.USERNAME = tenantInfo.getContextUser().getUserNameWithoutDomain();
        this.PASSWORD = tenantInfo.getContextUser().getPassword();
        setSystemproperties();
        client = HttpClients.createDefault();
        scim2RestClient = new SCIM2RestClient(serverURL, tenantInfo);

        roleId = scim2RestClient.addRole(getRoleCreationInfo());
        userId = scim2RestClient.createUser(getUserCreationInfo());
    }

    @AfterClass(alwaysRun = true)
    public void atEnd() throws Exception {

        deleteApp(applicationId);
        scim2RestClient.deleteRole(roleId);
        scim2RestClient.deleteUser(userId);
        consumerKey = null;
        consumerSecret = null;
        applicationId = null;
        roleId = null;
        userId = null;

        client.close();
        restClient.closeHttpClient();
        scim2RestClient.closeHttpClient();
    }

    @Test(groups = "wso2.is", description = "Check Oauth2 application registration")
    public void testRegisterApplication() throws Exception {

        ApplicationResponseModel application = addApplication();
        Assert.assertNotNull(application, "OAuth App creation failed.");

        OpenIDConnectConfiguration oidcConfig = getOIDCInboundDetailsOfApplication(application.getId());

        consumerKey = oidcConfig.getClientId();
        Assert.assertNotNull(consumerKey, "Application creation failed.");

        consumerSecret = oidcConfig.getClientSecret();
        Assert.assertNotNull(consumerSecret, "Application creation failed.");

        applicationId = application.getId();
    }

    @Test(groups = "wso2.is", description = "Check id_token before updating roles.", dependsOnMethods =
            "testRegisterApplication")
    public void testSendAuthorizedPost() throws Exception {

        HttpPost request = new HttpPost(getTenantQualifiedURL(
                OAuth2Constant.ACCESS_TOKEN_ENDPOINT, tenantInfo.getDomain()));
        List<NameValuePair> urlParameters = new ArrayList<>();
        urlParameters.add(new BasicNameValuePair("grant_type",
                OAuth2Constant.OAUTH2_GRANT_TYPE_RESOURCE_OWNER));
        urlParameters.add(new BasicNameValuePair("username", USERNAME ));
        urlParameters.add(new BasicNameValuePair("password", PASSWORD));
        urlParameters.add(new BasicNameValuePair("scope", "openid"));

        request.setHeader("User-Agent", OAuth2Constant.USER_AGENT);
        request.setHeader("Authorization", "Basic " + Base64.encodeBase64String((consumerKey + ":" + consumerSecret)
                .getBytes()).trim());
        request.setHeader("Content-Type", "application/x-www-form-urlencoded;charset=UTF-8");
        request.setEntity(new UrlEncodedFormEntity(urlParameters));

        HttpResponse response = client.execute(request);

        BufferedReader rd = new BufferedReader(new InputStreamReader(response.getEntity().getContent()));

        Object obj = JSONValue.parse(rd);
        EntityUtils.consume(response.getEntity());

        String encodedIdToken = ((JSONObject) obj).get("id_token").toString().split("\\.")[1];
        Object idToken = JSONValue.parse(new String(Base64.decodeBase64(encodedIdToken)));
        Assert.assertNull(((JSONObject) idToken).get(OIDC_ROLES_CLAIM_URI), 
                "Id token must not contain role claim which is not configured for the requested scope.");
    }

    @Test(groups = "wso2.is", description = "Check id_token after updating roles", dependsOnMethods =
            "testSendAuthorizedPost")
    public void testSendAuthorizedPostAfterRoleUpdate() throws Exception {

        scim2RestClient.updateUserRole(getAddUserPatchRole(userId), roleId);

        HttpPost request = new HttpPost(getTenantQualifiedURL(
                OAuth2Constant.ACCESS_TOKEN_ENDPOINT, tenantInfo.getDomain()));
        List<NameValuePair> urlParameters = new ArrayList<>();
        urlParameters.add(new BasicNameValuePair("grant_type",
                OAuth2Constant.OAUTH2_GRANT_TYPE_RESOURCE_OWNER));
        urlParameters.add(new BasicNameValuePair("username", USERNAME));
        urlParameters.add(new BasicNameValuePair("password", PASSWORD));
        urlParameters.add(new BasicNameValuePair("scope", "openid"));

        request.setHeader("User-Agent", OAuth2Constant.USER_AGENT);
        request.setHeader("Authorization", "Basic " + Base64.encodeBase64String((consumerKey + ":" + consumerSecret)
                .getBytes()).trim());
        request.setHeader("Content-Type", "application/x-www-form-urlencoded;charset=UTF-8");
        request.setEntity(new UrlEncodedFormEntity(urlParameters));

        HttpResponse response = client.execute(request);

        BufferedReader rd = new BufferedReader(new InputStreamReader(response.getEntity().getContent()));

        Object obj = JSONValue.parse(rd);
        EntityUtils.consume(response.getEntity());

        String encodedIdToken = ((JSONObject) obj).get("id_token").toString().split("\\.")[1];
        Object idToken = JSONValue.parse(new String(Base64.decodeBase64(encodedIdToken)));
        Assert.assertNull(((JSONObject) idToken).get(OIDC_ROLES_CLAIM_URI), 
                "Id token must not contain role claim which is not configured for the requested scope.");
    }

    private UserObject getUserCreationInfo() {

        UserObject userInfo = new UserObject();

        userInfo.setUserName(USER_USERNAME);
        userInfo.setPassword(USER_PASSWORD);

        Name name = new Name();
        name.setGivenName(GIVEN_NAME);
        name.setFamilyName(FAMILY_NAME);
        userInfo.setName(name);

        Email homeEmail = new Email();
        homeEmail.setPrimary(true);
        homeEmail.setType(HOME_ATTRIBUTE);
        homeEmail.setValue(HOME_EMAIL);

        Email workEmail = new Email();
        workEmail.setType(WORK_ATTRIBUTE);
        workEmail.setValue(WORK_EMAIL);

        userInfo.addEmail(homeEmail);
        userInfo.addEmail(workEmail);

        ScimSchemaExtensionEnterprise scimSchema = new ScimSchemaExtensionEnterprise();
        scimSchema.setEmployeeNumber(EMPLOYEE_NUMBER);
        scimSchema.setManager(new Manager().value(MANAGER_NAME));

        userInfo.setScimSchemaExtensionEnterprise(scimSchema);

        return userInfo;
    }

    private RoleRequestObject getRoleCreationInfo() {

        RoleRequestObject roleInfo = new RoleRequestObject();
        roleInfo.setDisplayName(OAUTH_ROLE);

        return roleInfo;
    }

    private PatchOperationRequestObject getAddUserPatchRole(String userId) {

        RoleItemAddGroupobj patchRoleItem = new RoleItemAddGroupobj();
        patchRoleItem.setOp(RoleItemAddGroupobj.OpEnum.ADD);
        patchRoleItem.setPath(USERS_PATH);
        patchRoleItem.addValue(new ListObject().value(userId));

        return new PatchOperationRequestObject().addOperations(patchRoleItem);
    }
}

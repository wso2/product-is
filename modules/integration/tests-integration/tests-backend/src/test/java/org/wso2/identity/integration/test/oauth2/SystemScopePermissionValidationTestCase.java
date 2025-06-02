/*
 * Copyright (c) 2023, WSO2 LLC. (http://www.wso2.com).
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
package org.wso2.identity.integration.test.oauth2;

import com.nimbusds.oauth2.sdk.AccessTokenResponse;
import com.nimbusds.oauth2.sdk.AuthorizationGrant;
import com.nimbusds.oauth2.sdk.ResourceOwnerPasswordCredentialsGrant;
import com.nimbusds.oauth2.sdk.Scope;
import com.nimbusds.oauth2.sdk.TokenRequest;
import com.nimbusds.oauth2.sdk.auth.ClientAuthentication;
import com.nimbusds.oauth2.sdk.auth.ClientSecretBasic;
import com.nimbusds.oauth2.sdk.auth.Secret;
import com.nimbusds.oauth2.sdk.http.HTTPResponse;
import com.nimbusds.oauth2.sdk.id.ClientID;

import org.apache.http.HttpResponse;
import org.apache.http.NameValuePair;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClientBuilder;
import org.apache.http.message.BasicNameValuePair;
import org.apache.http.util.EntityUtils;
import org.testng.Assert;
import org.testng.annotations.AfterClass;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.DataProvider;
import org.testng.annotations.Factory;
import org.testng.annotations.Test;
import org.wso2.carbon.automation.engine.context.AutomationContext;
import org.wso2.carbon.automation.engine.context.TestUserMode;
import org.wso2.identity.integration.test.rest.api.server.application.management.v1.model.ApplicationPatchModel;
import org.wso2.identity.integration.test.rest.api.server.application.management.v1.model.ApplicationResponseModel;
import org.wso2.identity.integration.test.rest.api.server.application.management.v1.model.AssociatedRolesConfig;
import org.wso2.identity.integration.test.rest.api.server.application.management.v1.model.OpenIDConnectConfiguration;
import org.wso2.identity.integration.test.utils.CarbonUtils;
import org.wso2.identity.integration.test.utils.DataExtractUtil;
import org.wso2.identity.integration.test.utils.OAuth2Constant;

import java.net.URI;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static org.wso2.identity.integration.test.utils.DataExtractUtil.KeyValue;

public class SystemScopePermissionValidationTestCase extends OAuth2ServiceAbstractIntegrationTest {

    private String accessToken;
    private String consumerKey;
    private String consumerSecret;
    private CloseableHttpClient client;
    private final String username;
    private final String usernameWithoutTenantDomain;
    private final String userPassword;
    private final String activeTenant;
    private final TestUserMode testUserMode;

    private static final String SYSTEM_SCOPE = "SYSTEM";
    private static boolean isLegacyRuntimeEnabled;
    private String applicationId;

    @DataProvider(name = "configProvider")
    public static Object[][] configProvider() {

        return new Object[][]{{TestUserMode.SUPER_TENANT_ADMIN}, {TestUserMode.TENANT_ADMIN},
                {TestUserMode.TENANT_USER}};
    }

    @Factory(dataProvider = "configProvider")
    public SystemScopePermissionValidationTestCase(TestUserMode userMode) throws Exception {

        super.init(userMode);
        AutomationContext context = new AutomationContext("IDENTITY", userMode);
        this.username = context.getContextTenant().getTenantAdmin().getUserName();
        this.usernameWithoutTenantDomain = context.getContextTenant().getTenantAdmin().getUserNameWithoutDomain();
        this.userPassword = context.getContextTenant().getTenantAdmin().getPassword();
        this.activeTenant = context.getContextTenant().getDomain();
        this.testUserMode = userMode;
    }

    @BeforeClass(alwaysRun = true)
    public void testInit() throws Exception {

        setSystemproperties();
        client = HttpClientBuilder.create().build();
        isLegacyRuntimeEnabled = CarbonUtils.isLegacyAuthzRuntimeEnabled();
    }

    @AfterClass(alwaysRun = true)
    public void atEnd() throws Exception {

        deleteApp(applicationId);
        client.close();
        restClient.closeHttpClient();
        consumerKey = null;
        accessToken = null;
    }

    @Test(groups = "wso2.is", description = "Check Oauth2 application flow")
    public void testRegisterApplication() throws Exception {

        ApplicationResponseModel application = addApplication();
        Assert.assertNotNull(application, "OAuth App creation failed.");
        applicationId = application.getId();
        OpenIDConnectConfiguration oidcConfig = getOIDCInboundDetailsOfApplication(applicationId);

        consumerKey = oidcConfig.getClientId();
        Assert.assertNotNull(consumerKey, "Application creation failed.");

        consumerSecret = oidcConfig.getClientSecret();
        Assert.assertNotNull(consumerSecret, "Application creation failed.");

        if (!isLegacyRuntimeEnabled) {
            // Authorize few system APIs.
            authorizeSystemAPIs(applicationId,
                    new ArrayList<>(Arrays.asList("/api/server/v1/tenants", "/scim2/Users")));
            // Associate roles.
            ApplicationPatchModel applicationPatch = new ApplicationPatchModel();
            AssociatedRolesConfig associatedRolesConfig =
                    new AssociatedRolesConfig().allowedAudience(AssociatedRolesConfig.AllowedAudienceEnum.ORGANIZATION);
            applicationPatch = applicationPatch.associatedRoles(associatedRolesConfig);
            updateApplication(applicationId, applicationPatch);
        }
    }

    @Test(groups = "wso2.is", description = "Send authorize user request and get access token", dependsOnMethods = "testRegisterApplication")
    public void testGetAccessToken() throws Exception {

        List<NameValuePair> urlParameters = new ArrayList<>();urlParameters.add(new BasicNameValuePair("grantType",
                OAuth2Constant.OAUTH2_GRANT_TYPE_CLIENT_CREDENTIALS));
        urlParameters.add(new BasicNameValuePair("consumerKey", consumerKey));
        urlParameters.add(new BasicNameValuePair("consumerSecret", consumerSecret));
        urlParameters.add(new BasicNameValuePair("accessEndpoint",
                getTenantQualifiedURL(OAuth2Constant.ACCESS_TOKEN_ENDPOINT, tenantInfo.getDomain())));
        urlParameters.add(new BasicNameValuePair("authorize", OAuth2Constant.AUTHORIZE_PARAM));
        urlParameters.add(new BasicNameValuePair("scope", SYSTEM_SCOPE));
        HttpResponse response =
                sendPostRequestWithParameters(client, urlParameters,
                        OAuth2Constant.AUTHORIZED_USER_URL);
        Assert.assertNotNull(response, "Authorization request failed. Authorized response is null");
        EntityUtils.consume(response.getEntity());

        response = sendPostRequest(client, OAuth2Constant.AUTHORIZED_URL);

        Map<String, Integer> keyPositionMap = new HashMap<>(1);
        keyPositionMap.put("name=\"accessToken\"", 1);

        List<KeyValue> keyValues =
                DataExtractUtil.extractInputValueFromResponse(response,
                        keyPositionMap);
        Assert.assertNotNull(keyValues, "Access token Key value is null.");
        accessToken = keyValues.get(0).getValue();

        EntityUtils.consume(response.getEntity());
        Assert.assertNotNull(accessToken, "Access token is null.");
    }

    @Test(groups = "wso2.is", description = "Test introspection endpoint", dependsOnMethods = "testGetAccessToken")
    public void testIntrospectionEndpoint() throws Exception {

        String scope = getScopesFromIntrospectionResponse();
        doTheScopeValidationBasedOnTheTestUserMode(scope, true);
    }

    @Test(groups = "wso2.is", description = "Test introspection endpoint", dependsOnMethods = "testIntrospectionEndpoint")
    public void getTokenAndValidate() throws Exception {

        try {
            client = HttpClientBuilder.create().disableRedirectHandling().build();
            Secret password = new Secret(userPassword);
            AuthorizationGrant passwordGrant = new ResourceOwnerPasswordCredentialsGrant(
                    usernameWithoutTenantDomain, password);
            ClientID clientID = new ClientID(consumerKey);
            Secret clientSecret = new Secret(consumerSecret);
            ClientAuthentication clientAuth = new ClientSecretBasic(clientID, clientSecret);
            URI tokenEndpoint = new URI(getTenantQualifiedURL(OAuth2Constant.ACCESS_TOKEN_ENDPOINT, tenantInfo.getDomain()));
            Scope systemScope = new Scope(SYSTEM_SCOPE);
            TokenRequest request = new TokenRequest(tokenEndpoint, clientAuth, passwordGrant, systemScope);

            HTTPResponse tokenHTTPResp = request.toHTTPRequest().send();
            Assert.assertNotNull(tokenHTTPResp, "Access token http response is null.");
            AccessTokenResponse tokenResponse = AccessTokenResponse.parse(tokenHTTPResp);
            Assert.assertNotNull(tokenResponse, "Access token response is null.");
            accessToken = tokenResponse.getTokens().getAccessToken().getValue();
            String scope = getScopesFromIntrospectionResponse();
            doTheScopeValidationBasedOnTheTestUserMode(scope, false);
        } finally {
            client.close();
        }
    }

    private String getScopesFromIntrospectionResponse() throws Exception {

        String introspectionUrl = "carbon.super".equalsIgnoreCase(activeTenant) ?
                OAuth2Constant.INTRO_SPEC_ENDPOINT : OAuth2Constant.TENANT_INTRO_SPEC_ENDPOINT;
        org.json.simple.JSONObject introspectionResponse =
                introspectTokenWithTenant(client, accessToken, introspectionUrl, username, userPassword);
        Assert.assertTrue(introspectionResponse.containsKey("scope"));
        return introspectionResponse.get("scope").toString();
    }

    private void doTheScopeValidationBasedOnTheTestUserMode(String scope, boolean isClientCredentialsGrant) {

        if (testUserMode == TestUserMode.SUPER_TENANT_ADMIN) {
            if (isLegacyRuntimeEnabled) {
                Assert.assertTrue(scope.contains("internal_server_admin"), "Scope should contain " +
                        "`internal_server_admin` scope");
            }
            Assert.assertTrue(scope.contains("internal_modify_tenants"), "Scope should contain " +
                    "`internal_modify_tenants` scope");
        } else if (testUserMode == TestUserMode.TENANT_ADMIN) {
            Assert.assertFalse(scope.contains("internal_server_admin"), "Scope should not contain " +
                    "`internal_server_admin` scope");
            Assert.assertFalse(scope.contains("internal_modify_tenants"), "Scope should not contain " +
                        "`internal_modify_tenants` scope");
        } else {
            // Normal user.
            if (isClientCredentialsGrant) {
                Assert.assertFalse(scope.contains("internal_login"), "Scope should not contain " +
                        "`internal_login` scope");
            } else {
                Assert.assertTrue(scope.contains("internal_login"), "Scope should contain " +
                        "`internal_login` scope");
            }
            Assert.assertFalse(scope.contains("internal_server_admin"), "Scope should not contain " +
                    "`internal_server_admin` scope");
            Assert.assertFalse(scope.contains("internal_modify_tenants"), "Scope should not contain " +
                        "`internal_modify_tenants` scope");
        }
    }
}

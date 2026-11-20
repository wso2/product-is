/*
 * Copyright (c) 2026, WSO2 LLC. (http://www.wso2.com).
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

package org.wso2.identity.integration.test.rest.api.server.flow.execution.v1;

import org.apache.commons.codec.binary.Base64;
import org.apache.http.HttpHeaders;
import org.apache.http.HttpResponse;
import org.apache.http.NameValuePair;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.client.utils.URLEncodedUtils;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.message.BasicNameValuePair;
import org.apache.http.util.EntityUtils;
import org.json.simple.JSONObject;
import org.json.simple.JSONValue;
import org.testng.Assert;
import org.testng.annotations.AfterClass;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.DataProvider;
import org.testng.annotations.Factory;
import org.testng.annotations.Test;
import org.wso2.carbon.automation.engine.context.TestUserMode;
import org.wso2.carbon.utils.multitenancy.MultitenantConstants;
import org.wso2.identity.integration.test.rest.api.server.application.management.v1.model.ApplicationModel;
import org.wso2.identity.integration.test.rest.api.server.application.management.v1.model.IdTokenConfiguration;
import org.wso2.identity.integration.test.rest.api.server.application.management.v1.model.InboundProtocols;
import org.wso2.identity.integration.test.rest.api.server.application.management.v1.model.OpenIDConnectConfiguration;
import org.wso2.identity.integration.test.rest.api.server.flow.execution.v1.model.FlowExecutionRequest;
import org.wso2.identity.integration.test.rest.api.server.flow.execution.v1.model.FlowExecutionResponse;
import org.wso2.identity.integration.test.rest.api.server.flow.management.v1.model.Error;
import org.wso2.identity.integration.test.rest.api.user.common.model.Email;
import org.wso2.identity.integration.test.rest.api.user.common.model.Name;
import org.wso2.identity.integration.test.rest.api.user.common.model.UserObject;
import org.wso2.identity.integration.test.restclients.FlowExecutionClient;
import org.wso2.identity.integration.test.restclients.FlowManagementClient;
import org.wso2.identity.integration.test.restclients.OAuth2RestClient;
import org.wso2.identity.integration.test.restclients.SCIM2RestClient;
import org.wso2.identity.integration.test.utils.OAuth2Constant;

import java.net.URI;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static org.wso2.identity.integration.test.rest.api.server.flow.execution.v1.FlowExecutionTestBase.FlowTypes.PROFILE_UPDATE;

/**
 * Test class for the profile update flow in Flow Execution API.
 */
public class ProfileUpdateFlowExecutionTest extends FlowExecutionTestBase {

    private static final String ACTION_ID = "button_z1wt";
    private static final String USER = "ProfileUpdateExecTestUser";
    private static final String USER_PASSWORD = "Wso2@test";
    private static final String USER_EMAIL = "profile.update.exec@example.com";
    private static final String GIVEN_NAME = "ProfileUpdateJohn";
    private static final String UPDATED_GIVEN_NAME = "ProfileUpdateJane";
    private static final String LAST_NAME = "ProfileUpdateDoe";
    private static final String UPDATED_LAST_NAME = "ProfileUpdateSmith";
    private static final String APP_NAME = "ProfileUpdateFlowTestApp";
    private static final String NO_AUDIENCE_APP_NAME = "ProfileUpdateFlowNoAudienceTestApp";
    private static final String USER_VERIFICATION_AUDIENCE = "urn:wso2:identity:flow:user-verification";
    private static final String ID_TOKEN_TYPE = "urn:ietf:params:oauth:token-type:id_token";
    private static final String TOKEN = "token";
    private static final String TOKEN_TYPE = "tokenType";
    private static final String FLOW_ID = "flowId";
    private static final String HANDOFF = "handoff";
    private FlowExecutionClient flowExecutionClient;
    private FlowManagementClient flowManagementClient;
    private OAuth2RestClient oAuth2RestClient;
    private SCIM2RestClient scim2RestClient;
    private CloseableHttpClient httpClient;
    private String userId;
    private String appId;
    private String clientId;
    private String clientSecret;
    private String noAudienceAppId;
    private String noAudienceClientId;
    private String noAudienceClientSecret;
    private String flowId;
    private String handoffFlowId;

    @DataProvider(name = "restAPIUserConfigProvider")
    public static Object[][] restAPIUserConfigProvider() {

        return new Object[][]{
                {TestUserMode.SUPER_TENANT_ADMIN},
                {TestUserMode.TENANT_ADMIN}
        };
    }

    @Factory(dataProvider = "restAPIUserConfigProvider")
    public ProfileUpdateFlowExecutionTest(TestUserMode userMode) throws Exception {

        super.init(userMode);
        this.context = isServer;
        this.authenticatingUserName = context.getContextTenant().getTenantAdmin().getUserName();
        this.authenticatingCredential = context.getContextTenant().getTenantAdmin().getPassword();
        this.tenant = context.getContextTenant().getDomain();
    }

    @BeforeClass(alwaysRun = true)
    public void setupClass() throws Exception {

        super.testInit(API_VERSION, swaggerDefinition, tenantInfo.getDomain());
        flowExecutionClient = new FlowExecutionClient(serverURL, tenantInfo);
        flowManagementClient = new FlowManagementClient(serverURL, tenantInfo);
        oAuth2RestClient = new OAuth2RestClient(serverURL, tenantInfo);
        scim2RestClient = new SCIM2RestClient(serverURL, tenantInfo);
        httpClient = HttpClients.createDefault();

        UserObject user = new UserObject()
                .userName(USER)
                .password(USER_PASSWORD)
                .name(new Name().givenName(GIVEN_NAME).familyName(LAST_NAME))
                .addEmail(new Email().value(USER_EMAIL));
        userId = scim2RestClient.createUser(user);

        appId = createApplication(APP_NAME, Collections.singletonList(USER_VERIFICATION_AUDIENCE));
        OpenIDConnectConfiguration oidcConfig = oAuth2RestClient.getOIDCInboundDetails(appId);
        clientId = oidcConfig.getClientId();
        clientSecret = oidcConfig.getClientSecret();

        noAudienceAppId = createApplication(NO_AUDIENCE_APP_NAME, null);
        OpenIDConnectConfiguration noAudienceOidcConfig = oAuth2RestClient.getOIDCInboundDetails(noAudienceAppId);
        noAudienceClientId = noAudienceOidcConfig.getClientId();
        noAudienceClientSecret = noAudienceOidcConfig.getClientSecret();

        enableFlow(PROFILE_UPDATE, flowManagementClient);
        addProfileUpdateFlow(flowManagementClient);
    }

    @AfterClass(alwaysRun = true)
    public void tearDownClass() throws Exception {

        disableFlow(PROFILE_UPDATE, flowManagementClient);
        oAuth2RestClient.deleteApplication(appId);
        oAuth2RestClient.deleteApplication(noAudienceAppId);
        scim2RestClient.deleteUser(userId);
        flowExecutionClient.closeHttpClient();
        flowManagementClient.closeHttpClient();
        oAuth2RestClient.closeHttpClient();
        scim2RestClient.closeHttpClient();
        httpClient.close();
    }

    @Test
    public void initiateFlowWithUserVerification() throws Exception {

        Object responseObj = flowExecutionClient.executeFlow(
                getUserVerificationRequest(getIdToken(clientId, clientSecret)));
        Assert.assertTrue(responseObj instanceof FlowExecutionResponse);
        FlowExecutionResponse response = (FlowExecutionResponse) responseObj;
        Assert.assertNotNull(response);
        Assert.assertNotNull(response.getFlowId());
        flowId = response.getFlowId();
        Assert.assertEquals(response.getFlowStatus(), STATUS_INCOMPLETE);
        Assert.assertEquals(response.getType().toString(), TYPE_REDIRECTION);
        Assert.assertNotNull(response.getData());
        Assert.assertNotNull(response.getData().getAdditionalData());
        Assert.assertEquals(response.getData().getAdditionalData().get(HANDOFF), "true");
        Assert.assertNotNull(response.getData().getRedirectURL());
        handoffFlowId = getQueryParam(response.getData().getRedirectURL(), FLOW_ID);
        Assert.assertNotNull(handoffFlowId);
        Assert.assertNotEquals(handoffFlowId, flowId);
    }

    @Test(dependsOnMethods = "initiateFlowWithUserVerification")
    public void executeFlowWithHandoffFlowId() throws Exception {

        Object responseObj = flowExecutionClient.executeFlow(getFlowExecutionRequest(handoffFlowId));
        Assert.assertTrue(responseObj instanceof FlowExecutionResponse);
        FlowExecutionResponse response = (FlowExecutionResponse) responseObj;
        Assert.assertNotNull(response);
        Assert.assertNotNull(response.getFlowId());
        flowId = response.getFlowId();
        Assert.assertEquals(response.getFlowStatus(), STATUS_INCOMPLETE);
        Assert.assertEquals(response.getType().toString(), TYPE_VIEW);
        Assert.assertNotNull(response.getData());
        Assert.assertNotNull(response.getData().getComponents());
        Assert.assertEquals(response.getData().getComponents().size(), 2);
    }

    @Test(dependsOnMethods = "executeFlowWithHandoffFlowId")
    public void executeFlowWithUsedHandoffFlowId() throws Exception {

        Object responseObj = flowExecutionClient.executeFlow(getFlowExecutionRequest(handoffFlowId));
        Assert.assertTrue(responseObj instanceof Error);
        Error error = (Error) responseObj;
        Assert.assertNotNull(error);
        Assert.assertNotNull(error.getCode());
        Assert.assertEquals(error.getCode(), "FE-60001");
    }

    @Test(dependsOnMethods = "executeFlowWithUsedHandoffFlowId")
    public void executeFlow() throws Exception {

        Object responseObj = flowExecutionClient.executeFlow(getProfileUpdateRequest());
        Assert.assertTrue(responseObj instanceof FlowExecutionResponse);
        FlowExecutionResponse response = (FlowExecutionResponse) responseObj;
        Assert.assertNotNull(response);
        Assert.assertEquals(response.getFlowStatus(), STATUS_COMPLETE);
        Assert.assertEquals(response.getType().toString(), TYPE_REDIRECTION);
        Assert.assertNotNull(response.getData());
    }

    @Test(dependsOnMethods = "executeFlow")
    public void verifyUpdatedProfile() throws Exception {

        JSONObject user = scim2RestClient.getUser(userId, null);
        JSONObject name = (JSONObject) user.get("name");
        Assert.assertNotNull(name);
        Assert.assertEquals(name.get("givenName"), UPDATED_GIVEN_NAME);
        Assert.assertEquals(name.get("familyName"), UPDATED_LAST_NAME);
        Assert.assertNotNull(user.get("emails"));
        Assert.assertTrue(user.get("emails").toString().contains(USER_EMAIL));
    }

    @Test
    public void initiateFlowWithInvalidIdToken() throws Exception {

        String idToken = getIdToken(clientId, clientSecret);
        String invalidIdToken = idToken.substring(0, idToken.lastIndexOf('.') + 1) + "invalidSignature";
        Object responseObj = flowExecutionClient.executeFlow(getUserVerificationRequest(invalidIdToken));
        Assert.assertTrue(responseObj instanceof Error);
        Error error = (Error) responseObj;
        Assert.assertNotNull(error);
        Assert.assertNotNull(error.getCode());
        Assert.assertEquals(error.getCode(), "FE-60006");
    }

    @Test
    public void initiateFlowWithIdTokenWithoutUserVerificationAudience() throws Exception {

        Object responseObj = flowExecutionClient.executeFlow(
                getUserVerificationRequest(getIdToken(noAudienceClientId, noAudienceClientSecret)));
        Assert.assertTrue(responseObj instanceof Error);
        Error error = (Error) responseObj;
        Assert.assertNotNull(error);
        Assert.assertNotNull(error.getCode());
        Assert.assertEquals(error.getCode(), "FE-60006");
    }

    private FlowExecutionRequest getUserVerificationRequest(String idToken) {

        FlowExecutionRequest flowExecutionRequest = new FlowExecutionRequest();
        flowExecutionRequest.setFlowType(PROFILE_UPDATE);
        flowExecutionRequest.setApplicationId(appId);
        Map<String, String> inputs = new HashMap<>();
        inputs.put(TOKEN, idToken);
        inputs.put(TOKEN_TYPE, ID_TOKEN_TYPE);
        flowExecutionRequest.setInputs(inputs);
        return flowExecutionRequest;
    }

    private static FlowExecutionRequest getFlowExecutionRequest(String flowId) {

        FlowExecutionRequest flowExecutionRequest = new FlowExecutionRequest();
        flowExecutionRequest.setFlowId(flowId);
        flowExecutionRequest.setActionId("");
        return flowExecutionRequest;
    }

    private FlowExecutionRequest getProfileUpdateRequest() {

        FlowExecutionRequest flowExecutionRequest = new FlowExecutionRequest();
        flowExecutionRequest.setFlowId(flowId);
        flowExecutionRequest.setFlowType(PROFILE_UPDATE);
        flowExecutionRequest.setActionId(ACTION_ID);
        Map<String, String> inputs = new HashMap<>();
        inputs.put("http://wso2.org/claims/givenname", UPDATED_GIVEN_NAME);
        inputs.put("http://wso2.org/claims/lastname", UPDATED_LAST_NAME);
        flowExecutionRequest.setInputs(inputs);
        return flowExecutionRequest;
    }

    private String createApplication(String appName, List<String> idTokenAudience) throws Exception {

        ApplicationModel application = new ApplicationModel();
        application.setName(appName);
        OpenIDConnectConfiguration oidcConfig = new OpenIDConnectConfiguration();
        oidcConfig.setGrantTypes(Collections.singletonList(OAuth2Constant.OAUTH2_GRANT_TYPE_RESOURCE_OWNER));
        if (idTokenAudience != null) {
            oidcConfig.setIdToken(new IdTokenConfiguration().audience(idTokenAudience));
        }
        InboundProtocols inboundProtocols = new InboundProtocols();
        inboundProtocols.setOidc(oidcConfig);
        application.setInboundProtocolConfiguration(inboundProtocols);
        return oAuth2RestClient.createApplication(application);
    }

    private String getIdToken(String clientId, String clientSecret) throws Exception {

        HttpPost post = new HttpPost(getTokenEndpoint());
        post.addHeader(HttpHeaders.AUTHORIZATION, "Basic " +
                Base64.encodeBase64String((clientId + ":" + clientSecret).getBytes()).trim());
        post.addHeader(HttpHeaders.CONTENT_TYPE, "application/x-www-form-urlencoded");
        List<NameValuePair> params = new ArrayList<>();
        params.add(new BasicNameValuePair("grant_type", OAuth2Constant.OAUTH2_GRANT_TYPE_RESOURCE_OWNER));
        params.add(new BasicNameValuePair("username", USER));
        params.add(new BasicNameValuePair("password", USER_PASSWORD));
        params.add(new BasicNameValuePair("scope", OAuth2Constant.OAUTH2_SCOPE_OPENID));
        post.setEntity(new UrlEncodedFormEntity(params));
        HttpResponse response = httpClient.execute(post);
        String responseStr = EntityUtils.toString(response.getEntity());
        JSONObject json = (JSONObject) JSONValue.parse(responseStr);
        Assert.assertNotNull(json.get(OAuth2Constant.ID_TOKEN), "Failed to get ID token: " + responseStr);
        return json.get(OAuth2Constant.ID_TOKEN).toString();
    }

    private String getTokenEndpoint() {

        if (MultitenantConstants.SUPER_TENANT_DOMAIN_NAME.equals(tenant)) {
            return serverURL + "oauth2/token";
        }
        return serverURL + "t/" + tenant + "/oauth2/token";
    }

    private static String getQueryParam(String url, String paramName) throws Exception {

        List<NameValuePair> params = URLEncodedUtils.parse(new URI(url), StandardCharsets.UTF_8);
        for (NameValuePair param : params) {
            if (paramName.equals(param.getName())) {
                return param.getValue();
            }
        }
        return null;
    }
}

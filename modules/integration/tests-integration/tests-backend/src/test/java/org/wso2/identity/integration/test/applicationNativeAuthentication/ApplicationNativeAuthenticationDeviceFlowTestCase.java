/*
 * Copyright (c) 2025, WSO2 LLC. (http://www.wso2.com).
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

package org.wso2.identity.integration.test.applicationNativeAuthentication;

import io.restassured.http.ContentType;
import io.restassured.response.ExtractableResponse;
import io.restassured.response.Response;
import org.apache.http.HttpHeaders;
import org.apache.http.HttpResponse;
import org.apache.http.HttpStatus;
import org.apache.http.NameValuePair;
import org.apache.http.client.config.CookieSpecs;
import org.apache.http.client.config.RequestConfig;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.config.Lookup;
import org.apache.http.config.RegistryBuilder;
import org.apache.http.cookie.CookieSpecProvider;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClientBuilder;
import org.apache.http.impl.cookie.RFC6265CookieSpecProvider;
import org.apache.http.message.BasicNameValuePair;
import org.apache.http.util.EntityUtils;
import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.json.simple.JSONValue;
import org.json.simple.parser.JSONParser;
import org.testng.Assert;
import org.testng.annotations.*;
import org.wso2.carbon.automation.engine.context.AutomationContext;
import org.wso2.carbon.automation.engine.context.TestUserMode;
import org.wso2.identity.integration.common.clients.UserManagementClient;
import org.wso2.identity.integration.test.oauth2.OAuth2ServiceAbstractIntegrationTest;
import org.wso2.identity.integration.test.rest.api.server.application.management.v1.model.*;
import org.wso2.identity.integration.test.utils.CommonConstants;
import org.wso2.identity.integration.test.utils.OAuth2Constant;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.*;

import static io.restassured.RestAssured.given;
import static org.wso2.identity.integration.test.applicationNativeAuthentication.Constants.*;
import static org.wso2.identity.integration.test.applicationNativeAuthentication.Constants.CONTENT_TYPE_APPLICATION_JSON;
import static org.wso2.identity.integration.test.utils.OAuth2Constant.SCOPE_PLAYGROUND_NAME;

/**
 * Integration test class for app-native device flow.
 */
public class ApplicationNativeAuthenticationDeviceFlowTestCase extends OAuth2ServiceAbstractIntegrationTest {

    private static final String CLIENT_ID_PARAM = "client_id";
    private static final String DEVICE_CODE = "device_code";
    private static final String USER_CODE = "user_code";
    private static final String INTERVAL = "interval";
    private static final String EXPIRES_IN = "expires_in";
    private static final String VERIFICATION_URI = "verification_uri";
    private static final String VERIFICATION_URI_COMPLETE = "verification_uri_complete";
    private static final String GRANT_TYPE = "urn:ietf:params:oauth:grant-type:device_code";
    private CloseableHttpClient client;
    private UserManagementClient userMgtServiceClient;
    private String appId;
    private String userCode;
    private String deviceCode;
    private String flowId;
    private String flowStatus;
    private String authenticatorId;
    private String href;
    private String deviceAuthPageEndpoint;
    private String deviceAuthEndpoint;
    private String deviceEndpoint;
    private String tokenEndpoint;

    @BeforeClass(alwaysRun = true)
    public void testInit() throws Exception {

        super.init(TestUserMode.SUPER_TENANT_USER);

        Lookup<CookieSpecProvider> cookieSpecRegistry = RegistryBuilder.<CookieSpecProvider>create()
                .register(CookieSpecs.DEFAULT, new RFC6265CookieSpecProvider())
                .build();
        RequestConfig requestConfig = RequestConfig.custom()
                .setCookieSpec(CookieSpecs.DEFAULT)
                .build();
        client = HttpClientBuilder.create()
                .setDefaultCookieSpecRegistry(cookieSpecRegistry)
                .setDefaultRequestConfig(requestConfig)
                .build();
        userMgtServiceClient = new UserManagementClient(backendURL, sessionCookie);
        userMgtServiceClient.addUser(TEST_USER_NAME, TEST_PASSWORD, null, TEST_PROFILE);

        setSystemproperties();
        setServerEndpoints();
    }

    private void setServerEndpoints() throws Exception {

        AutomationContext context = new AutomationContext("IDENTITY", TestUserMode.SUPER_TENANT_ADMIN);
        deviceAuthEndpoint = context.getContextUrls().getBackEndUrl()
                .replace("services/", "oauth2/device_authorize");
        deviceAuthPageEndpoint = context.getContextUrls().getBackEndUrl()
                .replace("services/", "authenticationendpoint/device.do");
        deviceEndpoint = context.getContextUrls().getBackEndUrl()
                .replace("services/", "oauth2/device");
        tokenEndpoint = context.getContextUrls().getBackEndUrl()
                .replace("services/", "oauth2/token");
    }

    @AfterClass(alwaysRun = true)
    public void atEnd() throws Exception {

        deleteApp(appId);
        userMgtServiceClient = new UserManagementClient(backendURL, sessionCookie);
        userMgtServiceClient.deleteUser(TEST_USER_NAME);
        // Nullifying attributes.
        consumerKey = null;
        consumerSecret = null;
        appId = null;
        flowId = null;
        flowStatus = null;
        authenticatorId = null;
        href = null;
        client.close();
        restClient.closeHttpClient();
    }

    @Test(groups = "wso2.is", description = "Check Oauth2 application registration")
    public void testRegisterApplication() throws Exception {

        ApplicationResponseModel application = createApp();
        Assert.assertNotNull(application, "OAuth App creation failed.");

        OpenIDConnectConfiguration oidcConfig = getOIDCInboundDetailsOfApplication(application.getId());

        consumerKey = oidcConfig.getClientId();
        Assert.assertNotNull(consumerKey, "Application creation failed.");

        consumerSecret = oidcConfig.getClientSecret();
        Assert.assertNotNull(consumerSecret, "Application creation failed.");
        appId = application.getId();
    }

    @Test(groups = "wso2.is", description = "Send authorize user request without redirect_uri param", dependsOnMethods
            = "testRegisterApplication")
    public void testSendDeviceAuthorize() throws Exception {

        List<NameValuePair> urlParameters = new ArrayList<>();
        urlParameters.add(new BasicNameValuePair(CLIENT_ID_PARAM, consumerKey));
        urlParameters.add(new BasicNameValuePair(SCOPE_PLAYGROUND_NAME, "device_01"));
        JSONObject responseObject = responseObjectNew(urlParameters, deviceAuthEndpoint);
        deviceCode = responseObject.get(DEVICE_CODE).toString();
        userCode = responseObject.get(USER_CODE).toString();
        Assert.assertNotNull(deviceCode, "device_code is null");
        Assert.assertNotNull(userCode, "user_code is null");
        Assert.assertEquals(responseObject.get(INTERVAL).toString(), "5",
                "interval period is incorrect.");
        Assert.assertEquals(responseObject.get(EXPIRES_IN).toString(), "600",
                "interval period is incorrect.");
        Assert.assertEquals(responseObject.get(VERIFICATION_URI).toString(), deviceAuthPageEndpoint,
                "verification uri is incorrect.");
        Assert.assertEquals(responseObject.get(VERIFICATION_URI_COMPLETE).toString(),
                deviceAuthPageEndpoint + "?user_code=" + userCode,
                "complete verification uri is incorrect.");
    }

    @Test(groups = "wso2.is", description = "Send init authorize POST request.",
            dependsOnMethods = "testSendDeviceAuthorize")
    public void testSendInitAuthRequestPost() throws Exception {

        List<NameValuePair> urlParameters = new ArrayList<>();
        urlParameters.add(new BasicNameValuePair(USER_CODE, userCode));
        urlParameters.add(new BasicNameValuePair("response_mode", RESPONSE_MODE));
        HttpResponse response = sendPostRequestWithParameters(client, urlParameters, deviceEndpoint);
        Assert.assertNotNull(response);
        String responseString = EntityUtils.toString(response.getEntity(), UTF_8);
        EntityUtils.consume(response.getEntity());
        JSONParser parser = new JSONParser();
        JSONObject json = (org.json.simple.JSONObject) parser.parse(responseString);
        Assert.assertNotNull(json, "Client Native Authentication Init response is null.");
        // TODO: Validate response
        validInitClientNativeAuthnResponse(json);
    }

    @Test(groups = "wso2.is", description = "Send Basic authentication POST request.",
            dependsOnMethods = "testSendInitAuthRequestPost")
    public void testSendBasicAuthRequestPost() {

        String body = "{\n" +
                "    \"flowId\": \"" + flowId + "\",\n" +
                "    \"selectedAuthenticator\": {\n" +
                "        \"authenticatorId\": \"" + authenticatorId + "\",\n" +
                "        \"params\": {\n" +
                "           \"username\": \"" + TEST_USER_NAME + "\",\n" +
                "           \"password\": \"" + TEST_PASSWORD + "\"\n" +
                "        }\n" +
                "    }\n" +
                "}";

        Response authnResponse = getResponseOfJSONPost(href, body, new HashMap<>());
        ExtractableResponse<Response> extractableResponse = authnResponse.then()
                .log().ifValidationFails()
                .assertThat()
                .statusCode(HttpStatus.SC_OK)
                .and()
                .assertThat()
                .header(HttpHeaders.CONTENT_TYPE, CONTENT_TYPE_APPLICATION_JSON)
                .extract();
        Assert.assertNotNull(extractableResponse, "Basic Authentication request failed. Authentication response is null.");

        validateBasicAuthenticationResponseBody(extractableResponse);
    }

    @Test(groups = "wso2.is", description = "Send token post request", dependsOnMethods = "testSendBasicAuthRequestPost")
    public void testTokenRequest() throws Exception {

        // Wait 5 seconds because of the token polling interval.
        Thread.sleep(5000);
        JSONObject obj = sendTokenRequest(GRANT_TYPE, consumerKey, deviceCode);
        String accessToken = obj.get("access_token").toString();
        Assert.assertNotNull(accessToken, "Assess token is null");
    }

    private ApplicationResponseModel createApp() throws Exception {

        ApplicationModel application = new ApplicationModel();

        List<String> grantTypes = new ArrayList<>();
        Collections.addAll(grantTypes, GRANT_TYPE);


        OpenIDConnectConfiguration oidcConfig = new OpenIDConnectConfiguration();
        oidcConfig.setGrantTypes(grantTypes);
        oidcConfig.setPublicClient(true);

        InboundProtocols inboundProtocolsConfig = new InboundProtocols();
        inboundProtocolsConfig.setOidc(oidcConfig);

        AdvancedApplicationConfiguration advancedAppConfig = new AdvancedApplicationConfiguration();
        advancedAppConfig.setEnableAPIBasedAuthentication(true);

        application.setInboundProtocolConfiguration(inboundProtocolsConfig);
        application.setAdvancedConfigurations(advancedAppConfig);
        application.setName(TEST_APP_NAME);

        String appId = addApplication(application);
        return getApplication(appId);
    }

    private JSONObject responseObjectNew(List<NameValuePair> postParameters, String uri) throws Exception {

        HttpPost httpPost = new HttpPost(uri);
        //generate post request
        httpPost.setHeader("Content-Type", "application/x-www-form-urlencoded");
        httpPost.setEntity(new UrlEncodedFormEntity(postParameters));
        HttpResponse response = client.execute(httpPost);
        String responseString = EntityUtils.toString(response.getEntity(), "UTF-8");
        EntityUtils.consume(response.getEntity());

        JSONParser parser = new JSONParser();
        JSONObject json = (JSONObject) parser.parse(responseString);
        if (json == null) {
            throw new Exception(
                    "Error occurred while getting the response");
        }

        return json;
    }

    private Response getResponseOfJSONPost(String endpointUri, String body, Map<String, String> headers) {

        return given()
                .contentType(ContentType.JSON)
                .headers(headers)
                .body(body)
                .when()
                .post(endpointUri);
    }

    private void validInitClientNativeAuthnResponse(JSONObject json) {

        // Check for the presence of required keys and their expected types
        if (json.containsKey(FLOW_ID) && json.containsKey(FLOW_STATUS) && json.containsKey(FLOW_TYPE) &&
                json.containsKey(NEXT_STEP) && json.containsKey(LINKS)) {

            flowId = (String) json.get(FLOW_ID);
            flowStatus = (String) json.get(FLOW_STATUS);

            JSONObject nextStepNode = (JSONObject) json.get(NEXT_STEP);
            if (nextStepNode.containsKey(STEP_TYPE) && nextStepNode.containsKey(AUTHENTICATORS)) {
                JSONArray authenticatorsArray = (JSONArray) nextStepNode.get(AUTHENTICATORS);
                if (!authenticatorsArray.isEmpty()) {
                    JSONObject authenticator = (JSONObject) authenticatorsArray.get(0);
                    if (authenticator.containsKey(AUTHENTICATOR_ID) && authenticator.containsKey(AUTHENTICATOR) &&
                            authenticator.containsKey(IDP) && authenticator.containsKey(METADATA) &&
                            authenticator.containsKey(REQUIRED_PARAMS)) {

                        authenticatorId = (String) authenticator.get(AUTHENTICATOR_ID);
                        JSONObject metadataNode = (JSONObject) authenticator.get(METADATA);
                        if (metadataNode.containsKey(PROMPT_TYPE) && metadataNode.containsKey(PARAMS)) {
                            JSONArray paramsArray = (JSONArray) metadataNode.get(PARAMS);
                            if (paramsArray.isEmpty()) {
                                Assert.fail("Content of param for the authenticator is null in " +
                                        "Client native authentication JSON Response.");
                            }
                        } else {
                            Assert.fail("Params for the authenticator is null in " +
                                    "Client native authentication JSON Response.");
                        }
                    }
                } else {
                    Assert.fail("Authenticator is not expected format in Client native authentication");
                }
            } else {
                Assert.fail("Authenticators in Client native authentication JSON Response is null, " +
                        "expecting list of Authentication.");
            }
            JSONArray links = (JSONArray) json.get(LINKS);
            JSONObject link = (JSONObject) links.get(0);
            if (link.containsKey(HREF)) {
                href = link.get(HREF).toString();
            } else {
                Assert.fail("Link is not available for next step in Client native authentication JSON Response.");
            }
        } else {
            Assert.fail("Client native authentication JSON Response is not in expected format.");
        }
    }

    private void validateBasicAuthenticationResponseBody(ExtractableResponse<Response> extractableResponse) {

        // Validate specific fields in the JSON response
        flowStatus = extractableResponse
                .jsonPath()
                .getString(FLOW_STATUS);
        Assert.assertEquals(flowStatus, SUCCESS_COMPLETED);

        String appName = extractableResponse
                .jsonPath()
                .getString(AUTH_DATA_APP_NAME);
        Assert.assertEquals(appName, TEST_APP_NAME);
    }

    private JSONObject sendTokenRequest(String grantType, String clientId, String deviceCode) throws IOException {

        List<NameValuePair> urlParameters = new ArrayList<>();
        urlParameters.add(new BasicNameValuePair(OAuth2Constant.GRANT_TYPE_NAME, grantType));
        urlParameters.add(new BasicNameValuePair(CLIENT_ID_PARAM, clientId));
        urlParameters.add(new BasicNameValuePair(DEVICE_CODE, deviceCode));

        HttpPost request = new HttpPost(tokenEndpoint);
        request.setHeader(CommonConstants.USER_AGENT_HEADER, OAuth2Constant.USER_AGENT);
        request.setEntity(new UrlEncodedFormEntity(urlParameters));
        HttpResponse response = client.execute(request);

        try (BufferedReader responseBuffer =
                     new BufferedReader(new InputStreamReader(response.getEntity().getContent()))) {
            return (JSONObject) JSONValue.parse(responseBuffer);
        }
    }
}

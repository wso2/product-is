/*
 * Copyright (c) 2021, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
 *
 * WSO2 Inc. licenses this file to you under the Apache License,
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

import io.restassured.http.ContentType;
import io.restassured.response.Response;
import org.apache.http.Header;
import org.apache.http.HttpHeaders;
import org.apache.http.HttpResponse;
import org.apache.http.HttpStatus;
import org.apache.http.NameValuePair;
import org.apache.http.client.HttpClient;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.impl.client.BasicCookieStore;
import org.apache.http.impl.client.HttpClientBuilder;
import org.apache.http.message.BasicNameValuePair;
import org.apache.http.util.EntityUtils;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import org.testng.Assert;
import org.testng.annotations.AfterClass;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;
import org.wso2.carbon.automation.engine.context.TestUserMode;
import org.wso2.carbon.identity.oauth.stub.dto.OAuthConsumerAppDTO;
import org.wso2.carbon.um.ws.api.stub.ClaimValue;
import org.wso2.identity.integration.test.util.Utils;
import org.wso2.identity.integration.test.utils.CommonConstants;
import org.wso2.identity.integration.test.utils.DataExtractUtil;
import org.wso2.identity.integration.test.utils.OAuth2Constant;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static io.restassured.RestAssured.given;
import static org.hamcrest.CoreMatchers.notNullValue;
import static org.hamcrest.core.Is.is;

/**
 * This test class is used to check the behaviour of token revocation when user session is terminated via Session
 * termination REST API. When the accesstoken is issued, we store a mapping to session to token (irrespective of
 * binding type). So when the session is terminated via REST API, the respective token should be revoked.
 */
public class OAuth2TokenRevocationWithSessionTerminationTestCase extends OAuth2ServiceAbstractIntegrationTest {

    private String consumerKey;
    private String consumerSecret;
    private String sessionDataKeyConsent;
    private String sessionDataKey;
    private String authorizationCode;
    private HttpClient client;
    private String accessToken;
    private List<String> sessionIdList;
    private static final String SESSION_API_ENDPOINT = "https://localhost:9853/t/carbon.super/api/users/v1/me/sessions";
    private static final String USER_EMAIL = "authTokenRevokeUser@wso2.com";
    private static final String USERNAME = "authTokenRevokeUser";
    private static final String PASSWORD = "AuthTokenRevokeUser@123";
    private static final String emailClaimURI = "http://wso2.org/claims/emailaddress";

    @BeforeClass(alwaysRun = true)
    public void testInit() throws Exception {

        super.init();
        setSystemproperties();
        remoteUSMServiceClient.addUser(USERNAME, PASSWORD, new String[]{"admin"},
                getUserClaims(), "default", true);
        client = HttpClientBuilder.create().setDefaultCookieStore(new BasicCookieStore()).build();
    }

    @AfterClass(alwaysRun = true)
    public void testConclude() throws Exception {

        deleteApplication();
    }

    @Test(groups = "wso2.is", description = "Create OAuth2 application")
    public void testRegisterApplication() throws Exception {

        OAuthConsumerAppDTO appDto = createApplication();
        Assert.assertNotNull(appDto, "Application creation failed.");
        consumerKey = appDto.getOauthConsumerKey();
        Assert.assertNotNull(consumerKey, "OAuth clientId is invalid.");
        consumerSecret = appDto.getOauthConsumerSecret();
    }

    @Test(groups = "wso2.is", dependsOnMethods = {"testRegisterApplication"}, description = "Test login using OpenId " +
            "connect authorization code flow")
    public void testOIDCLogin() throws Exception {

        initiateAuthorizationRequest();
        authenticateUser();
        performConsentApproval();
        generateAuthzCodeAccessToken();
        introspectActiveAccessToken();
    }

    @Test(groups = "wso2.is", dependsOnMethods = {"testOIDCLogin"}, description = "Get User session using session " +
            "management REST API")
    public void testGetUserSessions() {

        Response response = getResponseOfGet(SESSION_API_ENDPOINT);
        response.then()
                .assertThat()
                .statusCode(HttpStatus.SC_OK)
                .log().ifValidationFails()
                .body("size()", notNullValue())
                .body("userId", notNullValue())
                .body("sessions", notNullValue())
                .body("sessions.applications", notNullValue())
                .body("sessions.applications.subject", notNullValue())
                .body("sessions.applications.appName[0]", notNullValue())
                .body("sessions.applications.appId", notNullValue())
                .body("sessions.userAgent", notNullValue())
                .body("sessions.ip", notNullValue())
                .body("sessions.loginTime", notNullValue())
                .body("sessions.lastAccessTime", notNullValue())
                .body("sessions.id", notNullValue());
        sessionIdList = response.jsonPath().getList("sessions.id");
        Assert.assertEquals(sessionIdList.size(), 1);
    }

    @Test(groups = "wso2.is", dependsOnMethods = {"testGetUserSessions"}, description = "Terminate User session using" +
            " session management REST API")
    public void testDeleteUserSessionById() {

        String endpointURI = SESSION_API_ENDPOINT + "/" + sessionIdList.get(0);
        // Delete the sessionId using session management api.
        getResponseOfDelete(endpointURI).then()
                .log().ifValidationFails()
                .assertThat()
                .statusCode(HttpStatus.SC_NO_CONTENT);
        // Get all sessions.
        getResponseOfGet(SESSION_API_ENDPOINT)
                .then()
                .log().ifValidationFails()
                .assertThat()
                .statusCode(HttpStatus.SC_OK)
                .body("size()", is(0));
    }

    @Test(groups = "wso2.is", dependsOnMethods = {"testDeleteUserSessionById"}, description = " The expected " +
            "behaviour is, When the session is terminated via REST API, the corresponding mapped accesstoken " +
            "should be revoked")
    public void testTokenRevocationWhenSessionIsTerminated() throws Exception {

        JSONObject object = testIntrospectionEndpoint();
        Assert.assertEquals(object.get("active"), false);
    }

    /**
     * Playground app will initiate authorization request to IS and obtain session data key.
     *
     * @throws IOException
     */
    private void initiateAuthorizationRequest() throws IOException {

        List<NameValuePair> urlParameters = getOIDCInitiationRequestParams();
        HttpResponse response = sendPostRequestWithParameters(client, urlParameters,
                OAuth2Constant.AUTHORIZED_USER_URL);
        Assert.assertNotNull(response, "Authorization response is null");

        Header locationHeader = response.getFirstHeader(OAuth2Constant.HTTP_RESPONSE_HEADER_LOCATION);
        Assert.assertNotNull(locationHeader, "Authorization response header is null.");

        EntityUtils.consume(response.getEntity());
        response = sendGetRequest(client, locationHeader.getValue());
        sessionDataKey = Utils.extractDataFromResponse(response, CommonConstants.SESSION_DATA_KEY, 1);
        EntityUtils.consume(response.getEntity());
    }

    /**
     * Provide user credentials and authenticate to the system.
     *
     * @throws IOException
     */
    private void authenticateUser() throws IOException {

        // Pass user credentials to commonauth endpoint and authenticate the user.
        HttpResponse response = sendLoginPostForCustomUsers(client, sessionDataKey, USERNAME, PASSWORD);
        Assert.assertNotNull(response, "OIDC login request response is null.");
        Header locationHeader = response.getFirstHeader(OAuth2Constant.HTTP_RESPONSE_HEADER_LOCATION);
        Assert.assertNotNull(locationHeader, "OIDC login response header is null.");
        EntityUtils.consume(response.getEntity());
        // Get the sessionDatakeyConsent from the redirection after authenticating the user.
        response = sendGetRequest(client, locationHeader.getValue());
        Map<String, Integer> keyPositionMap = new HashMap<>(1);
        keyPositionMap.put("name=\"sessionDataKeyConsent\"", 1);
        List<DataExtractUtil.KeyValue> keyValues = DataExtractUtil.extractSessionConsentDataFromResponse(response,
                keyPositionMap);
        Assert.assertNotNull(keyValues, "SessionDataKeyConsent keyValues map is null.");
        sessionDataKeyConsent = keyValues.get(0).getValue();
        Assert.assertNotNull(sessionDataKeyConsent, "sessionDataKeyConsent is null.");
        EntityUtils.consume(response.getEntity());
    }

    /**
     * Approve the consent.
     *
     * @throws IOException
     */
    private void performConsentApproval() throws IOException {

        HttpResponse response = sendApprovalPostWithConsent(client, sessionDataKeyConsent, null);
        Assert.assertNotNull(response, "OIDC consent approval request response is null.");
        Header locationHeader = response.getFirstHeader(OAuth2Constant.HTTP_RESPONSE_HEADER_LOCATION);
        Assert.assertNotNull(locationHeader, "OIDC consent approval request location header is null.");
        EntityUtils.consume(response.getEntity());
        // Get authorization code flow.
        response = sendPostRequest(client, locationHeader.getValue());
        Map<String, Integer> keyPositionMap = new HashMap<>(1);
        keyPositionMap.put("Authorization Code", 1);
        List<DataExtractUtil.KeyValue> keyValues = DataExtractUtil.extractTableRowDataFromResponse(response,
                keyPositionMap);
        Assert.assertNotNull(keyValues, "Authorization code not received.");
        authorizationCode = keyValues.get(0).getValue();
        Assert.assertNotNull(authorizationCode, "Authorization code not received.");
        EntityUtils.consume(response.getEntity());
    }

    /**
     * Exchange authorization code and get accesstoken.
     *
     * @throws Exception
     */
    private void generateAuthzCodeAccessToken() throws Exception {

        List<NameValuePair> urlParameters = new ArrayList<>();
        urlParameters.add(new BasicNameValuePair(OAuth2Constant.GRANT_TYPE_NAME,
                OAuth2Constant.OAUTH2_GRANT_TYPE_AUTHORIZATION_CODE));
        urlParameters.add(new BasicNameValuePair(OAuth2Constant.OAUTH2_REDIRECT_URI, OAuth2Constant.CALLBACK_URL));
        urlParameters.add(new BasicNameValuePair(OAuth2Constant.AUTHORIZATION_CODE_NAME, authorizationCode));
        JSONObject jsonResponse = responseObject(OAuth2Constant.ACCESS_TOKEN_ENDPOINT, urlParameters, consumerKey,
                consumerSecret);
        Assert.assertNotNull(jsonResponse.get(OAuth2Constant.ACCESS_TOKEN), "Access token is null.");
        Assert.assertNotNull(jsonResponse.get(OAuth2Constant.REFRESH_TOKEN), "Refresh token is null.");
        accessToken = (String) jsonResponse.get(OAuth2Constant.ACCESS_TOKEN);
    }

    private List<NameValuePair> getOIDCInitiationRequestParams() {

        List<NameValuePair> urlParameters = new ArrayList<NameValuePair>();
        urlParameters.add(new BasicNameValuePair("grantType", OAuth2Constant.OAUTH2_GRANT_TYPE_CODE));
        urlParameters.add(new BasicNameValuePair("consumerKey", consumerKey));
        urlParameters.add(new BasicNameValuePair("callbackurl", OAuth2Constant.CALLBACK_URL));
        urlParameters.add(new BasicNameValuePair("authorizeEndpoint", OAuth2Constant.APPROVAL_URL));
        urlParameters.add(new BasicNameValuePair("authorize", OAuth2Constant.AUTHORIZE_PARAM));
        urlParameters.add(new BasicNameValuePair("scope", OAuth2Constant.OAUTH2_SCOPE_OPENID + " " +
                OAuth2Constant.OAUTH2_SCOPE_EMAIL));
        return urlParameters;
    }

    /**
     * Introspect the obtained accesstoken and it should be an active token.
     *
     * @throws Exception
     */
    private void introspectActiveAccessToken() throws Exception {

        JSONObject object = testIntrospectionEndpoint();
        Assert.assertEquals(object.get("active"), true);
    }

    /**
     * Invoke given endpointUri for GET with given body and Basic authentication.
     *
     * @param endpointURI EndpointURI.
     * @return Response Response of the GET request.
     */
    private Response getResponseOfGet(String endpointURI) {

        return given().auth().preemptive().basic(USERNAME, PASSWORD)
                .contentType(ContentType.JSON)
                .header(HttpHeaders.ACCEPT, ContentType.JSON)
                .log().ifValidationFails()
                .when()
                .get(endpointURI);
    }

    /**
     * Invoke given endpointUri for DELETE with given body and Basic authentication, authentication credential being
     * the authenticatingUserName and authenticatingCredential.
     *
     * @param endpointURI EndpointURI.
     * @return Response Response of the DELETE request.
     */
    private Response getResponseOfDelete(String endpointURI) {

        return given().auth().preemptive().basic(USERNAME, PASSWORD)
                .contentType(ContentType.JSON)
                .header(HttpHeaders.ACCEPT, ContentType.JSON)
                .log().ifValidationFails().log().ifValidationFails()
                .when().log().ifValidationFails()
                .delete(endpointURI);
    }

    /**
     * Get introspection endpoint response by callling introspection endpoint.
     *
     * @return JSONObject
     * @throws Exception
     */
    private JSONObject testIntrospectionEndpoint() throws Exception {

        List<NameValuePair> urlParameters = new ArrayList<NameValuePair>();
        urlParameters.add(new BasicNameValuePair("token", accessToken));
        return responseObject(OAuth2Constant.INTRO_SPEC_ENDPOINT, urlParameters, USERNAME, PASSWORD);
    }

    /**
     * Build post request and return json response object.
     *
     * @param endpoint       Endpoint.
     * @param postParameters postParameters.
     * @param key            Basic authentication key.
     * @param secret         Basic authentication secret.
     * @return JSON object of the response.
     * @throws Exception
     */
    private JSONObject responseObject(String endpoint, List<NameValuePair> postParameters, String key, String secret)
            throws Exception {

        HttpPost httpPost = new HttpPost(endpoint);
        httpPost.setHeader("Authorization", "Basic " + getBase64EncodedString(key, secret));
        httpPost.setHeader("Content-Type", "application/x-www-form-urlencoded");
        httpPost.setEntity(new UrlEncodedFormEntity(postParameters));
        HttpResponse response = client.execute(httpPost);
        String responseString = EntityUtils.toString(response.getEntity(), "UTF-8");
        EntityUtils.consume(response.getEntity());
        JSONParser parser = new JSONParser();
        JSONObject json = (JSONObject) parser.parse(responseString);
        if (json == null) {
            throw new Exception("Error occurred while getting the response.");
        }
        return json;
    }

    /**
     * Crete claim values array.
     *
     * @return Array of claim values.
     */
    protected ClaimValue[] getUserClaims() {

        ClaimValue[] claimValues = new ClaimValue[1];

        ClaimValue email = new ClaimValue();
        email.setClaimURI(emailClaimURI);
        email.setValue(USER_EMAIL);
        claimValues[0] = email;

        return claimValues;
    }
}

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

package org.wso2.identity.integration.test.oauth2.dcrm.api;

import org.apache.commons.codec.binary.Base64;
import org.apache.http.HttpHeaders;
import org.apache.http.HttpResponse;
import org.apache.http.NameValuePair;
import org.apache.http.client.HttpClient;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.client.methods.HttpDelete;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.client.methods.HttpPut;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.message.BasicNameValuePair;
import org.apache.http.util.EntityUtils;
import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.json.simple.JSONValue;
import org.testng.annotations.AfterClass;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.DataProvider;
import org.testng.annotations.Factory;
import org.testng.annotations.Test;
import org.wso2.carbon.automation.engine.context.AutomationContext;
import org.wso2.carbon.automation.engine.context.TestUserMode;
import org.wso2.identity.integration.common.utils.ISIntegrationTest;
import org.wso2.identity.integration.test.oauth2.dcrm.api.util.OAuthDCRMConstants;
import org.wso2.identity.integration.test.utils.OAuth2Constant;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.List;

import static org.testng.Assert.assertEquals;
import static org.testng.Assert.assertFalse;
import static org.testng.Assert.assertNotNull;
import static org.testng.Assert.assertTrue;

/**
 * OAuth2 DCRM API client secret expiry test case.
 */
public class OAuthDCRMClientSecretExpiryTestCase extends ISIntegrationTest {

    private static final String SUPER_TENANT_DOMAIN = "carbon.super";
    private static final String EXT_PARAM_CLIENT_SECRET_EXPIRES_AT = "ext_param_client_secret_expires_at";
    private static final String CLIENT_SECRET_EXPIRES_AT = "client_secret_expires_at";
    private static final String INVALID_CLIENT_METADATA = "invalid_client_metadata";
    private static final String INVALID_CLIENT = "invalid_client";
    private static final String EXPIRY_IN_PAST_ERROR = "is in the past";
    private static final String EXPIRY_INVALID_ERROR = "is invalid";

    private static final String EXPIRING_APP = "dcrmExpiringSecretApp";
    private static final String EXPIRING_APP_UPDATED = "dcrmExpiringSecretAppUpdated";
    private static final String DEFAULT_EXPIRY_APP = "dcrmDefaultSecretExpiryApp";
    private static final String NEVER_EXPIRING_APP = "dcrmNeverExpiringSecretApp";
    private static final String PAST_EXPIRY_APP = "dcrmPastSecretExpiryApp";
    private static final String INVALID_EXPIRY_APP = "dcrmInvalidSecretExpiryApp";

    private static final long NEVER_EXPIRES = 0L;
    private static final long EXPIRY_OFFSET_IN_SECONDS = 3600L;
    private static final long INVALID_EXPIRY = -1L;

    private final List<String> registeredClientIds = new ArrayList<>();

    private HttpClient client;

    private String username;
    private String password;
    private String tenant;

    private String clientId;
    private String clientSecret;
    private long secretExpiry;

    @Factory(dataProvider = "dcrmConfigProvider")
    public OAuthDCRMClientSecretExpiryTestCase(TestUserMode userMode) throws Exception {

        AutomationContext context = new AutomationContext("IDENTITY", userMode);
        this.username = context.getContextTenant().getTenantAdmin().getUserName();
        this.password = context.getContextTenant().getTenantAdmin().getPassword();
        this.tenant = context.getContextTenant().getDomain();
    }

    @DataProvider(name = "dcrmConfigProvider")
    public static Object[][] dcrmConfigProvider() {

        return new Object[][]{{TestUserMode.SUPER_TENANT_ADMIN}, {TestUserMode.TENANT_ADMIN}};
    }

    @BeforeClass(alwaysRun = true)
    public void testInit() throws Exception {

        super.init();
        client = HttpClients.createDefault();
    }

    @AfterClass(alwaysRun = true)
    public void testCleanup() throws Exception {

        for (String registeredClientId : registeredClientIds) {
            HttpDelete request = new HttpDelete(getPath() + registeredClientId);
            request.addHeader(HttpHeaders.AUTHORIZATION, getAuthzHeader());
            EntityUtils.consume(client.execute(request).getEntity());
        }
        registeredClientIds.clear();
    }

    @Test(alwaysRun = true, groups = "wso2.is", priority = 1, description = "Register an application with a future " +
            "client secret expiry")
    public void testRegisterApplicationWithFutureSecretExpiry() throws IOException {

        secretExpiry = currentTimeInSeconds() + EXPIRY_OFFSET_IN_SECONDS;
        JSONObject registrationRequest = buildRegistrationRequest(EXPIRING_APP);
        registrationRequest.put(EXT_PARAM_CLIENT_SECRET_EXPIRES_AT, secretExpiry);

        HttpResponse response = registerApplication(registrationRequest);
        assertEquals(response.getStatusLine().getStatusCode(), 201, "Application with a future client secret " +
                "expiry has not been registered successfully");

        JSONObject registeredApp = parseJSONResponse(response);
        clientId = getRequiredValue(registeredApp, OAuthDCRMConstants.CLIENT_ID);
        clientSecret = getRequiredValue(registeredApp, OAuthDCRMConstants.CLIENT_SECRET);
        registeredClientIds.add(clientId);

        assertEquals(getClientSecretExpiresAt(registeredApp), secretExpiry, "The registration response should echo " +
                "the requested client secret expiry");

        HttpResponse tokenResponse = requestClientCredentialsToken(clientId, clientSecret);
        assertTokenIssued(tokenResponse);
    }

    @Test(alwaysRun = true, groups = "wso2.is", priority = 2, description = "Register an application without a " +
            "client secret expiry")
    public void testRegisterApplicationWithoutSecretExpiry() throws IOException {

        HttpResponse response = registerApplication(buildRegistrationRequest(DEFAULT_EXPIRY_APP));
        assertEquals(response.getStatusLine().getStatusCode(), 201, "Application without a client secret expiry " +
                "has not been registered successfully");

        JSONObject registeredApp = parseJSONResponse(response);
        registeredClientIds.add(getRequiredValue(registeredApp, OAuthDCRMConstants.CLIENT_ID));

        assertEquals(getClientSecretExpiresAt(registeredApp), NEVER_EXPIRES, "A client secret registered without " +
                "an expiry should be reported as never expiring");
    }

    @Test(alwaysRun = true, groups = "wso2.is", priority = 3, description = "Register an application with a zero " +
            "client secret expiry")
    public void testRegisterApplicationWithZeroSecretExpiry() throws IOException {

        JSONObject registrationRequest = buildRegistrationRequest(NEVER_EXPIRING_APP);
        registrationRequest.put(EXT_PARAM_CLIENT_SECRET_EXPIRES_AT, NEVER_EXPIRES);

        HttpResponse response = registerApplication(registrationRequest);
        assertEquals(response.getStatusLine().getStatusCode(), 201, "Application with a zero client secret expiry " +
                "has not been registered successfully");

        JSONObject registeredApp = parseJSONResponse(response);
        registeredClientIds.add(getRequiredValue(registeredApp, OAuthDCRMConstants.CLIENT_ID));

        assertEquals(getClientSecretExpiresAt(registeredApp), NEVER_EXPIRES, "A zero client secret expiry should be " +
                "echoed as zero");
    }

    @Test(alwaysRun = true, groups = "wso2.is", priority = 4, description = "Register an application with a past " +
            "client secret expiry")
    public void testRegisterApplicationWithPastSecretExpiry() throws IOException {

        JSONObject registrationRequest = buildRegistrationRequest(PAST_EXPIRY_APP);
        registrationRequest.put(EXT_PARAM_CLIENT_SECRET_EXPIRES_AT, currentTimeInSeconds() - EXPIRY_OFFSET_IN_SECONDS);

        JSONObject error = assertClientMetadataError(registerApplication(registrationRequest));
        String errorDescription = getRequiredValue(error, OAuthDCRMConstants.ERROR_DESCRIPTION);

        assertTrue(errorDescription.contains(EXPIRY_IN_PAST_ERROR), "A past client secret expiry should be rejected " +
                "with the past expiry description, but received: " + errorDescription);
        assertFalse(errorDescription.contains(EXPIRY_INVALID_ERROR), "A past client secret expiry should not be " +
                "reported as an invalid expiry");
    }

    @Test(alwaysRun = true, groups = "wso2.is", priority = 5, description = "Register an application with a negative " +
            "client secret expiry")
    public void testRegisterApplicationWithNegativeSecretExpiry() throws IOException {

        JSONObject registrationRequest = buildRegistrationRequest(INVALID_EXPIRY_APP);
        registrationRequest.put(EXT_PARAM_CLIENT_SECRET_EXPIRES_AT, INVALID_EXPIRY);

        JSONObject error = assertClientMetadataError(registerApplication(registrationRequest));
        String errorDescription = getRequiredValue(error, OAuthDCRMConstants.ERROR_DESCRIPTION);

        assertTrue(errorDescription.contains(EXPIRY_INVALID_ERROR), "A negative client secret expiry should be " +
                "rejected with the invalid expiry description, but received: " + errorDescription);
        assertFalse(errorDescription.contains(EXPIRY_IN_PAST_ERROR), "A negative client secret expiry should not be " +
                "reported as a past expiry");
    }

    @Test(alwaysRun = true, groups = "wso2.is", priority = 6, description = "Read an application with a client " +
            "secret expiry")
    public void testReadApplicationWithSecretExpiry() throws IOException {

        HttpGet request = new HttpGet(getPath() + clientId);
        request.addHeader(HttpHeaders.AUTHORIZATION, getAuthzHeader());
        request.addHeader(HttpHeaders.CONTENT_TYPE, OAuthDCRMConstants.CONTENT_TYPE);

        HttpResponse response = client.execute(request);
        assertEquals(response.getStatusLine().getStatusCode(), 200, "Application read request has not returned with " +
                "successful response");

        JSONObject application = parseJSONResponse(response);
        assertEquals(getClientSecretExpiresAt(application), secretExpiry, "The read response should echo the client " +
                "secret expiry set at registration");
    }

    @Test(alwaysRun = true, groups = "wso2.is", priority = 7, description = "Update an application and verify the " +
            "client secret and its expiry are preserved")
    public void testUpdateApplicationPreservesSecretAndExpiry() throws IOException {

        JSONObject updateRequest = new JSONObject();
        updateRequest.put(OAuthDCRMConstants.CLIENT_NAME, EXPIRING_APP_UPDATED);

        JSONObject updatedApp = parseJSONResponse(updateApplication(updateRequest));
        assertEquals(getRequiredValue(updatedApp, OAuthDCRMConstants.CLIENT_NAME), EXPIRING_APP_UPDATED,
                "The client name has not been updated");
        assertEquals(getRequiredValue(updatedApp, OAuthDCRMConstants.CLIENT_SECRET), clientSecret, "The client " +
                "secret should be preserved across an update");
        assertEquals(getClientSecretExpiresAt(updatedApp), secretExpiry, "The client secret expiry should be " +
                "preserved across an update");

        HttpResponse tokenResponse = requestClientCredentialsToken(clientId, clientSecret);
        assertTokenIssued(tokenResponse);
    }

    @Test(alwaysRun = true, groups = "wso2.is", priority = 8, description = "Update an application with a new client " +
            "secret and verify it is not applied")
    public void testUpdateApplicationDoesNotApplyClientSecret() throws IOException {

        JSONObject updateRequest = new JSONObject();
        updateRequest.put(OAuthDCRMConstants.CLIENT_NAME, EXPIRING_APP_UPDATED);
        updateRequest.put(OAuthDCRMConstants.CLIENT_SECRET, OAuthDCRMConstants.INVALID_CLIENT_SECRET);

        JSONObject updatedApp = parseJSONResponse(updateApplication(updateRequest));
        assertEquals(getRequiredValue(updatedApp, OAuthDCRMConstants.CLIENT_SECRET), clientSecret, "A client secret " +
                "sent in an update request should not replace the existing client secret");
        assertEquals(getClientSecretExpiresAt(updatedApp), secretExpiry, "The client secret expiry should be " +
                "preserved when an update request carries a client secret");

        HttpResponse tokenResponse = requestClientCredentialsToken(clientId, clientSecret);
        assertTokenIssued(tokenResponse);
    }

    @Test(alwaysRun = true, groups = "wso2.is", priority = 9, description = "Delete an application and verify its " +
            "client secret no longer authenticates")
    public void testDeleteApplicationInvalidatesSecret() throws IOException {

        HttpDelete request = new HttpDelete(getPath() + clientId);
        request.addHeader(HttpHeaders.AUTHORIZATION, getAuthzHeader());

        HttpResponse response = client.execute(request);
        assertEquals(response.getStatusLine().getStatusCode(), 204, "Application has not been deleted successfully");
        EntityUtils.consume(response.getEntity());

        HttpResponse tokenResponse = requestClientCredentialsToken(clientId, clientSecret);
        assertEquals(tokenResponse.getStatusLine().getStatusCode(), 401, "The client secret of a deleted application " +
                "should not authenticate at the token endpoint");
        assertEquals(getRequiredValue(parseJSONResponse(tokenResponse), OAuthDCRMConstants.ERROR), INVALID_CLIENT,
                "Invalid error message");
    }

    private JSONObject buildRegistrationRequest(String applicationName) {

        JSONArray grantTypes = new JSONArray();
        grantTypes.add(OAuth2Constant.OAUTH2_GRANT_TYPE_CLIENT_CREDENTIALS);

        JSONObject registrationRequest = new JSONObject();
        registrationRequest.put(OAuthDCRMConstants.CLIENT_NAME, applicationName);
        registrationRequest.put(OAuthDCRMConstants.GRANT_TYPES, grantTypes);
        return registrationRequest;
    }

    private HttpResponse registerApplication(JSONObject registrationRequest) throws IOException {

        HttpPost request = new HttpPost(getPath());
        request.addHeader(HttpHeaders.AUTHORIZATION, getAuthzHeader());
        request.addHeader(HttpHeaders.CONTENT_TYPE, OAuthDCRMConstants.CONTENT_TYPE);
        request.setEntity(new StringEntity(registrationRequest.toJSONString()));

        return client.execute(request);
    }

    private HttpResponse updateApplication(JSONObject updateRequest) throws IOException {

        HttpPut request = new HttpPut(getPath() + clientId);
        request.addHeader(HttpHeaders.AUTHORIZATION, getAuthzHeader());
        request.addHeader(HttpHeaders.CONTENT_TYPE, OAuthDCRMConstants.CONTENT_TYPE);
        request.setEntity(new StringEntity(updateRequest.toJSONString()));

        HttpResponse response = client.execute(request);
        assertEquals(response.getStatusLine().getStatusCode(), 200, "Application has not been updated successfully");
        return response;
    }

    private HttpResponse requestClientCredentialsToken(String consumerKey, String consumerSecret) throws IOException {

        HttpPost request = new HttpPost(getTokenEndpoint());
        request.addHeader(HttpHeaders.AUTHORIZATION, getBasicAuthHeader(consumerKey, consumerSecret));

        List<NameValuePair> parameters = new ArrayList<>();
        parameters.add(new BasicNameValuePair(OAuth2Constant.GRANT_TYPE_NAME,
                OAuth2Constant.OAUTH2_GRANT_TYPE_CLIENT_CREDENTIALS));
        request.setEntity(new UrlEncodedFormEntity(parameters));

        return client.execute(request);
    }

    private void assertTokenIssued(HttpResponse tokenResponse) throws IOException {

        assertEquals(tokenResponse.getStatusLine().getStatusCode(), 200, "The client secret should authenticate at " +
                "the token endpoint");
        assertNotNull(parseJSONResponse(tokenResponse).get(OAuth2Constant.ACCESS_TOKEN), "access_token cannot be null");
    }

    private JSONObject assertClientMetadataError(HttpResponse response) throws IOException {

        assertEquals(response.getStatusLine().getStatusCode(), 400, "An unacceptable client secret expiry should " +
                "have returned a bad request");

        JSONObject error = parseJSONResponse(response);
        assertEquals(getRequiredValue(error, OAuthDCRMConstants.ERROR), INVALID_CLIENT_METADATA,
                "Invalid error message");
        return error;
    }

    private JSONObject parseJSONResponse(HttpResponse response) throws IOException {

        BufferedReader rd = new BufferedReader(new InputStreamReader(response.getEntity().getContent()));
        Object responseObj = JSONValue.parse(rd);
        EntityUtils.consume(response.getEntity());
        return (JSONObject) responseObj;
    }

    private String getRequiredValue(JSONObject responseObj, String key) {

        Object value = responseObj.get(key);
        assertNotNull(value, key + " is not present in the response");
        return value.toString();
    }

    private long getClientSecretExpiresAt(JSONObject application) {

        Object expiry = application.get(CLIENT_SECRET_EXPIRES_AT);
        assertNotNull(expiry, CLIENT_SECRET_EXPIRES_AT + " is not present in the response");
        return ((Number) expiry).longValue();
    }

    private long currentTimeInSeconds() {

        return System.currentTimeMillis() / 1000L;
    }

    private String getPath() {

        if (tenant.equals(SUPER_TENANT_DOMAIN)) {
            return OAuthDCRMConstants.DCR_ENDPOINT_HOST_PART + OAuthDCRMConstants.DCR_ENDPOINT_PATH_PART;
        } else {
            return OAuthDCRMConstants.DCR_ENDPOINT_HOST_PART + "/t/" + tenant + OAuthDCRMConstants
                    .DCR_ENDPOINT_PATH_PART;
        }
    }

    private String getTokenEndpoint() {

        if (tenant.equals(SUPER_TENANT_DOMAIN)) {
            return OAuth2Constant.ACCESS_TOKEN_ENDPOINT;
        } else {
            return OAuth2Constant.TENANT_TOKEN_ENDPOINT.replace(OAuth2Constant.TENANT_PLACEHOLDER, tenant);
        }
    }

    private String getAuthzHeader() {

        return getBasicAuthHeader(username, password);
    }

    private String getBasicAuthHeader(String user, String secret) {

        return "Basic " + Base64.encodeBase64String((user + ":" + secret).getBytes()).trim();
    }
}

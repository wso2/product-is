/*
 * Copyright (c) 2022, WSO2 Inc. (http://www.wso2.org).
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

package org.wso2.identity.integration.test.webhooks.usermanagement;

import org.apache.commons.codec.binary.Base64;
import org.apache.http.HttpHeaders;
import org.apache.http.HttpResponse;
import org.apache.http.client.methods.HttpDelete;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.util.EntityUtils;
import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.json.simple.JSONValue;
import org.testng.Assert;
import org.testng.annotations.AfterClass;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.DataProvider;
import org.testng.annotations.Factory;
import org.testng.annotations.Test;
import org.wso2.carbon.automation.engine.context.AutomationContext;
import org.wso2.carbon.automation.engine.context.TestUserMode;
import org.wso2.carbon.utils.multitenancy.MultitenantConstants;
import org.wso2.identity.integration.common.utils.ISIntegrationTest;
import org.wso2.identity.integration.test.webhooks.WebhookEventTestManager;
import org.wso2.identity.integration.test.webhooks.usermanagement.eventpayloadbuilder.AdminInitUserManagementEventTestExpectedEventPayloadBuilder;

import java.io.UnsupportedEncodingException;
import java.util.Arrays;

import static org.testng.Assert.assertEquals;
import static org.testng.Assert.assertNotNull;
import static org.wso2.identity.integration.test.scim2.SCIM2BaseTestCase.EMAILS_ATTRIBUTE;
import static org.wso2.identity.integration.test.scim2.SCIM2BaseTestCase.EMAIL_ADDRESSES_ATTRIBUTE;
import static org.wso2.identity.integration.test.scim2.SCIM2BaseTestCase.EMAIL_TYPE_HOME_ATTRIBUTE;
import static org.wso2.identity.integration.test.scim2.SCIM2BaseTestCase.EMAIL_TYPE_WORK_ATTRIBUTE;
import static org.wso2.identity.integration.test.scim2.SCIM2BaseTestCase.ERROR_SCHEMA;
import static org.wso2.identity.integration.test.scim2.SCIM2BaseTestCase.FAMILY_NAME_ATTRIBUTE;
import static org.wso2.identity.integration.test.scim2.SCIM2BaseTestCase.GIVEN_NAME_ATTRIBUTE;
import static org.wso2.identity.integration.test.scim2.SCIM2BaseTestCase.ID_ATTRIBUTE;
import static org.wso2.identity.integration.test.scim2.SCIM2BaseTestCase.NAME_ATTRIBUTE;
import static org.wso2.identity.integration.test.scim2.SCIM2BaseTestCase.PASSWORD_ATTRIBUTE;
import static org.wso2.identity.integration.test.scim2.SCIM2BaseTestCase.SCHEMAS_ATTRIBUTE;
import static org.wso2.identity.integration.test.scim2.SCIM2BaseTestCase.SCIM2_USERS_ENDPOINT;
import static org.wso2.identity.integration.test.scim2.SCIM2BaseTestCase.SERVER_URL;
import static org.wso2.identity.integration.test.scim2.SCIM2BaseTestCase.TYPE_PARAM;
import static org.wso2.identity.integration.test.scim2.SCIM2BaseTestCase.USER_NAME_ATTRIBUTE;
import static org.wso2.identity.integration.test.scim2.SCIM2BaseTestCase.USER_SYSTEM_SCHEMA;
import static org.wso2.identity.integration.test.scim2.SCIM2BaseTestCase.VALUE_PARAM;

/**
 * This class tests the user management events triggered by the admin user.
 */
public class AdminInitUserManagementEventTestCase extends ISIntegrationTest {

    private static final String FAMILY_NAME_CLAIM_VALUE = "scim";
    private static final String GIVEN_NAME_CLAIM_VALUE = "user";
    private static final String EMAIL_TYPE_WORK_CLAIM_VALUE = "scim2user@wso2.com";
    private static final String EMAIL_TYPE_HOME_CLAIM_VALUE = "scim2user@gmail.com";
    public static final String USERNAME = "scim2user";
    public static final String PASSWORD = "Wso2@test";
    private String userId;
    private final String adminUsername;
    private final String adminPassword;
    private final String tenant;
    private final AutomationContext automationContext;
    private CloseableHttpClient client;
    private WebhookEventTestManager webhookEventTestManager;

    @BeforeClass(alwaysRun = true)
    public void testInit() throws Exception {

        super.init();
        client = HttpClients.createDefault();

        webhookEventTestManager = new WebhookEventTestManager("/scim2/webhook", "WSO2",
                Arrays.asList("https://schemas.identity.wso2.org/events/user",
                        "https://schemas.identity.wso2.org/events/registration"),
                "AdminInitUserManagementEventTestCase",
                automationContext);
    }

    @AfterClass(alwaysRun = true)
    public void atEnd() throws Exception {

        webhookEventTestManager.teardown();
        client.close();
    }

    @Factory(dataProvider = "SCIM2UserConfigProvider")
    public AdminInitUserManagementEventTestCase(TestUserMode userMode) throws Exception {

        automationContext = new AutomationContext("IDENTITY", userMode);
        adminUsername = automationContext.getContextTenant().getTenantAdmin().getUserName();
        adminPassword = automationContext.getContextTenant().getTenantAdmin().getPassword();
        tenant = automationContext.getContextTenant().getDomain();
    }

    @DataProvider(name = "SCIM2UserConfigProvider")
    public static Object[][] sCIM2UserConfigProvider() {

        return new Object[][]{
                {TestUserMode.SUPER_TENANT_ADMIN},
                {TestUserMode.TENANT_ADMIN}
        };
    }

    @Test
    public void testCreateUser() throws Exception {

        HttpPost request = buildUserCreateRequest();

        HttpResponse response = client.execute(request);
        assertEquals(response.getStatusLine().getStatusCode(), 201, "User " +
                "has not been created successfully");

        Object responseObj = JSONValue.parse(EntityUtils.toString(response.getEntity()));
        EntityUtils.consume(response.getEntity());

        userId = ((JSONObject) responseObj).get(ID_ATTRIBUTE).toString();
        assertNotNull(userId);

        webhookEventTestManager.stackExpectedEventPayload(
                "https://schemas.identity.wso2.org/events/registration/event-type/registrationSuccess",
                AdminInitUserManagementEventTestExpectedEventPayloadBuilder.buildExpectedRegistrationSuccessEventPayloadForTestCreateUser(
                        userId, tenant));
        webhookEventTestManager.stackExpectedEventPayload(
                "https://schemas.identity.wso2.org/events/user/event-type/userCreated",
                AdminInitUserManagementEventTestExpectedEventPayloadBuilder.buildExpectedUserCreatedEventPayloadForTestCreateUser(
                        userId,
                        tenant));
        webhookEventTestManager.validateStackedEventPayloads();
    }

    @Test (dependsOnMethods = "testCreateUser")
    public void testAddUserFailure() throws Exception {

        HttpPost request = buildInvalidUserCreateRequest();

        HttpResponse response = client.execute(request);

        Object responseObj = JSONValue.parse(EntityUtils.toString(response.getEntity()));
        EntityUtils.consume(response.getEntity());

        JSONArray schemasArray = (JSONArray) ((JSONObject) responseObj).get("schemas");
        Assert.assertNotNull(schemasArray);
        Assert.assertEquals(schemasArray.size(), 1);
        Assert.assertEquals(schemasArray.get(0).toString(), ERROR_SCHEMA);

        //todo: Better if this error description which eventually goes out over the event can be improved.
        webhookEventTestManager.stackExpectedEventPayload(
                "https://schemas.identity.wso2.org/events/registration/event-type/registrationFailed",
                AdminInitUserManagementEventTestExpectedEventPayloadBuilder.buildExpectedRegistrationFailedEventPayload(
                        userId, tenant, "20035 - The minimum length of password should be 8."));
        webhookEventTestManager.validateStackedEventPayloads();
    }

    @Test(dependsOnMethods = "testAddUserFailure")
    public void testDeleteUser() throws Exception {

        String userResourcePath = getPath() + "/" + userId;
        HttpDelete request = new HttpDelete(userResourcePath);
        request.addHeader(HttpHeaders.AUTHORIZATION, getAuthzHeader());
        request.addHeader(HttpHeaders.CONTENT_TYPE, "application/json");

        HttpResponse response = client.execute(request);
        assertEquals(response.getStatusLine().getStatusCode(), 204, "User " +
                "has not been retrieved successfully");

        EntityUtils.consume(response.getEntity());

        webhookEventTestManager.stackExpectedEventPayload(
                "https://schemas.identity.wso2.org/events/user/event-type/userDeleted",
                AdminInitUserManagementEventTestExpectedEventPayloadBuilder.buildExpectedUserDeletedEventPayload(
                        userId, tenant));
        webhookEventTestManager.validateStackedEventPayloads();
    }

    private HttpPost buildUserCreateRequest() throws UnsupportedEncodingException {

        HttpPost request = new HttpPost(getPath());
        request.addHeader(HttpHeaders.AUTHORIZATION, getAuthzHeader());
        request.addHeader(HttpHeaders.CONTENT_TYPE, "application/json");

        JSONObject rootObject = new JSONObject();

        JSONArray schemas = new JSONArray();
        rootObject.put(SCHEMAS_ATTRIBUTE, schemas);

        JSONObject names = new JSONObject();
        names.put(FAMILY_NAME_ATTRIBUTE, FAMILY_NAME_CLAIM_VALUE);
        names.put(GIVEN_NAME_ATTRIBUTE, GIVEN_NAME_CLAIM_VALUE);

        rootObject.put(NAME_ATTRIBUTE, names);
        rootObject.put(USER_NAME_ATTRIBUTE, USERNAME);

        JSONObject emailWork = new JSONObject();
        emailWork.put(TYPE_PARAM, EMAIL_TYPE_WORK_ATTRIBUTE);
        emailWork.put(VALUE_PARAM, EMAIL_TYPE_WORK_CLAIM_VALUE);

        JSONObject emailHome = new JSONObject();
        emailHome.put(TYPE_PARAM, EMAIL_TYPE_HOME_ATTRIBUTE);
        emailHome.put(VALUE_PARAM, EMAIL_TYPE_HOME_CLAIM_VALUE);

        JSONArray emails = new JSONArray();
        emails.add(emailWork);
        emails.add(emailHome);

        rootObject.put(EMAILS_ATTRIBUTE, emails);

        rootObject.put(PASSWORD_ATTRIBUTE, PASSWORD);

        JSONArray emailAddresses = new JSONArray();
        emailAddresses.add(EMAIL_TYPE_WORK_CLAIM_VALUE);
        emailAddresses.add(EMAIL_TYPE_HOME_CLAIM_VALUE);
        JSONObject emailAddressesObject = new JSONObject();
        emailAddressesObject.put(EMAIL_ADDRESSES_ATTRIBUTE, emailAddresses);
        rootObject.put(USER_SYSTEM_SCHEMA, emailAddressesObject);

        StringEntity entity = new StringEntity(rootObject.toString());
        request.setEntity(entity);
        return request;
    }

    private HttpPost buildInvalidUserCreateRequest() throws UnsupportedEncodingException {

        HttpPost request = new HttpPost(getPath());
        request.addHeader(HttpHeaders.AUTHORIZATION, getAuthzHeader());
        request.addHeader(HttpHeaders.CONTENT_TYPE, "application/json");

        JSONObject rootObject = new JSONObject();

        JSONArray schemas = new JSONArray();
        rootObject.put(SCHEMAS_ATTRIBUTE, schemas);

        JSONObject names = new JSONObject();
        rootObject.put(NAME_ATTRIBUTE, names);
        rootObject.put(USER_NAME_ATTRIBUTE, "passwordIncompatibleUser");
        rootObject.put(PASSWORD_ATTRIBUTE, "a");

        StringEntity entity = new StringEntity(rootObject.toString());
        request.setEntity(entity);
        return request;
    }

    private String getPath() {

        if (tenant.equals(MultitenantConstants.SUPER_TENANT_DOMAIN_NAME)) {
            return SERVER_URL + SCIM2_USERS_ENDPOINT;
        } else {
            return SERVER_URL + "/t/" + tenant + SCIM2_USERS_ENDPOINT;
        }
    }

    private String getAuthzHeader() {

        return "Basic " + Base64.encodeBase64String((adminUsername + ":" + adminPassword).getBytes()).trim();
    }
}

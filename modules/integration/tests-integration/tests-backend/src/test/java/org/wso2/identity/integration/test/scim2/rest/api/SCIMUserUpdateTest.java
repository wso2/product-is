/*
 * Copyright (c) 2020, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
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

package org.wso2.identity.integration.test.scim2.rest.api;

import io.restassured.RestAssured;
import io.restassured.response.ExtractableResponse;
import io.restassured.response.Response;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.http.HttpHeaders;
import org.apache.http.HttpStatus;
import org.testng.Assert;
import org.testng.annotations.AfterClass;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.DataProvider;
import org.testng.annotations.Factory;
import org.testng.annotations.Test;
import org.wso2.carbon.automation.engine.context.TestUserMode;

import java.io.IOException;
import java.rmi.RemoteException;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import static org.hamcrest.core.IsNull.notNullValue;
import static org.wso2.identity.integration.test.scim2.SCIM2BaseTestCase.USERS_ENDPOINT;

/**
 * Test cases for updating users via SCIM PATCH and PUT operations.
 */
public class SCIMUserUpdateTest extends SCIM2BaseTest {

    private static final Log log = LogFactory.getLog(SCIMUserUpdateTest.class);
    private static final String DEFAULT_CORRELATION_HEADER = "X-WSO2-traceId";
    private static final String SYSTEM_SCHEMA_URI_WITH_ESCAPE_CHARS = "\"urn:scim:wso2:schema\"";

    private String endpointURL;
    private String userId = null;
    private static final String SCIM_CONTENT_TYPE = "application/scim+json";

    @Factory(dataProvider = "restAPIUserConfigProvider")
    public SCIMUserUpdateTest(TestUserMode userMode) throws Exception {

        super.init(userMode);
        this.context = isServer;
        this.authenticatingUserName = context.getContextTenant().getTenantAdmin().getUserName();
        this.authenticatingCredential = context.getContextTenant().getTenantAdmin().getPassword();
        this.tenant = context.getContextTenant().getDomain();
    }

    @DataProvider(name = "restAPIUserConfigProvider")
    public static Object[][] restAPIUserConfigProvider() {

        return new Object[][]{
                {TestUserMode.SUPER_TENANT_ADMIN},
                {TestUserMode.TENANT_ADMIN}
        };
    }

    @BeforeClass(alwaysRun = true)
    public void init() throws RemoteException, InterruptedException {

        super.testInit(swaggerDefinition, tenant);
        Thread.sleep(20000);
    }

    @AfterClass(alwaysRun = true)
    public void testFinish() {

        super.conclude();
    }

    @BeforeMethod(alwaysRun = true)
    public void testInit() {

        RestAssured.basePath = basePath;
    }

    @Test
    public void testGetUsers() {

        endpointURL = USERS_ENDPOINT;
        Response response = getResponseOfGet(endpointURL, SCIM_CONTENT_TYPE);
        if (HttpStatus.SC_INTERNAL_SERVER_ERROR == response.getStatusCode()) {
            log.info(">>> Content: >>>" + response.getBody().prettyPrint());
        }
        response.then()
                .assertThat()
                .statusCode(HttpStatus.SC_OK)
                .log()
                .ifValidationFails()
                .and()
                .assertThat()
                .header(HttpHeaders.CONTENT_TYPE, SCIM_CONTENT_TYPE);

        Assert.assertNotNull(response.header(DEFAULT_CORRELATION_HEADER));
    }

    @Test(dependsOnMethods = "testGetUsers")
    public void testCreateUser() throws IOException {

        String body = readResource("scim2-add-user.json");
        Response response = getResponseOfPost(USERS_ENDPOINT, body, SCIM_CONTENT_TYPE);
        ExtractableResponse<Response> extractableResponse = response.then()
                .log().ifValidationFails()
                .assertThat()
                .statusCode(HttpStatus.SC_CREATED)
                .header(HttpHeaders.LOCATION, notNullValue())
                .and()
                .assertThat()
                .header(HttpHeaders.CONTENT_TYPE, SCIM_CONTENT_TYPE)
                .extract();
        Assert.assertNotNull(extractableResponse);

        String location = response.getHeader(HttpHeaders.LOCATION);
        userId = location.split(USERS_ENDPOINT)[1];
        Assert.assertNotNull(userId, "The user did not get created.");
        log.info("Created a user with userId :" + userId);
        endpointURL = USERS_ENDPOINT + userId;

        SCIMUtils.validateSchemasAttributeOfUsersEndpoint(extractableResponse.path("schemas"));
        SCIMUtils.validateMetaAttributeOfUsersEndpoint(extractableResponse.path("meta"), response, endpointURL);
    }

    @Test(dependsOnMethods = "testCreateUser")
    public void testPatchUserRemoveOperation() throws IOException {

        String body = readResource("scim2-patch-user-remove-op.json");
        Response response = getResponseOfPatch(endpointURL, body, SCIM_CONTENT_TYPE);
        ExtractableResponse<Response> extractableResponse = response.then()
                .log().ifValidationFails()
                .assertThat()
                .statusCode(HttpStatus.SC_OK)
                .and()
                .assertThat()
                .header(HttpHeaders.CONTENT_TYPE, SCIM_CONTENT_TYPE)
                .extract();
        Assert.assertNotNull(extractableResponse);

        Object emailsAttribute = extractableResponse.path("emails");

        Assert.assertTrue(emailsAttribute instanceof ArrayList, "'emails' attribute is not a list of objects");
        Assert.assertEquals(((ArrayList) emailsAttribute).size(), 1);
        Assert.assertNotEquals(((LinkedHashMap) ((ArrayList) emailsAttribute).get(0)).get("type"), "work");

        SCIMUtils.validateSchemasAttributeOfUsersEndpoint(extractableResponse.path("schemas"));
        SCIMUtils.validateMetaAttributeOfUsersEndpoint(extractableResponse.path("meta"), response, endpointURL);
    }

    @Test(dependsOnMethods = "testPatchUserRemoveOperation")
    public void testPatchUserAddOperation() throws IOException {

        String body = readResource("scim2-patch-user-add-op.json");
        Response response = getResponseOfPatch(endpointURL, body, SCIM_CONTENT_TYPE);
        ExtractableResponse<Response> extractableResponse = response.then()
                .log().ifValidationFails()
                .assertThat()
                .statusCode(HttpStatus.SC_OK)
                .and()
                .assertThat()
                .header(HttpHeaders.CONTENT_TYPE, SCIM_CONTENT_TYPE)
                .extract();
        Assert.assertNotNull(extractableResponse);

        Object emailsAttribute = extractableResponse.path("emails");

        Assert.assertTrue(emailsAttribute instanceof ArrayList, "'emails' attribute is not a list of objects");
        Assert.assertEquals(((ArrayList) emailsAttribute).size(), 2);

        Object wso2SystemSchema = extractableResponse.path(SYSTEM_SCHEMA_URI_WITH_ESCAPE_CHARS);
        Assert.assertNotNull(wso2SystemSchema, "'urn:scim:wso2:schema' schema is missing");
        Assert.assertTrue(wso2SystemSchema instanceof Map, "'urn:scim:wso2:schema' schema is not a Map");
        Map<String, Object> wso2SystemSchemaObj = (Map<String, Object>) wso2SystemSchema;

        List<Object> emailAddresses = (List<Object>) wso2SystemSchemaObj.get("emailAddresses");
        Assert.assertNotNull(emailAddresses, "'emailAddresses' attribute is missing in the custom schema");
        Assert.assertEquals(emailAddresses.size(), 2,
                "Expected two email addresses in the custom schema");

        List<Object> mobileNumbers = (List<Object>) wso2SystemSchemaObj.get("mobileNumbers");
        Assert.assertNotNull(mobileNumbers, "'mobileNumbers' attribute is missing in the custom schema");
        Assert.assertEquals(mobileNumbers.size(), 2,
                "Expected two mobile numbers in the custom schema");

        Assert.assertNull(wso2SystemSchemaObj.get("verifiedEmailAddresses"));
        Assert.assertNull(wso2SystemSchemaObj.get("verifiedMobileNumbers"));

        SCIMUtils.validateSchemasAttributeOfUsersEndpoint(extractableResponse.path("schemas"));
        SCIMUtils.validateMetaAttributeOfUsersEndpoint(extractableResponse.path("meta"), response, endpointURL);
    }

    @Test(dependsOnMethods = "testPatchUserAddOperation")
    public void testPatchUserReplaceOperation() throws IOException {

        String body = readResource("scim2-patch-user-replace-op.json");
        Response response = getResponseOfPatch(endpointURL, body, SCIM_CONTENT_TYPE);
        ExtractableResponse<Response> extractableResponse = response.then()
                .log().ifValidationFails()
                .assertThat()
                .statusCode(HttpStatus.SC_OK)
                .and()
                .assertThat()
                .header(HttpHeaders.CONTENT_TYPE, SCIM_CONTENT_TYPE)
                .extract();
        Assert.assertNotNull(extractableResponse);

        Object nameAttribute = extractableResponse.path("name");
        Object emailsAttribute = extractableResponse.path("emails");

        Assert.assertTrue(nameAttribute instanceof LinkedHashMap, "'name' attribute is not a list of " +
                "key-value pairs");
        Assert.assertTrue(emailsAttribute instanceof ArrayList, "'emails' attribute is not a list of objects");

        Assert.assertEquals(((LinkedHashMap) nameAttribute).get("familyName"), "Miles");
        Assert.assertEquals(((LinkedHashMap) nameAttribute).get("givenName"), "Desmond");

        Assert.assertEquals(((LinkedHashMap) ((ArrayList) emailsAttribute).get(0)).get("type"), "other");
        Assert.assertEquals(((LinkedHashMap) ((ArrayList) emailsAttribute).get(0)).get("value"),
                "desmond@mail.net");

        Object wso2SystemSchema = extractableResponse.path(SYSTEM_SCHEMA_URI_WITH_ESCAPE_CHARS);
        Assert.assertNotNull(wso2SystemSchema, "'urn:scim:wso2:schema' schema is missing");
        Assert.assertTrue(wso2SystemSchema instanceof Map, "'urn:scim:wso2:schema' schema is not a Map");
        Map<String, Object> wso2SystemSchemaObj = (Map<String, Object>) wso2SystemSchema;

        List<Object> emailAddresses = (List<Object>) wso2SystemSchemaObj.get("emailAddresses");
        Assert.assertNotNull(emailAddresses, "'emailAddresses' attribute is missing in the custom schema");
        Assert.assertEquals(emailAddresses.size(), 2,
                "Expected two email addresses in the custom schema");

        List<Object> mobileNumbers = (List<Object>) wso2SystemSchemaObj.get("mobileNumbers");
        Assert.assertNotNull(mobileNumbers, "'mobileNumbers' attribute is missing in the custom schema");
        Assert.assertEquals(mobileNumbers.size(), 2,
                "Expected two mobile numbers in the custom schema");

        SCIMUtils.validateSchemasAttributeOfUsersEndpoint(extractableResponse.path("schemas"));
        SCIMUtils.validateMetaAttributeOfUsersEndpoint(extractableResponse.path("meta"), response, endpointURL);
    }

    @Test(dependsOnMethods = "testPatchUserReplaceOperation")
    public void testPutUser() throws IOException {

        String body = readResource("scim2-put-user.json");
        body = body.replaceAll("(?i)\\b" + "userId-value" + "\\b", userId);
        Response response = getResponseOfPut(endpointURL, body, SCIM_CONTENT_TYPE);
        ExtractableResponse<Response> extractableResponse = response.then()
                .log().ifValidationFails()
                .assertThat()
                .statusCode(HttpStatus.SC_OK)
                .and()
                .assertThat()
                .header(HttpHeaders.CONTENT_TYPE, SCIM_CONTENT_TYPE)
                .extract();
        Assert.assertNotNull(extractableResponse);

        Object nameAttribute = extractableResponse.path("name");

        Assert.assertTrue(nameAttribute instanceof LinkedHashMap, "'name' attribute is not a list of " +
                "key-value pairs");
        Assert.assertEquals(((LinkedHashMap) nameAttribute).get("familyName"), "jackson");
        Assert.assertEquals(((LinkedHashMap) nameAttribute).get("givenName"), "kim");

        Object wso2SystemSchema = extractableResponse.path(SYSTEM_SCHEMA_URI_WITH_ESCAPE_CHARS);
        Assert.assertNotNull(wso2SystemSchema, "'urn:scim:wso2:schema' schema is missing");
        Assert.assertTrue(wso2SystemSchema instanceof Map, "'urn:scim:wso2:schema' schema is not a Map");
        Map<String, Object> wso2SystemSchemaObj = (Map<String, Object>) wso2SystemSchema;

        List<Object> emailAddresses = (List<Object>) wso2SystemSchemaObj.get("emailAddresses");
        Assert.assertNotNull(emailAddresses, "'emailAddresses' attribute is missing in the custom schema");
        Assert.assertEquals(emailAddresses.size(), 2,
                "Expected two email addresses in the custom schema");

        List<Object> mobileNumbers = (List<Object>) wso2SystemSchemaObj.get("mobileNumbers");
        Assert.assertNotNull(mobileNumbers, "'mobileNumbers' attribute is missing in the custom schema");
        Assert.assertEquals(mobileNumbers.size(), 2,
                "Expected two mobile numbers in the custom schema");

        Assert.assertNull(wso2SystemSchemaObj.get("verifiedEmailAddresses"));
        Assert.assertNull(wso2SystemSchemaObj.get("verifiedMobileNumbers"));

        SCIMUtils.validateSchemasAttributeOfUsersEndpoint(extractableResponse.path("schemas"));
        SCIMUtils.validateMetaAttributeOfUsersEndpoint(extractableResponse.path("meta"), response, endpointURL);
    }

    @Test(dependsOnMethods = "testPutUser")
    public void testPutUserWithUsernameChange() throws IOException {

        String body = readResource("scim2-put-user-change-username.json");
        body = body.replaceAll("(?i)\\b" + "userId-value" + "\\b", userId);
        Response response = getResponseOfPut(endpointURL, body, SCIM_CONTENT_TYPE);
        Assert.assertEquals(response.getStatusCode(), HttpStatus.SC_BAD_REQUEST, "Able to update username attribute.");
    }
}

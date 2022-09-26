/*
 * Copyright (c) 2021, WSO2 Inc. (http://www.wso2.com).
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

package org.wso2.identity.integration.test.rest.api.server.branding.preference.management.v1;

import io.restassured.RestAssured;
import io.restassured.response.Response;
import org.apache.commons.lang.StringUtils;
import org.apache.http.HttpHeaders;
import org.apache.http.HttpStatus;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.testng.Assert;
import org.testng.annotations.AfterClass;
import org.testng.annotations.AfterMethod;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.DataProvider;
import org.testng.annotations.Factory;
import org.testng.annotations.Test;
import org.wso2.carbon.automation.engine.context.TestUserMode;

import java.io.IOException;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Map;
import java.util.Set;

import static org.hamcrest.CoreMatchers.equalTo;
import static org.hamcrest.core.IsNull.notNullValue;
import static org.testng.Assert.assertNotNull;

/**
 * Test class for Branding Preference Management REST APIs success paths.
 */
public class BrandingPreferenceManagementSuccessTest extends BrandingPreferenceManagementTestBase {

    @Factory(dataProvider = "restAPIUserConfigProvider")
    public BrandingPreferenceManagementSuccessTest(TestUserMode userMode) throws Exception {

        super.init(userMode);
        this.context = isServer;
        this.authenticatingUserName = context.getContextTenant().getTenantAdmin().getUserName();
        this.authenticatingCredential = context.getContextTenant().getTenantAdmin().getPassword();
        this.tenant = context.getContextTenant().getDomain();
    }

    public static boolean areJSONObjectsEqual(Object ob1, Object ob2) throws JSONException {

        Object obj1Converted = convertJsonElement(ob1);
        Object obj2Converted = convertJsonElement(ob2);
        return obj1Converted.equals(obj2Converted);
    }

    private static Object convertJsonElement(Object elem) throws JSONException {

        if (elem instanceof JSONObject) {
            JSONObject obj = (JSONObject) elem;
            Iterator<String> keys = obj.keys();
            Map<String, Object> jsonMap = new HashMap<>();
            while (keys.hasNext()) {
                String key = keys.next();
                jsonMap.put(key, convertJsonElement(obj.get(key)));
            }
            return jsonMap;
        } else if (elem instanceof JSONArray) {
            JSONArray arr = (JSONArray) elem;
            Set<Object> jsonSet = new HashSet<>();
            for (int i = 0; i < arr.length(); i++) {
                jsonSet.add(convertJsonElement(arr.get(i)));
            }
            return jsonSet;
        } else {
            return elem;
        }
    }

    @DataProvider(name = "restAPIUserConfigProvider")
    public static Object[][] restAPIUserConfigProvider() {

        return new Object[][]{
                {TestUserMode.SUPER_TENANT_ADMIN},
                {TestUserMode.TENANT_ADMIN}
        };
    }

    @BeforeClass(alwaysRun = true)
    public void init() throws IOException {

        super.testInit(API_VERSION, swaggerDefinition, tenant);
    }

    @AfterClass(alwaysRun = true)
    public void testConclude() {

        super.conclude();
    }

    @BeforeMethod(alwaysRun = true)
    public void testInit() {

        RestAssured.basePath = basePath;
    }

    @AfterMethod(alwaysRun = true)
    public void testFinish() {

        RestAssured.basePath = StringUtils.EMPTY;
    }

    @Test
    public void testAddBrandingPreference() throws IOException, JSONException {

        String body = readResource("add-branding-preference.json");
        Response response = getResponseOfPost(BRANDING_PREFERENCE_API_BASE_PATH, body);

        response.then()
                .log().ifValidationFails()
                .assertThat()
                .statusCode(HttpStatus.SC_CREATED)
                .header(HttpHeaders.LOCATION, notNullValue());
        String location = response.getHeader(HttpHeaders.LOCATION);
        assertNotNull(location);

        JSONObject expectedPreference = new JSONObject(new JSONObject(readResource("add-branding-preference.json")).
                get("preference").toString());
        JSONObject receivedPreference = new JSONObject(new JSONObject(response.asString()).
                get("preference").toString());
        Assert.assertTrue(areJSONObjectsEqual(expectedPreference, receivedPreference),
                "The preference schema of the Response is incorrect");
    }

    @Test(dependsOnMethods = {"testAddBrandingPreference"})
    public void testGetBrandingPreference() throws IOException, JSONException {

        Response response = getResponseOfGet(BRANDING_PREFERENCE_API_BASE_PATH);
        response.then()
                .log().ifValidationFails()
                .assertThat()
                .statusCode(HttpStatus.SC_OK)
                .body("type", equalTo(ORGANIZATION_TYPE))
                .body("name", equalTo(tenant))
                .body("locale", equalTo(DEFAULT_LOCALE));

        JSONObject expectedPreference = new JSONObject(new JSONObject(readResource("add-branding-preference.json")).
                get("preference").toString());
        JSONObject receivedPreference = new JSONObject(new JSONObject(response.asString()).
                get("preference").toString());
        Assert.assertTrue(areJSONObjectsEqual(expectedPreference, receivedPreference),
                "The preference schema of the Response is incorrect");
    }

    @Test(dependsOnMethods = {"testGetBrandingPreference"})
    public void testGetBrandingPreferenceByQueryParams() throws IOException, JSONException {

        Response response = getResponseOfGet(BRANDING_PREFERENCE_API_BASE_PATH + String.format
                (PREFERENCE_COMPONENT_WITH_QUERY_PARAM, ORGANIZATION_TYPE, tenant, DEFAULT_LOCALE));
        response.then()
                .log().ifValidationFails()
                .assertThat()
                .statusCode(HttpStatus.SC_OK)
                .body("type", equalTo(ORGANIZATION_TYPE))
                .body("name", equalTo(tenant))
                .body("locale", equalTo(DEFAULT_LOCALE));

        JSONObject expectedPreference = new JSONObject(new JSONObject(readResource("add-branding-preference.json")).
                get("preference").toString());
        JSONObject receivedPreference = new JSONObject(new JSONObject(response.asString()).
                get("preference").toString());
        Assert.assertTrue(areJSONObjectsEqual(expectedPreference, receivedPreference),
                "The preference schema of the Response is incorrect");
    }

    @Test(dependsOnMethods = {"testGetBrandingPreferenceByQueryParams"})
    public void testGetBrandingPreferenceByTypeQueryParam() throws IOException, JSONException {

        Response response = getResponseOfGet(BRANDING_PREFERENCE_API_BASE_PATH + QUERY_PARAM_SEPARATOR +
                String.format(TYPE_QUERY_PARAM, ORGANIZATION_TYPE));
        response.then()
                .log().ifValidationFails()
                .assertThat()
                .statusCode(HttpStatus.SC_OK)
                .body("type", equalTo(ORGANIZATION_TYPE))
                .body("name", equalTo(tenant))
                .body("locale", equalTo(DEFAULT_LOCALE));

        JSONObject expectedPreference = new JSONObject(new JSONObject(readResource("add-branding-preference.json")).
                get("preference").toString());
        JSONObject receivedPreference = new JSONObject(new JSONObject(response.asString()).
                get("preference").toString());
        Assert.assertTrue(areJSONObjectsEqual(expectedPreference, receivedPreference),
                "The preference schema of the Response is incorrect");
    }

    @Test(dependsOnMethods = {"testGetBrandingPreferenceByTypeQueryParam"})
    public void testGetBrandingPreferenceByLocaleQueryParam() throws IOException, JSONException {

        Response response = getResponseOfGet(BRANDING_PREFERENCE_API_BASE_PATH + QUERY_PARAM_SEPARATOR +
                String.format(LOCALE_QUERY_PARAM, DEFAULT_LOCALE));
        response.then()
                .log().ifValidationFails()
                .assertThat()
                .statusCode(HttpStatus.SC_OK)
                .body("type", equalTo(ORGANIZATION_TYPE))
                .body("name", equalTo(tenant))
                .body("locale", equalTo(DEFAULT_LOCALE));

        JSONObject expectedPreference = new JSONObject(new JSONObject(readResource("add-branding-preference.json")).
                get("preference").toString());
        JSONObject receivedPreference = new JSONObject(new JSONObject(response.asString()).
                get("preference").toString());
        Assert.assertTrue(areJSONObjectsEqual(expectedPreference, receivedPreference),
                "The preference schema of the Response is incorrect");
    }

    @Test(dependsOnMethods = {"testGetBrandingPreferenceByLocaleQueryParam"})
    public void testUpdateBrandingPreference() throws IOException, JSONException {

        String body = readResource("update-branding-preference.json");
        Response response = getResponseOfPut(BRANDING_PREFERENCE_API_BASE_PATH, body);

        response.then()
                .log().ifValidationFails()
                .assertThat()
                .statusCode(HttpStatus.SC_OK)
                .body("type", equalTo(ORGANIZATION_TYPE))
                .body("name", equalTo(tenant))
                .body("locale", equalTo(DEFAULT_LOCALE));

        JSONObject expectedPreference = new JSONObject(new JSONObject(readResource("update-branding-preference.json")).
                get("preference").toString());
        JSONObject receivedPreference = new JSONObject(new JSONObject(response.asString()).
                get("preference").toString());
        Assert.assertTrue(areJSONObjectsEqual(expectedPreference, receivedPreference),
                "The preference schema of the Response is incorrect");
    }

    @Test(dependsOnMethods = {"testUpdateBrandingPreference"})
    public void testGetBrandingPreferenceAfterUpdate() throws IOException, JSONException {

        Response response = getResponseOfGet(BRANDING_PREFERENCE_API_BASE_PATH);
        response.then()
                .log().ifValidationFails()
                .assertThat()
                .statusCode(HttpStatus.SC_OK)
                .body("type", equalTo(ORGANIZATION_TYPE))
                .body("name", equalTo(tenant))
                .body("locale", equalTo(DEFAULT_LOCALE));

        JSONObject expectedPreference = new JSONObject(new JSONObject(readResource("update-branding-preference.json")).
                get("preference").toString());
        JSONObject receivedPreference = new JSONObject(new JSONObject(response.asString()).
                get("preference").toString());
        Assert.assertTrue(areJSONObjectsEqual(expectedPreference, receivedPreference),
                "The preference schema of the Response is incorrect");
    }

    @Test(dependsOnMethods = {"testGetBrandingPreferenceAfterUpdate"})
    public void testDeleteBrandingPreference() {

        Response response = getResponseOfDelete(BRANDING_PREFERENCE_API_BASE_PATH);
        response.then()
                .log().ifValidationFails()
                .assertThat()
                .statusCode(HttpStatus.SC_NO_CONTENT);
    }

    @Test(dependsOnMethods = {"testDeleteBrandingPreference"})
    public void testDeleteBrandingPreferenceByQueryParams() throws IOException {

        String body = readResource("add-branding-preference.json");
        // Add Branding preference.
        Response response = getResponseOfPost(BRANDING_PREFERENCE_API_BASE_PATH, body);

        response.then()
                .log().ifValidationFails()
                .assertThat()
                .statusCode(HttpStatus.SC_CREATED)
                .header(HttpHeaders.LOCATION, notNullValue());
        String location = response.getHeader(HttpHeaders.LOCATION);
        assertNotNull(location);

        // Delete Branding preference.
        response = getResponseOfDelete(BRANDING_PREFERENCE_API_BASE_PATH + String.format
                (PREFERENCE_COMPONENT_WITH_QUERY_PARAM, ORGANIZATION_TYPE, tenant, DEFAULT_LOCALE));
        response.then()
                .log().ifValidationFails()
                .assertThat()
                .statusCode(HttpStatus.SC_NO_CONTENT);
    }

    @Test(dependsOnMethods = {"testDeleteBrandingPreferenceByQueryParams"})
    public void testDeleteBrandingPreferenceByTypeQueryParam() throws IOException {

        String body = readResource("add-branding-preference.json");
        // Add Branding preference.
        Response response = getResponseOfPost(BRANDING_PREFERENCE_API_BASE_PATH, body);

        response.then()
                .log().ifValidationFails()
                .assertThat()
                .statusCode(HttpStatus.SC_CREATED)
                .header(HttpHeaders.LOCATION, notNullValue());
        String location = response.getHeader(HttpHeaders.LOCATION);
        assertNotNull(location);

        // Delete Branding preference.
        response = getResponseOfDelete(BRANDING_PREFERENCE_API_BASE_PATH + QUERY_PARAM_SEPARATOR +
                String.format(TYPE_QUERY_PARAM, ORGANIZATION_TYPE));
        response.then()
                .log().ifValidationFails()
                .assertThat()
                .statusCode(HttpStatus.SC_NO_CONTENT);
    }

    @Test(dependsOnMethods = {"testDeleteBrandingPreferenceByTypeQueryParam"})
    public void testDeleteBrandingPreferenceByLocaleQueryParam() throws IOException {

        String body = readResource("add-branding-preference.json");
        // Add Branding preference.
        Response response = getResponseOfPost(BRANDING_PREFERENCE_API_BASE_PATH, body);

        response.then()
                .log().ifValidationFails()
                .assertThat()
                .statusCode(HttpStatus.SC_CREATED)
                .header(HttpHeaders.LOCATION, notNullValue());
        String location = response.getHeader(HttpHeaders.LOCATION);
        assertNotNull(location);

        // Delete Branding preference.
        response = getResponseOfDelete(BRANDING_PREFERENCE_API_BASE_PATH + QUERY_PARAM_SEPARATOR +
                String.format(LOCALE_QUERY_PARAM, DEFAULT_LOCALE));
        response.then()
                .log().ifValidationFails()
                .assertThat()
                .statusCode(HttpStatus.SC_NO_CONTENT);
    }
}

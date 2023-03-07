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

package org.wso2.identity.integration.test.rest.api.server.application.management.v1;

import io.restassured.response.Response;
import org.apache.http.HttpHeaders;
import org.apache.http.HttpStatus;
import org.testng.annotations.AfterMethod;
import org.testng.annotations.AfterTest;
import org.testng.annotations.BeforeTest;
import org.testng.annotations.Factory;
import org.testng.annotations.Test;
import org.wso2.carbon.automation.engine.context.TestUserMode;

import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.notNullValue;
import static org.wso2.identity.integration.test.rest.api.server.application.management.v1.Utils.assertNotBlank;
import static org.wso2.identity.integration.test.rest.api.server.application.management.v1.Utils.extractApplicationIdFromLocationHeader;

import java.io.IOException;
import java.util.HashSet;
import java.util.Set;

/**
 * Test class for Application Management User Registrant Success Test.
 */
public class ApplicationManagementUserRegistrantSuccessTest extends ApplicationManagementBaseTest {

    private String createdAppId;
    static final String APPLICATION_USER_REGISTRANT_CONTEXT = "/user-registrants";
    static final String RESPONSE_ELEMENT_TOTAL_RESULTS = "totalResults";
    static final String RESPONSE_ELEMENT_USER_REGISTRANTS = "userRegistrants";

    @Factory(dataProvider = "restAPIUserConfigProvider")
    public ApplicationManagementUserRegistrantSuccessTest(TestUserMode userMode) throws Exception {

        super(userMode);
    }

    @BeforeTest(alwaysRun = true)
    public void initTestClass() throws IOException {

        super.init();
    }

    @AfterTest(alwaysRun = true)
    public void testFinish() {

        super.testFinish();
    }

    @Test
    public void testRetrieveUserRegistrantsOfInvalidApp() {

        Response response = getResponseOfGet(APPLICATION_MANAGEMENT_API_BASE_PATH + PATH_SEPARATOR + "some-wrong-id" +
                APPLICATION_USER_REGISTRANT_CONTEXT);

        response.then()
                .log().ifValidationFails()
                .assertThat()
                .statusCode(HttpStatus.SC_OK)
                .body(RESPONSE_ELEMENT_TOTAL_RESULTS, equalTo(0))
                .body(RESPONSE_ELEMENT_USER_REGISTRANTS + ".size()", equalTo(0));
    }

    @Test
    public void testRetrieveUserRegistrantsDefault() throws IOException {

        createInitialApp();

        Response response = getResponseOfGet(APPLICATION_MANAGEMENT_API_BASE_PATH + PATH_SEPARATOR + createdAppId +
                APPLICATION_USER_REGISTRANT_CONTEXT);

        response.then()
                .log().ifValidationFails()
                .assertThat()
                .statusCode(HttpStatus.SC_OK)
                .body(RESPONSE_ELEMENT_TOTAL_RESULTS, equalTo(1))
                .body(RESPONSE_ELEMENT_USER_REGISTRANTS + ".size()", equalTo(1));
    }

    @Test(dependsOnMethods = "testRetrieveUserRegistrantsDefault")
    public void testRetrieveUserRegistrantUsernamePassword() throws IOException {

        String authenticatorsPutPayload = readResource("patch-app-with-username-password-authenticator.json");
        doPutAuthenticatorsAndVerify(authenticatorsPutPayload);

        Response response = getResponseOfGet(APPLICATION_MANAGEMENT_API_BASE_PATH + PATH_SEPARATOR + createdAppId +
                APPLICATION_USER_REGISTRANT_CONTEXT);

        String baseIdentifier =
                RESPONSE_ELEMENT_USER_REGISTRANTS + ".find{ it.id == 'QmFzaWNBdXRoQXV0aEF0dHJpYnV0ZUhhbmRsZXI' }.";

        response.then()
                .log().ifValidationFails()
                .assertThat()
                .statusCode(HttpStatus.SC_OK)
                .body(RESPONSE_ELEMENT_TOTAL_RESULTS, equalTo(1))
                .body(RESPONSE_ELEMENT_USER_REGISTRANTS + ".size()", equalTo(1))
                .body(baseIdentifier + "name", equalTo("BasicAuthAuthAttributeHandler"))
                .body(baseIdentifier + "authAttributes.size()", equalTo(2))
                .body(baseIdentifier + "authAttributes[0].attribute", equalTo("username"))
                .body(baseIdentifier + "authAttributes[1].attribute", equalTo("password"));
    }

    @Test(dependsOnMethods = "testRetrieveUserRegistrantUsernamePassword")
    public void testRetrieveTwoConfiguredUserRegistrants() throws IOException {

        String authenticatorsPutPayload = readResource("patch-app-with-two-auth-options.json");
        doPutAuthenticatorsAndVerify(authenticatorsPutPayload);

        Response response = getResponseOfGet(APPLICATION_MANAGEMENT_API_BASE_PATH + PATH_SEPARATOR + createdAppId +
                APPLICATION_USER_REGISTRANT_CONTEXT);

        String encodedIdForBasicAuth = "QmFzaWNBdXRoQXV0aEF0dHJpYnV0ZUhhbmRsZXI";
        String encodedIdForMagicLink = "TWFnaWNMaW5rQXV0aEF0dHJpYnV0ZUhhbmRsZXI";
        String basicAuthBaseIdentifier =
                RESPONSE_ELEMENT_USER_REGISTRANTS + ".find{ it.id == '" + encodedIdForBasicAuth + "' }.";
        String magicLinkBaseIdentifier =
                RESPONSE_ELEMENT_USER_REGISTRANTS + ".find{ it.id == '" + encodedIdForMagicLink + "' }.";

        response.then()
                .log().ifValidationFails()
                .assertThat()
                .statusCode(HttpStatus.SC_OK)
                .body(RESPONSE_ELEMENT_TOTAL_RESULTS, equalTo(2))
                .body(RESPONSE_ELEMENT_USER_REGISTRANTS + ".size()", equalTo(2))
                .body(basicAuthBaseIdentifier + "name", equalTo("BasicAuthAuthAttributeHandler"))
                .body(basicAuthBaseIdentifier + "authAttributes.size()", equalTo(2))
                .body(basicAuthBaseIdentifier + "authAttributes[0].attribute", equalTo("username"))
                .body(basicAuthBaseIdentifier + "authAttributes[1].attribute", equalTo("password"))
                .body(magicLinkBaseIdentifier + "name", equalTo("MagicLinkAuthAttributeHandler"))
                .body(magicLinkBaseIdentifier + "authAttributes.size()", equalTo(2))
                .body(magicLinkBaseIdentifier + "authAttributes[0].attribute", equalTo("username"))
                .body(magicLinkBaseIdentifier + "authAttributes[1].attribute", equalTo("http://wso2.org/claims/emailaddress"));
    }

    @Test (dependsOnMethods = "testRetrieveTwoConfiguredUserRegistrants")
    public void testClearAppData() {

        String path = APPLICATION_MANAGEMENT_API_BASE_PATH + "/" + createdAppId;

        Response responseOfDelete = getResponseOfDelete(path);
        responseOfDelete.then()
                .log()
                .ifValidationFails()
                .assertThat()
                .statusCode(HttpStatus.SC_NO_CONTENT);

        // Make sure we don't have deleted application details.
        getResponseOfGet(path).then().assertThat().statusCode(HttpStatus.SC_NOT_FOUND);
    }

    private void createInitialApp() throws IOException {

        String body = readResource("create-app-with-no-authenticators.json");

        Response response = getResponseOfPost(APPLICATION_MANAGEMENT_API_BASE_PATH, body);
        response.then()
                .log().ifValidationFails()
                .assertThat()
                .statusCode(HttpStatus.SC_CREATED)
                .header(HttpHeaders.LOCATION, notNullValue());

        String location = response.getHeader(HttpHeaders.LOCATION);
        createdAppId = extractApplicationIdFromLocationHeader(location);
    }

    private void doPutAuthenticatorsAndVerify(String authenticatorsPutPayload) {

        String path = APPLICATION_MANAGEMENT_API_BASE_PATH + PATH_SEPARATOR + createdAppId;
        Response responseOfPatch = getResponseOfPatch(path, authenticatorsPutPayload);

        // Validate PATCH response.
        responseOfPatch.then()
                .log().ifValidationFails()
                .assertThat()
                .statusCode(HttpStatus.SC_OK);
    }
}

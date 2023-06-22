/*
 * Copyright (c) 2019, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.wso2.identity.integration.test.rest.api.server.application.management.v1;

import io.restassured.response.Response;
import org.apache.http.HttpHeaders;
import org.apache.http.HttpStatus;
import org.json.JSONException;
import org.json.JSONObject;
import org.testng.annotations.Factory;
import org.testng.annotations.Test;
import org.wso2.carbon.automation.engine.context.TestUserMode;

import static org.hamcrest.CoreMatchers.equalTo;
import static org.hamcrest.Matchers.notNullValue;
import static org.wso2.identity.integration.test.rest.api.server.application.management.v1.Utils.assertNotBlank;
import static org.wso2.identity.integration.test.rest.api.server.application.management.v1.Utils.extractApplicationIdFromLocationHeader;

/**
 * Tests for happy paths of the managing Passive STS applications using Application Management REST API.
 */
public class ApplicationManagementPassiveStsSuccessTest extends ApplicationManagementBaseTest {

    private static final String INBOUND_PROTOCOLS_PASSIVE_STS_CONTEXT = "/inbound-protocols/passive-sts";
    private static final String PASSIVE_STS_RELAM = "realm";
    private static final String PASSIVE_STS_REPLY_TO = "replyTo";
    private String createdAppId;

    @Factory(dataProvider = "restAPIUserConfigProvider")
    public ApplicationManagementPassiveStsSuccessTest(TestUserMode userMode) throws Exception {

        super(userMode);
    }

    @Test
    public void testCreatePassiveSTSApp() throws Exception {

        String body = readResource("create-passive-sts-app.json");
        Response responseOfPost = getResponseOfPost(APPLICATION_MANAGEMENT_API_BASE_PATH, body);
        responseOfPost.then()
                .log().ifValidationFails()
                .assertThat()
                .statusCode(HttpStatus.SC_CREATED)
                .header(HttpHeaders.LOCATION, notNullValue());

        String location = responseOfPost.getHeader(HttpHeaders.LOCATION);
        createdAppId = extractApplicationIdFromLocationHeader(location);
        assertNotBlank(createdAppId);
    }

    @Test(dependsOnMethods = "testCreatePassiveSTSApp")
    public void testGetPassiveSTSInboundDetails() throws Exception {

        String path = APPLICATION_MANAGEMENT_API_BASE_PATH + "/" + createdAppId + INBOUND_PROTOCOLS_PASSIVE_STS_CONTEXT;
        final String realm = "https://myrealm.passivests.com";
        final String replyTo = "https://myrealm.passivests.com/replyTo";

        Response responseOfGet = getResponseOfGet(path);
        responseOfGet.then()
                .log().ifValidationFails()
                .assertThat()
                .statusCode(HttpStatus.SC_OK)
                .body(PASSIVE_STS_RELAM, equalTo(realm))
                .body(PASSIVE_STS_REPLY_TO, equalTo(replyTo));
    }

    @Test(dependsOnMethods = "testGetPassiveSTSInboundDetails")
    public void testDeletePassiveSTSInbound() throws Exception {

        String path = APPLICATION_MANAGEMENT_API_BASE_PATH + "/" + createdAppId + INBOUND_PROTOCOLS_PASSIVE_STS_CONTEXT;

        Response responseOfDelete = getResponseOfDelete(path);
        responseOfDelete.then()
                .log()
                .ifValidationFails()
                .assertThat()
                .statusCode(HttpStatus.SC_NO_CONTENT);

        // Make sure we don't have the Passive STS inbound details.
        getResponseOfGet(path).then().assertThat().statusCode(HttpStatus.SC_NOT_FOUND);
    }

    @Test(dependsOnMethods = "testDeletePassiveSTSInbound")
    public void testPutPassiveSTSInbound() throws Exception {

        final String realm = "https://myrealmnew.passivests.com";
        final String replyTo = "https://myrealm.passivests.com/replyTo";

        String passiveSTSPutPayload = getPassiveSTSPayload(realm, replyTo);
        // Create Passive STS inbound with a PUT.
        doPutPassiveSTSInboundAndAssert(passiveSTSPutPayload);
        doGetPassiveSTSInboundAndAssert(realm, replyTo);

        // Now we update the replyTo with a PUT.
        final String updatedReplyTo = "https://myrealm.passivests.com/replyToUpdated";
        String updatedPutPayload = getPassiveSTSPayload(realm, updatedReplyTo);

        // Update with a PUT.
        doPutPassiveSTSInboundAndAssert(updatedPutPayload);
        doGetPassiveSTSInboundAndAssert(realm, updatedReplyTo);
    }

    @Test(dependsOnMethods = "testPutPassiveSTSInbound")
    public void testDeleteApp() throws Exception {

        String path = APPLICATION_MANAGEMENT_API_BASE_PATH + "/" + createdAppId;

        Response responseOfDelete = getResponseOfDelete(path);
        responseOfDelete.then()
                .log()
                .ifValidationFails()
                .assertThat()
                .statusCode(HttpStatus.SC_NO_CONTENT);

        getResponseOfGet(path).then().assertThat().statusCode(HttpStatus.SC_NOT_FOUND);
        createdAppId = null;
    }

    private void doPutPassiveSTSInboundAndAssert(String passiveStsPayload) {

        String path = APPLICATION_MANAGEMENT_API_BASE_PATH + "/" + createdAppId + INBOUND_PROTOCOLS_PASSIVE_STS_CONTEXT;
        Response responseOfPut = getResponseOfPut(path, passiveStsPayload);

        // Validate PUT response.
        responseOfPut.then()
                .log().ifValidationFails()
                .assertThat()
                .statusCode(HttpStatus.SC_OK);
    }

    private void doGetPassiveSTSInboundAndAssert(String realm, String replyTo) {

        String path = APPLICATION_MANAGEMENT_API_BASE_PATH + "/" + createdAppId + INBOUND_PROTOCOLS_PASSIVE_STS_CONTEXT;
        Response responseOfGet = getResponseOfGet(path);
        responseOfGet.then()
                .log().ifValidationFails()
                .assertThat()
                .statusCode(HttpStatus.SC_OK)
                .body(PASSIVE_STS_RELAM, equalTo(realm))
                .body(PASSIVE_STS_REPLY_TO, equalTo(replyTo));
    }

    private String getPassiveSTSPayload(String realm, String replyTo) throws JSONException {

        return new JSONObject().put(PASSIVE_STS_RELAM, realm).put(PASSIVE_STS_REPLY_TO, replyTo).toString();
    }
}

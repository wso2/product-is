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
 * Tests for happy paths of the managing WSTrust applications using Application Management REST API.
 */
public class ApplicationManagementWSTrustTest extends ApplicationManagementBaseTest {

    private static final String INBOUND_PROTOCOLS_WS_TRUST_CONTEXT = "/inbound-protocols/ws-trust";
    private static final String AUDIENCE_PARAM_NAME = "audience";
    private static final String CERTIFICATE_ALIAS_PARAM_NAME = "certificateAlias";
    private String createdAppId;

    @Factory(dataProvider = "restAPIUserConfigProvider")
    public ApplicationManagementWSTrustTest(TestUserMode userMode) throws Exception {

        super(userMode);
    }

    @Test
    public void testCreateWSTrustApp() throws Exception {

        String body = readResource("create-ws-trust-app.json");

        Response response = getResponseOfPost(APPLICATION_MANAGEMENT_API_BASE_PATH, body);
        response.then()
                .log().ifValidationFails()
                .assertThat()
                .statusCode(HttpStatus.SC_CREATED)
                .header(HttpHeaders.LOCATION, notNullValue());

        String location = response.getHeader(HttpHeaders.LOCATION);
        createdAppId = extractApplicationIdFromLocationHeader(location);
        assertNotBlank(createdAppId);
    }

    @Test(dependsOnMethods = "testCreateWSTrustApp")
    public void testGetWSTrustInboundDetails() throws Exception {

        final String AUDIENCE = "https://trusterservice.wso2.com";
        final String CERTIFICATE_ALIAS = "wso2carbon";

        doGetWsTrustInboundAndAssert(AUDIENCE, CERTIFICATE_ALIAS);
    }

    @Test(dependsOnMethods = "testGetWSTrustInboundDetails")
    public void testDeleteWSTrustInbound() throws Exception {

        String path = APPLICATION_MANAGEMENT_API_BASE_PATH + "/" + createdAppId + INBOUND_PROTOCOLS_WS_TRUST_CONTEXT;

        Response responseOfDelete = getResponseOfDelete(path);
        responseOfDelete.then()
                .log()
                .ifValidationFails()
                .assertThat()
                .statusCode(HttpStatus.SC_NO_CONTENT);

        // Make sure we don't have the WS Trust inbound details.
        getResponseOfGet(path).then().assertThat().statusCode(HttpStatus.SC_NOT_FOUND);
    }

    @Test(dependsOnMethods = "testDeleteWSTrustInbound")
    public void testWSTrustInboundPut() throws Exception {

        String audience = "https://trusterservice.wso2.com";
        String certAlias = "wso2carbon";

        // Create WSTrust Inbound with a PUT.
        doPutWsTrustInboundAndAssert(getWSTrustInboundPayload(audience, certAlias));
        // Validate the GET response for inbound details.
        doGetWsTrustInboundAndAssert(audience, certAlias);

        // Now we update the cert alias with a PUT.
        String updatedCertAlias = "globalsignca";
        doPutWsTrustInboundAndAssert(getWSTrustInboundPayload(audience, updatedCertAlias));
        // Validate with an additional GET
        doGetWsTrustInboundAndAssert(audience, updatedCertAlias);
    }

    @Test(dependsOnMethods = "testWSTrustInboundPut")
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

    private void doPutWsTrustInboundAndAssert(String wsTrustPutPayload) {

        String path = APPLICATION_MANAGEMENT_API_BASE_PATH + "/" + createdAppId + INBOUND_PROTOCOLS_WS_TRUST_CONTEXT;
        Response responseOfPut = getResponseOfPut(path, wsTrustPutPayload);

        // Validate PUT response.
        responseOfPut.then()
                .log().ifValidationFails()
                .assertThat()
                .statusCode(HttpStatus.SC_OK);
    }

    private void doGetWsTrustInboundAndAssert(String audience, String certAlias) {

        String path = APPLICATION_MANAGEMENT_API_BASE_PATH + "/" + createdAppId + INBOUND_PROTOCOLS_WS_TRUST_CONTEXT;
        Response responseOfGet = getResponseOfGet(path);
        responseOfGet.then()
                .log().ifValidationFails()
                .assertThat()
                .statusCode(HttpStatus.SC_OK)
                .body(AUDIENCE_PARAM_NAME, equalTo(audience))
                .body(CERTIFICATE_ALIAS_PARAM_NAME, equalTo(certAlias));
    }

    private String getWSTrustInboundPayload(String audience, String certAlias) throws JSONException {

        return new JSONObject().put(AUDIENCE_PARAM_NAME, audience)
                .put(CERTIFICATE_ALIAS_PARAM_NAME, certAlias).toString();
    }
}

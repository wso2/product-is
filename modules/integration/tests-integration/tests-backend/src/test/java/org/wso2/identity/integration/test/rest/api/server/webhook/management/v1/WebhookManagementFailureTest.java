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

package org.wso2.identity.integration.test.rest.api.server.webhook.management.v1;

import io.restassured.RestAssured;
import io.restassured.response.Response;
import org.apache.commons.lang.StringUtils;
import org.apache.http.HttpStatus;
import org.testng.annotations.*;
import org.wso2.carbon.automation.engine.context.TestUserMode;
import org.wso2.identity.integration.test.rest.api.server.webhook.management.v1.model.WebhookRequest;
import org.wso2.identity.integration.test.rest.api.server.webhook.management.v1.model.WebhookRequestEventProfile;

import static org.hamcrest.CoreMatchers.equalTo;

/**
 * Negative-path tests for the Webhook Management REST API.
 */
public class WebhookManagementFailureTest extends WebhookManagementTestBase {

    @Factory(dataProvider = "restAPIUserConfigProvider")
    public WebhookManagementFailureTest(TestUserMode userMode) throws Exception {

        super.init(userMode);
        this.context = isServer;
        this.authenticatingUserName = context.getContextTenant().getTenantAdmin().getUserName();
        this.authenticatingCredential = context.getContextTenant().getTenantAdmin().getPassword();
        this.tenant = context.getContextTenant().getDomain();
    }

    @BeforeClass(alwaysRun = true)
    @Override
    public void init() throws Exception {

        super.testInit(API_VERSION, swaggerDefinition, tenant);
    }

    @AfterClass(alwaysRun = true)
    public void concludeClass() {

        super.conclude();
    }

    @BeforeMethod(alwaysRun = true)
    public void setBasePath() {

        RestAssured.basePath = basePath;
    }

    @AfterMethod(alwaysRun = true)
    public void clearBasePath() {

        RestAssured.basePath = StringUtils.EMPTY;
    }

    @Test
    public void testCreateWebhookWithInvalidEndpointUrl() {

        WebhookRequest req = validRequest()
                .endpoint("not-a-url");

        getResponseOfPost(WEBHOOK_MANAGEMENT_API_BASE_PATH, toJSONString(req))
                .then().log().ifValidationFails()
                .statusCode(HttpStatus.SC_BAD_REQUEST).body("code", equalTo("WEBHOOKMGT-60012"))
                .body("message", equalTo("Invalid request."))
                .body("description", equalTo("Endpoint URI is invalid."));
    }

    @Test
    public void testCreateWebhookWithEmptyEventProfileFields() {

        WebhookRequest req = validRequest()
                .eventProfile(new WebhookRequestEventProfile().name("").uri(""));

        getResponseOfPost(WEBHOOK_MANAGEMENT_API_BASE_PATH, toJSONString(req))
                .then().log().ifValidationFails()
                .statusCode(HttpStatus.SC_BAD_REQUEST).body("code", equalTo("WEBHOOKMGT-60011"))
                .body("message", equalTo("Invalid request."))
                .body("description",
                        equalTo("Event Profile Name is empty."));
    }

    @Test
    public void testGetWebhookByInvalidId() {

        getResponseOfGet(WEBHOOK_MANAGEMENT_API_BASE_PATH + "/" + INVALID_ID)
                .then().log().ifValidationFails()
                .statusCode(HttpStatus.SC_NOT_FOUND).body("code", equalTo("WEBHOOKMGT-60010"))
                .body("message", equalTo("Webhook is not found."))
                .body("description", equalTo("No webhook is found for given webhook id: " + INVALID_ID));
    }

    @Test
    public void testUpdateWebhookWithInvalidId() {

        WebhookRequest update = validRequest();

        getResponseOfPut(WEBHOOK_MANAGEMENT_API_BASE_PATH + "/" + INVALID_ID, toJSONString(update))
                .then().log().ifValidationFails()
                .statusCode(HttpStatus.SC_NOT_FOUND).body("code", equalTo("WEBHOOKMGT-60010"))
                .body("message", equalTo("Webhook is not found."))
                .body("description", equalTo("No webhook is found for given webhook id: " + INVALID_ID));
    }

    @Test
    public void testActivateWebhookWithInvalidId() {

        getResponseOfPost(WEBHOOK_MANAGEMENT_API_BASE_PATH + "/" + INVALID_ID + "/activate", "")
                .then().log().ifValidationFails()
                .statusCode(HttpStatus.SC_NOT_FOUND).body("code", equalTo("WEBHOOKMGT-60010"))
                .body("message", equalTo("Webhook is not found."))
                .body("description", equalTo("No webhook is found for given webhook id: " + INVALID_ID));
    }

    @Test
    public void testDeactivateWebhookWithInvalidId() {

        getResponseOfPost(WEBHOOK_MANAGEMENT_API_BASE_PATH + "/" + INVALID_ID + "/deactivate", "")
                .then().log().ifValidationFails()
                .statusCode(HttpStatus.SC_NOT_FOUND).body("code", equalTo("WEBHOOKMGT-60010"))
                .body("message", equalTo("Webhook is not found."))
                .body("description", equalTo("No webhook is found for given webhook id: " + INVALID_ID));
    }

    @Test
    public void testRetryWebhookWithInvalidId() {

        getResponseOfPost(WEBHOOK_MANAGEMENT_API_BASE_PATH + "/" + INVALID_ID + "/retry", "")
                .then().log().ifValidationFails()
                .statusCode(HttpStatus.SC_NOT_FOUND).body("code", equalTo("WEBHOOKMGT-60010"))
                .body("message", equalTo("Webhook is not found."))
                .body("description", equalTo("No webhook is found for given webhook id: " + INVALID_ID));
    }

    @Test
    public void testDeleteWebhookWithInvalidId() {

        getResponseOfDelete(WEBHOOK_MANAGEMENT_API_BASE_PATH + "/" + INVALID_ID)
                .then().log().ifValidationFails()
                .statusCode(HttpStatus.SC_NOT_FOUND).body("code", equalTo("WEBHOOKMGT-60010"))
                .body("message", equalTo("Webhook is not found."))
                .body("description", equalTo("No webhook is found for given webhook id: " + INVALID_ID));
    }


    @Test
    public void testRetryWebhook() {

        String testWebhookId = createWebhook();
        Response response = getResponseOfPost(WEBHOOK_MANAGEMENT_API_BASE_PATH + "/" + testWebhookId + "/retry", "");
        response.then()
                .log().ifValidationFails()
                .assertThat()
                .statusCode(HttpStatus.SC_BAD_REQUEST).body("code", equalTo("WEBHOOKMGT-65028"))
                .body("message", equalTo("Unable to perform the operation."))
                .body("description", equalTo("Operation is not supported for Publisher adapter type."));
    }

    // -------- Helpers --------

    private WebhookRequest validRequest() {

        return new WebhookRequest()
                .endpoint(TEST_ENDPOINT_URI)
                .name(TEST_WEBHOOK_NAME)
                .secret(TEST_SECRET)
                .eventProfile(
                        new WebhookRequestEventProfile().name(TEST_EVENT_PROFILE_NAME).uri(TEST_EVENT_PROFILE_URI))
                .addChannelsSubscribedItem(CHANNEL_LOGIN)
                .addChannelsSubscribedItem(CHANNEL_REG)
                .status(WebhookRequest.StatusEnum.ACTIVE);
    }

    /**
     * Creates one valid webhook and returns its id (used for negative update-body test).
     */
    private String createWebhook() {

        Response res = getResponseOfPost(WEBHOOK_MANAGEMENT_API_BASE_PATH, toJSONString(validRequest()));
        res.then().statusCode(HttpStatus.SC_CREATED);
        return res.getBody().jsonPath().getString("id");
    }
}

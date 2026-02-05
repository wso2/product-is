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
import org.apache.http.HttpStatus;
import org.hamcrest.CoreMatchers;
import org.testng.annotations.AfterClass;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Factory;
import org.testng.annotations.Test;
import org.wso2.carbon.automation.engine.context.TestUserMode;
import org.wso2.identity.integration.test.rest.api.server.webhook.management.v1.model.WebhookRequest;
import org.wso2.identity.integration.test.rest.api.server.webhook.management.v1.model.WebhookRequestEventProfile;

import static org.hamcrest.CoreMatchers.anyOf;
import static org.hamcrest.CoreMatchers.equalTo;
import static org.hamcrest.CoreMatchers.not;
import static org.hamcrest.CoreMatchers.notNullValue;
import static org.hamcrest.CoreMatchers.nullValue;
import static org.hamcrest.Matchers.hasKey;

/**
 * Tests for happy paths of the Webhook Management REST API.
 */
public class WebhookManagementSuccessTest extends WebhookManagementTestBase {

    private static String testWebhookId;

    @Factory(dataProvider = "restAPIUserConfigProvider")
    public WebhookManagementSuccessTest(TestUserMode userMode) throws Exception {

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

        testWebhookId = null;
        super.conclude();
    }

    @BeforeMethod(alwaysRun = true)
    public void setBasePath() {

        RestAssured.basePath = basePath;
    }

    @Test
    public void testCreateWebhook() {

        WebhookRequestEventProfile profile = new WebhookRequestEventProfile()
                .name(TEST_EVENT_PROFILE_NAME)
                .uri(TEST_EVENT_PROFILE_URI);

        WebhookRequest request = new WebhookRequest()
                .endpoint(TEST_ENDPOINT_URI)
                .name(TEST_WEBHOOK_NAME)
                .secret(TEST_SECRET)
                .addChannelsSubscribedItem(CHANNEL_LOGIN)
                .addChannelsSubscribedItem(CHANNEL_REG)
                .eventProfile(profile)
                .status(WebhookRequest.StatusEnum.ACTIVE);

        String body = toJSONString(request);

        Response response = getResponseOfPost(WEBHOOK_MANAGEMENT_API_BASE_PATH, body);
        response.then()
                .log().ifValidationFails()
                .assertThat()
                .statusCode(HttpStatus.SC_CREATED)
                .body("id", notNullValue())
                .body("name", equalTo(TEST_WEBHOOK_NAME))
                .body("endpoint", equalTo(TEST_ENDPOINT_URI))
                .body("eventProfile.name", equalTo(TEST_EVENT_PROFILE_NAME))
                .body("eventProfile.uri", equalTo(TEST_EVENT_PROFILE_URI))
                .body("status", equalTo(STATUS_ACTIVE))
                // response must not echo secrets
                .body("$", not(hasKey("secret")))
                // subscribed channels should be reflected as objects with channelUri
                .body("channelsSubscribed.find { it.channelUri == '" + CHANNEL_LOGIN + "' }", notNullValue())
                .body("channelsSubscribed.find { it.channelUri == '" + CHANNEL_REG + "' }", notNullValue());

        testWebhookId = response.getBody().jsonPath().getString("id");
    }

    @Test(dependsOnMethods = "testCreateWebhook")
    public void testListWebhooks() {

        Response response = getResponseOfGet(WEBHOOK_MANAGEMENT_API_BASE_PATH);
        response.then()
                .log().ifValidationFails()
                .assertThat()
                .statusCode(HttpStatus.SC_OK)
                .body("webhooks.find { it.id == '" + testWebhookId + "' }.name", equalTo(TEST_WEBHOOK_NAME))
                .body("webhooks.find { it.id == '" + testWebhookId + "' }.endpoint", equalTo(TEST_ENDPOINT_URI))
                // "self" link exists and ends with /webhooks/{id}
                .body("webhooks.find { it.id == '" + testWebhookId + "' }.self",
                        CoreMatchers.endsWith("/webhooks/" + testWebhookId));
    }

    @Test(dependsOnMethods = "testListWebhooks")
    public void testGetWebhookById() {

        Response response = getResponseOfGet(WEBHOOK_MANAGEMENT_API_BASE_PATH + "/" + testWebhookId);
        response.then()
                .log().ifValidationFails()
                .assertThat()
                .statusCode(HttpStatus.SC_OK)
                .body("id", equalTo(testWebhookId))
                .body("name", equalTo(TEST_WEBHOOK_NAME))
                .body("endpoint", equalTo(TEST_ENDPOINT_URI))
                .body("eventProfile.name", equalTo(TEST_EVENT_PROFILE_NAME))
                .body("eventProfile.uri", equalTo(TEST_EVENT_PROFILE_URI))
                .body("status", anyOf(equalTo(STATUS_ACTIVE), equalTo("PARTIALLY_ACTIVE")))
                .body("channelsSubscribed.find { it.channelUri == '" + CHANNEL_LOGIN + "' }", notNullValue())
                .body("channelsSubscribed.find { it.channelUri == '" + CHANNEL_REG + "' }", notNullValue());
    }

    @Test(dependsOnMethods = "testGetWebhookById")
    public void testUpdateWebhook() {

        // PUT uses the same request model. Update name, endpoint and subscriptions; set INACTIVE.
        WebhookRequest update = new WebhookRequest()
                .endpoint(TEST_UPDATED_ENDPOINT_URI)
                .name(TEST_WEBHOOK_UPDATED_NAME)
                .secret("updated-secret")
                .addChannelsSubscribedItem(CHANNEL_LOGIN) // drop the registration channel in this update
                .eventProfile(new WebhookRequestEventProfile()
                        .name(TEST_EVENT_PROFILE_NAME)
                        .uri(TEST_EVENT_PROFILE_URI))
                .status(WebhookRequest.StatusEnum.INACTIVE);

        String body = toJSONString(update);

        System.out.println(body);

        Response response = getResponseOfPut(WEBHOOK_MANAGEMENT_API_BASE_PATH + "/" + testWebhookId, body);
        response.then()
                .log().ifValidationFails()
                .assertThat()
                .statusCode(HttpStatus.SC_OK)
                .body("id", equalTo(testWebhookId))
                .body("name", equalTo(TEST_WEBHOOK_UPDATED_NAME))
                .body("endpoint", equalTo(TEST_UPDATED_ENDPOINT_URI))
                .body("status", equalTo(STATUS_INACTIVE))
                .body("$", not(hasKey("secret")))
                .body("channelsSubscribed.find { it.channelUri == '" + CHANNEL_LOGIN + "' }", notNullValue())
                .body("channelsSubscribed.find { it.channelUri == '" + CHANNEL_REG + "' }", nullValue());
    }

    @Test(dependsOnMethods = "testUpdateWebhook")
    public void testActivateWebhook() {

        Response response = getResponseOfPost(WEBHOOK_MANAGEMENT_API_BASE_PATH + "/" + testWebhookId + "/activate", "");
        response.then()
                .log().ifValidationFails()
                .assertThat()
                .statusCode(HttpStatus.SC_OK)
                .body("id", equalTo(testWebhookId))
                // Allow partial active in case any subscription is pending/error.
                .body("status", anyOf(equalTo("ACTIVE"), equalTo("PARTIALLY_ACTIVE")));
    }

    @Test(dependsOnMethods = "testActivateWebhook")
    public void testDeactivateWebhook() {

        Response response =
                getResponseOfPost(WEBHOOK_MANAGEMENT_API_BASE_PATH + "/" + testWebhookId + "/deactivate", "");
        response.then()
                .log().ifValidationFails()
                .assertThat()
                .statusCode(HttpStatus.SC_OK)
                .body("id", equalTo(testWebhookId))
                .body("status", anyOf(equalTo("INACTIVE"), equalTo("PARTIALLY_INACTIVE")));
    }

    @Test(dependsOnMethods = "testDeactivateWebhook")
    public void testDeleteWebhook() {

        getResponseOfDelete(WEBHOOK_MANAGEMENT_API_BASE_PATH + "/" + testWebhookId)
                .then()
                .log().ifValidationFails()
                .assertThat()
                .statusCode(HttpStatus.SC_NO_CONTENT);

        // Ensure it no longer appears in list.
        getResponseOfGet(WEBHOOK_MANAGEMENT_API_BASE_PATH)
                .then()
                .log().ifValidationFails()
                .assertThat()
                .statusCode(HttpStatus.SC_OK)
                .body("webhooks.find { it.id == '" + testWebhookId + "' }", nullValue());
    }
}

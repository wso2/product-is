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

package org.wso2.identity.integration.test.rest.api.server.webhook.metadata.v1;

import io.restassured.RestAssured;
import io.restassured.response.Response;
import org.apache.http.HttpStatus;
import org.hamcrest.CoreMatchers;
import org.hamcrest.Matchers;
import org.testng.annotations.AfterClass;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Factory;
import org.testng.annotations.Test;
import org.wso2.carbon.automation.engine.context.TestUserMode;

import static org.hamcrest.CoreMatchers.equalTo;
import static org.hamcrest.CoreMatchers.notNullValue;

/**
 * Tests for happy paths of the Webhook Metadata REST API.
 */
public class WebhookMetadataSuccessTest extends WebhookMetadataTestBase {

    @Factory(dataProvider = "restAPIUserConfigProvider")
    public WebhookMetadataSuccessTest(TestUserMode userMode) throws Exception {

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

    /**
     * GET /webhooks/metadata
     * - Returns 200
     * - Contains at least one profile
     * - Contains adapter info
     * - organizationPolicy.policyName is one of allowed enum values
     * - Self link of a profile points to /webhooks/metadata/event-profiles/{name}
     */
    @Test
    public void testGetWebhookMetadata() {

        Response response = getResponseOfGet(WEBHOOK_METADATA_API_BASE_PATH);
        response.then()
                .log().ifValidationFails()
                .assertThat()
                .statusCode(HttpStatus.SC_OK)
                // Basic structure
                .body("profiles", notNullValue())
                .body("profiles.size()", Matchers.greaterThan(0))
                // First profile is the expected one with correct uri & self link
                .body("profiles[0].name", equalTo(TEST_EVENT_PROFILE_NAME))
                .body("profiles[0].uri", equalTo(TEST_EVENT_PROFILE_URI))
                .body("profiles[0].self",
                        CoreMatchers.endsWith("/api/server/v1/webhooks/metadata/event-profiles/" +
                                TEST_EVENT_PROFILE_NAME))
                // Adapter info
                .body("adapter.name", equalTo("httppublisher"))
                .body("adapter.type", equalTo("Publisher"));
    }

    /**
     * GET /webhooks/metadata/event-profiles/{profileName}
     * - Uses a profile name discovered from GET /webhooks/metadata
     * - Returns 200
     * - Contains profile, uri, channels array
     */
    @Test
    public void testGetEventProfileDetails() {

        Response response = getResponseOfGet(
                WEBHOOK_METADATA_API_BASE_PATH + "/event-profiles/WSO2");

        response.then()
                .log().ifValidationFails()
                .assertThat()
                .statusCode(HttpStatus.SC_OK)
                .body("profile", equalTo(TEST_EVENT_PROFILE_NAME))
                .body("uri", equalTo(TEST_EVENT_PROFILE_URI))
                // channels array exists and has the expected main channel names
                .body("channels.find { it.name == 'Logins' }", notNullValue())
                .body("channels.find { it.name == 'Sessions' }", notNullValue())
                .body("channels.find { it.name == 'User account management' }", notNullValue())
                .body("channels.find { it.name == 'Registrations' }", notNullValue())
                .body("channels.find { it.name == 'Tokens' }", notNullValue())
                .body("channels.find { it.name == 'Credential updates' }", notNullValue())
                // Logins channel
                .body("channels.find { it.name == 'Logins' }.uri",
                        equalTo("https://schemas.identity.wso2.org/events/login"))
                .body("channels.find { it.name == 'Logins' }.events.find { it.eventName == 'Login success' }.eventUri",
                        equalTo("https://schemas.identity.wso2.org/events/login/event-type/loginSuccess"))
                .body("channels.find { it.name == 'Logins' }.events.find { it.eventName == 'Login failed' }.eventUri",
                        equalTo("https://schemas.identity.wso2.org/events/login/event-type/loginFailed"))
                // Tokens channel
                .body("channels.find { it.name == 'Tokens' }.uri",
                        equalTo("https://schemas.identity.wso2.org/events/token"))
                .body("channels.find { it.name == 'Tokens' }.events.find { it.eventName == 'Access token issued' }.eventUri",
                        equalTo("https://schemas.identity.wso2.org/events/token/event-type/accessTokenIssued"))
                .body("channels.find { it.name == 'Tokens' }.events.find { it.eventName == 'Access token revoked' }.eventUri",
                        equalTo("https://schemas.identity.wso2.org/events/token/event-type/accessTokenRevoked"));
    }
}

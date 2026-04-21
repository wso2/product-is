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
import org.apache.commons.lang.StringUtils;
import org.apache.http.HttpStatus;
import org.testng.annotations.AfterClass;
import org.testng.annotations.AfterMethod;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Factory;
import org.testng.annotations.Test;
import org.wso2.carbon.automation.engine.context.TestUserMode;

import static org.hamcrest.CoreMatchers.equalTo;
import static org.hamcrest.Matchers.notNullValue;

/**
 * Negative-path tests for the Webhook Metadata REST API.
 */
public class WebhookMetadataFailureTest extends WebhookMetadataTestBase {

    @Factory(dataProvider = "restAPIUserConfigProvider")
    public WebhookMetadataFailureTest(TestUserMode userMode) throws Exception {

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

    /**
     * GET /webhooks/metadata/event-profiles/{profileName} with a non-existing profile name.
     * Expect 404 Not Found with an Error payload.
     */
    @Test
    public void testGetEventProfileWithInvalidName() {

        getResponseOfGet(WEBHOOK_METADATA_API_BASE_PATH + "/event-profiles/" + INVALID_PROFILE_NAME)
                .then()
                .log().ifValidationFails()
                .statusCode(HttpStatus.SC_NOT_FOUND)
                .body("code", equalTo(PROFILE_NOT_FOUND_ERROR_CODE))
                .body("message", equalTo(PROFILE_NOT_FOUND_MESSAGE))
                .body("description", notNullValue());
    }
}

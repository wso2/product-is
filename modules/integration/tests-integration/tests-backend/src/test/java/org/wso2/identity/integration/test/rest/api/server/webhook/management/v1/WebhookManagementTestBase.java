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

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import io.restassured.RestAssured;
import org.apache.http.HttpStatus;
import org.testng.Assert;
import org.testng.annotations.AfterClass;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.DataProvider;
import org.wso2.carbon.automation.engine.context.TestUserMode;
import org.wso2.identity.integration.test.rest.api.server.common.RESTAPIServerTestBase;

import java.io.IOException;

public class WebhookManagementTestBase extends RESTAPIServerTestBase {

    private static final String API_DEFINITION_NAME = "webhook-management.yaml";
    protected static final String API_VERSION = "v1";
    protected static final String WEBHOOK_MANAGEMENT_API_BASE_PATH = "/webhooks";

    protected static final String INVALID_ID = "00000000-0000-0000-0000-000000000000";

    protected static final String CHANNEL_LOGIN = "https://schemas.identity.wso2.org/events/login";
    protected static final String CHANNEL_REG = "https://schemas.identity.wso2.org/events/registration";

    protected static final String TEST_WEBHOOK_NAME = "Login Webhook";
    protected static final String TEST_WEBHOOK_UPDATED_NAME = "Login Webhook Updated";
    protected static final String TEST_ENDPOINT_URI = "https://example.com/webhook";
    protected static final String TEST_UPDATED_ENDPOINT_URI = "https://example.com/webhook-updated";
    protected static final String TEST_EVENT_PROFILE_NAME = "WSO2";
    protected static final String TEST_EVENT_PROFILE_URI = "https://schemas.identity.wso2.org/events";
    protected static final String TEST_SECRET = "my-secret";

    protected static final String STATUS_ACTIVE = "ACTIVE";
    protected static final String STATUS_INACTIVE = "INACTIVE";

    protected static String swaggerDefinition;

    static {
        String API_PACKAGE_NAME = "org.wso2.carbon.identity.api.server.webhook.management.v1";
        try {
            swaggerDefinition = getAPISwaggerDefinition(API_PACKAGE_NAME, API_DEFINITION_NAME);
        } catch (IOException e) {
            Assert.fail(String.format("Unable to read the swagger definition %s from %s", API_DEFINITION_NAME,
                    API_PACKAGE_NAME), e);
        }
    }

    @BeforeClass(alwaysRun = true)
    @Override
    public void init() throws Exception {

        super.init();
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

    @DataProvider(name = "restAPIUserConfigProvider")
    public static Object[][] restAPIUserConfigProvider() {

        return new Object[][]{
                {TestUserMode.SUPER_TENANT_ADMIN},
                {TestUserMode.TENANT_ADMIN}
        };
    }

    /**
     * Delete a Webhook.
     *
     * @param webhookId ID of the Webhook.
     */
    protected void deleteWebhook(String webhookId) {

        getResponseOfDelete(WEBHOOK_MANAGEMENT_API_BASE_PATH + "/" +
                webhookId).then().assertThat().statusCode(HttpStatus.SC_NO_CONTENT);
    }

    /**
     * To convert object to a json string.
     *
     * @param object Respective java object.
     * @return Relevant json string.
     */
    protected String toJSONString(Object object) {

        Gson gson = new GsonBuilder().setPrettyPrinting().create();
        return gson.toJson(object);
    }
}

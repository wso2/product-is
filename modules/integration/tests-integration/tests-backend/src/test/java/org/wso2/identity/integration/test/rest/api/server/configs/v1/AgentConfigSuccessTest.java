/*
 * Copyright (c) 2026, WSO2 LLC. (http://www.wso2.com).
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

package org.wso2.identity.integration.test.rest.api.server.configs.v1;

import io.restassured.RestAssured;
import org.apache.http.HttpStatus;
import org.testng.annotations.AfterClass;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.DataProvider;
import org.testng.annotations.Factory;
import org.testng.annotations.Test;
import org.wso2.carbon.automation.engine.context.TestUserMode;

import java.io.IOException;

import static org.hamcrest.CoreMatchers.equalTo;

/**
 * Integration tests for the /configs/agent REST API (success paths).
 *
 * <p>Test flow:
 * <ol>
 *   <li>GET the agent configuration — expects 200 with the default {@code agentsExternallyManaged=false}</li>
 *   <li>PATCH to set {@code agentsExternallyManaged=true} — expects 200, then GET reflects the new value</li>
 *   <li>DELETE to revert the configuration — expects 204, then GET returns the default again</li>
 * </ol>
 */
public class AgentConfigSuccessTest extends ConfigTestBase {

    @Factory(dataProvider = "restAPIUserConfigProvider")
    public AgentConfigSuccessTest(TestUserMode userMode) throws Exception {

        super.init(userMode);
        this.context = isServer;
        this.authenticatingUserName = context.getContextTenant().getTenantAdmin().getUserName();
        this.authenticatingCredential = context.getContextTenant().getTenantAdmin().getPassword();
        this.tenant = context.getContextTenant().getDomain();
    }

    @BeforeClass(alwaysRun = true)
    public void init() throws IOException {

        super.testInit(API_VERSION, swaggerDefinition, tenant);
    }

    @AfterClass(alwaysRun = true)
    public void testConclude() {

        try {
            // Revert the agent configuration to its default state to avoid affecting subsequent tests.
            RestAssured.basePath = basePath;
            getResponseOfDelete(CONFIGS_AGENT_API_BASE_PATH);
        } finally {
            super.conclude();
        }
    }

    @DataProvider(name = "restAPIUserConfigProvider")
    public static Object[][] restAPIUserConfigProvider() {

        return new Object[][]{
                {TestUserMode.SUPER_TENANT_ADMIN},
                {TestUserMode.TENANT_ADMIN}
        };
    }

    @Test(groups = "wso2.is")
    public void testGetAgentConfiguration() {

        getResponseOfGet(CONFIGS_AGENT_API_BASE_PATH)
                .then()
                .log().ifValidationFails()
                .assertThat()
                .statusCode(HttpStatus.SC_OK)
                .body("agentsExternallyManaged", equalTo(false));
    }

    @Test(groups = "wso2.is", dependsOnMethods = {"testGetAgentConfiguration"})
    public void testPatchAgentConfiguration() throws IOException {

        String body = readResource("patch-agent-config.json");
        getResponseOfPatch(CONFIGS_AGENT_API_BASE_PATH, body)
                .then()
                .log().ifValidationFails()
                .assertThat()
                .statusCode(HttpStatus.SC_OK);

        getResponseOfGet(CONFIGS_AGENT_API_BASE_PATH)
                .then()
                .log().ifValidationFails()
                .assertThat()
                .statusCode(HttpStatus.SC_OK)
                .body("agentsExternallyManaged", equalTo(true));
    }

    @Test(groups = "wso2.is", dependsOnMethods = {"testPatchAgentConfiguration"})
    public void testDeleteAgentConfiguration() {

        getResponseOfDelete(CONFIGS_AGENT_API_BASE_PATH)
                .then()
                .log().ifValidationFails()
                .assertThat()
                .statusCode(HttpStatus.SC_NO_CONTENT);

        getResponseOfGet(CONFIGS_AGENT_API_BASE_PATH)
                .then()
                .log().ifValidationFails()
                .assertThat()
                .statusCode(HttpStatus.SC_OK)
                .body("agentsExternallyManaged", equalTo(false));
    }
}

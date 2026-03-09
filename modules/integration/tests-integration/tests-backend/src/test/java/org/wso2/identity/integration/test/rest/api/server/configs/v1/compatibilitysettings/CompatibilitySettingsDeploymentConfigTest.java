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

package org.wso2.identity.integration.test.rest.api.server.configs.v1.compatibilitysettings;

import io.restassured.RestAssured;
import io.restassured.response.Response;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.http.HttpStatus;
import org.testng.annotations.AfterClass;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;
import org.wso2.carbon.automation.engine.context.TestUserMode;
import org.wso2.carbon.integration.common.utils.mgt.ServerConfigurationManager;
import org.wso2.identity.integration.test.rest.api.server.configs.v1.ConfigTestBase;
import org.wso2.identity.integration.test.util.Utils;

import java.io.File;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;

import static org.hamcrest.CoreMatchers.equalTo;
import static org.hamcrest.core.IsNull.notNullValue;

/**
 * Integration tests that verify compatibility settings reflect deployment.toml overrides
 * (timestamp_reference, target_value, default_value) after merging the compatibility-settings
 * fragment into the existing deployment.toml and restarting the server.
 */
public class CompatibilitySettingsDeploymentConfigTest extends ConfigTestBase {

    private static final Log log = LogFactory.getLog(CompatibilitySettingsDeploymentConfigTest.class);
    private static final String FLOW_EXECUTION_GROUP = "flowExecution";
    private static final String ENABLE_LEGACY_FLOWS = "enableLegacyFlows";
    private static final String COMPATIBILITY_SETTINGS_FRAGMENT_TOML = "compatibility-settings-fragment.toml";

    private ServerConfigurationManager serverConfigurationManager;
    private File defaultConfigFile;

    public CompatibilitySettingsDeploymentConfigTest() throws Exception {

        super.init(TestUserMode.SUPER_TENANT_ADMIN);
        this.context = isServer;
        this.authenticatingUserName = context.getContextTenant().getTenantAdmin().getUserName();
        this.authenticatingCredential = context.getContextTenant().getTenantAdmin().getPassword();
        this.tenant = context.getContextTenant().getDomain();
    }

    @BeforeClass(alwaysRun = true)
    public void init() throws Exception {

        super.testInit(API_VERSION, swaggerDefinition, tenant);

        String carbonHome = Utils.getResidentCarbonHome();
        defaultConfigFile = getDeploymentTomlFile(carbonHome);
        File fragmentFile = new File(
                getISResourceLocation() + File.separator + "compatibility-settings" + File.separator
                        + COMPATIBILITY_SETTINGS_FRAGMENT_TOML);

        // Merge only the compatibility-settings fragment into existing deployment.toml to avoid conflicts
        String existingContent = new String(Files.readAllBytes(defaultConfigFile.toPath()), StandardCharsets.UTF_8);
        String fragmentContent = new String(Files.readAllBytes(fragmentFile.toPath()), StandardCharsets.UTF_8);
        String mergedContent = existingContent.trim() + "\n\n" + fragmentContent.trim() + "\n";

        File mergedTomlFile = File.createTempFile("deployment-merged-", ".toml");
        mergedTomlFile.deleteOnExit();
        Files.write(mergedTomlFile.toPath(), mergedContent.getBytes(StandardCharsets.UTF_8));

        log.info("Merging compatibility-settings fragment into existing deployment.toml and restarting the server.");
        serverConfigurationManager = new ServerConfigurationManager(isServer);
        serverConfigurationManager.applyConfigurationWithoutRestart(mergedTomlFile, defaultConfigFile, true);
        serverConfigurationManager.restartGracefully();

        log.info("Re-initializing after server restart.");
        super.init(TestUserMode.SUPER_TENANT_ADMIN);
        this.authenticatingUserName = context.getContextTenant().getTenantAdmin().getUserName();
        this.authenticatingCredential = context.getContextTenant().getTenantAdmin().getPassword();
        super.testInit(API_VERSION, swaggerDefinition, tenant);
    }

    @AfterClass(alwaysRun = true)
    public void tearDown() throws Exception {

        try {
            if (serverConfigurationManager != null) {
                log.info("Restoring deployment.toml to last configuration.");
                serverConfigurationManager.restoreToLastConfiguration(true);
            }
        } finally {
            super.conclude();
        }
    }

    @BeforeMethod(alwaysRun = true)
    public void testInit() {

        RestAssured.basePath = basePath;
    }

    @Test(description = "After deployment.toml override and restart, GET all compatibility settings returns 200")
    public void testCompatibilitySettingsReflectDeploymentConfigGetAll() throws Exception {

        Response response = getResponseOfGet(CONFIGS_COMPATIBILITY_SETTINGS_API_BASE_PATH);
        response.then()
                .log().ifValidationFails()
                .assertThat()
                .statusCode(HttpStatus.SC_OK);
        response.then().body("$", notNullValue());
    }

    @Test(description = "After deployment.toml override and restart, GET flowExecution reflects target_value=true")
    public void testCompatibilitySettingsReflectDeploymentConfigByGroup() throws Exception {

        Response response = getResponseOfGet(
                CONFIGS_COMPATIBILITY_SETTINGS_API_BASE_PATH + PATH_SEPARATOR + FLOW_EXECUTION_GROUP);
        response.then()
                .log().ifValidationFails()
                .assertThat()
                .statusCode(HttpStatus.SC_OK)
                .body(ENABLE_LEGACY_FLOWS, notNullValue());
        // compatibility-settings-fragment.toml sets target_value=true and timestamp 2030;
        // super tenant (org created before 2030) gets targetValue -> enableLegacyFlows should be true;
        response.then().body(ENABLE_LEGACY_FLOWS, equalTo("true"));
    }
}

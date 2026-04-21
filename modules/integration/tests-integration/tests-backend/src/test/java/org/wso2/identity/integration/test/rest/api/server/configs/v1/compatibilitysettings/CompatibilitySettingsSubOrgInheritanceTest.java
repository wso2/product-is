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
import org.apache.commons.lang.StringUtils;
import org.apache.http.HttpStatus;
import org.json.JSONObject;
import org.testng.Assert;
import org.testng.annotations.AfterClass;
import org.testng.annotations.AfterMethod;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.DataProvider;
import org.testng.annotations.Factory;
import org.testng.annotations.Test;
import org.wso2.carbon.automation.engine.context.TestUserMode;
import org.wso2.identity.integration.test.rest.api.server.configs.v1.ConfigTestBase;
import org.wso2.identity.integration.test.restclients.OrgMgtRestClient;

/**
 * Integration tests for Compatibility Settings REST API inheritance and overrides in sub-organizations.
 */
public class CompatibilitySettingsSubOrgInheritanceTest extends ConfigTestBase {

    private static final String AUTHORIZED_APIS_JSON = "authorized-apis.json";
    private static final String AUTHORIZED_APIS_RESOURCE_PATH =
            "/org/wso2/identity/integration/test/inheritance/" + AUTHORIZED_APIS_JSON;
    private static final String ORG_VERSION_V1 = "v1.0.0";
    private static final String SUB_ORG_NAME = "subOrg-compatibility-settings";

    private static final String FLOW_EXECUTION_GROUP = "flowExecution";
    private static final String ENABLE_LEGACY_FLOWS = "enableLegacyFlows";

    private OrgMgtRestClient orgMgtRestClient;
    private String subOrgId;
    private String switchedM2MToken;
    private String subOrgBasePath;

    private String rootOriginalValue;
    private String subOrgOriginalValue;
    private String rootPatchedValue;
    private String subOrgPatchedValue;

    @Factory(dataProvider = "restAPIUserConfigProvider")
    public CompatibilitySettingsSubOrgInheritanceTest(TestUserMode userMode) throws Exception {

        super.init(userMode);
        this.context = isServer;
        this.authenticatingUserName = context.getContextTenant().getTenantAdmin().getUserName();
        this.authenticatingCredential = context.getContextTenant().getTenantAdmin().getPassword();
        this.tenant = context.getContextTenant().getDomain();
    }

    @BeforeClass(alwaysRun = true)
    public void init() throws Exception {

        super.testInit(API_VERSION, swaggerDefinition, tenant);

        orgMgtRestClient = new OrgMgtRestClient(context, tenantInfo, serverURL,
                new JSONObject(readResource(AUTHORIZED_APIS_RESOURCE_PATH, this.getClass())));
        subOrgId = orgMgtRestClient.addOrganization(SUB_ORG_NAME);
        switchedM2MToken = orgMgtRestClient.switchM2MToken(subOrgId);
        orgMgtRestClient.updateOrganizationVersion(ORG_VERSION_V1);

        subOrgBasePath = buildSubOrgBasePath(tenant);
    }

    @AfterClass(alwaysRun = true)
    public void atEnd() throws Exception {

        try {
            restoreCompatibilitySettings();
        } finally {
            if (orgMgtRestClient != null) {
                orgMgtRestClient.deleteOrganization(subOrgId);
                orgMgtRestClient.closeHttpClient();
            }
            super.conclude();
        }
    }

    @BeforeMethod(alwaysRun = true)
    public void testInit() {

        RestAssured.basePath = basePath;
    }

    @AfterMethod(alwaysRun = true)
    public void testFinish() {

        RestAssured.basePath = StringUtils.EMPTY;
    }

    @DataProvider(name = "restAPIUserConfigProvider")
    public static Object[][] restAPIUserConfigProvider() {

        return new Object[][]{
                {TestUserMode.SUPER_TENANT_ADMIN},
                {TestUserMode.TENANT_ADMIN}
        };
    }

    @Test(description = "If not patched in root or sub-org, sub-org returns the root value.")
    public void testCompatibilitySettingsInheritanceWhenNotPatched() {

        rootOriginalValue = getEnableLegacyFlowsFromRoot();
        subOrgOriginalValue = getEnableLegacyFlowsFromSubOrg();

        Assert.assertEquals(subOrgOriginalValue, rootOriginalValue,
                "When not patched, sub-org should return the root value.");
    }

    @Test(dependsOnMethods = "testCompatibilitySettingsInheritanceWhenNotPatched",
            description = "If patched in root, updated value should be returned for both root and sub-org.")
    public void testCompatibilitySettingsInheritanceAfterRootPatch() {

        rootPatchedValue = toggle(rootOriginalValue);
        patchEnableLegacyFlowsInRoot(rootPatchedValue);

        Assert.assertEquals(getEnableLegacyFlowsFromRoot(), rootPatchedValue,
                "Root should return the updated value after root patch.");
        Assert.assertEquals(getEnableLegacyFlowsFromSubOrg(), rootPatchedValue,
                "Sub-org should inherit the updated value after root patch.");
    }

    @Test(dependsOnMethods = "testCompatibilitySettingsInheritanceAfterRootPatch",
            description = "If patched in sub-org, root retains its value while sub-org returns its patched value.")
    public void testCompatibilitySettingsOverrideAfterSubOrgPatch() {

        subOrgPatchedValue = toggle(rootPatchedValue);
        patchEnableLegacyFlowsInSubOrg(subOrgPatchedValue);

        Assert.assertEquals(getEnableLegacyFlowsFromRoot(), rootPatchedValue,
                "Root should retain its value when sub-org is patched.");
        Assert.assertEquals(getEnableLegacyFlowsFromSubOrg(), subOrgPatchedValue,
                "Sub-org should return its patched value after sub-org patch.");
    }

    private String getEnableLegacyFlowsFromRoot() {

        Response response = getResponseOfGet(
                CONFIGS_COMPATIBILITY_SETTINGS_API_BASE_PATH + PATH_SEPARATOR + FLOW_EXECUTION_GROUP);
        response.then()
                .log().ifValidationFails()
                .assertThat()
                .statusCode(HttpStatus.SC_OK);
        return response.then().extract().jsonPath().getString(ENABLE_LEGACY_FLOWS);
    }

    private String getEnableLegacyFlowsFromSubOrg() {

        String previousBasePath = RestAssured.basePath;
        try {
            RestAssured.basePath = subOrgBasePath;
            Response response = getResponseOfGetWithOAuth2(
                    CONFIGS_COMPATIBILITY_SETTINGS_API_BASE_PATH + PATH_SEPARATOR + FLOW_EXECUTION_GROUP,
                    switchedM2MToken);
            response.then()
                    .log().ifValidationFails()
                    .assertThat()
                    .statusCode(HttpStatus.SC_OK);
            return response.then().extract().jsonPath().getString(ENABLE_LEGACY_FLOWS);
        } finally {
            RestAssured.basePath = previousBasePath;
        }
    }

    private void patchEnableLegacyFlowsInRoot(String value) {

        Response patchResponse = getResponseOfPatch(CONFIGS_COMPATIBILITY_SETTINGS_API_BASE_PATH,
                buildFlowExecutionPatchBody(value));
        patchResponse.then()
                .log().ifValidationFails()
                .assertThat()
                .statusCode(HttpStatus.SC_OK);
    }

    private void patchEnableLegacyFlowsInSubOrg(String value) {

        String previousBasePath = RestAssured.basePath;
        try {
            RestAssured.basePath = subOrgBasePath;
            Response patchResponse = getResponseOfPatchWithOAuth2(CONFIGS_COMPATIBILITY_SETTINGS_API_BASE_PATH,
                    buildFlowExecutionPatchBody(value), switchedM2MToken);
            patchResponse.then()
                    .log().ifValidationFails()
                    .assertThat()
                    .statusCode(HttpStatus.SC_OK);
        } finally {
            RestAssured.basePath = previousBasePath;
        }
    }

    private String buildFlowExecutionPatchBody(String value) {

        boolean booleanValue = Boolean.parseBoolean(value);
        return "{ \"flowExecution\": { \"enableLegacyFlows\": " + booleanValue + " } }";
    }

    private static String toggle(String value) {

        return Boolean.toString(!Boolean.parseBoolean(value));
    }

    private String buildSubOrgBasePath(String tenantDomain) {

        String versionBase = String.format("/o/api/server/%s", API_VERSION);
        if ("carbon.super".equals(tenantDomain)) {
            return versionBase;
        }
        return String.format("/t/%s", tenantDomain) + versionBase;
    }

    private void restoreCompatibilitySettings() {

        RestAssured.basePath = basePath;
        try {
            // Restore root first (if we captured it).
            if (rootOriginalValue != null) {
                patchEnableLegacyFlowsInRoot(rootOriginalValue);
            }
            // Restore sub-org to root's original value to avoid keeping an override in the child org.
            if (rootOriginalValue != null && switchedM2MToken != null) {
                patchEnableLegacyFlowsInSubOrg(rootOriginalValue);
            }
        } finally {
            RestAssured.basePath = StringUtils.EMPTY;
        }
    }
}


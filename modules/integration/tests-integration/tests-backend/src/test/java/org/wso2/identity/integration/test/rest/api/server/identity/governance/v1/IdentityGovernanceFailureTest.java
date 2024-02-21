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

package org.wso2.identity.integration.test.rest.api.server.identity.governance.v1;

import io.restassured.RestAssured;
import io.restassured.response.Response;
import org.apache.commons.lang.StringUtils;
import org.apache.http.HttpStatus;
import org.testng.annotations.AfterClass;
import org.testng.annotations.AfterMethod;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.DataProvider;
import org.testng.annotations.Factory;
import org.testng.annotations.Test;
import org.wso2.carbon.automation.engine.context.TestUserMode;

import java.io.IOException;

/**
 * Test class for Claim Management REST APIs failure path.
 */
public class IdentityGovernanceFailureTest extends IdentityGovernanceTestBase {

    private static final String CATEGORY_PASSWORD_POLICIES = "UGFzc3dvcmQgUG9saWNpZXM";
    private static final String INCORRECT_CATEGORY_PASSWORD_POLICIES = "YWNjb3VudC5sb2mhhbmRsZXI";
    private static final String CATEGORY_ACCOUNT_MANAGEMENT_PROPERTIES = "QWNjb3VudCBNYW5hZ2VtZW50";

    @Factory(dataProvider = "restAPIUserConfigProvider")
    public IdentityGovernanceFailureTest(TestUserMode userMode) throws Exception {

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

        super.conclude();
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

    @Test
    public void testGetGovernanceConnectorCategory() {

        Response response = getResponseOfGet(IDENTITY_GOVERNANCE_ENDPOINT_URI + "/" + "randomCategory");
        validateErrorResponse(response, HttpStatus.SC_NOT_FOUND, "IDG-50008", "randomCategory");
    }

    @Test
    public void testGetGovernanceConnectors() {

        Response response = getResponseOfGet(IDENTITY_GOVERNANCE_ENDPOINT_URI + "/" + "randomCategory" + "/" +
                "connectors/randomConnector");
        validateErrorResponse(response, HttpStatus.SC_NOT_FOUND, "IDG-50009", "randomConnector");
    }

    @Test
    public void testSearchGovernanceConnectorProperties() throws IOException {

        String body = readResource("get-properties-with-invalid-connector-name.json");
        Response response = getResponseOfPost(IDENTITY_GOVERNANCE_ENDPOINT_URI + "/preferences", body);
        validateErrorResponse(response, HttpStatus.SC_BAD_REQUEST, "IDG-50011", "self-sin-up");
    }

    @Test
    public void testSearchInvalidGovernanceConnectorProperties() throws IOException {

        String body = readResource("get-properties-with-invalid-property-name.json");
        Response response = getResponseOfPost(IDENTITY_GOVERNANCE_ENDPOINT_URI + "/preferences", body);
        validateErrorResponse(response, HttpStatus.SC_BAD_REQUEST, "IDG-50012",
                "SelfRegistration.Enble");
    }

    @Test(description = "Update governance connectors with incorrect category ID.")
    public void testUpdateGovernanceConnectorsIncorrectCategoryID() throws IOException {

        String body = readResource("update-multiple-connector-properties.json");
        Response response = getResponseOfPatch(IDENTITY_GOVERNANCE_ENDPOINT_URI +
                "/" + INCORRECT_CATEGORY_PASSWORD_POLICIES + "/connectors/", body);
        validateErrorResponse(response, HttpStatus.SC_NOT_FOUND, "IDG-50008");
    }

    @Test(description = "Update governance connectors with incorrect connector ID.")
    public void testUpdateGovernanceConnectorsIncorrectConnectorID() throws IOException {

        String body = readResource("update-multiple-connector-properties-incorrect.json");
        Response response = getResponseOfPatch(IDENTITY_GOVERNANCE_ENDPOINT_URI +
                                "/" + CATEGORY_PASSWORD_POLICIES + "/connectors/", body);
        validateErrorResponse(response, HttpStatus.SC_NOT_FOUND, "IDG-50009");
    }

    @Test(description = "Update governance connectors with a mismatching category.")
    public void testUpdateGovernanceConnectorsMismatch() throws IOException {

        String body = readResource("update-multiple-connector-properties.json");
        Response response = getResponseOfPatch(IDENTITY_GOVERNANCE_ENDPOINT_URI +
                "/" + CATEGORY_ACCOUNT_MANAGEMENT_PROPERTIES + "/connectors/", body);
        validateErrorResponse(response, HttpStatus.SC_NOT_FOUND, "IDG-50009");
    }
}

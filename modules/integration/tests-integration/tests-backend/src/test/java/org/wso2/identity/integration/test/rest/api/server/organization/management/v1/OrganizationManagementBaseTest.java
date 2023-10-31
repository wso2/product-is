/*
 * Copyright (c) 2023, WSO2 LLC. (http://www.wso2.com).
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

package org.wso2.identity.integration.test.rest.api.server.organization.management.v1;

import io.restassured.RestAssured;
import io.restassured.response.Response;
import org.apache.commons.lang.StringUtils;
import org.apache.http.HttpHeaders;
import org.apache.http.HttpStatus;
import org.testng.Assert;
import org.testng.ISuite;
import org.testng.ITestContext;
import org.testng.annotations.AfterClass;
import org.testng.annotations.AfterMethod;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.DataProvider;
import org.wso2.carbon.automation.engine.context.TestUserMode;
import org.wso2.identity.integration.test.rest.api.server.common.B2BRESTAPIServerTestBase;
import org.wso2.identity.integration.test.rest.api.server.organization.management.v1.model.OrganizationLevel;
import org.wso2.identity.integration.test.utils.CarbonUtils;

import java.io.IOException;
import java.util.Set;

import static org.hamcrest.core.IsNull.notNullValue;
import static org.wso2.identity.integration.test.rest.api.server.organization.management.v1.Utils.assertNotBlank;
import static org.wso2.identity.integration.test.rest.api.server.organization.management.v1.Utils.extractOrganizationIdFromLocationHeader;

/**
 * Base test class for Organization Management REST APIs.
 */
public class OrganizationManagementBaseTest extends B2BRESTAPIServerTestBase {

    private static final String API_DEFINITION_NAME = "org.wso2.carbon.identity.organization.management.yaml";
    static final String API_VERSION = "v1";
    public final OrganizationLevel organizationLevel;
    static final String SUPER_ORGANIZATION_NAME = "Super";
    static final String ORGANIZATION_NAME = "name";
    static final String ORGANIZATION_PARENT_ID = "parentId";
    static final String SUPER_ORGANIZATION_ID = "10084a8d-113f-4211-a0d5-efe36b082211";
    protected String subOrganizationId;
    protected static final String ORGANIZATION_MANAGEMENT_API_BASE_PATH = "/organizations";
    protected static String swaggerDefinition;
    static boolean isLegacyRuntimeEnabled;

    static {
        String apiPackageName = "org.wso2.carbon.identity.api.server.organization.management.v1";
        try {
            swaggerDefinition = getAPISwaggerDefinition(apiPackageName, API_DEFINITION_NAME);
        } catch (IOException e) {
            Assert.fail(String.format("Unable to read the swagger definition %s from %s", API_DEFINITION_NAME,
                    apiPackageName), e);
        }
    }

    public OrganizationManagementBaseTest(TestUserMode userMode, OrganizationLevel organizationLevel) throws Exception {

        this.organizationLevel = organizationLevel;
        super.init(userMode);
        this.context = isServer;
        this.authenticatingUserName = context.getContextTenant().getTenantAdmin().getUserName();
        this.authenticatingCredential = context.getContextTenant().getTenantAdmin().getPassword();
    }

    @BeforeClass(alwaysRun = true)
    public void init(ITestContext context) throws Exception {

        ISuite suite = context.getSuite();
        isLegacyRuntimeEnabled = CarbonUtils.isLegacyAuthzRuntimeEnabled();
        String orgId = (String) suite.getAttribute("createdOrgId");
        if (orgId == null) {
            orgId = SUPER_ORGANIZATION_ID;
        }
        this.subOrganizationId = orgId;
        if (OrganizationLevel.SUPER_ORGANIZATION.equals(this.organizationLevel)) {
            super.testInitWithoutTenantQualifiedPath(API_VERSION, swaggerDefinition);
        } else {
            this.tenant = subOrganizationId;
            String tenantDomain = tenantInfo.getDomain();
            this.authenticatingUserName = "admin@" + tenantDomain;
            super.testInit(API_VERSION, swaggerDefinition, tenantDomain);
        }
    }

    @AfterClass(alwaysRun = true)
    public void testConclude() throws Exception {

        super.conclude();
    }

    @BeforeMethod(alwaysRun = true)
    public void testInit(ITestContext context) throws Exception {

        RestAssured.basePath = basePath;
        ISuite suite = context.getSuite();
        String orgId = (String) suite.getAttribute("createdOrgId");

        if (orgId == null) {
            orgId = createBaseOrg();
            suite.setAttribute("createdOrgId", orgId);
        }
    }

    @AfterMethod(alwaysRun = true)
    public void testFinish() throws Exception{

        RestAssured.basePath = StringUtils.EMPTY;
    }

    @DataProvider(name = "restAPIUserConfigProvider")
    public static Object[][] restAPIUserConfigProvider() {

        return new Object[][]{
                {TestUserMode.SUPER_TENANT_ADMIN, OrganizationLevel.SUPER_ORGANIZATION},
                {TestUserMode.SUPER_TENANT_ADMIN, OrganizationLevel.SUB_ORGANIZATION}
        };
    }

    @DataProvider(name = "initRESTAPIUserConfigProvider")
    public static Object[][] initRESTAPIUserConfigProvider() {

        return new Object[][]{
                {TestUserMode.SUPER_TENANT_ADMIN, OrganizationLevel.SUPER_ORGANIZATION},
        };
    }

    protected void cleanUpOrganizations(Set<String> orgsToCleanUp) {

        orgsToCleanUp.forEach(orgId -> {
            String organizationPath = ORGANIZATION_MANAGEMENT_API_BASE_PATH + "/" + orgId;
            Response responseOfDelete = getResponseOfDelete(organizationPath);
            responseOfDelete.then()
                    .log()
                    .ifValidationFails()
                    .assertThat()
                    .statusCode(HttpStatus.SC_NO_CONTENT);
        });
    }

    private String createBaseOrg() {

        String body = "{\n" +
                "  \"name\": \"ABC Builders\",\n" +
                "  \"description\": \"Building constructions\",\n" +
                "  \"type\": \"TENANT\",\n" +
                "  \"parentId\": \"10084a8d-113f-4211-a0d5-efe36b082211\",\n" +
                "  \"attributes\": [\n" +
                "    {\n" +
                "      \"key\": \"Country\",\n" +
                "      \"value\": \"Sri Lanka\"\n" +
                "    }\n" +
                "  ]\n" +
                "}";
        Response responseOfPost = getResponseOfPost(ORGANIZATION_MANAGEMENT_API_BASE_PATH, body);
        responseOfPost.then()
                .log().ifValidationFails()
                .assertThat()
                .statusCode(HttpStatus.SC_CREATED)
                .header(HttpHeaders.LOCATION, notNullValue());

        String location = responseOfPost.getHeader(HttpHeaders.LOCATION);
        String createdOrgId = extractOrganizationIdFromLocationHeader(location);
        assertNotBlank(createdOrgId);
        return createdOrgId;
    }
}

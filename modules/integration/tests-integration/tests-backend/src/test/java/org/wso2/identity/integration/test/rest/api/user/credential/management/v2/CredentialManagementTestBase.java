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

package org.wso2.identity.integration.test.rest.api.user.credential.management.v2;

import io.restassured.RestAssured;
import org.apache.commons.lang.StringUtils;
import org.json.JSONException;
import org.json.JSONObject;
import org.testng.Assert;
import org.testng.annotations.AfterClass;
import org.testng.annotations.AfterMethod;
import org.testng.annotations.BeforeMethod;
import org.wso2.carbon.base.MultitenantConstants;
import org.wso2.identity.integration.test.rest.api.user.common.RESTAPIUserTestBase;

import java.io.IOException;

/**
 * Base test class for Credential Management REST API v2.
 */
public class CredentialManagementTestBase extends RESTAPIUserTestBase {

    private static final String API_DEFINITION_NAME = "credential-management.yaml";
    protected static final String API_VERSION = "v2";
    private static final String API_PACKAGE_NAME = "org.wso2.carbon.identity.rest.api.user.credential.v2";

    public static final String PATH_SEPARATOR = "/";
    protected static final String ORGANIZATION_PATH_SPECIFIER = "/o";
    protected static final String TENANTED_URL_PATH_SPECIFIER = "/t/";

    protected static final String TYPE_PASSKEY = "passkey";
    protected static final String TYPE_PUSH_AUTH = "push-auth";
    protected static final String TYPE_BACKUP_CODE = "backup-code";

    protected static final String SECONDARY_ORG_USER_ID = "secondary-org-user-789";
    protected static final String TEST_PASSKEY_CREDENTIAL_ID = "test-passkey-credential-001";

    protected static String swaggerDefinition;

    static {
        try {
            swaggerDefinition = getAPISwaggerDefinition(API_PACKAGE_NAME, API_DEFINITION_NAME);
        } catch (IOException e) {
            Assert.fail(String.format("Unable to read the swagger definition %s from %s", API_DEFINITION_NAME,
                    API_PACKAGE_NAME), e);
        }
    }

    @AfterClass(alwaysRun = true)
    public void testConclude() throws Exception {

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

    /**
     * Get the list of APIs that need to be authorized for credential management v2 operations.
     *
     * @return A JSON object containing the API and scopes list.
     * @throws JSONException If an error occurs while creating the JSON object.
     */
    protected JSONObject getAuthorizedAPIList() throws JSONException {

        JSONObject jsonObject = new JSONObject();
        jsonObject.put("/api/server/v1/organizations",
                new String[]{"internal_organization_create", "internal_organization_delete",
                        "internal_organization_view"});
        return jsonObject;
    }

    /**
     * Convert tenant base path to organization base path.
     *
     * @param basePath Tenant base path.
     * @return Organization base path.
     */
    protected String convertToOrgBasePath(String basePath) {

        if (StringUtils.equals(MultitenantConstants.SUPER_TENANT_DOMAIN_NAME, tenant)) {
            return TENANTED_URL_PATH_SPECIFIER + tenant + ORGANIZATION_PATH_SPECIFIER + basePath;
        } else {
            return basePath.replace(tenant, tenant + ORGANIZATION_PATH_SPECIFIER);
        }
    }
}

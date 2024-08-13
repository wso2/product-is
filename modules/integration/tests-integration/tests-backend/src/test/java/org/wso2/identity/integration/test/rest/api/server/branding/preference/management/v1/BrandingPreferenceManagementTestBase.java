/*
 * Copyright (c) 2021-2024, WSO2 LLC. (http://www.wso2.com).
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

package org.wso2.identity.integration.test.rest.api.server.branding.preference.management.v1;

import io.restassured.RestAssured;
import org.apache.commons.lang.StringUtils;
import org.testng.Assert;
import org.testng.annotations.AfterClass;
import org.testng.annotations.AfterMethod;
import org.testng.annotations.BeforeMethod;
import org.wso2.identity.integration.test.rest.api.server.common.RESTAPIServerTestBase;

import java.io.IOException;

/**
 * Base test class for the Branding Preference Management Rest APIs.
 */
public class BrandingPreferenceManagementTestBase extends RESTAPIServerTestBase {

    public static final String API_DEFINITION_NAME = "branding-preference.yaml";
    public static final String API_VERSION = "v1";
    public static final String API_PACKAGE_NAME =
            "org.wso2.carbon.identity.api.server.branding.preference.management.v1";
    public static final String BRANDING_PREFERENCE_API_BASE_PATH = "/branding-preference";
    public static final String CUSTOM_TEXT_API_BASE_PATH = "/branding-preference/text";
    public static final String BRANDING_PREFERENCE_RESOLVE_PATH = "/branding-preference/resolve";
    public static final String API_SERVER_BASE_PATH = "api/server/v1";
    public static final String PATH_SEPARATOR = "/";
    public static final String ORGANIZATION_PATH = "o/";
    public static final String TENANT_PATH = "t/";
    public static final String QUERY_PARAM_SEPARATOR = "?";
    public static final String ORGANIZATION_TYPE = "ORG";
    public static final String CUSTOM_TYPE = "CUSTOM";
    public static final String DEFAULT_LOCALE = "en-US";
    public static final String LOGIN_SCREEN = "login";
    public static final String PREFERENCE_COMPONENT_WITH_QUERY_PARAM = "?type=%s&name=%s&locale=%s";
    public static final String CUSTOM_TEXT_COMPONENT_WITH_QUERY_PARAM = "?type=%s&name=%s&screen=%s&locale=%s";
    public static final String TYPE_QUERY_PARAM = "type=%s";
    public static final String LOCALE_QUERY_PARAM = "locale=%s";
    public static final String SCREEN_QUERY_PARAM = "screen=%s";
    public static final String RESTRICTED_TO_PUBLISHED_QUERY_PARAM = "restrictToPublished=%s";
    public static final String CONTENT_TYPE_ATTRIBUTE = "Content-Type";
    public static final String AUTHORIZATION_ATTRIBUTE = "Authorization";
    public static final String ORG_NAME_PLACEHOLDER = "organization-name";
    public static final String TRUE = "true";
    public static final String AMPERSAND = "&";

    public static final String ADD_ROOT_ORG_BRANDING_RESOURCE_FILE = "add-root-org-branding-preference.json";
    public static final String UPDATE_ROOT_ORG_BRANDING_RESOURCE_FILE = "update-root-org-branding-preference.json";
    public static final String ADD_L1_ORG_BRANDING_RESOURCE_FILE = "add-l1-org-branding-preference.json";
    public static final String ADD_L2_ORG_BRANDING_RESOURCE_FILE = "add-l2-org-branding-preference.json";

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
}

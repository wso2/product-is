/*
 * Copyright (c) 2023-2024, WSO2 LLC. (http://www.wso2.com).
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
import org.apache.commons.lang.StringUtils;
import org.testng.Assert;
import org.testng.annotations.AfterClass;
import org.testng.annotations.AfterMethod;
import org.testng.annotations.BeforeMethod;
import org.wso2.identity.integration.test.rest.api.server.common.RESTAPIServerTestBase;
import org.wso2.identity.integration.test.restclients.OAuth2RestClient;

import java.io.IOException;

/**
 * Base test class for the Organization Management REST APIs.
 */
public class OrganizationManagementBaseTest extends RESTAPIServerTestBase {

    public static final String SUPER_ORGANIZATION_ID = "10084a8d-113f-4211-a0d5-efe36b082211";
    public static final String SUPER_TENANT_DOMAIN = "carbon.super";
    private static final String API_DEFINITION_NAME = "org.wso2.carbon.identity.organization.management.yaml";
    static final String API_VERSION = "v1";

    static final String ORGANIZATION_MANAGEMENT_API_BASE_PATH = "/organizations";
    static final String ORGANIZATION_CONFIGS_API_BASE_PATH = "/organization-configs";
    static final String ORGANIZATION_DISCOVERY_API_PATH = "/discovery";
    static final String ORGANIZATION_META_ATTRIBUTES_API_PATH = "/meta-attributes";
    static final String PATH_SEPARATOR = "/";

    protected static final String ORGANIZATION_ID = "id";
    protected static final String ORGANIZATION_NAME = "name";
    protected static final String ORGANIZATION_NAME_FORMAT = "Org-%d";

    protected static final String ORGANIZATION_EMAIL_FORMAT_1 = "org%d.com";
    protected static final String ORGANIZATION_EMAIL_FORMAT_2 = "organization%d.com";

    protected static final String LIMIT_QUERY_PARAM = "limit";
    protected static final String AFTER_QUERY_PARAM = "after";
    protected static final String BEFORE_QUERY_PARAM = "before";
    protected static final String RECURSIVE_QUERY_PARAM = "recursive";
    protected static final String OFFSET_QUERY_PARAM = "offset";
    protected static final String FILTER_QUERY_PARAM = "filter";

    protected static final String ORGANIZATIONS_PATH_PARAM = "organizations";
    protected static final String LINKS_PATH_PARAM = "links";
    protected static final String COUNT_PATH_PARAM = "count";
    protected static final String TOTAL_RESULTS_PATH_PARAM = "totalResults";
    protected static final String START_INDEX_PATH_PARAM = "startIndex";

    protected static final String ORGANIZATION_NAME_ATTRIBUTE = "organizationName";
    protected static final String ORGANIZATION_MULTIPLE_META_ATTRIBUTE_ATTRIBUTES = "attributes";

    protected static final String LINK_REL_PREVIOUS = "previous";
    protected static final String LINK_REL_NEXT = "next";
    protected static final String REL = "rel";
    protected static final String HREF = "href";

    protected static final String AMPERSAND = "&";
    protected static final String QUESTION_MARK = "?";
    protected static final String EQUAL = "=";

    protected static final String ZERO = "0";

    protected static final String FALSE = "false";

    protected static final String TOTAL_RESULT_MISMATCH_ERROR = "Total results mismatched.";
    protected static final String START_INDEX_MISMATCH_ERROR = "Start index mismatched.";
    protected static final String COUNT_MISMATCH_ERROR = "Count mismatch";

    protected static final int NUM_OF_ORGANIZATIONS_FOR_PAGINATION_TESTS = 20;
    protected static final int DEFAULT_ORG_LIMIT = 15;
    protected static final int NUM_OF_ORGANIZATIONS_WITH_META_ATTRIBUTES = 3;
    protected static final int DEFAULT_META_ATTRIBUTES_LIMIT = 15;

    protected static String swaggerDefinition;
    protected OAuth2RestClient oAuth2RestClient;

    static {
        String API_PACKAGE_NAME = "org.wso2.carbon.identity.api.server.organization.management.v1";

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

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

package org.wso2.identity.integration.test.rest.api.server.policy.management.v1;

import io.restassured.RestAssured;
import io.restassured.response.Response;
import org.apache.http.HttpStatus;
import org.testng.Assert;
import org.testng.annotations.AfterClass;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Factory;
import org.testng.annotations.Test;
import org.wso2.carbon.automation.engine.context.TestUserMode;
import org.wso2.identity.integration.test.rest.api.server.policy.management.v1.model.PolicyUpdateRequest;

import static org.hamcrest.CoreMatchers.endsWith;
import static org.hamcrest.CoreMatchers.equalTo;
import static org.hamcrest.CoreMatchers.notNullValue;
import static org.hamcrest.CoreMatchers.nullValue;
import static org.hamcrest.Matchers.greaterThanOrEqualTo;
import static org.hamcrest.Matchers.hasSize;

/**
 * Tests for happy paths of the Policy Management REST API.
 */
public class PolicyManagementSuccessTest extends PolicyManagementTestBase {

    private static final String TEST_POLICY_NAME = "CorporateDevicePolicy";
    private static final String SECOND_TEST_POLICY_NAME = "ContractorDevicePolicy";

    private String testPolicyId;
    private String secondTestPolicyId;

    @Factory(dataProvider = "restAPIUserConfigProvider")
    public PolicyManagementSuccessTest(TestUserMode userMode) throws Exception {

        super.init(userMode);
        this.context = isServer;
        this.authenticatingUserName = context.getContextTenant().getTenantAdmin().getUserName();
        this.authenticatingCredential = context.getContextTenant().getTenantAdmin().getPassword();
        this.tenant = context.getContextTenant().getDomain();
    }

    @BeforeClass(alwaysRun = true)
    public void init() throws Exception {

        super.testInit(API_VERSION, swaggerDefinition, tenant);
    }

    @AfterClass(alwaysRun = true)
    public void concludeClass() {

        // Delete is idempotent, so this is safe even if the tests already removed the policies.
        if (testPolicyId != null) {
            deletePolicy(testPolicyId);
        }
        if (secondTestPolicyId != null) {
            deletePolicy(secondTestPolicyId);
        }
        super.conclude();
    }

    @BeforeMethod(alwaysRun = true)
    public void setBasePath() {

        RestAssured.basePath = basePath;
    }

    @Test
    public void testCreatePolicy() {

        String body = toJSONString(buildIosPolicyRequest(TEST_POLICY_NAME));
        Response response = getResponseOfPost(POLICY_MANAGEMENT_API_BASE_PATH, body);
        response.then()
                .log().ifValidationFails()
                .assertThat()
                .statusCode(HttpStatus.SC_CREATED)
                .body("id", notNullValue())
                .body("name", equalTo(TEST_POLICY_NAME))
                .body("resources", hasSize(1))
                .body("resources[0].target", equalTo(TARGET_IOS))
                .body("resources[0].resourceType", equalTo(RESOURCE_TYPE_RULE))
                .body("resources[0].rule.rules", hasSize(2))
                .body("resources[0].rule.rules[0].expressions", hasSize(2))
                .body("resources[0].rule.rules[0].expressions.find { it.field == '" + FIELD_PLATFORM + "' }"
                        + ".operator", equalTo(OPERATOR_EQUALS))
                .body("resources[0].rule.rules[0].expressions.find { it.field == '" + FIELD_PLATFORM + "' }"
                        + ".value.value", equalTo(TARGET_IOS))
                .body("resources[0].rule.rules[1].expressions[0].field", equalTo(FIELD_JAILBREAK));

        testPolicyId = response.getBody().jsonPath().getString("id");
        String location = response.getHeader("Location");
        Assert.assertNotNull(location, "Location header is not returned for the created policy.");
        Assert.assertTrue(location.endsWith(POLICY_MANAGEMENT_API_BASE_PATH + "/" + testPolicyId),
                "Location header does not point to the created policy: " + location);
    }

    @Test(dependsOnMethods = "testCreatePolicy")
    public void testGetPolicyById() {

        getResponseOfGet(POLICY_MANAGEMENT_API_BASE_PATH + "/" + testPolicyId)
                .then()
                .log().ifValidationFails()
                .assertThat()
                .statusCode(HttpStatus.SC_OK)
                .body("id", equalTo(testPolicyId))
                .body("name", equalTo(TEST_POLICY_NAME))
                .body("resources", hasSize(1))
                .body("resources[0].target", equalTo(TARGET_IOS))
                .body("resources[0].resourceType", equalTo(RESOURCE_TYPE_RULE))
                .body("resources[0].rule.rules", hasSize(2))
                .body("resources[0].rule.rules[0].expressions.find { it.field == '" + FIELD_IOS_OS_VERSION
                        + "' }.operator", equalTo(OPERATOR_GREATER_THAN));
    }

    @Test(dependsOnMethods = "testGetPolicyById")
    public void testUpdatePolicy() {

        PolicyUpdateRequest updateRequest = new PolicyUpdateRequest()
                .addResourceItem(ruleResource(TARGET_ANDROID,
                        andRule(expression(FIELD_PLATFORM, OPERATOR_EQUALS, TARGET_ANDROID),
                                expression(FIELD_ANDROID_OS_VERSION, OPERATOR_GREATER_THAN, "12"),
                                expression(FIELD_IS_ROOTED, OPERATOR_EQUALS, "false"))));

        getResponseOfPut(POLICY_MANAGEMENT_API_BASE_PATH + "/" + testPolicyId, toJSONString(updateRequest))
                .then()
                .log().ifValidationFails()
                .assertThat()
                .statusCode(HttpStatus.SC_OK)
                .body("id", equalTo(testPolicyId))
                // Policy name is immutable, so it must be retained.
                .body("name", equalTo(TEST_POLICY_NAME))
                .body("resources", hasSize(1))
                .body("resources[0].target", equalTo(TARGET_ANDROID))
                .body("resources[0].rule.rules", hasSize(1))
                .body("resources[0].rule.rules[0].expressions", hasSize(3))
                .body("resources[0].rule.rules[0].expressions.find { it.field == '" + FIELD_PLATFORM + "' }"
                        + ".value.value", equalTo(TARGET_ANDROID));

        // Verify the update is persisted.
        getResponseOfGet(POLICY_MANAGEMENT_API_BASE_PATH + "/" + testPolicyId)
                .then()
                .log().ifValidationFails()
                .assertThat()
                .statusCode(HttpStatus.SC_OK)
                .body("resources", hasSize(1))
                .body("resources[0].target", equalTo(TARGET_ANDROID));
    }

    @Test(dependsOnMethods = "testUpdatePolicy")
    public void testListPolicies() {

        secondTestPolicyId = createPolicy(SECOND_TEST_POLICY_NAME);

        getResponseOfGet(POLICY_MANAGEMENT_API_BASE_PATH)
                .then()
                .log().ifValidationFails()
                .assertThat()
                .statusCode(HttpStatus.SC_OK)
                .body("totalResults", greaterThanOrEqualTo(2))
                .body("startIndex", equalTo(1))
                .body("policies.find { it.id == '" + testPolicyId + "' }.name", equalTo(TEST_POLICY_NAME))
                .body("policies.find { it.id == '" + testPolicyId + "' }.self",
                        endsWith(POLICY_MANAGEMENT_API_BASE_PATH + "/" + testPolicyId))
                .body("policies.find { it.id == '" + secondTestPolicyId + "' }.name",
                        equalTo(SECOND_TEST_POLICY_NAME));
    }

    @Test(dependsOnMethods = "testListPolicies")
    public void testListPoliciesWithFilter() {

        getResponseOfGet(POLICY_MANAGEMENT_API_BASE_PATH + "?filter=Corporate")
                .then()
                .log().ifValidationFails()
                .assertThat()
                .statusCode(HttpStatus.SC_OK)
                .body("totalResults", equalTo(1))
                .body("count", equalTo(1))
                .body("policies[0].id", equalTo(testPolicyId))
                .body("policies[0].name", equalTo(TEST_POLICY_NAME))
                .body("policies.find { it.id == '" + secondTestPolicyId + "' }", nullValue());
    }

    @Test(dependsOnMethods = "testListPolicies")
    public void testListPoliciesWithPagination() {

        getResponseOfGet(POLICY_MANAGEMENT_API_BASE_PATH + "?limit=1&offset=0")
                .then()
                .log().ifValidationFails()
                .assertThat()
                .statusCode(HttpStatus.SC_OK)
                .body("count", equalTo(1))
                .body("startIndex", equalTo(1))
                .body("policies", hasSize(1))
                .body("links.find { it.rel == 'next' }", notNullValue())
                .body("links.find { it.rel == 'previous' }", nullValue());

        getResponseOfGet(POLICY_MANAGEMENT_API_BASE_PATH + "?limit=1&offset=1")
                .then()
                .log().ifValidationFails()
                .assertThat()
                .statusCode(HttpStatus.SC_OK)
                .body("count", equalTo(1))
                .body("startIndex", equalTo(2))
                .body("policies", hasSize(1))
                .body("links.find { it.rel == 'previous' }", notNullValue());
    }

    @Test(dependsOnMethods = {"testListPoliciesWithFilter", "testListPoliciesWithPagination"})
    public void testDeletePolicy() {

        deletePolicy(testPolicyId);
        deletePolicy(secondTestPolicyId);

        getResponseOfGet(POLICY_MANAGEMENT_API_BASE_PATH + "/" + testPolicyId)
                .then()
                .log().ifValidationFails()
                .assertThat()
                .statusCode(HttpStatus.SC_NOT_FOUND);

        getResponseOfGet(POLICY_MANAGEMENT_API_BASE_PATH)
                .then()
                .log().ifValidationFails()
                .assertThat()
                .statusCode(HttpStatus.SC_OK)
                .body("policies.find { it.id == '" + testPolicyId + "' }", nullValue())
                .body("policies.find { it.id == '" + secondTestPolicyId + "' }", nullValue());

        testPolicyId = null;
        secondTestPolicyId = null;
    }
}

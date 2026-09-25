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
import org.apache.commons.lang3.StringUtils;
import org.apache.http.HttpStatus;
import org.testng.annotations.AfterClass;
import org.testng.annotations.AfterMethod;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Factory;
import org.testng.annotations.Test;
import org.wso2.carbon.automation.engine.context.TestUserMode;
import org.wso2.identity.integration.test.rest.api.server.policy.management.v1.model.PolicyRequest;
import org.wso2.identity.integration.test.rest.api.server.policy.management.v1.model.PolicyResourceRequest;
import org.wso2.identity.integration.test.rest.api.server.policy.management.v1.model.PolicyUpdateRequest;
import org.wso2.identity.integration.test.rest.api.server.policy.management.v1.model.RuleRequest;

import java.util.Collections;

import static org.hamcrest.CoreMatchers.equalTo;

/**
 * Negative-path tests for the Policy Management REST API.
 */
public class PolicyManagementFailureTest extends PolicyManagementTestBase {

    private static final String DUPLICATE_POLICY_NAME = "DuplicateNamePolicy";
    private static final String INVALID_RULE_POLICY_NAME = "InvalidRulePolicy";
    private static final String DUPLICATE_TARGET_POLICY_NAME = "DuplicateTargetPolicy";

    @Factory(dataProvider = "restAPIUserConfigProvider")
    public PolicyManagementFailureTest(TestUserMode userMode) throws Exception {

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

        super.conclude();
    }

    @BeforeMethod(alwaysRun = true)
    public void setBasePath() {

        RestAssured.basePath = basePath;
    }

    @AfterMethod(alwaysRun = true)
    public void clearBasePath() {

        RestAssured.basePath = StringUtils.EMPTY;
    }

    @Test
    public void testGetPolicyWithInvalidId() {

        getResponseOfGet(POLICY_MANAGEMENT_API_BASE_PATH + "/" + INVALID_POLICY_ID)
                .then()
                .log().ifValidationFails()
                .assertThat()
                .statusCode(HttpStatus.SC_NOT_FOUND)
                .body("code", equalTo("PM-60001"))
                .body("message", equalTo("Policy not found."))
                .body("description", equalTo("No policy found for the given policy id: " + INVALID_POLICY_ID + "."));
    }

    @Test
    public void testUpdatePolicyWithInvalidId() {

        PolicyUpdateRequest updateRequest = new PolicyUpdateRequest()
                .resources(buildIosPolicyRequest(DUPLICATE_POLICY_NAME).getResources());

        getResponseOfPut(POLICY_MANAGEMENT_API_BASE_PATH + "/" + INVALID_POLICY_ID, toJSONString(updateRequest))
                .then()
                .log().ifValidationFails()
                .assertThat()
                .statusCode(HttpStatus.SC_NOT_FOUND)
                .body("code", equalTo("PM-65003"))
                .body("message", equalTo("Unable to update policy."))
                .body("description", equalTo("Policy not found."));
    }

    @Test
    public void testDeletePolicyWithInvalidId() {

        // Delete is idempotent; deleting a non-existing policy is not an error.
        getResponseOfDelete(POLICY_MANAGEMENT_API_BASE_PATH + "/" + INVALID_POLICY_ID)
                .then()
                .log().ifValidationFails()
                .assertThat()
                .statusCode(HttpStatus.SC_NO_CONTENT);
    }

    @Test
    public void testCreatePolicyWithDuplicateName() {

        String policyId = createPolicy(DUPLICATE_POLICY_NAME);
        try {
            getResponseOfPost(POLICY_MANAGEMENT_API_BASE_PATH,
                    toJSONString(buildIosPolicyRequest(DUPLICATE_POLICY_NAME)))
                    .then()
                    .log().ifValidationFails()
                    .assertThat()
                    .statusCode(HttpStatus.SC_CONFLICT)
                    .body("code", equalTo("PM-65001"))
                    .body("message", equalTo("Unable to add policy."))
                    .body("description", equalTo("Policy already exists."));
        } finally {
            deletePolicy(policyId);
        }
    }

    @Test
    public void testCreatePolicyWithInvalidRuleField() {

        PolicyRequest request = new PolicyRequest()
                .name(INVALID_RULE_POLICY_NAME)
                .addResourceItem(ruleResource(TARGET_IOS,
                        andRule(expression("invalidField", OPERATOR_EQUALS, "value"))));

        getResponseOfPost(POLICY_MANAGEMENT_API_BASE_PATH, toJSONString(request))
                .then()
                .log().ifValidationFails()
                .assertThat()
                .statusCode(HttpStatus.SC_BAD_REQUEST)
                .body("code", equalTo("PM-60004"))
                .body("message", equalTo("Invalid policy rule."));
    }

    @Test
    public void testCreatePolicyWithInvalidRuleOperator() {

        PolicyRequest request = new PolicyRequest()
                .name(INVALID_RULE_POLICY_NAME)
                .addResourceItem(ruleResource(TARGET_IOS,
                        andRule(expression(FIELD_JAILBREAK, OPERATOR_GREATER_THAN, "false"))));

        getResponseOfPost(POLICY_MANAGEMENT_API_BASE_PATH, toJSONString(request))
                .then()
                .log().ifValidationFails()
                .assertThat()
                .statusCode(HttpStatus.SC_BAD_REQUEST)
                .body("code", equalTo("PM-60004"))
                .body("message", equalTo("Invalid policy rule."));
    }

    @Test
    public void testCreatePolicyWithDuplicateTarget() {

        PolicyRequest request = new PolicyRequest()
                .name(DUPLICATE_TARGET_POLICY_NAME)
                .addResourceItem(ruleResource(TARGET_IOS,
                        andRule(expression(FIELD_JAILBREAK, OPERATOR_EQUALS, "false"))))
                .addResourceItem(ruleResource(TARGET_IOS,
                        andRule(expression(FIELD_IOS_OS_VERSION, OPERATOR_GREATER_THAN, "16"))));

        getResponseOfPost(POLICY_MANAGEMENT_API_BASE_PATH, toJSONString(request))
                .then()
                .log().ifValidationFails()
                .assertThat()
                .statusCode(HttpStatus.SC_BAD_REQUEST)
                .body("code", equalTo("PM-65001"))
                .body("message", equalTo("Unable to add policy."))
                .body("description", equalTo("Duplicate target in policy."));
    }

    @Test
    public void testCreatePolicyWithoutName() {

        PolicyRequest request = buildIosPolicyRequest(null);

        getResponseOfPost(POLICY_MANAGEMENT_API_BASE_PATH, toJSONString(request))
                .then()
                .log().ifValidationFails()
                .assertThat()
                .statusCode(HttpStatus.SC_BAD_REQUEST);
    }

    @Test
    public void testCreatePolicyWithEmptyRules() {

        PolicyResourceRequest resource = new PolicyResourceRequest()
                .target(TARGET_IOS)
                .resourceType(RESOURCE_TYPE_RULE)
                .rule(new RuleRequest().condition(CONDITION_OR).rules(Collections.emptyList()));
        PolicyRequest request = new PolicyRequest()
                .name(INVALID_RULE_POLICY_NAME)
                .addResourceItem(resource);

        getResponseOfPost(POLICY_MANAGEMENT_API_BASE_PATH, toJSONString(request))
                .then()
                .log().ifValidationFails()
                .assertThat()
                .statusCode(HttpStatus.SC_BAD_REQUEST);
    }

    @Test
    public void testListPoliciesWithInvalidLimit() {

        getResponseOfGet(POLICY_MANAGEMENT_API_BASE_PATH + "?limit=0")
                .then()
                .log().ifValidationFails()
                .assertThat()
                .statusCode(HttpStatus.SC_BAD_REQUEST);
    }
}

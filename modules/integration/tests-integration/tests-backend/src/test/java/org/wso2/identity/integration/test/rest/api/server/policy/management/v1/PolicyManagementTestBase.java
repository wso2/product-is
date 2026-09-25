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

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import io.restassured.response.Response;
import org.apache.http.HttpStatus;
import org.testng.Assert;
import org.testng.annotations.DataProvider;
import org.wso2.carbon.automation.engine.context.TestUserMode;
import org.wso2.identity.integration.test.rest.api.server.common.RESTAPIServerTestBase;
import org.wso2.identity.integration.test.rest.api.server.policy.management.v1.model.ANDRuleRequest;
import org.wso2.identity.integration.test.rest.api.server.policy.management.v1.model.ExpressionRequest;
import org.wso2.identity.integration.test.rest.api.server.policy.management.v1.model.PolicyRequest;
import org.wso2.identity.integration.test.rest.api.server.policy.management.v1.model.PolicyResourceRequest;
import org.wso2.identity.integration.test.rest.api.server.policy.management.v1.model.RuleRequest;

import java.io.IOException;

/**
 * Base class for Policy Management REST API tests.
 */
public class PolicyManagementTestBase extends RESTAPIServerTestBase {

    private static final String API_DEFINITION_NAME = "policy.yaml";
    protected static final String API_VERSION = "v1";
    protected static final String POLICY_MANAGEMENT_API_BASE_PATH = "/policies";

    protected static final String INVALID_POLICY_ID = "00000000-0000-0000-0000-000000000000";

    protected static final String RESOURCE_TYPE_RULE = "RULE";
    protected static final String CONDITION_OR = "OR";
    protected static final String CONDITION_AND = "AND";
    protected static final String OPERATOR_EQUALS = "equals";
    protected static final String OPERATOR_GREATER_THAN = "greaterThan";

    protected static final String TARGET_IOS = "ios";
    protected static final String TARGET_ANDROID = "android";

    // Fields registered for the devicePolicy flow in the rule metadata.
    protected static final String FIELD_PLATFORM = "platform";
    protected static final String FIELD_IOS_OS_VERSION = "iosOsVersion";
    protected static final String FIELD_JAILBREAK = "jailbreak";
    protected static final String FIELD_ANDROID_OS_VERSION = "androidOsVersion";
    protected static final String FIELD_IS_ROOTED = "isRooted";

    protected static String swaggerDefinition;

    static {
        String API_PACKAGE_NAME = "org.wso2.carbon.identity.api.server.policy.v1";
        try {
            swaggerDefinition = getAPISwaggerDefinition(API_PACKAGE_NAME, API_DEFINITION_NAME);
        } catch (IOException e) {
            Assert.fail(String.format("Unable to read the swagger definition %s from %s", API_DEFINITION_NAME,
                    API_PACKAGE_NAME), e);
        }
    }

    @DataProvider(name = "restAPIUserConfigProvider")
    public static Object[][] restAPIUserConfigProvider() {

        return new Object[][]{
                {TestUserMode.SUPER_TENANT_ADMIN},
                {TestUserMode.TENANT_ADMIN}
        };
    }

    /**
     * Build an expression for a policy rule.
     *
     * @param field    Rule field.
     * @param operator Rule operator.
     * @param value    Value to compare against.
     * @return Expression request.
     */
    protected ExpressionRequest expression(String field, String operator, String value) {

        return new ExpressionRequest().field(field).operator(operator).value(value);
    }

    /**
     * Build a RULE resource for the given target, OR-combining the given AND sub-rules.
     *
     * @param target   Target the resource applies to.
     * @param andRules AND-combined sub-rules.
     * @return Policy resource request.
     */
    protected PolicyResourceRequest ruleResource(String target, ANDRuleRequest... andRules) {

        RuleRequest rule = new RuleRequest().condition(CONDITION_OR);
        for (ANDRuleRequest andRule : andRules) {
            rule.addRuleItem(andRule);
        }
        return new PolicyResourceRequest()
                .target(target)
                .resourceType(RESOURCE_TYPE_RULE)
                .rule(rule);
    }

    /**
     * Build an AND sub-rule from the given expressions.
     *
     * @param expressions Expressions to AND-combine.
     * @return AND sub-rule request.
     */
    protected ANDRuleRequest andRule(ExpressionRequest... expressions) {

        ANDRuleRequest andRule = new ANDRuleRequest().condition(CONDITION_AND);
        for (ExpressionRequest expression : expressions) {
            andRule.addExpressionItem(expression);
        }
        return andRule;
    }

    /**
     * Build a valid policy creation request with a single iOS RULE resource.
     *
     * @param policyName Name of the policy.
     * @return Policy creation request.
     */
    protected PolicyRequest buildIosPolicyRequest(String policyName) {

        return new PolicyRequest()
                .name(policyName)
                .addResourceItem(ruleResource(TARGET_IOS,
                        andRule(expression(FIELD_PLATFORM, OPERATOR_EQUALS, TARGET_IOS),
                                expression(FIELD_IOS_OS_VERSION, OPERATOR_GREATER_THAN, "16")),
                        andRule(expression(FIELD_JAILBREAK, OPERATOR_EQUALS, "false"))));
    }

    /**
     * Create a policy and return its ID.
     *
     * @param policyName Name of the policy.
     * @return ID of the created policy.
     */
    protected String createPolicy(String policyName) {

        Response response = getResponseOfPost(POLICY_MANAGEMENT_API_BASE_PATH,
                toJSONString(buildIosPolicyRequest(policyName)));
        response.then()
                .log().ifValidationFails()
                .assertThat()
                .statusCode(HttpStatus.SC_CREATED);
        return response.getBody().jsonPath().getString("id");
    }

    /**
     * Delete a policy.
     *
     * @param policyId ID of the policy.
     */
    protected void deletePolicy(String policyId) {

        getResponseOfDelete(POLICY_MANAGEMENT_API_BASE_PATH + "/" + policyId)
                .then()
                .log().ifValidationFails()
                .assertThat()
                .statusCode(HttpStatus.SC_NO_CONTENT);
    }

    /**
     * To convert object to a json string.
     *
     * @param object Respective java object.
     * @return Relevant json string.
     */
    protected String toJSONString(Object object) {

        Gson gson = new GsonBuilder().setPrettyPrinting().create();
        return gson.toJson(object);
    }
}

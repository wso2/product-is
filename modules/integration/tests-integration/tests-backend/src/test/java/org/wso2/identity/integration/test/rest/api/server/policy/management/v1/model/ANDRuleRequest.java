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

package org.wso2.identity.integration.test.rest.api.server.policy.management.v1.model;

import java.util.ArrayList;
import java.util.List;

/**
 * AND-combined sub-rule request model.
 */
public class ANDRuleRequest {

    private String condition;
    private List<ExpressionRequest> expressions;

    public ANDRuleRequest condition(String condition) {

        this.condition = condition;
        return this;
    }

    public String getCondition() {

        return condition;
    }

    public void setCondition(String condition) {

        this.condition = condition;
    }

    public ANDRuleRequest expressions(List<ExpressionRequest> expressions) {

        this.expressions = expressions;
        return this;
    }

    public ANDRuleRequest addExpressionItem(ExpressionRequest expressionItem) {

        if (this.expressions == null) {
            this.expressions = new ArrayList<>();
        }
        this.expressions.add(expressionItem);
        return this;
    }

    public List<ExpressionRequest> getExpressions() {

        return expressions;
    }

    public void setExpressions(List<ExpressionRequest> expressions) {

        this.expressions = expressions;
    }
}

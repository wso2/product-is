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

package org.wso2.identity.integration.test.serviceextensions.common.management.model;

import com.fasterxml.jackson.annotation.JsonProperty;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

import javax.validation.Valid;
import javax.validation.constraints.Size;
import javax.xml.bind.annotation.XmlEnum;
import javax.xml.bind.annotation.XmlEnumValue;
import javax.xml.bind.annotation.XmlType;
@ApiModel(description = "Rule configuration for the action. Combined with OR condition.")
public class ORRule  {
  

@XmlType(name="ConditionEnum")
@XmlEnum(String.class)
public enum ConditionEnum {

    @XmlEnumValue("OR") OR(String.valueOf("OR"));


    private String value;

    ConditionEnum(String v) {
        value = v;
    }

    public String value() {
        return value;
    }

    @Override
    public String toString() {
        return String.valueOf(value);
    }

    public static ConditionEnum fromValue(String value) {
        for (ConditionEnum b : ConditionEnum.values()) {
            if (b.value.equals(value)) {
                return b;
            }
        }
        throw new IllegalArgumentException("Unexpected value '" + value + "'");
    }
}

    private ConditionEnum condition;
    private List<ANDRule> rules = null;


    /**
    **/
    public ORRule condition(ConditionEnum condition) {

        this.condition = condition;
        return this;
    }
    
    @ApiModelProperty(value = "")
    @JsonProperty("condition")
    @Valid
    public ConditionEnum getCondition() {
        return condition;
    }
    public void setCondition(ConditionEnum condition) {
        this.condition = condition;
    }

    /**
    **/
    public ORRule rules(List<ANDRule> rules) {

        this.rules = rules;
        return this;
    }
    
    @ApiModelProperty(value = "")
    @JsonProperty("rules")
    @Valid @Size(min=1)
    public List<ANDRule> getRules() {
        return rules;
    }
    public void setRules(List<ANDRule> rules) {
        this.rules = rules;
    }

    public ORRule addRulesItem(ANDRule rulesItem) {
        if (this.rules == null) {
            this.rules = new ArrayList<ANDRule>();
        }
        this.rules.add(rulesItem);
        return this;
    }

    

    @Override
    public boolean equals(Object o) {

        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }
        ORRule orRule = (ORRule) o;
        return Objects.equals(this.condition, orRule.condition) &&
            Objects.equals(this.rules, orRule.rules);
    }

    @Override
    public int hashCode() {
        return Objects.hash(condition, rules);
    }

    @Override
    public String toString() {

        StringBuilder sb = new StringBuilder();
        sb.append("class ORRule {\n");
        
        sb.append("    condition: ").append(toIndentedString(condition)).append("\n");
        sb.append("    rules: ").append(toIndentedString(rules)).append("\n");
        sb.append("}");
        return sb.toString();
    }

    /**
    * Convert the given object to string with each line indented by 4 spaces
    * (except the first line).
    */
    private String toIndentedString(Object o) {

        if (o == null) {
            return "null";
        }
        return o.toString().replace("\n", "\n");
    }
}


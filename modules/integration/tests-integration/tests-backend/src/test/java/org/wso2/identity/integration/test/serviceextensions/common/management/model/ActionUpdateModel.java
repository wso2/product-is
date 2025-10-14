/*
 * Copyright (c) 2024, WSO2 LLC. (http://www.wso2.com).
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
import io.swagger.annotations.ApiModelProperty;

import java.util.Objects;

import javax.validation.Valid;
import javax.validation.constraints.Size;

public class ActionUpdateModel  {

    private String name;
    private String description;
    private EndpointUpdateModel endpoint;
    private ORRule rule;

    /**
     **/
    public ActionUpdateModel name(String name) {

        this.name = name;
        return this;
    }

    @ApiModelProperty(example = "Access Token Pre Issue", value = "")
    @JsonProperty("name")
    @Valid @Size(min=1,max=255)
    public String getName() {
        return name;
    }
    public void setName(String name) {
        this.name = name;
    }

    /**
     **/
    public ActionUpdateModel description(String description) {

        this.description = description;
        return this;
    }

    @ApiModelProperty(example = "This is the configuration of pre-action for issuing access token.", value = "")
    @JsonProperty("description")
    @Valid @Size(max=255)
    public String getDescription() {
        return description;
    }
    public void setDescription(String description) {
        this.description = description;
    }

    /**
     **/
    public ActionUpdateModel endpoint(EndpointUpdateModel endpoint) {

        this.endpoint = endpoint;
        return this;
    }

    @ApiModelProperty(value = "")
    @JsonProperty("endpoint")
    @Valid
    public EndpointUpdateModel getEndpoint() {
        return endpoint;
    }
    public void setEndpoint(EndpointUpdateModel endpoint) {
        this.endpoint = endpoint;
    }

    /**
     **/
    public ActionUpdateModel rule(ORRule rule) {

        this.rule = rule;
        return this;
    }

    @ApiModelProperty(value = "")
    @JsonProperty("rule")
    @Valid
    public ORRule getRule() {
        return rule;
    }
    public void setRule(ORRule rule) {
        this.rule = rule;
    }



    @Override
    public boolean equals(java.lang.Object o) {

        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }
        ActionUpdateModel actionUpdateModel = (ActionUpdateModel) o;
        return Objects.equals(this.name, actionUpdateModel.name) &&
                Objects.equals(this.description, actionUpdateModel.description) &&
                Objects.equals(this.endpoint, actionUpdateModel.endpoint) &&
                Objects.equals(this.rule, actionUpdateModel.rule);
    }

    @Override
    public int hashCode() {
        return Objects.hash(name, description, endpoint, rule);
    }

    @Override
    public String toString() {

        StringBuilder sb = new StringBuilder();
        sb.append("class ActionUpdateModel {\n");

        sb.append("    name: ").append(toIndentedString(name)).append("\n");
        sb.append("    description: ").append(toIndentedString(description)).append("\n");
        sb.append("    endpoint: ").append(toIndentedString(endpoint)).append("\n");
        sb.append("    rule: ").append(toIndentedString(rule)).append("\n");
        sb.append("}");
        return sb.toString();
    }

    /**
     * Convert the given object to string with each line indented by 4 spaces
     * (except the first line).
     */
    private String toIndentedString(java.lang.Object o) {

        if (o == null) {
            return "null";
        }
        return o.toString().replace("\n", "\n");
    }
}

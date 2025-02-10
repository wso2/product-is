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

package org.wso2.identity.integration.test.rest.api.server.user.sharing.management.v1.model;

import com.fasterxml.jackson.annotation.JsonProperty;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

import javax.validation.Valid;
import javax.validation.constraints.NotNull;
import javax.xml.bind.annotation.XmlEnum;
import javax.xml.bind.annotation.XmlEnumValue;
import javax.xml.bind.annotation.XmlType;

@ApiModel(description = "Process a request to share users with all organizations.  The payload contains the roles applicable across all organizations and a policy that defines the scope of sharing. ")
public class UserShareWithAllRequestBody {
  
    private UserShareRequestBodyUserCriteria userCriteria;

@XmlType(name="PolicyEnum")
@XmlEnum(String.class)
public enum PolicyEnum {

    @XmlEnumValue("ALL_EXISTING_ORGS_ONLY") ALL_EXISTING_ORGS_ONLY(String.valueOf("ALL_EXISTING_ORGS_ONLY")), @XmlEnumValue("ALL_EXISTING_AND_FUTURE_ORGS") ALL_EXISTING_AND_FUTURE_ORGS(String.valueOf("ALL_EXISTING_AND_FUTURE_ORGS")), @XmlEnumValue("IMMEDIATE_EXISTING_ORGS_ONLY") IMMEDIATE_EXISTING_ORGS_ONLY(String.valueOf("IMMEDIATE_EXISTING_ORGS_ONLY")), @XmlEnumValue("IMMEDIATE_EXISTING_AND_FUTURE_ORGS") IMMEDIATE_EXISTING_AND_FUTURE_ORGS(String.valueOf("IMMEDIATE_EXISTING_AND_FUTURE_ORGS"));


    private String value;

    PolicyEnum(String v) {
        value = v;
    }

    public String value() {
        return value;
    }

    @Override
    public String toString() {
        return String.valueOf(value);
    }

    public static PolicyEnum fromValue(String value) {
        for (PolicyEnum b : PolicyEnum.values()) {
            if (b.value.equals(value)) {
                return b;
            }
        }
        throw new IllegalArgumentException("Unexpected value '" + value + "'");
    }
}

    private PolicyEnum policy;
    private List<RoleWithAudience> roles = null;


    /**
    **/
    public UserShareWithAllRequestBody userCriteria(UserShareRequestBodyUserCriteria userCriteria) {

        this.userCriteria = userCriteria;
        return this;
    }
    
    @ApiModelProperty(required = true, value = "")
    @JsonProperty("userCriteria")
    @Valid
    @NotNull(message = "Property userCriteria cannot be null.")

    public UserShareRequestBodyUserCriteria getUserCriteria() {
        return userCriteria;
    }
    public void setUserCriteria(UserShareRequestBodyUserCriteria userCriteria) {
        this.userCriteria = userCriteria;
    }

    /**
    * A policy to specify the sharing scope.
    **/
    public UserShareWithAllRequestBody policy(PolicyEnum policy) {

        this.policy = policy;
        return this;
    }
    
    @ApiModelProperty(required = true, value = "A policy to specify the sharing scope.")
    @JsonProperty("policy")
    @Valid
    @NotNull(message = "Property policy cannot be null.")

    public PolicyEnum getPolicy() {
        return policy;
    }
    public void setPolicy(PolicyEnum policy) {
        this.policy = policy;
    }

    /**
    * A list of roles shared across all organizations.
    **/
    public UserShareWithAllRequestBody roles(List<RoleWithAudience> roles) {

        this.roles = roles;
        return this;
    }
    
    @ApiModelProperty(value = "A list of roles shared across all organizations.")
    @JsonProperty("roles")
    @Valid
    public List<RoleWithAudience> getRoles() {
        return roles;
    }
    public void setRoles(List<RoleWithAudience> roles) {
        this.roles = roles;
    }

    public UserShareWithAllRequestBody addRolesItem(RoleWithAudience rolesItem) {
        if (this.roles == null) {
            this.roles = new ArrayList<>();
        }
        this.roles.add(rolesItem);
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
        UserShareWithAllRequestBody userShareWithAllRequestBody = (UserShareWithAllRequestBody) o;
        return Objects.equals(this.userCriteria, userShareWithAllRequestBody.userCriteria) &&
            Objects.equals(this.policy, userShareWithAllRequestBody.policy) &&
            Objects.equals(this.roles, userShareWithAllRequestBody.roles);
    }

    @Override
    public int hashCode() {
        return Objects.hash(userCriteria, policy, roles);
    }

    @Override
    public String toString() {

        StringBuilder sb = new StringBuilder();
        sb.append("class UserShareWithAllRequestBody {\n");
        
        sb.append("    userCriteria: ").append(toIndentedString(userCriteria)).append("\n");
        sb.append("    policy: ").append(toIndentedString(policy)).append("\n");
        sb.append("    roles: ").append(toIndentedString(roles)).append("\n");
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


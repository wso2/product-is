/*
 * Copyright (c) 2023, WSO2 LLC. (http://www.wso2.com).
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

package org.wso2.identity.integration.test.rest.api.user.common.model;

import com.fasterxml.jackson.annotation.JsonProperty;
import io.swagger.annotations.ApiModelProperty;

import java.util.Objects;

import javax.validation.Valid;
import javax.validation.constraints.NotNull;

/**
 * InvitationRequest model.
 */
public class InvitationRequest {
  
    private String username;
    private String userstore;

    public InvitationRequest username(String username) {

        this.username = username;
        return this;
    }
    
    @ApiModelProperty(example = "JohnDoe123", required = true, value = "")
    @JsonProperty("username")
    @Valid
    @NotNull(message = "Property username cannot be null.")
    public String getUsername() {
        return username;
    }
    public void setUsername(String username) {
        this.username = username;
    }

    public InvitationRequest userstore(String userstore) {

        this.userstore = userstore;
        return this;
    }
    
    @ApiModelProperty(example = "PRIMARY", required = true, value = "")
    @JsonProperty("userstore")
    @Valid
    @NotNull(message = "Property userstore cannot be null.")
    public String getUserstore() {
        return userstore;
    }
    public void setUserstore(String userstore) {
        this.userstore = userstore;
    }

    @Override
    public boolean equals(Object o) {

        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }
        InvitationRequest invitationRequest = (InvitationRequest) o;
        return Objects.equals(this.username, invitationRequest.username) &&
            Objects.equals(this.userstore, invitationRequest.userstore);
    }

    @Override
    public int hashCode() {
        return Objects.hash(username, userstore);
    }

    @Override
    public String toString() {

        StringBuilder sb = new StringBuilder();
        sb.append("class InvitationRequest {\n");
        sb.append("    username: ").append(toIndentedString(username)).append("\n");
        sb.append("    userstore: ").append(toIndentedString(userstore)).append("\n");
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
        return o.toString().replace("\n", "    \n");
    }
}

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

package org.wso2.identity.integration.test.rest.api.user.password.v1.model;

import com.fasterxml.jackson.annotation.JsonProperty;
import io.swagger.annotations.ApiModelProperty;

import java.util.Objects;
import javax.validation.Valid;
import javax.validation.constraints.NotNull;

public class PasswordChangeRequest  {
  
    private String currentPassword;
    private String newPassword;

    /**
    * Current password of the user.
    **/
    public PasswordChangeRequest currentPassword(String currentPassword) {

        this.currentPassword = currentPassword;
        return this;
    }
    
    @ApiModelProperty(example = "CurrentPassword123", required = true, value = "Current password of the user.")
    @JsonProperty("currentPassword")
    @Valid
    @NotNull(message = "Property currentPassword cannot be null.")

    public String getCurrentPassword() {
        return currentPassword;
    }
    public void setCurrentPassword(String currentPassword) {
        this.currentPassword = currentPassword;
    }

    /**
    * New password to set for the user.
    **/
    public PasswordChangeRequest newPassword(String newPassword) {

        this.newPassword = newPassword;
        return this;
    }
    
    @ApiModelProperty(example = "NewPassword456", required = true, value = "New password to set for the user.")
    @JsonProperty("newPassword")
    @Valid
    @NotNull(message = "Property newPassword cannot be null.")

    public String getNewPassword() {
        return newPassword;
    }
    public void setNewPassword(String newPassword) {
        this.newPassword = newPassword;
    }

    @Override
    public boolean equals(java.lang.Object o) {

        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }
        PasswordChangeRequest passwordChangeRequest = (PasswordChangeRequest) o;
        return Objects.equals(this.currentPassword, passwordChangeRequest.currentPassword) &&
            Objects.equals(this.newPassword, passwordChangeRequest.newPassword);
    }

    @Override
    public int hashCode() {
        return Objects.hash(currentPassword, newPassword);
    }

    @Override
    public String toString() {

        StringBuilder sb = new StringBuilder();
        sb.append("class PasswordChangeRequest {\n");
        
        sb.append("    currentPassword: ").append(toIndentedString(currentPassword)).append("\n");
        sb.append("    newPassword: ").append(toIndentedString(newPassword)).append("\n");
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

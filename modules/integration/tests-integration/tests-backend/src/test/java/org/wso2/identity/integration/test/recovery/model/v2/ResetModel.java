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


package org.wso2.identity.integration.test.recovery.model.v2;

import com.fasterxml.jackson.annotation.JsonProperty;
import io.swagger.annotations.ApiModelProperty;

import javax.validation.constraints.NotNull;

import java.util.Objects;

public class ResetModel {

    private String resetCode;
    private String password;
    private String flowConfirmationCode;

    public ResetModel resetCode(String resetCode) {

        this.resetCode = resetCode;
        return this;
    }

    @ApiModelProperty(example = "yb3YRU", required = true)
    @JsonProperty("resetCode")
    @NotNull(message = "Reset code cannot be blank.")
    public String getResetCode() {

        return resetCode;
    }

    public void setResetCode(String resetCode) {

        this.resetCode = resetCode;
    }

    public ResetModel password(String password) {

        this.password = password;
        return this;
    }

    @ApiModelProperty(example = "Userpass@123", required = true)
    @JsonProperty("password")
    @NotNull(message = "Password cannot be blank.")
    public String getPassword() {

        return password;
    }

    public void setPassword(String password) {

        this.password = password;
    }

    public ResetModel flowConfirmationCode(String flowConfirmationCode) {

        this.flowConfirmationCode = flowConfirmationCode;
        return this;
    }

    @ApiModelProperty(example = "afb09659-ab01-4913-b2dc-0ad7fda8a194", required = true)
    @JsonProperty("flowConfirmationCode")
    @NotNull(message = "Flow confirmation code cannot be blank.")
    public String getFlowConfirmationCode() {

        return flowConfirmationCode;
    }

    public void setFlowConfirmationCode(String flowConfirmationCode) {

        this.flowConfirmationCode = flowConfirmationCode;
    }

    @Override
    public boolean equals(Object o) {

        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        ResetModel that = (ResetModel) o;
        return Objects.equals(resetCode, that.resetCode) &&
                Objects.equals(password, that.password) &&
                Objects.equals(flowConfirmationCode, that.flowConfirmationCode);
    }

    @Override
    public int hashCode() {

        return Objects.hash(resetCode, password, flowConfirmationCode);
    }

    @Override
    public String toString() {

        StringBuilder sb = new StringBuilder();
        sb.append("class ResetModel {\n");
        sb.append("    resetCode: ").append(toIndentedString(resetCode)).append("\n");
        sb.append("    password: ").append(toIndentedString(password)).append("\n");
        sb.append("    flowConfirmationCode: ").append(toIndentedString(flowConfirmationCode)).append("\n");
        sb.append("}");
        return sb.toString();
    }

    private String toIndentedString(Object o) {

        if (o == null) {
            return "null";
        }
        return o.toString().replace("\n", "\n    ");
    }
}

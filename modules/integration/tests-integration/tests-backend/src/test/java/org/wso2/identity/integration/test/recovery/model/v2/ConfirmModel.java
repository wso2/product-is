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

public class ConfirmModel {

    private String confirmationCode;
    private String otp;

    public ConfirmModel confirmationCode(String confirmationCode) {

        this.confirmationCode = confirmationCode;
        return this;
    }

    @ApiModelProperty(example = "afb09659-ab01-4913-b2dc-0ad7fda8a194", required = true)
    @JsonProperty("confirmationCode")
    @NotNull(message = "Confirmation code cannot be blank.")
    public String getConfirmationCode() {

        return confirmationCode;
    }

    public void setConfirmationCode(String confirmationCode) {

        this.confirmationCode = confirmationCode;
    }

    public ConfirmModel otp(String otp) {

        this.otp = otp;
        return this;
    }

    @ApiModelProperty(example = "yb3YRU", required = true)
    @JsonProperty("otp")
    @NotNull(message = "OTP cannot be blank.")
    public String getOtp() {

        return otp;
    }

    public void setOtp(String otp) {

        this.otp = otp;
    }

    @Override
    public boolean equals(Object o) {

        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        ConfirmModel that = (ConfirmModel) o;
        return Objects.equals(confirmationCode, that.confirmationCode) &&
                Objects.equals(otp, that.otp);
    }

    @Override
    public int hashCode() {

        return Objects.hash(confirmationCode, otp);
    }

    @Override
    public String toString() {

        StringBuilder sb = new StringBuilder();
        sb.append("class ConfirmModel {\n");
        sb.append("    confirmationCode: ").append(toIndentedString(confirmationCode)).append("\n");
        sb.append("    otp: ").append(toIndentedString(otp)).append("\n");
        sb.append("}");
        return sb.toString();
    }

    private String toIndentedString(Object o) {

        if (o == null) {
            return "null";
        }
        return o.toString().replace("\n", "\n");
    }
}

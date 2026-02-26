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

package org.wso2.identity.integration.test.rest.api.server.user.sharing.management.v2.model;

import com.fasterxml.jackson.annotation.JsonProperty;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;

import java.util.Objects;

import javax.validation.Valid;

/**
 * Indicates that a sharing/unsharing/patch process has been triggered.
 */
@ApiModel(description = "Indicates that a sharing/unsharing/patch process has been triggered.")
public class ProcessSuccessResponse {

    private String status;
    private String details;

    /**
     * Status of the process.
     **/
    public ProcessSuccessResponse status(String status) {
        this.status = status;
        return this;
    }

    @ApiModelProperty(value = "Status of the process.")
    @JsonProperty("status")
    @Valid
    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    /**
     * Additional information about the process.
     **/
    public ProcessSuccessResponse details(String details) {
        this.details = details;
        return this;
    }

    @ApiModelProperty(value = "Additional information about the process.")
    @JsonProperty("details")
    @Valid
    public String getDetails() {
        return details;
    }

    public void setDetails(String details) {
        this.details = details;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }
        ProcessSuccessResponse processSuccessResponse = (ProcessSuccessResponse) o;
        return Objects.equals(this.status, processSuccessResponse.status) &&
                Objects.equals(this.details, processSuccessResponse.details);
    }

    @Override
    public int hashCode() {
        return Objects.hash(status, details);
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        sb.append("class ProcessSuccessResponse {\n");
        sb.append("    status: ").append(toIndentedString(status)).append("\n");
        sb.append("    details: ").append(toIndentedString(details)).append("\n");
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

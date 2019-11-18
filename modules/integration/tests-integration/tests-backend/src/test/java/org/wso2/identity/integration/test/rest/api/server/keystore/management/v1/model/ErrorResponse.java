/*
* Copyright (c) 2019, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
*
* Licensed under the Apache License, Version 2.0 (the "License");
* you may not use this file except in compliance with the License.
* You may obtain a copy of the License at
*
* http://www.apache.org/licenses/LICENSE-2.0
*
* Unless required by applicable law or agreed to in writing, software
* distributed under the License is distributed on an "AS IS" BASIS,
* WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
* See the License for the specific language governing permissions and
* limitations under the License.
*/

package org.wso2.identity.integration.test.rest.api.server.keystore.management.v1.model;

import com.fasterxml.jackson.annotation.JsonProperty;
import io.swagger.annotations.ApiModelProperty;

import javax.validation.Valid;
import javax.validation.constraints.NotNull;
import java.util.Objects;

public class ErrorResponse  {
  
    private String code;
    private String message;
    private String description;
    private String traceId;

    /**
    **/
    public ErrorResponse code(String code) {

        this.code = code;
        return this;
    }
    
    @ApiModelProperty(example = "AAA-00000", required = true, value = "")
    @JsonProperty("code")
    @Valid
    @NotNull(message = "Property code cannot be null.")

    public String getCode() {
        return code;
    }
    public void setCode(String code) {
        this.code = code;
    }

    /**
    **/
    public ErrorResponse message(String message) {

        this.message = message;
        return this;
    }
    
    @ApiModelProperty(example = "Some Error Message", required = true, value = "")
    @JsonProperty("message")
    @Valid
    @NotNull(message = "Property message cannot be null.")

    public String getMessage() {
        return message;
    }
    public void setMessage(String message) {
        this.message = message;
    }

    /**
    **/
    public ErrorResponse description(String description) {

        this.description = description;
        return this;
    }
    
    @ApiModelProperty(example = "Some Error Description", value = "")
    @JsonProperty("description")
    @Valid
    public String getDescription() {
        return description;
    }
    public void setDescription(String description) {
        this.description = description;
    }

    /**
    **/
    public ErrorResponse traceId(String traceId) {

        this.traceId = traceId;
        return this;
    }
    
    @ApiModelProperty(example = "e0fbcfeb-3617-43c4-8dd0-7b7d38e13047", value = "")
    @JsonProperty("traceId")
    @Valid
    public String getTraceId() {
        return traceId;
    }
    public void setTraceId(String traceId) {
        this.traceId = traceId;
    }



    @Override
    public boolean equals(java.lang.Object o) {

        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }
        ErrorResponse errorResponse = (ErrorResponse) o;
        return Objects.equals(this.code, errorResponse.code) &&
            Objects.equals(this.message, errorResponse.message) &&
            Objects.equals(this.description, errorResponse.description) &&
            Objects.equals(this.traceId, errorResponse.traceId);
    }

    @Override
    public int hashCode() {
        return Objects.hash(code, message, description, traceId);
    }

    @Override
    public String toString() {

        StringBuilder sb = new StringBuilder();
        sb.append("class ErrorResponse {\n");

        sb.append("    code: ").append(toIndentedString(code)).append("\n");
        sb.append("    message: ").append(toIndentedString(message)).append("\n");
        sb.append("    description: ").append(toIndentedString(description)).append("\n");
        sb.append("    traceId: ").append(toIndentedString(traceId)).append("\n");
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


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

package org.wso2.identity.integration.test.rest.api.server.application.management.v1.model;

import com.fasterxml.jackson.annotation.JsonProperty;
import io.swagger.annotations.ApiModelProperty;

import java.util.Objects;
import javax.validation.Valid;

public class IdTokenEncryptionConfiguration {
  
    private Boolean enabled = false;
    private String algorithm;
    private String method;

    /**
    **/
    public IdTokenEncryptionConfiguration enabled(Boolean enabled) {

        this.enabled = enabled;
        return this;
    }
    
    @ApiModelProperty(example = "false", value = "")
    @JsonProperty("enabled")
    @Valid
    public Boolean getEnabled() {
        return enabled;
    }
    public void setEnabled(Boolean enabled) {
        this.enabled = enabled;
    }

    /**
    **/
    public IdTokenEncryptionConfiguration algorithm(String algorithm) {

        this.algorithm = algorithm;
        return this;
    }
    
    @ApiModelProperty(example = "RSA-OAEP", value = "")
    @JsonProperty("algorithm")
    @Valid
    public String getAlgorithm() {
        return algorithm;
    }
    public void setAlgorithm(String algorithm) {
        this.algorithm = algorithm;
    }

    /**
    **/
    public IdTokenEncryptionConfiguration method(String method) {

        this.method = method;
        return this;
    }
    
    @ApiModelProperty(example = "A128CBC+HS256", value = "")
    @JsonProperty("method")
    @Valid
    public String getMethod() {
        return method;
    }
    public void setMethod(String method) {
        this.method = method;
    }



    @Override
    public boolean equals(Object o) {

        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }
        IdTokenEncryptionConfiguration idTokenEncryptionConfiguration = (IdTokenEncryptionConfiguration) o;
        return Objects.equals(this.enabled, idTokenEncryptionConfiguration.enabled) &&
            Objects.equals(this.algorithm, idTokenEncryptionConfiguration.algorithm) &&
            Objects.equals(this.method, idTokenEncryptionConfiguration.method);
    }

    @Override
    public int hashCode() {
        return Objects.hash(enabled, algorithm, method);
    }

    @Override
    public String toString() {

        StringBuilder sb = new StringBuilder();
        sb.append("class IdTokenEncryptionConfiguration {\n");

        sb.append("    enabled: ").append(toIndentedString(enabled)).append("\n");
        sb.append("    algorithm: ").append(toIndentedString(algorithm)).append("\n");
        sb.append("    method: ").append(toIndentedString(method)).append("\n");
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


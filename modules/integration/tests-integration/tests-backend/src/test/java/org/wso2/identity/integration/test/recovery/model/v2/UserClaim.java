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

public class UserClaim {

    private String uri;
    private String value;

    public UserClaim uri(String uri) {

        this.uri = uri;
        return this;
    }

    @ApiModelProperty(example = "http://wso2.org/claims/username", required = true)
    @JsonProperty("uri")
    @NotNull(message = "Claim URI cannot be blank.")
    public String getUri() {

        return uri;
    }

    public void setUri(String uri) {

        this.uri = uri;
    }

    public UserClaim value(String value) {

        this.value = value;
        return this;
    }

    @ApiModelProperty(example = "user@wso2.com", required = true)
    @JsonProperty("value")
    @NotNull(message = "Claim value cannot be blank.")
    public String getValue() {

        return value;
    }

    public void setValue(String value) {

        this.value = value;
    }

    @Override
    public boolean equals(Object o) {

        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        UserClaim that = (UserClaim) o;
        return Objects.equals(uri, that.uri) && Objects.equals(value, that.value);
    }

    @Override
    public int hashCode() {

        return Objects.hash(uri, value);
    }

    @Override
    public String toString() {

        StringBuilder sb = new StringBuilder();
        sb.append("class UserClaim {\n");
        sb.append("    uri: ").append(toIndentedString(uri)).append("\n");
        sb.append("    value: ").append(toIndentedString(value)).append("\n");
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

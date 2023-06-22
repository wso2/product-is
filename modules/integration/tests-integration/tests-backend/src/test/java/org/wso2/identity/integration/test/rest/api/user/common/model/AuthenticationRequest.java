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

import javax.validation.Valid;
import java.util.Objects;

public class AuthenticationRequest {

    private String username;
    private String password;

    /**
     *
     **/
    public AuthenticationRequest username(String username) {

        this.username = username;
        return this;
    }

    @ApiModelProperty(example = "Ashan")
    @JsonProperty("username")
    @Valid
    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    /**
     *
     **/
    public AuthenticationRequest password(String password) {

        this.password = password;
        return this;
    }

    @ApiModelProperty(example = "Zoyza")
    @JsonProperty("password")
    @Valid
    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }


    @Override
    public boolean equals(Object o) {

        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }
        AuthenticationRequest AuthenticationRequest = (AuthenticationRequest) o;
        return Objects.equals(this.username, AuthenticationRequest.username) &&
                Objects.equals(this.password, AuthenticationRequest.password);
    }

    @Override
    public int hashCode() {
        return Objects.hash(username, password);
    }

    @Override
    public String toString() {

        return "class AuthenticationRequest {\n" +
                "    username: " + toIndentedString(username) + "\n" +
                "    password: " + toIndentedString(password) + "\n" +
                "}";
    }

    /**
     * Convert the given object to string with each line indented by 4 spaces
     * (except the first line).
     */
    private String toIndentedString(java.lang.Object o) {

        if (o == null) {
            return "null";
        }
        return o.toString();
    }
}

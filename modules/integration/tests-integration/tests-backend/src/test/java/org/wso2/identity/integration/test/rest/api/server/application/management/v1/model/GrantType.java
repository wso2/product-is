/*
 * Copyright (c) 2020-2025, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
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

import javax.validation.Valid;
import java.util.Objects;

public class GrantType {

    private String name;
    private String displayName;
    private Boolean publicClientAllowed;

    /**
     *
     **/
    public GrantType name(String name) {

        this.name = name;
        return this;
    }

    @ApiModelProperty(example = "authorization_code", value = "")
    @JsonProperty("name")
    @Valid
    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    /**
     *
     **/
    public GrantType displayName(String displayName) {

        this.displayName = displayName;
        return this;
    }

    @ApiModelProperty(example = "Code", value = "")
    @JsonProperty("displayName")
    @Valid
    public String getDisplayName() {
        return displayName;
    }

    public void setDisplayName(String displayName) {
        this.displayName = displayName;
    }

    public GrantType publicClientAllowed(Boolean publicClientAllowed) {

        this.publicClientAllowed = publicClientAllowed;
        return this;
    }

    @ApiModelProperty(example = "false", value = "")
    @JsonProperty("publicClientAllowed")
    @Valid
    public Boolean getPublicClientAllowed() {
        return publicClientAllowed;
    }

    public void setPublicClientAllowed(Boolean publicClientAllowed) {
        this.publicClientAllowed = publicClientAllowed;
    }


    @Override
    public boolean equals(Object o) {

        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }
        GrantType grantType = (GrantType) o;
        return Objects.equals(this.name, grantType.name) &&
            Objects.equals(this.displayName, grantType.displayName) &&
            Objects.equals(this.publicClientAllowed, grantType.publicClientAllowed);

    }

    @Override
    public int hashCode() {
        return Objects.hash(name, displayName, publicClientAllowed);
    }

    @Override
    public String toString() {

        StringBuilder sb = new StringBuilder();
        sb.append("class GrantType {\n");

        sb.append("    name: ").append(toIndentedString(name)).append("\n");
        sb.append("    displayName: ").append(toIndentedString(displayName)).append("\n");
        sb.append("    publicClientAllowed: ").append(toIndentedString(publicClientAllowed)).append("\n");
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


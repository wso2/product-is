/*
 * Copyright (c) 2020, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
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

package org.wso2.identity.integration.test.rest.api.server.tenant.management.v1.model;

import com.fasterxml.jackson.annotation.JsonProperty;
import io.swagger.annotations.ApiModelProperty;

import java.util.Objects;

import javax.validation.Valid;

public class OwnerResponse {
  
    private String id;
    private String username;

    /**
    * id of the tenant owner.
    **/
    public OwnerResponse id(String id) {

        this.id = id;
        return this;
    }
    
    @ApiModelProperty(example = "4875-jhgr-454hb", value = "id of the tenant owner.")
    @JsonProperty("id")
    @Valid
    public String getId() {
        return id;
    }
    public void setId(String id) {
        this.id = id;
    }

    /**
    * user name of the tenant owner.
    **/
    public OwnerResponse username(String username) {

        this.username = username;
        return this;
    }
    
    @ApiModelProperty(example = "kim", value = "user name of the tenant owner.")
    @JsonProperty("username")
    @Valid
    public String getUsername() {
        return username;
    }
    public void setUsername(String username) {
        this.username = username;
    }



    @Override
    public boolean equals(Object o) {

        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }
        OwnerResponse ownerResponse = (OwnerResponse) o;
        return Objects.equals(this.id, ownerResponse.id) &&
            Objects.equals(this.username, ownerResponse.username);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id, username);
    }

    @Override
    public String toString() {

        StringBuilder sb = new StringBuilder();
        sb.append("class OwnerResponse {\n");

        sb.append("    id: ").append(toIndentedString(id)).append("\n");
        sb.append("    username: ").append(toIndentedString(username)).append("\n");
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


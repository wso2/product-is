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

package org.wso2.identity.integration.test.rest.api.server.application.management.v1.model;

import com.fasterxml.jackson.annotation.JsonProperty;
import io.swagger.annotations.ApiModelProperty;

import javax.validation.Valid;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

public class ClientSecretList {

    private Integer count;
    private List<ClientSecretResponse> list = null;

    /**
    * Number of client secrets returned.
    **/
    public ClientSecretList count(Integer count) {

        this.count = count;
        return this;
    }

    @ApiModelProperty(example = "2", value = "Number of client secrets returned.")
    @JsonProperty("count")
    @Valid
    public Integer getCount() {
        return count;
    }
    public void setCount(Integer count) {
        this.count = count;
    }

    /**
    * List of client secrets attached to the application.
    **/
    public ClientSecretList list(List<ClientSecretResponse> list) {

        this.list = list;
        return this;
    }

    @ApiModelProperty(value = "List of client secrets attached to the application.")
    @JsonProperty("list")
    @Valid
    public List<ClientSecretResponse> getList() {
        return list;
    }
    public void setList(List<ClientSecretResponse> list) {
        this.list = list;
    }

    public ClientSecretList addListItem(ClientSecretResponse listItem) {

        if (this.list == null) {
            this.list = new ArrayList<>();
        }
        this.list.add(listItem);
        return this;
    }

    @Override
    public boolean equals(Object o) {

        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }
        ClientSecretList clientSecretList = (ClientSecretList) o;
        return Objects.equals(this.count, clientSecretList.count) &&
            Objects.equals(this.list, clientSecretList.list);
    }

    @Override
    public int hashCode() {
        return Objects.hash(count, list);
    }

    @Override
    public String toString() {

        StringBuilder sb = new StringBuilder();
        sb.append("class ClientSecretList {\n");

        sb.append("    count: ").append(toIndentedString(count)).append("\n");
        sb.append("    list: ").append(toIndentedString(list)).append("\n");
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

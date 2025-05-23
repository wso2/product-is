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

package org.wso2.identity.integration.test.rest.api.server.registration.management.v1.model;

import com.fasterxml.jackson.annotation.JsonProperty;
import io.swagger.annotations.ApiModelProperty;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

import javax.validation.Valid;

public class Data {

    private List<Component> components = null;

    private Action action;

    /**
     *
     **/
    public Data components(List<Component> components) {

        this.components = components;
        return this;
    }

    @ApiModelProperty(value = "")
    @JsonProperty("components")
    @Valid
    public List<Component> getComponents() {
        return components;
    }

    public void setComponents(List<Component> components) {
        this.components = components;
    }

    public Data addComponentsItem(Component componentsItem) {
        if (this.components == null) {
            this.components = new ArrayList<Component>();
        }
        this.components.add(componentsItem);
        return this;
    }

    /**
     *
     **/
    public Data action(Action action) {

        this.action = action;
        return this;
    }

    @ApiModelProperty(value = "")
    @JsonProperty("action")
    @Valid
    public Action getAction() {
        return action;
    }

    public void setAction(Action action) {
        this.action = action;
    }


    @Override
    public boolean equals(Object o) {

        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }
        Data data = (Data) o;
        return Objects.equals(this.components, data.components) &&
                Objects.equals(this.action, data.action);
    }

    @Override
    public int hashCode() {
        return Objects.hash(components, action);
    }

    @Override
    public String toString() {

        StringBuilder sb = new StringBuilder();
        sb.append("class Data {\n");

        sb.append("    components: ").append(toIndentedString(components)).append("\n");
        sb.append("    action: ").append(toIndentedString(action)).append("\n");
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


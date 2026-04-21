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

package org.wso2.identity.integration.test.rest.api.server.flow.execution.v1.model;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonCreator;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import java.util.ArrayList;
import java.util.List;
import javax.validation.constraints.*;

/**
 * Represent individual UI components in the flow execution
 **/

import io.swagger.annotations.*;
import java.util.Objects;
import javax.validation.Valid;
import javax.xml.bind.annotation.*;
@ApiModel(description = "Represent individual UI components in the flow execution")
public class Component  {

    private String id;
    private String actionId;
    private String type;
    private String variant;
    private List<Component> components = null;

    private Object config;

    /**
     * Unique identifier of the component
     **/
    public Component id(String id) {

        this.id = id;
        return this;
    }

    @ApiModelProperty(example = "button_40f64", required = true, value = "Unique identifier of the component")
    @JsonProperty("id")
    @Valid
    @NotNull(message = "Property id cannot be null.")

    public String getId() {
        return id;
    }
    public void setId(String id) {
        this.id = id;
    }

    /**
     * Action identifier
     **/
    public Component actionId(String actionId) {

        this.actionId = actionId;
        return this;
    }

    @ApiModelProperty(example = "button_40f64", value = "Action identifier")
    @JsonProperty("actionId")
    @Valid
    public String getActionId() {
        return actionId;
    }
    public void setActionId(String actionId) {
        this.actionId = actionId;
    }

    /**
     * Specific component type (e.g., INPUT, BUTTON)
     **/
    public Component type(String type) {

        this.type = type;
        return this;
    }

    @ApiModelProperty(example = "INPUT", required = true, value = "Specific component type (e.g., INPUT, BUTTON)")
    @JsonProperty("type")
    @Valid
    @NotNull(message = "Property type cannot be null.")

    public String getType() {
        return type;
    }
    public void setType(String type) {
        this.type = type;
    }

    /**
     * Specific component variant
     **/
    public Component variant(String variant) {

        this.variant = variant;
        return this;
    }

    @ApiModelProperty(example = "HEADING1", value = "Specific component variant")
    @JsonProperty("variant")
    @Valid
    public String getVariant() {
        return variant;
    }
    public void setVariant(String variant) {
        this.variant = variant;
    }

    /**
     **/
    public Component components(List<Component> components) {

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

    public Component addComponentsItem(Component componentsItem) {
        if (this.components == null) {
            this.components = new ArrayList<Component>();
        }
        this.components.add(componentsItem);
        return this;
    }

    /**
     * Configuration details of the component
     **/
    public Component config(Object config) {

        this.config = config;
        return this;
    }

    @ApiModelProperty(value = "Configuration details of the component")
    @JsonProperty("config")
    @Valid
    public Object getConfig() {
        return config;
    }
    public void setConfig(Object config) {
        this.config = config;
    }



    @Override
    public boolean equals(java.lang.Object o) {

        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }
        Component component = (Component) o;
        return Objects.equals(this.id, component.id) &&
                Objects.equals(this.actionId, component.actionId) &&
                Objects.equals(this.type, component.type) &&
                Objects.equals(this.variant, component.variant) &&
                Objects.equals(this.components, component.components) &&
                Objects.equals(this.config, component.config);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id, actionId, type, variant, components, config);
    }

    @Override
    public String toString() {

        StringBuilder sb = new StringBuilder();
        sb.append("class Component {\n");

        sb.append("    id: ").append(toIndentedString(id)).append("\n");
        sb.append("    actionId: ").append(toIndentedString(actionId)).append("\n");
        sb.append("    type: ").append(toIndentedString(type)).append("\n");
        sb.append("    variant: ").append(toIndentedString(variant)).append("\n");
        sb.append("    components: ").append(toIndentedString(components)).append("\n");
        sb.append("    config: ").append(toIndentedString(config)).append("\n");
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

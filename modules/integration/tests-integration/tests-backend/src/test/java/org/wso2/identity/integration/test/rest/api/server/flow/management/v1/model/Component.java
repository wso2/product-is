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

package org.wso2.identity.integration.test.rest.api.server.flow.management.v1.model;

import com.fasterxml.jackson.annotation.JsonProperty;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

import javax.validation.Valid;
import javax.validation.constraints.NotNull;

@ApiModel(description = "Represent individual UI components in the flow")
public class Component {

    private String id;
    private String category;
    private String type;
    private String variant;
    private String version;
    private Boolean deprecated;
    private List<Component> components = null;

    private Action action;
    private Object config;

    /**
     * Unique identifier of the component
     **/
    public Component id(String id) {

        this.id = id;
        return this;
    }

    @ApiModelProperty(example = "dnd-component-210e95c0-c580-40b0-9646-7054bb340f64", required = true, value = "Unique identifier of the component")
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
     * Type of component
     **/
    public Component category(String category) {

        this.category = category;
        return this;
    }

    @ApiModelProperty(example = "FIELD", required = true, value = "Type of component")
    @JsonProperty("category")
    @Valid
    @NotNull(message = "Property category cannot be null.")

    public String getCategory() {
        return category;
    }

    public void setCategory(String category) {
        this.category = category;
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
     * Variant of the component (e.g., PRIMARY, TEXT)
     **/
    public Component variant(String variant) {

        this.variant = variant;
        return this;
    }

    @ApiModelProperty(example = "PASSWORD", value = "Variant of the component (e.g., PRIMARY, TEXT)")
    @JsonProperty("variant")
    @Valid
    public String getVariant() {
        return variant;
    }

    public void setVariant(String variant) {
        this.variant = variant;
    }

    /**
     * Version of the component
     **/
    public Component version(String version) {

        this.version = version;
        return this;
    }

    @ApiModelProperty(example = "1.0.0", value = "Version of the component")
    @JsonProperty("version")
    @Valid
    public String getVersion() {
        return version;
    }

    public void setVersion(String version) {
        this.version = version;
    }

    /**
     * Indicate whether the component is deprecated
     **/
    public Component deprecated(Boolean deprecated) {

        this.deprecated = deprecated;
        return this;
    }

    @ApiModelProperty(example = "false", value = "Indicate whether the component is deprecated")
    @JsonProperty("deprecated")
    @Valid
    public Boolean getDeprecated() {
        return deprecated;
    }

    public void setDeprecated(Boolean deprecated) {
        this.deprecated = deprecated;
    }

    /**
     *
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
     *
     **/
    public Component action(Action action) {

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
    public boolean equals(Object o) {

        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }
        Component component = (Component) o;
        return Objects.equals(this.id, component.id) &&
                Objects.equals(this.category, component.category) &&
                Objects.equals(this.type, component.type) &&
                Objects.equals(this.variant, component.variant) &&
                Objects.equals(this.version, component.version) &&
                Objects.equals(this.deprecated, component.deprecated) &&
                Objects.equals(this.components, component.components) &&
                Objects.equals(this.action, component.action) &&
                Objects.equals(this.config, component.config);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id, category, type, variant, version, deprecated, components, action, config);
    }

    @Override
    public String toString() {

        StringBuilder sb = new StringBuilder();
        sb.append("class Component {\n");

        sb.append("    id: ").append(toIndentedString(id)).append("\n");
        sb.append("    category: ").append(toIndentedString(category)).append("\n");
        sb.append("    type: ").append(toIndentedString(type)).append("\n");
        sb.append("    variant: ").append(toIndentedString(variant)).append("\n");
        sb.append("    version: ").append(toIndentedString(version)).append("\n");
        sb.append("    deprecated: ").append(toIndentedString(deprecated)).append("\n");
        sb.append("    components: ").append(toIndentedString(components)).append("\n");
        sb.append("    action: ").append(toIndentedString(action)).append("\n");
        sb.append("    config: ").append(toIndentedString(config)).append("\n");
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


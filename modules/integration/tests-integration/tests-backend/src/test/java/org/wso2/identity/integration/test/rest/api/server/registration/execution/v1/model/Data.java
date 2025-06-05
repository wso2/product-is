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

package org.wso2.identity.integration.test.rest.api.server.registration.execution.v1.model;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonCreator;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import javax.validation.constraints.*;


import io.swagger.annotations.*;
import java.util.Objects;
import javax.validation.Valid;
import javax.xml.bind.annotation.*;

public class Data  {

    private List<Component> components = null;

    private String redirectURL;
    private List<String> requiredParams = null;

    private Map<String, Object> additionalData = null;


    /**
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
     **/
    public Data redirectURL(String redirectURL) {

        this.redirectURL = redirectURL;
        return this;
    }

    @ApiModelProperty(value = "")
    @JsonProperty("redirectURL")
    @Valid
    public String getRedirectURL() {
        return redirectURL;
    }
    public void setRedirectURL(String redirectURL) {
        this.redirectURL = redirectURL;
    }

    /**
     **/
    public Data requiredParams(List<String> requiredParams) {

        this.requiredParams = requiredParams;
        return this;
    }

    @ApiModelProperty(value = "")
    @JsonProperty("requiredParams")
    @Valid
    public List<String> getRequiredParams() {
        return requiredParams;
    }
    public void setRequiredParams(List<String> requiredParams) {
        this.requiredParams = requiredParams;
    }

    public Data addRequiredParamsItem(String requiredParamsItem) {
        if (this.requiredParams == null) {
            this.requiredParams = new ArrayList<String>();
        }
        this.requiredParams.add(requiredParamsItem);
        return this;
    }

    /**
     **/
    public Data additionalData(Map<String, Object> additionalData) {

        this.additionalData = additionalData;
        return this;
    }

    @ApiModelProperty(value = "")
    @JsonProperty("additionalData")
    @Valid
    public Map<String, Object> getAdditionalData() {
        return additionalData;
    }
    public void setAdditionalData(Map<String, Object> additionalData) {
        this.additionalData = additionalData;
    }


    public Data putAdditionalDataItem(String key, Object additionalDataItem) {
        if (this.additionalData == null) {
            this.additionalData = new HashMap<String, Object>();
        }
        this.additionalData.put(key, additionalDataItem);
        return this;
    }



    @Override
    public boolean equals(java.lang.Object o) {

        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }
        Data data = (Data) o;
        return Objects.equals(this.components, data.components) &&
                Objects.equals(this.redirectURL, data.redirectURL) &&
                Objects.equals(this.requiredParams, data.requiredParams) &&
                Objects.equals(this.additionalData, data.additionalData);
    }

    @Override
    public int hashCode() {
        return Objects.hash(components, redirectURL, requiredParams, additionalData);
    }

    @Override
    public String toString() {

        StringBuilder sb = new StringBuilder();
        sb.append("class Data {\n");

        sb.append("    components: ").append(toIndentedString(components)).append("\n");
        sb.append("    redirectURL: ").append(toIndentedString(redirectURL)).append("\n");
        sb.append("    requiredParams: ").append(toIndentedString(requiredParams)).append("\n");
        sb.append("    additionalData: ").append(toIndentedString(additionalData)).append("\n");
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

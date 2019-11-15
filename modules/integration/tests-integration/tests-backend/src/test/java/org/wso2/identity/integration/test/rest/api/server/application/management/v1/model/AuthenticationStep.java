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

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import javax.validation.Valid;
import javax.validation.constraints.Min;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.Size;

public class AuthenticationStep  {
  
    private Integer id;
    private List<Authenticator> options = new ArrayList<>();


    /**
    * minimum: 1
    **/
    public AuthenticationStep id(Integer id) {

        this.id = id;
        return this;
    }
    
    @ApiModelProperty(example = "1", required = true, value = "")
    @JsonProperty("id")
    @Valid
    @NotNull(message = "Property id cannot be null.")
 @Min(1)
    public Integer getId() {
        return id;
    }
    public void setId(Integer id) {
        this.id = id;
    }

    /**
    **/
    public AuthenticationStep options(List<Authenticator> options) {

        this.options = options;
        return this;
    }
    
    @ApiModelProperty(required = true, value = "")
    @JsonProperty("options")
    @Valid
    @NotNull(message = "Property options cannot be null.")
 @Size(min=1)
    public List<Authenticator> getOptions() {
        return options;
    }
    public void setOptions(List<Authenticator> options) {
        this.options = options;
    }

    public AuthenticationStep addOptionsItem(Authenticator optionsItem) {
        this.options.add(optionsItem);
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
        AuthenticationStep authenticationStep = (AuthenticationStep) o;
        return Objects.equals(this.id, authenticationStep.id) &&
            Objects.equals(this.options, authenticationStep.options);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id, options);
    }

    @Override
    public String toString() {

        StringBuilder sb = new StringBuilder();
        sb.append("class AuthenticationStep {\n");

        sb.append("    id: ").append(toIndentedString(id)).append("\n");
        sb.append("    options: ").append(toIndentedString(options)).append("\n");
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


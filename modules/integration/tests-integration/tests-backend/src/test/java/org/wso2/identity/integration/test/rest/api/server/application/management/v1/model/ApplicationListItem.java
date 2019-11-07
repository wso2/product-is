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

import java.util.Objects;
import javax.validation.Valid;

public class ApplicationListItem  {
  
    private String id;
    private String name;
    private String description;
    private String image;
    private String loginUrl;
    private String self;

    /**
    **/
    public ApplicationListItem id(String id) {

        this.id = id;
        return this;
    }
    
    @ApiModelProperty(example = "85e3f4b8-0d22-4181-b1e3-1651f71b88bd", value = "")
    @JsonProperty("id")
    @Valid
    public String getId() {
        return id;
    }
    public void setId(String id) {
        this.id = id;
    }

    /**
    **/
    public ApplicationListItem name(String name) {

        this.name = name;
        return this;
    }
    
    @ApiModelProperty(example = "user-portal", value = "")
    @JsonProperty("name")
    @Valid
    public String getName() {
        return name;
    }
    public void setName(String name) {
        this.name = name;
    }

    /**
    **/
    public ApplicationListItem description(String description) {

        this.description = description;
        return this;
    }
    
    @ApiModelProperty(example = "Application representing user portal", value = "")
    @JsonProperty("description")
    @Valid
    public String getDescription() {
        return description;
    }
    public void setDescription(String description) {
        this.description = description;
    }

    /**
    **/
    public ApplicationListItem image(String image) {

        this.image = image;
        return this;
    }
    
    @ApiModelProperty(example = "https://example.com/logo/my-logo.png", value = "")
    @JsonProperty("image")
    @Valid
    public String getImage() {
        return image;
    }
    public void setImage(String image) {
        this.image = image;
    }

    /**
    **/
    public ApplicationListItem loginUrl(String loginUrl) {

        this.loginUrl = loginUrl;
        return this;
    }
    
    @ApiModelProperty(example = "https://example.com/app/login", value = "")
    @JsonProperty("loginUrl")
    @Valid
    public String getLoginUrl() {
        return loginUrl;
    }
    public void setLoginUrl(String loginUrl) {
        this.loginUrl = loginUrl;
    }

    /**
    **/
    public ApplicationListItem self(String self) {

        this.self = self;
        return this;
    }
    
    @ApiModelProperty(example = "/t/wso2.com/api/server/v1/applications/85e3f4b8-0d22-4181-b1e3-1651f71b88bd", value = "")
    @JsonProperty("self")
    @Valid
    public String getSelf() {
        return self;
    }
    public void setSelf(String self) {
        this.self = self;
    }



    @Override
    public boolean equals(Object o) {

        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }
        ApplicationListItem applicationListItem = (ApplicationListItem) o;
        return Objects.equals(this.id, applicationListItem.id) &&
            Objects.equals(this.name, applicationListItem.name) &&
            Objects.equals(this.description, applicationListItem.description) &&
            Objects.equals(this.image, applicationListItem.image) &&
            Objects.equals(this.loginUrl, applicationListItem.loginUrl) &&
            Objects.equals(this.self, applicationListItem.self);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id, name, description, image, loginUrl, self);
    }

    @Override
    public String toString() {

        StringBuilder sb = new StringBuilder();
        sb.append("class ApplicationListItem {\n");

        sb.append("    id: ").append(toIndentedString(id)).append("\n");
        sb.append("    name: ").append(toIndentedString(name)).append("\n");
        sb.append("    description: ").append(toIndentedString(description)).append("\n");
        sb.append("    image: ").append(toIndentedString(image)).append("\n");
        sb.append("    loginUrl: ").append(toIndentedString(loginUrl)).append("\n");
        sb.append("    self: ").append(toIndentedString(self)).append("\n");
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


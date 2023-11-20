/*
 * Copyright (c) 2019-2023, WSO2 LLC. (http://www.wso2.com).
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

import java.util.Objects;
import javax.validation.Valid;
import javax.xml.bind.annotation.*;

public class ApplicationListItem  {

    private String id;
    private String name;
    private String description;
    private String image;
    private String accessUrl;
    private String clientId;

    @XmlType(name="AccessEnum")
    @XmlEnum(String.class)
    public enum AccessEnum {

        @XmlEnumValue("READ") READ(String.valueOf("READ")), @XmlEnumValue("WRITE") WRITE(String.valueOf("WRITE"));


        private String value;

        AccessEnum(String v) {
            value = v;
        }

        public String value() {
            return value;
        }

        @Override
        public String toString() {
            return String.valueOf(value);
        }

        public static AccessEnum fromValue(String value) {
            for (AccessEnum b : AccessEnum.values()) {
                if (b.value.equals(value)) {
                    return b;
                }
            }
            throw new IllegalArgumentException("Unexpected value '" + value + "'");
        }
    }

    private AccessEnum access = AccessEnum.READ;
    private String self;
    private AssociatedRolesConfig associatedRoles;

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
    public ApplicationListItem accessUrl(String accessUrl) {

        this.accessUrl = accessUrl;
        return this;
    }

    @ApiModelProperty(example = "https://example.com/app/login", value = "")
    @JsonProperty("accessUrl")
    @Valid
    public String getAccessUrl() {
        return accessUrl;
    }
    public void setAccessUrl(String accessUrl) {
        this.accessUrl = accessUrl;
    }

    /**
     **/
    public ApplicationListItem access(AccessEnum access) {

        this.access = access;
        return this;
    }

    @ApiModelProperty(value = "")
    @JsonProperty("access")
    @Valid
    public AccessEnum getAccess() {
        return access;
    }
    public void setAccess(AccessEnum access) {
        this.access = access;
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

    public ApplicationListItem clientId(String clientId) {

        this.clientId = clientId;
        return this;
    }

    @ApiModelProperty(example = "clientId", value = "")
    @JsonProperty("clientId")
    @Valid
    public String getClientId() {

        return clientId;
    }

    public void setClientId(String clientId) {

        this.clientId = clientId;
    }

    /**
     *
     **/
    public ApplicationListItem associatedRoles(AssociatedRolesConfig associatedRoles) {

        this.associatedRoles = associatedRoles;
        return this;
    }

    @ApiModelProperty(value = "")
    @JsonProperty("associatedRoles")
    @Valid
    public AssociatedRolesConfig getAssociatedRoles() {

        return associatedRoles;
    }

    public void setAssociatedRoles(AssociatedRolesConfig associatedRoles) {

        this.associatedRoles = associatedRoles;
    }

    @Override
    public boolean equals(java.lang.Object o) {

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
                Objects.equals(this.accessUrl, applicationListItem.accessUrl) &&
                Objects.equals(this.access, applicationListItem.access) &&
                Objects.equals(this.self, applicationListItem.self) &&
                Objects.equals(this.associatedRoles, applicationListItem.associatedRoles);
    }

    @Override
    public int hashCode() {

        return Objects.hash(id, name, description, image, accessUrl, access, self, associatedRoles);
    }

    @Override
    public String toString() {

        StringBuilder sb = new StringBuilder();
        sb.append("class ApplicationListItem {\n");

        sb.append("    id: ").append(toIndentedString(id)).append("\n");
        sb.append("    name: ").append(toIndentedString(name)).append("\n");
        sb.append("    description: ").append(toIndentedString(description)).append("\n");
        sb.append("    image: ").append(toIndentedString(image)).append("\n");
        sb.append("    accessUrl: ").append(toIndentedString(accessUrl)).append("\n");
        sb.append("    access: ").append(toIndentedString(access)).append("\n");
        sb.append("    self: ").append(toIndentedString(self)).append("\n");
        sb.append("    associatedRoles: ").append(toIndentedString(associatedRoles)).append("\n");
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

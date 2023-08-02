/*
 * Copyright (c) 2023, WSO2 LLC. (http://www.wso2.com).
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

public class InboundProtocolListItem {
   
    private String type;
    private String name;
    private String self;

    /**
    **/
    public InboundProtocolListItem type(String type) {

        this.type = type;
        return this;
    }
    
    @ApiModelProperty(example = "samlsso")
    @JsonProperty("type")
    @Valid
    public String getType() {
        return type;
    }
    public void setType(String type) {
        this.type = type;
    }   
    
    /**
    **/
    public InboundProtocolListItem name(String name) {

        this.name = name;
        return this;
    }
    
    @ApiModelProperty(example = "SAML2 Inbound")
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
    public InboundProtocolListItem self(String self) {

        this.self = self;
        return this;
    }
    
    @ApiModelProperty(example = "/t/carbon.super/api/server/v1/applications/29048810-1447-4ea0-a348-30d15ab65fa3/inbound-protocols/saml")
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
        InboundProtocolListItem inboundProtocolItem = (InboundProtocolListItem) o;
        return Objects.equals(this.type, inboundProtocolItem.type) &&
            Objects.equals(this.name, inboundProtocolItem.name) &&
            Objects.equals(this.self, inboundProtocolItem.self);
    }

    @Override
    public int hashCode() {
        return Objects.hash(type, name, self);
    }

    @Override
    public String toString() {

        return "class InboundProtocolListItem {\n" +
                    "    type: " + toIndentedString(type) + "\n" +
                    "    name: " + toIndentedString(name) + "\n" +
                    "    self: " + toIndentedString(self) + "\n" +
                    "}";
    }

    /**
     * Convert the given object to string with each line indented by 4 spaces
     * (except the first line).
     */
    private String toIndentedString(java.lang.Object o) {

        if (o == null) {
            return "null";
        }
        return o.toString();
    }
}

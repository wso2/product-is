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

package org.wso2.identity.integration.test.rest.api.server.idp.v1.model;

import com.fasterxml.jackson.annotation.JsonProperty;
import io.swagger.annotations.ApiModelProperty;

import java.util.Objects;
import javax.validation.Valid;

public class MetaOutboundConnectorListItem {
  
    private String connectorId;
    private String name;
    private String self;

    /**
    **/
    public MetaOutboundConnectorListItem connectorId(String connectorId) {

        this.connectorId = connectorId;
        return this;
    }
    
    @ApiModelProperty(example = "U0NJTQ", value = "")
    @JsonProperty("connectorId")
    @Valid
    public String getConnectorId() {
        return connectorId;
    }
    public void setConnectorId(String connectorId) {
        this.connectorId = connectorId;
    }

    /**
    **/
    public MetaOutboundConnectorListItem name(String name) {

        this.name = name;
        return this;
    }
    
    @ApiModelProperty(example = "SCIM", value = "")
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
    public MetaOutboundConnectorListItem self(String self) {

        this.self = self;
        return this;
    }
    
    @ApiModelProperty(example = "/t/carbon.super/api/server/v1/identity-providers/meta/outbound-provisioning-connectos/U0NJTQ", value = "")
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
        MetaOutboundConnectorListItem metaOutboundConnectorListItem = (MetaOutboundConnectorListItem) o;
        return Objects.equals(this.connectorId, metaOutboundConnectorListItem.connectorId) &&
            Objects.equals(this.name, metaOutboundConnectorListItem.name) &&
            Objects.equals(this.self, metaOutboundConnectorListItem.self);
    }

    @Override
    public int hashCode() {
        return Objects.hash(connectorId, name, self);
    }

    @Override
    public String toString() {

        StringBuilder sb = new StringBuilder();
        sb.append("class MetaOutboundConnectorListItem {\n");

        sb.append("    connectorId: ").append(toIndentedString(connectorId)).append("\n");
        sb.append("    name: ").append(toIndentedString(name)).append("\n");
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


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

package org.wso2.identity.integration.test.rest.api.server.claim.management.v1.model;

import com.fasterxml.jackson.annotation.JsonProperty;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;

import java.util.Objects;

/**
 * Claim userstore attribute mapping.
 **/
@ApiModel(description = "Claim user store attribute mapping.")
public class AttributeMappingDTO {

    private String mappedAttribute = null;
    private String userstore = null;

    @ApiModelProperty(required = true, value = "User store attribute to be mapped to.")
    @JsonProperty("mappedAttribute")
    public String getMappedAttribute() {

        return mappedAttribute;
    }

    public void setMappedAttribute(String mappedAttribute) {

        this.mappedAttribute = mappedAttribute;
    }

    @ApiModelProperty(required = true, value = "Userstore domain name.")
    @JsonProperty("userstore")
    public String getUserstore() {

        return userstore;
    }

    public void setUserstore(String userstore) {

        this.userstore = userstore;
    }

    @Override
    public boolean equals(Object o) {

        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }
        AttributeMappingDTO attributeMappingDTO = (AttributeMappingDTO) o;
        return Objects.equals(this.mappedAttribute, attributeMappingDTO.mappedAttribute) &&
                Objects.equals(this.userstore, attributeMappingDTO.userstore);
    }

    @Override
    public int hashCode() {

        return Objects.hash(mappedAttribute, userstore);
    }

    @Override
    public String toString() {

        return "class AttributeMappingDTO {\n" +
                "    mappedAttribute: " + mappedAttribute + "\n" +
                "    userstore: " + userstore + "\n" +
                "}\n";
    }
}

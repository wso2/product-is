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

import javax.validation.Valid;
import javax.validation.constraints.NotNull;

/**
* Claim dialect request.
**/
@ApiModel(description = "Claim dialect request.")
public class ClaimDialectReqDTO {

@Valid
@NotNull(message = "Property dialectURI cannot be null.")
private String dialectURI = null;

/**
* URI of the claim dialect.
**/
@ApiModelProperty(required = true, value = "URI of the claim dialect.")
@JsonProperty("dialectURI")
public String getDialectURI() {
    return dialectURI;
}
public void setDialectURI(String dialectURI) {
    this.dialectURI = dialectURI;
}

@Override
public String toString() {

    StringBuilder sb = new StringBuilder();
    sb.append("class ClaimDialectReqDTO {\n");

    sb.append("    dialectURI: ").append(dialectURI).append("\n");

    sb.append("}\n");
    return sb.toString();
}
}

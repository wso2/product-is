/*
 * Copyright (c) 2026, WSO2 LLC. (http://www.wso2.com).
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

import javax.validation.Valid;
import java.util.Objects;

public class TokenExchangeConfiguration {

    private Boolean restrictScopeIssuanceForFederatedTokens;

    /**
    * If enabled, no scopes are issued for federated tokens in token exchange grant type.
    **/
    public TokenExchangeConfiguration restrictScopeIssuanceForFederatedTokens(Boolean restrictScopeIssuanceForFederatedTokens) {

        this.restrictScopeIssuanceForFederatedTokens = restrictScopeIssuanceForFederatedTokens;
        return this;
    }

    @ApiModelProperty(example = "false", value = "If enabled, no scopes are issued for federated tokens in token exchange grant type.")
    @JsonProperty("restrictScopeIssuanceForFederatedTokens")
    @Valid
    public Boolean getRestrictScopeIssuanceForFederatedTokens() {
        return restrictScopeIssuanceForFederatedTokens;
    }
    public void setRestrictScopeIssuanceForFederatedTokens(Boolean restrictScopeIssuanceForFederatedTokens) {
        this.restrictScopeIssuanceForFederatedTokens = restrictScopeIssuanceForFederatedTokens;
    }



    @Override
    public boolean equals(Object o) {

        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }
        TokenExchangeConfiguration tokenExchangeConfiguration = (TokenExchangeConfiguration) o;
        return Objects.equals(this.restrictScopeIssuanceForFederatedTokens, tokenExchangeConfiguration.restrictScopeIssuanceForFederatedTokens);
    }

    @Override
    public int hashCode() {
        return Objects.hash(restrictScopeIssuanceForFederatedTokens);
    }

    @Override
    public String toString() {

        StringBuilder sb = new StringBuilder();
        sb.append("class TokenExchangeConfiguration {\n");
        
        sb.append("    restrictScopeIssuanceForFederatedTokens: ").append(toIndentedString(restrictScopeIssuanceForFederatedTokens)).append("\n");
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

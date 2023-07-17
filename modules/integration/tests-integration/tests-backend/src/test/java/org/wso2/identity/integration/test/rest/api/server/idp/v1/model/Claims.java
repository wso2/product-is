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

package org.wso2.identity.integration.test.rest.api.server.idp.v1.model;

import com.fasterxml.jackson.annotation.JsonProperty;
import io.swagger.annotations.ApiModelProperty;

import javax.validation.Valid;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

public class Claims {
    private Claim userIdClaim;
    private Claim roleClaim;
    private List<ClaimMapping> mappings;
    private List<ProvisioningClaim> provisioningClaims;

    /**
     *
     **/
    public Claims userIdClaim(Claim userIdClaim) {

        this.userIdClaim = userIdClaim;
        return this;
    }

    @ApiModelProperty()
    @JsonProperty("userIdClaim")
    @Valid
    public Claim getUserIdClaim() {
        return userIdClaim;
    }

    public void setUserIdClaim(Claim userIdClaim) {
        this.userIdClaim = userIdClaim;
    }

    /**
     *
     **/
    public Claims roleClaim(Claim roleClaim) {

        this.roleClaim = roleClaim;
        return this;
    }

    @ApiModelProperty()
    @JsonProperty("roleClaim")
    @Valid
    public Claim getRoleClaim() {
        return roleClaim;
    }

    public void setRoleClaim(Claim roleClaim) {
        this.roleClaim = roleClaim;
    }

    /**
     *
     **/
    public Claims mappings(List<ClaimMapping> mappings) {

        this.mappings = mappings;
        return this;
    }

    @ApiModelProperty()
    @JsonProperty("mappings")
    @Valid
    public List<ClaimMapping> getMappings() {
        return mappings;
    }

    public void setMappings(List<ClaimMapping> mappings) {
        this.mappings = mappings;
    }

    public Claims addMappings(ClaimMapping mapping) {
        if (this.mappings == null) {
            this.mappings = new ArrayList<>();
        }
        this.mappings.add(mapping);
        return this;
    }

    /**
     *
     **/
    public Claims provisioningClaims(List<ProvisioningClaim> provisioningClaims) {

        this.provisioningClaims = provisioningClaims;
        return this;
    }

    @ApiModelProperty()
    @JsonProperty("provisioningClaims")
    @Valid
    public List<ProvisioningClaim> getProvisioningClaims() {
        return provisioningClaims;
    }

    public void setProvisioningClaims(List<ProvisioningClaim> provisioningClaims) {
        this.provisioningClaims = provisioningClaims;
    }

    @Override
    public boolean equals(Object o) {

        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }
        Claims claims = (Claims) o;
        return  Objects.equals(this.userIdClaim, claims.userIdClaim) &&
                Objects.equals(this.roleClaim, claims.roleClaim) &&
                Objects.equals(this.mappings, claims.mappings) &&
                Objects.equals(this.provisioningClaims, claims.provisioningClaims);
    }

    @Override
    public int hashCode() {
        return Objects.hash(userIdClaim, roleClaim, mappings, provisioningClaims);
    }

    @Override
    public String toString() {

        return "class Claims {\n" +
                "    userIdClaim: " + toIndentedString(userIdClaim) + "\n" +
                "    roleClaim: " + toIndentedString(roleClaim) + "\n" +
                "    mappings: " + toIndentedString(mappings) + "\n" +
                "    provisioningClaims: " + toIndentedString(provisioningClaims) + "\n" +
                "}";
    }

    /**
     * Convert the given object to string with each line indented by 4 spaces
     * (except the first line).
     */
    private static String toIndentedString(java.lang.Object o) {

        if (o == null) {
            return "null";
        }
        return o.toString();
    }

    public static class ClaimMapping {
        private String idpClaim;
        private Claim localClaim;

        /**
         *
         **/
        public ClaimMapping idpClaim(String idpClaim) {

            this.idpClaim = idpClaim;
            return this;
        }

        @ApiModelProperty()
        @JsonProperty("idpClaim")
        @Valid
        public String getIdpClaim() {
            return idpClaim;
        }

        public void setIdpClaim(String idpClaim) {
            this.idpClaim = idpClaim;
        }

        /**
         *
         **/
        public ClaimMapping localClaim(Claim localClaim) {

            this.localClaim = localClaim;
            return this;
        }

        @ApiModelProperty()
        @JsonProperty("localClaim")
        @Valid
        public Claim getLocalClaim() {
            return localClaim;
        }

        public void setLocalClaim(Claim localClaim) {
            this.localClaim = localClaim;
        }

        @Override
        public boolean equals(Object o) {

            if (this == o) {
                return true;
            }
            if (o == null || getClass() != o.getClass()) {
                return false;
            }
            ClaimMapping claimMapping = (ClaimMapping) o;
            return Objects.equals(this.idpClaim, claimMapping.idpClaim) &&
                    Objects.equals(this.localClaim, claimMapping.localClaim);
        }

        @Override
        public int hashCode() {
            return Objects.hash(idpClaim, localClaim);
        }

        @Override
        public String toString() {

            return "class ClaimMapping {\n" +
                    "    idpClaim: " + toIndentedString(idpClaim) + "\n" +
                    "    localClaim: " + toIndentedString(localClaim) + "\n" +
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

    public static class ProvisioningClaim {
        private Claim claim;
        private String defaultValue;

        /**
         *
         **/
        public ProvisioningClaim claim(Claim claim) {

            this.claim = claim;
            return this;
        }

        @ApiModelProperty()
        @JsonProperty("claim")
        @Valid
        public Claim getClaim() {
            return claim;
        }

        public void setClaim(Claim claim) {
            this.claim = claim;
        }

        /**
         *
         **/
        public ProvisioningClaim defaultValue(String defaultValue) {

            this.defaultValue = defaultValue;
            return this;
        }

        @ApiModelProperty()
        @JsonProperty("defaultValue")
        @Valid
        public String getDefaultValue() {
            return defaultValue;
        }

        public void setDefaultValue(String defaultValue) {
            this.defaultValue = defaultValue;
        }

        @Override
        public boolean equals(Object o) {

            if (this == o) {
                return true;
            }
            if (o == null || getClass() != o.getClass()) {
                return false;
            }
            ProvisioningClaim provisioningClaim = (ProvisioningClaim) o;
            return Objects.equals(this.claim, provisioningClaim.claim) &&
                    Objects.equals(this.defaultValue, provisioningClaim.defaultValue);
        }

        @Override
        public int hashCode() {
            return Objects.hash(claim, defaultValue);
        }

        @Override
        public String toString() {

            return "class ProvisioningClaim {\n" +
                    "    claim: " + toIndentedString(claim) + "\n" +
                    "    defaultValue: " + toIndentedString(defaultValue) + "\n" +
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

    public static class Claim {
        private String id;
        private String uri;
        private String displayName;

        /**
         *
         **/
        public Claim id(String id) {

            this.id = id;
            return this;
        }

        @ApiModelProperty()
        @JsonProperty("id")
        @Valid
        public String getId() {
            return id;
        }

        public void setId(String id) {
            this.id = id;
        }

        /**
         *
         **/
        public Claim uri(String uri) {

            this.uri = uri;
            return this;
        }

        @ApiModelProperty()
        @JsonProperty("uri")
        @Valid
        public String getUri() {
            return uri;
        }

        public void setUri(String uri) {
            this.uri = uri;
        }

        /**
         *
         **/
        public Claim displayName(String displayName) {

            this.displayName = displayName;
            return this;
        }

        @ApiModelProperty()
        @JsonProperty("displayName")
        @Valid
        public String getDisplayName() {
            return displayName;
        }

        public void setDisplayName(String displayName) {
            this.displayName = displayName;
        }

        @Override
        public boolean equals(Object o) {

            if (this == o) {
                return true;
            }
            if (o == null || getClass() != o.getClass()) {
                return false;
            }
            Claim claim = (Claim) o;
            return Objects.equals(this.id, claim.id) &&
                    Objects.equals(this.uri, claim.uri) &&
                    Objects.equals(this.displayName, claim.displayName);
        }

        @Override
        public int hashCode() {
            return Objects.hash(id, uri, displayName);
        }

        @Override
        public String toString() {

            return "class Claim {\n" +
                    "    id: " + toIndentedString(id) + "\n" +
                    "    uri: " + toIndentedString(uri) + "\n" +
                    "    displayName: " + toIndentedString(displayName) + "\n" +
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
}

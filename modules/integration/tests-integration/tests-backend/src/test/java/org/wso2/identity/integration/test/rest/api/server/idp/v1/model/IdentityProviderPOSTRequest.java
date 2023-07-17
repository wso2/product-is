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

public class IdentityProviderPOSTRequest {

    private String name;
    private String description;
    private String image;
    private Boolean isPrimary = false;
    private Boolean isFederationHub = false;
    private String homeRealmIdentifier;
    private Certificate certificate;
    private String alias;
    private Claims claims;
    private Roles roles;
    private FederatedAuthenticatorRequest federatedAuthenticators;
    private ProvisioningRequest provisioning;

    /**
     *
     **/
    public IdentityProviderPOSTRequest name(String name) {

        this.name = name;
        return this;
    }

    @ApiModelProperty()
    @JsonProperty("name")
    @Valid
    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    /**
     *
     **/
    public IdentityProviderPOSTRequest description(String description) {

        this.description = description;
        return this;
    }

    @ApiModelProperty()
    @JsonProperty("description")
    @Valid
    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    /**
     *
     **/
    public IdentityProviderPOSTRequest image(String image) {

        this.image = image;
        return this;
    }

    @ApiModelProperty()
    @JsonProperty("image")
    @Valid
    public String getImage() {
        return image;
    }

    public void setImage(String image) {
        this.image = image;
    }

    /**
     *
     **/
    public IdentityProviderPOSTRequest isPrimary(Boolean isPrimary) {

        this.isPrimary = isPrimary;
        return this;
    }

    @ApiModelProperty()
    @JsonProperty("isPrimary")
    @Valid
    public Boolean getIsPrimary() {
        return isPrimary;
    }

    public void setIsPrimary(Boolean isPrimary) {
        this.isPrimary = isPrimary;
    }

    /**
     *
     **/
    public IdentityProviderPOSTRequest isFederationHub(Boolean isFederationHub) {

        this.isFederationHub = isFederationHub;
        return this;
    }

    @ApiModelProperty()
    @JsonProperty("isFederationHub")
    @Valid
    public Boolean getIsFederationHub() {
        return isFederationHub;
    }

    public void setIsFederationHub(Boolean isFederationHub) {
        this.isFederationHub = isFederationHub;
    }

    /**
     *
     **/
    public IdentityProviderPOSTRequest homeRealmIdentifier(String homeRealmIdentifier) {

        this.homeRealmIdentifier = homeRealmIdentifier;
        return this;
    }

    @ApiModelProperty()
    @JsonProperty("homeRealmIdentifier")
    @Valid
    public String getHomeRealmIdentifier() {
        return homeRealmIdentifier;
    }

    public void setHomeRealmIdentifier(String homeRealmIdentifier) {
        this.homeRealmIdentifier = homeRealmIdentifier;
    }

    /**
     *
     **/
    public IdentityProviderPOSTRequest certificate(Certificate certificate) {

        this.certificate = certificate;
        return this;
    }

    @ApiModelProperty()
    @JsonProperty("certificate")
    @Valid
    public Certificate getCertificate() {
        return certificate;
    }

    public void setCertificate(Certificate certificate) {
        this.certificate = certificate;
    }

    /**
     *
     **/
    public IdentityProviderPOSTRequest alias(String alias) {

        this.alias = alias;
        return this;
    }

    @ApiModelProperty()
    @JsonProperty("alias")
    @Valid
    public String getAlias() {
        return alias;
    }

    public void setAlias(String alias) {
        this.alias = alias;
    }

    /**
     *
     **/
    public IdentityProviderPOSTRequest claims(Claims claims) {

        this.claims = claims;
        return this;
    }

    @ApiModelProperty()
    @JsonProperty("claims")
    @Valid
    public Claims getClaims() {
        return claims;
    }

    public void setClaims(Claims claims) {
        this.claims = claims;
    }

    /**
     *
     **/
    public IdentityProviderPOSTRequest roles(Roles roles) {

        this.roles = roles;
        return this;
    }

    @ApiModelProperty()
    @JsonProperty("roles")
    @Valid
    public Roles getRoles() {
        return roles;
    }

    public void setRoles(Roles roles) {
        this.roles = roles;
    }

    /**
     *
     **/
    public IdentityProviderPOSTRequest federatedAuthenticators(FederatedAuthenticatorRequest federatedAuthenticators) {

        this.federatedAuthenticators = federatedAuthenticators;
        return this;
    }

    @ApiModelProperty()
    @JsonProperty("federatedAuthenticators")
    @Valid
    public FederatedAuthenticatorRequest getFederatedAuthenticators() {
        return federatedAuthenticators;
    }

    public void setFederatedAuthenticators(FederatedAuthenticatorRequest federatedAuthenticators) {
        this.federatedAuthenticators = federatedAuthenticators;
    }

    /**
     *
     **/
    public IdentityProviderPOSTRequest provisioning(ProvisioningRequest provisioning) {

        this.provisioning = provisioning;
        return this;
    }

    @ApiModelProperty()
    @JsonProperty("provisioning")
    @Valid
    public ProvisioningRequest getProvisioning() {
        return provisioning;
    }

    public void setProvisioning(ProvisioningRequest provisioning) {
        this.provisioning = provisioning;
    }

    @Override
    public boolean equals(Object o) {

        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }
        IdentityProviderPOSTRequest identityProviderPOSTRequest = (IdentityProviderPOSTRequest) o;
        return Objects.equals(this.name, identityProviderPOSTRequest.name) && Objects.equals(this.description, identityProviderPOSTRequest.description) && Objects.equals(this.image, identityProviderPOSTRequest.image) && Objects.equals(this.isPrimary, identityProviderPOSTRequest.isPrimary) && Objects.equals(this.isFederationHub, identityProviderPOSTRequest.isFederationHub) && Objects.equals(this.homeRealmIdentifier, identityProviderPOSTRequest.homeRealmIdentifier) && Objects.equals(this.certificate, identityProviderPOSTRequest.certificate) && Objects.equals(this.alias, identityProviderPOSTRequest.alias) && Objects.equals(this.claims, identityProviderPOSTRequest.claims) && Objects.equals(this.roles, identityProviderPOSTRequest.roles) && Objects.equals(this.federatedAuthenticators, identityProviderPOSTRequest.federatedAuthenticators) && Objects.equals(this.provisioning, identityProviderPOSTRequest.provisioning);
    }

    @Override
    public int hashCode() {
        return Objects.hash(name, description, image, isPrimary, isFederationHub, homeRealmIdentifier, certificate, alias, claims, roles, federatedAuthenticators, provisioning);
    }

    @Override
    public String toString() {

        return "class IdentityProviderPOSTRequest {\n" + "    name: " + toIndentedString(name) + "\n" + "    description: " + toIndentedString(description) + "\n" + "    image: " + toIndentedString(image) + "\n" + "    isPrimary: " + toIndentedString(isPrimary) + "\n" + "    isFederationHub: " + toIndentedString(isFederationHub) + "\n" + "    homeRealmIdentifier: " + toIndentedString(homeRealmIdentifier) + "\n" + "    certificate: " + toIndentedString(certificate) + "\n" + "    alias: " + toIndentedString(alias) + "\n" + "    claims: " + toIndentedString(claims) + "\n" + "    roles: " + toIndentedString(roles) + "\n" + "    federatedAuthenticators: " + toIndentedString(federatedAuthenticators) + "\n" + "    provisioning: " + toIndentedString(provisioning) + "\n" + "}";
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

    public static class Certificate {
        private List<String> certificates = null;
        private String jwksUri;

        /**
         *
         **/
        public Certificate certificates(List<String> certificates) {

            this.certificates = certificates;
            return this;
        }

        @ApiModelProperty()
        @JsonProperty("certificates")
        @Valid
        public List<String> getCertificates() {
            return certificates;
        }

        public void setCertificates(List<String> certificates) {
            this.certificates = certificates;
        }

        public Certificate addCertificates(String certificate) {
            if (this.certificates == null) {
                this.certificates = new ArrayList<>();
            }
            this.certificates.add(certificate);
            return this;
        }

        /**
         *
         **/
        public Certificate jwksUri(String jwksUri) {

            this.jwksUri = jwksUri;
            return this;
        }

        @ApiModelProperty()
        @JsonProperty("jwksUri")
        @Valid
        public String getJwksUri() {
            return jwksUri;
        }

        public void setJwksUri(String jwksUri) {
            this.jwksUri = jwksUri;
        }

        @Override
        public boolean equals(Object o) {

            if (this == o) {
                return true;
            }
            if (o == null || getClass() != o.getClass()) {
                return false;
            }
            Certificate certificate = (Certificate) o;
            return Objects.equals(this.certificates, certificate.certificates) &&
                    Objects.equals(this.jwksUri, certificate.jwksUri);
        }

        @Override
        public int hashCode() {
            return Objects.hash(certificates, jwksUri);
        }

        @Override
        public String toString() {

            return "class Certificate {\n" +
                    "    certificates: " + toIndentedString(certificates) + "\n" +
                    "    jwksUri: " + toIndentedString(jwksUri) + "\n" +
                    "}";
        }
    }
}
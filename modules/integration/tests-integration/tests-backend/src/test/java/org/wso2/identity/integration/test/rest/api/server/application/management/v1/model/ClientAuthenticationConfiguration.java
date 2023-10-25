package org.wso2.identity.integration.test.rest.api.server.application.management.v1.model;

import com.fasterxml.jackson.annotation.JsonProperty;
import io.swagger.annotations.ApiModelProperty;

import javax.validation.Valid;
import java.util.Objects;

public class ClientAuthenticationConfiguration {

    private String tokenEndpointAuthMethod;
    private String tokenEndpointAuthSigningAlg;
    private String tlsClientAuthSubjectDn;

    /**
     *
     **/
    public ClientAuthenticationConfiguration tokenEndpointAuthMethod(String tokenEndpointAuthMethod) {

        this.tokenEndpointAuthMethod = tokenEndpointAuthMethod;
        return this;
    }

    @ApiModelProperty(example = "true", value = "")
    @JsonProperty("tokenEndpointAuthMethod")
    @Valid
    public String getTokenEndpointAuthMethod() {
        return tokenEndpointAuthMethod;
    }

    public void setTokenEndpointAuthMethod(String tokenEndpointAuthMethod) {
        this.tokenEndpointAuthMethod = tokenEndpointAuthMethod;
    }

    /**
     *
     **/
    public ClientAuthenticationConfiguration tokenEndpointAuthSigningAlg(String tokenEndpointAuthSigningAlg) {

        this.tokenEndpointAuthSigningAlg = tokenEndpointAuthSigningAlg;
        return this;
    }

    @ApiModelProperty(example = "PS256", value = "")
    @JsonProperty("tokenEndpointAuthSigningAlg")
    @Valid
    public String getTokenEndpointAuthSigningAlg() {
        return tokenEndpointAuthSigningAlg;
    }

    public void setTokenEndpointAuthSigningAlg(String tokenEndpointAuthSigningAlg) {
        this.tokenEndpointAuthSigningAlg = tokenEndpointAuthSigningAlg;
    }

    /**
     *
     **/
    public ClientAuthenticationConfiguration tlsClientAuthSubjectDn(String tlsClientAuthSubjectDn) {

        this.tlsClientAuthSubjectDn = tlsClientAuthSubjectDn;
        return this;
    }

    @ApiModelProperty(example = "CN=John Doe,OU=OrgUnit,O=Organization,L=Colombo,ST=Western,C=LK", value = "")
    @JsonProperty("tlsClientAuthSubjectDn")
    @Valid
    public String getTlsClientAuthSubjectDn() {
        return tlsClientAuthSubjectDn;
    }

    public void setTlsClientAuthSubjectDn(String tlsClientAuthSubjectDn) {
        this.tlsClientAuthSubjectDn = tlsClientAuthSubjectDn;
    }

    @Override
    public boolean equals(java.lang.Object o) {

        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }
        ClientAuthenticationConfiguration clientAuthenticationConfiguration = (ClientAuthenticationConfiguration) o;
        return Objects.equals(this.tokenEndpointAuthMethod, clientAuthenticationConfiguration.tokenEndpointAuthMethod) &&
                Objects.equals(this.tokenEndpointAuthSigningAlg, clientAuthenticationConfiguration.tokenEndpointAuthSigningAlg) &&
                Objects.equals(this.tlsClientAuthSubjectDn, clientAuthenticationConfiguration.tlsClientAuthSubjectDn);
    }

    @Override
    public int hashCode() {
        return Objects.hash(tokenEndpointAuthMethod, tokenEndpointAuthSigningAlg, tlsClientAuthSubjectDn);
    }

    @Override
    public String toString() {

        StringBuilder sb = new StringBuilder();
        sb.append("class ClientAuthenticationConfiguration {\n");

        sb.append("    tokenEndpointAuthMethod: ").append(toIndentedString(tokenEndpointAuthMethod)).append("\n");
        sb.append("    tokenEndpointAuthSigningAlg: ").append(toIndentedString(tokenEndpointAuthSigningAlg)).append("\n");
        sb.append("    tlsClientAuthSubjectDn: ").append(toIndentedString(tlsClientAuthSubjectDn)).append("\n");
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

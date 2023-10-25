package org.wso2.identity.integration.test.rest.api.server.application.management.v1.model;

import com.fasterxml.jackson.annotation.JsonProperty;
import io.swagger.annotations.ApiModelProperty;

import javax.validation.Valid;
import java.util.Objects;

public class RequestObjectConfiguration {

    private String requestObjectSigningAlg;
    private Boolean requireSignedRequestObject;

    private RequestObjectEncryptionConfiguration encryption;

    /**
     **/
    public RequestObjectConfiguration requestObjectSigningAlg(String requestObjectSigningAlg) {

        this.requestObjectSigningAlg = requestObjectSigningAlg;
        return this;
    }

    @ApiModelProperty(example = "PS256", value = "")
    @JsonProperty("requestObjectSigningAlg")
    @Valid
    public String getRequestObjectSigningAlg() {
        return requestObjectSigningAlg;
    }
    public void setRequestObjectSigningAlg(String requestObjectSigningAlg) {
        this.requestObjectSigningAlg = requestObjectSigningAlg;
    }

    /**
     **/
    public RequestObjectConfiguration requireSignedRequestObject(Boolean requireSignedRequestObject) {

        this.requireSignedRequestObject = requireSignedRequestObject;
        return this;
    }

    @ApiModelProperty(example = "false", value = "")
    @JsonProperty("requireSignedRequestObject")
    @Valid
    public Boolean getRequireSignedRequestObject() {
        return requireSignedRequestObject;
    }
    public void setRequireSignedRequestObject(Boolean requireSignedRequestObject) {
        this.requireSignedRequestObject = requireSignedRequestObject;
    }

    /**
     **/
    public RequestObjectConfiguration encryption(RequestObjectEncryptionConfiguration encryption) {

        this.encryption = encryption;
        return this;
    }

    @ApiModelProperty(value = "")
    @JsonProperty("encryption")
    @Valid
    public RequestObjectEncryptionConfiguration getEncryption() {
        return encryption;
    }
    public void setEncryption(RequestObjectEncryptionConfiguration encryption) {
        this.encryption = encryption;
    }

    @Override
    public boolean equals(java.lang.Object o) {

        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }
        RequestObjectConfiguration requestObjectConfiguration = (RequestObjectConfiguration) o;
        return Objects.equals(this.requestObjectSigningAlg, requestObjectConfiguration.requestObjectSigningAlg) &&
                Objects.equals(this.requireSignedRequestObject, requestObjectConfiguration.requireSignedRequestObject) &&
                Objects.equals(this.encryption, requestObjectConfiguration.encryption);
    }

    @Override
    public int hashCode() {
        return Objects.hash(requestObjectSigningAlg, requireSignedRequestObject, encryption);
    }

    @Override
    public String toString() {

        StringBuilder sb = new StringBuilder();
        sb.append("class RequestObjectConfiguration {\n");

        sb.append("    requestObjectSigningAlg: ").append(toIndentedString(requestObjectSigningAlg)).append("\n");
        sb.append("    requireSignedRequestObject: ").append(toIndentedString(requireSignedRequestObject)).append("\n");
        sb.append("    encryption: ").append(toIndentedString(encryption)).append("\n");
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

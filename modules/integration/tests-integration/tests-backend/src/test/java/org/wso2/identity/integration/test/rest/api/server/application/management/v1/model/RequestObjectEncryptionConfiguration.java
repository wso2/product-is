package org.wso2.identity.integration.test.rest.api.server.application.management.v1.model;

import com.fasterxml.jackson.annotation.JsonProperty;
import io.swagger.annotations.ApiModelProperty;

import javax.validation.Valid;
import java.util.Objects;

public class RequestObjectEncryptionConfiguration {

    private String algorithm;
    private String method;

    /**
     **/
    public RequestObjectEncryptionConfiguration algorithm(String algorithm) {

        this.algorithm = algorithm;
        return this;
    }

    @ApiModelProperty(example = "RSA-OAEP", value = "")
    @JsonProperty("algorithm")
    @Valid
    public String getAlgorithm() {
        return algorithm;
    }
    public void setAlgorithm(String algorithm) {
        this.algorithm = algorithm;
    }

    /**
     **/
    public RequestObjectEncryptionConfiguration method(String method) {

        this.method = method;
        return this;
    }

    @ApiModelProperty(example = "A128CBC+HS256", value = "")
    @JsonProperty("method")
    @Valid
    public String getMethod() {
        return method;
    }
    public void setMethod(String method) {
        this.method = method;
    }

    @Override
    public boolean equals(java.lang.Object o) {

        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }
        RequestObjectEncryptionConfiguration requestObjectEncryptionConfiguration = (RequestObjectEncryptionConfiguration) o;
        return Objects.equals(this.algorithm, requestObjectEncryptionConfiguration.algorithm) &&
                Objects.equals(this.method, requestObjectEncryptionConfiguration.method);
    }

    @Override
    public int hashCode() {
        return Objects.hash(algorithm, method);
    }

    @Override
    public String toString() {

        StringBuilder sb = new StringBuilder();
        sb.append("class RequestObjectEncryptionConfiguration {\n");

        sb.append("    algorithm: ").append(toIndentedString(algorithm)).append("\n");
        sb.append("    method: ").append(toIndentedString(method)).append("\n");
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

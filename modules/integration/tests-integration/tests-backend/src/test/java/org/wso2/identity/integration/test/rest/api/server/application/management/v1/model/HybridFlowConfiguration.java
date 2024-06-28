package org.wso2.identity.integration.test.rest.api.server.application.management.v1.model;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonCreator;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import javax.validation.constraints.*;


import io.swagger.annotations.*;
import java.util.Objects;
import javax.validation.Valid;
import javax.xml.bind.annotation.*;

public class HybridFlowConfiguration {

    private Boolean enable;
    private String responseType;

    /**
     *
     **/
    public HybridFlowConfiguration enable(Boolean enable) {

        this.enable = enable;
        return this;
    }

    @ApiModelProperty(example = "true", value = "")
    @JsonProperty("enable")
    @Valid
    public Boolean getEnable() {
        return enable;
    }

    public void setEnable(Boolean enable) {
        this.enable = enable;
    }

    /**
     *
     **/
    public HybridFlowConfiguration responseType(String responseType) {

        this.responseType = responseType;
        return this;
    }

    @ApiModelProperty(example = "code id_token", value = "")
    @JsonProperty("responseType")
    @Valid
    public String getResponseType() {
        return responseType;
    }

    public void setResponseType(String responseType) {
        this.responseType = responseType;
    }


    @Override
    public boolean equals(java.lang.Object o) {

        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }
        HybridFlowConfiguration hybridFlowConfiguration = (HybridFlowConfiguration) o;
        return Objects.equals(this.enable, hybridFlowConfiguration.enable) &&
                Objects.equals(this.responseType, hybridFlowConfiguration.responseType);
    }

    @Override
    public int hashCode() {
        return Objects.hash(enable, responseType);
    }

    @Override
    public String toString() {

        StringBuilder sb = new StringBuilder();
        sb.append("class HybridFlowConfiguration {\n");

        sb.append("    enable: ").append(toIndentedString(enable)).append("\n");
        sb.append("    responseType: ").append(toIndentedString(responseType)).append("\n");
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

package org.wso2.identity.integration.test.rest.api.server.application.management.v1.model;

import com.fasterxml.jackson.annotation.JsonProperty;
import io.swagger.annotations.ApiModelProperty;

import javax.validation.Valid;
import java.util.Objects;

public class SubjectConfiguration {

    private String subjectType;
    private String sectorIdentifierUri;

    /**
     **/
    public SubjectConfiguration subjectType(String subjectType) {

        this.subjectType = subjectType;
        return this;
    }

    @ApiModelProperty(example = "public", value = "")
    @JsonProperty("subjectType")
    @Valid
    public String getSubjectType() {
        return subjectType;
    }
    public void setSubjectType(String subjectType) {
        this.subjectType = subjectType;
    }

    /**
     **/
    public SubjectConfiguration sectorIdentifierUri(String sectorIdentifierUri) {

        this.sectorIdentifierUri = sectorIdentifierUri;
        return this;
    }

    @ApiModelProperty(example = "https://app.example.com", value = "")
    @JsonProperty("sectorIdentifierUri")
    @Valid
    public String getSectorIdentifierUri() {
        return sectorIdentifierUri;
    }
    public void setSectorIdentifierUri(String sectorIdentifierUri) {
        this.sectorIdentifierUri = sectorIdentifierUri;
    }

    @Override
    public boolean equals(java.lang.Object o) {

        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }
        SubjectConfiguration subjectConfiguration = (SubjectConfiguration) o;
        return Objects.equals(this.subjectType, subjectConfiguration.subjectType) &&
                Objects.equals(this.sectorIdentifierUri, subjectConfiguration.sectorIdentifierUri);
    }

    @Override
    public int hashCode() {
        return Objects.hash(subjectType, sectorIdentifierUri);
    }

    @Override
    public String toString() {

        StringBuilder sb = new StringBuilder();
        sb.append("class SubjectConfiguration {\n");

        sb.append("    subjectType: ").append(toIndentedString(subjectType)).append("\n");
        sb.append("    sectorIdentifierUri: ").append(toIndentedString(sectorIdentifierUri)).append("\n");
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
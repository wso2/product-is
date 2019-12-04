/*
* Copyright (c) 2019, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
*
* Licensed under the Apache License, Version 2.0 (the "License");
* you may not use this file except in compliance with the License.
* You may obtain a copy of the License at
*
* http://www.apache.org/licenses/LICENSE-2.0
*
* Unless required by applicable law or agreed to in writing, software
* distributed under the License is distributed on an "AS IS" BASIS,
* WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
* See the License for the specific language governing permissions and
* limitations under the License.
*/

package org.wso2.identity.integration.test.rest.api.server.application.management.v1.model;

import com.fasterxml.jackson.annotation.JsonProperty;
import io.swagger.annotations.ApiModelProperty;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import javax.validation.Valid;
import javax.xml.bind.annotation.XmlEnum;
import javax.xml.bind.annotation.XmlEnumValue;
import javax.xml.bind.annotation.XmlType;

public class ClaimConfiguration  {
  

@XmlType(name="DialectEnum")
@XmlEnum(String.class)
public enum DialectEnum {

    @XmlEnumValue("CUSTOM") CUSTOM(String.valueOf("CUSTOM")), @XmlEnumValue("EXTERNAL") EXTERNAL(String.valueOf("EXTERNAL")), @XmlEnumValue("LOCAL") LOCAL(String.valueOf("LOCAL"));


    private String value;

    DialectEnum(String v) {
        value = v;
    }

    public String value() {
        return value;
    }

    @Override
    public String toString() {
        return String.valueOf(value);
    }

    public static DialectEnum fromValue(String value) {
        for (DialectEnum b : DialectEnum.values()) {
            if (b.value.equals(value)) {
                return b;
            }
        }
        throw new IllegalArgumentException("Unexpected value '" + value + "'");
    }
}

    private DialectEnum dialect = DialectEnum.LOCAL;
    private List<ClaimMappings> claimMappings = null;

    private List<RequestedClaimConfiguration> requestedClaims = null;

    private SubjectConfig subject;
    private RoleConfig role;

    /**
    **/
    public ClaimConfiguration dialect(DialectEnum dialect) {

        this.dialect = dialect;
        return this;
    }
    
    @ApiModelProperty(value = "")
    @JsonProperty("dialect")
    @Valid
    public DialectEnum getDialect() {
        return dialect;
    }
    public void setDialect(DialectEnum dialect) {
        this.dialect = dialect;
    }

    /**
    **/
    public ClaimConfiguration claimMappings(List<ClaimMappings> claimMappings) {

        this.claimMappings = claimMappings;
        return this;
    }
    
    @ApiModelProperty(value = "")
    @JsonProperty("claimMappings")
    @Valid
    public List<ClaimMappings> getClaimMappings() {
        return claimMappings;
    }
    public void setClaimMappings(List<ClaimMappings> claimMappings) {
        this.claimMappings = claimMappings;
    }

    public ClaimConfiguration addClaimMappingsItem(ClaimMappings claimMappingsItem) {
        if (this.claimMappings == null) {
            this.claimMappings = new ArrayList<>();
        }
        this.claimMappings.add(claimMappingsItem);
        return this;
    }

        /**
    **/
    public ClaimConfiguration requestedClaims(List<RequestedClaimConfiguration> requestedClaims) {

        this.requestedClaims = requestedClaims;
        return this;
    }
    
    @ApiModelProperty(value = "")
    @JsonProperty("requestedClaims")
    @Valid
    public List<RequestedClaimConfiguration> getRequestedClaims() {
        return requestedClaims;
    }
    public void setRequestedClaims(List<RequestedClaimConfiguration> requestedClaims) {
        this.requestedClaims = requestedClaims;
    }

    public ClaimConfiguration addRequestedClaimsItem(RequestedClaimConfiguration requestedClaimsItem) {
        if (this.requestedClaims == null) {
            this.requestedClaims = new ArrayList<>();
        }
        this.requestedClaims.add(requestedClaimsItem);
        return this;
    }

        /**
    **/
    public ClaimConfiguration subject(SubjectConfig subject) {

        this.subject = subject;
        return this;
    }
    
    @ApiModelProperty(value = "")
    @JsonProperty("subject")
    @Valid
    public SubjectConfig getSubject() {
        return subject;
    }
    public void setSubject(SubjectConfig subject) {
        this.subject = subject;
    }

    /**
    **/
    public ClaimConfiguration role(RoleConfig role) {

        this.role = role;
        return this;
    }
    
    @ApiModelProperty(value = "")
    @JsonProperty("role")
    @Valid
    public RoleConfig getRole() {
        return role;
    }
    public void setRole(RoleConfig role) {
        this.role = role;
    }



    @Override
    public boolean equals(Object o) {

        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }
        ClaimConfiguration claimConfiguration = (ClaimConfiguration) o;
        return Objects.equals(this.dialect, claimConfiguration.dialect) &&
            Objects.equals(this.claimMappings, claimConfiguration.claimMappings) &&
            Objects.equals(this.requestedClaims, claimConfiguration.requestedClaims) &&
            Objects.equals(this.subject, claimConfiguration.subject) &&
            Objects.equals(this.role, claimConfiguration.role);
    }

    @Override
    public int hashCode() {
        return Objects.hash(dialect, claimMappings, requestedClaims, subject, role);
    }

    @Override
    public String toString() {

        StringBuilder sb = new StringBuilder();
        sb.append("class ClaimConfiguration {\n");

        sb.append("    dialect: ").append(toIndentedString(dialect)).append("\n");
        sb.append("    claimMappings: ").append(toIndentedString(claimMappings)).append("\n");
        sb.append("    requestedClaims: ").append(toIndentedString(requestedClaims)).append("\n");
        sb.append("    subject: ").append(toIndentedString(subject)).append("\n");
        sb.append("    role: ").append(toIndentedString(role)).append("\n");
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


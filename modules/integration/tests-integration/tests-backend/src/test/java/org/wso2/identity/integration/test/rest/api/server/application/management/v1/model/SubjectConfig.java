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

import java.util.Objects;
import javax.validation.Valid;

public class SubjectConfig  {
  
    private String claimId;
    private Boolean includeUserDomain;
    private Boolean includeTenantDomain;
    private Boolean useMappedLocalSubject;

    /**
    **/
    public SubjectConfig claimId(String claimId) {

        this.claimId = claimId;
        return this;
    }
    
    @ApiModelProperty(example = "http://wso2.org/claims/emailaddress", value = "")
    @JsonProperty("claimId")
    @Valid
    public String getClaimId() {
        return claimId;
    }
    public void setClaimId(String claimId) {
        this.claimId = claimId;
    }

    /**
    **/
    public SubjectConfig includeUserDomain(Boolean includeUserDomain) {

        this.includeUserDomain = includeUserDomain;
        return this;
    }
    
    @ApiModelProperty(example = "false", value = "")
    @JsonProperty("includeUserDomain")
    @Valid
    public Boolean getIncludeUserDomain() {
        return includeUserDomain;
    }
    public void setIncludeUserDomain(Boolean includeUserDomain) {
        this.includeUserDomain = includeUserDomain;
    }

    /**
    **/
    public SubjectConfig includeTenantDomain(Boolean includeTenantDomain) {

        this.includeTenantDomain = includeTenantDomain;
        return this;
    }
    
    @ApiModelProperty(example = "false", value = "")
    @JsonProperty("includeTenantDomain")
    @Valid
    public Boolean getIncludeTenantDomain() {
        return includeTenantDomain;
    }
    public void setIncludeTenantDomain(Boolean includeTenantDomain) {
        this.includeTenantDomain = includeTenantDomain;
    }

    /**
    **/
    public SubjectConfig useMappedLocalSubject(Boolean useMappedLocalSubject) {

        this.useMappedLocalSubject = useMappedLocalSubject;
        return this;
    }
    
    @ApiModelProperty(example = "false", value = "")
    @JsonProperty("useMappedLocalSubject")
    @Valid
    public Boolean getUseMappedLocalSubject() {
        return useMappedLocalSubject;
    }
    public void setUseMappedLocalSubject(Boolean useMappedLocalSubject) {
        this.useMappedLocalSubject = useMappedLocalSubject;
    }



    @Override
    public boolean equals(Object o) {

        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }
        SubjectConfig subjectConfig = (SubjectConfig) o;
        return Objects.equals(this.claimId, subjectConfig.claimId) &&
            Objects.equals(this.includeUserDomain, subjectConfig.includeUserDomain) &&
            Objects.equals(this.includeTenantDomain, subjectConfig.includeTenantDomain) &&
            Objects.equals(this.useMappedLocalSubject, subjectConfig.useMappedLocalSubject);
    }

    @Override
    public int hashCode() {
        return Objects.hash(claimId, includeUserDomain, includeTenantDomain, useMappedLocalSubject);
    }

    @Override
    public String toString() {

        StringBuilder sb = new StringBuilder();
        sb.append("class SubjectConfig {\n");

        sb.append("    claimId: ").append(toIndentedString(claimId)).append("\n");
        sb.append("    includeUserDomain: ").append(toIndentedString(includeUserDomain)).append("\n");
        sb.append("    includeTenantDomain: ").append(toIndentedString(includeTenantDomain)).append("\n");
        sb.append("    useMappedLocalSubject: ").append(toIndentedString(useMappedLocalSubject)).append("\n");
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


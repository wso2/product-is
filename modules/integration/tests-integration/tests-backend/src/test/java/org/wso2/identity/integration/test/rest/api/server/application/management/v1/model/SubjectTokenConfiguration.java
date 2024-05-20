/*
 * Copyright (c) 2024, WSO2 LLC. (http://www.wso2.com).
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

public class SubjectTokenConfiguration {
  
    private Boolean enable;
    private Integer applicationSubjectTokenExpiryInSeconds;

    /**
    * If enabled, subject token can be issued for token exchange grant type.
    **/
    public SubjectTokenConfiguration enable(Boolean enable) {

        this.enable = enable;
        return this;
    }
    
    @ApiModelProperty(value = "If enabled, subject token can be issued for token exchange grant type.")
    @JsonProperty("enable")
    @Valid
    public Boolean getEnable() {
        return enable;
    }
    public void setEnable(Boolean enable) {
        this.enable = enable;
    }

    /**
    **/
    public SubjectTokenConfiguration applicationSubjectTokenExpiryInSeconds(Integer applicationSubjectTokenExpiryInSeconds) {

        this.applicationSubjectTokenExpiryInSeconds = applicationSubjectTokenExpiryInSeconds;
        return this;
    }
    
    @ApiModelProperty(example = "3600", value = "")
    @JsonProperty("applicationSubjectTokenExpiryInSeconds")
    @Valid
    public Integer getApplicationSubjectTokenExpiryInSeconds() {
        return applicationSubjectTokenExpiryInSeconds;
    }
    public void setApplicationSubjectTokenExpiryInSeconds(Integer applicationSubjectTokenExpiryInSeconds) {
        this.applicationSubjectTokenExpiryInSeconds = applicationSubjectTokenExpiryInSeconds;
    }



    @Override
    public boolean equals(Object o) {

        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }
        SubjectTokenConfiguration subjectTokenConfiguration = (SubjectTokenConfiguration) o;
        return Objects.equals(this.enable, subjectTokenConfiguration.enable) &&
            Objects.equals(this.applicationSubjectTokenExpiryInSeconds, subjectTokenConfiguration.applicationSubjectTokenExpiryInSeconds);
    }

    @Override
    public int hashCode() {
        return Objects.hash(enable, applicationSubjectTokenExpiryInSeconds);
    }

    @Override
    public String toString() {

        StringBuilder sb = new StringBuilder();
        sb.append("class SubjectTokenConfiguration {\n");
        
        sb.append("    enable: ").append(toIndentedString(enable)).append("\n");
        sb.append("    applicationSubjectTokenExpiryInSeconds: ").append(toIndentedString(applicationSubjectTokenExpiryInSeconds)).append("\n");
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


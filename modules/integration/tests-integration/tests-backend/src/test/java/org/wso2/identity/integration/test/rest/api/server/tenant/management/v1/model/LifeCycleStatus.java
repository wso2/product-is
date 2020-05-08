/*
 * Copyright (c) 2020, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
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

package org.wso2.identity.integration.test.rest.api.server.tenant.management.v1.model;

import com.fasterxml.jackson.annotation.JsonProperty;
import io.swagger.annotations.ApiModelProperty;

import java.util.Objects;

import javax.validation.Valid;

public class LifeCycleStatus {
  
    private Boolean activated;

    /**
    * Status of the tenant life cycle
    **/
    public LifeCycleStatus activated(Boolean activated) {

        this.activated = activated;
        return this;
    }
    
    @ApiModelProperty(example = "true", value = "Status of the tenant life cycle")
    @JsonProperty("activated")
    @Valid
    public Boolean getActivated() {
        return activated;
    }
    public void setActivated(Boolean activated) {
        this.activated = activated;
    }



    @Override
    public boolean equals(Object o) {

        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }
        LifeCycleStatus lifeCycleStatus = (LifeCycleStatus) o;
        return Objects.equals(this.activated, lifeCycleStatus.activated);
    }

    @Override
    public int hashCode() {
        return Objects.hash(activated);
    }

    @Override
    public String toString() {

        StringBuilder sb = new StringBuilder();
        sb.append("class LifeCycleStatus {\n");

        sb.append("    activated: ").append(toIndentedString(activated)).append("\n");
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


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

package org.wso2.identity.integration.test.rest.api.server.application.management.v1.model;

import com.fasterxml.jackson.annotation.JsonProperty;
import io.swagger.annotations.ApiModelProperty;

import javax.validation.Valid;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

public class IdpInitiatedSingleLogout {

    private Boolean enabled = false;
    private List<String> returnToUrls;

    /**
     *
     **/
    public IdpInitiatedSingleLogout enabled(Boolean enabled) {

        this.enabled = enabled;
        return this;
    }

    @ApiModelProperty(example = "false")
    @JsonProperty("enabled")
    @Valid
    public Boolean getEnabled() {
        return enabled;
    }

    public void setEnabled(Boolean enabled) {
        this.enabled = enabled;
    }

    /**
     *
     **/
    public IdpInitiatedSingleLogout returnToUrls(List<String> returnToUrls) {

        this.returnToUrls = returnToUrls;
        return this;
    }

    @ApiModelProperty()
    @JsonProperty("returnToUrls")
    @Valid
    public List<String> getReturnToUrls() {
        return returnToUrls;
    }

    public void setReturnToUrls(List<String> returnToUrls) {
        this.returnToUrls = returnToUrls;
    }

    public IdpInitiatedSingleLogout addReturnToUrls(String returnToUrl) {
        if (this.returnToUrls == null) {
            this.returnToUrls = new ArrayList<>();
        }
        this.returnToUrls.add(returnToUrl);
        return this;
    }

    @Override
    public boolean equals(Object o) {

        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }
        IdpInitiatedSingleLogout idpInitiatedSingleLogout = (IdpInitiatedSingleLogout) o;
        return Objects.equals(this.enabled, idpInitiatedSingleLogout.enabled) &&
                Objects.equals(this.returnToUrls, idpInitiatedSingleLogout.returnToUrls);
    }

    @Override
    public int hashCode() {
        return Objects.hash(enabled, returnToUrls);
    }

    @Override
    public String toString() {

        return "class IdpInitiatedSingleLogout {\n" +
                "    enabled: " + toIndentedString(enabled) + "\n" +
                "    returnToUrls: " + toIndentedString(returnToUrls) + "\n" +
                "}";
    }

    /**
     * Convert the given object to string with each line indented by 4 spaces
     * (except the first line).
     */
    private String toIndentedString(Object o) {

        if (o == null) {
            return "null";
        }
        return o.toString();

    }

}

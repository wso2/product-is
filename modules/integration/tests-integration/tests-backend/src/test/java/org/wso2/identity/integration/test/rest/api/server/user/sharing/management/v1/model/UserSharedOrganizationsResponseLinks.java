/*
 * Copyright (c) 2025, WSO2 LLC. (http://www.wso2.com).
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

package org.wso2.identity.integration.test.rest.api.server.user.sharing.management.v1.model;

import com.fasterxml.jackson.annotation.JsonProperty;
import io.swagger.annotations.ApiModelProperty;

import java.util.Objects;

import javax.validation.Valid;

public class UserSharedOrganizationsResponseLinks  {
  
    private String href;
    private String rel;

    /**
    * URL to navigate to the next or previous page.
    **/
    public UserSharedOrganizationsResponseLinks href(String href) {

        this.href = href;
        return this;
    }
    
    @ApiModelProperty(value = "URL to navigate to the next or previous page.")
    @JsonProperty("href")
    @Valid
    public String getHref() {
        return href;
    }
    public void setHref(String href) {
        this.href = href;
    }

    /**
    * Indicates if the link is for the \&quot;next\&quot; or \&quot;previous\&quot; page.
    **/
    public UserSharedOrganizationsResponseLinks rel(String rel) {

        this.rel = rel;
        return this;
    }
    
    @ApiModelProperty(value = "Indicates if the link is for the \"next\" or \"previous\" page.")
    @JsonProperty("rel")
    @Valid
    public String getRel() {
        return rel;
    }
    public void setRel(String rel) {
        this.rel = rel;
    }



    @Override
    public boolean equals(Object o) {

        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }
        UserSharedOrganizationsResponseLinks userSharedOrganizationsResponseLinks = (UserSharedOrganizationsResponseLinks) o;
        return Objects.equals(this.href, userSharedOrganizationsResponseLinks.href) &&
            Objects.equals(this.rel, userSharedOrganizationsResponseLinks.rel);
    }

    @Override
    public int hashCode() {
        return Objects.hash(href, rel);
    }

    @Override
    public String toString() {

        StringBuilder sb = new StringBuilder();
        sb.append("class UserSharedOrganizationsResponseLinks {\n");
        
        sb.append("    href: ").append(toIndentedString(href)).append("\n");
        sb.append("    rel: ").append(toIndentedString(rel)).append("\n");
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


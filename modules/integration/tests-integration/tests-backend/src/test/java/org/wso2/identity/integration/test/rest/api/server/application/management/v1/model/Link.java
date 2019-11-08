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

public class Link {
  
    private String href;
    private String rel;

    /**
    **/
    public Link href(String href) {

        this.href = href;
        return this;
    }
    
    @ApiModelProperty(example = "applications?offset=10&limit=10", value = "")
    @JsonProperty("href")
    @Valid
    public String getHref() {
        return href;
    }
    public void setHref(String href) {
        this.href = href;
    }

    /**
    **/
    public Link rel(String rel) {

        this.rel = rel;
        return this;
    }
    
    @ApiModelProperty(example = "next", value = "")
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
        Link link = (Link) o;
        return Objects.equals(this.href, link.href) &&
            Objects.equals(this.rel, link.rel);
    }

    @Override
    public int hashCode() {
        return Objects.hash(href, rel);
    }

    @Override
    public String toString() {

        StringBuilder sb = new StringBuilder();
        sb.append("class Link {\n");

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


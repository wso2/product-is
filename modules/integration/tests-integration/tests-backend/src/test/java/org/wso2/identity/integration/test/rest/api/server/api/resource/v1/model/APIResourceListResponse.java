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

package org.wso2.identity.integration.test.rest.api.server.api.resource.v1.model;

import com.fasterxml.jackson.annotation.JsonProperty;
import io.swagger.annotations.ApiModelProperty;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

import javax.validation.Valid;
import javax.validation.constraints.NotNull;

public class APIResourceListResponse {

    private Integer totalResults;
    private List<PaginationLink> links = new ArrayList<PaginationLink>();

    private List<APIResourceListItem> apiResources = null;


    /**
     **/
    public APIResourceListResponse totalResults(Integer totalResults) {

        this.totalResults = totalResults;
        return this;
    }

    @ApiModelProperty(example = "1", value = "")
    @JsonProperty("totalResults")
    @Valid
    public Integer getTotalResults() {
        return totalResults;
    }
    public void setTotalResults(Integer totalResults) {
        this.totalResults = totalResults;
    }

    /**
     **/
    public APIResourceListResponse links(List<PaginationLink> links) {

        this.links = links;
        return this;
    }

    @ApiModelProperty(required = true, value = "")
    @JsonProperty("links")
    @Valid
    @NotNull(message = "Property links cannot be null.")

    public List<PaginationLink> getLinks() {
        return links;
    }
    public void setLinks(List<PaginationLink> links) {
        this.links = links;
    }

    public APIResourceListResponse addLinksItem(PaginationLink linksItem) {
        this.links.add(linksItem);
        return this;
    }

    /**
     **/
    public APIResourceListResponse apiResources(List<APIResourceListItem> apiResources) {

        this.apiResources = apiResources;
        return this;
    }

    @ApiModelProperty(value = "")
    @JsonProperty("apiResources")
    @Valid
    public List<APIResourceListItem> getApiResources() {
        return apiResources;
    }
    public void setApiResources(List<APIResourceListItem> apiResources) {
        this.apiResources = apiResources;
    }

    public APIResourceListResponse addApiResourcesItem(APIResourceListItem apiResourcesItem) {
        if (this.apiResources == null) {
            this.apiResources = new ArrayList<APIResourceListItem>();
        }
        this.apiResources.add(apiResourcesItem);
        return this;
    }



    @Override
    public boolean equals(java.lang.Object o) {

        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }
        APIResourceListResponse apIResourceListResponse = (APIResourceListResponse) o;
        return Objects.equals(this.totalResults, apIResourceListResponse.totalResults) &&
                Objects.equals(this.links, apIResourceListResponse.links) &&
                Objects.equals(this.apiResources, apIResourceListResponse.apiResources);
    }

    @Override
    public int hashCode() {
        return Objects.hash(totalResults, links, apiResources);
    }

    @Override
    public String toString() {

        StringBuilder sb = new StringBuilder();
        sb.append("class APIResourceListResponse {\n");

        sb.append("    totalResults: ").append(toIndentedString(totalResults)).append("\n");
        sb.append("    links: ").append(toIndentedString(links)).append("\n");
        sb.append("    apiResources: ").append(toIndentedString(apiResources)).append("\n");
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

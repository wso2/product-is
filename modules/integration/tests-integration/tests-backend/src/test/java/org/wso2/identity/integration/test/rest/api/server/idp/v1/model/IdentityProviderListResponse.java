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

package org.wso2.identity.integration.test.rest.api.server.idp.v1.model;

import com.fasterxml.jackson.annotation.JsonProperty;
import io.swagger.annotations.ApiModelProperty;

import javax.validation.Valid;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;


public class IdentityProviderListResponse   {
  
  private Integer totalResults;

  private Integer startIndex;

  private Integer count;

  private List<Link> links = null;

  private List<IdentityProviderListItem> identityProviders = null;


  /**
   **/
  public IdentityProviderListResponse totalResults(Integer totalResults) {
    this.totalResults = totalResults;
    return this;
  }

  
  @ApiModelProperty(example = "10", value = "")
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
  public IdentityProviderListResponse startIndex(Integer startIndex) {
    this.startIndex = startIndex;
    return this;
  }

  
  @ApiModelProperty(example = "1", value = "")
  @JsonProperty("startIndex")
  @Valid
  public Integer getStartIndex() {
    return startIndex;
  }
  public void setStartIndex(Integer startIndex) {
    this.startIndex = startIndex;
  }


  /**
   **/
  public IdentityProviderListResponse count(Integer count) {
    this.count = count;
    return this;
  }

  
  @ApiModelProperty(example = "10", value = "")
  @JsonProperty("count")
  @Valid
  public Integer getCount() {
    return count;
  }
  public void setCount(Integer count) {
    this.count = count;
  }


  /**
   **/
  public IdentityProviderListResponse links(List<Link> links) {
    this.links = links;
    return this;
  }

  
  @ApiModelProperty(example = "[{\"href\":\"identity-provider?offset=50&limit=10\",\"rel\":\"next\"},{\"href\":\"identity-provider?offset=30&limit=10\",\"rel\":\"previous\"},{\"href\":\"identity-provider?offset=0&limit=10\",\"rel\":\"first\"},{\"href\":\"identity-provider?offset=100&limit=10\",\"rel\":\"last\"}]", value = "")
  @JsonProperty("links")
  @Valid
  public List<Link> getLinks() {
    return links;
  }
  public void setLinks(List<Link> links) {
    this.links = links;
  }

  public IdentityProviderListResponse addLinksItem(Link linksItem) {
    if (this.links == null) {
      this.links = new ArrayList<>();
    }
    this.links.add(linksItem);
    return this;
  }


  /**
   **/
  public IdentityProviderListResponse identityProviders(List<IdentityProviderListItem> identityProviders) {
    this.identityProviders = identityProviders;
    return this;
  }

  
  @ApiModelProperty(value = "")
  @JsonProperty("identityProviders")
  @Valid
  public List<IdentityProviderListItem> getIdentityProviders() {
    return identityProviders;
  }
  public void setIdentityProviders(List<IdentityProviderListItem> identityProviders) {
    this.identityProviders = identityProviders;
  }

  public IdentityProviderListResponse addIdentityProvidersItem(IdentityProviderListItem identityProvidersItem) {
    if (this.identityProviders == null) {
      this.identityProviders = new ArrayList<>();
    }
    this.identityProviders.add(identityProvidersItem);
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
    IdentityProviderListResponse identityProviderListResponse = (IdentityProviderListResponse) o;
    return Objects.equals(this.totalResults, identityProviderListResponse.totalResults) &&
        Objects.equals(this.startIndex, identityProviderListResponse.startIndex) &&
        Objects.equals(this.count, identityProviderListResponse.count) &&
        Objects.equals(this.links, identityProviderListResponse.links) &&
        Objects.equals(this.identityProviders, identityProviderListResponse.identityProviders);
  }

  @Override
  public int hashCode() {
    return Objects.hash(totalResults, startIndex, count, links, identityProviders);
  }

  @Override
  public String toString() {
    StringBuilder sb = new StringBuilder();
    sb.append("class IdentityProviderListResponse {\n");

    sb.append("    totalResults: ").append(toIndentedString(totalResults)).append("\n");
    sb.append("    startIndex: ").append(toIndentedString(startIndex)).append("\n");
    sb.append("    count: ").append(toIndentedString(count)).append("\n");
    sb.append("    links: ").append(toIndentedString(links)).append("\n");
    sb.append("    identityProviders: ").append(toIndentedString(identityProviders)).append("\n");
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
    return o.toString().replace("\n", "\n    ");
  }
}


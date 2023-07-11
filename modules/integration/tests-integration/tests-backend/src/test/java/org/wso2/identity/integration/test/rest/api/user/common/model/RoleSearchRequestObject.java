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

package org.wso2.identity.integration.test.rest.api.user.common.model;

import com.fasterxml.jackson.annotation.JsonProperty;
import io.swagger.annotations.ApiModelProperty;

import javax.validation.Valid;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

public class RoleSearchRequestObject {

    private List<String> schemas = null;

    private Integer startIndex;
    private Integer count;
    private String filter;

    /**
     *
     **/
    public RoleSearchRequestObject schemas(List<String> schemas) {

        this.schemas = schemas;
        return this;
    }

    @ApiModelProperty()
    @JsonProperty("schemas")
    @Valid
    public List<String> getSchemas() {
        return schemas;
    }

    public void setSchemas(List<String> schemas) {
        this.schemas = schemas;
    }

    public RoleSearchRequestObject addSchemas(String schema) {
        if (this.schemas == null) {
            this.schemas = new ArrayList<>();
        }
        this.schemas.add(schema);
        return this;
    }

    /**
     *
     **/
    public RoleSearchRequestObject startIndex(Integer startIndex) {

        this.startIndex = startIndex;
        return this;
    }

    @ApiModelProperty()
    @JsonProperty("startIndex")
    @Valid
    public Integer getStartIndex() {
        return startIndex;
    }

    public void setStartIndex(Integer startIndex) {
        this.startIndex = startIndex;
    }

    /**
     *
     **/
    public RoleSearchRequestObject count(Integer count) {

        this.count = count;
        return this;
    }

    @ApiModelProperty()
    @JsonProperty("count")
    @Valid
    public Integer getCount() {
        return count;
    }

    public void setCount(Integer count) {
        this.count = count;
    }

    /**
     *
     **/
    public RoleSearchRequestObject filter(String filter) {

        this.filter = filter;
        return this;
    }

    @ApiModelProperty(example = "abc")
    @JsonProperty("filter")
    @Valid
    public String getFilter() {
        return filter;
    }

    public void setFilter(String filter) {
        this.filter = filter;
    }


    @Override
    public boolean equals(Object o) {

        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }
        RoleSearchRequestObject RoleSearchRequestObject = (RoleSearchRequestObject) o;
        return Objects.equals(this.schemas, RoleSearchRequestObject.schemas) &&
                Objects.equals(this.filter, RoleSearchRequestObject.filter) &&
                Objects.equals(this.startIndex, RoleSearchRequestObject.startIndex) &&
                Objects.equals(this.count, RoleSearchRequestObject.count);
    }

    @Override
    public int hashCode() {
        return Objects.hash(schemas, startIndex, count, filter);
    }

    @Override
    public String toString() {

        return "class RoleSearchRequestObject {\n" +
                "    schemas: " + toIndentedString(schemas) + "\n" +
                "    startIndex: " + toIndentedString(startIndex) + "\n" +
                "    count: " + toIndentedString(count) + "\n" +
                "    filter: " + toIndentedString(filter) + "\n" +
                "}";
    }

    /**
     * Convert the given object to string with each line indented by 4 spaces
     * (except the first line).
     */
    private String toIndentedString(java.lang.Object o) {

        if (o == null) {
            return "null";
        }
        return o.toString();
    }
}

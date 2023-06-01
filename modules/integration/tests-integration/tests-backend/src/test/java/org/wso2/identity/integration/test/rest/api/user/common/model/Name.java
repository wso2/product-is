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
import java.util.Objects;

public class Name {

    private String givenName;
    private String familyName;

    /**
     *
     **/
    public Name givenName(String givenName) {

        this.givenName = givenName;
        return this;
    }

    @ApiModelProperty(example = "Ashan")
    @JsonProperty("givenName")
    @Valid
    public String getGivenName() {
        return givenName;
    }

    public void setGivenName(String givenName) {
        this.givenName = givenName;
    }

    /**
     *
     **/
    public Name familyName(String familyName) {

        this.familyName = familyName;
        return this;
    }

    @ApiModelProperty(example = "Zoyza")
    @JsonProperty("familyName")
    @Valid
    public String getFamilyName() {
        return familyName;
    }

    public void setFamilyName(String familyName) {
        this.familyName = familyName;
    }


    @Override
    public boolean equals(Object o) {

        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }
        Name name = (Name) o;
        return Objects.equals(this.givenName, name.givenName) &&
                Objects.equals(this.familyName, name.familyName);
    }

    @Override
    public int hashCode() {
        return Objects.hash(givenName, familyName);
    }

    @Override
    public String toString() {

        return "class Name {\n" +
                "    givenName: " + toIndentedString(givenName) + "\n" +
                "    familyName: " + toIndentedString(familyName) + "\n" +
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
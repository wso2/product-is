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

public class GroupRequestObject {

    private List<String> schemas = null;
    private String displayName;
    private List<MemberItem> members = new ArrayList<>();

    /**
     *
     **/
    public GroupRequestObject schemas(List<String> schemas) {

        this.schemas = schemas;
        return this;
    }

    @ApiModelProperty(example = "abc")
    @JsonProperty("schemas")
    @Valid
    public List<String> getDisplay() {
        return schemas;
    }

    public void setDisplay(List<String> schemas) {
        this.schemas = schemas;
    }

    /**
     *
     **/
    public GroupRequestObject displayName(String displayName) {

        this.displayName = displayName;
        return this;
    }

    @ApiModelProperty(example = "testGroup")
    @JsonProperty("displayName")
    @Valid
    public String getValue() {
        return displayName;
    }

    public void setValue(String displayName) {
        this.displayName = displayName;
    }

    /**
     *
     **/
    public GroupRequestObject members(List<MemberItem> members) {

        this.members = members;
        return this;
    }

    @ApiModelProperty()
    @JsonProperty("members")
    @Valid
    public List<MemberItem> getMembers() {
        return members;
    }

    public void setMembers(List<MemberItem> members) {
        this.members = members;
    }

    public GroupRequestObject addMember(MemberItem member) {
        this.members.add(member);
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
        GroupRequestObject listObj = (GroupRequestObject) o;
        return Objects.equals(this.schemas, listObj.schemas) &&
                Objects.equals(this.displayName, listObj.displayName) &&
                Objects.equals(this.members, listObj.members);
    }

    @Override
    public int hashCode() {
        return Objects.hash(schemas, displayName, members);
    }

    @Override
    public String toString() {

        return "class GroupRequestObject {\n" +
                "    schemas: " + toIndentedString(schemas) + "\n" +
                "    displayName: " + toIndentedString(displayName) + "\n" +
                "    members: " + toIndentedString(members) + "\n" +
                "}";
    }

    /**
     * Convert the given object to string with each line indented by 4 spaces
     * (except the first line).
     */
    private static String toIndentedString(java.lang.Object o) {

        if (o == null) {
            return "null";
        }
        return o.toString();
    }
    
    public static class MemberItem {
        private String value;
        private String display;

        /**
         *
         **/
        public MemberItem value(String value) {

            this.value = value;
            return this;
        }

        @ApiModelProperty(example = "Ashan")
        @JsonProperty("value")
        @Valid
        public String getValue() {
            return value;
        }

        public void setValue(String value) {
            this.value = value;
        }

        /**
         *
         **/
        public MemberItem display(String display) {

            this.display = display;
            return this;
        }

        @ApiModelProperty()
        @JsonProperty("display")
        @Valid
        public String getDisplay() {
            return display;
        }

        public void setDisplay(String display) {
            this.display = display;
        }


        @Override
        public boolean equals(Object o) {

            if (this == o) {
                return true;
            }
            if (o == null || getClass() != o.getClass()) {
                return false;
            }
            MemberItem memberItem = (MemberItem) o;
            return Objects.equals(this.value, memberItem.value) &&
                    Objects.equals(this.display, memberItem.display);
        }

        @Override
        public int hashCode() {
            return Objects.hash(value, display);
        }

        @Override
        public String toString() {

            return "class MemberItem {\n" +
                    "    value: " + toIndentedString(value) + "\n" +
                    "    display: " + toIndentedString(display) + "\n" +
                    "}";
        }
    }
}

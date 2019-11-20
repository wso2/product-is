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

package org.wso2.identity.integration.test.rest.api.server.user.store.v1.model;

import java.util.Objects;

public class AvailableUserStoreClassesRes {

    private String typeId;
    private String typeName;
    private String className;

    public AvailableUserStoreClassesRes typeId(String typeId) {

        this.typeId = typeId;
        return this;
    }

    public String getTypeId() {

        return typeId;
    }

    public void setTypeId(String typeId) {

        this.typeId = typeId;
    }

    public AvailableUserStoreClassesRes typeName(String typeName) {

        this.typeName = typeName;
        return this;
    }

    public String getTypeName() {

        return typeName;
    }

    public void setTypeName(String typeName) {

        this.typeName = typeName;
    }

    public AvailableUserStoreClassesRes className(String className) {

        this.className = className;
        return this;
    }

    public String getClassName() {
        return className;
    }

    public void setClassName(String className) {
        this.className = className;
    }


    @Override
    public boolean equals(Object o) {

        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }
        AvailableUserStoreClassesRes availableUserStoreClassesRes = (AvailableUserStoreClassesRes) o;
        return Objects.equals(this.typeId, availableUserStoreClassesRes.typeId) &&
                Objects.equals(this.typeName, availableUserStoreClassesRes.typeName) &&
                Objects.equals(this.className, availableUserStoreClassesRes.className);
    }

    @Override
    public int hashCode() {
        return Objects.hash(typeId, typeName, className);
    }

    @Override
    public String toString() {

        StringBuilder sb = new StringBuilder();
        sb.append("class AvailableUserStoreClassesRes {\n");

        sb.append("    typeId: ").append(toIndentedString(typeId)).append("\n");
        sb.append("    typeName: ").append(toIndentedString(typeName)).append("\n");
        sb.append("    className: ").append(toIndentedString(className)).append("\n");
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

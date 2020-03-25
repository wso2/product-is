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
    private String self;

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

    public AvailableUserStoreClassesRes self(String self) {

        this.self = self;
        return this;
    }

    public String getSelf() {
        return self;
    }

    public void setSelf(String self) {
        this.self = self;
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
                Objects.equals(this.className, availableUserStoreClassesRes.className) &&
                Objects.equals(this.self, availableUserStoreClassesRes.self);
    }

    @Override
    public int hashCode() {

        return Objects.hash(typeId, typeName, className, self);
    }
}

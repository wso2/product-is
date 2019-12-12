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

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

public class UserStoreConfigurationsRes {

    private String typeName;
    private String typeId;
    private String name;
    private String description;
    private String className;
    private List<AddUserStorePropertiesRes> properties = null;

    public UserStoreConfigurationsRes typeName(String typeName) {

        this.typeName = typeName;
        return this;
    }

    public String getTypeName() {

        return typeName;
    }

    public void setTypeName(String typeName) {

        this.typeName = typeName;
    }

    public UserStoreConfigurationsRes typeId(String typeId) {

        this.typeId = typeId;
        return this;
    }

    public String getTypeId() {

        return typeId;
    }

    public void setTypeId(String typeId) {

        this.typeId = typeId;
    }

    public UserStoreConfigurationsRes name(String name) {

        this.name = name;
        return this;
    }

    public String getName() {

        return name;
    }

    public void setName(String name) {

        this.name = name;
    }

    public UserStoreConfigurationsRes description(String description) {

        this.description = description;
        return this;
    }

    public String getDescription() {

        return description;
    }

    public void setDescription(String description) {

        this.description = description;
    }

    public UserStoreConfigurationsRes className(String className) {

        this.className = className;
        return this;
    }

    public String getClassName() {

        return className;
    }

    public void setClassName(String className) {

        this.className = className;
    }

    /**
     * Configured user store property for the set
     **/
    public UserStoreConfigurationsRes properties(List<AddUserStorePropertiesRes> properties) {

        this.properties = properties;
        return this;
    }

    public List<AddUserStorePropertiesRes> getProperties() {

        return properties;
    }

    public void setProperties(List<AddUserStorePropertiesRes> properties) {

        this.properties = properties;
    }

    public UserStoreConfigurationsRes addPropertiesItem(AddUserStorePropertiesRes propertiesItem) {

        if (this.properties == null) {
            this.properties = new ArrayList<>();
        }
        this.properties.add(propertiesItem);
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
        UserStoreConfigurationsRes userStoreConfigurationsRes = (UserStoreConfigurationsRes) o;
        return Objects.equals(this.typeName, userStoreConfigurationsRes.typeName) &&
                Objects.equals(this.typeId, userStoreConfigurationsRes.typeId) &&
                Objects.equals(this.name, userStoreConfigurationsRes.name) &&
                Objects.equals(this.description, userStoreConfigurationsRes.description) &&
                Objects.equals(this.className, userStoreConfigurationsRes.className) &&
                Objects.equals(this.properties, userStoreConfigurationsRes.properties);
    }

    @Override
    public int hashCode() {

        return Objects.hash(typeName, typeId, name, description, className, properties);
    }
}

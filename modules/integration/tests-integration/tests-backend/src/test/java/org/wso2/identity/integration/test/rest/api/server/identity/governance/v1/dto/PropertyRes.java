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

package org.wso2.identity.integration.test.rest.api.server.identity.governance.v1.dto;

import java.util.Objects;

/**
 * Governance connector property.
 **/
public class PropertyRes {

    private String name;
    private String value;
    private String displayName;
    private String description;

    /**
     * Property name.
     **/
    public PropertyRes name(String name) {

        this.name = name;
        return this;
    }

    public String getName() {

        return name;
    }

    public void setName(String name) {

        this.name = name;
    }

    /**
     * Property value.
     **/
    public PropertyRes value(String value) {

        this.value = value;
        return this;
    }

    public String getValue() {

        return value;
    }

    public void setValue(String value) {

        this.value = value;
    }

    /**
     * Property display name.
     **/
    public PropertyRes displayName(String displayName) {

        this.displayName = displayName;
        return this;
    }

    public String getDisplayName() {

        return displayName;
    }

    public void setDisplayName(String displayName) {

        this.displayName = displayName;
    }

    /**
     * Property description.
     **/
    public PropertyRes description(String description) {

        this.description = description;
        return this;
    }

    public String getDescription() {

        return description;
    }

    public void setDescription(String description) {

        this.description = description;
    }

    @Override
    public boolean equals(Object o) {

        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }
        PropertyRes propertyRes = (PropertyRes) o;
        return Objects.equals(this.name, propertyRes.name) &&
                Objects.equals(this.value, propertyRes.value) &&
                Objects.equals(this.displayName, propertyRes.displayName) &&
                Objects.equals(this.description, propertyRes.description);
    }

    @Override
    public int hashCode() {

        return Objects.hash(name, value, displayName, description);
    }
}


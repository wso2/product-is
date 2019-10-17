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

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

/**
 * Governance connector response.
 **/
public class ConnectorRes {

    private String id;
    private String name;
    private String category;
    private String friendlyName;
    private Integer order;
    private String subCategory;
    private List<PropertyRes> properties = null;

    /**
     * Connector id.
     **/
    public ConnectorRes id(String id) {

        this.id = id;
        return this;
    }

    public String getId() {

        return id;
    }

    public void setId(String id) {

        this.id = id;
    }

    /**
     * Connector name.
     **/
    public ConnectorRes name(String name) {

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
     * Connector category.
     **/
    public ConnectorRes category(String category) {

        this.category = category;
        return this;
    }

    public String getCategory() {

        return category;
    }

    public void setCategory(String category) {

        this.category = category;
    }

    /**
     * Connector friendly name.
     **/
    public ConnectorRes friendlyName(String friendlyName) {

        this.friendlyName = friendlyName;
        return this;
    }

    public String getFriendlyName() {

        return friendlyName;
    }

    public void setFriendlyName(String friendlyName) {

        this.friendlyName = friendlyName;
    }

    /**
     * Connector order.
     **/
    public ConnectorRes order(Integer order) {

        this.order = order;
        return this;
    }

    public Integer getOrder() {

        return order;
    }

    public void setOrder(Integer order) {

        this.order = order;
    }

    /**
     * Connector subcategory.
     **/
    public ConnectorRes subCategory(String subCategory) {

        this.subCategory = subCategory;
        return this;
    }

    public String getSubCategory() {

        return subCategory;
    }

    public void setSubCategory(String subCategory) {

        this.subCategory = subCategory;
    }

    /**
     * Define any additional properties if required.
     **/
    public ConnectorRes properties(List<PropertyRes> properties) {

        this.properties = properties;
        return this;
    }

    public List<PropertyRes> getProperties() {

        return properties;
    }

    public void setProperties(List<PropertyRes> properties) {

        this.properties = properties;
    }

    public ConnectorRes addPropertiesItem(PropertyRes propertiesItem) {

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
        ConnectorRes connectorRes = (ConnectorRes) o;
        return Objects.equals(this.id, connectorRes.id) &&
                Objects.equals(this.name, connectorRes.name) &&
                Objects.equals(this.category, connectorRes.category) &&
                Objects.equals(this.friendlyName, connectorRes.friendlyName) &&
                Objects.equals(this.order, connectorRes.order) &&
                Objects.equals(this.subCategory, connectorRes.subCategory) &&
                Objects.equals(this.properties, connectorRes.properties);
    }

    @Override
    public int hashCode() {

        return Objects.hash(id, name, category, friendlyName, order, subCategory, properties);
    }

}


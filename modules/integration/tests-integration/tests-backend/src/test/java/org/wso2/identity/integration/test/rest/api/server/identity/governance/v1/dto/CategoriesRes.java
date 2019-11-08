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
 * Governance connector category response.
 **/
public class CategoriesRes {

    private String id;
    private String name;
    private String self;
    private List<CategoryConnectorsRes> connectors = null;

    /**
     * Connector category id.
     **/
    public CategoriesRes id(String id) {

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
     * Connector category name.
     **/
    public CategoriesRes name(String name) {

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
     * Path retrieve the full connector information.
     **/
    public CategoriesRes self(String self) {

        this.self = self;
        return this;
    }

    public String getSelf() {

        return self;
    }

    public void setSelf(String self) {

        this.self = self;
    }

    /**
     * Connectors of the category with minimal attributes.
     **/
    public CategoriesRes connectors(List<CategoryConnectorsRes> connectors) {

        this.connectors = connectors;
        return this;
    }

    public List<CategoryConnectorsRes> getConnectors() {

        return connectors;
    }

    public void setConnectors(List<CategoryConnectorsRes> connectors) {

        this.connectors = connectors;
    }

    public CategoriesRes addConnectorsItem(CategoryConnectorsRes connectorsItem) {

        if (this.connectors == null) {
            this.connectors = new ArrayList<>();
        }
        this.connectors.add(connectorsItem);
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
        CategoriesRes categoriesRes = (CategoriesRes) o;
        return Objects.equals(this.id, categoriesRes.id) &&
                Objects.equals(this.name, categoriesRes.name) &&
                Objects.equals(this.self, categoriesRes.self) &&
                Objects.equals(this.connectors, categoriesRes.connectors);
    }

    @Override
    public int hashCode() {

        return Objects.hash(id, name, self, connectors);
    }
}


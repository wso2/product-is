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
public class CategoryRes {

    private String name;
    private List<ConnectorRes> connectors = null;

    /**
     * Connector category name.
     **/
    public CategoryRes name(String name) {

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
     * Connectors of the category with minimal attributes.
     **/
    public CategoryRes connectors(List<ConnectorRes> connectors) {

        this.connectors = connectors;
        return this;
    }

    public List<ConnectorRes> getConnectors() {

        return connectors;
    }

    public void setConnectors(List<ConnectorRes> connectors) {

        this.connectors = connectors;
    }

    public CategoryRes addConnectorsItem(ConnectorRes connectorsItem) {

        if (this.connectors == null) {
            this.connectors = new ArrayList<>();
        }
        this.connectors.add(connectorsItem);
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
        CategoryRes categoryRes = (CategoryRes) o;
        return Objects.equals(this.name, categoryRes.name) &&
                Objects.equals(this.connectors, categoryRes.connectors);
    }

    @Override
    public int hashCode() {

        return Objects.hash(name, connectors);
    }
}


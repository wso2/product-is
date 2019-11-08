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
 * Governance connector response with minimal attributes.
 **/

public class CategoryConnectorsRes {

    private String id;
    private String self;

    /**
     * Connector id.
     **/
    public CategoryConnectorsRes id(String id) {

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
     * Path to retrieve the full connector information.
     **/
    public CategoryConnectorsRes self(String self) {

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
    public boolean equals(java.lang.Object o) {

        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }
        CategoryConnectorsRes categoryConnectorsRes = (CategoryConnectorsRes) o;
        return Objects.equals(this.id, categoryConnectorsRes.id) &&
                Objects.equals(this.self, categoryConnectorsRes.self);
    }

    @Override
    public int hashCode() {

        return Objects.hash(id, self);
    }

}


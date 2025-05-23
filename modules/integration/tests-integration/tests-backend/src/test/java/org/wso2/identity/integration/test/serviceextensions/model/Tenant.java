/*
 * Copyright (c) 2024, WSO2 LLC. (http://www.wso2.com).
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

package org.wso2.identity.integration.test.serviceextensions.model;

import java.util.Objects;

/**
 * This class models the Tenant sent in the request payload to the API endpoint of a particular action.
 */
public class Tenant {

    private  String id;
    private  String name;

    public Tenant(String id, String name) {

        this.id = id;
        this.name = name;
    }

    public Tenant() {

    }

    public String getId() {

        return id;
    }

    public String getName() {

        return name;
    }

    public void setId(String id) {

        this.id = id;
    }

    public void setName(String name) {

        this.name = name;
    }

    @Override
    public boolean equals(Object o) {

        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Tenant tenant = (Tenant) o;
        return Objects.equals(id, tenant.id) && Objects.equals(name, tenant.name);
    }

    @Override
    public int hashCode() {

        return Objects.hash(id, name);
    }
}

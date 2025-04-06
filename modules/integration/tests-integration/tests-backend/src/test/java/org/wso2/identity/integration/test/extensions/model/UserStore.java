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

package org.wso2.identity.integration.test.extensions.model;

import java.util.Objects;

/**
 * This class models the UserStore sent in the request payload to the API endpoint of a particular action.
 */
public class UserStore {

    private String id;
    private String name;

    public UserStore(String id, String name) {

        this.id = id;
        this.name = name;
    }

    public UserStore() {

    }

    public String getId() {

        return id;
    }

    public void setId(String id) {

        this.id = id;
    }

    public String getName() {

        return name;
    }

    public void setName(String name) {

        this.name = name;
    }

    @Override
    public boolean equals(Object o) {

        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        UserStore userStore = (UserStore) o;
        return Objects.equals(id, userStore.id) && Objects.equals(name, userStore.name);
    }

    @Override
    public int hashCode() {

        return Objects.hash(id, name);
    }
}

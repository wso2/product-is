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

public class UserStoreListResponse {

    private String id;
    private String name;
    private String description;
    private String self;

    public UserStoreListResponse id(String id) {

        this.id = id;
        return this;
    }

    public String getId() {

        return id;
    }

    public void setId(String id) {

        this.id = id;
    }

    public UserStoreListResponse name(String name) {

        this.name = name;
        return this;
    }

    public String getName() {

        return name;
    }

    public void setName(String name) {

        this.name = name;
    }

    public UserStoreListResponse description(String description) {

        this.description = description;
        return this;
    }

    public String getDescription() {

        return description;
    }

    public void setDescription(String description) {

        this.description = description;
    }

    public UserStoreListResponse self(String self) {

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
        UserStoreListResponse userStoreListResponse = (UserStoreListResponse) o;
        return Objects.equals(this.id, userStoreListResponse.id) &&
                Objects.equals(this.name, userStoreListResponse.name) &&
                Objects.equals(this.description, userStoreListResponse.description) &&
                Objects.equals(this.self, userStoreListResponse.self);
    }

    @Override
    public int hashCode() {

        return Objects.hash(id, name, description, self);
    }
}

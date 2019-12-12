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

public class AddUserStorePropertiesRes {

    private String name;
    private String value;

    public AddUserStorePropertiesRes name(String name) {

        this.name = name;
        return this;
    }

    public String getName() {

        return name;
    }

    public void setName(String name) {

        this.name = name;
    }

    public AddUserStorePropertiesRes value(String value) {

        this.value = value;
        return this;
    }

    public String getValue() {

        return value;
    }

    public void setValue(String value) {

        this.value = value;
    }

    @Override
    public boolean equals(Object o) {

        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }
        AddUserStorePropertiesRes addUserStorePropertiesRes = (AddUserStorePropertiesRes) o;
        return Objects.equals(this.name, addUserStorePropertiesRes.name) &&
                Objects.equals(this.value, addUserStorePropertiesRes.value);
    }

    @Override
    public int hashCode() {

        return Objects.hash(name, value);
    }
}

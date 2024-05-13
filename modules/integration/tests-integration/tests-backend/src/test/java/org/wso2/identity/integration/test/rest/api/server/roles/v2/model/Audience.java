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

package org.wso2.identity.integration.test.rest.api.server.roles.v2.model;

/**
 * Represents the audience of a role.
 * The audience specifies the intended recipient of the role, which could be an application or organization.
 * It consists of a type and a value, where the type describes the audience category (e.g., APPLICATION or ORGANIZATION)
 * and the value represents the unique identifier of the audience.
 */
public class Audience {
    private String type;
    private String value;

    // Constructors, getters, and setters

    public Audience() {
    }

    public Audience(String type, String value) {
        this.type = type;
        this.value = value;
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public String getValue() {
        return value;
    }

    public void setValue(String value) {
        this.value = value;
    }

    @Override
    public String toString() {
        return "Audience{" +
                "type='" + type + '\'' +
                ", value='" + value + '\'' +
                '}';
    }}

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

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonValue;

/**
 * This class models the Operation types allowed.
 */
public enum Operation {

    ADD("add"),
    REMOVE("remove"),
    REPLACE("replace"),
    REDIRECT("redirect");

    private final String value;

    Operation(String value) {

        this.value = value;
    }

    @JsonCreator
    public static Operation forValue(String value) {

        for (Operation op : Operation.values()) {
            if (op.getValue().equals(value)) {
                return op;
            }
        }
        throw new IllegalArgumentException("Invalid operation value: " + value);
    }

    @JsonValue
    public String getValue() {

        return value;
    }
}


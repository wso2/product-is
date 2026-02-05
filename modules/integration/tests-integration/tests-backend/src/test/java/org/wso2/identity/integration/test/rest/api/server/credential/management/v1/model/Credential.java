/*
 * Copyright (c) 2025, WSO2 LLC. (http://www.wso2.com).
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

package org.wso2.identity.integration.test.rest.api.server.credential.management.v1.model;

import com.fasterxml.jackson.annotation.JsonProperty;

import java.util.Objects;

/**
 * Model class for a single credential.
 */
public class Credential {

    @JsonProperty("credentialId")
    private String credentialId;

    @JsonProperty("type")
    private String type;

    @JsonProperty("displayName")
    private String displayName;

    public String getCredentialId() {

        return credentialId;
    }

    public void setCredentialId(String credentialId) {

        this.credentialId = credentialId;
    }

    public String getType() {

        return type;
    }

    public void setType(String type) {

        this.type = type;
    }

    public String getDisplayName() {

        return displayName;
    }

    public void setDisplayName(String displayName) {

        this.displayName = displayName;
    }

    @Override
    public boolean equals(Object o) {

        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Credential that = (Credential) o;
        return Objects.equals(credentialId, that.credentialId) &&
                Objects.equals(type, that.type) &&
                Objects.equals(displayName, that.displayName);
    }

    @Override
    public int hashCode() {

        return Objects.hash(credentialId, type, displayName);
    }
}

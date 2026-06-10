/*
 * Copyright (c) 2026, WSO2 LLC. (http://www.wso2.com).
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

package org.wso2.identity.integration.test.rest.api.user.credential.management.v2.model;

import com.fasterxml.jackson.annotation.JsonProperty;

import java.util.List;
import java.util.Objects;

/**
 * Model class for the v2 POST /credentials/{type} response (backup-code creation).
 * Contains the credential type and the list of generated backup codes.
 */
public class CredentialCreationResponse {

    @JsonProperty("type")
    private String type;

    @JsonProperty("credentials")
    private List<String> credentials;

    public String getType() {

        return type;
    }

    public void setType(String type) {

        this.type = type;
    }

    public List<String> getCredentials() {

        return credentials;
    }

    public void setCredentials(List<String> credentials) {

        this.credentials = credentials;
    }

    @Override
    public boolean equals(Object o) {

        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        CredentialCreationResponse that = (CredentialCreationResponse) o;
        return Objects.equals(type, that.type) &&
                Objects.equals(credentials, that.credentials);
    }

    @Override
    public int hashCode() {

        return Objects.hash(type, credentials);
    }
}

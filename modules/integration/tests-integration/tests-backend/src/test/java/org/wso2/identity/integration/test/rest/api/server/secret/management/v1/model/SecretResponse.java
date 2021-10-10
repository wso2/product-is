/*
 * Copyright (c) 2021, WSO2 Inc. (http://www.wso2.com).
 *
 * WSO2 Inc. licenses this file to you under the Apache License,
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

package org.wso2.identity.integration.test.rest.api.server.secret.management.v1.model;

/**
 * Class for the secret response object.
 */
public class SecretResponse  {

    private String secretId;
    private String secretName;
    private String created;
    private String lastModified;
    private String description;

    public SecretResponse secretId(String secretId) {

        this.secretId = secretId;
        return this;
    }

    public String getSecretId() {

        return secretId;
    }

    public void setSecretId(String secretId) {

        this.secretId = secretId;
    }

    public SecretResponse secretName(String secretName) {

        this.secretName = secretName;
        return this;
    }

    public String getSecretName() {

        return secretName;
    }

    public void setSecretName(String secretName) {

        this.secretName = secretName;
    }

    public String getCreated() {

        return created;
    }

    public void setCreated(String created) {

        this.created = created;
    }

    public String getLastModified() {

        return lastModified;
    }

    public void setLastModified(String lastModified) {

        this.lastModified = lastModified;
    }

    public String getDescription() {

        return description;
    }

    public void setDescription(String description) {

        this.description = description;
    }
}

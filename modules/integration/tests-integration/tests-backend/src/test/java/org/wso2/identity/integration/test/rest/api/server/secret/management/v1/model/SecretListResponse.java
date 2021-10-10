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

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

/**
 * Class for the secrets list response.
 */
public class SecretListResponse  {

    private List<SecretResponse> secrets = null;

    public SecretListResponse secrets(List<SecretResponse> secrets) {

        this.secrets = secrets;
        return this;
    }

    public List<SecretResponse> getSecrets() {

        return secrets;
    }

    public void setSecrets(List<SecretResponse> secrets) {

        this.secrets = secrets;
    }

    public SecretListResponse addSecretsItem(SecretResponse secretsItem) {

        if (this.secrets == null) {
            this.secrets = new ArrayList<>();
        }
        this.secrets.add(secretsItem);
        return this;
    }

    @Override
    public boolean equals(java.lang.Object o) {

        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }
        SecretListResponse secretListResponse = (SecretListResponse) o;
        return Objects.equals(this.secrets, secretListResponse.secrets);
    }

    @Override
    public int hashCode() {

        return Objects.hash(secrets);
    }

    @Override
    public String toString() {

        StringBuilder sb = new StringBuilder();
        sb.append("class SecretListResponse {\n");

        sb.append("    secrets: ").append(toIndentedString(secrets)).append("\n");
        sb.append("}");
        return sb.toString();
    }

    /**
     * Convert the given object to string with each line indented by 4 spaces.
     * (except the first line).
     */
    private String toIndentedString(java.lang.Object o) {

        if (o == null) {
            return "null";
        }
        return o.toString().replace("\n", "\n");
    }
}

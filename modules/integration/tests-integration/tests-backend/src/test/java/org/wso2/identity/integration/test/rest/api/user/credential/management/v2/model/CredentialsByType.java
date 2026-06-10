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

package org.wso2.identity.integration.test.rest.api.user.credential.management.v2.model;

import com.fasterxml.jackson.annotation.JsonProperty;

import java.util.List;
import java.util.Objects;

/**
 * Model class for the v2 GET /credentials response.
 * All fields are always present: passkey and push-auth as arrays, backup-code as a boolean.
 */
public class CredentialsByType {

    @JsonProperty("passkey")
    private List<CredentialEntry> passkey;

    @JsonProperty("push-auth")
    private List<CredentialEntry> pushAuth;

    @JsonProperty("backup-code")
    private Boolean backupCode;

    public List<CredentialEntry> getPasskey() {

        return passkey;
    }

    public void setPasskey(List<CredentialEntry> passkey) {

        this.passkey = passkey;
    }

    public List<CredentialEntry> getPushAuth() {

        return pushAuth;
    }

    public void setPushAuth(List<CredentialEntry> pushAuth) {

        this.pushAuth = pushAuth;
    }

    public Boolean getBackupCode() {

        return backupCode;
    }

    public void setBackupCode(Boolean backupCode) {

        this.backupCode = backupCode;
    }

    @Override
    public boolean equals(Object o) {

        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        CredentialsByType that = (CredentialsByType) o;
        return Objects.equals(passkey, that.passkey) &&
                Objects.equals(pushAuth, that.pushAuth) &&
                Objects.equals(backupCode, that.backupCode);
    }

    @Override
    public int hashCode() {

        return Objects.hash(passkey, pushAuth, backupCode);
    }
}

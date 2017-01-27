/*
 * Copyright (c) 2017, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
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

package org.wso2.is.portal.user.client.api.bean;

import org.wso2.carbon.identity.claim.mapping.profile.ClaimConfigEntry;

/**
 * Profile UI Entry.
 */
public class ProfileUIEntry {

    private ClaimConfigEntry claimConfigEntry;

    private String value;

    public ProfileUIEntry() {

    }

    public ProfileUIEntry(ClaimConfigEntry claimConfigEntry, String value) {

        this.claimConfigEntry = claimConfigEntry;
        this.value = value;
    }

    public ClaimConfigEntry getClaimConfigEntry() {
        return claimConfigEntry;
    }

    public void setClaimConfigEntry(ClaimConfigEntry claimConfigEntry) {
        this.claimConfigEntry = claimConfigEntry;
    }

    public String getValue() {
        return value;
    }

    public void setValue(String value) {
        this.value = value;
    }
}

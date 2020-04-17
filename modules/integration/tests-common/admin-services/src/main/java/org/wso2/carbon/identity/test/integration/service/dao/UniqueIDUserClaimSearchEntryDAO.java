/*
 * Copyright (c) 2020, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
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

package org.wso2.carbon.identity.test.integration.service.dao;

public class UniqueIDUserClaimSearchEntryDAO {

    private UserDTO user;
    private ClaimValue [] claims;
    private UserClaimSearchEntryDAO userClaimSearchEntry;

    public UniqueIDUserClaimSearchEntryDAO() {
    }

    public UserDTO getUser() {
        return this.user;
    }

    public void setUser(UserDTO user) {
        this.user = user;
    }

    public ClaimValue[] getClaims() {
        return this.claims;
    }

    public void setClaims(ClaimValue [] claims) {
        this.claims = claims;
    }

    public UserClaimSearchEntryDAO getUserClaimSearchEntry() {
        return this.userClaimSearchEntry;
    }

    public void setUserClaimSearchEntry(UserClaimSearchEntryDAO userClaimSearchEntry) {
        this.userClaimSearchEntry = userClaimSearchEntry;
    }
}

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

public class ClaimValue {

    private String claimUri;
    private String value;

    public ClaimValue() {
        super();
    }

    public ClaimValue(String claimUri, String claimValue) {
        this.claimUri = claimUri;
        this.value = claimValue;
    }

    public String getClaimUri() {
        return claimUri;
    }

    public void setClaimUri(String claimUri) {
        this.claimUri = claimUri;
    }

    public String getClaimValue() {
        return value;
    }

    public void setClaimValue(String claimValue) {
        this.value = claimValue;
    }


}

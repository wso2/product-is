/*
* Copyright (c) 2018, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
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
package org.wso2.carbon.is.migration.service.v550.bean;

public class AuthzCodeInfo {

    private String authorizationCode;
    private String codeId;
    private String authorizationCodeHash;

    public AuthzCodeInfo(String authorizationCode, String codeId) {
        this.authorizationCode = authorizationCode;
        this.codeId = codeId;
    }

    public String getAuthorizationCode() {
        return authorizationCode;
    }

    public void setAuthorizationCode(String authorizationCode) {
        this.authorizationCode = authorizationCode;
    }

    public String getCodeId() {
        return codeId;
    }

    public void setCodeId(String codeId) {
        this.codeId = codeId;
    }

    public String getAuthorizationCodeHash() {
        return authorizationCodeHash;
    }

    public void setAuthorizationCodeHash(String authorizationCodeHash) {
        this.authorizationCodeHash = authorizationCodeHash;
    }
}

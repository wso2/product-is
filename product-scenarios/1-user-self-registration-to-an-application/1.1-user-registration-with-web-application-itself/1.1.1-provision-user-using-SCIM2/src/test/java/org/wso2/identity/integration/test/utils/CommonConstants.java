/*
*Copyright (c) 2005-2015, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
*
*WSO2 Inc. licenses this file to you under the Apache License,
*Version 2.0 (the "License"); you may not use this file except
*in compliance with the License.
*You may obtain a copy of the License at
*
*http://www.apache.org/licenses/LICENSE-2.0
*
*Unless required by applicable law or agreed to in writing,
*software distributed under the License is distributed on an
*"AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
*KIND, either express or implied.  See the License for the
*specific language governing permissions and limitations
*under the License.
*/
package org.wso2.identity.integration.test.utils;

public class CommonConstants {

    public static final int IS_DEFAULT_OFFSET = 410;
    public static final int IS_DEFAULT_HTTPS_PORT = 9853;
    public static final int DEFAULT_TOMCAT_PORT = 8490;
    public static final String DEFAULT_SERVICE_URL = "https://localhost:9853/services/";
    public static final String SAML_REQUEST_PARAM = "SAMLRequest";
    public static final String SAML_RESPONSE_PARAM = "SAMLResponse";
    public static final String SESSION_DATA_KEY = "name=\"sessionDataKey\"";
    public static final String USER_DOES_NOT_EXIST = "17001";
    public static final String INVALID_CREDENTIAL = "17002";
    public static final String USER_IS_LOCKED = "17003";
    public static final String BASIC_AUTHENTICATOR="BasicAuthenticator";
    public static final String USER_AGENT_HEADER = "User-Agent";

    public enum AdminClients {
        IDENTITY_PROVIDER_MGT_SERVICE_CLIENT,
        APPLICATION_MANAGEMENT_SERVICE_CLIENT,
        USER_MANAGEMENT_CLIENT
    }



}

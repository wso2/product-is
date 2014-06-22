/*
*  Copyright (c) 2005-2010, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
*
*  WSO2 Inc. licenses this file to you under the Apache License,
*  Version 2.0 (the "License"); you may not use this file except
*  in compliance with the License.
*  You may obtain a copy of the License at
*
*    http://www.apache.org/licenses/LICENSE-2.0
*
* Unless required by applicable law or agreed to in writing,
* software distributed under the License is distributed on an
* "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
* KIND, either express or implied.  See the License for the
* specific language governing permissions and limitations
* under the License.
*/
package org.wso2.scim.sample.utils;

import org.apache.axiom.om.util.Base64;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.util.Properties;

/**
 * Utils class for SCIM Samples related operations.
 */
public class SCIMSamplesUtils {

    public static final String CONTENT_TYPE = "application/json";

    public static final String IS_HOME = ".." + File.separator + ".." + File.separator;

    public static final String TRUST_STORE_PATH = IS_HOME + "repository" + File.separator + "resources" +
                                                  File.separator + "security" + File.separator + "wso2carbon.jks";

    public static final String TRUST_STORE_PASS = "wso2carbon";

    public static final String PROPERTIES_FILE_NAME = "client.properties";
    /*property names as constants*/
    public static final String PROPERTY_NAME_USER_ENDPOINT_URL = "user.endpoint";
    public static final String PROPERTY_NAME_GROUP_ENDPOINT_URL = "group.endpoint";
    public static final String PROPERTY_NAME_USER_NAME = "provisioning.user.name";
    public static final String PROPERTY_NAME_PASSWORD = "provisioning.password";
    public static final String PROPERTY_NAME_ENABLE_OAUTH = "enable.oauth";
    public static final String PROPERTY_NAME_ACCESS_TOKEN = "oauth.access.token";

    public static final String USER_FILTER = "filter=userNameEq";
    public static final String GROUP_FILTER = "filter=displayNameEq";

    /*to be read from properties file*/
    public static String userEndpointURL = null;
    public static String groupEndpointURL = null;

    public static String userName = null;
    public static String password = null;

    public static boolean enableOAuth = false;
    public static String oauthAccessToken = null;

    public static String userNameToCreateUser = "HasiniTG";
    public static String userNameToUpdateUser = "HasiniG";
    public static String userNameToDeleteUser = "HasiniG";

    public static String groupDisplayNameToCreateGroup = "eng";
    public static String updatedGroupDisplayName = "Engineering";
    public static String groupDisplayNameToDeleteGroup = "Engineering";

    public static void setKeyStore() {
        System.setProperty("javax.net.ssl.trustStore", TRUST_STORE_PATH);
        System.setProperty("javax.net.ssl.trustStorePassword", TRUST_STORE_PASS);
    }

    public static String getBase64EncodedBasicAuthHeader(String userName, String password) {
        String concatenatedCredential = userName + ":" + password;
        byte[] byteValue = concatenatedCredential.getBytes();
        String encodedAuthHeader = Base64.encode(byteValue);
        encodedAuthHeader = "Basic " + encodedAuthHeader;
        return encodedAuthHeader;
    }

    public static void loadConfiguration() throws IOException {
        Properties properties = new Properties();
        FileInputStream freader = new FileInputStream(PROPERTIES_FILE_NAME);
        properties.load(freader);

        userEndpointURL = properties.getProperty(PROPERTY_NAME_USER_ENDPOINT_URL);
        groupEndpointURL = properties.getProperty(PROPERTY_NAME_GROUP_ENDPOINT_URL);
        userName = properties.getProperty(PROPERTY_NAME_USER_NAME);
        password = properties.getProperty(PROPERTY_NAME_PASSWORD);
        String isOAuth = properties.getProperty(PROPERTY_NAME_ENABLE_OAUTH);
        enableOAuth = Boolean.parseBoolean(isOAuth);
        oauthAccessToken = properties.getProperty(PROPERTY_NAME_ACCESS_TOKEN);
    }

    /*Util method to get authorization header according to the authentication method specified*/

    public static String getAuthorizationHeader() {
        if (enableOAuth) {
            return "Bearer " + oauthAccessToken;
        }
        return getBase64EncodedBasicAuthHeader(userName, password);
    }
}

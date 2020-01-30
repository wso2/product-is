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

package org.wso2.identity.integration.test.oidc;

import org.apache.http.Header;
import org.apache.http.HttpResponse;
import org.apache.http.NameValuePair;
import org.apache.http.client.HttpClient;
import org.apache.http.impl.client.HttpClientBuilder;
import org.apache.http.message.BasicNameValuePair;
import org.apache.http.util.EntityUtils;
import org.testng.Assert;
import org.wso2.identity.integration.test.oidc.bean.OIDCApplication;
import org.wso2.identity.integration.test.oidc.bean.OIDCUser;
import org.wso2.identity.integration.test.util.Utils;
import org.wso2.identity.integration.test.utils.DataExtractUtil;
import org.wso2.identity.integration.test.utils.OAuth2Constant;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static org.wso2.identity.integration.test.oidc.OIDCAbstractIntegrationTest.TOMCAT_PORT;

/**
 * This class will contain the common methods using in all OIDC related test cases.
 */
public class OIDCUtilTest {

    protected static OIDCUser user;
    protected HttpClient client;
    protected static Map<String, OIDCApplication> applications = new HashMap<>(2);
    public static final String username = "oidcsessiontestuser";
    public static final String password = "oidcsessiontestuser";
    public static final String email = "oidcsessiontestuser@wso2.com";
    public static final String firstName = "oidcsessiontestuser-first";
    public static final String lastName = "oidcsessiontestuser-last";
    public static final String role = "internal/everyone";
    public static final String profile = "default";
    protected static String sessionDataKey;

    public static final String playgroundAppOneAppName = "playground.appone";
    public static final String playgroundAppOneAppCallBackUri = "http://localhost:" + TOMCAT_PORT + "/playground" + "" +
            ".appone/oauth2client";
    public static final String playgroundAppOneAppContext = "/playground.appone";

    public static final String playgroundAppTwoAppName = "playground.apptwo";
    public static final String playgroundAppTwoAppCallBackUri = "http://localhost:" + TOMCAT_PORT + "/playground" + "" +
            ".apptwo/oauth2client";
    public static final String playgroundAppTwoAppContext = "/playground.apptwo";

    public static final String targetApplicationUrl = "http://localhost:" + TOMCAT_PORT + "%s";

    public static final String emailClaimUri = "http://wso2.org/claims/emailaddress";
    public static final String firstNameClaimUri = "http://wso2.org/claims/givenname";
    public static final String lastNameClaimUri = "http://wso2.org/claims/lastname";

    /**
     * Intiates an user.
     */
    public static void initUser() {

        user = new OIDCUser(username, password);
        user.setProfile(profile);
        user.addUserClaim(emailClaimUri, email);
        user.addUserClaim(firstNameClaimUri, firstName);
        user.addUserClaim(lastNameClaimUri, lastName);
        user.addRole(role);
    }

    /**
     * Initiate an Application.
     */
    public static void initApplications() {

        OIDCApplication playgroundApp = new OIDCApplication(playgroundAppOneAppName, playgroundAppOneAppContext,
                playgroundAppOneAppCallBackUri);
        playgroundApp.addRequiredClaim(emailClaimUri);
        playgroundApp.addRequiredClaim(firstNameClaimUri);
        playgroundApp.addRequiredClaim(lastNameClaimUri);
        applications.put(playgroundAppOneAppName, playgroundApp);

        playgroundApp = new OIDCApplication(playgroundAppTwoAppName, playgroundAppTwoAppContext,
                playgroundAppTwoAppCallBackUri);
        playgroundApp.addRequiredClaim(emailClaimUri);
        playgroundApp.addRequiredClaim(firstNameClaimUri);
        playgroundApp.addRequiredClaim(lastNameClaimUri);
        applications.put(playgroundAppTwoAppName, playgroundApp);
    }

    /**
     * To set and get name-value pairs.
     * @param application application
     * @return name-value pairs.
     */
    public static List<NameValuePair> getNameValuePairs(OIDCApplication application) {

        List<NameValuePair> urlParameters = new ArrayList<NameValuePair>();
        urlParameters.add(new BasicNameValuePair("grantType", OAuth2Constant.OAUTH2_GRANT_TYPE_CODE));
        urlParameters.add(new BasicNameValuePair("consumerKey", application.getClientId()));
        urlParameters.add(new BasicNameValuePair("callbackurl", application.getCallBackURL()));
        urlParameters.add(new BasicNameValuePair("authorizeEndpoint", OAuth2Constant.APPROVAL_URL));
        urlParameters.add(new BasicNameValuePair("authorize", OAuth2Constant.AUTHORIZE_PARAM));
        urlParameters.add(new BasicNameValuePair("scope", OAuth2Constant.OAUTH2_SCOPE_OPENID + " " + OAuth2Constant
                .OAUTH2_SCOPE_EMAIL));
        return urlParameters;
    }

    /**
     * Set sessionDataKey
     * @param response response
     * @param keyPositionMap map to preserve the sessionDataKey
     * @throws IOException if an error occurs when extracting data from the response.
     */
    public static void setSessionDataKey(HttpResponse response, Map<String, Integer> keyPositionMap) throws IOException {

        keyPositionMap.put("name=\"sessionDataKey\"", 1);
        List<DataExtractUtil.KeyValue> keyValues = DataExtractUtil.extractDataFromResponse(response,
                keyPositionMap);

        if (keyValues != null) {
            sessionDataKey = keyValues.get(0).getValue();
        }
    }
}

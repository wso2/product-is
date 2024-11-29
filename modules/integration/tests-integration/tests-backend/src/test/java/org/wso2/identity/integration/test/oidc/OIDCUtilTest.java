/*
 * Copyright (c) 2020, WSO2 LLC. (http://www.wso2.com).
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

package org.wso2.identity.integration.test.oidc;

import org.apache.http.HttpResponse;
import org.apache.http.NameValuePair;
import org.apache.http.client.HttpClient;
import org.apache.http.message.BasicNameValuePair;
import org.wso2.identity.integration.test.base.MockClientCallback;
import org.wso2.identity.integration.test.oidc.bean.OIDCApplication;
import org.wso2.identity.integration.test.rest.api.user.common.model.Email;
import org.wso2.identity.integration.test.rest.api.user.common.model.Name;
import org.wso2.identity.integration.test.rest.api.user.common.model.UserObject;
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

    protected static UserObject user;
    protected HttpClient client;
    protected static Map<String, OIDCApplication> applications = new HashMap<>(2);
    public static final String username = "oidcsessiontestuser";
    public static final String password = "Oidcsessiontestuser@123";
    public static final String email = "oidcsessiontestuser@wso2.com";
    public static final String firstName = "oidcsessiontestuser-first";
    public static final String lastName = "oidcsessiontestuser-last";
    public static final String role = "internal/everyone";
    public static final String profile = "default";
    protected static String sessionDataKey;

    public static final String playgroundAppOneAppName = "playground.appone";
    public static final String playgroundAppOneAppCallBackUri = MockClientCallback.CALLBACK_URL_APP1;

    // TODO find the usages to identify the test cases that initiate the login from the app, instead of sending the
    //  login request directly to IS.
    public static final String playgroundAppOneAppContext = "/playground.appone";

    public static final String playgroundAppTwoAppName = "playground.apptwo";
    public static final String playgroundAppTwoAppCallBackUri = MockClientCallback.CALLBACK_URL_APP2;
    public static final String playgroundAppTwoAppContext = "/playground.apptwo";

    public static final String targetApplicationUrl = "http://localhost:" + TOMCAT_PORT + "%s";

    public static final String emailClaimUri = "http://wso2.org/claims/emailaddress";
    public static final String firstNameClaimUri = "http://wso2.org/claims/givenname";
    public static final String lastNameClaimUri = "http://wso2.org/claims/lastname";

    /**
     * Initiates a user.
     */
    public static void initUser() {

        user = new UserObject();
        user.setUserName(username);
        user.setPassword(password);
        user.setName(new Name().givenName(firstName).familyName(lastName));
        user.addEmail(new Email().value(email));
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
     *
     * @param application application.
     * @return name-value pairs.
     */
    public static List<NameValuePair> getNameValuePairs(OIDCApplication application) {

        return getNameValuePairs(application, OAuth2Constant.APPROVAL_URL);
    }

    public static List<NameValuePair> getNameValuePairs(OIDCApplication application, String approvalUrl) {

        List<NameValuePair> urlParameters = new ArrayList<>();
        urlParameters.add(new BasicNameValuePair("grantType", OAuth2Constant.OAUTH2_GRANT_TYPE_CODE));
        urlParameters.add(new BasicNameValuePair("consumerKey", application.getClientId()));
        urlParameters.add(new BasicNameValuePair("callbackurl", application.getCallBackURL()));
        urlParameters.add(new BasicNameValuePair("authorizeEndpoint", approvalUrl));
        urlParameters.add(new BasicNameValuePair("authorize", OAuth2Constant.AUTHORIZE_PARAM));
        urlParameters.add(new BasicNameValuePair("scope", OAuth2Constant.OAUTH2_SCOPE_OPENID + " " +
                OAuth2Constant.OAUTH2_SCOPE_EMAIL + " " + OAuth2Constant.OAUTH2_SCOPE_PROFILE));
        return urlParameters;
    }

    /**
     * Set sessionDataKey.
     *
     * @param response       response
     * @param keyPositionMap map to preserve the sessionDataKey.
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

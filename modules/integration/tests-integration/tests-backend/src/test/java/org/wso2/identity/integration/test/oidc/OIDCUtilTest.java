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
import org.wso2.identity.integration.test.base.MockApplicationServer;
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

/**
 * This class will contain the common methods using in all OIDC related test cases.
 */
public class OIDCUtilTest {

    protected HttpClient client;
    public static final String USERNAME = "oidcsessiontestuser";
    public static final String PASSWORD = "Oidcsessiontestuser@123";
    public static final String EMAIL = "oidcsessiontestuser@wso2.com";
    public static final String FIRST_NAME = "oidcsessiontestuser-first";
    public static final String LAST_NAME = "oidcsessiontestuser-last";

    public static final String PLAYGROUND_APP_ONE_APP_NAME = MockApplicationServer.Constants.APP1.NAME;
    public static final String PLAYGROUND_APP_ONE_APP_CALL_BACK_URI = MockApplicationServer.Constants.APP1.CALLBACK_URL;

    public static final String PLAYGROUND_APP_TWO_APP_NAME = MockApplicationServer.Constants.APP2.NAME;
    public static final String PLAYGROUND_APP_TWO_APP_CALL_BACK_URI = MockApplicationServer.Constants.APP2.CALLBACK_URL;

    public static final String EMAIL_CLAIM_URI = "http://wso2.org/claims/emailaddress";
    public static final String FIRST_NAME_CLAIM_URI = "http://wso2.org/claims/givenname";
    public static final String LAST_NAME_CLAIM_URI = "http://wso2.org/claims/lastname";

    /**
     * Initiates a user.
     */
    public static UserObject initUser() {

        UserObject user = new UserObject();
        user.setUserName(USERNAME);
        user.setPassword(PASSWORD);
        user.setName(new Name().givenName(FIRST_NAME).familyName(LAST_NAME));
        user.addEmail(new Email().value(EMAIL));
        return user;
    }

    /**
     * Initiate an Application.
     */
    public static Map<String, OIDCApplication> initApplications() {

        Map<String, OIDCApplication> applications = new HashMap<>(2);
        OIDCApplication playgroundApp = initApplicationOne();
        applications.put(PLAYGROUND_APP_ONE_APP_NAME, playgroundApp);

        playgroundApp = new OIDCApplication(PLAYGROUND_APP_TWO_APP_NAME,
                PLAYGROUND_APP_TWO_APP_CALL_BACK_URI);
        playgroundApp.addRequiredClaim(EMAIL_CLAIM_URI);
        playgroundApp.addRequiredClaim(FIRST_NAME_CLAIM_URI);
        playgroundApp.addRequiredClaim(LAST_NAME_CLAIM_URI);
        applications.put(PLAYGROUND_APP_TWO_APP_NAME, playgroundApp);
        return applications;
    }

    public static OIDCApplication initApplicationOne() {

        OIDCApplication playgroundApp = new OIDCApplication(PLAYGROUND_APP_ONE_APP_NAME,
                PLAYGROUND_APP_ONE_APP_CALL_BACK_URI);
        playgroundApp.addRequiredClaim(EMAIL_CLAIM_URI);
        playgroundApp.addRequiredClaim(FIRST_NAME_CLAIM_URI);
        playgroundApp.addRequiredClaim(LAST_NAME_CLAIM_URI);
        return playgroundApp;
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
            // There is no value in doing this.
            String sessionDataKey = keyValues.get(0).getValue();
        }
    }
}

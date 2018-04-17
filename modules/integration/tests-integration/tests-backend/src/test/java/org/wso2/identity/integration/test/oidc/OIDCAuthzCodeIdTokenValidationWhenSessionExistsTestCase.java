/*
 * Copyright (c) 2018, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
 *
 * WSO2 Inc. licenses this file to you under the Apache License,
 * Version 2.0 (the "License"); you may not use this file except
 * in compliance with the License.
 * You may obtain a copy of the License at
 *
 *    http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied. See the License for the
 * specific language governing permissions and limitations
 * under the License.
 *
 */

package org.wso2.identity.integration.test.oidc;

import org.apache.http.HttpResponse;
import org.apache.http.NameValuePair;
import org.apache.http.message.BasicNameValuePair;
import org.testng.Assert;
import org.testng.annotations.Test;
import org.wso2.identity.integration.test.utils.DataExtractUtil;
import org.wso2.identity.integration.test.utils.OAuth2Constant;

import java.util.ArrayList;
import java.util.List;

public class OIDCAuthzCodeIdTokenValidationWhenSessionExistsTestCase extends OIDCAuthzCodeIdTokenValidationTestCase {


    @Test(groups = "wso2.is", description = "Send authorize user request for authorization code grant type.")
    public void testNewAuthCodeGrantSendAuthRequestPost() throws Exception {

        testSecondAuthCodeGrantSendAuthRequestPost();
        testAuthCodeGrantSendApprovalPost();
        testAuthCodeGrantSendGetTokensPost();
    }

    /**
     * Test method for the second authorization code request with approved consent.
     * @throws Exception
     */
    public void testSecondAuthCodeGrantSendAuthRequestPost() throws Exception {

        // Send a direct auth code request to IS instance.
        List<NameValuePair> urlParameters = new ArrayList<>();
        urlParameters.add(new BasicNameValuePair(OAuth2Constant.OAUTH2_RESPONSE_TYPE,
                OAuth2Constant.OAUTH2_GRANT_TYPE_CODE));
        urlParameters.add(new BasicNameValuePair(OAuth2Constant.OAUTH2_CLIENT_ID, consumerKey));
        urlParameters.add(new BasicNameValuePair(OAuth2Constant.OAUTH2_REDIRECT_URI, CALLBACK_URL));
        urlParameters.add(new BasicNameValuePair(OAuth2Constant.OAUTH2_SCOPE, OAuth2Constant.OAUTH2_SCOPE_OPENID));
        urlParameters.add(new BasicNameValuePair(OAuth2Constant.OAUTH2_NONCE, TEST_NONCE));

        HttpResponse response =
                sendPostRequestWithParameters(client, urlParameters, OAuth2Constant.AUTHORIZE_ENDPOINT_URL);
        Assert.assertNotNull(response, "Authorization request failed. Authorized response is null");

        String locationValue = getLocationHeaderValue(response);
        Assert.assertTrue(locationValue.contains(OAuth2Constant.SESSION_DATA_KEY),
                "sessionDataKey not found in response.");
        sessionDataKeyConsent = DataExtractUtil.getParamFromURIString(locationValue, OAuth2Constant
                .SESSION_DATA_KEY_CONSENT);
    }

}

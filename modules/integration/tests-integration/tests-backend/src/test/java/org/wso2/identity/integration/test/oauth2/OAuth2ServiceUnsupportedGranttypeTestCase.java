/*
 * Copyright (c) 2014, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
 *
 * WSO2 Inc. licenses this file to you under the Apache License,
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
package org.wso2.identity.integration.test.oauth2;

import org.apache.http.HttpResponse;
import org.apache.http.NameValuePair;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.message.BasicNameValuePair;
import org.apache.http.util.EntityUtils;
import org.json.simple.JSONObject;
import org.json.simple.JSONValue;
import org.testng.Assert;
import org.testng.annotations.AfterClass;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;
import org.wso2.carbon.automation.engine.context.TestUserMode;
import org.wso2.carbon.identity.oauth.stub.dto.OAuthConsumerAppDTO;
import org.wso2.carbon.integration.common.admin.client.AuthenticatorClient;
import org.wso2.identity.integration.test.utils.OAuth2Constant;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.util.ArrayList;

public class OAuth2ServiceUnsupportedGranttypeTestCase extends OAuth2ServiceAbstractIntegrationTest {

    private String consumerKey;
    private String consumerSecret;
    private String unSupportedGrantType;
    private DefaultHttpClient client;
    private AuthenticatorClient logManger;

    @BeforeClass(alwaysRun = true)
    public void testInit() throws Exception {

        super.init(TestUserMode.SUPER_TENANT_USER);
        logManger = new AuthenticatorClient(backendURL);
        logManger.login(isServer.getSuperTenant().getTenantAdmin().getUserName(),
                isServer.getSuperTenant().getTenantAdmin().getPassword(),
                isServer.getInstance().getHosts().get("default"));

        setSystemproperties();
        client = new DefaultHttpClient();
    }

    @AfterClass(alwaysRun = true)
    public void atEnd() throws Exception {

        deleteApplication();
        removeOAuthApplicationData();

        logManger = null;
        consumerKey = null;
    }

    @Test(groups = "wso2.is",
            description = "Unsupported grant type")
    public void testUnsupportedGrantype() throws Exception {

        OAuthConsumerAppDTO appDto = createApplication();
        consumerKey = appDto.getOauthConsumerKey();
        consumerSecret = appDto.getOauthConsumerSecret();
        unSupportedGrantType = "1qaz2wsx";

        ArrayList<NameValuePair> postParameters;
        HttpPost httpPost = new HttpPost(OAuth2Constant.ACCESS_TOKEN_ENDPOINT);
        //generate post request
        httpPost.setHeader("Authorization", "Basic " + getBase64EncodedString(consumerKey, consumerSecret));
        httpPost.setHeader("Content-Type", "application/x-www-form-urlencoded");
        postParameters = new ArrayList<NameValuePair>();
        postParameters.add(new BasicNameValuePair("username", "admin"));
        postParameters.add(new BasicNameValuePair("password", "admin"));
        postParameters.add(new BasicNameValuePair("grant_type", unSupportedGrantType));
        httpPost.setEntity(new UrlEncodedFormEntity(postParameters));
        HttpResponse response = client.execute(httpPost);

        BufferedReader rd = new BufferedReader(new InputStreamReader(response.getEntity().getContent()));

        Object obj = JSONValue.parse(rd);
        String errorMessage = ((JSONObject) obj).get("error").toString();
        EntityUtils.consume(response.getEntity());
        Assert.assertEquals(OAuth2Constant.UNSUPPORTED_GRANT_TYPE, errorMessage,
                "Unsupported grant type should have " + "produced error code : "
                        + OAuth2Constant.UNSUPPORTED_GRANT_TYPE);
    }

}
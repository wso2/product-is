/*
 * Copyright (c) 2015, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.wso2.identity.integration.test.oauth2;

import org.apache.http.Header;
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
import org.wso2.identity.integration.test.utils.OAuth2Constant;
import org.wso2.carbon.integration.common.admin.client.AuthenticatorClient;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.List;

public class OAuth2ServiceErrorResponseTest extends OAuth2ServiceAbstractIntegrationTest {

    private AuthenticatorClient authenticatorClient;
    private DefaultHttpClient client;

    @BeforeClass(alwaysRun = true)
    public void testInit() throws Exception {
        super.init(TestUserMode.SUPER_TENANT_USER);
        authenticatorClient = new AuthenticatorClient(backendURL);
        authenticatorClient.login(isServer.getSuperTenant().getTenantAdmin().getUserName(),
                isServer.getSuperTenant().getTenantAdmin().getPassword(),
                isServer.getInstance().getHosts().get("default"));

        setSystemproperties();
        client = new DefaultHttpClient();
    }

    @AfterClass(alwaysRun = true)
    public void atEnd() throws Exception {
        removeOAuthApplicationData();
        authenticatorClient = null;
        consumerKey = null;
    }

    @Test(groups = "wso2.is", description = "Check Oauth2 application registration")
    public void testRegisterApplication() throws Exception {
        OAuthConsumerAppDTO appDto = createApplication();
        Assert.assertNotNull(appDto, "Application creation failed.");

        consumerKey = appDto.getOauthConsumerKey();
        Assert.assertNotNull(consumerKey, "Application creation failed.");
        consumerSecret = appDto.getOauthConsumerSecret();
    }

    @Test(groups = "wso2.is", description = "Check whether the client is not authorized to use the " +
            "authorization grant type", dependsOnMethods = "testRegisterApplication")
    public void testUnauthorizeClientErrorResponse() throws Exception {
        List<NameValuePair> urlParameters = new ArrayList<NameValuePair>();
        urlParameters.add(new BasicNameValuePair("response_type",
                OAuth2Constant.OAUTH2_GRANT_TYPE_IMPLICIT));
        urlParameters.add(new BasicNameValuePair("client_id", consumerKey));
        urlParameters.add(new BasicNameValuePair("redirect_uri", OAuth2Constant.CALLBACK_URL));

        HttpResponse response =
                sendPostRequestWithParameters(client, urlParameters,
                                              OAuth2Constant.APPROVAL_URL);
        Assert.assertNotNull(response, "Authorization request failed. Authorized response is null");

        Header locationHeader =
                response.getFirstHeader(OAuth2Constant.HTTP_RESPONSE_HEADER_LOCATION);

        String locationURI = locationHeader.getValue();

        Assert.assertTrue(locationURI.contains(OAuth2Constant.UNAUTHORIZED_CLIENT));
        Assert.assertTrue(locationURI.contains("The+authenticated+client+is+not+authorized+to+use+this+authorization+" +
                "grant+type"));
    }

    @Test(groups = "wso2.is",
            description = "Check whether the grant type is not supported by the authorization server",
            dependsOnMethods = "testRegisterApplication")
    public void testUnsupportedGrantype() throws Exception {

        String unSupportedGrantType = "1qaz2wsx";
        client = new DefaultHttpClient();
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


    public OAuthConsumerAppDTO createApplication() throws Exception {

        OAuthConsumerAppDTO appDtoResult = null;

        OAuthConsumerAppDTO appDTO = new OAuthConsumerAppDTO();
        appDTO.setApplicationName(OAuth2Constant.OAUTH_APPLICATION_NAME);
        appDTO.setCallbackUrl(OAuth2Constant.CALLBACK_URL);
        appDTO.setOAuthVersion(OAuth2Constant.OAUTH_VERSION_2);
        appDTO.setGrantTypes("");

        adminClient.registerOAuthApplicationData(appDTO);
        OAuthConsumerAppDTO[] appDtos = adminClient.getAllOAuthApplicationData();

        for (OAuthConsumerAppDTO appDto : appDtos) {
            if (appDto.getApplicationName().equals(OAuth2Constant.OAUTH_APPLICATION_NAME)) {
                appDtoResult = appDto;
                consumerKey = appDto.getOauthConsumerKey();
                consumerSecret = appDto.getOauthConsumerSecret();
            }
        }
        return appDtoResult;
    }
}

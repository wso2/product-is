/*
 * Copyright (c) 2019, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
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
package org.wso2.identity.scenarios.access.delegation.oauth2.selfcontainedtoken;

import com.nimbusds.jwt.SignedJWT;
import org.apache.http.HttpResponse;
import org.json.simple.JSONObject;
import org.testng.Assert;
import org.testng.annotations.AfterClass;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.DataProvider;
import org.testng.annotations.Factory;
import org.testng.annotations.Test;
import org.wso2.carbon.identity.application.common.model.xsd.ServiceProvider;
import org.wso2.carbon.identity.oauth.stub.dto.OAuthConsumerAppDTO;
import org.wso2.identity.scenarios.commons.HTTPCommonClient;
import org.wso2.identity.scenarios.commons.OAuth2CommonClient;
import org.wso2.identity.scenarios.commons.SSOCommonClient;
import org.wso2.identity.scenarios.commons.ScenarioTestBase;
import org.wso2.identity.scenarios.commons.clients.oauth.OauthAdminClient;
import org.wso2.identity.scenarios.commons.util.Constants;
import org.wso2.identity.scenarios.commons.util.OAuth2Constants;
import org.wso2.identity.scenarios.commons.util.SSOConstants;

import static org.testng.Assert.assertEquals;
import static org.testng.Assert.assertNotNull;
import static org.wso2.identity.scenarios.commons.util.Constants.IS_HTTPS_URL;

/**
 * This test class tests the retrieval of self- contained access token when the grant type is authorization code grant.
 */
public class OAuth2SelfContainedTokenCodeGrantTest extends ScenarioTestBase {

    private String username;

    private String password;

    private String tenantDomain;

    private String clientId;

    private String clientSecret;

    private String redirectUri;

    private String sessionDataKey;

    private String sessionDataKeyConsent;

    private String consentUrl;

    private String authorizeCode;

    private String accessToken;

    private HTTPCommonClient httpCommonClient;

    private OAuth2CommonClient oAuth2CommonClient;

    private SSOCommonClient ssoCommonClient;

    private SignedJWT signedJWT;

    private String spName;

    private ServiceProvider serviceProvider;

    private OauthAdminClient oauthAdminClient;

    private String spConfigFile;

    @Factory(dataProvider = "oAuth2SelfContainedTokenCodeGrantTestConfigProvider")
    public OAuth2SelfContainedTokenCodeGrantTest(String spConfigFile, String username, String password,
                                                 String tenantDomain) {

        this.spConfigFile = spConfigFile;
        this.username = username;
        this.password = password;
        this.tenantDomain = tenantDomain;

    }

    @DataProvider(name = "oAuth2SelfContainedTokenCodeGrantTestConfigProvider")
    private static Object[][] oAuth2SelfContainedTokenCodeGrantTestConfigProvider() throws Exception {

        return new Object[][]{
                {
                        "selfcontainedtoken-code-grant.xml", ADMIN_USERNAME, ADMIN_PASSWORD,
                        SUPER_TENANT_DOMAIN
                }
        };
    }

    @BeforeClass(alwaysRun = true)
    public void testInit() throws Exception {

        super.init();
        loginAndObtainSessionCookie();

        httpCommonClient = new HTTPCommonClient();
        oAuth2CommonClient = new OAuth2CommonClient(httpCommonClient, getDeploymentProperty(IS_HTTPS_URL),
                tenantDomain);
        ssoCommonClient = new SSOCommonClient(httpCommonClient, getDeploymentProperty(IS_HTTPS_URL), tenantDomain,
                sessionCookie, backendServiceURL, configContext);
        oauthAdminClient = new OauthAdminClient(backendServiceURL, sessionCookie);

        spName = ssoCommonClient.createServiceProvider(spConfigFile);
        assertNotNull(spName, "Failed to create service provider from file: " + spConfigFile);

        serviceProvider = ssoCommonClient.getServiceProvider(spName);
        Assert.assertNotNull(serviceProvider, "Failed to load service provider : " + spName);

        OAuthConsumerAppDTO oauthApp = oAuth2CommonClient.getOAuthConsumerApp(serviceProvider, oauthAdminClient);

        clientId = oauthApp.getOauthConsumerKey();
        clientSecret = oauthApp.getOauthConsumerSecret();
        redirectUri = oauthApp.getCallbackUrl();

        Thread.sleep(5000);
    }

    @AfterClass(alwaysRun = true)
    public void atEnd() throws Exception {

        if (serviceProvider != null) {
            ssoCommonClient.deleteServiceProvider(serviceProvider.getApplicationName());
        }
        httpCommonClient.closeHttpClient();
    }

    @Test(description = "9.1.10.2.1")
    public void intiAuthorizeRequest() throws Exception {

        HttpResponse response = oAuth2CommonClient
                .sendAuthorizeGet(clientId, null, redirectUri, OAuth2Constants.ResponseTypes.CODE, null);
        sessionDataKey = ssoCommonClient.getSessionDataKey(response);
        assertNotNull(sessionDataKey, "sessionDataKey parameter value is null.");

        httpCommonClient.consume(response);
    }

    @Test(description = "9.1.10.2.2",
            dependsOnMethods = "intiAuthorizeRequest")
    public void authenticate() throws Exception {

        HttpResponse response = ssoCommonClient.sendLoginPost(sessionDataKey, username, password);
        consentUrl = ssoCommonClient.getLocationHeader(response);
        assertNotNull(consentUrl, "Location header is null. Invalid consent page url.");

        httpCommonClient.consume(response);
    }

    @Test(description = "9.1.10.2.3",
            dependsOnMethods = "authenticate")
    public void initOAuthConsent() throws Exception {

        HttpResponse response = httpCommonClient.sendGetRequest(consentUrl, null, null);
        sessionDataKeyConsent = ssoCommonClient.getSessionDataKeyConsent(response);
        assertNotNull(sessionDataKeyConsent, "sessionDataKeyConsent parameter value is null");

        httpCommonClient.consume(response);
    }

    @Test(description = "9.1.10.2.4",
            dependsOnMethods = "initOAuthConsent")
    public void submitOAuthConsent() throws Exception {

        HttpResponse response = oAuth2CommonClient
                .sendOAuthConsentApprovePost(sessionDataKeyConsent, SSOConstants.ApprovalType.APPROVE_ONCE);
        authorizeCode = oAuth2CommonClient.getAuthorizeCode(response);
        assertNotNull(authorizeCode, "code parameter value is null. Invalid authorization code.");

        httpCommonClient.consume(response);
    }

    @Test(description = "9.1.10.2.5",
            dependsOnMethods = "submitOAuthConsent")
    public void getOAccessToken() throws Exception {

        HttpResponse response = oAuth2CommonClient
                .sendCodeGrantTokenRequest(authorizeCode, redirectUri, clientId, clientSecret, null);
        JSONObject responseJSON = httpCommonClient.getJSONFromResponse(response);
        oAuth2CommonClient.validateAccessToken(responseJSON, false);
        accessToken = responseJSON.get(OAuth2Constants.TokenResponseElements.ACCESS_TOKEN).toString();
        signedJWT = SignedJWT.parse(accessToken);
        assertNotNull(signedJWT, "JWT token value is null. Invalid self-contained access token.");
        assertEquals(SignedJWT.parse(accessToken).getJWTClaimsSet().getIssuer(), getDeploymentProperty(IS_HTTPS_URL) +
                Constants.OAUTH_TOKEN_URI_CONTEXT, "Invalid issuer id in the signed jwt");

        httpCommonClient.consume(response);
    }

    @Test(description = "9.1.10.2.6",
            dependsOnMethods = "getOAccessToken")
    public void introspectAccessToken() throws Exception {

        HttpResponse response = oAuth2CommonClient.sendIntrospectRequest(accessToken, username, password);
        JSONObject responseJSON = httpCommonClient.getJSONFromResponse(response);
        oAuth2CommonClient.validateIntrospectResponse(responseJSON);

        httpCommonClient.consume(response);
    }
}

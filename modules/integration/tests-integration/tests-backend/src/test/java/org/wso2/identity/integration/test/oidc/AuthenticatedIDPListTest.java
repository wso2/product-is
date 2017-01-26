/*
 * Copyright (c) 201, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
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

package org.wso2.identity.integration.test.oidc;

import org.apache.commons.lang.StringUtils;
import org.apache.http.Header;
import org.apache.http.HttpResponse;
import org.apache.http.NameValuePair;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.HttpClientBuilder;
import org.apache.http.message.BasicNameValuePair;
import org.apache.http.util.EntityUtils;
import org.testng.Assert;
import org.testng.annotations.AfterClass;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;
import org.wso2.carbon.identity.application.common.model.xsd.Claim;
import org.wso2.carbon.identity.application.common.model.xsd.ClaimConfig;
import org.wso2.carbon.identity.application.common.model.xsd.ClaimMapping;
import org.wso2.carbon.identity.application.common.model.xsd.InboundAuthenticationRequestConfig;
import org.wso2.carbon.identity.application.common.model.xsd.LocalAndOutboundAuthenticationConfig;
import org.wso2.carbon.identity.application.common.model.xsd.OutboundProvisioningConfig;
import org.wso2.carbon.identity.application.common.model.xsd.Property;
import org.wso2.carbon.identity.application.common.model.xsd.ServiceProvider;
import org.wso2.carbon.identity.oauth.stub.dto.OAuthConsumerAppDTO;
import org.wso2.identity.integration.test.oidc.bean.OIDCApplication;
import org.wso2.identity.integration.test.oidc.bean.OIDCUser;
import org.wso2.identity.integration.test.utils.DataExtractUtil;
import org.wso2.identity.integration.test.utils.OAuth2Constant;

import java.io.File;
import java.io.IOException;
import java.net.URL;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * This test class tests OIDC SSO functionality for two replying party applications
 */
public class AuthenticatedIDPListTest extends OIDCAbstractIntegrationTest {

    public static final String username = "oidcsessiontestuser1";
    public static final String password = "oidcsessiontestuser1";


    public static final String playgroundAppOneAppName = "playground.appone";
    public static final String playgroundAppOneAppCallBackUri = "http://localhost:" + TOMCAT_PORT + "/playground" + "" +
            ".appone/oauth2client";
    public static final String playgroundAppOneAppContext = "/playground.appone";
    public static final String targetApplicationUrl = "http://localhost:" + TOMCAT_PORT + "%s";
    protected OIDCUser user;
    protected Map<String, OIDCApplication> applications = new HashMap<>(2);

    protected String accessToken;
    protected String sessionDataKeyConsent;
    protected String sessionDataKey;
    protected String authorizationCode;

    protected HttpClient client;


    @BeforeClass(alwaysRun = true)
    public void testInit() throws Exception {

        super.init();

        initUser();
        createUser(user);

        initApplications();
        createApplications();

        startTomcat();
        deployApplications();

        client = HttpClientBuilder.create().build();

    }

    @AfterClass(alwaysRun = true)
    public void testClear() throws Exception {

        deleteUser(user);
        deleteApplications();

        stopTomcat();

        appMgtclient = null;
        remoteUSMServiceClient = null;
        adminClient = null;

        client = null;

    }

    @Test(groups = "wso2.is", description = "Initiate authentication request from playground.appone")
    public void testSendAuthenticationRequestFromRP1() throws Exception {

        testSendAuthenticationRequest(applications.get(playgroundAppOneAppName), true);
    }

    @Test(groups = "wso2.is", description = "Authenticate for playground.appone", dependsOnMethods =
            "testSendAuthenticationRequestFromRP1")
    public void testAuthenticationFromRP1() throws Exception {

        testAuthentication(applications.get(playgroundAppOneAppName));
    }


    @Test(groups = "wso2.is", description = "Approve consent for playground.appone", dependsOnMethods =
            "testAuthenticationFromRP1")
    public void testConsentApprovalFromRP1() throws Exception {

        testConsentApproval(applications.get(playgroundAppOneAppName));
    }

    @Test(groups = "wso2.is", description = "Authenticate for playground.appone", dependsOnMethods =
            "testConsentApprovalFromRP1")
    public void testSendAuthenticationRequest2FromRP1() throws Exception {
        client = HttpClientBuilder.create().build();
        testSendAuthenticationRequest(applications.get(playgroundAppOneAppName), true);
    }

    @Test(groups = "wso2.is", description = "Authenticate for playground.appone", dependsOnMethods =
            "testSendAuthenticationRequest2FromRP1")
    public void testAuthenticationAttempt2FromRP1() throws Exception {

        testAuthentication2(applications.get(playgroundAppOneAppName));
    }

    private void testSendAuthenticationRequest(OIDCApplication application, boolean isFirstAuthenticationRequest)
            throws Exception {

        List<NameValuePair> urlParameters = new ArrayList<NameValuePair>();
        urlParameters.add(new BasicNameValuePair("grantType", OAuth2Constant.OAUTH2_GRANT_TYPE_CODE));
        urlParameters.add(new BasicNameValuePair("consumerKey", application.getClientId()));
        urlParameters.add(new BasicNameValuePair("callbackurl", application.getCallBackURL()));
        urlParameters.add(new BasicNameValuePair("authorizeEndpoint", OAuth2Constant.APPROVAL_URL));
        urlParameters.add(new BasicNameValuePair("authorize", OAuth2Constant.AUTHORIZE_PARAM));
        urlParameters.add(new BasicNameValuePair("scope", OAuth2Constant.OAUTH2_SCOPE_OPENID + " " + OAuth2Constant
                .OAUTH2_SCOPE_EMAIL));

        HttpResponse response = sendPostRequestWithParameters(client, urlParameters, String.format
                (targetApplicationUrl, application.getApplicationContext() + OAuth2Constant.PlaygroundAppPaths
                        .appUserAuthorizePath));
        Assert.assertNotNull(response, "Authorization request failed for " + application.getApplicationName() + ". "
                + "Authorized response is null");

        Header locationHeader = response.getFirstHeader(OAuth2Constant.HTTP_RESPONSE_HEADER_LOCATION);

        Assert.assertNotNull(locationHeader, "Authorization request failed for " + application.getApplicationName() +
                ". Authorized response header is null");
        EntityUtils.consume(response.getEntity());

        response = sendGetRequest(client, locationHeader.getValue());
        Assert.assertNotNull(response, "Authorization request failed for " + application.getApplicationName() + ". "
                + "Authorized user response is null.");

        Map<String, Integer> keyPositionMap = new HashMap<>(1);
        if (isFirstAuthenticationRequest) {
            keyPositionMap.put("name=\"sessionDataKey\"", 1);
            List<DataExtractUtil.KeyValue> keyValues = DataExtractUtil.extractDataFromResponse(response,
                    keyPositionMap);
            Assert.assertNotNull(keyValues, "sessionDataKey key value is null for " + application.getApplicationName());

            sessionDataKey = keyValues.get(0).getValue();
            Assert.assertNotNull(sessionDataKey, "Invalid sessionDataKey for " + application.getApplicationName());
        } else {
            keyPositionMap.put("name=\"sessionDataKeyConsent\"", 1);
            List<DataExtractUtil.KeyValue> keyValues = DataExtractUtil.extractSessionConsentDataFromResponse
                    (response, keyPositionMap);
            Assert.assertNotNull(keyValues, "SessionDataKeyConsent key value is null for " + application
                    .getApplicationName());

            sessionDataKeyConsent = keyValues.get(0).getValue();
            Assert.assertNotNull(sessionDataKeyConsent, "Invalid sessionDataKeyConsent for " + application
                    .getApplicationName());
        }

        EntityUtils.consume(response.getEntity());
    }

    private void testAuthentication(OIDCApplication application) throws Exception {

        HttpResponse response = sendLoginPost(client, sessionDataKey);
        Assert.assertNotNull(response, "Login request failed for " + application.getApplicationName() + ". response "
                + "is null.");

        Header locationHeader = response.getFirstHeader(OAuth2Constant.HTTP_RESPONSE_HEADER_LOCATION);
        Assert.assertNotNull(locationHeader, "Login response header is null for " + application.getApplicationName());
        EntityUtils.consume(response.getEntity());
        response = sendGetRequest(client, locationHeader.getValue());
        Map<String, Integer> keyPositionMap = new HashMap<>(2);
        keyPositionMap.put("name=\"sessionDataKeyConsent\"", 1);
        List<DataExtractUtil.KeyValue> keyValues = DataExtractUtil.extractSessionConsentDataFromResponse(response,
                keyPositionMap);

        if (keyValues != null && keyValues.get(0) != null) {
            sessionDataKeyConsent = keyValues.get(0).getValue();
        }
        EntityUtils.consume(response.getEntity());
    }

    private void testAuthentication2(OIDCApplication application) throws Exception {

        HttpResponse response = sendLoginPost(client, sessionDataKey);
        Assert.assertNotNull(response, "Login request failed for " + application.getApplicationName() + ". response "
                + "is null.");

        Header locationHeader = response.getFirstHeader(OAuth2Constant.HTTP_RESPONSE_HEADER_LOCATION);
        Assert.assertNotNull(locationHeader, "Login response header is null for " + application.getApplicationName());
        EntityUtils.consume(response.getEntity());
        client = HttpClientBuilder.create().disableRedirectHandling().build();
        response = sendGetRequestWithoutFollowingRedirects(client, locationHeader.getValue());
        locationHeader = response.getFirstHeader(OAuth2Constant.HTTP_RESPONSE_HEADER_LOCATION);
        Assert.assertTrue(locationHeader.getValue().contains("AuthenticatedIdPs"));
        Map<String, Integer> keyPositionMap = new HashMap<>(2);
        keyPositionMap.put("name=\"sessionDataKeyConsent\"", 1);
        List<DataExtractUtil.KeyValue> keyValues = DataExtractUtil.extractSessionConsentDataFromResponse(response,
                keyPositionMap);

        if (keyValues != null && keyValues.get(0) != null) {
            sessionDataKeyConsent = keyValues.get(0).getValue();
        }
        EntityUtils.consume(response.getEntity());
    }

    private void testConsentApproval(OIDCApplication application) throws Exception {

        HttpResponse response = sendApproveAlwaysPost(client, sessionDataKeyConsent);
        Assert.assertNotNull(response, "Approval request failed for " + application.getApplicationName() + ". " +
                "response is invalid.");

        Header locationHeader = response.getFirstHeader(OAuth2Constant.HTTP_RESPONSE_HEADER_LOCATION);
        Assert.assertNotNull(locationHeader, "Approval request failed for " + application.getApplicationName() + ". "
                + "Location header is null.");
        EntityUtils.consume(response.getEntity());

        response = sendPostRequest(client, locationHeader.getValue());
        Assert.assertNotNull(response, "Authorization code response is invalid for " + application.getApplicationName
                ());

        Map<String, Integer> keyPositionMap = new HashMap<>(1);
        keyPositionMap.put("Authorization Code", 1);
        List<DataExtractUtil.KeyValue> keyValues = DataExtractUtil.extractTableRowDataFromResponse(response,
                keyPositionMap);
        Assert.assertNotNull(keyValues, "Authorization code not received for " + application.getApplicationName());

        authorizationCode = keyValues.get(0).getValue();
        Assert.assertTrue(locationHeader.getValue().contains("AuthenticatedIdPs"));
        Assert.assertNotNull(authorizationCode, "Authorization code not received for " + application
                .getApplicationName());
        EntityUtils.consume(response.getEntity());
    }

    protected void initUser() throws Exception {

        user = new OIDCUser(username, password);
        user.setProfile("default");
    }

    protected void initApplications() throws Exception {

        OIDCApplication playgroundApp = new OIDCApplication(playgroundAppOneAppName, playgroundAppOneAppContext,
                playgroundAppOneAppCallBackUri);

        applications.put(playgroundAppOneAppName, playgroundApp);

    }

    protected void createApplications() throws Exception {

        for (Map.Entry<String, OIDCApplication> entry : applications.entrySet()) {
            createApplication(entry.getValue());
        }
    }

    protected void deleteApplications() throws Exception {

        for (Map.Entry<String, OIDCApplication> entry : applications.entrySet()) {
            deleteApplication(entry.getValue());
        }
    }

    protected void deployApplications() {

        for (Map.Entry<String, OIDCApplication> entry : applications.entrySet()) {
            URL resourceUrl = getClass().getResource(File.separator + "samples" + File.separator + entry.getKey() +
                    "" + ".war");
            tomcat.addWebapp(tomcat.getHost(), entry.getValue().getApplicationContext(), resourceUrl.getPath());
        }
    }


    public HttpResponse sendGetRequestWithoutFollowingRedirects(HttpClient client, String locationURL)
            throws ClientProtocolException, IOException {
        HttpGet getRequest = new HttpGet(locationURL);
        getRequest.setHeader("User-Agent", OAuth2Constant.USER_AGENT);
        getRequest.setHeader("Accept", "text,plain,text/html,application/xhtml+xml,application/xml;q=0.9,*/*;q=0.8");
        getRequest.setHeader("Accept-Language", "en-US,en;q=0.5");
        getRequest.setHeader("Accept-Encoding", "gzip, deflate, br");
        HttpResponse response = client.execute(getRequest);
        return response;
    }

    public void createApplication(OIDCApplication application) throws Exception {

        OAuthConsumerAppDTO appDTO = new OAuthConsumerAppDTO();
        appDTO.setApplicationName(application.getApplicationName());
        appDTO.setCallbackUrl(application.getCallBackURL());
        appDTO.setOAuthVersion(OAuth2Constant.OAUTH_VERSION_2);
        appDTO.setGrantTypes("authorization_code implicit password client_credentials refresh_token " +
                "urn:ietf:params:oauth:grant-type:saml2-bearer iwa:ntlm");

        adminClient.registerOAuthApplicationData(appDTO);
        OAuthConsumerAppDTO[] appDtos = adminClient.getAllOAuthApplicationData();

        for (OAuthConsumerAppDTO appDto : appDtos) {
            if (appDto.getApplicationName().equals(application.getApplicationName())) {
                application.setClientId(appDto.getOauthConsumerKey());
                application.setClientSecret(appDto.getOauthConsumerSecret());
            }
        }

        ServiceProvider serviceProvider = new ServiceProvider();
        serviceProvider.setApplicationName(application.getApplicationName());
        serviceProvider.setDescription(application.getApplicationName());
        appMgtclient.createApplication(serviceProvider);

        serviceProvider = appMgtclient.getApplication(application.getApplicationName());

        ClaimConfig claimConfig = null;
        if (!application.getRequiredClaims().isEmpty()) {
            claimConfig = new ClaimConfig();
            for (String claimUri : application.getRequiredClaims()) {
                Claim claim = new Claim();
                claim.setClaimUri(claimUri);
                ClaimMapping claimMapping = new ClaimMapping();
                claimMapping.setRequested(true);
                claimMapping.setLocalClaim(claim);
                claimMapping.setRemoteClaim(claim);
                claimConfig.addClaimMappings(claimMapping);
            }
        }

        serviceProvider.setClaimConfig(claimConfig);
        serviceProvider.setOutboundProvisioningConfig(new OutboundProvisioningConfig());

        LocalAndOutboundAuthenticationConfig localAndOutboundAuthenticationConfig = new
                LocalAndOutboundAuthenticationConfig();
        localAndOutboundAuthenticationConfig.setAlwaysSendBackAuthenticatedListOfIdPs(true);
        serviceProvider.setLocalAndOutBoundAuthenticationConfig(localAndOutboundAuthenticationConfig);
        List<InboundAuthenticationRequestConfig> authRequestList = new ArrayList<>();

        if (application.getClientId() != null) {
            InboundAuthenticationRequestConfig inboundAuthenticationRequestConfig = new InboundAuthenticationRequestConfig();
            inboundAuthenticationRequestConfig.setInboundAuthKey(application.getClientId());
            inboundAuthenticationRequestConfig.setInboundAuthType(OAuth2Constant.OAUTH_2);
            if (StringUtils.isNotBlank(application.getClientSecret())) {
                Property property = new Property();
                property.setName(OAuth2Constant.OAUTH_CONSUMER_SECRET);
                property.setValue(application.getClientSecret());
                Property[] properties = {property};
                inboundAuthenticationRequestConfig.setProperties(properties);
            }
            authRequestList.add(inboundAuthenticationRequestConfig);
        }

        if (authRequestList.size() > 0) {
            serviceProvider.getInboundAuthenticationConfig().setInboundAuthenticationRequestConfigs(authRequestList
                    .toArray(new InboundAuthenticationRequestConfig[authRequestList.size()]));
        }

        appMgtclient.updateApplicationData(serviceProvider);
    }

}

/*
* Copyright (c) WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
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
* KIND, either express or implied. See the License for the
* specific language governing permissions and limitations
* under the License.
*/
package org.wso2.identity.integration.test.analytics.oauth;

import org.apache.axis2.context.ConfigurationContext;
import org.apache.axis2.context.ConfigurationContextFactory;
import org.apache.catalina.startup.Tomcat;
import org.apache.http.HttpResponse;
import org.apache.http.NameValuePair;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.message.BasicNameValuePair;
import org.apache.http.util.EntityUtils;
import org.testng.Assert;
import org.testng.annotations.AfterClass;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;
import org.wso2.carbon.automation.engine.context.TestUserMode;
import org.wso2.carbon.databridge.commons.Event;
import org.wso2.carbon.h2.osgi.utils.CarbonUtils;
import org.wso2.carbon.identity.oauth.stub.dto.OAuthConsumerAppDTO;
import org.wso2.carbon.integration.common.admin.client.AuthenticatorClient;
import org.wso2.carbon.integration.common.utils.exceptions.AutomationUtilException;
import org.wso2.carbon.integration.common.utils.mgt.ServerConfigurationManager;
import org.wso2.identity.integration.common.utils.ISIntegrationTest;
import org.wso2.identity.integration.test.analytics.commons.ThriftServer;
import org.wso2.identity.integration.test.oauth2.OAuth2ServiceAbstractIntegrationTest;
import org.wso2.identity.integration.test.utils.DataExtractUtil;
import org.wso2.identity.integration.test.utils.OAuth2Constant;

import javax.xml.xpath.XPathExpressionException;
import java.io.File;
import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static org.wso2.identity.integration.test.utils.DataExtractUtil.KeyValue;

public class OAuth2TokenIssuance extends OAuth2ServiceAbstractIntegrationTest {

    private static final Long WAIT_TIME = 3000L;
    private AuthenticatorClient logManger;
    private String adminUsername;
    private String adminPassword;
    private String accessToken;
    private String consumerKey;
    private String consumerSecret;
    private ThriftServer thriftServer;
    private ServerConfigurationManager serverConfigurationManager;
    private DefaultHttpClient client;
    private Tomcat tomcat;
    private OAuthConsumerAppDTO appDto;

    @BeforeClass(alwaysRun = true)
    public void testInit() throws Exception {
        super.init(TestUserMode.SUPER_TENANT_USER);
        changeIdentityXml();
        super.init(TestUserMode.SUPER_TENANT_USER);
        thriftServer = new ThriftServer("Wso2EventTestCase", 8021, true);
        thriftServer.start(8021);
        log.info("Thrift Server is Started on port 8462");
        ConfigurationContext configContext = ConfigurationContextFactory.createConfigurationContextFromFileSystem(null, null);
        logManger = new AuthenticatorClient(backendURL);
        adminUsername = userInfo.getUserName();
        adminPassword = userInfo.getPassword();
        logManger.login(isServer.getSuperTenant().getTenantAdmin().getUserName(), isServer.getSuperTenant()
                .getTenantAdmin().getPassword(), isServer.getInstance().getHosts().get("default"));

        setSystemproperties();
        client = new DefaultHttpClient();
    }

    @AfterClass(alwaysRun = true)
    public void atEnd() throws Exception {
        deleteApplication();
        removeOAuthApplicationData();
        thriftServer.stop();
        replaceIdentityXml();
        stopTomcat(tomcat);

        logManger = null;
        consumerKey = null;
        accessToken = null;
    }

    @Test(alwaysRun = true, description = "Deploy playground application")
    public void testDeployPlaygroundApp() {
        try {
            tomcat = getTomcat();
            URL resourceUrl =
                    getClass().getResource(ISIntegrationTest.URL_SEPARATOR + "samples" + ISIntegrationTest.URL_SEPARATOR +
                            "playground2.war");
            startTomcat(tomcat, OAuth2Constant.PLAYGROUND_APP_CONTEXT_ROOT, resourceUrl.getPath());

        } catch (Exception e) {
            Assert.fail("Playground application deployment failed.", e);
        }
    }

    @Test(groups = "wso2.is", description = "Check Oauth2 application registration", dependsOnMethods = "testDeployPlaygroundApp")
    public void testRegisterApplication() throws Exception {

        appDto = createApplication();
        Assert.assertNotNull(appDto, "Application creation failed.");

        consumerKey = appDto.getOauthConsumerKey();
        Assert.assertNotNull(consumerKey, "Application creation failed.");

        consumerSecret = appDto.getOauthConsumerSecret();
        Assert.assertNotNull(consumerSecret, "Application creation failed.");

    }

    @Test(groups = "wso2.is", description = "Send authorize user request", dependsOnMethods = "testRegisterApplication")
    public void testSendAuthorozedPost() throws Exception {
       try {
           String tokenIssuanceStreamId = "org.wso2.is.analytics.stream.OauthTokenIssuance:1.0.0";
           List<NameValuePair> urlParameters = new ArrayList<NameValuePair>();
           urlParameters.add(new BasicNameValuePair(
                   "grantType",
                   OAuth2Constant.OAUTH2_GRANT_TYPE_CLIENT_CREDENTIALS));
           urlParameters.add(new BasicNameValuePair("consumerKey", consumerKey));
           urlParameters.add(new BasicNameValuePair("consumerSecret", consumerSecret));
           urlParameters.add(new BasicNameValuePair("accessEndpoint",
                   OAuth2Constant.ACCESS_TOKEN_ENDPOINT));
           urlParameters.add(new BasicNameValuePair("authorize", OAuth2Constant.AUTHORIZE_PARAM));
           HttpResponse response =
                   sendPostRequestWithParameters(client, urlParameters,
                           OAuth2Constant.AUTHORIZED_USER_URL);
           Assert.assertNotNull(response, "Authorization request failed. Authorized response is null");
           EntityUtils.consume(response.getEntity());

           response = sendPostRequest(client, OAuth2Constant.AUTHORIZED_URL);

           Map<String, Integer> keyPositionMap = new HashMap<String, Integer>(1);
           keyPositionMap.put("name=\"accessToken\"", 1);

           List<KeyValue> keyValues =
                   DataExtractUtil.extractInputValueFromResponse(response,
                           keyPositionMap);
           Assert.assertNotNull(keyValues, "Access token Key value is null.");
           accessToken = keyValues.get(0).getValue();

           EntityUtils.consume(response.getEntity());

           Assert.assertNotNull(accessToken, "Access token is null.");
           waitUntilEventsReceive(1);
           Assert.assertNotNull(thriftServer.getPreservedEventList());
           Event tokenIssuanceEvent = null;
           for (Event event : thriftServer.getPreservedEventList()) {
               String streamId = event.getStreamId();
               if (tokenIssuanceStreamId.equalsIgnoreCase(streamId)) {
                   tokenIssuanceEvent = event;
               }
           }
           Assert.assertNotNull(tokenIssuanceEvent);
           Assert.assertEquals("carbon.super", tokenIssuanceEvent.getPayloadData()[1]);
           Assert.assertEquals("PRIMARY", tokenIssuanceEvent.getPayloadData()[2]);
           Assert.assertEquals(appDto.getOauthConsumerKey(), tokenIssuanceEvent.getPayloadData()[3]);
           Assert.assertEquals("client_credentials", tokenIssuanceEvent.getPayloadData()[4]);
           Assert.assertEquals("default", tokenIssuanceEvent.getPayloadData()[6]);

       } finally {
           thriftServer.resetPreservedEventList();
       }
    }

    public void changeIdentityXml() {

        log.info("Changing identity.xml file to enable analytics");
        String carbonHome = CarbonUtils.getCarbonHome();

        String analyticsEnabledIdentityXml = getISResourceLocation() + File.separator + "analytics" + File.separator
                + "config" + File.separator + "identity_token_analytics_enabled.xml";
        File defaultIdentityXml = new File(carbonHome + File.separator
                + "repository" + File.separator + "conf" + File.separator + "identity" + File.separator + "identity.xml");
        try {

            serverConfigurationManager = new ServerConfigurationManager(isServer);
            File configuredNotificationProperties = new File(analyticsEnabledIdentityXml);
            serverConfigurationManager = new ServerConfigurationManager(isServer);
            serverConfigurationManager.applyConfigurationWithoutRestart(configuredNotificationProperties,
                    defaultIdentityXml, true);
            copyAuthenticationDataPublisher();
            serverConfigurationManager.restartForcefully();

        } catch (AutomationUtilException e) {
            log.error("Error while changing configurations in identity.xml");
        } catch (XPathExpressionException e) {
            log.error("Error while changing configurations in identity.xml");
        } catch (MalformedURLException e) {
            log.error("Error while changing configurations in identity.xml");
        } catch (IOException e) {
            log.error("Error while changing configurations in identity.xml");
        }
    }

    public void copyAuthenticationDataPublisher() {
        log.info("Changing AuthenticationDataPublisher.xml file to change default port");

        String carbonHome = CarbonUtils.getCarbonHome();
        String authnDataPublisherWithOffset = getISResourceLocation() + File.separator + "analytics" + File.separator
                + "config" + File.separator + "IsAnalytics-Publisher-wso2event-OauthTokenIssueRefresh.xml";
        File defaultAuthenticationDataPublisher = new File(carbonHome + File.separator
                + "repository" + File.separator + "deployment" + File.separator + "server" + File.separator +
                "eventpublishers" + File.separator + "IsAnalytics-Publisher-wso2event-OauthTokenIssueRefresh.xml");

        try {
            File configuredAuthnPublisherFile = new File(authnDataPublisherWithOffset);
            serverConfigurationManager = new ServerConfigurationManager(isServer);
            serverConfigurationManager.applyConfigurationWithoutRestart(configuredAuthnPublisherFile,
                    defaultAuthenticationDataPublisher, true);

        } catch (AutomationUtilException e) {
            log.error("Error while changing publisher configurations");
        } catch (XPathExpressionException e) {
            log.error("Error while changing publisher configurations");
        } catch (MalformedURLException e) {
            log.error("Error while changing publisher configurations");
        } catch (IOException e) {
            log.error("Error while changing publisher configurations");
        }
    }

    public void replaceIdentityXml() {
        log.info("Changing identity.xml file to enable analytics");

        String carbonHome = CarbonUtils.getCarbonHome();

        String defaultIdentityXml = getISResourceLocation() + File.separator + "default-identity.xml";
        File defaultIdentityXmlLocation = new File(carbonHome + File.separator
                + "repository" + File.separator + "conf" + File.separator + "identity" + File.separator + "identity.xml");
        try {
            serverConfigurationManager = new ServerConfigurationManager(isServer);
            File configuredNotificationProperties = new File(defaultIdentityXml);
            serverConfigurationManager = new ServerConfigurationManager(isServer);
            serverConfigurationManager.applyConfigurationWithoutRestart(configuredNotificationProperties,
                    defaultIdentityXmlLocation, true);
            copyAuthenticationDataPublisher();
            serverConfigurationManager.restartForcefully();

        } catch (AutomationUtilException e) {
            log.error("Error while changing configurations in identity.xml to default configurations");
        } catch (XPathExpressionException e) {
            log.error("Error while changing configurations in identity.xml to default configurations");
        } catch (MalformedURLException e) {
            log.error("Error while changing configurations in identity.xml to default configurations");
        } catch (IOException e) {
            log.error("Error while changing configurations in identity.xml to default configurations");
        }
    }

    private void waitUntilEventsReceive(int eventCount) {

        long terminationTime = System.currentTimeMillis() + WAIT_TIME;
        while (System.currentTimeMillis() < terminationTime) {
            if (thriftServer.getPreservedEventList().size() == eventCount) ;
            break;
        }
    }
}
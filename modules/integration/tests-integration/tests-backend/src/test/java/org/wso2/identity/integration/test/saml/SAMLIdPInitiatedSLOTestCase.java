/*
 * Copyright (c) 2018, WSO2 LLC. (http://www.wso2.com).
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

package org.wso2.identity.integration.test.saml;

import org.apache.commons.lang.StringUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.http.HttpResponse;
import org.apache.http.util.EntityUtils;
import org.testng.Assert;
import org.testng.annotations.AfterClass;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.DataProvider;
import org.testng.annotations.Factory;
import org.testng.annotations.Test;
import org.wso2.carbon.automation.engine.context.TestUserMode;
import org.wso2.carbon.integration.common.admin.client.LogViewerClient;
import org.wso2.carbon.integration.common.utils.mgt.ServerConfigurationManager;
import org.wso2.carbon.logging.view.data.xsd.LogEvent;
import org.wso2.identity.integration.test.rest.api.server.application.management.v1.model.SAML2ServiceProvider;
import org.wso2.identity.integration.test.util.Utils;
import org.wso2.identity.integration.test.utils.CommonConstants;
import org.wso2.identity.integration.test.utils.DataExtractUtil;

import java.io.File;
import java.rmi.RemoteException;

/**
 * This test class tests SAML IdP initiated SLO functionality for two SAML applications. Since logout requests are sent
 * to every session participant asynchronously, there is no way of validating this from the application side.
 * Therefore debug log for org.wso2.carbon.identity.sso.saml.logout.LogoutRequestSender is enabled to check this
 * functionality.
 */
public class SAMLIdPInitiatedSLOTestCase extends AbstractSAMLSSOTestCase {

    private static final Log log = LogFactory.getLog(SAMLIdPInitiatedSLOTestCase.class);

    private static final String APPLICATION_ONE = "SAML-TestApplication-01";
    private static final String APPLICATION_TWO = "SAML-TestApplication-02";
    private static final String SAML_APP_ONE_ACS_URL = "http://localhost:8490/travelocity.com/home.jsp";
    private static final String SAML_APP_TWO_ACS_URL = "http://localhost:8490/travelocity.com-saml-tenantwithoutsigning/home.jsp";

    private final SAMLConfig samlConfigOne;
    private final SAMLConfig samlConfigTwo;
    private String resultPage;
    private ServerConfigurationManager serverConfigurationManager;
    private String userId;

    private LogViewerClient logViewer;

    private static final Long WAIT_TIME = 10000L;
    private String appOneId;
    private String appTwoId;
    private String tenantedSamlSSOUrl;
    private String tenantedCommonAuthUrl;

    @Factory(dataProvider = "samlConfigProvider")
    public SAMLIdPInitiatedSLOTestCase(SAMLConfig samlConfigOne, SAMLConfig samlConfigTwo) {

        if (log.isDebugEnabled()) {
            log.info("Test initialized for " + samlConfigOne + " & " + samlConfigTwo);
        }
        this.samlConfigOne = samlConfigOne;
        this.samlConfigTwo = samlConfigTwo;
    }

    @DataProvider(name = "samlConfigProvider")
    public static SAMLConfig[][] samlConfigProvider() {

        return new SAMLConfig[][]{
                {new SAMLConfig(TestUserMode.SUPER_TENANT_ADMIN, User.SUPER_TENANT_USER, HttpBinding.HTTP_POST,
                        ClaimType.LOCAL, App.SUPER_TENANT_APP_WITH_SIGNING),
                        new SAMLConfig(TestUserMode.SUPER_TENANT_ADMIN, User.SUPER_TENANT_USER, HttpBinding.HTTP_POST,
                                ClaimType.LOCAL, App.SUPER_TENANT_APP_WITH_SAMLARTIFACT_CONFIG)},
        };
    }

    @BeforeClass(alwaysRun = true)
    public void testInit() throws Exception {

        super.init(samlConfigOne.getUserMode());
        super.testInit();

        tenantedSamlSSOUrl = getTenantQualifiedURL( SAML_SSO_URL, tenantInfo.getDomain());
        tenantedCommonAuthUrl = getTenantQualifiedURL(COMMON_AUTH_URL, tenantInfo.getDomain());
        userId = super.addUser(samlConfigOne);

        appOneId = super.addApplication(samlConfigOne, APPLICATION_ONE);
        appTwoId = super.addApplication(samlConfigTwo, APPLICATION_TWO);

        logViewer = new LogViewerClient(backendURL, sessionCookie);
        changeISConfiguration();
    }

    @AfterClass(alwaysRun = true)
    public void testClear() throws Exception {

        resetISConfiguration();
        super.deleteUser(userId);
        super.deleteApp(appOneId);
        super.deleteApp(appTwoId);
        super.testClear();
    }

    private void changeISConfiguration() throws Exception {

        log.info("Replacing log4j2.properties enabling saml debug logs");
        String carbonHome = Utils.getResidentCarbonHome();
        File defaultLog4j2File = new File(carbonHome + File.separator + "repository" + File.separator
                + "conf" + File.separator + "log4j2.properties");
        File configuredLo4j2File = new File(getISResourceLocation() + File.separator + "saml" + File.separator
                + "log4j2_saml_enabled.properties");
        serverConfigurationManager = new ServerConfigurationManager(isServer);
        serverConfigurationManager.applyConfigurationWithoutRestart(configuredLo4j2File, defaultLog4j2File, true);
        // Waiting to apply the log4j2 changes
        checkForLog(logViewer, "Logging configuration applied successfully");
    }

    private void resetISConfiguration() throws Exception {

        log.info("Replacing log4j2.properties with default configurations");
        serverConfigurationManager.restoreToLastConfiguration(false);
    }

    @Test(description = "Add service providers", groups = "wso2.is", priority = 1)
    public void testAddSP() throws Exception {

        applicationMgtRestClient.updateInboundDetailsOfApplication(appOneId, getSAMLConfigurations(samlConfigOne), SAML);
        SAML2ServiceProvider samlConfig = applicationMgtRestClient.getSAMLInboundDetails(appOneId);
        Assert.assertNotNull(samlConfig, "Adding a service provider has failed for " + samlConfigOne);

        applicationMgtRestClient.updateInboundDetailsOfApplication(appTwoId, getSAMLConfigurations(samlConfigTwo),
                "saml");
        samlConfig = applicationMgtRestClient.getSAMLInboundDetails(appTwoId);
        Assert.assertNotNull(samlConfig, "Adding a service provider has failed for " + samlConfigTwo);
    }

    @Test(alwaysRun = true, description = "Testing SAML SSO login", groups = "wso2.is",
            dependsOnMethods = {"testAddSP"})
    public void testSAMLSSOLogin() {

        try {
            HttpResponse response;

            response = Utils.sendGetRequest(String.format(SAML_SSO_LOGIN_URL, samlConfigOne.getApp().getArtifact(),
                    samlConfigOne.getHttpBinding().binding), USER_AGENT, httpClient);
            String samlRequest = Utils.extractDataFromResponse(response, CommonConstants.SAML_REQUEST_PARAM, 5);
            response = super.sendSAMLMessage(tenantedSamlSSOUrl, CommonConstants.SAML_REQUEST_PARAM, samlRequest,
                    samlConfigOne);
            EntityUtils.consume(response.getEntity());

            response = Utils.sendRedirectRequest(response, USER_AGENT, ACS_URL, samlConfigOne.getApp().getArtifact(),
                    httpClient);

            String sessionKey = Utils.extractDataFromResponse(response, CommonConstants.SESSION_DATA_KEY, 1);
            response = Utils.sendPOSTMessage(sessionKey, tenantedSamlSSOUrl, USER_AGENT, ACS_URL, samlConfigOne.getApp().
                    getArtifact(), samlConfigOne.getUser().getTenantAwareUsername(), samlConfigOne.getUser().getPassword()
                    , httpClient, tenantedSamlSSOUrl);

            if (Utils.requestMissingClaims(response)) {
                String pastrCookie = Utils.getPastreCookie(response);
                Assert.assertNotNull(pastrCookie, "pastr cookie not found in response.");
                EntityUtils.consume(response.getEntity());

                response = Utils.sendPOSTConsentMessage(response, tenantedCommonAuthUrl, USER_AGENT,
                        String.format(ACS_URL, samlConfigOne.getApp().getArtifact()), httpClient, pastrCookie);
                EntityUtils.consume(response.getEntity());
            }

            String redirectUrl = Utils.getRedirectUrl(response);
            if (StringUtils.isNotBlank(redirectUrl)) {
                response = Utils.sendRedirectRequest(response, USER_AGENT, ACS_URL, samlConfigOne.getApp().
                        getArtifact(), httpClient);
            }

            String samlResponse = Utils.extractDataFromResponse(response, CommonConstants.SAML_RESPONSE_PARAM, 5);
            EntityUtils.consume(response.getEntity());

            response = super.sendSAMLMessage(String.format(ACS_URL, samlConfigOne.getApp().getArtifact()),
                    CommonConstants.SAML_RESPONSE_PARAM, samlResponse, samlConfigOne);
            resultPage = DataExtractUtil.getContentData(response);

            Assert.assertTrue(resultPage.contains("You are logged in as " + userId),
                    "SAML SSO Login failed for " + samlConfigOne.getApp().getArtifact());

        } catch (Exception e) {
            Assert.fail("SAML SSO Login test failed for " + samlConfigOne.getApp().getArtifact(), e);
        }

        doSSOtoAppTwo();
    }

    @Test(alwaysRun = true, description = "Testing SAML IdP initiated SLO", groups = "wso2.is",
            dependsOnMethods = {"testSAMLSSOLogin"})
    public void testSAMLIdpInitiatedSLO() throws Exception {

        try {
            logViewer.clearLogs();

            HttpResponse response = Utils.sendGetRequest(getTenantQualifiedURL(SAML_IDP_SLO_URL,
                    tenantInfo.getDomain()), USER_AGENT, httpClient);
            String resultPage = DataExtractUtil.getContentData(response);

            Assert.assertTrue(resultPage.contains("You have successfully logged out") &&
                    !resultPage.contains("error"), "SAML IdP initiated SLO failed for " +
                    samlConfigOne.getApp().getArtifact() + " & " + samlConfigTwo.getApp().getArtifact());

            boolean requestOneSentLogFound = checkForLog(logViewer,
                    "single logout request is sent to : " + SAML_APP_ONE_ACS_URL + " is returned with OK");
            Assert.assertTrue(requestOneSentLogFound, "System Log not found. Single logout request is not " +
                    "sent to travelocity.com app.");

            boolean requestTwoSentLogFound = checkForLog(logViewer,
                    "single logout request is sent to : " + SAML_APP_TWO_ACS_URL + " is returned with OK");
            Assert.assertTrue(requestTwoSentLogFound, "System Log not found. Single logout request is not " +
                    "sent to travelocity.com-saml-tenantwithoutsigning app.");

            boolean responseOneReceivedLogFound = checkForLog(logViewer,
                    "Logout response received for issuer: travelocity.com for tenant domain: carbon.super");
            Assert.assertTrue(responseOneReceivedLogFound, "System Log not found. Logout response is not " +
                    "received for issuer travelocity.com");

            boolean responseTwoReceivedLogFound = checkForLog(logViewer,
                    "Logout response received for issuer: travelocity.com-saml-tenantwithoutsigning for " +
                            "tenant domain: carbon.super");
            Assert.assertTrue(responseTwoReceivedLogFound, "System Log not found. Logout response is not " +
                    "received for issuer travelocity.com-saml-tenantwithoutsigning");
        } catch (Exception e) {
            Assert.fail("SAML IdP initiated SLO test failed for " + samlConfigOne.getApp().getArtifact()
                    + " & " + samlConfigTwo.getApp().getArtifact(), e);
        }
    }

    protected void doSSOtoAppTwo() {

        try {
            HttpResponse response;

            response = Utils.sendGetRequest(String.format(SAML_SSO_LOGIN_URL, samlConfigTwo.getApp().getArtifact(),
                    samlConfigTwo.getHttpBinding().binding), USER_AGENT, httpClient);
            String samlRequest = Utils.extractDataFromResponse(response, CommonConstants.SAML_REQUEST_PARAM, 5);
            response = super.sendSAMLMessage(tenantedSamlSSOUrl, CommonConstants.SAML_REQUEST_PARAM, samlRequest,
                    samlConfigTwo);

            if (Utils.requestMissingClaims(response)) {
                String pastrCookie = Utils.getPastreCookie(response);
                Assert.assertNotNull(pastrCookie, "pastr cookie not found in response.");
                EntityUtils.consume(response.getEntity());
                response = Utils.sendPOSTConsentMessage(response,  tenantedCommonAuthUrl, USER_AGENT,
                        String.format(ACS_URL, samlConfigTwo.getApp().getArtifact()), httpClient, pastrCookie);
                EntityUtils.consume(response.getEntity());
            }

            String redirectUrl = Utils.getRedirectUrl(response);
            if (StringUtils.isNotBlank(redirectUrl)) {
                response = Utils.sendRedirectRequest(response, USER_AGENT, ACS_URL, samlConfigTwo.getApp().
                        getArtifact(), httpClient);
            }

            String samlResponse = Utils.extractDataFromResponse(response, CommonConstants.SAML_RESPONSE_PARAM, 5);
            EntityUtils.consume(response.getEntity());
            response = super.sendSAMLMessage(String.format(ACS_URL, samlConfigTwo.getApp().getArtifact()),
                    CommonConstants.SAML_RESPONSE_PARAM, samlResponse, samlConfigTwo);
            resultPage = DataExtractUtil.getContentData(response);

            Assert.assertTrue(resultPage.contains("You are logged in as " + userId),
                    "SAML SSO Login failed for " + samlConfigTwo.getApp().
                    getArtifact());
        } catch (Exception e) {
            Assert.fail("SAML SSO Login test failed for " + samlConfigTwo.getApp().getArtifact(), e);
        }
    }

    protected static boolean checkForLog(LogViewerClient logViewerClient, String expected) throws
            InterruptedException, RemoteException {

        boolean logExists = false;
        long terminationTime = System.currentTimeMillis() + WAIT_TIME;
        while (System.currentTimeMillis() < terminationTime) {
            if (assertIfLogExists(logViewerClient, expected)) {
                logExists = true;
                break;
            }
        }
        return logExists;
    }

    protected static boolean assertIfLogExists(LogViewerClient logViewerClient, String expected)
            throws RemoteException {

        LogEvent[] systemLogs;
        systemLogs = logViewerClient.getAllRemoteSystemLogs();
        boolean matchFound = false;
        if (systemLogs != null) {
            for (LogEvent logEvent : systemLogs) {
                if (logEvent == null) {
                    continue;
                }
                if (logEvent.getMessage().contains(expected)) {
                    matchFound = true;
                    break;
                }
            }
        }
        return matchFound;
    }
}

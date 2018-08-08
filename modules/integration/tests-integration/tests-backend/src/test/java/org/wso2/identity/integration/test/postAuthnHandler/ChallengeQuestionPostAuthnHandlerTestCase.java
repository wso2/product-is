/*
 * Copyright (c) 2018, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
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
package org.wso2.identity.integration.test.postAuthnHandler;


import org.apache.axis2.context.ConfigurationContext;
import org.apache.axis2.context.ConfigurationContextFactory;
import org.apache.catalina.startup.Tomcat;
import org.apache.commons.lang.ArrayUtils;
import org.apache.commons.lang.StringUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.http.HttpResponse;
import org.apache.http.NameValuePair;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClientBuilder;
import org.apache.http.message.BasicNameValuePair;
import org.apache.http.util.EntityUtils;
import org.testng.Assert;
import org.testng.annotations.AfterClass;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.DataProvider;
import org.testng.annotations.Factory;
import org.testng.annotations.Test;
import org.wso2.carbon.automation.engine.context.TestUserMode;
import org.wso2.carbon.identity.application.common.model.idp.xsd.FederatedAuthenticatorConfig;
import org.wso2.carbon.identity.application.common.model.idp.xsd.IdentityProvider;
import org.wso2.carbon.identity.application.common.model.idp.xsd.IdentityProviderProperty;
import org.wso2.carbon.identity.application.common.model.xsd.Claim;
import org.wso2.carbon.identity.application.common.model.xsd.ClaimMapping;
import org.wso2.carbon.identity.application.common.model.xsd.InboundAuthenticationConfig;
import org.wso2.carbon.identity.application.common.model.xsd.InboundAuthenticationRequestConfig;
import org.wso2.carbon.identity.application.common.model.xsd.Property;
import org.wso2.carbon.identity.application.common.model.xsd.ServiceProvider;
import org.wso2.carbon.identity.sso.saml.stub.types.SAMLSSOServiceProviderDTO;
import org.wso2.carbon.integration.common.admin.client.AuthenticatorClient;
import org.wso2.carbon.um.ws.api.stub.ClaimValue;
import org.wso2.identity.integration.common.clients.Idp.IdentityProviderMgtServiceClient;
import org.wso2.identity.integration.common.clients.application.mgt.ApplicationManagementServiceClient;
import org.wso2.identity.integration.common.clients.sso.saml.SAMLSSOConfigServiceClient;
import org.wso2.identity.integration.common.clients.usermgt.remote.RemoteUserStoreManagerServiceClient;
import org.wso2.identity.integration.common.utils.ISIntegrationTest;
import org.wso2.identity.integration.test.util.Utils;
import org.wso2.identity.integration.test.utils.CommonConstants;

import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;

public class ChallengeQuestionPostAuthnHandlerTestCase extends ISIntegrationTest {

    private static final String TENANT_DOMAIN_PARAM = "tenantDomain";
    private static final Log log = LogFactory.getLog(ChallengeQuestionPostAuthnHandlerTestCase.class);
    // SAML Application attributes
    private static final String USER_AGENT = "Apache-HttpClient/4.2.5 (java 1.5)";
    private static final String APPLICATION_NAME = "SAML-SSO-TestApplication";
    private static final String INBOUND_AUTH_TYPE = "samlsso";
    private static final String ATTRIBUTE_CS_INDEX_VALUE = "1239245949";
    private static final String ATTRIBUTE_CS_INDEX_NAME = "attrConsumServiceIndex";
    private static final String SAML_SSO_URL = "https://localhost:9853/samlsso";
    private static final String ACS_URL = "http://localhost:8490/%s/home.jsp";
    private static final String COMMON_AUTH_URL = "https://localhost:9853/commonauth";
    private static final String SAML_SSO_LOGIN_URL =
            "http://localhost:8490/%s/samlsso?SAML2.HTTPBinding=%s";

    private static final String NAMEID_FORMAT =
            "urn:oasis:names:tc:SAML:1.1:nameid-format:emailAddress";
    private static final String LOGIN_URL = "/carbon/admin/login.jsp";

    //Claim Uris
    private static final String FIRST_NAME_CLAIM_URI = "http://wso2.org/claims/givenname";
    private static final String LAST_NAME_CLAIM_URI = "http://wso2.org/claims/lastname";
    private static final String EMAIL_CLAIM_URI = "http://wso2.org/claims/emailaddress";

    //Force challenge question attributes
    private static final String PROFILE_NAME = "default";
    private static final String FORCE_ADD_PW_RECOVERY_QUESTION = "Recovery.Question.Password.Forced.Enable";

    private static final String ADMIN = "admin";

    private ApplicationManagementServiceClient applicationManagementServiceClient;
    private SAMLSSOConfigServiceClient ssoConfigServiceClient;
    private RemoteUserStoreManagerServiceClient remoteUSMServiceClient;
    private SAMLConfig config;
    private Tomcat tomcatServer;
    private IdentityProviderMgtServiceClient superTenantIDPMgtClient;
    private IdentityProviderMgtServiceClient tenantIDPMgtClient;
    private IdentityProvider superTenantResidentIDP;
    private String resultPage;

    @Factory(dataProvider = "samlConfigProvider")
    public ChallengeQuestionPostAuthnHandlerTestCase(SAMLConfig config) {
        if (log.isDebugEnabled()) {
            log.debug("Missing Challenge Question post-authentication handler Test initialized for " + config);
        }
        this.config = config;
    }

    @DataProvider(name = "samlConfigProvider")
    public static SAMLConfig[][] samlConfigProvider() {
        return new SAMLConfig[][]{
                {new SAMLConfig(TestUserMode.SUPER_TENANT_ADMIN, User.SUPER_TENANT_USER, HttpBinding.HTTP_REDIRECT,
                        ClaimType.NONE, App.SUPER_TENANT_APP_WITH_SIGNING)},
        };
    }

    @BeforeClass(alwaysRun = true)
    public void testInit() throws Exception {
        super.init(config.getUserMode());
        ConfigurationContext configContext = ConfigurationContextFactory
                .createConfigurationContextFromFileSystem(null, null);
        applicationManagementServiceClient =
                new ApplicationManagementServiceClient(sessionCookie, backendURL, configContext);
        ssoConfigServiceClient =
                new SAMLSSOConfigServiceClient(backendURL, sessionCookie);
        remoteUSMServiceClient = new RemoteUserStoreManagerServiceClient(backendURL, sessionCookie);

        createUser();
        createApplication();

        //Starting tomcat
        log.info("Starting Tomcat");
        tomcatServer = Utils.getTomcat(getClass());

        URL resourceUrl = getClass()
                .getResource(File.separator + "samples" + File.separator + config.getApp().getArtifact() + ".war");
        Utils.startTomcat(tomcatServer, "/" + config.getApp().getArtifact(), resourceUrl.getPath());

        AuthenticatorClient logManager = new AuthenticatorClient(backendURL);
        String secondaryTenantDomain = isServer.getTenantList().get(1);

        String tenantCookie = logManager.login(ADMIN + "@" + secondaryTenantDomain,
                ADMIN, isServer.getInstance().getHosts().get("default"));

        superTenantIDPMgtClient = new IdentityProviderMgtServiceClient(sessionCookie, backendURL);
        tenantIDPMgtClient = new IdentityProviderMgtServiceClient(tenantCookie, backendURL);
        superTenantResidentIDP = superTenantIDPMgtClient.getResidentIdP();
    }

    @AfterClass(alwaysRun = true)
    public void testClear() throws Exception {
        deleteUser();
        deleteApplication();

        ssoConfigServiceClient = null;
        applicationManagementServiceClient = null;
        remoteUSMServiceClient = null;
        //Stopping tomcat
        tomcatServer.stop();
        tomcatServer.destroy();
        Thread.sleep(10000);
    }

    @Test(description = "Add service provider", groups = "wso2.is", priority = 1)
    public void testAddSP() throws Exception {
        boolean isAddSuccess = ssoConfigServiceClient
                .addServiceProvider(createSsoServiceProviderDTO());
        Assert.assertTrue(isAddSuccess, "Adding a service provider has failed for " + config);

        SAMLSSOServiceProviderDTO[] samlssoServiceProviderDTOs = ssoConfigServiceClient
                .getServiceProviders().getServiceProviders();
        Assert.assertEquals(samlssoServiceProviderDTOs[0].getIssuer(), config.getApp().getArtifact(),
                "Adding a service provider has failed for " + config);
    }

    @Test(alwaysRun = true, description = "Testing login when the default setting for the Force Challenge Questions " +
            "is used",
            groups = "wso2.is",
            dependsOnMethods = {"testAddSP"})
    public void testLoginWithDefaultSetting() {
        try (CloseableHttpClient httpClient = HttpClientBuilder.create().build()) {
            HttpResponse response;
            // Update resident IDP property for forcing challenge questions
            response = Utils.sendGetRequest(String.format(SAML_SSO_LOGIN_URL, config.getApp().getArtifact(), config
                    .getHttpBinding().binding), USER_AGENT, httpClient);
            if (config.getHttpBinding() == HttpBinding.HTTP_POST) {
                String samlRequest = Utils.extractDataFromResponse(response, CommonConstants.SAML_REQUEST_PARAM, 5);
                response = sendSAMLMessage(SAML_SSO_URL, CommonConstants.SAML_REQUEST_PARAM, samlRequest, httpClient);
                EntityUtils.consume(response.getEntity());
                response = Utils.sendRedirectRequest(response, USER_AGENT, ACS_URL, config.getApp().getArtifact(),
                        httpClient);
            }
            String sessionKey = Utils.extractDataFromResponse(response, CommonConstants.SESSION_DATA_KEY, 1);
            response = Utils.sendPOSTMessage(sessionKey, SAML_SSO_URL, USER_AGENT, ACS_URL, config.getApp()
                    .getArtifact(), config.getUser().getUsername(), config.getUser().getPassword(), httpClient);

            // Assert for not invoking missing challenge question post authentication handler
            Assert.assertFalse(isChallengeQuestionsRequested(response), "Missing challenge questions post " +
                    "authentication handler invoked when default setting is false");
            // Check for the missing claim handler post authenticator
            if (requestMissingClaims(response)) {
                String pastrCookie = Utils.getPastreCookie(response);
                EntityUtils.consume(response.getEntity());
                response = Utils.sendPOSTConsentMessage(response, COMMON_AUTH_URL, USER_AGENT, String.format(ACS_URL, config.getApp()
                        .getArtifact()), httpClient, pastrCookie);
                EntityUtils.consume(response.getEntity());
            }
            String redirectUrl = Utils.getRedirectUrl(response);
            if (StringUtils.isNotBlank(redirectUrl)) {
                EntityUtils.consume(response.getEntity());
                response = Utils.sendRedirectRequest(response, USER_AGENT, ACS_URL, config.getApp().getArtifact(),
                        httpClient);
            }
            String samlResponse = Utils.extractDataFromResponse(response, CommonConstants.SAML_RESPONSE_PARAM, 5);
            EntityUtils.consume(response.getEntity());

            response = sendSAMLMessage(String.format(ACS_URL, config.getApp().getArtifact()), CommonConstants
                    .SAML_RESPONSE_PARAM, samlResponse, httpClient);
            resultPage = extractDataFromResponse(response);
            Assert.assertTrue(resultPage.contains("You are logged in as " + config.getUser().getTenantAwareUsername()),
                    "SAML SSO Login failed for " + config);
            EntityUtils.consume(response.getEntity());
        } catch (Exception e) {
            Assert.fail("Missing Challenge Question post authentication handler failed for " + config, e);
        }
    }

    @Test(alwaysRun = true, description = "Testing login when the Force Challenge Questions setting is disabled",
            groups = "wso2.is",
            dependsOnMethods = {"testAddSP"})
    public void testLoginWithDisabledSetting() {
        try (CloseableHttpClient httpClient = HttpClientBuilder.create().build()) {
            HttpResponse response;
            // Update resident IDP property for forcing challenge questions
            updateResidentIDPProperty(superTenantResidentIDP, FORCE_ADD_PW_RECOVERY_QUESTION, "false", true);
            response = Utils.sendGetRequest(String.format(SAML_SSO_LOGIN_URL, config.getApp().getArtifact(), config
                    .getHttpBinding().binding), USER_AGENT, httpClient);
            if (config.getHttpBinding() == HttpBinding.HTTP_POST) {
                String samlRequest = Utils.extractDataFromResponse(response, CommonConstants.SAML_REQUEST_PARAM, 5);
                response = sendSAMLMessage(SAML_SSO_URL, CommonConstants.SAML_REQUEST_PARAM, samlRequest, httpClient);
                EntityUtils.consume(response.getEntity());
                response = Utils.sendRedirectRequest(response, USER_AGENT, ACS_URL, config.getApp().getArtifact(),
                        httpClient);
            }
            String sessionKey = Utils.extractDataFromResponse(response, CommonConstants.SESSION_DATA_KEY, 1);
            response = Utils.sendPOSTMessage(sessionKey, SAML_SSO_URL, USER_AGENT, ACS_URL, config.getApp()
                    .getArtifact(), config.getUser().getUsername(), config.getUser().getPassword(), httpClient);

            // Assert for not invoking missing challenge question post authentication handler
            Assert.assertFalse(isChallengeQuestionsRequested(response), "Missing challenge questions post " +
                    "authentication handler invoked when default setting is false");
            // Check for the missing claim handler post authenticator
            if (requestMissingClaims(response)) {
                String pastrCookie = Utils.getPastreCookie(response);
                EntityUtils.consume(response.getEntity());
                response = Utils.sendPOSTConsentMessage(response, COMMON_AUTH_URL, USER_AGENT, String.format(ACS_URL, config.getApp()
                        .getArtifact()), httpClient, pastrCookie);
                EntityUtils.consume(response.getEntity());
            }
            String redirectUrl = Utils.getRedirectUrl(response);
            if (StringUtils.isNotBlank(redirectUrl)) {
                EntityUtils.consume(response.getEntity());
                response = Utils.sendRedirectRequest(response, USER_AGENT, ACS_URL, config.getApp().getArtifact(),
                        httpClient);
            }
            String samlResponse = Utils.extractDataFromResponse(response, CommonConstants.SAML_RESPONSE_PARAM, 5);
            EntityUtils.consume(response.getEntity());
            response = sendSAMLMessage(String.format(ACS_URL, config.getApp().getArtifact()), CommonConstants
                    .SAML_RESPONSE_PARAM, samlResponse, httpClient);
            resultPage = extractDataFromResponse(response);
            Assert.assertTrue(resultPage.contains("You are logged in as " + config.getUser().getTenantAwareUsername()),
                    "SAML SSO Login failed for " + config);
            EntityUtils.consume(response.getEntity());
        } catch (Exception e) {
            Assert.fail("Missing Challenge Question post authentication handler failed for " + config, e);
        }
    }

    @Test(alwaysRun = true, description = "Testing login when the Force Challenge Questions setting is enabled",
            groups = "wso2.is",
            dependsOnMethods = {"testAddSP"})
    public void testLoginWithEnabledSetting() {
        try (CloseableHttpClient httpClient = HttpClientBuilder.create().build()) {
            HttpResponse response;
            // Update resident IDP property for forcing challenge questions
            updateResidentIDPProperty(superTenantResidentIDP, FORCE_ADD_PW_RECOVERY_QUESTION, "true", true);
            response = Utils.sendGetRequest(String.format(SAML_SSO_LOGIN_URL, config.getApp().getArtifact(), config
                    .getHttpBinding().binding), USER_AGENT, httpClient);
            if (config.getHttpBinding() == HttpBinding.HTTP_POST) {
                String samlRequest = Utils.extractDataFromResponse(response, CommonConstants.SAML_REQUEST_PARAM, 5);
                response = sendSAMLMessage(SAML_SSO_URL, CommonConstants.SAML_REQUEST_PARAM, samlRequest, httpClient);
                EntityUtils.consume(response.getEntity());
                response = Utils.sendRedirectRequest(response, USER_AGENT, ACS_URL, config.getApp().getArtifact(),
                        httpClient);
            }
            String sessionKey = Utils.extractDataFromResponse(response, CommonConstants.SESSION_DATA_KEY, 1);
            response = Utils.sendPOSTMessage(sessionKey, SAML_SSO_URL, USER_AGENT, ACS_URL, config.getApp()
                    .getArtifact(), config.getUser().getUsername(), config.getUser().getPassword(), httpClient);
            // Check whether missing challenge question post authentication handler invoked and sent the redirect
            Assert.assertTrue(isChallengeQuestionsRequested(response), "Missing challenge questions post " +
                    "authenticator not invoked as expected");
            if (isChallengeQuestionsRequested(response)) {
                String pastrCookie = Utils.getPastreCookie(response);
                Assert.assertNotNull(pastrCookie, "pastr cookie not found in response.");
                EntityUtils.consume(response.getEntity());
                response = Utils.sendPOSTChallengeQuestionResponse(response, COMMON_AUTH_URL, USER_AGENT, String.format(ACS_URL, config.getApp()
                        .getArtifact()), httpClient, pastrCookie);
            }
            // Check for the missing claim handler post authenticator
            if (requestMissingClaims(response)) {
                String pastrCookie = Utils.getPastreCookie(response);
                EntityUtils.consume(response.getEntity());

                response = Utils.sendPOSTConsentMessage(response, COMMON_AUTH_URL, USER_AGENT, String.format(ACS_URL, config.getApp()
                        .getArtifact()), httpClient, pastrCookie);
                EntityUtils.consume(response.getEntity());
            }
            String redirectUrl = Utils.getRedirectUrl(response);
            if (StringUtils.isNotBlank(redirectUrl)) {
                EntityUtils.consume(response.getEntity());
                response = Utils.sendRedirectRequest(response, USER_AGENT, ACS_URL, config.getApp().getArtifact(),
                        httpClient);
            }
            String samlResponse = Utils.extractDataFromResponse(response, CommonConstants.SAML_RESPONSE_PARAM, 5);
            EntityUtils.consume(response.getEntity());
            response = sendSAMLMessage(String.format(ACS_URL, config.getApp().getArtifact()), CommonConstants
                    .SAML_RESPONSE_PARAM, samlResponse, httpClient);
            resultPage = extractDataFromResponse(response);
            Assert.assertTrue(resultPage.contains("You are logged in as " + config.getUser().getTenantAwareUsername()),
                    "SAML SSO Login failed for " + config);
            EntityUtils.consume(response.getEntity());

        } catch (Exception e) {
            Assert.fail("Missing Challenge Question post authentication handler failed for " + config, e);
        }
    }

    @Test(alwaysRun = true, description = "Testing login when the user already has given challenge questions",
            groups = "wso2.is",
            dependsOnMethods = {"testAddSP", "testLoginWithEnabledSetting"})
    public void testLoginWithChallengeQuestions() {
        try (CloseableHttpClient httpClient = HttpClientBuilder.create().build()) {
            HttpResponse response;
            // Update resident IDP property for forcing challenge questions
            updateResidentIDPProperty(superTenantResidentIDP, FORCE_ADD_PW_RECOVERY_QUESTION, "true", true);
            response = Utils.sendGetRequest(String.format(SAML_SSO_LOGIN_URL, config.getApp().getArtifact(), config
                    .getHttpBinding().binding), USER_AGENT, httpClient);
            if (config.getHttpBinding() == HttpBinding.HTTP_POST) {
                String samlRequest = Utils.extractDataFromResponse(response, CommonConstants.SAML_REQUEST_PARAM, 5);
                response = sendSAMLMessage(SAML_SSO_URL, CommonConstants.SAML_REQUEST_PARAM, samlRequest, httpClient);
                EntityUtils.consume(response.getEntity());
                response = Utils.sendRedirectRequest(response, USER_AGENT, ACS_URL, config.getApp().getArtifact(),
                        httpClient);
            }
            String sessionKey = Utils.extractDataFromResponse(response, CommonConstants.SESSION_DATA_KEY, 1);
            response = Utils.sendPOSTMessage(sessionKey, SAML_SSO_URL, USER_AGENT, ACS_URL, config.getApp()
                    .getArtifact(), config.getUser().getUsername(), config.getUser().getPassword(), httpClient);
            // Assert for not invoking missing challenge question post authentication handler
            Assert.assertFalse(isChallengeQuestionsRequested(response), "Challenge questions were not added for" +
                    "the user " + config.getUser().toString() + "from the previous test case " +
                    "[testLoginWithEnabledSetting]");
            // Check for the missing claim handler post authenticator
            if (requestMissingClaims(response)) {
                String pastrCookie = Utils.getPastreCookie(response);
                EntityUtils.consume(response.getEntity());
                response = Utils.sendPOSTConsentMessage(response, COMMON_AUTH_URL, USER_AGENT, String.format(ACS_URL, config.getApp()
                        .getArtifact()), httpClient, pastrCookie);
                EntityUtils.consume(response.getEntity());
            }
            String redirectUrl = Utils.getRedirectUrl(response);
            if (StringUtils.isNotBlank(redirectUrl)) {
                EntityUtils.consume(response.getEntity());
                response = Utils.sendRedirectRequest(response, USER_AGENT, ACS_URL, config.getApp().getArtifact(),
                        httpClient);
            }
            String samlResponse = Utils.extractDataFromResponse(response, CommonConstants.SAML_RESPONSE_PARAM, 5);
            EntityUtils.consume(response.getEntity());
            response = sendSAMLMessage(String.format(ACS_URL, config.getApp().getArtifact()), CommonConstants
                    .SAML_RESPONSE_PARAM, samlResponse, httpClient);
            resultPage = extractDataFromResponse(response);
            Assert.assertTrue(resultPage.contains("You are logged in as " + config.getUser().getTenantAwareUsername()),
                    "Missing Challenge Question post authentication handler failed for " + config);
            EntityUtils.consume(response.getEntity());

        } catch (Exception e) {
            Assert.fail("Missing Challenge Question post authentication handler failed for " + config, e);
        }
    }

    private HttpResponse sendSAMLMessage(String url, String samlMsgKey, String samlMsgValue, CloseableHttpClient httpClient) throws IOException {
        List<NameValuePair> urlParameters = new ArrayList<NameValuePair>();
        HttpPost post = new HttpPost(url);
        post.setHeader("User-Agent", USER_AGENT);
        urlParameters.add(new BasicNameValuePair(samlMsgKey, samlMsgValue));
        if (config.getUserMode() == TestUserMode.TENANT_ADMIN || config.getUserMode() == TestUserMode.TENANT_USER) {
            urlParameters.add(new BasicNameValuePair(TENANT_DOMAIN_PARAM, config.getUser().getTenantDomain()));
        }
        post.setEntity(new UrlEncodedFormEntity(urlParameters));
        return httpClient.execute(post);
    }

    private String extractDataFromResponse(HttpResponse response) throws IOException {
        BufferedReader rd = new BufferedReader(
                new InputStreamReader(response.getEntity().getContent()));
        StringBuilder result = new StringBuilder();
        String line;
        while ((line = rd.readLine()) != null) {
            result.append(line);
        }
        rd.close();
        return result.toString();
    }

    private void createApplication() throws Exception {
        ServiceProvider serviceProvider = new ServiceProvider();
        serviceProvider.setApplicationName(APPLICATION_NAME);
        serviceProvider.setDescription("This is a test Service Provider");
        applicationManagementServiceClient.createApplication(serviceProvider);

        serviceProvider = applicationManagementServiceClient.getApplication(APPLICATION_NAME);

        serviceProvider.getClaimConfig().setClaimMappings(getClaimMappings());

        InboundAuthenticationRequestConfig requestConfig = new InboundAuthenticationRequestConfig();
        requestConfig.setInboundAuthType(INBOUND_AUTH_TYPE);
        requestConfig.setInboundAuthKey(config.getApp().getArtifact());

        Property attributeConsumerServiceIndexProp = new Property();
        attributeConsumerServiceIndexProp.setName(ATTRIBUTE_CS_INDEX_NAME);
        attributeConsumerServiceIndexProp.setValue(ATTRIBUTE_CS_INDEX_VALUE);
        requestConfig.setProperties(new Property[]{attributeConsumerServiceIndexProp});

        InboundAuthenticationConfig inboundAuthenticationConfig = new InboundAuthenticationConfig();
        inboundAuthenticationConfig.setInboundAuthenticationRequestConfigs(
                new InboundAuthenticationRequestConfig[]{requestConfig});

        serviceProvider.setInboundAuthenticationConfig(inboundAuthenticationConfig);
        applicationManagementServiceClient.updateApplicationData(serviceProvider);
    }

    private void deleteApplication() throws Exception {
        applicationManagementServiceClient.deleteApplication(APPLICATION_NAME);
    }

    private void createUser() {
        log.info("Creating User " + config.getUser().getUsername());
        try {
            // creating the user
            remoteUSMServiceClient.addUser(config.getUser().getTenantAwareUsername(), config.getUser().getPassword(),
                    null, getUserClaims(config.getUser().getSetUserClaims()),
                    PROFILE_NAME, true);
        } catch (Exception e) {
            Assert.fail("Error while creating the user", e);
        }

    }

    private void deleteUser() {
        log.info("Deleting User " + config.getUser().getUsername());
        try {
            remoteUSMServiceClient.deleteUser(config.getUser().getTenantAwareUsername());
        } catch (Exception e) {
            Assert.fail("Error while deleting the user", e);
        }
    }

    private SAMLSSOServiceProviderDTO createSsoServiceProviderDTO() {
        SAMLSSOServiceProviderDTO samlssoServiceProviderDTO = new SAMLSSOServiceProviderDTO();
        samlssoServiceProviderDTO.setIssuer(config.getApp().getArtifact());
        samlssoServiceProviderDTO.setAssertionConsumerUrls(new String[]{String.format(ACS_URL,
                config.getApp().getArtifact())});
        samlssoServiceProviderDTO.setDefaultAssertionConsumerUrl(String.format(ACS_URL, config.getApp().getArtifact()));
        samlssoServiceProviderDTO.setAttributeConsumingServiceIndex(ATTRIBUTE_CS_INDEX_VALUE);
        samlssoServiceProviderDTO.setNameIDFormat(NAMEID_FORMAT);
        samlssoServiceProviderDTO.setDoSignAssertions(config.getApp().isSigningEnabled());
        samlssoServiceProviderDTO.setDoSignResponse(config.getApp().isSigningEnabled());
        samlssoServiceProviderDTO.setDoSingleLogout(true);
        samlssoServiceProviderDTO.setLoginPageURL(LOGIN_URL);
        if (config.getClaimType() != ClaimType.NONE) {
            samlssoServiceProviderDTO.setEnableAttributeProfile(true);
            samlssoServiceProviderDTO.setEnableAttributesByDefault(true);
        }

        return samlssoServiceProviderDTO;
    }

    private ClaimMapping[] getClaimMappings() {
        List<ClaimMapping> claimMappingList = new ArrayList<>();

        Claim firstNameClaim = new Claim();
        firstNameClaim.setClaimUri(FIRST_NAME_CLAIM_URI);
        ClaimMapping firstNameClaimMapping = new ClaimMapping();
        firstNameClaimMapping.setRequested(true);
        firstNameClaimMapping.setLocalClaim(firstNameClaim);
        firstNameClaimMapping.setRemoteClaim(firstNameClaim);
        claimMappingList.add(firstNameClaimMapping);

        Claim lastNameClaim = new Claim();
        lastNameClaim.setClaimUri(LAST_NAME_CLAIM_URI);
        ClaimMapping lastNameClaimMapping = new ClaimMapping();
        lastNameClaimMapping.setRequested(true);
        lastNameClaimMapping.setLocalClaim(lastNameClaim);
        lastNameClaimMapping.setRemoteClaim(lastNameClaim);
        claimMappingList.add(lastNameClaimMapping);

        Claim emailClaim = new Claim();
        emailClaim.setClaimUri(EMAIL_CLAIM_URI);
        ClaimMapping emailClaimMapping = new ClaimMapping();
        emailClaimMapping.setRequested(true);
        emailClaimMapping.setLocalClaim(emailClaim);
        emailClaimMapping.setRemoteClaim(emailClaim);
        claimMappingList.add(emailClaimMapping);

        return claimMappingList.toArray(new ClaimMapping[claimMappingList.size()]);
    }

    private ClaimValue[] getUserClaims(boolean setClaims) {

        ClaimValue[] claimValues;

        if (setClaims) {
            claimValues = new ClaimValue[3];

            ClaimValue firstName = new ClaimValue();
            firstName.setClaimURI(FIRST_NAME_CLAIM_URI);
            firstName.setValue(config.getUser().getNickname());
            claimValues[0] = firstName;

            ClaimValue lastName = new ClaimValue();
            lastName.setClaimURI(LAST_NAME_CLAIM_URI);
            lastName.setValue(config.getUser().getUsername());
            claimValues[1] = lastName;

            ClaimValue email = new ClaimValue();
            email.setClaimURI(EMAIL_CLAIM_URI);
            email.setValue(config.getUser().getEmail());
            claimValues[2] = email;
        } else {
            claimValues = new ClaimValue[1];

            ClaimValue lastName = new ClaimValue();
            lastName.setClaimURI(LAST_NAME_CLAIM_URI);
            lastName.setValue(config.getUser().getUsername());
            claimValues[0] = lastName;
        }

        return claimValues;
    }

    private boolean requestMissingClaims(HttpResponse response) {

        String redirectUrl = Utils.getRedirectUrl(response);
        return redirectUrl.contains("consent.do");
    }

    private boolean isChallengeQuestionsRequested(HttpResponse response) {

        String redirectUrl = Utils.getRedirectUrl(response);
        return redirectUrl.contains("add-security-questions");
    }

    private void updateResidentIDP(IdentityProvider residentIdentityProvider, boolean isSuperTenant) throws Exception {

        FederatedAuthenticatorConfig[] federatedAuthenticatorConfigs =
                residentIdentityProvider.getFederatedAuthenticatorConfigs();
        for (FederatedAuthenticatorConfig authenticatorConfig : federatedAuthenticatorConfigs) {
            if (!authenticatorConfig.getName().equalsIgnoreCase("samlsso")) {
                federatedAuthenticatorConfigs = (FederatedAuthenticatorConfig[])
                        ArrayUtils.removeElement(federatedAuthenticatorConfigs,
                                authenticatorConfig);
            }
        }
        residentIdentityProvider.setFederatedAuthenticatorConfigs(federatedAuthenticatorConfigs);
        if (isSuperTenant) {
            superTenantIDPMgtClient.updateResidentIdP(residentIdentityProvider);
        } else {
            tenantIDPMgtClient.updateResidentIdP(residentIdentityProvider);
        }
    }

    private void updateResidentIDPProperty(IdentityProvider residentIdp, String propertyKey, String value, boolean
            isSuperTenant)
            throws Exception {

        IdentityProviderProperty[] idpProperties = residentIdp.getIdpProperties();
        for (IdentityProviderProperty providerProperty : idpProperties) {
            if (propertyKey.equalsIgnoreCase(providerProperty.getName())) {
                providerProperty.setValue(value);
            }
        }
        updateResidentIDP(residentIdp, isSuperTenant);
    }

    private enum HttpBinding {
        HTTP_REDIRECT("HTTP-Redirect"),
        HTTP_POST("HTTP-POST");

        String binding;

        HttpBinding(String binding) {
            this.binding = binding;
        }
    }

    private enum ClaimType {
        LOCAL, CUSTOM, NONE
    }

    private enum User {
        SUPER_TENANT_USER("samluser1", "samluser1", "carbon.super", "samluser1", "samluser1@abc.com", "samlnickuser1", true),
        TENANT_USER("samluser2@wso2.com", "samluser2", "wso2.com", "samluser2", "samluser2@abc.com", "samlnickuser2", true),
        SUPER_TENANT_USER_WITHOUT_MANDATORY_CLAIMS("samluser3", "samluser3", "carbon.super", "samluser3", "providedClaimValue", "providedClaimValue", false),
        TENANT_USER_WITHOUT_MANDATORY_CLAIMS("samluser4@wso2.com", "samluser4", "wso2.com", "samluser4", "providedClaimValue", "providedClaimValue", false);

        private String username;
        private String password;
        private String tenantDomain;
        private String tenantAwareUsername;
        private String email;
        private String nickname;
        private boolean setUserClaims;

        User(String username, String password, String tenantDomain, String tenantAwareUsername, String email,
             String nickname, boolean setUserClaims) {
            this.username = username;
            this.password = password;
            this.tenantDomain = tenantDomain;
            this.tenantAwareUsername = tenantAwareUsername;
            this.email = email;
            this.nickname = nickname;
            this.setUserClaims = setUserClaims;
        }

        public String getUsername() {
            return username;
        }

        public String getPassword() {
            return password;
        }

        public String getTenantDomain() {
            return tenantDomain;
        }

        public String getTenantAwareUsername() {
            return tenantAwareUsername;
        }

        public String getEmail() {
            return email;
        }

        public String getNickname() {
            return nickname;
        }

        public boolean getSetUserClaims() {
            return setUserClaims;
        }
    }

    private enum App {
        SUPER_TENANT_APP_WITH_SIGNING("travelocity.com", true),
        TENANT_APP_WITHOUT_SIGNING("travelocity.com-saml-tenantwithoutsigning", false);

        private String artifact;
        private boolean signingEnabled;

        App(String artifact, boolean signingEnabled) {
            this.artifact = artifact;
            this.signingEnabled = signingEnabled;
        }

        public String getArtifact() {
            return artifact;
        }

        public boolean isSigningEnabled() {
            return signingEnabled;
        }
    }

    private static class SAMLConfig {
        private TestUserMode userMode;
        private User user;
        private HttpBinding httpBinding;
        private ClaimType claimType;
        private App app;

        private SAMLConfig(TestUserMode userMode, User user, HttpBinding httpBinding, ClaimType claimType, App app) {
            this.userMode = userMode;
            this.user = user;
            this.httpBinding = httpBinding;
            this.claimType = claimType;
            this.app = app;
        }

        public TestUserMode getUserMode() {
            return userMode;
        }

        public App getApp() {
            return app;
        }

        public User getUser() {
            return user;
        }

        public ClaimType getClaimType() {
            return claimType;
        }

        public HttpBinding getHttpBinding() {
            return httpBinding;
        }

        @Override
        public String toString() {
            return "SAMLConfig[" +
                    ", userMode=" + userMode.name() +
                    ", user=" + user.getUsername() +
                    ", httpBinding=" + httpBinding +
                    ", claimType=" + claimType +
                    ", app=" + app.getArtifact() +
                    ']';
        }
    }

}


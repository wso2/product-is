/*
 * Copyright (c) 2016, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
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
package org.wso2.identity.integration.test.analytics.authentication;


import org.apache.axis2.context.ConfigurationContext;
import org.apache.axis2.context.ConfigurationContextFactory;
import org.apache.catalina.startup.Tomcat;
import org.apache.commons.codec.binary.Base64;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.http.HttpResponse;
import org.apache.http.NameValuePair;
import org.apache.http.client.HttpClient;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.message.BasicNameValuePair;
import org.apache.http.util.EntityUtils;
import org.testng.Assert;
import org.testng.annotations.AfterClass;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.DataProvider;
import org.testng.annotations.Factory;
import org.testng.annotations.Test;
import org.wso2.carbon.automation.engine.context.TestUserMode;
import org.wso2.carbon.databridge.commons.Event;
import org.wso2.carbon.h2.osgi.utils.CarbonUtils;
import org.wso2.carbon.identity.application.common.model.xsd.InboundAuthenticationConfig;
import org.wso2.carbon.identity.application.common.model.xsd.InboundAuthenticationRequestConfig;
import org.wso2.carbon.identity.application.common.model.xsd.Property;
import org.wso2.carbon.identity.application.common.model.xsd.ServiceProvider;
import org.wso2.carbon.identity.sso.saml.stub.types.SAMLSSOServiceProviderDTO;
import org.wso2.carbon.integration.common.utils.exceptions.AutomationUtilException;
import org.wso2.carbon.integration.common.utils.mgt.ServerConfigurationManager;
import org.wso2.identity.integration.common.clients.application.mgt.ApplicationManagementServiceClient;
import org.wso2.identity.integration.common.clients.sso.saml.SAMLSSOConfigServiceClient;
import org.wso2.identity.integration.common.clients.usermgt.remote.RemoteUserStoreManagerServiceClient;
import org.wso2.identity.integration.common.utils.ISIntegrationTest;
import org.wso2.identity.integration.test.analytics.commons.ThriftServer;
import org.wso2.identity.integration.test.util.Utils;
import org.wso2.identity.integration.test.utils.CommonConstants;

import javax.xml.xpath.XPathExpressionException;
import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;

public class AnalyticsLoginTestCase extends ISIntegrationTest {

    private static final Log log = LogFactory.getLog(AnalyticsLoginTestCase.class);

    // SAML Application attributes
    private static final String USER_AGENT = "Apache-HttpClient/4.2.5 (java 1.5)";
    private static final String APPLICATION_NAME = "SAML-SSO-TestApplication";
    private static final String INBOUND_AUTH_TYPE = "samlsso";
    private static final String ATTRIBUTE_CS_INDEX_VALUE = "1239245949";
    private static final String ATTRIBUTE_CS_INDEX_NAME = "attrConsumServiceIndex";
    public static final String TENANT_DOMAIN_PARAM = "tenantDomain";

    private static final String SAML_SSO_URL = "https://localhost:9853/samlsso";
    private static final String ACS_URL = "http://localhost:8490/%s/home.jsp";
    private static final String COMMON_AUTH_URL = "https://localhost:9853/commonauth";
    private static final String SAML_SSO_LOGIN_URL = "http://localhost:8490/%s/samlsso?SAML2.HTTPBinding=%s";
    private static final String SAML_SSO_INDEX_URL = "http://localhost:8490/%s/";
    private static final String SAML_SSO_LOGOUT_URL = "http://localhost:8490/%s/logout?SAML2.HTTPBinding=%s";
    private static final String NAMEID_FORMAT = "urn:oasis:names:tc:SAML:1.1:nameid-format:emailAddress";
    private static final String LOGIN_URL = "/carbon/admin/login.jsp";
    private static final String profileName = "default";
    private static final String sessionStreamId = "org.wso2.is.analytics.stream.OverallSession:1.0.0";
    private static final String authenticationStreamId = "org.wso2.is.analytics.stream.OverallAuthentication:1.0.0";

    private ApplicationManagementServiceClient applicationManagementServiceClient;
    private SAMLSSOConfigServiceClient ssoConfigServiceClient;
    private RemoteUserStoreManagerServiceClient remoteUSMServiceClient;
    private SAMLConfig config;
    private Tomcat tomcatServer;
    private ThriftServer thriftServer;
    private ServerConfigurationManager serverConfigurationManager;
    HttpClient sharedHttpClient = new DefaultHttpClient();


    private String resultPage;

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
        SUPER_TENANT_USER("samlAnalyticsuser1", "samlAnalyticsuser1", "carbon.super", "samlAnalyticsuser1", "samlAnalyticsuser1@abc.com",
                "samlnickuser1"),
        TENANT_USER("samlAnalyticsuser2@wso2.com", "samlAnalyticsuser2", "wso2.com", "samlAnalyticsuser2", "samlAnalyticsuser2@abc.com",
                "samlnickuser2");

        private String username;
        private String password;
        private String tenantDomain;
        private String tenantAwareUsername;
        private String email;
        private String nickname;

        User(String username, String password, String tenantDomain, String tenantAwareUsername, String email,
             String nickname) {
            this.username = username;
            this.password = password;
            this.tenantDomain = tenantDomain;
            this.tenantAwareUsername = tenantAwareUsername;
            this.email = email;
            this.nickname = nickname;
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
    }

    ;

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

    @Factory(dataProvider = "samlConfigProvider")
    public AnalyticsLoginTestCase(SAMLConfig config) {
        if (log.isDebugEnabled()) {
            log.debug("SAML SSO Test initialized for " + config);
        }
        this.config = config;
    }

    @BeforeClass(alwaysRun = true)
    public void testInit() throws Exception {

        super.init();
        changeIdentityXml();
        super.init(config.getUserMode());
        thriftServer = new ThriftServer("Wso2EventTestCase", 8021, true);
        thriftServer.start(8021);
        log.info("Thrift Server is Started on port 8462");
        ConfigurationContext configContext = ConfigurationContextFactory.createConfigurationContextFromFileSystem(null, null);
        applicationManagementServiceClient = new ApplicationManagementServiceClient(sessionCookie, backendURL, configContext);
        ssoConfigServiceClient = new SAMLSSOConfigServiceClient(backendURL, sessionCookie);
        remoteUSMServiceClient = new RemoteUserStoreManagerServiceClient(backendURL, sessionCookie);
        createUser();
        createApplication();

        //Starting tomcat
        log.info("Starting Tomcat");
        tomcatServer = Utils.getTomcat(getClass());

        URL resourceUrl = getClass().getResource(File.separator + "samples" + File.separator + config.getApp()
                .getArtifact() + ".war");
        Utils.startTomcat(tomcatServer, "/" + config.getApp().getArtifact(), resourceUrl.getPath());

    }

    @AfterClass(alwaysRun = true)
    public void testClear() throws Exception {

        deleteUser();
        deleteApplication();
        ssoConfigServiceClient = null;
        applicationManagementServiceClient = null;
        remoteUSMServiceClient = null;
        thriftServer.stop();
        replaceIdentityXml();
        //Stopping tomcat
        tomcatServer.stop();
        tomcatServer.destroy();
        Thread.sleep(1000);
    }

    @Test(description = "Add service provider", groups = "wso2.is", priority = 1)
    public void testAddSP() throws Exception {

        Boolean isAddSuccess = ssoConfigServiceClient.addServiceProvider(createSsoServiceProviderDTO());
        Assert.assertTrue(isAddSuccess, "Adding a service provider has failed for " + config);

        SAMLSSOServiceProviderDTO[] samlssoServiceProviderDTOs = ssoConfigServiceClient
                .getServiceProviders().getServiceProviders();
        Assert.assertEquals(samlssoServiceProviderDTOs[0].getIssuer(), config.getApp().getArtifact(),
                "Adding a service provider has failed for " + config);
    }

    @Test(alwaysRun = true, description = "Testing SAML SSO login", groups = "wso2.is", dependsOnMethods = {"testAddSP"})
    public void testSAMLSSOIsPassiveLogin() {
        try {
            HttpClient httpClient = sharedHttpClient;
            HttpResponse response;
            response = Utils.sendGetRequest(String.format(SAML_SSO_INDEX_URL, config.getApp().getArtifact(), config
                    .getHttpBinding().binding), USER_AGENT, httpClient);
            String samlResponse = Utils.extractDataFromResponse(response, "name='SAMLResponse'", 5);
            samlResponse = new String(Base64.decodeBase64(samlResponse));
            Assert.assertTrue(samlResponse.contains("Destination=\"" + String.format(ACS_URL, config.getApp()
                    .getArtifact()) + "\""));
        } catch (Exception e) {
            Assert.fail("SAML SSO Login test failed for " + config, e);
        }
    }

    @Test(alwaysRun = true, description = "Testing SAML SSO login", groups = "wso2.is",
            dependsOnMethods = {"testSAMLSSOIsPassiveLogin"})
    public void testSAMLSSOLogin() {
        try {
            HttpResponse response;
            HttpClient httpClient = sharedHttpClient;
            response = Utils.sendGetRequest(String.format(SAML_SSO_LOGIN_URL, config.getApp().getArtifact(), config
                    .getHttpBinding().binding), USER_AGENT, httpClient);

            if (config.getHttpBinding() == HttpBinding.HTTP_POST) {
                String samlRequest = Utils.extractDataFromResponse(response, CommonConstants.SAML_REQUEST_PARAM, 5);
                response = sendSAMLMessage(SAML_SSO_URL, CommonConstants.SAML_REQUEST_PARAM, samlRequest);
                EntityUtils.consume(response.getEntity());
                response = Utils.sendRedirectRequest(response, USER_AGENT, ACS_URL, config.getApp().getArtifact(),
                        httpClient);
            }

            String sessionKey = Utils.extractDataFromResponse(response, CommonConstants.SESSION_DATA_KEY, 1);
            response = Utils.sendPOSTMessage(sessionKey, COMMON_AUTH_URL, USER_AGENT, ACS_URL, config.getApp()
                    .getArtifact(), config.getUser().getUsername(), config.getUser().getPassword(), httpClient);
            EntityUtils.consume(response.getEntity());

            response = Utils.sendRedirectRequest(response, USER_AGENT, ACS_URL, config.getApp().getArtifact(), httpClient);
            String samlResponse = Utils.extractDataFromResponse(response, CommonConstants.SAML_RESPONSE_PARAM, 5);
            response = sendSAMLMessage(String.format(ACS_URL, config.getApp().getArtifact()), CommonConstants
                    .SAML_RESPONSE_PARAM, samlResponse);
            Thread.sleep(2000);

            Assert.assertEquals(thriftServer.getPreservedEventList().size(), 3);

            Event sessionEvent = null;
            Event authStepEvent = null;
            Event overallAuthEvent = null;

            for (Event event : thriftServer.getPreservedEventList()) {
                String streamId = event.getStreamId();
                if (sessionStreamId.equalsIgnoreCase(streamId)) {
                    sessionEvent = event;
                }
                if (authenticationStreamId.equalsIgnoreCase(streamId)) {
                    Object[] eventStreamData = event.getPayloadData();
                    if ("overall".equalsIgnoreCase((String) eventStreamData[2])) {
                        overallAuthEvent = event;
                    } else if ("step".equalsIgnoreCase((String) eventStreamData[2])) {
                        authStepEvent = event;
                    }
                }
            }

            assertSessionEvent(sessionEvent);

            Object[] eventStreamData = overallAuthEvent.getPayloadData();

            eventStreamData = authStepEvent.getPayloadData();
            // authenticationSuccess
            Assert.assertEquals(eventStreamData[3], false);
            // userName
            Assert.assertEquals(eventStreamData[4], "samlAnalyticsuser1");
            // userStoreDomain
            Assert.assertEquals(eventStreamData[6], "PRIMARY");
            // tenantDomain
            Assert.assertEquals(eventStreamData[7], "carbon.super");
            // inboundAuthType
            Assert.assertEquals(eventStreamData[10], "samlsso");
            // serviceprovider
            Assert.assertEquals(eventStreamData[11], "SAML-SSO-TestApplication");
            // remembermeEnabled
            Assert.assertEquals(eventStreamData[12], false);
            // forceAuthEnabled
            Assert.assertEquals(eventStreamData[13], false);
            // rolesCommaSeperated
            Assert.assertEquals(eventStreamData[15], "NOT_AVAILABLE");
            // authenticationStep
            Assert.assertEquals(eventStreamData[16], "1");
            // isFirstLogin
            Assert.assertEquals(eventStreamData[20], false);
            extractDataFromResponse(response);
        } catch (Exception e) {
            Assert.fail("SAML SSO Login Analytics test failed for " + config, e);
        } finally {
            thriftServer.resetPreservedEventList();
        }
    }


    @Test(alwaysRun = true, description = "Testing SAML SSO login", groups = "wso2.is",
            dependsOnMethods = {"testSAMLSSOLogin"})
    public void testSAMLSSOLoginWithExistingSession() {
        try {
            HttpResponse response;
            HttpClient httpClient = sharedHttpClient;
            response = Utils.sendGetRequest(String.format(SAML_SSO_LOGIN_URL, config.getApp().getArtifact(), config
                    .getHttpBinding().binding), USER_AGENT, httpClient);

            if (config.getHttpBinding() == HttpBinding.HTTP_POST) {
                String samlRequest = Utils.extractDataFromResponse(response, CommonConstants.SAML_REQUEST_PARAM, 5);
                response = sendSAMLMessage(SAML_SSO_URL, CommonConstants.SAML_REQUEST_PARAM, samlRequest);
                EntityUtils.consume(response.getEntity());
                response = Utils.sendRedirectRequest(response, USER_AGENT, ACS_URL, config.getApp().getArtifact(),
                        httpClient);
            }

            Event sessionEvent = null;
            for (Event event : thriftServer.getPreservedEventList()) {
                String streamId = event.getStreamId();
                if (sessionStreamId.equalsIgnoreCase(streamId)) {
                    sessionEvent = event;
                }
            }

            assertSessionUpdateEvent(sessionEvent);
            extractDataFromResponse(response);
        } catch (Exception e) {
            Assert.fail("SAML SSO Login Analytics test failed for " + config, e);
        } finally {
            thriftServer.resetPreservedEventList();
        }
    }

    @Test(alwaysRun = true, description = "Testing SAML SSO login fail", groups = "wso2.is",
            dependsOnMethods = {"testSAMLSSOLogout"})
    public void testSAMLSSOLoginFail() {
        try {
            HttpResponse response;
            HttpClient httpClient = new DefaultHttpClient();
            response = Utils.sendGetRequest(String.format(SAML_SSO_LOGIN_URL, config.getApp().getArtifact(), config
                    .getHttpBinding().binding), USER_AGENT, httpClient);

            if (config.getHttpBinding() == HttpBinding.HTTP_POST) {
                String samlRequest = Utils.extractDataFromResponse(response, CommonConstants.SAML_REQUEST_PARAM, 5);
                response = sendSAMLMessage(SAML_SSO_URL, CommonConstants.SAML_REQUEST_PARAM, samlRequest);
                EntityUtils.consume(response.getEntity());

                response = Utils.sendRedirectRequest(response, USER_AGENT, ACS_URL, config.getApp().getArtifact(),
                        httpClient);
            }

            String sessionKey = Utils.extractDataFromResponse(response, CommonConstants.SESSION_DATA_KEY, 1);
            response = Utils.sendPOSTMessage(sessionKey, COMMON_AUTH_URL, USER_AGENT, ACS_URL, config.getApp()
                    .getArtifact(), "dummy", config.getUser().getPassword(), httpClient);
            EntityUtils.consume(response.getEntity());

            response = Utils.sendRedirectRequest(response, USER_AGENT, ACS_URL, config.getApp().getArtifact(), httpClient);
            String samlResponse = Utils.extractDataFromResponse(response, CommonConstants.SAML_RESPONSE_PARAM, 5);
            response = sendSAMLMessage(String.format(ACS_URL, config.getApp().getArtifact()), CommonConstants
                    .SAML_RESPONSE_PARAM, samlResponse);
            Thread.sleep(2000);

            Event event = (Event) thriftServer.getPreservedEventList().get(0);
            Object[] eventStreamData = event.getPayloadData();

            // authenticationSuccess
            Assert.assertEquals(eventStreamData[3], false);
            // userName
            Assert.assertEquals(eventStreamData[4], "dummy");
            // userStoreDomain
            Assert.assertEquals(eventStreamData[6], "PRIMARY");
            // tenantDomain
            Assert.assertEquals(eventStreamData[7], "carbon.super");
            // inboundAuthType
            Assert.assertEquals(eventStreamData[10], "samlsso");
            // serviceprovider
            Assert.assertEquals(eventStreamData[11], "SAML-SSO-TestApplication");
            // remembermeEnabled
            Assert.assertEquals(eventStreamData[12], false);
            // forceAuthEnabled
            Assert.assertEquals(eventStreamData[13], false);
            // rolesCommaSeperated
            Assert.assertEquals(eventStreamData[15], "NOT_AVAILABLE");
            // authenticationStep
            Assert.assertEquals(eventStreamData[16], "1");
            extractDataFromResponse(response);
        } catch (Exception e) {
            Assert.fail("SAML SSO Login Analytics test failed for " + config, e);
        } finally {
            thriftServer.resetPreservedEventList();
        }
    }

    @Test(alwaysRun = true, description = "Testing SAML SSO logout", groups = "wso2.is",
            dependsOnMethods = {"testSAMLSSOLoginWithExistingSession"})
    public void testSAMLSSOLogout() throws Exception {
        try {
            HttpResponse response;
            HttpClient httpClient = sharedHttpClient;
            response = Utils.sendGetRequest(String.format(SAML_SSO_LOGOUT_URL, config.getApp().getArtifact(), config
                    .getHttpBinding().binding), USER_AGENT, httpClient);

            if (config.getHttpBinding() == HttpBinding.HTTP_POST) {
                String samlRequest = Utils.extractDataFromResponse(response, CommonConstants.SAML_REQUEST_PARAM, 5);
                response = sendSAMLMessage(SAML_SSO_URL, CommonConstants.SAML_REQUEST_PARAM, samlRequest);
            }

            String samlResponse = Utils.extractDataFromResponse(response, CommonConstants.SAML_RESPONSE_PARAM, 5);
            response = sendSAMLMessage(String.format(ACS_URL, config.getApp().getArtifact()), CommonConstants
                    .SAML_RESPONSE_PARAM, samlResponse);
            assertSessionTerminationEvent(thriftServer.getPreservedEventList().get(0));
            extractDataFromResponse(response);
        } catch (Exception e) {
            Assert.fail("SAML SSO Logout test failed for " + config, e);
        } finally {
            thriftServer.resetPreservedEventList();
        }
    }


    @DataProvider(name = "samlConfigProvider")
    public static SAMLConfig[][] samlConfigProvider() {
        return new SAMLConfig[][]{
                {new SAMLConfig(TestUserMode.SUPER_TENANT_ADMIN, User.SUPER_TENANT_USER, HttpBinding.HTTP_REDIRECT,
                        ClaimType.NONE, App.SUPER_TENANT_APP_WITH_SIGNING)},
        };
    }

    private HttpResponse sendSAMLMessage(String url, String samlMsgKey, String samlMsgValue) throws IOException {
        HttpClient httpClient = sharedHttpClient;
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


    private void createApplication() throws Exception {
        ServiceProvider serviceProvider = new ServiceProvider();
        serviceProvider.setApplicationName(APPLICATION_NAME);
        serviceProvider.setDescription("This is a test Service Provider");
        applicationManagementServiceClient.createApplication(serviceProvider);

        serviceProvider = applicationManagementServiceClient.getApplication(APPLICATION_NAME);

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
                    null, null, profileName, true);
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


    public void changeIdentityXml() {
        log.info("Changing identity.xml file to enable analytics");

        String carbonHome = CarbonUtils.getCarbonHome();

        String analyticsEnabledIdentityXml = getISResourceLocation() + File.separator + "analytics" + File.separator
                + "config" + File.separator + "identit_analytics_enabled.xml";
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
                + "config" + File.separator + "IsAnalytics-Publisher-wso2event-AuthenticationData.xml";
        File defaultAuthenticationDataPublisher = new File(carbonHome + File.separator
                + "repository" + File.separator + "deployment" + File.separator + "server" + File.separator +
                "eventpublishers" + File.separator + "IsAnalytics-Publisher-wso2event-AuthenticationData.xml");

        String sessionDataPublisherWithOffset = getISResourceLocation() + File.separator + "analytics" + File.separator
                + "config" + File.separator + "IsAnalytics-Publisher-wso2event-SessionData.xml";
        File defaultSessionDataPublisher = new File(carbonHome + File.separator
                + "repository" + File.separator + "deployment" + File.separator + "server" + File.separator +
                "eventpublishers" + File.separator + "IsAnalytics-Publisher-wso2event-SessionData.xml");
        try {

            File configuredAuthnPublisherFile = new File(authnDataPublisherWithOffset);
            File configuredSessionPublisherFile = new File(sessionDataPublisherWithOffset);
            serverConfigurationManager = new ServerConfigurationManager(isServer);
            serverConfigurationManager.applyConfigurationWithoutRestart(configuredAuthnPublisherFile,
                    defaultAuthenticationDataPublisher, true);
            serverConfigurationManager.applyConfigurationWithoutRestart(configuredSessionPublisherFile,
                    defaultSessionDataPublisher, true);

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

        String defaultIdentityXml = getISResourceLocation() + File.separator + "analytics" + File.separator
                + "config" + File.separator + "identit_original.xml";
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


    public void assertSessionEvent(Event sessionEvent) {
        Object[] sessionObjects = sessionEvent.getPayloadData();
        Assert.assertEquals(sessionObjects[1], sessionObjects[2]);
        Assert.assertEquals(sessionObjects[4], 1);
        Assert.assertEquals(sessionObjects[5], "samlAnalyticsuser1");
        Assert.assertEquals(sessionObjects[6], "PRIMARY");
        Assert.assertTrue((Long) sessionObjects[2] < (Long) sessionObjects[14]);
    }

    public void assertSessionUpdateEvent(Event sessionEvent) {
        Object[] sessionObjects = sessionEvent.getPayloadData();
        // Assert.assertTrue((Long)sessionObjects[1] < (Long)sessionObjects[2]);
        Assert.assertEquals(sessionObjects[4], 2);
        Assert.assertEquals(sessionObjects[5], "samlAnalyticsuser1");
        Assert.assertEquals(sessionObjects[6], "PRIMARY");
        //  Assert.assertTrue((Long)sessionObjects[2] < (Long)sessionObjects[10]);
    }

    public void assertSessionTerminationEvent(Event sessionEvent) {
        Object[] sessionObjects = sessionEvent.getPayloadData();
        // Assert.assertTrue((Long) sessionObjects[1] < (Long) sessionObjects[2]);
        Assert.assertEquals(sessionObjects[4], 0);
        Assert.assertEquals(sessionObjects[5], "samlAnalyticsuser1");
        Assert.assertEquals(sessionObjects[6], "PRIMARY");
        // Assert.assertTrue((Long)sessionObjects[2] < (Long)sessionObjects[10]);
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

}

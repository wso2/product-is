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
package org.wso2.identity.integration.test.saml;

import org.apache.axis2.context.ConfigurationContext;
import org.apache.axis2.context.ConfigurationContextFactory;
import org.apache.catalina.startup.Tomcat;
import org.apache.commons.lang3.StringUtils;
import org.apache.http.HttpResponse;
import org.apache.http.client.HttpClient;
import org.apache.http.impl.client.DefaultHttpClient;
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
import org.wso2.carbon.identity.application.common.model.xsd.AuthenticationStep;
import org.wso2.carbon.identity.application.common.model.xsd.InboundAuthenticationConfig;
import org.wso2.carbon.identity.application.common.model.xsd.InboundAuthenticationRequestConfig;
import org.wso2.carbon.identity.application.common.model.xsd.LocalAndOutboundAuthenticationConfig;
import org.wso2.carbon.identity.application.common.model.idp.xsd.Property;
import org.wso2.carbon.identity.application.common.model.xsd.LocalAuthenticatorConfig;
import org.wso2.carbon.identity.application.common.model.xsd.ServiceProvider;
import org.wso2.carbon.identity.sso.saml.stub.types.SAMLSSOServiceProviderDTO;
import org.wso2.carbon.integration.common.utils.mgt.ServerConfigurationManager;
import org.wso2.carbon.um.ws.api.stub.ClaimValue;
import org.wso2.carbon.utils.CarbonUtils;
import org.wso2.identity.integration.common.clients.Idp.IdentityProviderMgtServiceClient;
import org.wso2.identity.integration.common.clients.application.mgt.ApplicationManagementServiceClient;
import org.wso2.identity.integration.common.clients.sso.saml.SAMLSSOConfigServiceClient;
import org.wso2.identity.integration.common.clients.usermgt.remote.RemoteUserStoreManagerServiceClient;
import org.wso2.identity.integration.common.utils.ISIntegrationTest;
import org.wso2.identity.integration.test.util.Utils;
import org.wso2.identity.integration.test.utils.CommonConstants;

import java.io.File;
import java.net.URL;
import java.util.HashMap;
import java.util.Map;

/**
 * This class tests the different types of authenticators
 * with SAML Service provider and different kind of users.
 */
public class SAMLLocalAndOutboundAuthenticatorsTestCase extends ISIntegrationTest {

    private static final String profileName = "default";
    private static final String APPLICATION_NAME = "SAML-SSO-TestApplication";
    private static final String INBOUND_AUTH_TYPE = "samlsso";
    private static final String ACS_URL = "http://localhost:8490/%s/home.jsp";
    private static final String NAMEID_FORMAT = "urn:oasis:names:tc:SAML:1.1:nameid-format:emailAddress";
    private static final String LOGIN_URL = "/carbon/admin/login.jsp";
    private static final String IDENTITY_PROVIDER_NAME = "GoogleIDP";
    private static final String IDENTITY_PROVIDER_ALIAS = "https://localhost:" + CommonConstants
            .IS_DEFAULT_HTTPS_PORT + "/oauth2/token/";
    private static final String CLIENT_ID = "ClientId";
    private static final String CLIENT_SECRET = "ClientSecret";
    private static final String CALLBACK_URL = "callbackUrl";
    private static final String SAML_SSO_LOGIN_URL = "http://localhost:8490/%s/samlsso?SAML2.HTTPBinding=%s";
    private static final String USER_AGENT = "Apache-HttpClient/4.2.5 (java 1.5)";
    public static final String TENANT_DOMAIN_PARAM = "tenantDomain";
    private static final String SAML_SSO_URL = "https://localhost:" + CommonConstants.IS_DEFAULT_HTTPS_PORT +
            "/samlsso";
    private static final String COMMON_AUTH_URL = "https://localhost:" + CommonConstants.IS_DEFAULT_HTTPS_PORT +
            "/commonauth";
    private static final String ACCOUNT_LOCK_CLAIM_URI = "http://wso2.org/claims/identity/accountLocked";
    private static  final String GOOGLE_AUTHENTICATOR = "GoogleOIDCAuthenticator";

    private ServerConfigurationManager serverConfigurationManager;
    private SPConfig config;
    private Tomcat tomcatServer;
    private ApplicationManagementServiceClient applicationManagementServiceClient;
    private SAMLSSOConfigServiceClient ssoConfigServiceClient;
    private RemoteUserStoreManagerServiceClient remoteUSMServiceClient;
    private IdentityProviderMgtServiceClient identityProviderMgtServiceClient;
    private HttpClient httpClient;

    private enum User {
        SUPER_TENANT_USER("samluser1", "samluser1", "carbon.super", "samluser1@wso2.com", "samlnickuser1",
                "samluser1", ""),
        SUPER_TENANT_LOCKED_USER("isura", "Lakmal@123", "carbon.super", "isura@wso2.com", "samlnickuser2", "isura",
                CommonConstants.USER_IS_LOCKED),
        SUPER_TENANT_WRONG_CREDENTIAL_USER("samluser1", "wrongpass", "carbon.super", "samluser1@wso2.com",
                "samlnickuser1", "samluser1", CommonConstants.INVALID_CREDENTIAL);

        private String username;
        private String password;
        private String tenantDomain;
        private String email;
        private String nickname;
        private String tenantAwareUsername;
        private String expectedErrorcode;

        User(String username, String password, String tenantDomain, String email, String nickname, String
                tenantAwareUsername, String expectedErrorcode) {
            this.username = username;
            this.password = password;
            this.tenantDomain = tenantDomain;
            this.email = email;
            this.nickname = nickname;
            this.tenantAwareUsername = tenantAwareUsername;
            this.expectedErrorcode = expectedErrorcode;
        }

        public String getNickname() {
            return nickname;
        }

        public String getEmail() {
            return email;
        }

        public String getTenantDomain() {
            return tenantDomain;
        }

        public String getPassword() {
            return password;
        }

        public String getUsername() {
            return username;
        }

        public String getTenantAwareUsername() {
            return tenantAwareUsername;
        }

        public String getExpectedErrorcode() {
            return expectedErrorcode;
        }

        public void setExpectedErrorcode(String expectedErrorcode) {
            this.expectedErrorcode = expectedErrorcode;
        }
    }

    private enum Authenticator {
        DEFAULT_AUTHENTICATOR, LOCAL_AUTHENTICATOR, FEDERATED_AUTHENTICATOR, ADVANCED_AUTHENTICATOR;
        private LocalAndOutboundAuthenticationConfig localAndOutboundAuthenticationConfig;

        public LocalAndOutboundAuthenticationConfig getLocalAndOutboundAuthenticationConfig() {
            return localAndOutboundAuthenticationConfig;
        }

        public void setLocalAndOutboundAuthenticationConfig(LocalAndOutboundAuthenticationConfig
                                                                    localAndOutboundAuthenticationConfig) {
            this.localAndOutboundAuthenticationConfig = localAndOutboundAuthenticationConfig;
        }
    }

    private enum SPApplication {
        SUPER_TENANT_APP("travelocity.com");
        private String artifact;

        SPApplication(String artifact) {
            this.artifact = artifact;
        }

        public String getArtifact() {
            return artifact;
        }
    }

    private enum HttpBinding {
        HTTP_REDIRECT("HTTP-Redirect"),
        HTTP_POST("HTTP-POST");

        String binding;

        HttpBinding(String binding) {
            this.binding = binding;
        }
    }

    private static class SPConfig {
        private Authenticator authenticator;
        private User user;
        private SPApplication application;
        private TestUserMode userMode;
        private HttpBinding httpBinding;

        private SPConfig(Authenticator authenticator, User user, SPApplication application, TestUserMode userMode,
                         HttpBinding httpBinding) {
            this.authenticator = authenticator;
            this.user = user;
            this.application = application;
            this.userMode = userMode;
            this.httpBinding = httpBinding;
        }

        public SPApplication getApplication() {
            return application;
        }

        public User getUser() {
            return user;
        }

        public Authenticator getAuthenticator() {
            return authenticator;
        }

        public TestUserMode getUserMode() {
            return userMode;
        }

        public HttpBinding getHttpBinding() {
            return httpBinding;
        }
    }

    @Factory(dataProvider = "spConfigProvider")
    public SAMLLocalAndOutboundAuthenticatorsTestCase(SPConfig config) {
        if (log.isDebugEnabled()) {
            log.info("SAML LocalAndOutboundAuthenticators Test initialized for " + config);
        }
        this.config = config;
    }

    @BeforeClass(alwaysRun = true)
    public void testInit() throws Exception {
        super.init(config.getUserMode());
        changeISConfiguration();
        super.init(config.getUserMode());

        ConfigurationContext configContext = ConfigurationContextFactory.createConfigurationContextFromFileSystem
                (null, null);
        applicationManagementServiceClient = new ApplicationManagementServiceClient(sessionCookie, backendURL,
                configContext);
        ssoConfigServiceClient = new SAMLSSOConfigServiceClient(backendURL, sessionCookie);
        remoteUSMServiceClient = new RemoteUserStoreManagerServiceClient(backendURL, sessionCookie);
        identityProviderMgtServiceClient = new IdentityProviderMgtServiceClient(sessionCookie, backendURL);
        httpClient = new DefaultHttpClient();

        createIDP();
        createUser();
        createLocalAndOutBoundAuthenticator();
        createApplication();
        ssoConfigServiceClient.addServiceProvider(getSsoServiceProviderDTO());

        //Starting tomcat
        log.info("Starting Tomcat");
        tomcatServer = Utils.getTomcat(getClass());

        URL resourceUrl = getClass().getResource(ISIntegrationTest.URL_SEPARATOR + "samples" + ISIntegrationTest.URL_SEPARATOR + config.getApplication
                ().getArtifact() + "" + ".war");
        Utils.startTomcat(tomcatServer, "/" + config.getApplication().getArtifact(), resourceUrl.getPath());

    }

    @AfterClass(alwaysRun = true)
    public void testClear() throws Exception {
        deleteUser();
        deleteApplication();
        deleteIDP();
        ssoConfigServiceClient = null;
        applicationManagementServiceClient = null;
        remoteUSMServiceClient = null;
        httpClient = null;
        //Stopping tomcat
        tomcatServer.stop();
        tomcatServer.destroy();
        resetISConfiguration();
        Thread.sleep(10000);
    }

    @Test(description = "Test whether error code included in redirect URL with DefaultAuthenticator", groups = "wso2" +
            ".is", priority = 1)
    public void testErrorCodeInRedirectUrl() throws Exception {
        try {
            HttpResponse response;
            String redirectUrl;
            response = Utils.sendGetRequest(String.format(SAML_SSO_LOGIN_URL, config.getApplication().getArtifact(),
                    config.getHttpBinding().binding), USER_AGENT, httpClient);

            if (config.getHttpBinding() == HttpBinding.HTTP_POST) {
                String samlRequest = Utils.extractDataFromResponse(response, CommonConstants.SAML_REQUEST_PARAM, 5);
                Map<String, String> paramters = new HashMap<String, String>();
                paramters.put(CommonConstants.SAML_REQUEST_PARAM, samlRequest);
                response = Utils.sendSAMLMessage(SAML_SSO_URL, paramters, USER_AGENT, config.getUserMode(),
                        TENANT_DOMAIN_PARAM, config.getUser().getTenantDomain(), httpClient);
                EntityUtils.consume(response.getEntity());
                response = Utils.sendRedirectRequest(response, USER_AGENT, ACS_URL, config.getApplication()
                        .getArtifact(), httpClient);
            }
            String sessionKey = Utils.extractDataFromResponse(response, CommonConstants.SESSION_DATA_KEY, 1);
            response = sendPostMessage(sessionKey);
            EntityUtils.consume(response.getEntity());
            redirectUrl = Utils.getRedirectUrl(response);
            Assert.assertTrue(StringUtils.contains(redirectUrl,config.getUser().getExpectedErrorcode()));
        } catch (Exception e) {
            Assert.fail("SAML SSO Logout test failed for " + config, e);
        }
    }

    @DataProvider(name = "spConfigProvider")
    public static SPConfig[][] spConfigProvider() {
        return new SPConfig[][]{
                {new SPConfig(Authenticator.DEFAULT_AUTHENTICATOR, User.SUPER_TENANT_LOCKED_USER, SPApplication
                        .SUPER_TENANT_APP, TestUserMode.SUPER_TENANT_USER, HttpBinding.HTTP_POST)},
                {new SPConfig(Authenticator.ADVANCED_AUTHENTICATOR, User.SUPER_TENANT_LOCKED_USER, SPApplication
                        .SUPER_TENANT_APP, TestUserMode.SUPER_TENANT_USER, HttpBinding.HTTP_POST)},
        };
    }

    private void createApplication() throws Exception {
        ServiceProvider serviceProvider = new ServiceProvider();
        serviceProvider.setApplicationName(APPLICATION_NAME);
        serviceProvider.setDescription("This is a test Service Provider");
        applicationManagementServiceClient.createApplication(serviceProvider);

        serviceProvider = applicationManagementServiceClient.getApplication(APPLICATION_NAME);

        InboundAuthenticationRequestConfig requestConfig = new InboundAuthenticationRequestConfig();
        requestConfig.setInboundAuthType(INBOUND_AUTH_TYPE);
        requestConfig.setInboundAuthKey(config.getApplication().getArtifact());


        InboundAuthenticationConfig inboundAuthenticationConfig = new InboundAuthenticationConfig();
        inboundAuthenticationConfig.setInboundAuthenticationRequestConfigs(
                new InboundAuthenticationRequestConfig[]{requestConfig});

        serviceProvider.setInboundAuthenticationConfig(inboundAuthenticationConfig);
        serviceProvider.setLocalAndOutBoundAuthenticationConfig(config.getAuthenticator()
                .getLocalAndOutboundAuthenticationConfig());
        applicationManagementServiceClient.updateApplicationData(serviceProvider);
    }

    private void deleteApplication() throws Exception {
        applicationManagementServiceClient.deleteApplication(APPLICATION_NAME);
    }

    private void createUser() {
        log.info("Creating User " + config.getUser().getUsername());

        try {
            // creating the user
            if(!remoteUSMServiceClient.isExistingUser(config.getUser().getTenantAwareUsername())) {
                switch (config.getUser()) {
                    case SUPER_TENANT_USER:
                        remoteUSMServiceClient.addUser(config.getUser().getTenantAwareUsername(), config.getUser().getPassword(), null, null, profileName, true);
                        break;
                    case SUPER_TENANT_LOCKED_USER:
                        ClaimValue[] claimValues = new ClaimValue[1];
                        // Need to add this claim and have the value true in order to test the fix
                        ClaimValue accountLockClaim = new ClaimValue();
                        accountLockClaim.setClaimURI(ACCOUNT_LOCK_CLAIM_URI);
                        accountLockClaim.setValue(Boolean.TRUE.toString());
                        claimValues[0] = accountLockClaim;
                        remoteUSMServiceClient.addUser(config.getUser().getTenantAwareUsername(), config.getUser().getPassword(), null, claimValues, profileName, true);
                        break;
                    case SUPER_TENANT_WRONG_CREDENTIAL_USER:
                        //user is already created. Will be tried out with wrong password.
                        break;
                }
            }

        } catch (Exception e) {
            Assert.fail("Error while creating the user", e);
        }

    }

    private void deleteUser() {
        log.info("Deleting User " + config.getUser().getUsername());
        try {
            switch (config.getUser()) {
                case SUPER_TENANT_USER:
                    remoteUSMServiceClient.deleteUser(config.getUser().getTenantAwareUsername());
                    break;
                case SUPER_TENANT_LOCKED_USER:
                    break;
                case SUPER_TENANT_WRONG_CREDENTIAL_USER:
                    //user is already created. Will be tried out with wrong password.
                    break;
            }
        } catch (Exception e) {
            Assert.fail("Error while deleting the user", e);
        }
    }

    private SAMLSSOServiceProviderDTO getSsoServiceProviderDTO() {
        SAMLSSOServiceProviderDTO samlssoServiceProviderDTO = new SAMLSSOServiceProviderDTO();
        samlssoServiceProviderDTO.setIssuer(config.getApplication().getArtifact());
        samlssoServiceProviderDTO.setAssertionConsumerUrls(new String[]{String.format(ACS_URL, config.getApplication
                ().getArtifact())});
        samlssoServiceProviderDTO.setDefaultAssertionConsumerUrl(String.format(ACS_URL, config.getApplication()
                .getArtifact()));
        samlssoServiceProviderDTO.setNameIDFormat(NAMEID_FORMAT);
        samlssoServiceProviderDTO.setDoSignAssertions(true);
        samlssoServiceProviderDTO.setDoSignResponse(true);
        samlssoServiceProviderDTO.setDoSingleLogout(true);
        samlssoServiceProviderDTO.setLoginPageURL(LOGIN_URL);
        return samlssoServiceProviderDTO;
    }

    private void createIDP() throws Exception {
        IdentityProvider identityProvider = new IdentityProvider();
        identityProvider.setIdentityProviderName(IDENTITY_PROVIDER_NAME);
        identityProvider.setAlias(IDENTITY_PROVIDER_ALIAS);
        identityProvider.setEnable(true);

        FederatedAuthenticatorConfig federatedAuthenticatorConfig = new FederatedAuthenticatorConfig();
        federatedAuthenticatorConfig.setProperties(getGoogleIDPConfigurationProperties());
        federatedAuthenticatorConfig.setName(GOOGLE_AUTHENTICATOR);
        federatedAuthenticatorConfig.setDisplayName("Google");
        federatedAuthenticatorConfig.setEnabled(true);
        identityProvider.setDefaultAuthenticatorConfig(federatedAuthenticatorConfig);
        identityProvider.setFederatedAuthenticatorConfigs(new
                FederatedAuthenticatorConfig[]{federatedAuthenticatorConfig});
        identityProviderMgtServiceClient.addIdP(identityProvider);
    }

    private org.wso2.carbon.identity.application.common.model.xsd.IdentityProvider getFederatedGoogleIDP() {

        org.wso2.carbon.identity.application.common.model.xsd.IdentityProvider identityProvider = new org.wso2.carbon
                .identity.application.common.model.xsd.IdentityProvider();
        identityProvider.setIdentityProviderName(IDENTITY_PROVIDER_NAME);
        identityProvider.setAlias(IDENTITY_PROVIDER_ALIAS);
        identityProvider.setEnable(true);

        org.wso2.carbon.identity.application.common.model.xsd.FederatedAuthenticatorConfig
                federatedAuthenticatorConfig = new org.wso2.carbon.identity.application.common.model.xsd
                .FederatedAuthenticatorConfig();
        federatedAuthenticatorConfig.setProperties(getGoogleIDPConfigurationPropertiesForXSD());
        federatedAuthenticatorConfig.setName(GOOGLE_AUTHENTICATOR);
        federatedAuthenticatorConfig.setDisplayName("Google");
        federatedAuthenticatorConfig.setEnabled(true);
        identityProvider.setDefaultAuthenticatorConfig(federatedAuthenticatorConfig);
        identityProvider.setFederatedAuthenticatorConfigs(new org.wso2.carbon.identity.application.common.model.xsd
                .FederatedAuthenticatorConfig[]{federatedAuthenticatorConfig});
        return identityProvider;
    }

    private void deleteIDP() throws Exception {
        identityProviderMgtServiceClient.deleteIdP(IDENTITY_PROVIDER_NAME);
    }

    private void changeISConfiguration() throws Exception {

        log.info("Replacing application-authentication.xml setting showAuthFailureReason true");

        String carbonHome = CarbonUtils.getCarbonHome();
        File applicationXML = new File(carbonHome + File.separator + "repository" + File.separator + "conf" + File
                .separator + "identity" + File.separator + "application-authentication.xml");
        File configuredApplicationXML = new File(getISResourceLocation() + File.separator + "saml" + File.separator +
                "error-code-enabled-application-authentication.xml");
        File properties = new File(carbonHome + File.separator + "repository" + File.separator + "conf" + File
                .separator + "identity" + File.separator + "identity-mgt.properties");
        File configuredProperties = new File(getISResourceLocation() + File.separator + "saml" + File.separator +
                "identity-mgt-uselocked.properties");
        File identityXML = new File(carbonHome + File.separator + "repository" + File.separator + "conf" + File
                .separator + "identity" + File.separator + "identity.xml");
        File configuredIdentityXML = new File(getISResourceLocation() + File.separator + "saml" + File.separator +
                "identity-mgt-listener-enabled.xml");
        serverConfigurationManager = new ServerConfigurationManager(isServer);
        serverConfigurationManager.applyConfigurationWithoutRestart(configuredApplicationXML, applicationXML, true);
        serverConfigurationManager.applyConfigurationWithoutRestart(configuredProperties, properties, true);
        serverConfigurationManager.applyConfigurationWithoutRestart(configuredIdentityXML, identityXML, true);
        serverConfigurationManager.restartGracefully();
    }

    private void resetISConfiguration() throws Exception {

        log.info("Replacing identity.xml with default configurations");
        serverConfigurationManager.restoreToLastConfiguration();
    }

    private void createLocalAndOutBoundAuthenticator() throws Exception {
        switch (config.getAuthenticator()) {
            case DEFAULT_AUTHENTICATOR:
                createDefaultAuthenticator();
                break;
            case ADVANCED_AUTHENTICATOR:
                createAdvanceAuthenticatorWithMultiOptions();
                break;
            case FEDERATED_AUTHENTICATOR:
                createFederatedAuthenticator();
                break;
            case LOCAL_AUTHENTICATOR:
                createLocalAuthenticator();
                break;
            default:
                createDefaultAuthenticator();
                break;
        }

    }

    private HttpResponse sendPostMessage(String sessionKey) throws Exception{
        switch (config.getUser()){
            case SUPER_TENANT_USER:
                return Utils.sendPOSTMessage(sessionKey, COMMON_AUTH_URL, USER_AGENT, ACS_URL, config.getApplication()
                        .getArtifact(), config.getUser().getUsername(), config.getUser().getPassword(), httpClient);
            case SUPER_TENANT_LOCKED_USER:
                return Utils.sendPOSTMessage(sessionKey, COMMON_AUTH_URL, USER_AGENT, ACS_URL, config.getApplication()
                        .getArtifact(), config.getUser().getUsername(), config.getUser().getPassword(), httpClient);
            case SUPER_TENANT_WRONG_CREDENTIAL_USER:
                return Utils.sendPOSTMessage(sessionKey, COMMON_AUTH_URL, USER_AGENT, ACS_URL, config.getApplication()
                        .getArtifact(), config.getUser().getUsername(), "wrongpass", httpClient);
            default:
                return null;
        }

    }

    /**
     * Create the Default Authenticator.
     * Use this method to assign properties to the default authenticator.
     */
    private void createDefaultAuthenticator() {
        config.getAuthenticator().setLocalAndOutboundAuthenticationConfig(new LocalAndOutboundAuthenticationConfig());
    }

    /**
     * Create the AdvancedAuthenticator with Multi options.
     * Use any attributes needed if needed to do multiple tests with different advanced authenticators.
     * @throws Exception
     */
    private void createAdvanceAuthenticatorWithMultiOptions() throws Exception {
        LocalAndOutboundAuthenticationConfig localAndOutboundAuthenticationConfig = new
                LocalAndOutboundAuthenticationConfig();
        AuthenticationStep authenticationStep = new AuthenticationStep();
        authenticationStep.setStepOrder(1);
        LocalAuthenticatorConfig localConfig = new LocalAuthenticatorConfig();
        localConfig.setName(CommonConstants.BASIC_AUTHENTICATOR);
        localConfig.setDisplayName("basicauth");
        localConfig.setEnabled(true);
        authenticationStep.setLocalAuthenticatorConfigs(new LocalAuthenticatorConfig[]{localConfig});
        authenticationStep.setFederatedIdentityProviders(new org.wso2.carbon.identity.application.common.model.xsd
                .IdentityProvider[]{getFederatedGoogleIDP()});
        authenticationStep.setSubjectStep(true);
        authenticationStep.setAttributeStep(true);
        localAndOutboundAuthenticationConfig.addAuthenticationSteps(authenticationStep);
        config.getAuthenticator().setLocalAndOutboundAuthenticationConfig(localAndOutboundAuthenticationConfig);
    }

    /**
     * Create the federated authenticator as needed for the test
     */
    private void createFederatedAuthenticator() {
        // This method needed to be implemented as expected for the testcase
    }

    /**
     * Create the local authenticator as needed for the test
     */
    private void createLocalAuthenticator() {
        // This method needed to be implemented as expected for the testcase
    }
    private Property[] getGoogleIDPConfigurationProperties() {

        Property[] configProperties = new Property[4];

        Property clientId = new Property();
        clientId.setName(CLIENT_ID);
        clientId.setValue("522114753454-89bueikiqlkhc7feene1j0k5rm17nd31.apps.googleusercontent.com");
        clientId.setDisplayName("Client Id");
        clientId.setRequired(true);
        clientId.setDescription("Enter Google IDP client identifier value");
        clientId.setDisplayOrder(1);
        configProperties[0] = clientId;

        Property clientSecret = new Property();
        clientSecret.setName(CLIENT_SECRET);
        clientSecret.setValue("ndq8-TlL5s5W2WhmXztPjmD9");
        clientSecret.setDisplayName("Client Secret");
        clientSecret.setRequired(true);
        clientSecret.setConfidential(true);
        clientSecret.setDescription("Enter Google IDP client secret value");
        clientSecret.setDisplayOrder(2);
        configProperties[1] = clientSecret;

        Property callbackUrl = new Property();
        callbackUrl.setDisplayName("Callback Url");
        callbackUrl.setName(CALLBACK_URL);
        callbackUrl.setDescription("Enter value corresponding to callback url.");
        callbackUrl.setDisplayOrder(3);
        configProperties[2] = callbackUrl;

        Property scope = new Property();
        scope.setDisplayName("Additional Query Parameters");
        scope.setName("AdditionalQueryParameters");
        scope.setValue("scope=openid email profile");
        scope.setDescription("Additional query parameters. e.g: paramName1=value1");
        scope.setDisplayOrder(4);
        configProperties[3] = scope;

        return configProperties;
    }

    private org.wso2.carbon.identity.application.common.model.xsd.Property[]
    getGoogleIDPConfigurationPropertiesForXSD() {

        org.wso2.carbon.identity.application.common.model.xsd.Property[] configProperties = new org.wso2.carbon
                .identity.application.common.model.xsd.Property[4];

        org.wso2.carbon.identity.application.common.model.xsd.Property clientId = new org.wso2.carbon.identity
                .application.common.model.xsd.Property();
        clientId.setName(CLIENT_ID);
        clientId.setValue("522114753454-89bueikiqlkhc7feene1j0k5rm17nd31.apps.googleusercontent.com");
        clientId.setDisplayName("Client Id");
        clientId.setRequired(true);
        clientId.setDescription("Enter Google IDP client identifier value");
        clientId.setDisplayOrder(1);
        configProperties[0] = clientId;

        org.wso2.carbon.identity.application.common.model.xsd.Property clientSecret = new org.wso2.carbon.identity
                .application.common.model.xsd.Property();
        clientSecret.setName(CLIENT_SECRET);
        clientSecret.setValue("ndq8-TlL5s5W2WhmXztPjmD9");
        clientSecret.setDisplayName("Client Secret");
        clientSecret.setRequired(true);
        clientSecret.setConfidential(true);
        clientSecret.setDescription("Enter Google IDP client secret value");
        clientSecret.setDisplayOrder(2);
        configProperties[1] = clientSecret;

        org.wso2.carbon.identity.application.common.model.xsd.Property callbackUrl = new org.wso2.carbon.identity
                .application.common.model.xsd.Property();
        callbackUrl.setDisplayName("Callback Url");
        callbackUrl.setName(CALLBACK_URL);
        callbackUrl.setDescription("Enter value corresponding to callback url.");
        callbackUrl.setDisplayOrder(3);
        configProperties[2] = callbackUrl;

        org.wso2.carbon.identity.application.common.model.xsd.Property scope = new org.wso2.carbon.identity
                .application.common.model.xsd.Property();
        scope.setDisplayName("Additional Query Parameters");
        scope.setName("AdditionalQueryParameters");
        scope.setValue("scope=openid email profile");
        scope.setDescription("Additional query parameters. e.g: paramName1=value1");
        scope.setDisplayOrder(4);
        configProperties[3] = scope;

        return configProperties;
    }

}

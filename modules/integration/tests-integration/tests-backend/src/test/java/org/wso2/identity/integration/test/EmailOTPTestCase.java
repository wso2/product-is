/*
 *  Copyright (c) 2020, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
 *
 *  Licensed under the Apache License, Version 2.0 (the "License");
 *  you may not use this file except in compliance with the License.
 *  You may obtain a copy of the License at
 *
 *  http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  See the License for the specific language governing permissions and
 *  limitations under the License.
 */

package org.wso2.identity.integration.test;

import org.apache.axis2.context.ConfigurationContext;
import org.apache.axis2.context.ConfigurationContextFactory;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.http.HttpResponse;
import org.apache.http.NameValuePair;
import org.apache.http.client.HttpClient;
import org.apache.http.client.config.CookieSpecs;
import org.apache.http.client.config.RequestConfig;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.config.Lookup;
import org.apache.http.config.RegistryBuilder;
import org.apache.http.cookie.CookieSpecProvider;
import org.apache.http.impl.client.BasicCookieStore;
import org.apache.http.impl.client.HttpClientBuilder;
import org.apache.http.impl.cookie.RFC6265CookieSpecProvider;
import org.apache.http.message.BasicNameValuePair;
import org.apache.http.util.EntityUtils;
import org.testng.Assert;
import org.testng.annotations.AfterClass;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.DataProvider;
import org.testng.annotations.Factory;
import org.testng.annotations.Test;
import org.wso2.carbon.automation.engine.context.TestUserMode;
import org.wso2.carbon.identity.application.common.model.xsd.AuthenticationStep;
import org.wso2.carbon.identity.application.common.model.xsd.FederatedAuthenticatorConfig;
import org.wso2.carbon.identity.application.common.model.xsd.IdentityProvider;
import org.wso2.carbon.identity.application.common.model.xsd.InboundAuthenticationConfig;
import org.wso2.carbon.identity.application.common.model.xsd.InboundAuthenticationRequestConfig;
import org.wso2.carbon.identity.application.common.model.xsd.LocalAndOutboundAuthenticationConfig;
import org.wso2.carbon.identity.application.common.model.xsd.LocalAuthenticatorConfig;
import org.wso2.carbon.identity.application.common.model.xsd.Property;
import org.wso2.carbon.identity.application.common.model.xsd.ServiceProvider;
import org.wso2.carbon.identity.sso.saml.stub.types.SAMLSSOServiceProviderDTO;
import org.wso2.carbon.integration.common.utils.exceptions.AutomationUtilException;
import org.wso2.carbon.integration.common.utils.mgt.ServerConfigurationManager;
import org.wso2.carbon.um.ws.api.stub.ClaimValue;
import org.wso2.carbon.um.ws.api.stub.RemoteUserStoreManagerServiceUserStoreExceptionException;
import org.wso2.carbon.user.api.UserStoreException;
import org.wso2.identity.integration.common.clients.Idp.IdentityProviderMgtServiceClient;
import org.wso2.identity.integration.common.clients.application.mgt.ApplicationManagementServiceClient;
import org.wso2.identity.integration.common.clients.sso.saml.SAMLSSOConfigServiceClient;
import org.wso2.identity.integration.common.clients.usermgt.remote.RemoteUserStoreManagerServiceClient;
import org.wso2.identity.integration.common.utils.ISIntegrationTest;
import org.wso2.identity.integration.test.util.Utils;
import org.wso2.identity.integration.test.utils.CommonConstants;

import java.io.File;
import java.io.IOException;
import java.rmi.RemoteException;
import java.util.ArrayList;
import java.util.List;
import javax.xml.xpath.XPathExpressionException;

import static org.wso2.identity.integration.test.utils.CommonConstants.DEFAULT_TOMCAT_PORT;

/**
 * This class adds test cases for configuring email otp and login with a super tenant and a tenant user.
 */
public class EmailOTPTestCase extends ISIntegrationTest {

    private static final Log log = LogFactory.getLog(EmailOTPTestCase.class);

    private static final String APPLICATION_NAME = "SAML-SSO-TestApplication";
    private static final String FIRST_NAME_CLAIM_URI = "http://wso2.org/claims/givenname";
    private static final String LAST_NAME_CLAIM_URI = "http://wso2.org/claims/lastname";
    private static final String EMAIL_CLAIM_URI = "http://wso2.org/claims/emailaddress";
    private static final String INBOUND_AUTH_TYPE = "samlsso";
    private static final String ATTRIBUTE_CS_INDEX_VALUE = "1239245949";
    private static final String ATTRIBUTE_CS_INDEX_NAME = "attrConsumServiceIndex";
    private static final String ACS_URL = "http://localhost:8490/%s/home.jsp";
    private static final String NAMEID_FORMAT = "urn:oasis:names:tc:SAML:1.1:nameid-format:emailAddress";
    private static final String LOGIN_URL = "/carbon/admin/login.jsp";
    private static final String IDENTITY_PROVIDER_NAME = "emailOTPIdP";
    private static final String IDENTITY_PROVIDER_ALIAS = "https://localhost:" + CommonConstants.IS_DEFAULT_HTTPS_PORT
            + "/oauth2/token/";
    private static final String AUTHENTICATOR_NAME = "EmailOTP";
    private static final String EMAIL_OTP_CONFIG_TOML = "email_otp_config.toml";
    private static final String EMAIL_ADMIN_CONFIG_XML = "email-admin-config.xml";
    private static final String EMAIL_OTP_TEMPLATE_CONFIGURED_XML = "emailOTP-email-template.xml";
    private static final String SAML_SSO_LOGIN_URL = "http://localhost:" + DEFAULT_TOMCAT_PORT +
            "/%s/samlsso?SAML2.HTTPBinding=HTTP-POST";
    private static final String SAML_SSO_URL = "https://localhost:9853/samlsso";
    private static final String COMMON_AUTH_URL = "https://localhost:9853/commonauth";
    private static final String EMAIL_OTP_AUTHENTICATION_ENDPOINT_URL = "https://localhost:" +
            CommonConstants.IS_DEFAULT_HTTPS_PORT + "/authenticationendpoint/email_otp.do";
    private static final String USER_AGENT = "Apache-HttpClient/4.2.5 (java 1.5)";
    private static final String profileName = "default";

    private Lookup<CookieSpecProvider> cookieSpecRegistry;
    private RequestConfig requestConfig;
    private HttpClient httpClient;
    private ApplicationManagementServiceClient applicationManagementServiceClient;
    private SAMLSSOConfigServiceClient ssoConfigServiceClient;
    private IdentityProviderMgtServiceClient identityProviderMgtServiceClient;
    private ServerConfigurationManager serverConfigurationManager;
    private RemoteUserStoreManagerServiceClient remoteUSMServiceClient;
    private TestConfig config;

    @Factory(dataProvider = "testConfigProvider")
    public EmailOTPTestCase(TestConfig config) {

        if (log.isDebugEnabled()) {
            log.info("Email OTP test case initialized for " + config);
        }
        this.config = config;
    }

    @BeforeClass(alwaysRun = true)
    public void testInit() throws Exception {

        super.init();
        changeISConfiguration();
        // Re-initiating after the restart.
        super.init(config.getUserMode());
        ConfigurationContext configContext = ConfigurationContextFactory.
                createConfigurationContextFromFileSystem(null, null);
        applicationManagementServiceClient = new ApplicationManagementServiceClient(sessionCookie, backendURL,
                configContext);
        ssoConfigServiceClient = new SAMLSSOConfigServiceClient(backendURL, sessionCookie);
        identityProviderMgtServiceClient = new IdentityProviderMgtServiceClient(sessionCookie, backendURL,
                configContext);
        remoteUSMServiceClient = new RemoteUserStoreManagerServiceClient(backendURL, sessionCookie);

        cookieSpecRegistry = RegistryBuilder.<CookieSpecProvider>create()
                .register(CookieSpecs.DEFAULT, new RFC6265CookieSpecProvider())
                .build();
        requestConfig = RequestConfig.custom()
                .setCookieSpec(CookieSpecs.DEFAULT)
                .build();
        httpClient = HttpClientBuilder.create().setDefaultCookieStore(new BasicCookieStore())
                .setDefaultRequestConfig(requestConfig)
                .setDefaultCookieSpecRegistry(cookieSpecRegistry)
                .build();
        createUser();
        createApplication();
    }

    @AfterClass(alwaysRun = true)
    public void testClear() throws Exception {

        deleteUser();
        deleteApplication();

        applicationManagementServiceClient = null;
        ssoConfigServiceClient = null;
        identityProviderMgtServiceClient = null;
        remoteUSMServiceClient = null;
        httpClient = null;
    }

    @Test(description = "Testing Email OTP authentication with a SAML SP", groups = "wso2.is", priority = 1)
    public void testEmailOTPAuthentication() {

        try {
            HttpResponse response = Utils.sendGetRequest(String.format(SAML_SSO_LOGIN_URL, config.getSpEntityId()),
                    USER_AGENT, httpClient);
            String samlRequest = Utils.extractDataFromResponse(response, CommonConstants.SAML_REQUEST_PARAM, 5);
            response = sendSAMLMessage(SAML_SSO_URL, samlRequest);
            EntityUtils.consume(response.getEntity());
            response = Utils.sendRedirectRequest(response, USER_AGENT, ACS_URL, config.getSpEntityId(), httpClient);
            String sessionKey = Utils.extractDataFromResponse(response, CommonConstants.SESSION_DATA_KEY, 1);
            response = Utils.sendPOSTMessage(sessionKey, getTenantQualifiedURL(SAML_SSO_URL,
                            config.getTenantDomain()), USER_AGENT, ACS_URL, config.getSpEntityId(),
                    config.getTenantAwareUsername(), config.getPassword(), httpClient,
                    getTenantQualifiedURL(SAML_SSO_URL, config.getTenantDomain()));

            if (Utils.requestMissingClaims(response)) {
                String pastrCookie = Utils.getPastreCookie(response);
                Assert.assertNotNull(pastrCookie, "pastr cookie not found in response.");
                EntityUtils.consume(response.getEntity());
                response = Utils.sendPOSTConsentMessage(response, COMMON_AUTH_URL, USER_AGENT, String.format(ACS_URL,
                        config.getSpEntityId()), httpClient, pastrCookie);
                EntityUtils.consume(response.getEntity());
            }

            String redirectUrl = Utils.getRedirectUrl(response);
            Assert.assertTrue(redirectUrl.contains(getTenantQualifiedURL(
                    EMAIL_OTP_AUTHENTICATION_ENDPOINT_URL, config.getTenantDomain())),
                    "Error in redirection to email OTP authentication page for user: " + config.getUsername());
        } catch (Exception e) {
            Assert.fail("Authentication failed for user: " + config.getUsername(), e);
        }
    }

    private void createUser() {

        log.info("Creating user: " + config.getUsername());
        try {
            remoteUSMServiceClient.addUser(config.getTenantAwareUsername(), config.getPassword(), null,
                    getUserClaims(), profileName, true);
        } catch (UserStoreException | RemoteException | RemoteUserStoreManagerServiceUserStoreExceptionException e) {
            log.error("Error while creating the user: " + config.getUsername(), e);
        }
    }

    private void deleteUser() {

        log.info("Deleting user: " + config.getUsername());
        try {
            remoteUSMServiceClient.deleteUser(config.getTenantAwareUsername());
        } catch (RemoteUserStoreManagerServiceUserStoreExceptionException | RemoteException e) {
            log.error("Error while deleting the user: " + config.getUsername(), e);
        }
    }

    private HttpResponse sendSAMLMessage(String url, String samlMsgValue) throws IOException {

        List<NameValuePair> urlParameters = new ArrayList<>();
        HttpPost post = new HttpPost(getTenantQualifiedURL(url, tenantInfo.getDomain()));
        post.setHeader("User-Agent", USER_AGENT);
        urlParameters.add(new BasicNameValuePair(CommonConstants.SAML_REQUEST_PARAM, samlMsgValue));
        post.setEntity(new UrlEncodedFormEntity(urlParameters));
        return httpClient.execute(post);
    }

    private void changeISConfiguration() throws AutomationUtilException, XPathExpressionException, IOException {

        String carbonHome = Utils.getResidentCarbonHome();
        File defaultTomlFile = getDeploymentTomlFile(carbonHome);
        File emailOTPConfigFile = new File(getISResourceLocation() + File.separator + "email" + File.separator
                + EmailOTPTestCase.EMAIL_OTP_CONFIG_TOML);
        serverConfigurationManager = new ServerConfigurationManager(isServer);
        serverConfigurationManager.applyConfigurationWithoutRestart(emailOTPConfigFile, defaultTomlFile, true);
        changeEmailAdminConfigXml(carbonHome);
        serverConfigurationManager.restartGracefully();
    }

    /**
     * This method is to add the email template for email otp in the email-admin-config.xml file.
     *
     * @param carbonHome Resident carbon home.
     * @throws IOException Error in applying the config file change.
     */
    private void changeEmailAdminConfigXml(String carbonHome) throws IOException {

        File defaultEmailAdminConfigFile = new File(carbonHome + File.separator + "repository" + File.separator
                + "conf" + File.separator + "email" + File.separator + EMAIL_ADMIN_CONFIG_XML);
        File emailOTPEmailTemplateAddedFile = new File(getISResourceLocation() + File.separator + "email" +
                File.separator + EMAIL_OTP_TEMPLATE_CONFIGURED_XML);
        serverConfigurationManager.applyConfigurationWithoutRestart(emailOTPEmailTemplateAddedFile,
                defaultEmailAdminConfigFile, true);
    }

    private void createApplication() throws Exception {

        ServiceProvider serviceProvider = new ServiceProvider();
        serviceProvider.setApplicationName(APPLICATION_NAME);
        serviceProvider.setDescription("This is a test Service Provider");
        applicationManagementServiceClient.createApplication(serviceProvider);
        serviceProvider = applicationManagementServiceClient.getApplication(APPLICATION_NAME);

        InboundAuthenticationRequestConfig requestConfig = new InboundAuthenticationRequestConfig();
        requestConfig.setInboundAuthType(INBOUND_AUTH_TYPE);
        requestConfig.setInboundAuthKey(config.getSpEntityId());
        Property attributeConsumerServiceIndexProp = new Property();
        attributeConsumerServiceIndexProp.setName(ATTRIBUTE_CS_INDEX_NAME);
        attributeConsumerServiceIndexProp.setValue(ATTRIBUTE_CS_INDEX_VALUE);
        requestConfig.setProperties(new Property[]{attributeConsumerServiceIndexProp});
        InboundAuthenticationConfig inboundAuthenticationConfig = new InboundAuthenticationConfig();
        inboundAuthenticationConfig.setInboundAuthenticationRequestConfigs(new InboundAuthenticationRequestConfig[]
                {requestConfig});
        serviceProvider.setInboundAuthenticationConfig(inboundAuthenticationConfig);

        serviceProvider.setLocalAndOutBoundAuthenticationConfig(getLocalAndOutBoundAuthenticator());
        serviceProvider.getLocalAndOutBoundAuthenticationConfig().setSubjectClaimUri(EMAIL_CLAIM_URI);

        applicationManagementServiceClient.updateApplicationData(serviceProvider);
        ssoConfigServiceClient.addServiceProvider(getSsoServiceProviderDTO());
    }

    private void deleteApplication() throws Exception {

        applicationManagementServiceClient.deleteApplication(APPLICATION_NAME);
    }

    private SAMLSSOServiceProviderDTO getSsoServiceProviderDTO() {

        SAMLSSOServiceProviderDTO samlssoServiceProviderDTO = new SAMLSSOServiceProviderDTO();
        samlssoServiceProviderDTO.setIssuer(config.getSpEntityId());
        samlssoServiceProviderDTO.setAssertionConsumerUrls(new String[]{String.format(ACS_URL,
                config.getSpEntityId())});
        samlssoServiceProviderDTO.setDefaultAssertionConsumerUrl(String.format(ACS_URL, config.getSpEntityId()));
        samlssoServiceProviderDTO.setNameIDFormat(NAMEID_FORMAT);
        samlssoServiceProviderDTO.setDoSignAssertions(true);
        samlssoServiceProviderDTO.setDoSignResponse(false);
        samlssoServiceProviderDTO.setDoSingleLogout(true);
        samlssoServiceProviderDTO.setLoginPageURL(LOGIN_URL);

        return samlssoServiceProviderDTO;
    }

    /**
     * This method is to create and get the email otp identity provider.
     *
     * @return Identity provider.
     * @throws Exception Error when adding Identity Provider information.
     */
    private IdentityProvider getEmailOTPIdP() throws Exception {

        IdentityProvider identityProvider = new IdentityProvider();
        identityProvider.setIdentityProviderName(IDENTITY_PROVIDER_NAME);
        identityProvider.setAlias(IDENTITY_PROVIDER_ALIAS);
        identityProvider.setEnable(true);

        FederatedAuthenticatorConfig federatedAuthenticatorConfig = new FederatedAuthenticatorConfig();
        federatedAuthenticatorConfig.setName(AUTHENTICATOR_NAME);
        federatedAuthenticatorConfig.setEnabled(true);
        identityProvider.setDefaultAuthenticatorConfig(federatedAuthenticatorConfig);
        identityProvider.setFederatedAuthenticatorConfigs(new FederatedAuthenticatorConfig[]
                {federatedAuthenticatorConfig});
        createIdP(identityProvider);

        return identityProvider;
    }

    private LocalAndOutboundAuthenticationConfig getLocalAndOutBoundAuthenticator() throws Exception {

        LocalAndOutboundAuthenticationConfig localAndOutboundAuthenticationConfig = new
                LocalAndOutboundAuthenticationConfig();
        // This will add basic authentication as the first step for authentication.
        AuthenticationStep authenticationStepOne = new AuthenticationStep();
        authenticationStepOne.setStepOrder(1);
        LocalAuthenticatorConfig localConfig = new LocalAuthenticatorConfig();
        localConfig.setName(CommonConstants.BASIC_AUTHENTICATOR);
        localConfig.setDisplayName("basicauth");
        localConfig.setEnabled(true);
        authenticationStepOne.setLocalAuthenticatorConfigs(new LocalAuthenticatorConfig[]{localConfig});
        localAndOutboundAuthenticationConfig.addAuthenticationSteps(authenticationStepOne);
        // This will add email otp as the second step for authentication.
        AuthenticationStep authenticationStepTwo = new AuthenticationStep();
        authenticationStepTwo.setStepOrder(2);
        authenticationStepTwo.setSubjectStep(false);
        authenticationStepTwo.setAttributeStep(false);
        authenticationStepTwo.setFederatedIdentityProviders(new IdentityProvider[]{getEmailOTPIdP()});
        localAndOutboundAuthenticationConfig.addAuthenticationSteps(authenticationStepTwo);

        return localAndOutboundAuthenticationConfig;
    }

    private void createIdP(IdentityProvider idp) throws Exception {

        org.wso2.carbon.identity.application.common.model.idp.xsd.IdentityProvider identityProvider
                = new org.wso2.carbon.identity.application.common.model.idp.xsd.IdentityProvider();
        identityProvider.setIdentityProviderName(idp.getIdentityProviderName());
        org.wso2.carbon.identity.application.common.model.idp.xsd.FederatedAuthenticatorConfig
                federatedAuthenticatorConfig = new org.wso2.carbon.identity.application.common.model.idp.xsd.
                FederatedAuthenticatorConfig();
        federatedAuthenticatorConfig.setName(idp.getDefaultAuthenticatorConfig().getName());
        federatedAuthenticatorConfig.setEnabled(idp.getDefaultAuthenticatorConfig().getEnabled());
        identityProvider.setFederatedAuthenticatorConfigs(new org.wso2.carbon.identity.application.common.model.idp.xsd.
                FederatedAuthenticatorConfig[]{federatedAuthenticatorConfig});
        identityProvider.setDefaultAuthenticatorConfig(federatedAuthenticatorConfig);
        identityProviderMgtServiceClient.addIdP(identityProvider);
    }

    private ClaimValue[] getUserClaims() {

        ClaimValue[] claimValues = new ClaimValue[3];

        ClaimValue firstName = new ClaimValue();
        firstName.setClaimURI(FIRST_NAME_CLAIM_URI);
        firstName.setValue(config.getUsername());
        claimValues[0] = firstName;

        ClaimValue lastName = new ClaimValue();
        lastName.setClaimURI(LAST_NAME_CLAIM_URI);
        lastName.setValue(config.getUsername());
        claimValues[1] = lastName;

        ClaimValue email = new ClaimValue();
        email.setClaimURI(EMAIL_CLAIM_URI);
        email.setValue(config.getEmailAddress());
        claimValues[2] = email;

        return claimValues;
    }

    @DataProvider(name = "testConfigProvider")
    public static TestConfig[][] testConfigProvider(){

        return new TestConfig[][] {
                {new TestConfig(TestUserMode.SUPER_TENANT_ADMIN, "testuser1", "Wso2@test1",
                "carbon.super", "testuser1", "testuser1@abc.com", "travelocity.com")},
                {new TestConfig(TestUserMode.TENANT_ADMIN, "testuser2@wso2.com", "Wso2@test2",
                        "wso2.com", "testuser2", "testuser2@abc.com", "travelocity.com-saml-tenantwithoutsigning")}
        };
    }

    private static class TestConfig {

        private TestUserMode userMode;
        private String username;
        private String password;
        private String tenantDomain;
        private String tenantAwareUsername;
        private String emailAddress;
        private String spEntityId;

        TestConfig(TestUserMode userMode, String username, String password, String tenantDomain,
                   String tenantAwareUsername, String emailAddress, String spEntityId) {

            this.userMode = userMode;
            this.username = username;
            this.password = password;
            this.tenantDomain = tenantDomain;
            this.tenantAwareUsername = tenantAwareUsername;
            this.emailAddress = emailAddress;
            this.spEntityId = spEntityId;
        }

        public TestUserMode getUserMode() {

            return userMode;
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

        public String getEmailAddress() {

            return emailAddress;
        }

        public String getSpEntityId() {

            return spEntityId;
        }

        @Override
        public String toString() {

            return "TestConfig{" +
                    "userMode=" + userMode +
                    ", username='" + username +
                    '\'' + ", password='" + password +
                    '\'' + ", tenantDomain='" + tenantDomain +
                    '\'' + ", tenantAwareUsername='" + tenantAwareUsername +
                    '\'' + ", emailAddress='" + emailAddress +
                    '\'' + ", spEntityId='" + spEntityId +
                    '\'' + '}';
        }
    }

}

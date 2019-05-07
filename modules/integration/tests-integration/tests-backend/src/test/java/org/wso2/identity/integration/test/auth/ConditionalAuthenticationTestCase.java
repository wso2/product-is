/*
 *  Copyright (c) 2018 WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
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

package org.wso2.identity.integration.test.auth;

import org.apache.axis2.context.ConfigurationContext;
import org.apache.axis2.context.ConfigurationContextFactory;
import org.apache.http.Header;
import org.apache.http.HttpResponse;
import org.apache.http.client.CookieStore;
import org.apache.http.impl.client.BasicCookieStore;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.util.EntityUtils;
import org.testng.annotations.AfterClass;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;
import org.wso2.carbon.automation.engine.context.AutomationContext;
import org.wso2.carbon.automation.engine.context.TestUserMode;
import org.wso2.carbon.automation.extensions.servers.carbonserver.MultipleServersManager;
import org.wso2.carbon.identity.application.common.model.idp.xsd.FederatedAuthenticatorConfig;
import org.wso2.carbon.identity.application.common.model.idp.xsd.IdentityProvider;
import org.wso2.carbon.identity.application.common.model.script.xsd.AuthenticationScriptConfig;
import org.wso2.carbon.identity.application.common.model.xsd.AuthenticationStep;
import org.wso2.carbon.identity.application.common.model.xsd.InboundAuthenticationConfig;
import org.wso2.carbon.identity.application.common.model.xsd.InboundAuthenticationRequestConfig;
import org.wso2.carbon.identity.application.common.model.xsd.LocalAndOutboundAuthenticationConfig;
import org.wso2.carbon.identity.application.common.model.xsd.ServiceProvider;
import org.wso2.carbon.identity.sso.saml.stub.types.SAMLSSOServiceProviderDTO;
import org.wso2.carbon.integration.common.admin.client.AuthenticatorClient;
import org.wso2.identity.integration.common.clients.Idp.IdentityProviderMgtServiceClient;
import org.wso2.identity.integration.common.clients.application.mgt.ApplicationManagementServiceClient;
import org.wso2.identity.integration.common.clients.oauth.OauthAdminClient;
import org.wso2.identity.integration.common.clients.sso.saml.SAMLSSOConfigServiceClient;
import org.wso2.identity.integration.common.utils.CarbonTestServerManager;
import org.wso2.identity.integration.test.base.TestDataHolder;
import org.wso2.identity.integration.test.utils.CommonConstants;
import org.wso2.identity.integration.test.utils.IdentityConstants;

import java.util.HashMap;
import java.util.Map;

import static org.testng.Assert.assertTrue;
import static org.wso2.identity.integration.test.utils.CommonConstants.IS_DEFAULT_HTTPS_PORT;
import static org.wso2.identity.integration.test.utils.OAuth2Constant.CALLBACK_URL;
import static org.wso2.identity.integration.test.utils.OAuth2Constant.COMMON_AUTH_URL;

/**
 * Test class to test the conditional authentication support using Javascript feature.
 */
public class ConditionalAuthenticationTestCase extends AbstractAdaptiveAuthenticationTestCase {

    private static final String IDENTITY_PROVIDER_ALIAS =
            "https://localhost:" + IS_DEFAULT_HTTPS_PORT + "/oauth2/token/";
    private static final String SECONDARY_IS_SAMLSSO_URL = "https://localhost:9854/samlsso";
    private static final int PORT_OFFSET_1 = 1;
    private static final String SAML_NAME_ID_FORMAT = "urn:oasis:names:tc:SAML:1.1:nameid-format:emailAddress";
    private static final String PRIMARY_IS_APPLICATION_NAME = "testOauthApp";
    private static final String SECONDARY_IS_APPLICATION_NAME = "testSAMLApp";
    private static final String IDP_NAME = "secondaryIS";

    private AuthenticatorClient logManger;
    private OauthAdminClient oauthAdminClient;
    private ApplicationManagementServiceClient applicationManagementServiceClient, applicationManagementServiceClient2;
    private IdentityProviderMgtServiceClient identityProviderMgtServiceClient;
    private MultipleServersManager manager;
    private SAMLSSOConfigServiceClient samlSSOConfigServiceClient;
    private DefaultHttpClient client;
    private ServiceProvider serviceProvider;
    private HttpResponse response;
    private CookieStore cookieStore;
    private TestDataHolder testDataHolder;

    private String initialCarbonHome;


    @BeforeClass(alwaysRun = true)
    public void testInit() throws Exception {

        super.init();
        testDataHolder = TestDataHolder.getInstance();
        initialCarbonHome = System.getProperty("carbon.home");
        logManger = new AuthenticatorClient(backendURL);
        String cookie = this.logManger.login(isServer.getSuperTenant().getTenantAdmin().getUserName(),
                isServer.getSuperTenant().getTenantAdmin().getPassword(),
                isServer.getInstance().getHosts().get("default"));
        oauthAdminClient = new OauthAdminClient(backendURL, cookie);
        ConfigurationContext configContext = ConfigurationContextFactory
                .createConfigurationContextFromFileSystem(null, null);
        applicationManagementServiceClient = new ApplicationManagementServiceClient(sessionCookie, backendURL,
                configContext);
        identityProviderMgtServiceClient = new IdentityProviderMgtServiceClient(sessionCookie, backendURL);
        manager = testDataHolder.getManager();

        client = new DefaultHttpClient();
        cookieStore = new BasicCookieStore();
        client.setCookieStore(cookieStore);

        startSecondaryIS();
        String script = getConditionalAuthScript("ConditionalAuthenticationTestCase.js");

        createSAMLAppInSecondaryIS();
        createServiceProviderInSecondaryIS();

        // Create federated IDP in primary IS.
        createIDPInPrimaryIS();
        createOauthApp(CALLBACK_URL, PRIMARY_IS_APPLICATION_NAME, oauthAdminClient);
        // Create service provider in primary IS with conditional authentication script enabled.
        serviceProvider = createServiceProvider(PRIMARY_IS_APPLICATION_NAME,
                applicationManagementServiceClient, oauthAdminClient, script);
    }

    @AfterClass(alwaysRun = true)
    public void atEnd() throws Exception {

        try {
            oauthAdminClient.removeOAuthApplicationData(consumerKey);
            samlSSOConfigServiceClient.removeServiceProvider(SECONDARY_IS_APPLICATION_NAME);
            applicationManagementServiceClient.deleteApplication(PRIMARY_IS_APPLICATION_NAME);
            applicationManagementServiceClient2.deleteApplication(SECONDARY_IS_APPLICATION_NAME);
            identityProviderMgtServiceClient.deleteIdP(IDP_NAME);
            client.getConnectionManager().shutdown();

            this.logManger.logOut();
            logManger = null;
            //Restore carbon.home system property to initial value
            System.setProperty("carbon.home", initialCarbonHome);
        } catch (Exception e) {
            log.error("Failure occured due to :" + e.getMessage(), e);
            throw e;
        }
    }

    @Test(groups = "wso2.is", description = "Check conditional authentication flow.")
    public void testConditionalAuthentication() throws Exception {

        updateAuthScript("ConditionalAuthenticationTestCase.js");
        response = loginWithOIDC(PRIMARY_IS_APPLICATION_NAME, consumerKey, client);
        /* Here if the client is redirected to the secondary IS, it indicates that the conditional authentication steps
         has been successfully completed. */
        String locationHeader = response.getFirstHeader("location").getValue();
        EntityUtils.consume(response.getEntity());
        log.info("The location header value of the response: " + locationHeader);
        assertTrue(locationHeader.contains(SECONDARY_IS_SAMLSSO_URL),
                "Failed to follow the conditional authentication steps.");

        cookieStore.clear();
    }

    @Test(groups = "wso2.is", description = "Check conditional authentication flow based on HTTP Cookie.")
    public void testConditionalAuthenticationUsingHTTPCookie() throws Exception {

        // Update authentication script to handle authentication based on HTTP context.
        updateAuthScript("ConditionalAuthenticationHTTPCookieTestCase.js");
        response = loginWithOIDC(PRIMARY_IS_APPLICATION_NAME, consumerKey, client);

        /* Here if the response headers contains the custom HTTP cookie we set from the authentication script, it
        indicates that the conditional authentication steps has been successfully completed. */
        boolean hasTestCookie = false;
        Header[] headers = response.getHeaders("Set-Cookie");
        if (headers != null) {
            for (Header header : headers) {
                String headerValue = header.getValue();
                if (headerValue.contains("testcookie")) {
                    hasTestCookie = true;
                }
            }
        }
        assertTrue(hasTestCookie, "Failed to follow the conditional authentication steps. HTTP Cookie : "
                + "testcookie was not found in the response.");
        EntityUtils.consume(response.getEntity());
        cookieStore.clear();
    }

    @Test(groups = "wso2.is", description = "Check conditional authentication flow with claim assignment.")
    public void testConditionalAuthenticationClaimAssignment() throws Exception {

        // Update authentication script to handle authentication based on HTTP context.
        try {
            updateAuthScript("ConditionalAuthenticationClaimAssignTestCase.js");
            response = loginWithOIDC(PRIMARY_IS_APPLICATION_NAME, consumerKey, client);

            EntityUtils.consume(response.getEntity());
            cookieStore.clear();
        } catch (Exception e) {
            //Temporary added the catch part for the debugging purpose.
            log.error("Failed to execute the testConditionalAuthenticationClaimAssignment: " + e.getMessage(), e);
            throw e;
        }

    }

    /**
     * Create IDP for SAMLSSO Authenticator.
     *
     * @throws Exception
     */
    private void createIDPInPrimaryIS() throws Exception {

        IdentityProvider identityProvider = new IdentityProvider();
        identityProvider.setIdentityProviderName(IDP_NAME);
        identityProvider.setAlias(IDENTITY_PROVIDER_ALIAS);
        identityProvider.setEnable(true);

        FederatedAuthenticatorConfig saml2SSOAuthnConfig = new FederatedAuthenticatorConfig();
        saml2SSOAuthnConfig.setName("SAMLSSOAuthenticator");
        saml2SSOAuthnConfig.setDisplayName("samlsso");
        saml2SSOAuthnConfig.setEnabled(true);
        saml2SSOAuthnConfig.setProperties(getSAML2SSOAuthnConfigProperties());
        identityProvider.setDefaultAuthenticatorConfig(saml2SSOAuthnConfig);
        identityProvider.setFederatedAuthenticatorConfigs(new FederatedAuthenticatorConfig[] { saml2SSOAuthnConfig });

        identityProviderMgtServiceClient.addIdP(identityProvider);
    }

    /**
     * Get SAMLSSO configuration properties.
     *
     * @return
     */
    private org.wso2.carbon.identity.application.common.model.idp.xsd.Property[] getSAML2SSOAuthnConfigProperties() {

        org.wso2.carbon.identity.application.common.model.idp.xsd.Property[] properties = new org.wso2.carbon.identity.application.common.model.idp.xsd.Property[13];
        org.wso2.carbon.identity.application.common.model.idp.xsd.Property property = new org.wso2.carbon.identity.application.common.model.idp.xsd.Property();
        property.setName(IdentityConstants.Authenticator.SAML2SSO.IDP_ENTITY_ID);
        property.setValue(IDP_NAME);
        properties[0] = property;

        property = new org.wso2.carbon.identity.application.common.model.idp.xsd.Property();
        property.setName(IdentityConstants.Authenticator.SAML2SSO.SP_ENTITY_ID);
        property.setValue(SECONDARY_IS_APPLICATION_NAME);
        properties[1] = property;

        property = new org.wso2.carbon.identity.application.common.model.idp.xsd.Property();
        property.setName(IdentityConstants.Authenticator.SAML2SSO.SSO_URL);
        property.setValue(SECONDARY_IS_SAMLSSO_URL);
        properties[2] = property;

        property = new org.wso2.carbon.identity.application.common.model.idp.xsd.Property();
        property.setName(IdentityConstants.Authenticator.SAML2SSO.IS_AUTHN_REQ_SIGNED);
        property.setValue("false");
        properties[3] = property;

        property = new org.wso2.carbon.identity.application.common.model.idp.xsd.Property();
        property.setName(IdentityConstants.Authenticator.SAML2SSO.IS_LOGOUT_ENABLED);
        property.setValue("true");
        properties[4] = property;

        property = new org.wso2.carbon.identity.application.common.model.idp.xsd.Property();
        property.setName(IdentityConstants.Authenticator.SAML2SSO.LOGOUT_REQ_URL);
        properties[5] = property;

        property = new org.wso2.carbon.identity.application.common.model.idp.xsd.Property();
        property.setName(IdentityConstants.Authenticator.SAML2SSO.IS_LOGOUT_REQ_SIGNED);
        property.setValue("false");
        properties[6] = property;

        property = new org.wso2.carbon.identity.application.common.model.idp.xsd.Property();
        property.setName(IdentityConstants.Authenticator.SAML2SSO.IS_AUTHN_RESP_SIGNED);
        property.setValue("false");
        properties[7] = property;

        property = new org.wso2.carbon.identity.application.common.model.idp.xsd.Property();
        property.setName(IdentityConstants.Authenticator.SAML2SSO.IS_USER_ID_IN_CLAIMS);
        property.setValue("false");
        properties[8] = property;

        property = new org.wso2.carbon.identity.application.common.model.idp.xsd.Property();
        property.setName(IdentityConstants.Authenticator.SAML2SSO.IS_ENABLE_ASSERTION_ENCRYPTION);
        property.setValue("false");
        properties[9] = property;

        property = new org.wso2.carbon.identity.application.common.model.idp.xsd.Property();
        property.setName(IdentityConstants.Authenticator.SAML2SSO.IS_ENABLE_ASSERTION_SIGNING);
        property.setValue("false");
        properties[10] = property;

        property = new org.wso2.carbon.identity.application.common.model.idp.xsd.Property();
        property.setName("commonAuthQueryParams");
        properties[11] = property;

        property = new org.wso2.carbon.identity.application.common.model.idp.xsd.Property();
        property.setName("AttributeConsumingServiceIndex");
        properties[12] = property;

        return properties;
    }

    /**
     * Get SAML SSO configuration properties for XSD.
     *
     * @return
     */
    private org.wso2.carbon.identity.application.common.model.xsd.Property[] getSAMLSSOConfigurationPropertiesForXSD() {

        org.wso2.carbon.identity.application.common.model.xsd.Property[] properties = new org.wso2.carbon.identity.application.common.model.xsd.Property[13];

        org.wso2.carbon.identity.application.common.model.xsd.Property property = new org.wso2.carbon.identity.application.common.model.xsd.Property();
        property.setName(IdentityConstants.Authenticator.SAML2SSO.IDP_ENTITY_ID);
        property.setValue(IDP_NAME);
        properties[0] = property;

        property = new org.wso2.carbon.identity.application.common.model.xsd.Property();
        property.setName(IdentityConstants.Authenticator.SAML2SSO.SP_ENTITY_ID);
        property.setValue(SECONDARY_IS_APPLICATION_NAME);
        properties[1] = property;

        property = new org.wso2.carbon.identity.application.common.model.xsd.Property();
        property.setName(IdentityConstants.Authenticator.SAML2SSO.SSO_URL);
        property.setValue(SECONDARY_IS_SAMLSSO_URL);
        properties[2] = property;

        property = new org.wso2.carbon.identity.application.common.model.xsd.Property();
        property.setName(IdentityConstants.Authenticator.SAML2SSO.IS_AUTHN_REQ_SIGNED);
        property.setValue("false");
        properties[3] = property;

        property = new org.wso2.carbon.identity.application.common.model.xsd.Property();
        property.setName(IdentityConstants.Authenticator.SAML2SSO.IS_LOGOUT_ENABLED);
        property.setValue("true");
        properties[4] = property;

        property = new org.wso2.carbon.identity.application.common.model.xsd.Property();
        property.setName(IdentityConstants.Authenticator.SAML2SSO.LOGOUT_REQ_URL);
        properties[5] = property;

        property = new org.wso2.carbon.identity.application.common.model.xsd.Property();
        property.setName(IdentityConstants.Authenticator.SAML2SSO.IS_LOGOUT_REQ_SIGNED);
        property.setValue("false");
        properties[6] = property;

        property = new org.wso2.carbon.identity.application.common.model.xsd.Property();
        property.setName(IdentityConstants.Authenticator.SAML2SSO.IS_AUTHN_RESP_SIGNED);
        property.setValue("false");
        properties[7] = property;

        property = new org.wso2.carbon.identity.application.common.model.xsd.Property();
        property.setName(IdentityConstants.Authenticator.SAML2SSO.IS_USER_ID_IN_CLAIMS);
        property.setValue("false");
        properties[8] = property;

        property = new org.wso2.carbon.identity.application.common.model.xsd.Property();
        property.setName(IdentityConstants.Authenticator.SAML2SSO.IS_ENABLE_ASSERTION_ENCRYPTION);
        property.setValue("false");
        properties[9] = property;

        property = new org.wso2.carbon.identity.application.common.model.xsd.Property();
        property.setName(IdentityConstants.Authenticator.SAML2SSO.IS_ENABLE_ASSERTION_SIGNING);
        property.setValue("false");
        properties[10] = property;

        property = new org.wso2.carbon.identity.application.common.model.xsd.Property();
        property.setName("commonAuthQueryParams");
        properties[11] = property;

        property = new org.wso2.carbon.identity.application.common.model.xsd.Property();
        property.setName("AttributeConsumingServiceIndex");
        properties[12] = property;

        return properties;
    }

    private org.wso2.carbon.identity.application.common.model.xsd.IdentityProvider getFederatedSAMLSSOIDP() {

        org.wso2.carbon.identity.application.common.model.xsd.IdentityProvider identityProvider = new org.wso2.carbon.identity.application.common.model.xsd.IdentityProvider();
        identityProvider.setIdentityProviderName(IDP_NAME);
        identityProvider.setAlias(IDENTITY_PROVIDER_ALIAS);
        identityProvider.setEnable(true);

        org.wso2.carbon.identity.application.common.model.xsd.FederatedAuthenticatorConfig federatedAuthenticatorConfig = new org.wso2.carbon.identity.application.common.model.xsd.FederatedAuthenticatorConfig();
        federatedAuthenticatorConfig.setProperties(getSAMLSSOConfigurationPropertiesForXSD());
        federatedAuthenticatorConfig.setName("SAMLSSOAuthenticator");
        federatedAuthenticatorConfig.setDisplayName("samlsso");
        federatedAuthenticatorConfig.setEnabled(true);
        identityProvider.setDefaultAuthenticatorConfig(federatedAuthenticatorConfig);
        identityProvider.setFederatedAuthenticatorConfigs(
                new org.wso2.carbon.identity.application.common.model.xsd.FederatedAuthenticatorConfig[] {
                        federatedAuthenticatorConfig });
        return identityProvider;
    }

    private void startSecondaryIS() throws Exception {

        AutomationContext context = testDataHolder.getAutomationContext();
        String serviceUrl = (context.getContextUrls().getSecureServiceUrl())
                .replace("9853", String.valueOf(IS_DEFAULT_HTTPS_PORT + PORT_OFFSET_1)) + "/";

        AuthenticatorClient authenticatorClient = new AuthenticatorClient(serviceUrl);

        sessionCookie = authenticatorClient.login(context.getSuperTenant().getTenantAdmin().getUserName(),
                context.getSuperTenant().getTenantAdmin().getPassword(),
                context.getDefaultInstance().getHosts().get("default"));

        if (sessionCookie != null) {
            ConfigurationContext configContext = ConfigurationContextFactory
                    .createConfigurationContextFromFileSystem(null, null);
            applicationManagementServiceClient2 = new ApplicationManagementServiceClient(sessionCookie, serviceUrl,
                    configContext);
            samlSSOConfigServiceClient = new SAMLSSOConfigServiceClient(serviceUrl, sessionCookie);
        }
    }

    private void createServiceProviderInSecondaryIS() throws Exception {

        ServiceProvider serviceProvider = new ServiceProvider();
        serviceProvider.setApplicationName(SECONDARY_IS_APPLICATION_NAME);
        serviceProvider.setDescription("This is a test Service Provider");
        applicationManagementServiceClient2.createApplication(serviceProvider);

        serviceProvider = applicationManagementServiceClient2.getApplication(SECONDARY_IS_APPLICATION_NAME);

        InboundAuthenticationRequestConfig samlAuthenticationRequestConfig = new InboundAuthenticationRequestConfig();
        samlAuthenticationRequestConfig.setInboundAuthKey(SECONDARY_IS_APPLICATION_NAME);
        samlAuthenticationRequestConfig.setInboundAuthType("samlsso");
        org.wso2.carbon.identity.application.common.model.xsd.Property property = new org.wso2.carbon.identity.application.common.model.xsd.Property();

        samlAuthenticationRequestConfig
                .setProperties(new org.wso2.carbon.identity.application.common.model.xsd.Property[] { property });

        InboundAuthenticationConfig inboundAuthenticationConfig = new InboundAuthenticationConfig();
        inboundAuthenticationConfig.setInboundAuthenticationRequestConfigs(
                new InboundAuthenticationRequestConfig[] { samlAuthenticationRequestConfig });

        serviceProvider.setInboundAuthenticationConfig(inboundAuthenticationConfig);
        applicationManagementServiceClient2.updateApplicationData(serviceProvider);
    }

    private void createSAMLAppInSecondaryIS() throws Exception {

        SAMLSSOServiceProviderDTO samlssoServiceProviderDTO = new SAMLSSOServiceProviderDTO();
        samlssoServiceProviderDTO.setIssuer(SECONDARY_IS_APPLICATION_NAME);
        samlssoServiceProviderDTO.setAssertionConsumerUrls(new String[] { COMMON_AUTH_URL });
        samlssoServiceProviderDTO.setDefaultAssertionConsumerUrl(COMMON_AUTH_URL);
        samlssoServiceProviderDTO.setNameIDFormat(SAML_NAME_ID_FORMAT);
        samlssoServiceProviderDTO.setDoSignAssertions(false);
        samlssoServiceProviderDTO.setDoSignResponse(false);
        samlssoServiceProviderDTO.setDoSingleLogout(true);
        samlssoServiceProviderDTO.setEnableAttributeProfile(false);
        samlssoServiceProviderDTO.setEnableAttributesByDefault(false);

        samlSSOConfigServiceClient.addServiceProvider(samlssoServiceProviderDTO);
    }

    /**
     * Update service provider authentication script config.
     *
     * @param filename File Name of the authentication script.
     * @throws Exception
     */
    private void updateAuthScript(String filename) throws Exception {

        LocalAndOutboundAuthenticationConfig outboundAuthConfig = createLocalAndOutboundAuthenticationConfig();
        outboundAuthConfig.setEnableAuthorization(true);

        String script = getConditionalAuthScript(filename);
        AuthenticationScriptConfig config = new AuthenticationScriptConfig();
        config.setContent(script);
        config.setEnabled(true);
        outboundAuthConfig.setAuthenticationScriptConfig(config);
        serviceProvider.setLocalAndOutBoundAuthenticationConfig(outboundAuthConfig);
        applicationManagementServiceClient.updateApplicationData(serviceProvider);
    }

    protected LocalAndOutboundAuthenticationConfig createLocalAndOutboundAuthenticationConfig() throws Exception {

        LocalAndOutboundAuthenticationConfig localAndOutboundAuthenticationConfig = super
                .createLocalAndOutboundAuthenticationConfig();

        AuthenticationStep authenticationStep2 = new AuthenticationStep();
        authenticationStep2.setStepOrder(2);
        authenticationStep2.setSubjectStep(false);
        authenticationStep2.setAttributeStep(false);

        authenticationStep2.setFederatedIdentityProviders(new org.wso2.carbon.identity.application.common.model.xsd
                .IdentityProvider[]{getFederatedSAMLSSOIDP()});
        localAndOutboundAuthenticationConfig.addAuthenticationSteps(authenticationStep2);

        return localAndOutboundAuthenticationConfig;
    }
}

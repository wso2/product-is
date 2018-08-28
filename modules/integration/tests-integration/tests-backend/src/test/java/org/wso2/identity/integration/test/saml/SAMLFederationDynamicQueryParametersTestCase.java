/*
 * Copyright (c) 2018, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
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

package org.wso2.identity.integration.test.saml;

import org.apache.catalina.core.StandardHost;
import org.apache.catalina.startup.Tomcat;
import org.apache.commons.lang.StringUtils;
import org.apache.http.HttpResponse;
import org.apache.http.NameValuePair;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.utils.URLEncodedUtils;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClientBuilder;
import org.apache.http.util.EntityUtils;
import org.testng.Assert;
import org.testng.annotations.AfterClass;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;
import org.wso2.carbon.identity.application.common.model.idp.xsd.FederatedAuthenticatorConfig;
import org.wso2.carbon.identity.application.common.model.idp.xsd.IdentityProvider;
import org.wso2.carbon.identity.application.common.model.idp.xsd.Property;
import org.wso2.carbon.identity.application.common.model.xsd.AuthenticationStep;
import org.wso2.carbon.identity.application.common.model.xsd.InboundAuthenticationConfig;
import org.wso2.carbon.identity.application.common.model.xsd.InboundAuthenticationRequestConfig;
import org.wso2.carbon.identity.application.common.model.xsd.ServiceProvider;
import org.wso2.carbon.identity.sso.saml.stub.types.SAMLSSOServiceProviderDTO;
import org.wso2.identity.integration.common.clients.Idp.IdentityProviderMgtServiceClient;
import org.wso2.identity.integration.common.clients.application.mgt.ApplicationManagementServiceClient;
import org.wso2.identity.integration.common.clients.sso.saml.SAMLSSOConfigServiceClient;
import org.wso2.identity.integration.common.utils.ISIntegrationTest;
import org.wso2.identity.integration.test.application.mgt.AbstractIdentityFederationTestCase;
import org.wso2.identity.integration.test.util.Utils;
import org.wso2.identity.integration.test.utils.IdentityConstants;

import java.io.File;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.util.List;

/**
 * Integration test for Dynamic Query parameter support for SAML Federated Authenticator.
 */
public class SAMLFederationDynamicQueryParametersTestCase extends AbstractIdentityFederationTestCase {

    private static final String IDENTITY_PROVIDER_NAME = "testIdp";
    public static final String SERVICE_PROVIDER = "SERVICE_PROVIDER";
    public static final String INBOUND_AUTH_KEY = "travelocity.com";
    public static final String INBOUND_AUTH_TYPE = "samlsso";
    private ApplicationManagementServiceClient appMgtclient;
    private IdentityProviderMgtServiceClient idpMgtClient;
    private SAMLSSOConfigServiceClient ssoConfigServiceClient;

    private static final String INBOUND_QUERY_PARAM = "inbound_request_param_key";
    private static final String INBOUND_QUERY_PARAM_VALUE = "inbound_request_param_value";

    private static final String DYNAMIC_QUERY_PARAM_KEY = "dynamic_query";
    private static final String DYNAMIC_QUERY = "dynamic_query={inbound_request_param_key}";
    private static final String FEDERATED_AUTHENTICATION_TYPE = "federated";
    private static final String TRAVELOCITY_SAMPLE_APP_URL = "http://localhost:8490/travelocity.com";
    private Tomcat tomcat;

    @BeforeClass(alwaysRun = true)
    public void initTest() throws Exception {

        super.initTest();

        String userName = userInfo.getUserName();
        String password = userInfo.getPassword();

        appMgtclient = new ApplicationManagementServiceClient(sessionCookie, backendURL, null);
        idpMgtClient = new IdentityProviderMgtServiceClient(userName, password, backendURL);
        ssoConfigServiceClient = new SAMLSSOConfigServiceClient(backendURL, userName, password);

        tomcat = startTomcatAndDeployTravelocityApp();
    }

    private Tomcat startTomcatAndDeployTravelocityApp() {

        Tomcat tomcat = null;
        try {
            tomcat = getTomcat();
            URL resourceUrl = getClass()
                    .getResource(ISIntegrationTest.URL_SEPARATOR + "samples" + ISIntegrationTest.URL_SEPARATOR + "travelocity.com.war");
            tomcat.addWebapp(tomcat.getHost(), "/travelocity.com", resourceUrl.getPath());
            tomcat.start();
        } catch (Exception e) {
            Assert.fail("travelocity.com application deployment failed.", e);
        }
        return tomcat;
    }

    private Tomcat getTomcat() {

        Tomcat tomcat = new Tomcat();
        tomcat.getService().setContainer(tomcat.getEngine());
        tomcat.setPort(8490);
        tomcat.setBaseDir("");

        StandardHost stdHost = (StandardHost) tomcat.getHost();

        stdHost.setAppBase("");
        stdHost.setAutoDeploy(true);
        stdHost.setDeployOnStartup(true);
        stdHost.setUnpackWARs(true);
        tomcat.setHost(stdHost);
        return tomcat;
    }

    @AfterClass(alwaysRun = true)
    public void endTest() throws Exception {

        appMgtclient.deleteApplication(SERVICE_PROVIDER);
        idpMgtClient.deleteIdP(IDENTITY_PROVIDER_NAME);

        appMgtclient = null;
        idpMgtClient = null;

        if (tomcat != null) {
            tomcat.stop();
            tomcat.destroy();
            Thread.sleep(10000);
        }
    }

    @Test(groups = "wso2.is", description = "Test federated IDP creation with SAML Federated Authenticator")
    public void testIdpWithDynamicQueryParams() throws Exception {

        IdentityProvider identityProvider = new IdentityProvider();
        identityProvider.setIdentityProviderName(IDENTITY_PROVIDER_NAME);

        FederatedAuthenticatorConfig saml2SSOAuthnConfig = new FederatedAuthenticatorConfig();
        saml2SSOAuthnConfig.setName("SAMLSSOAuthenticator");
        saml2SSOAuthnConfig.setDisplayName("samlsso");
        saml2SSOAuthnConfig.setEnabled(true);
        saml2SSOAuthnConfig.setProperties(getSAML2SSOAuthnConfigProperties());
        identityProvider.setDefaultAuthenticatorConfig(saml2SSOAuthnConfig);
        identityProvider.setFederatedAuthenticatorConfigs(new FederatedAuthenticatorConfig[]{saml2SSOAuthnConfig});

        idpMgtClient.addIdP(identityProvider);

        IdentityProvider idPByName = idpMgtClient.getIdPByName(IDENTITY_PROVIDER_NAME);
        Assert.assertNotNull(idPByName);
    }

    @Test(groups = "wso2.is", description = "Test Service Provider creation with SAML Federated IDP Authentication",
            dependsOnMethods = {"testIdpWithDynamicQueryParams"})
    public void testCreateServiceProviderWithSAMLConfigsAndSAMLFedIdp() throws Exception {

        ServiceProvider serviceProvider = new ServiceProvider();
        serviceProvider.setApplicationName(SERVICE_PROVIDER);
        appMgtclient.createApplication(serviceProvider);

        serviceProvider = appMgtclient.getApplication(SERVICE_PROVIDER);
        Assert.assertNotNull(serviceProvider, "Service Provider creation has failed.");

        // Set SAML Inbound for the service provider.
        ssoConfigServiceClient.addServiceProvider(createSsoServiceProviderDTOForTravelocityApp());
        InboundAuthenticationConfig inboundAuthenticationConfig = new InboundAuthenticationConfig();
        InboundAuthenticationRequestConfig requestConfig = new InboundAuthenticationRequestConfig();
        requestConfig.setInboundAuthKey(INBOUND_AUTH_KEY);
        requestConfig.setInboundAuthType(INBOUND_AUTH_TYPE);

        org.wso2.carbon.identity.application.common.model.xsd.Property attributeConsumerServiceIndexProp =
                new org.wso2.carbon.identity.application.common.model.xsd.Property();
        attributeConsumerServiceIndexProp.setName("attrConsumServiceIndex");
        attributeConsumerServiceIndexProp.setValue("1239245949");
        requestConfig.setProperties(new org.wso2.carbon.identity.application.common.model.xsd.Property[]{
                attributeConsumerServiceIndexProp});
        inboundAuthenticationConfig
                .setInboundAuthenticationRequestConfigs(new InboundAuthenticationRequestConfig[]{requestConfig});
        serviceProvider.setInboundAuthenticationConfig(inboundAuthenticationConfig);

        // Add SAML IDP as authentication step.
        AuthenticationStep authStep = new AuthenticationStep();
        org.wso2.carbon.identity.application.common.model.xsd.IdentityProvider idP =
                new org.wso2.carbon.identity.application.common.model.xsd.IdentityProvider();
        idP.setIdentityProviderName(IDENTITY_PROVIDER_NAME);
        org.wso2.carbon.identity.application.common.model.xsd.FederatedAuthenticatorConfig saml2SSOAuthnConfig = new org.wso2.carbon.identity.application.common.model.xsd.FederatedAuthenticatorConfig();
        saml2SSOAuthnConfig.setName("SAMLSSOAuthenticator");
        saml2SSOAuthnConfig.setDisplayName("samlsso");
        idP.setFederatedAuthenticatorConfigs(new org.wso2.carbon.identity.application.common.model.xsd.FederatedAuthenticatorConfig[]{saml2SSOAuthnConfig});
        authStep.setFederatedIdentityProviders(
                new org.wso2.carbon.identity.application.common.model.xsd.IdentityProvider[]{idP});
        serviceProvider.getLocalAndOutBoundAuthenticationConfig().setAuthenticationSteps(
                new AuthenticationStep[]{authStep});
        serviceProvider.getLocalAndOutBoundAuthenticationConfig().setAuthenticationType(FEDERATED_AUTHENTICATION_TYPE);

        appMgtclient.updateApplicationData(serviceProvider);
        serviceProvider = appMgtclient.getApplication(SERVICE_PROVIDER);

        Assert.assertNotNull(serviceProvider);

        Assert.assertNotNull(serviceProvider.getInboundAuthenticationConfig());
        InboundAuthenticationRequestConfig[] inboundAuthenticationRequestConfigs =
                serviceProvider.getInboundAuthenticationConfig().getInboundAuthenticationRequestConfigs();
        Assert.assertNotNull(inboundAuthenticationRequestConfigs);

        boolean inboundAuthUpdateSuccess = false;
        for (InboundAuthenticationRequestConfig config : inboundAuthenticationRequestConfigs) {
            if (INBOUND_AUTH_KEY.equals(config.getInboundAuthKey())
                    && INBOUND_AUTH_TYPE.equals(config.getInboundAuthType())) {
                inboundAuthUpdateSuccess = true;
                break;
            }
        }
        Assert.assertTrue(inboundAuthUpdateSuccess, "Failed to update service provider with SAML inbound configs.");

        Assert.assertNotNull(serviceProvider.getLocalAndOutBoundAuthenticationConfig());
        Assert.assertEquals(serviceProvider.getLocalAndOutBoundAuthenticationConfig().getAuthenticationType(),
                FEDERATED_AUTHENTICATION_TYPE);
    }

    @Test(alwaysRun = true, description = "Test SAML Federation Request with Dynamic Query Parameters",
            dependsOnMethods = {"testCreateServiceProviderWithSAMLConfigsAndSAMLFedIdp"})
    public void testSAMLRedirectBindingDynamicWithInboundQueryParam() throws Exception {

        HttpGet request = new HttpGet(TRAVELOCITY_SAMPLE_APP_URL + "/samlsso?SAML2.HTTPBinding=HTTP-Redirect");
        CloseableHttpClient client = null;
        try {
            client = HttpClientBuilder.create().disableRedirectHandling().build();
            // Do a redirect to travelocity app.
            HttpResponse response = client.execute(request);
            EntityUtils.consume(response.getEntity());

            // Modify the location header to included the secToken.
            String location = Utils.getRedirectUrl(response) + "&" + INBOUND_QUERY_PARAM + "=" + INBOUND_QUERY_PARAM_VALUE;

            // Do a GET manually to send the SAML Request to IS.
            HttpGet requestToIS = new HttpGet(location);
            HttpResponse requestToFederatedIdp = client.execute(requestToIS);
            EntityUtils.consume(requestToFederatedIdp.getEntity());

            // 302 to SAML Federated IDP initiated from the primary IS
            String requestToFedIdpLocationHeader = Utils.getRedirectUrl(requestToFederatedIdp);
            // Assert whether the query param value sent in the inbound request was passed in the 302 to Federated IDP
            List<NameValuePair> nameValuePairs = buildQueryParamList(requestToFedIdpLocationHeader);
            boolean isDynamicQueryParamReplaced = false;
            for (NameValuePair valuePair : nameValuePairs) {
                if (StringUtils.equalsIgnoreCase(DYNAMIC_QUERY_PARAM_KEY, valuePair.getName())) {
                    // Check whether the query param value sent in inbound request was included to the additional query
                    // params defined in the SAML Application Authenticator.
                    isDynamicQueryParamReplaced = StringUtils.equals(valuePair.getValue(), INBOUND_QUERY_PARAM_VALUE);
                }
            }
            Assert.assertTrue(isDynamicQueryParamReplaced);
        } finally {
            if (client != null) {
                client.close();
            }
        }
    }

    @Test(alwaysRun = true, description = "Test SAML Federation Request with Dynamic Query Parameters",
            dependsOnMethods = {"testCreateServiceProviderWithSAMLConfigsAndSAMLFedIdp"})
    public void testSAMLRedirectBindingDynamicWithoutInboundQueryParam() throws Exception {

        HttpGet request = new HttpGet(TRAVELOCITY_SAMPLE_APP_URL + "/samlsso?SAML2.HTTPBinding=HTTP-Redirect");
        CloseableHttpClient client = null;
        try {
            client = HttpClientBuilder.create().disableRedirectHandling().build();
            // Do a redirect to travelocity app.
            HttpResponse response = client.execute(request);
            EntityUtils.consume(response.getEntity());

            // Modify the location header to included the secToken.
            String location = Utils.getRedirectUrl(response);

            // Do a GET manually to send the SAML Request to IS.
            HttpGet requestToIS = new HttpGet(location);
            HttpResponse requestToFederatedIdp = client.execute(requestToIS);
            EntityUtils.consume(requestToFederatedIdp.getEntity());

            // 302 to SAML Federated IDP initiated from the primary IS
            String requestToFedIdpLocationHeader = Utils.getRedirectUrl(requestToFederatedIdp);
            // Assert whether the query param value sent in the inbound request was passed in the 302 to Federated IDP
            List<NameValuePair> nameValuePairs = buildQueryParamList(requestToFedIdpLocationHeader);
            boolean isDynamicQuerySentInFedAuthRequestEmpty = false;
            for (NameValuePair valuePair : nameValuePairs) {
                if (StringUtils.equalsIgnoreCase(DYNAMIC_QUERY_PARAM_KEY, valuePair.getName())) {
                    // Check whether the query param value sent in inbound request was included to the additional query
                    // params defined in the SAML Application Authenticator.
                    isDynamicQuerySentInFedAuthRequestEmpty = StringUtils.isEmpty(valuePair.getValue());
                }
            }

            Assert.assertTrue(isDynamicQuerySentInFedAuthRequestEmpty);
        } finally {
            if (client != null) {
                client.close();
            }
        }
    }

    private List<NameValuePair> buildQueryParamList(String requestToFedIdpLocationHeader) {

        return URLEncodedUtils.parse(requestToFedIdpLocationHeader, StandardCharsets.UTF_8);
    }

    private Property[] getSAML2SSOAuthnConfigProperties() {

        Property[] properties = new Property[4];
        Property property = new Property();
        property.setName(IdentityConstants.Authenticator.SAML2SSO.IDP_ENTITY_ID);
        property.setValue("samlFedIdP");
        properties[0] = property;

        property = new Property();
        property.setName(IdentityConstants.Authenticator.SAML2SSO.SP_ENTITY_ID);
        property.setValue("samlFedSP");
        properties[1] = property;

        property = new Property();
        property.setName(IdentityConstants.Authenticator.SAML2SSO.SSO_URL);
        property.setValue("https://localhost:9453/samlsso");
        properties[2] = property;

        property = new Property();
        property.setName("commonAuthQueryParams");
        property.setValue(DYNAMIC_QUERY);
        properties[3] = property;

        return properties;
    }

    private SAMLSSOServiceProviderDTO createSsoServiceProviderDTOForTravelocityApp() {

        SAMLSSOServiceProviderDTO samlssoServiceProviderDTO = new SAMLSSOServiceProviderDTO();
        samlssoServiceProviderDTO.setIssuer(INBOUND_AUTH_KEY);
        samlssoServiceProviderDTO.setAssertionConsumerUrls(new String[]{TRAVELOCITY_SAMPLE_APP_URL + "/home" +
                ".jsp"});
        samlssoServiceProviderDTO.setDefaultAssertionConsumerUrl(TRAVELOCITY_SAMPLE_APP_URL + "/home.jsp");
        samlssoServiceProviderDTO.setAttributeConsumingServiceIndex("1239245949");
        samlssoServiceProviderDTO.setNameIDFormat("urn:oasis:names:tc:SAML:1.1:nameid-format:emailAddress");
        samlssoServiceProviderDTO.setDoSignAssertions(true);
        samlssoServiceProviderDTO.setDoSignResponse(true);
        samlssoServiceProviderDTO.setDoSingleLogout(true);
        samlssoServiceProviderDTO.setLoginPageURL("/carbon/admin/login.jsp");
        return samlssoServiceProviderDTO;
    }
}

/*
 * Copyright (c) 2015, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
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
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */

package org.wso2.identity.integration.test.saml;

import org.apache.http.HttpResponse;
import org.apache.http.NameValuePair;
import org.apache.http.client.HttpClient;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.message.BasicNameValuePair;
import org.testng.Assert;
import org.testng.annotations.AfterClass;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;
import org.wso2.carbon.automation.engine.context.AutomationContext;
import org.wso2.carbon.automation.engine.context.TestUserMode;
import org.wso2.carbon.automation.engine.frameworkutils.FrameworkPathUtil;
import org.wso2.carbon.identity.application.common.model.idp.xsd.FederatedAuthenticatorConfig;
import org.wso2.carbon.identity.application.common.model.idp.xsd.IdentityProvider;
import org.wso2.carbon.identity.application.common.model.idp.xsd.Property;
import org.wso2.carbon.identity.application.common.model.xsd.AuthenticationStep;
import org.wso2.carbon.identity.application.common.model.xsd.InboundAuthenticationRequestConfig;
import org.wso2.carbon.identity.application.common.model.xsd.ServiceProvider;
import org.wso2.carbon.identity.sso.saml.stub.types.SAMLSSOServiceProviderDTO;
import org.wso2.carbon.integration.common.utils.mgt.ServerConfigurationManager;
import org.wso2.identity.integration.test.application.mgt.AbstractIdentityFederationTestCase;
import org.wso2.identity.integration.test.util.Utils;
import org.wso2.identity.integration.test.utils.CommonConstants;
import org.wso2.identity.integration.test.utils.IdentityConstants;

import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.URL;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class ChangeACSUrlTestCase extends AbstractIdentityFederationTestCase {

    private static final String PRIMARY_IS_SERVICE_PROVIDER_NAME = "travelocity-changeACS";
    private static final String SECONDARY_IS_SERVICE_PROVIDER_NAME = "secondaryChangeACSSP";
    private static final String IDENTITY_PROVIDER_NAME = "ChangeACSIdP";
    private static final String PRIMARY_IS_SAML_ISSUER_NAME = "travelocity.com";
    private static final String PRIMARY_IS_SAML_ACS_URL = "http://localhost:8490/travelocity.com/home.jsp";
    private static final String SECONDARY_IS_SAML_ISSUER_NAME = "samlChangeACSSP";
    private static final String SAML_NAME_ID_FORMAT = "urn:oasis:names:tc:SAML:1.1:nameid-format:emailAddress";
    private static final String SAML_SSO_URL = "http://localhost:8490/travelocity.com/samlsso?SAML2" +
            ".HTTPBinding=HTTP-Redirect";
    private static final String USER_AGENT = "Apache-HttpClient/4.2.5 (java 1.5)";
    private static final String AUTHENTICATION_TYPE = "federated";
    private static final String INBOUND_AUTH_TYPE = "samlsso";
    private static final int TOMCAT_8490 = 8490;
    private static final int PORT_OFFSET_0 = 0;
    private static final int PORT_OFFSET_1 = 1;
    private String COMMON_AUTH_URL = "https://localhost:%s/commonauth";
    private String COMMON_AUTH_URL_CHANGED = "https://localhost:%s/commonauth1";

    private String usrName = "admin";
    private String usrPwd = "admin";

    private File applicationAuthenticationXml;
    private ServerConfigurationManager serverConfigurationManager;

    @BeforeClass(alwaysRun = true)
    public void initTest() throws Exception {

        super.initTest();
        applicationAuthenticationXml = new File(Utils.getResidentCarbonHome() + File.separator
                + "repository" + File.separator + "conf" + File.separator
                + "identity" + File.separator + "application-authentication.xml");
        File applicationAuthenticationXmlToCopy = new File(FrameworkPathUtil.getSystemResourceLocation() +
                "artifacts" + File.separator + "IS" + File.separator + "saml" + File.separator
                + "application-authentication-changed-acs.xml");

        serverConfigurationManager = new ServerConfigurationManager(isServer);
        serverConfigurationManager.applyConfigurationWithoutRestart(applicationAuthenticationXmlToCopy,
                applicationAuthenticationXml, false);

        serverConfigurationManager.restartGracefully();
        Thread.sleep(20000);

        super.initTest();

        Map<String, String> startupParameters = new HashMap<String, String>();
        startupParameters.put("-DportOffset", String.valueOf(PORT_OFFSET_1 + CommonConstants.IS_DEFAULT_OFFSET));
        AutomationContext context = new AutomationContext("IDENTITY", "identity002", TestUserMode.SUPER_TENANT_ADMIN);

        startCarbonServer(PORT_OFFSET_1, context, startupParameters);

        super.startTomcat(TOMCAT_8490);

        URL resourceUrl = getClass().getResource(File.separator + "samples" + File.separator + "travelocity.com.war");
        super.addWebAppToTomcat(TOMCAT_8490, "/travelocity.com", resourceUrl.getPath());


        super.createServiceClients(PORT_OFFSET_0, sessionCookie, new IdentityConstants
                .ServiceClientType[]{IdentityConstants.ServiceClientType.APPLICATION_MANAGEMENT, IdentityConstants.ServiceClientType.IDENTITY_PROVIDER_MGT, IdentityConstants.ServiceClientType.SAML_SSO_CONFIG});
        super.createServiceClients(PORT_OFFSET_1, null, new IdentityConstants.ServiceClientType[]{IdentityConstants.ServiceClientType.APPLICATION_MANAGEMENT, IdentityConstants.ServiceClientType.SAML_SSO_CONFIG});

        //create identity provider in primary IS
        IdentityProvider identityProvider = new IdentityProvider();
        identityProvider.setIdentityProviderName(IDENTITY_PROVIDER_NAME);

        FederatedAuthenticatorConfig saml2SSOAuthnConfig = new FederatedAuthenticatorConfig();
        saml2SSOAuthnConfig.setName("SAMLSSOAuthenticator");
        saml2SSOAuthnConfig.setDisplayName("samlsso");
        saml2SSOAuthnConfig.setEnabled(true);
        saml2SSOAuthnConfig.setProperties(getSAML2SSOAuthnConfigProperties());
        identityProvider.setDefaultAuthenticatorConfig(saml2SSOAuthnConfig);
        identityProvider.setFederatedAuthenticatorConfigs(new FederatedAuthenticatorConfig[]{saml2SSOAuthnConfig});

        super.addIdentityProvider(PORT_OFFSET_0, identityProvider);

        //create service provider in primary IS
        super.addServiceProvider(PORT_OFFSET_0, PRIMARY_IS_SERVICE_PROVIDER_NAME);

        ServiceProvider serviceProvider = getServiceProvider(PORT_OFFSET_0, PRIMARY_IS_SERVICE_PROVIDER_NAME);

        updateServiceProviderWithSAMLConfigs(PORT_OFFSET_0, PRIMARY_IS_SAML_ISSUER_NAME, PRIMARY_IS_SAML_ACS_URL, serviceProvider);

        AuthenticationStep authStep = new AuthenticationStep();
        org.wso2.carbon.identity.application.common.model.xsd.IdentityProvider idP = new org.wso2.carbon.identity.application.common.model.xsd.IdentityProvider();
        idP.setIdentityProviderName(IDENTITY_PROVIDER_NAME);
        authStep.setFederatedIdentityProviders(new org.wso2.carbon.identity.application.common.model.xsd.IdentityProvider[]{idP});
        serviceProvider.getLocalAndOutBoundAuthenticationConfig().setAuthenticationSteps(new AuthenticationStep[]{authStep});
        serviceProvider.getLocalAndOutBoundAuthenticationConfig().setAuthenticationType(AUTHENTICATION_TYPE);

        updateServiceProvider(PORT_OFFSET_0, serviceProvider);

        //create service provider in secondary IS
        super.addServiceProvider(PORT_OFFSET_1, SECONDARY_IS_SERVICE_PROVIDER_NAME);

        serviceProvider = getServiceProvider(PORT_OFFSET_1, SECONDARY_IS_SERVICE_PROVIDER_NAME);

        updateServiceProviderWithSAMLConfigs(PORT_OFFSET_1, SECONDARY_IS_SAML_ISSUER_NAME, String.format
                (COMMON_AUTH_URL_CHANGED, DEFAULT_PORT + PORT_OFFSET_0), serviceProvider);

        updateServiceProvider(PORT_OFFSET_1, serviceProvider);
    }

    @Test(priority = 1, groups = "wso2.is", description = "Check SAML To SAML fedaration flow")
    public void testChangeACSUrl() throws Exception {

        HttpClient client = getHttpClient();

        String sessionId = sendSAMLRequestToPrimaryIS(client);
        Assert.assertNotNull(sessionId, "Unable to acquire 'sessionDataKey' value");

        String redirectURL = authenticateWithSecondaryIS(client, sessionId);
        Assert.assertNotNull(redirectURL, "Unable to acquire redirect url after login to secondary IS");

        Map<String, String> responseParameters = getSAMLResponseFromSecondaryIS(client, redirectURL);
        Assert.assertNotNull(responseParameters.get("SAMLResponse"), "Unable to acquire 'SAMLResponse' value");
        Assert.assertNotNull(responseParameters.get("RelayState"), "Unable to acquire 'RelayState' value");

        redirectURL = sendSAMLResponseToPrimaryIS(client, responseParameters);
        Assert.assertNotNull(redirectURL, "Unable to acquire redirect url after sending SAML response to primary IS");

        String samlResponse = getSAMLResponseFromPrimaryIS(client, redirectURL);
        Assert.assertNotNull(samlResponse, "Unable to acquire SAML response from primary IS");

        boolean validResponse = sendSAMLResponseToWebApp(client, samlResponse);
        Assert.assertTrue(validResponse, "Invalid SAML response received by travelocity app");
    }

    @AfterClass(alwaysRun = true)
    public void endTest() throws Exception {

        super.deleteSAML2WebSSOConfiguration(PORT_OFFSET_0, PRIMARY_IS_SAML_ISSUER_NAME);
        super.deleteServiceProvider(PORT_OFFSET_0, PRIMARY_IS_SERVICE_PROVIDER_NAME);
        super.deleteIdentityProvider(PORT_OFFSET_0, IDENTITY_PROVIDER_NAME);

        super.deleteSAML2WebSSOConfiguration(PORT_OFFSET_1, SECONDARY_IS_SAML_ISSUER_NAME);
        super.deleteServiceProvider(PORT_OFFSET_1, SECONDARY_IS_SERVICE_PROVIDER_NAME);

        super.stopCarbonServer(PORT_OFFSET_1);
        super.stopTomcat(TOMCAT_8490);

        super.stopHttpClient();

        File applicationAuthenticationXmlToCopy = new File(FrameworkPathUtil.getSystemResourceLocation() +
                "artifacts" + File.separator + "IS" + File.separator + "saml" + File.separator +
                "application-authentication-default.xml");


        serverConfigurationManager.applyConfigurationWithoutRestart(applicationAuthenticationXmlToCopy,
                applicationAuthenticationXml, false);

        serverConfigurationManager.restartGracefully();

    }

    private String sendSAMLRequestToPrimaryIS(HttpClient client) throws Exception {

        HttpGet request = new HttpGet(SAML_SSO_URL);
        request.addHeader("User-Agent", USER_AGENT);
        HttpResponse response = client.execute(request);

        return extractValueFromResponse(response, "name=\"sessionDataKey\"", 1);
    }

    private String authenticateWithSecondaryIS(HttpClient client, String sessionId)
            throws Exception {

        HttpPost request = new HttpPost(String.format(COMMON_AUTH_URL, DEFAULT_PORT + PORT_OFFSET_1));
        request.addHeader("User-Agent", USER_AGENT);
        request.addHeader("Referer", PRIMARY_IS_SAML_ACS_URL);

        List<NameValuePair> urlParameters = new ArrayList<NameValuePair>();
        urlParameters.add(new BasicNameValuePair("username", usrName));
        urlParameters.add(new BasicNameValuePair("password", usrPwd));
        urlParameters.add(new BasicNameValuePair("sessionDataKey", sessionId));
        request.setEntity(new UrlEncodedFormEntity(urlParameters));

        HttpResponse response = client.execute(request);
        closeHttpConnection(response);
        return getHeaderValue(response, "Location");
    }

    private Map<String, String> getSAMLResponseFromSecondaryIS(HttpClient client, String redirectURL) throws Exception {

        HttpPost request = new HttpPost(redirectURL);
        request.addHeader("User-Agent", USER_AGENT);
        request.addHeader("Referer", PRIMARY_IS_SAML_ACS_URL);
        HttpResponse response = client.execute(request);

        Map<String, Integer> searchParams = new HashMap<String, Integer>();
        searchParams.put("SAMLResponse", 5);
        searchParams.put("RelayState", 5);
        return extractValuesFromResponse(response, searchParams);
    }

    private String sendSAMLResponseToPrimaryIS(HttpClient client, Map<String, String> searchResults) throws Exception {

        HttpPost request = new HttpPost(String.format(COMMON_AUTH_URL, DEFAULT_PORT + PORT_OFFSET_0));
        request.setHeader("User-Agent", USER_AGENT);

        List<NameValuePair> urlParameters = new ArrayList<NameValuePair>();
        urlParameters.add(new BasicNameValuePair("SAMLResponse", searchResults.get("SAMLResponse")));
        urlParameters.add(new BasicNameValuePair("RelayState", searchResults.get("RelayState")));
        request.setEntity(new UrlEncodedFormEntity(urlParameters));

        HttpResponse response = new DefaultHttpClient().execute(request);
        closeHttpConnection(response);
        return getHeaderValue(response, "Location");
    }

    private String getSAMLResponseFromPrimaryIS(HttpClient client, String redirectURL)
            throws Exception {

        HttpGet request = new HttpGet(redirectURL);
        request.addHeader("User-Agent", USER_AGENT);
        HttpResponse response = client.execute(request);

        return extractValueFromResponse(response, "SAMLResponse", 5);
    }

    private boolean sendSAMLResponseToWebApp(HttpClient client, String samlResponse)
            throws Exception {

        HttpPost request = new HttpPost(PRIMARY_IS_SAML_ACS_URL);
        request.setHeader("User-Agent", USER_AGENT);
        List<NameValuePair> urlParameters = new ArrayList<NameValuePair>();
        urlParameters.add(new BasicNameValuePair("SAMLResponse", samlResponse));
        request.setEntity(new UrlEncodedFormEntity(urlParameters));
        HttpResponse response = new DefaultHttpClient().execute(request);

        return validateSAMLResponse(response, usrName);
    }

    private void updateServiceProviderWithSAMLConfigs(int portOffset, String issuerName,
                                                      String acsUrl,
                                                      ServiceProvider serviceProvider)
            throws Exception {

        String attributeConsumingServiceIndex = super.createSAML2WebSSOConfiguration(portOffset, getSAMLSSOServiceProviderDTO(issuerName, acsUrl));
        Assert.assertNotNull(attributeConsumingServiceIndex, "Failed to create SAML2 Web SSO configuration for issuer '" + issuerName + "'");

        InboundAuthenticationRequestConfig samlAuthenticationRequestConfig = new InboundAuthenticationRequestConfig();
        samlAuthenticationRequestConfig.setInboundAuthKey(issuerName);
        samlAuthenticationRequestConfig.setInboundAuthType(INBOUND_AUTH_TYPE);
        org.wso2.carbon.identity.application.common.model.xsd.Property property = new org.wso2.carbon.identity.application.common.model.xsd.Property();
        property.setName("attrConsumServiceIndex");
        property.setValue(attributeConsumingServiceIndex);
        samlAuthenticationRequestConfig.setProperties(new org.wso2.carbon.identity.application.common.model.xsd.Property[]{property});

        serviceProvider.getInboundAuthenticationConfig().setInboundAuthenticationRequestConfigs(new InboundAuthenticationRequestConfig[]{samlAuthenticationRequestConfig});
    }

    private SAMLSSOServiceProviderDTO getSAMLSSOServiceProviderDTO(String issuerName,
                                                                   String acsUrl) {

        SAMLSSOServiceProviderDTO samlssoServiceProviderDTO = new SAMLSSOServiceProviderDTO();
        samlssoServiceProviderDTO.setIssuer(issuerName);
        samlssoServiceProviderDTO.setAssertionConsumerUrls(new String[]{acsUrl});
        samlssoServiceProviderDTO.setDefaultAssertionConsumerUrl(acsUrl);
        samlssoServiceProviderDTO.setNameIDFormat(SAML_NAME_ID_FORMAT);
        samlssoServiceProviderDTO.setDoSignAssertions(true);
        samlssoServiceProviderDTO.setDoSignResponse(true);
        samlssoServiceProviderDTO.setDoSingleLogout(true);
        samlssoServiceProviderDTO.setEnableAttributeProfile(true);
        samlssoServiceProviderDTO.setEnableAttributesByDefault(true);
        return samlssoServiceProviderDTO;
    }

    private Property[] getSAML2SSOAuthnConfigProperties() {

        Property[] properties = new Property[13];
        Property property = new Property();
        property.setName(IdentityConstants.Authenticator.SAML2SSO.IDP_ENTITY_ID);
        property.setValue("samlChangeACSIdP");
        properties[0] = property;

        property = new Property();
        property.setName(IdentityConstants.Authenticator.SAML2SSO.SP_ENTITY_ID);
        property.setValue("samlChangeACSSP");
        properties[1] = property;

        property = new Property();
        property.setName(IdentityConstants.Authenticator.SAML2SSO.SSO_URL);
        property.setValue("https://localhost:9854/samlsso");
        properties[2] = property;

        property = new Property();
        property.setName(IdentityConstants.Authenticator.SAML2SSO.IS_AUTHN_REQ_SIGNED);
        property.setValue("false");
        properties[3] = property;

        property = new Property();
        property.setName(IdentityConstants.Authenticator.SAML2SSO.IS_LOGOUT_ENABLED);
        property.setValue("true");
        properties[4] = property;

        property = new Property();
        property.setName(IdentityConstants.Authenticator.SAML2SSO.LOGOUT_REQ_URL);
        properties[5] = property;

        property = new Property();
        property.setName(IdentityConstants.Authenticator.SAML2SSO.IS_LOGOUT_REQ_SIGNED);
        property.setValue("false");
        properties[6] = property;

        property = new Property();
        property.setName(IdentityConstants.Authenticator.SAML2SSO.IS_AUTHN_RESP_SIGNED);
        property.setValue("false");
        properties[7] = property;

        property = new Property();
        property.setName(IdentityConstants.Authenticator.SAML2SSO.IS_USER_ID_IN_CLAIMS);
        property.setValue("false");
        properties[8] = property;

        property = new Property();
        property.setName(IdentityConstants.Authenticator.SAML2SSO.IS_ENABLE_ASSERTION_ENCRYPTION);
        property.setValue("false");
        properties[9] = property;

        property = new Property();
        property.setName(IdentityConstants.Authenticator.SAML2SSO.IS_ENABLE_ASSERTION_SIGNING);
        property.setValue("false");
        properties[10] = property;

        property = new Property();
        property.setName("commonAuthQueryParams");
        properties[11] = property;

        property = new Property();
        property.setName("AttributeConsumingServiceIndex");
        properties[12] = property;

        return properties;
    }

    public boolean validateSAMLResponse(HttpResponse response, String userName) throws IOException {
        BufferedReader bufferedReader = new BufferedReader(new InputStreamReader(response.getEntity().getContent()));
        StringBuffer buffer = new StringBuffer();
        String line = "";
        while ((line = bufferedReader.readLine()) != null) {
            buffer.append(line);
        }
        bufferedReader.close();

        return buffer.toString().contains("You are logged in as " + userName);
    }

}

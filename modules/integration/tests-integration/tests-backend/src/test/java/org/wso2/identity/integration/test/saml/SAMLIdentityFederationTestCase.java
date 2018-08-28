/*
*  Copyright (c)  WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
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

import org.apache.commons.lang.ArrayUtils;
import org.apache.commons.lang.StringUtils;
import org.apache.http.HttpResponse;
import org.apache.http.NameValuePair;
import org.apache.http.client.HttpClient;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.message.BasicNameValuePair;
import org.apache.http.util.EntityUtils;
import org.opensaml.xml.util.Base64;
import org.testng.Assert;
import org.testng.annotations.AfterClass;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;
import org.wso2.carbon.automation.engine.context.AutomationContext;
import org.wso2.carbon.automation.engine.context.TestUserMode;
import org.wso2.carbon.identity.application.common.model.idp.xsd.FederatedAuthenticatorConfig;
import org.wso2.carbon.identity.application.common.model.idp.xsd.IdentityProvider;
import org.wso2.carbon.identity.application.common.model.idp.xsd.JustInTimeProvisioningConfig;
import org.wso2.carbon.identity.application.common.model.idp.xsd.Property;
import org.wso2.carbon.identity.application.common.model.xsd.AuthenticationStep;
import org.wso2.carbon.identity.application.common.model.xsd.Claim;
import org.wso2.carbon.identity.application.common.model.xsd.ClaimMapping;
import org.wso2.carbon.identity.application.common.model.xsd.InboundAuthenticationRequestConfig;
import org.wso2.carbon.identity.application.common.model.xsd.ServiceProvider;
import org.wso2.carbon.identity.sso.saml.stub.types.SAMLSSOServiceProviderDTO;
import org.wso2.identity.integration.common.clients.UserManagementClient;
import org.wso2.carbon.user.mgt.stub.UserAdminUserAdminException;
import org.wso2.identity.integration.common.utils.ISIntegrationTest;
import org.wso2.identity.integration.test.application.mgt.AbstractIdentityFederationTestCase;
import org.wso2.identity.integration.test.util.Utils;
import org.wso2.identity.integration.test.utils.CommonConstants;
import org.wso2.identity.integration.test.utils.IdentityConstants;

import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.URL;
import java.rmi.RemoteException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class SAMLIdentityFederationTestCase extends AbstractIdentityFederationTestCase {

    private static final String PRIMARY_IS_SERVICE_PROVIDER_NAME = "travelocity";
    private static final String SECONDARY_IS_SERVICE_PROVIDER_NAME = "secondarySP";
    protected static final String IDENTITY_PROVIDER_NAME = "trustedIdP";
    private static final String PRIMARY_IS_SAML_ISSUER_NAME = "travelocity.com";
    private static final String PRIMARY_IS_SAML_ACS_URL = "http://localhost:8490/travelocity.com/home.jsp";
    private static final String SECONDARY_IS_SAML_ISSUER_NAME = "samlFedSP";
    private static final String SAML_NAME_ID_FORMAT = "urn:oasis:names:tc:SAML:1.1:nameid-format:emailAddress";
    private static final String SAML_SSO_URL = "http://localhost:8490/travelocity.com/samlsso?SAML2" +
                                               ".HTTPBinding=HTTP-Redirect";
    private static final String SAML_SSO_LOGOUT_URL = "http://localhost:8490/travelocity"
            + ".com/logout?SAML2.HTTPBinding=HTTP-Redirect";

    private static final String USER_AGENT = "Apache-HttpClient/4.2.5 (java 1.5)";
    private static final String AUTHENTICATION_TYPE = "federated";
    private static final String INBOUND_AUTH_TYPE = "samlsso";
    private static final int TOMCAT_8490 = 8490;
    protected static final int PORT_OFFSET_0 = 0;
    private static final int PORT_OFFSET_1 = 1;
    private static final String SAMLSSOAUTHENTICATOR = "SAMLSSOAuthenticator";
    private String COMMON_AUTH_URL = "https://localhost:%s/commonauth";

    private String usrName = "testFederatedUser";
    private String usrPwd = "testFederatePassword";
    private String usrRole = "admin";

    //Claim Uris
    private static final String lastNameClaimURI = "http://wso2.org/claims/lastname";

    private static final String ATTRIBUTE_CS_INDEX_NAME_SP = "attrConsumServiceIndex";
    private static final String ATTRIBUTE_CS_INDEX_NAME_IDP = "AttributeConsumingServiceIndex";

    @BeforeClass(alwaysRun = true)
    public void initTest() throws Exception {

        super.initTest();

        Map<String, String> startupParameters = new HashMap<String, String>();
        startupParameters.put("-DportOffset", String.valueOf(PORT_OFFSET_1 + CommonConstants.IS_DEFAULT_OFFSET));
        AutomationContext context = new AutomationContext("IDENTITY", "identity002", TestUserMode.SUPER_TENANT_ADMIN);

        startCarbonServer(PORT_OFFSET_1, context, startupParameters);

//TODO: Need to fix tomcat issue
        super.startTomcat(TOMCAT_8490);
//        super.addWebAppToTomcat(TOMCAT_8490, "/travelocity.com", getClass().getResource(ISIntegrationTest.URL_SEPARATOR + "samples" +
//                                                                                        ISIntegrationTest.URL_SEPARATOR + "org.wso2.sample.is.sso.agent.war").getPath());

        URL resourceUrl = getClass().getResource(ISIntegrationTest.URL_SEPARATOR + "samples" + ISIntegrationTest.URL_SEPARATOR + "travelocity.com.war");
        super.addWebAppToTomcat(TOMCAT_8490, "/travelocity.com", resourceUrl.getPath());


        super.createServiceClients(PORT_OFFSET_0, sessionCookie, new IdentityConstants
                .ServiceClientType[]{IdentityConstants.ServiceClientType.APPLICATION_MANAGEMENT, IdentityConstants.ServiceClientType.IDENTITY_PROVIDER_MGT, IdentityConstants.ServiceClientType.SAML_SSO_CONFIG});
        super.createServiceClients(PORT_OFFSET_1, null, new IdentityConstants.ServiceClientType[]{IdentityConstants.ServiceClientType.APPLICATION_MANAGEMENT, IdentityConstants.ServiceClientType.SAML_SSO_CONFIG});
        //add new test user to secondary IS
        boolean userCreated = addUserToSecondaryIS();
        Assert.assertTrue(userCreated, "User creation failed");
    }

    @AfterClass(alwaysRun = true)
    public void endTest() throws Exception {

        super.deleteSAML2WebSSOConfiguration(PORT_OFFSET_0, PRIMARY_IS_SAML_ISSUER_NAME);
        super.deleteServiceProvider(PORT_OFFSET_0, PRIMARY_IS_SERVICE_PROVIDER_NAME);
        super.deleteIdentityProvider(PORT_OFFSET_0, IDENTITY_PROVIDER_NAME);

        super.deleteSAML2WebSSOConfiguration(PORT_OFFSET_1, SECONDARY_IS_SAML_ISSUER_NAME);
        super.deleteServiceProvider(PORT_OFFSET_1, SECONDARY_IS_SERVICE_PROVIDER_NAME);

        //delete added users to secondary IS
        deleteAddedUsers();

        super.stopCarbonServer(PORT_OFFSET_1);
        super.stopTomcat(TOMCAT_8490);

        super.stopHttpClient();

    }

    @Test(priority = 1, groups = "wso2.is", description = "Check create identity provider in primary IS")
    public void testCreateIdentityProviderInPrimaryIS() throws Exception {

        IdentityProvider identityProvider = new IdentityProvider();
        identityProvider.setIdentityProviderName(IDENTITY_PROVIDER_NAME);

        FederatedAuthenticatorConfig saml2SSOAuthnConfig = new FederatedAuthenticatorConfig();
        saml2SSOAuthnConfig.setName(SAMLSSOAUTHENTICATOR);
        saml2SSOAuthnConfig.setDisplayName("samlsso");
        saml2SSOAuthnConfig.setEnabled(true);
        saml2SSOAuthnConfig.setProperties(getSAML2SSOAuthnConfigProperties());
        identityProvider.setDefaultAuthenticatorConfig(saml2SSOAuthnConfig);
        identityProvider.setFederatedAuthenticatorConfigs(new FederatedAuthenticatorConfig[]{saml2SSOAuthnConfig});

        JustInTimeProvisioningConfig jitConfig = new JustInTimeProvisioningConfig();
        jitConfig.setProvisioningEnabled(true);
        jitConfig.setProvisioningUserStore("PRIMARY");
        identityProvider.setJustInTimeProvisioningConfig(jitConfig);

        super.addIdentityProvider(PORT_OFFSET_0, identityProvider);

        Assert.assertNotNull(getIdentityProvider(PORT_OFFSET_0, IDENTITY_PROVIDER_NAME), "Failed to create Identity Provider 'trustedIdP' in primary IS");
    }

    @Test(priority = 2, groups = "wso2.is", description = "Check create service provider in primary IS")
    public void testCreateServiceProviderInPrimaryIS() throws Exception {

        super.addServiceProvider(PORT_OFFSET_0, PRIMARY_IS_SERVICE_PROVIDER_NAME);

        ServiceProvider serviceProvider = getServiceProvider(PORT_OFFSET_0, PRIMARY_IS_SERVICE_PROVIDER_NAME);
        Assert.assertNotNull(serviceProvider, "Failed to create service provider 'travelocity' in primary IS");

        updateServiceProviderWithSAMLConfigs(PORT_OFFSET_0, PRIMARY_IS_SAML_ISSUER_NAME, PRIMARY_IS_SAML_ACS_URL, serviceProvider);

        AuthenticationStep authStep = new AuthenticationStep();
        org.wso2.carbon.identity.application.common.model.xsd.IdentityProvider idP = new org.wso2.carbon.identity.application.common.model.xsd.IdentityProvider();
        idP.setIdentityProviderName(IDENTITY_PROVIDER_NAME);
        org.wso2.carbon.identity.application.common.model.xsd.FederatedAuthenticatorConfig saml2SSOAuthnConfig = new org.wso2.carbon.identity.application.common.model.xsd.FederatedAuthenticatorConfig();
        saml2SSOAuthnConfig.setName("SAMLSSOAuthenticator");
        saml2SSOAuthnConfig.setDisplayName("samlsso");
        idP.setFederatedAuthenticatorConfigs(new org.wso2.carbon.identity.application.common.model.xsd.FederatedAuthenticatorConfig[]{saml2SSOAuthnConfig});
        authStep.setFederatedIdentityProviders(new org.wso2.carbon.identity.application.common.model.xsd.IdentityProvider[]{idP});
        serviceProvider.getLocalAndOutBoundAuthenticationConfig().setAuthenticationSteps(new AuthenticationStep[]{authStep});
        serviceProvider.getLocalAndOutBoundAuthenticationConfig().setAuthenticationType(AUTHENTICATION_TYPE);

        updateServiceProvider(PORT_OFFSET_0, serviceProvider);
        serviceProvider = getServiceProvider(PORT_OFFSET_0, PRIMARY_IS_SERVICE_PROVIDER_NAME);

        InboundAuthenticationRequestConfig[] configs = serviceProvider.getInboundAuthenticationConfig().getInboundAuthenticationRequestConfigs();
        boolean success = false;
        if (configs != null) {
            for (InboundAuthenticationRequestConfig config : configs) {
                if (PRIMARY_IS_SAML_ISSUER_NAME.equals(config.getInboundAuthKey()) && INBOUND_AUTH_TYPE.equals(config.getInboundAuthType())) {
                    success = true;
                    break;
                }
            }
        }

        Assert.assertTrue(success, "Failed to update service provider with inbound SAML2 configs in primary IS");
        Assert.assertTrue(AUTHENTICATION_TYPE.equals(serviceProvider.getLocalAndOutBoundAuthenticationConfig().getAuthenticationType()), "Failed to update local and out bound configs in primary IS");
    }

    @Test(priority = 3, groups = "wso2.is", description = "Check create service provider in secondary IS")
    public void testCreateServiceProviderInSecondaryIS() throws Exception {

        super.addServiceProvider(PORT_OFFSET_1, SECONDARY_IS_SERVICE_PROVIDER_NAME);

        ServiceProvider serviceProvider = getServiceProvider(PORT_OFFSET_1, SECONDARY_IS_SERVICE_PROVIDER_NAME);
        Assert.assertNotNull(serviceProvider, "Failed to create service provider 'secondarySP' in secondary IS");

        updateServiceProviderWithSAMLConfigs(PORT_OFFSET_1, SECONDARY_IS_SAML_ISSUER_NAME, String.format(COMMON_AUTH_URL, DEFAULT_PORT + PORT_OFFSET_0), serviceProvider);

        updateServiceProvider(PORT_OFFSET_1, serviceProvider);
        serviceProvider = getServiceProvider(PORT_OFFSET_1, SECONDARY_IS_SERVICE_PROVIDER_NAME);

        InboundAuthenticationRequestConfig[] configs = serviceProvider.getInboundAuthenticationConfig().getInboundAuthenticationRequestConfigs();
        boolean success = false;
        if (configs != null) {
            for (InboundAuthenticationRequestConfig config : configs) {
                if (SECONDARY_IS_SAML_ISSUER_NAME.equals(config.getInboundAuthKey()) && INBOUND_AUTH_TYPE.equals(config.getInboundAuthType())) {
                    success = true;
                    break;
                }
            }
        }

        Assert.assertTrue(success, "Failed to update service provider with inbound SAML2 configs in secondary IS");
    }

    @Test(priority = 4, groups = "wso2.is", description = "Check functionality of attribute consumer index")
    public void testAttributeConsumerIndex() throws Exception {

        ServiceProvider serviceProvider = getServiceProvider(PORT_OFFSET_1, SECONDARY_IS_SERVICE_PROVIDER_NAME);
        serviceProvider.getClaimConfig().setClaimMappings(getClaimMappings());
        updateServiceProvider(PORT_OFFSET_1,serviceProvider);

        InboundAuthenticationRequestConfig requestConfigs[] = serviceProvider.getInboundAuthenticationConfig()
                .getInboundAuthenticationRequestConfigs();

        String attributeConsumerServiceIndex = null;

        if(!ArrayUtils.isEmpty(requestConfigs)) {
            org.wso2.carbon.identity.application.common.model.xsd.Property[] properties = requestConfigs[0]
                    .getProperties();
            for (int i = 0; i < properties.length ; i++) {
                if(ATTRIBUTE_CS_INDEX_NAME_SP.equals(properties[0].getName())){
                    attributeConsumerServiceIndex = properties[0].getValue();
                    break;
                }
                if (i == properties.length - 1) {
                    Assert.fail();
                }
            }
        } else {
            Assert.fail();
        }

        IdentityProvider identityProvider = getIdentityProvider(PORT_OFFSET_0, IDENTITY_PROVIDER_NAME);
        FederatedAuthenticatorConfig[] federatedAuthenticatorConfigs = identityProvider
                .getFederatedAuthenticatorConfigs();
        FederatedAuthenticatorConfig SAMLAuthenticatorConfig = null;
        for (int i = 0; i < federatedAuthenticatorConfigs.length; i++) {
            if (SAMLSSOAUTHENTICATOR.equals(federatedAuthenticatorConfigs[i].getName())) {
                SAMLAuthenticatorConfig = federatedAuthenticatorConfigs[i];
                break;
            }
        }

        Property[] properties = SAMLAuthenticatorConfig.getProperties();
        for (int i = 0; i < properties.length; i++) {
            if (ATTRIBUTE_CS_INDEX_NAME_IDP.equals(properties[i].getName())){
                properties[i].setValue(attributeConsumerServiceIndex);
            }
        }

        updateIdentityProvider(PORT_OFFSET_0, IDENTITY_PROVIDER_NAME, identityProvider);
    }

    public void testSAMLToSAMLFederation() throws Exception {

        HttpClient client = new DefaultHttpClient();

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

        String decodedSAMLResponse = new String(Base64.decode(samlResponse));
        Assert.assertTrue(decodedSAMLResponse.contains("AuthnContextClassRef"), "AuthnContextClassRef is not received" +
                ".");
        Assert.assertTrue(decodedSAMLResponse.contains("AuthenticatingAuthority"), "AuthenticatingAuthority is not " +
                "received.");

        boolean validResponse = sendSAMLResponseToWebApp(client, samlResponse);
        Assert.assertTrue(validResponse, "Invalid SAML response received by travelocity app");
    }

    private ClaimMapping[] getClaimMappings(){
        List<ClaimMapping> claimMappingList = new ArrayList<ClaimMapping>();

        Claim lastNameClaim = new Claim();
        lastNameClaim.setClaimUri(lastNameClaimURI);
        ClaimMapping lastNameClaimMapping = new ClaimMapping();
        lastNameClaimMapping.setRequested(true);
        lastNameClaimMapping.setLocalClaim(lastNameClaim);
        lastNameClaimMapping.setRemoteClaim(lastNameClaim);
        claimMappingList.add(lastNameClaimMapping);

        return claimMappingList.toArray(new ClaimMapping[claimMappingList.size()]);
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
        String locationHeader = getHeaderValue(response, "Location");
        if (Utils.requestMissingClaims(response)) {
            String pastrCookie = Utils.getPastreCookie(response);
            Assert.assertNotNull(pastrCookie, "pastr cookie not found in response.");
            EntityUtils.consume(response.getEntity());

            response = Utils.sendPOSTConsentMessage(response, String.format(COMMON_AUTH_URL, DEFAULT_PORT +
                            PORT_OFFSET_1), USER_AGENT , locationHeader, client, pastrCookie);
            EntityUtils.consume(response.getEntity());
            locationHeader = getHeaderValue(response, "Location");
        }
        closeHttpConnection(response);

        return locationHeader;
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
        String locationHeader = getHeaderValue(response, "Location");
        String pastrCookie = Utils.getPastreCookie(response);
        if (Utils.requestMissingClaims(response)) {
            locationHeader = handleMissingClaims(response, locationHeader, client, pastrCookie);

            if (locationHeader.contains("signup.do")) {
                response = Utils.sendPostJITHandlerResponse(response, String.format(COMMON_AUTH_URL, DEFAULT_PORT +
                        PORT_OFFSET_0), USER_AGENT, locationHeader, client, pastrCookie);
                EntityUtils.consume(response.getEntity());
                locationHeader = getHeaderValue(response, "Location");
            }
        } else if (locationHeader.contains("signup.do") || locationHeader.contains("register.do")) {
            Assert.assertNotNull(pastrCookie, "pastr cookie not found in response.");
            EntityUtils.consume(response.getEntity());
            response = Utils.sendPostJITHandlerResponse(response, String.format(COMMON_AUTH_URL, DEFAULT_PORT +
                    PORT_OFFSET_0), USER_AGENT, locationHeader, client, pastrCookie);
            EntityUtils.consume(response.getEntity());
            locationHeader = getHeaderValue(response, "Location");

            if (locationHeader.contains("signup.do")) {
                EntityUtils.consume(response.getEntity());
                response = Utils.sendPostJITHandlerResponse(response, String.format(COMMON_AUTH_URL, DEFAULT_PORT +
                        PORT_OFFSET_0), USER_AGENT, locationHeader, client, pastrCookie);
                EntityUtils.consume(response.getEntity());
                locationHeader = getHeaderValue(response, "Location");

                if (Utils.requestMissingClaims(response)) {
                    locationHeader = handleMissingClaims(response, locationHeader, client, pastrCookie);
                }
            }
            if (Utils.requestMissingClaims(response)) {
                locationHeader = handleMissingClaims(response, locationHeader, client, pastrCookie);
            }
        }
        closeHttpConnection(response);
        return locationHeader;
    }

    private String handleMissingClaims(HttpResponse response, String locationHeader, HttpClient client, String
            pastrCookie) throws Exception {
        EntityUtils.consume(response.getEntity());

        response = Utils.sendPOSTConsentMessage(response, String.format(COMMON_AUTH_URL, DEFAULT_PORT + PORT_OFFSET_0),
                USER_AGENT, locationHeader, client, pastrCookie);
        EntityUtils.consume(response.getEntity());
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


    /**
     * Function to retrieve service URI of secondary IS
     * @return service uri
     */
    protected String getSecondaryISURI() {
        return String.format("https://localhost:%s/services/", DEFAULT_PORT + PORT_OFFSET_1);
    }

    /**
     * Function to retrieve test user added to secondary IS, to test federated authentication
     *
     * @return user name
     */
    protected String getFederatedTestUser() {
        return usrName;
    }

    private boolean addUserToSecondaryIS() throws Exception {
        UserManagementClient usrMgtClient = new UserManagementClient(getSecondaryISURI(), "admin", "admin");
        if (usrMgtClient == null) {
            return false;
        } else {
            String[] roles = {usrRole};
            usrMgtClient.addUser(usrName, usrPwd, roles, null);
            if (usrMgtClient.userNameExists(usrRole, usrName)) {
                return true;
            } else {
                return false;
            }
        }
    }

    private void deleteAddedUsers() throws RemoteException, UserAdminUserAdminException {
        UserManagementClient usrMgtClient = new UserManagementClient(getSecondaryISURI(), "admin", "admin");
        usrMgtClient.deleteUser(usrName);
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

        Property[] properties = new Property[14];
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

        property = new Property();
        property.setName(IdentityConstants.Authenticator.SAML2SSO.RESPONSE_AUTHN_CONTEXT_CLASS_REF);
        property.setValue("as_response");
        properties[13] = property;

        return properties;
    }

    private void assertLocalClaims(String resultPage) {
        String claimString = resultPage.substring(resultPage.lastIndexOf("<table>"));
        Map<String, String> attributeMap = extractClaims(claimString);
        Assert.assertTrue(attributeMap.containsKey(lastNameClaimURI), "Claim lastname is expected");
        Assert.assertEquals(attributeMap.get(lastNameClaimURI), usrName,
                "Expected claim value for lastname is " + usrName);
    }

    private Map<String, String> extractClaims(String claimString) {
        String[] dataArray = StringUtils.substringsBetween(claimString, "<td>", "</td>");
        Map<String, String> attributeMap = new HashMap<String, String>();
        String key = null;
        String value;
        for (int i = 0; i < dataArray.length; i++) {
            if ((i % 2) == 0) {
                key = dataArray[i];
            } else {
                value = dataArray[i].trim();
                attributeMap.put(key, value);
            }
        }

        return attributeMap;
    }

    public boolean validateSAMLResponse(HttpResponse response, String userName) throws IOException {
        BufferedReader bufferedReader = new BufferedReader(new InputStreamReader(response.getEntity().getContent()));
        StringBuffer buffer = new StringBuffer();
        String line = "";
        while ((line = bufferedReader.readLine()) != null) {
            buffer.append(line);
        }
        bufferedReader.close();
        assertLocalClaims(buffer.toString());
        return buffer.toString().contains("You are logged in as " + userName);
    }

}
/*
*  Copyright (c) 2017, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
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

import org.apache.commons.lang.StringUtils;
import org.apache.http.HttpResponse;
import org.apache.http.NameValuePair;
import org.apache.http.client.HttpClient;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClientBuilder;
import org.apache.http.message.BasicNameValuePair;
import org.apache.http.util.EntityUtils;
import org.testng.Assert;
import org.testng.annotations.AfterClass;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;
import org.wso2.carbon.automation.engine.frameworkutils.FrameworkPathUtil;
import org.wso2.carbon.identity.application.common.model.xsd.Claim;
import org.wso2.carbon.identity.application.common.model.xsd.ClaimMapping;
import org.wso2.carbon.identity.application.common.model.xsd.InboundAuthenticationRequestConfig;
import org.wso2.carbon.identity.application.common.model.xsd.ServiceProvider;
import org.wso2.carbon.identity.sso.saml.stub.types.SAMLSSOServiceProviderDTO;
import org.wso2.carbon.integration.common.utils.FileManager;
import org.wso2.carbon.integration.common.utils.exceptions.AutomationUtilException;
import org.wso2.carbon.integration.common.utils.mgt.ServerConfigurationManager;
import org.wso2.carbon.um.ws.api.stub.ClaimValue;
import org.wso2.identity.integration.common.clients.usermgt.remote.RemoteUserStoreManagerServiceClient;
import org.wso2.identity.integration.test.application.mgt.AbstractIdentityFederationTestCase;
import org.wso2.identity.integration.test.base.TestDataHolder;
import org.wso2.identity.integration.test.util.Utils;
import org.wso2.identity.integration.test.utils.IdentityConstants;

import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class SAMLFederationWithFileBasedSPAndIDPTestCase extends AbstractIdentityFederationTestCase {

    private static final String SAML_SSO_URL = "http://localhost:8490/travelocity.com/samlsso?SAML2" + "" +
            ".HTTPBinding=HTTP-Redirect";
    private static final String PRIMARY_IS_SAML_ACS_URL = "http://localhost:8490/travelocity.com/home.jsp";
    private static final String USER_AGENT = "Apache-HttpClient/4.2.5 (java 1.5)";
    private static final String SAML_NAME_ID_FORMAT = "urn:oasis:names:tc:SAML:1.1:nameid-format:emailAddress";
    private static final String INBOUND_AUTH_TYPE = "samlsso";
    private static final String SECONDARY_IS_SERVICE_PROVIDER_NAME = "primaryIS";
    private static final String SECONDARY_IS_SAML_ISSUER_NAME = "is-sp-saml";
    private static final int TOMCAT_8490 = 8490;
    private static final int PORT_OFFSET_0 = 0;
    private static final int PORT_OFFSET_1 = 1;
    //Claim Uris
    private static final String firstNameLocalClaimURI = "http://wso2.org/claims/givenname";
    private static final String firstNameRemoteIdPClaimURI = "http://is.idp/claims/givenname";
    private static final String emailLocalClaimURI = "http://wso2.org/claims/emailaddress";
    private static final String emailRemoteIdPClaimURI = "http://is.idp/claims/emailaddress";
    // User Profile Attributes
    private static final String userName = "samlFederatedUser1";
    private static final String password = "samlFederatedUserPassword1";
    private static final String email = "samlFederatedUser1@wso2.com";
    private static final String profileName = "default";
    private static String COMMON_AUTH_URL = "https://localhost:%s/commonauth";
    private ServerConfigurationManager serverConfigurationManager;
    private RemoteUserStoreManagerServiceClient remoteUSMServiceClient;

    @BeforeClass(alwaysRun = true)
    public void initTest() throws Exception {

        super.initTest();
        // Apply file based configurations
        serverConfigurationManager = new ServerConfigurationManager(isServer);
        applyConfigurationsForPrimaryIS();
        // Start secondary IS server
        TestDataHolder testDataHolder = TestDataHolder.getInstance();
        // Create service clients
        super.createServiceClients(PORT_OFFSET_1, null, new IdentityConstants.ServiceClientType[]{IdentityConstants
                .ServiceClientType.APPLICATION_MANAGEMENT, IdentityConstants.ServiceClientType.SAML_SSO_CONFIG});
        remoteUSMServiceClient = new RemoteUserStoreManagerServiceClient(getSecondaryISURI(), testDataHolder.getAutomationContext()
                .getContextTenant().getContextUser().getUserName(), testDataHolder.getAutomationContext().getContextTenant()
                .getContextUser().getPassword());
        // Create user in secondary IS server
        createUserInSecondaryIS();
    }

    @AfterClass(alwaysRun = true)
    public void endTest() throws Exception {

        try {
            super.deleteServiceProvider(PORT_OFFSET_1, SECONDARY_IS_SERVICE_PROVIDER_NAME);
            deleteUserInSecondaryIS();

            remoteUSMServiceClient = null;

            removeConfigurationsFromPrimaryIS();
        } catch (Exception e) {
            log.error("Failure occured due to :" + e.getMessage(), e);
            throw e;
        }
    }

    @Test(priority = 1, groups = "wso2.is", description = "Check create service provider in secondary IS")
    public void testCreateServiceProviderInSecondaryIS() throws Exception {

        super.addServiceProvider(PORT_OFFSET_1, SECONDARY_IS_SERVICE_PROVIDER_NAME);

        ServiceProvider serviceProvider = getServiceProvider(PORT_OFFSET_1, SECONDARY_IS_SERVICE_PROVIDER_NAME);
        Assert.assertNotNull(serviceProvider, "Failed to create service provider 'secondarySP' in secondary IS");
        // Set SAML configurations
        updateServiceProviderWithSAMLConfigs(PORT_OFFSET_1, SECONDARY_IS_SAML_ISSUER_NAME, String.format
                (COMMON_AUTH_URL, DEFAULT_PORT + PORT_OFFSET_0), serviceProvider);
        // Set claim configurations
        serviceProvider.getClaimConfig().setLocalClaimDialect(false);
        serviceProvider.getClaimConfig().setClaimMappings(getClaimMappingsForSPInSecondaryIS());
        updateServiceProvider(PORT_OFFSET_1, serviceProvider);

        serviceProvider = getServiceProvider(PORT_OFFSET_1, SECONDARY_IS_SERVICE_PROVIDER_NAME);

        InboundAuthenticationRequestConfig[] configs = serviceProvider.getInboundAuthenticationConfig()
                .getInboundAuthenticationRequestConfigs();
        boolean success = false;
        if (configs != null) {
            for (InboundAuthenticationRequestConfig config : configs) {
                if (SECONDARY_IS_SAML_ISSUER_NAME.equals(config.getInboundAuthKey()) && INBOUND_AUTH_TYPE.equals
                        (config.getInboundAuthType())) {
                    success = true;
                    break;
                }
            }
        }

        Assert.assertTrue(success, "Failed to update service provider with inbound SAML2 configs in secondary IS");
    }

    @Test(priority = 5, groups = "wso2.is", description = "Check SAML To SAML fedaration flow")
    public void testSAMLToSAMLFederation() throws Exception {

        try (CloseableHttpClient client = HttpClientBuilder.create().build()) {
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
    }

    protected String sendSAMLRequestToPrimaryIS(HttpClient client) throws Exception {

        HttpGet request = new HttpGet(SAML_SSO_URL);
        request.addHeader("User-Agent", USER_AGENT);
        HttpResponse response = client.execute(request);

        return extractValueFromResponse(response, "name=\"sessionDataKey\"", 1);
    }

    protected String authenticateWithSecondaryIS(HttpClient client, String sessionId) throws Exception {

        HttpPost request = new HttpPost(String.format(COMMON_AUTH_URL, DEFAULT_PORT + PORT_OFFSET_1));
        request.addHeader("User-Agent", USER_AGENT);
        request.addHeader("Referer", PRIMARY_IS_SAML_ACS_URL);

        List<NameValuePair> urlParameters = new ArrayList<NameValuePair>();
        urlParameters.add(new BasicNameValuePair("username", userName));
        urlParameters.add(new BasicNameValuePair("password", password));
        urlParameters.add(new BasicNameValuePair("sessionDataKey", sessionId));
        request.setEntity(new UrlEncodedFormEntity(urlParameters));

        HttpResponse response = client.execute(request);
        String locationHeader = getHeaderValue(response, "Location");
        if (Utils.requestMissingClaims(response)) {
            String pastrCookie = Utils.getPastreCookie(response);
            Assert.assertNotNull(pastrCookie, "pastr cookie not found in response.");
            EntityUtils.consume(response.getEntity());

            response = Utils.sendPOSTConsentMessage(response, String.format(COMMON_AUTH_URL, DEFAULT_PORT +
                    PORT_OFFSET_1), USER_AGENT, locationHeader, client, pastrCookie);
            EntityUtils.consume(response.getEntity());
            locationHeader = getHeaderValue(response, "Location");
        }
        closeHttpConnection(response);

        return locationHeader;
    }

    protected Map<String, String> getSAMLResponseFromSecondaryIS(HttpClient client, String redirectURL) throws
            Exception {

        HttpPost request = new HttpPost(redirectURL);
        request.addHeader("User-Agent", USER_AGENT);
        request.addHeader("Referer", PRIMARY_IS_SAML_ACS_URL);
        HttpResponse response = client.execute(request);

        Map<String, Integer> searchParams = new HashMap<String, Integer>();
        searchParams.put("SAMLResponse", 5);
        searchParams.put("RelayState", 5);
        return extractValuesFromResponse(response, searchParams);
    }

    protected String sendSAMLResponseToPrimaryIS(HttpClient client, Map<String, String> searchResults) throws
            Exception {

        HttpPost request = new HttpPost(String.format(COMMON_AUTH_URL, DEFAULT_PORT + PORT_OFFSET_0));
        request.setHeader("User-Agent", USER_AGENT);

        List<NameValuePair> urlParameters = new ArrayList<NameValuePair>();
        urlParameters.add(new BasicNameValuePair("SAMLResponse", searchResults.get("SAMLResponse")));
        urlParameters.add(new BasicNameValuePair("RelayState", searchResults.get("RelayState")));
        request.setEntity(new UrlEncodedFormEntity(urlParameters));

        HttpResponse response = client.execute(request);
        String locationHeader = getHeaderValue(response, "Location");
        if (Utils.requestMissingClaims(response)) {

            String pastrCookie = Utils.getPastreCookie(response);
            Assert.assertNotNull(pastrCookie, "pastr cookie not found in response.");
            EntityUtils.consume(response.getEntity());

            response = Utils.sendPOSTConsentMessage(response, String.format(COMMON_AUTH_URL, DEFAULT_PORT +
                    PORT_OFFSET_0), USER_AGENT, locationHeader, client, pastrCookie);
            EntityUtils.consume(response.getEntity());
            locationHeader = getHeaderValue(response, "Location");
        }
        closeHttpConnection(response);

        return locationHeader;
    }

    protected String getSAMLResponseFromPrimaryIS(HttpClient client, String redirectURL) throws Exception {

        HttpGet request = new HttpGet(redirectURL);
        request.addHeader("User-Agent", USER_AGENT);
        HttpResponse response = client.execute(request);

        return extractValueFromResponse(response, "SAMLResponse", 5);
    }

    protected boolean sendSAMLResponseToWebApp(HttpClient client, String samlResponse) throws Exception {

        HttpPost request = new HttpPost(PRIMARY_IS_SAML_ACS_URL);
        request.setHeader("User-Agent", USER_AGENT);
        List<NameValuePair> urlParameters = new ArrayList<NameValuePair>();
        urlParameters.add(new BasicNameValuePair("SAMLResponse", samlResponse));
        request.setEntity(new UrlEncodedFormEntity(urlParameters));
        HttpResponse response = client.execute(request);

        return validateSAMLResponse(response, userName);
    }

    public boolean validateSAMLResponse(HttpResponse response, String userName) throws IOException {
        BufferedReader bufferedReader = new BufferedReader(new InputStreamReader(response.getEntity().getContent()));
        StringBuffer buffer = new StringBuffer();
        String line = "";
        while ((line = bufferedReader.readLine()) != null) {
            System.out.println(line);
            buffer.append(line);
        }
        bufferedReader.close();
        assertLocalClaims(buffer.toString());
        return buffer.toString().contains("You are logged in as " + userName);
    }

    protected void assertLocalClaims(String resultPage) {
        String claimString = resultPage.substring(resultPage.lastIndexOf("<table>"));
        Map<String, String> attributeMap = extractClaims(claimString);
        Assert.assertTrue(attributeMap.containsKey(firstNameLocalClaimURI), "Claim firstname is expected");
        Assert.assertEquals(attributeMap.get(firstNameLocalClaimURI), userName, "Expected claim value for lastname "
                + "is" + " " + userName);
        Assert.assertTrue(attributeMap.containsKey(emailLocalClaimURI), "Claim email is expected");
        Assert.assertEquals(attributeMap.get(emailLocalClaimURI), email, "Expected claim value for email is " + email);
    }

    protected void updateServiceProviderWithSAMLConfigs(int portOffset, String issuerName, String acsUrl,
                                                        ServiceProvider serviceProvider) throws Exception {

        String attributeConsumingServiceIndex = super.createSAML2WebSSOConfiguration(portOffset,
                getSAMLSSOServiceProviderDTO(issuerName, acsUrl));
        Assert.assertNotNull(attributeConsumingServiceIndex, "Failed to create SAML2 Web SSO configuration for " +
                "issuer" + " '" + issuerName + "'");

        InboundAuthenticationRequestConfig samlAuthenticationRequestConfig = new InboundAuthenticationRequestConfig();
        samlAuthenticationRequestConfig.setInboundAuthKey(issuerName);
        samlAuthenticationRequestConfig.setInboundAuthType(INBOUND_AUTH_TYPE);
        org.wso2.carbon.identity.application.common.model.xsd.Property property = new org.wso2.carbon.identity
                .application.common.model.xsd.Property();
        property.setName("attrConsumServiceIndex");
        property.setValue(attributeConsumingServiceIndex);
        samlAuthenticationRequestConfig.setProperties(new org.wso2.carbon.identity.application.common.model.xsd
                .Property[]{property});

        serviceProvider.getInboundAuthenticationConfig().setInboundAuthenticationRequestConfigs(new
                InboundAuthenticationRequestConfig[]{samlAuthenticationRequestConfig});
    }

    protected SAMLSSOServiceProviderDTO getSAMLSSOServiceProviderDTO(String issuerName, String acsUrl) {

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

    protected void applyConfigurationsForPrimaryIS() throws IOException, AutomationUtilException {

        File samlSPXml = new File(FrameworkPathUtil.getSystemResourceLocation() + "artifacts" + File.separator + "IS"
                + File.separator + "saml" + File.separator + "filebasedspidpconfigs" + File.separator + "saml-sp.xml");
        copyToIdentity(samlSPXml, "service-providers");

        File samlIdPXml = new File(FrameworkPathUtil.getSystemResourceLocation() + "artifacts" + File.separator +
                "IS" + File.separator + "saml" + File.separator + "filebasedspidpconfigs" + File.separator +
                "saml-idp.xml");
        copyToIdentity(samlIdPXml, "identity-providers");

        File ssoIdPConfigXml = new File(Utils.getResidentCarbonHome() + File.separator + "repository" + File
                .separator + "conf" + File.separator + "identity" + File.separator + "sso-idp-config.xml");
        File ssoIdPConfigXmlToCopy = new File(FrameworkPathUtil.getSystemResourceLocation() + "artifacts" + File
                .separator + "IS" + File.separator + "saml" + File.separator + "filebasedspidpconfigs" + File
                .separator + "saml-sp-sso-idp-config.xml");

        serverConfigurationManager.applyConfiguration(ssoIdPConfigXmlToCopy, ssoIdPConfigXml, false, true);
    }

    protected void removeConfigurationsFromPrimaryIS() throws IOException, AutomationUtilException {

        removeFromIdentity("saml-sp.xml", "service-providers");
        removeFromIdentity("saml-idp.xml", "identity-providers");

        File ssoIdPConfigXml = new File(Utils.getResidentCarbonHome() + File.separator + "repository" + File
                .separator + "conf" + File.separator + "identity" + File.separator + "sso-idp-config.xml");
        File ssoIdPConfigXmlToCopy = new File(FrameworkPathUtil.getSystemResourceLocation() + "artifacts" + File
                .separator + "IS" + File.separator + "saml" + File.separator + "filebasedspidpconfigs" + File
                .separator + "original-sso-idp-config.xml");

        serverConfigurationManager.applyConfiguration(ssoIdPConfigXmlToCopy, ssoIdPConfigXml, false, true);
    }

    protected void copyToIdentity(File sourceFile, String targetDirectory) throws IOException {

        String identityConfigPath = Utils.getResidentCarbonHome() + File.separator + "repository" + File.separator +
                "conf" + File.separator + "identity";
        if (StringUtils.isNotBlank(targetDirectory)) {
            identityConfigPath = identityConfigPath.concat(File.separator + targetDirectory);
        }
        FileManager.copyResourceToFileSystem(sourceFile.getAbsolutePath(), identityConfigPath, sourceFile.getName());
    }

    protected void removeFromIdentity(String fileName, String targetDirectory) throws IOException {

        String identityConfigPath = Utils.getResidentCarbonHome() + File.separator + "repository" + File.separator +
                "conf" + File.separator + "identity";
        if (StringUtils.isNotBlank(targetDirectory)) {
            identityConfigPath = identityConfigPath.concat(File.separator + targetDirectory);
        }

        File file = new File(identityConfigPath + File.separator + fileName);
        if (file.exists()) {
            FileManager.deleteFile(file.getAbsolutePath());
        }
    }

    protected void createUserInSecondaryIS() {
        log.info("Creating User " + userName);
        try {
            remoteUSMServiceClient.addUser(userName, password, null, getUserClaims(), profileName, true);
        } catch (Exception e) {
            Assert.fail("Error while creating the user", e);
        }

    }

    protected void deleteUserInSecondaryIS() {
        log.info("Deleting User " + userName);
        try {
            remoteUSMServiceClient.deleteUser(userName);
        } catch (Exception e) {
            Assert.fail("Error while deleting the user", e);
        }
    }

    protected ClaimValue[] getUserClaims() {

        ClaimValue[] claimValues;

        claimValues = new ClaimValue[2];

        ClaimValue firstNameClaim = new ClaimValue();
        firstNameClaim.setClaimURI(firstNameLocalClaimURI);
        firstNameClaim.setValue(userName);
        claimValues[0] = firstNameClaim;

        ClaimValue emailClaim = new ClaimValue();
        emailClaim.setClaimURI(emailLocalClaimURI);
        emailClaim.setValue(email);
        claimValues[1] = emailClaim;

        return claimValues;
    }

    protected ClaimMapping[] getClaimMappingsForSPInSecondaryIS() {

        ClaimMapping[] claimMappingArray = new ClaimMapping[2];

        Claim firstNameLocalClaim = new Claim();
        firstNameLocalClaim.setClaimUri(firstNameLocalClaimURI);
        Claim firstNameRemoteClaim = new Claim();
        firstNameRemoteClaim.setClaimUri(firstNameRemoteIdPClaimURI);
        ClaimMapping firstNameClaimMapping = new ClaimMapping();
        firstNameClaimMapping.setRequested(true);
        firstNameClaimMapping.setLocalClaim(firstNameLocalClaim);
        firstNameClaimMapping.setRemoteClaim(firstNameRemoteClaim);
        claimMappingArray[0] = firstNameClaimMapping;

        Claim emailLocalClaim = new Claim();
        emailLocalClaim.setClaimUri(emailLocalClaimURI);
        Claim emailRemoteClaim = new Claim();
        emailRemoteClaim.setClaimUri(emailRemoteIdPClaimURI);
        ClaimMapping emailClaimMapping = new ClaimMapping();
        emailClaimMapping.setRequested(true);
        emailClaimMapping.setLocalClaim(emailLocalClaim);
        emailClaimMapping.setRemoteClaim(emailRemoteClaim);
        claimMappingArray[1] = emailClaimMapping;

        return claimMappingArray;
    }

    protected String getSecondaryISURI() {
        return String.format("https://localhost:%s/services/", DEFAULT_PORT + PORT_OFFSET_1);
    }

}
/*
 * Copyright (c) 2017, WSO2 LLC. (http://www.wso2.com).
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
import org.apache.http.HttpResponse;
import org.apache.http.NameValuePair;
import org.apache.http.client.HttpClient;
import org.apache.http.client.config.CookieSpecs;
import org.apache.http.client.config.RequestConfig;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.config.Lookup;
import org.apache.http.config.RegistryBuilder;
import org.apache.http.cookie.CookieSpecProvider;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClientBuilder;
import org.apache.http.impl.cookie.RFC6265CookieSpecProvider;
import org.apache.http.message.BasicNameValuePair;
import org.apache.http.util.EntityUtils;
import org.testng.Assert;
import org.testng.annotations.AfterClass;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;
import org.wso2.carbon.automation.engine.context.beans.Tenant;
import org.wso2.carbon.automation.engine.context.beans.User;
import org.wso2.carbon.automation.engine.frameworkutils.FrameworkPathUtil;
import org.wso2.carbon.integration.common.utils.FileManager;
import org.wso2.carbon.integration.common.utils.exceptions.AutomationUtilException;
import org.wso2.carbon.integration.common.utils.mgt.ServerConfigurationManager;
import org.wso2.identity.integration.test.application.mgt.AbstractIdentityFederationTestCase;
import org.wso2.identity.integration.test.base.TestDataHolder;
import org.wso2.identity.integration.test.rest.api.server.application.management.v1.model.ApplicationModel;
import org.wso2.identity.integration.test.rest.api.server.application.management.v1.model.ApplicationResponseModel;
import org.wso2.identity.integration.test.rest.api.server.application.management.v1.model.Claim;
import org.wso2.identity.integration.test.rest.api.server.application.management.v1.model.ClaimConfiguration;
import org.wso2.identity.integration.test.rest.api.server.application.management.v1.model.ClaimConfiguration.DialectEnum;
import org.wso2.identity.integration.test.rest.api.server.application.management.v1.model.ClaimMappings;
import org.wso2.identity.integration.test.rest.api.server.application.management.v1.model.InboundProtocols;
import org.wso2.identity.integration.test.rest.api.server.application.management.v1.model.RequestedClaimConfiguration;
import org.wso2.identity.integration.test.rest.api.server.application.management.v1.model.SAML2Configuration;
import org.wso2.identity.integration.test.rest.api.server.application.management.v1.model.SAML2ServiceProvider;
import org.wso2.identity.integration.test.rest.api.server.application.management.v1.model.SAMLAssertionConfiguration;
import org.wso2.identity.integration.test.rest.api.server.application.management.v1.model.SAMLAttributeProfile;
import org.wso2.identity.integration.test.rest.api.server.application.management.v1.model.SAMLResponseSigning;
import org.wso2.identity.integration.test.rest.api.server.application.management.v1.model.SingleLogoutProfile;
import org.wso2.identity.integration.test.rest.api.server.application.management.v1.model.SingleSignOnProfile;
import org.wso2.identity.integration.test.rest.api.user.common.model.Email;
import org.wso2.identity.integration.test.rest.api.user.common.model.Name;
import org.wso2.identity.integration.test.rest.api.user.common.model.UserObject;
import org.wso2.identity.integration.test.restclients.SCIM2RestClient;
import org.wso2.identity.integration.test.util.Utils;
import org.wso2.identity.integration.test.utils.IdentityConstants;

import javax.xml.xpath.XPathExpressionException;
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
    private static final int PORT_OFFSET_0 = 0;
    private static final int PORT_OFFSET_1 = 1;
    //Claim Uris
    private static final String firstNameLocalClaimURI = "http://wso2.org/claims/givenname";
    private static final String firstNameRemoteIdPClaimURI = "http://is.idp/claims/givenname";
    private static final String emailLocalClaimURI = "http://wso2.org/claims/emailaddress";
    private static final String emailRemoteIdPClaimURI = "http://is.idp/claims/emailaddress";
    // User Profile Attributes
    private static final String userName = "samlFederatedUser1";
    private static final String password = "SamlFederatedUserPassword@1";
    private static final String email = "samlFederatedUser1@wso2.com";
    private static String COMMON_AUTH_URL = "https://localhost:%s/commonauth";
    private ServerConfigurationManager serverConfigurationManager;
    private TestDataHolder testDataHolder;
    private SCIM2RestClient scim2RestClient;
    private String userId;
    private String secondaryISAppId;

    @BeforeClass(alwaysRun = true)
    public void initTest() throws Exception {

        super.initTest();
        // Apply file based configurations
        serverConfigurationManager = new ServerConfigurationManager(isServer);
        applyConfigurationsForPrimaryIS();
        // Start secondary IS server
        testDataHolder = TestDataHolder.getInstance();
        // Create service clients
        createServiceClients(PORT_OFFSET_1, new IdentityConstants.ServiceClientType[]{
                IdentityConstants.ServiceClientType.APPLICATION_MANAGEMENT});
        scim2RestClient = new SCIM2RestClient(getSecondaryISURI(), getTenantInfo());

        // Create user in secondary IS server
        createUserInSecondaryIS();
    }

    @AfterClass(alwaysRun = true)
    public void endTest() throws Exception {

        try {
            deleteApplication(PORT_OFFSET_1, secondaryISAppId);
            deleteUserInSecondaryIS();

            scim2RestClient.closeHttpClient();
            removeConfigurationsFromPrimaryIS();
        } catch (Exception e) {
            log.error("Failure occurred due to :" + e.getMessage(), e);
            throw e;
        }
    }

    @Test(groups = "wso2.is", description = "Check create service provider in secondary IS")
    public void testCreateServiceProviderInSecondaryIS() throws Exception {

        ApplicationModel applicationCreationModel = new ApplicationModel()
                .name(SECONDARY_IS_SERVICE_PROVIDER_NAME)
                .description("This is a test Service Provider")
                .isManagementApp(true)
                .inboundProtocolConfiguration(new InboundProtocols().saml(getSAMLConfigsForSPInSecondaryIS()))
                .claimConfiguration(getClaimConfigsForSPInSecondaryIS());

        secondaryISAppId = addApplication(PORT_OFFSET_1, applicationCreationModel);
        ApplicationResponseModel application = getApplication(PORT_OFFSET_1, secondaryISAppId);
        Assert.assertNotNull(application, "Failed to create service provider 'primaryIS' in primary IS");

        boolean success = SECONDARY_IS_SAML_ISSUER_NAME.equals(application.getIssuer()) &&
                INBOUND_AUTH_TYPE.equals(application.getInboundProtocols().get(0).getType());

        Assert.assertTrue(success, "Failed to update service provider with inbound SAML2 configs in secondary IS");
    }

    @Test(groups = "wso2.is", dependsOnMethods = {"testCreateServiceProviderInSecondaryIS"}, description = "Check SAML To SAML fedaration flow")
    public void testSAMLToSAMLFederation() throws Exception {

        try (CloseableHttpClient client = getClosableHTTPClient()) {
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

        List<NameValuePair> urlParameters = new ArrayList<>();
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

        Map<String, Integer> searchParams = new HashMap<>();
        searchParams.put("SAMLResponse", 5);
        searchParams.put("RelayState", 5);
        return extractValuesFromResponse(response, searchParams);
    }

    protected String sendSAMLResponseToPrimaryIS(HttpClient client, Map<String, String> searchResults) throws
            Exception {

        HttpPost request = new HttpPost(String.format(COMMON_AUTH_URL, DEFAULT_PORT + PORT_OFFSET_0));
        request.setHeader("User-Agent", USER_AGENT);

        List<NameValuePair> urlParameters = new ArrayList<>();
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
        List<NameValuePair> urlParameters = new ArrayList<>();
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
            log.info("############## " + line);
            buffer.append(line);
        }
        bufferedReader.close();
        assertLocalClaims(buffer.toString());
        return buffer.toString().contains("You are logged in as ");
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

    private Map<String, String> extractClaims(String claimString) {

        String[] dataArray = StringUtils.substringsBetween(claimString, "<td>", "</td>");
        Map<String, String> attributeMap = new HashMap<>();
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

        // Not restarting since the next test will restart the server.
        serverConfigurationManager.applyConfiguration(ssoIdPConfigXmlToCopy, ssoIdPConfigXml, false, false);
    }

    protected void copyToIdentity(File sourceFile, String targetDirectory) throws IOException {

        String identityConfigPath = Utils.getResidentCarbonHome() + File.separator + "repository" + File.separator +
                "conf" + File.separator + "identity";
        if (StringUtils.isNotBlank(targetDirectory)) {
            identityConfigPath = identityConfigPath.concat(File.separator + targetDirectory);
        }
        FileManager.copyResourceToFileSystem(sourceFile.getAbsolutePath(), identityConfigPath, sourceFile.getName());
    }

    protected void removeFromIdentity(String fileName, String targetDirectory) {

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
            UserObject user = new UserObject()
                    .userName(userName)
                    .password(password)
                    .name(new Name().givenName(userName))
                    .addEmail(new Email().value(email));

            userId = scim2RestClient.createUser(user);
        } catch (Exception e) {
            Assert.fail("Error while creating the user", e);
        }
    }

    protected void deleteUserInSecondaryIS() {

        log.info("Deleting User " + userName);
        try {
            scim2RestClient.deleteUser(userId);
        } catch (Exception e) {
            Assert.fail("Error while deleting the user", e);
        }
    }

    private SAML2Configuration getSAMLConfigsForSPInSecondaryIS() {

        SAML2ServiceProvider serviceProvider = new SAML2ServiceProvider()
                .issuer(SECONDARY_IS_SAML_ISSUER_NAME)
                .addAssertionConsumerUrl(String.format(COMMON_AUTH_URL, DEFAULT_PORT + PORT_OFFSET_0))
                .defaultAssertionConsumerUrl(String.format(COMMON_AUTH_URL, DEFAULT_PORT + PORT_OFFSET_0))
                .attributeProfile(new SAMLAttributeProfile()
                        .enabled(true)
                        .alwaysIncludeAttributesInResponse(true))
                .singleLogoutProfile(new SingleLogoutProfile()
                        .enabled(true))
                .responseSigning(new SAMLResponseSigning()
                        .enabled(true))
                .singleSignOnProfile(new SingleSignOnProfile()
                        .assertion(new SAMLAssertionConfiguration().nameIdFormat(SAML_NAME_ID_FORMAT)));

        return new SAML2Configuration().manualConfiguration(serviceProvider);
    }

    private ClaimConfiguration getClaimConfigsForSPInSecondaryIS() {

        return new ClaimConfiguration()
                .dialect(DialectEnum.CUSTOM)
                .addClaimMappingsItem(new ClaimMappings()
                        .applicationClaim(firstNameRemoteIdPClaimURI)
                        .localClaim(new Claim().uri(firstNameLocalClaimURI)))
                .addClaimMappingsItem(new ClaimMappings()
                        .applicationClaim(emailRemoteIdPClaimURI)
                        .localClaim(new Claim().uri(emailLocalClaimURI)))
                .addRequestedClaimsItem(new RequestedClaimConfiguration()
                        .claim(new Claim().uri(firstNameRemoteIdPClaimURI)))
                .addRequestedClaimsItem(new RequestedClaimConfiguration()
                        .claim(new Claim().uri(emailRemoteIdPClaimURI)));
    }

    protected String getSecondaryISURI() {

        return String.format("https://localhost:%s/", DEFAULT_PORT + PORT_OFFSET_1);
    }

    private Tenant getTenantInfo() throws XPathExpressionException {

        User registryMountTenantAdmin = new User();
        registryMountTenantAdmin.setUserName(testDataHolder.getAutomationContext().getContextTenant().getContextUser()
                .getUserName());
        registryMountTenantAdmin.setPassword(testDataHolder.getAutomationContext().getContextTenant().getContextUser()
                .getPassword());
        Tenant registryMountTenant =  new Tenant();
        registryMountTenant.setContextUser(registryMountTenantAdmin);

        return registryMountTenant;
    }

    private CloseableHttpClient getClosableHTTPClient() {

        Lookup<CookieSpecProvider> cookieSpecRegistry = RegistryBuilder.<CookieSpecProvider>create()
                .register(CookieSpecs.DEFAULT, new RFC6265CookieSpecProvider())
                .build();
        RequestConfig requestConfig = RequestConfig.custom()
                .setCookieSpec(CookieSpecs.DEFAULT)
                .build();
        return HttpClientBuilder.create()
                .setDefaultCookieSpecRegistry(cookieSpecRegistry)
                .setDefaultRequestConfig(requestConfig)
                .build();
    }
}

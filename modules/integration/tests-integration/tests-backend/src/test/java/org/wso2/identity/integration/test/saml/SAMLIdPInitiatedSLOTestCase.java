/*
 * Copyright (c) 2018, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
 *
 * WSO2 Inc. licenses this file to you under the Apache License,
 * Version 2.0 (the "License"); you may not use this file except
 * in compliance with the License.
 * You may obtain a copy of the License at
 *
 *    http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied. See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */

package org.wso2.identity.integration.test.saml;

import org.apache.axis2.context.ConfigurationContext;
import org.apache.axis2.context.ConfigurationContextFactory;
import org.apache.catalina.startup.Tomcat;
import org.apache.commons.lang.StringUtils;
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
import org.testng.annotations.Test;
import org.wso2.carbon.automation.engine.frameworkutils.FrameworkPathUtil;
import org.wso2.carbon.identity.application.common.model.xsd.*;
import org.wso2.carbon.identity.sso.saml.stub.types.SAMLSSOServiceProviderDTO;
import org.wso2.carbon.um.ws.api.stub.ClaimValue;
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
import java.util.*;

/**
 * This test class tests SAML IdP initiated SLO functionality for two SAML applications. Since logout requests are sent
 * to every session participant asynchronously, there is no way of validating this from the application side.
 * Therefore debug log for org.wso2.carbon.identity.sso.saml.logout.LogoutRequestSender has to be enabled to check this
 * functionality.
 */
public class SAMLIdPInitiatedSLOTestCase extends ISIntegrationTest {

    private static final Log log = LogFactory.getLog(SAMLIdPInitiatedSLOTestCase.class);

    private static final String APPLICATION_ONE = "SAML-TestApplication-01";
    private static final String APPLICATION_TWO = "SAML-TestApplication-02";
    private static final String INBOUND_AUTH_TYPE = "samlsso";
    private static final String samlAppOneArtifact = "travelocity.com";
    private static final String samlAppTwoArtifact = "travelocity.com-saml-tenantwithoutsigning";
    private static final String ATTRIBUTE_CS_INDEX_VALUE = "1239245949";
    private static final String ATTRIBUTE_CS_INDEX_NAME = "attrConsumServiceIndex";

    private static final String SAML_SSO_URL = "https://localhost:9853/samlsso";
    private static final String SAML_IDP_SLO_URL = SAML_SSO_URL + "?slo=true";
    private static final String ACS_URL = "http://localhost:8490/%s/home.jsp";
    private static final String SAML_SSO_LOGIN_URL = "http://localhost:8490/%s/samlsso?SAML2.HTTPBinding=%s";
    private static final String USER_AGENT = "Apache-HttpClient/4.2.5 (java 1.5)";
    private static final String HTTP_BINDING = "HTTP-POST";
    private static final String COMMON_AUTH_URL = "https://localhost:9853/commonauth";

    private static final String NAMEID_FORMAT = "urn:oasis:names:tc:SAML:1.1:nameid-format:emailAddress";
    private static final String LOGIN_URL = "/carbon/admin/login.jsp";

    public static final String username = "samluser";
    public static final String password = "samluser";
    public static final String tenantDomain = "carbon.super";
    public static final String tenantAwareUsername = "samluser";
    public static final String email = "samluser@abc.com";
    public static final String nickname = "samlusernickname";
    public static final boolean setUserClaims = true;

    private static final String firstNameClaimURI = "http://wso2.org/claims/givenname";
    private static final String lastNameClaimURI = "http://wso2.org/claims/lastname";
    private static final String emailClaimURI = "http://wso2.org/claims/emailaddress";

    private static final String profileName = "default";

    private ApplicationManagementServiceClient applicationManagementServiceClient;
    private SAMLSSOConfigServiceClient ssoConfigServiceClient;
    private SAMLSSOServiceProviderDTO[] samlssoServiceProviderDTOs;
    private RemoteUserStoreManagerServiceClient remoteUserStoreManagerServiceClient;
    private HttpClient httpClient;

    protected SAMLUser user;
    protected Map<String, SAMLApp> applications = new HashMap<>(2);

    private Tomcat tomcatServer;

    private String resultPage;

    private static class SAMLUser {

        private String username;
        private String password;
        private String tenantDomain;
        private String tenantAwareUsername;
        private String email;
        private String nickname;
        private boolean setUserClaims;

        SAMLUser(String username, String password, String tenantDomain, String tenantAwareUsername, String email,
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

    private static class SAMLApp {

        private String artifact;
        private boolean signingEnabled;

        SAMLApp(String artifact, boolean signingEnabled) {
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

    @BeforeClass(alwaysRun = true)
    public void testInit() throws Exception {

        super.init();

        ConfigurationContext configContext = ConfigurationContextFactory
                .createConfigurationContextFromFileSystem(null
                        , null);
        applicationManagementServiceClient =
                new ApplicationManagementServiceClient(sessionCookie, backendURL, configContext);
        ssoConfigServiceClient =
                new SAMLSSOConfigServiceClient(backendURL, sessionCookie);
        remoteUserStoreManagerServiceClient = new RemoteUserStoreManagerServiceClient(backendURL, sessionCookie);

        httpClient = new DefaultHttpClient();

        initUser();
        createUser(user);

        initApplications();
        createApplications();

        log.info("Starting Tomcat");
        tomcatServer = Utils.getTomcat(getClass());
        deployApplications(tomcatServer);
        tomcatServer.start();
    }

    @AfterClass(alwaysRun = true)
    public void testClear() throws Exception{

        deleteUser();
        deleteApplications();

        ssoConfigServiceClient = null;
        applicationManagementServiceClient = null;
        remoteUserStoreManagerServiceClient = null;
        httpClient = null;

        //Stopping tomcat
        tomcatServer.stop();
        tomcatServer.destroy();
        Thread.sleep(10000);
    }

    @Test(description = "Add service providers", groups = "wso2.is", priority = 1)
    public void testAddSP() throws Exception {

        Boolean isAddSuccess;

        for (Map.Entry<String, SAMLApp> entry : applications.entrySet()) {
            isAddSuccess = ssoConfigServiceClient.addServiceProvider(createSsoServiceProviderDTO(entry.getValue()));
            Assert.assertTrue(isAddSuccess, "Adding a service provider has failed for " + entry.getKey());

            samlssoServiceProviderDTOs = ssoConfigServiceClient.getServiceProviders().getServiceProviders();
            Assert.assertEquals(samlssoServiceProviderDTOs[0].getIssuer(), entry.getValue().getArtifact(),
                    "Adding a service provider has failed for " + entry.getKey());
        }
    }

    @Test(alwaysRun = true, description = "Testing SAML SSO login", groups = "wso2.is",
            dependsOnMethods = { "testAddSP" })
    public void testSAMLSSOLogin() {

        try {
            HttpResponse response;

            response = Utils.sendGetRequest(String.format(SAML_SSO_LOGIN_URL, applications.get(APPLICATION_ONE)
                    .getArtifact(), HTTP_BINDING), USER_AGENT, httpClient);
            String samlRequest = Utils.extractDataFromResponse(response, CommonConstants.SAML_REQUEST_PARAM, 5);
            response = sendSAMLMessage(SAML_SSO_URL, CommonConstants.SAML_REQUEST_PARAM, samlRequest);
            EntityUtils.consume(response.getEntity());

            response = Utils.sendRedirectRequest(response, USER_AGENT, ACS_URL, applications.get(APPLICATION_ONE)
                    .getArtifact(), httpClient);

            String sessionKey = Utils.extractDataFromResponse(response, CommonConstants.SESSION_DATA_KEY, 1);
            response = Utils.sendPOSTMessage(sessionKey, SAML_SSO_URL, USER_AGENT, ACS_URL, applications
                    .get(APPLICATION_ONE).getArtifact(), user.getUsername(), user.getPassword(), httpClient);

            if (requestMissingClaims(response)) {
                String pastrCookie = Utils.getPastreCookie(response);
                Assert.assertNotNull(pastrCookie, "pastr cookie not found in response.");
                EntityUtils.consume(response.getEntity());

                response = Utils.sendPOSTConsentMessage(response, COMMON_AUTH_URL, USER_AGENT, String.format(ACS_URL
                        , applications.get(APPLICATION_ONE).getArtifact()), httpClient, pastrCookie);
                EntityUtils.consume(response.getEntity());
            }

            String redirectUrl = Utils.getRedirectUrl(response);
            if(StringUtils.isNotBlank(redirectUrl)) {
                response = Utils.sendRedirectRequest(response, USER_AGENT, ACS_URL, applications.get(APPLICATION_ONE)
                        .getArtifact(), httpClient);
            }

            String samlResponse = Utils.extractDataFromResponse(response, CommonConstants.SAML_RESPONSE_PARAM, 5);
            EntityUtils.consume(response.getEntity());

            response = sendSAMLMessage(String.format(ACS_URL, applications.get(APPLICATION_ONE).getArtifact()),
                    CommonConstants.SAML_RESPONSE_PARAM, samlResponse);
            resultPage = extractDataFromResponse(response);

            Assert.assertTrue(resultPage.contains("You are logged in as " + user.getTenantAwareUsername()),
                    "SAML SSO Login failed for " + applications.get(APPLICATION_ONE).getArtifact());

        } catch (Exception e) {
            Assert.fail("SAML SSO Login test failed for " + applications.get(APPLICATION_ONE).getArtifact(), e);
        }

        doSSOtoAppTwo();
    }

    @Test(alwaysRun = true, description = "Testing SAML IdP initiated SLO", groups = "wso2.is",
            dependsOnMethods = { "testSAMLSSOLogin" })
    public void testSAMLIdpInitiatedSLO() throws Exception {

        try {
            HttpResponse response = Utils.sendGetRequest(SAML_IDP_SLO_URL, USER_AGENT, httpClient);
            String resultPage = extractDataFromResponse(response);

            Assert.assertTrue(resultPage.contains("You have successfully logged out") &&
                    !resultPage.contains("error"), "SAML IdP initiated SLO failed for " +
                    applications.get(APPLICATION_ONE).getArtifact() + " & " +
                    applications.get(APPLICATION_TWO).getArtifact());

            File filePath = new File(FrameworkPathUtil.getCarbonHome() + ISIntegrationTest.URL_SEPARATOR +
                    "repository" + ISIntegrationTest.URL_SEPARATOR + "logs" + ISIntegrationTest.URL_SEPARATOR +
                    "wso2carbon.log");
            Scanner scanner = new Scanner(filePath);

            while (scanner.hasNextLine()) {
                String line = scanner.nextLine();
                if (line.contains("DEBUG")) {
                    if (line.contains("single logout request is sent to : http://localhost:8490/travelocity.com/home.jsp is returned with OK")) {
                        Assert.assertEquals("single logout request is sent to : " +
                                        samlssoServiceProviderDTOs[0].getAssertionConsumerUrls()[0] + " is returned with OK",
                                "single logout request is sent to : http://localhost:8490/travelocity.com/home.jsp is returned with OK");
                    } else if (line.contains("single logout request is sent to : http://localhost:8490/travelocity.com-saml-tenantwithoutsigning/home.jsp " +
                            "is returned with OK")) {
                        Assert.assertEquals("single logout request is sent to : "+
                                        samlssoServiceProviderDTOs[1].getAssertionConsumerUrls()[0] +" is returned with OK",
                                "single logout request is sent to : http://localhost:8490/travelocity.com-saml-tenantwithoutsigning/home.jsp " +
                                        "is returned with OK");
                    } else if (line.contains("Logout response received for issuer: travelocity.com-saml-tenantwithoutsigning " +
                            "for tenant domain: carbon.super")) {
                        Assert.assertEquals("Logout response received for issuer: " +
                                        samlssoServiceProviderDTOs[1].getIssuer() + " for tenant domain: " + tenantDomain,
                                "Logout response received for issuer: travelocity.com-saml-tenantwithoutsigning for tenant domain: carbon.super");
                    } else if (line.contains("Logout response received for issuer: travelocity.com for tenant domain: carbon.super")) {
                        Assert.assertEquals("Logout response received for issuer: " +
                                        samlssoServiceProviderDTOs[0].getIssuer() + " for tenant domain: " + tenantDomain,
                                "Logout response received for issuer: travelocity.com for tenant domain: carbon.super");
                    }
                }
            }
        } catch (Exception e) {
            Assert.fail("SAML IdP initiated SLO test failed for " + applications.get(APPLICATION_ONE).
                    getArtifact() + " & " + applications.get(APPLICATION_TWO).getArtifact(), e);
        }
    }

    protected void initUser() throws Exception {

        user = new SAMLUser(username, password, tenantDomain, tenantAwareUsername, email, nickname, setUserClaims);
    }

    protected void createUser(SAMLUser user) {

        log.info("Creating user " + user.getUsername());
        try {
            remoteUserStoreManagerServiceClient.addUser(user.getTenantAwareUsername(), user.getPassword(), null,
                    getUserClaims(user.getSetUserClaims()), profileName, true);
        } catch (Exception e) {
            Assert.fail("Error while creating the user", e);
        }
    }

    protected void deleteUser() {

        log.info("Deleting User " + user.getUsername());
        try {
            remoteUserStoreManagerServiceClient.deleteUser(user.getTenantAwareUsername());
        } catch (Exception e) {
            Assert.fail("Error while deleting the user", e);
        }
    }

    protected ClaimValue[] getUserClaims(boolean setClaims){

        ClaimValue[] claimValues;

        if (setClaims) {
            claimValues = new ClaimValue[3];

            ClaimValue firstName = new ClaimValue();
            firstName.setClaimURI(firstNameClaimURI);
            firstName.setValue(user.getNickname());
            claimValues[0] = firstName;

            ClaimValue lastName = new ClaimValue();
            lastName.setClaimURI(lastNameClaimURI);
            lastName.setValue(user.getUsername());
            claimValues[1] = lastName;

            ClaimValue email = new ClaimValue();
            email.setClaimURI(emailClaimURI);
            email.setValue(user.getEmail());
            claimValues[2] = email;
        } else {
            claimValues = new ClaimValue[1];

            ClaimValue lastName = new ClaimValue();
            lastName.setClaimURI(lastNameClaimURI);
            lastName.setValue(user.getUsername());
            claimValues[0] = lastName;
        }

        return claimValues;
    }

    protected void initApplications() throws Exception {

        SAMLApp samlApp = new SAMLApp(samlAppOneArtifact, true);
        applications.put(APPLICATION_ONE, samlApp);

        samlApp = new SAMLApp(samlAppTwoArtifact, true);
        applications.put(APPLICATION_TWO, samlApp);
    }

    protected void createApplications() throws Exception {

        for (Map.Entry<String, SAMLApp> entry : applications.entrySet()) {
            log.info("Creating application " + entry.getKey());

            ServiceProvider serviceProvider = new ServiceProvider();
            serviceProvider.setApplicationName(entry.getKey());
            serviceProvider.setDescription("This is a test Service Provider");
            applicationManagementServiceClient.createApplication(serviceProvider);

            serviceProvider = applicationManagementServiceClient.getApplication(entry.getKey());
            serviceProvider.getClaimConfig().setClaimMappings(getClaimMappings());

            InboundAuthenticationRequestConfig requestConfig = new InboundAuthenticationRequestConfig();
            requestConfig.setInboundAuthType(INBOUND_AUTH_TYPE);
            requestConfig.setInboundAuthKey(entry.getValue().getArtifact());

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
    }

    protected void deleteApplications() throws Exception {

        for (Map.Entry<String, SAMLApp> entry : applications.entrySet()) {
            log.info("Deleting application " + entry.getKey());
            applicationManagementServiceClient.deleteApplication(entry.getKey());
        }
    }

    protected ClaimMapping[] getClaimMappings() {

        List<ClaimMapping> claimMappingList = new ArrayList<ClaimMapping>();

        Claim firstNameClaim = new Claim();
        firstNameClaim.setClaimUri(firstNameClaimURI);
        ClaimMapping firstNameClaimMapping = new ClaimMapping();
        firstNameClaimMapping.setRequested(true);
        firstNameClaimMapping.setLocalClaim(firstNameClaim);
        firstNameClaimMapping.setRemoteClaim(firstNameClaim);
        claimMappingList.add(firstNameClaimMapping);

        Claim lastNameClaim = new Claim();
        lastNameClaim.setClaimUri(lastNameClaimURI);
        ClaimMapping lastNameClaimMapping = new ClaimMapping();
        lastNameClaimMapping.setRequested(true);
        lastNameClaimMapping.setLocalClaim(lastNameClaim);
        lastNameClaimMapping.setRemoteClaim(lastNameClaim);
        claimMappingList.add(lastNameClaimMapping);

        Claim emailClaim = new Claim();
        emailClaim.setClaimUri(emailClaimURI);
        ClaimMapping emailClaimMapping = new ClaimMapping();
        emailClaimMapping.setRequested(true);
        emailClaimMapping.setLocalClaim(emailClaim);
        emailClaimMapping.setRemoteClaim(emailClaim);
        claimMappingList.add(emailClaimMapping);

        return claimMappingList.toArray(new ClaimMapping[claimMappingList.size()]);
    }

    protected void deployApplications(Tomcat tomcatServer) {

        for (Map.Entry<String, SAMLApp> entry : applications.entrySet()) {
            URL resourceUrl = getClass().getResource(ISIntegrationTest.URL_SEPARATOR + "samples" +
                    ISIntegrationTest.URL_SEPARATOR + entry.getValue().getArtifact() + ".war");
            tomcatServer.addWebapp(tomcatServer.getHost(), "/" + entry.getValue().getArtifact(), resourceUrl.
                    getPath());
        }
    }

    protected SAMLSSOServiceProviderDTO createSsoServiceProviderDTO(SAMLApp samlApp) {

        SAMLSSOServiceProviderDTO samlssoServiceProviderDTO = new SAMLSSOServiceProviderDTO();
        samlssoServiceProviderDTO.setIssuer(samlApp.getArtifact());
        samlssoServiceProviderDTO.setAssertionConsumerUrls(new String[] {String.format(ACS_URL,
                samlApp.getArtifact())});
        samlssoServiceProviderDTO.setDefaultAssertionConsumerUrl(String.format(ACS_URL, samlApp.getArtifact()));
        samlssoServiceProviderDTO.setAttributeConsumingServiceIndex(ATTRIBUTE_CS_INDEX_VALUE);
        samlssoServiceProviderDTO.setNameIDFormat(NAMEID_FORMAT);
        samlssoServiceProviderDTO.setDoSignAssertions(samlApp.isSigningEnabled());
        samlssoServiceProviderDTO.setDoSignResponse(samlApp.isSigningEnabled());
        samlssoServiceProviderDTO.setDoSingleLogout(true);
        samlssoServiceProviderDTO.setLoginPageURL(LOGIN_URL);
        samlssoServiceProviderDTO.setEnableAttributeProfile(true);
        samlssoServiceProviderDTO.setEnableAttributesByDefault(true);

        return samlssoServiceProviderDTO;
    }

    protected HttpResponse sendSAMLMessage(String url, String samlMsgKey, String samlMsgValue) throws IOException {

        List<NameValuePair> urlParameters = new ArrayList<NameValuePair>();
        HttpPost post = new HttpPost(url);
        post.setHeader("User-Agent", USER_AGENT);
        urlParameters.add(new BasicNameValuePair(samlMsgKey, samlMsgValue));
        post.setEntity(new UrlEncodedFormEntity(urlParameters));
        return httpClient.execute(post);
    }

    protected boolean requestMissingClaims (HttpResponse response) {

        String redirectUrl = Utils.getRedirectUrl(response);
        return redirectUrl.contains("consent.do");
    }

    protected String extractDataFromResponse(HttpResponse response) throws IOException {

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

    protected void doSSOtoAppTwo() {

        try {
            HttpResponse response;

            response = Utils.sendGetRequest(String.format(SAML_SSO_LOGIN_URL, applications.get(APPLICATION_TWO)
                    .getArtifact(), HTTP_BINDING), USER_AGENT, httpClient);
            String samlRequest = Utils.extractDataFromResponse(response, CommonConstants.SAML_REQUEST_PARAM, 5);
            response = sendSAMLMessage(SAML_SSO_URL, CommonConstants.SAML_REQUEST_PARAM, samlRequest);

            if (requestMissingClaims(response)) {
                String pastrCookie = Utils.getPastreCookie(response);
                Assert.assertNotNull(pastrCookie, "pastr cookie not found in response.");
                EntityUtils.consume(response.getEntity());
                response = Utils.sendPOSTConsentMessage(response, COMMON_AUTH_URL, USER_AGENT, String.format(ACS_URL
                        , applications.get(APPLICATION_TWO).getArtifact()), httpClient, pastrCookie);
                EntityUtils.consume(response.getEntity());
            }

            String redirectUrl = Utils.getRedirectUrl(response);
            if(StringUtils.isNotBlank(redirectUrl)) {
                response = Utils.sendRedirectRequest(response, USER_AGENT, ACS_URL, applications.get(APPLICATION_TWO)
                        .getArtifact(), httpClient);
            }

            String samlResponse = Utils.extractDataFromResponse(response, CommonConstants.SAML_RESPONSE_PARAM, 5);
            EntityUtils.consume(response.getEntity());
            response = sendSAMLMessage(String.format(ACS_URL, applications.get(APPLICATION_TWO).getArtifact()),
                    CommonConstants.SAML_RESPONSE_PARAM, samlResponse);
            resultPage = extractDataFromResponse(response);

            Assert.assertTrue(resultPage.contains("You are logged in as " + user.getTenantAwareUsername()),
                    "SAML SSO Login failed for " + applications.get(APPLICATION_TWO).getArtifact());
        } catch (Exception e) {
            Assert.fail("SAML SSO Login test failed for " + applications.get(APPLICATION_TWO).getArtifact(), e);
        }
    }

}

/*
*Copyright (c) 2005-2010, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
*
*WSO2 Inc. licenses this file to you under the Apache License,
*Version 2.0 (the "License"); you may not use this file except
*in compliance with the License.
*You may obtain a copy of the License at
*
*http://www.apache.org/licenses/LICENSE-2.0
*
*Unless required by applicable law or agreed to in writing,
*software distributed under the License is distributed on an
*"AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
*KIND, either express or implied.  See the License for the
*specific language governing permissions and limitations
*under the License.
*/
package org.wso2.identity.integration.test.saml;


import org.apache.axis2.context.ConfigurationContext;
import org.apache.axis2.context.ConfigurationContextFactory;
import org.apache.catalina.LifecycleException;
import org.apache.catalina.core.StandardHost;
import org.apache.catalina.startup.Tomcat;
import org.apache.commons.lang.StringUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.http.*;
import org.apache.http.client.HttpClient;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.message.BasicNameValuePair;
import org.apache.http.util.EntityUtils;
import org.testng.Assert;
import org.testng.annotations.*;
import org.wso2.carbon.identity.sso.saml.stub.types.SAMLSSOServiceProviderDTO;
import org.wso2.identity.integration.common.clients.application.mgt.ApplicationManagementServiceClient;
import org.wso2.identity.integration.common.clients.sso.saml.SAMLSSOConfigServiceClient;
import org.wso2.identity.integration.common.clients.usermgt.remote.RemoteUserStoreManagerServiceClient;
import org.wso2.carbon.identity.application.common.model.xsd.*;
import org.wso2.identity.integration.common.utils.ISIntegrationTest;
import org.wso2.carbon.um.ws.api.stub.ClaimValue;

import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.URL;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class SAMLSSOTestCase extends ISIntegrationTest {

    private static final Log log = LogFactory.getLog(SAMLSSOTestCase.class);

    // SAML Application attributes
    private static final String USER_AGENT = "Apache-HttpClient/4.2.5 (java 1.5)";
    private static final String ISSUER_NAME = "travelocity.com";
    private static final String APPLICATION_NAME = "SAML-SSO-TestApplication";
    private static final String INBOUND_AUTH_TYPE = "samlsso";
    private static final String ATTRIBUTE_CS_INDEX_VALUE = "1239245949";
    private static final String ATTRIBUTE_CS_INDEX_NAME = "attrConsumServiceIndex";

    // User Attributes
    private static final String USERNAME = "testUser";
    private static final String PASSWORD = "testUser";
    private static final String EMAIL = "testUser@wso2.com";
    private static final String NICKNAME = "testUserNick";

    private static final String SAML_SSO_URL = "https://localhost:9443/samlsso";
    private static final String ACS_URL = "http://localhost:8080/travelocity.com/home.jsp";
    private static final String COMMON_AUTH_URL = "https://localhost:9443/commonauth";
    private static final String SAML_SSO_LOGIN_URL =
            "http://localhost:8080/travelocity.com/samlsso?SAML2.HTTPBinding=%s";
    private static final String SAML_SSO_LOGOUT_URL =
            "http://localhost:8080/travelocity.com/logout?SAML2.HTTPBinding=%s";

    private static final String NAMEID_FORMAT =
            "urn:oasis:names:tc:SAML:1.1:nameid-format:emailAddress";
    private static final String LOGIN_URL = "/carbon/admin/login.jsp";

    //Claim Uris
    private static final String firstNameClaimURI = "http://wso2.org/claims/givenname";
    private static final String lastNameClaimURI = "http://wso2.org/claims/lastname";
    private static final String emailClaimURI = "http://wso2.org/claims/emailaddress";

    private static final String profileName = "default";

    private ApplicationManagementServiceClient applicationManagementServiceClient;
    private SAMLSSOConfigServiceClient ssoConfigServiceClient;
    private RemoteUserStoreManagerServiceClient remoteUSMServiceClient;
    private HttpClient httpClient;
    private SAMLConfig config;
    private Tomcat tomcatServer;

    private String resultPage;

    private enum HttpBinding{
        HTTP_REDIRECT("HTTP-Redirect"),
        HTTP_POST("HTTP-POST");

        String binding;

        HttpBinding(String binding) {
            this.binding = binding;
        }
    }

    private enum ClaimType{
        LOCAL, CUSTOM, NONE
    }

    private static class SAMLConfig{
        private HttpBinding httpBinding;
        private ClaimType claimType;

        private SAMLConfig(HttpBinding httpBinding, ClaimType claimType) {
            this.httpBinding = httpBinding;
            this.claimType = claimType;
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
                    "httpBinding=" + httpBinding +
                    ", claimType=" + claimType +
                    ']';
        }
    }

    @Factory(dataProvider = "samlConfigProvider")
    public SAMLSSOTestCase(SAMLConfig config) {
        if (log.isDebugEnabled()){
            log.info("SAML SSO Test initialized for " + config);
        }
        this.config = config;
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
        remoteUSMServiceClient = new RemoteUserStoreManagerServiceClient(backendURL, sessionCookie);

        httpClient = new DefaultHttpClient();

        createUser();
        createApplication();

        //Starting tomcat
        log.info("Starting Tomcat");
        tomcatServer = getTomcat();

        //TODO: Uncomment below once the tomcat dependency issue is resolved
//        URL resourceUrl = getClass()
//                .getResource(File.separator + "samples" + File.separator + "org.wso2.sample.is .sso.agent.war");
        URL resourceUrl = getClass().getResource(File.separator + "samples" + File.separator + "travelocity.com.war");
        startTomcat(tomcatServer, "/travelocity.com", resourceUrl.getPath());

    }

    @AfterClass(alwaysRun = true)
    public void testClear() throws Exception{
        deleteUser();
        deleteApplication();

        ssoConfigServiceClient = null;
        applicationManagementServiceClient = null;
        remoteUSMServiceClient = null;
        httpClient = null;
        //Stopping tomcat
        tomcatServer.stop();
        tomcatServer.destroy();
    }

    @Test(description = "Add service provider", groups = "wso2.is", priority = 1)
    public void testAddSP() throws Exception {
        Boolean isAddSuccess = ssoConfigServiceClient
                .addServiceProvider(createSsoServiceProviderDTO());
        Assert.assertTrue(isAddSuccess, "Adding a service provider has failed for " + config);

        SAMLSSOServiceProviderDTO[] samlssoServiceProviderDTOs = ssoConfigServiceClient
                .getServiceProviders().getServiceProviders();
        Assert.assertEquals(samlssoServiceProviderDTOs[0].getIssuer(), ISSUER_NAME,
                "Adding a service provider has failed for " + config);
    }

    @Test(description = "Remove service provider", groups = "wso2.is", dependsOnMethods = { "testSAMLSSOLogout" })
    public void testRemoveSP()
            throws Exception {
        Boolean isAddSuccess = ssoConfigServiceClient.removeServiceProvider(ISSUER_NAME);
        Assert.assertTrue(isAddSuccess, "Removing a service provider has failed for " + config);
    }

    @Test(alwaysRun = true, description = "Testing SAML SSO login", groups = "wso2.is",
            dependsOnMethods = { "testAddSP" })
    public void testSAMLSSOLogin() {
        try {
            HttpResponse response;

            response = sendGetRequest(String.format(SAML_SSO_LOGIN_URL, config.getHttpBinding().binding));

            if (config.getHttpBinding() == HttpBinding.HTTP_POST){
                String samlRequest = extractDataFromResponse(response, "SAMLRequest", 5);
                response = sendSAMLMessage(SAML_SSO_URL, "SAMLRequest", samlRequest);
                EntityUtils.consume(response.getEntity());

                response = sendRedirectRequest(response);
            }

            String sessionKey = extractDataFromResponse(response, "name=\"sessionDataKey\"", 1);
            response = sendPOSTMessage(sessionKey);
            EntityUtils.consume(response.getEntity());

            response = sendRedirectRequest(response);
            String samlResponse = extractDataFromResponse(response, "SAMLResponse", 5);

            response = sendSAMLMessage(ACS_URL, "SAMLResponse", samlResponse);
            resultPage = extractDataFromResponse(response);

            Assert.assertTrue(resultPage.contains("You are logged in as " + USERNAME),
                    "SAML SSO Login failed for " + config);
        } catch (Exception e) {
            Assert.fail("SAML SSO Login test failed for " + config, e);
        }
    }

    @Test(alwaysRun = true, description = "Testing SAML SSO Claims", groups = "wso2.is",
            dependsOnMethods = { "testSAMLSSOLogin" })
    public void testClaims(){
        String claimString = resultPage.substring(resultPage.lastIndexOf("<table>"));

        switch (config.getClaimType()){
            case LOCAL:
                assertLocalClaims(claimString);
                break;
            case NONE:
                assertNoneClaims(claimString);
                break;
        }
    }

    @Test(alwaysRun = true, description = "Testing SAML SSO logout", groups = "wso2.is",
            dependsOnMethods = { "testSAMLSSOLogin" })
    public void testSAMLSSOLogout() throws Exception {
        try {
            HttpResponse response;

            response = sendGetRequest(String.format(SAML_SSO_LOGOUT_URL, config.getHttpBinding().binding));

            if (config.getHttpBinding() == HttpBinding.HTTP_POST){
                String samlRequest = extractDataFromResponse(response, "SAMLRequest", 5);
                response = sendSAMLMessage(SAML_SSO_URL, "SAMLRequest", samlRequest);
                EntityUtils.consume(response.getEntity());

                response = sendRedirectRequest(response);
            }

            String samlResponse = extractDataFromResponse(response, "SAMLResponse", 5);
            response = sendSAMLMessage(ACS_URL, "SAMLResponse", samlResponse);
            String resultPage = extractDataFromResponse(response);

            Assert.assertTrue(resultPage.contains("index.jsp") && !resultPage.contains("error"),
                    "SAML SSO Logout failed for " + config);
        } catch (Exception e) {
            Assert.fail("SAML SSO Logout test failed for " + config, e);
        }
    }

    @DataProvider(name = "samlConfigProvider")
    public static SAMLConfig[][] samlConfigProvider(){
        return  new SAMLConfig[][]{
                {new SAMLConfig(HttpBinding.HTTP_REDIRECT, ClaimType.NONE)},
                {new SAMLConfig(HttpBinding.HTTP_REDIRECT, ClaimType.LOCAL)},
                {new SAMLConfig(HttpBinding.HTTP_POST, ClaimType.NONE)},
                {new SAMLConfig(HttpBinding.HTTP_POST, ClaimType.LOCAL)},
        };
    }

    private void assertLocalClaims(String claims){
        Map<String, String> attributeMap = extractClaims(claims);
        Assert.assertTrue(attributeMap.containsKey(firstNameClaimURI), "Claim nickname is expected");
        Assert.assertEquals(attributeMap.get(firstNameClaimURI), NICKNAME,
                "Expected claim value for nickname is " + NICKNAME);
        Assert.assertTrue(attributeMap.containsKey(lastNameClaimURI), "Claim lastname is expected");
        Assert.assertEquals(attributeMap.get(lastNameClaimURI), USERNAME,
                "Expected claim value for lastname is " + USERNAME);
        Assert.assertTrue(attributeMap.containsKey(emailClaimURI), "Claim email is expected");
        Assert.assertEquals(attributeMap.get(emailClaimURI), EMAIL,
                "Expected claim value for email is " + EMAIL);
    }

    private void assertNoneClaims(String claims){
        String[] dataArray = StringUtils.substringsBetween(claims, "<td>", "</td>");
        Assert.assertNull(dataArray, "Claims are not expected for " + config);
    }

    private void startTomcat(Tomcat tomcat, String webAppUrl, String webAppPath)
            throws LifecycleException {
        tomcat.addWebapp(tomcat.getHost(), webAppUrl, webAppPath);
        tomcat.start();
    }

    private Tomcat getTomcat() {
        Tomcat tomcat = new Tomcat();
        tomcat.getService().setContainer(tomcat.getEngine());
        tomcat.setPort(8080);
        tomcat.setBaseDir("");

        StandardHost stdHost = (StandardHost) tomcat.getHost();

        stdHost.setAppBase("");
        stdHost.setAutoDeploy(true);
        stdHost.setDeployOnStartup(true);
        stdHost.setUnpackWARs(true);
        tomcat.setHost(stdHost);

        setSystemProperties();
        return tomcat;
    }

    private void setSystemProperties() {
        URL resourceUrl = getClass().getResource(File.separator + "keystores" + File.separator
                + "products" + File.separator + "wso2carbon.jks");
        System.setProperty("javax.net.ssl.trustStore", resourceUrl.getPath());
        System.setProperty("javax.net.ssl.trustStorePassword",
                "wso2carbon");
        System.setProperty("javax.net.ssl.trustStoreType", "JKS");
    }

    private String extractDataFromResponse(HttpResponse response, String key, int token)
            throws IOException {
        BufferedReader rd = new BufferedReader(
                new InputStreamReader(response.getEntity().getContent()));
        String line;
        String value = "";

        while ((line = rd.readLine()) != null) {
            if (line.contains(key)) {
                String[] tokens = line.split("'");
                value = tokens[token];
            }
        }
        rd.close();
        return value;
    }

    private HttpResponse sendPOSTMessage(String sessionKey) throws Exception {
        HttpPost post = new HttpPost(COMMON_AUTH_URL);
        post.setHeader("User-Agent", USER_AGENT);
        post.addHeader("Referer", ACS_URL);
        List<NameValuePair> urlParameters = new ArrayList<NameValuePair>();
        urlParameters.add(new BasicNameValuePair("username", USERNAME));
        urlParameters.add(new BasicNameValuePair("password", PASSWORD));
        urlParameters.add(new BasicNameValuePair("sessionDataKey", sessionKey));
        post.setEntity(new UrlEncodedFormEntity(urlParameters));
        return httpClient.execute(post);
    }

    private HttpResponse sendGetRequest(String url) throws Exception {
        HttpGet request = new HttpGet(url);
        request.addHeader("User-Agent", USER_AGENT);
        return httpClient.execute(request);
    }

    private HttpResponse sendSAMLMessage(String url, String samlMsgKey, String samlMsgValue) throws IOException {
        List<NameValuePair> urlParameters = new ArrayList<NameValuePair>();
        HttpPost post = new HttpPost(url);
        post.setHeader("User-Agent", USER_AGENT);
        urlParameters.add(new BasicNameValuePair(samlMsgKey, samlMsgValue));
        post.setEntity(new UrlEncodedFormEntity(urlParameters));
        return httpClient.execute(post);
    }

    private HttpResponse sendRedirectRequest(HttpResponse response) throws IOException {
        Header[] headers = response.getAllHeaders();
        String url = "";
        for (Header header : headers) {
            if ("Location".equals(header.getName())) {
                url = header.getValue();
            }
        }

        HttpGet request = new HttpGet(url);
        request.addHeader("User-Agent", USER_AGENT);
        request.addHeader("Referer", ACS_URL);
        return httpClient.execute(request);
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

    private Map<String,String> extractClaims(String claimString){
        String[] dataArray = StringUtils.substringsBetween(claimString, "<td>", "</td>");
        Map<String,String> attributeMap = new HashMap<String, String>();
        String key = null;
        String value;
        for (int i = 0; i< dataArray.length; i++){
            if((i%2) == 0){
                key = dataArray[i];
            }else{
                value = dataArray[i].trim();
                attributeMap.put(key,value);
            }
        }

        return attributeMap;
    }

    private void createApplication() throws Exception{
        ServiceProvider serviceProvider = new ServiceProvider();
        serviceProvider.setApplicationName(APPLICATION_NAME);
        serviceProvider.setDescription("This is a test Service Provider");
        applicationManagementServiceClient.createApplication(serviceProvider);

        serviceProvider = applicationManagementServiceClient.getApplication(APPLICATION_NAME);

        serviceProvider.getClaimConfig().setClaimMappings(getClaimMappings());

        InboundAuthenticationRequestConfig requestConfig = new InboundAuthenticationRequestConfig();
        requestConfig.setInboundAuthType(INBOUND_AUTH_TYPE);
        requestConfig.setInboundAuthKey(ISSUER_NAME);

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

    private void deleteApplication() throws Exception{
        applicationManagementServiceClient.deleteApplication(APPLICATION_NAME);
    }

    private void createUser(){
        log.info("Creating User " + USERNAME);
        try {
            // creating the user
            remoteUSMServiceClient.addUser(USERNAME, PASSWORD,
                    null, getUserClaims(),
                    profileName, true);
        } catch (Exception e) {
            Assert.fail("Error while creating the user", e);
        }

    }

    private void deleteUser(){
        log.info("Deleting User " + USERNAME);
        try {
            remoteUSMServiceClient.deleteUser(USERNAME);
        } catch (Exception e) {
            Assert.fail("Error while deleting the user", e);
        }
    }

    private SAMLSSOServiceProviderDTO createSsoServiceProviderDTO() {
        SAMLSSOServiceProviderDTO samlssoServiceProviderDTO = new SAMLSSOServiceProviderDTO();
        samlssoServiceProviderDTO.setIssuer(ISSUER_NAME);
        samlssoServiceProviderDTO.setAssertionConsumerUrl(ACS_URL);
        samlssoServiceProviderDTO.setAttributeConsumingServiceIndex(ATTRIBUTE_CS_INDEX_VALUE);
        samlssoServiceProviderDTO.setNameIDFormat(NAMEID_FORMAT);
        samlssoServiceProviderDTO.setDoSignAssertions(true);
        samlssoServiceProviderDTO.setDoSignResponse(true);
        samlssoServiceProviderDTO.setDoSingleLogout(true);
        samlssoServiceProviderDTO.setLoginPageURL(LOGIN_URL);
        if (config.getClaimType() != ClaimType.NONE){
            samlssoServiceProviderDTO.setEnableAttributesByDefault(true);
        }

        return samlssoServiceProviderDTO;
    }

    private ClaimMapping[] getClaimMappings(){
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

    private ClaimValue[] getUserClaims(){
        ClaimValue[] claimValues = new ClaimValue[3];

        ClaimValue firstName = new ClaimValue();
        firstName.setClaimURI(firstNameClaimURI);
        firstName.setValue(NICKNAME);
        claimValues[0] = firstName;

        ClaimValue lastName = new ClaimValue();
        lastName.setClaimURI(lastNameClaimURI);
        lastName.setValue(USERNAME);
        claimValues[1] = lastName;

        ClaimValue email = new ClaimValue();
        email.setClaimURI(emailClaimURI);
        email.setValue(EMAIL);
        claimValues[2] = email;

        return claimValues;
    }

}

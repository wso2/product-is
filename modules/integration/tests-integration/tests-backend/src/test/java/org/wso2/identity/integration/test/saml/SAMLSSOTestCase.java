/*
 * Copyright (c) 2015, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
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

import org.apache.commons.codec.binary.Base64;
import org.apache.commons.lang.StringUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.http.HttpResponse;
import org.apache.http.util.EntityUtils;
import org.testng.Assert;
import org.testng.annotations.AfterClass;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.DataProvider;
import org.testng.annotations.Factory;
import org.testng.annotations.Test;
import org.wso2.carbon.automation.engine.context.TestUserMode;
import org.wso2.carbon.identity.sso.saml.stub.types.SAMLSSOServiceProviderDTO;
import org.wso2.identity.integration.test.util.Utils;
import org.wso2.identity.integration.test.utils.CommonConstants;
import org.wso2.identity.integration.test.utils.UserUtil;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.HashMap;
import java.util.Map;

public class SAMLSSOTestCase extends AbstractSAMLSSOTestCase {

    private static final Log log = LogFactory.getLog(SAMLSSOTestCase.class);

    // SAML Application attributes
    private static final String APPLICATION_NAME = "SAML-SSO-TestApplication";

    private static final String SAML_SSO_INDEX_URL = "http://localhost:8490/%s/";
    private static final String SAML_SSO_LOGOUT_URL =
            "http://localhost:8490/%s/logout?SAML2.HTTPBinding=%s";

    //Claim Uris
    private static final String firstNameClaimURI = "http://wso2.org/claims/givenname";
    private static final String lastNameClaimURI = "http://wso2.org/claims/lastname";
    private static final String emailClaimURI = "http://wso2.org/claims/emailaddress";

    private SAMLConfig config;
    private String userId;

    private String resultPage;

    @Factory(dataProvider = "samlConfigProvider")
    public SAMLSSOTestCase(SAMLConfig config) {
        if (log.isDebugEnabled()){
            log.info("SAML SSO Test initialized for " + config);
        }
        this.config = config;
    }

    @BeforeClass(alwaysRun = true)
    public void testInit() throws Exception {
        super.init(config.getUserMode());

        super.testInit();
        super.createUser(config);
        userId = UserUtil.getUserId(config.getUser().getTenantAwareUsername(), isServer.getContextTenant());

        super.createApplication(config, APPLICATION_NAME);
    }

    @AfterClass(alwaysRun = true)
    public void testClear() throws Exception{

        super.deleteUser(config);
        super.deleteApplication(APPLICATION_NAME);

        super.testClear();
    }

    @Test(description = "Add service provider", groups = "wso2.is", priority = 1)
    public void testAddSP() throws Exception {
        Boolean isAddSuccess = ssoConfigServiceClient.addServiceProvider(super.createSsoServiceProviderDTO(config));
        Assert.assertTrue(isAddSuccess, "Adding a service provider has failed for " + config);

        SAMLSSOServiceProviderDTO[] samlssoServiceProviderDTOs = ssoConfigServiceClient
                .getServiceProviders().getServiceProviders();
        Assert.assertEquals(samlssoServiceProviderDTOs[0].getIssuer(), config.getApp().getArtifact(),
                            "Adding a service provider has failed for " + config);
    }

    @Test(description = "Remove service provider", groups = "wso2.is", dependsOnMethods = { "testSAMLSSOIdPLogout" })
    public void testRemoveSP()
            throws Exception {
        Boolean isAddSuccess = ssoConfigServiceClient.removeServiceProvider(config.getApp().getArtifact());
        Assert.assertTrue(isAddSuccess, "Removing a service provider has failed for " + config);
    }

    @Test(alwaysRun = true, description = "Testing SAML SSO login", groups = "wso2.is",
            dependsOnMethods = { "testAddSP" })
    public void testSAMLSSOIsPassiveLogin() {
        try {
            HttpResponse response;
            response = Utils.sendGetRequest(String.format(SAML_SSO_INDEX_URL, config.getApp().getArtifact(), config
                    .getHttpBinding().binding), USER_AGENT, httpClient);
            String samlResponse = Utils.extractDataFromResponse(response, "name='SAMLResponse'", 5);
            samlResponse = new String(Base64.decodeBase64(samlResponse));
            Assert.assertTrue(samlResponse.contains("Destination=\"" + String.format(ACS_URL, config.getApp()
                    .getArtifact()) + "\""));
        } catch (Exception e) {
            Assert.fail("SAML SSO Login test failed for " + config, e);
        }
    }

    @Test(alwaysRun = true, description = "Testing SAML SSO login", groups = "wso2.is",
          dependsOnMethods = { "testSAMLSSOIsPassiveLogin" })
    public void testSAMLSSOLogin() {
        try {
            HttpResponse response;

            response = Utils.sendGetRequest(String.format(SAML_SSO_LOGIN_URL, config.getApp().getArtifact(), config
                    .getHttpBinding().binding), USER_AGENT, httpClient);

            if (config.getHttpBinding() == HttpBinding.HTTP_POST){
                String samlRequest = Utils.extractDataFromResponse(response, CommonConstants.SAML_REQUEST_PARAM, 5);
                response = super.sendSAMLMessage(SAML_SSO_URL, CommonConstants.SAML_REQUEST_PARAM, samlRequest, config);
                EntityUtils.consume(response.getEntity());

                response = Utils.sendRedirectRequest(response, USER_AGENT, ACS_URL, config.getApp().getArtifact(),
                        httpClient);
            }

            String sessionKey = Utils.extractDataFromResponse(response, CommonConstants.SESSION_DATA_KEY, 1);
            response = Utils.sendPOSTMessage(sessionKey, getTenantQualifiedURL(SAML_SSO_URL,
                    config.getUser().getTenantDomain()), USER_AGENT, ACS_URL, config.getApp()
                    .getArtifact(), config.getUser().getTenantAwareUsername(), config.getUser().getPassword(),
                    httpClient, getTenantQualifiedURL(SAML_SSO_URL, config.getUser().getTenantDomain()));


            if (requestMissingClaims(response)) {
                String pastrCookie = Utils.getPastreCookie(response);
                Assert.assertNotNull(pastrCookie, "pastr cookie not found in response.");
                EntityUtils.consume(response.getEntity());

                response = Utils.sendPOSTConsentMessage(response, COMMON_AUTH_URL, USER_AGENT, String.format(ACS_URL, config.getApp()
                        .getArtifact()), httpClient, pastrCookie);
                EntityUtils.consume(response.getEntity());
            }

            String redirectUrl = Utils.getRedirectUrl(response);
            if (StringUtils.isNotBlank(redirectUrl)) {
                response = Utils.sendRedirectRequest(response, USER_AGENT, ACS_URL, config.getApp().getArtifact(),
                        httpClient);
            }
            String samlResponse = Utils.extractDataFromResponse(response, CommonConstants.SAML_RESPONSE_PARAM, 5);
            EntityUtils.consume(response.getEntity());

            response = super.sendSAMLMessage(String.format(ACS_URL, config.getApp().getArtifact()), CommonConstants
                    .SAML_RESPONSE_PARAM, samlResponse, config);
            resultPage = extractDataFromResponse(response);

            Assert.assertTrue(resultPage.contains("You are logged in as " + userId),
                              "SAML SSO Login failed for " + config);
        } catch (Exception e) {
            Assert.fail("SAML SSO Login test failed for " + config, e);
        }
    }

    @Test(alwaysRun = true, description = "Testing SAML SSO Claims", groups = "wso2.is",
          dependsOnMethods = { "testSAMLSSOLogin" })
    public void testClaims() {
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

            response = Utils.sendGetRequest(String.format(SAML_SSO_LOGOUT_URL, config.getApp().getArtifact(), config
                    .getHttpBinding().binding), USER_AGENT, httpClient);

            if (config.getHttpBinding() == HttpBinding.HTTP_POST){
                String samlRequest = Utils.extractDataFromResponse(response, CommonConstants.SAML_REQUEST_PARAM, 5);
                response = super.sendSAMLMessage(SAML_SSO_URL, CommonConstants.SAML_REQUEST_PARAM, samlRequest, config);
            }

            String samlResponse = Utils.extractDataFromResponse(response, CommonConstants.SAML_RESPONSE_PARAM, 5);
            response = super.sendSAMLMessage(String.format(ACS_URL, config.getApp().getArtifact()), CommonConstants
                    .SAML_RESPONSE_PARAM, samlResponse, config);
            String resultPage = extractDataFromResponse(response);
            Assert.assertTrue(resultPage.contains("index.jsp") && !resultPage.contains("error"),
                    "SAML SSO Logout failed for " + config);
        } catch (Exception e) {
            Assert.fail("SAML SSO Logout test failed for " + config, e);
        }
    }


    @Test(alwaysRun = true, description = "Testing SAML RelayState decode", groups = "wso2.is", dependsOnMethods =
            {"testSAMLSSOLogout"})
    public void testSAMLRelayStateDecode() throws Exception {
        try {
            String relayState = "https%3A%2F%2Fwww.google.com%2Fa%2Fcoolguseconcepts" +
                    ".com%2FServiceLogin%3Fservice%3Dmail%26passive%3Dtrue%26rm%3Dfalse%26continue%3Dhttps%253A%252F" +
                    "%252Fmail.google.com%252Fa%252Fcoolguseconcepts" +
                    ".com%252F%26ss%3D1%26ltmpl%3Ddefault%26ltmplcache%3D2%26emr%3D1%26osid%3D1%26scope%3Dhttp%3A%2F" +
                    "%2Fmeyerweb.com%2Feric%2Ftools%2Fdencoder%2F%2bhttp%3A%2F%2Fmeyerweb" +
                    ".com%2Feric%2Ftools%2Fdencoder%2F&";
            HttpResponse response;
            response = Utils.sendGetRequest(String.format(SAML_SSO_LOGIN_URL, config.getApp().getArtifact(), config
                    .getHttpBinding().binding), USER_AGENT, httpClient);

            if (config.getHttpBinding() == HttpBinding.HTTP_POST) {
                String samlRequest = Utils.extractDataFromResponse(response, CommonConstants.SAML_REQUEST_PARAM, 5);
                Map<String, String> paramters = new HashMap<String, String>();
                paramters.put(CommonConstants.SAML_REQUEST_PARAM, samlRequest);
                paramters.put("RelayState", relayState);
                response = Utils.sendSAMLMessage(getTenantQualifiedURL(SAML_SSO_URL,
                        config.getUser().getTenantDomain()), paramters, USER_AGENT, httpClient);
                EntityUtils.consume(response.getEntity());
                response = Utils.sendRedirectRequest(response, USER_AGENT, ACS_URL, config.getApp().getArtifact(),
                        httpClient);

                String sessionKey = Utils.extractDataFromResponse(response, CommonConstants.SESSION_DATA_KEY, 1);
                response = Utils.sendPOSTMessage(sessionKey, getTenantQualifiedURL(COMMON_AUTH_URL, config.getUser().getTenantDomain()), USER_AGENT, ACS_URL, config.getApp()
                        .getArtifact(), config.getUser().getTenantAwareUsername(), config.getUser().getPassword(), httpClient);
                EntityUtils.consume(response.getEntity());

                response = Utils.sendRedirectRequest(response, USER_AGENT, ACS_URL, config.getApp().getArtifact(),
                        httpClient);
                String receivedRelayState = Utils.extractDataFromResponse(response, "RelayState", 5);
                relayState = relayState.replaceAll("&", "&amp;").replaceAll("\"", "&quot;").replaceAll("'", "&apos;").
                        replaceAll("<", "&lt;").replaceAll(">", "&gt;").replace("\n", "");
                Assert.assertEquals(relayState, receivedRelayState, "Sent parameter : " + relayState + "\nRecieved : " +
                        "" + receivedRelayState + "\n");
            }

            EntityUtils.consumeQuietly(response.getEntity());

        } catch (Exception e) {
            Assert.fail("SAML SSO Logout test failed for " + config, e);
        }
    }

    @Test(alwaysRun = true, description = "Testing SAML SSO logout", groups = "wso2.is",
            dependsOnMethods = { "testSAMLRelayStateDecode" })
    public void testSAMLSSOIdPLogout() throws Exception {

        try {
            if (config.getHttpBinding() == HttpBinding.HTTP_POST) {
                HttpResponse response = Utils.sendGetRequest(SAML_IDP_SLO_URL, USER_AGENT, httpClient);
                String resultPage = extractDataFromResponse(response);

                Assert.assertTrue(resultPage.contains("You have successfully logged out") &&
                        !resultPage.contains("error"), "SAML SSO IdP Logout failed for " + config);
            }
        } catch (Exception e) {
            Assert.fail("SAML SSO IdP Logout test failed for " + config, e);
        }
    }

    @DataProvider(name = "samlConfigProvider")
    public static SAMLConfig[][] samlConfigProvider(){
        return  new SAMLConfig[][]{
                {new SAMLConfig(TestUserMode.SUPER_TENANT_ADMIN, User.SUPER_TENANT_USER, HttpBinding.HTTP_REDIRECT,
                                ClaimType.NONE, App.SUPER_TENANT_APP_WITH_SIGNING)},
                {new SAMLConfig(TestUserMode.SUPER_TENANT_ADMIN, User.SUPER_TENANT_USER, HttpBinding.HTTP_REDIRECT,
                                ClaimType.LOCAL, App.SUPER_TENANT_APP_WITH_SIGNING)},
                {new SAMLConfig(TestUserMode.SUPER_TENANT_ADMIN, User.SUPER_TENANT_USER, HttpBinding.HTTP_POST,
                                ClaimType.NONE, App.SUPER_TENANT_APP_WITH_SIGNING)},
                {new SAMLConfig(TestUserMode.SUPER_TENANT_ADMIN, User.SUPER_TENANT_USER, HttpBinding.HTTP_POST,
                                ClaimType.LOCAL, App.SUPER_TENANT_APP_WITH_SIGNING)},
                {new SAMLConfig(TestUserMode.TENANT_ADMIN, User.TENANT_USER, HttpBinding.HTTP_REDIRECT,
                                ClaimType.NONE, App.TENANT_APP_WITHOUT_SIGNING)},
                {new SAMLConfig(TestUserMode.TENANT_ADMIN, User.TENANT_USER, HttpBinding.HTTP_REDIRECT,
                                ClaimType.LOCAL, App.TENANT_APP_WITHOUT_SIGNING)},
                {new SAMLConfig(TestUserMode.TENANT_ADMIN, User.TENANT_USER, HttpBinding.HTTP_POST,
                                ClaimType.NONE, App.TENANT_APP_WITHOUT_SIGNING)},
                {new SAMLConfig(TestUserMode.TENANT_ADMIN, User.TENANT_USER, HttpBinding.HTTP_POST,
                                ClaimType.LOCAL, App.TENANT_APP_WITHOUT_SIGNING)},

//                {new SAMLConfig(TestUserMode.SUPER_TENANT_ADMIN, User.SUPER_TENANT_USER_WITHOUT_MANDATORY_CLAIMS,
//                        HttpBinding.HTTP_REDIRECT, ClaimType.LOCAL, App.SUPER_TENANT_APP_WITH_SIGNING)},
//                {new SAMLConfig(TestUserMode.TENANT_ADMIN, User.TENANT_USER_WITHOUT_MANDATORY_CLAIMS,
//                        HttpBinding.HTTP_REDIRECT, ClaimType.LOCAL, App.TENANT_APP_WITHOUT_SIGNING)},
        };
    }

    private void assertLocalClaims(String claims){
        Map<String, String> attributeMap = extractClaims(claims);
        Assert.assertTrue(attributeMap.containsKey(firstNameClaimURI), "Claim nickname is expected");
        Assert.assertEquals(attributeMap.get(firstNameClaimURI), config.getUser().getNickname(),
                            "Expected claim value for nickname is " + config.getUser().getNickname());
        Assert.assertTrue(attributeMap.containsKey(lastNameClaimURI), "Claim lastname is expected");
        Assert.assertEquals(attributeMap.get(lastNameClaimURI), config.getUser().getUsername(),
                "Expected claim value for lastname is " + config.getUser().getUsername());
        Assert.assertTrue(attributeMap.containsKey(emailClaimURI), "Claim email is expected");
        Assert.assertEquals(attributeMap.get(emailClaimURI), config.getUser().getEmail(),
                "Expected claim value for email is " + config.getUser().getEmail());
    }

    private void assertNoneClaims(String claims){
        String[] dataArray = StringUtils.substringsBetween(claims, "<td>", "</td>");
        Assert.assertNull(dataArray, "Claims are not expected for " + config);
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

    private boolean requestMissingClaims (HttpResponse response) {

        String redirectUrl = Utils.getRedirectUrl(response);
        return redirectUrl.contains("consent.do");

    }
}

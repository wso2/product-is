/*
 * Copyright (c) 2021, WSO2 LLC. (http://www.wso2.com).
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

import org.apache.commons.codec.binary.Base64;
import org.apache.commons.lang.StringUtils;
import org.apache.http.HttpResponse;
import org.apache.http.util.EntityUtils;
import org.testng.Assert;

import org.testng.annotations.AfterClass;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;
import org.wso2.carbon.automation.engine.context.TestUserMode;
import org.wso2.identity.integration.test.rest.api.server.application.management.v1.model.ApplicationPatchModel;
import org.wso2.identity.integration.test.rest.api.server.application.management.v1.model.Claim;
import org.wso2.identity.integration.test.rest.api.server.application.management.v1.model.ClaimConfiguration;
import org.wso2.identity.integration.test.rest.api.server.application.management.v1.model.ClaimConfiguration.DialectEnum;
import org.wso2.identity.integration.test.rest.api.server.application.management.v1.model.ClaimMappings;
import org.wso2.identity.integration.test.rest.api.server.application.management.v1.model.RequestedClaimConfiguration;
import org.wso2.identity.integration.test.rest.api.server.application.management.v1.model.SAML2ServiceProvider;
import org.wso2.identity.integration.test.util.Utils;
import org.wso2.identity.integration.test.utils.CommonConstants;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.HashMap;
import java.util.Map;

/**
 * Test case to test the consent management with SAML SSO.
 */
public class SAMLSSOConsentTestCase extends AbstractSAMLSSOTestCase{

    private static final String APPLICATION_NAME = "SAML-SSO-TestApplication";
    private static final String SAML_SSO_INDEX_URL = "http://localhost:8490/%s/";
    private static final String SAML_SSO_LOGOUT_URL =
            "http://localhost:8490/%s/logout?SAML2.HTTPBinding=%s";

    private static final String firstNameClaimURI = "http://wso2.org/claims/givenname";
    private static final String lastNameClaimURI = "http://wso2.org/claims/lastname";
    private static final String emailClaimURI = "http://wso2.org/claims/emailaddress";

    private SAMLConfig config;
    private String resultPage;
    private String userId;
    private String appId;

    @BeforeClass(alwaysRun = true)
    public void testInit() throws Exception {

        config = new SAMLConfig(TestUserMode.SUPER_TENANT_ADMIN, User.SUPER_TENANT_USER, HttpBinding.HTTP_REDIRECT,
                ClaimType.LOCAL, App.SUPER_TENANT_APP_WITH_SIGNING);
        super.init(config.getUserMode());
        super.testInit();

        userId = super.addUser(config);
        appId = super.addApplication(config, APPLICATION_NAME);
    }

    @AfterClass(alwaysRun = true)
    public void testClear() throws Exception{

        super.deleteUser(userId);
        super.deleteApp(appId);
        super.testClear();
    }

    @Test(description = "Add service provider", groups = "wso2.is", priority = 1)
    public void testAddSP() throws Exception {

        applicationMgtRestClient.updateInboundDetailsOfApplication(appId, getSAMLConfigurations(config), SAML);
        SAML2ServiceProvider samlConfig = applicationMgtRestClient.getSAMLInboundDetails(appId);
        Assert.assertNotNull(samlConfig, "Adding a service provider has failed for " + config);
        Assert.assertEquals(samlConfig.getIssuer(), config.getApp().getArtifact(),
                "Adding a service provider has failed for " + config);
    }

    @Test(alwaysRun = true, description = "Testing SAML SSO login", groups = "wso2.is",
            dependsOnMethods = { "testAddSP" })
    public void testConsentWithAppClaimConfigUpdate() throws Exception {

        samlSSOLogin();
        testClaims();
        testSAMLSSOLogout();

        updateSPClaimConfiguration();

        // Login again with updated claim configurations.
        samlSSOLogin();
        testUpdatedClaims();
    }

    private void samlSSOLogin() {

        try {
            HttpResponse response;
            response = Utils.sendGetRequest(String.format(SAML_SSO_INDEX_URL, config.getApp().getArtifact()),
                    USER_AGENT, httpClient);
            String samlResponse = Utils.extractDataFromResponse(response, "name='SAMLResponse'", 5);
            samlResponse = new String(Base64.decodeBase64(samlResponse));
            Assert.assertTrue(samlResponse.contains("Destination=\"" + String.format(ACS_URL, config.getApp()
                    .getArtifact()) + "\""));

            response = Utils.sendGetRequest(String.format(SAML_SSO_LOGIN_URL, config.getApp().getArtifact(), config
                    .getHttpBinding().binding), USER_AGENT, httpClient);

            String sessionKey = Utils.extractDataFromResponse(response, CommonConstants.SESSION_DATA_KEY, 1);
            response = Utils.sendPOSTMessage(sessionKey, SAML_SSO_URL, USER_AGENT, ACS_URL, config.getApp()
                    .getArtifact(), config.getUser().getUsername(), config.getUser().getPassword(), httpClient);

            String pastrCookie = Utils.getPastreCookie(response);
            Assert.assertNotNull(pastrCookie, "pastr cookie not found in response.");
            EntityUtils.consume(response.getEntity());

            response = Utils.sendPOSTConsentMessage(response, COMMON_AUTH_URL, USER_AGENT, String.format(ACS_URL,
                    config.getApp().getArtifact()), httpClient, pastrCookie);
            EntityUtils.consume(response.getEntity());

            String redirectUrl = Utils.getRedirectUrl(response);
            if(StringUtils.isNotBlank(redirectUrl)) {
                response = Utils.sendRedirectRequest(response, USER_AGENT, ACS_URL, config.getApp().getArtifact(),
                        httpClient);
            }
            samlResponse = Utils.extractDataFromResponse(response, CommonConstants.SAML_RESPONSE_PARAM, 5);
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

    private void testClaims() {

        String claimString = resultPage.substring(resultPage.lastIndexOf("<table>"));
        Map<String, String> attributeMap = extractClaims(claimString);
        Assert.assertTrue(attributeMap.containsKey(firstNameClaimURI), "Claim nickname is expected");
        Assert.assertEquals(attributeMap.get(firstNameClaimURI), config.getUser().getNickname(),
                "Expected claim value for nickname is " + config.getUser().getNickname());
        Assert.assertTrue(attributeMap.containsKey(lastNameClaimURI), "Claim lastname is expected");
        Assert.assertEquals(attributeMap.get(lastNameClaimURI), config.getUser().getUsername(),
                "Expected claim value for lastname is " + config.getUser().getUsername());
    }

    private void testUpdatedClaims () {

        String claimString = resultPage.substring(resultPage.lastIndexOf("<table>"));
        Map<String, String> attributeMap = extractClaims(claimString);
        Assert.assertTrue(attributeMap.containsKey(emailClaimURI), "Claim email is expected");
        Assert.assertEquals(attributeMap.get(emailClaimURI), config.getUser().getEmail(),
                "Expected claim value for email is " + config.getUser().getEmail());
    }

    private void testSAMLSSOLogout() {
        try {
            HttpResponse response;

            response = Utils.sendGetRequest(String.format(SAML_SSO_LOGOUT_URL, config.getApp().getArtifact(), config
                    .getHttpBinding().binding), USER_AGENT, httpClient);

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

    private void updateSPClaimConfiguration() throws Exception {

        ApplicationPatchModel applicationPatch = new ApplicationPatchModel()
                .claimConfiguration(new ClaimConfiguration()
                        .dialect(DialectEnum.LOCAL)
                        .addClaimMappingsItem(new ClaimMappings()
                                .applicationClaim(emailClaimURI)
                                .localClaim(new Claim().uri(emailClaimURI)))
                        .addRequestedClaimsItem(new RequestedClaimConfiguration()
                                .claim(new Claim().uri(emailClaimURI))));

        applicationMgtRestClient.updateApplication(appId, applicationPatch);
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
        Map<String,String> attributeMap = new HashMap<>();
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

    public ClaimConfiguration getClaimConfigurations() {

        return new ClaimConfiguration()
                .dialect(DialectEnum.LOCAL)
                .addClaimMappingsItem(new ClaimMappings()
                        .applicationClaim(firstNameClaimURI)
                        .localClaim(new Claim().uri(firstNameClaimURI)))
                .addClaimMappingsItem(new ClaimMappings()
                        .applicationClaim(lastNameClaimURI)
                        .localClaim(new Claim().uri(lastNameClaimURI)))
                .addRequestedClaimsItem(new RequestedClaimConfiguration()
                        .claim(new Claim().uri(firstNameClaimURI)))
                .addRequestedClaimsItem(new RequestedClaimConfiguration()
                        .claim(new Claim().uri(lastNameClaimURI)));
    }
}

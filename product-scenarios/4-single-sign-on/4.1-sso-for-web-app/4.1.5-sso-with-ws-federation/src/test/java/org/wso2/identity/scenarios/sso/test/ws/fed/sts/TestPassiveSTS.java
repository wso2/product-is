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

package org.wso2.identity.scenarios.sso.test.ws.fed.sts;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.http.Header;
import org.apache.http.HttpHeaders;
import org.apache.http.HttpResponse;
import org.apache.http.HttpStatus;
import org.apache.http.impl.client.BasicCookieStore;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClientBuilder;
import org.apache.http.util.EntityUtils;
import org.testng.Assert;
import org.testng.annotations.AfterClass;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.DataProvider;
import org.testng.annotations.Factory;
import org.testng.annotations.Test;
import org.wso2.carbon.identity.application.common.model.xsd.ClaimConfig;
import org.wso2.carbon.identity.application.common.model.xsd.ClaimMapping;
import org.wso2.carbon.identity.application.common.model.xsd.InboundAuthenticationRequestConfig;
import org.wso2.carbon.identity.application.common.model.xsd.OutboundProvisioningConfig;
import org.wso2.carbon.identity.application.common.model.xsd.Property;
import org.wso2.carbon.identity.application.common.model.xsd.ServiceProvider;
import org.wso2.identity.scenarios.commons.ScenarioTestBase;
import org.wso2.identity.scenarios.commons.TestConfig;
import org.wso2.identity.scenarios.commons.TestConfig.ClaimType;
import org.wso2.identity.scenarios.commons.TestUserMode;
import org.wso2.identity.scenarios.commons.clients.application.mgt.ApplicationManagementServiceClient;
import org.wso2.identity.scenarios.commons.clients.login.AuthenticatorClient;
import org.wso2.identity.scenarios.commons.clients.usermgt.remote.RemoteUserStoreManagerServiceClient;
import org.wso2.identity.scenarios.commons.util.DataExtractUtil;

import java.io.IOException;
import java.net.URISyntaxException;
import java.util.ArrayList;
import java.util.List;

import static org.testng.Assert.assertTrue;
import static org.wso2.carbon.utils.multitenancy.MultitenantConstants.SUPER_TENANT_DOMAIN_NAME;
import static org.wso2.identity.scenarios.commons.util.Constants.COMMONAUTH_URI_CONTEXT;
import static org.wso2.identity.scenarios.commons.util.Constants.ClaimURIs.EMAIL_CLAIM_URI;
import static org.wso2.identity.scenarios.commons.util.Constants.ClaimURIs.FIRST_NAME_CLAIM_URI;
import static org.wso2.identity.scenarios.commons.util.DataExtractUtil.extractFullContentFromResponse;
import static org.wso2.identity.scenarios.commons.util.DataExtractUtil.getCookieFromResponse;
import static org.wso2.identity.scenarios.commons.util.DataExtractUtil.getTestUser;
import static org.wso2.identity.scenarios.commons.util.DataExtractUtil.isConsentRequested;
import static org.wso2.identity.scenarios.commons.util.IdentityScenarioUtil.sendGetRequest;
import static org.wso2.identity.scenarios.commons.util.SSOUtil.getClaimMappings;
import static org.wso2.identity.scenarios.commons.util.SSOUtil.sendLoginPost;
import static org.wso2.identity.scenarios.commons.util.SSOUtil.sendPOSTConsentMessage;

public class TestPassiveSTS extends ScenarioTestBase {

    private static final String SERVICE_PROVIDER_NAME = "PassiveSTSSampleApp";
    private static final String SERVICE_PROVIDER_Desc = "PassiveSTS Service Provider";
    private static final String PASSIVE_STS_SAMPLE_APP_URL = "%s/PassiveSTSSampleApp/index.jsp";
    private final static String USER_AGENT = "Apache-HttpClient/4.2.5 (java 1.6)";
    private static final String URN_OASIS_NAMES_TC_SAML_2_0_ASSERTION = "urn:oasis:names:tc:SAML:2.0:assertion";
    private static final String PASSIVESTS_URI_CONTEXT = "/passivests";

    private RemoteUserStoreManagerServiceClient remoteUSMServiceClient;

    private String username;
    private String password;
    private String sessionDataKey;
    private String resultPage;
    private Header locationHeader;
    private String passiveStsURL;
    private String passiveStsSampleAppURL;
    private String commonAuthUrl;

    private AuthenticatorClient logManger;
    private ApplicationManagementServiceClient appMgtclient;
    private ServiceProvider serviceProvider;
    private CloseableHttpClient client;

    private TestConfig config;

    private static final Log log = LogFactory.getLog(TestPassiveSTS.class);

    @Factory(dataProvider = "stsConfigProvider")
    public TestPassiveSTS(TestConfig config) {
        if (log.isDebugEnabled()) {
            log.info("SAML SSO Test initialized for " + config);
        }
        this.config = config;
    }

    @DataProvider(name = "stsConfigProvider")
    public static TestConfig[][] samlConfigProvider() throws Exception {
        return new TestConfig[][]{
                {new TestConfig(TestUserMode.SUPER_TENANT_USER, new TestConfig.User(getTestUser("super-tenant-user" +
                        ".json"), SUPER_TENANT_DOMAIN_NAME), ClaimType.NONE)}
        };
    }

    @BeforeClass(alwaysRun = true)
    public void testInit() throws Exception {
        super.init();
        loginAndObtainSessionCookie();

        logManger = new AuthenticatorClient(backendURL);
        appMgtclient = new ApplicationManagementServiceClient(sessionCookie, backendServiceURL, configContext);
        remoteUSMServiceClient = new RemoteUserStoreManagerServiceClient(backendServiceURL, sessionCookie);

        client = HttpClientBuilder.create().setDefaultCookieStore(new BasicCookieStore()).build();
        this.passiveStsURL = backendURL + PASSIVESTS_URI_CONTEXT;
        passiveStsSampleAppURL = String.format(PASSIVE_STS_SAMPLE_APP_URL, webAppHost);
        commonAuthUrl = backendURL + COMMONAUTH_URI_CONTEXT;
        super.createUser(this.config, remoteUSMServiceClient, "default");
        this.username = config.getUser().getUsername();
        this.password = config.getUser().getPassword();
    }

    @AfterClass(alwaysRun = true)
    public void atEnd() throws Exception {
        appMgtclient.deleteApplication(SERVICE_PROVIDER_NAME);
        super.deleteUser(config, remoteUSMServiceClient);
    }

    @Test(alwaysRun = true, description = "4.1.5.1")
    public void testAddSP() throws Exception {

        serviceProvider = new ServiceProvider();
        serviceProvider.setApplicationName(SERVICE_PROVIDER_NAME);
        serviceProvider.setDescription(SERVICE_PROVIDER_Desc);
        appMgtclient.createApplication(serviceProvider);
        serviceProvider = appMgtclient.getApplication(SERVICE_PROVIDER_NAME);
        Assert.assertNotNull(serviceProvider, "Service provider registration failed.");
    }

    @Test(alwaysRun = true, description = "4.1.5.2",
            dependsOnMethods = {"testAddSP"})
    public void testUpdateSP() throws Exception {

        serviceProvider.setOutboundProvisioningConfig(new OutboundProvisioningConfig());
        List<InboundAuthenticationRequestConfig> authRequestList = new ArrayList<InboundAuthenticationRequestConfig>();
        String passiveSTSRealm = SERVICE_PROVIDER_NAME;
        if (passiveSTSRealm != null) {
            InboundAuthenticationRequestConfig opicAuthenticationRequest = new InboundAuthenticationRequestConfig();
            opicAuthenticationRequest.setInboundAuthKey(passiveSTSRealm);
            opicAuthenticationRequest.setInboundAuthType("passivests");
            Property property = new Property();
            property.setName("passiveSTSWReply");
            property.setValue(passiveStsSampleAppURL);
            opicAuthenticationRequest.setProperties(new Property[]{property});
            authRequestList.add(opicAuthenticationRequest);
        }
        if (authRequestList.size() > 0) {
            serviceProvider.getInboundAuthenticationConfig()
                    .setInboundAuthenticationRequestConfigs(
                            authRequestList
                                    .toArray(new InboundAuthenticationRequestConfig[authRequestList
                                            .size()]));
        }
        appMgtclient.updateApplicationData(serviceProvider);
        Assert.assertNotEquals(appMgtclient.getApplication(SERVICE_PROVIDER_NAME)
                        .getInboundAuthenticationConfig()
                        .getInboundAuthenticationRequestConfigs().length,
                0, "Fail to update service provider with passiveSTS configs");
    }

    @Test(alwaysRun = true,
          description = "4.1.5.3",
          dependsOnMethods = { "testUpdateSP" })
    public void testAddClaimConfiguration() throws Exception {

        serviceProvider.getClaimConfig().setClaimMappings(getClaimMappings());
        appMgtclient.updateApplicationData(serviceProvider);
        ServiceProvider updatedServiceProvider = appMgtclient.getApplication(SERVICE_PROVIDER_NAME);
        ClaimConfig updatedClaimConfig = updatedServiceProvider.getClaimConfig();

        Assert.assertNotNull(updatedClaimConfig.getClaimMappings(),
                "Claim mapping is null. Claim mapping creation failed.");

        for (ClaimMapping claimMapping : getClaimMappings()) {
            boolean success = false;
            for (ClaimMapping updatedClaimMapping : updatedClaimConfig.getClaimMappings()) {
                if (claimMapping.getLocalClaim().getClaimUri()
                        .equals(updatedClaimMapping.getLocalClaim().getClaimUri())) {
                    success = true;
                    break;
                }
            }
            Assert.assertTrue(success, "Failed to set claim uri: " + claimMapping.getLocalClaim().getClaimUri());
        }
    }

    @Test(alwaysRun = true, description = "4.1.5.4",
            dependsOnMethods = {"testAddClaimConfiguration"})
    public void testInvokePassiveSTSSampleApp() throws IOException, URISyntaxException {

        HttpResponse response;
        response = sendGetRequest(client, passiveStsSampleAppURL, null);

        Assert.assertNotNull(response, "PassiveSTSSampleApp invoke response is null");
        int responseCode = response.getStatusLine().getStatusCode();
        Assert.assertEquals(responseCode, 200, "Invalid Response");

        sessionDataKey = DataExtractUtil.getSessionDataKey(response);
        Assert.assertNotNull(sessionDataKey, "Session data key is null.");
        EntityUtils.consume(response.getEntity());

    }

    @Test(alwaysRun = true, description = "4.1.5.5", dependsOnMethods =
            {"testInvokePassiveSTSSampleApp"})
    public void testSendLoginRequestPost() throws Exception {

        HttpResponse response = sendLoginPost(client, sessionDataKey, commonAuthUrl, username, password);
        Assert.assertNotNull(response, "Login response is null for: " + this.config);
        Assert.assertEquals(response.getStatusLine().getStatusCode(), 302, "Invalid Response for: " + this.config);

        locationHeader = response.getFirstHeader(HttpHeaders.LOCATION);
        Assert.assertNotNull(locationHeader, "Login response header is null for: " + this.config);
        if (isConsentRequested(response)) {
            String pastrCookie = getCookieFromResponse(response, "pastr");
            Assert.assertNotNull(pastrCookie, "pastr cookie not found in response for: " + this.config);
            EntityUtils.consume(response.getEntity());

            response = sendPOSTConsentMessage(response, commonAuthUrl, USER_AGENT, locationHeader.getValue()
                    , client, pastrCookie);
            locationHeader = response.getFirstHeader(HttpHeaders.LOCATION);
        }

        EntityUtils.consume(response.getEntity());
        response = sendGetRequest(client, locationHeader.getValue(), null);
        resultPage = extractFullContentFromResponse(response);

        // Following error log is added to analyze an intermittent error caught with Passive STS Login fail assertion.
        boolean successfullyRedirected = resultPage.contains("You are now redirected to " + passiveStsSampleAppURL);
        if (!successfullyRedirected) {
            log.error(String.format("Could not find the successfully redirected message from the result page. " +
                    "Here are some helpful information to analyze the root cause. responseStatus: %s, " +
                    "resultPage: %s", response.getStatusLine(), resultPage));
        }
        assertTrue(successfullyRedirected, "Passive STS Login failed for: " + this.config);
        assertTrue(resultPage.contains("RequestSecurityTokenResponseCollection"), "Passive STS " +
                "Login response doesn't have wresult for: " + this.config);
        assertTrue(resultPage.contains("urn:oasis:names:tc:SAML:1.0:assertion"), "Passive STS " +
                "Login response doesn't have SAMLAssertion for: " + this.config);
        EntityUtils.consume(response.getEntity());
        testPassiveSTSClaims();
    }

    private void testPassiveSTSClaims() {

        Assert.assertTrue(resultPage.contains(FIRST_NAME_CLAIM_URI), "Claim givenname is expected for: " + this
                .config);
        Assert.assertTrue(resultPage.contains(username), "Claim value givenname is expected for: " + this.config);

        Assert.assertTrue(resultPage.contains(EMAIL_CLAIM_URI), "Claim email is expected for: " + this.config);
        Assert.assertTrue(resultPage.contains(this.config.getUser().getUserClaim(EMAIL_CLAIM_URI)), "Claim value email is expected for:" +
                " " + this.config);
    }

    @Test(alwaysRun = true, description = "4.1.5.6", dependsOnMethods = {"testSendLoginRequestPost"})
    public void testPassiveSAML2Assertion() throws Exception {
        String passiveParams = "?wa=wsignin1.0&wreply=" + passiveStsSampleAppURL + "&wtrealm=PassiveSTSSampleApp";
        String wreqParam = "&wreq=%3Cwst%3ARequestSecurityToken+xmlns%3Awst%3D%22http%3A%2F%2Fdocs.oasis-open.org"
                + "%2Fws-sx%2Fws-trust%2F200512%22%3E%3Cwst%3ATokenType%3Ehttp%3A%2F%2Fdocs.oasis-open.org"
                + "%2Fwss%2Foasis-wss-saml-token-profile-1.1%23SAMLV2.0%3C%2Fwst%3ATokenType%3E%3C%2Fwst"
                + "%3ARequestSecurityToken%3E";

        String responseString = getResponsePageForPassiveSTSRequest(passiveParams, wreqParam);
        Assert.assertTrue(responseString.contains(URN_OASIS_NAMES_TC_SAML_2_0_ASSERTION),
                "No SAML2 Assertion found for the SAML2 request.");
    }

    private String getResponsePageForPassiveSTSRequest(String passiveParams, String wreqParam) throws IOException,
            URISyntaxException {
        HttpResponse response = sendGetRequest(client, this.passiveStsURL + passiveParams + wreqParam, null);

        Assert.assertNotNull(response, "PassiveSTSSampleApp invoke response is null.");
        int responseCode = response.getStatusLine().getStatusCode();
        Assert.assertEquals(responseCode, HttpStatus.SC_OK, "Invalid Response.");
        String responseString = extractFullContentFromResponse(response);
        EntityUtils.consume(response.getEntity());
        return responseString;
    }

    @Test(alwaysRun = true, description = "4.1.5.7",
            dependsOnMethods = {"testPassiveSAML2Assertion"})
    public void testPassiveSAML2AssertionWithoutWReply() throws Exception {
        String passiveParams = "?wa=wsignin1.0&wtrealm=PassiveSTSSampleApp";
        String wreqParam = "&wreq=%3Cwst%3ARequestSecurityToken+xmlns%3Awst%3D%22http%3A%2F%2Fdocs.oasis-open.org"
                + "%2Fws-sx%2Fws-trust%2F200512%22%3E%3Cwst%3ATokenType%3Ehttp%3A%2F%2Fdocs.oasis-open.org"
                + "%2Fwss%2Foasis-wss-saml-token-profile-1.1%23SAMLV2.0%3C%2Fwst%3ATokenType%3E%3C%2Fwst"
                + "%3ARequestSecurityToken%3E";

        String responseString = getResponsePageForPassiveSTSRequest(passiveParams, wreqParam);
        Assert.assertTrue(responseString.contains(TestPassiveSTS.URN_OASIS_NAMES_TC_SAML_2_0_ASSERTION),
                "No SAML2 Assertion found for the SAML2 request without WReply in passive-sts request.");
    }

    @Test(alwaysRun = true, description = "4.1.5.8", dependsOnMethods = {
            "testPassiveSAML2AssertionWithoutWReply"})
    public void testPassiveSAML2AssertionForInvalidWReply() throws Exception {

        String INVALID_PASSIVE_STS_SAMPLE_APP_URL = passiveStsSampleAppURL + "INVALID";
        String passiveParams = "?wa=wsignin1.0&wreply=" + INVALID_PASSIVE_STS_SAMPLE_APP_URL + "&wtrealm=PassiveSTSSampleApp";
        String wreqParam = "&wreq=%3Cwst%3ARequestSecurityToken+xmlns%3Awst%3D%22http%3A%2F%2Fdocs.oasis-open.org"
                + "%2Fws-sx%2Fws-trust%2F200512%22%3E%3Cwst%3ATokenType%3Ehttp%3A%2F%2Fdocs.oasis-open.org"
                + "%2Fwss%2Foasis-wss-saml-token-profile-1.1%23SAMLV2.0%3C%2Fwst%3ATokenType%3E%3C%2Fwst"
                + "%3ARequestSecurityToken%3E";

        String responseString = getResponsePageForPassiveSTSRequest(passiveParams, wreqParam);
        assertTrue(responseString.contains("soapenv:Fault"), "Cannot find soap fault for invalid WReply URL");
    }
}

package org.wso2.identity.integration.test.sts;

import org.apache.commons.lang.StringUtils;
import org.apache.http.Header;
import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.NameValuePair;
import org.apache.http.client.config.CookieSpecs;
import org.apache.http.client.config.RequestConfig;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.config.Lookup;
import org.apache.http.config.RegistryBuilder;
import org.apache.http.cookie.CookieSpecProvider;
import org.apache.http.impl.client.BasicCookieStore;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClientBuilder;
import org.apache.http.impl.cookie.RFC6265CookieSpecProvider;
import org.apache.http.message.BasicNameValuePair;
import org.apache.http.util.EntityUtils;
import org.testng.Assert;
import org.testng.annotations.AfterClass;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.DataProvider;
import org.testng.annotations.Factory;
import org.testng.annotations.Test;
import org.wso2.carbon.automation.engine.context.AutomationContext;
import org.wso2.carbon.automation.engine.context.TestUserMode;
import org.wso2.carbon.identity.application.common.model.xsd.Claim;
import org.wso2.carbon.identity.application.common.model.xsd.ClaimConfig;
import org.wso2.carbon.identity.application.common.model.xsd.ClaimMapping;
import org.wso2.carbon.identity.application.common.model.xsd.InboundAuthenticationRequestConfig;
import org.wso2.carbon.identity.application.common.model.xsd.OutboundProvisioningConfig;
import org.wso2.carbon.identity.application.common.model.xsd.Property;
import org.wso2.carbon.identity.application.common.model.xsd.ServiceProvider;
import org.wso2.carbon.integration.common.admin.client.AuthenticatorClient;
import org.wso2.identity.integration.common.clients.application.mgt.ApplicationManagementServiceClient;
import org.wso2.identity.integration.common.utils.ISIntegrationTest;
import org.wso2.identity.integration.test.util.Utils;
import org.wso2.identity.integration.test.utils.DataExtractUtil;

import java.io.IOException;
import java.net.URL;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static org.testng.Assert.assertEquals;
import static org.testng.Assert.assertNotNull;
import static org.testng.Assert.assertTrue;

public class TestPassiveSTS extends ISIntegrationTest {

    private static final String SERVICE_PROVIDER_NAME = "PassiveSTSSampleApp";
    private static final String SERVICE_PROVIDER_Desc = "PassiveSTS Service Provider";
    private static final String EMAIL_CLAIM_URI = "http://wso2.org/claims/emailaddress";
    private static final String GIVEN_NAME_CLAIM_URI = "http://wso2.org/claims/givenname";
    private static final String PASSIVE_STS_SAMPLE_APP_URL = "http://localhost:8490/PassiveSTSSampleApp";
    private static final String PASSIVE_STS_SAMPLE_APP_URL_INCORRECT = "http://localhost:8490/Incorrect";
    private static final String COMMON_AUTH_URL =
            "https://localhost:9853/commonauth";
    private static final String HTTP_RESPONSE_HEADER_LOCATION = "location";
    public final static String USER_AGENT = "Apache-HttpClient/4.2.5 (java 1.6)";

    private String username;
    private String userPassword;
    private String tenantDomain;

    private String sessionDataKey;
    private Header locationHeader;
    private String passiveStsURL;

    private ApplicationManagementServiceClient appMgtClient;
    private ServiceProvider serviceProvider;
    private Lookup<CookieSpecProvider> cookieSpecRegistry;
    private RequestConfig requestConfig;
    private CloseableHttpClient client;
    private BasicCookieStore cookieStore;

    @DataProvider(name = "configProvider")
    public static Object[][] configProvider() {
        return new Object[][]{
                {TestUserMode.TENANT_ADMIN},
                {TestUserMode.SUPER_TENANT_ADMIN}
        };
    }

    @Factory(dataProvider = "configProvider")
    public TestPassiveSTS(TestUserMode userMode) throws Exception {

        super.init(userMode);
        AutomationContext context = new AutomationContext("IDENTITY", userMode);
        this.username = context.getContextTenant().getTenantAdmin().getUserName();
        this.userPassword = context.getContextTenant().getTenantAdmin().getPassword();
        this.tenantDomain = context.getContextTenant().getDomain();
    }

    @BeforeClass(alwaysRun = true)
    public void testInit() throws Exception {

        AuthenticatorClient logManger = new AuthenticatorClient(backendURL);
        logManger.login(username, userPassword, isServer.getInstance().getHosts().get("default"));

        appMgtClient = new ApplicationManagementServiceClient(sessionCookie, backendURL, null);
        cookieStore = new BasicCookieStore();

        cookieSpecRegistry = RegistryBuilder.<CookieSpecProvider>create()
                .register(CookieSpecs.DEFAULT, new RFC6265CookieSpecProvider())
                .build();
        requestConfig = RequestConfig.custom()
                .setCookieSpec(CookieSpecs.DEFAULT)
                .build();
        client = HttpClientBuilder.create()
                .setDefaultCookieSpecRegistry(cookieSpecRegistry)
                .setDefaultRequestConfig(requestConfig)
                .setDefaultCookieStore(cookieStore).build();
        String isURL = backendURL.substring(0, backendURL.indexOf("services/"));
        this.passiveStsURL = getTenantQualifiedURL(isURL + "passivests", tenantDomain);

        setSystemProperties();
    }

    @AfterClass(alwaysRun = true)
    public void atEnd() throws Exception {
        appMgtClient.deleteApplication(SERVICE_PROVIDER_NAME);
    }

    @Test(alwaysRun = true, description = "Add service provider")
    public void testAddSP() throws Exception {

        serviceProvider = new ServiceProvider();
        serviceProvider.setApplicationName(SERVICE_PROVIDER_NAME);
        serviceProvider.setDescription(SERVICE_PROVIDER_Desc);
        appMgtClient.createApplication(serviceProvider);
        serviceProvider = appMgtClient.getApplication(SERVICE_PROVIDER_NAME);
        Assert.assertNotNull(serviceProvider, "Service provider registration failed for tenant domain: " +
                tenantDomain);
    }

    @Test(alwaysRun = true, description = "Update service provider with passiveSTS configs",
            dependsOnMethods = {"testAddSP"})
    public void testUpdateSP() throws Exception {

        serviceProvider.setOutboundProvisioningConfig(new OutboundProvisioningConfig());
        List<InboundAuthenticationRequestConfig> authRequestList = new ArrayList<InboundAuthenticationRequestConfig>();
        InboundAuthenticationRequestConfig opicAuthenticationRequest = new InboundAuthenticationRequestConfig();
        opicAuthenticationRequest.setInboundAuthKey(SERVICE_PROVIDER_NAME);
        opicAuthenticationRequest.setInboundAuthType("passivests");
        List<Property> propertiesArrList = new ArrayList<>();
        Property propertyWReplyURL = new Property();
        propertyWReplyURL.setName("passiveSTSWReply");
        propertyWReplyURL.setValue(PASSIVE_STS_SAMPLE_APP_URL);
        propertiesArrList.add(propertyWReplyURL);
        Property propertyWReplyLogoutURL = new Property();
        propertyWReplyLogoutURL.setName("passiveSTSWReplyLogout");
        propertyWReplyLogoutURL.setValue(PASSIVE_STS_SAMPLE_APP_URL);
        propertiesArrList.add(propertyWReplyLogoutURL);
        opicAuthenticationRequest.setProperties(propertiesArrList.toArray(new Property[0]));
        authRequestList.add(opicAuthenticationRequest);

        if (authRequestList.size() > 0) {
            serviceProvider.getInboundAuthenticationConfig()
                    .setInboundAuthenticationRequestConfigs(
                            authRequestList
                                    .toArray(new InboundAuthenticationRequestConfig[0]));
        }
        appMgtClient.updateApplicationData(serviceProvider);
        Assert.assertNotEquals(appMgtClient.getApplication(SERVICE_PROVIDER_NAME)
                        .getInboundAuthenticationConfig()
                        .getInboundAuthenticationRequestConfigs().length,
                0, "Fail to update service provider with passiveSTS configs for tenant domain: " + tenantDomain);
    }

    @Test(alwaysRun = true, description = "Update service provider with claim configurations",
            dependsOnMethods = {"testUpdateSP"})
    public void testAddClaimConfiguration() throws Exception {

        serviceProvider.getClaimConfig().setClaimMappings(getClaimMappings());
        appMgtClient.updateApplicationData(serviceProvider);
        ServiceProvider updatedServiceProvider = appMgtClient.getApplication(SERVICE_PROVIDER_NAME);
        ClaimConfig updatedClaimConfig = updatedServiceProvider.getClaimConfig();

        int arraySize = updatedClaimConfig.getClaimMappings().length;
        String[] claimsUris = new String[arraySize];
        for (int index = 0; index < arraySize; index++) {
            claimsUris[index] = updatedClaimConfig.getClaimMappings()[index].getLocalClaim().getClaimUri();
        }

        List<String> claimsList = Arrays.asList(claimsUris);
        Assert.assertTrue(claimsList.contains(GIVEN_NAME_CLAIM_URI),
                "Failed update given name claim uri for tenant domain: " + tenantDomain);
        Assert.assertTrue(claimsList.contains(EMAIL_CLAIM_URI), "Failed update email claim uri for tenant domain: " +
                tenantDomain);
    }

    @Test(alwaysRun = true, description = "Invoke PassiveSTS endpoint",
            dependsOnMethods = {"testAddClaimConfiguration"})
    public void testInvokePassiveSTSEndPoint() throws IOException {

        cookieStore.clear();
        String passiveParams = "?wreply=" + PASSIVE_STS_SAMPLE_APP_URL + "&wtrealm=PassiveSTSSampleApp";
        HttpGet request = new HttpGet(this.passiveStsURL + passiveParams);
        HttpResponse response = client.execute(request);
        Assert.assertNotNull(response, "PassiveSTSSampleApp invoke response is null for tenant domain: " +
                tenantDomain);
        int responseCode = response.getStatusLine().getStatusCode();
        Assert.assertEquals(responseCode, 200, "Invalid Response for tenant domain: " + tenantDomain);

        Map<String, Integer> keyPositionMap = new HashMap<>(1);
        keyPositionMap.put("name=\"sessionDataKey\"", 1);
        List<DataExtractUtil.KeyValue> keyValues = DataExtractUtil.extractDataFromResponse(response,
                keyPositionMap);
        EntityUtils.consume(response.getEntity());
        Assert.assertNotNull(keyValues, "sessionDataKey key value is null for tenant domain: " + tenantDomain);
        sessionDataKey = keyValues.get(0).getValue();
        Assert.assertNotNull(sessionDataKey, "Session data key is null for tenant domain: " + tenantDomain);
    }

    @Test(alwaysRun = true, description = "Send login post request", dependsOnMethods =
            {"testInvokePassiveSTSEndPoint"})
    public void testSendLoginRequestPost() throws Exception {

        cookieStore.clear();
        HttpPost request = new HttpPost(COMMON_AUTH_URL);
        List<NameValuePair> urlParameters = new ArrayList<NameValuePair>();
        urlParameters.add(new BasicNameValuePair("username", username));
        urlParameters.add(new BasicNameValuePair("password", userPassword));
        urlParameters.add(new BasicNameValuePair("sessionDataKey", sessionDataKey));
        request.setEntity(new UrlEncodedFormEntity(urlParameters));

        HttpResponse response = client.execute(request);
        Assert.assertNotNull(response, "Login response is null for tenant domain: " + tenantDomain);
        Assert.assertEquals(response.getStatusLine().getStatusCode(), 302, "Invalid Response " +
                "for tenant domain: " + tenantDomain);

        locationHeader = response.getFirstHeader(HTTP_RESPONSE_HEADER_LOCATION);
        Assert.assertNotNull(locationHeader, "Login response header is null for tenant domain: " + tenantDomain);
        if (requestMissingClaims(response)) {
            String pastrCookie = Utils.getPastreCookie(response);
            Assert.assertNotNull(pastrCookie, "pastr cookie not found in response for tenant domain: " + tenantDomain);
            EntityUtils.consume(response.getEntity());

            response = Utils.sendPOSTConsentMessage(response, COMMON_AUTH_URL, USER_AGENT, locationHeader.getValue()
                    , client, pastrCookie);
            locationHeader = response.getFirstHeader(HTTP_RESPONSE_HEADER_LOCATION);
            EntityUtils.consume(response.getEntity());
        }

        HttpGet getRequest = new HttpGet(locationHeader.getValue());
        EntityUtils.consume(response.getEntity());
        response = client.execute(getRequest);
        EntityUtils.consume(response.getEntity());
    }

    @Test(alwaysRun = true, description = "Test PassiveSTS SAML2 Assertion", dependsOnMethods = {
            "testSendLoginRequestPost"})
    public void testPassiveSAML2Assertion() throws Exception {
        String passiveParams = "?wa=wsignin1.0&wreply=" + PASSIVE_STS_SAMPLE_APP_URL + "&wtrealm=PassiveSTSSampleApp";
        String wreqParam = "&wreq=%3Cwst%3ARequestSecurityToken+xmlns%3Awst%3D%22http%3A%2F%2Fdocs.oasis-open.org"
                + "%2Fws-sx%2Fws-trust%2F200512%22%3E%3Cwst%3ATokenType%3Ehttp%3A%2F%2Fdocs.oasis-open.org"
                + "%2Fwss%2Foasis-wss-saml-token-profile-1.1%23SAMLV2.0%3C%2Fwst%3ATokenType%3E%3C%2Fwst"
                + "%3ARequestSecurityToken%3E";

        HttpGet request = new HttpGet(this.passiveStsURL + passiveParams + wreqParam);
        HttpResponse response = client.execute(request);

        Assert.assertNotNull(response, "PassiveSTSSampleApp invoke response is null for tenant domain: " +
                tenantDomain);
        int responseCode = response.getStatusLine().getStatusCode();
        Assert.assertEquals(responseCode, 200, "Invalid Response for tenant domain: " + tenantDomain);

        HttpEntity entity = response.getEntity();
        String responseString = EntityUtils.toString(entity, "UTF-8");
        Assert.assertTrue(responseString.contains("urn:oasis:names:tc:SAML:2.0:assertion"),
                "No SAML2 Assertion found for the SAML2 request for tenant domain: " + tenantDomain);
    }

    @Test(alwaysRun = true, description = "Test PassiveSTS SAML2 Assertion with WReply URL in passive-sts request",
            dependsOnMethods = {"testPassiveSAML2Assertion"})
    public void testPassiveSAML2AssertionWithoutWReply() throws Exception {
        String passiveParams = "?wa=wsignin1.0&wtrealm=PassiveSTSSampleApp";
        String wreqParam = "&wreq=%3Cwst%3ARequestSecurityToken+xmlns%3Awst%3D%22http%3A%2F%2Fdocs.oasis-open.org"
                + "%2Fws-sx%2Fws-trust%2F200512%22%3E%3Cwst%3ATokenType%3Ehttp%3A%2F%2Fdocs.oasis-open.org"
                + "%2Fwss%2Foasis-wss-saml-token-profile-1.1%23SAMLV2.0%3C%2Fwst%3ATokenType%3E%3C%2Fwst"
                + "%3ARequestSecurityToken%3E";

        HttpGet request = new HttpGet(this.passiveStsURL + passiveParams + wreqParam);
        HttpResponse response = client.execute(request);

        Assert.assertNotNull(response, "PassiveSTSSampleApp invoke response is null for tenant domain: " +
                tenantDomain);
        int responseCode = response.getStatusLine().getStatusCode();
        Assert.assertEquals(responseCode, 200, "Invalid Response for tenant domain: " + tenantDomain);

        HttpEntity entity = response.getEntity();
        String responseString = EntityUtils.toString(entity, "UTF-8");
        Assert.assertTrue(responseString.contains("urn:oasis:names:tc:SAML:2.0:assertion"),
                "No SAML2 Assertion found for the SAML2 request without WReply in passive-sts request for " +
                        "tenant domain: " + tenantDomain);
    }

    @Test(alwaysRun = true, description = "Test Soap fault in case invalid WReply URL", dependsOnMethods = {
            "testSendLoginRequestPost"})
    public void testPassiveSAML2AssertionForInvalidWReply() throws Exception {

        String INVALID_PASSIVE_STS_SAMPLE_APP_URL = PASSIVE_STS_SAMPLE_APP_URL + "INVALID";
        String passiveParams = "?wa=wsignin1.0&wreply=" + INVALID_PASSIVE_STS_SAMPLE_APP_URL +
                "&wtrealm=PassiveSTSSampleApp";
        String wreqParam = "&wreq=%3Cwst%3ARequestSecurityToken+xmlns%3Awst%3D%22http%3A%2F%2Fdocs.oasis-open.org"
                + "%2Fws-sx%2Fws-trust%2F200512%22%3E%3Cwst%3ATokenType%3Ehttp%3A%2F%2Fdocs.oasis-open.org"
                + "%2Fwss%2Foasis-wss-saml-token-profile-1.1%23SAMLV2.0%3C%2Fwst%3ATokenType%3E%3C%2Fwst"
                + "%3ARequestSecurityToken%3E";

        HttpGet request = new HttpGet(this.passiveStsURL + passiveParams + wreqParam);
        HttpResponse response = client.execute(request);

        assertNotNull(response, "PassiveSTSSampleApp invoke response is null for tenant domain: " + tenantDomain);
        int responseCode = response.getStatusLine().getStatusCode();
        assertEquals(responseCode, 200, "Invalid Response for tenant domain: " + tenantDomain);

        HttpEntity entity = response.getEntity();
        String responseString = EntityUtils.toString(entity, "UTF-8");
        assertTrue(responseString.contains("soapenv:Fault"),
                "Cannot find soap fault for invalid WReply URL for tenant domain: " + tenantDomain);
    }

    @Test(alwaysRun = true, description = "Test Session Hijacking", dependsOnMethods = {"testPassiveSAML2Assertion"})
    public void testSessionHijacking() throws Exception {

        HttpGet getRequest = new HttpGet(locationHeader.getValue());
        HttpResponse response = client.execute(getRequest);
        String resultPage2 = DataExtractUtil.getContentData(response);
        EntityUtils.consume(response.getEntity());
        Assert.assertTrue(resultPage2.contains("Authentication Error!"), "Session hijacking is possible for " +
                "tenant domain: " + tenantDomain);
    }

    @Test(alwaysRun = true, description = "Test logout request",
            dependsOnMethods = {"testPassiveSAML2Assertion", "testSessionHijacking"})
    public void testSendLogoutRequest() throws Exception {

        String passiveParams = "?wa=wsignout1.0&wreply=" + PASSIVE_STS_SAMPLE_APP_URL + "&wtrealm=PassiveSTSSampleApp";
        HttpGet request = new HttpGet(this.passiveStsURL + passiveParams);
        HttpResponse response = client.execute(request);
        Assert.assertNotNull(response, "PassiveSTSSampleApp logout response is null for tenant domain: " +
                tenantDomain);
        int responseCode = response.getStatusLine().getStatusCode();
        Assert.assertEquals(responseCode, 200, "Invalid Response for tenant domain: " + tenantDomain);

        Map<String, Integer> keyPositionMap = new HashMap<>(1);
        keyPositionMap.put("name=\"sessionDataKey\"", 1);
        List<DataExtractUtil.KeyValue> keyValues = DataExtractUtil.extractDataFromResponse(response,
                keyPositionMap);
        EntityUtils.consume(response.getEntity());
        Assert.assertNotNull(keyValues, "sessionDataKey key value is null for tenant domain: " + tenantDomain +
                "Authentication request was not initiated after logout from sample. Possible logout failure.");
        sessionDataKey = keyValues.get(0).getValue();
        Assert.assertNotNull(sessionDataKey, "Session data key is null for tenant domain: " + tenantDomain);
    }

    @Test(alwaysRun = true, description = "Test logout request with empty wreply url",
            dependsOnMethods = {"testPassiveSAML2Assertion", "testSessionHijacking"})
    public void testSendLogoutRequestEmptyWreply() throws Exception {

        String passiveParams = "?wa=wsignout1.0&wtrealm=PassiveSTSSampleApp";
        HttpGet request = new HttpGet(this.passiveStsURL + passiveParams);
        HttpResponse response = client.execute(request);
        Assert.assertNotNull(response, "PassiveSTSSampleApp logout response is null for tenant domain: " +
                tenantDomain);
        int responseCode = response.getStatusLine().getStatusCode();
        Assert.assertEquals(responseCode, 200, "Invalid Response for tenant domain: " + tenantDomain);

        Map<String, Integer> keyPositionMap = new HashMap<>(1);
        keyPositionMap.put("name=\"sessionDataKey\"", 1);
        List<DataExtractUtil.KeyValue> keyValues = DataExtractUtil.extractDataFromResponse(response,
                keyPositionMap);
        EntityUtils.consume(response.getEntity());
        Assert.assertNotNull(keyValues, "sessionDataKey key value is null for tenant domain: " + tenantDomain +
                "Authentication request was not initiated after logout from sample. Possible logout failure.");
        sessionDataKey = keyValues.get(0).getValue();
        Assert.assertNotNull(sessionDataKey, "Session data key is null for tenant domain: " + tenantDomain);
    }

    @Test(alwaysRun = true, description = "Test logout request with empty wreply url and empty wtealm",
            dependsOnMethods = {"testPassiveSAML2Assertion", "testSessionHijacking"})
    public void testSendLogoutRequestEmptyWreplyAndWtrealm() throws Exception {

        String passiveParams = "?wa=wsignout1.0";
        HttpGet request = new HttpGet(this.passiveStsURL + passiveParams);
        HttpResponse response = client.execute(request);
        String resultPage = DataExtractUtil.getContentData(response);
        Assert.assertTrue(resultPage.contains("Authentication Error!"), "Logout wreply url validation has failed");
    }

    @Test(alwaysRun = true, description = "Test logout request with incorrect wreply url",
            dependsOnMethods = {"testPassiveSAML2Assertion", "testSessionHijacking"})
    public void testSendLogoutRequestIncorrectWreply() throws Exception {

        String passiveParams = "?wa=wsignout1.0&wreply=" + PASSIVE_STS_SAMPLE_APP_URL_INCORRECT + "&wtrealm=PassiveSTSSampleApp";
        HttpGet request = new HttpGet(this.passiveStsURL + passiveParams);
        HttpResponse response = client.execute(request);
        String resultPage = DataExtractUtil.getContentData(response);
        Assert.assertTrue(resultPage.contains("Authentication Error!"), "Logout wreply url validation has failed");
    }

    @Test(alwaysRun = true, description = "Test logout request with empty",
            dependsOnMethods = {"testPassiveSAML2Assertion", "testSessionHijacking"})
    public void testSendLogoutRequestEmptyWtrealm() throws Exception {

        String passiveParams = "?wa=wsignout1.0&wreply=" + PASSIVE_STS_SAMPLE_APP_URL_INCORRECT;
        HttpGet request = new HttpGet(this.passiveStsURL + passiveParams);
        HttpResponse response = client.execute(request);
        String resultPage = DataExtractUtil.getContentData(response);
        Assert.assertTrue(resultPage.contains("Authentication Error!"), "Logout wreply url validation has failed");
    }

    private void setSystemProperties() {
        URL resourceUrl = getClass().getResource(ISIntegrationTest.URL_SEPARATOR + "keystores" + ISIntegrationTest.URL_SEPARATOR
                + "products" + ISIntegrationTest.URL_SEPARATOR + "wso2carbon.p12");
        System.setProperty("javax.net.ssl.trustStore", resourceUrl.getPath());
        System.setProperty("javax.net.ssl.trustStorePassword",
                "wso2carbon");
        System.setProperty("javax.net.ssl.trustStoreType", "PKCS12");
    }

    private ClaimMapping[] getClaimMappings() {
        List<ClaimMapping> claimMappingList = new ArrayList<ClaimMapping>();

        Claim givenNameClaim = new Claim();
        givenNameClaim.setClaimUri(GIVEN_NAME_CLAIM_URI);
        ClaimMapping givenNameClaimMapping = new ClaimMapping();
        givenNameClaimMapping.setRequested(true);
        givenNameClaimMapping.setLocalClaim(givenNameClaim);
        givenNameClaimMapping.setRemoteClaim(givenNameClaim);
        claimMappingList.add(givenNameClaimMapping);

        Claim emailClaim = new Claim();
        emailClaim.setClaimUri(EMAIL_CLAIM_URI);
        ClaimMapping emailClaimMapping = new ClaimMapping();
        emailClaimMapping.setRequested(true);
        emailClaimMapping.setLocalClaim(emailClaim);
        emailClaimMapping.setRemoteClaim(emailClaim);
        claimMappingList.add(emailClaimMapping);

        return claimMappingList.toArray(new ClaimMapping[0]);
    }

    private boolean requestMissingClaims(HttpResponse response) {

        String redirectUrl = Utils.getRedirectUrl(response);
        return redirectUrl.contains("consent.do");
    }
}

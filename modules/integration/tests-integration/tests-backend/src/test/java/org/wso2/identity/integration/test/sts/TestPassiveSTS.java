package org.wso2.identity.integration.test.sts;

import org.apache.catalina.LifecycleException;
import org.apache.catalina.core.StandardHost;
import org.apache.catalina.startup.Tomcat;
import org.apache.http.Header;
import org.apache.http.HttpResponse;
import org.apache.http.NameValuePair;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.message.BasicNameValuePair;
import org.apache.http.util.EntityUtils;
import org.testng.Assert;
import org.testng.annotations.AfterClass;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;
import org.wso2.carbon.integration.common.admin.client.AuthenticatorClient;
import org.wso2.identity.integration.common.clients.application.mgt.ApplicationManagementServiceClient;
import org.wso2.carbon.identity.application.common.model.xsd.*;
import org.wso2.identity.integration.common.utils.ISIntegrationTest;
import org.wso2.identity.integration.test.utils.DataExtractUtil;

import java.io.File;
import java.io.IOException;
import java.net.URL;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class TestPassiveSTS extends ISIntegrationTest {

    private static final String ADMIN_EMAIL = "admin@wso2.com";
    private static final String SERVICE_PROVIDER_NAME = "PassiveSTSSampleApp";
    private static final String SERVICE_PROVIDER_Desc = "PassiveSTS Service Provider";
    private static final String PASSIVE_STS_SAMPLE_APP_NAME = "/PassiveSTSSampleApp";
    private static final String EMAIL_CLAIM_URI = "http://wso2.org/claims/emailaddress";
    private static final String GIVEN_NAME_CLAIM_URI = "http://wso2.org/claims/givenname";
    private static final String PASSIVE_STS_SAMPLE_APP_URL =
            "http://localhost:8090/PassiveSTSSampleApp";
    private static final String COMMON_AUTH_URL =
            "https://localhost:9443/commonauth";
    private static final String HTTP_RESPONSE_HEADER_LOCATION = "location";

    private String adminUsername;
    private String adminPassword;
    private String sessionDataKey;
    private String resultPage;
    private Tomcat tomcat;

    private AuthenticatorClient logManger;
    private ApplicationManagementServiceClient appMgtclient;
    private ServiceProvider serviceProvider;
    private DefaultHttpClient client;

    @BeforeClass(alwaysRun = true)
    public void testInit() throws Exception {
        super.init();

        logManger = new AuthenticatorClient(backendURL);
        adminUsername = userInfo.getUserName();
        adminPassword = userInfo.getPassword();
        logManger.login(isServer.getSuperTenant().getTenantAdmin().getUserName(),
                isServer.getSuperTenant().getTenantAdmin().getPassword(),
                isServer.getInstance().getHosts().get("default"));

        appMgtclient = new ApplicationManagementServiceClient(sessionCookie, backendURL, null);

        client = new DefaultHttpClient();

        setSystemProperties();
    }

    @AfterClass(alwaysRun = true)
    public void atEnd() throws Exception {
        if(tomcat != null){
            tomcat.stop();
            tomcat.destroy();
            Thread.sleep(10000);
        }
    }

    @Test(alwaysRun = true, description = "Deploy PassiveSTSSampleApp")
    public void testDeployPassiveSTSSampleApp() {
        try {
            tomcat = getTomcat();
            URL resourceUrl = getClass().getResource(File.separator + "samples"
                    + File.separator + "PassiveSTSSampleApp.war");
            startTomcat(tomcat, PASSIVE_STS_SAMPLE_APP_NAME, resourceUrl.getPath());

        } catch (Exception e) {
            Assert.fail("PassiveSTSSampleApp application deployment failed.", e);
        }
    }


    @Test(alwaysRun = true, description = "Add service provider")
    public void testAddSP() throws Exception {

        serviceProvider = new ServiceProvider();
        serviceProvider.setApplicationName(SERVICE_PROVIDER_NAME);
        serviceProvider.setDescription(SERVICE_PROVIDER_Desc);
        appMgtclient.createApplication(serviceProvider);
        serviceProvider = appMgtclient.getApplication(SERVICE_PROVIDER_NAME);
        Assert.assertNotNull(serviceProvider, "Service provider registration failed.");
    }

    @Test(alwaysRun = true, description = "Update service provider with passiveSTS configs",
            dependsOnMethods = { "testAddSP" })
    public void testUpdateSP() throws Exception {

        serviceProvider.setOutboundProvisioningConfig(new OutboundProvisioningConfig());
        List<InboundAuthenticationRequestConfig> authRequestList = new ArrayList<InboundAuthenticationRequestConfig>();
        String passiveSTSRealm = SERVICE_PROVIDER_NAME;
        if (passiveSTSRealm != null) {
            InboundAuthenticationRequestConfig opicAuthenticationRequest = new InboundAuthenticationRequestConfig();
            opicAuthenticationRequest.setInboundAuthKey(passiveSTSRealm);
            opicAuthenticationRequest.setInboundAuthType("passivests");
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

    @Test(alwaysRun = true, description = "Update service provider with claim configurations",
            dependsOnMethods = { "testUpdateSP" })
    public void testAddClaimConfiguration() throws Exception {

        serviceProvider.getClaimConfig().setClaimMappings(getClaimMappings());
        appMgtclient.updateApplicationData(serviceProvider);
        ServiceProvider updatedServiceProvider = appMgtclient.getApplication(SERVICE_PROVIDER_NAME);
        ClaimConfig updatedClaimConfig = updatedServiceProvider.getClaimConfig();

        Assert.assertEquals(updatedClaimConfig.getClaimMappings()[0].getLocalClaim().getClaimUri(),
                            GIVEN_NAME_CLAIM_URI, "Failed update given name claim uri");

        Assert.assertEquals(updatedClaimConfig.getClaimMappings()[1].getLocalClaim().getClaimUri(),
                            EMAIL_CLAIM_URI, "Failed update email claim uri");
    }

    @Test(alwaysRun = true, description = "Invoke PassiveSTSSampleApp",
            dependsOnMethods = {"testDeployPassiveSTSSampleApp", "testAddClaimConfiguration"})
    public void testInvokePassiveSTSSampleApp() throws IOException {
        HttpGet request = new HttpGet(PASSIVE_STS_SAMPLE_APP_URL);
        HttpResponse response = client.execute(request);
        Assert.assertNotNull(response, "PassiveSTSSampleApp invoke response is null");
        int responseCode = response.getStatusLine().getStatusCode();
        Assert.assertEquals(responseCode, 200, "Invalid Response");

        Map<String, Integer> keyPositionMap = new HashMap<String, Integer>(1);
        keyPositionMap.put("name=\"sessionDataKey\"", 1);
        List<DataExtractUtil.KeyValue> keyValues = DataExtractUtil.extractDataFromResponse(response,
                keyPositionMap);
        Assert.assertNotNull(keyValues, "sessionDataKey key value is null");
        sessionDataKey = keyValues.get(0).getValue();
        Assert.assertNotNull(sessionDataKey, "Session data key is null.");
        EntityUtils.consume(response.getEntity());
    }

    @Test(alwaysRun = true, description = "Send login post request", dependsOnMethods =
            { "testInvokePassiveSTSSampleApp" })
    public void testSendLoginRequestPost() throws Exception {

        HttpPost request = new HttpPost(COMMON_AUTH_URL);
        List<NameValuePair> urlParameters = new ArrayList<NameValuePair>();
        urlParameters.add(new BasicNameValuePair("username", adminUsername));
        urlParameters.add(new BasicNameValuePair("password", adminPassword));
        urlParameters.add(new BasicNameValuePair("sessionDataKey", sessionDataKey));
        request.setEntity(new UrlEncodedFormEntity(urlParameters));

        HttpResponse response = client.execute(request);
        Assert.assertNotNull(response, "Login response is null.");
        Assert.assertEquals(response.getStatusLine().getStatusCode(), 302, "Invalid Response");

        Header locationHeader = response.getFirstHeader(HTTP_RESPONSE_HEADER_LOCATION);
        Assert.assertNotNull(locationHeader, "Login response header is null");

        HttpGet getRequest = new HttpGet(locationHeader.getValue());
        EntityUtils.consume(response.getEntity());
        response = client.execute(getRequest);
        resultPage = DataExtractUtil.getContentData(response);
        EntityUtils.consume(response.getEntity());

    }

    @Test(alwaysRun = true, description = "Test PassiveSTS Claims",
            dependsOnMethods = { "testSendLoginRequestPost" })
    public void testPassiveSTSClaims() {

        Assert.assertTrue(resultPage.contains(GIVEN_NAME_CLAIM_URI), "Claim givenname is expected");
        Assert.assertTrue(resultPage.contains(adminUsername), "Claim value givenname is expected");

        Assert.assertTrue(resultPage.contains(EMAIL_CLAIM_URI), "Claim email is expected");
        Assert.assertTrue(resultPage.contains(ADMIN_EMAIL), "Claim value email is expected");
    }

    private void startTomcat(Tomcat tomcat, String webAppUrl, String webAppPath)
            throws LifecycleException {
        tomcat.addWebapp(tomcat.getHost(), webAppUrl, webAppPath);
        tomcat.start();
    }

    private Tomcat getTomcat() {
        Tomcat tomcat = new Tomcat();
        tomcat.getService().setContainer(tomcat.getEngine());
        tomcat.setPort(8090);
        tomcat.setBaseDir("");

        StandardHost stdHost = (StandardHost) tomcat.getHost();

        stdHost.setAppBase("");
        stdHost.setAutoDeploy(true);
        stdHost.setDeployOnStartup(true);
        stdHost.setUnpackWARs(true);
        tomcat.setHost(stdHost);

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

        return claimMappingList.toArray(new ClaimMapping[claimMappingList.size()]);
    }

}

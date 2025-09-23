/*
 *  Copyright (c) 2018 WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
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

package org.wso2.identity.integration.test.auth;

import com.google.gson.Gson;
import com.google.gson.JsonObject;
import org.apache.axis2.context.ConfigurationContext;
import org.apache.axis2.context.ConfigurationContextFactory;
import org.apache.commons.io.FileUtils;
import org.apache.http.Header;
import org.apache.http.HttpResponse;
import org.apache.http.NameValuePair;
import org.apache.http.client.CookieStore;
import org.apache.http.client.HttpClient;
import org.apache.http.client.config.CookieSpecs;
import org.apache.http.client.config.RequestConfig;
import org.apache.http.config.Lookup;
import org.apache.http.config.RegistryBuilder;
import org.apache.http.cookie.CookieSpecProvider;
import org.apache.http.impl.client.BasicCookieStore;
import org.apache.http.impl.client.HttpClientBuilder;
import org.apache.http.impl.cookie.RFC6265CookieSpecProvider;
import org.apache.http.message.BasicNameValuePair;
import org.apache.http.util.EntityUtils;
import org.testng.Assert;
import org.testng.annotations.AfterClass;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Optional;
import org.testng.annotations.Parameters;
import org.testng.annotations.Test;
import org.wso2.carbon.identity.application.common.model.idp.xsd.IdentityProvider;
import org.wso2.carbon.identity.application.common.model.xsd.AuthenticationStep;
import org.wso2.carbon.identity.application.common.model.xsd.LocalAndOutboundAuthenticationConfig;
import org.wso2.carbon.identity.application.common.model.xsd.LocalAuthenticatorConfig;
import org.wso2.carbon.identity.application.common.model.xsd.ServiceProvider;
import org.wso2.carbon.integration.common.admin.client.AuthenticatorClient;
import org.wso2.carbon.integration.common.utils.mgt.ServerConfigurationManager;
import org.wso2.identity.integration.common.clients.application.mgt.ApplicationManagementServiceClient;
import org.wso2.identity.integration.common.clients.oauth.OauthAdminClient;
import org.wso2.identity.integration.common.utils.ISIntegrationTest;
import org.wso2.identity.integration.common.utils.MicroserviceServer;
import org.wso2.identity.integration.common.utils.MicroserviceUtil;
import org.wso2.identity.integration.test.util.Utils;
import org.wso2.identity.integration.test.utils.DataExtractUtil;
import org.wso2.identity.integration.test.utils.OAuth2Constant;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.ws.rs.Consumes;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;

import static org.wso2.identity.integration.test.utils.OAuth2Constant.CALLBACK_URL;
import static org.wso2.identity.integration.test.utils.OAuth2Constant.COMMON_AUTH_URL;
import static org.wso2.identity.integration.test.utils.OAuth2Constant.SESSION_DATA_KEY;

@Path("/")
public class RiskBasedLoginTestCase extends AbstractAdaptiveAuthenticationTestCase {

    private static final String PRIMARY_IS_APPLICATION_NAME = "testOauthApp";
    public static final String ANALYTICS_PAYLOAD_JSON = "analytics-payload.json";

    private AuthenticatorClient logManger;
    private OauthAdminClient oauthAdminClient;
    private ApplicationManagementServiceClient applicationManagementServiceClient;
    private CookieStore cookieStore = new BasicCookieStore();
    private HttpClient client;
    private HttpResponse response;
    private ServerConfigurationManager serverConfigurationManager;
    private boolean openJDKNashornEnabled = false;

    private Map<String, Integer> userRiskScores = new HashMap<>();

    private ServiceProvider serviceProvider;

    MicroserviceServer microserviceServer;

    @BeforeClass(alwaysRun = true)
    @Parameters({"scriptEngine"})
    public void testInit(@Optional("graaljs") String scriptEngine) throws Exception {

        super.init();

        InputStream webappUrl = getClass()
                .getResourceAsStream(ISIntegrationTest.URL_SEPARATOR + "samples" + ISIntegrationTest.URL_SEPARATOR +
                        "authenticators" + ISIntegrationTest.URL_SEPARATOR + "sample-auth.war");

        InputStream jarUrl = getClass()
                .getResourceAsStream(ISIntegrationTest.URL_SEPARATOR + "samples" + ISIntegrationTest.URL_SEPARATOR +
                        "authenticators" + ISIntegrationTest.URL_SEPARATOR +
                        "org.wso2.carbon.identity.sample.extension.authenticators.jar");

        String authenticatorPathString = Utils.getResidentCarbonHome()
                + File.separator + "repository"
                + File.separator + "components" + File.separator
                + "dropins" + File.separator + "org.wso2.carbon.identity.sample.extension.authenticators.jar";
        File jarDestFile = new File(authenticatorPathString);
        FileOutputStream jarDest = new FileOutputStream(jarDestFile);
        copyFileUsingStream(jarUrl, jarDest);
        log.info("Copied the demo authenticator jar file to " + authenticatorPathString);
        Assert.assertTrue(Files.exists(Paths.get(authenticatorPathString)), "Demo Authenticator is not copied " +
                "successfully. File path: " + authenticatorPathString);

        String authenticatorWarPathString = Utils.getResidentCarbonHome()
                + File.separator + "repository" + File.separator + "deployment" + File.separator
                + "server" + File.separator + "webapps" + File.separator + "sample-auth.war";
        File warDestFile = new File(authenticatorWarPathString);
        FileOutputStream warDest = new FileOutputStream(warDestFile);
        copyFileUsingStream(webappUrl, warDest);

        // Waiting for the war file to deploy.
        String authenticatorWebappPathString = Utils.getResidentCarbonHome()
                + File.separator + "repository" + File.separator + "deployment" + File.separator
                + "server" + File.separator + "webapps" + File.separator + "sample-auth";
        waitForWebappToDeploy(authenticatorWebappPathString);

        log.info("Copied the demo authenticator war file to " + authenticatorWarPathString);
        Assert.assertTrue(Files.exists(Paths.get(authenticatorWarPathString)), "Demo Authenticator war is not copied " +
                "successfully. File path: " + authenticatorWarPathString);

        log.info("Restarting the server at: " + isServer.getContextUrls().getBackEndUrl());
        serverConfigurationManager = new ServerConfigurationManager(isServer);
        changeISConfiguration(scriptEngine);
        log.info("Restarting the server at: " + isServer.getContextUrls().getBackEndUrl() + " is successful");

        super.init();
        logManger = new AuthenticatorClient(backendURL);
        String cookie = this.logManger.login(isServer.getSuperTenant().getTenantAdmin().getUserName(),
                isServer.getSuperTenant().getTenantAdmin().getPassword(),
                isServer.getInstance().getHosts().get("default"));
        oauthAdminClient = new OauthAdminClient(backendURL, cookie);
        ConfigurationContext configContext = ConfigurationContextFactory
                .createConfigurationContextFromFileSystem(null, null);
        applicationManagementServiceClient = new ApplicationManagementServiceClient(sessionCookie, backendURL,
                configContext);

        Lookup<CookieSpecProvider> cookieSpecRegistry = RegistryBuilder.<CookieSpecProvider>create()
                .register(CookieSpecs.DEFAULT, new RFC6265CookieSpecProvider())
                .build();
        RequestConfig requestConfig = RequestConfig.custom()
                .setCookieSpec(CookieSpecs.DEFAULT)
                .build();
        client = HttpClientBuilder.create()
                .disableRedirectHandling()
                .setDefaultCookieStore(cookieStore)
                .setDefaultRequestConfig(requestConfig)
                .setDefaultCookieSpecRegistry(cookieSpecRegistry)
                .build();

        String script = getConditionalAuthScript("RiskBasedLoginScript.js");

        createOauthApp(CALLBACK_URL, PRIMARY_IS_APPLICATION_NAME, oauthAdminClient);
        // Create service provider in primary IS with conditional authentication script enabled.
        serviceProvider = createServiceProvider(PRIMARY_IS_APPLICATION_NAME,
                applicationManagementServiceClient, oauthAdminClient, script);

        microserviceServer = MicroserviceUtil.initMicroserviceServer();
        MicroserviceUtil.deployService(microserviceServer, this);

        IdentityProvider superTenantResidentIDP = superTenantIDPMgtClient.getResidentIdP();
        updateResidentIDPProperty(superTenantResidentIDP, "adaptive_authentication.analytics.receiver",
                "http://localhost:" + microserviceServer.getPort());

        userRiskScores.put(userInfo.getUserName(), 0);
    }

    private void changeAdaptiveAuthenticationScript() throws Exception {

        String script = getConditionalAuthScript("RiskBasedLoginScriptPayload.js");
        serviceProvider.getLocalAndOutBoundAuthenticationConfig().getAuthenticationScriptConfig().setContent(script);
        applicationManagementServiceClient.updateApplicationData(serviceProvider);
    }

    private void changeISConfiguration(String scriptEngine) throws Exception {

        String identityNewResourceFileName = "identity_new_resource.toml";
        if (scriptEngine.equalsIgnoreCase("nashorn")) {
            if (Utils.getJavaVersion() >= 15) {
                identityNewResourceFileName = "identity_new_resource_openjdknashorn.toml";
                NashornAdaptiveScriptInitializerTestCase.runAdaptiveAuthenticationDependencyScript(false,
                        serverConfigurationManager, log);
                openJDKNashornEnabled = true;
            } else {
                identityNewResourceFileName = "identity_new_resource_nashorn.toml";
            }
        }

        String carbonHome = Utils.getResidentCarbonHome();
        File defaultTomlFile = getDeploymentTomlFile(carbonHome);
        File configuredTomlFile = new File(getISResourceLocation() + File.separator
                + identityNewResourceFileName);
        serverConfigurationManager = new ServerConfigurationManager(isServer);
        serverConfigurationManager.applyConfigurationWithoutRestart(configuredTomlFile, defaultTomlFile, true);
        serverConfigurationManager.restartGracefully();
    }

    private void resetISConfiguration() throws Exception {

        serverConfigurationManager.restoreToLastConfiguration(false);
        if (openJDKNashornEnabled) {
            NashornAdaptiveScriptInitializerTestCase.runAdaptiveAuthenticationDependencyScript(true,
                    serverConfigurationManager, log);
        }
    }

    private void waitForWebappToDeploy(String authenticatorWebappPathString) {

        long startTime = System.currentTimeMillis();

        while (System.currentTimeMillis() - startTime < 120000L) {
            if (Files.exists(Paths.get(authenticatorWebappPathString))) {
                log.info(authenticatorWebappPathString + " deployed successfully.");
                break;
            }
            try {
                Thread.sleep(1000L);
            } catch (InterruptedException e) {
                // ignore
            }
        }
    }

    @AfterClass(alwaysRun = true)
    public void atEnd() throws Exception {

        oauthAdminClient.removeOAuthApplicationData(consumerKey);
        applicationManagementServiceClient.deleteApplication(PRIMARY_IS_APPLICATION_NAME);
        client.getConnectionManager().shutdown();

        this.logManger.logOut();
        logManger = null;

        MicroserviceUtil.destroyService(microserviceServer);

        File jarDestFile = new File(Utils.getResidentCarbonHome()
                + File.separator + File.separator + "repository"
                + File.separator + "components" + File.separator
                + "dropins" + File.separator + "org.wso2.carbon.identity.sample.extension.authenticators.jar");
        jarDestFile.delete();
        boolean deleted = deleteWebApp("sample-auth");
        deleted = deleted || deleteWebApp("sample-auth");
        Assert.assertTrue(deleted, "sample-auth webapp deletion failed.");

        log.info("Replacing with default configurations.");
        resetISConfiguration();
    }

    /**
     * Deletes a webapp from the is server.
     * <p>
     * This first tries to delete the webapp via the webapp admin service.
     * Failing that, it tries to delete the war and exploded dir manually.
     *
     * @param webappName the name of the webapp to be deleted.
     * @return deletion status
     * @throws Exception for any unhandled exceptions in this test utility
     */
    private boolean deleteWebApp(String webappName) throws Exception {

        File warDestFile = new File(Utils.getResidentCarbonHome()
                + File.separator + File.separator + "repository"
                + File.separator + "deployment" + File.separator
                + "server" + File.separator + "webapps" + File.separator + webappName + ".war");
        File warDestFolder = new File(Utils.getResidentCarbonHome()
                + File.separator + File.separator + "repository"
                + File.separator + "deployment" + File.separator
                + "server" + File.separator + "webapps" + File.separator + webappName);
        if (warDestFile.exists() || warDestFolder.exists()) {
            log.warn("Webapp deletion via WebAppAdmin service failed. Trying manual deletion..");
            boolean deleted = warDestFile.delete();

            try {
                FileUtils.deleteDirectory(warDestFolder);
            } catch (IOException e) {
                log.error("Error while deleting webapp directory: " + warDestFile, e);
                return false;
            }
            return deleted;
        }
        return true;
    }

    @Test(groups = "wso2.is", description = "Check conditional authentication flow.")
    public void testAuthenticationForNoRisk() throws Exception {

        response = loginWithOIDC(PRIMARY_IS_APPLICATION_NAME, consumerKey, client);

        EntityUtils.consume(response.getEntity());

        Header locationHeader = response.getFirstHeader(OAuth2Constant.HTTP_RESPONSE_HEADER_LOCATION);
        Assert.assertNotNull(locationHeader, "Login response header is null");
        response = sendGetRequest(client, locationHeader.getValue());
        EntityUtils.consume(response.getEntity());
        locationHeader = response.getFirstHeader(OAuth2Constant.HTTP_RESPONSE_HEADER_LOCATION);

        log.info("############## " + "testAuthenticationForNoRisk - location header " + locationHeader.getValue());

        URL clientUrl = new URL(locationHeader.getValue());
        Assert.assertTrue(clientUrl.getQuery().contains("code="), "Authentication flow was un-successful with " +
                "identifier first login");

    }

    @Test(groups = "wso2.is", description = "Check conditional authentication flow.")
    public void testAuthenticationForRisk() throws Exception {

        userRiskScores.put(userInfo.getUserName(), 1);
        cookieStore.clear();

        response = loginWithOIDC(PRIMARY_IS_APPLICATION_NAME, consumerKey, client);

        EntityUtils.consume(response.getEntity());

        Header locationHeader = response.getFirstHeader(OAuth2Constant.HTTP_RESPONSE_HEADER_LOCATION);

        String callbackUrl = DataExtractUtil.getParamFromURIString(locationHeader.getValue(), "callbackUrl");
        if (callbackUrl == null) {
            callbackUrl = COMMON_AUTH_URL + String.format("?sessionDataKey=%s&authenticatorName=%s",
                    DataExtractUtil.getParamFromURIString(locationHeader.getValue(), SESSION_DATA_KEY),
                    "DemoFingerprintAuthenticator");
        }
        String[] urlParts = locationHeader.getValue().split("\\?");

        List<NameValuePair> urlParameters = new ArrayList<>();
        urlParameters.add(new BasicNameValuePair("fingerprint", "fingerprint"));
        urlParameters.add(new BasicNameValuePair("callbackUrl", callbackUrl));

        response = sendPostRequestWithParameters(client, urlParameters, urlParts[0]);
        EntityUtils.consume(response.getEntity());
        locationHeader = response.getFirstHeader(OAuth2Constant.HTTP_RESPONSE_HEADER_LOCATION);
        log.info("############## " + "testAuthenticationForRisk - location header 1 " + locationHeader.getValue());


        response = sendGetRequest(client, locationHeader.getValue());
        EntityUtils.consume(response.getEntity());
        locationHeader = response.getFirstHeader(OAuth2Constant.HTTP_RESPONSE_HEADER_LOCATION);
        log.info("############## " + "testAuthenticationForRisk - location header 2 " + locationHeader.getValue());

        response = sendGetRequest(client, locationHeader.getValue());
        EntityUtils.consume(response.getEntity());
        locationHeader = response.getFirstHeader(OAuth2Constant.HTTP_RESPONSE_HEADER_LOCATION);
        log.info("############## " + "testAuthenticationForRisk - location header 3 " + locationHeader.getValue());

        URL clientUrl = new URL(locationHeader.getValue());
        Assert.assertTrue(clientUrl.getQuery().contains("code="), "Authentication flow was un-successful with " +
                "risk based login");
    }

    @Test(groups = "wso2.is", description = "Check conditional authentication flow.")
    public void testAuthenticationForRiskWithComplexPayload() throws Exception {

        changeAdaptiveAuthenticationScript();
        cookieStore.clear();
        response = loginWithOIDC(PRIMARY_IS_APPLICATION_NAME, consumerKey, client);

        EntityUtils.consume(response.getEntity());

        Header locationHeader = response.getFirstHeader(OAuth2Constant.HTTP_RESPONSE_HEADER_LOCATION);
        Assert.assertNotNull(locationHeader, "Login response header is null");
        response = sendGetRequest(client, locationHeader.getValue());
        EntityUtils.consume(response.getEntity());
        locationHeader = response.getFirstHeader(OAuth2Constant.HTTP_RESPONSE_HEADER_LOCATION);

        log.info("############## " + "testAuthenticationForNoRisk - location header " + locationHeader.getValue());

        URL clientUrl = new URL(locationHeader.getValue());
        Assert.assertTrue(clientUrl.getQuery().contains("code="), "Authentication flow was un-successful with " +
                "identifier first login");

    }

    @POST
    @Path("/{appName}/{inputStream}")
    @Consumes("application/json")
    @Produces("application/json")
    public Map<String, Map<String, String>> analyticsReceiver(@PathParam("appName") String appName,
                                                              @PathParam("inputStream") String inputStream,
                                                              Map<String, Map<String, String>> data) {

        Map<String, String> event = data.get("event");
        String username = event.get("username");
        Integer riskScore = userRiskScores.get(username);
        Map<String, String> responseEvent = new HashMap<>();
        responseEvent.put("username", username);
        responseEvent.put("riskScore", String.valueOf(riskScore));
        Map<String, Map<String, String>> response = new HashMap<>();
        response.put("event", responseEvent);
        return response;

    }

    @POST
    @Path("/risk-based-login-endpoint")
    @Consumes("application/json")
    @Produces("application/json")
    public Map<String, Object> analyticsPayloadReceiver(Map<String, Object> data) throws Exception {

        JsonObject expectedPayload = getJsonObjectFromFile(ANALYTICS_PAYLOAD_JSON);
        Gson gson = new Gson();
        String dataStr = gson.toJson(data.get("event"));
        Map<String, Object> response = new HashMap<>();
        Map<String, String> responseEvent = new HashMap<>();
        responseEvent.put("riskScore", String.valueOf(0));
        JsonObject actualPayload = gson.fromJson(dataStr, JsonObject.class);

        response.put("data", expectedPayload);

        if (expectedPayload.equals(actualPayload)) {
            response.put("event", responseEvent);
            return response;
        }
        throw new RuntimeException("Expected payload and received payload do not match.");
    }

    protected LocalAndOutboundAuthenticationConfig createLocalAndOutboundAuthenticationConfig() throws Exception {

        LocalAndOutboundAuthenticationConfig localAndOutboundAuthenticationConfig =
                super.createLocalAndOutboundAuthenticationConfig();

        AuthenticationStep authenticationStep2 = new AuthenticationStep();
        authenticationStep2.setStepOrder(2);
        LocalAuthenticatorConfig localConfig = new LocalAuthenticatorConfig();
        localConfig.setName("DemoFingerprintAuthenticator");
        localConfig.setDisplayName("Demo Fingerprint Authenticator");
        localConfig.setEnabled(true);
        authenticationStep2.setLocalAuthenticatorConfigs(new LocalAuthenticatorConfig[]{localConfig});
        authenticationStep2.setSubjectStep(false);
        authenticationStep2.setAttributeStep(false);
        localAndOutboundAuthenticationConfig.addAuthenticationSteps(authenticationStep2);

        return localAndOutboundAuthenticationConfig;
    }
}

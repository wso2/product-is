package org.wso2.identity.integration.test.auth;

import com.google.gson.Gson;
import org.apache.axis2.context.ConfigurationContext;
import org.apache.axis2.context.ConfigurationContextFactory;
import org.apache.commons.io.FileUtils;
import org.apache.http.Header;
import org.apache.http.HttpResponse;
import org.apache.http.NameValuePair;
import org.apache.http.client.CookieStore;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.BasicCookieStore;
import org.apache.http.impl.client.HttpClientBuilder;
import org.apache.http.message.BasicNameValuePair;
import org.apache.http.util.EntityUtils;
import org.testng.Assert;
import org.testng.annotations.AfterClass;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;
import org.wso2.carbon.automation.extensions.servers.carbonserver.MultipleServersManager;
import org.wso2.carbon.identity.application.common.model.idp.xsd.IdentityProvider;
import org.wso2.carbon.identity.application.common.model.xsd.AuthenticationStep;
import org.wso2.carbon.identity.application.common.model.xsd.LocalAndOutboundAuthenticationConfig;
import org.wso2.carbon.identity.application.common.model.xsd.LocalAuthenticatorConfig;
import org.wso2.carbon.identity.common.testng.InjectMicroservicePort;
import org.wso2.carbon.identity.common.testng.WithMicroService;
import org.wso2.carbon.integration.common.admin.client.AuthenticatorClient;
import org.wso2.carbon.integration.common.utils.mgt.ServerConfigurationManager;
import org.wso2.identity.integration.common.clients.application.mgt.ApplicationManagementServiceClient;
import org.wso2.identity.integration.common.clients.oauth.OauthAdminClient;
import org.wso2.identity.integration.test.util.Utils;
import org.wso2.identity.integration.test.utils.CommonConstants;
import org.wso2.identity.integration.test.utils.DataExtractUtil;
import org.wso2.identity.integration.test.utils.OAuth2Constant;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.URL;
import java.net.URLDecoder;
import java.nio.channels.FileChannel;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
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

@WithMicroService
@Path("/")
public class RiskBasedLoginTestCase extends AbstractConditionalAuthenticationTestCase {

    private static final String PRIMARY_IS_APPLICATION_NAME = "testOauthApp";

    private AuthenticatorClient logManger;
    private OauthAdminClient oauthAdminClient;
    private ApplicationManagementServiceClient applicationManagementServiceClient;
    private CookieStore cookieStore = new BasicCookieStore();
    private HttpClient client;
    private HttpResponse response;
    private List<NameValuePair> consentParameters = new ArrayList<>();
    private ServerConfigurationManager serverConfigurationManager;
    private IdentityProvider superTenantResidentIDP;

    Map<String, Integer> userRiskScores = new HashMap<>();

    @InjectMicroservicePort
    private int microServicePort;

    @BeforeClass(alwaysRun = true)
    public void testInit() throws Exception {

        super.init();

        InputStream webappUrl = getClass()
                .getResourceAsStream(File.separator + "samples" + File.separator + "authenticators" + File.separator +
                        "sample-auth.war");

        InputStream jarUrl = getClass()
                .getResourceAsStream(File.separator + "samples" + File.separator + "authenticators" + File.separator +
                        "org.wso2.carbon.identity.sample.extension.authenticators.jar");

        File jarDestFile = new File(Utils.getResidentCarbonHome()
                + File.separator + File.separator + "repository"
                + File.separator + "components" + File.separator
                + "dropins" + File.separator + "org.wso2.carbon.identity.sample.extension.authenticators.jar");
        FileOutputStream jarDest = new FileOutputStream(jarDestFile);
        copyFileUsingStream(jarUrl, jarDest);

        File warDestFile = new File(Utils.getResidentCarbonHome()
                + File.separator + File.separator + "repository"
                + File.separator + "deployment" + File.separator
                + "server" + File.separator + "webapps" + File.separator + "sample-auth.war");
        FileOutputStream warDest = new FileOutputStream(warDestFile);
        copyFileUsingStream(webappUrl, warDest);

        serverConfigurationManager = new ServerConfigurationManager(isServer);
        serverConfigurationManager.restartGracefully();

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

        client = HttpClientBuilder.create()
                .disableRedirectHandling()
                .setDefaultCookieStore(cookieStore)
                .build();

        String script = getConditionalAuthScript("RiskBasedLoginScript.js");

        createOauthApp(CALLBACK_URL, PRIMARY_IS_APPLICATION_NAME, oauthAdminClient);
        // Create service provider in primary IS with conditional authentication script enabled.
        createServiceProvider(PRIMARY_IS_APPLICATION_NAME,
                applicationManagementServiceClient, oauthAdminClient, script);

        superTenantResidentIDP = superTenantIDPMgtClient.getResidentIdP();
        updateResidentIDPProperty(superTenantResidentIDP, "adaptive_authentication.analytics.receiver", "http://localhost:" + microServicePort);

        userRiskScores.put(userInfo.getUserName(), 0);
    }

    @AfterClass(alwaysRun = true)
    public void atEnd() throws Exception {

        oauthAdminClient.removeOAuthApplicationData(consumerKey);
        applicationManagementServiceClient.deleteApplication(PRIMARY_IS_APPLICATION_NAME);
        client.getConnectionManager().shutdown();

        this.logManger.logOut();
        logManger = null;

        File jarDestFile = new File(Utils.getResidentCarbonHome()
                + File.separator + File.separator + "repository"
                + File.separator + "components" + File.separator
                + "dropins" + File.separator + "org.wso2.carbon.identity.sample.extension.authenticators.jar");
        jarDestFile.delete();

        File warDestFile = new File(Utils.getResidentCarbonHome()
                + File.separator + File.separator + "repository"
                + File.separator + "deployment" + File.separator
                + "server" + File.separator + "webapps" + File.separator + "sample-auth.war");
        warDestFile.delete();
        File warDestFolder = new File(Utils.getResidentCarbonHome()
                + File.separator + File.separator + "repository"
                + File.separator + "deployment" + File.separator
                + "server" + File.separator + "webapps" + File.separator + "sample-auth");
        FileUtils.deleteDirectory(warDestFolder);

        serverConfigurationManager = new ServerConfigurationManager(isServer);
        serverConfigurationManager.restartGracefully();
    }

    @Test(groups = "wso2.is", description = "Check conditional authentication flow.")
    public void testAuthenticationForNoRisk() throws Exception {

        response = loginWithOIDC(PRIMARY_IS_APPLICATION_NAME, consumerKey, client);

        EntityUtils.consume(response.getEntity());

        Header locationHeader = response.getFirstHeader(OAuth2Constant.HTTP_RESPONSE_HEADER_LOCATION);
        Assert.assertNotNull(locationHeader, "Login response header is null");
        locationHeader = handleConsent(locationHeader);

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
        String[] urlParts = locationHeader.getValue().split("\\?");

        List<NameValuePair> urlParameters = new ArrayList<>();
        urlParameters.add(new BasicNameValuePair("fingerprint", "fingerprint"));
        urlParameters.add(new BasicNameValuePair("callbackUrl", callbackUrl));

        response = sendPostRequestWithParameters(client, urlParameters, urlParts[0]);
        locationHeader = response.getFirstHeader(OAuth2Constant.HTTP_RESPONSE_HEADER_LOCATION);
        EntityUtils.consume(response.getEntity());

        response = sendGetRequest(client, locationHeader.getValue());
        locationHeader = response.getFirstHeader(OAuth2Constant.HTTP_RESPONSE_HEADER_LOCATION);
        EntityUtils.consume(response.getEntity());

        Assert.assertNotNull(locationHeader, "Login response header is null");
        locationHeader = handleConsent(locationHeader);

        URL clientUrl = new URL(locationHeader.getValue());
        Assert.assertTrue(clientUrl.getQuery().contains("code="), "Authentication flow was un-successful with " +
                "risk based login");
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

    protected LocalAndOutboundAuthenticationConfig createLocalAndOutboundAuthenticationConfig() throws Exception {

        LocalAndOutboundAuthenticationConfig localAndOutboundAuthenticationConfig = super.createLocalAndOutboundAuthenticationConfig();

        AuthenticationStep authenticationStep2 = new AuthenticationStep();
        authenticationStep2.setStepOrder(2);
        LocalAuthenticatorConfig localConfig = new LocalAuthenticatorConfig();
        localConfig.setName("SampleFingerprintAuthenticator");
        localConfig.setDisplayName("fingerPrintAuth");
        localConfig.setEnabled(true);
        authenticationStep2.setLocalAuthenticatorConfigs(new LocalAuthenticatorConfig[]{localConfig});
        authenticationStep2.setSubjectStep(false);
        authenticationStep2.setAttributeStep(false);
        localAndOutboundAuthenticationConfig.addAuthenticationSteps(authenticationStep2);

        return localAndOutboundAuthenticationConfig;
    }

    private Header handleConsent(Header locationHeader) throws Exception {

        response = sendConsentGetRequest(locationHeader.getValue(), cookieStore, consentParameters);
        Map<String, Integer> keyPositionMap = new HashMap<>(1);
        keyPositionMap.put("name=\"sessionDataKeyConsent\"", 1);
        List<DataExtractUtil.KeyValue> keyValues =
                DataExtractUtil.extractSessionConsentDataFromResponse(response,
                        keyPositionMap);
        Assert.assertNotNull(keyValues, "SessionDataKeyConsent key value is null");

        String sessionDataKeyConsent = keyValues.get(0).getValue();
        Assert.assertNotNull(sessionDataKeyConsent, "Invalid session key consent.");
        EntityUtils.consume(response.getEntity());

        HttpResponse response = sendApprovalPostWithConsent(client, sessionDataKeyConsent, consentParameters);
        Assert.assertNotNull(response, "Approval request failed. response is invalid.");

        locationHeader =
                response.getFirstHeader(OAuth2Constant.HTTP_RESPONSE_HEADER_LOCATION);
        Assert.assertNotNull(locationHeader, "Approval request failed. Location header is null.");
        EntityUtils.consume(response.getEntity());
        return locationHeader;
    }
}

/*
 * Copyright (c) 2024, WSO2 LLC. (http://www.wso2.org) All Rights Reserved.
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
package org.wso2.identity.integration.test.auth;

import com.google.gson.Gson;
import com.google.gson.JsonObject;
import com.nimbusds.jose.JOSEException;
import com.nimbusds.jose.JOSEObjectType;
import com.nimbusds.jose.JWSAlgorithm;
import com.nimbusds.jose.JWSHeader;
import com.nimbusds.jose.crypto.RSASSASigner;
import com.nimbusds.jose.jwk.KeyUse;
import com.nimbusds.jose.jwk.RSAKey;
import com.nimbusds.jose.jwk.gen.RSAKeyGenerator;
import com.nimbusds.jwt.JWTClaimsSet;
import com.nimbusds.jwt.SignedJWT;
import org.apache.axis2.context.ConfigurationContext;
import org.apache.axis2.context.ConfigurationContextFactory;
import org.apache.commons.io.FileUtils;
import org.apache.http.Header;
import org.apache.http.HttpResponse;
import org.apache.http.client.CookieStore;
import org.apache.http.client.config.CookieSpecs;
import org.apache.http.client.config.RequestConfig;
import org.apache.http.config.Lookup;
import org.apache.http.config.RegistryBuilder;
import org.apache.http.cookie.CookieSpecProvider;
import org.apache.http.impl.client.BasicCookieStore;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClientBuilder;
import org.apache.http.impl.cookie.RFC6265CookieSpecProvider;
import org.apache.http.util.EntityUtils;
import org.testng.Assert;
import org.testng.annotations.AfterClass;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;
import org.wso2.carbon.identity.application.common.model.xsd.ServiceProvider;
import org.wso2.carbon.integration.common.admin.client.AuthenticatorClient;
import org.wso2.carbon.integration.common.utils.mgt.ServerConfigurationManager;
import org.wso2.identity.integration.common.clients.application.mgt.ApplicationManagementServiceClient;
import org.wso2.identity.integration.common.clients.oauth.OauthAdminClient;
import org.wso2.identity.integration.common.clients.webappmgt.WebAppAdminClient;
import org.wso2.identity.integration.common.utils.MicroserviceServer;
import org.wso2.identity.integration.common.utils.MicroserviceUtil;
import org.wso2.identity.integration.test.util.Utils;
import org.wso2.identity.integration.test.utils.OAuth2Constant;

import java.io.File;
import java.io.IOException;
import java.net.URL;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.time.Instant;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.ws.rs.Consumes;
import javax.ws.rs.FormParam;
import javax.ws.rs.GET;
import javax.ws.rs.HeaderParam;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;

import static org.wso2.identity.integration.test.utils.OAuth2Constant.CALLBACK_URL;

@Path("/")
public class HttpMethodsTestCase extends AbstractAdaptiveAuthenticationTestCase {

    public static final String ANALYTICS_PAYLOAD_JSON = "analytics-payload.json";
    private static final String PRIMARY_IS_APPLICATION_NAME = "testOauthApp";
    private static final String STATUS = "status";
    private static final String SUCCESS = "SUCCESS";
    private static final String FAILED = "FAILED";
    private static final String AUTHORIZATION = "Authorization";
    private static final String API_KEY_HEADER = "X-API-KEY";

    private AuthenticatorClient logManager;
    private OauthAdminClient oauthAdminClient;
    private ApplicationManagementServiceClient applicationManagementServiceClient;
    private WebAppAdminClient webAppAdminClient;
    private final CookieStore cookieStore = new BasicCookieStore();
    private Lookup<CookieSpecProvider> cookieSpecRegistry;
    private RequestConfig requestConfig;
    private CloseableHttpClient client;
    private HttpResponse response;
    private ServerConfigurationManager serverConfigurationManager;

    private ServiceProvider serviceProvider;

    MicroserviceServer microserviceServer;

    @BeforeClass(alwaysRun = true)
    public void testInit() throws Exception {

        super.init();
        logManager = new AuthenticatorClient(backendURL);
        String cookie = this.logManager.login(isServer.getSuperTenant().getTenantAdmin().getUserName(),
                isServer.getSuperTenant().getTenantAdmin().getPassword(),
                isServer.getInstance().getHosts().get("default"));
        oauthAdminClient = new OauthAdminClient(backendURL, cookie);
        ConfigurationContext configContext = ConfigurationContextFactory
                .createConfigurationContextFromFileSystem(null, null);
        applicationManagementServiceClient = new ApplicationManagementServiceClient(sessionCookie, backendURL,
                configContext);
        cookieSpecRegistry = RegistryBuilder.<CookieSpecProvider>create()
                .register(CookieSpecs.DEFAULT, new RFC6265CookieSpecProvider())
                .build();
        requestConfig = RequestConfig.custom()
                .setCookieSpec(CookieSpecs.DEFAULT)
                .build();
        client = HttpClientBuilder.create()
                .setDefaultRequestConfig(requestConfig)
                .setDefaultCookieSpecRegistry(cookieSpecRegistry)
                .disableRedirectHandling()
                .setDefaultCookieStore(cookieStore)
                .build();

        String script = getConditionalAuthScript("TemporaryClaimsAdaptiveScript.js");

        createOauthApp(CALLBACK_URL, PRIMARY_IS_APPLICATION_NAME, oauthAdminClient);
        serviceProvider = createServiceProvider(PRIMARY_IS_APPLICATION_NAME,
                applicationManagementServiceClient, oauthAdminClient, script);

        microserviceServer = MicroserviceUtil.initMicroserviceServer();
        MicroserviceUtil.deployService(microserviceServer, this);

    }

    @AfterClass(alwaysRun = true)
    public void atEnd() throws Exception {

        oauthAdminClient.removeOAuthApplicationData(consumerKey);
        applicationManagementServiceClient.deleteApplication(PRIMARY_IS_APPLICATION_NAME);
        client.close();
        MicroserviceUtil.destroyService(microserviceServer);

        this.logManager.logOut();
        logManager = null;
    }

    @Test(groups = "wso2.is", description = "Check conditional authentication flow.")
    public void testHttpGetWithClientCredentialsAndComplexResponse() throws Exception {

        changeAdaptiveAuthenticationScript("HttpGetScript.js", "dummy-get-with-clientcredential-auth-config");
        loginAndAssert();
    }

    @Test(groups = "wso2.is", description = "Check conditional authentication flow.")
    public void testHttpPostWithApiKeyAndComplexPayloadAndResponse() throws Exception {

        changeAdaptiveAuthenticationScript("HttpPostScript.js", "dummy-post-with-apikey-auth-config");
        loginAndAssert();
    }

    private void loginAndAssert() throws Exception {

        cookieStore.clear();
        response = loginWithOIDC(PRIMARY_IS_APPLICATION_NAME, consumerKey, client);

        EntityUtils.consume(response.getEntity());

        Header locationHeader = response.getFirstHeader(OAuth2Constant.HTTP_RESPONSE_HEADER_LOCATION);
        Assert.assertNotNull(locationHeader, "Login response header is null");
        response = sendGetRequest(client, locationHeader.getValue());
        EntityUtils.consume(response.getEntity());
        locationHeader = response.getFirstHeader(OAuth2Constant.HTTP_RESPONSE_HEADER_LOCATION);

        log.info("############## " + "testHttpMethod - location header " + locationHeader.getValue());

        URL clientUrl = new URL(locationHeader.getValue());
        Assert.assertTrue(clientUrl.getQuery().contains("code="), "Authentication flow was un-successful with " +
                "identifier first login");
    }

    private void changeAdaptiveAuthenticationScript(String scriptFileName, String path) throws Exception {

        String script = getConditionalAuthScript(scriptFileName);
        script = getFormattedScript(script, path);
        serviceProvider.getLocalAndOutBoundAuthenticationConfig().getAuthenticationScriptConfig().setContent(script);
        applicationManagementServiceClient.updateApplicationData(serviceProvider);
    }

    private String getFormattedScript(String script, String path) throws Exception {

        switch (path) {
            case "dummy-get-with-clientcredential-auth-config":
                return String.format(script, getRequestUrl("dummy-get-with-clientcredential-auth-config"),
                        getRequestUrl("dummy-token-endpoint"));
            case "dummy-post-with-apikey-auth-config":
                String payload = getJsonObjectFromFile(ANALYTICS_PAYLOAD_JSON).toString();
                return String.format(script, payload, getRequestUrl("dummy-post-with-apikey-auth-config"));
            default:
                return null;
        }
    }

    private String getRequestUrl(String path) {

        return "http://localhost:" + microserviceServer.getPort() + "/" + path;
    }

    private String generateTestAccessToken() throws JOSEException {

        Instant instant = Instant.now().plusSeconds(3600);
        RSAKey senderJWK = new RSAKeyGenerator(2048)
                .keyID("123")
                .keyUse(KeyUse.SIGNATURE)
                .generate();
        JWSHeader header = new JWSHeader.Builder(JWSAlgorithm.RS256)
                .type(JOSEObjectType.JWT)
                .keyID("MWQ5NWUwYWZiMmMzZTIzMzdmMzBhMWM4YjQyMjVhNWM4NjhkMGRmNzFlMGI3ZDlmYmQzNmEyMzhhYjBiNmZhYw_RS256")
                .build();
        JWTClaimsSet payload = new JWTClaimsSet.Builder()
                .issuer("https://test/oauth2/token")
                .audience("3ENOyHzZtwaP54apEjuV5H31Q_gb")
                .subject("0aac3d44-b5tf-4641-8902-7af8713364f8")
                .expirationTime(Date.from(instant))
                .build();

        SignedJWT signedJWT = new SignedJWT(header, payload);
        signedJWT.sign(new RSASSASigner(senderJWK));
        return signedJWT.serialize();
    }

    @POST
    @Path("/dummy-token-endpoint")
    @Consumes("application/x-www-form-urlencoded")
    @Produces("application/json")
    public Map<String, String> dummyTokenEndpoint(@HeaderParam("Authorization") String authorization,
                                                  @FormParam("grant_type") String grantType) throws JOSEException {

        Map<String, String> response = new HashMap<>();
        if (grantType.equals("client_credentials")) {
            response.put("access_token", generateTestAccessToken());
            response.put("scope", "default");
            response.put("token_type", "Bearer");
            response.put("expires_in", "3600");
            return response;
        } else {
            response.put(STATUS, FAILED);
        }
        return response;
    }

    @GET
    @Path("/dummy-get-with-clientcredential-auth-config")
    @Produces("application/json")
    public Map<String, Object> dummyGetWithClientCredentialAuthConfig(
            @HeaderParam(AUTHORIZATION) String authorization) throws Exception {

        Map<String, Object> response = new HashMap<>();
        JsonObject responseObject = getJsonObjectFromFile(ANALYTICS_PAYLOAD_JSON);
        if (authorization.startsWith("Bearer")) {
            response.put(STATUS, SUCCESS);
            response.put("responseObject", responseObject);
        } else {
            response.put(STATUS, FAILED);
        }
        return response;
    }

    @POST
    @Path("/dummy-post-with-apikey-auth-config")
    @Produces("application/json")
    public Map<String, Object> dummyPostWithApiKeyAuthConfig(@HeaderParam(API_KEY_HEADER) String apikeyHeader,
                                                             Map<String, Object> data)
            throws Exception {

        Map<String, Object> response = new HashMap<>();
        JsonObject expectedPayload = getJsonObjectFromFile(ANALYTICS_PAYLOAD_JSON);
        Gson gson = new Gson();
        String dataStr = gson.toJson(data);
        JsonObject actualPayload = gson.fromJson(dataStr, JsonObject.class);
        if (apikeyHeader != null & actualPayload.equals(expectedPayload)) {
            response.put(STATUS, SUCCESS);
            response.put("responseObject", expectedPayload);

        } else {
            response.put(STATUS, FAILED);
        }
        return response;
    }
}

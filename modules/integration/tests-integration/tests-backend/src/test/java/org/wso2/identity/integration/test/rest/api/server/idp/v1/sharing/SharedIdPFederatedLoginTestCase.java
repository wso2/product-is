/*
 * Copyright (c) 2026, WSO2 LLC. (http://www.wso2.com).
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
 * KIND, either express or implied. See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */

package org.wso2.identity.integration.test.rest.api.server.idp.v1.sharing;

import com.nimbusds.jwt.JWTClaimsSet;
import com.nimbusds.jwt.SignedJWT;
import org.apache.commons.codec.binary.Base64;
import org.apache.http.Header;
import org.apache.http.HttpResponse;
import org.apache.http.NameValuePair;
import org.apache.http.client.config.CookieSpecs;
import org.apache.http.client.config.RequestConfig;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpDelete;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.config.Lookup;
import org.apache.http.config.RegistryBuilder;
import org.apache.http.conn.ssl.NoopHostnameVerifier;
import org.apache.http.cookie.CookieSpecProvider;
import org.apache.http.impl.client.BasicCookieStore;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.DefaultRedirectStrategy;
import org.apache.http.impl.client.HttpClientBuilder;
import org.apache.http.impl.cookie.RFC6265CookieSpecProvider;
import org.apache.http.message.BasicHeader;
import org.apache.http.message.BasicNameValuePair;
import org.apache.http.ssl.SSLContextBuilder;
import org.apache.http.util.EntityUtils;
import org.json.JSONArray;
import org.json.JSONObject;
import org.testng.Assert;
import org.testng.annotations.AfterClass;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.DataProvider;
import org.testng.annotations.Test;
import org.wso2.carbon.automation.engine.context.TestUserMode;
import org.wso2.identity.integration.test.base.MockOIDCIdentityProvider;
import org.wso2.identity.integration.test.oauth2.OAuth2ServiceAbstractIntegrationTest;
import org.wso2.identity.integration.test.rest.api.common.RESTTestBase;
import org.wso2.identity.integration.test.rest.api.server.application.management.v1.model.AdvancedApplicationConfiguration;
import org.wso2.identity.integration.test.rest.api.server.application.management.v1.model.ApplicationModel;
import org.wso2.identity.integration.test.rest.api.server.application.management.v1.model.ApplicationPatchModel;
import org.wso2.identity.integration.test.rest.api.server.application.management.v1.model.AuthenticationSequence;
import org.wso2.identity.integration.test.rest.api.server.application.management.v1.model.AuthenticationStep;
import org.wso2.identity.integration.test.rest.api.server.application.management.v1.model.Authenticator;
import org.wso2.identity.integration.test.rest.api.server.application.management.v1.model.InboundProtocols;
import org.wso2.identity.integration.test.rest.api.server.application.management.v1.model.OpenIDConnectConfiguration;
import org.wso2.identity.integration.test.rest.api.server.idp.v1.model.FederatedAuthenticatorRequest;
import org.wso2.identity.integration.test.rest.api.server.idp.v1.model.FederatedAuthenticatorRequest.FederatedAuthenticator;
import org.wso2.identity.integration.test.rest.api.server.idp.v1.model.IdentityProviderPOSTRequest;
import org.wso2.identity.integration.test.rest.api.server.idp.v1.model.Property;
import org.wso2.identity.integration.test.restclients.IdpMgtRestClient;
import org.wso2.identity.integration.test.restclients.OAuth2RestClient;
import org.wso2.identity.integration.test.restclients.OrgMgtRestClient;
import org.wso2.identity.integration.test.utils.DataExtractUtil;
import org.wso2.identity.integration.test.utils.OAuth2Constant;

import java.io.IOException;
import java.net.URLDecoder;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.TimeUnit;
import java.util.function.Predicate;

import javax.net.ssl.SSLContext;

import static org.awaitility.Awaitility.await;
import static org.testng.Assert.assertNotNull;
import static org.testng.Assert.assertTrue;
import static org.wso2.identity.integration.test.base.MockOIDCIdentityProvider.MOCK_IDP_AUTHORIZE_ENDPOINT;
import static org.wso2.identity.integration.test.base.MockOIDCIdentityProvider.MOCK_IDP_CLIENT_ID;
import static org.wso2.identity.integration.test.base.MockOIDCIdentityProvider.MOCK_IDP_CLIENT_SECRET;
import static org.wso2.identity.integration.test.base.MockOIDCIdentityProvider.MOCK_IDP_TOKEN_ENDPOINT;

/**
 * Integration tests for the runtime execution of identity providers shared with organizations.
 * <p>
 * An OIDC identity provider backed by a mock external IdP is created in the root organization and shared with the
 * organizations. A user logs in to an application of a (nested) organization through the shared identity provider.
 * The external IdP redirects back to the root organization's commonauth endpoint as the shared identity provider
 * inherits the callback URL of the parent identity provider, and the login flow should be completed for the
 * organization the flow was initiated for.
 */
public class SharedIdPFederatedLoginTestCase extends OAuth2ServiceAbstractIntegrationTest {

    private static final String AUTHORIZED_APIS_JSON = "idp-sharing-apis.json";
    private static final String ORG_L1 = "SharedIdPLoginOrgL1";
    private static final String ORG_L2 = "SharedIdPLoginOrgL2";
    private static final String IDP_NAME = "SharedIdPLoginMockOIDCIdP";
    private static final String OIDC_AUTHENTICATOR_ID = "T3BlbklEQ29ubmVjdEF1dGhlbnRpY2F0b3I";
    private static final String OIDC_AUTHENTICATOR_NAME = "OpenIDConnectAuthenticator";
    private static final String ROOT_COMMONAUTH_URL = "https://localhost:9853/commonauth";
    private static final String APP_CALLBACK_URL = "https://localhost:8490/shared-idp-login/callback";
    private static final String AUTHORIZATION_CODE = "authorization_code";
    private static final String SUPER_TENANT_PATH = "t/carbon.super/";
    private static final String ORGANIZATION_PATH = "o/";
    private static final int MAX_REDIRECTS = 5;

    private final Map<String, String> orgIds = new HashMap<>();
    private final Map<String, String> switchedTokens = new HashMap<>();
    private final Map<String, String> appIds = new HashMap<>();
    private final Map<String, OpenIDConnectConfiguration> appOidcConfigs = new HashMap<>();

    private MockOIDCIdentityProvider mockIdP;
    private IdpMgtRestClient idpMgtRestClient;
    private OrgMgtRestClient orgMgtRestClient;
    private OAuth2RestClient oAuth2RestClient;
    private CloseableHttpClient client;
    private String idpId;

    @BeforeClass(alwaysRun = true)
    public void init() throws Exception {

        super.init(TestUserMode.SUPER_TENANT_ADMIN);

        mockIdP = new MockOIDCIdentityProvider();
        mockIdP.start();

        idpMgtRestClient = new IdpMgtRestClient(serverURL, tenantInfo);
        oAuth2RestClient = new OAuth2RestClient(serverURL, tenantInfo);
        orgMgtRestClient = new OrgMgtRestClient(isServer, tenantInfo, serverURL,
                new JSONObject(RESTTestBase.readResource(AUTHORIZED_APIS_JSON, getClass())));

        String l1OrgId = orgMgtRestClient.addOrganization(ORG_L1);
        orgIds.put(ORG_L1, l1OrgId);
        orgIds.put(ORG_L2, orgMgtRestClient.addSubOrganization(ORG_L2, l1OrgId));
        for (Map.Entry<String, String> org : orgIds.entrySet()) {
            switchedTokens.put(org.getKey(), orgMgtRestClient.switchM2MToken(org.getValue()));
        }

        idpId = idpMgtRestClient.createIdentityProvider(buildMockOIDCIdentityProvider());
        idpMgtRestClient.shareIdentityProviderWithAllOrgs(idpId);
        for (String orgName : orgIds.keySet()) {
            awaitIdpSharedWithOrg(orgName);
            createApplicationWithSharedIdp(orgName);
        }
    }

    @AfterClass(alwaysRun = true)
    public void cleanUp() throws Exception {

        for (Map.Entry<String, String> app : appIds.entrySet()) {
            try {
                deleteOrganizationApplication(app.getKey(), app.getValue());
            } catch (Exception e) {
                log.warn("Failed to delete the application of the organization: " + app.getKey(), e);
            }
        }
        if (idpId != null) {
            try {
                idpMgtRestClient.unshareIdentityProviderFromAllOrgs(idpId);
                for (String orgName : orgIds.keySet()) {
                    await().atMost(60, TimeUnit.SECONDS).pollInterval(2, TimeUnit.SECONDS).ignoreExceptions()
                            .until(() -> findSharedIdpInOrg(orgName) == null);
                }
                idpMgtRestClient.deleteIdp(idpId);
            } catch (Exception e) {
                log.warn("Failed to unshare and delete the identity provider during cleanup.", e);
            }
        }
        try {
            if (orgIds.containsKey(ORG_L2)) {
                orgMgtRestClient.deleteSubOrganization(orgIds.get(ORG_L2), orgIds.get(ORG_L1));
            }
            if (orgIds.containsKey(ORG_L1)) {
                orgMgtRestClient.deleteOrganization(orgIds.get(ORG_L1));
            }
        } catch (Exception e) {
            log.warn("Failed to delete the organizations during cleanup.", e);
        }
        if (mockIdP != null) {
            mockIdP.stop();
        }
        idpMgtRestClient.closeHttpClient();
        oAuth2RestClient.closeHttpClient();
        orgMgtRestClient.closeHttpClient();
    }

    @DataProvider(name = "organizationProvider")
    public Object[][] organizationProvider() {

        return new Object[][]{
                {ORG_L1},
                {ORG_L2}
        };
    }

    @Test(dataProvider = "organizationProvider")
    public void testLoginToOrganizationApplicationThroughSharedIdp(String orgName) throws Exception {

        client = createHttpClient();
        try {
            String orgId = orgIds.get(orgName);
            OpenIDConnectConfiguration oidcConfig = appOidcConfigs.get(orgName);
            String orgQualifiedPath = SUPER_TENANT_PATH + ORGANIZATION_PATH + orgId + "/";

            // Initiate the login to the organization application.
            List<NameValuePair> authorizeParams = new ArrayList<>();
            authorizeParams.add(new BasicNameValuePair("response_type", "code"));
            authorizeParams.add(new BasicNameValuePair("client_id", oidcConfig.getClientId()));
            authorizeParams.add(new BasicNameValuePair("redirect_uri", APP_CALLBACK_URL));
            authorizeParams.add(new BasicNameValuePair("scope", "openid"));
            String location = getLocation(sendPostRequest(serverURL + orgQualifiedPath + "oauth2/authorize",
                    authorizeParams));

            // The login is redirected to the external IdP of the shared identity provider.
            location = followRedirectsUntil(location, url -> url.startsWith(MOCK_IDP_AUTHORIZE_ENDPOINT));
            Assert.assertEquals(URLDecoder.decode(DataExtractUtil.getParamFromURIString(location, "redirect_uri"),
                    StandardCharsets.UTF_8.name()),
                    ROOT_COMMONAUTH_URL, "Shared identity provider should use the callback URL of the parent.");

            // The external IdP redirects back to the root organization's commonauth endpoint.
            location = getLocation(sendGetRequest(location));
            assertTrue(location.startsWith(ROOT_COMMONAUTH_URL),
                    "External IdP should redirect to the root organization commonauth endpoint. Location: "
                            + location);

            // The flow should continue for the organization the login was initiated for.
            location = getLocation(sendGetRequest(location));
            assertTrue(location.contains(orgQualifiedPath + "oauth2/authorize"),
                    "Login flow should continue in the organization: " + orgName + ". Location: " + location);

            location = followRedirectsUntil(location, url -> url.startsWith(APP_CALLBACK_URL));
            String authorizationCode = DataExtractUtil.getParamFromURIString(location, "code");
            assertNotNull(authorizationCode,
                    "Authorization code is not returned for the organization login. Location: " + location);

            // The token request to the external IdP should be made with the client secret of the parent IdP.
            mockIdP.verifyForAuthzCodeFlow();

            JWTClaimsSet idTokenClaims = getIdTokenClaims(orgQualifiedPath, oidcConfig, authorizationCode);
            assertNotNull(idTokenClaims.getSubject(), "Subject of the federated user is not available in the ID token.");
            Assert.assertEquals(idTokenClaims.getStringClaim("org_id"), orgId,
                    "ID token should be issued for the organization the login was initiated for.");
        } finally {
            client.close();
        }
    }

    private IdentityProviderPOSTRequest buildMockOIDCIdentityProvider() {

        FederatedAuthenticator authenticator = new FederatedAuthenticator()
                .authenticatorId(OIDC_AUTHENTICATOR_ID)
                .name(OIDC_AUTHENTICATOR_NAME)
                .isEnabled(true)
                .addProperty(new Property().key("ClientId").value(MOCK_IDP_CLIENT_ID))
                .addProperty(new Property().key("ClientSecret").value(MOCK_IDP_CLIENT_SECRET))
                .addProperty(new Property().key("OAuth2AuthzEPUrl").value(MOCK_IDP_AUTHORIZE_ENDPOINT))
                .addProperty(new Property().key("OAuth2TokenEPUrl").value(MOCK_IDP_TOKEN_ENDPOINT))
                .addProperty(new Property().key("callbackUrl").value(ROOT_COMMONAUTH_URL))
                .addProperty(new Property().key("commonAuthQueryParams").value("scope=openid"));

        return new IdentityProviderPOSTRequest()
                .name(IDP_NAME)
                .federatedAuthenticators(new FederatedAuthenticatorRequest()
                        .defaultAuthenticatorId(OIDC_AUTHENTICATOR_ID)
                        .addAuthenticator(authenticator));
    }

    private void createApplicationWithSharedIdp(String orgName) throws Exception {

        String token = switchedTokens.get(orgName);
        OpenIDConnectConfiguration oidcConfig = new OpenIDConnectConfiguration();
        oidcConfig.addGrantTypesItem(AUTHORIZATION_CODE);
        oidcConfig.addCallbackURLsItem(APP_CALLBACK_URL);
        ApplicationModel application = new ApplicationModel()
                .name(orgName + "-App")
                .inboundProtocolConfiguration(new InboundProtocols().oidc(oidcConfig))
                .advancedConfigurations(new AdvancedApplicationConfiguration()
                        .skipLoginConsent(true)
                        .skipLogoutConsent(true));
        String appId = oAuth2RestClient.createOrganizationApplication(application, token);
        appIds.put(orgName, appId);

        ApplicationPatchModel patchModel = new ApplicationPatchModel();
        patchModel.setAuthenticationSequence(new AuthenticationSequence()
                .type(AuthenticationSequence.TypeEnum.USER_DEFINED)
                .addStepsItem(new AuthenticationStep()
                        .id(1)
                        .addOptionsItem(new Authenticator().idp(IDP_NAME).authenticator(OIDC_AUTHENTICATOR_NAME))));
        oAuth2RestClient.updateSubOrgApplication(appId, patchModel, token);
        appOidcConfigs.put(orgName, oAuth2RestClient.getOIDCInboundDetailsOfOrganizationApp(appId, token));
    }

    private void awaitIdpSharedWithOrg(String orgName) {

        await().atMost(60, TimeUnit.SECONDS)
                .pollInterval(2, TimeUnit.SECONDS)
                .ignoreExceptions()
                .until(() -> findSharedIdpInOrg(orgName) != null);
    }

    private JSONObject findSharedIdpInOrg(String orgName) throws Exception {

        String url = serverURL + "o/api/server/v1/identity-providers?filter=name+eq+" + IDP_NAME;
        try (CloseableHttpClient httpClient = HttpClientBuilder.create().build();
             CloseableHttpResponse response = httpClient.execute(buildGetWithBearerToken(url, orgName))) {
            Assert.assertEquals(response.getStatusLine().getStatusCode(), 200,
                    "Failed to list the identity providers of the organization: " + orgName);
            JSONArray idps = new JSONObject(EntityUtils.toString(response.getEntity()))
                    .optJSONArray("identityProviders");
            if (idps == null) {
                return null;
            }
            for (int i = 0; i < idps.length(); i++) {
                JSONObject idp = idps.getJSONObject(i);
                if (IDP_NAME.equals(idp.optString("name"))) {
                    return idp;
                }
            }
            return null;
        }
    }

    private void deleteOrganizationApplication(String orgName, String appId) throws IOException {

        String url = serverURL + "o/api/server/v1/applications/" + appId;
        try (CloseableHttpClient httpClient = HttpClientBuilder.create().build()) {
            HttpDelete request = new HttpDelete(url);
            request.setHeader("Authorization", "Bearer " + switchedTokens.get(orgName));
            httpClient.execute(request).close();
        }
    }

    private HttpGet buildGetWithBearerToken(String url, String orgName) {

        HttpGet request = new HttpGet(url);
        request.setHeader("Authorization", "Bearer " + switchedTokens.get(orgName));
        request.setHeader("Accept", "application/json");
        return request;
    }

    private JWTClaimsSet getIdTokenClaims(String orgQualifiedPath, OpenIDConnectConfiguration oidcConfig,
                                          String authorizationCode) throws Exception {

        List<NameValuePair> params = new ArrayList<>();
        params.add(new BasicNameValuePair("code", authorizationCode));
        params.add(new BasicNameValuePair("grant_type", AUTHORIZATION_CODE));
        params.add(new BasicNameValuePair("redirect_uri", APP_CALLBACK_URL));

        HttpPost request = new HttpPost(serverURL + orgQualifiedPath + "oauth2/token");
        request.setHeader("Authorization", "Basic " + Base64.encodeBase64String(
                (oidcConfig.getClientId() + ":" + oidcConfig.getClientSecret()).getBytes()).trim());
        request.setHeader("Content-Type", "application/x-www-form-urlencoded;charset=UTF-8");
        request.setHeader("User-Agent", OAuth2Constant.USER_AGENT);
        request.setEntity(new UrlEncodedFormEntity(params));

        HttpResponse response = client.execute(request);
        JSONObject tokenResponse = new JSONObject(EntityUtils.toString(response.getEntity(), "UTF-8"));
        assertTrue(tokenResponse.has("id_token"), "ID token is not returned. Response: " + tokenResponse);
        return SignedJWT.parse(tokenResponse.getString("id_token")).getJWTClaimsSet();
    }

    private HttpResponse sendPostRequest(String url, List<NameValuePair> params) throws IOException {

        HttpPost request = new HttpPost(url);
        request.setHeader("User-Agent", OAuth2Constant.USER_AGENT);
        request.setEntity(new UrlEncodedFormEntity(params));
        return client.execute(request);
    }

    private HttpResponse sendGetRequest(String url) throws IOException {

        HttpGet request = new HttpGet(url);
        request.setHeader("User-Agent", OAuth2Constant.USER_AGENT);
        return client.execute(request);
    }

    /**
     * Follows the redirects from the given location until a location matching the given condition is reached.
     */
    private String followRedirectsUntil(String location, Predicate<String> condition) throws IOException {

        String currentLocation = location;
        for (int hop = 0; hop < MAX_REDIRECTS && !condition.test(currentLocation); hop++) {
            currentLocation = getLocation(sendGetRequest(currentLocation));
        }
        assertTrue(condition.test(currentLocation), "Expected redirect is not reached. Location: " + currentLocation);
        return currentLocation;
    }

    private String getLocation(HttpResponse response) throws IOException {

        Header location = response.getFirstHeader(OAuth2Constant.HTTP_RESPONSE_HEADER_LOCATION);
        EntityUtils.consume(response.getEntity());
        assertNotNull(location, "Location header is not returned. Status: " + response.getStatusLine());
        return location.getValue();
    }

    /**
     * Creates a browser-like client that keeps cookies and does not follow redirects. The client trusts the
     * certificates of the local test servers, as the login flow is redirected to the mock external IdP.
     */
    private CloseableHttpClient createHttpClient() throws Exception {

        SSLContext sslContext = SSLContextBuilder.create()
                .loadTrustMaterial(null, (chain, authType) -> true)
                .build();
        Lookup<CookieSpecProvider> cookieSpecRegistry = RegistryBuilder.<CookieSpecProvider>create()
                .register(CookieSpecs.DEFAULT, new RFC6265CookieSpecProvider())
                .build();
        return HttpClientBuilder.create()
                .setSSLContext(sslContext)
                .setSSLHostnameVerifier(NoopHostnameVerifier.INSTANCE)
                .setDefaultCookieStore(new BasicCookieStore())
                .setDefaultRequestConfig(RequestConfig.custom().setCookieSpec(CookieSpecs.DEFAULT).build())
                .setDefaultCookieSpecRegistry(cookieSpecRegistry)
                .setRedirectStrategy(new DefaultRedirectStrategy() {
                    @Override
                    protected boolean isRedirectable(String method) {

                        return false;
                    }
                })
                .build();
    }
}

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
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */

package org.wso2.identity.integration.test.oauth2;

import org.apache.commons.lang3.StringUtils;
import org.apache.http.Header;
import org.apache.http.HttpResponse;
import org.apache.http.HttpStatus;
import org.apache.http.NameValuePair;
import org.apache.http.client.config.CookieSpecs;
import org.apache.http.client.config.RequestConfig;
import org.apache.http.client.utils.URLEncodedUtils;
import org.apache.http.config.Lookup;
import org.apache.http.config.RegistryBuilder;
import org.apache.http.cookie.CookieSpecProvider;
import org.apache.http.impl.client.BasicCookieStore;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.DefaultRedirectStrategy;
import org.apache.http.impl.client.HttpClientBuilder;
import org.apache.http.impl.cookie.RFC6265CookieSpecProvider;
import org.apache.http.message.BasicHeader;
import org.apache.http.message.BasicNameValuePair;
import org.apache.http.util.EntityUtils;
import org.json.JSONException;
import org.json.JSONObject;
import org.wso2.identity.integration.test.rest.api.server.application.management.v1.model.AdvancedApplicationConfiguration;
import org.wso2.identity.integration.test.rest.api.server.application.management.v1.model.ApplicationModel;
import org.wso2.identity.integration.test.rest.api.server.application.management.v1.model.ApplicationSharePOSTRequest;
import org.wso2.identity.integration.test.rest.api.server.application.management.v1.model.ClientSecretList;
import org.wso2.identity.integration.test.rest.api.server.application.management.v1.model.ClientSecretResponse;
import org.wso2.identity.integration.test.rest.api.server.application.management.v1.model.InboundProtocols;
import org.wso2.identity.integration.test.rest.api.server.application.management.v1.model.OpenIDConnectConfiguration;
import org.wso2.identity.integration.test.utils.CarbonUtils;
import org.wso2.identity.integration.test.utils.DataExtractUtil;
import org.wso2.identity.integration.test.utils.OAuth2Constant;

import java.io.IOException;
import java.net.URI;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicReference;

import static org.awaitility.Awaitility.await;
import static org.testng.Assert.assertEquals;
import static org.testng.Assert.assertNotNull;
import static org.testng.Assert.assertTrue;
import static org.wso2.identity.integration.test.restclients.RestBaseClient.CONTENT_TYPE_ATTRIBUTE;
import static org.wso2.identity.integration.test.restclients.RestBaseClient.USER_AGENT_ATTRIBUTE;
import static org.wso2.identity.integration.test.utils.OAuth2Constant.ACCESS_TOKEN;
import static org.wso2.identity.integration.test.utils.OAuth2Constant.ACCESS_TOKEN_ENDPOINT;
import static org.wso2.identity.integration.test.utils.OAuth2Constant.AUTHORIZATION_HEADER;
import static org.wso2.identity.integration.test.utils.OAuth2Constant.AUTHORIZE_ENDPOINT_URL;
import static org.wso2.identity.integration.test.utils.OAuth2Constant.BASIC_HEADER;
import static org.wso2.identity.integration.test.utils.OAuth2Constant.CALLBACK_URL;
import static org.wso2.identity.integration.test.utils.OAuth2Constant.COMMON_AUTH_URL;
import static org.wso2.identity.integration.test.utils.OAuth2Constant.GRANT_TYPE_NAME;
import static org.wso2.identity.integration.test.utils.OAuth2Constant.HTTP_RESPONSE_HEADER_LOCATION;
import static org.wso2.identity.integration.test.utils.OAuth2Constant.OAUTH2_GRANT_TYPE_AUTHORIZATION_CODE;
import static org.wso2.identity.integration.test.utils.OAuth2Constant.OAUTH2_GRANT_TYPE_CLIENT_CREDENTIALS;
import static org.wso2.identity.integration.test.utils.OAuth2Constant.OAUTH2_GRANT_TYPE_ORGANIZATION_SWITCH;
import static org.wso2.identity.integration.test.utils.OAuth2Constant.OAUTH2_GRANT_TYPE_REFRESH_TOKEN;
import static org.wso2.identity.integration.test.utils.OAuth2Constant.OAUTH2_SCOPE;
import static org.wso2.identity.integration.test.utils.OAuth2Constant.OAUTH2_SCOPE_OPENID;
import static org.wso2.identity.integration.test.utils.OAuth2Constant.USER_AGENT;

/**
 * Carries the scaffolding shared by the sub-organization client secret test classes, covering the root and the client
 * secret management applications, the organization switch grant used to address the organization perspective APIs,
 * the token requests sent to a sub-organization token endpoint and the response readers asserted on by the tests.
 */
public abstract class SubOrgClientSecretTestBase extends OAuth2ServiceAbstractIntegrationTest {

    protected static final String ORG_APPLICATION_MGT_API = "/o/api/server/v1/applications";
    protected static final String SYSTEM_SCOPE = "SYSTEM";

    private static final String SWITCHING_ORGANIZATION = "switching_organization";
    private static final String TOKEN = "token";

    private static final long APPLICATION_SHARE_WAIT_MILLIS = 20000L;

    protected CloseableHttpClient client;

    protected String rootApplicationId;
    protected String rootClientId;
    protected String rootInitialSecretValue;
    protected String secretMgtApplicationId;
    protected String secretMgtClientId;
    protected String secretMgtClientSecret;

    /**
     * Creates the root organization application shared with the sub-organizations.
     *
     * @param applicationName Name of the application to create.
     * @throws Exception If an error occurred while creating the application.
     */
    protected void createRootApplication(String applicationName) throws Exception {

        OpenIDConnectConfiguration oidcConfig = new OpenIDConnectConfiguration();
        oidcConfig.setGrantTypes(Arrays.asList(OAUTH2_GRANT_TYPE_CLIENT_CREDENTIALS,
                OAUTH2_GRANT_TYPE_AUTHORIZATION_CODE, OAUTH2_GRANT_TYPE_REFRESH_TOKEN));
        oidcConfig.addCallbackURLsItem(CALLBACK_URL);

        InboundProtocols inboundProtocols = new InboundProtocols();
        inboundProtocols.setOidc(oidcConfig);

        ApplicationModel application = new ApplicationModel()
                .name(applicationName)
                .enhancedOrgAuthenticationEnabled(true)
                .inboundProtocolConfiguration(inboundProtocols)
                .advancedConfigurations(new AdvancedApplicationConfiguration()
                        .skipLoginConsent(true)
                        .skipLogoutConsent(true));

        rootApplicationId = addApplication(application);
        OpenIDConnectConfiguration createdOidcConfig = restClient.getOIDCInboundDetails(rootApplicationId);
        rootClientId = createdOidcConfig.getClientId();
        rootInitialSecretValue = createdOidcConfig.getClientSecret();
        assertNotNull(rootClientId, "Client id of the root application should not be null.");
        assertNotNull(rootInitialSecretValue, "Client secret of the root application should not be null.");
    }

    /**
     * Creates the management application whose token is switched into the organizations under test.
     *
     * @param applicationName      Name of the management application to create.
     * @param authorizedSystemAPIs System APIs to authorize the management application for.
     * @throws Exception If an error occurred while creating the application.
     */
    protected void createClientSecretManagementApplication(String applicationName, List<String> authorizedSystemAPIs)
            throws Exception {

        OpenIDConnectConfiguration oidcConfig = new OpenIDConnectConfiguration();
        oidcConfig.setGrantTypes(Arrays.asList(OAUTH2_GRANT_TYPE_CLIENT_CREDENTIALS,
                OAUTH2_GRANT_TYPE_ORGANIZATION_SWITCH));

        InboundProtocols inboundProtocols = new InboundProtocols();
        inboundProtocols.setOidc(oidcConfig);

        ApplicationModel application = new ApplicationModel()
                .name(applicationName)
                .isManagementApp(true)
                .enhancedOrgAuthenticationEnabled(false)
                .inboundProtocolConfiguration(inboundProtocols);

        secretMgtApplicationId = addApplication(application);
        if (!CarbonUtils.isLegacyAuthzRuntimeEnabled()) {
            authorizeSystemAPIs(secretMgtApplicationId, authorizedSystemAPIs);
        }

        OpenIDConnectConfiguration oidcConfigOfMgtApp = restClient.getOIDCInboundDetails(secretMgtApplicationId);
        secretMgtClientId = oidcConfigOfMgtApp.getClientId();
        secretMgtClientSecret = oidcConfigOfMgtApp.getClientSecret();
    }

    protected void shareApplicationWithAllChildOrganizations(String applicationId) throws Exception {

        ApplicationSharePOSTRequest applicationSharePOSTRequest = new ApplicationSharePOSTRequest();
        applicationSharePOSTRequest.setShareWithAllChildren(true);
        restClient.shareApplication(applicationId, applicationSharePOSTRequest);
    }

    /**
     * Polls the organization of the given token until the shared application resolves there.
     *
     * @param applicationName Name of the application shared from the root organization.
     * @param accessToken     Organization switched token of the organization to resolve the application in.
     * @return Application id of the shared application in that organization.
     */
    protected String waitForApplicationSharedToOrganization(String applicationName, String accessToken) {

        AtomicReference<String> sharedAppId = new AtomicReference<>();
        await("shared application '" + applicationName + "' in the organization")
                .atMost(APPLICATION_SHARE_WAIT_MILLIS, TimeUnit.MILLISECONDS)
                .pollInterval(1, TimeUnit.SECONDS)
                .ignoreExceptions()
                .until(() -> {
                    String appId = restClient.getAppIdUsingAppNameInOrganization(applicationName, accessToken);
                    if (StringUtils.isBlank(appId)) {
                        return false;
                    }
                    sharedAppId.set(appId);
                    return true;
                });
        return sharedAppId.get();
    }

    /**
     * Retrieves an organization switched token of the client secret management application for the given scope.
     *
     * @param scope          Scope requested with the organization switch grant.
     * @param organizationId Id of the organization to switch into, at any level of the hierarchy.
     * @return Organization switched access token.
     * @throws Exception If an error occurred while retrieving the token.
     */
    protected String getOrganizationSwitchedToken(String scope, String organizationId) throws Exception {

        List<NameValuePair> parameters = new ArrayList<>();
        parameters.add(new BasicNameValuePair(GRANT_TYPE_NAME, OAUTH2_GRANT_TYPE_ORGANIZATION_SWITCH));
        parameters.add(new BasicNameValuePair(TOKEN, getClientSecretManagementAppToken()));
        parameters.add(new BasicNameValuePair(OAUTH2_SCOPE, scope));
        parameters.add(new BasicNameValuePair(SWITCHING_ORGANIZATION, organizationId));

        HttpResponse response = sendPostRequest(client, getTokenRequestHeaders(secretMgtClientId,
                secretMgtClientSecret), parameters, getTenantQualifiedURL(ACCESS_TOKEN_ENDPOINT,
                tenantInfo.getDomain()));
        JSONObject tokenResponse = getResponseBody(response, HttpStatus.SC_OK);
        assertTrue(tokenResponse.has(ACCESS_TOKEN), "Access token is not present in the organization switch " +
                "grant response.");
        if (!SYSTEM_SCOPE.equals(scope)) {
            assertTrue(tokenResponse.optString(OAUTH2_SCOPE, StringUtils.EMPTY).contains(scope),
                    "The organization switched token does not carry the requested scope: " + scope);
        }
        return tokenResponse.getString(ACCESS_TOKEN);
    }

    protected String getClientSecretManagementAppToken() throws Exception {

        List<NameValuePair> parameters = new ArrayList<>();
        parameters.add(new BasicNameValuePair(GRANT_TYPE_NAME, OAUTH2_GRANT_TYPE_CLIENT_CREDENTIALS));
        parameters.add(new BasicNameValuePair(OAUTH2_SCOPE, SYSTEM_SCOPE));

        HttpResponse response = sendPostRequest(client, getTokenRequestHeaders(secretMgtClientId,
                secretMgtClientSecret), parameters, getTenantQualifiedURL(ACCESS_TOKEN_ENDPOINT,
                tenantInfo.getDomain()));
        JSONObject tokenResponse = getResponseBody(response, HttpStatus.SC_OK);
        assertTrue(tokenResponse.has(ACCESS_TOKEN),
                "Access token is not present in the client credentials grant response.");
        return tokenResponse.getString(ACCESS_TOKEN);
    }

    protected int getClientCredentialsTokenStatusCode(String tokenEndpoint, String clientId, String clientSecretValue)
            throws Exception {

        List<NameValuePair> parameters = new ArrayList<>();
        parameters.add(new BasicNameValuePair(GRANT_TYPE_NAME, OAUTH2_GRANT_TYPE_CLIENT_CREDENTIALS));

        HttpResponse response = sendTokenRequest(tokenEndpoint, parameters, clientId, clientSecretValue);
        int statusCode = response.getStatusLine().getStatusCode();
        EntityUtils.consume(response.getEntity());
        return statusCode;
    }

    protected HttpResponse sendTokenRequest(String tokenEndpoint, List<NameValuePair> parameters, String clientId,
                                            String clientSecretValue) throws Exception {

        return sendPostRequest(client, getTokenRequestHeaders(clientId, clientSecretValue), parameters,
                tokenEndpoint);
    }

    /**
     * Performs a login of the given organization user on the shared application and returns the authorization code.
     *
     * @param organizationId Id of the organization to authenticate the user in.
     * @param username       Username of the organization user.
     * @param password       Password of the organization user.
     * @return Authorization code issued at the given organization.
     * @throws Exception If an error occurred while retrieving the authorization code.
     */
    protected String getAuthorizationCodeFromOrgLogin(String organizationId, String username, String password)
            throws Exception {

        try (CloseableHttpClient loginClient = createHttpClient()) {
            List<NameValuePair> authorizeParameters = new ArrayList<>();
            authorizeParameters.add(new BasicNameValuePair("response_type", OAuth2Constant.OAUTH2_GRANT_TYPE_CODE));
            authorizeParameters.add(new BasicNameValuePair(OAuth2Constant.OAUTH2_CLIENT_ID, rootClientId));
            authorizeParameters.add(new BasicNameValuePair("redirect_uri", CALLBACK_URL));
            authorizeParameters.add(new BasicNameValuePair(OAUTH2_SCOPE, OAUTH2_SCOPE_OPENID));

            HttpResponse response = sendPostRequestWithParameters(loginClient, authorizeParameters,
                    getRootTenantQualifiedOrgURL(AUTHORIZE_ENDPOINT_URL, tenantInfo.getDomain(), organizationId));
            Header locationHeader = response.getFirstHeader(HTTP_RESPONSE_HEADER_LOCATION);
            assertNotNull(locationHeader, "Location header of the authorize request is not available.");
            EntityUtils.consume(response.getEntity());

            response = sendGetRequest(loginClient, locationHeader.getValue());
            Map<String, Integer> keyPositionMap = new HashMap<>(1);
            keyPositionMap.put("name=\"sessionDataKey\"", 1);
            List<DataExtractUtil.KeyValue> keyValues = DataExtractUtil.extractDataFromResponse(response,
                    keyPositionMap);
            assertTrue(keyValues != null && !keyValues.isEmpty(), "No session data key found on the login page.");
            String sessionDataKey = keyValues.get(0).getValue();
            EntityUtils.consume(response.getEntity());

            List<NameValuePair> loginParameters = new ArrayList<>();
            loginParameters.add(new BasicNameValuePair("username", username));
            loginParameters.add(new BasicNameValuePair("password", password));
            loginParameters.add(new BasicNameValuePair("sessionDataKey", sessionDataKey));

            response = sendPostRequestWithParameters(loginClient, loginParameters,
                    getRootTenantQualifiedOrgURL(COMMON_AUTH_URL, tenantInfo.getDomain(), organizationId));
            locationHeader = response.getFirstHeader(HTTP_RESPONSE_HEADER_LOCATION);
            assertNotNull(locationHeader, "Location header of the login request is not available.");
            EntityUtils.consume(response.getEntity());

            response = sendGetRequest(loginClient, locationHeader.getValue());
            locationHeader = response.getFirstHeader(HTTP_RESPONSE_HEADER_LOCATION);
            assertNotNull(locationHeader, "Redirection to the application with the authorization code is null.");
            EntityUtils.consume(response.getEntity());

            String authorizationCode = getAuthorizationCodeFromURL(locationHeader.getValue());
            assertNotNull(authorizationCode, "Authorization code is null.");
            return authorizationCode;
        }
    }

    protected ClientSecretResponse getNonLatestSecret(ClientSecretList clientSecrets) {

        return clientSecrets.getList().stream()
                .filter(clientSecret -> !Boolean.TRUE.equals(clientSecret.getLatest()))
                .findFirst()
                .orElseThrow(() -> new AssertionError("No non latest client secret is present in the list."));
    }

    protected JSONObject getResponseBody(HttpResponse response, int expectedStatusCode) throws IOException,
            JSONException {

        String responseBody = EntityUtils.toString(response.getEntity(), StandardCharsets.UTF_8.name());
        assertEquals(response.getStatusLine().getStatusCode(), expectedStatusCode,
                "Unexpected status code received for the request. Response: " + responseBody);
        return new JSONObject(responseBody);
    }

    protected List<Header> getTokenRequestHeaders(String clientId, String clientSecretValue) {

        List<Header> headers = new ArrayList<>();
        headers.add(new BasicHeader(AUTHORIZATION_HEADER, BASIC_HEADER + " " +
                getBase64EncodedString(clientId, clientSecretValue)));
        headers.add(new BasicHeader(CONTENT_TYPE_ATTRIBUTE, "application/x-www-form-urlencoded"));
        headers.add(new BasicHeader(USER_AGENT_ATTRIBUTE, USER_AGENT));
        return headers;
    }

    protected String getAuthorizationCodeFromURL(String location) {

        URI uri = URI.create(location);
        return URLEncodedUtils.parse(uri, StandardCharsets.UTF_8).stream()
                .filter(parameter -> "code".equals(parameter.getName()))
                .map(NameValuePair::getValue)
                .findFirst()
                .orElse(null);
    }

    protected CloseableHttpClient createHttpClient() {

        Lookup<CookieSpecProvider> cookieSpecRegistry = RegistryBuilder.<CookieSpecProvider>create()
                .register(CookieSpecs.DEFAULT, new RFC6265CookieSpecProvider())
                .build();
        return HttpClientBuilder.create()
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

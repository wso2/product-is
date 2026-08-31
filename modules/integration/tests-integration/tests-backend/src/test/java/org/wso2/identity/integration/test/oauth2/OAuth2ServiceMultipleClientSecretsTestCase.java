/*
 * Copyright (c) 2026, WSO2 LLC. (https://www.wso2.com).
 *
 * WSO2 LLC. licenses this file to you under the Apache License,
 * Version 2.0 (the "License"); you may not use this file except
 * in compliance with the License.
 * You may obtain a copy of the License at
 *
 * https://www.apache.org/licenses/LICENSE-2.0
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
import org.apache.http.client.CookieStore;
import org.apache.http.client.config.CookieSpecs;
import org.apache.http.client.config.RequestConfig;
import org.apache.http.client.utils.URLEncodedUtils;
import org.apache.http.config.Lookup;
import org.apache.http.config.RegistryBuilder;
import org.apache.http.cookie.CookieSpecProvider;
import org.apache.http.impl.client.BasicCookieStore;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClientBuilder;
import org.apache.http.impl.cookie.RFC6265CookieSpecProvider;
import org.apache.http.message.BasicHeader;
import org.apache.http.message.BasicNameValuePair;
import org.apache.http.util.EntityUtils;
import org.json.JSONObject;
import org.testng.Assert;
import org.testng.annotations.AfterClass;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;
import org.wso2.carbon.automation.engine.context.TestUserMode;
import org.wso2.identity.integration.test.rest.api.server.application.management.v1.model.ApplicationResponseModel;
import org.wso2.identity.integration.test.rest.api.server.application.management.v1.model.ClientSecretCreationRequest;
import org.wso2.identity.integration.test.rest.api.server.application.management.v1.model.ClientSecretList;
import org.wso2.identity.integration.test.rest.api.server.application.management.v1.model.ClientSecretResponse;
import org.wso2.identity.integration.test.rest.api.server.application.management.v1.model.OpenIDConnectConfiguration;
import org.wso2.identity.integration.test.utils.DataExtractUtil;
import org.wso2.identity.integration.test.utils.OAuth2Constant;

import java.net.URI;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.TimeUnit;

import static org.awaitility.Awaitility.await;
import static org.wso2.identity.integration.test.utils.DataExtractUtil.KeyValue;
import static org.wso2.identity.integration.test.utils.OAuth2Constant.ACCESS_TOKEN_ENDPOINT;
import static org.wso2.identity.integration.test.utils.OAuth2Constant.AUTHORIZATION_HEADER;
import static org.wso2.identity.integration.test.utils.OAuth2Constant.AUTHORIZE_ENDPOINT_URL;
import static org.wso2.identity.integration.test.utils.OAuth2Constant.OAUTH2_GRANT_TYPE_AUTHORIZATION_CODE;
import static org.wso2.identity.integration.test.utils.OAuth2Constant.OAUTH2_GRANT_TYPE_CLIENT_CREDENTIALS;
import static org.wso2.identity.integration.test.utils.OAuth2Constant.OAUTH2_GRANT_TYPE_REFRESH_TOKEN;

/**
 * Runtime behaviour of an OAuth2 application that has multiple client secrets.
 */
public class OAuth2ServiceMultipleClientSecretsTestCase extends OAuth2ServiceAbstractIntegrationTest {

    private static final boolean CLIENT_SECRET_BASIC = true;
    private static final boolean CLIENT_SECRET_POST = false;
    private static final String INVALID_CLIENT_ERROR = "invalid_client";
    private static final String INVALID_CONSUMER_SECRET = "invalidConsumerSecret";
    private static final long CLIENT_SECRET_EXPIRY_IN_SECONDS = 30L;
    private static final long CLIENT_SECRET_EXPIRY_BUFFER_IN_SECONDS = 10L;

    private String applicationId;
    private String consumerKey;
    private String initialConsumerSecret;
    private String initialSecretId;
    private String createdConsumerSecret;
    private String expiringConsumerSecret;
    private String expiringSecretId;
    private String regeneratedConsumerSecret;
    private String additionalConsumerSecret;
    private String accessTokenIssuedWithInitialSecret;
    private String initialSecretRefreshToken;
    private String adminUsername;
    private String adminPassword;

    private Lookup<CookieSpecProvider> cookieSpecRegistry;
    private RequestConfig requestConfig;
    private CloseableHttpClient client;
    private final CookieStore cookieStore = new BasicCookieStore();

    @BeforeClass(alwaysRun = true)
    public void testInit() throws Exception {

        super.init(TestUserMode.SUPER_TENANT_ADMIN);
        adminUsername = userInfo.getUserName();
        adminPassword = userInfo.getPassword();

        cookieSpecRegistry = RegistryBuilder.<CookieSpecProvider>create()
                .register(CookieSpecs.DEFAULT, new RFC6265CookieSpecProvider())
                .build();
        requestConfig = RequestConfig.custom()
                .setCookieSpec(CookieSpecs.DEFAULT)
                .build();
        client = HttpClientBuilder.create().disableRedirectHandling()
                .setDefaultRequestConfig(requestConfig)
                .setDefaultCookieStore(cookieStore)
                .setDefaultCookieSpecRegistry(cookieSpecRegistry)
                .build();

        setSystemproperties();
    }

    @AfterClass(alwaysRun = true)
    public void atEnd() throws Exception {

        if (applicationId != null) {
            deleteApp(applicationId);
        }
        client.close();
        restClient.closeHttpClient();

        applicationId = null;
        consumerKey = null;
    }

    @Test(groups = "wso2.is", description = "Check Oauth2 application registration")
    public void testRegisterApplication() throws Exception {

        ApplicationResponseModel application = addApplication();
        Assert.assertNotNull(application, "OAuth App creation failed.");
        applicationId = application.getId();

        OpenIDConnectConfiguration oidcConfig = getOIDCInboundDetailsOfApplication(applicationId);
        consumerKey = oidcConfig.getClientId();
        Assert.assertNotNull(consumerKey, "Application creation failed.");

        ClientSecretList clientSecrets = restClient.getClientSecrets(applicationId);
        Assert.assertEquals(clientSecrets.getCount().intValue(), 1,
                "Newly created application does not have exactly one client secret.");
        initialSecretId = clientSecrets.getList().get(0).getSecretId();
        Assert.assertNotNull(initialSecretId, "Secret id of the client secret created with the application is null.");
        initialConsumerSecret = clientSecrets.getList().get(0).getSecretValue();
        Assert.assertNotNull(initialConsumerSecret,
                "Secret value of the client secret created with the application is null.");
    }

    @Test(groups = "wso2.is", description = "Get an access token with the client secret created with the application",
            dependsOnMethods = "testRegisterApplication")
    public void testGetTokenBeforeAddingClientSecrets() throws Exception {

        accessTokenIssuedWithInitialSecret = assertTokenIssued(
                requestClientCredentialsToken(initialConsumerSecret, CLIENT_SECRET_BASIC),
                "Token request with the client secret created with the application failed.");
    }

    @Test(groups = "wso2.is", description = "Add a second client secret to the application",
            dependsOnMethods = "testGetTokenBeforeAddingClientSecrets")
    public void testCreateSecondClientSecret() throws Exception {

        ClientSecretResponse clientSecret = restClient.createClientSecret(applicationId,
                new ClientSecretCreationRequest().expiresAt(0L));
        createdConsumerSecret = clientSecret.getSecretValue();

        Assert.assertNotNull(clientSecret.getSecretId(), "Secret id of the created client secret is null.");
        Assert.assertNotNull(createdConsumerSecret, "Secret value of the created client secret is null.");
        Assert.assertNotEquals(createdConsumerSecret, initialConsumerSecret,
                "Created client secret is identical to the client secret created with the application.");
        Assert.assertEquals(clientSecret.getStatus(), ClientSecretResponse.StatusEnum.ACTIVE,
                "Created client secret is not in the active status.");
        Assert.assertTrue(clientSecret.getLatest(), "Created client secret is not marked as the latest.");

        ClientSecretList clientSecrets = restClient.getClientSecrets(applicationId);
        Assert.assertEquals(clientSecrets.getCount().intValue(), 2,
                "Application does not have two client secrets after the creation.");
    }

    @Test(groups = "wso2.is", description = "Check that an issued access token survives a client secret creation",
            dependsOnMethods = "testCreateSecondClientSecret")
    public void testTokenSurvivesClientSecretCreation() throws Exception {

        Assert.assertTrue(isTokenActive(accessTokenIssuedWithInitialSecret),
                "Access token issued before the client secret creation is not active.");
    }

    @Test(groups = "wso2.is", description = "Get tokens with both client secrets over both client authentication " +
            "methods", dependsOnMethods = "testTokenSurvivesClientSecretCreation")
    public void testGetTokenWithBothClientSecrets() throws Exception {

        assertTokenIssued(requestClientCredentialsToken(initialConsumerSecret, CLIENT_SECRET_BASIC),
                "Token request with the non latest client secret over client_secret_basic failed.");
        assertTokenIssued(requestClientCredentialsToken(initialConsumerSecret, CLIENT_SECRET_POST),
                "Token request with the non latest client secret over client_secret_post failed.");
        assertTokenIssued(requestClientCredentialsToken(createdConsumerSecret, CLIENT_SECRET_BASIC),
                "Token request with the latest client secret over client_secret_basic failed.");
        assertTokenIssued(requestClientCredentialsToken(createdConsumerSecret, CLIENT_SECRET_POST),
                "Token request with the latest client secret over client_secret_post failed.");
    }

    @Test(groups = "wso2.is", description = "Send token requests with invalid client secrets",
            dependsOnMethods = "testGetTokenWithBothClientSecrets")
    public void testGetTokenWithInvalidClientSecrets() throws Exception {

        assertInvalidClient(requestClientCredentialsToken(INVALID_CONSUMER_SECRET, CLIENT_SECRET_BASIC),
                "Token request with a wrong client secret over client_secret_basic did not fail.");
        assertInvalidClient(requestClientCredentialsToken(INVALID_CONSUMER_SECRET, CLIENT_SECRET_POST),
                "Token request with a wrong client secret over client_secret_post did not fail.");
        assertInvalidClient(requestClientCredentialsToken(StringUtils.EMPTY, CLIENT_SECRET_BASIC),
                "Token request with a blank client secret over client_secret_basic did not fail.");
        assertInvalidClient(requestClientCredentialsToken(StringUtils.EMPTY, CLIENT_SECRET_POST),
                "Token request with a blank client secret over client_secret_post did not fail.");
    }

    @Test(groups = "wso2.is", description = "Exchange authorization codes with both client secrets",
            dependsOnMethods = "testGetTokenWithInvalidClientSecrets")
    public void testAuthorizationCodeGrantWithBothClientSecrets() throws Exception {

        TokenEndpointResponse tokenResponse = exchangeAuthorizationCode(getAuthorizationCode(), initialConsumerSecret);
        assertTokenIssued(tokenResponse, "Authorization code exchange with the non latest client secret failed.");
        initialSecretRefreshToken = getRefreshToken(tokenResponse);

        tokenResponse = exchangeAuthorizationCode(getAuthorizationCode(), createdConsumerSecret);
        assertTokenIssued(tokenResponse, "Authorization code exchange with the latest client secret failed.");
        Assert.assertTrue(tokenResponse.body.has("refresh_token"),
                "Refresh token is not found in the token response of the latest client secret exchange.");
    }

    @Test(groups = "wso2.is", description = "Send refresh token grant requests with both client secrets",
            dependsOnMethods = "testAuthorizationCodeGrantWithBothClientSecrets")
    public void testRefreshTokenGrantWithBothClientSecrets() throws Exception {

        /* Both authorization code exchanges return the same active token pair for the same user, application and
           scope, and each refresh rotates the refresh token. Chain the rotated token into the second request
           instead of replaying the stale one. */
        TokenEndpointResponse firstRefreshResponse = requestRefreshTokenGrant(initialSecretRefreshToken,
                initialConsumerSecret);
        assertTokenIssued(firstRefreshResponse, "Refresh token grant with the non latest client secret failed.");
        assertTokenIssued(requestRefreshTokenGrant(getRefreshToken(firstRefreshResponse), createdConsumerSecret),
                "Refresh token grant with the latest client secret failed.");
    }

    @Test(groups = "wso2.is", description = "Check that a client secret deletion does not revoke issued tokens but " +
            "stops the deleted client secret immediately",
            dependsOnMethods = "testRefreshTokenGrantWithBothClientSecrets")
    public void testTokenSurvivesClientSecretDeletion() throws Exception {

        /* The earlier client credentials token shares its token bucket with the authorization code flow of the
           same user, application and scope, so the refresh token rotation above already revoked it. A token
           minted here isolates the assertion to the secret deletion alone. */
        String tokenIssuedBeforeDeletion = assertTokenIssued(
                requestClientCredentialsToken(initialConsumerSecret, CLIENT_SECRET_BASIC),
                "Token request with the client secret to be deleted failed.");

        Assert.assertEquals(restClient.deleteClientSecret(applicationId, initialSecretId), HttpStatus.SC_NO_CONTENT,
                "Deletion of the non latest client secret failed.");

        Assert.assertTrue(isTokenActive(tokenIssuedBeforeDeletion),
                "Access token issued with the deleted client secret is not active after the deletion.");
        assertInvalidClient(requestClientCredentialsToken(initialConsumerSecret, CLIENT_SECRET_BASIC),
                "Token request with the deleted client secret did not fail immediately.");
        assertTokenIssued(requestClientCredentialsToken(createdConsumerSecret, CLIENT_SECRET_BASIC),
                "Token request with the remaining client secret failed after the deletion.");
    }

    @Test(groups = "wso2.is", description = "Check that an expired client secret is rejected at the token endpoint",
            dependsOnMethods = "testTokenSurvivesClientSecretDeletion")
    public void testExpiredClientSecretIsRejectedAtTokenEndpoint() throws Exception {

        long expiresAt = System.currentTimeMillis() / 1000 + CLIENT_SECRET_EXPIRY_IN_SECONDS;
        ClientSecretResponse clientSecret = restClient.createClientSecret(applicationId,
                new ClientSecretCreationRequest().expiresAt(expiresAt));
        expiringSecretId = clientSecret.getSecretId();
        expiringConsumerSecret = clientSecret.getSecretValue();

        Assert.assertEquals(clientSecret.getExpiresAt().longValue(), expiresAt,
                "Expiry time of the created client secret is not the requested expiry time.");
        Assert.assertEquals(clientSecret.getStatus(), ClientSecretResponse.StatusEnum.ACTIVE,
                "Client secret created with a future expiry time is not in the active status.");

        String accessTokenIssuedBeforeExpiry = assertTokenIssued(
                requestClientCredentialsToken(expiringConsumerSecret, CLIENT_SECRET_BASIC),
                "Token request with a client secret that is not expired yet failed.");

        await().atMost(CLIENT_SECRET_EXPIRY_IN_SECONDS + 2 * CLIENT_SECRET_EXPIRY_BUFFER_IN_SECONDS,
                        TimeUnit.SECONDS)
                .pollDelay(CLIENT_SECRET_EXPIRY_IN_SECONDS, TimeUnit.SECONDS)
                .pollInterval(2, TimeUnit.SECONDS)
                .until(() -> requestClientCredentialsToken(expiringConsumerSecret, CLIENT_SECRET_BASIC).statusCode
                        == HttpStatus.SC_UNAUTHORIZED);

        assertInvalidClient(requestClientCredentialsToken(expiringConsumerSecret, CLIENT_SECRET_BASIC),
                "Token request with an expired client secret did not fail.");
        assertTokenIssued(requestClientCredentialsToken(createdConsumerSecret, CLIENT_SECRET_BASIC),
                "Token request with the remaining client secret failed after the other one expired.");
        Assert.assertEquals(restClient.getClientSecret(applicationId, expiringSecretId).getStatus(),
                ClientSecretResponse.StatusEnum.EXPIRED, "Expired client secret is not reported as expired.");
        Assert.assertTrue(isTokenActive(accessTokenIssuedBeforeExpiry),
                "Access token issued before the client secret expiry is not active.");
    }

    @Test(groups = "wso2.is", description = "Check that a client secret regeneration replaces all client secrets and " +
            "revokes issued tokens", dependsOnMethods = "testExpiredClientSecretIsRejectedAtTokenEndpoint")
    public void testRegenerateClientSecret() throws Exception {

        TokenEndpointResponse tokenResponse = exchangeAuthorizationCode(getAuthorizationCode(), createdConsumerSecret);
        String accessTokenBeforeRegeneration =
                assertTokenIssued(tokenResponse, "Authorization code exchange before the regeneration failed.");
        String refreshTokenBeforeRegeneration = getRefreshToken(tokenResponse);
        String authorizationCodeBeforeRegeneration = getAuthorizationCode();

        regeneratedConsumerSecret = regenerateClientSecret();
        Assert.assertNotEquals(regeneratedConsumerSecret, createdConsumerSecret,
                "Regenerated client secret is identical to a client secret that existed before the regeneration.");

        ClientSecretList clientSecrets = restClient.getClientSecrets(applicationId);
        Assert.assertEquals(clientSecrets.getCount().intValue(), 1,
                "Regeneration did not replace the existing client secrets with a single client secret.");

        Assert.assertFalse(isTokenActive(accessTokenBeforeRegeneration),
                "Access token issued before the regeneration is still active.");
        Assert.assertEquals(getError(requestRefreshTokenGrant(refreshTokenBeforeRegeneration,
                        regeneratedConsumerSecret)), OAuth2Constant.INVALID_GRANT_ERROR,
                "Refresh token issued before the regeneration is still usable.");
        assertInvalidClient(requestClientCredentialsToken(createdConsumerSecret, CLIENT_SECRET_BASIC),
                "Token request with a client secret replaced by the regeneration did not fail.");
        assertInvalidClient(requestClientCredentialsToken(expiringConsumerSecret, CLIENT_SECRET_BASIC),
                "Token request with a client secret replaced by the regeneration did not fail.");
        assertTokenIssued(requestClientCredentialsToken(regeneratedConsumerSecret, CLIENT_SECRET_BASIC),
                "Token request with the regenerated client secret failed.");

        /* Authorization codes are not bound to the client secret they were requested with, hence a regeneration
           leaves outstanding codes exchangeable. The assertion below pins that observed server behaviour and is not
           a statement of the expected contract. */
        assertTokenIssued(exchangeAuthorizationCode(authorizationCodeBeforeRegeneration, regeneratedConsumerSecret),
                "Authorization code issued before the regeneration could not be exchanged.");
    }

    @Test(groups = "wso2.is", description = "Regenerate the client secret twice in a row",
            dependsOnMethods = "testRegenerateClientSecret")
    public void testConsecutiveClientSecretRegeneration() throws Exception {

        /* A second regeneration right after the first one is served while the application data of the first
           regeneration is still cached. */
        String previousConsumerSecret = regeneratedConsumerSecret;
        regeneratedConsumerSecret = regenerateClientSecret();
        Assert.assertNotEquals(regeneratedConsumerSecret, previousConsumerSecret,
                "Consecutive regeneration returned the same client secret.");

        assertInvalidClient(requestClientCredentialsToken(previousConsumerSecret, CLIENT_SECRET_BASIC),
                "Token request with the client secret of the previous regeneration did not fail.");
        assertTokenIssued(requestClientCredentialsToken(regeneratedConsumerSecret, CLIENT_SECRET_BASIC),
                "Token request with the client secret of the second regeneration failed.");
    }

    @Test(groups = "wso2.is", description = "Revoke tokens with the latest and the non latest client secret",
            dependsOnMethods = "testConsecutiveClientSecretRegeneration")
    public void testRevokeTokenWithBothClientSecrets() throws Exception {

        additionalConsumerSecret = restClient.createClientSecret(applicationId,
                new ClientSecretCreationRequest().expiresAt(0L)).getSecretValue();

        String accessToken = assertTokenIssued(
                requestClientCredentialsToken(regeneratedConsumerSecret, CLIENT_SECRET_BASIC),
                "Token request with the non latest client secret failed.");
        Assert.assertEquals(revokeAccessToken(accessToken, regeneratedConsumerSecret), HttpStatus.SC_OK,
                "Token revocation authenticated with the non latest client secret failed.");
        Assert.assertFalse(isTokenActive(accessToken), "Revoked access token is still active.");

        accessToken = assertTokenIssued(requestClientCredentialsToken(additionalConsumerSecret, CLIENT_SECRET_BASIC),
                "Token request with the latest client secret failed.");
        Assert.assertEquals(revokeAccessToken(accessToken, additionalConsumerSecret), HttpStatus.SC_OK,
                "Token revocation authenticated with the latest client secret failed.");
        Assert.assertFalse(isTokenActive(accessToken), "Revoked access token is still active.");
    }

    @Test(groups = "wso2.is", description = "Send token requests after the application is deleted",
            dependsOnMethods = "testRevokeTokenWithBothClientSecrets")
    public void testGetTokenAfterApplicationDeletion() throws Exception {

        deleteApp(applicationId);
        applicationId = null;

        assertInvalidClient(requestClientCredentialsToken(regeneratedConsumerSecret, CLIENT_SECRET_BASIC),
                "Token request with a client secret of the deleted application did not fail.");
        assertInvalidClient(requestClientCredentialsToken(additionalConsumerSecret, CLIENT_SECRET_BASIC),
                "Token request with a client secret of the deleted application did not fail.");
    }

    /**
     * Send a client credentials grant request with the given client secret.
     *
     * @param clientSecret           Client secret to authenticate the client with.
     * @param useBasicAuthentication Whether to authenticate over client_secret_basic instead of client_secret_post.
     * @return Token endpoint response.
     * @throws Exception If an error occurred while invoking the token endpoint.
     */
    private TokenEndpointResponse requestClientCredentialsToken(String clientSecret, boolean useBasicAuthentication)
            throws Exception {

        List<NameValuePair> parameters = new ArrayList<>();
        parameters.add(new BasicNameValuePair("grant_type", OAUTH2_GRANT_TYPE_CLIENT_CREDENTIALS));

        return invokeTokenEndpoint(parameters, clientSecret, useBasicAuthentication);
    }

    /**
     * Send a refresh token grant request with the given client secret.
     *
     * @param refreshToken Refresh token to be exchanged.
     * @param clientSecret Client secret to authenticate the client with.
     * @return Token endpoint response.
     * @throws Exception If an error occurred while invoking the token endpoint.
     */
    private TokenEndpointResponse requestRefreshTokenGrant(String refreshToken, String clientSecret) throws Exception {

        List<NameValuePair> parameters = new ArrayList<>();
        parameters.add(new BasicNameValuePair("grant_type", OAUTH2_GRANT_TYPE_REFRESH_TOKEN));
        parameters.add(new BasicNameValuePair(OAUTH2_GRANT_TYPE_REFRESH_TOKEN, refreshToken));

        return invokeTokenEndpoint(parameters, clientSecret, CLIENT_SECRET_BASIC);
    }

    /**
     * Exchange an authorization code with the given client secret.
     *
     * @param authorizationCode Authorization code to be exchanged.
     * @param clientSecret      Client secret to authenticate the client with.
     * @return Token endpoint response.
     * @throws Exception If an error occurred while invoking the token endpoint.
     */
    private TokenEndpointResponse exchangeAuthorizationCode(String authorizationCode, String clientSecret)
            throws Exception {

        List<NameValuePair> parameters = new ArrayList<>();
        parameters.add(new BasicNameValuePair("grant_type", OAUTH2_GRANT_TYPE_AUTHORIZATION_CODE));
        parameters.add(new BasicNameValuePair("code", authorizationCode));
        parameters.add(new BasicNameValuePair("redirect_uri", OAuth2Constant.CALLBACK_URL));

        return invokeTokenEndpoint(parameters, clientSecret, CLIENT_SECRET_BASIC);
    }

    /**
     * Send a token request with the given parameters and client authentication method.
     *
     * @param parameters             Grant specific request parameters.
     * @param clientSecret           Client secret to authenticate the client with.
     * @param useBasicAuthentication Whether to authenticate over client_secret_basic instead of client_secret_post.
     * @return Token endpoint response.
     * @throws Exception If an error occurred while invoking the token endpoint.
     */
    private TokenEndpointResponse invokeTokenEndpoint(List<NameValuePair> parameters, String clientSecret,
                                                      boolean useBasicAuthentication) throws Exception {

        List<NameValuePair> requestParameters = new ArrayList<>(parameters);
        List<Header> headers = new ArrayList<>();
        headers.add(new BasicHeader("Content-Type", "application/x-www-form-urlencoded"));
        headers.add(new BasicHeader("User-Agent", OAuth2Constant.USER_AGENT));
        if (useBasicAuthentication) {
            headers.add(new BasicHeader(AUTHORIZATION_HEADER, OAuth2Constant.BASIC_HEADER + " " +
                    getBase64EncodedString(consumerKey, clientSecret)));
        } else {
            requestParameters.add(new BasicNameValuePair("client_id", consumerKey));
            requestParameters.add(new BasicNameValuePair("client_secret", clientSecret));
        }

        HttpResponse response = sendPostRequest(client, headers, requestParameters,
                getTenantQualifiedURL(ACCESS_TOKEN_ENDPOINT, tenantInfo.getDomain()));
        Assert.assertNotNull(response, "Token endpoint response is null.");
        String responseString = EntityUtils.toString(response.getEntity(), StandardCharsets.UTF_8.name());
        EntityUtils.consume(response.getEntity());

        return new TokenEndpointResponse(response.getStatusLine().getStatusCode(), new JSONObject(responseString));
    }

    /**
     * Revoke an access token authenticating the client with the given client secret.
     *
     * @param accessToken  Access token to be revoked.
     * @param clientSecret Client secret to authenticate the client with.
     * @return Status code of the revocation response.
     * @throws Exception If an error occurred while invoking the revocation endpoint.
     */
    private int revokeAccessToken(String accessToken, String clientSecret) throws Exception {

        List<NameValuePair> parameters = new ArrayList<>();
        parameters.add(new BasicNameValuePair("token", accessToken));

        List<Header> headers = new ArrayList<>();
        headers.add(new BasicHeader("Content-Type", "application/x-www-form-urlencoded"));
        headers.add(new BasicHeader("User-Agent", OAuth2Constant.USER_AGENT));
        headers.add(new BasicHeader(AUTHORIZATION_HEADER, OAuth2Constant.BASIC_HEADER + " " +
                getBase64EncodedString(consumerKey, clientSecret)));

        HttpResponse response = sendPostRequest(client, headers, parameters, OAuth2Constant.TOKEN_REVOKE_ENDPOINT);
        Assert.assertNotNull(response, "Token revocation response is null.");
        EntityUtils.consume(response.getEntity());

        return response.getStatusLine().getStatusCode();
    }

    /**
     * Run the authorization request and the login of the authorization code flow and return the issued code.
     *
     * @return Authorization code.
     * @throws Exception If an error occurred while retrieving the authorization code.
     */
    private String getAuthorizationCode() throws Exception {

        refreshHTTPClient();

        List<NameValuePair> urlParameters = new ArrayList<>();
        urlParameters.add(new BasicNameValuePair("response_type", OAuth2Constant.OAUTH2_GRANT_TYPE_CODE));
        urlParameters.add(new BasicNameValuePair("client_id", consumerKey));
        urlParameters.add(new BasicNameValuePair("redirect_uri", OAuth2Constant.CALLBACK_URL));
        urlParameters.add(new BasicNameValuePair("scope", ""));

        HttpResponse response = sendPostRequestWithParameters(client, urlParameters,
                getTenantQualifiedURL(AUTHORIZE_ENDPOINT_URL, tenantInfo.getDomain()));
        Assert.assertNotNull(response, "Authorize response is null.");
        Header locationHeader = response.getFirstHeader(OAuth2Constant.HTTP_RESPONSE_HEADER_LOCATION);
        Assert.assertNotNull(locationHeader, "Authorize response header is null.");
        EntityUtils.consume(response.getEntity());

        response = sendGetRequest(client, locationHeader.getValue());
        Assert.assertNotNull(response, "Login page response is null.");
        Map<String, Integer> keyPositionMap = new HashMap<>(1);
        keyPositionMap.put("name=\"sessionDataKey\"", 1);
        List<KeyValue> keyValues = DataExtractUtil.extractDataFromResponse(response, keyPositionMap);
        Assert.assertNotNull(keyValues, "sessionDataKey key value is null.");
        String sessionDataKey = keyValues.get(0).getValue();
        Assert.assertNotNull(sessionDataKey, "Session data key is null.");
        EntityUtils.consume(response.getEntity());

        response = sendLoginPost(client, sessionDataKey);
        locationHeader = response.getFirstHeader(OAuth2Constant.HTTP_RESPONSE_HEADER_LOCATION);
        Assert.assertNotNull(locationHeader, "Location header expected post login is not available.");
        EntityUtils.consume(response.getEntity());

        response = sendGetRequest(client, locationHeader.getValue());
        locationHeader = response.getFirstHeader(OAuth2Constant.HTTP_RESPONSE_HEADER_LOCATION);
        Assert.assertNotNull(locationHeader, "Redirection URL to the application with authorization code is null.");
        EntityUtils.consume(response.getEntity());

        String authorizationCode = getAuthorizationCodeFromURL(locationHeader.getValue());
        Assert.assertNotNull(authorizationCode, "Authorization code is null.");

        return authorizationCode;
    }

    /**
     * Regenerate the client secret of the application.
     *
     * @return Regenerated client secret.
     * @throws Exception If an error occurred while regenerating the client secret.
     */
    private String regenerateClientSecret() throws Exception {

        OpenIDConnectConfiguration oidcConfig = restClient.regenerateClientSecret(applicationId);
        Assert.assertNotNull(oidcConfig, "Client secret regeneration response is null.");
        String regeneratedClientSecret = oidcConfig.getClientSecret();
        Assert.assertNotNull(regeneratedClientSecret, "Regenerated client secret is null.");

        return regeneratedClientSecret;
    }

    /**
     * Introspect the given access token with the admin credentials.
     *
     * @param accessToken Access token to be introspected.
     * @return Whether the token is active.
     * @throws Exception If an error occurred while introspecting the access token.
     */
    private boolean isTokenActive(String accessToken) throws Exception {

        org.json.simple.JSONObject introspectionResponse = introspectTokenWithTenant(client, accessToken,
                OAuth2Constant.INTRO_SPEC_ENDPOINT, adminUsername, adminPassword);
        Assert.assertNotNull(introspectionResponse, "Token introspection response is null.");

        return Boolean.TRUE.equals(introspectionResponse.get("active"));
    }

    /**
     * Assert that the token endpoint issued an access token and return it.
     *
     * @param response Token endpoint response.
     * @param message  Assertion failure message.
     * @return Issued access token.
     */
    private String assertTokenIssued(TokenEndpointResponse response, String message) {

        Assert.assertEquals(response.statusCode, HttpStatus.SC_OK, message + " Response: " + response.body);
        Assert.assertTrue(response.body.has("access_token"), message + " Response: " + response.body);

        return response.body.optString("access_token");
    }

    /**
     * Assert that the token endpoint rejected the client authentication.
     *
     * @param response Token endpoint response.
     * @param message  Assertion failure message.
     */
    private void assertInvalidClient(TokenEndpointResponse response, String message) {

        Assert.assertEquals(response.statusCode, HttpStatus.SC_UNAUTHORIZED, message + " Response: " + response.body);
        Assert.assertEquals(getError(response), INVALID_CLIENT_ERROR, message + " Response: " + response.body);
    }

    /**
     * Get the refresh token of a successful token endpoint response.
     *
     * @param response Token endpoint response.
     * @return Refresh token.
     */
    private String getRefreshToken(TokenEndpointResponse response) {

        Assert.assertTrue(response.body.has("refresh_token"), "Refresh token is not found in the token response.");

        return response.body.optString("refresh_token");
    }

    /**
     * Get the error code of a failed token endpoint response.
     *
     * @param response Token endpoint response.
     * @return Error code.
     */
    private String getError(TokenEndpointResponse response) {

        Assert.assertTrue(response.body.has("error"), "Error is not found in the token response.");

        return response.body.optString("error");
    }

    /**
     * Get the authorization code from the provided URL.
     *
     * @param location Location header.
     * @return Authorization code.
     */
    private String getAuthorizationCodeFromURL(String location) {

        URI uri = URI.create(location);
        return URLEncodedUtils.parse(uri, StandardCharsets.UTF_8).stream()
                .filter(param -> "code".equals(param.getName()))
                .map(NameValuePair::getValue)
                .findFirst()
                .orElse(null);
    }

    /**
     * Refresh the cookie store and the http client.
     */
    private void refreshHTTPClient() {

        cookieStore.clear();
        client = HttpClientBuilder.create().disableRedirectHandling()
                .setDefaultCookieStore(cookieStore)
                .setDefaultCookieSpecRegistry(cookieSpecRegistry)
                .setDefaultRequestConfig(requestConfig)
                .build();
    }

    /**
     * Status code and parsed body of a token endpoint response.
     */
    private static class TokenEndpointResponse {

        private final int statusCode;
        private final JSONObject body;

        private TokenEndpointResponse(int statusCode, JSONObject body) {

            this.statusCode = statusCode;
            this.body = body;
        }
    }
}

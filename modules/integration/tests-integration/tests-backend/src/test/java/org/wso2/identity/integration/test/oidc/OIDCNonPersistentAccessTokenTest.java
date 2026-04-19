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

package org.wso2.identity.integration.test.oidc;

import com.nimbusds.jwt.JWTClaimsSet;
import com.nimbusds.jwt.SignedJWT;
import io.restassured.RestAssured;
import io.restassured.response.Response;
import org.apache.http.HttpStatus;
import org.testng.Assert;
import org.testng.annotations.AfterClass;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;
import org.wso2.carbon.integration.common.utils.mgt.ServerConfigurationManager;
import org.wso2.identity.integration.test.oidc.bean.OIDCApplication;
import org.wso2.identity.integration.test.rest.api.server.application.management.v1.model.AccessTokenConfiguration;
import org.wso2.identity.integration.test.rest.api.server.application.management.v1.model.ApplicationModel;
import org.wso2.identity.integration.test.rest.api.server.application.management.v1.model.Claim;
import org.wso2.identity.integration.test.rest.api.server.application.management.v1.model.ClaimConfiguration;
import org.wso2.identity.integration.test.rest.api.server.application.management.v1.model.ClaimConfiguration.DialectEnum;
import org.wso2.identity.integration.test.rest.api.server.application.management.v1.model.ClaimMappings;
import org.wso2.identity.integration.test.rest.api.server.application.management.v1.model.InboundProtocols;
import org.wso2.identity.integration.test.rest.api.server.application.management.v1.model.OpenIDConnectConfiguration;
import org.wso2.identity.integration.test.rest.api.server.application.management.v1.model.RequestedClaimConfiguration;
import org.wso2.identity.integration.test.util.Utils;
import org.wso2.identity.integration.test.utils.OAuth2Constant;

import java.io.File;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static io.restassured.RestAssured.given;
import static org.hamcrest.CoreMatchers.notNullValue;
import static org.hamcrest.Matchers.is;

/**
 * Integration tests for non-persistent access token flows covering two scenarios:
 *
 * Scenario 1: persist_access_token=false, persist_refresh_token=false, retain_revoked_token=true
 *   - Both AT and RT are JWTs and not stored in DB.
 *   - With retain_revoked_token=true, explicit revocation and password-change revocation are tracked.
 *   - Revoking AT causes introspect to return active=false.
 *   - Revoking RT prevents subsequent refresh grants.
 *
 * Scenario 2: persist_access_token=false, persist_refresh_token=true, retain_revoked_token=true
 *   - AT is a JWT, not stored in DB.
 *   - RT is an opaque token (starts with "npr_"), stored in DB.
 *   - Revoking RT correctly prevents subsequent refresh grants.
 */
public class OIDCNonPersistentAccessTokenTest extends OIDCAbstractIntegrationTest {

    private static final String OAUTH2_TOKEN_ENDPOINT_URI = "/oauth2/token";
    private static final String OAUTH2_INTROSPECT_ENDPOINT_URI = "/oauth2/introspect";
    private static final String OAUTH2_REVOKE_ENDPOINT_URI = "/oauth2/revoke";
    private static final String USER_INFO_ENDPOINT = "/oauth2/userinfo";
    private static final String SERVICES = "/services";
    private static final String NPR_PREFIX = "npr_";
    private static final String UPDATED_PASSWORD_SCENARIO_1 = "UpdatedP@ssw0rd1";
    private static final String UPDATED_PASSWORD_SCENARIO_2 = "UpdatedP@ssw0rd2";

    private OIDCApplication application;

    // Manages all config swaps against the original deployment.toml.
    private ServerConfigurationManager serverConfigurationManager;

    // Tokens for scenario 1 (both non-persistent).
    private String accessToken;
    private String refreshToken;

    // Tracks the current password of the test user across password-change tests.
    private String currentUserPassword = OIDCUtilTest.password;

    // Tokens for scenario 2 (non-persistent AT + persistent RT).
    private String accessTokenPersistentRT;
    private String refreshTokenPersistentRT;

    @BeforeClass(alwaysRun = true)
    public void testInit() throws Exception {

        super.init();

        // Apply non-persistent token TOML config (both AT and RT) and restart server.
        changeISConfiguration();

        // Re-initialize after server restart.
        super.init();

        // Create user before config change (needs SCIM2 client from super.init()).
        OIDCUtilTest.initUser();
        createUser(OIDCUtilTest.user);

        RestAssured.baseURI = backendURL.replace(SERVICES, "");

        // Create application with JWT token type.
        OIDCUtilTest.initApplications();
        application = OIDCUtilTest.applications.get(OIDCUtilTest.playgroundAppTwoAppName);
        createApplication(application);
    }

    @AfterClass(alwaysRun = true)
    public void testClear() throws Exception {

        deleteUser(OIDCUtilTest.user);
        deleteApplication(application);
        clear();
        // Restore the original deployment.toml configuration.
        serverConfigurationManager.restoreToLastConfiguration(false);
    }

    // =====================================================================
    // Scenario 1: persist_access_token=false, persist_refresh_token=false
    // =====================================================================

    @Test(groups = "wso2.is", description = "Validate non-persistent access token with client credentials grant")
    public void testClientCredentialsGrant() throws Exception {

        Map<String, String> params = new HashMap<>();
        params.put("grant_type", OAuth2Constant.OAUTH2_GRANT_TYPE_CLIENT_CREDENTIALS);
        params.put("scope", "internal_login");

        Response response = getResponseOfFormPostWithAuth(OAUTH2_TOKEN_ENDPOINT_URI, params, new HashMap<>(),
                application.getClientId(), application.getClientSecret());

        response.then()
                .log().ifValidationFails()
                .assertThat()
                .statusCode(HttpStatus.SC_OK)
                .body("access_token", notNullValue());

        String clientCredentialsAT = response.then().extract().path("access_token");
        Assert.assertTrue(isJWT(clientCredentialsAT), "Access token should be a JWT.");

        JWTClaimsSet claims = SignedJWT.parse(clientCredentialsAT).getJWTClaimsSet();
        Assert.assertNotNull(claims.getClaim("entity_id"), "entity_id claim is missing.");
        Assert.assertEquals(claims.getStringClaim("entityType"), "CLIENT_ID",
                "entityType should be CLIENT_ID for client credentials grant.");
    }

    @Test(groups = "wso2.is", description = "Validate non-persistent access token and refresh token with password grant",
            dependsOnMethods = "testClientCredentialsGrant")
    public void testPasswordGrant() throws Exception {

        Map<String, String> params = new HashMap<>();
        params.put("grant_type", OAuth2Constant.OAUTH2_GRANT_TYPE_RESOURCE_OWNER);
        params.put("scope", "openid profile email");
        params.put("username", OIDCUtilTest.user.getUserName());
        params.put("password", OIDCUtilTest.user.getPassword());

        Response response = getResponseOfFormPostWithAuth(OAUTH2_TOKEN_ENDPOINT_URI, params, new HashMap<>(),
                application.getClientId(), application.getClientSecret());

        response.then()
                .log().ifValidationFails()
                .assertThat()
                .statusCode(HttpStatus.SC_OK)
                .body("access_token", notNullValue())
                .body("refresh_token", notNullValue());

        accessToken = response.then().extract().path("access_token");
        refreshToken = response.then().extract().path("refresh_token");

        Assert.assertTrue(isJWT(accessToken), "Access token should be a JWT.");
        validateAccessTokenClaims(accessToken, "USER_ID");

        Assert.assertTrue(isJWT(refreshToken), "Refresh token should be a JWT.");
        validateRefreshTokenClaims(refreshToken);
    }

    @Test(groups = "wso2.is", description = "Validate user info endpoint with non-persistent access token",
            dependsOnMethods = "testPasswordGrant")
    public void testUserInfoEndpoint() {

        Map<String, String> headers = new HashMap<>();
        headers.put("Authorization", "Bearer " + accessToken);

        Response response = getResponseOfGet(USER_INFO_ENDPOINT, headers);

        response.then()
                .log().ifValidationFails()
                .assertThat()
                .statusCode(HttpStatus.SC_OK)
                .body("given_name", is(OIDCUtilTest.firstName))
                .body("family_name", is(OIDCUtilTest.lastName))
                .body("email", is(OIDCUtilTest.email));
    }

    @Test(groups = "wso2.is",
            description = "Validate that changing user password revokes non-persistent access and refresh tokens",
            dependsOnMethods = "testUserInfoEndpoint")
    public void testPasswordChangeRevokesTokens() throws Exception {

        // Change user password; retain_revoked_token=true means the revoked token IDs are tracked.
        changeUserPassword(UPDATED_PASSWORD_SCENARIO_1);
        // Wait 3 second so that new tokens will have iat > credential_changed_time.
        Thread.sleep(3000);

        // Verify the non-persistent JWT AT is now reported as inactive by introspect.
        Map<String, String> introspectParams = new HashMap<>();
        introspectParams.put("token", accessToken);

        Response introspectATResponse = getResponseOfFormPostWithAuth(OAUTH2_INTROSPECT_ENDPOINT_URI, introspectParams,
                new HashMap<>(), "admin", "admin");

        introspectATResponse.then()
                .log().ifValidationFails()
                .assertThat()
                .statusCode(HttpStatus.SC_OK)
                .body("active", is(false));

        // Verify the non-persistent JWT RT is revoked: refresh grant must fail.
        Map<String, String> refreshParams = new HashMap<>();
        refreshParams.put("grant_type", OAuth2Constant.OAUTH2_GRANT_TYPE_REFRESH_TOKEN);
        refreshParams.put(OAuth2Constant.OAUTH2_GRANT_TYPE_REFRESH_TOKEN, refreshToken);

        Response refreshResponse = getResponseOfFormPostWithAuth(OAUTH2_TOKEN_ENDPOINT_URI, refreshParams,
                new HashMap<>(), application.getClientId(), application.getClientSecret());

        refreshResponse.then()
                .log().ifValidationFails()
                .assertThat()
                .statusCode(HttpStatus.SC_BAD_REQUEST);

        // Wait an additional 2 seconds before re-issuing tokens.
        Thread.sleep(2000);

        // Re-issue a password grant with the new password to obtain fresh tokens for subsequent tests.
        Map<String, String> params = new HashMap<>();
        params.put("grant_type", OAuth2Constant.OAUTH2_GRANT_TYPE_RESOURCE_OWNER);
        params.put("scope", "openid profile email");
        params.put("username", OIDCUtilTest.user.getUserName());
        params.put("password", UPDATED_PASSWORD_SCENARIO_1);

        Response tokenResponse = getResponseOfFormPostWithAuth(OAUTH2_TOKEN_ENDPOINT_URI, params, new HashMap<>(),
                application.getClientId(), application.getClientSecret());

        tokenResponse.then()
                .log().ifValidationFails()
                .assertThat()
                .statusCode(HttpStatus.SC_OK)
                .body("access_token", notNullValue())
                .body("refresh_token", notNullValue());

        accessToken = tokenResponse.then().extract().path("access_token");
        refreshToken = tokenResponse.then().extract().path("refresh_token");
        currentUserPassword = UPDATED_PASSWORD_SCENARIO_1;
    }

    @Test(groups = "wso2.is", description = "Validate refresh token grant with non-persistent tokens",
            dependsOnMethods = "testPasswordChangeRevokesTokens")
    public void testRefreshTokenGrant() throws Exception {

        String oldRefreshToken = refreshToken;

        Map<String, String> params = new HashMap<>();
        params.put("grant_type", OAuth2Constant.OAUTH2_GRANT_TYPE_REFRESH_TOKEN);
        params.put(OAuth2Constant.OAUTH2_GRANT_TYPE_REFRESH_TOKEN, refreshToken);

        Response response = getResponseOfFormPostWithAuth(OAUTH2_TOKEN_ENDPOINT_URI, params, new HashMap<>(),
                application.getClientId(), application.getClientSecret());

        response.then()
                .log().ifValidationFails()
                .assertThat()
                .statusCode(HttpStatus.SC_OK)
                .body("access_token", notNullValue())
                .body("refresh_token", notNullValue());

        accessToken = response.then().extract().path("access_token");
        refreshToken = response.then().extract().path("refresh_token");

        Assert.assertTrue(isJWT(accessToken), "New access token should be a JWT.");
        validateAccessTokenClaims(accessToken, "USER_ID");

        Assert.assertTrue(isJWT(refreshToken), "New refresh token should be a JWT.");
        validateRefreshTokenClaims(refreshToken);

        Assert.assertNotEquals(refreshToken, oldRefreshToken, "New refresh token should differ from old one.");
    }

    @Test(groups = "wso2.is", description = "Validate access token revocation behavior for non-persistent tokens",
            dependsOnMethods = "testRefreshTokenGrant")
    public void testAccessTokenRevocation() throws Exception {

        // Revoke the access token.
        Map<String, String> revokeParams = new HashMap<>();
        revokeParams.put("token", accessToken);
        revokeParams.put("token_type_hint", "access_token");

        Response revokeResponse = getResponseOfFormPostWithAuth(OAUTH2_REVOKE_ENDPOINT_URI, revokeParams,
                new HashMap<>(), application.getClientId(), application.getClientSecret());

        revokeResponse.then()
                .log().ifValidationFails()
                .assertThat()
                .statusCode(HttpStatus.SC_OK);

        // Introspect the revoked access token.
        // With retain_revoked_token=true, the token ID is tracked in the revocation list,
        // so introspect must return active=false even for non-persistent JWTs.
        Map<String, String> introspectParams = new HashMap<>();
        introspectParams.put("token", accessToken);

        Response introspectResponse = getResponseOfFormPostWithAuth(OAUTH2_INTROSPECT_ENDPOINT_URI, introspectParams,
                new HashMap<>(), "admin", "admin");

        introspectResponse.then()
                .log().ifValidationFails()
                .assertThat()
                .statusCode(HttpStatus.SC_OK)
                .body("active", is(false));
    }

    @Test(groups = "wso2.is",
            description = "Validate refresh token revocation and subsequent refresh grant for non-persistent tokens",
            dependsOnMethods = "testAccessTokenRevocation")
    public void testRefreshTokenRevocationAndRefreshGrant() throws Exception {

        // Revoke the refresh token.
        Map<String, String> revokeParams = new HashMap<>();
        revokeParams.put("token", refreshToken);
        revokeParams.put("token_type_hint", "refresh_token");

        Response revokeResponse = getResponseOfFormPostWithAuth(OAUTH2_REVOKE_ENDPOINT_URI, revokeParams,
                new HashMap<>(), application.getClientId(), application.getClientSecret());

        revokeResponse.then()
                .log().ifValidationFails()
                .assertThat()
                .statusCode(HttpStatus.SC_OK);

        // Attempt refresh grant with the revoked refresh token.
        // With retain_revoked_token=true, the revoked RT's token ID is tracked,
        // so the refresh grant must fail even for a non-persistent JWT refresh token.
        Map<String, String> refreshParams = new HashMap<>();
        refreshParams.put("grant_type", OAuth2Constant.OAUTH2_GRANT_TYPE_REFRESH_TOKEN);
        refreshParams.put(OAuth2Constant.OAUTH2_GRANT_TYPE_REFRESH_TOKEN, refreshToken);

        Response refreshResponse = getResponseOfFormPostWithAuth(OAUTH2_TOKEN_ENDPOINT_URI, refreshParams,
                new HashMap<>(), application.getClientId(), application.getClientSecret());

        refreshResponse.then()
                .log().ifValidationFails()
                .assertThat()
                .statusCode(HttpStatus.SC_BAD_REQUEST);
    }

    // =====================================================================
    // Scenario 2: persist_access_token=false, persist_refresh_token=true
    // =====================================================================

    @Test(groups = "wso2.is",
            description = "Switch IS configuration to non-persistent access token with persistent refresh token",
            dependsOnMethods = "testRefreshTokenRevocationAndRefreshGrant")
    public void testSwitchToPersistentRefreshTokenConfiguration() throws Exception {

        changeToPersistentRefreshTokenConfig();

        // Re-initialize clients after server restart.
        super.init();
        RestAssured.baseURI = backendURL.replace(SERVICES, "");
    }

    @Test(groups = "wso2.is",
            description = "Validate non-persistent JWT access token and opaque persistent refresh token with password grant",
            dependsOnMethods = "testSwitchToPersistentRefreshTokenConfiguration")
    public void testPasswordGrantWithPersistentRefreshToken() throws Exception {

        Map<String, String> params = new HashMap<>();
        params.put("grant_type", OAuth2Constant.OAUTH2_GRANT_TYPE_RESOURCE_OWNER);
        params.put("scope", "openid profile email");
        params.put("username", OIDCUtilTest.user.getUserName());
        // Use currentUserPassword because testPasswordChangeRevokesTokens updated it.
        params.put("password", currentUserPassword);

        Response response = getResponseOfFormPostWithAuth(OAUTH2_TOKEN_ENDPOINT_URI, params, new HashMap<>(),
                application.getClientId(), application.getClientSecret());

        response.then()
                .log().ifValidationFails()
                .assertThat()
                .statusCode(HttpStatus.SC_OK)
                .body("access_token", notNullValue())
                .body("refresh_token", notNullValue());

        accessTokenPersistentRT = response.then().extract().path("access_token");
        refreshTokenPersistentRT = response.then().extract().path("refresh_token");

        // Access token is still a non-persistent JWT.
        Assert.assertTrue(isJWT(accessTokenPersistentRT), "Access token should be a JWT.");
        validateAccessTokenClaims(accessTokenPersistentRT, "USER_ID");

        // Refresh token is an opaque persistent token, NOT a JWT, prefixed with "npr_".
        Assert.assertFalse(isJWT(refreshTokenPersistentRT),
                "Persistent refresh token should be opaque, not a JWT.");
        Assert.assertTrue(refreshTokenPersistentRT.startsWith(NPR_PREFIX),
                "Persistent refresh token should start with '" + NPR_PREFIX + "'.");
    }

    @Test(groups = "wso2.is",
            description = "Validate that changing user password revokes non-persistent AT and persistent RT",
            dependsOnMethods = "testPasswordGrantWithPersistentRefreshToken")
    public void testPasswordChangeRevokesTokensWithPersistentRT() throws Exception {

        // Change user password; retain_revoked_token=true means token IDs are tracked in the revocation list.
        changeUserPassword(UPDATED_PASSWORD_SCENARIO_2);
        // Wait 3 second so that new tokens will have iat > credential_changed_time.
        Thread.sleep(3000);

        // Verify the non-persistent JWT AT is now reported as inactive by introspect.
        Map<String, String> introspectATParams = new HashMap<>();
        introspectATParams.put("token", accessTokenPersistentRT);

        Response introspectATResponse = getResponseOfFormPostWithAuth(OAUTH2_INTROSPECT_ENDPOINT_URI,
                introspectATParams, new HashMap<>(), "admin", "admin");

        introspectATResponse.then()
                .log().ifValidationFails()
                .assertThat()
                .statusCode(HttpStatus.SC_OK)
                .body("active", is(false));

        // Verify the persistent RT is revoked: refresh grant must fail.
        Map<String, String> refreshParams = new HashMap<>();
        refreshParams.put("grant_type", OAuth2Constant.OAUTH2_GRANT_TYPE_REFRESH_TOKEN);
        refreshParams.put(OAuth2Constant.OAUTH2_GRANT_TYPE_REFRESH_TOKEN, refreshTokenPersistentRT);

        Response refreshResponse = getResponseOfFormPostWithAuth(OAUTH2_TOKEN_ENDPOINT_URI, refreshParams,
                new HashMap<>(), application.getClientId(), application.getClientSecret());

        refreshResponse.then()
                .log().ifValidationFails()
                .assertThat()
                .statusCode(HttpStatus.SC_BAD_REQUEST);

        // Wait an additional 2 seconds before re-issuing tokens.
        Thread.sleep(2000);

        // Re-issue a password grant with the new password to obtain fresh tokens for subsequent tests.
        Map<String, String> params = new HashMap<>();
        params.put("grant_type", OAuth2Constant.OAUTH2_GRANT_TYPE_RESOURCE_OWNER);
        params.put("scope", "openid profile email");
        params.put("username", OIDCUtilTest.user.getUserName());
        params.put("password", UPDATED_PASSWORD_SCENARIO_2);

        Response tokenResponse = getResponseOfFormPostWithAuth(OAUTH2_TOKEN_ENDPOINT_URI, params, new HashMap<>(),
                application.getClientId(), application.getClientSecret());

        tokenResponse.then()
                .log().ifValidationFails()
                .assertThat()
                .statusCode(HttpStatus.SC_OK)
                .body("access_token", notNullValue())
                .body("refresh_token", notNullValue());

        accessTokenPersistentRT = tokenResponse.then().extract().path("access_token");
        refreshTokenPersistentRT = tokenResponse.then().extract().path("refresh_token");
        currentUserPassword = UPDATED_PASSWORD_SCENARIO_2;
    }

    @Test(groups = "wso2.is",
            description = "Validate introspection of persistent opaque refresh token",
            dependsOnMethods = "testPasswordChangeRevokesTokensWithPersistentRT")
    public void testPersistentRefreshTokenIntrospection() {

        Map<String, String> params = new HashMap<>();
        params.put("token", refreshTokenPersistentRT);
        params.put("token_type_hint", "refresh_token");

        Response response = getResponseOfFormPostWithAuth(OAUTH2_INTROSPECT_ENDPOINT_URI, params,
                new HashMap<>(), "admin", "admin");

        response.then()
                .log().ifValidationFails()
                .assertThat()
                .statusCode(HttpStatus.SC_OK)
                .body("active", is(true));
    }

    @Test(groups = "wso2.is",
            description = "Validate refresh token grant issues new JWT AT and opaque persistent RT",
            dependsOnMethods = "testPersistentRefreshTokenIntrospection")
    public void testRefreshTokenGrantWithPersistentRT() throws Exception {

        String oldRefreshToken = refreshTokenPersistentRT;

        Map<String, String> params = new HashMap<>();
        params.put("grant_type", OAuth2Constant.OAUTH2_GRANT_TYPE_REFRESH_TOKEN);
        params.put(OAuth2Constant.OAUTH2_GRANT_TYPE_REFRESH_TOKEN, refreshTokenPersistentRT);

        Response response = getResponseOfFormPostWithAuth(OAUTH2_TOKEN_ENDPOINT_URI, params, new HashMap<>(),
                application.getClientId(), application.getClientSecret());

        response.then()
                .log().ifValidationFails()
                .assertThat()
                .statusCode(HttpStatus.SC_OK)
                .body("access_token", notNullValue())
                .body("refresh_token", notNullValue());

        String newAT = response.then().extract().path("access_token");
        String newRT = response.then().extract().path("refresh_token");

        Assert.assertTrue(isJWT(newAT), "New access token should be a JWT.");
        validateAccessTokenClaims(newAT, "USER_ID");

        Assert.assertFalse(isJWT(newRT), "New persistent refresh token should be opaque.");
        Assert.assertTrue(newRT.startsWith(NPR_PREFIX),
                "New persistent refresh token should start with '" + NPR_PREFIX + "'.");
        Assert.assertNotEquals(newRT, oldRefreshToken, "New refresh token should differ from old one.");

        // Update to the latest refresh token for use in the next test.
        refreshTokenPersistentRT = newRT;
    }

    @Test(groups = "wso2.is",
            description = "Validate that revoking a persistent refresh token prevents subsequent refresh grants",
            dependsOnMethods = "testRefreshTokenGrantWithPersistentRT")
    public void testPersistentRefreshTokenRevocationPreventsRefresh() {

        // Revoke the persistent refresh token.
        Map<String, String> revokeParams = new HashMap<>();
        revokeParams.put("token", refreshTokenPersistentRT);
        revokeParams.put("token_type_hint", "refresh_token");

        Response revokeResponse = getResponseOfFormPostWithAuth(OAUTH2_REVOKE_ENDPOINT_URI, revokeParams,
                new HashMap<>(), application.getClientId(), application.getClientSecret());

        revokeResponse.then()
                .log().ifValidationFails()
                .assertThat()
                .statusCode(HttpStatus.SC_OK);

        // Attempt a refresh grant with the now-revoked persistent RT.
        // Since the RT is stored in DB, revocation takes effect and the grant should FAIL.
        Map<String, String> refreshParams = new HashMap<>();
        refreshParams.put("grant_type", OAuth2Constant.OAUTH2_GRANT_TYPE_REFRESH_TOKEN);
        refreshParams.put(OAuth2Constant.OAUTH2_GRANT_TYPE_REFRESH_TOKEN, refreshTokenPersistentRT);

        Response refreshResponse = getResponseOfFormPostWithAuth(OAUTH2_TOKEN_ENDPOINT_URI, refreshParams,
                new HashMap<>(), application.getClientId(), application.getClientSecret());

        refreshResponse.then()
                .log().ifValidationFails()
                .assertThat()
                .statusCode(HttpStatus.SC_BAD_REQUEST);
    }

    // =====================================================================
    // Application creation
    // =====================================================================

    @Override
    public void createApplication(OIDCApplication application) throws Exception {

        ApplicationModel applicationModel = new ApplicationModel();

        List<String> grantTypes = new ArrayList<>();
        Collections.addAll(grantTypes, "authorization_code", OAuth2Constant.OAUTH2_GRANT_TYPE_RESOURCE_OWNER,
                OAuth2Constant.OAUTH2_GRANT_TYPE_CLIENT_CREDENTIALS, OAuth2Constant.OAUTH2_GRANT_TYPE_REFRESH_TOKEN);

        OpenIDConnectConfiguration oidcConfig = new OpenIDConnectConfiguration();
        oidcConfig.setGrantTypes(grantTypes);
        oidcConfig.addCallbackURLsItem(application.getCallBackURL());

        AccessTokenConfiguration accessTokenConfig = new AccessTokenConfiguration().type("JWT");
        accessTokenConfig.setUserAccessTokenExpiryInSeconds(3600L);
        accessTokenConfig.setApplicationAccessTokenExpiryInSeconds(3600L);
        oidcConfig.setAccessToken(accessTokenConfig);

        // Set claim configuration for email, given_name, family_name.
        ClaimConfiguration claimConfiguration = new ClaimConfiguration().dialect(DialectEnum.CUSTOM);
        for (String claimUri : application.getRequiredClaims()) {
            ClaimMappings claimMapping = new ClaimMappings().applicationClaim(claimUri);
            claimMapping.setLocalClaim(new Claim().uri(claimUri));

            RequestedClaimConfiguration requestedClaim = new RequestedClaimConfiguration();
            requestedClaim.setClaim(new Claim().uri(claimUri));

            claimConfiguration.addClaimMappingsItem(claimMapping);
            claimConfiguration.addRequestedClaimsItem(requestedClaim);
        }

        applicationModel.setName(application.getApplicationName());
        applicationModel.setInboundProtocolConfiguration(new InboundProtocols().oidc(oidcConfig));
        applicationModel.setClaimConfiguration(claimConfiguration);

        String applicationId = addApplication(applicationModel);
        oidcConfig = getOIDCInboundDetailsOfApplication(applicationId);

        application.setApplicationId(applicationId);
        application.setClientId(oidcConfig.getClientId());
        application.setClientSecret(oidcConfig.getClientSecret());
    }

    // =====================================================================
    // Configuration helpers
    // =====================================================================

    /**
     * Apply the fully non-persistent config (persist_access_token=false, persist_refresh_token=false)
     * and restart the server.
     */
    private void changeISConfiguration() throws Exception {

        String carbonHome = Utils.getResidentCarbonHome();
        File defaultTomlFile = getDeploymentTomlFile(carbonHome);
        File configuredTomlFile = new File(getISResourceLocation() + File.separator + "oauth" + File.separator
                + "non_persistent_access_token.toml");
        serverConfigurationManager = new ServerConfigurationManager(isServer);
        serverConfigurationManager.applyConfigurationWithoutRestart(configuredTomlFile, defaultTomlFile, true);
        serverConfigurationManager.restartGracefully();
    }

    /**
     * Apply the non-persistent AT + persistent RT config (persist_access_token=false, persist_refresh_token=true)
     * and restart the server.
     */
    private void changeToPersistentRefreshTokenConfig() throws Exception {

        // First, restore the original deployment.toml so the backup file is clean.
        serverConfigurationManager.restoreToLastConfiguration(false);

        String carbonHome = Utils.getResidentCarbonHome();
        File defaultTomlFile = getDeploymentTomlFile(carbonHome);
        File configuredTomlFile = new File(getISResourceLocation() + File.separator + "oauth" + File.separator
                + "non_persistent_access_token_persistent_refresh_token.toml");
        serverConfigurationManager = new ServerConfigurationManager(isServer);
        serverConfigurationManager.applyConfigurationWithoutRestart(configuredTomlFile, defaultTomlFile, true);
        serverConfigurationManager.restartGracefully();
    }

    // =====================================================================
    // Validation helpers
    // =====================================================================

    /**
     * Updates the test user's password via the SCIM2 admin API.
     *
     * @param newPassword The new password to set.
     * @throws Exception If the SCIM2 patch request fails.
     */
    private void changeUserPassword(String newPassword) throws Exception {

        String patchJson = "{"
                + "\"schemas\": [\"urn:ietf:params:scim:api:messages:2.0:PatchOp\"],"
                + "\"Operations\": [{\"op\": \"replace\", \"value\": {\"password\": \"" + newPassword + "\"}}]"
                + "}";
        scim2RestClient.patchUserWithRawJSON(patchJson, userId);
    }

    private void validateAccessTokenClaims(String token, String expectedEntityType) throws Exception {

        JWTClaimsSet claims = SignedJWT.parse(token).getJWTClaimsSet();
        Assert.assertNotNull(claims.getClaim("entity_id"), "entity_id claim is missing in access token.");
        Assert.assertEquals(claims.getStringClaim("entityType"), expectedEntityType,
                "entityType mismatch in access token.");
        Assert.assertNotNull(claims.getClaim("token_id"), "token_id claim is missing in access token.");
    }

    private void validateRefreshTokenClaims(String token) throws Exception {

        SignedJWT signedJWT = SignedJWT.parse(token);
        JWTClaimsSet claims = signedJWT.getJWTClaimsSet();
        Assert.assertNotNull(claims.getClaim("entity_id"), "entity_id claim is missing in refresh token.");
        Assert.assertNotNull(claims.getClaim("token_id"), "token_id claim is missing in refresh token.");
        Assert.assertNotNull(claims.getClaim("rt_scope"), "rt_scope claim is missing in refresh token.");
        Assert.assertEquals(signedJWT.getHeader().getType().toString(), "rt+jwt",
                "Refresh token header type should be rt+jwt.");
    }

    private boolean isJWT(String token) {

        return token != null && token.split("\\.").length == 3;
    }

    // =====================================================================
    // HTTP helpers
    // =====================================================================

    private Response getResponseOfFormPostWithAuth(String endpointUri, Map<String, String> params,
                                                   Map<String, String> headers, String username, String password) {

        return given().auth().preemptive().basic(username, password)
                .headers(headers)
                .params(params)
                .when()
                .post(endpointUri);
    }

    private Response getResponseOfGet(String endpointUri, Map<String, String> headers) {

        return given()
                .headers(headers)
                .when()
                .get(endpointUri);
    }
}

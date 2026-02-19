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

package org.wso2.identity.integration.test.rest.api.server.session;

import io.restassured.RestAssured;
import io.restassured.response.Response;
import org.apache.commons.lang.StringUtils;
import org.apache.http.HttpStatus;
import org.apache.http.client.CookieStore;
import org.testng.annotations.AfterClass;
import org.testng.annotations.AfterMethod;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.DataProvider;
import org.testng.annotations.Factory;
import org.testng.annotations.Test;
import org.wso2.carbon.automation.engine.context.TestUserMode;
import org.wso2.identity.integration.test.rest.api.server.application.management.v1.model.ApplicationModel;
import org.wso2.identity.integration.test.rest.api.server.application.management.v1.model.InboundProtocols;
import org.wso2.identity.integration.test.rest.api.server.application.management.v1.model.OpenIDConnectConfiguration;
import org.wso2.identity.integration.test.restclients.OAuth2RestClient;

import java.util.ArrayList;
import java.util.Collections;

import static org.hamcrest.CoreMatchers.equalTo;
import static org.testng.Assert.assertFalse;
import static org.testng.Assert.assertTrue;

/**
 * Test class for verifying session management with maximum timeout period feature.
 */
public class SessionMgtTest extends SessionMgtTestBase {

    private static final String APPLICATION_NAME = "SessionTestApp";
    private static final String CALLBACK_URL = "https://example.com/callback";
    
    private OAuth2RestClient restClient;
    private String appId;
    private String clientId;

    @Factory(dataProvider = "restAPIUserConfigProvider")
    public SessionMgtTest(TestUserMode userMode) throws Exception {

        super.init(userMode);
        this.context = isServer;
        this.authenticatingUserName = context.getContextTenant().getTenantAdmin().getUserName();
        this.authenticatingCredential = context.getContextTenant().getTenantAdmin().getPassword();
        this.tenant = context.getContextTenant().getDomain();
    }

    @DataProvider(name = "restAPIUserConfigProvider")
    public static Object[][] restAPIUserConfigProvider() {

        return new Object[][]{
                {TestUserMode.SUPER_TENANT_ADMIN},
                {TestUserMode.TENANT_ADMIN}
        };
    }

    @BeforeClass(alwaysRun = true)
    public void init() throws Exception {

        super.testInit(API_VERSION, swaggerDefinition, tenant);
        restClient = new OAuth2RestClient(serverURL, context.getContextTenant());
        
        // Create test application.
        createTestApplication();

        // Set base URI for REST Assured for restoring default configuration.
        testInit();
        restoreDefaultConfiguration();
        testFinish();
    }

    @AfterClass(alwaysRun = true)
    public void finish() throws Exception {

        super.conclude();

        // Delete test application.
        if (appId != null) {
            restClient.deleteApplication(appId);
        }
        restClient.closeHttpClient();
    }

    /**
     * Test session extension with idle timeout when maximum timeout is disabled.
     */
    @Test(description = "Test session extension with idle timeout (no max timeout)")
    public void testSessionExtensionWithIdleTimeout() throws Exception {

        updateServerConfiguration("1", null, "false", "1");

        CookieStore cookieStore = performLogin(false);

        Thread.sleep(30 * 1000);

        boolean sessionActive = checkSessionActive(cookieStore);
        assertTrue(sessionActive, "Session should be active and extended within idle timeout period");

        Thread.sleep(35 * 1000);

        sessionActive = checkSessionActive(cookieStore);
        assertTrue(sessionActive, "Session should still be active after extension");

        Thread.sleep(60 * 1000);

        sessionActive = checkSessionActive(cookieStore);
        assertFalse(sessionActive, "Session should be expired after idle timeout");
    }

    /**
     * Test session extension with remember me enabled.
     */
    @Test(description = "Test session with remember me enabled", dependsOnMethods = {
            "testSessionExtensionWithIdleTimeout"})
    public void testSessionWithRememberMe() throws Exception {

        updateServerConfiguration("1", "2", "false", "1");

        CookieStore cookieStore = performLogin(true);

        Thread.sleep(90 * 1000);

        boolean sessionActive = checkSessionActive(cookieStore);
        assertTrue(sessionActive, "Session should be active within remember me period");

        Thread.sleep(60 * 1000);

        sessionActive = checkSessionActive(cookieStore);
        assertFalse(sessionActive, "Session should be expired after remember me period");
    }

    /**
     * Test maximum session timeout cap with idle timeout.
     */
    @Test(description = "Test maximum timeout caps session extension", dependsOnMethods = {
            "testSessionWithRememberMe"})
    public void testMaximumSessionTimeoutWithIdleTimeout() throws Exception {

        updateServerConfiguration("1", null, "true", "2");

        CookieStore cookieStore = performLogin(false);

        Thread.sleep(45 * 1000);
        boolean sessionActive = checkSessionActive(cookieStore);
        assertTrue(sessionActive, "Session should be active after first extension");

        Thread.sleep(45 * 1000);
        sessionActive = checkSessionActive(cookieStore);
        assertTrue(sessionActive, "Session should be active after second extension");

        Thread.sleep(30 * 1000);

        sessionActive = checkSessionActive(cookieStore);
        assertFalse(sessionActive, "Session should be expired due to maximum timeout cap");
    }

    /**
     * Test maximum session timeout cap with remember me enabled.
     */
    @Test(description = "Test maximum timeout caps remember me period", dependsOnMethods = {
            "testMaximumSessionTimeoutWithIdleTimeout"})
    public void testMaximumSessionTimeoutWithRememberMe() throws Exception {

        updateServerConfiguration("1", "2", "true", "3");

        CookieStore cookieStore = performLogin(true);

        Thread.sleep(100 * 1000);
        boolean sessionActive = checkSessionActive(cookieStore);
        assertTrue(sessionActive, "Session should be active within max timeout");

        Thread.sleep(50 * 1000);
        sessionActive = checkSessionActive(cookieStore);
        assertTrue(sessionActive, "Session should still be within max timeout");

        Thread.sleep(30 * 1000);

        sessionActive = checkSessionActive(cookieStore);
        assertFalse(sessionActive, "Session should be expired due to maximum timeout cap, " +
                "even with remember me enabled");
    }

    /**
     * Test default configuration values for session management.
     */
    @Test(description = "Test default configuration values for session management", dependsOnMethods = {
            "testMaximumSessionTimeoutWithRememberMe"})
    public void testDefaultConfigurationValues() throws Exception {

        // Remove all session timeout configurations to verify defaults.
        restoreDefaultConfiguration();
        
        // Get configuration and verify default values.
        Response response = getResponseOfGet(CONFIGS_API_PATH);
        response.then()
                .log().ifValidationFails()
                .assertThat()
                .statusCode(HttpStatus.SC_OK)
                .body(ENABLE_MAX_TIMEOUT_KEY, equalTo(DEFAULT_ENABLE_MAX_TIMEOUT))
                .body(MAX_TIMEOUT_KEY, equalTo(DEFAULT_MAX_TIMEOUT));
    }

    /**
     * Creates the test application for OAuth2 flows.
     */
    private void createTestApplication() throws Exception {

        ApplicationModel applicationModel = new ApplicationModel();
        applicationModel.setName(APPLICATION_NAME);
        InboundProtocols inboundProtocols = new InboundProtocols();
        OpenIDConnectConfiguration oidcConfig = new OpenIDConnectConfiguration();
        oidcConfig.setGrantTypes(new ArrayList<>(Collections.singletonList(CODE_GRANT_TYPE)));
        oidcConfig.setCallbackURLs(new ArrayList<>(Collections.singletonList(CALLBACK_URL)));
        inboundProtocols.setOidc(oidcConfig);
        applicationModel.setInboundProtocolConfiguration(inboundProtocols);
        
        appId = restClient.createApplication(applicationModel);
        OpenIDConnectConfiguration oidcDetails = restClient.getOIDCInboundDetails(appId);
        clientId = oidcDetails.getClientId();
    }

    /**
     * Performs login flow and returns the cookie store with session cookies.
     *
     * @param rememberMe Whether to enable remember me.
     * @return CookieStore containing session cookies.
     * @throws Exception If an error occurs during login.
     */
    private CookieStore performLogin(boolean rememberMe) throws Exception {

        return performLogin(clientId, CALLBACK_URL, rememberMe, authenticatingUserName, authenticatingCredential);
    }

    /**
     * Checks if the session is still active by making an authorize request.
     *
     * @param cookieStore Cookie store containing session cookies.
     * @return True if session is active, false otherwise.
     * @throws Exception If an error occurs during session check.
     */
    private boolean checkSessionActive(CookieStore cookieStore) throws Exception {

        return checkSessionActive(cookieStore, clientId, CALLBACK_URL);
    }
}

/*
 * Copyright (c) 2026, WSO2 LLC. (http://www.wso2.com).
 *
 * WSO2 LLC. licenses this file to you under the Apache License,
 * Version 2.0 (the "License"); you may not use this file except
 * in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied. See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */

package org.wso2.identity.integration.test.rest.api.server.session;

import org.apache.http.client.CookieStore;
import org.json.simple.JSONObject;
import org.testng.annotations.Test;

import static org.testng.Assert.assertEquals;
import static org.testng.Assert.assertFalse;
import static org.testng.Assert.assertTrue;

/**
 * Test that the maximum session timeout caps the remember me period, and that the session timeout
 * configurations fall back to the defaults once removed.
 */
public class SessionMaximumTimeoutWithRememberMeTest extends SessionTimeoutTestBase {

    public SessionMaximumTimeoutWithRememberMeTest() throws Exception {

        super("sessionmaxtimeoutrememberme.com");
    }

    @Test(description = "Test maximum timeout caps remember me period")
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

    @Test(description = "Test default configuration values for session management",
            dependsOnMethods = {"testMaximumSessionTimeoutWithRememberMe"})
    public void testDefaultConfigurationValues() throws Exception {

        // Remove all session timeout configurations to verify the defaults.
        restoreDefaultConfiguration();

        JSONObject configuration = getServerConfiguration();
        assertEquals(configuration.get(ENABLE_MAX_TIMEOUT_KEY), DEFAULT_ENABLE_MAX_TIMEOUT);
        assertEquals(configuration.get(MAX_TIMEOUT_KEY), DEFAULT_MAX_TIMEOUT);
    }
}

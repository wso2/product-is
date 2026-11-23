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
import org.testng.annotations.Test;

import static org.testng.Assert.assertFalse;
import static org.testng.Assert.assertTrue;

/**
 * Test that the maximum session timeout caps the session extension with idle timeout.
 */
public class SessionMaximumTimeoutWithIdleTimeoutTest extends SessionTimeoutTestBase {

    public SessionMaximumTimeoutWithIdleTimeoutTest() throws Exception {

        super("sessionmaxtimeoutidle.com");
    }

    @Test(description = "Test maximum timeout caps session extension")
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
}

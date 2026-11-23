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
 * Test session extension with idle timeout when the maximum session timeout is disabled.
 */
public class SessionIdleTimeoutTest extends SessionTimeoutTestBase {

    public SessionIdleTimeoutTest() throws Exception {

        super("sessionidletimeout.com");
    }

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
}

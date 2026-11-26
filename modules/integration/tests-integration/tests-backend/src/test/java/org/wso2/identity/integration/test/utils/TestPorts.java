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

package org.wso2.identity.integration.test.utils;

/**
 * Every network port the integration tests use, in one place.
 *
 * Each port is its usual value plus a shift (default 0), so test lanes running in parallel on one machine never
 * share a port. There are two shifts:
 * - {@link #SERVER_SHIFT} ({@code lane.server.port.shift}) for ports that belong to the IS server: its own ports
 *   and the servers it is configured to call (external LDAP, mail, Thrift). Test JVMs that share one IS server use
 *   the same server shift.
 * - {@link #SHIFT} ({@code lane.port.shift}) for servers each test JVM starts for itself: Tomcat sample apps, mock
 *   servers and webhook receivers.
 * Without a server shift, it equals the JVM shift. With shift 0 every port is the same as before.
 *
 * The same ports are filled into the test resource files as {@code @port.*@} tokens, from
 * {@code src/test/lanes/ports.properties}. Keep the two in sync.
 */
public final class TestPorts {

    /** Added to the ports of servers this test JVM starts. */
    public static final int SHIFT = Integer.getInteger("lane.port.shift", 0);

    /** Added to the IS server's ports and the servers it calls. */
    public static final int SERVER_SHIFT = Integer.getInteger("lane.server.port.shift", SHIFT);

    /** Port offset of the primary IS server. Secondary servers use IS_OFFSET + 1 and IS_OFFSET + 2. */
    public static final int IS_OFFSET = 410 + SERVER_SHIFT;

    // The IS servers' own ports (base port + IS_OFFSET), and the servers IS is configured to call.
    public static final int IS_HTTPS = 9853 + SERVER_SHIFT;
    public static final int IS_HTTP = 10173 + SERVER_SHIFT;
    public static final int IS2_HTTPS = 9854 + SERVER_SHIFT;
    public static final int IS2_HTTP = 10174 + SERVER_SHIFT;
    public static final int IS3_HTTPS = 9855 + SERVER_SHIFT;
    public static final int IS3_HTTP = 10175 + SERVER_SHIFT;
    public static final int IS_EMBEDDED_LDAP = 10799 + SERVER_SHIFT;
    public static final int IS_THRIFT = 8021 + SERVER_SHIFT;
    /** Mail server the IS server sends to; started by the JVM that owns the server. */
    public static final int SMTP = 3025 + SERVER_SHIFT;

    // Servers each test JVM starts for itself.
    public static final int TOMCAT = 8490 + SHIFT;
    public static final int MOCK_SMS_PROVIDER = 8090 + SHIFT;
    public static final int MOCK_HTTP_EMAIL_PROVIDER = 8094 + SHIFT;
    public static final int MOCK_OIDC_IDP = 8089 + SHIFT;
    public static final int MOCK_APPLICATION = 8091 + SHIFT;
    public static final int MOCK_OAUTH2_TOKEN_SERVER = 8093 + SHIFT;
    public static final int MOCK_SERVICE_EXTENSION = 8587 + SHIFT;
    public static final int MOCK_CUSTOM_AUTHENTICATOR = 3999 + SHIFT;
    /** Webhook receivers use ports from WEBHOOK_RANGE_START to WEBHOOK_RANGE_END. */
    public static final int WEBHOOK_RANGE_START = 8580 + SHIFT;
    public static final int WEBHOOK_RANGE_END = 8590 + SHIFT;

    private TestPorts() {

    }
}

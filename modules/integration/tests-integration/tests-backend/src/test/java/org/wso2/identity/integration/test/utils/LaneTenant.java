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
 * The tenant that tenant-mode tests run in: {@code wso2.com} by default. A test lane that shares an IS server
 * with other lanes gets its own tenant through the {@code lane.tenant} system property, so lanes don't see each
 * other's data. The same value is filled into test resources as the {@code @lane.tenant@} token
 * (automation.xml and expected responses), from {@code src/test/lanes/ports.properties}.
 */
public final class LaneTenant {

    public static final String DOMAIN = System.getProperty("lane.tenant", "wso2.com");

    private LaneTenant() {

    }
}

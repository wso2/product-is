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

import org.testng.Assert;
import org.testng.annotations.Test;
import org.wso2.charon.core.extensions.AuthenticationInfo;

/**
 * Unit test cases for {@link BasicAuthHandler}.
 */
public class BasicAuthHandlerTestCase {

    @Test
    public void testGetAuthenticationInfo() {

        BasicAuthHandler basicAuthHandler = new BasicAuthHandler();
        AuthenticationInfo authenticationInfo = basicAuthHandler.getAuthenticationInfo();

        Assert.assertNotNull(authenticationInfo, "AuthenticationInfo should not be null.");
        Assert.assertTrue(authenticationInfo instanceof BasicAuthInfo,
                "AuthenticationInfo should be an instance of BasicAuthInfo.");

        BasicAuthInfo basicAuthInfo = (BasicAuthInfo) authenticationInfo;
        Assert.assertEquals(basicAuthInfo.getUserName(), "wso2charonAdmin",
                "Username should match default charon admin username.");
        Assert.assertEquals(basicAuthInfo.getPassword(), "charonAdmin123@wso2",
                "Password should match default charon admin password.");

        String expectedHeader = basicAuthHandler.getBase64EncodedBasicAuthHeader("wso2charonAdmin",
                "charonAdmin123@wso2");
        Assert.assertEquals(basicAuthInfo.getAuthorizationHeader(), expectedHeader,
                "Authorization header should match expected base64 basic auth header.");
        Assert.assertTrue(basicAuthInfo.getAuthorizationHeader().startsWith("Basic "),
                "Authorization header should start with 'Basic ' prefix.");
    }
}

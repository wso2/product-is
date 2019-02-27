/*
 * Copyright (c) 2016, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
 *
 *  WSO2 Inc. licenses this file to you under the Apache License,
 *  Version 2.0 (the "License"); you may not use this file except
 *  in compliance with the License.
 *  You may obtain a copy of the License at
 *
 *    http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */
package org.wso2.identity.integration.test.analytics.authentication;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.testng.annotations.AfterClass;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.DataProvider;
import org.testng.annotations.Factory;
import org.testng.annotations.Test;
import org.wso2.carbon.automation.engine.context.TestUserMode;

import java.io.IOException;

public class AnalyticsLoginTestCase extends AbstractAnalyticsLoginTestCase {

    private static final Log log = LogFactory.getLog(AnalyticsLoginTestCase.class);

    @Factory(dataProvider = "samlConfigProvider")
    public AnalyticsLoginTestCase(SAMLConfig config) {

        if (log.isDebugEnabled()) {
            log.debug("SAML SSO Test initialized for " + config);
        }
        setConfig(config);
    }

    @BeforeClass(alwaysRun = true)
    public void testInit() throws Exception {

        super.testInit();
    }

    @AfterClass(alwaysRun = true)
    public void testClear() throws Exception {

        super.testClear();
    }

    @Test(description = "Add service provider", groups = "wso2.is")
    public void testAddSP() throws Exception {

        super.testAddSP();
    }

    @Test(alwaysRun = true, description = "Testing SAML SSO login", groups = "wso2.is", dependsOnMethods = {"testAddSP"})
    public void testSAMLSSOIsPassiveLogin() throws IOException {

        super.testSAMLSSOIsPassiveLogin();
    }

    @Test(alwaysRun = true, description = "Testing SAML SSO login", groups = "wso2.is",
            dependsOnMethods = {"testSAMLSSOIsPassiveLogin"})
    public void testSAMLSSOLogin() throws IOException {

        super.testSAMLSSOLogin();
    }

    @DataProvider(name = "samlConfigProvider")
    public static SAMLConfig[][] samlConfigProvider() {

        return new SAMLConfig[][]{
                {new SAMLConfig(TestUserMode.SUPER_TENANT_ADMIN, User.SUPER_TENANT_USER, HttpBinding.HTTP_REDIRECT,
                        ClaimType.NONE, App.SUPER_TENANT_APP_WITH_SIGNING)},
        };
    }
}

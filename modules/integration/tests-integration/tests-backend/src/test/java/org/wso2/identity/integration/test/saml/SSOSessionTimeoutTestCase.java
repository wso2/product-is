/*
*Copyright (c) 2005-2010, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
*
*WSO2 Inc. licenses this file to you under the Apache License,
*Version 2.0 (the "License"); you may not use this file except
*in compliance with the License.
*You may obtain a copy of the License at
*
*http://www.apache.org/licenses/LICENSE-2.0
*
*Unless required by applicable law or agreed to in writing,
*software distributed under the License is distributed on an
*"AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
*KIND, either express or implied.  See the License for the
*specific language governing permissions and limitations
*under the License.
*/
package org.wso2.identity.integration.test.saml;


import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.testng.Assert;
import org.testng.annotations.AfterClass;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;
import org.wso2.carbon.automation.test.utils.common.TestConfigurationProvider;
import org.wso2.carbon.integration.common.utils.mgt.ServerConfigurationManager;
import org.wso2.identity.integration.common.clients.sso.saml.SAMLSSOServiceClient;
import org.wso2.identity.integration.common.utils.ISIntegrationTest;

import java.io.File;

import static java.io.File.separator;

public class SSOSessionTimeoutTestCase extends ISIntegrationTest{

    private static final Log log = LogFactory.getLog(SSOSessionTimeoutTestCase.class);
    private ServerConfigurationManager serverConfigurationManager;

    @BeforeClass(alwaysRun = true)
    public void testInit() throws Exception {
        super.init();

        serverConfigurationManager = new ServerConfigurationManager(isServer);
        String identityXMLFile = TestConfigurationProvider.getResourceLocation("IS") +
                separator + "conf" + separator + "identity.xml";
        File srcFile = new File(identityXMLFile);
//        TestConfigurationProvider.getResourceLocation("IS");

        serverConfigurationManager.applyConfiguration(srcFile);
        super.init();
    }

    @Test(groups = "wso2.is", description = "Get SSO session timeout")
    public void testSSOSessionTimeout()
            throws Exception {
        SAMLSSOServiceClient ssoServiceClient =
                new SAMLSSOServiceClient(backendURL, sessionCookie);
        int ssoTimeoutVal = ssoServiceClient.getSSOSessionTimeout();
        Assert.assertEquals(ssoTimeoutVal,"120");
    }

    /**
     * Replace identity.xml file with the original one in the IS pack.
     *
     * @throws Exception
     */
    @AfterClass(alwaysRun = true)
    public void atEnd() throws Exception {
            Thread.sleep(3000);
            serverConfigurationManager.restoreToLastConfiguration();
            serverConfigurationManager = null;


    }
}

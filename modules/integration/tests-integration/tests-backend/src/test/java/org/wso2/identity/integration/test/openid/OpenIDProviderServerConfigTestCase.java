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
package org.wso2.identity.integration.test.openid;

import java.rmi.RemoteException;

import org.testng.Assert;
import org.testng.annotations.AfterClass;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;
import org.wso2.carbon.identity.provider.openid.stub.dto.OpenIDProviderInfoDTO;
import org.wso2.identity.integration.common.clients.openid.OpenIDProviderServiceClient;
import org.wso2.identity.integration.common.utils.ISIntegrationTest;

public class OpenIDProviderServerConfigTestCase extends ISIntegrationTest {

	OpenIDProviderServiceClient openidServiceClient;
	String adminUserName;
	
    @BeforeClass(alwaysRun = true)
    public void testInit() throws Exception {
        super.init();
        
        openidServiceClient = new OpenIDProviderServiceClient(backendURL, sessionCookie);
        adminUserName = userInfo.getUserName();
    }
    
    @AfterClass(alwaysRun = true)
    public void atEnd() throws Exception {
     	openidServiceClient = null;
    }


    @Test(alwaysRun = true, description = "Testing Provider Info")
    public void testProviderInfo() {

        OpenIDProviderInfoDTO providerInfo = null;

        try {
            providerInfo = openidServiceClient.getOpenIDProviderInfo(adminUserName,
                    Util.getDefaultOpenIDIdentifier(adminUserName));
        } catch (Exception e) {
            Assert.fail("Error while getting OpenID Provider Info", e);
        }

        Assert.assertEquals(providerInfo.getOpenID(), "https://localhost:9853/openid/admin");

        Assert.assertEquals(providerInfo.getOpenIDProviderServerUrl(), "https://localhost:9853/openidserver");
    }

    @Test(alwaysRun = true, description = "Test Session Timeout", dependsOnMethods = "testProviderInfo")
    public void testSessionTimeOut() {
        
        int sessionTimeout = 0;
        try {
        	sessionTimeout = openidServiceClient.getOpenIDSessionTimeout();
        } catch (RemoteException e) {
            Assert.fail("Error while getting session timeout", e);
        }
        
        Assert.assertEquals(sessionTimeout, 36000);
    }
    
    @Test(alwaysRun = true, description = "Check SkipUserConsent", dependsOnMethods = "testSessionTimeOut")
    public void testSkipUserConsent() {
        
        boolean isSkipped = true;
        try {
        	isSkipped = openidServiceClient.isOpenIDUserApprovalBypassEnabled();
        } catch (RemoteException e) {
            Assert.fail("Error while reading SkipUserConsent config", e);
        }
        
        Assert.assertFalse(isSkipped);
    }
}

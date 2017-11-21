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

import org.testng.Assert;
import org.testng.annotations.AfterClass;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;
import org.wso2.carbon.identity.provider.openid.stub.dto.OpenIDRememberMeDTO;
import org.wso2.identity.integration.common.clients.openid.OpenIDProviderServiceClient;
import org.wso2.identity.integration.common.utils.ISIntegrationTest;

public class OpenIDAuthenticationTestCase extends ISIntegrationTest {
    
	OpenIDProviderServiceClient openidServiceClient;
	String adminUserName;
	String adminPassword;
	
    @BeforeClass(alwaysRun = true)
    public void testInit() throws Exception {
        super.init();
        
        openidServiceClient = new OpenIDProviderServiceClient(backendURL, sessionCookie);
        adminUserName = userInfo.getUserName();
        adminPassword = userInfo.getPassword();
    }
    
    @AfterClass(alwaysRun = true)
    public void atEnd() throws Exception {
 
    	openidServiceClient = null;
    }

    @Test(alwaysRun = true, description = "Authenticate with Password")
    public void testOpenidPasswordAuthentication() {
        
        String openId = Util.getDefaultOpenIDIdentifier(adminUserName);
        
        boolean isAuthenticated = false;
        
        try {
            isAuthenticated = openidServiceClient.authenticateWithOpenID(openId, adminPassword);
        } catch (Exception e) {
            Assert.fail("Error while authenticating", e);
        }
        
        Assert.assertTrue(isAuthenticated);
    }

    @Test(alwaysRun = true, description = "Authenticate with Remember ME", dependsOnMethods="testOpenidPasswordAuthentication")
    public void testOpenidRememberMeAuthentication() {
        
        // first authenticate without a cookie 
        String openID = Util.getDefaultOpenIDIdentifier(adminUserName);
        OpenIDRememberMeDTO rememberMeDTO = null;
        
        try {
            rememberMeDTO = openidServiceClient.authenticateWithOpenIDRememberMe(openID, adminPassword, "127.0.0.1", null);
        } catch (Exception e) {
            Assert.fail("Error while authenticating with remember me", e);
        }
        
        Assert.assertTrue(rememberMeDTO.getAuthenticated());
        
        // now lets authenticate with remember me
        
        String cookie = rememberMeDTO.getNewCookieValue();
        
        OpenIDRememberMeDTO newRememberMeDTO = null;
        
        try {
            newRememberMeDTO = openidServiceClient.authenticateWithOpenIDRememberMe(openID, null, "127.0.0.1", cookie);
        } catch (Exception e) {
            Assert.fail("Error while authenticating with remember me cookie", e);
        }
        
        Assert.assertTrue(newRememberMeDTO.getAuthenticated());
        
    }

}

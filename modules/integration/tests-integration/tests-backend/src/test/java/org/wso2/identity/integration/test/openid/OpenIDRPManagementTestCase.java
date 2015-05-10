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

import java.util.Calendar;
import java.util.Date;

import org.testng.Assert;
import org.testng.annotations.AfterClass;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;
import org.wso2.carbon.identity.provider.openid.stub.dto.OpenIDUserRPDTO;
import org.wso2.identity.integration.common.clients.openid.OpenIDProviderServiceClient;
import org.wso2.identity.integration.common.utils.ISIntegrationTest;

public class OpenIDRPManagementTestCase extends ISIntegrationTest {

	OpenIDProviderServiceClient openidServiceClient;
	
    private String userName = "admin";
    private String profileName = "default";
    
    private String rp1Url = "http://localhost:8090/openidclient";
    private boolean rp1TrustedAlways = false;
    private int rp1VisitCount = 0;
    private Date rp1lastVisit = Calendar.getInstance().getTime();
    
    private String rp2Url = "http://localhost:8090/openidclient2";
    private boolean rp2TrustedAlways = true;
    private int rp2VisitCount = 1;
    private Date rp2lastVisit = Calendar.getInstance().getTime();
    
    @BeforeClass(alwaysRun = true)
    public void testInit() throws Exception {
        super.init();
        
        openidServiceClient = new OpenIDProviderServiceClient(backendURL, sessionCookie);
    }
    
    @AfterClass(alwaysRun = true)
    public void atEnd() throws Exception {
     	openidServiceClient = null;
    }

    @Test(alwaysRun = true, description = "Authenticate with Password")
    public void testOpenidRPAddUpdate() {
        
        // add rp1
        OpenIDUserRPDTO rp1dto = new OpenIDUserRPDTO();
        rp1dto.setDefaultProfileName(profileName);
        rp1dto.setUserName(userName);
        rp1dto.setOpenID(Util.getDefaultOpenIDIdentifier(userName));
        rp1dto.setRpUrl(rp1Url);
        rp1dto.setTrustedAlways(rp1TrustedAlways);
        rp1dto.setVisitCount(rp1VisitCount);
        rp1dto.setLastVisit(rp1lastVisit);
        
        try {
            openidServiceClient.updateOpenIDUserRPInfo(rp1dto);
        } catch (Exception e) {
            Assert.fail("Error while updating user RPInfo", e);
        }
        
        // add rp2
        OpenIDUserRPDTO rp2dto = new OpenIDUserRPDTO();
        rp2dto.setDefaultProfileName(profileName);
        rp2dto.setUserName(userName);
        rp2dto.setOpenID(Util.getDefaultOpenIDIdentifier(userName));
        rp2dto.setRpUrl(rp2Url);
        rp2dto.setTrustedAlways(rp2TrustedAlways);
        rp2dto.setVisitCount(rp2VisitCount);
        rp2dto.setLastVisit(rp2lastVisit);
        
        try {
            openidServiceClient.updateOpenIDUserRPInfo(rp2dto);
        } catch (Exception e) {
            Assert.fail("Error while updating user RPInfo", e);
        }
        
        // reading rps back 
        OpenIDUserRPDTO[] rps = null;
        
        try {
            rps = openidServiceClient.getOpenIDUserRPs(Util.getDefaultOpenIDIdentifier(userName));
        } catch (Exception e) {
            Assert.fail("Error while getting user RPs", e);
        }
        
        // we should get two rps 
        Assert.assertEquals(rps.length, 2);
        
        // lets read values back and check
        for(OpenIDUserRPDTO rp : rps) {
            
            if(rp1Url.equals(rp.getRpUrl())) {
                Assert.assertEquals(rp.getTrustedAlways(), rp1TrustedAlways);
                Assert.assertEquals(rp.getUserName(), userName);
                
            } else if(rp2Url.equals(rp.getRpUrl())) {
                Assert.assertEquals(rp.getTrustedAlways(), rp2TrustedAlways);
                Assert.assertEquals(rp.getUserName(), userName);
                
            } else {
                Assert.fail("Invalid RP returned");
            }
            
        }
        
        // update the RP1, lets trust it always
        rp1TrustedAlways = true;
        rp1VisitCount++;
        rp1lastVisit = Calendar.getInstance().getTime();
        
        // update rp1
        OpenIDUserRPDTO rp1Updateddto = new OpenIDUserRPDTO();
        rp1Updateddto.setDefaultProfileName(profileName);
        rp1Updateddto.setUserName(userName);
        rp1Updateddto.setOpenID(Util.getDefaultOpenIDIdentifier(userName));
        rp1Updateddto.setRpUrl(rp1Url);
        rp1Updateddto.setTrustedAlways(rp1TrustedAlways);
        rp1Updateddto.setVisitCount(rp1VisitCount);
        rp1Updateddto.setLastVisit(rp1lastVisit);
        try {
            openidServiceClient.updateOpenIDUserRPInfo(rp1Updateddto);
        } catch (Exception e) {
            Assert.fail("Error while updating user RPInfo", e);
        }
        
        // read the RP1 back now
        OpenIDUserRPDTO rp1updted = null;
        try {
            rp1updted = openidServiceClient.getOpenIDUserRPInfo(Util.getDefaultOpenIDIdentifier(userName), rp1Url);
        } catch (Exception e) {
            Assert.fail("Error while updating user RPInfo", e);
        }
        
        Assert.assertEquals(rp1updted.getRpUrl(), rp1Url);
        
        Assert.assertEquals(rp1updted.getTrustedAlways(), rp1TrustedAlways);
        
    }

}

/*
 * Copyright (c) 2005-2014, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
 *
 * WSO2 Inc. licenses this file to you under the Apache License,
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

package org.wso2.identity.integration.test.certificateauthority;


import org.wso2.identity.integration.common.utils.ISIntegrationTest;

public class CaCsrServicesTestCase extends ISIntegrationTest {

    /*
    * Pre conditions : None
    *
    * Test Scenario:
    *
    * User 1 adds two CSRs and should get not null serial numbers
    * User 1 query CSR status using serial numbers and should get not null response
    * User 1 query CSR list and should get a array with size 2
    *
    * User 2 adds another csr and should get not null serial numbers
    * User 2 query CSR status of above using its serial number and should get not null response
    * User 2 query CSR status of one of user 1's csr and it should fail
    * User 2 query CSR list and should get a array with size 1
    *
    * */
/**
 * Commenting out until feature is complete
 */
//    private CAClientServiceClient clientServiceClient;
//
//    private String serialNo1;
//    private String serialNo2;
//    private String serialNo3;
//
//    @BeforeClass(alwaysRun = true)
//    public void testInit() throws Exception {
//        super.init(2);
//         clientServiceClient = new CAClientServiceClient(backendURL, sessionCookie);
//    }
//
//    @AfterClass(alwaysRun = true)
//    public void atEnd() throws Exception {
//        super.init();
//        CAAdminServiceClient adminServiceClient = new CAAdminServiceClient(backendURL, sessionCookie);
//        adminServiceClient.deleteCSR(serialNo1);
//        adminServiceClient.deleteCSR(serialNo2);
//        adminServiceClient.deleteCSR(serialNo3);
//    }
//
//    @Test(groups = "wso2.is", description = "Check Add CSR")
//    public void testAddCsr() throws Exception{
//        serialNo1 = clientServiceClient.addCsr(CaResources.csr1);
//        serialNo2 = clientServiceClient.addCsr(CaResources.csr2);
//        Assert.assertNotNull(serialNo1,"Adding new CSR returned null serial no");
//    }
//
//    @Test(groups = "wso2.is", description = "Check get added CSR", dependsOnMethods="testAddCsr")
//    public void testGetCsr() throws Exception{
//        CsrDTO csr = clientServiceClient.getCsr(serialNo1);
//        Assert.assertNotNull(csr,"CSR not added at server");
//        csr = clientServiceClient.getCsr(serialNo2);
//        Assert.assertNotNull(csr,"CSR not added at server");
//    }
//
//    @Test(groups = "wso2.is", description = "Check get added CSR list of a user", dependsOnMethods="testAddCsr")
//    public void testGetCsrList() throws Exception{
//        CsrMetaInfo[] csrList = clientServiceClient.getCsrList();
//        Assert.assertEquals(2,csrList.length,"User 1 added two requests, but "+csrList.length+" returned");
//    }
//
//    @Test(groups = "wso2.is", description = "Check get added CSR of other users", dependsOnMethods="testGetCsr", expectedExceptions = RemoteException.class)
//    public void testGetCsrOfOtherUser() throws Exception{
//        super.init(3);
//        clientServiceClient = new CAClientServiceClient(backendURL, sessionCookie);
//        serialNo3 = clientServiceClient.addCsr(CaResources.csr3);
//
//        CsrMetaInfo[] csrList = clientServiceClient.getCsrList();
//        Assert.assertEquals(1,csrList.length,"User 2 added only one request, but "+csrList.length+" returned");
//
//        CsrDTO csr = clientServiceClient.getCsr(serialNo3);
//        Assert.assertNotNull(csr);
//        csr = clientServiceClient.getCsr(serialNo1);    //should fail
//    }

}

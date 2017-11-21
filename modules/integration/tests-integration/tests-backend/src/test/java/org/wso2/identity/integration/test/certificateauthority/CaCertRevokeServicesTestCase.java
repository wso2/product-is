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

public class CaCertRevokeServicesTestCase extends ISIntegrationTest{

    /*
    * Pre conditions : Should have passed CaCertSignServicesTestCase
    *
    * Test Scenario:
    *
    * User 1 adds three CSRs csr1,csr2,csr3, admin sign them all cert1,cert2,cert3
    * Admin revoke cert3, It should have status revoked
    * User 1 revoke cert2, It should have status revoked
    * User 2 try to revoke cert1, It should fail
    *
    * */
/*
Commented out till the feature is fully complete
 */
//    private CAClientServiceClient clientServiceClient;
//    private CAClientServiceClient clientServiceClient2;
//    private CAAdminServiceClient adminServiceClient;
//
//    private String serialNo1;
//    private String serialNo2;
//    private String serialNo3;
//
//    @BeforeClass(alwaysRun = true)
//    public void testInit() throws Exception {
//        super.init();
//        clientServiceClient = new CAClientServiceClient(backendURL, sessionCookie);
//        super.init(3);
//        clientServiceClient2 = new CAClientServiceClient(backendURL, sessionCookie);
//        super.init();
//        adminServiceClient = new CAAdminServiceClient(backendURL, sessionCookie);
//
//        //todo: can this run after sign services test case
//        serialNo1 = clientServiceClient.addCsr(CaResources.csr1);
//        serialNo2 = clientServiceClient.addCsr(CaResources.csr2);
//        serialNo3 = clientServiceClient.addCsr(CaResources.csr3);
//
//        adminServiceClient.signCSR(serialNo1, 30);
//        adminServiceClient.signCSR(serialNo2, 30);
//        adminServiceClient.signCSR(serialNo3, 30);
//    }
//
//    @AfterClass(alwaysRun = true)
//    public void atEnd() throws Exception {
//        adminServiceClient.deleteCSR(serialNo1);
//        adminServiceClient.deleteCSR(serialNo2);
//        adminServiceClient.deleteCSR(serialNo3);
//
//        adminServiceClient.deleteCertificate(serialNo1);
//        adminServiceClient.deleteCertificate(serialNo2);
//        adminServiceClient.deleteCertificate(serialNo3);
//    }
//
//    @Test(groups = "wso2.is", description = "Test revoking certificate by admin")
//    public void testRevokeByAdmin() throws Exception{
//        adminServiceClient.revokeCertificate(serialNo3,CaResources.RevokeReason.REVOCATION_REASON_UNSPECIFIED_VAL);
//        CertificateDTO certificate3 = clientServiceClient.getCertificate(serialNo3);
//        Assert.assertEquals(CaResources.CertificateStatus.REVOKED,certificate3.getCertificateMetaInfo().getStatus(),
//                "Certificate is not revoked");
//    }
//
//    @Test(groups = "wso2.is", description = "Test revoking certificate by client")
//    public void testRevokeByClient() throws Exception{
//        clientServiceClient.revokeCertificate(serialNo2,CaResources.RevokeReason.REVOCATION_REASON_KEYCOMPROMISE_VAL);
//        CertificateDTO certificate2 = clientServiceClient.getCertificate(serialNo2);
//        Assert.assertEquals(CaResources.CertificateStatus.REVOKED,certificate2.getCertificateMetaInfo().getStatus(),"Certificate is not revoked");
//    }
//
//    @Test(groups = "wso2.is", description = "Test revoking certificate by other client",
//            expectedExceptions = RemoteException.class)
//    public void testRevokeByOtherClient() throws Exception{
//        CertificateDTO certificate1 = clientServiceClient2.getCertificate(serialNo1);  //todo fail
//        Assert.assertEquals(CaResources.CertificateStatus.ACTIVE,certificate1.getCertificateMetaInfo().getStatus(),
//                "Certificate is not active");
//
//
//        clientServiceClient2.revokeCertificate(serialNo1,CaResources.RevokeReason
//                .REVOCATION_REASON_KEYCOMPROMISE_VAL);
//    }




}

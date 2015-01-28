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

public class CaCertSignServicesTestCase extends ISIntegrationTest {

    /*
    * Pre conditions : Should have passed CaCsrServicesTestCase
    *
    * Test Scenario:
    *
    * User 1 adds three CSRs csr1,csr2,csr3
    * Admin signs 2 of them csr1,csr2
    * User 1 query status of CSRs 1,2 should be signed, and 3 should be revoked.
    *
    * */
/**
 * Commenting out till the feature is fully complete
 */
//    private CAClientServiceClient clientServiceClient;
//    private CAAdminServiceClient adminServiceClient;
//
//    private String serialNo1;
//    private String serialNo2;
//    private String serialNo3;
//
//    private String encodedCert1;
//    private String encodedCert2;
//
//    @BeforeClass(alwaysRun = true)
//    public void testInit() throws Exception {
//        super.init(2);
//        clientServiceClient = new CAClientServiceClient(backendURL, sessionCookie);
//        super.init();
//        adminServiceClient = new CAAdminServiceClient(backendURL, sessionCookie);
//
//        //todo: can this run after CSR services test case
//        serialNo1 = clientServiceClient.addCsr(CaResources.csr1);
//        serialNo2 = clientServiceClient.addCsr(CaResources.csr2);
//        serialNo3 = clientServiceClient.addCsr(CaResources.csr3);
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
//    @Test(groups = "wso2.is", description = "Test signing CSR")
//    public void testSignCsr() throws Exception{
//        CsrDTO csr1 = clientServiceClient.getCsr(serialNo1);
//        Assert.assertEquals(CaResources.CsrStatus.PENDING,csr1.getCsrMetaInfo().getStatus(),"CSR 1 is not in pending status");
//        CsrDTO csr2 = clientServiceClient.getCsr(serialNo2);
//        Assert.assertEquals(CaResources.CsrStatus.PENDING,csr2.getCsrMetaInfo().getStatus(),"CSR 2 is not in pending status");
//        adminServiceClient.signCSR(serialNo1, 30);
//        adminServiceClient.signCSR(serialNo2, 90);
//        csr1 = clientServiceClient.getCsr(serialNo1);
//        Assert.assertEquals(CaResources.CsrStatus.SIGNED,csr1.getCsrMetaInfo().getStatus(),"CSR 1 has not been signed");
//        csr2 = clientServiceClient.getCsr(serialNo2);
//        Assert.assertEquals(CaResources.CsrStatus.PENDING,csr2.getCsrMetaInfo().getStatus(),"CSR 2 has not been signed");
//        CertificateDTO certificate1 = clientServiceClient.getCertificate(serialNo1);
//        Assert.assertNotNull(certificate1,"Certificate 1 should be non-null");
//        CertificateDTO certificate2 = clientServiceClient.getCertificate(serialNo1);
//        Assert.assertNotNull(certificate2,"Certificate 2 should be non-null");
//        encodedCert1 = certificate1.getEncodedCertificate();
//        encodedCert2 = certificate2.getEncodedCertificate();
//    }
//
//    @Test(groups = "wso2.is", description = "Test signing CSR", dependsOnMethods = "testSignCsr")
//    public void testRejectCsr() throws Exception{
//        CsrDTO csr = clientServiceClient.getCsr(serialNo3);
//        Assert.assertEquals(CaResources.CsrStatus.PENDING,csr.getCsrMetaInfo().getStatus(),"CSR 3 is not in pending status");
//        adminServiceClient.rejectCSR(serialNo3);
//        csr = clientServiceClient.getCsr(serialNo3);
//        Assert.assertEquals(CaResources.CsrStatus.REJECTED,csr.getCsrMetaInfo().getStatus(),"CSR 3 has not been Rejected");
//    }


}

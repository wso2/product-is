package org.wso2.identity.integration.test.provisioning;

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
* KIND, either express or implied. See the License for the
* specific language governing permissions and limitations
* under the License.
*/

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.testng.Assert;
import org.testng.annotations.AfterClass;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;
import org.wso2.carbon.um.ws.api.stub.RemoteUserStoreManagerServiceUserStoreExceptionException;
import org.wso2.identity.integration.common.clients.usermgt.remote.RemoteUserStoreManagerServiceClient;
import org.wso2.identity.integration.test.saml.SAMLIdentityFederationTestCase;

import javax.xml.xpath.XPathExpressionException;
import java.rmi.RemoteException;

public class JustInTimeProvisioningTestCase extends SAMLIdentityFederationTestCase {

    private static Log log = LogFactory.getLog(JustInTimeProvisioningTestCase.class);

    @BeforeClass(alwaysRun = true)
    public void initTest() throws Exception {
        super.initTest();
    }

    @AfterClass(alwaysRun = true)
    public void endTest() throws Exception {
        super.endTest();
    }

    @Override
    @Test(priority = 1, groups = "wso2.is", description = "test testCreateIdentityProviderInPrimaryIS")
    public void testCreateIdentityProviderInPrimaryIS() throws Exception {
        super.testCreateIdentityProviderInPrimaryIS();
    }

    @Override
    @Test(priority = 2, groups = "wso2.is", description = "test testCreateServiceProviderInPrimaryIS")
    public void testCreateServiceProviderInPrimaryIS() throws Exception {
        super.testCreateServiceProviderInPrimaryIS();
    }

    @Override
    @Test(priority = 3, groups = "wso2.is", description = "test testCreateServiceProviderInSecondaryIS")
    public void testCreateServiceProviderInSecondaryIS() throws Exception {
        super.testCreateServiceProviderInSecondaryIS();
    }

    @Override
    @Test(priority = 4, groups = "wso2.is", description = "Check functionality of attribute consumer index")
    public void testAttributeConsumerIndex() throws Exception {
        super.testAttributeConsumerIndex();
    }

    @Override
    @Test(priority = 5, groups = "wso2.is", description = "test testSAMLToSAMLFederation")
    public void testSAMLToSAMLFederation() throws Exception {
        super.testSAMLToSAMLFederation();
    }

    @Test(priority = 6, groups = "wso2.is", description = "test Just in time provisioning")
    public void testJustInTimeProvisioning()
            throws RemoteException, RemoteUserStoreManagerServiceUserStoreExceptionException, XPathExpressionException {
        RemoteUserStoreManagerServiceClient userStoreClient = new RemoteUserStoreManagerServiceClient(getBackendURL()
                , getSessionCookie());
        Assert.assertTrue(userStoreClient.isExistingUser(getFederatedTestUser()));
    }
}
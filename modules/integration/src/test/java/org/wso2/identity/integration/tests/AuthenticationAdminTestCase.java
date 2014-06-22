/*
*  Copyright (c) WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
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

package org.wso2.identity.integration.tests;

import org.apache.axis2.AxisFault;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.testng.Assert;
import org.testng.annotations.AfterTest;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;
import org.wso2.carbon.authenticator.stub.AuthenticationAdminStub;
import org.wso2.carbon.authenticator.stub.LogoutAuthenticationExceptionException;
import org.wso2.carbon.integration.framework.ClientConnectionUtil;

import java.rmi.RemoteException;

/**
 * Test class to test authentications.
 */
public class AuthenticationAdminTestCase {

    private static Log logger = LogFactory.getLog(AuthenticationAdminTestCase.class);

    private AuthenticationAdminStub authenticationAdminStub;

    @BeforeClass(groups = {"wso2.is"})
    public void setUp() {
        ClientConnectionUtil.waitForPort(9443);
    }

    @Test (groups = "wso2.is")
    public void testAdminLogin() throws Exception{

        boolean b = login("admin", "admin");
        Assert.assertTrue(b, "Admin should be able to login.");
    }

    @AfterTest(groups = {"wso2.is"})
    public void tearDown() {

        if (this.authenticationAdminStub != null) {
            try {
                this.authenticationAdminStub.logout();
            } catch (RemoteException e) {
                logger.error("Error when logout", e);
            } catch (LogoutAuthenticationExceptionException e) {
                logger.error("Error when logout", e);
            }
        }

    }

    /**
     * Relevant Carbon Jira - https://wso2.org/jira/browse/CARBON-11225
     * @throws Exception If an error occurred while creating AuthenticationAdmin stub.
     */
    @Test (groups = "wso2.is")
    public void testWildCardAdminLogin() throws Exception{

        boolean b = login("admin*", "admin");
        Assert.assertFalse(b, "Admin* should not be able to login.");
    }

    private boolean login(String userName, String password) throws Exception {

        try {
            this.authenticationAdminStub
                    = new AuthenticationAdminStub(UserAdminConstants.AUTHENTICATION_ADMIN_SERVICE_URL);
            return this.authenticationAdminStub.login(userName, password, "localhost");
        } catch (AxisFault axisFault) {
            logger.error("Error creating authentication admin stub.", axisFault);
            throw axisFault;

        }
    }

}

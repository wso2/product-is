/*
 * Copyright (c) 2016, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
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

package org.wso2.identity.integration.test.identity.mgt;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.testng.Assert;
import org.testng.annotations.AfterClass;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;
import org.wso2.carbon.identity.governance.stub.bean.ConnectorConfig;
import org.wso2.carbon.identity.governance.stub.bean.Property;
import org.wso2.carbon.identity.governance.stub.IdentityGovernanceAdminServiceIdentityGovernanceExceptionException;
import org.wso2.identity.integration.common.clients.mgt.IdentityGovernanceServiceClient;
import org.wso2.identity.integration.common.utils.ISIntegrationTest;

import java.rmi.RemoteException;

public class IdentityGovernanceTestCase extends ISIntegrationTest {

    private static final Log log = LogFactory.getLog(IdentityGovernanceTestCase.class.getName());
    private static final String ACCOUNT_LOCK_TIME_PROPERTY_NAME = "account.lock.handler.Time";
    private static final String CONNECTOR_FRIENDLY_NAME = "Account Locking";
    private static final String ACCOUNT_LOCK_TIME_NEW_VALUE = "30";
    private IdentityGovernanceServiceClient identityGovernanceServiceClient;

    @BeforeClass(alwaysRun = true)
    public void testInit() throws Exception {
        super.init();
        identityGovernanceServiceClient = new IdentityGovernanceServiceClient(sessionCookie, backendURL);
    }

    @AfterClass(alwaysRun = true)
    public void atEnd() {
    }

    @Test(groups = "wso2.is", description = "Test retrieving identity governance connector configs")
    public void testGetConnectorConfigs() throws RemoteException, IdentityGovernanceAdminServiceIdentityGovernanceExceptionException {
        ConnectorConfig[] connectors = identityGovernanceServiceClient.getConnectorList();
        Assert.assertTrue(connectors.length>=1, "No connector configs received");
        boolean accountLockingAvailable = false;
        for (int i = 0; i < connectors.length; i++) {
            if (connectors[i].getFriendlyName().equals(CONNECTOR_FRIENDLY_NAME)) {
                Assert.assertEquals(connectors[i].getProperties().length, 5, "Account locking feature properties not " +
                        "received properly.");
                accountLockingAvailable = true;
                break;
            }
        }
        Assert.assertTrue(accountLockingAvailable, "Account locking connector details not available");
    }

    @Test(groups = "wso2.is", description = "Test updating identity governance connector configs")
    public void testUpdateConnectorProperties() throws RemoteException,
            IdentityGovernanceAdminServiceIdentityGovernanceExceptionException {

        Property[] newProperties = new Property[1];
        Property prop = new Property();
        prop.setName(ACCOUNT_LOCK_TIME_PROPERTY_NAME);
        prop.setValue(ACCOUNT_LOCK_TIME_NEW_VALUE);
        newProperties[0] = prop;
        identityGovernanceServiceClient.updateConfigurations(newProperties);
        ConnectorConfig[] connectors = identityGovernanceServiceClient.getConnectorList();
        Assert.assertTrue(connectors.length>=1, "No connector configs received");
        boolean propertyAvailable = false;
        outer:
        for (int i = 0; i < connectors.length; i++) {
            if (connectors[i].getFriendlyName().equals(CONNECTOR_FRIENDLY_NAME)) {
                Property[] connectorProperties = connectors[i].getProperties();
                for (int j = 0; j < connectorProperties.length; j++) {
                    if (ACCOUNT_LOCK_TIME_PROPERTY_NAME.equals(connectorProperties[j].getName())) {
                        Assert.assertEquals(connectorProperties[j].getValue(), ACCOUNT_LOCK_TIME_NEW_VALUE, "Property update unsuccessful");
                    }
                    propertyAvailable = true;
                }
            }
        }
        Assert.assertTrue(propertyAvailable, "Updated property was not available in connector configs retrieved.");
    }

}

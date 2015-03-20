/*
*  Copyright (c)  WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
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

package org.wso2.identity.integration.test.user.store.config;

import junit.framework.Assert;
import org.apache.axis2.AxisFault;
import org.testng.annotations.AfterClass;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;
import org.wso2.carbon.identity.user.store.configuration.stub.dto.PropertyDTO;
import org.wso2.carbon.identity.user.store.configuration.stub.dto.UserStoreDTO;
import org.wso2.carbon.user.core.jdbc.JDBCUserStoreManager;
import org.wso2.identity.integration.common.clients.user.store.config.UserStoreConfigAdminServiceClient;
import org.wso2.carbon.user.api.Property;
import org.wso2.identity.integration.common.utils.ISIntegrationTest;
import org.wso2.identity.integration.common.utils.UserStoreConfigUtils;

import java.util.Arrays;
import java.util.List;


public class UserStoreConfigAdminTestCase extends ISIntegrationTest {

    private UserStoreConfigAdminServiceClient userStoreConfigurationClient;
    private UserStoreConfigUtils userStoreConfigUtils = new UserStoreConfigUtils();
    private String jdbcClass = "org.wso2.carbon.user.core.jdbc.JDBCUserStoreManager";
    private String rwLDAPClass = "org.wso2.carbon.user.core.ldap.ReadWriteLDAPUserStoreManager";
    private String roLDAPClass = "org.wso2.carbon.user.core.ldap.ReadOnlyLDAPUserStoreManager";
    private String adLDAPClass = "org.wso2.carbon.user.core.ldap.ActiveDirectoryUserStoreManager";

    @BeforeClass(alwaysRun = true)
    public void testInit() throws Exception {
        super.init();
        userStoreConfigurationClient = new UserStoreConfigAdminServiceClient(backendURL, sessionCookie);
    }

    @Test(groups = "wso2.is", description = "Check user store manager implementations")
    public void testAvailableUserStoreClasses() throws Exception {
        String[] classes = userStoreConfigurationClient.getAvailableUserStoreClasses();
        List<String> classNames = Arrays.asList(classes);
        Assert.assertTrue(jdbcClass + " not present.", classNames.contains(jdbcClass));
        Assert.assertTrue(rwLDAPClass + " not present.", classNames.contains(rwLDAPClass));
        Assert.assertTrue(roLDAPClass + " not present.", classNames.contains(roLDAPClass));
        Assert.assertTrue(adLDAPClass + " not present.", classNames.contains(adLDAPClass));

    }

    @Test(groups = "wso2.is", description = "Check add user store via DTO", dependsOnMethods = "testAvailableUserStoreClasses")
    public void testAddUserStore() throws Exception {

        Property[] properties = (new JDBCUserStoreManager()).getDefaultUserStoreProperties().getMandatoryProperties();
        PropertyDTO[] propertyDTOs = new PropertyDTO[properties.length];
        for (int i = 0; i < properties.length; i++) {
            PropertyDTO propertyDTO = new PropertyDTO();
            propertyDTO.setName(properties[i].getName());
            propertyDTO.setValue(properties[i].getValue());
            propertyDTOs[i] = propertyDTO;
        }
        UserStoreDTO userStoreDTO = userStoreConfigurationClient.createUserStoreDTO(jdbcClass, "lanka.com", propertyDTOs);
        userStoreConfigurationClient.addUserStore(userStoreDTO);
        Assert.assertTrue("Domain addition via DTO has failed.", userStoreConfigUtils.waitForUserStoreDeployment
                (userStoreConfigurationClient, "lanka.com"));

    }

    @Test(expectedExceptions = AxisFault.class, groups = "wso2.is", description = "Check add user store via DTO", dependsOnMethods = "testAddUserStore")
    public void testAddDuplicateUserStore() throws Exception {

        Property[] properties = (new JDBCUserStoreManager()).getDefaultUserStoreProperties().getMandatoryProperties();
        PropertyDTO[] propertyDTOs = new PropertyDTO[properties.length];
        for (int i = 0; i < properties.length; i++) {
            PropertyDTO propertyDTO = new PropertyDTO();
            propertyDTO.setName(properties[i].getName());
            propertyDTO.setValue(properties[i].getValue());
            propertyDTOs[i] = propertyDTO;
        }
        UserStoreDTO userStoreDTO = userStoreConfigurationClient.createUserStoreDTO(jdbcClass, "lanka.com", propertyDTOs);
        userStoreConfigurationClient.addUserStore(userStoreDTO);

    }

    @Test(groups = "wso2.is", description = "Delete user store", dependsOnMethods = "testAddDuplicateUserStore")
    public void testDeleteUserStore() throws Exception {
        userStoreConfigurationClient.deleteUserStore("lanka.com");
        Assert.assertTrue("Deletion of user store has failed", userStoreConfigUtils.waitForUserStoreUnDeployment
                (userStoreConfigurationClient, "lanka.com"));

    }

    @AfterClass(alwaysRun = true)
    public void atEnd() throws Exception {
    }
}
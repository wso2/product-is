/*
 * Copyright (c) 2017, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
 *
 *  WSO2 Inc. licenses this file to you under the Apache License,
 *  Version 2.0 (the "License"); you may not use this file except
 *  in compliance with the License.
 *  You may obtain a copy of the License at
 *
 *  http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing,
 *  software distributed under the License is distributed on an
 *  "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 *  KIND, either express or implied.  See the License for the
 *  specific language governing permissions and limitations
 *  under the License.
 *
 */

package org.wso2.identity.integration.test.user.store.config;

import junit.framework.Assert;
import org.apache.commons.io.FileUtils;
import org.apache.commons.lang.StringUtils;
import org.testng.annotations.AfterClass;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;
import org.wso2.carbon.identity.user.store.configuration.stub.dto.PropertyDTO;
import org.wso2.carbon.identity.user.store.configuration.stub.dto.UserStoreDTO;
import org.wso2.carbon.user.api.Property;
import org.wso2.carbon.user.core.jdbc.JDBCUserStoreManager;
import org.wso2.identity.integration.common.clients.user.store.config.UserStoreConfigAdminServiceClient;
import org.wso2.identity.integration.common.utils.ISIntegrationTest;
import org.wso2.identity.integration.common.utils.UserStoreConfigUtils;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

public class UserStoreConfigTestForIDENTITY5573 extends ISIntegrationTest {

    private UserStoreConfigAdminServiceClient userStoreConfigurationClient;
    private static final String JDBC_USM_CLASS = "org.wso2.carbon.user.core.jdbc.JDBCUserStoreManager";
    private static final String USER_STORE_NAME = "identity5573";
    private UserStoreConfigUtils userStoreConfigUtils = new UserStoreConfigUtils();

    @BeforeClass(alwaysRun = true)
    public void testInit() throws Exception {

        super.init();
        userStoreConfigurationClient =
                new UserStoreConfigAdminServiceClient(backendURL, sessionCookie);
    }


    @Test(groups = "wso2.is", description = "Check add user store via DTO")
    public void testAddUserStoreWithAdvPropertiesAndSpecialChars() throws Exception {

        Property[] properties = (new JDBCUserStoreManager()).getDefaultUserStoreProperties().getMandatoryProperties();
        List<PropertyDTO> propertyDTOList = new ArrayList<PropertyDTO>();
        for (Property property : properties) {
            PropertyDTO propertyDTO = new PropertyDTO();
            propertyDTO.setName(property.getName());
            propertyDTO.setValue(property.getValue());
            propertyDTOList.add(propertyDTO);
        }
        PropertyDTO dummySqlProperty = new PropertyDTO();
        dummySqlProperty.setName("DummySQLWithSpecialChar");
        dummySqlProperty.setValue(
                "SELECT UM_USER_NAME FROM UM_USER WHERE UM_USER_NAME LIKE ? AND UM_TENANT_ID>? ORDER BY UM_USER_NAME");
        propertyDTOList.add(dummySqlProperty);
        UserStoreDTO userStoreDTO = userStoreConfigurationClient.createUserStoreDTO(JDBC_USM_CLASS, USER_STORE_NAME,
                propertyDTOList.toArray(new PropertyDTO[propertyDTOList.size()]));
        userStoreConfigurationClient.addUserStore(userStoreDTO);
        Assert.assertTrue("Domain addition via DTO has failed.", userStoreConfigUtils.waitForUserStoreDeployment
                (userStoreConfigurationClient, USER_STORE_NAME));
        //Now check whether the config file is properly encoded,
        //Since the carbon home system property is changed when servers are started we use the original carbon home
        // captured by a listener.
        String carbonHome = System.getProperty("original.carbon.home");
        if (StringUtils.isBlank(carbonHome)) {
            carbonHome = System.getProperty("carbon.home");
        }
        String pathToConfig =
                String.format("%s/repository/deployment/server/userstores/%s.xml", carbonHome, USER_STORE_NAME);
        File configFile = new File(pathToConfig);
        Assert.assertTrue(String.format("Config file is not found at %s", pathToConfig),
                waitForUserStoreFileDeployment(configFile));

        String content = FileUtils.readFileToString(configFile);
        Assert.assertFalse("Query not encoded properly, it contains '&amp;gt;'", content.contains("&amp;gt;"));
    }

    @AfterClass(alwaysRun = true)
    public void atEnd() throws Exception {

        userStoreConfigurationClient.deleteUserStore(USER_STORE_NAME);
        Assert.assertTrue("Deletion of user store has failed",
                userStoreConfigUtils.waitForUserStoreUnDeployment(userStoreConfigurationClient, USER_STORE_NAME));
    }


    public boolean waitForUserStoreFileDeployment(File file) throws Exception {

        long waitTime = System.currentTimeMillis() + 30000; //wait for max 30 seconds
        while (System.currentTimeMillis() < waitTime) {
            if (file.exists()) {
                return true;
            }
            Thread.sleep(500);
        }
        log.error("Userstore config file is not deployed at " + file.getAbsolutePath());
        return false;
    }

}

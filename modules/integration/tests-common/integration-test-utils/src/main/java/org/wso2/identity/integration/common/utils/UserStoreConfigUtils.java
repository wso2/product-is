/*
 *  Copyright (c) 2014 WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
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
package org.wso2.identity.integration.common.utils;

import org.wso2.carbon.automation.test.utils.dbutils.H2DataBaseManager;
import org.wso2.carbon.identity.user.store.configuration.stub.dto.PropertyDTO;
import org.wso2.carbon.identity.user.store.configuration.stub.dto.UserStoreDTO;
import org.wso2.carbon.integration.common.utils.mgt.ServerConfigurationManager;
import org.wso2.identity.integration.common.clients.user.store.config.UserStoreConfigAdminServiceClient;

import java.io.File;
import java.io.IOException;
import java.sql.SQLException;

/**
 * The Util class which carries common functionality required by the user store configuration scenarios
 */
public class UserStoreConfigUtils {

    private static PropertyDTO[] propertyDTOs;
    private static final String userStoreDBName = "JDBC_USER_STORE_DB";
    private static final String dbUserName = "wso2automation";
    private static final String dbUserPassword = "wso2automation";

    public boolean waitForUserStoreDeployment(UserStoreConfigAdminServiceClient userStoreConfigAdminServiceClient,
                                              String domain) throws Exception {

        Thread.sleep(5000);
        long waitTime = System.currentTimeMillis() + 30000; //wait for 45 seconds
        while (System.currentTimeMillis() < waitTime) {
            UserStoreDTO[] userStoreDTOs = userStoreConfigAdminServiceClient.getActiveDomains();
            if (userStoreDTOs != null) {
                for (UserStoreDTO userStoreDTO : userStoreDTOs) {
                    if (userStoreDTO != null) {
                        if (userStoreDTO.getDomainId().equalsIgnoreCase(domain)) {
                            return true;
                        }
                    }
                }
            }
            Thread.sleep(500);
        }
        return false;
    }

    public boolean waitForUserStoreUnDeployment(UserStoreConfigAdminServiceClient userStoreConfigAdminServiceClient,
                                                String domain) throws Exception {

        long waitTime = System.currentTimeMillis() + 20000; //wait for 15 seconds
        while (System.currentTimeMillis() < waitTime) {
            UserStoreDTO[] userStoreDTOs = userStoreConfigAdminServiceClient.getActiveDomains();
            userStoreConfigAdminServiceClient.getActiveDomains();
            if (userStoreDTOs != null) {
                for (UserStoreDTO userStoreDTO : userStoreDTOs) {
                    if (userStoreDTO != null && userStoreDTO.getDomainId() != null) {
                        if (userStoreDTO.getDomainId().equalsIgnoreCase(domain)) {
                            Thread.sleep(500);
                        }
                    }
                }
            }
        }
        return true;
    }

    /**
     * Sets properties for the JDBC Userstore.
     *
     * @throws IOException            throws if an error occurs when setting JDBC userstore properties.
     * @throws SQLException           throws if an error occurs when perfomring a db action.
     * @throws ClassNotFoundException throws if it fails to find the relevant java class.
     * @deprecated This method uses a common user store database name. When multiple test cases reuse the same database,
     * there can be transient connection exceptions due to database not properly closed. Thus please use the utility
     * method {@link UserStoreConfigUtils#getJDBCUserStoreProperties(String)} with a test case specific user database
     * name instead.
     */
    public PropertyDTO[] getJDBCUserStoreProperties() throws IOException, SQLException, ClassNotFoundException {

        propertyDTOs = new PropertyDTO[12];
        for (int i = 0; i < 12; i++) {
            propertyDTOs[i] = new PropertyDTO();
        }
        //creating database
        H2DataBaseManager dbmanager = new H2DataBaseManager("jdbc:h2:" + ServerConfigurationManager.getCarbonHome()
                + "/repository/database/" + userStoreDBName,
                dbUserName, dbUserPassword);
        dbmanager.executeUpdate(new File(ServerConfigurationManager.getCarbonHome() + "/dbscripts/h2.sql"));
        dbmanager.disconnect();

        propertyDTOs[0].setName("driverName");
        propertyDTOs[0].setValue("org.h2.Driver");

        propertyDTOs[1].setName("url");
        propertyDTOs[1].setValue("jdbc:h2:./repository/database/" + userStoreDBName);

        propertyDTOs[2].setName("userName");
        propertyDTOs[2].setValue(dbUserName);

        propertyDTOs[3].setName("password");
        propertyDTOs[3].setValue(dbUserPassword);

        propertyDTOs[4].setName("PasswordJavaRegEx");
        propertyDTOs[4].setValue("^[\\S]{5,30}$");

        propertyDTOs[5].setName("UsernameJavaRegEx");
        propertyDTOs[5].setValue("^[\\S]{5,30}$");

        propertyDTOs[6].setName("Disabled");
        propertyDTOs[6].setValue("false");

        propertyDTOs[7].setName("PasswordDigest");
        propertyDTOs[7].setValue("SHA-256");

        propertyDTOs[8].setName("StoreSaltedPassword");
        propertyDTOs[8].setValue("true");

        propertyDTOs[9].setName("SCIMEnabled");
        propertyDTOs[9].setValue("true");

        propertyDTOs[9].setName("CountRetrieverClass");
        propertyDTOs[9].setValue("org.wso2.carbon.identity.user.store.count.jdbc.JDBCUserStoreCountRetriever");

        propertyDTOs[10].setName("UserIDEnabled");
        propertyDTOs[10].setValue("true");

        propertyDTOs[11].setName("GroupIDEnabled");
        propertyDTOs[11].setValue("true");
        return propertyDTOs;
    }

    /**
     * Creates an H2 database and gets its properties.
     *
     * @param userStoreDBName The test case specific user database name.
     * @throws IOException            throws if an error occurs when setting JDBC userstore properties.
     * @throws SQLException           throws if an error occurs when perfomring a db action.
     * @throws ClassNotFoundException throws if it fails to find the relevant java class.
     */
    public PropertyDTO[] getJDBCUserStoreProperties(final String userStoreDBName)
            throws IOException, SQLException, ClassNotFoundException {

        propertyDTOs = new PropertyDTO[12];
        for (int i = 0; i < 12; i++) {
            propertyDTOs[i] = new PropertyDTO();
        }
        //creating database
        H2DataBaseManager dbmanager = new H2DataBaseManager("jdbc:h2:" + ServerConfigurationManager.getCarbonHome()
                + "/repository/database/" + userStoreDBName,
                dbUserName, dbUserPassword);
        dbmanager.executeUpdate(new File(ServerConfigurationManager.getCarbonHome() + "/dbscripts/h2.sql"));
        dbmanager.disconnect();

        propertyDTOs[0].setName("driverName");
        propertyDTOs[0].setValue("org.h2.Driver");

        propertyDTOs[1].setName("url");
        propertyDTOs[1].setValue("jdbc:h2:./repository/database/" + userStoreDBName);

        propertyDTOs[2].setName("userName");
        propertyDTOs[2].setValue(dbUserName);

        propertyDTOs[3].setName("password");
        propertyDTOs[3].setValue(dbUserPassword);

        propertyDTOs[4].setName("PasswordJavaRegEx");
        propertyDTOs[4].setValue("^[\\S]{5,30}$");

        propertyDTOs[5].setName("UsernameJavaRegEx");
        propertyDTOs[5].setValue("^[\\S]{5,30}$");

        propertyDTOs[6].setName("Disabled");
        propertyDTOs[6].setValue("false");

        propertyDTOs[7].setName("PasswordDigest");
        propertyDTOs[7].setValue("SHA-256");

        propertyDTOs[8].setName("StoreSaltedPassword");
        propertyDTOs[8].setValue("true");

        propertyDTOs[9].setName("SCIMEnabled");
        propertyDTOs[9].setValue("true");

        propertyDTOs[9].setName("CountRetrieverClass");
        propertyDTOs[9].setValue("org.wso2.carbon.identity.user.store.count.jdbc.JDBCUserStoreCountRetriever");

        propertyDTOs[10].setName("UserIDEnabled");
        propertyDTOs[10].setValue("true");

        propertyDTOs[11].setName("GroupIDEnabled");
        propertyDTOs[11].setValue("true");
        return propertyDTOs;
    }
}

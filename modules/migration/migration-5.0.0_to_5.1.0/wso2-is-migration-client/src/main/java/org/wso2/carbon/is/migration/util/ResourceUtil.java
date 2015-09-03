/*
* Copyright (c) 2015, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
*
* Licensed under the Apache License, Version 2.0 (the "License");
* you may not use this file except in compliance with the License.
* You may obtain a copy of the License at
*
* http://www.apache.org/licenses/LICENSE-2.0
*
* Unless required by applicable law or agreed to in writing, software
* distributed under the License is distributed on an "AS IS" BASIS,
* WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
* See the License for the specific language governing permissions and
* limitations under the License.
*/
package org.wso2.carbon.is.migration.util;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.wso2.carbon.identity.core.persistence.JDBCPersistenceManager;
import org.wso2.carbon.is.migration.ISMigrationException;
import org.wso2.carbon.utils.CarbonUtils;
import org.wso2.carbon.utils.dbcreator.DatabaseCreator;

import java.io.IOException;
import java.sql.SQLException;

public class ResourceUtil {

    private static final Log log = LogFactory.getLog(ResourceUtil.class);

    /**
     * This method picks the query according to the users database
     *
     * @param migrateVersion migrate version
     * @return exact query to execute
     * @throws SQLException
     * @throws ISMigrationException
     * @throws IOException
     */
    public static String pickQueryFromResources(String migrateVersion) throws SQLException, ISMigrationException,
            IOException {

        String queryTobeExecuted;
        try {
            String databaseType = DatabaseCreator.getDatabaseType(JDBCPersistenceManager.getInstance().getDBConnection());

            String resourcePath;

            if (migrateVersion.equalsIgnoreCase(Constants.VERSION_5_1_0)) {
                resourcePath = CarbonUtils.getCarbonHome() + "/dbscripts/migration-5.0.0_to_5.1.0/";
            } else {
                throw new ISMigrationException("No query picked up for the given migrate version. Please check the migrate version.");
            }
            queryTobeExecuted = resourcePath +  databaseType + ".sql";
                //queryTobeExecuted = IOUtils.toString(new FileInputStream(new File(resourcePath + databaseType + ".sql")), "UTF-8");


        } catch (IOException e) {
            throw new ISMigrationException("Error occurred while accessing the sql from resources. " + e);
        } catch (Exception e) {
            throw new ISMigrationException("Error occurred while accessing the sql from resources. " + e);
        }

        return queryTobeExecuted;
    }

    /**
     * To handle exceptions
     *
     * @param msg error message
     * @throws ISMigrationException
     */
    public static void handleException(String msg, Throwable e) throws ISMigrationException {
        log.error(msg, e);
        throw new ISMigrationException(msg, e);
    }

}

/*
* Copyright (c) 2017, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
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
package org.wso2.carbon.is.migration.service.v530.util;

import org.wso2.carbon.identity.core.util.IdentityDatabaseUtil;
import org.wso2.carbon.is.migration.internal.ISMigrationServiceDataHolder;
import org.wso2.carbon.utils.dbcreator.DatabaseCreator;

import java.sql.Connection;
import java.sql.DatabaseMetaData;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import javax.sql.DataSource;

public class ResourceUtil {

    public static String setMySQLDBName(Connection conn) throws SQLException {

        try (PreparedStatement ps = conn.prepareStatement("SELECT DATABASE() FROM DUAL;")) {
            try (ResultSet rs = ps.executeQuery();) {
                String name = null;
                if (rs.next()) {
                    name = rs.getString(1);
                    try (PreparedStatement ps1 = conn.prepareStatement("SET @databasename = ?;")) {
                        ps1.setString(1, name);
                        ps1.execute();
                    }
                }
                return name;
            }
        }
    }


    public static boolean isSchemaMigrated(DataSource dataSource) throws Exception {

        Connection conn = null;
        try {
            conn = dataSource.getConnection();
            String databaseType = DatabaseCreator.getDatabaseType(conn);
            DatabaseMetaData meta = conn.getMetaData();
            String schema = null;
            if ("oracle".equals(databaseType)) {
                schema = ISMigrationServiceDataHolder.getIdentityOracleUser();
            }
            ResultSet res = meta.getTables(null, schema, "IDN_CLAIM", new String[]{"TABLE"});
            boolean schemaMigrated = false;
            if (res.next()) {
                schemaMigrated = true;
            }
            return schemaMigrated;
        } finally {
            IdentityDatabaseUtil.closeConnection(conn);
        }
    }

}

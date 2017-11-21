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

import org.wso2.carbon.identity.core.util.IdentityDatabaseUtil;
import org.wso2.carbon.is.migration.client.internal.ISMigrationServiceDataHolder;
import org.wso2.carbon.utils.dbcreator.DatabaseCreator;

import javax.sql.DataSource;
import java.sql.Connection;
import java.sql.DatabaseMetaData;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

public class ResourceUtil {

    public static String setMySQLDBName(Connection conn) throws SQLException {
        PreparedStatement ps = conn.prepareStatement("SELECT DATABASE() FROM DUAL;");
        ResultSet rs = ps.executeQuery();
        String name = null;
        if(rs.next()){
            name = rs.getString(1);
            ps = conn.prepareStatement("SET @databasename = ?;");
            ps.setString(1, name);
            ps.execute();
        }
        return name;
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
            ResultSet res = meta.getTables(null, schema, "WF_WORKFLOW_REQUEST_RELATION", new String[]{"TABLE"});
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

/*
* Copyright (c) 2016, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
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
import org.wso2.carbon.identity.core.util.IdentityDatabaseUtil;
import org.wso2.carbon.identity.core.util.IdentityUtil;
import org.wso2.carbon.is.migration.client.internal.ISMigrationServiceDataHolder;
import org.wso2.carbon.user.api.Tenant;
import org.wso2.carbon.user.api.UserStoreException;
import org.wso2.carbon.utils.dbcreator.DatabaseCreator;

import java.sql.Connection;
import java.sql.DatabaseMetaData;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import javax.sql.DataSource;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;

public class ResourceUtil {

    private static final Log log = LogFactory.getLog(ResourceUtil.class);

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
            ResultSet res = meta.getTables(null, schema, "IDN_OAUTH2_SCOPE_BINDING", new String[]{"TABLE"});
            boolean schemaMigrated = false;
            if (res.next()) {
                schemaMigrated = true;
            }
            return schemaMigrated;
        } finally {
            IdentityDatabaseUtil.closeConnection(conn);
        }
    }

    /**
     * * This method provides a secured document builder which will secure XXE attacks.
     *
     * @return DocumentBuilder
     * @throws javax.xml.parsers.ParserConfigurationException
     */
    public static DocumentBuilder getSecuredDocumentBuilder() {
        DocumentBuilderFactory documentBuilderFactory = IdentityUtil.getSecuredDocumentBuilderFactory();
        DocumentBuilder documentBuilder = null;
        try {
            documentBuilder = documentBuilderFactory.newDocumentBuilder();
        } catch (ParserConfigurationException e) {
            log.error("Error while getting document builder.", e);
        }
        return documentBuilder;
    }

    public static List<Tenant> getTenants() {
        List<Tenant> tenantList = new ArrayList<>();
        try {
            Tenant[] tenants = ISMigrationServiceDataHolder.getRealmService().getTenantManager().getAllTenants();
            tenantList = Arrays.asList(tenants);

        } catch (UserStoreException e) {
            log.error("Error occurred while reading tenant list.");

        }
        return tenantList;
    }

}

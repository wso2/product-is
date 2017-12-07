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
package org.wso2.carbon.is.migration.util;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.wso2.carbon.identity.core.migrate.MigrationClientException;
import org.wso2.carbon.identity.core.util.IdentityUtil;
import org.wso2.carbon.is.migration.config.Config;
import org.wso2.carbon.is.migration.internal.ISMigrationServiceDataHolder;
import org.wso2.carbon.user.api.Tenant;
import org.wso2.carbon.user.api.UserStoreException;
import org.yaml.snakeyaml.Yaml;
import org.yaml.snakeyaml.introspector.BeanAccess;

import java.io.File;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.Reader;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;

/**
 * Util class.
 */
public class Utility {

    private static Log log = LogFactory.getLog(Utility.class);

    public static String getMigrationResourceDirectoryPath() {

        Path path = Paths.get(System.getProperty(Constant.CARBON_HOME), Constant.MIGRATION_RESOURCE_HOME);
        return path.toString();
    }

    public static String getDataFilePath(String dataFileName, String version) {

        Path path = Paths.get(getMigrationResourceDirectoryPath(), version, Constant.MIGRATION_RESOURCE_DATA_FILES,
                dataFileName);
        return path.toString();
    }

    public static String getSchemaPath(String schema, String databaseType, String location, String version) {

        Path path = Paths.get(getMigrationResourceDirectoryPath(), version, Constant.MIGRATION_RESOURCE_DBSCRIPTS,
                location, schema, databaseType + ".sql");
        return path.toString();
    }

    public static Config loadMigrationConfig(String configFilePath) throws MigrationClientException {
        Config config = null ;
        Path path = Paths.get(configFilePath);
        if (Files.exists(path)) {
            try {
                Reader in = new InputStreamReader(Files.newInputStream(path), StandardCharsets.UTF_8);
                Yaml yaml = new Yaml();
                yaml.setBeanAccess(BeanAccess.FIELD);
                config = yaml.loadAs(in, Config.class);
                if (config == null) {
                    throw new MigrationClientException("Provider is not loaded correctly.");
                }
            } catch (IOException e) {
                String errorMessage = "Error occurred while loading the " + Config.class
                                      + " yaml file, " +
                                      e.getMessage();
                log.error(errorMessage, e);
                throw new MigrationClientException(errorMessage, e);
            }
        }
        return config;
    }

    public static List<Integer> getInactiveTenants() {
        List<Integer> inactiveTenants = new ArrayList<>();
        try {
            Tenant[] tenants = ISMigrationServiceDataHolder.getRealmService().getTenantManager().getAllTenants();
            for (Tenant tenant : tenants) {
                if (!tenant.isActive()) {
                    inactiveTenants.add(tenant.getId());
                }
            }
        } catch (UserStoreException e) {
            log.error("Error while getting inactive tenant details. Assuming zero inactive tenants.");
            return new ArrayList<>();
        }

        return inactiveTenants;
    }

    public static String setMySQLDBName(Connection conn) throws SQLException {

        PreparedStatement ps = conn.prepareStatement("SELECT DATABASE() FROM DUAL;");
        ResultSet rs = ps.executeQuery();
        String name = null;
        if (rs.next()) {
            name = rs.getString(1);
            ps = conn.prepareStatement("SET @databasename = ?;");
            ps.setString(1, name);
            ps.execute();
        }
        return name;
    }

    /**
     * * This method provides a secured document builder which will secure XXE attacks.
     *
     * @return DocumentBuilder
     * @throws ParserConfigurationException
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

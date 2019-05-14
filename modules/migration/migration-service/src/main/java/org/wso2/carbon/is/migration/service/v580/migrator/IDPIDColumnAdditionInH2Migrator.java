/*
 * Copyright (c) 2019, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
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

package org.wso2.carbon.is.migration.service.v580.migrator;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.wso2.carbon.identity.core.migrate.MigrationClientException;
import org.wso2.carbon.is.migration.service.Migrator;
import org.wso2.carbon.is.migration.service.v580.dao.IDPIDColumnAdditionInH2DAO;

import java.sql.Connection;
import java.sql.SQLException;
import java.util.Arrays;
import java.util.List;

import static org.wso2.carbon.is.migration.util.SchemaUtil.isColumnExist;

/**
 * Schema migration to add IDP_ID column in H2 if not exist.
 */
public class IDPIDColumnAdditionInH2Migrator extends Migrator {

    private static final Log log = LogFactory.getLog(IDPIDColumnAdditionInH2Migrator.class);

    private static final String COLUMN_NAME_IDP_ID = "IDP_ID";
    private static final String UNIQUE_CONSTRAINT_NAME_CON_APP_KEY = "IDP_ID";

    private static final String TABLE_NAME_IDN_OAUTH2_ACCESS_TOKEN = "IDN_OAUTH2_ACCESS_TOKEN";

    private static final List<String> TABLE_NAMES = Arrays.asList("IDN_OAUTH2_AUTHORIZATION_CODE",
            "IDN_OAUTH2_ACCESS_TOKEN", "IDN_OAUTH2_ACCESS_TOKEN_AUDIT");

    @Override
    public void migrate() throws MigrationClientException {

        try (Connection connection = getDataSource().getConnection()) {
            // All the other database types are handled with stored procedures, except H2.
            if (connection.getMetaData().getDriverName().contains("H2")) {
                for (String eachTable : TABLE_NAMES) {
                    if (!isColumnExist(connection, COLUMN_NAME_IDP_ID, eachTable)) {
                        log.info(COLUMN_NAME_IDP_ID + " column does not exist in the table " +
                                eachTable + ". Hence adding the column.");
                        addIdpColumn(connection, eachTable);
                    } else {
                        log.info(COLUMN_NAME_IDP_ID + " column already exist in the table " +
                                eachTable + ". Hence skipping.");
                    }
                }
                updateConAppKey(connection);
            } else {
                log.info("IDP ID column is added with schema migrators for this environment, thus skipping.");
            }
        } catch (SQLException e) {
            log.error("Error while obtaining connection from identity data source.", e);
        }
    }

    private void addIdpColumn(Connection connection, String eachTable) throws SQLException {

        connection.setAutoCommit(false);
        IDPIDColumnAdditionInH2DAO.getInstance().addIdpIdColumn(connection, eachTable, COLUMN_NAME_IDP_ID);
        connection.commit();
    }

    private void updateConAppKey(Connection connection) throws SQLException {

        connection.setAutoCommit(false);
        IDPIDColumnAdditionInH2DAO.getInstance().updateUniqueConstraint(connection);
        connection.commit();
    }
}

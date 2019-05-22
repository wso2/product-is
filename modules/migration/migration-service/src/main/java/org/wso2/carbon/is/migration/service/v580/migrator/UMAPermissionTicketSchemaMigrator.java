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
import org.wso2.carbon.identity.core.util.IdentityDatabaseUtil;
import org.wso2.carbon.is.migration.service.SchemaMigrator;

import java.sql.Connection;
import java.sql.SQLException;

import static org.wso2.carbon.is.migration.util.SchemaUtil.isColumnExist;

/**
 * Schema migration of IDN_UMA_PERMISSION_TICKET table to add TOKEN_ID column if not exist.
 */
public class UMAPermissionTicketSchemaMigrator extends SchemaMigrator {

    private static final Log log = LogFactory.getLog(UMAPermissionTicketSchemaMigrator.class);
    private static final String COLUMN_NAME_TOKEN_ID = "TOKEN_ID";
    private static final String TABLE_NAME_IDN_UMA_PERMISSION_TICKET = "IDN_UMA_PERMISSION_TICKET";

    @Override
    public void migrate() throws MigrationClientException {

        try (Connection connection = IdentityDatabaseUtil.getDBConnection()) {
            if (!isColumnExist(connection, COLUMN_NAME_TOKEN_ID, TABLE_NAME_IDN_UMA_PERMISSION_TICKET)) {

                log.info(COLUMN_NAME_TOKEN_ID + " column does not exist in the table " +
                         TABLE_NAME_IDN_UMA_PERMISSION_TICKET + ". Hence adding the column.");
                super.migrate();
            } else {
                log.info(COLUMN_NAME_TOKEN_ID + " column already exist in the table " +
                         TABLE_NAME_IDN_UMA_PERMISSION_TICKET + ". Hence skipping.");
            }
        } catch (SQLException e) {
            log.error("Error while obtaining connection from identity data source.", e);
        }
    }
}

/*
 * Copyright (c) 2019, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
 *
 * WSO2 Inc. licenses this file to you under the Apache License,
 * Version 2.0 (the "License"); you may not use this file except
 * in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied. See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */
package org.wso2.carbon.is.migration.service.v580.migrator;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.wso2.carbon.identity.core.migrate.MigrationClientException;
import org.wso2.carbon.is.migration.service.Migrator;
import org.wso2.carbon.is.migration.service.v580.dao.OAuthDAO;
import org.wso2.carbon.is.migration.util.Constant;

import java.sql.Connection;
import java.sql.SQLException;

public class OAuthDataMigrator extends Migrator {

    private static final Log log = LogFactory.getLog(OAuthDataMigrator.class);

    @Override
    public void migrate() throws MigrationClientException {

        migrateTokensOfLocalUsers();
        migrateAuthCodesOfLocalUsers();
    }

    private void migrateTokensOfLocalUsers() throws MigrationClientException {

        log.info(Constant.MIGRATION_LOG + "Migration starting on OAuth2 access token table");

        try (Connection connection = getDataSource().getConnection()) {
            connection.setAutoCommit(false);
            OAuthDAO.getInstance().updateTokensOfLocalUsers(connection);
            connection.commit();
        } catch (SQLException e) {
            String error = "SQL error while updating tokens of local users";
            throw new MigrationClientException(error, e);
        }
    }

    private void migrateAuthCodesOfLocalUsers() throws MigrationClientException {

        log.info(Constant.MIGRATION_LOG + "Migration starting on Authorization code table");

        try (Connection connection = getDataSource().getConnection()) {
            connection.setAutoCommit(false);
            OAuthDAO.getInstance().updateAuthCodesOfLocalUsers(connection);
            connection.commit();
        } catch (SQLException e) {
            String error = "SQL error while updating authorization codes of local users";
            throw new MigrationClientException(error, e);
        }
    }
}

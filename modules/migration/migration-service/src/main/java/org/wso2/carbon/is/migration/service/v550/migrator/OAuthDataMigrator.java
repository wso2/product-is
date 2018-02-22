/*
* Copyright (c) 2018, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
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
package org.wso2.carbon.is.migration.service.v550.migrator;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.wso2.carbon.identity.core.migrate.MigrationClientException;
import org.wso2.carbon.is.migration.service.Migrator;
import org.wso2.carbon.is.migration.service.v550.dao.AuthzCodeDAO;
import org.wso2.carbon.is.migration.service.v550.dao.TokenDAO;

import java.sql.Connection;
import java.sql.SQLException;

public class OAuthDataMigrator extends Migrator{

    private static final Log log = LogFactory
            .getLog(org.wso2.carbon.is.migration.service.v550.migrator.OAuthDataMigrator.class);
    @Override
    public void migrate() throws MigrationClientException {
        try {
            addHashColumns();
        } catch (SQLException e) {
            throw new MigrationClientException("Error while adding hash columns",e);
        }
    }


    public void addHashColumns() throws MigrationClientException, SQLException {

        boolean isTokenHashColumnsAvailable;
        boolean isAuthzCodeHashColumnAvailable;
        try (Connection connection = getDataSource().getConnection()) {
            isTokenHashColumnsAvailable = TokenDAO.getInstance().isTokenHashColumnsAvailable(connection);
            isAuthzCodeHashColumnAvailable = AuthzCodeDAO.getInstance().isAuthzCodeHashColumnAvailable(connection);
        }
        if(!isTokenHashColumnsAvailable){
            try (Connection connection = getDataSource().getConnection()) {
                TokenDAO.getInstance().addTokenHashColumns(connection);
            }
        }
    }
}

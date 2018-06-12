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
package org.wso2.carbon.is.migration.service.v560.migrator;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.wso2.carbon.identity.core.migrate.MigrationClientException;
import org.wso2.carbon.identity.core.util.IdentityUtil;
import org.wso2.carbon.is.migration.service.Migrator;
import org.wso2.carbon.is.migration.service.v560.dao.SessionDAO;

import java.sql.Connection;
import java.sql.SQLException;

/**
 * This class handles the session data migration.
 */
public class SessionDataMigrator extends Migrator {

    private static final Log log = LogFactory.getLog(SessionDataMigrator.class);

    @Override
    public void migrate() throws MigrationClientException {

        // Get session clean up time out
        Long cleanUpTimeOut = IdentityUtil.getCleanUpTimeout();

        try (Connection connection = getDataSource().getConnection()) {
            SessionDAO.getInstance().updateSessionExpireTime(cleanUpTimeOut, connection);
        } catch (SQLException e) {
            log.error("Error while updating session expiry times. ", e);
            if (!isContinueOnError()) {
                throw new MigrationClientException("Error while updating session expiry times. ", e);
            }
        }
    }
}

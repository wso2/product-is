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
package org.wso2.carbon.is.migration.service.v560.dao;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import static org.wso2.carbon.is.migration.service.v560.util.SQLConstants.UPDATE_IDN_AUTH_SESSION_STORE;

/**
 * This class handles DAO methods for session data migration.
 */
public class SessionDAO {

    private static SessionDAO instance = new SessionDAO();

    private SessionDAO() {

    }

    public static SessionDAO getInstance() {

        return instance;
    }

    public void updateSessionExpireTime(Long cleanUpTimeOut, Connection connection) throws SQLException {

        try (PreparedStatement preparedStatement = connection.prepareStatement(UPDATE_IDN_AUTH_SESSION_STORE)) {
            preparedStatement.setLong(1, cleanUpTimeOut);
            preparedStatement.executeUpdate();
        }
    }
}

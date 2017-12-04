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
package org.wso2.carbon.is.migration.service.v540.dao;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.wso2.carbon.identity.core.migrate.MigrationClientException;
import org.wso2.carbon.identity.core.util.IdentityDatabaseUtil;
import org.wso2.carbon.is.migration.service.v540.SQLConstants;
import org.wso2.carbon.is.migration.service.v540.bean.OAuth2ScopeBinding;
import org.wso2.carbon.is.migration.util.Constant;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.util.List;

/**
 * DAO class for oauth2 scope binding
 */
public class IDNOAuth2ScopeBindingDAO {

    private static Log log = LogFactory.getLog(IDNOAuth2ScopeBindingDAO.class);

    private static IDNOAuth2ScopeBindingDAO idnoAuth2ScopeBindingDAO = new IDNOAuth2ScopeBindingDAO();

    private IDNOAuth2ScopeBindingDAO() {
    }

    public static IDNOAuth2ScopeBindingDAO getInstance() {
        return idnoAuth2ScopeBindingDAO;
    }

    /**
     * Add Scope binding to database.
     *
     * @param oAuth2ScopeBindingList
     * @param continueOnError
     * @throws MigrationClientException
     */
    public void addOAuth2ScopeBinding(List<OAuth2ScopeBinding> oAuth2ScopeBindingList, boolean continueOnError)
            throws MigrationClientException {

        log.info(Constant.MIGRATION_LOG + " call addOAuth2ScopeBinding.");
        Connection connection = IdentityDatabaseUtil.getDBConnection();
        PreparedStatement prepStmt = null;

        String query = SQLConstants.ADD_SCOPE_BINDINGS;

        try {
            prepStmt = connection.prepareStatement(query);

            for (OAuth2ScopeBinding oAuth2ScopeBinding : oAuth2ScopeBindingList) {
                prepStmt.setString(1, oAuth2ScopeBinding.getScopeId());
                prepStmt.setString(2, oAuth2ScopeBinding.getScopeBinding());
                prepStmt.addBatch();
            }

            prepStmt.executeBatch();
            connection.commit();
            log.info(Constant.MIGRATION_LOG + "");
        } catch (Exception e) {
            log.error(e);
            if (!continueOnError) {
                throw new MigrationClientException("Error while adding OAuth2ScopeBinding, ", e);
            }
        } finally {
            IdentityDatabaseUtil.closeStatement(prepStmt);
            IdentityDatabaseUtil.closeConnection(connection);
        }
    }
}

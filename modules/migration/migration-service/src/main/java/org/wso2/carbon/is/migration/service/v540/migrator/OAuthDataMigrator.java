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
package org.wso2.carbon.is.migration.service.v540.migrator;

import org.apache.commons.lang.StringUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.wso2.carbon.identity.core.migrate.MigrationClientException;
import org.wso2.carbon.is.migration.service.Migrator;
import org.wso2.carbon.is.migration.service.v540.bean.OAuth2Scope;
import org.wso2.carbon.is.migration.service.v540.bean.OAuth2ScopeBinding;
import org.wso2.carbon.is.migration.service.v540.dao.IDNOAuth2ScopeBindingDAO;
import org.wso2.carbon.is.migration.service.v540.dao.IDNOAuth2ScopeDAO;
import org.wso2.carbon.is.migration.util.Constant;

import java.sql.Connection;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

/**
 * Migrator implementation for OAuth Data
 */
public class OAuthDataMigrator extends Migrator {

    private static final Log log = LogFactory.getLog(OAuthDataMigrator.class);

    @Override
    public void migrate() throws MigrationClientException {
        try {
            migrateOAuth2ScopeData();
        } catch (Exception e) {
            log.error("Error while migrating OAuth2 scope data.", e);
        }
    }
    public void migrateOAuth2ScopeData() throws Exception {

        log.info(Constant.MIGRATION_LOG + "Migration starting OAuth2 Scope Bindings to the new table.");
        Connection conn = getDataSource().getConnection();
        try {
            List<OAuth2ScopeBinding> oAuth2ScopeBindingList = new ArrayList<>();

            IDNOAuth2ScopeDAO idnoAuth2ScopeDAO = IDNOAuth2ScopeDAO.getInstance();

            List<OAuth2Scope> oAuth2ScopeRoles = idnoAuth2ScopeDAO.getOAuth2ScopeRoles(conn);

            if (!oAuth2ScopeRoles.isEmpty()) {
                for (OAuth2Scope oAuth2ScopeRole : oAuth2ScopeRoles) {
                    String roleString = oAuth2ScopeRole.getRoleString();
                    if (StringUtils.isNotBlank(roleString)) {
                        String[] roleStringArray = roleString.split(",");
                        for (String role : roleStringArray) {
                            oAuth2ScopeBindingList.add(new OAuth2ScopeBinding(oAuth2ScopeRole.getScopeId(), role));
                        }
                    }
                }
                IDNOAuth2ScopeBindingDAO idnoAuth2ScopeBindingDAO = IDNOAuth2ScopeBindingDAO.getInstance();
                idnoAuth2ScopeBindingDAO.addOAuth2ScopeBinding(oAuth2ScopeBindingList, isContinueOnError());

                idnoAuth2ScopeDAO.updateOAuth2ScopeBinding(oAuth2ScopeRoles);
                log.info(Constant.MIGRATION_LOG + "OAuth2 Scope Bindings Successfully migrated.");
            } else {
                log.info(Constant.MIGRATION_LOG + "No Oauth2 Scopes found.");
            }
        } catch (Exception e) {
            log.error(e);
            if (!isContinueOnError()) {
                throw e;
            }
        } finally{
            try {
                if (conn != null) {
                    conn.close();
                }
            } catch (SQLException e) {
                log.error("Failed to close database connection.", e);
            }
        }
    }

}

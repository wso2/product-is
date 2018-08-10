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

package org.wso2.carbon.is.migration.service.v540.migrator;

import org.apache.commons.lang.StringUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.wso2.carbon.identity.core.migrate.MigrationClientException;
import org.wso2.carbon.is.migration.service.Migrator;
import org.wso2.carbon.is.migration.service.v540.SQLConstants;
import org.wso2.carbon.is.migration.service.v540.bean.Permission;
import org.wso2.carbon.is.migration.service.v540.bean.RolePermission;
import org.wso2.carbon.is.migration.service.v540.bean.UserPermission;
import org.wso2.carbon.is.migration.util.Constant;
import org.wso2.carbon.is.migration.util.Utility;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class PermissionMigrator extends Migrator {

    private static final Log log = LogFactory.getLog(PermissionMigrator.class);

    @Override
    public void migrate() throws MigrationClientException {

        try (Connection connection = getDataSource().getConnection()) {

            connection.setAutoCommit(false);

            List<Permission> duplicatedPermissions = getDuplicatedPermissions(connection);
            if (duplicatedPermissions.isEmpty()) {
                log.info(Constant.MIGRATION_LOG + " Permission migration is not required.");
                return;
            }

            log.info(Constant.MIGRATION_LOG + " Found " + duplicatedPermissions.size() + " duplicated permissions.");

            List<RolePermission> duplicatedRolePermissions = getDuplicatedRolePermissions(connection,
                    duplicatedPermissions);
            if (!duplicatedRolePermissions.isEmpty()) {
                log.info(Constant.MIGRATION_LOG + " Found " + duplicatedPermissions.size() + " duplicated role "
                        + "permissions.");
                deleteDuplicatedRolePermissions(connection, duplicatedRolePermissions);
                log.info(Constant.MIGRATION_LOG + " Removed duplicated role permissions.");
            }
            updateRolePermissionTable(connection, duplicatedPermissions);

            List<UserPermission> duplicatedUserPermissions = getDuplicatedUserPermissions(connection,
                    duplicatedPermissions);
            if (!duplicatedUserPermissions.isEmpty()) {
                log.info(Constant.MIGRATION_LOG + " Found " + duplicatedUserPermissions.size() + " duplicated user "
                        + "permissions.");
                deleteDuplicatedUserPermissions(connection, duplicatedUserPermissions);
                log.info(Constant.MIGRATION_LOG + " Removed duplicated user permissions.");
            }
            updateUserPermissionTable(connection, duplicatedPermissions);

            deleteDuplicatedPermissions(connection, duplicatedPermissions);
            connection.commit();
            log.info(Constant.MIGRATION_LOG + " Permission migration is successful.");
        } catch (SQLException e) {
            throw new MigrationClientException("Failed to migrate permissions.", e);
        }
    }

    private List<Permission> getDuplicatedPermissions(Connection connection)
            throws MigrationClientException, SQLException {

        List<Permission> allPermissions = getAllPermissions(connection);
        List<Permission> uniquePermissions = new ArrayList<>();
        List<Permission> duplicatedPermissions = new ArrayList<>();

        for (Permission permission : allPermissions) {
            if (uniquePermissions.contains(permission)) {
                permission.setUniqueId(uniquePermissions.get(uniquePermissions.indexOf(permission)).getId());
                duplicatedPermissions.add(permission);
            } else {
                uniquePermissions.add(permission);
            }
        }

        return duplicatedPermissions;
    }

    public List<RolePermission> getDuplicatedRolePermissions(Connection connection,
                                                             List<Permission> duplicatedPermissions) throws MigrationClientException, SQLException {

        List<RolePermission> allRolePermissions = getAllRolePermissions(connection);
        List<RolePermission> uniqueRolePermissions = new ArrayList<>();
        List<RolePermission> duplicatedRolePermissions = new ArrayList<>();

        Map<Integer, Permission> duplicatedPermissionMap = new HashMap<>();
        for (Permission permission : duplicatedPermissions) {
            duplicatedPermissionMap.put(permission.getId(), permission);
        }

        for (RolePermission rolePermission : allRolePermissions) {
            if (duplicatedPermissionMap.containsKey(rolePermission.getPermissionId())) {
                rolePermission
                        .setPermissionId(duplicatedPermissionMap.get(rolePermission.getPermissionId()).getUniqueId());
            }
        }

        for (RolePermission rolePermission : allRolePermissions) {
            if (uniqueRolePermissions.contains(rolePermission)) {
                duplicatedRolePermissions.add(rolePermission);
            } else {
                uniqueRolePermissions.add(rolePermission);
            }
        }

        return duplicatedRolePermissions;
    }

    private List<UserPermission> getDuplicatedUserPermissions(Connection connection,
                                                              List<Permission> duplicatedPermissions) throws MigrationClientException, SQLException {

        List<UserPermission> allUserPermissions = getAllUserPermissions(connection);
        List<UserPermission> uniqueUserPermissions = new ArrayList<>();
        List<UserPermission> duplicatedUserPermissions = new ArrayList<>();

        Map<Integer, Permission> duplicatedPermissionMap = new HashMap<>();
        for (Permission permission : duplicatedPermissions) {
            duplicatedPermissionMap.put(permission.getId(), permission);
        }

        for (UserPermission userPermission : allUserPermissions) {
            if (duplicatedPermissionMap.containsKey(userPermission.getPermissionId())) {
                userPermission
                        .setPermissionId(duplicatedPermissionMap.get(userPermission.getPermissionId()).getUniqueId());
            }
        }

        for (UserPermission userPermission : allUserPermissions) {
            if (uniqueUserPermissions.contains(userPermission)) {
                duplicatedUserPermissions.add(userPermission);
            } else {
                uniqueUserPermissions.add(userPermission);
            }
        }

        return duplicatedUserPermissions;
    }

    private List<Permission> getAllPermissions(Connection connection) throws MigrationClientException, SQLException {

        List<Permission> allPermissions = new ArrayList<>();

        try (PreparedStatement statement = connection.prepareStatement(getPermissionSelectQuery());
             ResultSet resultSet = statement.executeQuery()) {
            while (resultSet.next()) {
                Permission permission = new Permission(resultSet.getInt("UM_ID"), resultSet.getString("UM_RESOURCE_ID"),
                        resultSet.getString("UM_ACTION"), resultSet.getInt("UM_TENANT_ID"));
                allPermissions.add(permission);
            }
        } catch (SQLException e) {
            connection.rollback();
            throw new MigrationClientException("Failed to retrieve all permissions.", e);
        }

        return allPermissions;
    }

    private List<RolePermission> getAllRolePermissions(Connection connection)
            throws MigrationClientException, SQLException {

        List<RolePermission> allRolePermissions = new ArrayList<>();

        try (PreparedStatement statement = connection.prepareStatement(getRolePermissionSelectQuery());
             ResultSet resultSet = statement.executeQuery()) {
            while (resultSet.next()) {
                RolePermission rolePermission = new RolePermission(resultSet.getInt("UM_ID"),
                        resultSet.getInt("UM_PERMISSION_ID"), resultSet.getString("UM_ROLE_NAME"),
                        resultSet.getInt("UM_TENANT_ID"), resultSet.getInt("UM_DOMAIN_ID"));
                allRolePermissions.add(rolePermission);
            }
        } catch (SQLException e) {
            connection.rollback();
            throw new MigrationClientException("Failed to retrieve all role permissions.", e);
        }

        return allRolePermissions;
    }

    private List<UserPermission> getAllUserPermissions(Connection connection)
            throws MigrationClientException, SQLException {

        List<UserPermission> allUserPermissions = new ArrayList<>();

        try (PreparedStatement statement = connection.prepareStatement(getUserPermissionSelectQuery());
             ResultSet resultSet = statement.executeQuery()) {
            while (resultSet.next()) {
                UserPermission userPermission = new UserPermission(resultSet.getInt("UM_ID"),
                        resultSet.getInt("UM_PERMISSION_ID"), resultSet.getString("UM_USER_NAME"),
                        resultSet.getInt("UM_TENANT_ID"));
                allUserPermissions.add(userPermission);
            }
        } catch (SQLException e) {
            connection.rollback();
            throw new MigrationClientException("Failed to retrieve all user permissions.", e);
        }

        return allUserPermissions;
    }

    private String getPermissionSelectQuery() throws MigrationClientException {

        if (Utility.isMigrateTenantRange()) {
            return SQLConstants.SELECT_ALL_PERMISSIONS + " WHERE UM_TENANT_ID IN (" + StringUtils
                    .join(getSelectedTenants(), ",") + ")";
        }
        return SQLConstants.SELECT_ALL_PERMISSIONS;
    }

    private String getRolePermissionSelectQuery() throws MigrationClientException {

        if (Utility.isMigrateTenantRange()) {
            return SQLConstants.SELECT_ALL_ROLE_PERMISSIONS + " WHERE UM_TENANT_ID IN (" + StringUtils
                    .join(getSelectedTenants(), ",") + ")";
        }
        return SQLConstants.SELECT_ALL_ROLE_PERMISSIONS;
    }

    private String getUserPermissionSelectQuery() throws MigrationClientException {

        if (Utility.isMigrateTenantRange()) {
            return SQLConstants.SELECT_ALL_USER_PERMISSIONS + " WHERE UM_TENANT_ID IN (" + StringUtils
                    .join(getSelectedTenants(), ",") + ")";
        }
        return SQLConstants.SELECT_ALL_USER_PERMISSIONS;
    }

    private List<Integer> getSelectedTenants() throws MigrationClientException {

        int startingTenantID = Utility.getMigrationStartingTenantID();
        int endingTenantID = Utility.getMigrationEndingTenantID();

        if (!((startingTenantID == -1234 || startingTenantID > 0) && (endingTenantID == -1234 || endingTenantID > 0)
                && (startingTenantID <= endingTenantID))) {
            throw new MigrationClientException("Provided tenant range is invalid.");
        }

        List<Integer> selectedTenants = new ArrayList<>();

        if (startingTenantID == -1234) {
            selectedTenants.add(-1234);
            startingTenantID = 1;
        }
        for (int i = startingTenantID; i <= endingTenantID; i++) {
            selectedTenants.add(i);
        }
        return selectedTenants;
    }

    private void updateRolePermissionTable(Connection connection, List<Permission> permissions)
            throws SQLException, MigrationClientException {

        try (PreparedStatement statement = connection.prepareStatement(SQLConstants.UPDATE_UM_ROLE_PERMISSION)) {
            fillPreparedStatement(permissions, statement);
            statement.executeBatch();
        } catch (SQLException e) {
            connection.rollback();
            throw new MigrationClientException("Failed to update permission ids for roles.", e);
        }
    }

    private void updateUserPermissionTable(Connection connection, List<Permission> permissions)
            throws SQLException, MigrationClientException {

        try (PreparedStatement statement = connection.prepareStatement(SQLConstants.UPDATE_UM_USER_PERMISSION)) {
            fillPreparedStatement(permissions, statement);
            statement.executeBatch();
        } catch (SQLException e) {
            connection.rollback();
            throw new MigrationClientException("Failed to update permission ids for users.", e);
        }
    }

    private void fillPreparedStatement(List<Permission> permissions, PreparedStatement statement) throws SQLException {

        for (Permission permission : permissions) {
            statement.setInt(1, permission.getUniqueId());
            statement.setInt(2, permission.getId());
            statement.setInt(3, permission.getTenantId());
            statement.addBatch();
        }
    }

    private void deleteDuplicatedPermissions(Connection connection, List<Permission> permissions)
            throws SQLException, MigrationClientException {

        try (PreparedStatement statement = connection.prepareStatement(SQLConstants.DELETE_DUPLICATED_PERMISSIONS)) {
            for (Permission permission : permissions) {
                statement.setInt(1, permission.getId());
                statement.addBatch();
            }
            statement.executeBatch();
        } catch (SQLException e) {
            connection.rollback();
            throw new MigrationClientException("Failed to delete duplicated permissions.", e);
        }
    }

    private void deleteDuplicatedRolePermissions(Connection connection, List<RolePermission> duplicatedRolePermissions)
            throws MigrationClientException, SQLException {

        try (PreparedStatement statement = connection
                .prepareStatement(SQLConstants.DELETE_DUPLICATED_ROLE_PERMISSIONS)) {
            for (RolePermission rolePermission : duplicatedRolePermissions) {
                statement.setInt(1, rolePermission.getId());
                statement.addBatch();
            }
            statement.executeBatch();
        } catch (SQLException e) {
            connection.rollback();
            throw new MigrationClientException("Failed to delete duplicated role permissions.", e);
        }
    }

    private void deleteDuplicatedUserPermissions(Connection connection, List<UserPermission> duplicatedUserPermissions)
            throws MigrationClientException, SQLException {

        try (PreparedStatement statement = connection
                .prepareStatement(SQLConstants.DELETE_DUPLICATED_USER_PERMISSIONS)) {
            for (UserPermission userPermission : duplicatedUserPermissions) {
                statement.setInt(1, userPermission.getId());
                statement.addBatch();
            }
            statement.executeBatch();
        } catch (SQLException e) {
            connection.rollback();
            throw new MigrationClientException("Failed to delete duplicated user permissions.", e);
        }
    }
}

package org.wso2.carbon.is.migration.client;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NodeList;
import org.wso2.carbon.identity.core.util.IdentityDatabaseUtil;
import org.wso2.carbon.is.migration.SQLConstants;
import org.wso2.carbon.is.migration.service.v530.util.ResourceUtil;
import org.xml.sax.SAXException;

import java.io.IOException;
import java.io.InputStream;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import javax.sql.DataSource;
import javax.xml.parsers.DocumentBuilder;


public class PermissionDBMigrator {

    private static final Log log = LogFactory.getLog(PermissionDBMigrator.class);
    private static final String RESOURCES_XML = "/modules/migration/migration-5.3.0_to_5.4.0/migration-resources/data"
                                                + "/resources.xml";

    private DataSource umDataSource;
    private boolean continueOnError;
    private boolean noBatchUpdate;

    public PermissionDBMigrator(DataSource umDataSource, boolean continueOnError, boolean noBatchUpdate) {
        this.umDataSource = umDataSource;
        this.continueOnError = continueOnError;
        this.noBatchUpdate = noBatchUpdate;
    }

    /**
     * MigratePermissionData
     */
    public void migratePermissionData() {
        Document permissionMap = getPermissionMap();
        if (permissionMap != null) {
            NodeList permissionsList = permissionMap.getElementsByTagName("permission");
            for (int i = 0; i < permissionsList.getLength(); ++i) {
                Element permission = (Element) permissionsList.item(i);
                migrateOldPermission(permission);
            }
        }
    }

    protected void migrateOldPermission(Element permission) {
        Connection umConnection = null;
        try {
            umConnection = umDataSource.getConnection();
            umConnection.setAutoCommit(false);
            String oldPermission = permission.getAttribute("old");
            NodeList newPermList = permission.getElementsByTagName("new");
            ResultSet oldPermissionsRS = selectExistingPermissions(oldPermission, umConnection);
            umConnection.commit();
            addNewPermissions(oldPermissionsRS, newPermList);
        } catch (SQLException e) {
            log.error("Error while migrating permission data", e);
        } finally {
            IdentityDatabaseUtil.closeConnection(umConnection);
        }
    }

    /**
     * Read permission map from resources. This file contains new permissions against old permission strings
     */
    private Document getPermissionMap() {
        InputStream permissionXmlFile = Thread.currentThread().getContextClassLoader().getResourceAsStream
                (RESOURCES_XML);
        DocumentBuilder dBuilder = ResourceUtil.getSecuredDocumentBuilder();
        Document doc = null;
        try {
            doc = dBuilder.parse(permissionXmlFile);
        } catch (SAXException e) {
            log.error("Error while parsing permission file content.", e);
        } catch (IOException e) {
            log.error("Error while parsing permission file content.", e);
        }
        return doc;
    }


    /**
     * Select permission entries in UM_PERMISSION Table
     */
    private ResultSet selectExistingPermissions(String permission, Connection umConnection) throws SQLException {
        PreparedStatement selectPermissions = umConnection.prepareStatement(SQLConstants.SELECT_PERMISSION);
        selectPermissions.setString(1, permission);
        return selectPermissions.executeQuery();
    }

    /**
     * Select permission entries in UM_PERMISSION Table for given tenant
     */
    private ResultSet selectAddedPermissions(String permission, Connection umConnection,
                                             int tenantId) throws SQLException {
        PreparedStatement selectPermissions = umConnection.prepareStatement(SQLConstants.SELECT_PERMISSION_IN_TENANT);
        selectPermissions.setString(1, permission);
        selectPermissions.setInt(2, tenantId);
        return selectPermissions.executeQuery();
    }

    /**
     * Check whether permission already exists in UM_PERMISSION Table
     */
    private boolean isPermissionExists(Connection umConnection, String resource,
                                       String action, int tenantId, int moduleId) throws SQLException {
        boolean isExist = false;
        PreparedStatement countPermissions = umConnection.prepareStatement(SQLConstants.SELECT_PERMISSION_COUNT);
        countPermissions.setString(1, resource);
        countPermissions.setString(2, action);
        countPermissions.setInt(3, tenantId);
        countPermissions.setInt(4, moduleId);
        ResultSet countRS = countPermissions.executeQuery();
        if (countRS.next()) {
            int numberOfRows = countRS.getInt(1);
            if (numberOfRows > 0) {
                isExist = true;
            }
        }
        IdentityDatabaseUtil.closeResultSet(countRS);
        umConnection.commit();
        return isExist;
    }

    /**
     * Check whether permission already assigned for role in UM_ROLE_PERMISSION Table
     */
    private boolean isPermissionAssignedForRole(Connection umConnection, String roleName, int permID, int isAllowed,
                                                int tenantId, int domainId) throws SQLException {

        boolean isExist = false;
        PreparedStatement countPermissions = umConnection.prepareStatement(SQLConstants.SELECT_ROLE_PERMISSION_COUNT);
        countPermissions.setInt(1, permID);
        countPermissions.setString(2, roleName);
        countPermissions.setInt(3, isAllowed);
        countPermissions.setInt(4, tenantId);
        countPermissions.setInt(5, domainId);

        ResultSet countRS = countPermissions.executeQuery();
        if (countRS.next()) {
            int numberOfRows = countRS.getInt(1);
            if (numberOfRows > 0) {
                isExist = true;
            }
        }
        IdentityDatabaseUtil.closeResultSet(countRS);
        umConnection.commit();
        return isExist;
    }

    /**
     * Select roles with given permission in UM_ROLE_PERMISSION Table
     */
    private ResultSet selectExistingRolesWithPermissions(int permissionId, Connection umConnection) throws
                                                                                                    SQLException {
        PreparedStatement selectPermissions = umConnection.prepareStatement(SQLConstants.SELECT_ROLES_WITH_PERMISSION);
        selectPermissions.setInt(1, permissionId);
        return selectPermissions.executeQuery();
    }

    /**
     * Add new permissions to UM_PERMISSION Table
     */
    private void addNewPermissions(ResultSet oldPermissionsRS, NodeList newPermList) {
        Connection umConnection = null;
        try {
            while (oldPermissionsRS.next()) {
                umConnection = umDataSource.getConnection();
                umConnection.setAutoCommit(false);
                String action = oldPermissionsRS.getString("UM_ACTION");
                int tenantId = oldPermissionsRS.getInt("UM_TENANT_ID");
                int moduleId = oldPermissionsRS.getInt("UM_MODULE_ID");
                int umID = oldPermissionsRS.getInt("UM_ID");

                for (int j = 0; j < newPermList.getLength(); ++j) {
                    Element newPerm = (Element) newPermList.item(j);
                    String newPermValue = newPerm.getTextContent();
                    ResultSet newPermissions = addNewPermission(umConnection, action, tenantId, moduleId, newPermValue);
                    if (newPermissions.next()) {
                        int newUMId = newPermissions.getInt("UM_ID");
                        assignNewPermissionForRoles(umID, newUMId);
                    }
                    IdentityDatabaseUtil.closeResultSet(newPermissions);
                }
                umConnection.commit();
            }
        } catch (SQLException e) {
            log.error("Error while adding new permission data", e);
        } finally {
            IdentityDatabaseUtil.closeConnection(umConnection);
        }
    }

    /**
     * Add new permission to UM_PERMISSION Table if not exists
     */
    private ResultSet addNewPermission(Connection umConnection, String action,
                                       int tenantId, int moduleId, String newPermValue) throws SQLException {

        if (!isPermissionExists(umConnection, newPermValue, action, tenantId, moduleId)) {
            PreparedStatement addPermission = umConnection.prepareStatement(SQLConstants.INSERT_PERMISSION);
            addPermission.setString(1, newPermValue);
            addPermission.setString(2, action);
            addPermission.setInt(3, tenantId);
            addPermission.setInt(4, moduleId);
            addPermission.execute();
            umConnection.commit();
        }
        return selectAddedPermissions(newPermValue, umConnection, tenantId);
    }


    /**
     * Add new permission to role in UM_ROLE_PERMISSION Table if not exists
     */
    private void assignNewPermissionForRoles(int oldPermUMId, int newPermUMId) {
        Connection umConnection = null;
        try {
            umConnection = umDataSource.getConnection();
            umConnection.setAutoCommit(false);
            ResultSet rolesWithExistingPerm = selectExistingRolesWithPermissions(oldPermUMId, umConnection);
            while (rolesWithExistingPerm.next()) {
                int isAllowed = rolesWithExistingPerm.getInt("UM_IS_ALLOWED");
                int tenantId = rolesWithExistingPerm.getInt("UM_TENANT_ID");
                int domainId = rolesWithExistingPerm.getInt("UM_DOMAIN_ID");
                String roleName = rolesWithExistingPerm.getString("UM_ROLE_NAME");
                if (!isPermissionAssignedForRole(umConnection, roleName, newPermUMId, isAllowed, tenantId, domainId)) {
                    PreparedStatement assignPermission = umConnection.prepareStatement(SQLConstants
                                                                                               .INSERT_ROLES_WITH_PERMISSION);
                    assignPermission.setInt(1, newPermUMId);
                    assignPermission.setString(2, roleName);
                    assignPermission.setInt(3, isAllowed);
                    assignPermission.setInt(4, tenantId);
                    assignPermission.setInt(5, domainId);
                    assignPermission.execute();
                    umConnection.commit();
                }
            }
            IdentityDatabaseUtil.closeResultSet(rolesWithExistingPerm);
            umConnection.commit();
        } catch (SQLException e) {
            log.error("Error while assigning new permission data", e);
        } finally {
            IdentityDatabaseUtil.closeConnection(umConnection);
        }
    }
}

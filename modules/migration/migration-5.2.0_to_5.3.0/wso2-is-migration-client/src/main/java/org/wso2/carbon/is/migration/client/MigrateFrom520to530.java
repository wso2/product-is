/*
* Copyright (c) 2016, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
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

package org.wso2.carbon.is.migration.client;

import org.apache.axiom.om.OMElement;
import org.apache.commons.lang.StringUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NodeList;
import org.wso2.carbon.identity.base.IdentityException;
import org.wso2.carbon.identity.claim.metadata.mgt.util.ClaimConstants;
import org.wso2.carbon.identity.core.util.IdentityConfigParser;
import org.wso2.carbon.identity.core.util.IdentityCoreConstants;
import org.wso2.carbon.identity.core.util.IdentityDatabaseUtil;
import org.wso2.carbon.identity.core.util.IdentityTenantUtil;
import org.wso2.carbon.identity.core.util.IdentityUtil;
import org.wso2.carbon.is.migration.ClaimManager;
import org.wso2.carbon.is.migration.ISMigrationException;
import org.wso2.carbon.is.migration.MigrationDatabaseCreator;
import org.wso2.carbon.is.migration.RegistryDataManager;
import org.wso2.carbon.is.migration.ResidentIdpMetadataManager;
import org.wso2.carbon.is.migration.SQLConstants;
import org.wso2.carbon.is.migration.bean.Claim;
import org.wso2.carbon.is.migration.bean.MappedAttribute;
import org.wso2.carbon.is.migration.client.internal.ISMigrationServiceDataHolder;
import org.wso2.carbon.is.migration.util.ResourceUtil;
import org.wso2.carbon.user.api.Tenant;
import org.wso2.carbon.user.api.UserStoreException;
import org.wso2.carbon.user.core.util.DatabaseUtil;
import org.wso2.carbon.utils.dbcreator.DatabaseCreator;
import org.xml.sax.SAXException;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.io.PrintWriter;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import javax.naming.Context;
import javax.naming.InitialContext;
import javax.naming.NamingException;
import javax.sql.DataSource;
import javax.xml.namespace.QName;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;

@SuppressWarnings("unchecked")
public class MigrateFrom520to530 implements MigrationClient {

    private static final Log log = LogFactory.getLog(MigrateFrom520to530.class);
    private static final String RESOURCES_XML = "/resources.xml";
    private DataSource dataSource;
    private DataSource umDataSource;

    public MigrateFrom520to530() throws IdentityException {
        try {
            initIdentityDataSource();
            initUMDataSource();
            Connection conn = null;
            try {
                conn = dataSource.getConnection();
                if ("oracle".equals(DatabaseCreator.getDatabaseType(conn)) && ISMigrationServiceDataHolder
                        .getIdentityOracleUser() == null) {
                    ISMigrationServiceDataHolder.setIdentityOracleUser(dataSource.getConnection().getMetaData()
                            .getUserName());
                }
            } catch (Exception e) {
                log.error("Error while reading the identity oracle username", e);
            } finally {
                try {
                    conn.close();
                } catch (SQLException e) {
                    log.warn("Error while closing the identity database connection", e);
                }
            }
            try {
                conn = umDataSource.getConnection();
                if ("oracle".equals(DatabaseCreator.getDatabaseType(conn)) && ISMigrationServiceDataHolder
                        .getIdentityOracleUser() == null) {
                    ISMigrationServiceDataHolder.setIdentityOracleUser(umDataSource.getConnection().getMetaData()
                            .getUserName());
                }
            } catch (Exception e) {
                log.error("Error while reading the user manager database oracle username", e);
            } finally {
                try {
                    conn.close();
                } catch (SQLException e) {
                    log.warn("Error while closing the user manager database connection", e);
                }
            }
        } catch (IdentityException e) {
            String errorMsg = "Error when reading the JDBC Configuration from the file.";
            log.error(errorMsg, e);
            throw new ISMigrationException(errorMsg, e);
        }
    }

    /**
     * Initialize the identity datasource
     *
     * @throws IdentityException
     */
    private void initIdentityDataSource() throws IdentityException {
        try {
            OMElement persistenceManagerConfigElem = IdentityConfigParser.getInstance()
                    .getConfigElement("JDBCPersistenceManager");

            if (persistenceManagerConfigElem == null) {
                String errorMsg = "Identity Persistence Manager configuration is not available in " +
                        "identity.xml file. Terminating the JDBC Persistence Manager " +
                        "initialization. This may affect certain functionality.";
                log.error(errorMsg);
                throw new ISMigrationException(errorMsg);
            }

            OMElement dataSourceElem = persistenceManagerConfigElem.getFirstChildWithName(
                    new QName(IdentityCoreConstants.IDENTITY_DEFAULT_NAMESPACE, "DataSource"));

            if (dataSourceElem == null) {
                String errorMsg = "DataSource Element is not available for JDBC Persistence " +
                        "Manager in identity.xml file. Terminating the JDBC Persistence Manager " +
                        "initialization. This might affect certain features.";
                log.error(errorMsg);
                throw new ISMigrationException(errorMsg);
            }

            OMElement dataSourceNameElem = dataSourceElem.getFirstChildWithName(
                    new QName(IdentityCoreConstants.IDENTITY_DEFAULT_NAMESPACE, "Name"));

            if (dataSourceNameElem != null) {
                String dataSourceName = dataSourceNameElem.getText();
                Context ctx = new InitialContext();
                dataSource = (DataSource) ctx.lookup(dataSourceName);
            }
        } catch (NamingException e) {
            String errorMsg = "Error when looking up the Identity Data Source.";
            log.error(errorMsg, e);
            throw new ISMigrationException(errorMsg, e);
        }
    }

    private void initUMDataSource() {
        umDataSource = DatabaseUtil.getRealmDataSource(ISMigrationServiceDataHolder.getRealmService()
                .getBootstrapRealmConfiguration());
    }

    /*
     * @throws ISMigrationException
     * @throws SQLException
     */
    public void databaseMigration() throws Exception {
        boolean selectiveMigration = false;
        String migrateIdentityDB = System.getProperty("migrateIdentityDB");
        String migrateClaimData = System.getProperty("migrateClaimData");
        String migrateUMDB = System.getProperty("migrateUMDB");
        String migrateEmailTemplateData = System.getProperty("migrateEmailTemplateData");
        String migratePermissionData = System.getProperty("migratePermissionData");
        String migrateChallengeQuestionData = System.getProperty("migrateChallengeQuestionData");
        String migrateResidentIdpMetadata = System.getProperty("migrateResidentIdpMetaData");
        String migrateOIDCScopeData = System.getProperty("migrateOIDCScopeData");

        // to decide whether we need to migrate active tenants only.
        String migrateActiveTenants = System.getProperty("migrateActiveTenantsOnly");
        boolean migrateActiveTenantsOnly = Boolean.parseBoolean(migrateActiveTenants);
        if (migrateActiveTenantsOnly) {
            log.info("Migrate Active Tenants Only option enabled.");
        }

        if (Boolean.parseBoolean(migrateIdentityDB)) {
            selectiveMigration = true;
            migrateIdentityDB();
            log.info("Migrated the identity database schema");
        }
        if (Boolean.parseBoolean(migrateUMDB)) {
            selectiveMigration = true;
            migrateUMDB();
            log.info("Migrated the user management database schema");
        }
        if (Boolean.parseBoolean(migrateClaimData)) {
            selectiveMigration = true;
            migrateClaimData(migrateActiveTenantsOnly);
            log.info("Migrated the Claim management data");
        }
        if (Boolean.parseBoolean(migrateEmailTemplateData)) {
            selectiveMigration = true;
            migrateEmailTemplateData(migrateActiveTenantsOnly);
            log.info("Migrated the Email template data");
        }

        if (Boolean.parseBoolean(migratePermissionData)) {
            selectiveMigration = true;
            migratePermissionData();
            log.info("Migrated the Permission data");
        }

        if (Boolean.parseBoolean(migrateChallengeQuestionData)) {
            selectiveMigration = true;
            migrateChallengeQuestionData(migrateActiveTenantsOnly);
            log.info("Migrated the Challenge Question data.");
        }

        if (Boolean.parseBoolean(migrateResidentIdpMetadata)) {
            selectiveMigration = true;
            migrateResidentIdpMetadata(migrateActiveTenantsOnly);
            log.info("Migrated the Resident IDP metadata.");
        }

        if (Boolean.parseBoolean(migrateOIDCScopeData)) {
            selectiveMigration = true;
            copyOIDCScopeData(migrateActiveTenantsOnly);
            log.info("Migrated OIDC Scope data.");
        }

        if (!selectiveMigration) {
            migrateIdentityDB();
            migrateClaimData(migrateActiveTenantsOnly);
            migratePermissionData();
            migrateEmailTemplateData(migrateActiveTenantsOnly);
            migrateChallengeQuestionData(migrateActiveTenantsOnly);
            migrateResidentIdpMetadata(migrateActiveTenantsOnly);
            copyOIDCScopeData(migrateActiveTenantsOnly);
            log.info("Migration completed from IS 5.2.0 to IS 5.3.0");
        }
    }

    public void migrateResidentIdpMetadata(boolean migrateActiveTenantsOnly) throws Exception {

        new ResidentIdpMetadataManager().migrateResidentIdpMetaData(migrateActiveTenantsOnly);
    }

    public void migrateEmailTemplateData(boolean migrateActiveTenantsOnly) throws Exception {

        RegistryDataManager registryDataManager = RegistryDataManager.getInstance();
        registryDataManager.migrateEmailTemplates(migrateActiveTenantsOnly);
    }

    public void migrateChallengeQuestionData(boolean migrateActiveTenantsOnly) throws Exception {

        RegistryDataManager registryDataManager = RegistryDataManager.getInstance();
        registryDataManager.migrateChallengeQuestions(migrateActiveTenantsOnly);
    }

    /*
        Copies the oidc-config file and add it as a registry resource
     */
    public void copyOIDCScopeData(boolean migrateActiveTenantsOnly) throws Exception {

        RegistryDataManager.getInstance().copyOIDCScopeData(migrateActiveTenantsOnly);
    }

    public void migrateIdentityDB() throws Exception {

        if (!ResourceUtil.isSchemaMigrated(dataSource)) {
            MigrationDatabaseCreator migrationDatabaseCreator = new MigrationDatabaseCreator(dataSource, umDataSource);
            migrationDatabaseCreator.executeIdentityMigrationScript();
        } else {
            log.info("Identity schema is already migrated");
        }
    }

    public void migrateUMDB() throws Exception {
        MigrationDatabaseCreator migrationDatabaseCreator = new MigrationDatabaseCreator(dataSource, umDataSource);
        migrationDatabaseCreator.executeUmMigrationScript();
    }

    public boolean migrateClaimData(boolean migrateActiveTenantsOnly) {
        List<Claim> claims = new ArrayList<>();
        Connection umConnection = null;

        PreparedStatement loadDialectsStatement = null;
        PreparedStatement loadMappedAttributeStatement;

        PreparedStatement updateRole = null;

        ResultSet dialects = null;
        ResultSet claimResultSet;
        StringBuilder report = new StringBuilder();
        report.append("---------------------------------- WSO2 Identity Server 5.3.0 claim Migration Report -----------------------------------------\n \n");


        report.append("\n\n------------------------------------------------- Validating Existing Claims----------------------------------------------\n \n");

        //Is validation success. If not success, it will be created additional calims to be success
        boolean isSuccess = true;
        //This is used to record error log
        int count = 1;
        try {
            umConnection = umDataSource.getConnection();

            umConnection.setAutoCommit(false);

            loadDialectsStatement = umConnection.prepareStatement(SQLConstants.LOAD_CLAIM_DIALECTS);
            dialects = loadDialectsStatement.executeQuery();

            //This is used for validating multiple mapped attribute in each dialect. Format : dialectURL-->
            // MappedAttribute---> List of claim URIs. If any of the mapped attribute corresponds to multiple claim
            // URI, the validation should be false.
            Map<String, Map<String, List<String>>> data = new HashMap<>();

            List<Integer> inactiveTenants = new ArrayList<>();
            if (migrateActiveTenantsOnly) {
                inactiveTenants = getInactiveTenants();
            }

            while (dialects.next()) {
                //Keep the list of claim URI against domain Qualified Mapped Attribute
                Map<String, List<String>> mappedAttributes = new HashMap<>();

                int dialectId = dialects.getInt("UM_ID");
                String dialectUri = dialects.getString("UM_DIALECT_URI");
                int tenantId = dialects.getInt("UM_TENANT_ID");

                if (migrateActiveTenantsOnly && inactiveTenants.contains(tenantId)) {
                    log.info("Inactive tenant : " + tenantId + " , " +
                            "Skipping claim data migration for dialect : " + dialectUri);
                    continue;
                }

                loadMappedAttributeStatement = umConnection.prepareStatement(SQLConstants.LOAD_MAPPED_ATTRIBUTE);
                loadMappedAttributeStatement.setInt(1, dialectId);
                claimResultSet = loadMappedAttributeStatement.executeQuery();

                //Read all records in UM_CLAIM one by one
                while (claimResultSet.next()) {
                    List<String> claimURIs;
                    String attribute = claimResultSet.getString("UM_MAPPED_ATTRIBUTE");
                    String claimURI = claimResultSet.getString("UM_CLAIM_URI");
                    String displayTag = claimResultSet.getString("UM_DISPLAY_TAG");
                    String description = claimResultSet.getString("UM_DESCRIPTION");
                    String mappedAttributeDomain = claimResultSet.getString("UM_MAPPED_ATTRIBUTE_DOMAIN");
                    String regEx = claimResultSet.getString("UM_REG_EX");
                    int supportedByDefault = claimResultSet.getInt("UM_SUPPORTED");
                    int required = claimResultSet.getInt("UM_REQUIRED");
                    int displayOrder = claimResultSet.getInt("UM_DISPLAY_ORDER");
                    int readOnly = claimResultSet.getInt("UM_READ_ONLY");

                    boolean isRequired = required == 1;
                    boolean isSupportedByDefault = supportedByDefault == 1;
                    boolean isReadOnly = readOnly == 1;

                    Claim claimDTO = new Claim(claimURI, displayTag, description, regEx, isSupportedByDefault, isRequired, displayOrder, isReadOnly,
                            tenantId, dialectUri);
                    if (claims.contains(claimDTO)) {
                        for (Claim claim : claims) {
                            if (claim.equals(claimDTO)) {
                                MappedAttribute mappedAttribute = new MappedAttribute(attribute, mappedAttributeDomain);
                                claim.getAttributes().add(mappedAttribute);
                                break;
                            }
                        }
                    } else {
                        MappedAttribute mappedAttribute = new MappedAttribute(attribute, mappedAttributeDomain);
                        List<MappedAttribute> mappedAttributesList = claimDTO.getAttributes();
                        mappedAttributesList.add(mappedAttribute);
                        claimDTO.setAttributes(mappedAttributesList);
                        claims.add(claimDTO);
                    }

                    String domainQualifiedAttribute;
                    if (StringUtils.isBlank(mappedAttributeDomain)) {
                        domainQualifiedAttribute = attribute;
                    } else {
                        domainQualifiedAttribute = mappedAttributeDomain + "/" + attribute;
                    }
                    if (mappedAttributes.get(domainQualifiedAttribute) != null) {
                        claimURIs = mappedAttributes.get(domainQualifiedAttribute);
                    } else {
                        claimURIs = new ArrayList<>();
                    }

                    claimURIs.add(claimURI);
                    mappedAttributes.put(domainQualifiedAttribute, claimURIs);
                }

                //get the tenant qualified dialect URL
                dialectUri = dialectUri + "@" + IdentityTenantUtil.getTenantDomain(tenantId);
                data.put(dialectUri, mappedAttributes);
            }

            //This is used to keep mapped attributes in each dialect in each tenant.
            // Format is tenantDomain:dialectURL->List of Mapped Attributes. If any remote dialect has a mapped
            // attribute which is not matching to any of the local claim's mapped attribute, the validation should be
            // false.
            Map<String, Map<String, List<String>>> tenantDialectMappedAttributes = new HashMap<>();

            for (Map.Entry<String, Map<String, List<String>>> entry : data.entrySet()) {

                //This is used to keep the mapped attributes against dialect URI
                Map<String, List<String>> dialectMappedAttributes = new HashMap<>();

                List<String> attributes = new ArrayList<>();
                String dialect = entry.getKey();
                String[] split = dialect.split("@");
                //separate the dialect URL and tenant domain from domain qualified dialect URL
                dialect = split[0];
                String tenantDomain = split[1];
                if (tenantDialectMappedAttributes.get(tenantDomain) != null) {
                    dialectMappedAttributes = tenantDialectMappedAttributes.get(tenantDomain);
                }

                if (dialectMappedAttributes.get(dialect) != null) {
                    attributes = dialectMappedAttributes.get(dialect);
                }

                if (entry.getValue() != null) {
                    for (Map.Entry<String, List<String>> claimEntry : entry.getValue().entrySet()) {
                        String mappedAttribute = claimEntry.getKey();
                        attributes.add(mappedAttribute.trim());
                        if (claimEntry.getValue() != null && claimEntry.getValue().size() > 1) {
                            isSuccess = false;

                            report.append(count + ")  Duplicate Mapped Attribute found for dialect :" + dialect +
                                    " | Mapped Attribute :" + mappedAttribute + " | " +
                                    "Relevant Claims : " + claimEntry.getValue() + " | Tenant Domain :" + tenantDomain);
                            report.append("\n\n");
                            if (log.isDebugEnabled()) {
                                log.debug("Duplicate Mapped Attribute found for dialect :" + dialect +
                                        " | Mapped Attribute :" + mappedAttribute + " | " +
                                        "Relevant Claims : " + claimEntry.getValue() + " | Tenant Domain :" + tenantDomain);
                            }

                            count++;
                        }
                    }

                    dialectMappedAttributes.put(entry.getKey().replace("@" + tenantDomain, ""), attributes);
                    tenantDialectMappedAttributes.put(tenantDomain, dialectMappedAttributes);
                }
            }

            //If there is no matching mapped attribute in each tenants remote dialect's claims in relevant tenant's
            // local dialect's claims mapped attributes, it is required to create a new mapping the local dialect
            // this variable is used to keep the new local claim URIs which needs to be added in each tenant. Format
            // is tenantDomain--> List of mappedAttributes
            Map<String, Set<String>> claimsToAddMap = new HashMap<>();
            if (tenantDialectMappedAttributes != null) {
                for (Map.Entry<String, Map<String, List<String>>> entry : tenantDialectMappedAttributes.entrySet()) {
                    String tenantDomain = entry.getKey();
                    Set<String> claimsToAdd = new HashSet<>();
                    if (claimsToAddMap.get(tenantDomain) != null) {
                        claimsToAdd = claimsToAddMap.get(tenantDomain);
                    }

                    if (entry.getValue() != null) {
                        List<String> localAttributes = entry.getValue().get(ClaimConstants.LOCAL_CLAIM_DIALECT_URI);
                        for (Map.Entry<String, List<String>> dialect : entry.getValue().entrySet()) {
                            if (!ClaimConstants.LOCAL_CLAIM_DIALECT_URI.equalsIgnoreCase(dialect.getKey())) {
                                List<String> remoteClaimAttributes = dialect.getValue();
                                if (remoteClaimAttributes != null) {
                                    for (String remoteClaimAttribute : remoteClaimAttributes) {
                                        if (!localAttributes.contains(remoteClaimAttribute)) {
                                            claimsToAdd.add(remoteClaimAttribute);
                                            isSuccess = false;
                                            report.append("\n\n" + count + ")  Mapped Attribute : " +
                                                    remoteClaimAttribute + " in dialect :" + dialect.getKey() + " is not associated to any of the local claim in tenant domain: " + tenantDomain);

                                            if (log.isDebugEnabled()) {
                                                log.debug("Mapped Attribute : " + remoteClaimAttribute + " in dialect :"
                                                        + dialect.getKey() + " is not associated to any of the local claim in tenant domain: " + tenantDomain);
                                            }
                                            count++;
                                        }
                                    }
                                }
                            }
                        }
                    }
                    claimsToAddMap.put(tenantDomain, claimsToAdd);
                }
            }

        } catch (SQLException e) {
            log.error("Error while validating claim management data", e);
        } finally {
            IdentityDatabaseUtil.closeResultSet(dialects);
            IdentityDatabaseUtil.closeStatement(loadDialectsStatement);
            IdentityDatabaseUtil.closeStatement(updateRole);
            IdentityDatabaseUtil.closeConnection(umConnection);
        }


        // Migrating claim Data starts here.
        ClaimManager claimManager = ClaimManager.getInstance();

        if (claims != null) {

            report.append("\n\n------------------------------------------------------------------------------ Claim " +
                    "Migration -------------------------------------------------------------------------------\n \n");
            try {
                // Add Claim Dialects
                claimManager.addClaimDialects(claims, report);

                // Add Local Claims.
                claimManager.addLocalClaims(claims, report);

                // Add External Claims
                claimManager.addExternalClaim(claims, report);
            } catch (ISMigrationException e) {
                log.error("Error while migrating claim data", e);
            }
        }

        if (!isSuccess) {
            PrintWriter out = null;
            try {
                out = new PrintWriter("claim-migration.txt");
                out.println(report.toString());
            } catch (FileNotFoundException e) {
                log.error("Error while creating claim Migration Report");
            } finally {
                if (out != null) {
                    out.close();
                }
            }
        }
        return isSuccess;
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
        DocumentBuilder dBuilder = getSecuredDocumentBuilder();
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
     * * This method provides a secured document builder which will secure XXE attacks.
     *
     * @return DocumentBuilder
     * @throws javax.xml.parsers.ParserConfigurationException
     */
    private DocumentBuilder getSecuredDocumentBuilder() {
        DocumentBuilderFactory documentBuilderFactory = IdentityUtil.getSecuredDocumentBuilderFactory();
        DocumentBuilder documentBuilder = null;
        try {
            documentBuilder = documentBuilderFactory.newDocumentBuilder();
        } catch (ParserConfigurationException e) {
            log.error("Error while getting document builder.", e);
        }
        return documentBuilder;
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


    private List<Integer> getInactiveTenants() {
        List<Integer> inactiveTenants = new ArrayList<>();
        try {
            Tenant[] tenants = ISMigrationServiceDataHolder.getRealmService().getTenantManager().getAllTenants();
            for (Tenant tenant : tenants) {
                if (!tenant.isActive()) {
                    inactiveTenants.add(tenant.getId());
                }
            }
        } catch (UserStoreException e) {
            log.error("Error while getting inactive tenant details. Assuming zero inactive tenants.");
            return new ArrayList<>();
        }

        return inactiveTenants;
    }
}

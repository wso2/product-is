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
import org.wso2.carbon.identity.base.IdentityException;
import org.wso2.carbon.identity.claim.metadata.mgt.util.ClaimConstants;
import org.wso2.carbon.identity.core.util.IdentityConfigParser;
import org.wso2.carbon.identity.core.util.IdentityCoreConstants;
import org.wso2.carbon.identity.core.util.IdentityDatabaseUtil;
import org.wso2.carbon.identity.core.util.IdentityTenantUtil;
import org.wso2.carbon.is.migration.ClaimManager;
import org.wso2.carbon.is.migration.ISMigrationException;
import org.wso2.carbon.is.migration.MigrationDatabaseCreator;
import org.wso2.carbon.is.migration.SQLConstants;
import org.wso2.carbon.is.migration.bean.Claim;
import org.wso2.carbon.is.migration.bean.MappedAttribute;
import org.wso2.carbon.is.migration.client.internal.ISMigrationServiceDataHolder;
import org.wso2.carbon.is.migration.util.ResourceUtil;
import org.wso2.carbon.user.core.util.DatabaseUtil;
import org.wso2.carbon.utils.dbcreator.DatabaseCreator;

import javax.naming.Context;
import javax.naming.InitialContext;
import javax.naming.NamingException;
import javax.sql.DataSource;
import javax.xml.namespace.QName;
import java.io.FileNotFoundException;
import java.io.PrintWriter;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.*;

@SuppressWarnings("unchecked")
public class MigrateFrom520to530 implements MigrationClient {

    private static final Log log = LogFactory.getLog(MigrateFrom520to530.class);
    private DataSource dataSource;
    private DataSource umDataSource;
    private static List<Claim> claims = null;


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
        String migrateIdentityDB = System.getProperty("migrateIdentityDB");
        String migrateClaimData = System.getProperty("migrateClaimData");
        String migrateUMDB = System.getProperty("migrateUMDB");

        if (Boolean.parseBoolean(migrateIdentityDB)) {
            migrateIdentityDB();
            log.info("Migrated the identity database schema");
        } else if (Boolean.parseBoolean(migrateUMDB)) {
            migrateUMDB();
            log.info("Migrated the user management database schema");
        } else if (Boolean.parseBoolean(migrateClaimData)) {
            validateClaimMigration();
            migrateClaimData();
            log.info("Migrated the Claim management data");
        } else {
            migrateIdentityDB();
            validateClaimMigration();
            migrateClaimData();
            log.info("Migrated the identity database");
        }
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

    public boolean validateClaimMigration() {
        claims = new ArrayList<>();
        Connection umConnection = null;

        PreparedStatement loadDialectsStatement = null;
        PreparedStatement loadMappedAttributeStatement;

        PreparedStatement updateRole = null;

        ResultSet dialects = null;
        ResultSet claimResultSet;
        StringBuilder errors = new StringBuilder();
        errors.append("----- WSO2 Identity Server 5.3.0 claim Migration Report -----\n \n");

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

            while (dialects.next()) {
                //Keep the list of claim URI against domain Qualified Mapped Attribute
                Map<String, List<String>> mappedAttributes = new HashMap<>();

                int dialectId = dialects.getInt("UM_ID");
                String dialectUri = dialects.getString("UM_DIALECT_URI");
                int tenantId = dialects.getInt("UM_TENANT_ID");

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

                    boolean isRequired = required == 1 ? true : false;
                    boolean isSupportedByDefault = supportedByDefault == 1 ? true : false;
                    boolean isReadOnly = readOnly == 1 ? true : false;

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

                            errors.append(count + ")  Duplicate Mapped Attribute found for dialect :" + dialect +
                                    " | Mapped Attribute :" + mappedAttribute + " | " +
                                    "Relevant Claims : " + claimEntry.getValue() + " | Tenant Domain :" + tenantDomain);
                            errors.append("\n\n");
                            log.warn("Duplicate Mapped Attribute found for dialect :" + dialect +
                                    " | Mapped Attribute :" + mappedAttribute + " | " +
                                    "Relevant Claims : " + claimEntry.getValue() + " | Tenant Domain :" + tenantDomain);
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
                                            errors.append("\n\n" + count + ")  Mapped Attribute : " +
                                                    remoteClaimAttribute + " in dialect :" + dialect.getKey() + " is not associated to any of the local claim in tenant domain: " + tenantDomain);

                                            errors.append("\n It will be created a new claim :" + ClaimConstants.LOCAL_CLAIM_DIALECT_URI + "/migration__" + remoteClaimAttribute);
                                            log.warn("Mapped Attribute : " + remoteClaimAttribute + " in dialect :"
                                                    + dialect.getKey() + " is not associated to any of the local claim in tenant domain: " + tenantDomain);
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

            //Adding new claims to add in each tenant's local dialect to claims object.
            for (Map.Entry<String, Set<String>> claimsSet : claimsToAddMap.entrySet()) {
                if (claimsSet.getValue() != null) {
                    for (String attribute : claimsSet.getValue()) {
                        Claim claim = new Claim();
                        claim.setTenantId(IdentityTenantUtil.getTenantId(claimsSet.getKey()));
                        if (attribute.contains("/")) {
                            String[] splitAttribute = attribute.split("/");
                            claim.setClaimURI(ClaimConstants.LOCAL_CLAIM_DIALECT_URI + "/migration__" + splitAttribute[1]);
                            MappedAttribute mappedAttribute = new MappedAttribute(splitAttribute[1], splitAttribute[0]);
                            List<MappedAttribute> mappedAttributes = claim.getAttributes();
                            mappedAttributes.add(mappedAttribute);
                            claim.setAttributes(mappedAttributes);
                            claim.setDisplayTag("migration__" + splitAttribute[1]);
                        } else {
                            claim.setClaimURI(ClaimConstants.LOCAL_CLAIM_DIALECT_URI + "/migration__" + attribute);
                            MappedAttribute mappedAttribute = new MappedAttribute(attribute);
                            List<MappedAttribute> mappedAttributes = claim.getAttributes();
                            mappedAttributes.add(mappedAttribute);
                            claim.setAttributes(mappedAttributes);
                            claim.setDisplayTag("migration__" + attribute);
                        }
                        claim.setDescription(attribute);
                        claim.setDialectURI(ClaimConstants.LOCAL_CLAIM_DIALECT_URI);
                        claims.add(claim);
                    }
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

        if (!isSuccess) {
            PrintWriter out = null;
            try {
                out = new PrintWriter("claim-migration.txt");
                out.println(errors.toString());
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

    public void migrateClaimData() {
        ClaimManager claimManager = ClaimManager.getInstance();

        if (claims != null) {
            try {
                claimManager.addClaimDialects(claims);
                claimManager.addLocalClaims(claims);
                claimManager.addExternalClaim(claims);
            } catch (ISMigrationException e) {
                log.error("Error while migrating claim data", e);
            }
        }
    }
}

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
import org.wso2.carbon.identity.core.util.IdentityConfigParser;
import org.wso2.carbon.identity.core.util.IdentityCoreConstants;
import org.wso2.carbon.identity.core.util.IdentityDatabaseUtil;
import org.wso2.carbon.identity.core.util.IdentityTenantUtil;
import org.wso2.carbon.is.migration.ClaimManager;
import org.wso2.carbon.is.migration.ISMigrationException;
import org.wso2.carbon.is.migration.SQLConstants;
import org.wso2.carbon.is.migration.bean.Claim;
import org.wso2.carbon.is.migration.client.internal.ISMigrationServiceDataHolder;
import org.wso2.carbon.user.core.util.DatabaseUtil;
import org.wso2.carbon.utils.dbcreator.DatabaseCreator;

import javax.naming.Context;
import javax.naming.InitialContext;
import javax.naming.NamingException;
import javax.sql.DataSource;
import javax.xml.namespace.QName;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

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
        String checkClaimData = System.getProperty("checkClaimData");
        if (Boolean.parseBoolean(checkClaimData)) {
            validateClaimMigration();
            log.info("Check Claim Data");
        }
    }


    public boolean validateClaimMigration() {
        claims = new ArrayList<>();
        Connection umConnection = null;

        PreparedStatement loadDialectsStatement = null;
        PreparedStatement loadMappedAttributeStatement;

        PreparedStatement updateRole = null;

        ResultSet dialects = null;
        ResultSet claimResultSet;

        boolean isSuccess = true;

        try {
            umConnection = umDataSource.getConnection();

            umConnection.setAutoCommit(false);

            loadDialectsStatement = umConnection.prepareStatement(SQLConstants.LOAD_CLAIM_DIALECTS);
            dialects = loadDialectsStatement.executeQuery();
            Map<String, Map<String, List<String>>> data = new HashMap<>();

            while (dialects.next()) {
                Map<String, List<String>> mappedAttributes = new HashMap<>();

                int dialectId = dialects.getInt("UM_ID");
                String dialectUri = dialects.getString("UM_DIALECT_URI");
                int tenantId = dialects.getInt("UM_TENANT_ID");

                loadMappedAttributeStatement = umConnection.prepareStatement(SQLConstants.LOAD_MAPPED_ATTRIBUTE);
                loadMappedAttributeStatement.setInt(1, dialectId);
                claimResultSet = loadMappedAttributeStatement.executeQuery();
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

                    Claim claimDTO = new Claim(attribute, claimURI, displayTag, description,
                            mappedAttributeDomain, regEx, isSupportedByDefault, isRequired, displayOrder, isReadOnly,
                            tenantId, dialectUri);
                    claims.add(claimDTO);

                    String domainQualifiedAttribute;
                    if (StringUtils.isBlank(mappedAttributeDomain)) {
                        domainQualifiedAttribute = attribute;
                    } else {
                        domainQualifiedAttribute = mappedAttributeDomain + ":" + attribute;
                    }
                    if (mappedAttributes.get(domainQualifiedAttribute) != null) {
                        claimURIs = mappedAttributes.get(domainQualifiedAttribute);
                    } else {
                        claimURIs = new ArrayList<>();
                    }

                    claimURIs.add(claimURI);
                    mappedAttributes.put(domainQualifiedAttribute, claimURIs);
                }

                dialectUri = dialectUri + "@" + IdentityTenantUtil.getTenantDomain(tenantId);
                data.put(dialectUri, mappedAttributes);
            }

            for (Map.Entry<String, Map<String, List<String>>> entry : data.entrySet()) {
                String dialect = entry.getKey();
                String[] split = dialect.split("@");
                dialect = split[0];
                String tenantDomain = split[1];
                if (entry.getValue() != null) {
                    for (Map.Entry<String, List<String>> claimEntry : entry.getValue().entrySet()) {
                        String mappedAttribute = claimEntry.getKey();
                        if (claimEntry.getValue() != null && claimEntry.getValue().size() > 1) {
                            isSuccess = false;
                            log.error("Duplicate Mapped Attribute found for dialect :" + dialect +
                                    " | Mapped Attribute :" + mappedAttribute + " | " +
                                    "Relevant Claims : " + claimEntry.getValue() + " | Tenant Domain :" + tenantDomain);
                        }
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

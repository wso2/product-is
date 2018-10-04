/*
 * Copyright (c) 2017, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
 *
 *  WSO2 Inc. licenses this file to you under the Apache License,
 *  Version 2.0 (the "License"); you may not use this file except
 *  in compliance with the License.
 *  You may obtain a copy of the License at
 *
 *  http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing,
 *  software distributed under the License is distributed on an
 *  "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 *  KIND, either express or implied.  See the License for the
 *  specific language governing permissions and limitations
 *  under the License.
 *
 */
package org.wso2.carbon.is.migration.service.v530.migrator;

import org.apache.commons.lang.StringUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.wso2.carbon.identity.base.IdentityRuntimeException;
import org.wso2.carbon.identity.claim.metadata.mgt.dao.CacheBackedLocalClaimDAO;
import org.wso2.carbon.identity.claim.metadata.mgt.dao.ClaimDialectDAO;
import org.wso2.carbon.identity.claim.metadata.mgt.dao.LocalClaimDAO;
import org.wso2.carbon.identity.claim.metadata.mgt.exception.ClaimMetadataException;
import org.wso2.carbon.identity.claim.metadata.mgt.model.AttributeMapping;
import org.wso2.carbon.identity.claim.metadata.mgt.model.ClaimDialect;
import org.wso2.carbon.identity.claim.metadata.mgt.model.LocalClaim;
import org.wso2.carbon.identity.claim.metadata.mgt.util.ClaimConstants;
import org.wso2.carbon.identity.core.migrate.MigrationClientException;
import org.wso2.carbon.identity.core.util.IdentityDatabaseUtil;
import org.wso2.carbon.identity.core.util.IdentityTenantUtil;
import org.wso2.carbon.is.migration.internal.ISMigrationServiceDataHolder;
import org.wso2.carbon.is.migration.service.Migrator;
import org.wso2.carbon.is.migration.service.v530.ClaimManager;
import org.wso2.carbon.is.migration.service.v530.SQLConstants;
import org.wso2.carbon.is.migration.service.v530.bean.Claim;
import org.wso2.carbon.is.migration.service.v530.bean.Dialect;
import org.wso2.carbon.is.migration.service.v530.bean.MappedAttribute;
import org.wso2.carbon.is.migration.service.v540.util.FileBasedClaimBuilder;
import org.wso2.carbon.is.migration.util.Constant;
import org.wso2.carbon.is.migration.util.Utility;
import org.wso2.carbon.user.api.Tenant;
import org.wso2.carbon.user.api.UserRealm;
import org.wso2.carbon.user.api.UserStoreException;
import org.wso2.carbon.user.core.UserCoreConstants;
import org.wso2.carbon.user.core.claim.inmemory.ClaimConfig;

import javax.xml.stream.XMLStreamException;
import java.io.FileNotFoundException;
import java.io.IOException;
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

import static org.wso2.carbon.is.migration.util.Constant.MIGRATION_LOG;
import static org.wso2.carbon.is.migration.util.Constant.SUPER_TENANT_ID;

public class ClaimDataMigrator extends Migrator{

    private static Log log = LogFactory.getLog(ClaimDataMigrator.class);
    //Is validation success. If not success, it will be created additional calims to be success
    private boolean isSuccess = true;
    //This is used to record error log
    private int count = 1;

    private static final String CLAIM_CONFIG = "claim-config.xml";

    private ClaimConfig claimConfig;

    private ClaimDialectDAO claimDialectDAO = new ClaimDialectDAO();

    private CacheBackedLocalClaimDAO localClaimDAO = new CacheBackedLocalClaimDAO(new LocalClaimDAO());

    @Override
    public void migrate() throws MigrationClientException {
        migrateClaimData();
    }


    public boolean migrateClaimData() throws MigrationClientException {
        List<Claim> claims = new ArrayList<>();
        Connection umConnection = null;

        PreparedStatement loadDialectsStatement = null;
        PreparedStatement loadMappedAttributeStatement = null;

        ResultSet dialects = null;
        ResultSet claimResultSet = null;
        StringBuilder report = new StringBuilder();
        report.append("---------------------------------- WSO2 Identity Server 5.3.0 claim Migration Report " +
                      "-----------------------------------------\n \n");


        report.append("\n\n------------------------------------------------- Validating Existing Claims" +
                      "----------------------------------------------\n \n");


        try {

            umConnection = getDataSource().getConnection();
            umConnection.setAutoCommit(false);

            //This is used for validating multiple mapped attribute in each dialect. Format : dialectURL-->
            // MappedAttribute---> List of claim URIs. If any of the mapped attribute corresponds to multiple claim
            // URI, the validation should be false.
            Map<String, Map<String, List<String>>> data = new HashMap<>();
            Map<Integer, Dialect> dialectMap = new HashMap<>();

            try {
                loadDialectsStatement = umConnection.prepareStatement(SQLConstants.LOAD_CLAIM_DIALECTS);
                dialects = loadDialectsStatement.executeQuery();

                List<Integer> inactiveTenants = new ArrayList<>();
                if (isIgnoreForInactiveTenants()) {
                    inactiveTenants = Utility.getInactiveTenants();
                }

                processDialects(dialects, dialectMap, inactiveTenants);

            } finally {
                IdentityDatabaseUtil.closeResultSet(dialects);
                IdentityDatabaseUtil.closeStatement(loadDialectsStatement);
            }

            for (Map.Entry<Integer, Dialect> dialectEntry : dialectMap.entrySet()) {

                Dialect dialect = dialectEntry.getValue();
                //Keep the list of claim URI against domain Qualified Mapped Attribute
                Map<String, List<String>> mappedAttributes = new HashMap<>();

                try {
                    loadMappedAttributeStatement = umConnection.prepareStatement(SQLConstants.LOAD_MAPPED_ATTRIBUTE);
                    loadMappedAttributeStatement.setInt(1, dialect.getDialectId());
                    claimResultSet = loadMappedAttributeStatement.executeQuery();

                    //Read all records in UM_CLAIM one by one
                    processClaimResultSet(claims, claimResultSet, mappedAttributes, dialect.getDialectUri(),
                                          dialect.getTenantId());
                } finally {
                    IdentityDatabaseUtil.closeResultSet(claimResultSet);
                    IdentityDatabaseUtil.closeStatement(loadMappedAttributeStatement);
                }

                //get the tenant qualified dialect URL
                getTenantQualifiedDialectUris(data, mappedAttributes, dialect.getDialectUri(), dialect.getTenantId());
            }

            //This is used to keep mapped attributes in each dialect in each tenant.
            // Format is tenantDomain:dialectURL->List of Mapped Attributes. If any remote dialect has a mapped
            // attribute which is not matching to any of the local claim's mapped attribute, the validation should be
            // false.
            Map<String, Map<String, List<String>>> tenantDialectMappedAttributes = new HashMap<>();

            for (Map.Entry<String, Map<String, List<String>>> entry : data.entrySet()) {
                mapAttributesAgainstDialects(report, tenantDialectMappedAttributes, entry);
            }

            //If there is no matching mapped attribute in each tenants remote dialect's claims in relevant tenant's
            // local dialect's claims mapped attributes, it is required to create a new mapping the local dialect
            // this variable is used to keep the new local claim URIs which needs to be added in each tenant. Format
            // is tenantDomain--> List of mappedAttributes
            mapAttributesAgainstTenant(report, tenantDialectMappedAttributes);

        } catch (SQLException e) {
            log.error("Error while validating claim management data", e);
        } finally {
            IdentityDatabaseUtil.closeConnection(umConnection);
        }


        // Migrating claim Data starts here.
        migrateClaimData(claims, report);
        return isSuccess;
    }

    private void processDialects(ResultSet dialects, Map<Integer, Dialect> dialectMap, List<Integer> inactiveTenants)
            throws SQLException {
        while (dialects.next()) {

            int dialectId = dialects.getInt("UM_ID");
            String dialectUri = dialects.getString("UM_DIALECT_URI");
            int tenantId = dialects.getInt("UM_TENANT_ID");

            if (isIgnoreForInactiveTenants() && inactiveTenants.contains(tenantId)) {
                log.info("Inactive tenant : " + tenantId + " , " +
                         "Skipping claim data migration for dialect : " + dialectUri);
                continue;
            }
            Dialect dialect = new Dialect(dialectId, dialectUri, tenantId);
            dialectMap.put(dialectId, dialect);
        }
    }

    private void migrateClaimData(List<Claim> claims, StringBuilder report) {
        ClaimManager claimManager = ClaimManager.getInstance();

        if (claims != null) {

            report.append("\n\n------------------------------------------------------------------------------ Claim " +
                          "Migration ---------------------------------------------------------------------------" +
                          "----\n \n");
            try {
                // Add Claim Dialects
                claimManager.addClaimDialects(claims, report);

                // Add Local Claims.
                claimManager.addLocalClaims(claims, report);
                migrateLocalClaims();
                log.info("end adding local claims");

                // Add External Claims
                claimManager.addExternalClaim(claims, report);
            } catch (MigrationClientException e) {
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
    }

    private void getTenantQualifiedDialectUris(Map<String, Map<String, List<String>>> data,
                                               Map<String, List<String>> mappedAttributes, String dialectUri,
                                               int tenantId) {

        try {
            dialectUri = dialectUri + "@" + IdentityTenantUtil.getTenantDomain(tenantId);
            data.put(dialectUri, mappedAttributes);
        } catch (IdentityRuntimeException e) {
            if (e.getMessage().contains("Can not find the tenant domain for the tenant id")) {
                String errorMessage = "Error while migrating data. " + e.getMessage() + " for tenantId : " +
                                      tenantId;
                log.error(errorMessage);
                if (log.isDebugEnabled()) {
                    log.debug(errorMessage, e);
                }
            }
        }
    }

    private void processClaimResultSet(List<Claim> claims, ResultSet claimResultSet,
                                       Map<String, List<String>> mappedAttributes, String dialectUri, int tenantId)
            throws SQLException {

        try {
            IdentityTenantUtil.getTenantDomain(tenantId);
        } catch (IdentityRuntimeException e) {
            if (e.getMessage().contains("Can not find the tenant domain for the tenant id")) {
                String errorMessage = "Error while migrating data. " + e.getMessage() + " for tenantId : " +
                                      tenantId;
                log.error(errorMessage);
                if (log.isDebugEnabled()) {
                    log.debug(errorMessage, e);
                }
            }
            return;
        }

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

            Claim claimDTO = new Claim(claimURI, displayTag, description, regEx, isSupportedByDefault,
                                       isRequired, displayOrder, isReadOnly, tenantId, dialectUri);
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
    }

    private void mapAttributesAgainstTenant(StringBuilder report,
                                            Map<String, Map<String, List<String>>> tenantDialectMappedAttributes) {

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
                                                      remoteClaimAttribute + " in dialect :" + dialect.getKey() +
                                                      " is not associated to any of the local claim in " +
                                                      "tenant domain: " + tenantDomain);

                                        if (log.isDebugEnabled()) {
                                            log.debug("Mapped Attribute : " + remoteClaimAttribute +
                                                      " in dialect :" + dialect.getKey() +
                                                      " is not associated to any of the local claim in" +
                                                      " tenant domain: " + tenantDomain);
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
    }

    private void mapAttributesAgainstDialects(StringBuilder report,
                                              Map<String, Map<String, List<String>>> tenantDialectMappedAttributes,
                                              Map.Entry<String, Map<String, List<String>>> entry) {

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
                if(mappedAttribute != null) {
                    attributes.add(mappedAttribute.trim());
                    if (claimEntry.getValue() != null && claimEntry.getValue().size() > 1) {
                        isSuccess = false;

                        report.append(count + ")  Duplicate Mapped Attribute found for dialect :" + dialect +
                                      " | Mapped Attribute :" + mappedAttribute + " | " +
                                      "Relevant Claims : " + claimEntry.getValue() + " | Tenant Domain :"
                                      + tenantDomain);
                        report.append("\n\n");
                        if (log.isDebugEnabled()) {
                            log.debug("Duplicate Mapped Attribute found for dialect :" + dialect +
                                      " | Mapped Attribute :" + mappedAttribute + " | " +
                                      "Relevant Claims : " + claimEntry.getValue() + " | Tenant Domain :"
                                      + tenantDomain);
                        }

                        count++;
                    }
                }
            }

            dialectMappedAttributes.put(entry.getKey().replace("@" + tenantDomain, ""), attributes);
            tenantDialectMappedAttributes.put(tenantDomain, dialectMappedAttributes);
        }
    }

    private void migrateLocalClaims() throws MigrationClientException {

        String filePath = Utility.getDataFilePath(CLAIM_CONFIG, getVersionConfig().getVersion());
        try {
            claimConfig = FileBasedClaimBuilder.buildClaimMappingsFromConfigFile(filePath);
        } catch (IOException | XMLStreamException | UserStoreException e) {
            String message = "Error while building claims from config file";
            if (isContinueOnError()) {
                log.error(message, e);
            } else {
                throw new MigrationClientException(message, e);
            }
        }

        if (claimConfig.getClaims().isEmpty()) {
            log.info(MIGRATION_LOG + "No data to migrate related with claim mappings.");
            return;
        }

        try {
            // Migrate super tenant.
            migrateLocalClaimData(SUPER_TENANT_ID);

            // Migrate other tenants.
            Set<Tenant> tenants = Utility.getTenants();
            List<Integer> inactiveTenants = Utility.getInactiveTenants();
            boolean ignoreForInactiveTenants = isIgnoreForInactiveTenants();
            for (Tenant tenant : tenants) {
                int tenantId = tenant.getId();
                if (ignoreForInactiveTenants && inactiveTenants.contains(tenantId)) {
                    log.info("Skipping claim data migration for inactive tenant: " + tenantId);
                    continue;
                }
                migrateLocalClaimData(tenant.getId());
            }
        } catch (UserStoreException | ClaimMetadataException e) {
            String message = "Error while migrating claim data";
            if (isContinueOnError()) {
                log.error(message, e);
            } else {
                throw new MigrationClientException(message, e);
            }
        }
    }

    /**
     * Migrate claim mappings.
     *
     * @param tenantId tenant id
     * @throws UserStoreException     UserStoreException
     * @throws ClaimMetadataException ClaimMetadataException
     */
    private void migrateLocalClaimData(int tenantId) throws UserStoreException, ClaimMetadataException {

        UserRealm realm = ISMigrationServiceDataHolder.getRealmService().getTenantUserRealm(tenantId);
        String primaryDomainName = realm.getRealmConfiguration().getUserStoreProperty(UserCoreConstants.RealmConfig
                .PROPERTY_DOMAIN_NAME);
        if (StringUtils.isBlank(primaryDomainName)) {
            primaryDomainName = UserCoreConstants.PRIMARY_DEFAULT_DOMAIN_NAME;
        }

        Set<String> claimDialects = new HashSet<>();
        for (ClaimDialect claimDialect : claimDialectDAO.getClaimDialects(tenantId)) {
            claimDialects.add(claimDialect.getClaimDialectURI());
        }

        Map<String, org.wso2.carbon.user.core.claim.ClaimMapping> externalClaims = new HashMap<>();
        Set<String> existingLocalClaimURIs = new HashSet<>();

        // Add local claim mappings.
        for (Map.Entry<String, org.wso2.carbon.user.core.claim.ClaimMapping> entry : claimConfig.getClaims()
                .entrySet()) {

            String claimURI = entry.getKey();
            org.wso2.carbon.user.core.claim.ClaimMapping claimMapping = entry.getValue();
            String claimDialectURI = claimMapping.getClaim().getDialectURI();

            if (ClaimConstants.LOCAL_CLAIM_DIALECT_URI.equals(claimDialectURI)) {
                if (existingLocalClaimURIs.isEmpty()) {
                    existingLocalClaimURIs = getExistingLocalClaimURIs(tenantId);
                }
                if (existingLocalClaimURIs.contains(claimURI)) {
                    log.warn(MIGRATION_LOG + "Local claim: " + claimURI + " already exists in the system for" +
                            " tenant: " + tenantId);
                    continue;
                }

                addLocalClaimMapping(tenantId, primaryDomainName, claimURI, claimMapping);
                existingLocalClaimURIs.add(claimURI);
            } else {
                externalClaims.put(entry.getKey(), entry.getValue());
            }
        }
    }

    /**
     * Get existing local claim URIs.
     *
     * @param tenantId tenant id
     * @return existing claim URI set
     * @throws ClaimMetadataException ClaimMetadataException
     */
    private Set<String> getExistingLocalClaimURIs(int tenantId) throws ClaimMetadataException {

        Set<String> localClaimURIs = new HashSet<>();
        for (LocalClaim localClaim : localClaimDAO.getLocalClaims(tenantId)) {
            localClaimURIs.add(localClaim.getClaimURI());
        }
        return localClaimURIs;
    }

    /**
     * Add local claim mapping.
     *
     * @param tenantId          tenant id
     * @param primaryDomainName primary domain name
     * @param claimURI          claim URI
     * @param claimMapping      claim mappings
     * @throws ClaimMetadataException ClaimMetadataException
     */
    private void addLocalClaimMapping(int tenantId, String primaryDomainName, String claimURI, org.wso2.carbon.user
            .core.claim.ClaimMapping claimMapping) throws ClaimMetadataException {

        List<AttributeMapping> mappedAttributes = new ArrayList<>();
        if (StringUtils.isNotBlank(claimMapping.getMappedAttribute())) {
            mappedAttributes.add(new AttributeMapping(primaryDomainName, claimMapping.getMappedAttribute()));
        }
        if (claimMapping.getMappedAttributes() != null) {
            for (Map.Entry<String, String> claimMappingEntry : claimMapping.getMappedAttributes().entrySet()) {
                mappedAttributes.add(new AttributeMapping(claimMappingEntry.getKey(), claimMappingEntry.getValue()));
            }
        }

        Map<String, String> claimProperties = claimConfig.getPropertyHolder().get(claimURI);
        claimProperties.remove(ClaimConstants.DIALECT_PROPERTY);
        claimProperties.remove(ClaimConstants.CLAIM_URI_PROPERTY);
        claimProperties.remove(ClaimConstants.ATTRIBUTE_ID_PROPERTY);

        if (!claimProperties.containsKey(ClaimConstants.DISPLAY_NAME_PROPERTY)) {
            claimProperties.put(ClaimConstants.DISPLAY_NAME_PROPERTY, "0");
        }
        if (claimProperties.containsKey(ClaimConstants.SUPPORTED_BY_DEFAULT_PROPERTY) &&
                StringUtils.isBlank(claimProperties.get(ClaimConstants.SUPPORTED_BY_DEFAULT_PROPERTY))) {
            claimProperties.put(ClaimConstants.SUPPORTED_BY_DEFAULT_PROPERTY, "true");
        }
        if (claimProperties.containsKey(ClaimConstants.READ_ONLY_PROPERTY) &&
                StringUtils.isBlank(claimProperties.get(ClaimConstants.READ_ONLY_PROPERTY))) {
            claimProperties.put(ClaimConstants.READ_ONLY_PROPERTY, "true");
        }
        if (claimProperties.containsKey(ClaimConstants.REQUIRED_PROPERTY) &&
                StringUtils.isBlank(claimProperties.get(ClaimConstants.REQUIRED_PROPERTY))) {
            claimProperties.put(ClaimConstants.REQUIRED_PROPERTY, "true");
        }

        LocalClaim localClaim = new LocalClaim(claimURI, mappedAttributes, claimProperties);
        localClaimDAO.addLocalClaim(localClaim, tenantId);
    }
}

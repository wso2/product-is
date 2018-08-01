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
import org.wso2.carbon.identity.claim.metadata.mgt.dao.CacheBackedExternalClaimDAO;
import org.wso2.carbon.identity.claim.metadata.mgt.dao.CacheBackedLocalClaimDAO;
import org.wso2.carbon.identity.claim.metadata.mgt.dao.ClaimDialectDAO;
import org.wso2.carbon.identity.claim.metadata.mgt.dao.ExternalClaimDAO;
import org.wso2.carbon.identity.claim.metadata.mgt.dao.LocalClaimDAO;
import org.wso2.carbon.identity.claim.metadata.mgt.exception.ClaimMetadataException;
import org.wso2.carbon.identity.claim.metadata.mgt.model.AttributeMapping;
import org.wso2.carbon.identity.claim.metadata.mgt.model.ClaimDialect;
import org.wso2.carbon.identity.claim.metadata.mgt.model.ExternalClaim;
import org.wso2.carbon.identity.claim.metadata.mgt.model.LocalClaim;
import org.wso2.carbon.identity.claim.metadata.mgt.util.ClaimConstants;
import org.wso2.carbon.identity.core.migrate.MigrationClientException;
import org.wso2.carbon.is.migration.internal.ISMigrationServiceDataHolder;
import org.wso2.carbon.is.migration.service.Migrator;
import org.wso2.carbon.is.migration.service.v540.util.FileBasedClaimBuilder;
import org.wso2.carbon.is.migration.util.Constant;
import org.wso2.carbon.is.migration.util.Utility;
import org.wso2.carbon.user.api.Tenant;
import org.wso2.carbon.user.api.UserRealm;
import org.wso2.carbon.user.api.UserStoreException;
import org.wso2.carbon.user.core.UserCoreConstants;
import org.wso2.carbon.user.core.claim.inmemory.ClaimConfig;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import javax.xml.stream.XMLStreamException;

/**
 * This class handles the claim data migration.
 */
public class ClaimDataMigrator extends Migrator {

    private static final Log log = LogFactory.getLog(ClaimDataMigrator.class);

    private static final String CLAIM_CONFIG = "claim-config.xml";

    private ClaimConfig claimConfig;

    private CacheBackedLocalClaimDAO localClaimDAO = new CacheBackedLocalClaimDAO(new LocalClaimDAO());

    private CacheBackedExternalClaimDAO externalClaimDAO = new CacheBackedExternalClaimDAO(new ExternalClaimDAO());

    @Override
    public void migrate() throws MigrationClientException {

        String filePath = Utility.getDataFilePath(CLAIM_CONFIG, getVersionConfig().getVersion());
        try {
            claimConfig = FileBasedClaimBuilder.buildClaimMappingsFromConfigFile(filePath);
            if (claimConfig.getClaims().isEmpty()) {
                log.info(Constant.MIGRATION_LOG + "No data to migrate related with claim mappings.");
                return;
            }
        } catch (IOException | XMLStreamException | UserStoreException e) {
            String message = "Error while building claims from config file";
            if (isContinueOnError()) {
                log.error(message, e);
            } else {
                throw new MigrationClientException(message, e);
            }
        }

        try {
            // Migrate super tenant.
            migrateClaimData(Constant.SUPER_TENANT_ID);

            // Migrate other tenants.
            Set<Tenant> tenants = Utility.getTenants();
            List<Integer> inactiveTenants = Utility.getInactiveTenants();
            boolean ignoreForInactiveTenants = isIgnoreForInactiveTenants();
            Set<Integer> tenantRangeID = new HashSet<>();
            if (Utility.isMigrateTenantRange()) {
                for (Tenant tenant : Utility.getTenants()) {
                    tenantRangeID.add(tenant.getId());
                }
            }
            for (Tenant tenant : tenants) {
                int tenantId = tenant.getId();
                if (ignoreForInactiveTenants && inactiveTenants.contains(tenantId)) {
                    log.info("Skipping claim data migration for Inactive tenant : " + tenantId);
                    continue;
                }
                if (Utility.isMigrateTenantRange() && !tenantRangeID.contains(tenantId)) {
                    log.info("Tenant " + tenantId + " is not in range, Skipping claim data migration.");
                    continue;
                }
                migrateClaimData(tenant.getId());
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
    private void migrateClaimData(int tenantId) throws UserStoreException, ClaimMetadataException {

        ClaimDialectDAO claimDialectDAO = new ClaimDialectDAO();
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
                    log.warn(Constant.MIGRATION_LOG + "Local claim: " + claimURI + " already exists in the system for" +
                            " tenant: " + tenantId);
                    continue;
                }

                addLocalClaimMapping(tenantId, primaryDomainName, claimURI, claimMapping);
                existingLocalClaimURIs.add(claimURI);
            } else {
                externalClaims.put(entry.getKey(), entry.getValue());
            }
        }

        Map<String, Set<String>> existingExternalClaimURIs = new HashMap<>();
        // Add external claim mappings.
        for (Map.Entry<String, org.wso2.carbon.user.core.claim.ClaimMapping> entry : externalClaims.entrySet()) {

            String claimURI = entry.getKey();
            String claimDialectURI = entry.getValue().getClaim().getDialectURI();
            if (!claimDialects.contains(claimDialectURI)) {
                claimDialectDAO.addClaimDialect(new ClaimDialect(claimDialectURI), tenantId);
                claimDialects.add(claimDialectURI);
                existingExternalClaimURIs.put(claimDialectURI, new HashSet<String>());
            }

            if (existingExternalClaimURIs.get(claimDialectURI) == null) {
                existingExternalClaimURIs.put(claimDialectURI, getExistingExternalClaimURIs(tenantId, claimDialectURI));
            }
            if (existingExternalClaimURIs.get(claimDialectURI).contains(claimURI)) {
                log.warn(Constant.MIGRATION_LOG + "External claim: " + claimURI + " already exists in the system for" +
                        " dialect: " + claimDialectURI + " in tenant: " + tenantId);
                continue;
            }

            addExternalClaimMapping(tenantId, claimURI, claimDialectURI);
            existingExternalClaimURIs.get(claimDialectURI).add(claimURI);
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
     * Get existing external claim URIs.
     *
     * @param tenantId        tenant id
     * @param claimDialectURI claim dialect URI
     * @return existing external claim URIs
     * @throws ClaimMetadataException ClaimMetadataException
     */
    private Set<String> getExistingExternalClaimURIs(int tenantId, String claimDialectURI)
            throws ClaimMetadataException {

        Set<String> externalClaimURIs = new HashSet<>();
        for (ExternalClaim externalClaim : externalClaimDAO.getExternalClaims(claimDialectURI, tenantId)) {
            externalClaimURIs.add(externalClaim.getClaimURI());
        }
        return externalClaimURIs;
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

    /**
     * Add external claim mapping.
     *
     * @param tenantId        tenant id
     * @param claimURI        claim URI
     * @param claimDialectURI claim dialect URI
     * @throws ClaimMetadataException ClaimMetadataException
     */
    private void addExternalClaimMapping(int tenantId, String claimURI, String claimDialectURI) throws
            ClaimMetadataException {

        String mappedLocalClaimURI = claimConfig.getPropertyHolder().get(claimURI)
                .get(ClaimConstants.MAPPED_LOCAL_CLAIM_PROPERTY);
        ExternalClaim externalClaim = new ExternalClaim(claimDialectURI, claimURI, mappedLocalClaimURI);
        externalClaimDAO.addExternalClaim(externalClaim, tenantId);
    }
}

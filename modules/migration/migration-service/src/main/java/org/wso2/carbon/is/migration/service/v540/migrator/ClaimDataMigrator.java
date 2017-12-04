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
import org.wso2.carbon.identity.claim.metadata.mgt.internal.IdentityClaimManagementServiceDataHolder;
import org.wso2.carbon.identity.claim.metadata.mgt.model.AttributeMapping;
import org.wso2.carbon.identity.claim.metadata.mgt.model.ClaimDialect;
import org.wso2.carbon.identity.claim.metadata.mgt.model.ExternalClaim;
import org.wso2.carbon.identity.claim.metadata.mgt.model.LocalClaim;
import org.wso2.carbon.identity.claim.metadata.mgt.util.ClaimConstants;
import org.wso2.carbon.identity.claim.metadata.mgt.util.ClaimMetadataUtils;
import org.wso2.carbon.identity.core.migrate.MigrationClientException;
import org.wso2.carbon.is.migration.internal.ISMigrationServiceDataHolder;
import org.wso2.carbon.is.migration.service.Migrator;
import org.wso2.carbon.is.migration.service.v540.util.FileBasedClaimBuilder;
import org.wso2.carbon.is.migration.util.Constant;
import org.wso2.carbon.is.migration.util.Utility;
import org.wso2.carbon.user.api.Claim;
import org.wso2.carbon.user.api.ClaimMapping;
import org.wso2.carbon.user.api.Tenant;
import org.wso2.carbon.user.api.UserRealm;
import org.wso2.carbon.user.api.UserStoreException;
import org.wso2.carbon.user.core.UserCoreConstants;
import org.wso2.carbon.user.core.claim.inmemory.ClaimConfig;
import org.wso2.carbon.user.core.listener.ClaimManagerListener;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import javax.xml.stream.XMLStreamException;

/**
 * Migrator implementation for Claim Data
 */
public class ClaimDataMigrator extends Migrator {

    private static final String CLAIM_CONFIG = "claim-config.xml";
    private static final Log log = LogFactory.getLog(ClaimDataMigrator.class);
    private ClaimConfig claimConfig;
    private int tenantId;
    private ClaimDialectDAO claimDialectDAO = new ClaimDialectDAO();
    private CacheBackedLocalClaimDAO localClaimDAO = new CacheBackedLocalClaimDAO(new LocalClaimDAO());
    private CacheBackedExternalClaimDAO externalClaimDAO = new CacheBackedExternalClaimDAO(new ExternalClaimDAO());

    @Override
    public void migrate() throws MigrationClientException {
        try {
            String filePath = Utility.getDataFilePath(CLAIM_CONFIG, getVersionConfig().getVersion());
            try {
                claimConfig = FileBasedClaimBuilder.buildClaimMappingsFromConfigFile(filePath);
            } catch (IOException e) {
                log.error("Error while building claims from config file", e);
            } catch (XMLStreamException e) {
                log.error("Error while building claims from config file", e);
            }

            this.tenantId = Constant.SUPER_TENANT_ID;
            migrateClaimData(Constant.SUPER_TENANT_ID);

            List<Tenant> tenants = Utility.getTenants();
            for (Tenant tenant : tenants) {
                this.tenantId = tenant.getId();
                migrateClaimData(tenant.getId());
            }
        } catch (UserStoreException e) {
            log.error("Error while migrating claim data", e);
        } catch (ClaimMetadataException e) {
            log.error("Error while migrating claim data", e);
        }
    }

    public void addNewClaimMapping(ClaimMapping claimMapping) throws UserStoreException {
        throw new UnsupportedOperationException("ClaimMetadataStore does not supports management operations");
    }

    
    
    public void deleteClaimMapping(ClaimMapping claimMapping) throws UserStoreException {
        throw new UnsupportedOperationException("ClaimMetadataStore does not supports management operations");
    }

    
    
    public ClaimMapping[] getAllClaimMappings(String dialectUri) throws UserStoreException {

        if (ClaimConstants.LOCAL_CLAIM_DIALECT_URI.equalsIgnoreCase(dialectUri)) {
            try {
                List<LocalClaim> localClaims = localClaimDAO.getLocalClaims(this.tenantId);

                List<ClaimMapping> claimMappings = new ArrayList<>();

                for (LocalClaim localClaim : localClaims) {
                    ClaimMapping claimMapping = ClaimMetadataUtils.convertLocalClaimToClaimMapping(localClaim,
                                                                                                   this.tenantId);
                    claimMappings.add(claimMapping);
                }

                return claimMappings.toArray(new ClaimMapping[0]);
            } catch (ClaimMetadataException e) {
                throw new UserStoreException(e.getMessage(), e);
            }
        } else {
            try {
                List<ExternalClaim> externalClaims = externalClaimDAO.getExternalClaims(dialectUri, this.tenantId);
                List<LocalClaim> localClaims = localClaimDAO.getLocalClaims(this.tenantId);

                List<ClaimMapping> claimMappings = new ArrayList<>();

                for (ExternalClaim externalClaim : externalClaims) {
                    ClaimMapping claimMapping =
                            ClaimMetadataUtils.convertExternalClaimToClaimMapping(externalClaim, localClaims,
                                                                                  this.tenantId);
                    claimMappings.add(claimMapping);
                }

                return claimMappings.toArray(new ClaimMapping[0]);
            } catch (ClaimMetadataException e) {
                throw new UserStoreException(e.getMessage(), e);
            }
        }
    }

    
    
    public ClaimMapping[] getAllClaimMappings() throws UserStoreException {

        return getAllClaimMappings(ClaimConstants.LOCAL_CLAIM_DIALECT_URI);
    }

    
    public String[] getAllClaimUris() throws UserStoreException {

        String[] localClaims;

        for (ClaimManagerListener listener : IdentityClaimManagementServiceDataHolder.getClaimManagerListeners()) {
            if (!listener.getAllClaimUris()) {
                return null;
            }
        }

        try {

            List<LocalClaim> localClaimList = this.localClaimDAO.getLocalClaims(tenantId);

            localClaims = new String[localClaimList.size()];

            int i = 0;
            for (LocalClaim localClaim : localClaimList) {
                localClaims[i] = localClaim.getClaimURI();
                i++;
            }
        } catch (ClaimMetadataException e) {
            throw new UserStoreException(e.getMessage(), e);
        }


        return localClaims;
    }

    
    
    public ClaimMapping[] getAllRequiredClaimMappings() throws UserStoreException {

        try {
            List<LocalClaim> localClaims = localClaimDAO.getLocalClaims(this.tenantId);

            List<ClaimMapping> claimMappings = new ArrayList<>();

            for (LocalClaim localClaim : localClaims) {
                ClaimMapping claimMapping = ClaimMetadataUtils.convertLocalClaimToClaimMapping(localClaim,
                                                                                               this.tenantId);

                if (claimMapping.getClaim().isRequired()) {
                    claimMappings.add(claimMapping);
                }
            }

            return claimMappings.toArray(new ClaimMapping[0]);
        } catch (ClaimMetadataException e) {
            throw new UserStoreException(e.getMessage(), e);
        }
    }

    
    
    public ClaimMapping[] getAllSupportClaimMappingsByDefault() throws UserStoreException {

        try {
            List<LocalClaim> localClaims = localClaimDAO.getLocalClaims(this.tenantId);

            List<ClaimMapping> claimMappings = new ArrayList<>();

            for (LocalClaim localClaim : localClaims) {
                ClaimMapping claimMapping = ClaimMetadataUtils.convertLocalClaimToClaimMapping(localClaim,
                                                                                               this.tenantId);

                if (claimMapping.getClaim().isSupportedByDefault()) {
                    claimMappings.add(claimMapping);
                }
            }

            return claimMappings.toArray(new ClaimMapping[0]);
        } catch (ClaimMetadataException e) {
            throw new UserStoreException(e.getMessage(), e);
        }
    }

    
    public String getAttributeName(String domainName, String claimURI) throws UserStoreException {

        if (StringUtils.isBlank(domainName)) {
            throw new IllegalArgumentException("User store domain name parameter cannot be empty");
        }

        if (StringUtils.isBlank(claimURI)) {
            throw new IllegalArgumentException("Local claim URI parameter cannot be empty");
        }


        for (ClaimManagerListener listener : IdentityClaimManagementServiceDataHolder.getClaimManagerListeners()) {
            if (!listener.getAttributeName(domainName, claimURI)) {
                return null;
            }
        }

        try {

            List<LocalClaim> localClaimList = this.localClaimDAO.getLocalClaims(tenantId);

            for (LocalClaim localClaim : localClaimList) {
                if (localClaim.getClaimURI().equalsIgnoreCase(claimURI)) {
                    return getMappedAttribute(domainName, localClaim, tenantId);
                }
            }


            // For backward compatibility
            List<ClaimDialect> claimDialects = claimDialectDAO.getClaimDialects(tenantId);

            for (ClaimDialect claimDialect : claimDialects) {
                if (ClaimConstants.LOCAL_CLAIM_DIALECT_URI.equalsIgnoreCase(claimDialect.getClaimDialectURI())) {
                    continue;
                }

                List<ExternalClaim> externalClaims =
                        externalClaimDAO.getExternalClaims(claimDialect.getClaimDialectURI(), tenantId);

                for (ExternalClaim externalClaim : externalClaims) {
                    if (externalClaim.getClaimURI().equalsIgnoreCase(claimURI)) {

                        for (LocalClaim localClaim : localClaimList) {
                            if (localClaim.getClaimURI().equalsIgnoreCase(externalClaim.getMappedLocalClaim())) {
                                if (log.isDebugEnabled()) {
                                    log.debug("Picking mapped attribute for external claim : " +
                                              externalClaim.getClaimURI() + " using mapped local claim : " +
                                              localClaim.getClaimURI());
                                }
                                return getMappedAttribute(domainName, localClaim, tenantId);
                            }
                        }
                    }
                }
            }

            if (log.isDebugEnabled()) {
                log.error("Returning NULL for getAttributeName() for domain : " + domainName + ", claim URI : " +
                          claimURI);
            }

            return null;
        } catch (ClaimMetadataException e) {
            throw new UserStoreException(e.getMessage(), e);
        }
    }

    
    
    public String getAttributeName(String claimURI) throws UserStoreException {

        UserRealm realm = IdentityClaimManagementServiceDataHolder.getInstance().getRealmService()
                .getTenantUserRealm(tenantId);
        String primaryDomainName = realm.getRealmConfiguration().getUserStoreProperty
                (UserCoreConstants.RealmConfig.PROPERTY_DOMAIN_NAME);
        return getAttributeName(primaryDomainName, claimURI);
    }

    
    
    public Claim getClaim(String claimURI) throws UserStoreException {
        try {
            List<LocalClaim> localClaims = localClaimDAO.getLocalClaims(this.tenantId);

            for (LocalClaim localClaim : localClaims) {
                if (localClaim.getClaimURI().equalsIgnoreCase(claimURI)) {
                    ClaimMapping claimMapping = ClaimMetadataUtils.convertLocalClaimToClaimMapping(localClaim,
                                                                                                   this.tenantId);
                    return claimMapping.getClaim();
                }
            }

            // For backward compatibility
            List<ClaimDialect> claimDialects = claimDialectDAO.getClaimDialects(tenantId);

            for (ClaimDialect claimDialect : claimDialects) {
                if (ClaimConstants.LOCAL_CLAIM_DIALECT_URI.equalsIgnoreCase(claimDialect.getClaimDialectURI())) {
                    continue;
                }

                List<ExternalClaim> externalClaims =
                        externalClaimDAO.getExternalClaims(claimDialect.getClaimDialectURI(), tenantId);

                for (ExternalClaim externalClaim : externalClaims) {
                    if (externalClaim.getClaimURI().equalsIgnoreCase(claimURI)) {

                        for (LocalClaim localClaim : localClaims) {
                            ClaimMapping claimMapping =
                                    ClaimMetadataUtils.convertLocalClaimToClaimMapping(localClaim, this.tenantId);
                            return claimMapping.getClaim();
                        }
                    }
                }
            }

            log.error("Returning NULL for getClaim() for claim URI : " + claimURI);
            return null;
        } catch (ClaimMetadataException e) {
            throw new UserStoreException(e.getMessage(), e);
        }
    }

    
    
    public ClaimMapping getClaimMapping(String claimURI) throws UserStoreException {
        try {
            List<LocalClaim> localClaims = localClaimDAO.getLocalClaims(this.tenantId);

            for (LocalClaim localClaim : localClaims) {
                if (localClaim.getClaimURI().equalsIgnoreCase(claimURI)) {
                    ClaimMapping claimMapping =
                            ClaimMetadataUtils.convertLocalClaimToClaimMapping(localClaim, this.tenantId);
                    return claimMapping;
                }
            }

            // For backward compatibility
            List<ClaimDialect> claimDialects = claimDialectDAO.getClaimDialects(tenantId);

            for (ClaimDialect claimDialect : claimDialects) {
                if (ClaimConstants.LOCAL_CLAIM_DIALECT_URI.equalsIgnoreCase(claimDialect.getClaimDialectURI())) {
                    continue;
                }

                List<ExternalClaim> externalClaims =
                        externalClaimDAO.getExternalClaims(claimDialect.getClaimDialectURI(), tenantId);

                for (ExternalClaim externalClaim : externalClaims) {
                    if (externalClaim.getClaimURI().equalsIgnoreCase(claimURI)) {

                        for (LocalClaim localClaim : localClaims) {
                            if (localClaim.getClaimURI().equalsIgnoreCase(externalClaim.getMappedLocalClaim())) {
                                ClaimMapping claimMapping =
                                        ClaimMetadataUtils.convertLocalClaimToClaimMapping(localClaim, this.tenantId);
                                return claimMapping;
                            }
                        }
                    }
                }
            }

            if (log.isDebugEnabled()) {
                log.debug("Returning NULL for getClaimMapping() for claim URI : " + claimURI);
            }
            return null;
        } catch (ClaimMetadataException e) {
            throw new UserStoreException(e.getMessage(), e);
        }
    }


    public void migrateClaimData(int tenantId) throws UserStoreException, ClaimMetadataException {

        if (claimConfig.getClaims() != null) {

            // Adding local claims
            String primaryDomainName = UserCoreConstants.PRIMARY_DEFAULT_DOMAIN_NAME;
            UserRealm realm;
            try {

                realm = ISMigrationServiceDataHolder.getRealmService().getTenantUserRealm(tenantId);
                primaryDomainName = realm.getRealmConfiguration().getUserStoreProperty
                        (UserCoreConstants.RealmConfig.PROPERTY_DOMAIN_NAME);
            } catch (Exception e) {
                log.error("Error while retrieving primary userstore domain name, ", e);
                if (isContinueOnError()) {
                    throw e;
                }
                return;
            }


            // Adding external dialects and claims
            Set<String> claimDialectList = new HashSet<>();

            for (Map.Entry<String, org.wso2.carbon.user.core.claim.ClaimMapping> entry : claimConfig.getClaims()
                    .entrySet()) {

                String claimURI = entry.getKey();
                org.wso2.carbon.user.core.claim.ClaimMapping claimMapping = entry.getValue();
                String claimDialectURI = claimMapping.getClaim().getDialectURI();

                if (ClaimConstants.LOCAL_CLAIM_DIALECT_URI.equalsIgnoreCase(claimDialectURI)) {

                    List<AttributeMapping> mappedAttributes = new ArrayList<>();
                    if (StringUtils.isNotBlank(claimMapping.getMappedAttribute())) {
                        mappedAttributes
                                .add(new AttributeMapping(primaryDomainName, claimMapping.getMappedAttribute()));
                    }

                    if (claimMapping.getMappedAttributes() != null) {
                        for (Map.Entry<String, String> claimMappingEntry :
                                claimMapping.getMappedAttributes().entrySet()) {
                            mappedAttributes.add(new AttributeMapping(claimMappingEntry.getKey(),
                                                                      claimMappingEntry.getValue()));
                        }
                    }

                    Map<String, String> claimProperties = claimConfig.getPropertyHolder().get(claimURI);
                    claimProperties.remove(ClaimConstants.DIALECT_PROPERTY);
                    claimProperties.remove(ClaimConstants.CLAIM_URI_PROPERTY);
                    claimProperties.remove(ClaimConstants.ATTRIBUTE_ID_PROPERTY);

                    if (!claimProperties.containsKey(ClaimConstants.DISPLAY_NAME_PROPERTY)) {
                        claimProperties.put(ClaimConstants.DISPLAY_NAME_PROPERTY, "0");
                    }

                    if (claimProperties.containsKey(ClaimConstants.SUPPORTED_BY_DEFAULT_PROPERTY)) {
                        if (StringUtils.isBlank(claimProperties.get(ClaimConstants.SUPPORTED_BY_DEFAULT_PROPERTY))) {
                            claimProperties.put(ClaimConstants.SUPPORTED_BY_DEFAULT_PROPERTY, "true");
                        }
                    }

                    if (claimProperties.containsKey(ClaimConstants.READ_ONLY_PROPERTY)) {
                        if (StringUtils.isBlank(claimProperties.get(ClaimConstants.READ_ONLY_PROPERTY))) {
                            claimProperties.put(ClaimConstants.READ_ONLY_PROPERTY, "true");
                        }
                    }

                    if (claimProperties.containsKey(ClaimConstants.REQUIRED_PROPERTY)) {
                        if (StringUtils.isBlank(claimProperties.get(ClaimConstants.REQUIRED_PROPERTY))) {
                            claimProperties.put(ClaimConstants.REQUIRED_PROPERTY, "true");
                        }
                    }

                    LocalClaim localClaim = new LocalClaim(claimURI, mappedAttributes, claimProperties);

                    try {
                        localClaimDAO.addLocalClaim(localClaim, tenantId);
                    } catch (Exception e) {
                        log.error("Error while adding local claim " + claimURI, e);
                        if (!isContinueOnError()) {
                            throw e;
                        }
                    }
                } else {
                    claimDialectList.add(claimDialectURI);
                }
            }

            // Add external claim dialects
            for (String claimDialectURI : claimDialectList) {

                ClaimDialect claimDialect = new ClaimDialect(claimDialectURI);
                try {
                    claimDialectDAO.addClaimDialect(claimDialect, tenantId);
                } catch (ClaimMetadataException e) {
                    log.error("Error while adding claim dialect " + claimDialectURI, e);
                    if (!isContinueOnError()) {
                        throw e;
                    }
                }
            }

            for (Map.Entry<String, org.wso2.carbon.user.core.claim.ClaimMapping> entry :
                    claimConfig.getClaims().entrySet()) {

                String claimURI = entry.getKey();
                String claimDialectURI = entry.getValue().getClaim().getDialectURI();

                if (!ClaimConstants.LOCAL_CLAIM_DIALECT_URI.equalsIgnoreCase(claimDialectURI)) {

                    String mappedLocalClaimURI = claimConfig.getPropertyHolder().get(claimURI)
                                                            .get(ClaimConstants.MAPPED_LOCAL_CLAIM_PROPERTY);
                    ExternalClaim externalClaim = new ExternalClaim(claimDialectURI, claimURI, mappedLocalClaimURI);

                    try {
                        externalClaimDAO.addExternalClaim(externalClaim, tenantId);
                    } catch (ClaimMetadataException e) {
                        log.error("Error while adding external claim " + claimURI + " to dialect " + claimDialectURI,
                                  e);
                        if (!isContinueOnError()) {
                            throw e;
                        }
                    }
                }
            }
        }
    }

    
    
    public void updateClaimMapping(ClaimMapping claimMapping) throws UserStoreException {
        throw new UnsupportedOperationException("ClaimMetadataStore does not supports management operations");
    }

    private String getMappedAttribute(String domainName, LocalClaim localClaim, int tenantId)
            throws UserStoreException {

        String mappedAttribute = localClaim.getMappedAttribute(domainName);

        if (StringUtils.isNotBlank(mappedAttribute)) {
            if (log.isDebugEnabled()) {
                log.debug("Assigned mapped attribute : " + mappedAttribute + " from user store domain : " + domainName +
                          " for claim : " + localClaim.getClaimURI() + " in tenant : " + tenantId);
            }

            return mappedAttribute;
        }

        mappedAttribute = localClaim.getClaimProperty(ClaimConstants.DEFAULT_ATTRIBUTE);

        if (StringUtils.isNotBlank(mappedAttribute)) {

            if (log.isDebugEnabled()) {
                log.debug(
                        "Assigned mapped attribute : " + mappedAttribute + " from " + ClaimConstants.DEFAULT_ATTRIBUTE +
                        " property for claim : " + localClaim.getClaimURI() + " in tenant : " + tenantId);
            }

            return mappedAttribute;
        }

        UserRealm realm = IdentityClaimManagementServiceDataHolder.getInstance().getRealmService()
                                                                  .getTenantUserRealm(tenantId);
        String primaryDomainName = realm.getRealmConfiguration().getUserStoreProperty
                (UserCoreConstants.RealmConfig.PROPERTY_DOMAIN_NAME);
        mappedAttribute = localClaim.getMappedAttribute(primaryDomainName);

        if (StringUtils.isNotBlank(mappedAttribute)) {
            if (log.isDebugEnabled()) {
                log.debug("Assigned mapped attribute : " + mappedAttribute + " from primary user store domain : " +
                          primaryDomainName + " for claim : " + localClaim.getClaimURI() + " in tenant : " + tenantId);
            }

            return mappedAttribute;
        } else {
            throw new IllegalStateException("Cannot find suitable mapped attribute for local claim " +
                                            localClaim.getClaimURI());
        }
    }

}

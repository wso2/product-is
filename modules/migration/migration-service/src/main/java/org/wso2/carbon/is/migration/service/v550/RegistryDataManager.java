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
package org.wso2.carbon.is.migration.service.v550;

import org.apache.commons.lang.StringUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.wso2.carbon.context.PrivilegedCarbonContext;
import org.wso2.carbon.context.RegistryType;
import org.wso2.carbon.core.util.CryptoException;
import org.wso2.carbon.core.util.CryptoUtil;
import org.wso2.carbon.identity.base.IdentityException;
import org.wso2.carbon.identity.core.util.IdentityTenantUtil;
import org.wso2.carbon.is.migration.internal.ISMigrationServiceDataHolder;
import org.wso2.carbon.registry.core.Collection;
import org.wso2.carbon.registry.core.Registry;
import org.wso2.carbon.registry.core.Resource;
import org.wso2.carbon.registry.core.exceptions.RegistryException;
import org.wso2.carbon.user.api.Tenant;
import org.wso2.carbon.user.api.UserStoreException;

import static org.wso2.carbon.base.MultitenantConstants.SUPER_TENANT_DOMAIN_NAME;

public class RegistryDataManager {

    private static final Log log = LogFactory.getLog(RegistryDataManager.class);

    public static final String ENTITLEMENT_POLICY_PUBLISHER = "/repository/identity/entitlement/publisher/";
    public static final String PASSWORD_PROPERTY = "subscriberPassword";

    private static RegistryDataManager instance = new RegistryDataManager();
    private static final String SYSLOG = "/repository/components/org.wso2.carbon.logging/loggers/syslog/SYSLOG_PROPERTIES";
    public static final String PASSWORD = "password";

    private RegistryDataManager(){}

    public static RegistryDataManager getInstance() {
        return instance;
    }

    private void startTenantFlow(Tenant tenant) {

        PrivilegedCarbonContext.startTenantFlow();
        PrivilegedCarbonContext carbonContext = PrivilegedCarbonContext.getThreadLocalCarbonContext();
        carbonContext.setTenantId(tenant.getId());
        carbonContext.setTenantDomain(tenant.getDomain());
    }

    public void migrateSubscriberPassword(boolean migrateActiveTenantsOnly) throws UserStoreException {

        //migrating super tenant configurations
        try {
            migrateSubscriberDataForTenant();
            log.info("Policy Subscribers migrated for tenant : " + SUPER_TENANT_DOMAIN_NAME);
        } catch (Exception e) {
            log.error("Error while migrating Policy Subscribers for tenant : " + SUPER_TENANT_DOMAIN_NAME, e);
        }

        //migrating tenant configurations
        Tenant[] tenants = ISMigrationServiceDataHolder.getRealmService().getTenantManager().getAllTenants();
        for (Tenant tenant : tenants) {
            if (migrateActiveTenantsOnly && !tenant.isActive()) {
                log.info("Tenant " + tenant.getDomain() + " is inactive. Skipping Subscriber migration!");
                continue;
            }
            try {
                startTenantFlow(tenant);
                IdentityTenantUtil.getTenantRegistryLoader().loadTenantRegistry(tenant.getId());
                migrateSubscriberDataForTenant();
                log.info("Subscribers migrated for tenant : " + tenant.getDomain());
            } catch (Exception e) {
                log.error("Error while migrating Subscribers for tenant : " + tenant.getDomain(), e);
            } finally {
                PrivilegedCarbonContext.endTenantFlow();
            }
        }
    }

    /**
     * Method to migrate encrypted password of SYSLOG_PROPERTIES registry resource
     *
     * @param migrateActiveTenantsOnly
     * @throws UserStoreException
     */
    public void migrateSysLogPropertyPassword(boolean migrateActiveTenantsOnly)
            throws UserStoreException, RegistryException, CryptoException, IdentityException {

        Tenant[] tenants = ISMigrationServiceDataHolder.getRealmService().getTenantManager().getAllTenants();
        for (Tenant tenant : tenants) {
            if (migrateActiveTenantsOnly && !tenant.isActive()) {
                log.info("Tenant " + tenant.getDomain() + " is inactive. Skipping SYSLOG_PROPERTIES file migration. ");
                continue;
            }
            try {
                startTenantFlow(tenant);
                IdentityTenantUtil.getTenantRegistryLoader().loadTenantRegistry(tenant.getId());
                IdentityTenantUtil
                        .initializeRegistry(tenant.getId(), IdentityTenantUtil.getTenantDomain(tenant.getId()));
                Registry registry = IdentityTenantUtil.getConfigRegistry(tenant.getId());
                if (registry.resourceExists(SYSLOG)) {
                    try {
                        registry.beginTransaction();
                        Resource resource = registry.get(SYSLOG);
                        String password = resource.getProperty(PASSWORD);
                        if (!CryptoUtil.getDefaultCryptoUtil().base64DecodeAndIsSelfContainedCipherText(password)) {
                            byte[] decryptedPassword = CryptoUtil.getDefaultCryptoUtil()
                                    .base64DecodeAndDecrypt(password, "RSA");
                            String newEncryptedPassword = CryptoUtil.getDefaultCryptoUtil()
                                    .encryptAndBase64Encode(decryptedPassword);
                            resource.setProperty(PASSWORD, newEncryptedPassword);
                        }
                        registry.put(SYSLOG, resource);
                        registry.commitTransaction();
                    } catch (RegistryException e) {
                        registry.rollbackTransaction();
                        log.error("Unable to update the appender", e);
                        throw e;
                    }
                }
            } catch (RegistryException registryException) {
                throw registryException;
            } catch (CryptoException cryptoException) {
                throw cryptoException;
            } catch (IdentityException identityException) {
                throw identityException;
            } finally {
                PrivilegedCarbonContext.endTenantFlow();
            }
        }
    }

    private void migrateSubscriberDataForTenant() throws RegistryException, CryptoException {

        Registry registry = IdentityTenantUtil.getRegistryService().getGovernanceSystemRegistry();

        if (registry.resourceExists(ENTITLEMENT_POLICY_PUBLISHER)) {
            Collection subscriberCollection = (Collection) registry.get(ENTITLEMENT_POLICY_PUBLISHER);

            for (String subscriberPath : subscriberCollection.getChildren()) {
                Resource subscriberResource = registry.get(subscriberPath);
                String encryptedPassword = subscriberResource.getProperty(PASSWORD_PROPERTY);

                if (StringUtils.isNotEmpty(encryptedPassword) && !CryptoUtil.getDefaultCryptoUtil()
                                .base64DecodeAndIsSelfContainedCipherText(encryptedPassword)) {
                    byte[] decryptedPassword = CryptoUtil.getDefaultCryptoUtil()
                            .base64DecodeAndDecrypt(encryptedPassword, "RSA");
                    String newEncryptedPassword = CryptoUtil.getDefaultCryptoUtil()
                            .encryptAndBase64Encode(decryptedPassword);
                    subscriberResource.setProperty(PASSWORD_PROPERTY, newEncryptedPassword);
                    registry.put(subscriberPath, subscriberResource);
                }
            }
        }
    }
}

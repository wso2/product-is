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
package org.wso2.carbon.is.migration.service.v510.migrator;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.wso2.carbon.base.MultitenantConstants;
import org.wso2.carbon.context.PrivilegedCarbonContext;
import org.wso2.carbon.context.RegistryType;
import org.wso2.carbon.identity.core.IdentityRegistryResources;
import org.wso2.carbon.identity.core.migrate.MigrationClientException;
import org.wso2.carbon.identity.core.util.IdentityTenantUtil;
import org.wso2.carbon.is.migration.internal.ISMigrationServiceDataHolder;
import org.wso2.carbon.is.migration.service.Migrator;
import org.wso2.carbon.registry.core.Registry;
import org.wso2.carbon.registry.core.Resource;
import org.wso2.carbon.registry.core.exceptions.RegistryException;
import org.wso2.carbon.registry.core.session.UserRegistry;
import org.wso2.carbon.user.api.Tenant;

import java.util.List;

/**
 * Registry Data Migrator implementation.
 */
public class RegistryDataMigrator extends Migrator {

    private static final String SAMLSSO_ASSERTION_CONSUMER_URL = "SAMLSSOAssertionConsumerURL";
    private static final String LOGOUT_URL = "logoutURL";
    private static final String DEFAULT_CONST = "[default]";


    private static final Log log = LogFactory.getLog(RegistryDataMigrator.class);

    @Override
    public void migrate() throws MigrationClientException {
        migrateRegistryData();
    }

    public void migrateRegistryData() throws MigrationClientException {
        log.info("MIGRATION-LOGS >> Going to start : migrateRegistryData.");
        //migrating super tenant configurations
        try {
            migrateSAMLConfiguration();
            log.info("MIGRATION-LOGS >> SAML Service Provider details are migrated successfully for tenant : " +
                     MultitenantConstants.SUPER_TENANT_DOMAIN_NAME);
        } catch (Exception e) {
            log.error("MIGRATION-ERROR-LOGS-039 >> Error while migrating registry data for tenant : " +
                      MultitenantConstants.SUPER_TENANT_DOMAIN_NAME, e);
            if (!isContinueOnError()) {
                throw new MigrationClientException("Error while executing the migration.", e);
            }
        }

        //migrating tenant configurations
        try {
            Tenant[] tenants = ISMigrationServiceDataHolder.getRealmService().getTenantManager().getAllTenants();
            for (Tenant tenant : tenants) {
                try {
                    PrivilegedCarbonContext.startTenantFlow();
                    PrivilegedCarbonContext carbonContext = PrivilegedCarbonContext
                            .getThreadLocalCarbonContext();
                    carbonContext.setTenantId(tenant.getId());
                    carbonContext.setTenantDomain(tenant.getDomain());

                    IdentityTenantUtil.getTenantRegistryLoader().loadTenantRegistry(tenant.getId());
                    migrateSAMLConfiguration();
                    log.info("MIGRATION-LOGS >> SAML Service Provider details are migrated successfully for tenant : "
                             + tenant.getDomain());
                } catch (Exception e) {
                    log.error("MIGRATION-ERROR-LOGS-040 >> Error while executing the migration.", e);
                    if (!isContinueOnError()) {
                        throw new MigrationClientException("Error while executing the migration.", e);
                    }
                } finally {
                    try {
                        PrivilegedCarbonContext.endTenantFlow();
                    } catch (Exception e) {
                    }
                }
            }
        }catch (Exception e) {
            log.error("MIGRATION-ERROR-LOGS-041 >> Error while migrating registry data " , e);
            if (!isContinueOnError()) {
                throw new MigrationClientException("Error while executing the migration.", e);
            }
        }
        log.info("MIGRATION-LOGS >> Done : migrateRegistryData.");
    }

    private void migrateSAMLConfiguration() throws MigrationClientException {
        log.info("MIGRATION-LOGS >> Going to start : migrateSAMLConfiguration.");
        Registry registry = (UserRegistry) PrivilegedCarbonContext.getThreadLocalCarbonContext()
                .getRegistry(RegistryType.SYSTEM_CONFIGURATION);
        try {
            if (registry.resourceExists(IdentityRegistryResources.SAML_SSO_SERVICE_PROVIDERS)) {
                String[] providers = (String[]) registry.get(
                        IdentityRegistryResources.SAML_SSO_SERVICE_PROVIDERS).getContent();

                if (providers != null) {
                    for (String provider : providers) {
                        Resource resource = registry.get(provider);

                        if (resource.getProperty(IdentityRegistryResources.PROP_SAML_SSO_ASSERTION_CONS_URLS) != null) {
                            List<String> acsUrls =
                                    resource.getPropertyValues(
                                            IdentityRegistryResources.PROP_SAML_SSO_ASSERTION_CONS_URLS);
                            if (acsUrls.size() > 1) {
                                String defaultAcsUrl = null;
                                for (int i = 0; i < acsUrls.size(); i++) {
                                    if (acsUrls.get(i).startsWith(DEFAULT_CONST)) {
                                        defaultAcsUrl = acsUrls.get(i).substring(acsUrls.get(i).indexOf("]") + 1);
                                        acsUrls.set(i, defaultAcsUrl);
                                        break;
                                    }
                                }
                                if (defaultAcsUrl != null) {
                                    resource.setProperty(IdentityRegistryResources.PROP_SAML_SSO_ASSERTION_CONS_URLS,
                                                         acsUrls);
                                    resource.setProperty(
                                            IdentityRegistryResources.PROP_DEFAULT_SAML_SSO_ASSERTION_CONS_URL,
                                            defaultAcsUrl);
                                }
                            } else if (acsUrls.size() == 1) {
                                resource.setProperty(IdentityRegistryResources
                                                             .PROP_DEFAULT_SAML_SSO_ASSERTION_CONS_URL, acsUrls.get(0));
                            }
                        } else if (resource.getProperty(SAMLSSO_ASSERTION_CONSUMER_URL) != null) {
                            String samlssoAssertionConsumerURL = resource.getProperty(SAMLSSO_ASSERTION_CONSUMER_URL);
                            resource.setProperty(IdentityRegistryResources.PROP_SAML_SSO_ASSERTION_CONS_URLS,
                                                 samlssoAssertionConsumerURL);
                            resource.setProperty(IdentityRegistryResources.PROP_DEFAULT_SAML_SSO_ASSERTION_CONS_URL,
                                                 samlssoAssertionConsumerURL);
                            resource.removeProperty(SAMLSSO_ASSERTION_CONSUMER_URL);
                        }

                        String logoutURL = resource.getProperty(LOGOUT_URL);
                        if (logoutURL != null) {
                            resource.setProperty(IdentityRegistryResources.PROP_SAML_SLO_RESPONSE_URL, logoutURL);
                            resource.removeProperty(LOGOUT_URL);
                        }

                        registry.put(resource.getPath(), resource);
                    }
                }
            }
        } catch (RegistryException e) {
            log.error("MIGRATION-ERROR-LOGS-042 >> Error while executing the migration.", e);
            if (!isContinueOnError()) {
                throw new MigrationClientException("Error while executing the migration.", e);
            }
        }
        log.info("MIGRATION-LOGS >> Done : migrateSAMLConfiguration.");
    }
}
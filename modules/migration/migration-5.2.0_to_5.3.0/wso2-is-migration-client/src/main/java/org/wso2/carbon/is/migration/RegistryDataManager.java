/*
 * Copyright (c) 2016, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
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

package org.wso2.carbon.is.migration;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.wso2.carbon.base.MultitenantConstants;
import org.wso2.carbon.context.PrivilegedCarbonContext;
import org.wso2.carbon.context.RegistryType;
import org.wso2.carbon.identity.base.IdentityException;
import org.wso2.carbon.identity.core.util.IdentityTenantUtil;
import org.wso2.carbon.is.migration.client.internal.ISMigrationServiceDataHolder;
import org.wso2.carbon.registry.api.Registry;
import org.wso2.carbon.registry.api.RegistryException;
import org.wso2.carbon.registry.api.Resource;
import org.wso2.carbon.user.api.Tenant;

import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Properties;
import java.util.Set;

public class RegistryDataManager {

    private static RegistryDataManager instance = new RegistryDataManager();

    private static final Log log = LogFactory.getLog(RegistryDataManager.class);

    private static final String EMAIL_TEMPLATE_OLD_REG_LOCATION = "/identity/config/emailTemplate";
    private static final String EMAIL_TEMPLATE_NEW_REG_LOCATION_ROOT = "/identity/Email/";
    private static final Set<String> TEMPLATE_NAMES = new HashSet<String>() {{
        add("accountConfirmation");
        add("accountDisable");
        add("accountEnable");
        add("accountIdRecovery");
        add("accountUnLock");
        add("askPassword");
        add("otp");
        add("passwordReset");
        add("temporaryPassword");
    }};

    private static final Map<String, String> PLACEHOLDERS_MAP = new HashMap<String, String>() {{
        put("\\{first-name\\}", "{{user.claim.givenname}}");
        put("\\{user-name\\}", "{{user-name}}");
        put("\\{confirmation-code\\}", "{{confirmation-code}}");
        put("\\{userstore-domain\\}", "{{userstore-domain}}");
        put("\\{url:user-name\\}", "{{url:user-name}}");
        put("\\{tenant-domain\\}", "{{tenant-domain}}");
        put("\\{temporary-password\\}", "{{temporary-password}}");
    }};

    private RegistryDataManager() {

    }

    public static RegistryDataManager getInstance() {

        return instance;
    }

    public void migrateEmailTemplates() throws Exception {

        //migrating super tenant configurations
        try {
            migrateTenantEmailTemplates();
            log.info("Email templates migrated for tenant : " +
                    MultitenantConstants.SUPER_TENANT_DOMAIN_NAME);
        } catch (Exception e) {
            log.error("Error while migrating email templates for tenant : " +
                    MultitenantConstants.SUPER_TENANT_DOMAIN_NAME, e);
        }

        //migrating tenant configurations
        Tenant[] tenants = ISMigrationServiceDataHolder.getRealmService().getTenantManager().getAllTenants();
        for (Tenant tenant : tenants) {
            try {
                PrivilegedCarbonContext.startTenantFlow();
                PrivilegedCarbonContext carbonContext = PrivilegedCarbonContext
                        .getThreadLocalCarbonContext();
                carbonContext.setTenantId(tenant.getId());
                carbonContext.setTenantDomain(tenant.getDomain());

                IdentityTenantUtil.getTenantRegistryLoader().loadTenantRegistry(tenant.getId());
                migrateTenantEmailTemplates();
                log.info("Email templates migrated for tenant : " + tenant.getDomain());
            } catch (Exception e) {
                log.error("Error while migrating email templates for tenant : " + tenant.getDomain(), e);
            } finally {
                PrivilegedCarbonContext.endTenantFlow();
            }
        }
    }

    private void migrateTenantEmailTemplates() throws IdentityException {

        Registry registry = PrivilegedCarbonContext.getThreadLocalCarbonContext()
                .getRegistry(RegistryType.SYSTEM_CONFIGURATION);
        try {
            if (registry.resourceExists(EMAIL_TEMPLATE_OLD_REG_LOCATION)) {
                Properties properties = registry.get(EMAIL_TEMPLATE_OLD_REG_LOCATION).getProperties();

                for (Map.Entry<Object, Object> entry : properties.entrySet()) {
                    if (!TEMPLATE_NAMES.contains(entry.getKey())) {
                        log.info("Skipping probable invalid template :" + entry.getKey());
                        continue;
                    }
                    String[] templateParts = ((List<String>) entry.getValue()).get(0).split("\\|");
                    if (templateParts.length != 3) {
                        log.warn("Skipping invalid template data. Expected 3 sections, but contains " +
                                templateParts.length);
                    }
                    String newResourcePath =
                            EMAIL_TEMPLATE_NEW_REG_LOCATION_ROOT + entry.getKey().toString().toLowerCase() +
                                    "/en_us";
                    String newContent = String.format("[\"%s\",\"%s\",\"%s\"]",
                            updateContent(templateParts[0]),
                            updateContent(templateParts[1]),
                            updateContent(templateParts[2]));
                    Resource resource;
                    if (registry.resourceExists(newResourcePath)) {
                        resource = registry.get(newResourcePath);
                    } else {
                        resource = registry.newResource();
                        resource.addProperty("display", (String) entry.getKey());
                        resource.addProperty("type", (String) entry.getKey());
                        resource.addProperty("emailContentType", "text/plain");
                        resource.addProperty("locale", "en_US");
                    }
                    resource.setContent(newContent);
                    resource.setMediaType("tag");
                    registry.put(newResourcePath, resource);
                }
            }
        } catch (RegistryException e) {
            throw IdentityException.error("Error while migration registry data", e);
        }
    }

    private String updateContent(String s) {

        //update the placeholders
        for (Map.Entry<String, String> entry : PLACEHOLDERS_MAP.entrySet()) {
            s = s.replaceAll(entry.getKey(), entry.getValue());
        }

        //update the new line
        s = s.replaceAll("\n", "\\\\n");
        return s;
    }
}

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

package org.wso2.carbon.is.migration;

import org.apache.axiom.om.OMElement;
import org.apache.axiom.om.impl.builder.StAXOMBuilder;
import org.apache.commons.lang.StringUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.wso2.carbon.context.PrivilegedCarbonContext;
import org.wso2.carbon.identity.base.IdentityException;
import org.wso2.carbon.identity.core.util.IdentityIOStreamUtils;
import org.wso2.carbon.identity.core.util.IdentityTenantUtil;
import org.wso2.carbon.is.migration.internal.ISMigrationServiceDataHolder;
import org.wso2.carbon.registry.api.Registry;
import org.wso2.carbon.registry.api.RegistryException;
import org.wso2.carbon.registry.api.Resource;
import org.wso2.carbon.user.api.Tenant;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.nio.file.Paths;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import javax.xml.namespace.QName;
import javax.xml.stream.XMLInputFactory;
import javax.xml.stream.XMLStreamException;
import javax.xml.stream.XMLStreamReader;

import static org.wso2.carbon.context.RegistryType.SYSTEM_CONFIGURATION;

public class RegistryDataManager {

    private static RegistryDataManager instance = new RegistryDataManager();
    private static final Log log = LogFactory.getLog(RegistryDataManager.class);
    private static final String SCOPE_RESOURCE_PATH = "/oidc";


    public static RegistryDataManager getInstance() {
        return instance;
    }

    private void startTenantFlow(Tenant tenant) {

        PrivilegedCarbonContext.startTenantFlow();
        PrivilegedCarbonContext carbonContext = PrivilegedCarbonContext.getThreadLocalCarbonContext();
        carbonContext.setTenantId(tenant.getId());
        carbonContext.setTenantDomain(tenant.getDomain());
    }

    public void copyOIDCScopeData(boolean migrateActiveTenantsOnly) throws Exception {

        log.info("--------------- OIDC SCOPE FILE MIGRATION START --------------------");

        Map<String, String> oidcScopes = loadOIDCScopes();

        // since copying oidc-config file for super tenant is handled by the OAuth component we only need to handle
        // this in migrated tenants.
        Tenant[] tenants = ISMigrationServiceDataHolder.getRealmService().getTenantManager().getAllTenants();
        for (Tenant tenant : tenants) {
            if (migrateActiveTenantsOnly && !tenant.isActive()) {
                log.info("Tenant " + tenant.getDomain() + " is inactive. Skipping copying OIDC Scopes Data !!!!");
                continue;
            }
            try {
                startTenantFlow(tenant);
                IdentityTenantUtil.getTenantRegistryLoader().loadTenantRegistry(tenant.getId());
                putOIDCScopeResource(oidcScopes);
            } catch (RegistryException | FileNotFoundException e) {
                log.error("Error while migrating OIDC Scope data for tenant:  " + tenant.getDomain(), e);
            } finally {
                PrivilegedCarbonContext.endTenantFlow();
            }
        }
        log.info("--------------- OIDC SCOPE FILE MIGRATION END --------------------");
    }

    private void putOIDCScopeResource(Map<String, String> scopes) throws RegistryException, FileNotFoundException,
            IdentityException {

        Registry registry = PrivilegedCarbonContext.getThreadLocalCarbonContext().getRegistry(SYSTEM_CONFIGURATION);
        String tenantDomain = PrivilegedCarbonContext.getThreadLocalCarbonContext().getTenantDomain();
        if (!registry.resourceExists(SCOPE_RESOURCE_PATH)) {
            Resource resource = registry.newResource();
            for (Map.Entry<String, String> entry : scopes.entrySet()) {
                resource.setProperty(entry.getKey(), entry.getValue());
            }
            registry.put(SCOPE_RESOURCE_PATH, resource);
            log.info("OIDC Scope file data migrated for tenant:" + tenantDomain);
        } else {
            log.info("OIDC Scope file already exists for tenant:" + tenantDomain);
        }
    }

    private static Map<String, String> loadOIDCScopes() throws FileNotFoundException, IdentityException {

        Map<String, String> scopes = new HashMap<>();
        String carbonHome = System.getProperty("carbon.home");
        String confXml = Paths.get(carbonHome,
                new String[]{"repository", "conf", "identity", "oidc-scope-config.xml"}).toString();
        File configfile = new File(confXml);
        if (!configfile.exists()) {
            String errMsg = "OIDC scope-claim Configuration File is not present at: " + confXml;
            throw new FileNotFoundException(errMsg);
        }

        XMLStreamReader parser = null;
        FileInputStream stream = null;
        try {
            stream = new FileInputStream(configfile);
            parser = XMLInputFactory.newInstance().createXMLStreamReader(stream);
            StAXOMBuilder builder = new StAXOMBuilder(parser);
            OMElement documentElement = builder.getDocumentElement();
            Iterator iterator = documentElement.getChildElements();

            while (iterator.hasNext()) {
                OMElement omElement = (OMElement) iterator.next();
                String configType = omElement.getAttributeValue(new QName("id"));
                scopes.put(configType, loadClaimConfig(omElement));
            }
        } catch (XMLStreamException ex) {
            throw IdentityException.error("Error while loading scope config.", ex);
        } finally {
            try {
                if (parser != null) {
                    parser.close();
                }

                if (stream != null) {
                    IdentityIOStreamUtils.closeInputStream(stream);
                }
            } catch (XMLStreamException ex) {
                log.error("Error while closing XML stream", ex);
            }

        }

        return scopes;
    }

    private static String loadClaimConfig(OMElement configElement) {
        StringBuilder claimConfig = new StringBuilder();
        Iterator it = configElement.getChildElements();

        while (it.hasNext()) {
            OMElement element = (OMElement) it.next();
            if ("Claim".equals(element.getLocalName())) {
                String commaSeparatedClaimNames = element.getText();
                if (StringUtils.isNotBlank(commaSeparatedClaimNames)) {
                    claimConfig.append(commaSeparatedClaimNames.trim());
                }
            }
        }

        return claimConfig.toString();
    }

}

/*
 * Copyright (c) 2019, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
 *
 * WSO2 Inc. licenses this file to you under the Apache License,
 * Version 2.0 (the "License"); you may not use this file except
 * in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 *
 */
package org.wso2.carbon.is.migration.service.v570.migrator;

import org.apache.axiom.om.OMElement;
import org.apache.axiom.om.impl.builder.StAXOMBuilder;
import org.apache.commons.lang.StringUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.wso2.carbon.context.PrivilegedCarbonContext;
import org.wso2.carbon.identity.core.migrate.MigrationClientException;
import org.wso2.carbon.identity.core.util.IdentityIOStreamUtils;
import org.wso2.carbon.identity.core.util.IdentityTenantUtil;
import org.wso2.carbon.identity.oauth.dto.ScopeDTO;
import org.wso2.carbon.identity.oauth2.IdentityOAuth2Exception;
import org.wso2.carbon.identity.oauth2.dao.OAuthTokenPersistenceFactory;
import org.wso2.carbon.is.migration.service.Migrator;
import org.wso2.carbon.is.migration.util.Constant;
import org.wso2.carbon.is.migration.util.Utility;
import org.wso2.carbon.registry.core.service.RegistryService;
import org.wso2.carbon.user.api.Tenant;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Properties;
import java.util.Set;
import javax.xml.namespace.QName;
import javax.xml.stream.XMLInputFactory;
import javax.xml.stream.XMLStreamException;
import javax.xml.stream.XMLStreamReader;

import static org.wso2.carbon.base.MultitenantConstants.SUPER_TENANT_DOMAIN_NAME;
import static org.wso2.carbon.base.MultitenantConstants.SUPER_TENANT_ID;
import static org.wso2.carbon.identity.oauth.common.OAuthConstants.SCOPE_RESOURCE_PATH;

public class OIDCScopeDataMigrator extends Migrator {

    private static final Log log = LogFactory.getLog(OIDCScopeDataMigrator.class);
    private static final String OIDC_SCOPE_CONFIG_PATH = "oidc-scope-config.xml";
    private static final String SCOPE_CLAIM_SEPERATOR = ",";
    private static final String ID = "id";
    private static final String CLAIM = "Claim";
    private Map<String, String> scopeConfigFile = null;
    @Override
    public void migrate() throws MigrationClientException {
        migrateOIDCScopes();
    }

    public void migrateOIDCScopes() throws MigrationClientException {

        log.info(Constant.MIGRATION_LOG + "Started to migrate OIDC scopes");

        //migrating super tenant configurations
        Properties oidcScopes = getOIDCScopeProperties(SUPER_TENANT_DOMAIN_NAME);
        addScopes(oidcScopes, SUPER_TENANT_ID);
        Set<Tenant> tenants;
        try {
            tenants = Utility.getTenants();

            for (Tenant tenant : tenants) {
                log.info(Constant.MIGRATION_LOG + "Started to migrate OIDC scopes for tenant: " + tenant.getDomain());
                if (isIgnoreForInactiveTenants() && !tenant.isActive()) {
                    log.info(Constant.MIGRATION_LOG + "Tenant " + tenant.getDomain() + " is inactive. Skipping oidc scope migration. ");
                    continue;
                }
                Properties scopes = getOIDCScopeProperties(tenant.getDomain());
                addScopes(scopes, tenant.getId());
            }
        } catch (MigrationClientException e) {
            String message = Constant.MIGRATION_LOG + "Error while migrating oidc scopes";
            if (isContinueOnError()) {
                log.error(message, e);
            } else {
                throw new MigrationClientException(message, e);
            }
        }
    }

    protected void addScopes(Properties properties, int tenantId) throws MigrationClientException {
        try {
            appendAdditionalProperties(properties);
            List<ScopeDTO> scopeDTOs = getScopeDTOs(properties);
            OAuthTokenPersistenceFactory.getInstance().getScopeClaimMappingDAO().addScopes(tenantId, scopeDTOs);
        } catch (IdentityOAuth2Exception e) {
            if (e.getMessage() != null && e.getMessage().contains("Duplicate scopes can not be added")) {
                log.warn("OIDC scopes are already added to the tenant: " + tenantId);
            } else {
                throw new MigrationClientException(e.getMessage(), e);
            }
        }
    }

    private List<ScopeDTO> getScopeDTOs(Properties properties) {

        List<ScopeDTO> scopeDTOs = new ArrayList<>();
        for (Map.Entry<Object, Object> entry : properties.entrySet()) {
            ScopeDTO scopeDTO = new ScopeDTO();
            scopeDTO.setName(entry.getKey().toString());
            if (entry.getValue() != null) {
                String[] claims = entry.getValue().toString().split(",");
                String[] trimmedClaims = new String[claims.length];
                for (int i = 0; i < claims.length; i++) {
                    trimmedClaims[i] = claims[i].trim();
                }
                scopeDTO.setClaim(trimmedClaims);
            }
            scopeDTOs.add(scopeDTO);
        }
        return scopeDTOs;
    }

    protected Properties getOIDCScopeProperties(String tenantDomain) {

        org.wso2.carbon.registry.api.Resource oidcScopesResource = null;
        try {
            int tenantId = IdentityTenantUtil.getTenantId(tenantDomain);
            startTenantFlow(tenantDomain, tenantId);
            IdentityTenantUtil.getTenantRegistryLoader().loadTenantRegistry(tenantId);
            RegistryService registryService = IdentityTenantUtil.getRegistryService();
            oidcScopesResource = registryService.getConfigSystemRegistry(tenantId).get(SCOPE_RESOURCE_PATH);
        } catch (org.wso2.carbon.registry.api.RegistryException e) {
            log.error(Constant.MIGRATION_LOG + "Error while obtaining registry collection from registry path:" +
                    SCOPE_RESOURCE_PATH, e);
        } finally {
            PrivilegedCarbonContext.endTenantFlow();
        }

        Properties propertiesToReturn = new Properties();
        if (oidcScopesResource != null) {
            for (Object scopeProperty : oidcScopesResource.getProperties().keySet()) {
                String propertyKey = (String) scopeProperty;
                propertiesToReturn.setProperty(propertyKey, oidcScopesResource.getProperty(propertyKey));
            }
        } else {
            log.error(Constant.MIGRATION_LOG + "OIDC scope resource cannot be found at " + SCOPE_RESOURCE_PATH + " for tenantDomain: "
                    + tenantDomain);
        }
        return propertiesToReturn;
    }

    protected void startTenantFlow(String tenantDomain, int tenantId) {

        PrivilegedCarbonContext.startTenantFlow();
        PrivilegedCarbonContext carbonContext = PrivilegedCarbonContext.getThreadLocalCarbonContext();
        carbonContext.setTenantId(tenantId);
        carbonContext.setTenantDomain(tenantDomain);
    }

    private void appendAdditionalProperties(Properties properties) {

        if (scopeConfigFile == null) {
            String confXml = Utility.getDataFilePath(OIDC_SCOPE_CONFIG_PATH, getVersionConfig().getVersion());
            File configfile = new File(confXml);
            if (!configfile.exists()) {
                if (log.isDebugEnabled()) {
                    log.debug(Constant.MIGRATION_LOG + "Additional OIDC scope-claim Configuration File is not present at: " + confXml);
                }
            } else {
                if (log.isDebugEnabled()) {
                    log.debug(Constant.MIGRATION_LOG + "Additional OIDC scope-claim Configuration File is present at: " + confXml);
                }
                scopeConfigFile = loadScopeConfigFile(configfile);
            }
        }

        if (scopeConfigFile != null) {
            for (Map.Entry<String, String> entry : scopeConfigFile.entrySet()) {
                if (properties.getProperty(entry.getKey()) != null) {
                    properties.setProperty(entry.getKey(), properties.getProperty(entry.getKey()) + SCOPE_CLAIM_SEPERATOR + entry.getValue());
                }
            }
        }
    }

    private  Map<String, String> loadScopeConfigFile(File configfile) {

        Map<String, String> scopes = new HashMap<>();
        XMLStreamReader parser = null;
        InputStream stream = null;

        try {
            stream = new FileInputStream(configfile);
            parser = XMLInputFactory.newInstance().createXMLStreamReader(stream);
            StAXOMBuilder builder = new StAXOMBuilder(parser);
            OMElement documentElement = builder.getDocumentElement();
            Iterator iterator = documentElement.getChildElements();
            while (iterator.hasNext()) {
                OMElement omElement = (OMElement) iterator.next();
                String configType = omElement.getAttributeValue(new QName(ID));
                scopes.put(configType, loadClaimConfig(omElement));
            }
        } catch (XMLStreamException e) {
            log.warn(Constant.MIGRATION_LOG + "Error while loading scope config.", e);
        } catch (FileNotFoundException e) {
            log.warn(Constant.MIGRATION_LOG + "Error while loading email config.", e);
        } finally {
            try {
                if (parser != null) {
                    parser.close();
                }
                if (stream != null) {
                    IdentityIOStreamUtils.closeInputStream(stream);
                }
            } catch (XMLStreamException e) {
                log.error(Constant.MIGRATION_LOG + "Error while closing XML stream", e);
            }
        }
        return scopes;
    }

    private static String loadClaimConfig(OMElement configElement) {
        StringBuilder claimConfig = new StringBuilder();
        Iterator it = configElement.getChildElements();
        while (it.hasNext()) {
            OMElement element = (OMElement) it.next();
            if (CLAIM.equals(element.getLocalName())) {
                String commaSeparatedClaimNames = element.getText();
                if(StringUtils.isNotBlank(commaSeparatedClaimNames)){
                    claimConfig.append(commaSeparatedClaimNames.trim());
                }
            }
        }
        return claimConfig.toString();
    }
}

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

import org.apache.axiom.om.OMElement;
import org.apache.axiom.om.impl.builder.StAXOMBuilder;
import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.lang.StringUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.wso2.carbon.context.PrivilegedCarbonContext;
import org.wso2.carbon.core.util.CryptoException;
import org.wso2.carbon.identity.core.migrate.MigrationClientException;
import org.wso2.carbon.identity.core.util.IdentityIOStreamUtils;
import org.wso2.carbon.identity.core.util.IdentityTenantUtil;
import org.wso2.carbon.is.migration.internal.ISMigrationServiceDataHolder;
import org.wso2.carbon.is.migration.service.v550.util.EncryptionUtil;
import org.wso2.carbon.is.migration.util.Utility;
import org.wso2.carbon.registry.core.Collection;
import org.wso2.carbon.registry.core.Registry;
import org.wso2.carbon.registry.core.Resource;
import org.wso2.carbon.registry.core.exceptions.RegistryException;
import org.wso2.carbon.registry.core.utils.RegistryUtils;
import org.wso2.carbon.user.api.Tenant;
import org.wso2.carbon.user.api.UserStoreException;

import java.io.InputStream;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Iterator;
import java.util.List;
import java.util.Set;
import javax.xml.namespace.QName;
import javax.xml.stream.XMLInputFactory;
import javax.xml.stream.XMLStreamException;
import javax.xml.stream.XMLStreamReader;

import static org.wso2.carbon.base.MultitenantConstants.SUPER_TENANT_DOMAIN_NAME;
import static org.wso2.carbon.base.MultitenantConstants.SUPER_TENANT_ID;

public class RegistryDataManager {

    private static final String STS_SERVICE_GROUP = "org.wso2.carbon.sts";
    private static final Log log = LogFactory.getLog(RegistryDataManager.class);
    private static final String SERVICE_PRINCIPAL_PASSWORD = "service.principal.password";
    private static final String KERBEROS = "Kerberos";
    private static final String NAME = "name";
    private static final String PASSWORD = "password";
    private static final String SUBSCRIBER_PASSWORD = "subscriberPassword";
    private static final String PRIVATE_KEY_PASS = "privatekeyPass";
    private static final String POLICY_PUBLISHER_RESOURCE_PATH = "/repository/identity/entitlement/publisher/";
    private static final String KEYSTORE_RESOURCE_PATH = "/repository/security/key-stores/";
    private static final String SYSLOG = "/repository/components/org.wso2.carbon.logging/loggers/syslog/SYSLOG_PROPERTIES";
    private static final String SECURITY_POLICY_RESOURCE_PATH = "/services/wso2carbon-sts/policies/";
    private static final String SERVICE_GROUPS_PATH = "/repository/axis2/service-groups/";
    private static final String CARBON_SEC_CONFIG = "CarbonSecConfig";

    private static RegistryDataManager instance = new RegistryDataManager();

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

    public void migrateSubscriberPassword(boolean migrateActiveTenantsOnly)
            throws UserStoreException, MigrationClientException {

        //migrating super tenant configurations
        try {
            migrateSubscriberDataForTenant(SUPER_TENANT_ID);
            log.info("Policy Subscribers migrated for tenant : " + SUPER_TENANT_DOMAIN_NAME);
        } catch (Exception e) {
            log.error("Error while migrating Policy Subscribers for tenant : " + SUPER_TENANT_DOMAIN_NAME, e);
        }

        //migrating tenant configurations
        Set<Tenant> tenants = Utility.getTenants();
        for (Tenant tenant : tenants) {
            if (migrateActiveTenantsOnly && !tenant.isActive()) {
                log.info("Tenant " + tenant.getDomain() + " is inactive. Skipping Subscriber migration!");
                continue;
            }
            try {
                startTenantFlow(tenant);
                migrateSubscriberDataForTenant(tenant.getId());
                log.info("Subscribers migrated for tenant : " + tenant.getDomain());
            } catch (Exception e) {
                log.error("Error while migrating Subscribers for tenant : " + tenant.getDomain(), e);
            } finally {
                PrivilegedCarbonContext.endTenantFlow();
            }
        }
    }

    /**
     * Method to migrate encrypted password of key stores
     *
     * @param migrateActiveTenantsOnly
     * @throws Exception
     */
    public void migrateKeyStorePassword(boolean migrateActiveTenantsOnly) throws Exception {

        //migrating super tenant configurations
        try {
            migrateKeyStorePasswordForTenant(SUPER_TENANT_ID);
            log.info("Keystore passwords migrated for tenant : " + SUPER_TENANT_DOMAIN_NAME);
        } catch (Exception e) {
            log.error("Error while migrating Keystore passwords for tenant : " + SUPER_TENANT_DOMAIN_NAME);
            throw e;
        }

        //migrating tenant configurations
        Set<Tenant> tenants = Utility.getTenants();
        for (Tenant tenant : tenants) {
            if (migrateActiveTenantsOnly && !tenant.isActive()) {
                log.info("Tenant " + tenant.getDomain() + " is inactive. Skipping keystore passwords migration!");
                continue;
            }
            try {
                startTenantFlow(tenant);
                migrateKeyStorePasswordForTenant(tenant.getId());
                log.info("Keystore passwords migrated for tenant : " + tenant.getDomain());
            } catch (Exception e) {
                log.error("Error while migrating keystore passwords for tenant : " + tenant.getDomain());
                throw e;
            } finally {
                PrivilegedCarbonContext.endTenantFlow();
            }
        }
    }

    /**
     * Method to migrate encrypted password of SYSLOG_PROPERTIES registry resource
     *
     * @param migrateActiveTenantsOnly
     * @throws UserStoreException,RegistryException,CryptoException,MigrationClientException
     */
    public void migrateSysLogPropertyPassword(boolean migrateActiveTenantsOnly)
            throws UserStoreException, RegistryException, CryptoException, MigrationClientException {

        //migrating super tenant configurations
        try {
            migrateSysLogPropertyPasswordForTenant(SUPER_TENANT_ID);
            log.info("Sys log property password migrated for tenant : " + SUPER_TENANT_DOMAIN_NAME);
        } catch (Exception e) {
            log.error("Error while migrating Sys log property password for tenant : " + SUPER_TENANT_DOMAIN_NAME, e);
        }
        Set<Tenant> tenants = Utility.getTenants();
        for (Tenant tenant : tenants) {
            if (migrateActiveTenantsOnly && !tenant.isActive()) {
                log.info("Tenant " + tenant.getDomain() + " is inactive. Skipping SYSLOG_PROPERTIES file migration. ");
                continue;
            }
            try {
                startTenantFlow(tenant);
                migrateSysLogPropertyPasswordForTenant(tenant.getId());
            } finally {
                PrivilegedCarbonContext.endTenantFlow();
            }
        }
    }

    /**
     * Method to migrate encrypted password of service principle registry resource
     *
     * @param migrateActiveTenantsOnly
     * @throws CryptoException
     * @throws RegistryException
     * @throws UserStoreException,RegistryException,CryptoException,MigrationClientException
     */
    public void migrateServicePrinciplePassword(boolean migrateActiveTenantsOnly) throws
            CryptoException, RegistryException, UserStoreException, MigrationClientException {

        //migrating super tenant configurations
        try {
            updateSecurityPolicyPassword(SUPER_TENANT_ID);
            log.info("Policy Subscribers migrated for tenant : " + SUPER_TENANT_DOMAIN_NAME);
        } catch (XMLStreamException e) {
            log.error("Error while migrating Policy Subscribers for tenant : " + SUPER_TENANT_DOMAIN_NAME, e);
        }

        //migrating tenant configurations
        Set<Tenant> tenants = Utility.getTenants();
        for (Tenant tenant : tenants) {
            if (migrateActiveTenantsOnly && !tenant.isActive()) {
                log.info("Tenant " + tenant.getDomain() + " is inactive. Skipping Service Principle Password migration!");
                continue;
            }
            try {
                startTenantFlow(tenant);
                updateSecurityPolicyPassword(tenant.getId());
                log.info("Service Principle Passwords migrated for tenant : " + tenant.getDomain());
            } catch (XMLStreamException e) {
                log.error("Error while migrating Service Principle Passwords for tenant : " + tenant.getDomain(), e);
            } finally {
                PrivilegedCarbonContext.endTenantFlow();
            }
        }
    }

    private void migrateKeyStorePasswordForTenant(int tenantId) throws RegistryException, CryptoException {
        Registry registry = IdentityTenantUtil.getRegistryService().getGovernanceSystemRegistry(tenantId);
        if (registry.resourceExists(KEYSTORE_RESOURCE_PATH)) {
            Collection keyStoreCollection = (Collection) registry.get(KEYSTORE_RESOURCE_PATH);
            for (String keyStorePath : keyStoreCollection.getChildren()) {
                updateRegistryProperties(registry, keyStorePath,
                        new ArrayList<>(Arrays.asList(PASSWORD, PRIVATE_KEY_PASS)));
            }
        }
    }

    private void migrateSubscriberDataForTenant(int tenantId) throws RegistryException, CryptoException {

        Registry registry = IdentityTenantUtil.getRegistryService().getGovernanceSystemRegistry(tenantId);
        if (registry.resourceExists(POLICY_PUBLISHER_RESOURCE_PATH)) {
            Collection subscriberCollection = (Collection) registry.get(POLICY_PUBLISHER_RESOURCE_PATH);
            for (String subscriberPath : subscriberCollection.getChildren()) {
                updateRegistryProperties(registry, subscriberPath, new ArrayList<>(Arrays.asList(SUBSCRIBER_PASSWORD)));
            }
        }
    }

    private void migrateSysLogPropertyPasswordForTenant(int tenantId) throws RegistryException, CryptoException {

        Registry registry = IdentityTenantUtil.getRegistryService().getConfigSystemRegistry(tenantId);
        updateRegistryProperties(registry, SYSLOG, new ArrayList<>(Arrays.asList(PASSWORD)));
    }

    private void updateSecurityPolicyPassword (int tenantId) throws RegistryException, CryptoException,
            XMLStreamException {

        InputStream resourceContent = null;
        XMLStreamReader parser = null;

        try {
            Registry registry = IdentityTenantUtil.getRegistryService().getConfigSystemRegistry(tenantId);
            List<String> policyPaths = getSTSPolicyPaths(registry);
            String newEncryptedPassword = null;
            for (String resourcePath : policyPaths) {
                if (registry.resourceExists(resourcePath)) {
                    Resource resource = registry.get(resourcePath);
                    resourceContent = resource.getContentStream();
                    parser = XMLInputFactory.newInstance().createXMLStreamReader(resourceContent);
                    StAXOMBuilder builder = new StAXOMBuilder(parser);
                    OMElement documentElement = builder.getDocumentElement();
                    Iterator it = documentElement.getChildrenWithName(new QName(CARBON_SEC_CONFIG));

                    while (it != null && it.hasNext()) {
                        OMElement secConfig = (OMElement) it.next();
                        Iterator kerberosProperties = secConfig.getChildrenWithName(new QName(KERBEROS));
                        Iterator propertySet = null;
                        if ((kerberosProperties != null && kerberosProperties.hasNext())) {
                            propertySet = ((OMElement) kerberosProperties.next()).getChildElements();
                        }
                        if (propertySet != null) {
                            while (propertySet.hasNext()) {
                                OMElement kbProperty = (OMElement) propertySet.next();
                                if (SERVICE_PRINCIPAL_PASSWORD.equals(kbProperty.getAttributeValue(new QName(NAME)))) {
                                    String encryptedPassword = kbProperty.getText();
                                    newEncryptedPassword = EncryptionUtil.getNewEncryptedValue(encryptedPassword);
                                    if (StringUtils.isNotEmpty(newEncryptedPassword)) {
                                        kbProperty.setText(newEncryptedPassword);
                                    }
                                }
                            }
                        }
                    }
                    if (StringUtils.isNotEmpty(newEncryptedPassword)) {
                        resource.setContent(RegistryUtils.encodeString(documentElement.toString()));
                        registry.beginTransaction();
                        registry.put(resourcePath, resource);
                        registry.commitTransaction();
                    }
                }
            }
        } finally {
            try {
                if(parser != null) {
                    parser.close();
                }
                if(resourceContent != null) {
                    IdentityIOStreamUtils.closeInputStream(resourceContent);
                }
            } catch (XMLStreamException ex) {
                log.error("Error while closing XML stream", ex);
            }
        }

    }

    private void updateRegistryProperties(Registry registry, String resource, List<String> properties)
            throws RegistryException, CryptoException {

        if (registry == null || StringUtils.isEmpty(resource) || CollectionUtils.isEmpty(properties)) {
            return;
        }

        if (registry.resourceExists(resource)) {
            try {
                registry.beginTransaction();
                Resource resourceObj = registry.get(resource);
                for (String encryptedPropertyName : properties) {
                    String oldValue = resourceObj.getProperty(encryptedPropertyName);
                    String newValue = EncryptionUtil.getNewEncryptedValue(oldValue);
                    if (StringUtils.isNotEmpty(newValue)) {
                        resourceObj.setProperty(encryptedPropertyName, newValue);
                    }
                }
                registry.put(resource, resourceObj);
                registry.commitTransaction();
            } catch (RegistryException e) {
                registry.rollbackTransaction();
                log.error("Unable to update the registry resource", e);
                throw e;
            }
        }
    }

    private List<String> getSTSPolicyPaths(Registry registry) throws RegistryException {

        List<String> policyPaths = new ArrayList<>();
        if (registry.resourceExists(SERVICE_GROUPS_PATH)) {
            Collection serviceGroups = (Collection)registry.get(SERVICE_GROUPS_PATH);
            if (serviceGroups != null) {
                for (String serviceGroupPath : serviceGroups.getChildren()) {
                    if ( StringUtils.isNotEmpty(serviceGroupPath) &&
                            serviceGroupPath.contains(STS_SERVICE_GROUP)) {
                        String policyCollectionPath = new StringBuilder().append(serviceGroupPath)
                                .append(SECURITY_POLICY_RESOURCE_PATH).toString();
                        Collection policies = (Collection) registry.get(policyCollectionPath);
                        if (policies != null) {
                            policyPaths.addAll(Arrays.asList(policies.getChildren()));
                        }
                    }
                }
            }
        }
        return policyPaths;
    }
}

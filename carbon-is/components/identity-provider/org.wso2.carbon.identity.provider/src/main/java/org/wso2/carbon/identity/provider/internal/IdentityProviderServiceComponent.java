/*
 * Copyright (c) 2016 WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
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
 */

package org.wso2.carbon.identity.provider.internal;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.osgi.framework.BundleContext;
import org.osgi.framework.ServiceRegistration;
import org.osgi.service.component.ComponentContext;
import org.wso2.carbon.base.MultitenantConstants;
import org.wso2.carbon.identity.application.common.model.IdentityProvider;
import org.wso2.carbon.identity.application.common.util.IdentityApplicationConstants;
import org.wso2.carbon.identity.core.util.IdentityTenantUtil;
import org.wso2.carbon.identity.core.util.IdentityUtil;
import org.wso2.carbon.idp.mgt.IdentityProviderManagementException;
import org.wso2.carbon.idp.mgt.listener.IDPMgtAuditLogger;
import org.wso2.carbon.idp.mgt.listener.IdPMgtValidationListener;
import org.wso2.carbon.idp.mgt.listener.IdentityProviderMgtListener;
import org.wso2.carbon.user.core.service.RealmService;
import org.wso2.carbon.utils.CarbonUtils;
import org.wso2.carbon.utils.ConfigurationContextService;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

/**
 * @scr.component name="idp.mgt.dscomponent" immediate="true"
 * @scr.reference name="idp.mgt.event.listener.service"
 * interface="org.wso2.carbon.idp.mgt.listener.IdentityProviderMgtListener"
 * cardinality="0..n" policy="dynamic"
 * bind="setIdentityProviderMgtListenerService"
 * unbind="unsetIdentityProviderMgtListenerService"
 */
public class IdentityProviderServiceComponent {

    private static Log log = LogFactory.getLog(IdentityProviderServiceComponent.class);

    private static volatile List<IdentityProviderMgtListener> idpMgtListeners = new ArrayList<>();

    protected void activate(ComponentContext ctxt) {
        try {
            BundleContext bundleCtx = ctxt.getBundleContext();

            ServiceRegistration auditLoggerSR = bundleCtx
                    .registerService(IdentityProviderMgtListener.class.getName(), new IDPMgtAuditLogger(), null);

            if (auditLoggerSR != null) {
                log.debug("Identity Provider Management - Audit Logger registered");
            } else {
                log.error("Identity Provider Management - Error while registering Audit Logger");
            }
            setIdentityProviderMgtListenerService(new IdPMgtValidationListener());

            CacheBackedIdPMgtDAO dao = new CacheBackedIdPMgtDAO(new org.wso2.carbon.idp.mgt.dao.IdentityProviderDAO());
            if (dao.getIdPByName(null, IdentityApplicationConstants.RESIDENT_IDP_RESERVED_NAME,
                    IdentityTenantUtil.getTenantId(MultitenantConstants.SUPER_TENANT_DOMAIN_NAME),
                    MultitenantConstants.SUPER_TENANT_DOMAIN_NAME) == null) {
                addSuperTenantIdp();
            }
            bundleCtx.registerService(org.wso2.carbon.idp.mgt.IdentityProviderService.class,
                    org.wso2.carbon.idp.mgt.IdentityProviderServiceImpl.getInstance(), null);

            buildFileBasedIdPList();
            cleanUpRemovedIdps();

            log.debug("Identity Provider Management bundle is activated");

        } catch (Throwable e) {

            log.error("Error while activating Identity Provider Management bundle", e);

        }
    }

    /**
     *
     */
    private void buildFileBasedIdPList() {

        String spConfigDirPath = CarbonUtils.getCarbonConfigDirPath() + File.separator + "identity" + File.separator
                + "identity-providers";
        FileInputStream fileInputStream = null;
        File spConfigDir = new File(spConfigDirPath);
        OMElement documentElement = null;

        if (spConfigDir.exists()) {

            for (final File fileEntry : spConfigDir.listFiles()) {
                try {
                    if (!fileEntry.isDirectory()) {
                        fileInputStream = new FileInputStream(new File(fileEntry.getAbsolutePath()));
                        documentElement = new StAXOMBuilder(fileInputStream).getDocumentElement();
                        IdentityProvider idp = IdentityProvider.build(documentElement);
                        if (idp != null) {
                            org.wso2.carbon.idp.mgt.IdentityProviderServiceImpl idpManager =
                                    org.wso2.carbon.idp.mgt.IdentityProviderServiceImpl.getInstance();
                            String superTenantDN = MultitenantConstants.SUPER_TENANT_DOMAIN_NAME;
                            if (isSharedIdP(idp)) {
                                IdentityProvider currentIdp = idpManager
                                        .getIdPByName(idp.getIdentityProviderName(), superTenantDN);
                                if (currentIdp != null && !IdentityApplicationConstants.DEFAULT_IDP_CONFIG
                                        .equals(currentIdp.getIdentityProviderName())) {
                                    idpManager.updateIdP(idp.getIdentityProviderName(), idp, superTenantDN);
                                    if (log.isDebugEnabled()) {
                                        log.debug("Shared IdP " + idp.getIdentityProviderName() + " updated");
                                    }
                                } else {
                                    idpManager.addIdP(idp, superTenantDN);
                                    if (log.isDebugEnabled()) {
                                        log.debug("Shared IdP " + idp.getIdentityProviderName() + " added");
                                    }
                                }
                                sharedIdps.add(idp.getIdentityProviderName());
                            } else {
                                fileBasedIdPs.put(idp.getIdentityProviderName(), idp);
                            }
                        }
                    }
                } catch (Exception e) {
                    log.error("Error while loading idp from file system.", e);
                } finally {
                    if (fileInputStream != null) {
                        try {
                            fileInputStream.close();
                        } catch (IOException e) {
                            log.error(e.getMessage(), e);
                        }
                    }
                }
            }
        }
    }

    private void cleanUpRemovedIdps() {
        org.wso2.carbon.idp.mgt.IdentityProviderServiceImpl idpManager =
                org.wso2.carbon.idp.mgt.IdentityProviderServiceImpl.getInstance();
        String superTenantDN = MultitenantConstants.SUPER_TENANT_DOMAIN_NAME;
        List<IdentityProvider> idPs;
        try {
            idPs = idpManager.getIdPs(superTenantDN);
        } catch (IdentityProviderManagementException e) {
            log.error("Error loading IDPs", e);
            return;
        }
        for (IdentityProvider idp : idPs) {
            if (isSharedIdP(idp) && !sharedIdps.contains(idp.getIdentityProviderName())) {
                //IDP config file has been deleted from filesystem
                try {
                    idpManager.deleteIdP(idp.getIdentityProviderName(), superTenantDN);
                    if (log.isDebugEnabled()) {
                        log.debug("Deleted shared IdP with the name : " + idp.getIdentityProviderName());
                    }
                } catch (IdentityProviderManagementException e) {
                    log.error("Error when deleting IdP " + idp.getIdentityProviderName(), e);
                }
            }
        }
    }

    /**
     * @param ctxt
     */
    protected void deactivate(ComponentContext ctxt) {
        log.debug("Identity Provider Management bundle is deactivated");
    }

    /**
     * @param realmService
     */
    protected void unsetRealmService(RealmService realmService) {
        realmService = null;
    }

    /**
     * @param service
     */
    protected void unsetConfigurationContextService(ConfigurationContextService service) {
        configurationContextService = null;
    }

    protected void setIdentityProviderMgtListenerService(
            IdentityProviderMgtListener identityProviderMgtListenerService) {

        idpMgtListeners.add(identityProviderMgtListenerService);
        Collections.sort(idpMgtListeners, idpMgtListenerComparator);
    }

    protected void unsetIdentityProviderMgtListenerService(
            IdentityProviderMgtListener identityProviderMgtListenerService) {

        idpMgtListeners.remove(identityProviderMgtListenerService);
    }

    public static Collection<IdentityProviderMgtListener> getIdpMgtListeners() {
        return idpMgtListeners;
    }

    private static Comparator<IdentityProviderMgtListener> idpMgtListenerComparator =
            new Comparator<IdentityProviderMgtListener>() {

        @Override
        public int compare(IdentityProviderMgtListener identityProviderMgtListener1,
                IdentityProviderMgtListener identityProviderMgtListener2) {

            if (identityProviderMgtListener1.getExecutionOrderId() > identityProviderMgtListener1
                    .getExecutionOrderId()) {
                return 1;
            } else if (identityProviderMgtListener1.getExecutionOrderId() < identityProviderMgtListener1
                    .getExecutionOrderId()) {
                return -1;
            } else {
                return 0;
            }
        }
    };

    private static void addSuperTenantIdp() throws Exception {

        try {
            IdentityProvider identityProvider = new IdentityProvider();
            identityProvider.setIdentityProviderName(IdentityApplicationConstants.RESIDENT_IDP_RESERVED_NAME);
            identityProvider.setHomeRealmId(IdentityUtil.getHostName());
            identityProvider.setPrimary(true);
            org.wso2.carbon.idp.mgt.IdentityProviderServiceImpl.getInstance()
                    .addResidentIdP(identityProvider, MultitenantConstants.SUPER_TENANT_DOMAIN_NAME);
        } catch (Throwable e) {
            throw new Exception("Error when adding Resident Identity Provider entry for super tenant ", e);
        }
    }
}

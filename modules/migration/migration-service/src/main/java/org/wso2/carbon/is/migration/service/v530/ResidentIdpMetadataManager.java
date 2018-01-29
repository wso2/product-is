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

package org.wso2.carbon.is.migration.service.v530;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.wso2.carbon.is.migration.internal.ISMigrationServiceDataHolder;
import org.wso2.carbon.is.migration.service.v530.dao.IdpMetaDataDAO;
import org.wso2.carbon.user.api.Tenant;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Migrates default governance connector properties for tenants created on IS 5.2.0
 */
public class ResidentIdpMetadataManager {

    private Log log = LogFactory.getLog(ResidentIdpMetadataManager.class);

    // Default governance connector properties
    private static final Map<String, String> DEFAULT_PROPERTIES = new HashMap<String, String>() {{
        put("RememberMeTimeout", String.valueOf(20160));
        put("SessionIdleTimeout", String.valueOf(15));
        put("suspension.notification.account.disable.delay", String.valueOf(90));
        put("suspension.notification.delays", "30,45,60,75");
        put("suspension.notification.enable", String.valueOf(false));
        put("suspension.notification.AlreadyWritten", String.valueOf(true));
        put("sso.login.recaptcha.enable", String.valueOf(false));
        put("sso.login.recaptcha.on.max.failed.attempts", String.valueOf(3));
        put("sso.login.recaptcha.AlreadyWritten", String.valueOf(true));
        put("account.lock.handler.On.Failure.Max.Attempts", String.valueOf(2));
        put("account.lock.handler.login.fail.timeout.ratio", String.valueOf(2));
        put("account.lock.handler.Time", String.valueOf(5));
        put("account.lock.handler.enable", String.valueOf(false));
        put("account.lock.handler.AlreadyWritten", String.valueOf(true));
        put("passwordHistory.count", String.valueOf(5));
        put("passwordHistory.enable", String.valueOf(false));
        put("passwordHistory.AlreadyWritten", String.valueOf(true));
        put("passwordPolicy.pattern", "^((?=.*\\d)(?=.*[a-z])(?=.*[A-Z])(?=.*[!@#$%&*])).{0,100}$");
        put("passwordPolicy.errorMsg", "Password pattern policy violated. Password should contain a digit[0-9], " +
                                       "a lower case letter[a-z], an upper case letter[A-Z], one of !@#$%&* characters");
        put("passwordPolicy.enable", String.valueOf(false));
        put("passwordPolicy.min.length", String.valueOf(6));
        put("passwordPolicy.max.length", String.valueOf(12));
        put("passwordPolicy.AlreadyWritten", String.valueOf(true));
        put("Recovery.Notification.InternallyManage", String.valueOf(true));
        put("Recovery.Notification.Username.Enable", String.valueOf(false));
        put("Recovery.Notification.Password.Enable", String.valueOf(false));
        put("Recovery.ExpiryTime", String.valueOf(1440));
        put("Recovery.Question.Password.Enable", String.valueOf(false));
        put("Recovery.Question.Password.ReCaptcha.Enable", String.valueOf(true));
        put("Recovery.Question.Password.ReCaptcha.MaxFailedAttempts", String.valueOf(3));
        put("Recovery.Question.Password.MinAnswers", String.valueOf(2));
        put("Recovery.Question.Password.NotifyStart", String.valueOf(true));
        put("Recovery.NotifySuccess", String.valueOf(true));
        put("account-recovery.AlreadyWritten", String.valueOf(true));
        put("SelfRegistration.LockOnCreation", String.valueOf(true));
        put("SelfRegistration.ReCaptcha", String.valueOf(true));
        put("SelfRegistration.Enable", String.valueOf(false));
        put("SelfRegistration.Notification.InternallyManage", String.valueOf(true));
        put("self-sign-up.AlreadyWritten", String.valueOf(true));
        put("EmailVerification.LockOnCreation", String.valueOf(false));
        put("EmailVerification.Enable", String.valueOf(false));
        put("EmailVerification.Notification.InternallyManage", String.valueOf(true));
        put("user-email-verification.AlreadyWritten", String.valueOf(true));
        put("Recovery.AdminPasswordReset.RecoveryLink", String.valueOf(false));
        put("Recovery.AdminPasswordReset.Offline", String.valueOf(false));
        put("Recovery.AdminPasswordReset.OTP", String.valueOf(false));
        put("admin-forced-password-reset.AlreadyWritten", String.valueOf(true));
    }};


    public void migrateResidentIdpMetaData(boolean migrateActiveTenantsOnly) throws Exception {

        IdpMetaDataDAO idpMetaDataDAO = IdpMetaDataDAO.getInstance();
        /*
            Migrating the default governance connector configurations for tenant resident IDPs.
         */
        Tenant[] tenants = ISMigrationServiceDataHolder.getRealmService().getTenantManager().getAllTenants();
        for (Tenant tenant : tenants) {

            try {
                if (migrateActiveTenantsOnly && !tenant.isActive()) {
                    log.info("Tenant " + tenant.getDomain() + " is inactive. Skipping Resident IDP " +
                             "metadata migration!!!!");
                    continue;
                }

                // get the resident IDP id of the tenant
                int idpId = idpMetaDataDAO.getResidentIdpId(tenant.getId());
                if (idpId == -9999) {
                    log.error("Couldn't find resident idp id of tenant : " + tenant.getDomain());
                    continue;
                }

                // already available resident idp property names
                List<String> availablePropertyNames = idpMetaDataDAO.getAvailableConfigNames(tenant.getId(), idpId);
                List<IdpMetaDataDAO.IdpMetaData> idpMetaDataToAdd = new ArrayList<>(); // default properties that we need to add
                // we try and insert the missing properties
                for (Map.Entry<String, String> entry : DEFAULT_PROPERTIES.entrySet()) {
                    // first check if the connector property is already defined in the resident IDP
                    if (!availablePropertyNames.contains(entry.getKey())) {
                        String msg = "Setting '%s' default connector property value to '%s' in tenant : %s";
                        log.info(String.format(msg, entry.getKey(), entry.getValue(), tenant.getDomain()));

                        idpMetaDataToAdd.add(new IdpMetaDataDAO.IdpMetaData(idpId, entry.getKey(), entry.getValue(),
                                                                            null, tenant.getId()));
                    }
                }
                // write the missing properties to the DB
                idpMetaDataDAO.addIdpMetaData(idpMetaDataToAdd);
            } catch (Exception ex) {
                log.error("Error while migrating IDP metadata of tenant : " + tenant.getDomain(), ex);
            }
        }
    }


}

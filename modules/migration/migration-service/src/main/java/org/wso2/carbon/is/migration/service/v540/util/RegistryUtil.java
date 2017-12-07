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

package org.wso2.carbon.is.migration.service.v540.util;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.json.JSONObject;
import org.wso2.carbon.identity.base.IdentityException;
import org.wso2.carbon.identity.core.util.IdentityTenantUtil;
import org.wso2.carbon.is.migration.service.v540.bean.SpOAuth2ExpiryTimeConfiguration;
import org.wso2.carbon.registry.core.Registry;
import org.wso2.carbon.registry.core.Resource;
import org.wso2.carbon.registry.core.exceptions.RegistryException;

import java.util.List;

public class RegistryUtil {

    private static Log log = LogFactory.getLog(RegistryUtil.class);

    private static final String TOKEN_EXPIRE_TIME_RESOURCE_PATH = "/identity/config/spTokenExpireTime";

    private static final String USER_ACCESS_TOKEN_EXP_TIME_IN_MILLISECONDS = "userAccessTokenExpireTime";

    private static final String REFRESH_TOKEN_EXP_TIME_IN_MILLISECONDS = "refreshTokenExpireTime";

    private static final String APPLICATION_ACCESS_TOKEN_EXP_TIME_IN_MILLISECONDS = "applicationAccessTokenExpireTime";

    private RegistryUtil() {

    }

    /***
     * Return the SP-token Expiry time configuration object when consumer key is given.
     * @param consumerKey consumer key
     * @param tenantId tenant Id
     * @return A SpOAuth2ExpiryTimeConfiguration Object
     */
    public static SpOAuth2ExpiryTimeConfiguration getSpTokenExpiryTimeConfig(String consumerKey, int tenantId) {

        SpOAuth2ExpiryTimeConfiguration spTokenTimeObject = new SpOAuth2ExpiryTimeConfiguration();
        try {
            if (log.isDebugEnabled()) {
                log.debug("SP wise token expiry time feature is applied for tenant id : " + tenantId
                        + "and consumer key : " + consumerKey);
            }
            IdentityTenantUtil.initializeRegistry(tenantId, IdentityTenantUtil.getTenantDomain(tenantId));
            Registry registry = IdentityTenantUtil.getConfigRegistry(tenantId);
            if (registry.resourceExists(TOKEN_EXPIRE_TIME_RESOURCE_PATH)) {
                Resource resource = registry.get(TOKEN_EXPIRE_TIME_RESOURCE_PATH);
                String jsonString = "{}";
                Object consumerKeyObject = resource.getProperties().get(consumerKey);
                if (consumerKeyObject instanceof List) {
                    if (!((List) consumerKeyObject).isEmpty()) {
                        jsonString = ((List) consumerKeyObject).get(0).toString();
                    }
                }
                JSONObject spTimeObject = new JSONObject(jsonString);
                if (spTimeObject.length() > 0) {
                    if (spTimeObject.has(USER_ACCESS_TOKEN_EXP_TIME_IN_MILLISECONDS) &&
                            !spTimeObject.isNull(USER_ACCESS_TOKEN_EXP_TIME_IN_MILLISECONDS)) {
                        try {
                            spTokenTimeObject.setUserAccessTokenExpiryTime(Long.parseLong(spTimeObject
                                    .get(USER_ACCESS_TOKEN_EXP_TIME_IN_MILLISECONDS).toString()));
                            if (log.isDebugEnabled()) {
                                log.debug("The user access token expiry time :" + spTimeObject
                                        .get(USER_ACCESS_TOKEN_EXP_TIME_IN_MILLISECONDS).toString() +
                                        "  for application id : " + consumerKey);
                            }
                        } catch (NumberFormatException e) {
                            String errorMsg = String.format("Invalid value provided as user access token expiry time " +
                                            "for consumer key %s, tenant id : %d. Given value: %s, Expected a long value",
                                    consumerKey, tenantId, spTimeObject.get
                                            (USER_ACCESS_TOKEN_EXP_TIME_IN_MILLISECONDS).toString());
                            log.error(errorMsg, e);
                        }
                    }

                    if (spTimeObject.has(APPLICATION_ACCESS_TOKEN_EXP_TIME_IN_MILLISECONDS) &&
                            !spTimeObject.isNull(APPLICATION_ACCESS_TOKEN_EXP_TIME_IN_MILLISECONDS)) {
                        try {
                            spTokenTimeObject.setApplicationAccessTokenExpiryTime(Long.parseLong(spTimeObject
                                    .get(APPLICATION_ACCESS_TOKEN_EXP_TIME_IN_MILLISECONDS).toString()));
                            if (log.isDebugEnabled()) {
                                log.debug("The application access token expiry time :" + spTimeObject
                                        .get(APPLICATION_ACCESS_TOKEN_EXP_TIME_IN_MILLISECONDS).toString() +
                                        "  for application id : " + consumerKey);
                            }
                        } catch (NumberFormatException e) {
                            String errorMsg = String.format("Invalid value provided as application access token " +
                                    "expiry time for consumer key %s, tenant id : %d. Given value: %s, Expected a " +
                                    "long value ", consumerKey, tenantId, spTimeObject
                                    .get(APPLICATION_ACCESS_TOKEN_EXP_TIME_IN_MILLISECONDS).toString());
                            log.error(errorMsg, e);
                        }
                    }

                    if (spTimeObject.has(REFRESH_TOKEN_EXP_TIME_IN_MILLISECONDS) &&
                            !spTimeObject.isNull(REFRESH_TOKEN_EXP_TIME_IN_MILLISECONDS)) {
                        try {
                            spTokenTimeObject.setRefreshTokenExpiryTime(Long.parseLong(spTimeObject
                                    .get(REFRESH_TOKEN_EXP_TIME_IN_MILLISECONDS).toString()));
                            if (log.isDebugEnabled()) {
                                log.debug("The refresh token expiry time :" + spTimeObject
                                        .get(REFRESH_TOKEN_EXP_TIME_IN_MILLISECONDS).toString() +
                                        " for application id : " + consumerKey);
                            }

                        } catch (NumberFormatException e) {
                            String errorMsg = String.format("Invalid value provided as refresh token expiry time for " +
                                            "consumer key %s, tenant id : %d. Given value: %s, Expected a long value",
                                    consumerKey, tenantId, spTimeObject
                                            .get(REFRESH_TOKEN_EXP_TIME_IN_MILLISECONDS).toString());
                            log.error(errorMsg, e);
                        }
                    }
                }
            }
        } catch (RegistryException e) {
            log.error("Error while getting data from the registry.", e);
        } catch (IdentityException e) {
            log.error("Error while getting the tenant domain from tenant id : " + tenantId, e);
        }
        return spTokenTimeObject;
    }

}

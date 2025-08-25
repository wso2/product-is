/*
 * Copyright (c) 2025, WSO2 LLC. (http://www.wso2.com).
 *
 * WSO2 LLC. licenses this file to you under the Apache License,
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

package org.wso2.identity.integration.test.webhooks.util;

import org.json.JSONException;
import org.json.JSONObject;
import org.wso2.carbon.utils.multitenancy.MultitenantConstants;

/**
 * Utility class to create common JSON objects for event payloads.
 */
public class EventPayloadUtils {

    public static JSONObject createTenantObject(String tenantDomain) throws JSONException {

        JSONObject tenant = new JSONObject();
        tenant.put("id", "dummy-tenant-id");
        tenant.put("name", tenantDomain);
        return tenant;
    }

    public static JSONObject createOrganizationObject(String tenantDomain) throws JSONException {

        JSONObject organization = new JSONObject();
        organization.put("id", "dummy-org-id");
        if (tenantDomain.equals(MultitenantConstants.SUPER_TENANT_DOMAIN_NAME)) {
            organization.put("name", "Super");
        } else {
            organization.put("name", tenantDomain);
        }
        organization.put("orgHandle", tenantDomain);
        organization.put("depth", 0);
        return organization;
    }

    public static JSONObject createUserStoreObject() throws JSONException {

        JSONObject userStore = new JSONObject();
        userStore.put("id", "UFJJTUFSWQ==");
        userStore.put("name", "PRIMARY");
        return userStore;
    }

    public static JSONObject createReasonObject(String reasonDescription) throws JSONException {

        JSONObject reason = new JSONObject();
        reason.put("description", reasonDescription);
        return reason;
    }
}

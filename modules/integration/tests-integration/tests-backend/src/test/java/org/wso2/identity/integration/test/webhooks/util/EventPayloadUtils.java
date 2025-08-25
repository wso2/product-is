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

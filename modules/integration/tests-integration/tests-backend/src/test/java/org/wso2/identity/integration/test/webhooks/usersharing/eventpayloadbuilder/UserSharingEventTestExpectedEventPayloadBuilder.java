/*
 * Copyright (c) 2026, WSO2 LLC. (http://www.wso2.com).
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

package org.wso2.identity.integration.test.webhooks.usersharing.eventpayloadbuilder;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.wso2.carbon.utils.multitenancy.MultitenantConstants;
import org.wso2.identity.integration.test.webhooks.util.EventPayloadUtils;

/**
 * Builds the expected payloads of the user sharing events received by the organization the user belongs to.
 */
public class UserSharingEventTestExpectedEventPayloadBuilder {

    private static final String SERVER_BASE_URL = "https://localhost:9853";
    private static final String USERNAME_CLAIM_URI = "http://wso2.org/claims/username";

    /**
     * Builds the expected payload of a user shared event.
     *
     * @param userId             ID of the shared user, in the root organization.
     * @param username           Username of the shared user.
     * @param rootTenantDomain   Tenant domain of the root organization.
     * @param sharedUserId       ID of the user created by the sharing, in the organization it was shared with.
     * @param sharedOrganization Organization the user was shared with.
     * @param roles              Roles assigned by the sharing, or null when no role is assigned.
     * @param action             Expected action of the event.
     * @param initiatorType      Expected initiator type of the event.
     * @return JSONObject representing the expected event payload.
     * @throws JSONException if an error occurs while building the payload.
     */
    public static JSONObject buildExpectedUserSharedEventPayload(String userId, String username,
                                                                 String rootTenantDomain, String sharedUserId,
                                                                 JSONObject sharedOrganization, JSONArray roles,
                                                                 String action, String initiatorType)
            throws JSONException {

        JSONObject sharedUser = new JSONObject();
        sharedUser.put("id", sharedUserId);
        sharedUser.put("organization", sharedOrganization);
        // Users of sub organizations are referred through the organization SCIM2 endpoint of the root tenant.
        sharedUser.put("ref", SERVER_BASE_URL + "/t/" + rootTenantDomain + "/o/scim2/Users/" + sharedUserId);
        if (roles != null) {
            sharedUser.put("roles", roles);
        }

        JSONObject userSharedEvent = createUserSharingEventPayload(userId, username, rootTenantDomain, action,
                initiatorType);
        userSharedEvent.put("sharedUser", sharedUser);
        return userSharedEvent;
    }

    /**
     * Builds the expected payload of a user unshared event.
     *
     * @param userId               ID of the unshared user, in the root organization.
     * @param username             Username of the unshared user.
     * @param rootTenantDomain     Tenant domain of the root organization.
     * @param unsharedUserId       ID of the removed user, in the organization it was unshared from.
     * @param unsharedOrganization Organization the user was unshared from.
     * @param action               Expected action of the event.
     * @return JSONObject representing the expected event payload.
     * @throws JSONException if an error occurs while building the payload.
     */
    public static JSONObject buildExpectedUserUnsharedEventPayload(String userId, String username,
                                                                   String rootTenantDomain, String unsharedUserId,
                                                                   JSONObject unsharedOrganization, String action)
            throws JSONException {

        // The removed user no longer exists, hence it has no ref.
        JSONObject unsharedUser = new JSONObject();
        unsharedUser.put("id", unsharedUserId);
        unsharedUser.put("organization", unsharedOrganization);

        JSONObject userUnsharedEvent = createUserSharingEventPayload(userId, username, rootTenantDomain, action,
                "ADMIN");
        userUnsharedEvent.put("unsharedUser", unsharedUser);
        return userUnsharedEvent;
    }

    /**
     * Builds the organization object of a sub organization.
     *
     * @param orgId     ID of the organization.
     * @param orgName   Name of the organization.
     * @param orgHandle Handle of the organization.
     * @param depth     Depth of the organization in the organization hierarchy.
     * @return JSONObject representing the organization.
     * @throws JSONException if an error occurs while building the object.
     */
    public static JSONObject createOrganizationObject(String orgId, String orgName, String orgHandle, int depth)
            throws JSONException {

        JSONObject organization = new JSONObject();
        organization.put("id", orgId);
        organization.put("name", orgName);
        organization.put("orgHandle", orgHandle);
        organization.put("depth", depth);
        return organization;
    }

    /**
     * Builds a role object of a role of the root organization, which is not a shared role.
     *
     * @param roleId   ID of the role.
     * @param roleName Name of the role.
     * @return JSONObject representing the role.
     * @throws JSONException if an error occurs while building the object.
     */
    public static JSONObject createRoleObject(String roleId, String roleName) throws JSONException {

        return new JSONObject().put("id", roleId).put("name", roleName);
    }

    private static JSONObject createUserSharingEventPayload(String userId, String username, String rootTenantDomain,
                                                            String action, String initiatorType)
            throws JSONException {

        JSONObject event = new JSONObject();
        event.put("initiatorType", initiatorType);
        event.put("initiatorIpAddress", "dummy-initiator-ip-address");
        event.put("action", action);
        event.put("tenant", EventPayloadUtils.createTenantObject(rootTenantDomain));
        event.put("organization", EventPayloadUtils.createOrganizationObject(rootTenantDomain));
        event.put("user", createRootOrgUserObject(userId, username, rootTenantDomain));
        return event;
    }

    private static JSONObject createRootOrgUserObject(String userId, String username, String rootTenantDomain)
            throws JSONException {

        JSONObject user = new JSONObject();
        user.put("id", userId);
        user.put("claims", new JSONArray().put(new JSONObject().put("uri", USERNAME_CLAIM_URI).put("value", username)));
        user.put("organization", EventPayloadUtils.createOrganizationObject(rootTenantDomain));
        if (MultitenantConstants.SUPER_TENANT_DOMAIN_NAME.equals(rootTenantDomain)) {
            user.put("ref", SERVER_BASE_URL + "/scim2/Users/" + userId);
        } else {
            user.put("ref", SERVER_BASE_URL + "/t/" + rootTenantDomain + "/scim2/Users/" + userId);
        }
        return user;
    }
}

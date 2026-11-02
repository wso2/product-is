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

package org.wso2.identity.integration.test.webhooks.suborg.eventpayloadbuilder;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.wso2.identity.integration.test.webhooks.util.EventPayloadUtils;

import static org.wso2.identity.integration.test.webhooks.suborg.eventpayloadbuilder.SubOrgEventPayloadUtils.EMAIL_CLAIM_URI;
import static org.wso2.identity.integration.test.webhooks.suborg.eventpayloadbuilder.SubOrgEventPayloadUtils.GIVEN_NAME_CLAIM_URI;
import static org.wso2.identity.integration.test.webhooks.suborg.eventpayloadbuilder.SubOrgEventPayloadUtils.LAST_NAME_CLAIM_URI;
import static org.wso2.identity.integration.test.webhooks.suborg.eventpayloadbuilder.SubOrgEventPayloadUtils.USERNAME_CLAIM_URI;
import static org.wso2.identity.integration.test.webhooks.suborg.eventpayloadbuilder.SubOrgEventPayloadUtils.createClaim;
import static org.wso2.identity.integration.test.webhooks.suborg.eventpayloadbuilder.SubOrgEventPayloadUtils.createSubOrgUserObject;

/**
 * Utility class to build expected event payloads for the user management events triggered when an administrator
 * manages a user of a sub organization.
 */
public class SubOrgUserManagementEventTestExpectedEventPayloadBuilder {

    /**
     * Builds the expected payload for a user created event of a sub organization user.
     *
     * @param userId           ID of the created user.
     * @param username         Username of the created user.
     * @param email            Email address of the created user.
     * @param givenName        Given name of the created user.
     * @param lastName         Last name of the created user.
     * @param rootTenantDomain Tenant domain of the root organization.
     * @param subOrganization  Sub organization of the user, which is also the organization the event belongs to.
     * @return JSONObject representing the expected user created event payload.
     * @throws JSONException if an error occurs while building the payload.
     */
    public static JSONObject buildExpectedUserCreatedEventPayload(String userId, String username, String email,
                                                                  String givenName, String lastName,
                                                                  String rootTenantDomain,
                                                                  JSONObject subOrganization) throws JSONException {

        JSONArray claims = new JSONArray();
        claims.put(createClaim(USERNAME_CLAIM_URI, username));
        claims.put(createClaim(EMAIL_CLAIM_URI, email));
        claims.put(createClaim(GIVEN_NAME_CLAIM_URI, givenName));
        claims.put(createClaim(LAST_NAME_CLAIM_URI, lastName));

        JSONObject user = createSubOrgUserObject(userId, rootTenantDomain, subOrganization);
        user.put("claims", claims);
        return createUserEventPayload(user, rootTenantDomain, subOrganization, "REGISTER");
    }

    /**
     * Builds the expected payload for a user profile updated event of a sub organization user, when the given name
     * of the user is updated.
     *
     * @param userId           ID of the updated user.
     * @param givenName        Updated given name of the user.
     * @param rootTenantDomain Tenant domain of the root organization.
     * @param subOrganization  Sub organization of the user, which is also the organization the event belongs to.
     * @return JSONObject representing the expected user profile updated event payload.
     * @throws JSONException if an error occurs while building the payload.
     */
    public static JSONObject buildExpectedUserProfileUpdatedEventPayload(String userId, String givenName,
                                                                         String rootTenantDomain,
                                                                         JSONObject subOrganization)
            throws JSONException {

        JSONArray updatedClaims = new JSONArray();
        updatedClaims.put(createClaim(GIVEN_NAME_CLAIM_URI, givenName));

        JSONObject user = createSubOrgUserObject(userId, rootTenantDomain, subOrganization);
        user.put("updatedClaims", updatedClaims);
        return createUserEventPayload(user, rootTenantDomain, subOrganization, "PROFILE_UPDATE");
    }

    /**
     * Builds the expected payload for a user account locked event of a sub organization user.
     *
     * @param userId           ID of the locked user.
     * @param email            Email address of the locked user.
     * @param rootTenantDomain Tenant domain of the root organization.
     * @param subOrganization  Sub organization of the user, which is also the organization the event belongs to.
     * @return JSONObject representing the expected user account locked event payload.
     * @throws JSONException if an error occurs while building the payload.
     */
    public static JSONObject buildExpectedUserAccountLockedEventPayload(String userId, String email,
                                                                        String rootTenantDomain,
                                                                        JSONObject subOrganization)
            throws JSONException {

        JSONObject user = createSubOrgUserObject(userId, rootTenantDomain, subOrganization);
        user.put("claims", new JSONArray().put(createClaim(EMAIL_CLAIM_URI, email)));
        return createUserEventPayload(user, rootTenantDomain, subOrganization, "USER_ACCOUNT_LOCK");
    }

    /**
     * Builds the expected payload for a user account unlocked event of a sub organization user.
     *
     * @param userId           ID of the unlocked user.
     * @param email            Email address of the unlocked user.
     * @param rootTenantDomain Tenant domain of the root organization.
     * @param subOrganization  Sub organization of the user, which is also the organization the event belongs to.
     * @return JSONObject representing the expected user account unlocked event payload.
     * @throws JSONException if an error occurs while building the payload.
     */
    public static JSONObject buildExpectedUserAccountUnlockedEventPayload(String userId, String email,
                                                                          String rootTenantDomain,
                                                                          JSONObject subOrganization)
            throws JSONException {

        JSONObject user = createSubOrgUserObject(userId, rootTenantDomain, subOrganization);
        user.put("claims", new JSONArray().put(createClaim(EMAIL_CLAIM_URI, email)));
        return createUserEventPayload(user, rootTenantDomain, subOrganization, "USER_ACCOUNT_UNLOCK");
    }

    /**
     * Builds the expected payload for a user deleted event of a sub organization user.
     *
     * @param userId           ID of the deleted user.
     * @param username         Username of the deleted user.
     * @param rootTenantDomain Tenant domain of the root organization.
     * @param subOrganization  Sub organization of the user, which is also the organization the event belongs to.
     * @return JSONObject representing the expected user deleted event payload.
     * @throws JSONException if an error occurs while building the payload.
     */
    public static JSONObject buildExpectedUserDeletedEventPayload(String userId, String username,
                                                                  String rootTenantDomain,
                                                                  JSONObject subOrganization) throws JSONException {

        JSONObject user = createSubOrgUserObject(userId, rootTenantDomain, subOrganization);
        user.put("claims", new JSONArray().put(createClaim(USERNAME_CLAIM_URI, username)));
        return createUserEventPayload(user, rootTenantDomain, subOrganization, "USER_ACCOUNT_DELETE");
    }

    private static JSONObject createUserEventPayload(JSONObject user, String rootTenantDomain,
                                                     JSONObject subOrganization, String action)
            throws JSONException {

        JSONObject userEvent = new JSONObject();
        userEvent.put("initiatorType", "ADMIN");
        userEvent.put("initiatorIpAddress", "dummy-initiator-ip-address");
        userEvent.put("user", user);
        userEvent.put("tenant", EventPayloadUtils.createTenantObject(rootTenantDomain));
        userEvent.put("organization", subOrganization);
        userEvent.put("userStore", EventPayloadUtils.createUserStoreObject());
        userEvent.put("action", action);
        return userEvent;
    }
}

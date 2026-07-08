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

package org.wso2.identity.integration.test.webhooks.rolemanagement.eventpayloadbuilder;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.wso2.identity.integration.test.webhooks.util.EventPayloadUtils;

/**
 * Utility class to build expected event payloads for the role management webhook test cases.
 * <p>
 * Values which are not deterministic across test runs (role id and role ref) are set to the {@code "dummy..."}
 * sentinel so that the {@code EventPayloadValidator} skips value comparison for them while still enforcing the presence
 * of the key. The application id/name, user ids, group ids and idp ids are known to the test and are therefore
 * asserted exactly.
 */
public class AdminInitRoleManagementEventTestExpectedEventPayloadBuilder {

    private static final String INITIATOR_TYPE_ADMIN = "ADMIN";
    private static final String INITIATOR_IP_ADDRESS = "127.0.0.1";
    private static final String ACTION_ROLE_CREATE = "ROLE_CREATE";
    private static final String ACTION_ROLE_UPDATE = "ROLE_UPDATE";
    private static final String ACTION_ROLE_DELETE = "ROLE_DELETE";
    private static final String AUDIENCE_TYPE_APPLICATION = "application";
    private static final String USER_STORE_PRIMARY = "PRIMARY";
    private static final String USER_STORE_AGENT = "AGENT";
    private static final String USERNAME_CLAIM_URI = "http://wso2.org/claims/username";
    private static final String AGENT_NAME_CLAIM_URI = "http://wso2.org/claims/agent/Name";

    /**
     * Builds the expected payload for a role created event with an application audience and an assigned permission.
     */
    public static JSONObject buildExpectedRoleCreatedEventPayload(String tenantDomain, String appId, String appName,
                                                                  String roleName, String permission) throws Exception {

        JSONObject event = buildBaseRoleEvent(tenantDomain, ACTION_ROLE_CREATE);

        JSONObject role = buildBaseRole(roleName, appId, appName);
        role.put("permissions", new JSONArray().put(permission));
        event.put("role", role);

        return event;
    }

    /**
     * Builds the expected payload for a role permissions updated event where a single permission is added.
     */
    public static JSONObject buildExpectedRolePermissionsUpdatedEventPayload(String tenantDomain, String appId,
                                                                             String appName, String roleName,
                                                                             String addedPermission)
            throws Exception {

        JSONObject event = buildBaseRoleEvent(tenantDomain, ACTION_ROLE_UPDATE);

        JSONObject role = buildBaseRole(roleName, appId, appName);
        role.put("addedPermissions", new JSONArray().put(addedPermission));
        event.put("role", role);

        return event;
    }

    /**
     * Builds the expected payload for a role groups updated event where a single group is added.
     */
    public static JSONObject buildExpectedRoleGroupsAddedEventPayload(String tenantDomain, String appId, String appName,
                                                                      String roleName, String groupId, String groupName)
            throws Exception {

        JSONObject event = buildBaseRoleEvent(tenantDomain, ACTION_ROLE_UPDATE);

        JSONObject role = buildBaseRole(roleName, appId, appName);
        role.put("addedGroups", new JSONArray().put(createRoleGroup(groupId, groupName)));
        event.put("role", role);

        return event;
    }

    /**
     * Builds the expected payload for a role groups updated event where a group is added and another is removed.
     */
    public static JSONObject buildExpectedRoleGroupsUpdatedEventPayload(String tenantDomain, String appId,
                                                                        String appName, String roleName,
                                                                        String addedGroupId, String addedGroupName,
                                                                        String removedGroupId, String removedGroupName)
            throws Exception {

        JSONObject event = buildBaseRoleEvent(tenantDomain, ACTION_ROLE_UPDATE);

        JSONObject role = buildBaseRole(roleName, appId, appName);
        role.put("addedGroups", new JSONArray().put(createRoleGroup(addedGroupId, addedGroupName)));
        role.put("removedGroups", new JSONArray().put(createRoleGroup(removedGroupId, removedGroupName)));
        event.put("role", role);

        return event;
    }

    /**
     * Builds the expected payload for a role idp groups updated event where a single idp group is added.
     */
    public static JSONObject buildExpectedRoleIdpGroupsUpdatedEventPayload(String tenantDomain, String appId,
                                                                           String appName, String roleName,
                                                                           String idpGroupId, String idpGroupName,
                                                                           String idpId, String idpName)
            throws Exception {

        JSONObject event = buildBaseRoleEvent(tenantDomain, ACTION_ROLE_UPDATE);

        JSONObject role = buildBaseRole(roleName, appId, appName);
        role.put("addedIdpGroups",
                new JSONArray().put(createIdpGroup(idpGroupId, idpGroupName, idpId, idpName)));
        event.put("role", role);

        return event;
    }

    /**
     * Builds the expected payload for a role users updated event where a single user is added.
     */
    public static JSONObject buildExpectedRoleUsersAddedEventPayload(String tenantDomain, String appId, String appName,
                                                                     String roleName, String userId, String username)
            throws Exception {

        JSONObject event = buildBaseRoleEvent(tenantDomain, ACTION_ROLE_UPDATE);

        JSONObject role = buildBaseRole(roleName, appId, appName);
        role.put("addedUsers", new JSONArray().put(createRoleUser(userId, username)));
        event.put("role", role);

        return event;
    }

    /**
     * Builds the expected payload for a role users updated event where a regular user and an agent are added. The role
     * management runtime emits the user additions and removals as separate events, hence the add and remove payloads
     * are built independently.
     */
    public static JSONObject buildExpectedRoleUsersAndAgentAddedEventPayload(String tenantDomain, String appId,
                                                                             String appName, String roleName,
                                                                             String userId, String username,
                                                                             String agentId, String agentName)
            throws Exception {

        JSONObject event = buildBaseRoleEvent(tenantDomain, ACTION_ROLE_UPDATE);

        JSONObject role = buildBaseRole(roleName, appId, appName);
        role.put("addedUsers", new JSONArray()
                .put(createRoleUser(userId, username))
                .put(createRoleAgent(agentId, agentName)));
        event.put("role", role);

        return event;
    }

    /**
     * Builds the expected payload for a role users updated event where all the users (two regular users and an agent)
     * are removed. A SCIM PATCH remove on the {@code users} path (without a value filter) removes every member, hence
     * all current members appear under {@code removedUsers}.
     */
    public static JSONObject buildExpectedRoleAllUsersRemovedEventPayload(String tenantDomain, String appId,
                                                                          String appName, String roleName,
                                                                          String userId1, String username1,
                                                                          String userId2, String username2,
                                                                          String agentId, String agentName)
            throws Exception {

        JSONObject event = buildBaseRoleEvent(tenantDomain, ACTION_ROLE_UPDATE);

        JSONObject role = buildBaseRole(roleName, appId, appName);
        role.put("removedUsers", new JSONArray()
                .put(createRoleUser(userId1, username1))
                .put(createRoleUser(userId2, username2))
                .put(createRoleAgent(agentId, agentName)));
        event.put("role", role);

        return event;
    }

    /**
     * Builds the expected payload for a role meta updated event (role rename).
     */
    public static JSONObject buildExpectedRoleMetaUpdatedEventPayload(String tenantDomain, String appId, String appName,
                                                                      String updatedRoleName) throws Exception {

        JSONObject event = buildBaseRoleEvent(tenantDomain, ACTION_ROLE_UPDATE);
        event.put("role", buildBaseRole(updatedRoleName, appId, appName));

        return event;
    }

    /**
     * Builds the expected payload for a role deleted event. The role object contains only its id.
     */
    public static JSONObject buildExpectedRoleDeletedEventPayload(String tenantDomain) throws Exception {

        JSONObject event = buildBaseRoleEvent(tenantDomain, ACTION_ROLE_DELETE);

        JSONObject role = new JSONObject();
        role.put("id", "dummy-role-id");
        event.put("role", role);

        return event;
    }

    private static JSONObject buildBaseRoleEvent(String tenantDomain, String action) throws Exception {

        JSONObject event = new JSONObject();
        event.put("initiatorType", INITIATOR_TYPE_ADMIN);
        event.put("initiatorIpAddress", INITIATOR_IP_ADDRESS);
        event.put("tenant", EventPayloadUtils.createTenantObject(tenantDomain));
        event.put("organization", EventPayloadUtils.createOrganizationObject(tenantDomain));
        event.put("action", action);
        return event;
    }

    private static JSONObject buildBaseRole(String roleName, String appId, String appName) throws JSONException {

        JSONObject role = new JSONObject();
        role.put("id", "dummy-role-id");
        role.put("name", roleName);
        role.put("audience", createApplicationAudience(appId, appName));
        role.put("ref", "dummy-role-ref");
        return role;
    }

    private static JSONObject createApplicationAudience(String appId, String appName) throws JSONException {

        JSONObject audience = new JSONObject();
        audience.put("type", AUDIENCE_TYPE_APPLICATION);
        audience.put("value", appId);
        audience.put("display", appName);
        return audience;
    }

    private static JSONObject createRoleUser(String userId, String username) throws JSONException {

        JSONObject user = new JSONObject();
        user.put("id", userId);
        user.put("userStoreDomain", USER_STORE_PRIMARY);
        user.put("claims", new JSONArray()
                .put(new JSONObject().put("uri", USERNAME_CLAIM_URI).put("value", username)));
        return user;
    }

    private static JSONObject createRoleAgent(String agentId, String agentName) throws JSONException {

        JSONObject agent = new JSONObject();
        agent.put("id", agentId);
        agent.put("userStoreDomain", USER_STORE_AGENT);
        agent.put("claims", new JSONArray()
                .put(new JSONObject().put("uri", USERNAME_CLAIM_URI).put("value", agentId))
                .put(new JSONObject().put("uri", AGENT_NAME_CLAIM_URI).put("value", agentName)));
        return agent;
    }

    private static JSONObject createRoleGroup(String groupId, String groupName) throws JSONException {

        JSONObject group = new JSONObject();
        group.put("id", groupId);
        group.put("groupName", groupName);
        group.put("userStoreDomain", USER_STORE_PRIMARY);
        return group;
    }

    private static JSONObject createIdpGroup(String groupId, String groupName, String idpId, String idpName)
            throws JSONException {

        JSONObject idpGroup = new JSONObject();
        idpGroup.put("groupId", groupId);
        idpGroup.put("groupName", groupName);
        idpGroup.put("idpId", idpId);
        idpGroup.put("idpName", idpName);
        return idpGroup;
    }

}

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

package org.wso2.identity.integration.test.webhooks.usermanagement.eventpayloadbuilder;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.wso2.carbon.utils.multitenancy.MultitenantConstants;

/**
 * Utility class to build expected event payloads for SCIM2 user test cases.
 */
public class AdminInitUserManagementEventTestExpectedEventPayloadBuilder {

    /**
     * Builds the expected payload for a user created event in the context of test case.
     *
     * @param userId       ID of the user being created
     * @param tenantDomain Tenant domain of the user
     * @return JSONObject representing the expected user created event payload
     * @throws Exception if an error occurs while building the payload
     */
    public static JSONObject buildExpectedUserCreatedEventPayloadForTestCreateUser(String userId,
                                                                                   String tenantDomain)
            throws Exception {

        JSONObject userCreatedEvent = new JSONObject();

        userCreatedEvent.put("initiatorType", "ADMIN");
        userCreatedEvent.put("user", createUserObjectForUserCreate(userId, tenantDomain));
        userCreatedEvent.put("tenant", createTenantObject(tenantDomain));
        userCreatedEvent.put("organization", createOrganizationObject(tenantDomain));
        userCreatedEvent.put("userStore", createUserStoreObject());
        userCreatedEvent.put("action", "REGISTER");

        return userCreatedEvent;
    }

    /**
     * Builds the expected payload for a user registration success event in the context of test case.
     *
     * @param userId       ID of the user being registered
     * @param tenantDomain Tenant domain of the user
     * @return JSONObject representing the expected user registration success event payload
     * @throws Exception if an error occurs while building the payload
     */
    public static JSONObject buildExpectedRegistrationSuccessEventPayloadForTestCreateUser(String userId,
                                                                                           String tenantDomain)
            throws Exception {

        JSONObject registrationSuccessEvent = new JSONObject();

        registrationSuccessEvent.put("initiatorType", "ADMIN");
        registrationSuccessEvent.put("user", createUserObjectForUserCreate(userId, tenantDomain));
        registrationSuccessEvent.put("tenant", createTenantObject(tenantDomain));
        registrationSuccessEvent.put("organization", createOrganizationObject(tenantDomain));
        registrationSuccessEvent.put("userStore", createUserStoreObject());
        registrationSuccessEvent.put("action", "REGISTER");

        return registrationSuccessEvent;
    }

    /**
     * Builds the expected payload for a user profile updated event when no account state management claims gets updated
     * in the context of test case.
     *
     * @param userId       ID of the user whose profile is being updated
     * @param tenantDomain Tenant domain of the user
     * @return JSONObject representing the expected user profile updated event payload
     * @throws Exception if an error occurs while building the payload
     */
    public static JSONObject buildExpectedUserProfileUpdatedEventPayloadWithoutAnyAccountStateManagementClaims(
            String userId, String tenantDomain) throws Exception {

        JSONObject userProfileUpdateEvent = new JSONObject();

        userProfileUpdateEvent.put("initiatorType", "ADMIN");
        userProfileUpdateEvent.put("user",
                createUserObjectForUserProfileUpdateWithoutAnyAccountStateManagementClaims(userId, tenantDomain));
        userProfileUpdateEvent.put("tenant", createTenantObject(tenantDomain));
        userProfileUpdateEvent.put("organization", createOrganizationObject(tenantDomain));
        userProfileUpdateEvent.put("userStore", createUserStoreObject());
        // todo: This value should be updated to 'PROFILE_UPDATE' in payload
        userProfileUpdateEvent.put("action", "UPDATE");

        return userProfileUpdateEvent;
    }

    /**
     * Builds the expected payload for a user profile updated event when account lock claim gets updated
     * along with profile in the context of the test case.
     *
     * @param userId       ID of the user whose profile is being updated
     * @param tenantDomain Tenant domain of the user
     * @return JSONObject representing the expected user profile updated event payload
     * @throws Exception if an error occurs while building the payload
     */
    public static JSONObject buildExpectedUserProfileUpdatedEventPayloadWithAccountLockClaim(
            String userId, String tenantDomain) throws Exception {

        JSONObject userProfileUpdateEvent = new JSONObject();

        userProfileUpdateEvent.put("initiatorType", "ADMIN");
        userProfileUpdateEvent.put("user",
                createUserObjectForUserProfileUpdateWithAccountLockClaim(userId, tenantDomain));
        userProfileUpdateEvent.put("tenant", createTenantObject(tenantDomain));
        userProfileUpdateEvent.put("organization", createOrganizationObject(tenantDomain));
        userProfileUpdateEvent.put("userStore", createUserStoreObject());
        // todo: This value should be updated to 'PROFILE_UPDATE' in payload
        userProfileUpdateEvent.put("action", "UPDATE");

        return userProfileUpdateEvent;
    }

    /**
     * Builds the expected payload for an account locked event in the context of test case.
     *
     * @param userId       ID of the user whose account is being locked
     * @param tenantDomain Tenant domain of the user
     * @return JSONObject representing the expected account locked event payload
     * @throws Exception if an error occurs while building the payload
     */
    public static JSONObject buildExpectedAccountLockedEventPayload(String userId, String tenantDomain)
            throws Exception {

        JSONObject accountLockedEvent = new JSONObject();

        accountLockedEvent.put("user", createUserObjectForUserAccountStateChange(userId, tenantDomain));
        accountLockedEvent.put("tenant", createTenantObject(tenantDomain));
        accountLockedEvent.put("organization", createOrganizationObject(tenantDomain));
        accountLockedEvent.put("userStore", createUserStoreObject());

        return accountLockedEvent;
    }

    /**
     * Builds the expected payload for an account unlocked event in the context of test case.
     *
     * @param userId       ID of the user whose account is being unlocked
     * @param tenantDomain Tenant domain of the user
     * @return JSONObject representing the expected account unlocked event payload
     * @throws Exception if an error occurs while building the payload
     */
    public static JSONObject buildExpectedAccountUnlockedEventPayload(String userId, String tenantDomain)
            throws Exception {

        JSONObject accountLockedEvent = new JSONObject();

        accountLockedEvent.put("user", createUserObjectForUserAccountStateChange(userId, tenantDomain));
        accountLockedEvent.put("tenant", createTenantObject(tenantDomain));
        accountLockedEvent.put("organization", createOrganizationObject(tenantDomain));
        accountLockedEvent.put("userStore", createUserStoreObject());

        return accountLockedEvent;
    }

    /**
     * Builds the expected payload for a user profile updated event when account disable claim gets updated
     * along with profile in the context of the test case.
     *
     * @param userId       ID of the user whose profile is being updated
     * @param tenantDomain Tenant domain of the user
     * @return JSONObject representing the expected user profile updated event payload
     * @throws Exception if an error occurs while building the payload
     */
    public static JSONObject buildExpectedUserProfileUpdatedEventPayloadWithAccountDisableClaim(
            String userId, String tenantDomain) throws Exception {

        JSONObject userProfileUpdateEvent = new JSONObject();

        userProfileUpdateEvent.put("initiatorType", "ADMIN");
        userProfileUpdateEvent.put("user",
                createUserObjectForUserProfileUpdateWithAccountDisableClaim(userId, tenantDomain));
        userProfileUpdateEvent.put("tenant", createTenantObject(tenantDomain));
        userProfileUpdateEvent.put("organization", createOrganizationObject(tenantDomain));
        userProfileUpdateEvent.put("userStore", createUserStoreObject());
        // todo: This value should be updated to 'PROFILE_UPDATE' in payload
        userProfileUpdateEvent.put("action", "UPDATE");

        return userProfileUpdateEvent;
    }

    /**
     * Builds the expected payload for an account disabled event in the context of test case.
     *
     * @param userId       ID of the user whose account is being disabled
     * @param tenantDomain Tenant domain of the user
     * @return JSONObject representing the expected account disabled event payload
     * @throws Exception if an error occurs while building the payload
     */
    public static JSONObject buildExpectedAccountDisabledEventPayload(String userId, String tenantDomain)
            throws Exception {

        JSONObject userDisableEvent = new JSONObject();

        userDisableEvent.put("initiatorType", "ADMIN");
        userDisableEvent.put("user", createUserObjectForUserAccountStateChange(userId, tenantDomain));
        userDisableEvent.put("tenant", createTenantObject(tenantDomain));
        userDisableEvent.put("organization", createOrganizationObject(tenantDomain));
        userDisableEvent.put("userStore", createUserStoreObject());

        return userDisableEvent;
    }

    /**
     * Builds the expected payload for an account enabled event in the context of test case.
     *
     * @param userId       ID of the user whose account is being enabled
     * @param tenantDomain Tenant domain of the user
     * @return JSONObject representing the expected account enabled event payload
     * @throws Exception if an error occurs while building the payload
     */
    public static JSONObject buildExpectedAccountEnabledEventPayload(String userId, String tenantDomain)
            throws Exception {

        JSONObject userDisableEvent = new JSONObject();

        userDisableEvent.put("initiatorType", "ADMIN");
        userDisableEvent.put("user", createUserObjectForUserAccountStateChange(userId, tenantDomain));
        userDisableEvent.put("tenant", createTenantObject(tenantDomain));
        userDisableEvent.put("organization", createOrganizationObject(tenantDomain));
        userDisableEvent.put("userStore", createUserStoreObject());

        return userDisableEvent;
    }

    /**
     * Builds the expected payload for a user deleted event in the context of test case.
     *
     * @param userId       ID of the user being deleted
     * @param tenantDomain Tenant domain of the user
     * @return JSONObject representing the expected user deleted event payload
     * @throws Exception if an error occurs while building the payload
     */
    public static JSONObject buildExpectedUserDeletedEventPayload(String userId, String tenantDomain)
            throws Exception {

        JSONObject userDeletedEvent = new JSONObject();

        userDeletedEvent.put("initiatorType", "ADMIN");
        userDeletedEvent.put("user", createUserObjectForUserDelete(userId, tenantDomain));
        userDeletedEvent.put("tenant", createTenantObject(tenantDomain));
        userDeletedEvent.put("organization", createOrganizationObject(tenantDomain));
        userDeletedEvent.put("userStore", createUserStoreObject());

        return userDeletedEvent;
    }

    /**
     * Builds the expected payload for a user registration failed event in the context of test case.
     *
     * @param tenantDomain      Tenant domain of the user
     * @param reasonDescription Description of the reason for the registration failure
     * @return JSONObject representing the expected user registration failed event payload
     * @throws Exception if an error occurs while building the payload
     */
    public static JSONObject buildExpectedRegistrationFailedEventPayload(String tenantDomain,
                                                                         String reasonDescription)
            throws Exception {

        JSONObject registrationFailedEvent = new JSONObject();

        registrationFailedEvent.put("initiatorType", "ADMIN");
        registrationFailedEvent.put("user", createUserObjectForUserCreateFailure(tenantDomain));
        registrationFailedEvent.put("tenant", createTenantObject(tenantDomain));
        registrationFailedEvent.put("organization", createOrganizationObject(tenantDomain));
        registrationFailedEvent.put("action", "REGISTER");
        registrationFailedEvent.put("reason", createReasonObject(reasonDescription));

        return registrationFailedEvent;
    }

    private static JSONObject createUserObjectForUserCreate(String userId, String tenantDomain) throws JSONException {

        JSONObject user = new JSONObject();
        user.put("id", userId);

        JSONArray claims = new JSONArray();
        claims.put(new JSONObject().put("uri", "http://wso2.org/claims/username").put("value", "test-user"));
        claims.put(
                new JSONObject().put("uri", "http://wso2.org/claims/emailaddress").put("value", "test-user@test.com"));
        claims.put(new JSONObject().put("uri", "http://wso2.org/claims/lastname").put("value", "test-user-last-name"));
        claims.put(
                new JSONObject().put("uri", "http://wso2.org/claims/givenname").put("value", "test-user-given-name"));
        user.put("claims", claims);

        JSONObject userOrganization = new JSONObject();
        userOrganization.put("id", "dummy-org-id");
        if (tenantDomain.equals(MultitenantConstants.SUPER_TENANT_DOMAIN_NAME)) {
            userOrganization.put("name", "Super");
        } else {
            userOrganization.put("name", tenantDomain);
        }
        userOrganization.put("orgHandle", tenantDomain);
        userOrganization.put("depth", 0);
        user.put("organization", userOrganization);

        if (tenantDomain.equals(MultitenantConstants.SUPER_TENANT_DOMAIN_NAME)) {
            user.put("ref", "https://localhost:9853/scim2/Users/" + userId);
        } else {
            user.put("ref", "https://localhost:9853/t/" + tenantDomain + "/scim2/Users/" + userId);
        }

        return user;
    }

    private static JSONObject createUserObjectForUserProfileUpdateWithoutAnyAccountStateManagementClaims(String userId,
                                                                                                         String tenantDomain)
            throws JSONException {

        JSONObject user = new JSONObject();
        user.put("id", userId);

        JSONObject userOrganization = new JSONObject();
        userOrganization.put("id", "dummy-org-id");
        if (tenantDomain.equals(MultitenantConstants.SUPER_TENANT_DOMAIN_NAME)) {
            userOrganization.put("name", "Super");
        } else {
            userOrganization.put("name", tenantDomain);
        }
        userOrganization.put("orgHandle", tenantDomain);
        userOrganization.put("depth", 0);
        user.put("organization", userOrganization);

        if (tenantDomain.equals(MultitenantConstants.SUPER_TENANT_DOMAIN_NAME)) {
            user.put("ref", "https://localhost:9853/scim2/Users/" + userId);
        } else {
            user.put("ref", "https://localhost:9853/t/" + tenantDomain + "/scim2/Users/" + userId);
        }

        // Added claims
        JSONArray addedClaims = new JSONArray();
        addedClaims.put(new JSONObject().put("uri", "http://wso2.org/claims/country").put("value", "United States"));
        addedClaims.put(
                new JSONObject().put("uri", "http://wso2.org/claims/nickname").put("value", "test-user-nickname"));
        user.put("addedClaims", addedClaims);

        // Updated claims
        JSONArray updatedClaims = new JSONArray();
        updatedClaims.put(new JSONObject()
                .put("uri", "http://wso2.org/claims/emailAddresses")
                .put("value", new JSONArray()
                        .put("test-user-personal@test.com")
                        .put("test-user-work@test.com")));
        user.put("updatedClaims", updatedClaims);

        // Removed claims
        JSONArray removedClaims = new JSONArray();
        removedClaims.put(
                new JSONObject().put("uri", "http://wso2.org/claims/givenname").put("value", "test-user-given-name"));
        user.put("removedClaims", removedClaims);

        return user;
    }

    private static JSONObject createUserObjectForUserProfileUpdateWithAccountLockClaim(String userId,
                                                                                       String tenantDomain)
            throws JSONException {

        JSONObject user = new JSONObject();
        user.put("id", userId);

        JSONObject userOrganization = new JSONObject();
        userOrganization.put("id", "dummy-org-id");
        if (tenantDomain.equals(MultitenantConstants.SUPER_TENANT_DOMAIN_NAME)) {
            userOrganization.put("name", "Super");
        } else {
            userOrganization.put("name", tenantDomain);
        }
        userOrganization.put("orgHandle", tenantDomain);
        userOrganization.put("depth", 0);
        user.put("organization", userOrganization);

        if (tenantDomain.equals(MultitenantConstants.SUPER_TENANT_DOMAIN_NAME)) {
            user.put("ref", "https://localhost:9853/scim2/Users/" + userId);
        } else {
            user.put("ref", "https://localhost:9853/t/" + tenantDomain + "/scim2/Users/" + userId);
        }

        JSONArray addedClaims = new JSONArray();
        addedClaims.put(
                new JSONObject().put("uri", "http://wso2.org/claims/givenname").put("value", "test-user-given-name"));
        addedClaims.put(
                new JSONObject().put("uri", "http://wso2.org/claims/identity/accountLocked").put("value", "true"));
        user.put("addedClaims", addedClaims);

        return user;
    }

    private static JSONObject createUserObjectForUserAccountStateChange(String userId, String tenantDomain)
            throws Exception {

        JSONObject user = new JSONObject();
        user.put("id", userId);

        JSONArray claims = new JSONArray();
        claims.put(new JSONObject()
                .put("uri", "http://wso2.org/claims/emailaddress")
                .put("value", "test-user@test.com"));
        user.put("claims", claims);

        JSONObject userOrganization = new JSONObject();
        userOrganization.put("id", "dummy-org-id");
        if (tenantDomain.equals(MultitenantConstants.SUPER_TENANT_DOMAIN_NAME)) {
            userOrganization.put("name", "Super");
        } else {
            userOrganization.put("name", tenantDomain);
        }
        userOrganization.put("orgHandle", tenantDomain);
        userOrganization.put("depth", 0);
        user.put("organization", userOrganization);

        if (tenantDomain.equals(MultitenantConstants.SUPER_TENANT_DOMAIN_NAME)) {
            user.put("ref", "https://localhost:9853/scim2/Users/" + userId);
        } else {
            user.put("ref", "https://localhost:9853/t/" + tenantDomain + "/scim2/Users/" + userId);
        }
        return user;
    }

    private static JSONObject createUserObjectForUserProfileUpdateWithAccountDisableClaim(String userId,
                                                                                          String tenantDomain)
            throws JSONException {

        JSONObject user = new JSONObject();
        user.put("id", userId);

        JSONObject userOrganization = new JSONObject();
        userOrganization.put("id", "dummy-org-id");
        if (tenantDomain.equals(MultitenantConstants.SUPER_TENANT_DOMAIN_NAME)) {
            userOrganization.put("name", "Super");
        } else {
            userOrganization.put("name", tenantDomain);
        }
        userOrganization.put("orgHandle", tenantDomain);
        userOrganization.put("depth", 0);
        user.put("organization", userOrganization);

        if (tenantDomain.equals(MultitenantConstants.SUPER_TENANT_DOMAIN_NAME)) {
            user.put("ref", "https://localhost:9853/scim2/Users/" + userId);
        } else {
            user.put("ref", "https://localhost:9853/t/" + tenantDomain + "/scim2/Users/" + userId);
        }

        JSONArray addedClaims = new JSONArray();
        addedClaims.put(
                new JSONObject().put("uri", "http://wso2.org/claims/identity/accountDisabled").put("value", "true"));
        user.put("addedClaims", addedClaims);

        JSONArray updatedClaims = new JSONArray();
        updatedClaims.put(new JSONObject().put("uri", "http://wso2.org/claims/givenname")
                .put("value", "test-user-updated-given-name"));
        user.put("updatedClaims", updatedClaims);

        return user;
    }

    private static JSONObject createUserObjectForUserDelete(String userId, String tenantDomain) throws JSONException {

        JSONObject user = new JSONObject();
        user.put("id", userId);

        JSONArray claims = new JSONArray();
        claims.put(new JSONObject().put("uri", "http://wso2.org/claims/username").put("value", "test-user"));
        user.put("claims", claims);

        JSONObject userOrganization = new JSONObject();
        userOrganization.put("id", "dummy-org-id");
        if (tenantDomain.equals(MultitenantConstants.SUPER_TENANT_DOMAIN_NAME)) {
            userOrganization.put("name", "Super");
        } else {
            userOrganization.put("name", tenantDomain);
        }
        userOrganization.put("orgHandle", tenantDomain);
        userOrganization.put("depth", 0);
        user.put("organization", userOrganization);

        if (tenantDomain.equals(MultitenantConstants.SUPER_TENANT_DOMAIN_NAME)) {
            user.put("ref", "https://localhost:9853/scim2/Users/" + userId);
        } else {
            user.put("ref", "https://localhost:9853/t/" + tenantDomain + "/scim2/Users/" + userId);
        }

        return user;
    }

    private static JSONObject createUserObjectForUserCreateFailure(String tenantDomain)
            throws JSONException {

        JSONObject user = new JSONObject();
        user.put("id", "dummy-user-id");
        user.put("ref", "https://localhost:9853/scim2/Users/dummy-user-id");

        JSONObject userOrganization = new JSONObject();
        userOrganization.put("id", "dummy-org-id");
        if (tenantDomain.equals(MultitenantConstants.SUPER_TENANT_DOMAIN_NAME)) {
            userOrganization.put("name", "Super");
        } else {
            userOrganization.put("name", tenantDomain);
        }
        userOrganization.put("orgHandle", tenantDomain);
        userOrganization.put("depth", 0);
        user.put("organization", userOrganization);

        return user;
    }

    private static JSONObject createTenantObject(String tenantDomain) throws JSONException {

        JSONObject tenant = new JSONObject();
        tenant.put("id", "dummy-tenant-id");
        tenant.put("name", tenantDomain);
        return tenant;
    }

    private static JSONObject createOrganizationObject(String tenantDomain) throws JSONException {

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

    private static JSONObject createUserStoreObject() throws JSONException {

        JSONObject userStore = new JSONObject();
        userStore.put("id", "UFJJTUFSWQ==");
        userStore.put("name", "PRIMARY");
        return userStore;
    }

    private static JSONObject createReasonObject(String reasonDescription) throws JSONException {

        JSONObject reason = new JSONObject();
        reason.put("description", reasonDescription);
        return reason;
    }
}

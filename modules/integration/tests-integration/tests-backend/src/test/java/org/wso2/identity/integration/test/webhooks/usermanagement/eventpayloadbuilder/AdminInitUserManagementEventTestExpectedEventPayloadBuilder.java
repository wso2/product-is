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
     * Builds the expected payload for a user created event in the context of a test case.
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
        userCreatedEvent.put("user", createUserObject(userId, tenantDomain));
        userCreatedEvent.put("tenant", createTenantObject(tenantDomain));
        userCreatedEvent.put("userStore", createUserStoreObject());
        userCreatedEvent.put("action", "REGISTER");

        return userCreatedEvent;
    }

    /**
     * Builds the expected payload for a user registration success event in the context of a test case.
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
        registrationSuccessEvent.put("user", createUserObject(userId, tenantDomain));
        registrationSuccessEvent.put("tenant", createTenantObject(tenantDomain));
        registrationSuccessEvent.put("userStore", createUserStoreObject());
        registrationSuccessEvent.put("action", "REGISTER");

        return registrationSuccessEvent;
    }

    /**
     * Builds the expected payload for a user deleted event.
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
        userDeletedEvent.put("user", createUserObjectForDelete(userId, tenantDomain));
        userDeletedEvent.put("tenant", createTenantObject(tenantDomain));
        userDeletedEvent.put("userStore", createUserStoreObject());

        return userDeletedEvent;
    }

    /**
     * Builds the expected payload for a user registration failed event.
     *
     * @param userId            ID of the user whose registration failed
     * @param tenantDomain      Tenant domain of the user
     * @param reasonDescription Description of the reason for the registration failure
     * @return JSONObject representing the expected user registration failed event payload
     * @throws Exception if an error occurs while building the payload
     */
    public static JSONObject buildExpectedRegistrationFailedEventPayload(String userId, String tenantDomain,
                                                                         String reasonDescription)
            throws Exception {

        JSONObject registrationFailedEvent = new JSONObject();

        registrationFailedEvent.put("initiatorType", "ADMIN");
        registrationFailedEvent.put("tenant", createTenantObject(tenantDomain));
        registrationFailedEvent.put("action", "REGISTER");
        registrationFailedEvent.put("reason", createReasonObject(reasonDescription));

        return registrationFailedEvent;
    }

    private static JSONObject createUserObject(String userId, String tenantDomain) throws JSONException {

        JSONObject user = new JSONObject();
        user.put("id", userId);

        JSONArray claims = new JSONArray();
        claims.put(new JSONObject().put("uri", "http://wso2.org/claims/username").put("value", "scim2user"));
        claims.put(new JSONObject().put("uri", "http://wso2.org/claims/emailAddresses")
                .put("value", new JSONArray().put("scim2user@wso2.com").put("scim2user@gmail.com")));
        claims.put(new JSONObject().put("uri", "http://wso2.org/claims/emails.home")
                .put("value", "scim2user@gmail.com"));
        claims.put(new JSONObject().put("uri", "http://wso2.org/claims/lastname").put("value", "scim"));
        claims.put(new JSONObject().put("uri", "http://wso2.org/claims/emails.work")
                .put("value", "scim2user@wso2.com"));
        claims.put(new JSONObject().put("uri", "http://wso2.org/claims/givenname").put("value", "user"));

        user.put("claims", claims);

        if (tenantDomain.equals(MultitenantConstants.SUPER_TENANT_DOMAIN_NAME)) {
            user.put("ref", "https://localhost:9853/scim2/Users/" + userId);
        } else {
            user.put("ref", "https://localhost:9853/t/" + tenantDomain + "/scim2/Users/" + userId);
        }

        return user;
    }

    private static JSONObject createUserObjectForDelete(String userId, String tenantDomain) throws JSONException {

        JSONObject user = new JSONObject();
        user.put("id", userId);

        JSONArray claims = new JSONArray();
        claims.put(new JSONObject().put("uri", "http://wso2.org/claims/username").put("value", "scim2user"));

        user.put("claims", claims);

        if (tenantDomain.equals(MultitenantConstants.SUPER_TENANT_DOMAIN_NAME)) {
            user.put("ref", "https://localhost:9853/scim2/Users/" + userId);
        } else {
            user.put("ref", "https://localhost:9853/t/" + tenantDomain + "/scim2/Users/" + userId);
        }

        return user;
    }

    private static JSONObject createUserObjectForFailure(String userId, String tenantDomain) throws JSONException {

        JSONObject user = new JSONObject();
        user.put("id", userId);

        if (tenantDomain.equals(MultitenantConstants.SUPER_TENANT_DOMAIN_NAME)) {
            user.put("ref", "https://localhost:9853/scim2/Users/" + userId);
        } else {
            user.put("ref", "https://localhost:9853/t/" + tenantDomain + "/scim2/Users/" + userId);
        }

        return user;
    }

    private static JSONObject createTenantObject(String tenantDomain) throws JSONException {

        JSONObject tenant = new JSONObject();
        tenant.put("name", tenantDomain);
        return tenant;
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

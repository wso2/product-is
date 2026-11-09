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

import org.json.JSONException;
import org.json.JSONObject;
import org.wso2.identity.integration.test.webhooks.util.EventPayloadUtils;

/**
 * Utility methods to build the objects shared by the expected payloads of the events triggered in sub organizations.
 */
public class SubOrgEventPayloadUtils {

    public static final String USERNAME_CLAIM_URI = "http://wso2.org/claims/username";
    public static final String EMAIL_CLAIM_URI = "http://wso2.org/claims/emailaddress";
    public static final String GIVEN_NAME_CLAIM_URI = "http://wso2.org/claims/givenname";
    public static final String LAST_NAME_CLAIM_URI = "http://wso2.org/claims/lastname";

    private static final String SERVER_BASE_URL = "https://localhost:9853";

    /**
     * Builds the organization object of a first level sub organization.
     *
     * @param orgId     ID of the sub organization.
     * @param orgName   Name of the sub organization.
     * @param orgHandle Handle of the sub organization.
     * @return JSONObject representing the sub organization.
     * @throws JSONException if an error occurs while building the object.
     */
    public static JSONObject createSubOrganizationObject(String orgId, String orgName, String orgHandle)
            throws JSONException {

        JSONObject organization = new JSONObject();
        organization.put("id", orgId);
        organization.put("name", orgName);
        organization.put("orgHandle", orgHandle);
        organization.put("depth", 1);
        return organization;
    }

    /**
     * Builds the organization object of the root organization.
     *
     * @param rootTenantDomain Tenant domain of the root organization.
     * @return JSONObject representing the root organization.
     * @throws JSONException if an error occurs while building the object.
     */
    public static JSONObject createRootOrganizationObject(String rootTenantDomain) throws JSONException {

        return EventPayloadUtils.createOrganizationObject(rootTenantDomain);
    }

    /**
     * Builds the application object of an application, with the organization that owns the application.
     *
     * @param applicationId           ID of the application.
     * @param applicationName         Name of the application.
     * @param applicationOrganization Organization that owns the application.
     * @return JSONObject representing the application.
     * @throws JSONException if an error occurs while building the object.
     */
    public static JSONObject createApplicationObject(String applicationId, String applicationName,
                                                     JSONObject applicationOrganization) throws JSONException {

        JSONObject application = new JSONObject();
        application.put("id", applicationId);
        application.put("name", applicationName);
        application.put("organization", applicationOrganization);
        return application;
    }

    /**
     * Builds the user object of a sub organization user, without the claims.
     *
     * @param userId           ID of the user.
     * @param rootTenantDomain Tenant domain of the root organization.
     * @param subOrganization  Sub organization of the user.
     * @return JSONObject representing the user.
     * @throws JSONException if an error occurs while building the object.
     */
    public static JSONObject createSubOrgUserObject(String userId, String rootTenantDomain,
                                                    JSONObject subOrganization) throws JSONException {

        JSONObject user = new JSONObject();
        user.put("id", userId);
        user.put("organization", subOrganization);
        // Sub organization users are referred through the organization SCIM2 endpoint of the root tenant.
        user.put("ref", SERVER_BASE_URL + "/t/" + rootTenantDomain + "/o/scim2/Users/" + userId);
        return user;
    }

    /**
     * Builds a claim object.
     *
     * @param uri   URI of the claim.
     * @param value Value of the claim.
     * @return JSONObject representing the claim.
     * @throws JSONException if an error occurs while building the object.
     */
    public static JSONObject createClaim(String uri, String value) throws JSONException {

        return new JSONObject().put("uri", uri).put("value", value);
    }
}

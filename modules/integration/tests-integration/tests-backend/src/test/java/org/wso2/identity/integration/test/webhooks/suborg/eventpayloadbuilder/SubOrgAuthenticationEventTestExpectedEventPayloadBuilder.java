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
import static org.wso2.identity.integration.test.webhooks.suborg.eventpayloadbuilder.SubOrgEventPayloadUtils.USERNAME_CLAIM_URI;
import static org.wso2.identity.integration.test.webhooks.suborg.eventpayloadbuilder.SubOrgEventPayloadUtils.createClaim;
import static org.wso2.identity.integration.test.webhooks.suborg.eventpayloadbuilder.SubOrgEventPayloadUtils.createSubOrgUserObject;

/**
 * Utility class to build expected event payloads for the login and session events triggered when a sub organization
 * user logs in, either to a root organization application shared with the sub organization or to an application
 * created in the sub organization.
 * Values that cannot be predicted by the test (e.g. session IDs and timestamps) are set to "dummy" values, which
 * makes the validator skip them while still requiring the field to be present.
 */
public class SubOrgAuthenticationEventTestExpectedEventPayloadBuilder {

    /**
     * Builds the expected payload for a login success event of a sub organization user.
     *
     * @param userId             ID of the authenticated user.
     * @param usernameClaimValue Value of the username claim of the authenticated user.
     * @param rootTenantDomain   Tenant domain of the root organization.
     * @param subOrganization    Sub organization of the user, which is also the organization the event belongs to.
     * @param application        Application the user logged in to.
     * @param localLogin         Whether the user authenticated directly in the sub organization. When the login
     *                           federates through the organization authenticator, the user store and the
     *                           authentication methods are not published.
     * @return JSONObject representing the expected login success event payload.
     * @throws JSONException if an error occurs while building the payload.
     */
    public static JSONObject buildExpectedLoginSuccessEventPayload(String userId, String usernameClaimValue,
                                                                   String rootTenantDomain,
                                                                   JSONObject subOrganization,
                                                                   JSONObject application, boolean localLogin)
            throws JSONException {

        JSONObject user = createSubOrgUserObject(userId, rootTenantDomain, subOrganization);
        user.put("claims", new JSONArray().put(createClaim(USERNAME_CLAIM_URI, usernameClaimValue)));

        JSONObject loginSuccessEvent = new JSONObject();
        loginSuccessEvent.put("initiatorType", "USER");
        loginSuccessEvent.put("initiatorIpAddress", "dummy-initiator-ip-address");
        loginSuccessEvent.put("user", user);
        loginSuccessEvent.put("tenant", EventPayloadUtils.createTenantObject(rootTenantDomain));
        loginSuccessEvent.put("organization", subOrganization);
        loginSuccessEvent.put("application", application);
        loginSuccessEvent.put("action", "LOGIN");

        if (localLogin) {
            loginSuccessEvent.put("userStore", EventPayloadUtils.createUserStoreObject());
            JSONArray authenticationMethods = new JSONArray();
            authenticationMethods.put("BasicAuthenticator");
            loginSuccessEvent.put("authenticationMethods", authenticationMethods);
        }
        return loginSuccessEvent;
    }

    /**
     * Builds the expected payload for a login success event of a sub organization user who logs in to an
     * application of the sub organization with single sign-on. No authenticator is executed in a single sign-on
     * login, hence the authentication methods are not published.
     *
     * @param userId             ID of the authenticated user.
     * @param usernameClaimValue Value of the username claim of the authenticated user.
     * @param rootTenantDomain   Tenant domain of the root organization.
     * @param subOrganization    Sub organization of the user, which is also the organization the event belongs to.
     * @param application        Application the user logged in to.
     * @return JSONObject representing the expected login success event payload.
     * @throws JSONException if an error occurs while building the payload.
     */
    public static JSONObject buildExpectedSSOLoginSuccessEventPayload(String userId, String usernameClaimValue,
                                                                      String rootTenantDomain,
                                                                      JSONObject subOrganization,
                                                                      JSONObject application) throws JSONException {

        JSONObject loginSuccessEvent = buildExpectedLoginSuccessEventPayload(userId, usernameClaimValue,
                rootTenantDomain, subOrganization, application, true);
        loginSuccessEvent.remove("authenticationMethods");
        return loginSuccessEvent;
    }

    /**
     * Builds the expected payload for a login failed event, when a sub organization user provides invalid
     * credentials at the basic authenticator step.
     *
     * @param username         Username of the user who attempted to log in.
     * @param email            Email address of the user who attempted to log in.
     * @param rootTenantDomain Tenant domain of the root organization.
     * @param subOrganization  Sub organization of the user, which is also the organization the event belongs to.
     * @param application      Application the user attempted to log in to.
     * @return JSONObject representing the expected login failed event payload.
     * @throws JSONException if an error occurs while building the payload.
     */
    public static JSONObject buildExpectedLoginFailedEventPayloadForInvalidCredentials(
            String username, String email, String rootTenantDomain, JSONObject subOrganization,
            JSONObject application) throws JSONException {

        // The claims of the failed login user are matched against this list, which may contain more claims than
        // the received payload, but not fewer.
        JSONArray claims = new JSONArray();
        claims.put(createClaim(USERNAME_CLAIM_URI, username));
        claims.put(createClaim(EMAIL_CLAIM_URI, email));

        JSONObject user = new JSONObject();
        user.put("claims", claims);
        user.put("organization", subOrganization);

        JSONObject loginFailedEvent = new JSONObject();
        loginFailedEvent.put("initiatorType", "USER");
        loginFailedEvent.put("initiatorIpAddress", "dummy-initiator-ip-address");
        loginFailedEvent.put("user", user);
        loginFailedEvent.put("tenant", EventPayloadUtils.createTenantObject(rootTenantDomain));
        loginFailedEvent.put("organization", subOrganization);
        loginFailedEvent.put("application", application);
        loginFailedEvent.put("reason", createReasonObjectForInvalidCredentials());
        loginFailedEvent.put("action", "LOGIN");
        return loginFailedEvent;
    }

    /**
     * Builds the expected payload for a session established event of a sub organization user.
     *
     * @param userId             ID of the user who owns the session.
     * @param usernameClaimValue Value of the username claim of the user who owns the session.
     * @param rootTenantDomain   Tenant domain of the root organization.
     * @param subOrganization    Sub organization of the user, which is also the organization the event belongs to.
     * @param application        Application the session was established for, which is also the only application
     *                           of the session.
     * @param localLogin         Whether the user authenticated directly in the sub organization. When the login
     *                           federates through the organization authenticator, the user store is not published.
     * @return JSONObject representing the expected session established event payload.
     * @throws JSONException if an error occurs while building the payload.
     */
    public static JSONObject buildExpectedSessionEstablishedEventPayload(String userId, String usernameClaimValue,
                                                                         String rootTenantDomain,
                                                                         JSONObject subOrganization,
                                                                         JSONObject application, boolean localLogin)
            throws JSONException {

        JSONObject sessionEstablishedEvent = createSessionEventBase(userId, usernameClaimValue, rootTenantDomain,
                subOrganization, localLogin);
        sessionEstablishedEvent.put("application", application);
        sessionEstablishedEvent.put("action", "LOGIN");
        sessionEstablishedEvent.put("session", createSessionObject(new JSONArray().put(application)));
        return sessionEstablishedEvent;
    }

    /**
     * Builds the expected payload for a session presented event, when a sub organization user logs in to an
     * application of the sub organization with single sign-on from an existing session.
     *
     * @param userId              ID of the user who owns the session.
     * @param usernameClaimValue  Value of the username claim of the user who owns the session.
     * @param rootTenantDomain    Tenant domain of the root organization.
     * @param subOrganization     Sub organization of the user, which is also the organization the event belongs to.
     * @param application         Application the session was presented to.
     * @param sessionApplications All the applications of the session, including the one it was presented to.
     * @return JSONObject representing the expected session presented event payload.
     * @throws JSONException if an error occurs while building the payload.
     */
    public static JSONObject buildExpectedSessionPresentedEventPayload(String userId, String usernameClaimValue,
                                                                       String rootTenantDomain,
                                                                       JSONObject subOrganization,
                                                                       JSONObject application,
                                                                       JSONArray sessionApplications)
            throws JSONException {

        JSONObject sessionPresentedEvent = createSessionEventBase(userId, usernameClaimValue, rootTenantDomain,
                subOrganization, true);
        sessionPresentedEvent.put("application", application);
        sessionPresentedEvent.put("action", "LOGIN");
        sessionPresentedEvent.put("session", createSessionObject(sessionApplications));
        return sessionPresentedEvent;
    }

    /**
     * Builds the expected payload for a session revoked event, when a sub organization user logs out of a session
     * of the sub organization.
     *
     * @param userId              ID of the user who owns the session.
     * @param usernameClaimValue  Value of the username claim of the user who owns the session.
     * @param rootTenantDomain    Tenant domain of the root organization.
     * @param subOrganization     Sub organization of the user, which is also the organization the event belongs to.
     * @param sessionApplications All the applications of the revoked session.
     * @return JSONObject representing the expected session revoked event payload.
     * @throws JSONException if an error occurs while building the payload.
     */
    public static JSONObject buildExpectedSessionRevokedEventPayload(String userId, String usernameClaimValue,
                                                                     String rootTenantDomain,
                                                                     JSONObject subOrganization,
                                                                     JSONArray sessionApplications)
            throws JSONException {

        JSONObject sessionRevokedEvent = createSessionEventBase(userId, usernameClaimValue, rootTenantDomain,
                subOrganization, true);
        sessionRevokedEvent.put("action", "LOGOUT");
        sessionRevokedEvent.put("sessions", new JSONArray().put(createSessionObject(sessionApplications)));
        return sessionRevokedEvent;
    }

    private static JSONObject createSessionEventBase(String userId, String usernameClaimValue,
                                                     String rootTenantDomain, JSONObject subOrganization,
                                                     boolean localLogin) throws JSONException {

        JSONObject user = createSubOrgUserObject(userId, rootTenantDomain, subOrganization);
        user.put("claims", new JSONArray().put(createClaim(USERNAME_CLAIM_URI, usernameClaimValue)));

        JSONObject sessionEvent = new JSONObject();
        sessionEvent.put("initiatorType", "USER");
        sessionEvent.put("initiatorIpAddress", "dummy-initiator-ip-address");
        sessionEvent.put("user", user);
        sessionEvent.put("tenant", EventPayloadUtils.createTenantObject(rootTenantDomain));
        sessionEvent.put("organization", subOrganization);
        if (localLogin) {
            sessionEvent.put("userStore", EventPayloadUtils.createUserStoreObject());
        }
        return sessionEvent;
    }

    private static JSONObject createSessionObject(JSONArray applications) throws JSONException {

        JSONObject session = new JSONObject();
        session.put("id", "dummy-session-id");
        session.put("loginTime", "dummy-login-time");
        session.put("applications", applications);
        return session;
    }

    private static JSONObject createReasonObjectForInvalidCredentials() throws JSONException {

        JSONObject failedStep = new JSONObject();
        failedStep.put("step", 1);
        failedStep.put("idp", "LOCAL");
        failedStep.put("authenticator", "BasicAuthenticator");

        JSONObject context = new JSONObject();
        context.put("failedStep", failedStep);

        JSONObject reason = EventPayloadUtils.createReasonObject(
                "User authentication failed due to invalid credentials");
        reason.put("context", context);
        return reason;
    }
}

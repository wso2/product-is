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

package org.wso2.identity.integration.test.webhooks.consent.eventpayloadbuilder;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.wso2.carbon.utils.multitenancy.MultitenantConstants;
import org.wso2.identity.integration.test.webhooks.util.EventPayloadUtils;

/**
 * Builds expected webhook event payloads for consent-related events.
 *
 * <p>Fields with values prefixed "dummy-" are skipped by the validator, covering
 * dynamic runtime values such as UUIDs, IP addresses, and SCIM refs.
 * Fields omitted from the expected payload (e.g., {@code action} for the added event)
 * are not validated.
 */
public class ConsentEventTestExpectedEventPayloadBuilder {

    /**
     * Builds the expected payload for a {@code consentAdded} webhook event triggered
     * when a user creates a consent receipt via the user consent API.
     *
     * <p>The {@code action} field is omitted from the expected payload because its value
     * depends on the identity flow context at the time of the API call, which varies
     * (e.g., "REGISTER" during self-registration, "LOGIN" during login). For direct
     * API calls the value is not specified here to keep the assertion stable.
     *
     * @param subjectId   The consent subject's username (plain username, no domain prefix).
     * @param serviceId   The service ID used when creating the consent.
     * @param purposeName The name of the consent purpose.
     * @param elementName The name of the consented PII element.
     * @param tenantDomain Tenant domain of the consent subject.
     * @return Expected event payload as a {@link JSONObject}.
     */
    public static JSONObject buildExpectedConsentAddedEventPayload(
            String subjectId,
            String serviceId,
            String purposeName,
            String elementName,
            String tenantDomain) throws JSONException {

        JSONObject payload = new JSONObject();
        payload.put("initiatorType", "USER");
        payload.put("initiatorIpAddress", "dummy-initiator-ip-address");
        payload.put("action", "CONSENT_GRANT");
        payload.put("user", buildUserObject());
        payload.put("tenant", EventPayloadUtils.createTenantObject(tenantDomain));
        payload.put("organization", EventPayloadUtils.createOrganizationObject(tenantDomain));
        payload.put("userStore", EventPayloadUtils.createUserStoreObject());
        payload.put("consent", buildConsentObject("dummy-consent-id", subjectId, "APPROVED",
                serviceId, purposeName, elementName));
        return payload;
    }

    /**
     * Builds the expected payload for a {@code consentRevoked} webhook event triggered
     * when a user revokes a consent receipt via the user consent API.
     *
     * @param subjectId   The consent subject's username (plain username, no domain prefix).
     * @param serviceId   The service ID of the revoked consent.
     * @param purposeName The name of the consent purpose.
     * @param elementName The name of the consented PII element.
     * @param tenantDomain Tenant domain of the consent subject.
     * @return Expected event payload as a {@link JSONObject}.
     */
    public static JSONObject buildExpectedConsentRevokedEventPayload(
            String subjectId,
            String serviceId,
            String purposeName,
            String elementName,
            String tenantDomain) throws JSONException {

        JSONObject payload = new JSONObject();
        payload.put("initiatorType", "USER");
        payload.put("initiatorIpAddress", "dummy-initiator-ip-address");
        payload.put("action", "CONSENT_REVOKE");
        payload.put("user", buildUserObject());
        payload.put("tenant", EventPayloadUtils.createTenantObject(tenantDomain));
        payload.put("organization", EventPayloadUtils.createOrganizationObject(tenantDomain));
        payload.put("userStore", EventPayloadUtils.createUserStoreObject());
        payload.put("consent", buildConsentObject("dummy-consent-id", subjectId, "REVOKED",
                serviceId, purposeName, elementName));
        return payload;
    }

    private static JSONObject buildUserObject() throws JSONException {

        JSONObject user = new JSONObject();
        user.put("id", "dummy-user-id");
        user.put("ref", "dummy-ref");
        return user;
    }

    private static JSONObject buildConsentObject(String consentId, String subjectId, String state,
                                                  String serviceId, String purposeName,
                                                  String elementName) throws JSONException {

        JSONObject consent = new JSONObject();
        consent.put("id", consentId);
        consent.put("subjectId", subjectId);
        consent.put("state", state);
        consent.put("serviceId", serviceId);
        consent.put("purpose", buildPurposeObject(purposeName, elementName));
        return consent;
    }

    private static JSONObject buildPurposeObject(String purposeName, String elementName) throws JSONException {

        JSONObject purpose = new JSONObject();
        purpose.put("id", "dummy-purpose-id");
        purpose.put("name", purposeName);
        purpose.put("version", "dummy-version-id");

        JSONArray elements = new JSONArray();
        JSONObject element = new JSONObject();
        element.put("name", elementName);
        elements.put(element);
        purpose.put("elements", elements);

        return purpose;
    }
}

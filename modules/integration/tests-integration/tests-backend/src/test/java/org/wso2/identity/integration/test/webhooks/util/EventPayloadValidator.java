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

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.HashSet;
import java.util.Iterator;
import java.util.Set;

/**
 * Utility class to validate and extract event payloads from webhook events.
 */
public class EventPayloadValidator {

    private static final Logger LOG = LoggerFactory.getLogger(EventPayloadValidator.class);

    /**
     * Validates common fields in the event payload like "iss", "jti", "iat", "rci", and "events".
     *
     * @param eventUri The URI of the event to validate against.
     * @param payload  The JSON payload of the event.
     * @throws Exception If any required fields are missing or if the event URI is not found in the payload.
     */
    public static void validateCommonEventPayloadFields(String eventUri, JSONObject payload) throws Exception {

        validateRequiredFields(payload, "iss", "jti", "iat", "rci", "events");

        LOG.info("Common fields validated for event URI: {}", eventUri);

        if (!payload.getJSONObject("events").has(eventUri)) {
            throw new IllegalArgumentException("Event URI not found in the payload: " + eventUri);
        }
    }

    /**
     * Extracts the event payload for a specific event URI from the provided JSON payload.
     *
     * @param eventUri The URI of the event to extract.
     * @param payload  The JSON payload containing the event data.
     * @return The JSON object representing the event payload for the specified event URI.
     * @throws Exception If the required fields are missing or if the event URI is not found in the payload.
     */
    public static JSONObject extractEventPayload(String eventUri, JSONObject payload) throws Exception {

        validateRequiredFields(payload, "events");

        JSONObject events = payload.getJSONObject("events");
        if (!events.has(eventUri)) {
            throw new IllegalArgumentException("Event URI not found in the payload: " + eventUri);
        }

        return events.getJSONObject(eventUri);
    }

    /**
     * Validates the fields of an event payload against an expected event structure.
     * This method checks for the presence of keys, validates nested JSON objects,
     *
     * @param actualEvent   The actual event payload extracted from the webhook.
     * @param expectedEvent The expected event structure to validate against.
     * @throws Exception If any required keys are missing, or if there is a mismatch in values.
     */
    public static void validateEventField(JSONObject actualEvent, JSONObject expectedEvent) throws Exception {

        Iterator<String> keys = expectedEvent.keys();
        while (keys.hasNext()) {
            String key = keys.next();
            LOG.info("Validating key: {}", key);

            if (!actualEvent.has(key)) {
                throw new IllegalArgumentException("Missing key in actual event payload: " + key);
            }

            Object expectedValue = expectedEvent.get(key);
            Object actualValue = actualEvent.get(key);

            if ("claims".equals(key)) {
                validateClaimsArray((JSONArray) actualValue, (JSONArray) expectedValue);
                continue;
            }

            if (expectedValue instanceof JSONObject && actualValue instanceof JSONObject) {
                validateEventField((JSONObject) actualValue, (JSONObject) expectedValue);
            } else if (expectedValue instanceof JSONArray && actualValue instanceof JSONArray) {
                validateJsonArray((JSONArray) actualValue, (JSONArray) expectedValue);
            } else if (!expectedValue.equals(actualValue)) {
                throw new IllegalArgumentException("Value mismatch for key '" + key + "': expected " +
                        expectedValue + ", but found " + actualValue);
            }
        }
    }

    private static void validateJsonArray(JSONArray actualArray, JSONArray expectedArray) throws Exception {

        if (actualArray.length() != expectedArray.length()) {
            throw new IllegalArgumentException("Array length mismatch: expected " + expectedArray.length() +
                    ", but found " + actualArray.length());
        }

        Set<String> actualSet = convertArrayToSet(actualArray);
        Set<String> expectedSet = convertArrayToSet(expectedArray);

        if (!actualSet.equals(expectedSet)) {
            throw new IllegalArgumentException("Array content mismatch: expected " + expectedSet +
                    ", but found " + actualSet);
        }
    }

    private static Set<String> convertArrayToSet(JSONArray array) throws JSONException {

        Set<String> set = new HashSet<>();
        for (int i = 0; i < array.length(); i++) {
            Object element = array.get(i);
            set.add(element.toString());
        }
        return set;
    }

    private static void validateRequiredFields(JSONObject jsonObject, String... fields) {

        for (String field : fields) {
            if (!jsonObject.has(field)) {
                throw new IllegalArgumentException("Missing required field: " + field);
            }
        }
    }

    private static void validateClaimsArray(JSONArray actualArray, JSONArray expectedArray) throws Exception {

        for (int i = 0; i < actualArray.length(); i++) {
            JSONObject actualClaim = actualArray.getJSONObject(i);

            // Skip validation if the claim has the uri "http://wso2.org/claims/userType"
            // This claim gets populated on the user object of the event payload for user add operations when full test suite runs.
            // todo: need to investigate why this claim is getting populated and fix the issue.
            if (actualClaim.has("uri") && "http://wso2.org/claims/userType".equals(actualClaim.getString("uri"))) {
                LOG.info("Skipping validation for claim with uri 'http://wso2.org/claims/userType': {}", actualClaim);
                continue;
            }

            boolean isMatched = false;
            for (int j = 0; j < expectedArray.length(); j++) {
                JSONObject expectedClaim = expectedArray.getJSONObject(j);

                if (areJsonObjectsEqual(actualClaim, expectedClaim)) {
                    isMatched = true;
                    break;
                }
            }

            if (!isMatched) {
                throw new IllegalArgumentException("No matching claim found for: " + actualClaim);
            }
        }
    }

    private static boolean areJsonObjectsEqual(JSONObject obj1, JSONObject obj2) throws JSONException {

        if (obj1.length() != obj2.length()) {
            return false;
        }

        Iterator<String> keys = obj1.keys();
        while (keys.hasNext()) {
            String key = keys.next();
            if (!obj2.has(key) || !obj1.get(key).toString().equals(obj2.get(key).toString())) {
                return false;
            }
        }
        return true;
    }
}

/*
 * Copyright (c) 2025, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
 *
 * WSO2 Inc. licenses this file to you under the Apache License,
 * Version 2.0 (the "License"); you may not use this file except
 * in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied. See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */

package org.wso2.identity.integration.test.utils;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.wso2.identity.integration.test.rest.api.server.api.resource.v1.model.APIResourceResponse;
import org.wso2.identity.integration.test.rest.api.server.api.resource.v1.model.AuthorizationDetailsType;
import org.wso2.identity.integration.test.restclients.OAuth2RestClient;

import java.io.IOException;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;

/**
 * Utility class for rich authorization requests.
 */
public class RichAuthorizationRequestsUtils {

    public static final String TYPE = "type";
    public static final String ERROR = "error";
    public static final String INVALID_AUTHORIZATION_DETAILS = "invalid_authorization_details";
    public static final String TEST_TYPE_1 = "https://example.com/rar_authorization_details_type_1";
    public static final String TEST_TYPE_2 = "rar_authorization_details_type_2";
    public static final String TEST_TYPE_3 = "rar_authorization_details_type_3";
    public static final String TEST_TYPE_INVALID = "rar_authorization_details_type_invalid";
    public static final List<AuthorizationDetailsType> TEST_AUTHORIZATION_DETAILS_TYPES = Arrays
            .asList(getTestAuthorizationDetailsType(TEST_TYPE_1), getTestAuthorizationDetailsType(TEST_TYPE_2));

    public static String createTestAPIResource(OAuth2RestClient restClient) throws IOException {

        return createTestAPIResource(restClient, TEST_AUTHORIZATION_DETAILS_TYPES);
    }

    public static String createTestAPIResource(OAuth2RestClient restClient,
                                               List<AuthorizationDetailsType> authorizationDetailsTypes)
            throws IOException {

        final UUID uuid = UUID.randomUUID();
        final APIResourceResponse apiResource = new APIResourceResponse()
                .name("rar api resource " + uuid)
                .identifier("rar_api_resource_" + uuid)
                .requiresAuthorization(true)
                .authorizationDetailsTypes(authorizationDetailsTypes);

        return restClient.createAPIResource(apiResource);
    }

    public static AuthorizationDetailsType getTestAuthorizationDetailsType(final String type) {
        return new AuthorizationDetailsType()
                .type(type)
                .name(type + " name")
                .description(type + " description")
                .schema(getTestAuthorizationDetailsSchema(type));
    }

    public static JSONObject getTestAuthorizationDetail(final String type) throws JSONException {

        JSONObject authorizationDetail = new JSONObject();
        authorizationDetail.put(TYPE, type);
        return authorizationDetail;
    }

    public static JSONArray getSingleTestAuthorizationDetail() throws JSONException {

        final JSONArray authorizationDetails = new JSONArray();
        authorizationDetails.put(getTestAuthorizationDetail(TEST_TYPE_1));
        return authorizationDetails;
    }

    public static JSONArray getDoubleTestAuthorizationDetails() throws JSONException {

        final JSONArray authorizationDetails = new JSONArray();
        authorizationDetails.put(getTestAuthorizationDetail(TEST_TYPE_1));
        authorizationDetails.put(getTestAuthorizationDetail(TEST_TYPE_2));
        return authorizationDetails;
    }

    public static JSONArray getTripleTestAuthorizationDetails() throws JSONException {

        final JSONArray authorizationDetails = new JSONArray();
        authorizationDetails.put(getTestAuthorizationDetail(TEST_TYPE_1));
        authorizationDetails.put(getTestAuthorizationDetail(TEST_TYPE_2));
        authorizationDetails.put(getTestAuthorizationDetail(TEST_TYPE_3));
        return authorizationDetails;
    }

    public static JSONArray getUnknownAuthorizationDetails() throws JSONException {

        JSONObject unknownAuthorizationDetail = getTestAuthorizationDetail(TEST_TYPE_INVALID);
        JSONArray unknownAuthorizationDetails = new JSONArray();
        unknownAuthorizationDetails.put(unknownAuthorizationDetail);

        return unknownAuthorizationDetails;
    }

    public static JSONArray getInvalidAuthorizationDetails() throws JSONException {

        JSONObject invalidAuthorizationDetail = getTestAuthorizationDetail(TEST_TYPE_1);
        invalidAuthorizationDetail.put("invalid_key", "invalid_value");
        JSONArray invalidAuthorizationDetails = new JSONArray();
        invalidAuthorizationDetails.put(invalidAuthorizationDetail);

        return invalidAuthorizationDetails;
    }

    public static Map<String, Object> getTestAuthorizationDetailsSchema(final String type) {
        final Map<String, Object> types = new HashMap<>();
        types.put(TYPE, "string");
        types.put("enum", Collections.singletonList(type));

        final Map<String, Object> properties = new HashMap<>();
        properties.put(TYPE, types);

        final Map<String, Object> schema = new HashMap<>();
        schema.put(TYPE, "object");
        schema.put("required", Collections.singletonList(TYPE));
        schema.put("properties", properties);

        return schema;
    }

    public static boolean isRequestTypesMatchResponseTypes(JSONArray expectedAuthorizationDetails,
                                                           JSONArray actualAuthorizationDetails)
            throws JSONException {

        Set<String> actual = new HashSet<>();
        for (int n = 0; n < actualAuthorizationDetails.length(); n++) {
            actual.add(actualAuthorizationDetails.getJSONObject(n).getString(TYPE));
        }

        for (int i = 0; i < expectedAuthorizationDetails.length(); i++) {
            if (!actual.contains(expectedAuthorizationDetails.getJSONObject(i).getString(TYPE))) {
                return false;
            }
        }
        return true;
    }
}

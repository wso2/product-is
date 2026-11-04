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

package org.wso2.identity.integration.test.webhooks.organizationManagement.eventpayloadbuilder;

import org.json.JSONArray;
import org.json.JSONObject;
import org.wso2.identity.integration.test.webhooks.util.EventPayloadUtils;

/**
 * Utility class to build expected event payloads for the organization management webhook test cases.
 * <p>
 * Values which are not deterministic across test runs (the organization id and the management API ref) are set to
 * the {@code "dummy..."} sentinel so that the {@code EventPayloadValidator} skips value comparison for them while
 * still enforcing the presence of the key. The organization name, the organization handle and the hierarchy depth
 * are known to the test and are therefore asserted exactly.
 */
public class OrganizationManagementEventTestExpectedEventPayloadBuilder {

    private static final String INITIATOR_TYPE_ADMIN = "ADMIN";
    private static final String INITIATOR_IP_ADDRESS = "127.0.0.1";
    private static final String ACTION_ORGANIZATION_CREATE = "ORGANIZATION_CREATE";
    private static final String ACTION_ORGANIZATION_UPDATE = "ORGANIZATION_UPDATE";
    private static final String ACTION_ORGANIZATION_ENABLE = "ORGANIZATION_ENABLE";
    private static final String ACTION_ORGANIZATION_DISABLE = "ORGANIZATION_DISABLE";
    private static final String ACTION_ORGANIZATION_DELETE = "ORGANIZATION_DELETE";

    private static final String DUMMY_ORGANIZATION_ID = "dummy-target-org-id";
    private static final String DUMMY_ORGANIZATION_REF = "dummy-target-org-ref";

    private OrganizationManagementEventTestExpectedEventPayloadBuilder() {

    }

    /**
     * Builds the expected payload for an organization created event, where the organization was created with a
     * single attribute.
     */
    public static JSONObject buildExpectedOrganizationCreatedEventPayload(String tenantDomain, String organizationName,
                                                                          String organizationHandle, int depth,
                                                                          String attributeName, String attributeValue)
            throws Exception {

        JSONObject event = buildBaseOrganizationEvent(tenantDomain, ACTION_ORGANIZATION_CREATE);

        JSONObject targetOrganization = buildBaseTargetOrganization(organizationName, organizationHandle, depth);
        targetOrganization.put("attributes", new JSONObject()
                .put("added", new JSONArray().put(createAttribute(attributeName, attributeValue))));
        event.put("targetOrganization", targetOrganization);

        return event;
    }

    /**
     * Builds the expected payload for an organization updated event which changed the name alone.
     */
    public static JSONObject buildExpectedOrganizationNameUpdatedEventPayload(String tenantDomain,
                                                                              String organizationName,
                                                                              String organizationHandle, int depth,
                                                                              String updatedName) throws Exception {

        return buildUpdatedEventPayload(tenantDomain, organizationName, organizationHandle, depth,
                new JSONObject().put("name", updatedName));
    }

    /**
     * Builds the expected payload for an organization updated event which changed the description alone.
     */
    public static JSONObject buildExpectedOrganizationDescriptionUpdatedEventPayload(String tenantDomain,
                                                                                      String organizationName,
                                                                                      String organizationHandle,
                                                                                      int depth,
                                                                                      String updatedDescription)
            throws Exception {

        return buildUpdatedEventPayload(tenantDomain, organizationName, organizationHandle, depth,
                new JSONObject().put("description", updatedDescription));
    }

    /**
     * Builds the expected payload for an organization updated event which replaced the organization, changing the
     * description and adding an attribute in the same request.
     */
    public static JSONObject buildExpectedOrganizationReplacedEventPayload(String tenantDomain,
                                                                           String organizationName,
                                                                           String organizationHandle, int depth,
                                                                           String updatedDescription,
                                                                           String attributeName,
                                                                           String attributeValue) throws Exception {

        JSONObject attributes = new JSONObject()
                .put("added", new JSONArray().put(createAttribute(attributeName, attributeValue)));
        JSONObject updatedValues = new JSONObject()
                .put("description", updatedDescription)
                .put("attributes", attributes);

        return buildUpdatedEventPayload(tenantDomain, organizationName, organizationHandle, depth, updatedValues);
    }

    /**
     * Builds the expected payload for an organization updated event which added a single attribute.
     */
    public static JSONObject buildExpectedOrganizationAttributeAddedEventPayload(String tenantDomain,
                                                                                 String organizationName,
                                                                                 String organizationHandle, int depth,
                                                                                 String attributeName,
                                                                                 String attributeValue)
            throws Exception {

        JSONObject attributes = new JSONObject()
                .put("added", new JSONArray().put(createAttribute(attributeName, attributeValue)));

        return buildUpdatedEventPayload(tenantDomain, organizationName, organizationHandle, depth,
                new JSONObject().put("attributes", attributes));
    }

    /**
     * Builds the expected payload for an organization enabled event. The status itself is carried by the event type
     * rather than by the organization, so no status is reported.
     */
    public static JSONObject buildExpectedOrganizationEnabledEventPayload(String tenantDomain,
                                                                           String organizationName,
                                                                           String organizationHandle, int depth)
            throws Exception {

        return buildStatusEventPayload(tenantDomain, organizationName, organizationHandle, depth,
                ACTION_ORGANIZATION_ENABLE);
    }

    /**
     * Builds the expected payload for an organization disabled event.
     */
    public static JSONObject buildExpectedOrganizationDisabledEventPayload(String tenantDomain,
                                                                            String organizationName,
                                                                            String organizationHandle, int depth)
            throws Exception {

        return buildStatusEventPayload(tenantDomain, organizationName, organizationHandle, depth,
                ACTION_ORGANIZATION_DISABLE);
    }

    /**
     * Builds the expected payload for an organization deleted event. The management API ref is not reported, since
     * the resource no longer exists once the deletion is complete.
     */
    public static JSONObject buildExpectedOrganizationDeletedEventPayload(String tenantDomain,
                                                                           String organizationName,
                                                                           String organizationHandle, int depth)
            throws Exception {

        JSONObject event = buildBaseOrganizationEvent(tenantDomain, ACTION_ORGANIZATION_DELETE);

        JSONObject targetOrganization = new JSONObject();
        targetOrganization.put("id", DUMMY_ORGANIZATION_ID);
        targetOrganization.put("name", organizationName);
        targetOrganization.put("orgHandle", organizationHandle);
        targetOrganization.put("depth", depth);
        event.put("targetOrganization", targetOrganization);

        return event;
    }

    private static JSONObject buildUpdatedEventPayload(String tenantDomain, String organizationName,
                                                       String organizationHandle, int depth, JSONObject updatedValues)
            throws Exception {

        JSONObject event = buildBaseOrganizationEvent(tenantDomain, ACTION_ORGANIZATION_UPDATE);

        JSONObject targetOrganization = buildBaseTargetOrganization(organizationName, organizationHandle, depth);
        targetOrganization.put("updatedValues", updatedValues);
        event.put("targetOrganization", targetOrganization);

        return event;
    }

    private static JSONObject buildStatusEventPayload(String tenantDomain, String organizationName,
                                                      String organizationHandle, int depth, String action)
            throws Exception {

        JSONObject event = buildBaseOrganizationEvent(tenantDomain, action);
        event.put("targetOrganization", buildBaseTargetOrganization(organizationName, organizationHandle, depth));

        return event;
    }

    private static JSONObject buildBaseOrganizationEvent(String tenantDomain, String action) throws Exception {

        JSONObject event = new JSONObject();
        event.put("initiatorType", INITIATOR_TYPE_ADMIN);
        event.put("initiatorIpAddress", INITIATOR_IP_ADDRESS);
        event.put("tenant", EventPayloadUtils.createTenantObject(tenantDomain));
        event.put("organization", EventPayloadUtils.createOrganizationObject(tenantDomain));
        event.put("action", action);
        return event;
    }

    private static JSONObject buildBaseTargetOrganization(String organizationName, String organizationHandle,
                                                          int depth) throws Exception {

        JSONObject targetOrganization = new JSONObject();
        targetOrganization.put("id", DUMMY_ORGANIZATION_ID);
        targetOrganization.put("name", organizationName);
        targetOrganization.put("orgHandle", organizationHandle);
        targetOrganization.put("depth", depth);
        targetOrganization.put("ref", DUMMY_ORGANIZATION_REF);
        return targetOrganization;
    }

    private static JSONObject createAttribute(String name, String value) throws Exception {

        JSONObject attribute = new JSONObject();
        attribute.put("name", name);
        attribute.put("value", value);
        return attribute;
    }
}

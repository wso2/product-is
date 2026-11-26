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

package org.wso2.identity.integration.test.webhooks.organizationManagement;

import io.restassured.response.Response;
import org.apache.http.HttpHeaders;
import org.apache.http.HttpStatus;
import org.json.JSONArray;
import org.json.JSONObject;
import org.testng.annotations.AfterClass;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.DataProvider;
import org.testng.annotations.Factory;
import org.testng.annotations.Test;
import org.wso2.carbon.automation.engine.context.AutomationContext;
import org.wso2.carbon.automation.engine.context.TestUserMode;
import org.wso2.identity.integration.test.rest.api.server.organization.management.v1.OrganizationManagementBaseTest;
import org.wso2.identity.integration.test.webhooks.organizationManagement.eventpayloadbuilder.OrganizationManagementEventTestExpectedEventPayloadBuilder;
import org.wso2.identity.integration.test.webhooks.util.WebhookEventTestManager;

import java.util.Collections;

import static org.testng.Assert.assertNotNull;

/**
 * This class tests the organization management lifecycle events triggered by the admin user via the organization
 * management REST API.
 * <p>
 * Each test drives one request against the API and stacks only the events that request is expected to publish. The
 * {@code WebhookEventTestManager} fails the validation when an event which was not stacked is received, so the tests
 * which stack a single event also assert that no other event was published for that request. That is what covers the
 * suppression of the organization updated event for an update which changed the status alone.
 */
public class OrganizationManagementEventTestCase extends OrganizationManagementBaseTest {

    private static final String ORGANIZATION_EVENT_CHANNEL = "https://schemas.identity.wso2.org/events/organization";
    private static final String ORGANIZATION_CREATED_EVENT_URI =
            ORGANIZATION_EVENT_CHANNEL + "/event-type/organizationCreated";
    private static final String ORGANIZATION_UPDATED_EVENT_URI =
            ORGANIZATION_EVENT_CHANNEL + "/event-type/organizationUpdated";
    private static final String ORGANIZATION_ENABLED_EVENT_URI =
            ORGANIZATION_EVENT_CHANNEL + "/event-type/organizationEnabled";
    private static final String ORGANIZATION_DISABLED_EVENT_URI =
            ORGANIZATION_EVENT_CHANNEL + "/event-type/organizationDisabled";
    private static final String ORGANIZATION_DELETED_EVENT_URI =
            ORGANIZATION_EVENT_CHANNEL + "/event-type/organizationDeleted";

    /*
     The organization management API base path and the API version are not visible outside the package of the base
     test, so they are declared here as well.
    */
    private static final String ORGANIZATIONS_API_BASE_PATH = "/organizations";
    private static final String ORGANIZATIONS_API_VERSION = "v1";
    private static final String PATH_DELIMITER = "/";

    private static final String WEBHOOK_ENDPOINT_PATH = "/organizations/webhook";
    private static final String EVENT_PROFILE_WSO2 = "WSO2";
    private static final String TEST_NAME = "OrganizationManagementEventTestCase";

    private static final String ORG_NAME = "webhook-test-org";
    private static final String UPDATED_ORG_NAME = "webhook-test-org-renamed";
    private static final String MIXED_UPDATE_ORG_NAME = "webhook-test-org-mixed";
    private static final String ORG_DESCRIPTION = "Organization used by the organization management event tests.";
    private static final String UPDATED_ORG_DESCRIPTION = "Description updated by the organization replacement.";
    private static final String ORG_HANDLE_SUFFIX = "-webhook-test-org.com";

    private static final String ATTRIBUTE_KEY_COUNTRY = "Country";
    private static final String ATTRIBUTE_KEY_INDUSTRY = "Industry";
    private static final String ATTRIBUTE_KEY_REGION = "Region";
    private static final String COUNTRY_USA = "USA";
    private static final String INDUSTRY_CONSTRUCTION = "Construction";
    private static final String REGION_APAC = "APAC";

    private static final String STATUS_ACTIVE = "ACTIVE";
    private static final String STATUS_DISABLED = "DISABLED";

    private static final String PATCH_OP_REPLACE = "REPLACE";
    private static final String PATCH_OP_ADD = "ADD";
    private static final String PATCH_PATH_NAME = "/name";
    private static final String PATCH_PATH_STATUS = "/status";
    private static final String PATCH_PATH_ATTRIBUTES_REGION = "/attributes/Region";

    private static final int SUB_ORGANIZATION_DEPTH = 1;

    private final TestUserMode userMode;
    private final AutomationContext testAutomationContext;

    private String testTenantDomain;
    private String organizationId;
    private String organizationHandle;
    private String currentOrganizationName;
    private WebhookEventTestManager webhookEventTestManager;

    @Factory(dataProvider = "testExecutionContextProvider")
    public OrganizationManagementEventTestCase(TestUserMode userMode) throws Exception {

        this.userMode = userMode;
        this.testAutomationContext = new AutomationContext("IDENTITY", userMode);
        this.authenticatingUserName = testAutomationContext.getContextTenant().getTenantAdmin().getUserName();
        this.authenticatingCredential = testAutomationContext.getContextTenant().getTenantAdmin().getPassword();
        this.testTenantDomain = testAutomationContext.getContextTenant().getDomain();
    }

    @DataProvider(name = "testExecutionContextProvider")
    public static Object[][] getTestExecutionContext() {

        return new Object[][]{
                {TestUserMode.SUPER_TENANT_ADMIN},
                {TestUserMode.TENANT_ADMIN}
        };
    }

    @BeforeClass(alwaysRun = true)
    public void testClassInit() throws Exception {

        super.init(userMode);
        super.testInit(ORGANIZATIONS_API_VERSION, swaggerDefinition, testTenantDomain);

        organizationHandle = System.currentTimeMillis() + ORG_HANDLE_SUFFIX;
        currentOrganizationName = ORG_NAME;

        webhookEventTestManager = new WebhookEventTestManager(WEBHOOK_ENDPOINT_PATH, EVENT_PROFILE_WSO2,
                Collections.singletonList(ORGANIZATION_EVENT_CHANNEL), TEST_NAME, testAutomationContext);
    }

    @AfterClass(alwaysRun = true)
    @Override
    public void testConclude() throws Exception {

        // Clean up the organization in case a test failed before it was deleted.
        if (organizationId != null) {
            getResponseOfDelete(ORGANIZATIONS_API_BASE_PATH + PATH_DELIMITER + organizationId);
        }
        if (webhookEventTestManager != null) {
            webhookEventTestManager.teardown();
        }
        super.testConclude();
    }

    @Test
    public void testOrganizationCreatedEvent() throws Exception {

        JSONObject body = new JSONObject();
        body.put("name", ORG_NAME);
        body.put("orgHandle", organizationHandle);
        body.put("description", ORG_DESCRIPTION);
        body.put("attributes", new JSONArray().put(buildAttribute(ATTRIBUTE_KEY_COUNTRY, COUNTRY_USA)));

        Response response = getResponseOfPost(ORGANIZATIONS_API_BASE_PATH, body.toString());
        validateHttpStatusCode(response, HttpStatus.SC_CREATED);

        String location = response.getHeader(HttpHeaders.LOCATION);
        assertNotNull(location, "The location header of the created organization is null.");
        organizationId = location.substring(location.lastIndexOf(PATH_DELIMITER) + 1);
        assertNotNull(organizationId, "The id of the created organization is null.");

        webhookEventTestManager.stackExpectedEventPayload(ORGANIZATION_CREATED_EVENT_URI,
                OrganizationManagementEventTestExpectedEventPayloadBuilder
                        .buildExpectedOrganizationCreatedEventPayload(testTenantDomain, ORG_NAME, organizationHandle,
                                SUB_ORGANIZATION_DEPTH, ATTRIBUTE_KEY_COUNTRY, COUNTRY_USA));
        webhookEventTestManager.validateStackedEventPayloads();
    }

    @Test(dependsOnMethods = "testOrganizationCreatedEvent")
    public void testOrganizationUpdatedEventFromPatchedName() throws Exception {

        JSONArray body = new JSONArray()
                .put(buildPatchOperation(PATCH_OP_REPLACE, PATCH_PATH_NAME, UPDATED_ORG_NAME));

        Response response = getResponseOfPatch(organizationEndpoint(), body.toString());
        validateHttpStatusCode(response, HttpStatus.SC_OK);
        currentOrganizationName = UPDATED_ORG_NAME;

        webhookEventTestManager.stackExpectedEventPayload(ORGANIZATION_UPDATED_EVENT_URI,
                OrganizationManagementEventTestExpectedEventPayloadBuilder
                        .buildExpectedOrganizationNameUpdatedEventPayload(testTenantDomain, UPDATED_ORG_NAME,
                                organizationHandle, SUB_ORGANIZATION_DEPTH, UPDATED_ORG_NAME));
        webhookEventTestManager.validateStackedEventPayloads();
    }

    /**
     * The replacement changes the description and adds an attribute, so the organization updated event reports both.
     * The organization was created with the country attribute alone, so the industry attribute is the added one.
     */
    @Test(dependsOnMethods = "testOrganizationUpdatedEventFromPatchedName")
    public void testOrganizationUpdatedEventFromReplacement() throws Exception {

        Response response = getResponseOfPut(organizationEndpoint(),
                buildReplacementBody(currentOrganizationName, UPDATED_ORG_DESCRIPTION, STATUS_ACTIVE).toString());
        validateHttpStatusCode(response, HttpStatus.SC_OK);

        webhookEventTestManager.stackExpectedEventPayload(ORGANIZATION_UPDATED_EVENT_URI,
                OrganizationManagementEventTestExpectedEventPayloadBuilder
                        .buildExpectedOrganizationReplacedEventPayload(testTenantDomain, currentOrganizationName,
                                organizationHandle, SUB_ORGANIZATION_DEPTH, UPDATED_ORG_DESCRIPTION,
                                ATTRIBUTE_KEY_INDUSTRY, INDUSTRY_CONSTRUCTION));
        webhookEventTestManager.validateStackedEventPayloads();
    }

    /**
     * A patch which changed the status alone publishes the organization disabled event and no organization updated
     * event. Only the disabled event is stacked, so the validation fails if the updated event is published as well.
     */
    @Test(dependsOnMethods = "testOrganizationUpdatedEventFromReplacement")
    public void testStatusOnlyPatchPublishesTheDisabledEventAlone() throws Exception {

        JSONArray body = new JSONArray()
                .put(buildPatchOperation(PATCH_OP_REPLACE, PATCH_PATH_STATUS, STATUS_DISABLED));

        Response response = getResponseOfPatch(organizationEndpoint(), body.toString());
        validateHttpStatusCode(response, HttpStatus.SC_OK);

        webhookEventTestManager.stackExpectedEventPayload(ORGANIZATION_DISABLED_EVENT_URI,
                OrganizationManagementEventTestExpectedEventPayloadBuilder
                        .buildExpectedOrganizationDisabledEventPayload(testTenantDomain, currentOrganizationName,
                                organizationHandle, SUB_ORGANIZATION_DEPTH));
        webhookEventTestManager.validateStackedEventPayloads();
    }

    /**
     * A replacement which changed the status alone publishes the organization enabled event and no organization
     * updated event.
     */
    @Test(dependsOnMethods = "testStatusOnlyPatchPublishesTheDisabledEventAlone")
    public void testStatusOnlyReplacementPublishesTheEnabledEventAlone() throws Exception {

        Response response = getResponseOfPut(organizationEndpoint(),
                buildReplacementBody(currentOrganizationName, UPDATED_ORG_DESCRIPTION, STATUS_ACTIVE).toString());
        validateHttpStatusCode(response, HttpStatus.SC_OK);

        webhookEventTestManager.stackExpectedEventPayload(ORGANIZATION_ENABLED_EVENT_URI,
                OrganizationManagementEventTestExpectedEventPayloadBuilder
                        .buildExpectedOrganizationEnabledEventPayload(testTenantDomain, currentOrganizationName,
                                organizationHandle, SUB_ORGANIZATION_DEPTH));
        webhookEventTestManager.validateStackedEventPayloads();
    }

    /**
     * A patch which changed the status along with another value publishes both events, and the organization updated
     * event reports the other value alone, since the status is reported by the organization disabled event.
     */
    @Test(dependsOnMethods = "testStatusOnlyReplacementPublishesTheEnabledEventAlone")
    public void testMixedPatchPublishesBothEvents() throws Exception {

        JSONArray body = new JSONArray()
                .put(buildPatchOperation(PATCH_OP_REPLACE, PATCH_PATH_NAME, MIXED_UPDATE_ORG_NAME))
                .put(buildPatchOperation(PATCH_OP_REPLACE, PATCH_PATH_STATUS, STATUS_DISABLED));

        Response response = getResponseOfPatch(organizationEndpoint(), body.toString());
        validateHttpStatusCode(response, HttpStatus.SC_OK);
        currentOrganizationName = MIXED_UPDATE_ORG_NAME;

        webhookEventTestManager.stackExpectedEventPayload(ORGANIZATION_UPDATED_EVENT_URI,
                OrganizationManagementEventTestExpectedEventPayloadBuilder
                        .buildExpectedOrganizationNameUpdatedEventPayload(testTenantDomain, MIXED_UPDATE_ORG_NAME,
                                organizationHandle, SUB_ORGANIZATION_DEPTH, MIXED_UPDATE_ORG_NAME));
        webhookEventTestManager.stackExpectedEventPayload(ORGANIZATION_DISABLED_EVENT_URI,
                OrganizationManagementEventTestExpectedEventPayloadBuilder
                        .buildExpectedOrganizationDisabledEventPayload(testTenantDomain, MIXED_UPDATE_ORG_NAME,
                                organizationHandle, SUB_ORGANIZATION_DEPTH));
        webhookEventTestManager.validateStackedEventPayloads();
    }

    /**
     * A patch which adds an attribute reports it under the added attributes of the organization updated event. The
     * test runs after the replacements, since a replacement carries the full set of attributes and would report the
     * patched attribute as removed. It adds an attribute the organization does not hold, since an addition of an
     * attribute the organization already holds does not complete.
     */
    @Test(dependsOnMethods = "testMixedPatchPublishesBothEvents")
    public void testOrganizationUpdatedEventFromPatchedAttribute() throws Exception {

        JSONArray body = new JSONArray().put(buildPatchOperation(PATCH_OP_ADD, PATCH_PATH_ATTRIBUTES_REGION,
                REGION_APAC));

        Response response = getResponseOfPatch(organizationEndpoint(), body.toString());
        validateHttpStatusCode(response, HttpStatus.SC_OK);

        webhookEventTestManager.stackExpectedEventPayload(ORGANIZATION_UPDATED_EVENT_URI,
                OrganizationManagementEventTestExpectedEventPayloadBuilder
                        .buildExpectedOrganizationAttributeAddedEventPayload(testTenantDomain, currentOrganizationName,
                                organizationHandle, SUB_ORGANIZATION_DEPTH, ATTRIBUTE_KEY_REGION, REGION_APAC));
        webhookEventTestManager.validateStackedEventPayloads();
    }

    @Test(dependsOnMethods = "testOrganizationUpdatedEventFromPatchedAttribute")
    public void testOrganizationDeletedEvent() throws Exception {

        Response response = getResponseOfDelete(organizationEndpoint());
        validateHttpStatusCode(response, HttpStatus.SC_NO_CONTENT);

        webhookEventTestManager.stackExpectedEventPayload(ORGANIZATION_DELETED_EVENT_URI,
                OrganizationManagementEventTestExpectedEventPayloadBuilder
                        .buildExpectedOrganizationDeletedEventPayload(testTenantDomain, currentOrganizationName,
                                organizationHandle, SUB_ORGANIZATION_DEPTH));
        webhookEventTestManager.validateStackedEventPayloads();
        organizationId = null;
    }

    private String organizationEndpoint() {

        return ORGANIZATIONS_API_BASE_PATH + PATH_DELIMITER + organizationId;
    }

    private JSONObject buildReplacementBody(String name, String description, String status) throws Exception {

        JSONObject body = new JSONObject();
        body.put("name", name);
        body.put("description", description);
        body.put("status", status);
        body.put("attributes", new JSONArray()
                .put(buildAttribute(ATTRIBUTE_KEY_COUNTRY, COUNTRY_USA))
                .put(buildAttribute(ATTRIBUTE_KEY_INDUSTRY, INDUSTRY_CONSTRUCTION)));
        return body;
    }

    private JSONObject buildAttribute(String key, String value) throws Exception {

        JSONObject attribute = new JSONObject();
        attribute.put("key", key);
        attribute.put("value", value);
        return attribute;
    }

    private JSONObject buildPatchOperation(String operation, String path, String value) throws Exception {

        JSONObject patchOperation = new JSONObject();
        patchOperation.put("operation", operation);
        patchOperation.put("path", path);
        patchOperation.put("value", value);
        return patchOperation;
    }
}

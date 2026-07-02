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

package org.wso2.identity.integration.test.webhooks.consent;

import org.testng.annotations.AfterClass;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.DataProvider;
import org.testng.annotations.Factory;
import org.testng.annotations.Test;
import org.wso2.carbon.automation.engine.context.AutomationContext;
import org.wso2.carbon.automation.engine.context.TestUserMode;
import org.wso2.carbon.utils.multitenancy.MultitenantConstants;
import org.wso2.identity.integration.common.utils.ISIntegrationTest;
import org.wso2.identity.integration.test.rest.api.user.common.model.Email;
import org.wso2.identity.integration.test.rest.api.user.common.model.Name;
import org.wso2.identity.integration.test.rest.api.user.common.model.UserObject;
import org.wso2.identity.integration.test.restclients.ConsentManagementRestClient;
import org.wso2.identity.integration.test.restclients.SCIM2RestClient;
import org.wso2.identity.integration.test.webhooks.consent.eventpayloadbuilder.ConsentEventTestExpectedEventPayloadBuilder;
import org.wso2.identity.integration.test.webhooks.util.WebhookEventTestManager;

import java.util.Arrays;

import static org.testng.Assert.assertNotNull;

/**
 * Integration tests for consent-related webhook events.
 *
 * <p>Covers:
 * <ul>
 *   <li>{@code consentAdded} — fired when a user grants consent via the user consent API.</li>
 *   <li>{@code consentRevoked} — fired when a user revokes a consent receipt.</li>
 * </ul>
 *
 * <p>The test subscribes to the {@code https://schemas.identity.wso2.org/events/consent} channel
 * and validates that the webhook payload received by the mock service matches the expected structure.
 */
public class ConsentEventTestCase extends ISIntegrationTest {

    private static final String CONSENT_TEST_USER_NAME = "consent-event-test-user";
    private static final String CONSENT_TEST_USER_PASSWORD = "TestPassword@123";
    private static final String CONSENT_TEST_USER_EMAIL = "consent-event-test@wso2.com";

    private static final String CONSENT_SERVICE_ID = "test-consent-webhook-service";
    // These values must match the names defined in create-purpose.json and create-element.json.
    private static final String CONSENT_PURPOSE_NAME = "User Authentication";
    private static final String CONSENT_ELEMENT_NAME = "email_address";

    private static final String CONSENT_ADDED_EVENT_URI =
            "https://schemas.identity.wso2.org/events/consent/event-type/consentAdded";
    private static final String CONSENT_REVOKED_EVENT_URI =
            "https://schemas.identity.wso2.org/events/consent/event-type/consentRevoked";

    private final AutomationContext automationContext;
    private final TestUserMode userMode;

    private SCIM2RestClient scim2RestClient;
    private ConsentManagementRestClient consentManagementRestClient;
    private WebhookEventTestManager webhookEventTestManager;

    private String testUserId;
    private String elementId;
    private String purposeId;
    private String consentReceiptId;

    /** Auth username for the consent subject; includes tenant domain for tenant users. */
    private String consentTestUserAuthName;

    @Factory(dataProvider = "testExecutionContextProvider")
    public ConsentEventTestCase(TestUserMode userMode) throws Exception {

        this.userMode = userMode;
        automationContext = new AutomationContext("IDENTITY", userMode);
    }

    @DataProvider(name = "testExecutionContextProvider")
    public static Object[][] getTestExecutionContext() {

        return new Object[][]{
                {TestUserMode.SUPER_TENANT_USER},
                {TestUserMode.TENANT_USER}
        };
    }

    @BeforeClass(alwaysRun = true)
    public void testInit() throws Exception {

        super.init(userMode);

        scim2RestClient = new SCIM2RestClient(serverURL, tenantInfo);
        consentManagementRestClient = new ConsentManagementRestClient(serverURL, tenantInfo);

        String tenantDomain = automationContext.getContextTenant().getDomain();
        consentTestUserAuthName = MultitenantConstants.SUPER_TENANT_DOMAIN_NAME.equals(tenantDomain)
                ? CONSENT_TEST_USER_NAME
                : CONSENT_TEST_USER_NAME + "@" + tenantDomain;

        UserObject userInfo = new UserObject()
                .userName(CONSENT_TEST_USER_NAME)
                .password(CONSENT_TEST_USER_PASSWORD)
                .name(new Name().givenName("Consent").familyName("TestUser"))
                .addEmail(new Email().value(CONSENT_TEST_USER_EMAIL));
        testUserId = scim2RestClient.createUser(userInfo);

        elementId = consentManagementRestClient.createElement();
        purposeId = consentManagementRestClient.createPurpose(elementId);

        webhookEventTestManager = new WebhookEventTestManager("/consent/webhook", "WSO2",
                Arrays.asList("https://schemas.identity.wso2.org/events/consent"),
                "ConsentEventTestCase",
                automationContext);
    }

    @AfterClass(alwaysRun = true)
    public void atEnd() throws Exception {

        if (scim2RestClient != null) {
            if (testUserId != null) {
                scim2RestClient.deleteUser(testUserId);
            }
            scim2RestClient.closeHttpClient();
        }
        if (consentManagementRestClient != null) {
            if (purposeId != null) {
                consentManagementRestClient.deletePurpose(purposeId);
            }
            if (elementId != null) {
                consentManagementRestClient.deleteElement(elementId);
            }
            consentManagementRestClient.closeHttpClient();
        }
        if (webhookEventTestManager != null) {
            webhookEventTestManager.teardown();
        }
    }

    @Test
    public void testConsentAdded() throws Exception {

        assertNotNull(testUserId);

        String tenantDomain = automationContext.getContextTenant().getDomain();

        consentReceiptId = consentManagementRestClient.createConsent(
                CONSENT_SERVICE_ID, purposeId, elementId,
                consentTestUserAuthName, CONSENT_TEST_USER_PASSWORD);

        assertNotNull(consentReceiptId, "Consent receipt ID should not be null after creation.");

        webhookEventTestManager.stackExpectedEventPayload(
                CONSENT_ADDED_EVENT_URI,
                ConsentEventTestExpectedEventPayloadBuilder.buildExpectedConsentAddedEventPayload(
                        CONSENT_TEST_USER_NAME,
                        CONSENT_SERVICE_ID,
                        CONSENT_PURPOSE_NAME,
                        CONSENT_ELEMENT_NAME,
                        tenantDomain));
        webhookEventTestManager.validateStackedEventPayloads();
    }

    @Test(dependsOnMethods = "testConsentAdded")
    public void testConsentRevoked() throws Exception {

        assertNotNull(consentReceiptId);

        String tenantDomain = automationContext.getContextTenant().getDomain();

        consentManagementRestClient.revokeConsent(
                consentReceiptId, consentTestUserAuthName, CONSENT_TEST_USER_PASSWORD);

        webhookEventTestManager.stackExpectedEventPayload(
                CONSENT_REVOKED_EVENT_URI,
                ConsentEventTestExpectedEventPayloadBuilder.buildExpectedConsentRevokedEventPayload(
                        CONSENT_TEST_USER_NAME,
                        CONSENT_SERVICE_ID,
                        CONSENT_PURPOSE_NAME,
                        CONSENT_ELEMENT_NAME,
                        tenantDomain));
        webhookEventTestManager.validateStackedEventPayloads();
    }
}

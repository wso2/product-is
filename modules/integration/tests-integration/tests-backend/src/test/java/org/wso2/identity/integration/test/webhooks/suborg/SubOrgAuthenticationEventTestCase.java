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

package org.wso2.identity.integration.test.webhooks.suborg;

import org.json.JSONObject;
import org.testng.annotations.AfterClass;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.DataProvider;
import org.testng.annotations.Factory;
import org.testng.annotations.Test;
import org.wso2.carbon.automation.engine.context.TestUserMode;

import static org.wso2.identity.integration.test.webhooks.suborg.eventpayloadbuilder.SubOrgAuthenticationEventTestExpectedEventPayloadBuilder.buildExpectedLoginFailedEventPayloadForInvalidCredentials;
import static org.wso2.identity.integration.test.webhooks.suborg.eventpayloadbuilder.SubOrgAuthenticationEventTestExpectedEventPayloadBuilder.buildExpectedLoginSuccessEventPayload;
import static org.wso2.identity.integration.test.webhooks.suborg.eventpayloadbuilder.SubOrgAuthenticationEventTestExpectedEventPayloadBuilder.buildExpectedSessionEstablishedEventPayload;

/**
 * Tests the login and session events triggered when sub organization users log in, as received by a root
 * organization webhook subscribed to the login and session events of all the sub organizations.
 */
public class SubOrgAuthenticationEventTestCase extends SubOrgLoginFlowTestBase {

    private static final String TEST_NAME = "SubOrgAuthenticationEventTestCase";
    private static final String LOGIN_EVENT_CHANNEL = "https://schemas.identity.wso2.org/events/login";
    private static final String SESSION_EVENT_CHANNEL = "https://schemas.identity.wso2.org/events/session";
    private static final String LOGIN_SUCCESS_EVENT_URI = LOGIN_EVENT_CHANNEL + "/event-type/loginSuccess";
    private static final String LOGIN_FAILED_EVENT_URI = LOGIN_EVENT_CHANNEL + "/event-type/loginFailed";
    private static final String SESSION_ESTABLISHED_EVENT_URI = SESSION_EVENT_CHANNEL +
            "/event-type/sessionEstablished";

    @Factory(dataProvider = "testExecutionContextProvider")
    public SubOrgAuthenticationEventTestCase(TestUserMode userMode, String[] subOrgNames) {

        super(userMode, subOrgNames, TEST_NAME);
    }

    @DataProvider(name = "testExecutionContextProvider")
    public static Object[][] getTestExecutionContext() {

        return new Object[][]{
                {TestUserMode.SUPER_TENANT_ADMIN, new String[]{"webhook_auth_sub_org_1", "webhook_auth_sub_org_2"}},
                {TestUserMode.TENANT_ADMIN, new String[]{"webhook_auth_t_sub_org_1", "webhook_auth_t_sub_org_2"}}
        };
    }

    @BeforeClass(alwaysRun = true)
    public void testInit() throws Exception {

        setUpLoginTestData();
        subscribeWebhook(LOGIN_EVENT_CHANNEL, SESSION_EVENT_CHANNEL);
    }

    @AfterClass(alwaysRun = true)
    public void atEnd() throws Exception {

        tearDownLoginTestData();
    }

    @Test(dataProvider = "subOrgProvider",
            description = "Sub organization user logs in to a shared application with enhanced login enabled, " +
                    "through the organization qualified path of the root tenant.")
    public void testEnhancedLoginSharedAppLoginSuccess(SubOrg subOrg) throws Exception {

        loginToEnhancedLoginApp(subOrg, true);

        // The login and session events refer to the main application in the root organization.
        stackExpectedLocalLoginSuccessEvents(subOrg, toApplicationObject(enhancedLoginApp));
        webhookEventTestManager.validateStackedEventPayloads();
    }

    @Test(dataProvider = "subOrgProvider", dependsOnMethods = "testEnhancedLoginSharedAppLoginSuccess",
            description = "Sub organization user fails to log in to a shared application with enhanced login " +
                    "enabled, through the organization qualified path of the root tenant.")
    public void testEnhancedLoginSharedAppLoginFailure(SubOrg subOrg) throws Exception {

        loginToEnhancedLoginApp(subOrg, false);

        stackExpectedLoginFailedEvent(subOrg, toApplicationObject(enhancedLoginApp));
        webhookEventTestManager.validateStackedEventPayloads();
    }

    @Test(dataProvider = "subOrgProvider", dependsOnMethods = "testEnhancedLoginSharedAppLoginFailure",
            description = "Sub organization user logs in to a shared application with enhanced login enabled, " +
                    "starting from the root organization and discovering the sub organization.")
    public void testEnhancedLoginSharedAppOrgDiscoveryLoginSuccess(SubOrg subOrg) throws Exception {

        loginToEnhancedLoginAppWithOrgDiscovery(subOrg, true);

        // Organization discovery switches the same authentication context to the shared application login, hence
        // the events are the same as when logging in through the organization qualified path.
        stackExpectedLocalLoginSuccessEvents(subOrg, toApplicationObject(enhancedLoginApp));
        webhookEventTestManager.validateStackedEventPayloads();
    }

    @Test(dataProvider = "subOrgProvider", dependsOnMethods = "testEnhancedLoginSharedAppOrgDiscoveryLoginSuccess",
            description = "Sub organization user fails to log in to a shared application with enhanced login " +
                    "enabled, starting from the root organization and discovering the sub organization.")
    public void testEnhancedLoginSharedAppOrgDiscoveryLoginFailure(SubOrg subOrg) throws Exception {

        loginToEnhancedLoginAppWithOrgDiscovery(subOrg, false);

        stackExpectedLoginFailedEvent(subOrg, toApplicationObject(enhancedLoginApp));
        webhookEventTestManager.validateStackedEventPayloads();
    }

    @Test(dataProvider = "subOrgProvider", dependsOnMethods = "testEnhancedLoginSharedAppOrgDiscoveryLoginFailure",
            description = "Sub organization user logs in to a shared application with enhanced login disabled.")
    public void testLegacyLoginSharedAppLoginSuccess(SubOrg subOrg) throws Exception {

        loginToLegacyLoginApp(subOrg, true);

        // Only the root organization leg of the login publishes the login and session events. They are published
        // for the sub organization, with the user federated from the sub organization. The federated user has no
        // user store, and its username claim carries the user ID.
        JSONObject subOrganization = subOrg.toOrganizationObject();
        JSONObject application = toApplicationObject(legacyLoginApp);
        webhookEventTestManager.stackExpectedEventPayload(LOGIN_SUCCESS_EVENT_URI,
                buildExpectedLoginSuccessEventPayload(subOrg.userId, subOrg.userId, rootTenantDomain,
                        subOrganization, application, false));
        webhookEventTestManager.stackExpectedEventPayload(SESSION_ESTABLISHED_EVENT_URI,
                buildExpectedSessionEstablishedEventPayload(subOrg.userId, subOrg.userId, rootTenantDomain,
                        subOrganization, application, false));
        webhookEventTestManager.validateStackedEventPayloads();
    }

    @Test(dataProvider = "subOrgProvider", dependsOnMethods = "testLegacyLoginSharedAppLoginSuccess",
            description = "Sub organization user fails to log in to a shared application with enhanced login " +
                    "disabled.")
    public void testLegacyLoginSharedAppLoginFailure(SubOrg subOrg) throws Exception {

        loginToLegacyLoginApp(subOrg, false);

        // The failure happens at the sub organization leg of the login, which does not publish a login failed
        // event when enhanced login is disabled for the main application.
        webhookEventTestManager.validateNoEventsReceived();
    }

    @Test(dataProvider = "subOrgProvider", dependsOnMethods = "testLegacyLoginSharedAppLoginFailure",
            description = "Sub organization user logs in to an application created in the sub organization.")
    public void testSubOrgAppLoginSuccess(SubOrg subOrg) throws Exception {

        loginToSubOrgApp(subOrg, true);

        stackExpectedLocalLoginSuccessEvents(subOrg, toSubOrgApplicationObject(subOrg));
        webhookEventTestManager.validateStackedEventPayloads();
    }

    @Test(dataProvider = "subOrgProvider", dependsOnMethods = "testSubOrgAppLoginSuccess",
            description = "Sub organization user fails to log in to an application created in the sub organization.")
    public void testSubOrgAppLoginFailure(SubOrg subOrg) throws Exception {

        loginToSubOrgApp(subOrg, false);

        stackExpectedLoginFailedEvent(subOrg, toSubOrgApplicationObject(subOrg));
        webhookEventTestManager.validateStackedEventPayloads();
    }

    /**
     * Stacks the login success and session established events of a login performed directly in the sub
     * organization.
     */
    private void stackExpectedLocalLoginSuccessEvents(SubOrg subOrg, JSONObject application) throws Exception {

        JSONObject subOrganization = subOrg.toOrganizationObject();
        webhookEventTestManager.stackExpectedEventPayload(LOGIN_SUCCESS_EVENT_URI,
                buildExpectedLoginSuccessEventPayload(subOrg.userId, subOrg.username, rootTenantDomain,
                        subOrganization, application, true));
        webhookEventTestManager.stackExpectedEventPayload(SESSION_ESTABLISHED_EVENT_URI,
                buildExpectedSessionEstablishedEventPayload(subOrg.userId, subOrg.username, rootTenantDomain,
                        subOrganization, application, true));
    }

    private void stackExpectedLoginFailedEvent(SubOrg subOrg, JSONObject application) throws Exception {

        webhookEventTestManager.stackExpectedEventPayload(LOGIN_FAILED_EVENT_URI,
                buildExpectedLoginFailedEventPayloadForInvalidCredentials(subOrg.username, subOrg.email,
                        rootTenantDomain, subOrg.toOrganizationObject(), application));
    }
}

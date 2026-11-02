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
import org.testng.annotations.DataProvider;
import org.wso2.carbon.automation.engine.context.AutomationContext;
import org.wso2.carbon.automation.engine.context.TestUserMode;
import org.wso2.identity.integration.test.oauth2.OAuth2ServiceAbstractIntegrationTest;
import org.wso2.identity.integration.test.rest.api.common.RESTTestBase;
import org.wso2.identity.integration.test.rest.api.server.webhook.management.v2.model.ChildOrgSubscription;
import org.wso2.identity.integration.test.restclients.OAuth2RestClient;
import org.wso2.identity.integration.test.restclients.OrgMgtRestClient;
import org.wso2.identity.integration.test.restclients.SCIM2RestClient;
import org.wso2.identity.integration.test.webhooks.util.WebhookEventTestManager;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import static org.testng.Assert.assertNotNull;
import static org.wso2.identity.integration.test.webhooks.suborg.eventpayloadbuilder.SubOrgEventPayloadUtils.createSubOrganizationObject;

/**
 * Base class for the tests of the events triggered in sub organizations, as received by a root organization webhook
 * that subscribes to the events of all child organizations through the webhook management v2 API.
 * Creates the sub organizations of the test, and the webhook after the test data is set up, so that the events
 * triggered while setting up the test data are not received.
 * Requires [webhooks] child_organization_subscription_enabled = true, which is set by
 * {@link ChildOrgSubscriptionEnabledInitializerTestCase}.
 */
public abstract class SubOrgEventTestBase extends OAuth2ServiceAbstractIntegrationTest {

    protected static final String PASSWORD = "TestPassword@123";
    // Absolute path, since the resource is kept with the OAuth2 tests rather than in the package of this class.
    private static final String MGT_APP_AUTHORIZED_API_RESOURCES =
            "/org/wso2/identity/integration/test/oauth2/management-app-authorized-apis.json";

    protected final TestUserMode userMode;
    protected final String testName;
    protected final List<SubOrg> subOrgs = new ArrayList<>();
    private final String[] subOrgNames;

    protected AutomationContext automationContext;
    protected WebhookEventTestManager webhookEventTestManager;
    protected OrgMgtRestClient orgMgtRestClient;
    protected OAuth2RestClient oAuth2RestClient;
    protected SCIM2RestClient scim2RestClient;
    protected String rootTenantDomain;

    protected SubOrgEventTestBase(TestUserMode userMode, String[] subOrgNames, String testName) {

        this.userMode = userMode;
        this.subOrgNames = subOrgNames;
        this.testName = testName;
    }

    @DataProvider(name = "subOrgProvider")
    public Object[][] getSubOrgs() {

        return subOrgs.stream().map(subOrg -> new Object[]{subOrg}).toArray(Object[][]::new);
    }

    /**
     * Initializes the clients and creates the sub organizations of the test.
     */
    protected void setUpSubOrgs() throws Exception {

        super.init(userMode);
        automationContext = new AutomationContext("IDENTITY", userMode);
        rootTenantDomain = tenantInfo.getDomain();

        oAuth2RestClient = new OAuth2RestClient(serverURL, tenantInfo);
        scim2RestClient = new SCIM2RestClient(serverURL, tenantInfo);
        orgMgtRestClient = new OrgMgtRestClient(isServer, tenantInfo, serverURL,
                new JSONObject(RESTTestBase.readResource(MGT_APP_AUTHORIZED_API_RESOURCES, getClass())));

        String m2mToken = orgMgtRestClient.getM2MAccessToken();
        for (String orgName : subOrgNames) {
            SubOrg subOrg = new SubOrg();
            subOrg.name = orgName;
            subOrg.handle = orgName;
            subOrg.id = orgMgtRestClient.addOrganizationWithToken(orgName, orgName, m2mToken);
            assertNotNull(subOrg.id, "Failed to create sub organization: " + orgName);
            subOrg.switchedToken = orgMgtRestClient.switchM2MToken(subOrg.id);
            subOrgs.add(subOrg);
        }
    }

    /**
     * Creates the webhook that subscribes to the given channels of the root organization and of all the sub
     * organizations, and discards any event received before the subscription is complete.
     */
    protected void subscribeWebhook(String... channels) throws Exception {

        webhookEventTestManager = new WebhookEventTestManager("/" + testName + "/webhook", "WSO2",
                Arrays.asList(channels), testName, automationContext,
                ChildOrgSubscription.SubscriptionPolicyEnum.ALL_EXISTING_AND_FUTURE_ORGS);
        webhookEventTestManager.clearReceivedEvents();
    }

    /**
     * Deletes the webhook, the sub organization users and the sub organizations, and closes the clients.
     */
    protected void tearDownSubOrgs() throws Exception {

        if (webhookEventTestManager != null) {
            webhookEventTestManager.teardown();
        }
        for (SubOrg subOrg : subOrgs) {
            if (subOrg.userId != null) {
                scim2RestClient.deleteSubOrgUser(subOrg.userId, subOrg.switchedToken);
            }
            // Deleting the sub organization deletes its applications as well.
            orgMgtRestClient.deleteOrganization(subOrg.id);
        }
        orgMgtRestClient.closeHttpClient();
        oAuth2RestClient.closeHttpClient();
        scim2RestClient.closeHttpClient();
        restClient.closeHttpClient();
    }

    /**
     * A sub organization of the test, with its user and application when the test creates them.
     */
    protected static class SubOrg {

        String id;
        String name;
        String handle;
        String switchedToken;
        String userId;
        String username;
        String email;
        String appId;
        String appName;
        String clientId;
        String clientSecret;

        JSONObject toOrganizationObject() throws Exception {

            return createSubOrganizationObject(id, name, handle);
        }

        @Override
        public String toString() {

            return name;
        }
    }
}

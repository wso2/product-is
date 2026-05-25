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

package org.wso2.identity.integration.test.serviceextensions.customauthentication;

import com.github.tomakehurst.wiremock.client.WireMock;
import org.apache.http.HttpResponse;
import org.apache.http.util.EntityUtils;
import org.testng.Assert;
import org.testng.annotations.DataProvider;
import org.testng.annotations.Factory;
import org.testng.annotations.Test;
import org.wso2.carbon.automation.engine.context.TestUserMode;
import org.wso2.identity.integration.test.serviceextensions.mockservices.MockCustomAuthenticatorService;
import org.wso2.identity.integration.test.serviceextensions.mockservices.ServiceExtensionMockServer;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import static org.testng.Assert.assertNotNull;

/**
 * End-to-end failure tests for user-defined federated custom authenticators configured with an
 * OAuth2 endpoint authentication mode (CLIENT_CREDENTIAL, PASSWORD_CREDENTIAL). The token endpoint
 * is stubbed to return HTTP 500.
 *
 * One TestNG instance per (tenant, OAuth2-scenario) pair via {@link Factory}.
 */
public class CustomAuthEndpointAuthFederatedFailureTestCase extends CustomAuthEndpointAuthFederatedBaseTestCase {

    @Factory(dataProvider = "scenarioProvider")
    public CustomAuthEndpointAuthFederatedFailureTestCase(TestUserMode testUserMode, EndpointAuthScenario scenario) {

        super(testUserMode, scenario);
    }

    @DataProvider(name = "scenarioProvider")
    public static Object[][] scenarioProvider() {

        TestUserMode[] userModes = {TestUserMode.SUPER_TENANT_USER, TestUserMode.TENANT_USER};
        List<Object[]> combinations = new ArrayList<>();
        for (TestUserMode mode : userModes) {
            for (EndpointAuthScenario scenario : EndpointAuthScenario.values()) {
                if (scenario.requiresTokenEndpoint()) {
                    combinations.add(new Object[]{mode, scenario});
                }
            }
        }
        return combinations.toArray(new Object[0][]);
    }

    @Override
    protected String variantSuffix() {

        return "failure";
    }

    @Override
    protected void installTokenEndpointStubs(ServiceExtensionMockServer server) throws IOException {

        scenario.installFailureTokenEndpointStub(server);
    }

    @Test(groups = "wso2.is", description = "When the OAuth2 token endpoint fails, the federated authorize " +
            "flow must not yield an authorization code.")
    public void testAuthorizeFlowFailsWhenTokenEndpointFails() throws Exception {

        Thread.sleep(5000);

        HttpResponse response = drivePinAuthenticationFlowExpectingFailure();
        assertNotNull(response, "Expected a response from the commonauth endpoint when token acquisition fails.");

        String authorizationCodeIfAny = null;
        if (response.getFirstHeader("Location") != null) {
            String location = response.getFirstHeader("Location").getValue();
            HttpResponse next = sendGetRequest(httpClient, location);
            String maybeCodeUrl = next.getFirstHeader("Location") != null
                    ? next.getFirstHeader("Location").getValue()
                    : location;
            EntityUtils.consume(next.getEntity());
            authorizationCodeIfAny = getAuthorizationCodeFromURL(maybeCodeUrl);
        }
        EntityUtils.consume(response.getEntity());

        Assert.assertNull(authorizationCodeIfAny,
                "Authorization code must not be issued when the token endpoint fails.");
    }

    @Test(groups = "wso2.is", dependsOnMethods = "testAuthorizeFlowFailsWhenTokenEndpointFails",
            description = "When the OAuth2 token endpoint fails, IS must not invoke the second " +
                    "/api/authenticate call against the custom authenticator endpoint.")
    public void testCustomAuthenticatorEndpointNotInvokedPostFailure() {

        int callCount = WireMock.findAll(WireMock.postRequestedFor(
                WireMock.urlEqualTo(MockCustomAuthenticatorService.API_AUTHENTICATE_ENDPOINT))).size();
        Assert.assertTrue(callCount <= 1,
                "Expected at most one /api/authenticate call when token endpoint fails, got " + callCount);
    }
}

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

import org.apache.http.HttpResponse;
import org.testng.SkipException;
import org.testng.annotations.DataProvider;
import org.testng.annotations.Factory;
import org.testng.annotations.Test;
import org.wso2.carbon.automation.engine.context.TestUserMode;
import org.wso2.identity.integration.test.serviceextensions.mockservices.ServiceExtensionMockServer;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import static com.github.tomakehurst.wiremock.client.WireMock.equalTo;
import static com.github.tomakehurst.wiremock.client.WireMock.postRequestedFor;
import static com.github.tomakehurst.wiremock.client.WireMock.urlEqualTo;
import static com.github.tomakehurst.wiremock.client.WireMock.verify;
import static org.testng.Assert.assertNotNull;
import static org.wso2.identity.integration.test.serviceextensions.mockservices.MockCustomAuthenticatorService.API_AUTHENTICATE_ENDPOINT;

/**
 * End-to-end success tests for local user-defined custom authenticators across every supported
 * endpoint authentication mode (BASIC, BEARER, API_KEY, CLIENT_CREDENTIAL, PASSWORD_CREDENTIAL).
 *
 * One TestNG instance is created per (tenant, auth-type) pair via {@link Factory}, so the same
 * three test methods run once per combination — keeping the class count low without losing
 * coverage breadth.
 */
public class CustomAuthEndpointAuthLocalSuccessTestCase extends CustomAuthEndpointAuthLocalBaseTestCase {

    @Factory(dataProvider = "scenarioProvider")
    public CustomAuthEndpointAuthLocalSuccessTestCase(TestUserMode testUserMode, EndpointAuthScenario scenario) {

        super(testUserMode, scenario);
    }

    @DataProvider(name = "scenarioProvider")
    public static Object[][] scenarioProvider() {

        TestUserMode[] userModes = {TestUserMode.SUPER_TENANT_USER, TestUserMode.TENANT_USER};
        EndpointAuthScenario[] scenarios = EndpointAuthScenario.values();
        List<Object[]> combinations = new ArrayList<>();
        for (TestUserMode mode : userModes) {
            for (EndpointAuthScenario scenario : scenarios) {
                combinations.add(new Object[]{mode, scenario});
            }
        }
        return combinations.toArray(new Object[0][]);
    }

    @Override
    protected String variantSuffix() {

        return "success";
    }

    @Override
    protected void installTokenEndpointStubs(ServiceExtensionMockServer server) throws IOException {

        scenario.installSuccessTokenEndpointStub(server);
    }

    @Test(groups = "wso2.is", description = "Drive the OAuth authorize-code flow against a local user-defined " +
            "custom authenticator and confirm the mock authenticator endpoint receives the configured inbound " +
            "credentials for the scenario.")
    public void testInitAuthorizeRequestWithCustomAuthentication() throws Exception {

        Thread.sleep(5000);
        authorizationCode = drivePinAuthenticationFlow();
        assertNotNull(authorizationCode);

        verify(postRequestedFor(urlEqualTo(API_AUTHENTICATE_ENDPOINT))
                .withHeader(scenario.expectedInboundHeaderName(), equalTo(scenario.expectedInboundHeaderValue())));
    }

    @Test(groups = "wso2.is", dependsOnMethods = "testInitAuthorizeRequestWithCustomAuthentication",
            description = "For OAuth2-based endpoint authentication modes, verify the configured token endpoint " +
                    "received the expected grant request. Skipped for scenarios that don't use a token endpoint.")
    public void testTokenEndpointReceivedExpectedGrantRequest() {

        if (!scenario.requiresTokenEndpoint()) {
            throw new SkipException("Not applicable for endpoint auth scenario: " + scenario.name());
        }
        scenario.verifyTokenEndpointReceivedGrantRequest(serviceExtensionMockServer);
    }

    @Test(groups = "wso2.is", dependsOnMethods = "testInitAuthorizeRequestWithCustomAuthentication",
            description = "Exchange the authorization code for tokens and verify id_token claims.")
    public void testGetAccessTokenWithAuthCodeGrant() throws Exception {

        HttpResponse response = exchangeAuthorizationCodeForTokens();
        assertExpectedTokenClaims(response);
    }
}

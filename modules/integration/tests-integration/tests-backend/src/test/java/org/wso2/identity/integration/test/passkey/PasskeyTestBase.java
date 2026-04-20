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

package org.wso2.identity.integration.test.passkey;

import org.wso2.identity.integration.test.oauth2.OAuth2ServiceAbstractIntegrationTest;
import org.wso2.identity.integration.test.rest.api.server.application.management.v1.model.ApplicationModel;
import org.wso2.identity.integration.test.rest.api.server.application.management.v1.model.AuthenticationSequence;
import org.wso2.identity.integration.test.rest.api.server.application.management.v1.model.AuthenticationStep;
import org.wso2.identity.integration.test.rest.api.server.application.management.v1.model.Authenticator;
import org.wso2.identity.integration.test.rest.api.server.application.management.v1.model.InboundProtocols;
import org.wso2.identity.integration.test.rest.api.server.application.management.v1.model.OpenIDConnectConfiguration;
import org.wso2.identity.integration.test.rest.api.server.identity.governance.v1.dto.ConnectorsPatchReq;
import org.wso2.identity.integration.test.rest.api.server.identity.governance.v1.dto.PropertyReq;
import org.wso2.identity.integration.test.restclients.IdentityGovernanceRestClient;

import java.io.IOException;
import java.util.Collections;

/**
 * Base class for passkey integration tests.
 */
public abstract class PasskeyTestBase extends OAuth2ServiceAbstractIntegrationTest {

    private static final String PASSKEY_MFA_CATEGORY_ID = "TXVsdGkgRmFjdG9yIEF1dGhlbnRpY2F0b3Jz";
    private static final String PASSKEY_CONNECTOR_ID = "RklET0F1dGhlbnRpY2F0b3I";

    /**
     * Enables or disables username-less authentication for passkeys via the identity governance API.
     *
     * @param enable Whether to enable username-less authentication.
     * @throws IOException If an error occurred while updating the connector property.
     */
    protected void setUsernameLessAuthenticationEnabled(boolean enable) throws IOException {

        updatePasskeyConnectorProperty("FIDO.EnableUsernameLessAuthentication", String.valueOf(enable));
    }

    /**
     * Enables or disables passkey progressive enrollment via the identity governance API.
     *
     * @param enable Whether to enable passkey progressive enrollment.
     * @throws IOException If an error occurred while updating the connector property.
     */
    protected void setPasskeyProgressiveEnrollmentEnabled(boolean enable) throws IOException {

        updatePasskeyConnectorProperty("FIDO.EnablePasskeyProgressiveEnrollment", String.valueOf(enable));
    }

    private void updatePasskeyConnectorProperty(String name, String value) throws IOException {

        PropertyReq property = new PropertyReq();
        property.setName(name);
        property.setValue(value);

        ConnectorsPatchReq connectorPatchRequest = new ConnectorsPatchReq();
        connectorPatchRequest.setOperation(ConnectorsPatchReq.OperationEnum.UPDATE);
        connectorPatchRequest.addProperties(property);

        IdentityGovernanceRestClient governanceClient =
                new IdentityGovernanceRestClient(serverURL, tenantInfo);
        try {
            governanceClient.updateConnectors(PASSKEY_MFA_CATEGORY_ID, PASSKEY_CONNECTOR_ID,
                    connectorPatchRequest);
        } finally {
            governanceClient.closeHttpClient();
        }
    }

    /**
     * Creates an OIDC application with authorization code grant configured for password-less login
     * using passkey with progressive enrollment.
     *
     * @param appName     Name of the application.
     * @param callbackUrl Redirect callback URL.
     * @return ID of the created application.
     * @throws Exception If an error occurred while creating the application.
     */
    protected String addOIDCAppWithPasskeyProgressiveEnrollment(String appName, String callbackUrl)
            throws Exception {

        ApplicationModel application = new ApplicationModel();
        application.setName(appName);

        OpenIDConnectConfiguration oidcConfig = new OpenIDConnectConfiguration();
        oidcConfig.setGrantTypes(Collections.singletonList("authorization_code"));
        oidcConfig.setCallbackURLs(Collections.singletonList(callbackUrl));

        InboundProtocols inboundProtocols = new InboundProtocols();
        inboundProtocols.setOidc(oidcConfig);
        application.setInboundProtocolConfiguration(inboundProtocols);

        Authenticator basicAuthenticator = new Authenticator();
        basicAuthenticator.setIdp("LOCAL");
        basicAuthenticator.setAuthenticator("BasicAuthenticator");

        Authenticator passkeyAuthenticator = new Authenticator();
        passkeyAuthenticator.setIdp("LOCAL");
        passkeyAuthenticator.setAuthenticator("FIDOAuthenticator");

        AuthenticationStep step = new AuthenticationStep();
        step.setId(1);
        step.addOptionsItem(basicAuthenticator);
        step.addOptionsItem(passkeyAuthenticator);

        String script = "var onLoginRequest = function(context) {\n" +
                "    executeStep(1, {\n" +
                "        onFail: function(context) {\n" +
                "            var authenticatorStatus = context.request.params.scenario;\n" +
                "\n" +
                "            // Passkey progressive enrollment flow trigger\n" +
                "            if (authenticatorStatus != null && authenticatorStatus[0] == 'INIT_FIDO_ENROLL') {\n" +
                "                var filteredAuthenticationOptions = filterAuthenticators(context.steps[1].options, 'FIDOAuthenticator');\n" +
                "                executeStep(1, {\n" +
                "                    stepOptions: {\n" +
                "                        markAsSubjectIdentifierStep: 'true',\n" +
                "                        markAsSubjectAttributeStep: 'true'\n" +
                "                    },\n" +
                "                    authenticationOptions: filteredAuthenticationOptions\n" +
                "                }, {\n" +
                "                    onSuccess: function(context) {\n" +
                "                        // Trigger FIDO Authenticator for Passkey enrollment\n" +
                "                        executeStep(1, {\n" +
                "                            stepOptions: {\n" +
                "                                forceAuth: 'true'\n" +
                "                            },\n" +
                "                            authenticationOptions: [{\n" +
                "                                authenticator: 'FIDOAuthenticator'\n" +
                "                            }]\n" +
                "                        }, {});\n" +
                "                    },\n" +
                "                });\n" +
                "            }\n" +
                "        }\n" +
                "    });\n" +
                "};";

        AuthenticationSequence authSequence = new AuthenticationSequence();
        authSequence.setType(AuthenticationSequence.TypeEnum.USER_DEFINED);
        authSequence.addStepsItem(step);
        authSequence.setSubjectStepId(1);
        authSequence.setScript(script);

        application.setAuthenticationSequence(authSequence);

        return addApplication(application);
    }

    /**
     * Creates an OIDC application with authorization code grant configured for MFA using passkey.
     *
     * @param appName     Name of the application.
     * @param callbackUrl Redirect callback URL.
     * @return ID of the created application.
     * @throws Exception If an error occurred while creating the application.
     */
    protected String addOIDCAppWithMFAPasskey(String appName, String callbackUrl)
            throws Exception {

        ApplicationModel application = new ApplicationModel();
        application.setName(appName);

        OpenIDConnectConfiguration oidcConfig = new OpenIDConnectConfiguration();
        oidcConfig.setGrantTypes(Collections.singletonList("authorization_code"));
        oidcConfig.setCallbackURLs(Collections.singletonList(callbackUrl));

        InboundProtocols inboundProtocols = new InboundProtocols();
        inboundProtocols.setOidc(oidcConfig);
        application.setInboundProtocolConfiguration(inboundProtocols);

        Authenticator basicAuthenticator = new Authenticator();
        basicAuthenticator.setIdp("LOCAL");
        basicAuthenticator.setAuthenticator("BasicAuthenticator");

        AuthenticationStep firstStep = new AuthenticationStep();
        firstStep.setId(1);
        firstStep.addOptionsItem(basicAuthenticator);

        Authenticator passkeyAuthenticator = new Authenticator();
        passkeyAuthenticator.setIdp("LOCAL");
        passkeyAuthenticator.setAuthenticator("FIDOAuthenticator");

        AuthenticationStep secondStep = new AuthenticationStep();
        secondStep.setId(2);
        secondStep.addOptionsItem(passkeyAuthenticator);

        AuthenticationSequence authSequence = new AuthenticationSequence();
        authSequence.setType(AuthenticationSequence.TypeEnum.USER_DEFINED);
        authSequence.addStepsItem(firstStep);
        authSequence.addStepsItem(secondStep);
        authSequence.setSubjectStepId(1);

        application.setAuthenticationSequence(authSequence);

        return addApplication(application);
    }
}

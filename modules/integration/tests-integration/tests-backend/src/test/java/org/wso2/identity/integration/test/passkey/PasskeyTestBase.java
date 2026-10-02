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

import org.wso2.identity.integration.test.rest.api.server.application.management.v1.model.AdvancedApplicationConfiguration;
import org.wso2.identity.integration.test.rest.api.server.application.management.v1.model.ApplicationModel;
import org.wso2.identity.integration.test.rest.api.server.application.management.v1.model.AuthenticationSequence;
import org.wso2.identity.integration.test.rest.api.server.application.management.v1.model.AuthenticationStep;
import org.wso2.identity.integration.test.rest.api.server.application.management.v1.model.Authenticator;
import org.wso2.identity.integration.test.rest.api.server.application.management.v1.model.InboundProtocols;
import org.wso2.identity.integration.test.rest.api.server.application.management.v1.model.OpenIDConnectConfiguration;
import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import org.wso2.identity.integration.test.rest.api.server.identity.governance.v1.dto.ConnectorsPatchReq;
import org.wso2.identity.integration.test.rest.api.server.identity.governance.v1.dto.PropertyReq;
import org.wso2.identity.integration.test.rest.api.user.common.model.Email;
import org.wso2.identity.integration.test.rest.api.user.common.model.UserObject;
import org.wso2.identity.integration.test.restclients.IdentityGovernanceRestClient;
import org.wso2.identity.integration.test.restclients.SCIM2RestClient;
import org.apache.http.HttpResponse;
import org.apache.http.NameValuePair;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.conn.ssl.NoopHostnameVerifier;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClientBuilder;
import org.apache.http.message.BasicNameValuePair;
import org.apache.http.ssl.SSLContextBuilder;
import org.apache.http.util.EntityUtils;

import javax.net.ssl.SSLContext;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Base64;
import java.util.Collections;
import java.util.List;

/**
 * Base class for passkey integration tests.
 */
public abstract class PasskeyTestBase extends PasskeyClient  {

    private static final String PASSKEY_MFA_CATEGORY_ID = "TXVsdGkgRmFjdG9yIEF1dGhlbnRpY2F0b3Jz";
    private static final String PASSKEY_CONNECTOR_ID = "RklET0F1dGhlbnRpY2F0b3I";

    /**
     * Enables or disables username-less authentication for passkeys via the identity governance API.
     *
     * @param enable Whether to enable username-less authentication.
     * @throws IOException If an error occurred while updating the connector property.
     */
    protected void setUsernameLessAuthenticationEnabled(boolean enable) throws IOException {

        updatePasskeyConnectorProperty("FIDO.EnableUsernamelessAuthentication", String.valueOf(enable));
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

    /**
     * Returns whether username-less authentication for passkeys is currently enabled.
     *
     * @return true if enabled, false otherwise.
     * @throws Exception If an error occurred while retrieving the connector property.
     */
    protected boolean isUsernameLessAuthenticationEnabled() throws Exception {

        return Boolean.parseBoolean(getPasskeyConnectorProperty("FIDO.EnableUsernamelessAuthentication"));
    }

    /**
     * Returns whether passkey progressive enrollment is currently enabled.
     *
     * @return true if enabled, false otherwise.
     * @throws Exception If an error occurred while retrieving the connector property.
     */
    protected boolean isPasskeyProgressiveEnrollmentEnabled() throws Exception {

        return Boolean.parseBoolean(getPasskeyConnectorProperty("FIDO.EnablePasskeyProgressiveEnrollment"));
    }

    private String getPasskeyConnectorProperty(String name) throws Exception {

        IdentityGovernanceRestClient governanceClient =
                new IdentityGovernanceRestClient(serverURL, tenantInfo);
        try {
            JSONObject connector = governanceClient.getConnector(PASSKEY_MFA_CATEGORY_ID, PASSKEY_CONNECTOR_ID);
            JSONArray properties = (JSONArray) connector.get("properties");
            for (Object item : properties) {
                JSONObject property = (JSONObject) item;
                if (name.equals(property.get("name"))) {
                    return (String) property.get("value");
                }
            }
            throw new IllegalStateException("Connector property not found: " + name);
        } finally {
            governanceClient.closeHttpClient();
        }
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

    /**
     * Creates an OIDC application configured for app-native authentication with passkey as the
     * second factor (BasicAuthenticator → FIDOAuthenticator). The application is registered as a
     * public client with API-based authentication enabled.
     *
     * @param appName     Name of the application.
     * @param callbackUrl Redirect callback URL.
     * @return ID of the created application.
     * @throws Exception If an error occurred while creating the application.
     */
    protected String addOIDCAppWithMFAPasskeyForAppNative(String appName, String callbackUrl)
            throws Exception {

        ApplicationModel application = new ApplicationModel();
        application.setName(appName);

        OpenIDConnectConfiguration oidcConfig = new OpenIDConnectConfiguration();
        oidcConfig.setGrantTypes(Collections.singletonList("authorization_code"));
        oidcConfig.setCallbackURLs(Collections.singletonList(callbackUrl));
        oidcConfig.setPublicClient(true);

        InboundProtocols inboundProtocols = new InboundProtocols();
        inboundProtocols.setOidc(oidcConfig);
        application.setInboundProtocolConfiguration(inboundProtocols);

        AdvancedApplicationConfiguration advancedConfig = new AdvancedApplicationConfiguration();
        advancedConfig.setEnableAPIBasedAuthentication(true);
        application.setAdvancedConfigurations(advancedConfig);

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

    /**
     * Creates a test user via the SCIM2 REST API and returns the user ID.
     *
     * @param scim2RestClient SCIM2 client to use for the request.
     * @param username        Username for the new user.
     * @param password        Password for the new user.
     * @param email           Email address for the new user.
     * @return ID of the created user.
     * @throws Exception If an error occurred while creating the user.
     */
    protected String createTestUser(SCIM2RestClient scim2RestClient, String username, String password,
            String email) throws Exception {

        UserObject user = new UserObject();
        user.setUserName(username);
        user.setPassword(password);
        user.addEmail(new Email().value(email));
        return scim2RestClient.createUser(user);
    }

    /**
     * Deletes a test user via the SCIM2 REST API.
     *
     * @param scim2RestClient SCIM2 client to use for the request.
     * @param userId          ID of the user to delete.
     * @throws Exception If an error occurred while deleting the user.
     */
    protected void deleteTestUser(SCIM2RestClient scim2RestClient, String userId) throws Exception {

        scim2RestClient.deleteUser(userId);
    }

    /**
     * Enrolls a passkey for the given user via the My Account WebAuthn API, simulating
     * the full registration ceremony with a virtual authenticator.
     *
     * @param username     Username of the user.
     * @param password     Password of the user.
     * @param clientId     OAuth2 client ID of an application with password grant enabled.
     * @param clientSecret OAuth2 client secret of the application.
     * @throws Exception If passkey enrollment fails at any step.
     */
    protected void enrollPasskeyForUser(String username, String password, String clientId,
            String clientSecret) throws Exception {

        String tenantDomain = tenantInfo.getDomain();
        String origin = serverURL.replaceAll("/$", "");
        String tokenEndpoint = getTenantQualifiedURL(serverURL + "oauth2/token", tenantDomain);
        String startRegUrl = getTenantQualifiedURL(
                serverURL + "api/users/v2/me/webauthn/start-usernameless-registration", tenantDomain);
        String finishRegUrl = getTenantQualifiedURL(
                serverURL + "api/users/v2/me/webauthn/finish-registration", tenantDomain);

        String accessToken = getUserAccessTokenViaPasswordGrant(
                username, password, clientId, clientSecret, tokenEndpoint);

        JSONObject creationOptions = startWebAuthnRegistration(accessToken, startRegUrl, origin);

        String requestId = (String) creationOptions.get("requestId");
        JSONObject publicKeyOptions = (JSONObject) creationOptions.get("publicKeyCredentialCreationOptions");
        String challenge = (String) publicKeyOptions.get("challenge");
        String rpId = (String) ((JSONObject) publicKeyOptions.get("rp")).get("id");
        byte[] userHandle = base64UrlDecode((String) ((JSONObject) publicKeyOptions.get("user")).get("id"));

        String challengeResponse = registerPasskey(requestId, challenge, rpId, origin, username, userHandle);

        finishWebAuthnRegistration(accessToken, finishRegUrl, challengeResponse);
    }

    private String getUserAccessTokenViaPasswordGrant(String username, String password,
            String clientId, String clientSecret, String tokenEndpoint) throws Exception {

        try (CloseableHttpClient client = createTrustAllHttpClient()) {
            HttpPost post = new HttpPost(tokenEndpoint);
            String credentials = Base64.getEncoder()
                    .encodeToString((clientId + ":" + clientSecret).getBytes(StandardCharsets.UTF_8));
            post.setHeader("Authorization", "Basic " + credentials);

            List<NameValuePair> params = new ArrayList<>();
            params.add(new BasicNameValuePair("grant_type", "password"));
            params.add(new BasicNameValuePair("username", username));
            params.add(new BasicNameValuePair("password", password));
            params.add(new BasicNameValuePair("scope", "internal_login"));
            post.setEntity(new UrlEncodedFormEntity(params));

            HttpResponse response = client.execute(post);
            String body = EntityUtils.toString(response.getEntity());
            JSONObject json = (JSONObject) new JSONParser().parse(body);
            String accessToken = (String) json.get("access_token");
            if (accessToken == null) {
                throw new IllegalStateException("Failed to obtain access token for user '" + username + "': " + body);
            }
            return accessToken;
        }
    }

    private JSONObject startWebAuthnRegistration(String accessToken, String startRegUrl,
            String origin) throws Exception {

        try (CloseableHttpClient client = createTrustAllHttpClient()) {
            HttpPost post = new HttpPost(startRegUrl);
            post.setHeader("Authorization", "Bearer " + accessToken);

            List<NameValuePair> params = new ArrayList<>();
            params.add(new BasicNameValuePair("appId", origin));
            post.setEntity(new UrlEncodedFormEntity(params));

            HttpResponse response = client.execute(post);
            String body = EntityUtils.toString(response.getEntity());
            return (JSONObject) new JSONParser().parse(body);
        }
    }

    private void finishWebAuthnRegistration(String accessToken, String finishRegUrl,
            String challengeResponse) throws Exception {

        try (CloseableHttpClient client = createTrustAllHttpClient()) {
            HttpPost post = new HttpPost(finishRegUrl);
            post.setHeader("Authorization", "Bearer " + accessToken);
            post.setHeader("Content-Type", "application/json");
            post.setEntity(new StringEntity(challengeResponse, StandardCharsets.UTF_8));

            HttpResponse response = client.execute(post);
            int status = response.getStatusLine().getStatusCode();
            EntityUtils.consume(response.getEntity());
            if (status < 200 || status >= 300) {
                throw new IllegalStateException("Passkey finish-registration failed with HTTP " + status);
            }
        }
    }

    /**
     * Creates an OIDC application with authorization code grant configured with passkey as the sole
     * first-factor authenticator. Suitable for passwordless passkey-first login flows.
     *
     * @param appName     Name of the application.
     * @param callbackUrl Redirect callback URL.
     * @return ID of the created application.
     * @throws Exception If an error occurred while creating the application.
     */
    protected String addOIDCAppWithPasskeyAsFirstFactor(String appName, String callbackUrl)
            throws Exception {

        ApplicationModel application = new ApplicationModel();
        application.setName(appName);

        OpenIDConnectConfiguration oidcConfig = new OpenIDConnectConfiguration();
        oidcConfig.setGrantTypes(Collections.singletonList("authorization_code"));
        oidcConfig.setCallbackURLs(Collections.singletonList(callbackUrl));

        InboundProtocols inboundProtocols = new InboundProtocols();
        inboundProtocols.setOidc(oidcConfig);
        application.setInboundProtocolConfiguration(inboundProtocols);

        Authenticator passkeyAuthenticator = new Authenticator();
        passkeyAuthenticator.setIdp("LOCAL");
        passkeyAuthenticator.setAuthenticator("FIDOAuthenticator");

        AuthenticationStep step = new AuthenticationStep();
        step.setId(1);
        step.addOptionsItem(passkeyAuthenticator);

        AuthenticationSequence authSequence = new AuthenticationSequence();
        authSequence.setType(AuthenticationSequence.TypeEnum.USER_DEFINED);
        authSequence.addStepsItem(step);
        authSequence.setSubjectStepId(1);

        application.setAuthenticationSequence(authSequence);
        return addApplication(application);
    }

    /**
     * Creates an OIDC application configured for app-native authentication with passkey as the sole
     * first-factor authenticator. The application is registered as a public client with
     * API-based authentication enabled.
     *
     * @param appName     Name of the application.
     * @param callbackUrl Redirect callback URL.
     * @return ID of the created application.
     * @throws Exception If an error occurred while creating the application.
     */
    protected String addOIDCAppWithPasskeyForAppNative(String appName, String callbackUrl)
            throws Exception {

        ApplicationModel application = new ApplicationModel();
        application.setName(appName);

        OpenIDConnectConfiguration oidcConfig = new OpenIDConnectConfiguration();
        oidcConfig.setGrantTypes(Collections.singletonList("authorization_code"));
        oidcConfig.setCallbackURLs(Collections.singletonList(callbackUrl));
        oidcConfig.setPublicClient(true);

        InboundProtocols inboundProtocols = new InboundProtocols();
        inboundProtocols.setOidc(oidcConfig);
        application.setInboundProtocolConfiguration(inboundProtocols);

        AdvancedApplicationConfiguration advancedConfig = new AdvancedApplicationConfiguration();
        advancedConfig.setEnableAPIBasedAuthentication(true);
        application.setAdvancedConfigurations(advancedConfig);

        Authenticator passkeyAuthenticator = new Authenticator();
        passkeyAuthenticator.setIdp("LOCAL");
        passkeyAuthenticator.setAuthenticator("FIDOAuthenticator");

        AuthenticationStep step = new AuthenticationStep();
        step.setId(1);
        step.addOptionsItem(passkeyAuthenticator);

        AuthenticationSequence authSequence = new AuthenticationSequence();
        authSequence.setType(AuthenticationSequence.TypeEnum.USER_DEFINED);
        authSequence.addStepsItem(step);
        authSequence.setSubjectStepId(1);

        application.setAuthenticationSequence(authSequence);
        return addApplication(application);
    }

    /**
     * Creates a minimal OIDC application with both authorization_code and password grant types.
     * Intended for test helper use (e.g., obtaining user tokens for API calls).
     *
     * @param appName     Name of the application.
     * @param callbackUrl Redirect callback URL.
     * @return ID of the created application.
     * @throws Exception If an error occurred while creating the application.
     */
    protected String addPasswordGrantApp(String appName, String callbackUrl) throws Exception {

        ApplicationModel application = new ApplicationModel();
        application.setName(appName);

        OpenIDConnectConfiguration oidcConfig = new OpenIDConnectConfiguration();
        oidcConfig.setGrantTypes(List.of("authorization_code", "password"));
        oidcConfig.setCallbackURLs(Collections.singletonList(callbackUrl));

        InboundProtocols inboundProtocols = new InboundProtocols();
        inboundProtocols.setOidc(oidcConfig);
        application.setInboundProtocolConfiguration(inboundProtocols);

        return addApplication(application);
    }

    protected CloseableHttpClient createTrustAllHttpClient() throws Exception {

        SSLContext sslContext = SSLContextBuilder.create()
                .loadTrustMaterial(null, (chain, authType) -> true)
                .build();
        return HttpClientBuilder.create()
                .setSSLContext(sslContext)
                .setSSLHostnameVerifier(NoopHostnameVerifier.INSTANCE)
                .build();
    }
}

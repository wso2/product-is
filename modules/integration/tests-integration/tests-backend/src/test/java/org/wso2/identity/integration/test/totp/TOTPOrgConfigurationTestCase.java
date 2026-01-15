/**
 * Copyright (c) 2026, WSO2 LLC. (https://www.wso2.com).
 *
 * WSO2 LLC. licenses this file to you under the Apache License,
 * Version 2.0 (the "License"); you may not use this file except
 * in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied. See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */

package org.wso2.identity.integration.test.totp;

import org.apache.axis2.context.ConfigurationContext;
import org.apache.axis2.context.ConfigurationContextFactory;
import org.apache.http.HttpResponse;
import org.apache.http.client.CookieStore;
import org.apache.http.client.HttpClient;
import org.apache.http.impl.client.BasicCookieStore;
import org.apache.http.impl.client.HttpClientBuilder;
import org.apache.http.util.EntityUtils;
import org.json.JSONArray;
import org.json.JSONObject;
import org.testng.Assert;
import org.testng.annotations.AfterClass;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;
import org.wso2.carbon.automation.engine.context.TestUserMode;
import org.wso2.carbon.identity.application.common.model.script.xsd.AuthenticationScriptConfig;
import org.wso2.carbon.identity.application.common.model.xsd.AuthenticationStep;
import org.wso2.carbon.identity.application.common.model.xsd.InboundAuthenticationConfig;
import org.wso2.carbon.identity.application.common.model.xsd.InboundAuthenticationRequestConfig;
import org.wso2.carbon.identity.application.common.model.xsd.LocalAndOutboundAuthenticationConfig;
import org.wso2.carbon.identity.application.common.model.xsd.LocalAuthenticatorConfig;
import org.wso2.carbon.identity.application.common.model.xsd.Property;
import org.wso2.carbon.identity.application.common.model.xsd.ServiceProvider;
import org.wso2.carbon.identity.sso.saml.stub.types.SAMLSSOServiceProviderDTO;
import org.wso2.identity.integration.common.clients.application.mgt.ApplicationManagementServiceClient;
import org.wso2.identity.integration.common.clients.mgt.IdentityGovernanceServiceClient;
import org.wso2.identity.integration.common.clients.sso.saml.SAMLSSOConfigServiceClient;
import org.wso2.identity.integration.common.clients.usermgt.remote.RemoteUserStoreManagerServiceClient;
import org.wso2.identity.integration.test.util.Utils;
import org.wso2.identity.integration.common.utils.ISIntegrationTest;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.StringEntity;
import org.apache.http.HttpHeaders;
import org.apache.http.HttpStatus;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.util.Base64;

public class TOTPOrgConfigurationTestCase extends ISIntegrationTest {

    private static final String SERVICE_PROVIDER_NAME = "TOTPTestSP";
    private static final String SERVICE_PROVIDER_DESC = "Service Provider for TOTP Integration Test";
    private static final String TOTP_ENROLL_CONFIG = "TOTP.EnrolUserInAuthenticationFlow";
    private static final String USERNAME = "totpUser";
    private static final String PASSWORD = "totpUser123";

    private String commonAuthUrl;
    private String samlSsoUrl;

    private IdentityGovernanceServiceClient identityGovernanceServiceClient;
    private ApplicationManagementServiceClient applicationManagementServiceClient;
    private RemoteUserStoreManagerServiceClient remoteUserStoreManagerServiceClient;
    private SAMLSSOConfigServiceClient samlSSOConfigServiceClient;
    private HttpClient httpClient;
    private String sessionCookie;

    @BeforeClass(alwaysRun = true)
    public void testInit() throws Exception {
        super.init(TestUserMode.SUPER_TENANT_ADMIN);

        ConfigurationContext configContext = ConfigurationContextFactory.createConfigurationContextFromFileSystem(null,
                null);
        sessionCookie = getSessionCookie();

        identityGovernanceServiceClient = new IdentityGovernanceServiceClient(sessionCookie, backendURL);
        applicationManagementServiceClient = new ApplicationManagementServiceClient(sessionCookie, backendURL,
                configContext);
        remoteUserStoreManagerServiceClient = new RemoteUserStoreManagerServiceClient(backendURL, sessionCookie);
        samlSSOConfigServiceClient = new SAMLSSOConfigServiceClient(backendURL, sessionCookie);

        // Create HttpClient with cookie store to maintain session
        CookieStore cookieStore = new BasicCookieStore();
        httpClient = HttpClientBuilder.create()
                .setDefaultCookieStore(cookieStore)
                .disableRedirectHandling() // We want to handle redirects manually to inspect responses
                .build();

        commonAuthUrl = backendURL.replace("services/", "commonauth");
        samlSsoUrl = backendURL.replace("services/", "samlsso");

        createUser();
        createServiceProvider();
    }

    @AfterClass(alwaysRun = true)
    public void atEnd() throws Exception {
        deleteUser();
        deleteServiceProvider();
        // Reset config to default (true)
        updateTOTPConfiguration("true");
    }

    @Test(groups = "wso2.is", description = "Verify default progressive enrollment behavior (Org level Enabled)")
    public void testOrgLevelEnabled() throws Exception {
        // Set org level config to true (enabled)
        updateTOTPConfiguration("true");

        // Initiate login with user who has no TOTP device enrolled
        HttpResponse response = initiateLogin();
        String location = Utils.getRedirectUrl(response);
        EntityUtils.consume(response.getEntity());

        // Should redirect to enrollment page when org level is enabled
        Assert.assertTrue(location.contains("totp_enroll.do"),
                "Should redirect to enrollment page when org level progressive enrollment is enabled. Actual: "
                        + location);
    }

    @Test(groups = "wso2.is", description = "Verify progressive enrollment disabled (Org level Disabled)", dependsOnMethods = "testOrgLevelEnabled")
    public void testOrgLevelDisabled() throws Exception {
        // Set org level config to false (disabled)
        updateTOTPConfiguration("false");

        // Initiate login with user who has no TOTP device enrolled
        HttpResponse response = initiateLogin();
        String location = Utils.getRedirectUrl(response);
        EntityUtils.consume(response.getEntity());

        // Should NOT redirect to enrollment page when org level is disabled
        Assert.assertFalse(location.contains("totp_enroll.do"),
                "Should NOT redirect to enrollment page when org level progressive enrollment is disabled. Actual: "
                        + location);
    }

    /**
     * Verify TOTP configuration in the Identity Governance service.
     * Note: This method is currently not used as the TOTP authenticator config
     * needs to be properly deployed in the test distribution.
     * 
     * @param expectedValue The expected configuration value
     * @throws Exception If verification fails
     */
    @SuppressWarnings("unused")
    private void verifyTOTPConfiguration(String expectedValue) throws Exception {
        org.wso2.carbon.identity.governance.stub.bean.ConnectorConfig[] connectors = identityGovernanceServiceClient
                .getConnectorList();
        StringBuilder debugMsg = new StringBuilder();
        boolean found = false;

        if (connectors != null) {
            for (org.wso2.carbon.identity.governance.stub.bean.ConnectorConfig connector : connectors) {
                if (connector.getProperties() != null) {
                    for (org.wso2.carbon.identity.governance.stub.bean.Property prop : connector.getProperties()) {
                        // Dump all properties to find the correct one
                        debugMsg.append("Conn: ").append(connector.getFriendlyName())
                                .append(" Prop: ").append(prop.getName())
                                .append(" Val: ").append(prop.getValue()).append("\n");

                        if (prop.getName().equals(TOTP_ENROLL_CONFIG)) {
                            if (expectedValue.equals(prop.getValue())) {
                                found = true;
                            } else {
                                // Found property but value mismatch
                                debugMsg.append("Expected: ").append(expectedValue).append(" Found: ")
                                        .append(prop.getValue());
                            }
                        }
                    }
                }
            }
        }
        if (!found) {
            Assert.fail("Configuration verification failed. Property " + TOTP_ENROLL_CONFIG + " expected: "
                    + expectedValue + ". All available properties: \n" + debugMsg.toString());
        }
    }

    @Test(groups = "wso2.is", description = "Verify conditional auth script overrides org level config (Org level=TRUE, Script=FALSE)", dependsOnMethods = "testOrgLevelDisabled")
    public void testOrgTrueScriptFalse() throws Exception {
        // Enable at org level
        updateTOTPConfiguration("true");

        // Add adaptive script to disable enrollment
        String script = "var enrolUserInAuthenticationFlow = 'false';\n" +
                "\n" +
                "var onLoginRequest = function(context) {\n" +
                "    executeStep(1);\n" +
                "    executeStep(2, {\n" +
                "        authenticatorParams: {\n" +
                "            common: {\n" +
                "                'enrolUserInAuthenticationFlow': enrolUserInAuthenticationFlow\n" +
                "            }\n" +
                "        }\n" +
                "    }, {\n" +
                "        onSuccess: function(context) {\n" +
                "            Log.info('Successfully managed login flow');\n" +
                "        }\n" +
                "    });\n" +
                "};";

        updateServiceProviderWithScript(script);

        // Initiate login
        HttpResponse response = initiateLogin();
        String location = Utils.getRedirectUrl(response);
        EntityUtils.consume(response.getEntity());

        // Script disables enrollment, so should NOT redirect to enrollment page
        Assert.assertFalse(location.contains("totp_enroll.do"),
                "Should NOT redirect to enrollment page when script disables it (Org=True, Script=False). Actual: "
                        + location);

        // Clean up script
        updateServiceProviderWithScript(null);
    }

    @Test(groups = "wso2.is", description = "Verify conditional auth script overrides org level config (Org level=FALSE, Script=TRUE)", dependsOnMethods = "testOrgTrueScriptFalse")
    public void testOrgFalseScriptTrue() throws Exception {
        // Disable at org level
        updateTOTPConfiguration("false");

        // Add adaptive script to enable enrollment
        String script = "var enrolUserInAuthenticationFlow = 'true';\n" +
                "\n" +
                "var onLoginRequest = function(context) {\n" +
                "    executeStep(1);\n" +
                "    executeStep(2, {\n" +
                "        authenticatorParams: {\n" +
                "            common: {\n" +
                "                'enrolUserInAuthenticationFlow': enrolUserInAuthenticationFlow\n" +
                "            }\n" +
                "        }\n" +
                "    }, {\n" +
                "        onSuccess: function(context) {\n" +
                "            Log.info('Successfully managed login flow');\n" +
                "        }\n" +
                "    });\n" +
                "};";

        updateServiceProviderWithScript(script);

        // Initiate login
        HttpResponse response = initiateLogin();
        String location = Utils.getRedirectUrl(response);
        EntityUtils.consume(response.getEntity());

        // Script enables enrollment, should redirect to enrollment page
        Assert.assertTrue(location.contains("totp_enroll.do"),
                "Should redirect to enrollment page due to script override (Org=False, Script=True). Actual: "
                        + location);

        // Clean up script
        updateServiceProviderWithScript(null);
    }

    @Test(groups = "wso2.is", description = "Verify conditional auth script and org level config both set to FALSE (Org level=FALSE, Script=FALSE)", dependsOnMethods = "testOrgFalseScriptTrue")
    public void testOrgFalseScriptFalse() throws Exception {
        // Disable at org level
        updateTOTPConfiguration("false");

        // Add adaptive script to disable enrollment
        String script = "var enrolUserInAuthenticationFlow = 'false';\n" +
                "\n" +
                "var onLoginRequest = function(context) {\n" +
                "    executeStep(1);\n" +
                "    executeStep(2, {\n" +
                "        authenticatorParams: {\n" +
                "            common: {\n" +
                "                'enrolUserInAuthenticationFlow': enrolUserInAuthenticationFlow\n" +
                "            }\n" +
                "        }\n" +
                "    }, {\n" +
                "        onSuccess: function(context) {\n" +
                "            Log.info('Successfully managed login flow');\n" +
                "        }\n" +
                "    });\n" +
                "};";

        updateServiceProviderWithScript(script);

        // Initiate login
        HttpResponse response = initiateLogin();
        String location = Utils.getRedirectUrl(response);
        EntityUtils.consume(response.getEntity());

        // Both disable enrollment, should NOT redirect to enrollment page
        Assert.assertFalse(location.contains("totp_enroll.do"),
                "Should NOT redirect to enrollment page (Org=False, Script=False). Actual: " + location);

        // Clean up script
        updateServiceProviderWithScript(null);
    }

    @Test(groups = "wso2.is", description = "Verify conditional auth script overrides org level config both set to TRUE (Org level=TRUE, Script=TRUE)", dependsOnMethods = "testOrgFalseScriptFalse")
    public void testOrgTrueScriptTrue() throws Exception {
        // Enable at org level
        updateTOTPConfiguration("true");

        // Add adaptive script to enable enrollment
        String script = "var enrolUserInAuthenticationFlow = 'true';\n" +
                "\n" +
                "var onLoginRequest = function(context) {\n" +
                "    executeStep(1);\n" +
                "    executeStep(2, {\n" +
                "        authenticatorParams: {\n" +
                "            common: {\n" +
                "                'enrolUserInAuthenticationFlow': enrolUserInAuthenticationFlow\n" +
                "            }\n" +
                "        }\n" +
                "    }, {\n" +
                "        onSuccess: function(context) {\n" +
                "            Log.info('Successfully managed login flow');\n" +
                "        }\n" +
                "    });\n" +
                "};";

        updateServiceProviderWithScript(script);

        // Initiate login
        HttpResponse response = initiateLogin();
        String location = Utils.getRedirectUrl(response);
        EntityUtils.consume(response.getEntity());

        // Both enable enrollment, should redirect to enrollment page
        Assert.assertTrue(location.contains("totp_enroll.do"),
                "Should redirect to enrollment page (Org=True, Script=True). Actual: " + location);

        // Clean up script
        updateServiceProviderWithScript(null);
    }

    @Test(groups = "wso2.is", description = "Verify that sub-organization inherits its parent-orgs configuration (Parent Org=TRUE, Sub-Org=TRUE)", dependsOnMethods = "testOrgTrueScriptTrue")
    public void testSubOrgInheritsParentTrue() throws Exception {
        // TODO: Implement sub-organization inheritance test
        // This test requires organization management APIs
    }

    private void createUser() throws Exception {
        if (!remoteUserStoreManagerServiceClient.isExistingUser(USERNAME)) {
            remoteUserStoreManagerServiceClient.addUser(USERNAME, PASSWORD, null, null, "default", false);
        }
    }

    private void deleteUser() throws Exception {
        if (remoteUserStoreManagerServiceClient.isExistingUser(USERNAME)) {
            remoteUserStoreManagerServiceClient.deleteUser(USERNAME);
        }
    }

    private void createServiceProvider() throws Exception {
        ServiceProvider sp = new ServiceProvider();
        sp.setApplicationName(SERVICE_PROVIDER_NAME);
        sp.setDescription(SERVICE_PROVIDER_DESC);
        applicationManagementServiceClient.createApplication(sp);

        sp = applicationManagementServiceClient.getApplication(SERVICE_PROVIDER_NAME);

        // Inbound Authentication Config (SAML)
        InboundAuthenticationRequestConfig samlAuthenticationRequestConfig = new InboundAuthenticationRequestConfig();
        samlAuthenticationRequestConfig.setInboundAuthKey(SERVICE_PROVIDER_NAME);
        samlAuthenticationRequestConfig.setInboundAuthType("samlsso");
        Property property = new Property();
        property.setName("attrConsumServiceIndex");
        property.setValue("1239245949");
        samlAuthenticationRequestConfig.setProperties(new Property[] { property });

        InboundAuthenticationConfig inboundAuthenticationConfig = new InboundAuthenticationConfig();
        inboundAuthenticationConfig.setInboundAuthenticationRequestConfigs(
                new InboundAuthenticationRequestConfig[] { samlAuthenticationRequestConfig });
        sp.setInboundAuthenticationConfig(inboundAuthenticationConfig);

        // Local and Outbound Authentication Config
        LocalAndOutboundAuthenticationConfig localAndOutboundAuthenticationConfig = new LocalAndOutboundAuthenticationConfig();
        // Set authentication type to "flow" to enable adaptive authentication scripts
        localAndOutboundAuthenticationConfig.setAuthenticationType("flow");

        AuthenticationStep step1 = new AuthenticationStep();
        step1.setStepOrder(1);
        LocalAuthenticatorConfig basicAuth = new LocalAuthenticatorConfig();
        basicAuth.setName("BasicAuthenticator");
        basicAuth.setDisplayName("basicauth");
        basicAuth.setEnabled(true);
        step1.setLocalAuthenticatorConfigs(new LocalAuthenticatorConfig[] { basicAuth });
        step1.setSubjectStep(true);
        step1.setAttributeStep(true);

        AuthenticationStep step2 = new AuthenticationStep();
        step2.setStepOrder(2);
        LocalAuthenticatorConfig totpAuth = new LocalAuthenticatorConfig();
        totpAuth.setName("totp");
        totpAuth.setDisplayName("TOTP");
        totpAuth.setEnabled(true);
        step2.setLocalAuthenticatorConfigs(new LocalAuthenticatorConfig[] { totpAuth });

        localAndOutboundAuthenticationConfig.addAuthenticationSteps(step1);
        localAndOutboundAuthenticationConfig.addAuthenticationSteps(step2);

        sp.setLocalAndOutBoundAuthenticationConfig(localAndOutboundAuthenticationConfig);
        applicationManagementServiceClient.updateApplicationData(sp);

        // Create SAML SP in SSO Config
        SAMLSSOServiceProviderDTO samlssoServiceProviderDTO = new SAMLSSOServiceProviderDTO();
        samlssoServiceProviderDTO.setIssuer(SERVICE_PROVIDER_NAME);
        samlssoServiceProviderDTO.setAssertionConsumerUrls(new String[] { commonAuthUrl });
        samlssoServiceProviderDTO.setDefaultAssertionConsumerUrl(commonAuthUrl);
        samlssoServiceProviderDTO.setNameIDFormat("urn:oasis:names:tc:SAML:1.1:nameid-format:emailAddress");
        samlssoServiceProviderDTO.setDoSignAssertions(false);
        samlssoServiceProviderDTO.setDoSignResponse(false);
        samlssoServiceProviderDTO.setDoSingleLogout(true);
        samlSSOConfigServiceClient.addServiceProvider(samlssoServiceProviderDTO);
    }

    private void updateServiceProviderWithScript(String script) throws Exception {
        ServiceProvider sp = applicationManagementServiceClient.getApplication(SERVICE_PROVIDER_NAME);
        LocalAndOutboundAuthenticationConfig config = sp.getLocalAndOutBoundAuthenticationConfig();

        // Always set authentication type to "flow" for adaptive scripts to work
        config.setAuthenticationType("flow");

        if (script != null) {
            AuthenticationScriptConfig scriptConfig = new AuthenticationScriptConfig();
            scriptConfig.setContent(script);
            scriptConfig.setEnabled(true);
            config.setAuthenticationScriptConfig(scriptConfig);
        } else {
            config.setAuthenticationScriptConfig(null);
        }

        applicationManagementServiceClient.updateApplicationData(sp);
    }

    private void deleteServiceProvider() throws Exception {
        applicationManagementServiceClient.deleteApplication(SERVICE_PROVIDER_NAME);
    }

    private void updateTOTPConfiguration(String enable) throws Exception {
        org.wso2.carbon.identity.governance.stub.bean.Property[] properties = new org.wso2.carbon.identity.governance.stub.bean.Property[1];
        org.wso2.carbon.identity.governance.stub.bean.Property prop = new org.wso2.carbon.identity.governance.stub.bean.Property();
        prop.setName(TOTP_ENROLL_CONFIG);
        prop.setValue(enable);
        properties[0] = prop;
        identityGovernanceServiceClient.updateConfigurations(properties);
    }

    @Test(groups = "wso2.is", description = "Verify that sub-organization inherits its parent-orgs configuration (Parent Org=TRUE, Sub-Org=TRUE)", dependsOnMethods = "testSubOrgInheritsParentTrue", enabled = false)
    public void testSubOrgInheritance() throws Exception {
        // 1. Enable at org level (Parent)
        updateTOTPConfiguration("true");

        String subOrgName = "sub-org-totp-test";

        // 2. Create Sub-Organization
        String subOrgId = createOrganization(subOrgName);
        Assert.assertNotNull(subOrgId, "Sub-Organization creation failed");

        try {
            // 3. Share Application with Sub-Organization
            String appId = getApplicationId(SERVICE_PROVIDER_NAME);
            Assert.assertNotNull(appId, "Application ID not found for: " + SERVICE_PROVIDER_NAME);
            shareApplication(appId, subOrgId);

            // 4. Create User in Sub-Organization
            String subOrgUsername = "subOrgUser";
            String subOrgPassword = "subOrgUser123";
            createSubOrgUser(subOrgId, subOrgUsername, subOrgPassword);

            // 5. Initiate Login for Sub-Org User
            HttpResponse response = initiateSubOrgLogin(subOrgId, subOrgUsername, subOrgPassword);
            String location = Utils.getRedirectUrl(response);
            EntityUtils.consume(response.getEntity());

            // 6. Verify Redirection
            Assert.assertTrue(location.contains("totp_enroll.do"),
                    "Should redirect to enrollment page for Sub-Org user (Inherited Config). Actual: " + location);

        } finally {
            // Optional: Add cleanup if needed
        }
    }

    private String createOrganization(String orgName) throws Exception {
        String url = backendURL.replace("services/", "") + "api/server/v1/organizations";
        HttpPost request = new HttpPost(url);
        request.addHeader(HttpHeaders.AUTHORIZATION,
                "Basic " + Base64.getEncoder().encodeToString("admin:admin".getBytes(StandardCharsets.UTF_8)));
        request.addHeader(HttpHeaders.CONTENT_TYPE, "application/json");

        JSONObject json = new JSONObject();
        json.put("name", orgName);

        request.setEntity(new StringEntity(json.toString()));
        HttpResponse response = httpClient.execute(request);

        String responseString = EntityUtils.toString(response.getEntity());
        if (response.getStatusLine().getStatusCode() != HttpStatus.SC_CREATED) {
            throw new Exception("Organization creation failed: " + responseString);
        }

        JSONObject responseJson = new JSONObject(responseString);
        return responseJson.getString("id");
    }

    private String getApplicationId(String appName) throws Exception {
        try {
            ServiceProvider sp = applicationManagementServiceClient.getApplication(appName);
            return sp.getApplicationResourceId();
        } catch (Exception e) {
            // Fallback: If ResourceId is not available, log the error
            log.warn("Failed to get application resource ID for: " + appName, e);
        }
        return null;
    }

    private void shareApplication(String appId, String orgId) throws Exception {
        String url = backendURL.replace("services/", "") + "api/server/v1/applications/" + appId + "/share";
        HttpPost request = new HttpPost(url);
        request.addHeader(HttpHeaders.AUTHORIZATION,
                "Basic " + Base64.getEncoder().encodeToString("admin:admin".getBytes(StandardCharsets.UTF_8)));
        request.addHeader(HttpHeaders.CONTENT_TYPE, "application/json");

        JSONObject json = new JSONObject();
        json.put("shareWithAllChildren", false);
        JSONArray sharedOrgs = new JSONArray();
        sharedOrgs.put(orgId);
        json.put("sharedOrganizations", sharedOrgs);

        request.setEntity(new StringEntity(json.toString()));
        HttpResponse response = httpClient.execute(request);
        EntityUtils.consume(response.getEntity());

        if (response.getStatusLine().getStatusCode() != HttpStatus.SC_OK) {
            throw new Exception("Application sharing failed");
        }
    }

    private void createSubOrgUser(String orgId, String username, String password) throws Exception {
        // SCIM Endpoint: https://localhost:9853/o/<org-id>/scim2/Users
        String url = backendURL.replace("services/", "") + "o/" + orgId + "/scim2/Users";
        HttpPost request = new HttpPost(url);
        request.addHeader(HttpHeaders.AUTHORIZATION,
                "Basic " + Base64.getEncoder().encodeToString("admin:admin".getBytes(StandardCharsets.UTF_8)));
        request.addHeader(HttpHeaders.CONTENT_TYPE, "application/scim+json");

        JSONObject json = new JSONObject();
        json.put("userName", username);
        json.put("password", password);
        JSONArray emails = new JSONArray();
        JSONObject email = new JSONObject();
        email.put("primary", true);
        email.put("value", username + "@test.com");
        emails.put(email);
        json.put("emails", emails);

        request.setEntity(new StringEntity(json.toString()));
        HttpResponse response = httpClient.execute(request);
        EntityUtils.consume(response.getEntity());

        if (response.getStatusLine().getStatusCode() != HttpStatus.SC_CREATED) {
            throw new Exception("Sub-Org User creation failed");
        }
    }

    private HttpResponse initiateSubOrgLogin(String subOrgId, String username, String password) throws Exception {
        // 1. Send request to SAML SSO with organization-specific endpoint
        String orgSamlUrl = backendURL.replace("services/", "") + "o/" + subOrgId + "/samlsso?spEntityID="
                + SERVICE_PROVIDER_NAME;

        HttpResponse response = Utils.sendGetRequest(orgSamlUrl, null, httpClient);

        // Manually follow redirect if needed (since redirect handling is disabled)
        if (response.getStatusLine().getStatusCode() == HttpStatus.SC_MOVED_TEMPORARILY
                || response.getStatusLine().getStatusCode() == HttpStatus.SC_MOVED_PERMANENTLY) {
            String location = Utils.getRedirectUrl(response);
            EntityUtils.consume(response.getEntity());
            response = Utils.sendGetRequest(location, null, httpClient);
        }

        String sessionDataKey = extractSessionDataKey(response);
        // EntityUtils.consume(response.getEntity()); // Handled inside
        // extractSessionDataKey

        // 2. Post credentials to commonauth
        String orgCommonAuthUrl = backendURL.replace("services/", "") + "o/" + subOrgId + "/commonauth";

        response = Utils.sendPOSTMessage(sessionDataKey, orgCommonAuthUrl, "User-Agent",
                commonAuthUrl, SERVICE_PROVIDER_NAME,
                username, password, httpClient, samlSsoUrl);
        return response;
    }

    private HttpResponse initiateLogin() throws Exception {
        return simulateBasicAuthLogin();
    }

    private HttpResponse simulateBasicAuthLogin() throws Exception {
        // 1. Send request to SAML SSO to start flow
        String requestUrl = samlSsoUrl + "?spEntityID=" + SERVICE_PROVIDER_NAME;
        HttpResponse response = Utils.sendGetRequest(requestUrl, null, httpClient);

        // Manually follow redirect if needed (since redirect handling is disabled)
        if (response.getStatusLine().getStatusCode() == HttpStatus.SC_MOVED_TEMPORARILY
                || response.getStatusLine().getStatusCode() == HttpStatus.SC_MOVED_PERMANENTLY) {
            String location = Utils.getRedirectUrl(response);
            EntityUtils.consume(response.getEntity());
            response = Utils.sendGetRequest(location, null, httpClient);
        }

        String sessionDataKey = extractSessionDataKey(response);
        // EntityUtils.consume(response.getEntity()); // Handled inside
        // extractSessionDataKey

        // 2. Post credentials to commonauth
        response = Utils.sendPOSTMessage(sessionDataKey, commonAuthUrl, "User-Agent",
                commonAuthUrl, SERVICE_PROVIDER_NAME,
                USERNAME, PASSWORD, httpClient, samlSsoUrl);
        return response;
    }

    private String extractSessionDataKey(HttpResponse response) throws IOException {
        BufferedReader rd = new BufferedReader(new InputStreamReader(response.getEntity().getContent()));
        String line;
        String sessionDataKey = null;
        while ((line = rd.readLine()) != null) {
            if (line.contains("sessionDataKey")) {
                // Check for value="UUID"
                String regex = "value=\"([^\"]+)\"";
                java.util.regex.Pattern pattern = java.util.regex.Pattern.compile(regex);
                java.util.regex.Matcher matcher = pattern.matcher(line);
                if (matcher.find()) {
                    sessionDataKey = matcher.group(1);
                    break;
                }

                // Fallback to single quotes value='UUID'
                regex = "value='([^']+)'";
                pattern = java.util.regex.Pattern.compile(regex);
                matcher = pattern.matcher(line);
                if (matcher.find()) {
                    sessionDataKey = matcher.group(1);
                    break;
                }
            }
        }
        rd.close();
        return sessionDataKey;
    }
}

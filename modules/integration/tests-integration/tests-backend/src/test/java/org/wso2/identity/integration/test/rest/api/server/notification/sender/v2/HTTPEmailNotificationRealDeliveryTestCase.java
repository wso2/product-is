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

package org.wso2.identity.integration.test.rest.api.server.notification.sender.v2;

import com.google.gson.Gson;
import io.restassured.response.Response;
import org.apache.commons.lang3.StringUtils;
import org.apache.http.HttpStatus;
import org.testng.annotations.AfterClass;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.DataProvider;
import org.testng.annotations.Factory;
import org.testng.annotations.Test;
import org.wso2.carbon.automation.engine.context.TestUserMode;
import org.wso2.identity.integration.test.base.MockHTTPEmailProvider;
import org.wso2.identity.integration.test.base.MockOAuth2TokenServer;
import org.wso2.identity.integration.test.recovery.model.v2.InitModel;
import org.wso2.identity.integration.test.recovery.model.v2.RecoverModel;
import org.wso2.identity.integration.test.recovery.model.v2.UserClaim;
import org.wso2.identity.integration.test.rest.api.server.identity.governance.v1.dto.ConnectorsPatchReq;
import org.wso2.identity.integration.test.rest.api.server.identity.governance.v1.dto.PropertyReq;
import org.wso2.identity.integration.test.rest.api.server.notification.sender.v2.model.EmailSender;
import org.wso2.identity.integration.test.rest.api.server.notification.sender.v2.model.Properties;
import org.wso2.identity.integration.test.rest.api.server.notification.sender.v2.util.EmailSenderRequestBuilder;
import org.wso2.identity.integration.test.rest.api.user.common.model.Email;
import org.wso2.identity.integration.test.rest.api.user.common.model.Name;
import org.wso2.identity.integration.test.rest.api.user.common.model.UserObject;
import org.wso2.identity.integration.test.restclients.IdentityGovernanceRestClient;
import org.wso2.identity.integration.test.restclients.PasswordRecoveryV2RestClient;
import org.wso2.identity.integration.test.restclients.SCIM2RestClient;

import java.util.List;
import java.util.Map;

import static org.testng.Assert.assertEquals;
import static org.testng.Assert.assertNotNull;
import static org.testng.Assert.assertTrue;

/**
 * Real end-to-end delivery test for the HTTP-based (non-SMTP) Email notification sender, covering
 * CLIENT_CREDENTIAL and PASSWORD_CREDENTIAL authentication, and refresh-token reuse on a second send.
 */
public class HTTPEmailNotificationRealDeliveryTestCase extends EmailSenderTestBase {

    private static final String CATEGORY_ID = "QWNjb3VudCBNYW5hZ2VtZW50";
    private static final String CONNECTOR_ID = "YWNjb3VudC1yZWNvdmVyeQ";
    private static final String TEST_USERNAME = "httpEmailRealDeliveryUser";
    private static final String TEST_USER_PASSWORD = "Testuser@123";
    private static final String TEST_USER_EMAIL = "httpemailrealdeliveryuser@wso2.com";

    private final String senderAuthType;

    private MockHTTPEmailProvider mockHTTPEmailProvider;
    private MockOAuth2TokenServer mockOAuth2TokenServer;
    private IdentityGovernanceRestClient identityGovernanceRestClient;
    private SCIM2RestClient scim2RestClient;
    private PasswordRecoveryV2RestClient passwordRecoveryV2RestClient;

    private String userId;

    @Factory(dataProvider = "authTypeProvider")
    public HTTPEmailNotificationRealDeliveryTestCase(String senderAuthType) throws Exception {

        super.init(TestUserMode.TENANT_ADMIN);
        this.context = isServer;
        this.authenticatingUserName = context.getContextTenant().getTenantAdmin().getUserName();
        this.authenticatingCredential = context.getContextTenant().getTenantAdmin().getPassword();
        this.tenant = context.getContextTenant().getDomain();
        this.senderAuthType = senderAuthType;
    }

    @DataProvider(name = "authTypeProvider")
    public static Object[][] getAuthTypes() {

        return new Object[][]{
                {AUTH_TYPE_CLIENT_CREDENTIAL},
                {AUTH_TYPE_PASSWORD_CREDENTIAL},
        };
    }

    @BeforeClass(alwaysRun = true)
    public void init() throws Exception {

        super.testInit(API_VERSION, swaggerDefinition, tenant);

        mockHTTPEmailProvider = new MockHTTPEmailProvider();
        mockHTTPEmailProvider.start();

        mockOAuth2TokenServer = new MockOAuth2TokenServer();
        mockOAuth2TokenServer.start();

        identityGovernanceRestClient = new IdentityGovernanceRestClient(backendURL, tenantInfo);
        scim2RestClient = new SCIM2RestClient(serverURL, tenantInfo);
        passwordRecoveryV2RestClient = new PasswordRecoveryV2RestClient(serverURL, tenantInfo);

        updateNotificationPasswordRecoveryStatus(true);

        UserObject userInfo = new UserObject()
                .userName(TEST_USERNAME)
                .password(TEST_USER_PASSWORD)
                .name(new Name().givenName("HTTP").familyName("EmailUser"))
                .addEmail(new Email().value(TEST_USER_EMAIL));
        userId = scim2RestClient.createUser(userInfo);

        String body = new Gson().toJson(buildHTTPEmailSender(senderAuthType));
        Response response = getResponseOfPost(NOTIFICATION_SENDER_API_BASE_PATH + PATH_SEPARATOR + EMAIL_SENDERS_PATH,
                body);
        response.then()
                .log().ifValidationFails()
                .assertThat()
                .statusCode(HttpStatus.SC_CREATED);
    }

    @AfterClass(alwaysRun = true)
    public void atEnd() throws Exception {

        getResponseOfDelete(NOTIFICATION_SENDER_API_BASE_PATH + PATH_SEPARATOR + EMAIL_SENDERS_PATH +
                PATH_SEPARATOR + EMAIL_SENDER_NAME);

        scim2RestClient.deleteUser(userId);
        updateNotificationPasswordRecoveryStatus(false);

        identityGovernanceRestClient.closeHttpClient();
        scim2RestClient.closeHttpClient();
        passwordRecoveryV2RestClient.closeHttpClient();

        mockHTTPEmailProvider.stop();
        mockOAuth2TokenServer.clearData();
        mockOAuth2TokenServer.stop();

        super.testConclude();
    }

    @Test(groups = "wso2.is", description = "Test real password recovery email delivery via a custom HTTP " +
            "provider secured with an OAuth2 grant")
    public void testRealEmailDeliveryWithOAuth2() throws Exception {

        triggerPasswordRecoveryEmail();

        String accessToken = mockOAuth2TokenServer.getLastAccessToken();
        Map<String, String> requestParams = mockOAuth2TokenServer.getLastRequestBodyContent();

        assertNotNull(accessToken, "Access token should not be null");
        if (AUTH_TYPE_PASSWORD_CREDENTIAL.equals(senderAuthType)) {
            assertEquals(requestParams.get("grant_type"), "password");
        } else {
            assertEquals(requestParams.get("grant_type"), "client_credentials");
        }

        String authorizationHeader = mockHTTPEmailProvider.getHeader("Authorization");
        assertTrue(authorizationHeader != null && authorizationHeader.startsWith("Bearer " + accessToken),
                "Authorization header should contain the Bearer token minted for the configured auth type");
        assertNotNull(mockHTTPEmailProvider.getEmailBody(), "Email body should have been delivered");
    }

    @Test(groups = "wso2.is", description = "Test that a second email send reuses the cached refresh token " +
            "instead of re-authenticating from scratch", dependsOnMethods = "testRealEmailDeliveryWithOAuth2")
    public void testSecondEmailSendReusesRefreshToken() throws Exception {

        String firstAccessToken = mockOAuth2TokenServer.getLastAccessToken();

        triggerPasswordRecoveryEmail();

        Map<String, String> secondRequestParams = mockOAuth2TokenServer.getLastRequestBodyContent();
        assertEquals(secondRequestParams.get("grant_type"), "refresh_token",
                "Second email send should reuse the cached refresh token instead of re-authenticating from scratch");

        String secondAccessToken = mockOAuth2TokenServer.getLastAccessToken();
        assertNotNull(secondAccessToken, "Access token should not be null");
        assertTrue(!secondAccessToken.equals(firstAccessToken),
                "Second send should use a newly refreshed access token, not the original one");

        String authorizationHeader = mockHTTPEmailProvider.getHeader("Authorization");
        assertTrue(authorizationHeader != null && authorizationHeader.startsWith("Bearer " + secondAccessToken),
                "Second email send's Authorization header should carry the refreshed access token");
    }

    private void triggerPasswordRecoveryEmail() throws Exception {

        UserClaim usernameClaim = new UserClaim()
                .uri("http://wso2.org/claims/username")
                .value(TEST_USERNAME);
        InitModel initModel = new InitModel().claims(List.of(usernameClaim));

        RecoverModel recoverModel = passwordRecoveryV2RestClient.init(initModel, "EMAIL");
        assertNotNull(recoverModel, "Recovery model should not be null");
        assertTrue(StringUtils.isNotBlank(recoverModel.getChannelId()), "Channel ID should not be blank");
        assertTrue(StringUtils.isNotBlank(recoverModel.getRecoveryCode()), "Recovery code should not be blank");

        String flowConfirmationCode = passwordRecoveryV2RestClient.recover(recoverModel);
        assertTrue(StringUtils.isNotBlank(flowConfirmationCode), "Flow confirmation code should not be blank");
    }

    private EmailSender buildHTTPEmailSender(String authType) throws Exception {

        EmailSender emailSender = EmailSenderRequestBuilder.createAddHTTPEmailSenderJSON(authType, getClass());
        emailSender.setProviderURL(MockHTTPEmailProvider.EMAIL_SENDER_URL);

        for (Properties property : emailSender.getProperties()) {
            if ("tokenEndpoint".equals(property.getKey())) {
                property.setValue(MockOAuth2TokenServer.TOKEN_ENDPOINT_URL);
            }
        }

        return emailSender;
    }

    private void updateNotificationPasswordRecoveryStatus(boolean enable) throws Exception {

        ConnectorsPatchReq connectorsPatchReq = new ConnectorsPatchReq();
        connectorsPatchReq.setOperation(ConnectorsPatchReq.OperationEnum.UPDATE);

        PropertyReq enablePasswordRecovery = new PropertyReq()
                .name("Recovery.Notification.Password.Enable")
                .value(enable ? "true" : "false");
        PropertyReq enableEmailLinkRecovery = new PropertyReq()
                .name("Recovery.Notification.Password.emailLink.Enable")
                .value(enable ? "true" : "false");

        connectorsPatchReq.addProperties(enablePasswordRecovery);
        connectorsPatchReq.addProperties(enableEmailLinkRecovery);

        identityGovernanceRestClient.updateConnectors(CATEGORY_ID, CONNECTOR_ID, connectorsPatchReq);
    }
}

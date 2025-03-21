/*
 * Copyright (c) 2021, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
 *
 * WSO2 Inc. licenses this file to you under the Apache License,
 *  Version 2.0 (the "License"); you may not use this file except
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

package org.wso2.identity.integration.test.idp.mgt;

import org.apache.commons.lang.ArrayUtils;
import org.apache.http.HttpResponse;
import org.apache.http.client.HttpClient;
import org.apache.http.impl.client.HttpClientBuilder;
import org.testng.Assert;
import org.testng.annotations.AfterClass;
import org.testng.annotations.AfterMethod;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.DataProvider;
import org.testng.annotations.Factory;
import org.testng.annotations.Test;
import org.wso2.carbon.automation.engine.context.AutomationContext;
import org.wso2.carbon.automation.engine.context.TestUserMode;
import org.wso2.carbon.identity.application.common.model.idp.xsd.FederatedAuthenticatorConfig;
import org.wso2.carbon.identity.application.common.model.idp.xsd.IdentityProvider;
import org.wso2.carbon.identity.application.common.model.idp.xsd.IdentityProviderProperty;
import org.wso2.carbon.identity.oauth.stub.dto.OAuthConsumerAppDTO;
import org.wso2.identity.integration.common.clients.Idp.IdentityProviderMgtServiceClient;
import org.wso2.identity.integration.common.clients.oauth.OauthAdminClient;
import org.wso2.identity.integration.test.oauth2.OAuth2ServiceAbstractIntegrationTest;
import org.wso2.identity.integration.test.utils.DataExtractUtil;
import org.wso2.identity.integration.test.utils.OAuth2Constant;

import java.io.IOException;
import java.util.Map;

import static org.wso2.identity.integration.test.utils.CommonConstants.DEFAULT_TOMCAT_PORT;

/**
 * Tests Preference API integration with Login & Recovery Portals.
 */
public class PreferenceAPIIntegrationUITestCase extends OAuth2ServiceAbstractIntegrationTest {

    private static final String ENABLE_SELF_REGISTRATION_PROP_KEY = "SelfRegistration.Enable";
    private static final String ENABLE_USERNAME_RECOVERY_PROP_KEY = "Recovery.Notification.Username.Enable";
    private static final String ENABLE_USERNAME_EMAIL_RECOVERY_PROP_KEY = "Recovery.Notification.Username.Email.Enable";
    private static final String ENABLE_USERNAME_SMS_RECOVERY_PROP_KEY = "Recovery.Notification.Username.SMS.Enable";
    private static final String ENABLE_PASSWORD_QS_RECOVERY_PROP_KEY = "Recovery.Question.Password.Enable";
    private static final String ENABLE_PASSWORD_NOTIFICATION_RECOVERY_PROP_KEY =
            "Recovery.Notification.Password.Enable";
    private static final String ENABLE_PASSWORD_EMAIL_OTP_RECOVERY_PROP_KEY =
            "Recovery.Notification.Password.OTP.SendOTPInEmail";
    private static final String ENABLE_PASSWORD_EMAIL_LINK_RECOVERY_PROP_KEY =
            "Recovery.Notification.Password.emailLink.Enable";
    private static final String ENABLE_PASSWORD_SMS_OTP_RECOVERY_PROP_KEY =
            "Recovery.Notification.Password.smsOtp.Enable";
    private static final String CALLBACK_URL = "https://localhost/callback";
    private static final String RECOVERY_USERNAME_CONTENT = "id=\"usernameRecoverLink\"";
    private static final String RECOVERY_PASSWORD_CONTENT = "id=\"passwordRecoverLink\"";
    private static final String RECOVERY_ENDPOINT_QS_CONTENT = "name=\"recoveryOption\" value=\"SECURITY_QUESTIONS\"";
    private static final String RECOVERY_ENDPOINT_NOTIFICATION_EMAIL_CONTENT = "name=\"recoveryOption\" value=\"EMAIL\"";
    private static final String RECOVERY_ENDPOINT_NOTIFICATION_SMS_OTP_CONTENT = "name=\"recoveryOption\" value=\"SMSOTP\"";
    private static final String CREATE_ACCOUNT_CONTENT = "id=\"registerLink\"";
    private static final String RECOVERY_ENDPOINT_URL = "/accountrecoveryendpoint/recoveraccountrouter.do";

    private IdentityProvider superTenantResidentIDP;
    private IdentityProviderMgtServiceClient superTenantIDPMgtClient;
    private String recoveryEndpoint;
    private final String activeTenant;
    private final String OIDC_APP_NAME = "playground23";
    private String oidcAppClientId = "";

    @DataProvider(name = "configProvider")
    public static Object[][] configProvider() {

        return new Object[][]{{TestUserMode.SUPER_TENANT_ADMIN}, {TestUserMode.TENANT_ADMIN}};
    }

    @Factory(dataProvider = "configProvider")
    public PreferenceAPIIntegrationUITestCase(TestUserMode userMode) throws Exception {

        super.init(userMode);
        AutomationContext context = new AutomationContext("IDENTITY", userMode);
        this.activeTenant = context.getContextTenant().getDomain();
    }

    @BeforeClass(alwaysRun = true)
    public void testInit() throws Exception {

        super.init();
        superTenantIDPMgtClient = new IdentityProviderMgtServiceClient(sessionCookie, backendURL);
        superTenantResidentIDP = superTenantIDPMgtClient.getResidentIdP();
        adminClient = new OauthAdminClient(backendURL, sessionCookie);
        String isServerBackendUrl = isServer.getContextUrls().getWebAppURLHttps();
        recoveryEndpoint = getTenantQualifiedURL(isServerBackendUrl + RECOVERY_ENDPOINT_URL, tenantInfo.getDomain());
        createOIDCApplication();
    }

    private void createOIDCApplication() throws Exception {

        OAuthConsumerAppDTO appDTO = new OAuthConsumerAppDTO();
        appDTO.setApplicationName(OIDC_APP_NAME + activeTenant);
        appDTO.setCallbackUrl(OAuth2Constant.CALLBACK_URL);
        appDTO.setOAuthVersion(OAuth2Constant.OAUTH_VERSION_2);
        appDTO.setGrantTypes(OAuth2Constant.OAUTH2_GRANT_TYPE_AUTHORIZATION_CODE);
        appDTO.setBackChannelLogoutUrl("http://localhost:" + DEFAULT_TOMCAT_PORT + "/playground2/bclogout");

        adminClient.registerOAuthApplicationData(appDTO);
        OAuthConsumerAppDTO createdApp = adminClient.getOAuthAppByName(OIDC_APP_NAME + activeTenant);
        Assert.assertNotNull(createdApp, "Adding OIDC app failed.");
        oidcAppClientId = createdApp.getOauthConsumerKey();
    }

    @AfterClass(alwaysRun = true)
    public void atEnd() throws Exception {

        adminClient.removeOAuthApplicationData(oidcAppClientId);
    }

    @AfterMethod
    public void resetResidentIDP() throws Exception {

        updateResidentIDPProperties(superTenantResidentIDP, Map.of(
                ENABLE_SELF_REGISTRATION_PROP_KEY, "false",
                ENABLE_USERNAME_RECOVERY_PROP_KEY, "false",
                ENABLE_USERNAME_EMAIL_RECOVERY_PROP_KEY, "false",
                ENABLE_USERNAME_SMS_RECOVERY_PROP_KEY, "false",
                ENABLE_PASSWORD_QS_RECOVERY_PROP_KEY, "false",
                ENABLE_PASSWORD_NOTIFICATION_RECOVERY_PROP_KEY, "false",
                ENABLE_PASSWORD_EMAIL_LINK_RECOVERY_PROP_KEY, "false",
                ENABLE_PASSWORD_EMAIL_OTP_RECOVERY_PROP_KEY, "false",
                ENABLE_PASSWORD_SMS_OTP_RECOVERY_PROP_KEY, "false"));
    }

    @Test(groups = "wso2.is", description = "Check Initial Login Page")
    public void testInitialLoginPage() throws IOException {

        String content = sendAuthorizeRequest();
        Assert.assertFalse(content.contains(RECOVERY_USERNAME_CONTENT));
        Assert.assertFalse(content.contains(RECOVERY_PASSWORD_CONTENT));
        Assert.assertFalse(content.contains(CREATE_ACCOUNT_CONTENT));
    }

    @Test(groups = "wso2.is", description = "Check SelfRegistration Login Page")
    public void testSelfRegistration() throws Exception {

        updateResidentIDPProperty(superTenantResidentIDP, ENABLE_SELF_REGISTRATION_PROP_KEY, "true");
        String content = sendAuthorizeRequest();
        Assert.assertTrue(content.contains(CREATE_ACCOUNT_CONTENT));
    }

    @Test(groups = "wso2.is", description = "Check Username recovery Login Page")
    public void testUsernameRecovery() throws Exception {

        updateResidentIDPProperties(superTenantResidentIDP, Map.of(
                ENABLE_USERNAME_EMAIL_RECOVERY_PROP_KEY, "true",
                ENABLE_USERNAME_RECOVERY_PROP_KEY, "true"
               ));
        String content = sendAuthorizeRequest();
        Assert.assertTrue(content.contains(RECOVERY_USERNAME_CONTENT));
    }

    @Test(groups = "wso2.is", description = "Check QS Password recovery Login Page")
    public void testQSPasswordRecovery() throws Exception {

        updateResidentIDPProperty(superTenantResidentIDP, ENABLE_PASSWORD_QS_RECOVERY_PROP_KEY, "true");
        String content = sendAuthorizeRequest();
        Assert.assertTrue(content.contains(RECOVERY_PASSWORD_CONTENT));
    }

    @Test(groups = "wso2.is", description = "Check Notification Password recovery Login Page")
    public void testNotificationPasswordEmailLinkRecovery() throws Exception {

        updateResidentIDPProperties(superTenantResidentIDP, Map.of(
                ENABLE_PASSWORD_EMAIL_LINK_RECOVERY_PROP_KEY, "true",
                ENABLE_PASSWORD_NOTIFICATION_RECOVERY_PROP_KEY, "true"));
        String content = sendAuthorizeRequest();
        Assert.assertTrue(content.contains(RECOVERY_PASSWORD_CONTENT));
    }

    @Test(groups = "wso2.is", description = "Check Notification Password recovery Login Page")
    public void testNotificationPasswordSMSOTPRecovery() throws Exception {

        updateResidentIDPProperties(superTenantResidentIDP, Map.of(
                ENABLE_PASSWORD_SMS_OTP_RECOVERY_PROP_KEY, "true",
                ENABLE_PASSWORD_NOTIFICATION_RECOVERY_PROP_KEY, "true"));
        String content = sendAuthorizeRequest();
        Assert.assertTrue(content.contains(RECOVERY_PASSWORD_CONTENT));
    }

    @Test(groups = "wso2.is", description = "Check QS recovery option recovery Page")
    public void testRecoveryQSOnly() throws Exception {

        updateResidentIDPProperty(superTenantResidentIDP, ENABLE_PASSWORD_QS_RECOVERY_PROP_KEY, "true");
        String content = sendRecoveryRequest();
        Assert.assertTrue(content.contains(RECOVERY_ENDPOINT_QS_CONTENT));
        Assert.assertFalse(content.contains(RECOVERY_ENDPOINT_NOTIFICATION_EMAIL_CONTENT));
        Assert.assertFalse(content.contains(RECOVERY_ENDPOINT_NOTIFICATION_SMS_OTP_CONTENT));
    }

    @Test(groups = "wso2.is", description = "Check Notification recovery option recovery Page")
    public void testRecoveryNotificationEmailLinkOnly() throws Exception {

        updateResidentIDPProperties(superTenantResidentIDP, Map.of(
                ENABLE_PASSWORD_EMAIL_LINK_RECOVERY_PROP_KEY, "true",
                ENABLE_PASSWORD_NOTIFICATION_RECOVERY_PROP_KEY, "true"));
        String content = sendRecoveryRequest();
        Assert.assertFalse(content.contains(RECOVERY_ENDPOINT_QS_CONTENT));
        Assert.assertFalse(content.contains(RECOVERY_ENDPOINT_NOTIFICATION_SMS_OTP_CONTENT));
        Assert.assertTrue(content.contains(RECOVERY_ENDPOINT_NOTIFICATION_EMAIL_CONTENT));
    }

    @Test(groups = "wso2.is", description = "Check Notification recovery option recovery Page")
    public void testRecoveryNotificationEmailOTPOnly() throws Exception {

        updateResidentIDPProperties(superTenantResidentIDP, Map.of(
                ENABLE_PASSWORD_NOTIFICATION_RECOVERY_PROP_KEY, "true",
                ENABLE_PASSWORD_EMAIL_OTP_RECOVERY_PROP_KEY, "true"));
        String content = sendRecoveryRequest();
        Assert.assertFalse(content.contains(RECOVERY_ENDPOINT_QS_CONTENT));
        Assert.assertFalse(content.contains(RECOVERY_ENDPOINT_NOTIFICATION_SMS_OTP_CONTENT));
        Assert.assertTrue(content.contains(RECOVERY_ENDPOINT_NOTIFICATION_EMAIL_CONTENT));
    }

    @Test(groups = "wso2.is", description = "Check Notification recovery option recovery Page")
    public void testRecoveryNotificationSMSOTPOnly() throws Exception {


        updateResidentIDPProperties(superTenantResidentIDP, Map.of(
                ENABLE_PASSWORD_SMS_OTP_RECOVERY_PROP_KEY, "true",
                ENABLE_PASSWORD_NOTIFICATION_RECOVERY_PROP_KEY, "true"));
        String content = sendRecoveryRequest();
        Assert.assertFalse(content.contains(RECOVERY_ENDPOINT_QS_CONTENT));
        Assert.assertFalse(content.contains(RECOVERY_ENDPOINT_NOTIFICATION_EMAIL_CONTENT));
        Assert.assertTrue(content.contains(RECOVERY_ENDPOINT_NOTIFICATION_SMS_OTP_CONTENT));
    }

    private void updateResidentIDPProperty(IdentityProvider residentIdp, String propertyKey, String value) throws Exception {

        IdentityProviderProperty[] idpProperties = residentIdp.getIdpProperties();
        for (IdentityProviderProperty providerProperty : idpProperties) {
            if (propertyKey.equalsIgnoreCase(providerProperty.getName())) {
                providerProperty.setValue(value);
            }
        }
        updateResidentIDP(residentIdp);
    }

    private void updateResidentIDPProperties(IdentityProvider residentIdp, Map<String,String> properties)
            throws Exception {

        IdentityProviderProperty[] idpProperties = residentIdp.getIdpProperties();
        for (IdentityProviderProperty providerProperty : idpProperties) {
            if (properties.containsKey(providerProperty.getName())) {
                providerProperty.setValue(properties.get(providerProperty.getName()));
            }
        }
        updateResidentIDP(residentIdp);
    }

    private void updateResidentIDP(IdentityProvider residentIdentityProvider) throws Exception {

        FederatedAuthenticatorConfig[] federatedAuthenticatorConfigs =
                residentIdentityProvider.getFederatedAuthenticatorConfigs();
        for (FederatedAuthenticatorConfig authenticatorConfig : federatedAuthenticatorConfigs) {
            if (!authenticatorConfig.getName().equalsIgnoreCase("samlsso")) {
                federatedAuthenticatorConfigs = (FederatedAuthenticatorConfig[])
                        ArrayUtils.removeElement(federatedAuthenticatorConfigs,
                                authenticatorConfig);
            }
        }
        residentIdentityProvider.setFederatedAuthenticatorConfigs(federatedAuthenticatorConfigs);
        superTenantIDPMgtClient.updateResidentIdP(residentIdentityProvider);
    }

    private String getAuthzRequestUrl(String clientId, String callbackUrl) {

        return getTenantQualifiedURL(OAuth2Constant.AUTHORIZE_ENDPOINT_URL + "?" + "client_id=" + clientId + "&redirect_uri=" + callbackUrl +
                "&response_type=code&scope=openid", tenantInfo.getDomain());
    }

    private String sendAuthorizeRequest() throws IOException {

        HttpClient client = HttpClientBuilder.create().build();
        HttpResponse response = sendGetRequest(client, getAuthzRequestUrl(oidcAppClientId, OAuth2Constant.CALLBACK_URL));
        String content = DataExtractUtil.getContentData(response);
        Assert.assertNotNull(content);
        return content;
    }

    private String sendRecoveryRequest() throws IOException {

        HttpClient client = HttpClientBuilder.create().build();
        HttpResponse response = sendGetRequest(client, recoveryEndpoint);
        String content = DataExtractUtil.getContentData(response);
        Assert.assertNotNull(content);
        return content;
    }
}

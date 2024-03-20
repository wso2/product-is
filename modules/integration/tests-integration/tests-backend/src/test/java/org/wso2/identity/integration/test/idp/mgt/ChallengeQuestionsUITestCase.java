/*
 * Copyright (c) 2024, WSO2 LLC. (https://www.wso2.com).
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

package org.wso2.identity.integration.test.idp.mgt;

import org.apache.commons.lang.ArrayUtils;
import org.apache.http.HttpResponse;
import org.apache.http.client.HttpClient;
import org.apache.http.impl.client.HttpClientBuilder;
import org.testng.Assert;
import org.testng.annotations.*;
import org.wso2.carbon.automation.engine.context.AutomationContext;
import org.wso2.carbon.automation.engine.context.TestUserMode;
import org.wso2.carbon.identity.application.common.model.idp.xsd.FederatedAuthenticatorConfig;
import org.wso2.carbon.identity.application.common.model.idp.xsd.IdentityProvider;
import org.wso2.carbon.identity.application.common.model.idp.xsd.IdentityProviderProperty;
import org.wso2.carbon.identity.oauth.stub.dto.OAuthConsumerAppDTO;
import org.wso2.carbon.integration.common.utils.mgt.ServerConfigurationManager;
import org.wso2.identity.integration.common.clients.Idp.IdentityProviderMgtServiceClient;
import org.wso2.identity.integration.common.clients.oauth.OauthAdminClient;
import org.wso2.identity.integration.test.oauth2.OAuth2ServiceAbstractIntegrationTest;
import org.wso2.identity.integration.test.util.Utils;
import org.wso2.identity.integration.test.utils.DataExtractUtil;
import org.wso2.identity.integration.test.utils.OAuth2Constant;

import java.io.File;
import java.io.IOException;

import static org.wso2.identity.integration.test.utils.CommonConstants.DEFAULT_TOMCAT_PORT;

public class ChallengeQuestionsUITestCase extends OAuth2ServiceAbstractIntegrationTest {

    private static final String ADD_CHALLENGE_QUESTIONS_CONFIG = "challenge_questions_config.toml";
    private static final String RECOVERY_ENDPOINT_URL = "/accountrecoveryendpoint/recoveraccountrouter.do";
    private static final String RECOVERY_ENDPOINT_QS_CONTENT = "name=\"recoveryOption\" value=\"SECURITY_QUESTIONS\"";
    private static final String RECOVERY_ENDPOINT_NOTIFICATION_CONTENT = "name=\"recoveryOption\" value=\"EMAIL\"";
    private static final String ENABLE_PASSWORD_QS_RECOVERY_PROP_KEY = "Recovery.Question.Password.Enable";
    private static final String ENABLE_PASSWORD_NOTIFICATION_RECOVERY_PROP_KEY =
            "Recovery.Notification.Password.Enable";
    private static final String OIDC_APP_NAME = "playground2";
    private IdentityProvider superTenantResidentIDP;
    private ServerConfigurationManager serverConfigurationManager;
    private IdentityProviderMgtServiceClient superTenantIDPMgtClient;
    private String oidcAppClientId = "";
    private String activeTenant;
    private String recoveryEndpoint;

    @DataProvider(name = "configProvider")
    public static Object[][] configProvider() {

        return new Object[][]{{TestUserMode.SUPER_TENANT_ADMIN}, {TestUserMode.TENANT_ADMIN}};
    }

    @Factory(dataProvider = "configProvider")
    public ChallengeQuestionsUITestCase(TestUserMode userMode) throws Exception {
        super.init(userMode);
        AutomationContext context = new AutomationContext("IDENTITY", userMode);
        this.activeTenant = context.getContextTenant().getDomain();
    }

    @BeforeClass(alwaysRun = true)
    public void testInit() throws Exception {

        super.init();
        String carbonHome = Utils.getResidentCarbonHome();
        File defaultConfigFile = getDeploymentTomlFile(carbonHome);
        File challengeQuestionsConfigFile = new File(
                getISResourceLocation() + File.separator + "challenge-questions" + File.separator + ADD_CHALLENGE_QUESTIONS_CONFIG);
        serverConfigurationManager = new ServerConfigurationManager(isServer);
        serverConfigurationManager.applyConfigurationWithoutRestart(challengeQuestionsConfigFile, defaultConfigFile, true);
        serverConfigurationManager.restartGracefully();

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

    @Test(groups = "wso2.is", description = "Check Password recovery option recovery Page")
    public void testRecovery() throws Exception {

        updateResidentIDPProperty(superTenantResidentIDP, ENABLE_PASSWORD_NOTIFICATION_RECOVERY_PROP_KEY, "true");
        updateResidentIDPProperty(superTenantResidentIDP, ENABLE_PASSWORD_QS_RECOVERY_PROP_KEY, "true");
        String content = sendRecoveryRequest();
        Assert.assertTrue(content.contains(RECOVERY_ENDPOINT_QS_CONTENT));
        Assert.assertTrue(content.contains(RECOVERY_ENDPOINT_NOTIFICATION_CONTENT));
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

    private String sendRecoveryRequest() throws IOException {

        HttpClient client = HttpClientBuilder.create().build();
        HttpResponse response = sendGetRequest(client, recoveryEndpoint);
        String content = DataExtractUtil.getContentData(response);
        Assert.assertNotNull(content);
        return content;
    }

    @AfterClass(alwaysRun = true)
    public void atEnd() throws Exception {

        adminClient.removeOAuthApplicationData(oidcAppClientId);
        serverConfigurationManager.restoreToLastConfiguration(false);
    }
}

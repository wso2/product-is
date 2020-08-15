/*
 * Copyright (c) 2020, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
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

package org.wso2.identity.integration.test.oauth2;

import org.apache.http.HttpResponse;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.util.EntityUtils;
import org.testng.Assert;
import org.testng.annotations.AfterClass;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.DataProvider;
import org.testng.annotations.Factory;
import org.testng.annotations.Test;
import org.wso2.carbon.automation.engine.context.AutomationContext;
import org.wso2.carbon.automation.engine.context.TestUserMode;
import org.wso2.carbon.automation.engine.context.beans.ContextUrls;
import org.wso2.carbon.automation.engine.context.beans.Tenant;
import org.wso2.carbon.automation.engine.context.beans.User;
import org.wso2.carbon.identity.oauth.stub.dto.OAuthConsumerAppDTO;
import org.wso2.carbon.integration.common.admin.client.AuthenticatorClient;
import org.wso2.carbon.integration.common.utils.LoginLogoutClient;
import org.wso2.identity.integration.common.clients.application.mgt.ApplicationManagementServiceClient;
import org.wso2.identity.integration.common.clients.oauth.OauthAdminClient;
import org.wso2.identity.integration.common.clients.usermgt.remote.RemoteUserStoreManagerServiceClient;
import org.wso2.identity.integration.test.utils.OAuth2Constant;

import static org.wso2.identity.integration.test.utils.OAuth2Constant.USER_AGENT;

public class Oauth2OPIframeTestCase extends OAuth2ServiceAbstractIntegrationTest {

    public static final String CALL_BACK_URL =
            "https://localhost:9853/oidc/checksession?client_id=%s&redirect_uri=http" +
                    "://localhost:8888/playground2/oauth2client";
    private AuthenticatorClient logManger;
    private DefaultHttpClient client;
    private final String username;
    private final String userPassword;
    private final AutomationContext context;
    private String backendURL;
    private String sessionCookie;
    private Tenant tenantInfo;
    private User userInfo;
    private LoginLogoutClient loginLogoutClient;
    private ContextUrls identityContextUrls;
    private RemoteUserStoreManagerServiceClient remoteUSMServiceClient;

    @DataProvider(name = "configProvider")
    public static Object[][] configProvider() {
        return new Object[][]{{TestUserMode.SUPER_TENANT_ADMIN}, {TestUserMode.TENANT_ADMIN}};
    }

    @Factory(dataProvider = "configProvider")
    public Oauth2OPIframeTestCase(TestUserMode userMode) throws Exception {

        context = new AutomationContext("IDENTITY", userMode);
        this.username = context.getContextTenant().getTenantAdmin().getUserName();
        this.userPassword = context.getContextTenant().getTenantAdmin().getPassword();
    }

    @BeforeClass(alwaysRun = true)
    public void testInit() throws Exception {

        backendURL = context.getContextUrls().getBackEndUrl();
        loginLogoutClient = new LoginLogoutClient(context);
        logManger = new AuthenticatorClient(backendURL);
        sessionCookie = logManger.login(username, userPassword, context.getInstance().getHosts().get("default"));
        identityContextUrls = context.getContextUrls();
        tenantInfo = context.getContextTenant();
        userInfo = tenantInfo.getContextUser();
        appMgtclient = new ApplicationManagementServiceClient(sessionCookie, backendURL, null);
        adminClient = new OauthAdminClient(backendURL, sessionCookie);
        remoteUSMServiceClient = new RemoteUserStoreManagerServiceClient(backendURL, sessionCookie);

        setSystemproperties();
        client = new DefaultHttpClient();
    }

    @AfterClass(alwaysRun = true)
    public void atEnd() throws Exception {

        appMgtclient.deleteApplication(SERVICE_PROVIDER_NAME);
        adminClient.removeOAuthApplicationData(consumerKey);

        logManger = null;
        consumerKey = null;
    }

    @Test(groups = "wso2.is", description = "Check Oauth2 application registration")
    public void testOPIFrameRegex() throws Exception {

        OAuthConsumerAppDTO appConfigData = new OAuthConsumerAppDTO();
        appConfigData
                .setApplicationName(org.wso2.identity.integration.test.utils.OAuth2Constant.OAUTH_APPLICATION_NAME);
        appConfigData.setCallbackUrl(OAuth2Constant.CALLBACK_URL_REGEXP);
        appConfigData.setOAuthVersion(OAuth2Constant.OAUTH_VERSION_2);
        appConfigData.setGrantTypes("authorization_code implicit password client_credentials refresh_token "
                + "urn:ietf:params:oauth:grant-type:saml2-bearer iwa:ntlm");

        OAuthConsumerAppDTO appDto = createApplication(appConfigData);
        Assert.assertNotNull(appDto, "Application creation failed.");

        consumerKey = appDto.getOauthConsumerKey();
        Assert.assertNotNull(consumerKey, "Application creation failed.");
        consumerSecret = appDto.getOauthConsumerSecret();

        HttpResponse response;

        String url = String.format(CALL_BACK_URL, consumerKey);
        response = sendGetRequest(url);
        EntityUtils.consume(response.getEntity());

    }

    private HttpResponse sendGetRequest(String url) throws Exception {

        HttpGet request = new HttpGet(url);
        request.addHeader("User-Agent", USER_AGENT);
        return client.execute(request);
    }
}

/*
 * Copyright (c) 2024, WSO2 LLC. (http://www.wso2.com).
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

package org.wso2.identity.integration.test.recovery;

import com.icegreen.greenmail.util.GreenMailUtil;
import jakarta.mail.Message;
import org.apache.http.Header;
import org.apache.http.HttpResponse;
import org.apache.http.NameValuePair;
import org.apache.http.client.CookieStore;
import org.apache.http.client.HttpClient;
import org.apache.http.client.config.CookieSpecs;
import org.apache.http.client.config.RequestConfig;
import org.apache.http.config.Lookup;
import org.apache.http.config.RegistryBuilder;
import org.apache.http.cookie.CookieSpecProvider;
import org.apache.http.impl.client.BasicCookieStore;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClientBuilder;
import org.apache.http.impl.cookie.RFC6265CookieSpecProvider;
import org.apache.http.message.BasicNameValuePair;
import org.apache.http.util.EntityUtils;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.testng.Assert;
import org.testng.annotations.AfterClass;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;
import org.wso2.identity.integration.test.base.MockApplicationServer;
import org.wso2.identity.integration.test.oidc.OIDCAbstractIntegrationTest;
import org.wso2.identity.integration.test.oidc.OIDCUtilTest;
import org.wso2.identity.integration.test.oidc.bean.OIDCApplication;
import org.wso2.identity.integration.test.rest.api.server.identity.governance.v1.dto.ConnectorsPatchReq;
import org.wso2.identity.integration.test.rest.api.server.identity.governance.v1.dto.PropertyReq;
import org.wso2.identity.integration.test.rest.api.user.common.model.Email;
import org.wso2.identity.integration.test.rest.api.user.common.model.Name;
import org.wso2.identity.integration.test.rest.api.user.common.model.UserObject;
import org.wso2.identity.integration.test.restclients.IdentityGovernanceRestClient;
import org.wso2.identity.integration.test.util.Utils;
import org.wso2.identity.integration.test.utils.OAuth2Constant;

import java.io.IOException;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;

import static org.wso2.identity.integration.test.utils.OAuth2Constant.AUTHORIZE_ENDPOINT_URL;

/**
 * Test password recovery functionality.
 */
public class PasswordRecoveryTestCase extends OIDCAbstractIntegrationTest {

    private IdentityGovernanceRestClient identityGovernanceRestClient;

    private final CookieStore cookieStore = new BasicCookieStore();

    private CloseableHttpClient client;
    private OIDCApplication oidcApplication;
    private UserObject userObject;
    private MockApplicationServer mockApplicationServer;

    public static final String USERNAME = "recoverytestuser";
    public static final String PASSWORD = "Oidcsessiontestuser@123";
    public static final String PASSWORD_NEW = "Oidcsessiontestuser@1234";

    @BeforeClass(alwaysRun = true)
    public void testInit() throws Exception {

        Utils.getMailServer().purgeEmailFromAllMailboxes();
        super.init();

        Lookup<CookieSpecProvider> cookieSpecRegistry = RegistryBuilder.<CookieSpecProvider>create()
                .register(CookieSpecs.DEFAULT, new RFC6265CookieSpecProvider())
                .build();
        RequestConfig requestConfig = RequestConfig.custom()
                .setCookieSpec(CookieSpecs.DEFAULT)
                .build();
        client = HttpClientBuilder.create()
                .setDefaultRequestConfig(requestConfig)
                .setDefaultCookieSpecRegistry(cookieSpecRegistry)
                .setDefaultCookieStore(cookieStore)
                .build();

        identityGovernanceRestClient = new IdentityGovernanceRestClient(serverURL, tenantInfo);
        updatePasswordRecoveryFeatureStatus(true);

        oidcApplication = initApplication();
        createApplication(oidcApplication);

        userObject = initUser();
        createUser(userObject);

        mockApplicationServer = new MockApplicationServer();
        mockApplicationServer.start();
    }

    @AfterClass(alwaysRun = true)
    public void testClear() throws Exception {

        updatePasswordRecoveryFeatureStatus(false);
        deleteApplication(oidcApplication);
        deleteUser(userObject);
        identityGovernanceRestClient.closeHttpClient();
        client.close();
        Utils.getMailServer().purgeEmailFromAllMailboxes();
        mockApplicationServer.stop();
    }

    @Test
    public void testPasswordRecovery() throws Exception {
        String passwordRecoveryFormURL = retrievePasswordResetURL(oidcApplication, client);
        submitPasswordRecoveryForm(passwordRecoveryFormURL, userObject.getUserName(), client);

        String recoveryLink = getRecoveryURLFromEmail();
        HttpResponse postResponse = resetPassword(recoveryLink);
        Assert.assertEquals(postResponse.getStatusLine().getStatusCode(), 200);
        Assert.assertTrue(EntityUtils.toString(postResponse.getEntity()).contains("Password Reset Successfully"));
    }

    private String retrievePasswordResetURL(OIDCApplication application, HttpClient client) throws Exception {

        List<NameValuePair> urlParameters = new ArrayList<>();
        urlParameters.add(new BasicNameValuePair("response_type", OAuth2Constant.OAUTH2_GRANT_TYPE_CODE));
        urlParameters.add(new BasicNameValuePair("client_id", application.getClientId()));
        urlParameters.add(new BasicNameValuePair("redirect_uri", application.getCallBackURL()));
        urlParameters.add(new BasicNameValuePair("scope", "openid email profile"));
        HttpResponse response = sendPostRequestWithParameters(client, urlParameters,
                getTenantQualifiedURL(AUTHORIZE_ENDPOINT_URL, tenantInfo.getDomain()));

        Header authorizeRequestURL = response.getFirstHeader(OAuth2Constant.HTTP_RESPONSE_HEADER_LOCATION);
        EntityUtils.consume(response.getEntity());

        response = sendGetRequest(client, authorizeRequestURL.getValue());
        String htmlContent = EntityUtils.toString(response.getEntity());
        Document doc = Jsoup.parse(htmlContent);
        Element link = doc.selectFirst("#passwordRecoverLink");
        Assert.assertNotNull(link, "Password recovery link not found in the response.");
        return link.attr("href");
    }

    private void submitPasswordRecoveryForm(String url, String username, HttpClient client) throws Exception {

        HttpResponse response = sendGetRequest(client, url);
        String htmlContent = EntityUtils.toString(response.getEntity());
        Document doc = Jsoup.parse(htmlContent);

        Element form = doc.selectFirst("form");
        String baseURL = url.substring(0, url.lastIndexOf('/') + 1);
        Assert.assertNotNull(form, "Password recovery form not found in the response.");
        String actionURL = new URL(new URL(baseURL), form.attr("action")).toString();

        List<NameValuePair> formParams = new ArrayList<>();
        for (Element input : form.select("input")) {
            String name = input.attr("name");
            String value = input.attr("value");
            if ("username".equals(name)) {
                value = username;
            }
            if ("usernameUserInput".equals(name)) {
                value = username;
            }
            formParams.add(new BasicNameValuePair(name, value));
        }

        HttpResponse postResponse = sendPostRequestWithParameters(client, formParams, actionURL);
        if (postResponse.getStatusLine().getStatusCode() != 200) {
            throw new Exception("Error occurred while submitting the password reset form.");
        }
        EntityUtils.consume(postResponse.getEntity());
    }

    private void updatePasswordRecoveryFeatureStatus(boolean enable) throws IOException {

        ConnectorsPatchReq connectorsPatchReq = new ConnectorsPatchReq();
        connectorsPatchReq.setOperation(ConnectorsPatchReq.OperationEnum.UPDATE);
        PropertyReq propertyReq = new PropertyReq();
        propertyReq.setName("Recovery.Notification.Password.emailLink.Enable");
        propertyReq.setValue(enable ? "true" : "false");
        connectorsPatchReq.addProperties(propertyReq);
        identityGovernanceRestClient.updateConnectors("QWNjb3VudCBNYW5hZ2VtZW50", "YWNjb3VudC1yZWNvdmVyeQ", connectorsPatchReq);
    }

    private OIDCApplication initApplication() {

        OIDCApplication playgroundApp = new OIDCApplication(OIDCUtilTest.playgroundAppOneAppName,
                OIDCUtilTest.playgroundAppOneAppCallBackUri);
        playgroundApp.addRequiredClaim(OIDCUtilTest.emailClaimUri);
        playgroundApp.addRequiredClaim(OIDCUtilTest.firstNameClaimUri);
        playgroundApp.addRequiredClaim(OIDCUtilTest.lastNameClaimUri);
        return playgroundApp;
    }

    protected UserObject initUser() {

        UserObject user = new UserObject();
        user.setUserName(USERNAME);
        user.setPassword(PASSWORD);
        user.setName(new Name().givenName(OIDCUtilTest.firstName).familyName(OIDCUtilTest.lastName));
        user.addEmail(new Email().value(OIDCUtilTest.email));
        return user;
    }

    private HttpResponse resetPassword(String recoveryLink) throws IOException {

        HttpResponse response = sendGetRequest(client, recoveryLink);

        String htmlContent = EntityUtils.toString(response.getEntity());
        Document doc = Jsoup.parse(htmlContent);

        Element form = doc.selectFirst("form");
        String baseURL = recoveryLink.substring(0, recoveryLink.lastIndexOf('/') + 1);
        Assert.assertNotNull(form, "Password reset form not found in the response.");
        String actionURL = new URL(new URL(baseURL), form.attr("action")).toString();

        List<NameValuePair> formParams = new ArrayList<>();
        for (Element input : form.select("input")) {
            String name = input.attr("name");
            String value = input.attr("value");
            if ("reset-password".equals(name) || "reset-password2".equals(name)) {
                value = PASSWORD_NEW;
            }
            formParams.add(new BasicNameValuePair(name, value));
        }

        return sendPostRequestWithParameters(client, formParams, actionURL);
    }

    private String getRecoveryURLFromEmail() {

        Assert.assertTrue(Utils.getMailServer().waitForIncomingEmail(10000, 1));
        Message[] messages = Utils.getMailServer().getReceivedMessages();
        String body = GreenMailUtil.getBody(messages[0]).replaceAll("=\r?\n", "");
        Document doc = Jsoup.parse(body);

        return doc.selectFirst("#bodyCell").selectFirst("a").attr("href");
    }
}

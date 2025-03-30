/*
 * Copyright (c) 2025, WSO2 LLC. (http://www.wso2.com).
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

import com.icegreen.greenmail.store.FolderException;
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
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.DataProvider;
import org.testng.annotations.Test;
import org.wso2.carbon.automation.test.utils.dbutils.H2DataBaseManager;
import org.wso2.carbon.integration.common.utils.exceptions.AutomationUtilException;
import org.wso2.carbon.integration.common.utils.mgt.ServerConfigurationManager;
import org.wso2.identity.integration.test.base.MockApplicationServer;
import org.wso2.identity.integration.test.base.MockSMSProvider;
import org.wso2.identity.integration.test.oidc.OIDCAbstractIntegrationTest;
import org.wso2.identity.integration.test.oidc.OIDCUtilTest;
import org.wso2.identity.integration.test.oidc.bean.OIDCApplication;
import org.wso2.identity.integration.test.rest.api.server.identity.governance.v1.dto.ConnectorsPatchReq;
import org.wso2.identity.integration.test.rest.api.server.identity.governance.v1.dto.PropertyReq;
import org.wso2.identity.integration.test.rest.api.server.notification.sender.v1.model.Properties;
import org.wso2.identity.integration.test.rest.api.server.notification.sender.v1.model.SMSSender;
import org.wso2.identity.integration.test.rest.api.server.user.store.v1.model.UserStoreReq;
import org.wso2.identity.integration.test.rest.api.user.common.model.Email;
import org.wso2.identity.integration.test.rest.api.user.common.model.Name;
import org.wso2.identity.integration.test.rest.api.user.common.model.PhoneNumbers;
import org.wso2.identity.integration.test.rest.api.user.common.model.UserObject;
import org.wso2.identity.integration.test.restclients.IdentityGovernanceRestClient;
import org.wso2.identity.integration.test.restclients.NotificationSenderRestClient;
import org.wso2.identity.integration.test.restclients.UserStoreMgtRestClient;
import org.wso2.identity.integration.test.util.Utils;
import org.wso2.identity.integration.test.utils.OAuth2Constant;

import java.io.File;
import java.io.IOException;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

import javax.xml.xpath.XPathExpressionException;

import static org.wso2.identity.integration.test.utils.OAuth2Constant.AUTHORIZE_ENDPOINT_URL;

/**
 * This class contains the test cases for the Username Recovery.
 */
public class UsernameRecoveryTestCase extends OIDCAbstractIntegrationTest {

    // Constants.
    private static final String SMS_SENDER_REQUEST_FORMAT = "{\"content\": {{body}}, \"to\": {{mobile}} }";
    private static final String USERNAME = "usernameRecoveryTestUser1";
    private static final String USER_MOBILE = "+94674898234";
    private static final String USER_PASSWORD = "Sample1$";
    private static final String MOBILE = "mobile";
    private static final String USERNAME_RECOVERY_EMAIL_ENABLE = "Recovery.Notification.Username.Email.Enable";
    private static final String USERNAME_RECOVERY_SMS_ENABLE = "Recovery.Notification.Username.SMS.Enable";
    private static final String CATEGORY_ID = "QWNjb3VudCBNYW5hZ2VtZW50";
    private static final String CONNECTOR_ID = "YWNjb3VudC1yZWNvdmVyeQ";
    private static final String NON_UNIQUE_USER_ENABLE_TOML = "non_unique_user_enable.toml";
    private static final String NON_UNIQUE_USER_DISABLE_TOML = "non_unique_user_disable.toml";
    private static final String USER_STORE_DB_NAME = "SECONDARY_USER_STORE_DB";
    private static final String DB_USER_NAME = "wso2automation";
    private static final String USER_STORE_TYPE = "VW5pcXVlSURKREJDVXNlclN0b3JlTWFuYWdlcg";
    private static final String DB_USER_PASSWORD = "wso2automation";
    private static final String SECONDARY_DOMAIN_ID = "WSO2TEST.COM";
    private static final String PRIMARY_DOMAIN_ID = "PRIMARY";
    private static final String FIRST_NAME = "urTestFirst";
    private static final String LAST_NAME = "urTestLast";

    private final CookieStore cookieStore = new BasicCookieStore();
    private IdentityGovernanceRestClient identityGovernanceRestClient;
    private CloseableHttpClient client;
    private OIDCApplication oidcApplication;
    private MockApplicationServer mockApplicationServer;
    private MockSMSProvider mockSMSProvider;
    private UserStoreMgtRestClient userStoreMgtRestClient;
    private String userStoreId;
    private NotificationSenderRestClient notificationSenderRestClient;
    private ServerConfigurationManager serverConfigurationManager;

    @BeforeClass(alwaysRun = true)
    public void init() throws Exception {

        super.init();
        changeISConfiguration(false);
        super.init();

        mockSMSProvider = new MockSMSProvider();
        mockSMSProvider.start();

        // Adding a secondary user store.
        userStoreMgtRestClient = new UserStoreMgtRestClient(serverURL, tenantInfo);
        addSecondaryJDBCUserStore();

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

        // Adding custom sms sender.
        notificationSenderRestClient = new NotificationSenderRestClient(
                serverURL, tenantInfo);
        SMSSender smsSender = initSMSSender();
        notificationSenderRestClient.createSMSProvider(smsSender);

        oidcApplication = initApplication();
        createApplication(oidcApplication);

        mockApplicationServer = new MockApplicationServer();
        mockApplicationServer.start();
    }

    @BeforeMethod(alwaysRun = true)
    public void testInit() throws FolderException {

        Utils.getMailServer().purgeEmailFromAllMailboxes();
        mockSMSProvider.clearSmsContent();
    }

    @AfterClass(alwaysRun = true)
    public void testClear() throws Exception {

        updateUsernameRecoveryFeature(ChannelType.SMS, false);
        updateUsernameRecoveryFeature(ChannelType.EMAIL, false);
        deleteApplication(oidcApplication);
        Utils.getMailServer().purgeEmailFromAllMailboxes();
        mockSMSProvider.stop();
        mockApplicationServer.stop();
        userStoreMgtRestClient.deleteUserStore(userStoreId);
        notificationSenderRestClient.deleteSMSProvider();
        client.close();
        scim2RestClient.closeHttpClient();
        restClient.closeHttpClient();
        super.clear();
        serverConfigurationManager.restoreToLastConfiguration(false);
    }

    @Test(dataProvider = "userProvider")
    public void testUsernameRecoveryForEmailOnly(UserObject user) throws Exception {

        updateUsernameRecoveryFeature(ChannelType.EMAIL, true);
        updateUsernameRecoveryFeature(ChannelType.SMS, false);

        // Create the user.
        createUser(user);

        // Perform the username recovery.
        String usernameRecoveryLink = retrieveUsernameRecoveryLink(oidcApplication, client);
        submitUsernameRecoveryForm(usernameRecoveryLink, user, client);
        String recoveredUsername = getUsernameFromEmail();

        Assert.assertEquals(recoveredUsername, user.getUserName(), "Received username does not match.");

        // Clearing the user.
        deleteUser(user);
    }

    @Test(dataProvider = "userProvider")
    public void testUsernameRecoveryForSmsOnly(UserObject user) throws Exception {

        // Disabling the email channel & enabling sms channel.
        updateUsernameRecoveryFeature(ChannelType.EMAIL, false);
        updateUsernameRecoveryFeature(ChannelType.SMS, true);

        // Create the user.
        createUser(user);

        // Perform the username recovery.
        String usernameRecoveryLink = retrieveUsernameRecoveryLink(oidcApplication, client);
        submitUsernameRecoveryForm(usernameRecoveryLink, user, client);
        String recoveredUsername = getUsernameFromSms();

        Assert.assertEquals(user.getUserName(), recoveredUsername, "Received username does not match.");

        // Clearing the user.
        deleteUser(user);
    }

    @Test(dataProvider = "userProviderWithChannel")
    public void testUsernameRecoveryWithBothChannels(UserObject user, ChannelType channelType) throws Exception {

        // Enabling the both username recovery channels.
        updateUsernameRecoveryFeature(ChannelType.EMAIL, true);
        updateUsernameRecoveryFeature(ChannelType.SMS, true);

        // Create the user.
        createUser(user);

        // Perform the username recovery.
        String usernameRecoveryLink = retrieveUsernameRecoveryLink(oidcApplication, client);
        String baseURL = usernameRecoveryLink.substring(0, usernameRecoveryLink.lastIndexOf('/') + 1);
        String usernameFormSubmissionResponseContent = submitUsernameRecoveryForm(usernameRecoveryLink, user, client);

        String recoveredUsername;
        if (channelType == ChannelType.EMAIL) {
            selectPreferredChannel(ChannelType.EMAIL, usernameFormSubmissionResponseContent, baseURL);
            recoveredUsername = getUsernameFromEmail();
        } else {
            selectPreferredChannel(ChannelType.SMS, usernameFormSubmissionResponseContent, baseURL);
            recoveredUsername = getUsernameFromSms();
        }
        Assert.assertEquals(user.getUserName(), recoveredUsername, "Received username does not match.");

        // Delete the user.
        deleteUser(user);
    }

    @Test
    public void testUsernameRecoveryWithMultipleUsersSupport() throws Exception {

        // Restarting the server with non-unique user recovery support config.
        changeISConfiguration(true);
        super.init();

        // Create two users with same attributes in two user stores.
        List<UserObject> users = new ArrayList<>();
        UserObject user1 = initUser(PRIMARY_DOMAIN_ID, "usernameRecoveryUser1");
        createUser(user1);
        String user1UserId = userId;
        users.add(user1);

        UserObject user2 = initUser(SECONDARY_DOMAIN_ID, "UsernameRecoveryUser2");
        createUser(user2);
        String user2UserId = userId;
        users.add(user2);

        // Enabling only email channel.
        updateUsernameRecoveryFeature(ChannelType.EMAIL, true);
        updateUsernameRecoveryFeature(ChannelType.SMS, false);

        // Perform the username recovery.
        String usernameRecoveryLink = retrieveUsernameRecoveryLink(oidcApplication, client);
        submitUsernameRecoveryForm(usernameRecoveryLink, user1, client);
        validateUsernameForMultipleUserInMail(users);

        // Deleting the users.
        deleteUser(user1UserId);
        deleteUser(user2UserId);
    }

    private void deleteUser(String userId) throws IOException {

        scim2RestClient.deleteUser(userId);
    }

    private UserObject initUser(String domain, String username) {

        UserObject user = new UserObject();
        user.setUserName(domain + "/" + username);
        user.setPassword(USER_PASSWORD);
        user.setName(new Name().givenName(FIRST_NAME).familyName(LAST_NAME));
        user.addEmail(new Email().value(OIDCUtilTest.email));
        user.addPhoneNumbers(new PhoneNumbers().value(USER_MOBILE).type(MOBILE));

        return user;
    }

    private void addSecondaryJDBCUserStore() throws Exception {

        // Creating database.
        H2DataBaseManager dbmanager = new H2DataBaseManager("jdbc:h2:" + ServerConfigurationManager.getCarbonHome()
                + "/repository/database/" + USER_STORE_DB_NAME, DB_USER_NAME, DB_USER_PASSWORD);
        dbmanager.executeUpdate(new File(ServerConfigurationManager.getCarbonHome() + "/dbscripts/h2.sql"));
        dbmanager.disconnect();

        // Register a secondary user store.
        UserStoreReq userStore = new UserStoreReq()
                .typeId(USER_STORE_TYPE)
                .name(SECONDARY_DOMAIN_ID)
                .addPropertiesItem(new UserStoreReq.Property()
                        .name("driverName")
                        .value("org.h2.Driver"))
                .addPropertiesItem(new UserStoreReq.Property()
                        .name("url")
                        .value("jdbc:h2:./repository/database/" + USER_STORE_DB_NAME))
                .addPropertiesItem(new UserStoreReq.Property()
                        .name("userName")
                        .value(DB_USER_NAME))
                .addPropertiesItem(new UserStoreReq.Property()
                        .name("password")
                        .value(DB_USER_PASSWORD))
                .addPropertiesItem(new UserStoreReq.Property()
                        .name("PasswordJavaRegEx")
                        .value("^[\\S]{5,30}$"))
                .addPropertiesItem(new UserStoreReq.Property()
                        .name("UsernameJavaRegEx")
                        .value("^[\\S]{5,30}$"))
                .addPropertiesItem(new UserStoreReq.Property()
                        .name("Disabled")
                        .value("false"))
                .addPropertiesItem(new UserStoreReq.Property()
                        .name("PasswordDigest")
                        .value("SHA-256"))
                .addPropertiesItem(new UserStoreReq.Property()
                        .name("StoreSaltedPassword")
                        .value("true"))
                .addPropertiesItem(new UserStoreReq.Property()
                        .name("SCIMEnabled")
                        .value("true"))
                .addPropertiesItem(new UserStoreReq.Property()
                        .name("CountRetrieverClass")
                        .value("org.wso2.carbon.identity.user.store.count.jdbc.JDBCUserStoreCountRetriever"))
                .addPropertiesItem(new UserStoreReq.Property()
                        .name("UserIDEnabled")
                        .value("true"))
                .addPropertiesItem(new UserStoreReq.Property()
                        .name("GroupIDEnabled")
                        .value("true"));

        userStoreId = userStoreMgtRestClient.addUserStore(userStore);
        Thread.sleep(5000);
        boolean isSecondaryUserStoreDeployed = userStoreMgtRestClient.waitForUserStoreDeployment(SECONDARY_DOMAIN_ID);
        Assert.assertTrue(isSecondaryUserStoreDeployed);
    }

    private void updateUsernameRecoveryFeature(ChannelType channelType, boolean enable) throws IOException {

        ConnectorsPatchReq connectorsPatchReq = new ConnectorsPatchReq();
        connectorsPatchReq.setOperation(ConnectorsPatchReq.OperationEnum.UPDATE);
        PropertyReq propertyReq = new PropertyReq();
        if (channelType == ChannelType.SMS) {
            propertyReq.setName(USERNAME_RECOVERY_SMS_ENABLE);
        } else {
            propertyReq.setName(USERNAME_RECOVERY_EMAIL_ENABLE);
        }
        propertyReq.setValue(enable ? "true" : "false");
        connectorsPatchReq.addProperties(propertyReq);
        identityGovernanceRestClient.updateConnectors(CATEGORY_ID, CONNECTOR_ID, connectorsPatchReq);
    }

    @DataProvider(name = "userProvider")
    private Object[][] userProvider() {

        // Primary user store user.
        UserObject userObject1 = initUser(PRIMARY_DOMAIN_ID, USERNAME);

        // Secondary user store user.
        UserObject userObject2 = initUser(SECONDARY_DOMAIN_ID, USERNAME);

        return new Object[][]{{userObject1}, {userObject2}};
    }

    @DataProvider(name = "userProviderWithChannel")
    private Object[][] userProviderWithChannel() {

        // Primary user store user.
        UserObject userObject1 = initUser(PRIMARY_DOMAIN_ID, USERNAME);

        // Secondary user store user.
        UserObject userObject2 = initUser(SECONDARY_DOMAIN_ID, USERNAME);

        return new Object[][]{
                {userObject1, ChannelType.EMAIL},
                {userObject1, ChannelType.SMS},
                {userObject2, ChannelType.EMAIL},
                {userObject2, ChannelType.SMS}
        };
    }

    private OIDCApplication initApplication() {

        return new OIDCApplication(OIDCUtilTest.playgroundAppOneAppName, OIDCUtilTest.playgroundAppOneAppCallBackUri);
    }

    private String retrieveUsernameRecoveryLink(OIDCApplication application, HttpClient client) throws IOException {

        // Getting the authorized request url.
        List<NameValuePair> urlParameters = new ArrayList<>();
        urlParameters.add(new BasicNameValuePair("response_type", OAuth2Constant.OAUTH2_GRANT_TYPE_CODE));
        urlParameters.add(new BasicNameValuePair("client_id", application.getClientId()));
        urlParameters.add(new BasicNameValuePair("redirect_uri", application.getCallBackURL()));
        urlParameters.add(new BasicNameValuePair("scope", "openid email profile"));
        HttpResponse response = sendPostRequestWithParameters(client, urlParameters,
                getTenantQualifiedURL(AUTHORIZE_ENDPOINT_URL, tenantInfo.getDomain()));

        Header authorizeRequestURL = response.getFirstHeader(OAuth2Constant.HTTP_RESPONSE_HEADER_LOCATION);
        EntityUtils.consume(response.getEntity());

        // Getting the login page.
        response = sendGetRequest(client, authorizeRequestURL.getValue());
        String htmlContent = EntityUtils.toString(response.getEntity());
        Document doc = Jsoup.parse(htmlContent);

        // Extracting the username recovery link from the login page.
        Element link = doc.selectFirst("#usernameRecoverLink");
        Assert.assertNotNull(link, "Username recovery link not found in the response.");
        return link.attr("href");
    }

    private void selectPreferredChannel(ChannelType channelType, String formResponseContent, String baseUrl)
            throws Exception {

        Document doc = Jsoup.parse(formResponseContent);
        Element form = doc.selectFirst("form");
        Assert.assertNotNull(form, "Username recovery form not found in the response.");
        String actionURL = new URL(new URL(baseUrl), form.attr("action")).toString();

        // Updating the values in the username recovery channel selection page.
        List<NameValuePair> formParams = new ArrayList<>();
        for (Element input : form.select("input")) {
            String name = input.attr("name");
            String value = input.attr("value");

            if ("usernameRecoveryOption".equals(name)) {
                if (channelType == ChannelType.EMAIL) {
                    value = "1:EMAIL";
                } else {
                    value = "2:SMS";
                }
            }
            formParams.add(new BasicNameValuePair(name, value));
        }

        // Submit the form.
        HttpResponse postResponse = sendPostRequestWithParameters(client, formParams, actionURL);
        if (postResponse.getStatusLine().getStatusCode() != 200) {
            throw new Exception("Error occurred while submitting the username recovery channel selection form.");
        }

        EntityUtils.consume(postResponse.getEntity());
    }

    private String submitUsernameRecoveryForm(String url, UserObject user, HttpClient client) throws Exception {

        HttpResponse response = sendGetRequest(client, url);
        String htmlContent = EntityUtils.toString(response.getEntity());
        Document doc = Jsoup.parse(htmlContent);

        Element form = doc.selectFirst("form");
        String baseURL = url.substring(0, url.lastIndexOf('/') + 1);
        Assert.assertNotNull(form, "Username recovery form not found in the response.");
        String actionURL = new URL(new URL(baseURL), form.attr("action")).toString();

        // Updating the values in the username recovery page.
        List<NameValuePair> formParams = new ArrayList<>();
        for (Element input : form.select("input")) {
            String name = input.attr("name");
            String value = input.attr("value");

            if ("http://wso2.org/claims/givenname".equals(name)) {
                value = user.getName().getGivenName();
            } else if ("http://wso2.org/claims/lastname".equals(name)) {
                value = user.getName().getFamilyName();
            } else if ("contact".equals(name)) {
                value = user.getEmails().get(0).getValue();
            }

            formParams.add(new BasicNameValuePair(name, value));
        }

        // Submit the form.
        HttpResponse postResponse = sendPostRequestWithParameters(client, formParams, actionURL);
        String postResponseContent = EntityUtils.toString(postResponse.getEntity());
        if (postResponse.getStatusLine().getStatusCode() != 200) {
            throw new Exception("Error occurred while submitting the username recovery form.");
        }

        EntityUtils.consume(postResponse.getEntity());

        return postResponseContent;
    }

    private String getUsernameFromEmail() {

        Assert.assertTrue(Utils.getMailServer().waitForIncomingEmail(10000, 1));
        Message[] messages = Utils.getMailServer().getReceivedMessages();
        String body = GreenMailUtil.getBody(messages[0]).replaceAll("=\r?\n", "");
        Document doc = Jsoup.parse(body);

        return doc.select("b").get(1).text();
    }

    private void validateUsernameForMultipleUserInMail(List<UserObject> users) {

        Assert.assertTrue(Utils.getMailServer().waitForIncomingEmail(10000L * users.size(), users.size()));
        Message[] messages = Utils.getMailServer().getReceivedMessages();

        // Extract expected usernames from the users list.
        List<String> expectedUsernames = users.stream()
                .map(UserObject::getUserName)
                .collect(Collectors.toList());

        // Extract actual usernames from the received messages.
        List<String> actualUsernames = new ArrayList<>();
        for (Message message : messages) {
            String body = GreenMailUtil.getBody(message).replaceAll("=\r?\n", "");
            Document doc = Jsoup.parse(body);
            String username = doc.select("b").get(1).text();
            actualUsernames.add(username);
        }

        // Verify that all expected usernames are present in the received emails.
        Assert.assertTrue(actualUsernames.containsAll(expectedUsernames),
                "Not all expected usernames were found in the emails.");
        Assert.assertEquals(actualUsernames.size(), expectedUsernames.size(),
                "Number of received usernames does not match expected count.");

    }

    private String getUsernameFromSms() throws InterruptedException {

        Thread.sleep(5000);
        String body = mockSMSProvider.getSmsContent();

        if (body != null) {
            body = body.trim();
        }

        // Regex to match "domain/user1" format.
        String regex = "([a-zA-Z0-9.-]+/[^@]+)";

        java.util.regex.Pattern pattern = java.util.regex.Pattern.compile(regex);
        java.util.regex.Matcher matcher = pattern.matcher(body);

        if (matcher.find()) {
            return matcher.group(1).trim();
        }

        return null;
    }

    private void changeISConfiguration(boolean nonUniqueUserSupport)
            throws IOException, XPathExpressionException, AutomationUtilException {

        String carbonHome = Utils.getResidentCarbonHome();
        File defaultTomlFile = getDeploymentTomlFile(carbonHome);
        File targetTomlFile;
        if (nonUniqueUserSupport) {
            targetTomlFile = new File(getISResourceLocation() + File.separator + "recovery" +
                    File.separator + NON_UNIQUE_USER_ENABLE_TOML);
        } else {
            targetTomlFile = new File(getISResourceLocation() + File.separator + "recovery" +
                    File.separator + NON_UNIQUE_USER_DISABLE_TOML);
        }

        serverConfigurationManager = new ServerConfigurationManager(isServer);
        serverConfigurationManager.applyConfiguration(targetTomlFile, defaultTomlFile, true, true);
    }

    public enum ChannelType {
        SMS,
        EMAIL
    }

    private static SMSSender initSMSSender() {

        SMSSender smsSender = new SMSSender();
        smsSender.setProvider(MockSMSProvider.SMS_SENDER_PROVIDER_TYPE);
        smsSender.setProviderURL(MockSMSProvider.SMS_SENDER_URL);
        smsSender.contentType(SMSSender.ContentTypeEnum.JSON);
        ArrayList<Properties> properties = new ArrayList<>();
        properties.add(new Properties().key("body").value(SMS_SENDER_REQUEST_FORMAT));
        smsSender.setProperties(properties);
        return smsSender;
    }

}

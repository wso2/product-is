/*
 * Copyright (c) 2016-2025, WSO2 LLC. (http://www.wso2.com).
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
package org.wso2.identity.integration.test.identity.governance;

import com.icegreen.greenmail.util.GreenMailUtil;
import jakarta.mail.Message;
import org.apache.commons.lang.StringUtils;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.testng.annotations.AfterClass;
import org.testng.annotations.AfterMethod;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;
import org.wso2.carbon.identity.governance.stub.bean.Property;
import org.wso2.carbon.identity.user.profile.stub.UserProfileMgtServiceUserProfileExceptionException;
import org.wso2.carbon.identity.user.profile.stub.types.UserFieldDTO;
import org.wso2.carbon.identity.user.profile.stub.types.UserProfileDTO;
import org.wso2.carbon.um.ws.api.stub.ClaimValue;
import org.wso2.carbon.um.ws.api.stub.RemoteUserStoreManagerServiceUserStoreExceptionException;
import org.wso2.carbon.user.api.UserStoreException;
import org.wso2.identity.integration.common.clients.UserManagementClient;
import org.wso2.identity.integration.common.clients.UserProfileMgtServiceClient;
import org.wso2.identity.integration.common.clients.mgt.IdentityGovernanceServiceClient;
import org.wso2.identity.integration.common.clients.usermgt.remote.RemoteUserStoreManagerServiceClient;
import org.wso2.identity.integration.common.utils.ISIntegrationTest;
import org.wso2.identity.integration.test.base.MockSMSProvider;
import org.wso2.identity.integration.test.rest.api.server.notification.sender.v1.model.Properties;
import org.wso2.identity.integration.test.rest.api.server.notification.sender.v1.model.SMSSender;
import org.wso2.identity.integration.test.restclients.NotificationSenderRestClient;
import org.wso2.identity.integration.test.util.Utils;

import java.rmi.RemoteException;
import java.util.ArrayList;

import static org.testng.Assert.assertTrue;

public class AdminForcedPasswordResetTestCase extends ISIntegrationTest {

    public static final String LOCATION = "Location";
    public static final String SESSION_DATA_KEY = "sessionDataKey";
    public static final String ENABLE_ADMIN_PASSWORD_RESET_WITH_SMS_OTP = "Recovery.AdminPasswordReset.SMSOTP";
    private static final String PROFILE_NAME = "default";
    private static final String TEST_USER_USERNAME = "testUser";
    private static final String TEST_USER_PASSWORD = "Abcd@123";
    private static final String TEST_USER_EMAIL = "test@test.com";
    private static final String TEST_USER_MOBILE = "+94674898234";
    private static final String TRUE_STRING = "true";
    private static final String FALSE_STRING = "false";
    private static final String SMS_SENDER_REQUEST_FORMAT = "{\"content\": {{body}}, \"to\": {{mobile}} }";
    private static final String ENABLE_ADMIN_PASSWORD_RESET_OFFLINE = "Recovery.AdminPasswordReset.Offline";
    private static final String ENABLE_ADMIN_PASSWORD_RESET_WITH_EMAIL_OTP = "Recovery.AdminPasswordReset.OTP";
    private static final String ENABLE_ADMIN_PASSWORD_RESET_WITH_EMAIL_LINK =
            "Recovery.AdminPasswordReset.RecoveryLink";
    private static final String ADMIN_FORCED_PASSWORD_RESET_CLAIM =
            "http://wso2.org/claims/identity/adminForcedPasswordReset";
    private static final String OTP_CLAIM = "http://wso2.org/claims/oneTimePassword";
    private static final String EMAIL_CLAIM = "http://wso2.org/claims/emailaddress";
    private static final String MOBILE_CLAIM = "http://wso2.org/claims/mobile";
    private static final String ACCOUNT_LOCKED_CLAIM = "http://wso2.org/claims/identity/accountLocked";

    private UserManagementClient userMgtClient;
    private RemoteUserStoreManagerServiceClient remoteUSMServiceClient;
    private UserProfileMgtServiceClient userProfileMgtClient;
    private RemoteUserStoreManagerServiceClient usmClient;
    private IdentityGovernanceServiceClient identityGovernanceServiceClient;
    private MockSMSProvider mockSMSProvider;
    private NotificationSenderRestClient notificationSenderRestClient;

    @BeforeClass(alwaysRun = true)
    public void testInit() throws Exception {

        super.init();

        mockSMSProvider = new MockSMSProvider();
        mockSMSProvider.start();
        setUpUser();
        usmClient = new RemoteUserStoreManagerServiceClient(backendURL, sessionCookie);

        // Adding custom sms sender.
        notificationSenderRestClient = new NotificationSenderRestClient(
                serverURL, tenantInfo);
        SMSSender smsSender = initSMSSender();
        notificationSenderRestClient.createSMSProvider(smsSender);
    }

    @BeforeMethod
    public void setUp() throws Exception {

        Utils.getMailServer().purgeEmailFromAllMailboxes();
        mockSMSProvider.clearSmsContent();
    }

    @AfterMethod
    public void cleanUp() throws Exception {

        // Clearing the account lock status.
        setUserClaim(ACCOUNT_LOCKED_CLAIM, FALSE_STRING);
    }

    @AfterClass(alwaysRun = true)
    public void atEnd() throws Exception {

        userMgtClient.deleteUser(TEST_USER_USERNAME);
        mockSMSProvider.stop();
    }

    @Test(groups = "wso2.is.governance")
    public void testAdminForcedPasswordResetOffline() throws Exception {

        enableAdminForcedPasswordResetOption(PasswordResetOption.OFFLINE);
        setUserClaim(ADMIN_FORCED_PASSWORD_RESET_CLAIM, TRUE_STRING);
        ClaimValue[] values =
                usmClient.getUserClaimValuesForClaims(TEST_USER_USERNAME, new String[]{ACCOUNT_LOCKED_CLAIM}, null);
        String accountLockClaim = values[0].getValue();
        assertTrue(Boolean.parseBoolean(accountLockClaim),
                "Account is not locked after admin forced password reset for user.");

        String offlineOTP = usmClient.getUserClaimValue(TEST_USER_USERNAME, OTP_CLAIM, PROFILE_NAME);
        assertTrue(StringUtils.isNotBlank(offlineOTP), "OTP claim hasn't updated.");
    }

    @Test(groups = "wso2.is.governance")
    public void testAdminForcedPasswordResetEmailOTP() throws Exception {

        enableAdminForcedPasswordResetOption(PasswordResetOption.EMAIL_OTP);
        setUserClaim(ADMIN_FORCED_PASSWORD_RESET_CLAIM, TRUE_STRING);
        ClaimValue[] values =
                usmClient.getUserClaimValuesForClaims(TEST_USER_USERNAME, new String[]{ACCOUNT_LOCKED_CLAIM}, null);
        String accountLockClaim = values[0].getValue();
        assertTrue(Boolean.parseBoolean(accountLockClaim),
                "Account is not locked after admin forced password reset for user.");
        String emailOTP = getEmailOTP();
        assertTrue(StringUtils.isNotBlank(emailOTP));
    }

    @Test(groups = "wso2.is.governance")
    public void testAdminForcedPasswordResetEmailLink() throws Exception {

        enableAdminForcedPasswordResetOption(PasswordResetOption.EMAIL_LINK);
        setUserClaim(ADMIN_FORCED_PASSWORD_RESET_CLAIM, TRUE_STRING);
        ClaimValue[] values =
                usmClient.getUserClaimValuesForClaims(TEST_USER_USERNAME, new String[]{ACCOUNT_LOCKED_CLAIM}, null);
        String accountLockClaim = values[0].getValue();
        assertTrue(Boolean.parseBoolean(accountLockClaim),
                "Account is not locked after admin forced password reset for user.");
        String emailLink = getEmailLink();
        assertTrue(StringUtils.isNotBlank(emailLink));
    }

    @Test(groups = "wso2.is.governance")
    public void testAdminForcedPasswordResetSMSOTP() throws Exception {

        enableAdminForcedPasswordResetOption(PasswordResetOption.SMS_OTP);
        setUserClaim(ADMIN_FORCED_PASSWORD_RESET_CLAIM, TRUE_STRING);
        ClaimValue[] values =
                usmClient.getUserClaimValuesForClaims(TEST_USER_USERNAME, new String[]{ACCOUNT_LOCKED_CLAIM}, null);
        String accountLockClaim = values[0].getValue();
        assertTrue(Boolean.parseBoolean(accountLockClaim),
                "Account is not locked after admin forced password reset for user.");
        Thread.sleep(5000);
        String smsOTP = mockSMSProvider.getSmsContent();
        assertTrue(StringUtils.isNotBlank(smsOTP), "SMS OTP is not received.");
    }

    private void enableAdminForcedPasswordResetOption(PasswordResetOption option) throws Exception {

        identityGovernanceServiceClient = new IdentityGovernanceServiceClient(sessionCookie, backendURL);

        Property prop1 = new Property();
        prop1.setName(ENABLE_ADMIN_PASSWORD_RESET_OFFLINE);
        prop1.setValue(PasswordResetOption.OFFLINE.equals(option) ? TRUE_STRING : FALSE_STRING);

        Property prop2 = new Property();
        prop2.setName(ENABLE_ADMIN_PASSWORD_RESET_WITH_EMAIL_LINK);
        prop2.setValue(PasswordResetOption.EMAIL_LINK.equals(option) ? TRUE_STRING : FALSE_STRING);

        Property prop3 = new Property();
        prop3.setName(ENABLE_ADMIN_PASSWORD_RESET_WITH_EMAIL_OTP);
        prop3.setValue(PasswordResetOption.EMAIL_OTP.equals(option) ? TRUE_STRING : FALSE_STRING);

        Property prop4 = new Property();
        prop4.setName(ENABLE_ADMIN_PASSWORD_RESET_WITH_SMS_OTP);
        prop4.setValue(PasswordResetOption.SMS_OTP.equals(option) ? TRUE_STRING : FALSE_STRING);

        identityGovernanceServiceClient.updateConfigurations(new Property[]{prop1, prop2, prop3, prop4});
    }

    private void setUpUser() throws UserStoreException, RemoteException,
            RemoteUserStoreManagerServiceUserStoreExceptionException,
            UserProfileMgtServiceUserProfileExceptionException {

        userMgtClient = new UserManagementClient(backendURL, sessionCookie);

        remoteUSMServiceClient = new RemoteUserStoreManagerServiceClient(backendURL, sessionCookie);
        remoteUSMServiceClient.addUser(TEST_USER_USERNAME, TEST_USER_PASSWORD, null, null, PROFILE_NAME, false);
        setUserClaim(EMAIL_CLAIM, TEST_USER_EMAIL);
        setUserClaim(MOBILE_CLAIM, TEST_USER_MOBILE);
    }

    private void setUserClaim(String claimURI, String claimValue)
            throws RemoteException, UserProfileMgtServiceUserProfileExceptionException {

        userProfileMgtClient = new UserProfileMgtServiceClient(backendURL, sessionCookie);
        UserProfileDTO profile = new UserProfileDTO();
        profile.setProfileName(PROFILE_NAME);

        UserFieldDTO passwordResetClaim = new UserFieldDTO();
        passwordResetClaim.setClaimUri(claimURI);
        passwordResetClaim.setFieldValue(claimValue);

        UserFieldDTO[] fields = new UserFieldDTO[1];
        fields[0] = passwordResetClaim;

        profile.setFieldValues(fields);
        userProfileMgtClient.setUserProfile(TEST_USER_USERNAME, profile);
    }

    private String getEmailOTP() {

        assertTrue(Utils.getMailServer().waitForIncomingEmail(10000, 1));
        Message[] messages = Utils.getMailServer().getReceivedMessages();
        String body = GreenMailUtil.getBody(messages[0]).replaceAll("=\r?\n", "");
        Document doc = Jsoup.parse(body);
        return doc.selectFirst("b").text();
    }

    private String getEmailLink() {

        assertTrue(Utils.getMailServer().waitForIncomingEmail(10000, 1));
        Message[] messages = Utils.getMailServer().getReceivedMessages();
        String body = GreenMailUtil.getBody(messages[0]).replaceAll("=\r?\n", "");
        Document doc = Jsoup.parse(body);
        return doc.select("a").attr("href");
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

    private enum PasswordResetOption {
        OFFLINE,
        EMAIL_OTP,
        EMAIL_LINK,
        SMS_OTP
    }
}

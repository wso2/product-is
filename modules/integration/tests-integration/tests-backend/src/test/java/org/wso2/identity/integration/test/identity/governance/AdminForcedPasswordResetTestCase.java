/*
 * Copyright (c) 2016, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
 *
 * WSO2 Inc. licenses this file to you under the Apache License,
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

import org.apache.axiom.om.OMElement;
import org.apache.axiom.om.util.AXIOMUtil;
import org.apache.http.Header;
import org.apache.http.HttpResponse;
import org.apache.http.NameValuePair;
import org.apache.http.client.HttpClient;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.message.BasicNameValuePair;
import org.apache.http.util.EntityUtils;
import org.testng.Assert;
import org.testng.annotations.AfterClass;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;
import org.wso2.carbon.authenticator.stub.LogoutAuthenticationExceptionException;
import org.wso2.carbon.automation.engine.annotations.ExecutionEnvironment;
import org.wso2.carbon.automation.engine.annotations.SetEnvironment;
import org.wso2.carbon.identity.governance.stub.bean.Property;
import org.wso2.carbon.identity.user.profile.stub.UserProfileMgtServiceUserProfileExceptionException;
import org.wso2.carbon.identity.user.profile.stub.types.UserFieldDTO;
import org.wso2.carbon.identity.user.profile.stub.types.UserProfileDTO;
import org.wso2.carbon.integration.common.admin.client.AuthenticatorClient;
import org.wso2.carbon.um.ws.api.stub.ClaimValue;
import org.wso2.carbon.um.ws.api.stub.RemoteUserStoreManagerServiceUserStoreExceptionException;
import org.wso2.carbon.user.api.UserStoreException;
import org.wso2.carbon.user.mgt.stub.UserAdminUserAdminException;
import org.wso2.identity.integration.common.clients.UserManagementClient;
import org.wso2.identity.integration.common.clients.UserProfileMgtServiceClient;
import org.wso2.identity.integration.common.clients.mgt.IdentityGovernanceServiceClient;
import org.wso2.identity.integration.common.clients.usermgt.remote.RemoteUserStoreManagerServiceClient;
import org.wso2.identity.integration.common.utils.ISIntegrationTest;
import org.wso2.identity.integration.test.util.Utils;
import org.wso2.identity.integration.test.utils.CommonConstants;

import javax.xml.namespace.QName;
import java.io.IOException;
import java.rmi.RemoteException;
import java.util.ArrayList;
import java.util.List;


public class AdminForcedPasswordResetTestCase extends ISIntegrationTest {

    public static final String AUTH_FAILURE_MSG = "authFailureMsg";
    public static final String LOGIN_FAIL_MESSAGE = "login.fail.message";
    public static final String CONFIRMATION = "confirmation";
    public static final String LOCATION = "Location";
    public static final String ACS = "/acs";
    public static final String SAMLSSO = "/samlsso";
    public static final String COMMONAUTH = "/commonauth";
    public static final String SESSION_DATA_KEY = "sessionDataKey";
    private UserManagementClient userMgtClient;
    private AuthenticatorClient loginManger;
    private RemoteUserStoreManagerServiceClient remoteUSMServiceClient;
    private static final String PROFILE_NAME = "default";
    private static final String TEST_USER_USERNAME = "testUser";
    private static final String TEST_USER_PASSWORD = "Ab@123";
    private static final String TEST_USER_NEW_PASSWORD = "AbNew@123";
    private static final String TEST_USER_EMAIL = "test@test.com";
    private static final String TEST_ROLE = "testRole";

    private static final String TRUE_STRING = "true";

    private static final String ENABLE_ADMIN_PASSWORD_RESET_OFFLINE = "Recovery.AdminPasswordReset.Offline";
    private static final String ENABLE_ADMIN_PASSWORD_RESET_WITH_OTP = "Recovery.AdminPasswordReset.OTP";
    private static final String ENABLE_ADMIN_PASSWORD_RESET_WITH_RECOVERY_LINK = "Recovery.AdminPasswordReset.RecoveryLink";
    private static final String ENABLE_NOTIFICATION_BASED_PASSWORD_RECOVERY = "Recovery.Notification.Password.Enable";
    private static final String ADMIN_FORCED_PASSWORD_RESET_CLAIM = "http://wso2.org/claims/identity/adminForcedPasswordReset";
    private static final String OTP_CLAIM = "http://wso2.org/claims/oneTimePassword";
    private static final String EMAIL_CLAIM = "http://wso2.org/claims/emailaddress";
    private static final String ACCOUNT_LOCKED_CLAIM = "http://wso2.org/claims/identity/accountLocked";

    private static final String USER_AGENT = "Apache-HttpClient/4.2.5 (java 1.5)";

    private static final String CONFIRM_RECOVERY_ENDPOINT = "confirmrecovery.do";
    private static final String COMPLETE_PASSWORD_RECOVERY_ENDPOINT = "completepasswordreset.do";

    private String dashboardPortalURL;
    private String webAppUrlContext;
    private static final String DASHBOARD = "dashboard";
    private static final String DASHBOARD_PORTAL_URL = "/" + DASHBOARD;
    private static final String DEFAULT = "default";

    private String currentOTP = "";

    private UserProfileMgtServiceClient userProfileMgtClient;
    private HttpClient httpClient;
    private RemoteUserStoreManagerServiceClient usmClient;

    private IdentityGovernanceServiceClient identityGovernanceServiceClient;

    @SetEnvironment(executionEnvironments = {ExecutionEnvironment.ALL})
    @BeforeClass(alwaysRun = true)
    public void testInit() throws Exception {
        super.init();
        loginManger = new AuthenticatorClient(backendURL);
        webAppUrlContext = identityContextUrls.getWebAppURLHttps();
        dashboardPortalURL = webAppUrlContext + DASHBOARD_PORTAL_URL;
        selectPasswordResetOption(ENABLE_ADMIN_PASSWORD_RESET_OFFLINE);
        setUpUser();
        httpClient = new DefaultHttpClient();
        usmClient = new RemoteUserStoreManagerServiceClient(backendURL, sessionCookie);
    }

    @SetEnvironment(executionEnvironments = {ExecutionEnvironment.ALL})
    @AfterClass(alwaysRun = true)
    public void atEnd() throws Exception {
        cleanUpUser();
    }

    @Test(groups = "wso2.is.governance", description = "Check whether the OTP and AccountLock claims get updated")
    public void testAdminForcedPasswordResetOfflineAction() throws Exception {

        setUserClaim(ADMIN_FORCED_PASSWORD_RESET_CLAIM, "true");

        ClaimValue[] values = usmClient.getUserClaimValuesForClaims(TEST_USER_USERNAME, new String[]{
                ACCOUNT_LOCKED_CLAIM}, null);
        String accountLockClaim = values[0].getValue();
        Assert.assertTrue(Boolean.valueOf(accountLockClaim), "Account is not locked after admin " +
                "forced password reset for user.");

        currentOTP = usmClient.getUserClaimValue(TEST_USER_USERNAME, OTP_CLAIM, PROFILE_NAME);
        Assert.assertFalse(currentOTP.isEmpty());

    }

    @Test(groups = "wso2.is.governance", description = "Check whether the login fails with current password",
            dependsOnMethods = "testAdminForcedPasswordResetOfflineAction")
    public void testAdminForcedPasswordResetOfflineLoginWithPreviousPassword() throws Exception {
        HttpResponse loginResponse = loginToDashboardApp(TEST_USER_USERNAME, TEST_USER_PASSWORD);
        String errorMessage = getQueryParamFromURL(getHeader(loginResponse, LOCATION), AUTH_FAILURE_MSG);
        EntityUtils.consume(loginResponse.getEntity());
        Assert.assertEquals(errorMessage, LOGIN_FAIL_MESSAGE);

    }

    @Test(groups = "wso2.is.governance", description = "Check whether password reset and login get success with OTP",
            dependsOnMethods = "testAdminForcedPasswordResetOfflineLoginWithPreviousPassword")
    public void testAdminForcedPasswordResetOfflineLoginWIthPreviousOTP() throws Exception {
        //First login attempt to dashboard app
        HttpResponse response = loginToDashboardApp(TEST_USER_USERNAME, currentOTP);
        String confirmation = getQueryParamFromURL(getHeader(response, LOCATION), CONFIRMATION);
        EntityUtils.consume(response.getEntity());

        Assert.assertEquals(confirmation, currentOTP, "Correct confirmation code is not present in password reset " +
                "redirect URL.");

        //GET password reset page
        String passwordRecoveryURL = getHeader(response, LOCATION);
        response = Utils.sendGetRequest(passwordRecoveryURL, USER_AGENT, httpClient);
        String resetPasswordElement = Utils.extractDataFromResponse(response, "Enter New Password", 0);
        Assert.assertTrue(!resetPasswordElement.isEmpty(), "Did not redirected to password reset Page after providing" +
                " correct OTP");

        EntityUtils.consume(response.getEntity());

        //Make password Reset Request
        response = sendPasswordResetMessage(passwordRecoveryURL.replace(CONFIRM_RECOVERY_ENDPOINT,
                COMPLETE_PASSWORD_RECOVERY_ENDPOINT), USER_AGENT, TEST_USER_NEW_PASSWORD, httpClient);
        String success = Utils.extractDataFromResponse(response, "Updated the password successfully", 0);
        EntityUtils.consume(response.getEntity());
        Assert.assertTrue(!success.isEmpty(), "Password reset was not successful.");

        //Next login attempt after password reset
        response = loginToDashboardApp(TEST_USER_USERNAME, TEST_USER_NEW_PASSWORD);

        if (Utils.requestMissingClaims(response)) {
            String pastrCookie = Utils.getPastreCookie(response);
            Assert.assertNotNull(pastrCookie, "pastr cookie not found in response.");
            EntityUtils.consume(response.getEntity());

            response = Utils.sendPOSTConsentMessage(response, webAppUrlContext + COMMONAUTH, USER_AGENT,
                                                    Utils.getRedirectUrl(response), httpClient, pastrCookie);
            EntityUtils.consume(response.getEntity());
        }

        response = Utils.sendRedirectRequest(response, USER_AGENT, Utils.getRedirectUrl(response), "", httpClient);
        //Extract SAMLResponse
        String samlResponse = Utils.extractDataFromResponse(response, CommonConstants.SAML_RESPONSE_PARAM, 5);
        EntityUtils.consume(response.getEntity());

        String acsURL = webAppUrlContext + DASHBOARD_PORTAL_URL + ACS;
        //send SAMLResponse to acs
        response = sendSAMLMessage(acsURL, CommonConstants.SAML_RESPONSE_PARAM, samlResponse);
        EntityUtils.consume(response.getEntity());

        //Redirect to dashboard home page after acs
        response = Utils.sendRedirectRequest(response, USER_AGENT, webAppUrlContext + "/%s" + ACS, DASHBOARD,
                httpClient);

        String resultPage = Utils.extractDataFromResponse(response, TEST_USER_USERNAME, 0);
        Assert.assertTrue(!resultPage.isEmpty(), "Login to dashboard After Password reset was not successful.");

        EntityUtils.consume(response.getEntity());
    }

    protected HttpResponse loginToDashboardApp(String username, String password) throws Exception {
        HttpResponse response = Utils.sendGetRequest(dashboardPortalURL, USER_AGENT, httpClient);

        //extract samlRequest from response body
        String samlRequestElement = Utils.extractDataFromResponse(response, CommonConstants.SAML_REQUEST_PARAM, 0);
        EntityUtils.consume(response.getEntity());
        OMElement documentElement = AXIOMUtil.stringToOM(samlRequestElement);
        String samlRequest = documentElement.getAttributeValue(new QName("value"));

        String samlEndpoint = webAppUrlContext + SAMLSSO;
        response = sendSAMLMessage(samlEndpoint, CommonConstants.SAML_REQUEST_PARAM, samlRequest);
        String loginURL = getHeader(response, LOCATION);
        String sessionDataKey = getQueryParamFromURL(loginURL, SESSION_DATA_KEY);

        EntityUtils.consume(response.getEntity());

        return sendLoginRequset(sessionDataKey, samlEndpoint, USER_AGENT, username, password, httpClient);
    }

    private String getQueryParamFromURL(String url, String key) {
        String params[] = url.split("&");
        String value = "";
        for (String param : params) {
            if (param.contains(key)) {
                value = param.split("=")[1];
                return value;
            }
        }
        return value;
    }

    protected void selectPasswordResetOption(String option) throws Exception {
        identityGovernanceServiceClient = new IdentityGovernanceServiceClient(sessionCookie, backendURL);

        Thread.sleep(5000);
        loginManger.login(isServer.getSuperTenant().getTenantAdmin().getUserName(),
                isServer.getSuperTenant().getTenantAdmin().getPassword(),
                isServer.getInstance().getHosts().get(DEFAULT));

        Property[] newProperties = new Property[3];

        Property prop = new Property();
        prop.setName(option);
        prop.setValue(TRUE_STRING);

        Property prop1 = new Property();
        prop1.setName(ENABLE_ADMIN_PASSWORD_RESET_WITH_RECOVERY_LINK);
        prop1.setValue("false");

        Property prop2 = new Property();
        prop2.setName(ENABLE_NOTIFICATION_BASED_PASSWORD_RECOVERY);
        prop2.setValue(TRUE_STRING);

        newProperties[0] = prop;
        newProperties[1] = prop1;
        newProperties[2] = prop2;
        identityGovernanceServiceClient.updateConfigurations(newProperties);
    }

    protected void setUpUser() throws UserStoreException, RemoteException, RemoteUserStoreManagerServiceUserStoreExceptionException, UserAdminUserAdminException, UserProfileMgtServiceUserProfileExceptionException, LogoutAuthenticationExceptionException {

        userMgtClient = new UserManagementClient(backendURL, sessionCookie);

        remoteUSMServiceClient = new RemoteUserStoreManagerServiceClient(backendURL, sessionCookie);
        remoteUSMServiceClient.addUser(TEST_USER_USERNAME, TEST_USER_PASSWORD, null,
                null, PROFILE_NAME, false);
        userMgtClient.addRole(TEST_ROLE, new String[]{TEST_USER_USERNAME}, new String[]{"/permission/admin/login"}, false);
        setUserClaim(EMAIL_CLAIM, TEST_USER_EMAIL);
    }

    protected void cleanUpUser() throws LogoutAuthenticationExceptionException, RemoteException, UserAdminUserAdminException {
        loginManger.logOut();
        userMgtClient.deleteUser(TEST_USER_USERNAME);
        userMgtClient.deleteRole(TEST_ROLE);
    }

    protected void setUserClaim(String claimURI, String calimValue) throws LogoutAuthenticationExceptionException,
            RemoteException,
            UserAdminUserAdminException, UserProfileMgtServiceUserProfileExceptionException {
        userProfileMgtClient = new UserProfileMgtServiceClient(backendURL, sessionCookie);
        UserProfileDTO profile = new UserProfileDTO();
        profile.setProfileName(PROFILE_NAME);

        UserFieldDTO passwordResetClaim = new UserFieldDTO();
        passwordResetClaim.setClaimUri(claimURI);
        passwordResetClaim.setFieldValue(calimValue);

        UserFieldDTO[] fields = new UserFieldDTO[1];
        fields[0] = passwordResetClaim;

        profile.setFieldValues(fields);

        userProfileMgtClient.setUserProfile(TEST_USER_USERNAME, profile);
    }

    private String getHeader(HttpResponse response, String key) throws IOException {
        Header[] headers = response.getAllHeaders();
        String url = "";
        for (Header header : headers) {
            if (key.equals(header.getName())) {
                url = header.getValue();
            }
        }
        return url;
    }

    private HttpResponse sendSAMLMessage(String url, String samlMsgKey, String samlMsgValue) throws IOException {
        List<NameValuePair> urlParameters = new ArrayList<NameValuePair>();
        HttpPost post = new HttpPost(url);
        post.setHeader("User-Agent", USER_AGENT);
        urlParameters.add(new BasicNameValuePair(samlMsgKey, samlMsgValue));
        post.setEntity(new UrlEncodedFormEntity(urlParameters));
        return httpClient.execute(post);
    }

    private static HttpResponse sendLoginRequset(String sessionKey, String url, String userAgent, String userName, String password, HttpClient httpClient) throws Exception {

        List<NameValuePair> urlParameters = new ArrayList<NameValuePair>();
        urlParameters.add(new BasicNameValuePair("username", userName));
        urlParameters.add(new BasicNameValuePair("password", password));
        urlParameters.add(new BasicNameValuePair(SESSION_DATA_KEY, sessionKey));
        urlParameters.add(new BasicNameValuePair("tocommonauth", "true"));

        return sendPOSTMessage(url, userAgent, urlParameters , httpClient);
    }

    private static HttpResponse sendPasswordResetMessage(String passwordResetURL, String userAgent, String password,
                                                         HttpClient httpClient) throws Exception {
        List<NameValuePair> urlParameters = new ArrayList<NameValuePair>();
        urlParameters.add(new BasicNameValuePair("reset-password", password));
        urlParameters.add(new BasicNameValuePair("reset-password2", password));
        passwordResetURL = passwordResetURL + "&reset-password=" + password + "&reset-password2=" + password;
        return sendPOSTMessage(passwordResetURL, userAgent, urlParameters , httpClient);
    }

    private static HttpResponse sendPOSTMessage(String url, String userAgent,
                                                List<NameValuePair> urlParameters,
                                                HttpClient httpClient) throws Exception {
        HttpPost post = new HttpPost(url);
        post.setHeader("User-Agent", userAgent);
        post.setEntity(new UrlEncodedFormEntity(urlParameters));
        return httpClient.execute(post);
    }

}

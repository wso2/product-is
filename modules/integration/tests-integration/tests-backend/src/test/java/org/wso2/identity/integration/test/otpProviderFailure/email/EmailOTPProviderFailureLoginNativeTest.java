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

package org.wso2.identity.integration.test.otpProviderFailure.email;

import org.testng.Assert;
import org.testng.annotations.AfterClass;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.DataProvider;
import org.testng.annotations.Factory;
import org.testng.annotations.Test;
import org.wso2.carbon.automation.engine.context.TestUserMode;
import org.wso2.identity.integration.test.otpProviderFailure.AbstractOTPProviderFailureTestBase;
import org.wso2.identity.integration.test.otpProviderFailure.Constants;
import org.wso2.identity.integration.test.restclients.OAuth2RestClient;
import org.wso2.identity.integration.test.restclients.SCIM2RestClient;
import org.wso2.identity.integration.test.util.Utils;

public class EmailOTPProviderFailureLoginNativeTest extends AbstractOTPProviderFailureTestBase {

    private static final String APP_NAME = "EmailOTPProviderFailureLoginNativeApp";

    private String appId;
    private String clientId;
    private String userId;
    private SCIM2RestClient scim2RestClient;

    @Factory(dataProvider = "testExecutionContextProvider")
    public EmailOTPProviderFailureLoginNativeTest(TestUserMode userMode) throws Exception {

        super.init(userMode);
    }

    @DataProvider(name = "testExecutionContextProvider")
    public static Object[][] getTestExecutionContext() {

        return new Object[][]{
                {TestUserMode.SUPER_TENANT_USER},
                {TestUserMode.TENANT_USER}
        };
    }

    @BeforeClass(alwaysRun = true)
    public void testInit() throws Exception {

        restClient = new OAuth2RestClient(serverURL, tenantInfo);
        scim2RestClient = new SCIM2RestClient(serverURL, tenantInfo);

        appId = addTwoStepNativeApp(APP_NAME, Constants.EMAIL_OTP_AUTHENTICATOR);
        clientId = getOIDCInboundDetailsOfApplication(appId).getClientId();
        userId = createTestUser(scim2RestClient);
    }

    @AfterClass(alwaysRun = true)
    public void testTearDown() throws Exception {

        disableEmailOTPSendingFailureNotification();

        if (appId != null) {
            deleteApp(appId);
        }
        if (userId != null) {
            scim2RestClient.deleteUser(userId);
        }

        restClient.closeHttpClient();
        scim2RestClient.closeHttpClient();
    }

    @Test(groups = "wso2.is")
    public void testProviderFailureHiddenByDefault() throws Exception {

        Utils.getMailServer().stop();
        AuthnFlowState state = initiateNativeFlow(clientId);
        state = completeBasicAuth(state, Constants.TEST_USER_NAME, Constants.TEST_USER_PASSWORD);
        Assert.assertFalse(hasProviderFailureInMessages(state.response),
                "Provider failure should not be visible in messages when config is disabled.");
        Assert.assertEquals(state.response.get("flowStatus"), Constants.FLOW_STATUS_INCOMPLETE,
                "Flow should be INCOMPLETE at OTP step.");
    }

    @Test(groups = "wso2.is", dependsOnMethods = "testProviderFailureHiddenByDefault")
    public void testProviderFailureShownWhenConfigEnabled() throws Exception {

        enableEmailOTPSendingFailureNotification();
        AuthnFlowState state = initiateNativeFlow(clientId);
        state = completeBasicAuth(state, Constants.TEST_USER_NAME, Constants.TEST_USER_PASSWORD);
        Assert.assertTrue(hasProviderFailureInMessages(state.response),
                "Provider failure should be visible in messages when config is enabled.");
        Assert.assertEquals(state.response.get("flowStatus"), Constants.FLOW_STATUS_INCOMPLETE,
                "Flow should be INCOMPLETE at OTP step.");
    }

    @Test(groups = "wso2.is", dependsOnMethods = "testProviderFailureShownWhenConfigEnabled")
    public void testSuccessfulLoginAfterProviderRestored() throws Exception {

        Utils.getMailServer().start();
        Utils.getMailServer().purgeEmailFromAllMailboxes();
        AuthnFlowState state = initiateNativeFlow(clientId);
        state = completeBasicAuth(state, Constants.TEST_USER_NAME, Constants.TEST_USER_PASSWORD);
        String otp = extractOtpFromEmail(1);
        Assert.assertNotNull(otp, "OTP should be received from mail server.");
        state = submitOTPCode(state, otp);
        Assert.assertEquals(state.response.get("flowStatus"), Constants.FLOW_STATUS_SUCCESS,
                "Flow should be SUCCESS_COMPLETED after correct OTP.");
    }
}

/*
 * Copyright (c) 2017, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
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

package org.wso2.is.portal.admin.test.ui;

import org.openqa.selenium.WebDriver;
import org.testng.Assert;
import org.testng.annotations.AfterClass;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;
import org.wso2.carbon.iam.adminportal.actionobject.AdminLoginPageAction;
import org.wso2.carbon.iam.adminportal.actionobject.AdminInitiatedPasswordResetAction;

import java.util.UUID;

/**
 * Test class for admin initiated password reset scenario.
 */
public class AdminInitiatedPasswordResetTest extends org.wso2.is.portal.admin.test.ui.SelectDriver {
    private static AdminLoginPageAction adminLoginPageAction;
    private static AdminInitiatedPasswordResetAction adminInitiatedPasswordResetAction;

    private static WebDriver driver;
    private static String adminLoginPage = "https://" + System.getProperty("home") + ":" +
            System.getProperty("port") + "/admin-portal/login";
    private static String adminPage = "https://" + System.getProperty("home") + ":" +
            System.getProperty("port") + "/admin-portal/";
    private static String adminInitiatedPasswordResetPage = "https://" + System.getProperty("home") + ":" +
            System.getProperty("port") + "/admin-portal/reset/admin-initiated-password-reset";

    @BeforeClass
    public void init() {
        driver = selectDriver(System.getProperty("driver"));
        adminLoginPageAction = new AdminLoginPageAction(driver);
        adminInitiatedPasswordResetAction = new AdminInitiatedPasswordResetAction(driver);
    }

    @Test(groups = "AdminInitiatedPasswordResetTest")
    public void loginPage() throws Exception {
        driver.get(adminLoginPage);
        Assert.assertEquals(driver.getCurrentUrl(), adminLoginPage,
                "This current page is not the login page.");
        String username = System.getProperty("username");
        String password = System.getProperty("password");
        boolean logged = adminLoginPageAction.adminLogin(username, password);
        Assert.assertTrue(logged, "Loggin failed.");
        Assert.assertEquals(driver.getCurrentUrl(), adminPage,
                "This current page is not the admin user page.");
    }


    @Test(groups = "AdminInitiatedPasswordResetTest", dependsOnMethods = "loginPage")
    public void testPasswordReset() throws Exception {
        driver.get(adminInitiatedPasswordResetPage);
        Assert.assertEquals(driver.getCurrentUrl(), adminInitiatedPasswordResetPage,
                "This current page is not the Admin Initiated Password Reset Page.");
        String passcode = adminInitiatedPasswordResetAction.generatePassCode();
        Assert.assertNotNull(passcode, "The user is not added correctly");
    }

    @AfterClass
    public void close() {
        driver.quit();
    }

}


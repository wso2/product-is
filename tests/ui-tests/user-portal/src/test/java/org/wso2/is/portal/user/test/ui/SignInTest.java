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

package org.wso2.is.portal.user.test.ui;

import org.openqa.selenium.WebDriver;
import org.testng.Assert;
import org.testng.annotations.Test;
import org.wso2.carbon.iam.userportal.actionobject.LoginPageAction;



/**
 * UI Tests for Sign In.
 */
public class SignInTest extends SelectDriver {

    private static LoginPageAction loginPageAction = new LoginPageAction();
    private static WebDriver driver;
    private static String loginPage = "https://" + System.getProperty("home")  + ":" +
            System.getProperty("port") + "/user-portal/login";
    private static String adminPage = "https://" + System.getProperty("home")  + ":" +
            System.getProperty("port") + "/user-portal/";
    private static String usernameRecoveryPage = "https://" + System.getProperty("home")  + ":" +
            System.getProperty("port") + "/user-portal/recovery/username";
    private static String passwordRecoveryPage = "https://" + System.getProperty("home")  + ":" +
            System.getProperty("port") + "/user-portal/recovery/password";

    @Test(groups = "signInTest")
    public void loadLoginPage() throws Exception {
        driver = selectDriver(System.getProperty("driver"));
        driver.get(loginPage);
        driver.quit();
    }

    @Test(groups = "signInTest", dependsOnMethods = "loadLoginPage")
    public void testLogin() throws Exception {
        driver = selectDriver(System.getProperty("driver"));
        driver.get(loginPage);
        String username = System.getProperty("username");
        String password = System.getProperty("password");
        loginPageAction.login(driver, username, password);
        Assert.assertEquals(driver.getCurrentUrl(), adminPage,
                "This current page is not the admin user page.");
        driver.quit();
    }

    @Test(groups = "signInTest", dependsOnMethods = "loadLoginPage")
    public void testClickUsernameRecovery() throws Exception {
        driver = selectDriver(System.getProperty("driver"));
        driver.get(loginPage);
        loginPageAction.clickForgetUsername(driver);
        Assert.assertEquals(driver.getCurrentUrl(), usernameRecoveryPage,
                "This current page is not the username recovery page.");
        driver.quit();
    }

    @Test(groups = "signInTest", dependsOnMethods = "loadLoginPage")
    public void testClickPasswordRecovery() throws Exception {
        driver = selectDriver(System.getProperty("driver"));
        driver.get(loginPage);
        loginPageAction.clickForgetPassword(driver);
        Assert.assertEquals(driver.getCurrentUrl(), passwordRecoveryPage,
                "This current page is not the password recovery page.");
        driver.quit();

    }
}

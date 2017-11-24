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
import org.testng.annotations.AfterClass;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;
import org.wso2.carbon.iam.userportal.actionobject.LoginPageAction;

/**
 * UI test for Sign in Test.
 */
public class SignInTest extends SelectDriver {

    private static LoginPageAction loginPageAction;

    private static WebDriver driver;
    private static String loginPage = "https://" + System.getProperty("home") + ":" +
            System.getProperty("port") + "/user-portal/login";
    private static String adminPage = "https://" + System.getProperty("home") + ":" +
            System.getProperty("port") + "/user-portal/";

    @BeforeClass
    public void init() {
        driver = selectDriver(System.getProperty("driver"));
        loginPageAction = new LoginPageAction(driver);
    }

    @Test(groups = "signInTest")
    public void loadLoginPage() throws Exception {

        driver.get(loginPage);
        Assert.assertEquals(driver.getCurrentUrl(), loginPage,
                "This current page is not the login page.");
    }

    @Test(groups = "signInTest", dependsOnMethods = "loadLoginPage")
    public void testLogin() throws Exception {
        String username = System.getProperty("username");
        String password = System.getProperty("password");
        boolean logged = loginPageAction.login(username, password);
        Assert.assertTrue(logged, "Loggin failed.");
        Assert.assertEquals(driver.getCurrentUrl(), adminPage,
                "This current page is not the admin user page.");
    }

    @AfterClass
    public void close() {
        driver.quit();
    }
}

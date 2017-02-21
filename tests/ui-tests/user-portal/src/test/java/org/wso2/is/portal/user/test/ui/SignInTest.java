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
import org.openqa.selenium.htmlunit.HtmlUnitDriver;
import org.testng.Assert;
import org.testng.annotations.Test;
import org.wso2.carbon.iam.userportal.actionobject.LoginPageAction;



public class SignInTest {

    private static LoginPageAction loginPageAction = new LoginPageAction();
    private static WebDriver driver = new HtmlUnitDriver();
    private static String loginPage = "https://localhost:9292/user-portal/login";
    private static String adminPage = "https://localhost:9292/user-portal/";
    private static String usernameRecoveryPage = "https://localhost:9292/user-portal/recovery/username";
    private static String passwordRecoveryPage = "https://localhost:9292/user-portal/recovery/password";


    @Test(groups = "signInTest")
    public void loadLoginPage() throws Exception {
        driver.get(loginPage);
    }

    @Test(groups = "signInTest", dependsOnMethods = "loadLoginPage")
    public void testLogin() throws Exception {
        String username = "admin";
        String password = "admin";
        loginPageAction.login(driver, username, password);
        Assert.assertEquals(driver.getCurrentUrl(), adminPage);
        driver.close();
        driver = new HtmlUnitDriver();
        driver.get(loginPage);
        testClickUsernameRecovery(driver);

    }

    @Test(groups = "signInTest", dependsOnMethods = "loadLoginPage")
    public void testClickUsernameRecovery(WebDriver driver) throws Exception {
        loginPageAction.clickForgetUsername(driver);
        Assert.assertEquals(driver.getCurrentUrl(), usernameRecoveryPage);
        driver.close();
        driver = new HtmlUnitDriver();
        driver.get(loginPage);
        testClickPasswordRecovery(driver);
    }

    @Test(groups = "signInTest", dependsOnMethods = "loadLoginPage")
    public void testClickPasswordRecovery(WebDriver driver) throws Exception {
        loginPageAction.clickForgetPassword(driver);
        Assert.assertEquals(driver.getCurrentUrl(), passwordRecoveryPage);
        driver.quit();

    }
}

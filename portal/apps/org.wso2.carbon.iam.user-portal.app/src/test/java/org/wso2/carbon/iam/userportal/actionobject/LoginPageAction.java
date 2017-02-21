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

package org.wso2.carbon.iam.userportal.actionobject;

import org.openqa.selenium.WebDriver;
import org.wso2.carbon.iam.userportal.pageobject.LoginPage;

/**
 * Action class for login.
 */
public class LoginPageAction {

    private static LoginPage loginPage = new LoginPage();

    public void login(WebDriver driver, String username, String password) {
        loginPage.txtbx_UserName(driver).sendKeys(username);
        loginPage.txtbx_Password(driver).sendKeys(password);
        loginPage.btn_SignIn(driver).click();
    }

    public void signUp(WebDriver driver) {
        loginPage.lnk_SignUp(driver).click();
    }

    public void clickForgetUsername(WebDriver driver) {
        loginPage.lnk_ForgotUsername(driver).click();
    }

    public void clickForgetPassword(WebDriver driver) {
        loginPage.lnk_ForgotPassword(driver).click();
    }

}

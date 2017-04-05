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
import org.wso2.carbon.iam.adminportal.actionobject.AskPwdViaEmailPageAction;
import org.wso2.is.portal.admin.test.ui.bean.AdminPortalURLBean;

import java.util.UUID;

/***
 *  Test class for add user - ask password via email scenario.
 */
public class AddUserAskPwdViaEmailTest extends UIBaseTest {
    private static AdminLoginPageAction  adminLoginPageAction;
    private static AskPwdViaEmailPageAction askPwdViaEmailPageAction;

    private static WebDriver driver;

    @BeforeClass
    public void init() {
        driver = selectDriver(System.getProperty("driver"));
        adminLoginPageAction = new AdminLoginPageAction(driver);
        askPwdViaEmailPageAction = new AskPwdViaEmailPageAction(driver);
    }

    @Test(groups = "AddUserAskPwdViaEmailTest")
    public void loginPage() throws Exception {
        driver.get(AdminPortalURLBean.getAdminLoginPage());
        Assert.assertEquals(driver.getCurrentUrl(), AdminPortalURLBean.getAdminLoginPage(),
                "This current page is not the login page.");
        String username = System.getProperty("username");
        String password = System.getProperty("password");
        boolean logged = adminLoginPageAction.adminLogin(username, password);
        Assert.assertEquals(driver.getCurrentUrl(), AdminPortalURLBean.getAdminPage(),
                "This current page is not the admin user page.");
    }


    @Test(groups = "AddUserAskPwdViaEmailTest", dependsOnMethods = "loginPage")
    public void testAddCorrectUser() throws Exception {
        driver.get(AdminPortalURLBean.getAddUserPage());
        Assert.assertEquals(driver.getCurrentUrl(), AdminPortalURLBean.getAddUserPage(),
                "This current page is not the add user page.");
        String username = UUID.randomUUID().toString();
        String email = "sample@wso2.com";
        String confirmEmail = "sample@wso2.com";
        boolean added = askPwdViaEmailPageAction.addUserAskPwdViaEmail(username, email, confirmEmail);
        Assert.assertTrue(added, "The user is not added correctly");
        Assert.assertEquals(driver.getCurrentUrl(), AdminPortalURLBean.getAddUserPage(),
                "This current page is not the add user page.");
    }

    @AfterClass
    public void close() {
        driver.quit();
    }

}

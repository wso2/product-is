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
import org.testng.annotations.AfterClass;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;
import org.wso2.carbon.iam.adminportal.actionobject.AddNewUserPageAction;
import org.wso2.carbon.iam.adminportal.actionobject.AdminLoginPageAction;

/***
 *  Test class for add user - with password scenario
 */
public class AddUserTest extends UIBaseTest {
    private static AdminLoginPageAction adminLoginPageAction;
    private static AddNewUserPageAction addNewUserPageAction;

    private static WebDriver driver;

    @BeforeClass
    public void init() {
        driver = selectDriver(System.getProperty("driver"));
        adminLoginPageAction = new AdminLoginPageAction(driver);
        addNewUserPageAction = new AddNewUserPageAction(driver);
    }

    @Test(groups = "AddUserTest")
    public void loginPage() throws Exception {
        //TODO:Test to log in as the admin user
    }

    @Test(groups = "AddUserTest", dependsOnMethods = "loginPage")
    public void testAddNewUser() throws Exception {
        //TODO:Test to add the new user with username, passwrod, confirmpassword and domain.
    }

    @Test(groups = "AddUserTest", dependsOnMethods = "loginPage")
    public void testAddNewUserWithEmptyUsername() throws Exception {
        //TODO:Test to add the new user with an empty username.
    }

    @Test(groups = "AddUserTest", dependsOnMethods = "loginPage")
    public void testAddNewUserWithInvalidPassword() throws Exception {
        //TODO:Test to add the new user with an invalid password (password that is not in compliance with
        // the default password policy)
    }

    @AfterClass
    public void close() {
        driver.quit();
    }
}

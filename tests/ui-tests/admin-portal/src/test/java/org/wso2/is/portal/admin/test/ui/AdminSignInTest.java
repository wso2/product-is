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
import org.wso2.is.portal.admin.test.ui.bean.AdminPortalURLBean;


/**
 * Test for Sign-in in admin portal.
 */
public class AdminSignInTest extends UIBaseTest {
    private static AdminLoginPageAction adminLoginPageAction;

    private static WebDriver driver;

    @BeforeClass
    public void init() {
        driver = selectDriver(System.getProperty("driver"));
        adminLoginPageAction = new AdminLoginPageAction(driver);
    }

    @Test(groups = "AdminSignInTest")
    public void loadLoginPage() throws Exception {

        driver.get(AdminPortalURLBean.getAdminLoginPage());
        Assert.assertEquals(driver.getCurrentUrl(), AdminPortalURLBean.getAdminLoginPage(),
                "This current page is not the login page.");
    }

    @Test(groups = "AdminSignInTest", dependsOnMethods = "loadLoginPage")
    public void testLogin() throws Exception {
        String username = System.getProperty("username");
        String password = System.getProperty("password");
        boolean logged = adminLoginPageAction.adminLogin(username, password);
        Assert.assertTrue(logged, "Loggin failed.");
        Assert.assertEquals(driver.getCurrentUrl(), AdminPortalURLBean.getAdminPage(),
                "This current page is not the admin user page.");

    }
    @AfterClass
    public void close() {
        driver.quit();
    }
}

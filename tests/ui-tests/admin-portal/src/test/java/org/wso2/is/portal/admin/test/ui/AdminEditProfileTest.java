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
import org.wso2.carbon.iam.adminportal.actionobject.EditAdminUserPageAction;

/**
 * Test for edit Default profile.
 */
public class AdminEditProfileTest extends SelectDriver {
    private static AdminLoginPageAction  adminLoginPageAction;
    private static EditAdminUserPageAction editAdminUserPageAction;

    private static WebDriver driver;
    private static String adminLoginPage = "https://" + System.getProperty("home") + ":" +
            System.getProperty("port") + "/admin-portal/login";
    private static String adminPage = "https://" + System.getProperty("home") + ":" +
            System.getProperty("port") + "/admin-portal/";
    private static String editAdminPage = "https://" + System.getProperty("home") + ":" +
            System.getProperty("port") + "/admin-portal/users/edit";
    private static String defaultProfilePage = "https://" + System.getProperty("home") + ":" +
            System.getProperty("port") + "/admin-portal/users/edit?default#default";
    private static String employeeProfilePage = "https://" + System.getProperty("home") + ":" +
            System.getProperty("port") + "/admin-portal/users/edit?employee#employee";

    @BeforeClass
    public void init() {
        driver = selectDriver(System.getProperty("driver"));
        adminLoginPageAction = new AdminLoginPageAction(driver);
        editAdminUserPageAction = new EditAdminUserPageAction(driver);
    }
    @Test(groups = "AdminEditDefaultProfileTest")
    public void loadLoginPage() throws Exception {

        driver.get(adminLoginPage);
        Assert.assertEquals(driver.getCurrentUrl(), adminLoginPage,
                "This current page is not the login page.");
    }

    @Test(groups = "AdminEditDefaultProfileTest", dependsOnMethods = "loadLoginPage")
    public void testLogin() throws Exception {
        String username = System.getProperty("username");
        String password = System.getProperty("password");
        boolean logged = adminLoginPageAction.adminLogin(username, password);
        Assert.assertTrue(logged, "Loggin failed.");
        Assert.assertEquals(driver.getCurrentUrl(), adminPage,
                "This current page is not the admin user page.");

    }

    @Test(groups = "AdminEditDefaultProfileTest", dependsOnMethods = "testLogin")
    public void editDefaultProfile() throws Exception {
        driver.get(editAdminPage);
        Assert.assertEquals(driver.getCurrentUrl(), editAdminPage,
                "This current page is not the edit user page.");

        String email = "admin@wso2.com";
        String firstName = "dinali";
        boolean edited = editAdminUserPageAction.updateDefaultProfile(email, firstName);
        Assert.assertTrue(edited, "Edited the profile");
        Assert.assertEquals(driver.getCurrentUrl(), defaultProfilePage,
                "This current page is not the edit default profile user page.");
        String availableEmail = editAdminUserPageAction.getEmployeeEmail().getAttribute("value");
        Assert.assertEquals(availableEmail, email, "The entered value is not updated.");

    }

    @Test(groups = "AdminEditDefaultProfileTest", dependsOnMethods = "editDefaultProfile")
    public void changeProfiles() throws Exception {
        driver.get(editAdminPage);
        Assert.assertEquals(driver.getCurrentUrl(), editAdminPage,
                "This current page is not the edit user page.");

        boolean changed = editAdminUserPageAction.selectEmployeeOption();
        Assert.assertTrue(changed, "Selected is not the Employee profile");
        Assert.assertEquals(driver.getCurrentUrl(), employeeProfilePage,
                "This current page is not the edit employee profile user page.");
    }

    @AfterClass
    public void close() {
        driver.quit();
    }

}

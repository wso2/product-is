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

/**
 * Test adding a new group by admin
 */
public class AdminAddUserTest extends SelectDriver {

    private static WebDriver driver;
    private static String adminAddGroupPage = "https://" + System.getProperty("home") + ":" +
            System.getProperty("port") + "/admin-portal/groups/add";

    @BeforeClass
    public void init() {
        driver = selectDriver(System.getProperty("driver"));
    }

    @Test(groups = "AdminAddNewGroup")
    public void loadAddGroupPage() throws Exception {

        driver.get(adminAddGroupPage);
        Assert.assertEquals(driver.getCurrentUrl(), adminAddGroupPage, "Add group page is not loaded properly.");
    }

    @Test(groups = "AdminAddNewGroup",
          dependsOnMethods = "loadAddGroupPage")
    public void addGroupWithGeneralDetails() throws Exception {
        //Add a group with just group name and description.
    }

    @Test(groups = "AdminAddNewGroup",
          dependsOnMethods = "loadAddGroupPage")
    public void addGroupWithUsers() throws Exception {
        //Add a group with group name and description and assigning users in the same flow.
        //The created group should have the users assigned to the group. When users are listed the groups should be
        //shown as assigned to users.
    }

    @AfterClass
    public void close() {
        driver.quit();
    }
}

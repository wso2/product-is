/*
*Copyright (c) 2005-2014, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
*
*WSO2 Inc. licenses this file to you under the Apache License,
*Version 2.0 (the "License"); you may not use this file except
*in compliance with the License.
*You may obtain a copy of the License at
*
*http://www.apache.org/licenses/LICENSE-2.0
*
*Unless required by applicable law or agreed to in writing,
*software distributed under the License is distributed on an
*"AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
*KIND, either express or implied.  See the License for the
*specific language governing permissions and limitations
*under the License.
*/

package org.wso2.identity.ui.integration.test;

import org.testng.annotations.AfterClass;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;
import org.wso2.identity.integration.ui.pages.ISIntegrationUiBaseTest;
import org.wso2.identity.integration.ui.pages.login.LoginPage;
import org.wso2.identity.integration.ui.pages.main.HomePage;

public class LoginTestCase extends ISIntegrationUiBaseTest {

    @BeforeClass(alwaysRun = true, groups = "is.ui.tests")
    public void init() throws Exception {
        super.init();
        driver.get(getLoginURL());
    }

    @Test(groups = "is.ui.tests", description = "login to is server as admin")
    public void testLogin() throws Exception {
        driver.get(getLoginURL());
        LoginPage loginPage = new LoginPage(driver);
        HomePage homePage = loginPage.loginAs(isServer.getContextTenant().getContextUser().getUserName(),
                isServer.getContextTenant().getContextUser().getPassword());
        homePage.logout();
    }

    @AfterClass(alwaysRun = true, groups = "is.ui.tests")
    public void tearDown() {
        driver.quit();
    }
}
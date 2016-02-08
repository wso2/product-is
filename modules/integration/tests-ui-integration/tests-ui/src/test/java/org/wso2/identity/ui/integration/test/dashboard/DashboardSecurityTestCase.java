/*
 * Copyright (c) 2016, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
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

package org.wso2.identity.ui.integration.test.dashboard;

import org.openqa.selenium.By;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.testng.Assert;
import org.testng.annotations.AfterClass;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;
import org.wso2.carbon.automation.extensions.selenium.BrowserManager;
import org.wso2.identity.integration.common.utils.ISIntegrationUITest;

public class DashboardSecurityTestCase extends ISIntegrationUITest {
    private static final String LOGIN_PAGE_USERNAME_ELEMENT_ID = "username";
    private static final String LOGIN_PAGE_PASSWORD_ELEMENT_ID = "password";
    private static final String ADMIN_USERNAME = "admin";
    private static final String ADMIN_PASSWORD = "admin";
    private static final String JSESSIONID = "jsessionid";
    private static final String HTTPS = "https";
    private static final String LOGIN_BUTTON_XPATH = "//*[@id=\"loginForm\"]/div[4]/div[2]/button";
    private static final String DASHBOARD_CONTEXT_URL = "/dashboard";
    private WebDriver driver;

    @BeforeClass(alwaysRun = true)
    public void setUp() throws Exception {
        super.init();
        driver = BrowserManager.getWebDriver();
        driver.get(isServer.getContextUrls().getWebAppURL() + DASHBOARD_CONTEXT_URL);
    }

    @Test(groups = "wso2.identity", description = "Verify that cookie not get printed in dashboard page and switching" +
            " between HTTPS and HTTP are not possble after login.")
    public void testDashboardSecurityVulnerabilities() throws Exception {
        WebElement userNameField = driver.findElement(By.id(LOGIN_PAGE_USERNAME_ELEMENT_ID));
        WebElement passwordField = driver.findElement(By.id(LOGIN_PAGE_PASSWORD_ELEMENT_ID));
        userNameField.sendKeys(ADMIN_USERNAME);
        passwordField.sendKeys(ADMIN_PASSWORD);
        driver.findElement(By.xpath(LOGIN_BUTTON_XPATH)).click();
        String content = driver.getPageSource();
        Assert.assertFalse(content.toLowerCase().contains(JSESSIONID), "jseesion id is printing in the dashboard " +
                "index page after login.");
        driver.get(isServer.getContextUrls().getWebAppURL() + DASHBOARD_CONTEXT_URL);
        driver.get(isServer.getContextUrls().getWebAppURL() + DASHBOARD_CONTEXT_URL);
        Assert.assertTrue(driver.getCurrentUrl().startsWith(HTTPS), "Switching between HTTPS and HTTP after login " +
                "is possible.");
    }

    @AfterClass(alwaysRun = true)
    public void tearDown() throws Exception {
        driver.quit();
    }

}

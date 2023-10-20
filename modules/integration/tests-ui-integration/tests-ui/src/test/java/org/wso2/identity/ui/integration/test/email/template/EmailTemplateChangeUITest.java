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

package org.wso2.identity.ui.integration.test.email.template;

import org.openqa.selenium.By;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.support.ui.Select;
import org.testng.Assert;
import org.testng.annotations.*;
import org.wso2.carbon.automation.extensions.selenium.BrowserManager;
import org.wso2.identity.integration.common.ui.page.LoginPage;
import org.wso2.identity.integration.common.ui.page.main.HomePage;
import org.wso2.identity.integration.ui.pages.ISIntegrationUiBaseTest;

@Test
public class EmailTemplateChangeUITest extends ISIntegrationUiBaseTest {

    private static final String TEST_EMAIL_TYPE_PASSWORD = "Ask Password";
    private static final String TEST_EMAIL_BODY_ASK_PASSWORD = "Hi {first-name}\n\n\"\n\nPlease change your password for the newly created account : {user-name}. Please click the link below to create the password.\n\nhttps://localhost:8443/InfoRecoverySample/infoRecover/verify?confirmation={confirmation-code}&userstoredomain={userstore-domain}&username={url:user-name}&tenantdomain={tenant-domain}\n\nIf clicking the link doesn't seem to work, you can copy and paste the\nlink into your browser's address window.";

    @Test
    public void testChangeTemplateBody() throws Exception {
        driver = BrowserManager.getWebDriver();
        driver.get(getLoginURL());
        LoginPage loginPage = new LoginPage(driver);
        HomePage home = loginPage.loginAs(userInfo.getUserName(), userInfo.getPassword());

        //Check the value persisted in current session
        navigateToEmailTemplatePage();
        changeEmailTemplateBody(TEST_EMAIL_TYPE_PASSWORD, TEST_EMAIL_BODY_ASK_PASSWORD);
        verifyTheChangedBodyExists(TEST_EMAIL_TYPE_PASSWORD, TEST_EMAIL_BODY_ASK_PASSWORD);
        home.logout();

        //Check the value persisted in new session after re-login
        loginPage = new LoginPage(driver);
        home = loginPage.loginAs(userInfo.getUserName(), userInfo.getPassword());
        navigateToEmailTemplatePage();
        verifyTheChangedBodyExists(TEST_EMAIL_TYPE_PASSWORD, TEST_EMAIL_BODY_ASK_PASSWORD);
        home.logout();

        driver.close();
    }

    @BeforeClass
    protected void init() throws Exception {
        super.init();
        driver.close(); //We do not need this window, so closing it.
    }

    @AfterClass(alwaysRun = true)
    public void tearDown() throws Exception {
        driver.quit();
    }

    private void navigateToEmailTemplatePage() throws Exception {
        driver.get(getLoginURL()
                + "/identity-mgt/email-template-config.jsp?region=region1&item=identity_emailtemplate_menu");
        driver.findElement(By.cssSelector("#menu-panel-button3 > span")).click();
    }

    private void changeEmailTemplateBody(String selectLabel, String body) {
        driver.findElement(By.linkText("Email Templates")).click();
        new Select(driver.findElement(By.id("emailTypes"))).selectByVisibleText(selectLabel);
        driver.findElement(By.id("emailBody")).clear();
        driver.findElement(By.id("emailBody")).sendKeys(body);
        driver.findElement(By.cssSelector("input.button")).click();
    }

    private void verifyTheChangedBodyExists(String selectLabel, String expected) {
        driver.findElement(By.linkText("Email Templates")).click();
        new Select(driver.findElement(By.id("emailTypes"))).selectByVisibleText(selectLabel);
        WebElement textArea = driver.findElement(By.id("emailBody"));
        String text = textArea.getAttribute("value");

        Assert.assertEquals(text, expected, "The body displayed needs to be the one updated");
    }
}

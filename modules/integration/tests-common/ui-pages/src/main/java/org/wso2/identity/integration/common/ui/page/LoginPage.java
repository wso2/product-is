/*
*Copyright (c) 2005-2010, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
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

package org.wso2.identity.integration.common.ui.page;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.openqa.selenium.By;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.wso2.identity.integration.common.ui.page.main.HomePage;
import org.wso2.identity.integration.common.ui.page.util.UIElementMapper;

import java.io.IOException;

/**
 * login page class - contains methods to login to wso2 products.
 */
public class LoginPage {
    private static final Log log = LogFactory.getLog(LoginPage.class);
    private WebDriver driver;
    private boolean isCloudEnvironment = false;

    public LoginPage(WebDriver driver) throws IOException {
        this.driver = driver;

        // Check that we're on the right page.
        if (!(driver.getCurrentUrl().contains("login.jsp"))) {
            // Alternatively, we could navigate to the login page, perhaps logging out first
            throw new IllegalStateException("This is not the login page");
        }
    }

    public LoginPage(WebDriver driver, boolean isCloudEnvironment) throws IOException {
        this.driver = driver;
        this.isCloudEnvironment = isCloudEnvironment;
        // Check that we're on the right page.
        if (this.isCloudEnvironment) {
            if (!(driver.getCurrentUrl().contains("home/index.html"))) {
                // Alternatively, we could navigate to the login page, perhaps logging out first
                throw new IllegalStateException("This is not the cloud login page");
            }
            driver.findElement(By.xpath("//*[@id=\"content\"]/div[1]/div/a[2]/img")).click();
        } else {
            if (!(driver.getCurrentUrl().contains("login.jsp"))) {
                // Alternatively, we could navigate to the login page, perhaps logging out first
                throw new IllegalStateException("This is not the product login page");
            }
        }
    }

    public HomePage loginAs(String userName, String password, boolean isTenant)
            throws IOException {
        log.info("login as " + userName + ":Tenant");
        WebElement userNameField = driver.findElement(By.name(UIElementMapper.getInstance().getElement("login.username.name")));
        WebElement passwordField = driver.findElement(By.name(UIElementMapper.getInstance().getElement("login.password")));

        userNameField.sendKeys(userName);
        passwordField.sendKeys(password);
        if (isCloudEnvironment) {
            driver.findElement(
                    By.xpath("//*[@id=\"loginForm\"]/table/tbody/tr[4]/td[2]/input"))
                    .click();
            return new HomePage(isTenant, driver);
        } else {
            driver.findElement(
                    By.className(UIElementMapper.getInstance()
                            .getElement("login.sign.in.button"))).click();
            return new HomePage(isTenant, driver);
        }
    }

    public HomePage loginAs(String userName, String password) throws IOException {
        log.info("login as " + userName);
        WebElement userNameField = driver.findElement(By.name(UIElementMapper.getInstance().getElement("login.username.name")));
        WebElement passwordField = driver.findElement(By.name(UIElementMapper.getInstance().getElement("login.password")));

        userNameField.sendKeys(userName);
        passwordField.sendKeys(password);
        if (isCloudEnvironment) {
            driver.findElement(By.xpath("//*[@id=\"loginForm\"]/table/tbody/tr[4]/td[2]/input")).click();
            return new HomePage(driver, isCloudEnvironment);
        } else {
            driver.findElement(By.className(UIElementMapper.getInstance().getElement("login.sign.in.button"))).click();
            return new HomePage(driver);
        }
    }


}












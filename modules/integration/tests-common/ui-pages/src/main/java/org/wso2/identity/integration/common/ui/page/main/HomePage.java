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

package org.wso2.identity.integration.common.ui.page.main;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.openqa.selenium.By;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.wso2.identity.integration.common.ui.page.LoginPage;
import org.wso2.identity.integration.common.ui.page.util.UIElementMapper;

import java.io.IOException;
import java.util.Arrays;
import java.util.List;

/**
 * Home page class holds the information of product page you got once login
 * NOTE: To navigate to a page Don't use direct links to pages. To ensure there is a UI element to navigate to
 * that page.
 */
public class HomePage {

    private static final Log log = LogFactory.getLog(HomePage.class);
    private WebDriver driver;
    private boolean isCloudEnvironment = false;
    private boolean isTenant = false;

    public HomePage(WebDriver driver) throws IOException {
        this.driver = driver;
        // Check that we're on the right page.
        if (!driver.findElement(By.id(UIElementMapper.getInstance().getElement("home.dashboard.middle.text")))
                .getText().toLowerCase().contains("home")) {
            throw new IllegalStateException("This is not the home page");
        }
    }

    public HomePage(WebDriver driver, boolean isCloudEnvironment) throws IOException {
        this.driver = driver;
        this.isCloudEnvironment = isCloudEnvironment;
        if (isCloudEnvironment) {
            if (!driver.findElement(By.className("dashboard-title")).getText().toLowerCase().contains("quick start dashboard")) {
                throw new IllegalStateException("This is not the cloud home page");
            }
        } else {
            // Check that we're on the right page.
            if (!driver.findElement(By.id(UIElementMapper.getInstance().getElement("home.dashboard.middle.text")))
                    .getText().toLowerCase().contains("home")) {
                throw new IllegalStateException("This is not the home page");
            }
        }
    }

    public HomePage(boolean isTenant, WebDriver driver) throws IOException {
        this.isTenant = isTenant;
        if (this.isTenant) {
            if (!(driver.getCurrentUrl().contains("loginStatus=true"))) {
                throw new IllegalStateException("This is not the home page");
            }
        }
    }

    public LoginPage logout() throws IOException {
        driver.findElement(By.xpath(UIElementMapper.getInstance().getElement("home.greg.sign.out.xpath"))).click();
        return new LoginPage(driver, isCloudEnvironment);
    }

    private int findMenuItem(int startIndex, String name, List<WebElement> menuItems){
        for (int i = startIndex; i < menuItems.size(); i++) {
            WebElement item = menuItems.get(i);
            if (name.equals(item.getText())) {
                return i;
            }
        }
        return menuItems.size();
    }

    public WebDriver clickMenu(String... itemNames) {
        List<WebElement> menuItems = driver.findElements(By.cssSelector(".main li"));
        int index = 0;
        for (String itemName : itemNames) {
            index = findMenuItem(index, itemName, menuItems);

        }

        if (index < menuItems.size()) {
            menuItems.get(index).findElement(By.tagName("a")).click();
        } else {
            throw new IllegalStateException("Menu item with text '" + Arrays.toString(itemNames) + "' does not exits.");
        }
        return driver;
    }
}
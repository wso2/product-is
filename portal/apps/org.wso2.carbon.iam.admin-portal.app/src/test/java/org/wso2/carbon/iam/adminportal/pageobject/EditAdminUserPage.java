/*
 * Copyright (c) 2017, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.wso2.carbon.iam.adminportal.pageobject;

import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.support.CacheLookup;
import org.openqa.selenium.support.FindBy;
import org.openqa.selenium.support.PageFactory;

import java.util.Map;
/**
 * Page object for edit profile page.
 */
public class EditAdminUserPage {
    private Map<String, String> data;
    private WebDriver driver;
    private int timeout = 15;

    @FindBy(id = "image")
    @CacheLookup
    private WebElement image;

    @FindBy(id = "profileSelector")
    @CacheLookup
    private WebElement profileSelector;

    @FindBy(id = "default-email")
    @CacheLookup
    private WebElement email1;

    @FindBy(id = "employee-email")
    @CacheLookup
    private WebElement email2;

    @FindBy(id = "default-givenname")
    @CacheLookup
    private WebElement firstName1;

    @FindBy(id = "employee-givenname")
    @CacheLookup
    private WebElement firstName2;

    @FindBy(id = "default-lastname")
    @CacheLookup
    private WebElement lastName;

    @FindBy(id = "default-telephone")
    @CacheLookup
    private WebElement telephone;

    @FindBy(id = "btn-default")
    @CacheLookup
    private WebElement update1;

    @FindBy(id = "btn-employee")
    @CacheLookup
    private WebElement update2;

    @FindBy(id = "default-username")
    @CacheLookup
    private WebElement username1;

    @FindBy(id = "employee-username")
    @CacheLookup
    private WebElement username2;

    public WebDriver getDriver() {
        return driver;
    }

    public int getTimeout() {
        return timeout;
    }

    public WebElement getImage() {
        return image;
    }

    public WebElement getProfileSelector() {
        return profileSelector;
    }

    public WebElement getEmail1() {
        return email1;
    }

    public WebElement getEmail2() {
        return email2;
    }

    public WebElement getFirstName1() {
        return firstName1;
    }

    public WebElement getFirstName2() {
        return firstName2;
    }

    public WebElement getLastName() {
        return lastName;
    }

    public WebElement getTelephone() {
        return telephone;
    }

    public WebElement getUpdate1() {
        return update1;
    }

    public WebElement getUpdate2() {
        return update2;
    }

    public WebElement getUsername1() {
        return username1;
    }

    public WebElement getUsername2() {
        return username2;
    }

    public EditAdminUserPage() {
    }

    public EditAdminUserPage(WebDriver driver) {
        PageFactory.initElements(driver, this);
        this.driver = driver;
    }

}

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
    private WebElement defaultEmail;

    @FindBy(id = "employee-email")
    @CacheLookup
    private WebElement employeeEmail;

    @FindBy(id = "default-givenname")
    @CacheLookup
    private WebElement defaulFirstName;

    @FindBy(id = "employee-givenname")
    @CacheLookup
    private WebElement employeeFirstName;

    @FindBy(id = "default-lastname")
    @CacheLookup
    private WebElement defaultLastName;

    @FindBy(id = "default-telephone")
    @CacheLookup
    private WebElement defaultTelephone;

    @FindBy(id = "btn-default")
    @CacheLookup
    private WebElement defaultUpdate;


    @FindBy(id = "btn-employee")
    @CacheLookup
    private WebElement employeeUpdate;

    @FindBy(id = "default-username")
    @CacheLookup
    private WebElement defaultUsername;

    @FindBy(id = "employee-username")
    @CacheLookup
    private WebElement employeeUsername;

    public WebDriver getDriver() {
        return driver;
    }

    public WebElement getImage() {
        return image;
    }

    public WebElement getProfileSelector() {
        return profileSelector;
    }

    public WebElement getDefaultEmail() {
        return defaultEmail;
    }

    public WebElement getEmployeeEmail() {
        return employeeEmail;
    }

    public WebElement getDefaulFirstName() {
        return defaulFirstName;
    }

    public WebElement getEmployeeFirstName() {
        return employeeFirstName;
    }

    public WebElement getDefaultLastName() {
        return defaultLastName;
    }

    public WebElement getDefaultTelephone() {
        return defaultTelephone;
    }

    public WebElement getDefaultUpdate() {
        return defaultUpdate;
    }

    public WebElement getEmployeeUpdate() {
        return employeeUpdate;
    }

    public WebElement getDefaultUsername() {
        return defaultUsername;
    }

    public WebElement getEmployeeUsername() {
        return employeeUsername;
    }


    public EditAdminUserPage(WebDriver driver) {
        PageFactory.initElements(driver, this);
        this.driver = driver;
    }

}

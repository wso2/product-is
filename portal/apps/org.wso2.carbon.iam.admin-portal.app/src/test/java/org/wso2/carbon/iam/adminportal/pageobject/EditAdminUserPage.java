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
    private int timeout = Integer.valueOf(System.getProperty("timeout"));

    @FindBy(id = "image")
    @CacheLookup
    private WebElement imgImage;

    @FindBy(id = "profileSelector")
    @CacheLookup
    private WebElement optionProfileSelector;

    @FindBy(id = "default-email")
    @CacheLookup
    private WebElement txtboxDefaultEmail;

    @FindBy(id = "employee-email")
    @CacheLookup
    private WebElement txtboxEmployeeEmail;

    @FindBy(id = "default-givenname")
    @CacheLookup
    private WebElement txtboxDefaulFirstName;

    @FindBy(id = "employee-givenname")
    @CacheLookup
    private WebElement txtboxEmployeeFirstName;

    @FindBy(id = "default-lastname")
    @CacheLookup
    private WebElement txtboxDefaultLastName;

    @FindBy(id = "default-telephone")
    @CacheLookup
    private WebElement txtboxDefaultTelephone;

    @FindBy(id = "btn-default")
    @CacheLookup
    private WebElement btnDefaultUpdate;


    @FindBy(id = "btn-employee")
    @CacheLookup
    private WebElement btnEmployeeUpdate;

    @FindBy(id = "default-username")
    @CacheLookup
    private WebElement txtboxDefaultUsername;

    @FindBy(id = "employee-username")
    @CacheLookup
    private WebElement txtboxEmployeeUsername;


    public WebDriver getDriver() {
        return driver;
    }

    public WebElement getImgImage() {
        return imgImage;
    }

    public WebElement getOptionProfileSelector() {
        return optionProfileSelector;
    }

    public WebElement getTxtboxDefaultEmail() {
        return txtboxDefaultEmail;
    }

    public WebElement getTxtboxEmployeeEmail() {
        return txtboxEmployeeEmail;
    }

    public WebElement getTxtboxDefaulFirstName() {
        return txtboxDefaulFirstName;
    }

    public WebElement getTxtboxEmployeeFirstName() {
        return txtboxEmployeeFirstName;
    }

    public WebElement getTxtboxDefaultLastName() {
        return txtboxDefaultLastName;
    }

    public WebElement getTxtboxDefaultTelephone() {
        return txtboxDefaultTelephone;
    }

    public WebElement getBtnDefaultUpdate() {
        return btnDefaultUpdate;
    }

    public WebElement getBtnEmployeeUpdate() {
        return btnEmployeeUpdate;
    }

    public WebElement getTxtboxDefaultUsername() {
        return txtboxDefaultUsername;
    }

    public WebElement getTxtboxEmployeeUsername() {
        return txtboxEmployeeUsername;
    }

    public EditAdminUserPage(WebDriver driver) {
        PageFactory.initElements(driver, this);
        this.driver = driver;
    }

}

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
 * 'Add new user' page object for user on boarding by admin.
 */
public class AddNewUserPage {
    private Map<String, String> data;
    private WebDriver driver;
    private int timeout = Integer.valueOf(System.getProperty("timeout"));

    @FindBy(id = "domainSelector")
    @CacheLookup
    private WebElement domain;

    @FindBy(id = "inputUsername")
    @CacheLookup
    private WebElement username;

    @FindBy(id = "verificationSelector")
    @CacheLookup
    private WebElement selectMethod;

    @FindBy(id = "newPassword")
    @CacheLookup
    private WebElement password;

    @FindBy(id = "confirmPassword")
    @CacheLookup
    private WebElement confirmPassword;

    @FindBy(id = "addUser")
    @CacheLookup
    private WebElement addUser;

    public AddNewUserPage(WebDriver driver) {
        PageFactory.initElements(driver, this);
        this.driver = driver;
    }

    public AddNewUserPage(WebDriver driver, Map<String, String> data) {
        this(driver);
        this.data = data;
    }

    public AddNewUserPage(WebDriver driver, Map<String, String> data, int timeout) {
        this(driver, data);
        this.timeout = timeout;
    }

    public WebDriver getDriver() {
        return driver;
    }

    public WebElement getAddUser() {
        return addUser;
    }

    public WebElement getConfirmPassword() {
        return confirmPassword;
    }

    public WebElement getDomain() {
        return domain;
    }

    public WebElement getPassword() {
        return password;
    }

    public WebElement getSelectMethod() {
        return selectMethod;
    }

    public WebElement getUsername() {
        return username;
    }
}

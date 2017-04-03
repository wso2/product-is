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
 * Login page object for admin login page.
 */
public class AdminLoginPage {
    private Map<String, String> data;
    private WebDriver driver;
    private int timeout = Integer.valueOf(System.getProperty("timeout"));


    @FindBy(id = "domainSelector")
    @CacheLookup
    private WebElement optionDomain;

    @FindBy(id = "password")
    @CacheLookup
    private WebElement txtboxPassword;

    @FindBy(id = "forget-password")
    @CacheLookup
    private WebElement linkForgetPassword;

    @FindBy(id = "sign-in")
    @CacheLookup
    private WebElement btnSignIn;

    @FindBy(id = "username")
    @CacheLookup
    private WebElement txtboxUsername;

    @FindBy(id = "forget-username")
    @CacheLookup
    private WebElement linkForgetUsername;

    public WebDriver getDriver() {
        return driver;
    }

    public WebElement getOptionDomain() {
        return optionDomain;
    }

    public WebElement getTxtboxPassword() {
        return txtboxPassword;
    }

    public WebElement getLinkForgetPassword() {
        return linkForgetPassword;
    }

    public WebElement getBtnSignIn() {
        return btnSignIn;
    }

    public WebElement getTxtboxUsername() {
        return txtboxUsername;
    }

    public WebElement getLinkForgetUsername() {
        return linkForgetUsername;
    }

    public AdminLoginPage(WebDriver driver) {
        PageFactory.initElements(driver, this);
        this.driver = driver;
    }

    public AdminLoginPage(WebDriver driver, Map<String, String> data) {
        this(driver);
        this.data = data;
    }

    public AdminLoginPage(WebDriver driver, Map<String, String> data, int timeout) {
        this(driver, data);
        this.timeout = timeout;
    }

}

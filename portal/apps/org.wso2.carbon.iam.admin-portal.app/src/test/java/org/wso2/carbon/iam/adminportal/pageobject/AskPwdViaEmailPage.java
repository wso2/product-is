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

public class AskPwdViaEmailPage {
    private Map<String, String> data;
    private WebDriver driver;
    private int timeout = Integer.valueOf(System.getProperty("timeout"));

    @FindBy(id = "addUser")
    @CacheLookup
    private WebElement btnAddUser;

    @FindBy(id = "txtBoxAskPwdViaEmailDuplicate")
    @CacheLookup
    private WebElement txtBoxConfirmEmail;

    @FindBy(id = "domainSelector")
    @CacheLookup
    private WebElement optionDomainSelector;

    @FindBy(id = "txtBoxAskPwdViaEmail")
    @CacheLookup
    private WebElement txtBoxUsersEmail;

    @FindBy(id = "verificationSelector")
    @CacheLookup
    private WebElement optionSelectMethod;

    @FindBy(id = "inputUsername")
    @CacheLookup
    private WebElement txtBoxUsername;


    public AskPwdViaEmailPage(WebDriver driver) {
        PageFactory.initElements(driver, this);
        this.driver = driver;
    }

    public WebDriver getDriver() {
        return driver;
    }

    public int getTimeout() {
        return timeout;
    }

    public WebElement getBtnAddUser() {
        return btnAddUser;
    }

    public WebElement getTxtBoxConfirmEmail() {
        return txtBoxConfirmEmail;
    }

    public WebElement getOptionDomainSelector() {
        return optionDomainSelector;
    }

    public WebElement getTxtBoxUsersEmail() {
        return txtBoxUsersEmail;
    }

    public WebElement getOptionSelectMethod() {
        return optionSelectMethod;
    }

    public WebElement getTxtBoxUsername() {
        return txtBoxUsername;
    }
}

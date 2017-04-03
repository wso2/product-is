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
package org.wso2.carbon.iam.userportal.pageobject;

import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.support.CacheLookup;
import org.openqa.selenium.support.FindBy;
import org.openqa.selenium.support.PageFactory;

import java.util.Map;

public class UsernameRecoveryPage {

    private Map<String, String> data;
    private WebDriver driver;
    private int timeout = 15;

    @FindBy(id = "back-sign-in")
    @CacheLookup
    private WebElement backToSignIn;

    @FindBy(id = "email")
    @CacheLookup
    private WebElement email;

    @FindBy(id = "givenname")
    @CacheLookup
    private WebElement firstName;

    @FindBy(id = "lastname")
    @CacheLookup
    private WebElement lastName;

    @FindBy(id = "recover")
    @CacheLookup
    private WebElement recover;

    public UsernameRecoveryPage() {
    }

    public UsernameRecoveryPage(WebDriver driver) {
        PageFactory.initElements(driver, this);
        this.driver = driver;
    }

    public Map<String, String> getData() {
        return data;
    }

    public WebDriver getDriver() {
        return driver;
    }

    public int getTimeout() {
        return timeout;
    }

    public WebElement getBackToSignIn() {
        return backToSignIn;
    }

    public WebElement getEmail() {
        return email;
    }

    public WebElement getFirstName() {
        return firstName;
    }

    public WebElement getLastName() {
        return lastName;
    }

    public WebElement getRecover() {
        return recover;
    }

}

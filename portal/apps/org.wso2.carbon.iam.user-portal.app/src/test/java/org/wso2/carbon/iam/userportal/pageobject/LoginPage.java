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

/**
 * Created by wso2dinali on 22/3/17.
 */
public class LoginPage {

    private Map<String, String> data;
    private WebDriver driver;
    private int timeout = 15;


    @FindBy(id = "domainSelector")
    @CacheLookup
    private WebElement domain;

    @FindBy(id = "password")
    @CacheLookup
    private WebElement password1;

    @FindBy(id = "recover-password")
    @CacheLookup
    private WebElement password2;

    @FindBy(id = "sign-in")
    @CacheLookup
    private WebElement signIn;

    @FindBy(id = "sign-up")
    @CacheLookup
    private WebElement signUp;

    @FindBy(id = "username")
    @CacheLookup
    private WebElement username1;

    @FindBy(id = "recover-username")
    @CacheLookup
    private WebElement username2;


    public LoginPage() {
    }


    public LoginPage(WebDriver driver) {
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

    public WebElement getDomain() {
        return domain;
    }

    public WebElement getPassword1() {
        return password1;
    }

    public WebElement getPassword2() {
        return password2;
    }

    public WebElement getSignIn() {
        return signIn;
    }

    public WebElement getSignUp() {
        return signUp;
    }

    public WebElement getUsername1() {
        return username1;
    }

    public WebElement getUsername2() {
        return username2;
    }

}

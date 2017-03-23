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
import org.openqa.selenium.support.ui.Select;

import java.util.Map;

/**
 * Created by wso2dinali on 22/3/17.
 */
public class LoginPageP {

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

    public LoginPageP() {
    }


    public LoginPageP(WebDriver driver) {
        this();
        this.driver = driver;
    }

    public LoginPageP(WebDriver driver, Map<String, String> data) {
        this(driver);
        this.data = data;
    }

    public LoginPageP(WebDriver driver, Map<String, String> data, int timeout) {
        this(driver, data);
        this.timeout = timeout;
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


    /**
     * Set default value to Password Password field.
     *
     * @return the LoginPageP class instance.
     */
    public LoginPageP clickPassword1Link() {
        return clickPassword1Link(data.get("PASSWORD"));
    }

    /**
     * Click on Password Link.
     *
     * @return the LoginPageP class instance.
     */
    public LoginPageP clickPassword1Link(String passwordValue) {
        password1.sendKeys(passwordValue);
        return this;
    }

    /**
     * Click on Password Link.
     *
     * @return the LoginPageP class instance.
     */
//    public LoginPageP clickPassword2Link() {
//        password2.click();
//        return this;
//    }

    /**
     * Click on Sign In Button.
     *
     * @return the LoginPageP class instance.
     */
    public LoginPageP clickSignInButton() {
        signIn.click();
        return this;
    }

    /**
     * Click on Sign Up Link.
     *
     * @return the LoginPageP class instance.
     */
    public LoginPageP clickSignUpLink() {
        signUp.click();
        return this;
    }

    /**
     * Set default value to Username Text field.
     *
     * @return the LoginPageP class instance.
     */
    public LoginPageP clickUsername1Link() {
        return clickUsername1Link(data.get("USERNAME"));
    }

    /**
     * Click on Username Link.
     *
     * @return the LoginPageP class instance.
     */
    public LoginPageP clickUsername1Link(String usernameValue) {
        username1.sendKeys(usernameValue);
        return this;
    }

    /**
     * Click on Username Link.
     *
     * @return the LoginPageP class instance.
     */
    public LoginPageP clickUsername2Link() {
        username2.click();
        return this;
    }

    /**
     * Fill every fields in the page.
     *
     * @return the LoginPageP class instance.
     */
    public LoginPageP fill() {
        setDomainDropDownListField();
        clickUsername1Link();
        clickPassword1Link();
        return this;
    }

    /**
     * Fill every fields in the page and submit it to target page.
     *
     * @return the LoginPageP class instance.
     */
    public LoginPageP fillAndSubmit() {
        fill();
        return submit();
    }

    /**
     * Set default value to Domain Drop Down List field.
     *
     * @return the LoginPageP class instance.
     */
    public LoginPageP setDomainDropDownListField() {
        return setDomainDropDownListField(data.get("DOMAIN"));
    }

    /**
     * Set value to Domain Drop Down List field.
     *
     * @return the LoginPageP class instance.
     */
    public LoginPageP setDomainDropDownListField(String domainValue) {
        new Select(domain).selectByVisibleText(domainValue);
        return this;
    }

    /**
     * Submit the form to target page.
     *
     * @return the LoginPageP class instance.
     */
    public LoginPageP submit() {
        clickSignInButton();
        return this;
    }

    /**
     * Unset default value from Domain Drop Down List field.
     *
     * @return the LoginPageP class instance.
     */
    public LoginPageP unsetDomainDropDownListField() {
        return unsetDomainDropDownListField(data.get("DOMAIN"));
    }

    /**
     * Unset value from Domain Drop Down List field.
     *
     * @return the LoginPageP class instance.
     */
    public LoginPageP unsetDomainDropDownListField(String domainValue) {
        new Select(domain).deselectByVisibleText(domainValue);
        return this;
    }
}

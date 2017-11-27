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

import java.util.List;
import java.util.Map;

public class AdminInitiatedPasswordResetPage {
    private Map<String, String> data;
    private WebDriver driver;
    private int timeout = 15;

    @FindBy(css = "a.dropdown")
    @CacheLookup
    private WebElement admin;

    public Map<String, String> getData() {
        return data;
    }

    public WebDriver getDriver() {
        return driver;
    }

    public int getTimeout() {
        return timeout;
    }


    public WebElement getAdmin() {
        return admin;
    }

    public WebElement getAdminInitiatedPasswordResetViaEmail1() {
        return adminInitiatedPasswordResetViaEmail1;
    }

    public List<WebElement> getAdminInitiatedPasswordResetViaEmail2() {
        return adminInitiatedPasswordResetViaEmail2;
    }

    public String getAdminInitiatedPasswordResetViaEmailValue() {
        return adminInitiatedPasswordResetViaEmailValue;
    }

    public List<WebElement> getAdminInitiatedPasswordResetViaOffline() {
        return adminInitiatedPasswordResetViaOffline;
    }

    public String getAdminInitiatedPasswordResetViaOfflineValue() {
        return adminInitiatedPasswordResetViaOfflineValue;
    }

    public List<WebElement> getAdminInitiatedPasswordResetViaReset() {
        return adminInitiatedPasswordResetViaReset;
    }

    public String getAdminInitiatedPasswordResetViaResetValue() {
        return adminInitiatedPasswordResetViaResetValue;
    }

    public WebElement getGenerate() {
        return generate;
    }

    public WebElement getGroups() {
        return groups;
    }

    public WebElement getIdentityServerAdminPortal() {
        return identityServerAdminPortal;
    }


    public WebElement getInc() {
        return inc;
    }

    public void setInc(WebElement inc) {
        this.inc = inc;
    }

    public WebElement getLogout() {
        return logout;
    }

    public WebElement getOverview() {
        return overview;
    }

    public WebElement getRoles() {
        return roles;
    }

    public WebElement getUsers() {
        return users;
    }

    public void setUsers(WebElement users) {
        this.users = users;
    }

    @FindBy(id = "newPassword")
    @CacheLookup
    private WebElement adminInitiatedPasswordResetViaEmail1;

    @FindBy(name = "radio-group")
    @CacheLookup
    private List<WebElement> adminInitiatedPasswordResetViaEmail2;

    private final String adminInitiatedPasswordResetViaEmailValue = "on";

    @FindBy(name = "radio-group")
    @CacheLookup
    private List<WebElement> adminInitiatedPasswordResetViaOffline;

    private final String adminInitiatedPasswordResetViaOfflineValue = "on";

    @FindBy(name = "radio-group")
    @CacheLookup
    private List<WebElement> adminInitiatedPasswordResetViaReset;

    private final String adminInitiatedPasswordResetViaResetValue = "on";

    @FindBy(id = "genbtn")
    @CacheLookup
    private WebElement generate;

    @FindBy(css = "a[href='/admin-portal/groups']")
    @CacheLookup
    private WebElement groups;

    @FindBy(css = "a[href='/admin-portal/']")
    @CacheLookup
    private WebElement identityServerAdminPortal;

    @FindBy(css = "a[href='http://wso2.com/']")
    @CacheLookup
    private WebElement inc;

    @FindBy(css = "a[href='/admin-portal/logout']")
    @CacheLookup
    private WebElement logout;

    @FindBy(css = "#sidebar-theme div.nano. div.nano-content. ul.nav.nav-pills.nav-stacked.pages li:nth-of-type(1) a")
    @CacheLookup
    private WebElement overview;

    @FindBy(css = "#sidebar-theme div.nano. div.nano-content. ul.nav.nav-pills.nav-stacked.pages li:nth-of-type(4) a")
    @CacheLookup
    private WebElement roles;

    @FindBy(css = "a[href='/admin-portal/users']")
    @CacheLookup
    private WebElement users;

    public AdminInitiatedPasswordResetPage(WebDriver driver) {
        PageFactory.initElements(driver, this);
        this.driver = driver;
    }

    public AdminInitiatedPasswordResetPage(WebDriver driver, Map<String, String> data) {
        this(driver);
        this.data = data;
    }

    public AdminInitiatedPasswordResetPage(WebDriver driver, Map<String, String> data, int timeout) {
        this(driver, data);
        this.timeout = timeout;
    }

}

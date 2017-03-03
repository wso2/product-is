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

import org.openqa.selenium.By;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;

/**
 * Represents login page
 */
public class LoginPage {
    private static WebElement element = null;

    public static WebElement txtbxUsername(WebDriver driver) {
        element = driver.findElement(By.id("username"));
        return element;
    }

    public static WebElement txtbxPassword(WebDriver driver) {
        element = driver.findElement(By.id("password"));
        return element;
    }

    public static WebElement btnSignIn(WebDriver driver) {
        element = driver.findElement(By.id("sign-in"));
        return element;
    }

    public static WebElement lnkForgotPassword(WebDriver driver) {
        element = driver.findElement(By.id("recover-password"));
        return element;
    }

    public static WebElement lnkSignUp(WebDriver driver) {
        element = driver.findElement(By.id("sign-up"));
        return element;
    }

    public static WebElement spanSignInError(WebDriver driver) {
        element = driver.findElement(By.id("sign-in-error"));
        return element;
    }

    public static WebElement lnkForgotUsername(WebDriver driver) {
        element = driver.findElement(By.id("recover-username"));
        return element;
    }
}

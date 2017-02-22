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
 * Represents username recovery page.
 */
public class UsernameRecoveryPage {

    private static WebElement element = null;

    public static WebElement btnBackSignIn(WebDriver driver) {
        element = driver.findElement(By.id("back-sign-in"));
        return element;
    }

    public static WebElement btnRecover(WebDriver driver) {
        element = driver.findElement(By.id("recover"));
        return element;
    }

    public static WebElement txtbxClaimLabel(WebDriver driver, String label) {
        element = driver.findElement(By.id(label));
        return element;
    }
}

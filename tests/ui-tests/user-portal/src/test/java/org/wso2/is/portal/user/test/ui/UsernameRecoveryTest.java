/*
 * Copyright (c) 2017, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
 *
 * WSO2 Inc. licenses this file to you under the Apache License,
 * Version 2.0 (the "License"); you may not use this file except
 * in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */

package org.wso2.is.portal.user.test.ui;

import com.thoughtworks.selenium.Selenium;
import org.testng.Assert;
import org.testng.annotations.Test;
import org.openqa.selenium.By;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.wso2.carbon.iam.userportal.pageobject.UsernamerecoveryPage;

/**
 * UI Tests for username recovery .
 */
public class UsernameRecoveryTest {

    private Selenium selenium;
    UsernamerecoveryPage usernamerecoveryPage = new UsernamerecoveryPage();

    @Test(groups = "usernameRecoveryTest")
    public void testUsernameRecovery() throws Exception {
        Assert.assertTrue(true);
        selenium.open("https://localhost:9292/user-portal");

    }




}

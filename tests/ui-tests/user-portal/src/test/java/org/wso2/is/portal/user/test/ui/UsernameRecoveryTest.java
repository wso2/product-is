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

import org.openqa.selenium.WebDriver;
import org.testng.Assert;
import org.testng.annotations.AfterClass;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;
import org.wso2.carbon.iam.userportal.actionobject.UsernameRecoveryPageAction;
import org.wso2.carbon.identity.mgt.connector.Attribute;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * UI Tests for Username Recovery.
 */
public class UsernameRecoveryTest extends SelectDriver {

    private static UsernameRecoveryPageAction usernameRecoveryPageAction;

    private static WebDriver driver;

    private static String  loginPage = "https://" + System.getProperty("home") + ":" +
            System.getProperty("port") + "/user-portal/login";
    private static String usernameRecoveryPage = "https://" + System.getProperty("home")  + ":" +
            System.getProperty("port") + "/user-portal/recovery/username";

    @BeforeClass
    public void init() {
        driver = selectDriver(System.getProperty("driver"));
        usernameRecoveryPageAction = new UsernameRecoveryPageAction(driver);

    }

    @Test(groups = "usernameRecoveryTest")
    public void loadRecoveryPage() throws Exception {

        driver.get(usernameRecoveryPage);
        Assert.assertEquals(driver.getCurrentUrl(), usernameRecoveryPage,
                "This current page is not the username recovery page.");
    }

    @Test(groups = "usernameRecoveryTest", dependsOnMethods = "loadRecoveryPage")
    public void testUsernameRecovery() throws Exception {
        Map<String, String> attibuteMap = new HashMap<>();
        attibuteMap.put("givenname", "dinali");
        attibuteMap.put("lastname", "silva");
        attibuteMap.put("email", "dinali@wso2.com");
        List<Attribute> attributes = attibuteMap.entrySet().stream().map(entry -> {
            Attribute attribute = new Attribute();
            attribute.setAttributeName(entry.getKey());
            attribute.setAttributeValue(entry.getValue());
            return attribute;
        }).collect(Collectors.toList());

        boolean result = usernameRecoveryPageAction.recoverUsername(attributes);
        Assert.assertTrue(result, "The User clicked the recover button");
        Assert.assertEquals(driver.getCurrentUrl(), usernameRecoveryPage,
                "This current page is not the username recovery page.");
    }

    @Test(groups = "usernameRecoveryTest", dependsOnMethods = "testUsernameRecovery")
    public void backToSignIn() throws Exception {
        boolean result = usernameRecoveryPageAction.backToSignIn();
        Assert.assertTrue(result, "The user has not returned back to the sign in");
        Assert.assertEquals(driver.getCurrentUrl(), loginPage, "This current page is not the login page.");
    }

    @AfterClass
    public void close() {
        driver.quit();
    }
}

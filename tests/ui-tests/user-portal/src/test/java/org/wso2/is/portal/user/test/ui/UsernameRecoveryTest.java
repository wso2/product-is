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
import org.openqa.selenium.htmlunit.HtmlUnitDriver;
import org.testng.annotations.Test;
import org.wso2.carbon.iam.userportal.actionobject.UsernameRecoveryPageAction;
import org.wso2.carbon.identity.mgt.connector.Attribute;

import java.util.ArrayList;
import java.util.List;


/**
 * UI Tests for username recovery .
 */
public class UsernameRecoveryTest {

    private static UsernameRecoveryPageAction usernameRecoveryPageAction
            = new UsernameRecoveryPageAction();
    private static WebDriver driver = new HtmlUnitDriver();

    @Test(groups = "usernameRecoveryTest")
    public void loadUsernameRecoveryPage() throws Exception {
        driver.get("https://localhost:9292/user-portal/recovery/username");

    }
    @Test(groups = "usernameRecoveryTest", dependsOnMethods = "loadUsernameRecoveryPage")
    public void testUssernameRecovery() throws Exception {
        String[] claims = {"givenname", "lastname", "email"};
        String[] values = {"dinali", "dabarera", "dinali@wso2.com"};
        List<Attribute> attributes = new ArrayList<>();
        for (int count = 0; count < 3; count++) {
            Attribute attribute = new Attribute();
            attribute.setAttributeName(claims[count]);
            attribute.setAttributeValue(values[count]);
        }
        usernameRecoveryPageAction.recoverUsername(driver, attributes);
    }

    @Test(groups = "usernameRecoveryTest", dependsOnMethods = "loadUsernameRecoveryPage")
    public void backToSignIn() throws Exception {
       usernameRecoveryPageAction.backToSignIn(driver);
    }

}

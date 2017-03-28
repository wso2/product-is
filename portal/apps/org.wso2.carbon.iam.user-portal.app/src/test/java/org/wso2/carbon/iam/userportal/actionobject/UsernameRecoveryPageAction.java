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

package org.wso2.carbon.iam.userportal.actionobject;

import org.openqa.selenium.WebDriver;
import org.wso2.carbon.iam.userportal.pageobject.UsernameRecoveryPage;
import org.wso2.carbon.identity.mgt.connector.Attribute;

import java.util.List;

/**
 * Action class for recoverying username.
 */
public class UsernameRecoveryPageAction {


    WebDriver webDriver = null;
    public UsernameRecoveryPageAction(WebDriver driver) {
        webDriver = driver;
    }

    public boolean recoverUsername(List<Attribute> attributes) {
        UsernameRecoveryPage usernameRecoveryPage = new UsernameRecoveryPage(webDriver);
        boolean result = false;
        try {
            for (Attribute attribute: attributes) {
                if (attribute.getAttributeName() == "givenname") {
                    usernameRecoveryPage.getFirstName().sendKeys(attribute.getAttributeValue());
                } else if (attribute.getAttributeName() == "lastname") {
                    usernameRecoveryPage.getLastName().sendKeys(attribute.getAttributeValue());
                } else if (attribute.getAttributeName() == "email") {
                    usernameRecoveryPage.getEmail().sendKeys(attribute.getAttributeValue());
                }
            }
            usernameRecoveryPage.getRecover().click();
            result = true;
        } catch (Exception e) {
            System.out.print(e.getMessage());
            result = false;
        }
        return result;
    }

    public  boolean backToSignIn() {
        UsernameRecoveryPage usernameRecoveryPage = new UsernameRecoveryPage(webDriver);
        boolean result = false;
        try {
            usernameRecoveryPage.getBackToSignIn().click();
            result = true;
        } catch (Exception e) {
            System.out.print(e.getMessage());
            result = false;
        }
        return result;

    }

}

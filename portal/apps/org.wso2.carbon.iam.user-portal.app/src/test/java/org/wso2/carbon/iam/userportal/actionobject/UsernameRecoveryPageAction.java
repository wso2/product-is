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
public class UsernameRecoveryPageAction extends UsernameRecoveryPage {

    WebDriver webDriver = null;

    public UsernameRecoveryPageAction(WebDriver driver) {
        super(driver);
        webDriver = driver;
    }

    public boolean recoverUsername(List<Attribute> attributes) {
        for (Attribute attribute : attributes) {
            if (attribute.getAttributeName() == "givenname") {
                getFirstName().sendKeys(attribute.getAttributeValue());
            } else if (attribute.getAttributeName() == "lastname") {
                getLastName().sendKeys(attribute.getAttributeValue());
            } else if (attribute.getAttributeName() == "email") {
                getEmail().sendKeys(attribute.getAttributeValue());
            }
        }
        getRecover().click();
        return true;
    }

    public boolean backToSignIn() {
        getBackToSignIn().click();
        return true;
    }

}

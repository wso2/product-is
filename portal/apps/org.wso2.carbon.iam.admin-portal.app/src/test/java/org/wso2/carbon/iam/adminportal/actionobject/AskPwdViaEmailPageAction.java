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

package org.wso2.carbon.iam.adminportal.actionobject;

import org.openqa.selenium.WebDriver;
import org.openqa.selenium.support.ui.Select;
import org.wso2.carbon.iam.adminportal.pageobject.AskPwdViaEmailPage;

/**
 * Action object for Ask password via email page.
 */
public class AskPwdViaEmailPageAction extends AskPwdViaEmailPage {

    WebDriver webDriver = null;

    public AskPwdViaEmailPageAction(WebDriver driver) {
        super(driver);
        webDriver = driver;
    }

    public boolean addUserAskPwdViaEmail(String username, String email, String confirmEmail) {
        getTxtBoxUsername().sendKeys(username);
        Select dropdown = new Select(getOptionSelectMethod());
        dropdown.selectByVisibleText("With email verification and ask password from user");
        getTxtBoxUsersEmail().sendKeys(email);
        getTxtBoxConfirmEmail().sendKeys(confirmEmail);
        getBtnAddUser().click();
        return true;
    }

}

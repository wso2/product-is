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
import org.wso2.carbon.iam.adminportal.pageobject.EditAdminUserPage;

/**
 * Action Object for admin edit profile page.
 */
public class EditAdminUserPageAction extends EditAdminUserPage {
    WebDriver webDriver = null;

    public EditAdminUserPageAction(WebDriver driver) {
        super(driver);
        webDriver = driver;
    }

    public boolean updateDefaultProfile(String email) {
        getEmail().clear();
        getEmail().sendKeys(email);
        getUpdate1().click();
        return true;

    }

    public boolean selectEmployeeOption() {
        getProfileSelector().clear();
        getProfileSelector().click();
        getUpdate2().click();
        return true;
    }


}

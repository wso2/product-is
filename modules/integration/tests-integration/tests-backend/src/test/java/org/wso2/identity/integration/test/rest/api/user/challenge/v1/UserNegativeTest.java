/*
 * Copyright (c) 2019, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
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

package org.wso2.identity.integration.test.rest.api.user.challenge.v1;

import org.apache.axis2.AxisFault;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.DataProvider;
import org.testng.annotations.Factory;
import org.wso2.carbon.automation.engine.context.TestUserMode;

import java.rmi.RemoteException;
import java.util.Base64;
import javax.xml.xpath.XPathExpressionException;

public class UserNegativeTest extends UserMeNegativeTest {

    @Factory(dataProvider = "restAPIUserConfigProvider")
    public UserNegativeTest(TestUserMode userMode, String userName, String password, String role, String permission, boolean isAddUser) throws Exception {
        super(userMode, userName, password, role, permission, isAddUser);
        this.authenticatingUserName = tenantInfo.getContextUser().getUserName();
        this.authenticatingCredential = tenantInfo.getContextUser().getPassword();
    }

    @DataProvider(name = "restAPIUserConfigProvider")
    public static Object[][] restAPIUserConfigProvider() {
        return new Object[][]{
                {TestUserMode.SUPER_TENANT_USER, "restApiUser1", "Pass@123", "testLoginRole1",
                        "/permission/admin/login", true},
                {TestUserMode.TENANT_USER, "restApiUser1", "Pass@123", "testLoginRole1",
                        "/permission/admin/login", true}
        };
    }

    @BeforeClass(alwaysRun = true)
    @Override
    public void init() throws XPathExpressionException, RemoteException {

        super.testInit(API_VERSION, swaggerDefinition, tenant);
        String user = this.user;
        String userID = Base64.getEncoder().encodeToString(user.getBytes());
        initUrls(userID);
    }
}

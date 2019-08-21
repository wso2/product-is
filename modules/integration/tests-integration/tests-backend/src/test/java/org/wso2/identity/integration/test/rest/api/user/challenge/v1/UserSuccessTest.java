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
import org.wso2.carbon.utils.multitenancy.MultitenantUtils;

import java.util.Base64;
import javax.xml.xpath.XPathExpressionException;

/**
 * Test REST API for managing an specific user's challenge question answers
 */
public class UserSuccessTest extends UserMeSuccessTest {

    @Factory(dataProvider = "restAPIUserConfigProvider")
    public UserSuccessTest(TestUserMode userMode) throws Exception {
        super(userMode);
    }

    @BeforeClass(alwaysRun = true)
    @Override
    public void init() throws XPathExpressionException, AxisFault {

        super.testInit(API_VERSION, swaggerDefinition, tenant);
        String user = MultitenantUtils.getTenantAwareUsername(context.getContextTenant().getTenantUserList().get(0)
                .getUserName());
        String userID = Base64.getEncoder().encodeToString(user.getBytes());
        initUrls(userID);
    }

    @DataProvider(name = "restAPIUserConfigProvider")
    public static Object[][] restAPIUserConfigProvider() {
        return new Object[][]{
                {TestUserMode.SUPER_TENANT_ADMIN},
                {TestUserMode.TENANT_ADMIN}
        };
    }
}

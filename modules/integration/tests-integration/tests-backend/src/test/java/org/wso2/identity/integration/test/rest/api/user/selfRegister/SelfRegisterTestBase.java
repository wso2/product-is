/*
 * Copyright (c) 2022, WSO2 LLC. (http://www.wso2.com).
 *
 * WSO2 LLC. licenses this file to you under the Apache License,
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

package org.wso2.identity.integration.test.rest.api.user.selfRegister;

import org.testng.Assert;
import org.testng.annotations.BeforeClass;
import org.wso2.carbon.automation.engine.context.TestUserMode;
import org.wso2.identity.integration.test.rest.api.user.common.RESTAPIUserTestBase;

import java.io.IOException;

public class SelfRegisterTestBase extends RESTAPIUserTestBase {

    protected static final String API_DEFINITION_NAME_SELF_REGISTER = "api.identity.user.yaml";
    protected static String swaggerDefinitionSelfRegister;
    protected static String API_PACKAGE_NAME_SELF_REGISTER = "org.wso2.carbon.identity.api.user.governance";

    static {
        try {
            swaggerDefinitionSelfRegister =
                    getAPISwaggerDefinition(API_PACKAGE_NAME_SELF_REGISTER, API_DEFINITION_NAME_SELF_REGISTER);
        } catch (IOException e) {
            Assert.fail(String.format("Unable to read the swagger definition"), e);
        }
    }

    @BeforeClass(alwaysRun = true)
    protected void initiateSelfRegistration() throws Exception {

        super.init(TestUserMode.SUPER_TENANT_ADMIN);

        // Initialise required properties to update IDP properties.
        initUpdateIDPProperty();

        this.context = isServer;
        this.authenticatingUserName = context.getContextTenant().getTenantAdmin().getUserName();
        this.authenticatingCredential = context.getContextTenant().getTenantAdmin().getPassword();
        this.tenant = context.getContextTenant().getDomain();
    }
}

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

package org.wso2.identity.integration.test.rest.api.user.liteUserRegister;

import org.testng.Assert;
import org.testng.annotations.AfterClass;
import org.testng.annotations.BeforeClass;
import org.wso2.carbon.automation.engine.context.TestUserMode;
import org.wso2.carbon.integration.common.utils.mgt.ServerConfigurationManager;
import org.wso2.identity.integration.test.rest.api.user.common.RESTAPIUserTestBase;
import org.wso2.identity.integration.test.util.Utils;

import java.io.File;
import java.io.IOException;

public class LiteUserRegisterTestBase extends RESTAPIUserTestBase {

    static final String API_DEFINITION_NAME_LITE_USER_REGISTER = "api.identity.user.yaml";
    private static final String API_DEFINITION_NAME_UPDATE_CLAIM = "claim-management.yaml";
    protected static String swaggerDefinitionLiteUserRegister;
    protected static String swaggerDefinitionUpdateClaim;
    static String API_PACKAGE_NAME_LITE_USER_REGISTER = "org.wso2.carbon.identity.api.user.governance";
    private static final String API_PACKAGE_NAME_UPDATE_CLAIM = "org.wso2.carbon.identity.rest.api.server.claim.management.v1";
    private ServerConfigurationManager serverConfigurationManager;

    static {
        try {
            swaggerDefinitionLiteUserRegister = getAPISwaggerDefinition(API_PACKAGE_NAME_LITE_USER_REGISTER, API_DEFINITION_NAME_LITE_USER_REGISTER);
            swaggerDefinitionUpdateClaim = getAPISwaggerDefinition(API_PACKAGE_NAME_UPDATE_CLAIM, API_DEFINITION_NAME_UPDATE_CLAIM);
        } catch (IOException e) {
            Assert.fail(String.format("Unable to read the swagger definition"), e);
        }
    }

    @BeforeClass(alwaysRun = true)
    protected void restartServerAndInitialiseLiteUserRegistration() throws Exception {

        super.init(TestUserMode.SUPER_TENANT_ADMIN);
        String carbonHome = Utils.getResidentCarbonHome();
        File defaultTomlFile = getDeploymentTomlFile(carbonHome);
        File emailLoginConfigFile = new File(getISResourceLocation() + File.separator + "user"
                + File.separator + "enable_email_username_deployment.toml");
        serverConfigurationManager = new ServerConfigurationManager(isServer);
        serverConfigurationManager.applyConfigurationWithoutRestart(emailLoginConfigFile, defaultTomlFile, true);
        serverConfigurationManager.restartGracefully();

        super.init(TestUserMode.SUPER_TENANT_ADMIN);

        //initialise required properties to update IDP properties
        initUpdateIDPProperty();

        this.context = isServer;
        this.authenticatingUserName = context.getContextTenant().getTenantAdmin().getUserName();
        this.authenticatingCredential = context.getContextTenant().getTenantAdmin().getPassword();
        this.tenant = context.getContextTenant().getDomain();
    }

    @AfterClass(alwaysRun = true)
    protected void restoreServerConfig() throws Exception {

        serverConfigurationManager.restoreToLastConfiguration();
    }
}

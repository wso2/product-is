/*
 * Copyright (c) 2023, WSO2 LLC. (http://www.wso2.com).
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

import io.restassured.RestAssured;
import io.restassured.response.Response;
import org.apache.commons.lang.StringUtils;
import org.apache.http.HttpStatus;
import org.testng.Assert;
import org.testng.annotations.AfterMethod;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;
import org.wso2.carbon.automation.engine.context.TestUserMode;

public class SelfRegisterTenantedTestCase extends SelfRegisterTestBase {

    private String selfRegisterUserInfo;

    @BeforeClass(alwaysRun = true)
    public void init() throws Exception {

        super.init(TestUserMode.TENANT_ADMIN);

        // Initialise required properties to update IDP properties.
        initUpdateIDPProperty();

        this.context = isServer;
        this.authenticatingUserName = context.getContextTenant().getTenantAdmin().getUserName();
        this.authenticatingCredential = context.getContextTenant().getTenantAdmin().getPassword();
        this.tenant = context.getContextTenant().getDomain();

        super.testInit(API_VERSION_SELF_REGISTER, swaggerDefinitionSelfRegister, tenant,
                API_SELF_REGISTER_BASE_PATH_IN_SWAGGER, API_SELF_REGISTER_BASE_PATH);
        selfRegisterUserInfo = readResource("self-register-tenanted-user-request-body.json");
    }

    @AfterMethod(alwaysRun = true)
    public void endTest() throws Exception {

        updateResidentIDPProperty(ENABLE_SELF_SIGN_UP, "false", true);
        RestAssured.basePath = StringUtils.EMPTY;
    }

    @Test(
            alwaysRun = true,
            groups = "wso2.is",
            description = "Attempt self registration for tenanted user before enabling the functionality"
    )
    public void testSelfRegisterBeforeEnable() throws Exception {

        updateResidentIDPProperty(ENABLE_SELF_SIGN_UP, "false", true);
        Response responseOfPost = getResponseOfPost(SELF_REGISTRATION_ENDPOINT, selfRegisterUserInfo);
        Assert.assertEquals(responseOfPost.statusCode(), HttpStatus.SC_BAD_REQUEST, "Self register user could be enabled");
    }

    @Test(alwaysRun = true, groups = "wso2.is", description = "Create self registered tenanted user")
    public void testSelfRegister() throws Exception {

        updateResidentIDPProperty(ENABLE_SELF_SIGN_UP, "true", true);
        Response responseOfPost = getResponseOfPost(SELF_REGISTRATION_ENDPOINT, selfRegisterUserInfo);
        Assert.assertEquals(responseOfPost.statusCode(), HttpStatus.SC_CREATED, "Self register user unsuccessful" +
                responseOfPost.asString());
    }
}

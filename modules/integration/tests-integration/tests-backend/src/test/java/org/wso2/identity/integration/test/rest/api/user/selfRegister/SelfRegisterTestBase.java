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

import io.restassured.RestAssured;
import org.testng.Assert;
import org.testng.annotations.BeforeMethod;
import org.wso2.identity.integration.test.rest.api.user.common.RESTAPIUserTestBase;

import java.io.IOException;

public class SelfRegisterTestBase extends RESTAPIUserTestBase {

    protected static final String API_DEFINITION_NAME_SELF_REGISTER = "api.identity.user.yaml";
    protected static String swaggerDefinitionSelfRegister;
    protected static String API_PACKAGE_NAME_SELF_REGISTER = "org.wso2.carbon.identity.api.user.governance";
    public static final String ENABLE_SELF_SIGN_UP = "SelfRegistration.Enable";
    public static final String SELF_REGISTRATION_ENDPOINT = "/me";
    protected static final String API_SELF_REGISTER_BASE_PATH = "/api/identity/user/%s";
    protected static final String API_SELF_REGISTER_BASE_PATH_IN_SWAGGER =
            "/t/\\{tenant-domain\\}" + API_SELF_REGISTER_BASE_PATH;
    protected static final String API_VERSION_SELF_REGISTER = "v1.0";

    static {
        try {
            swaggerDefinitionSelfRegister =
                    getAPISwaggerDefinition(API_PACKAGE_NAME_SELF_REGISTER, API_DEFINITION_NAME_SELF_REGISTER);
        } catch (IOException e) {
            Assert.fail("Unable to read the swagger definition", e);
        }
    }

    @BeforeMethod(alwaysRun = true)
    public void testInit() throws Exception {

        RestAssured.basePath = basePath;
    }
}

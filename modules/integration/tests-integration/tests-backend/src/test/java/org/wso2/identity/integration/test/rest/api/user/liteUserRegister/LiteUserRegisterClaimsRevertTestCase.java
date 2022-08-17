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

import io.restassured.RestAssured;
import org.apache.commons.lang.StringUtils;
import org.testng.annotations.AfterMethod;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

/**
 * Revert the claims updated for lite user registration
 */
public class LiteUserRegisterClaimsRevertTestCase extends LiteUserRegisterTestBase{

    protected static final String API_CLAIM_UPDATE_BASE_PATH = "/api/server/%s";
    protected static final String API_CLAIM_UPDATE_BASE_PATH_IN_SWAGGER = "/t/\\{tenant-domain\\}" + API_CLAIM_UPDATE_BASE_PATH;
    protected static final String API_LITE_CLAIM_UPDATE_BASE_PATH_WITH_TENANT_CONTEXT = TENANT_CONTEXT_IN_URL + API_CLAIM_UPDATE_BASE_PATH;
    private static final String LITE_USER_CLAIM_ID = "aHR0cDovL3dzbzIub3JnL2NsYWltcy9pZGVudGl0eS9pc0xpdGVVc2Vy";
    private static final String USERNAME_CLAIM_ID = "aHR0cDovL3dzbzIub3JnL2NsYWltcy91c2VybmFtZQ";
    static final String API_VERSION_CLAIM = "v1";
    protected static final String API_CLAIMS_BASE_PATH = "/claim-dialects/local/claims/";

    @BeforeClass(alwaysRun = true)
    public void init() throws Exception {

        super.testInit(API_VERSION_CLAIM, swaggerDefinitionUpdateClaim, tenant, API_CLAIM_UPDATE_BASE_PATH_IN_SWAGGER, API_LITE_CLAIM_UPDATE_BASE_PATH_WITH_TENANT_CONTEXT);
    }

    @BeforeMethod(alwaysRun = true)
    public void testInit() throws Exception {

        RestAssured.basePath = basePath;
    }

    @AfterMethod(alwaysRun = true)
    public void endTest() throws Exception {

        RestAssured.basePath = StringUtils.EMPTY;
    }

    @Test(alwaysRun = true, groups = "wso2.is", description = "Update claims required for lite user registration")
    public void liteUserRegistrationClaimsUpdate() throws Exception {

        String updateLiteUserRegistrationClaimRequestBody = readResource("lite-user-register-claim-revert.json");
        getResponseOfPut(API_CLAIMS_BASE_PATH + LITE_USER_CLAIM_ID, updateLiteUserRegistrationClaimRequestBody);

        String updateEmailAsUsernameClaimRequestBody = readResource("lite-user-register-claim-email-as-username-revert.json");
        getResponseOfPut(API_CLAIMS_BASE_PATH + USERNAME_CLAIM_ID, updateEmailAsUsernameClaimRequestBody);
    }
}

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

import static io.restassured.RestAssured.given;
import io.restassured.http.ContentType;
import io.restassured.response.Response;
import java.io.File;
import java.io.IOException;
import org.apache.http.HttpHeaders;
import org.testng.Assert;
import org.testng.annotations.AfterClass;
import org.testng.annotations.BeforeClass;
import org.wso2.carbon.automation.engine.context.TestUserMode;
import org.wso2.carbon.integration.common.utils.mgt.ServerConfigurationManager;
import org.wso2.identity.integration.test.rest.api.user.common.RESTAPIUserTestBase;
import org.wso2.identity.integration.test.util.Utils;

/**
 * Base class for lite user register REST endpoint integration tests
 */
public class LiteUserRegisterTestBase extends RESTAPIUserTestBase {

    protected static final String API_USERNAME_CLAIM_PATH =
            "/api/server/v1/claim-dialects/local/claims/aHR0cDovL3dzbzIub3JnL2NsYWltcy91c2VybmFtZQ";
    protected static final String API_DEFINITION_NAME_LITE_USER_REGISTER = "api.identity.user.yaml";
    protected static final String ENABLE_EMAIL_USERNAME_DEPLOYMENT_CONFIG = "enable_email_username_deployment.toml";
    protected static final String LITE_USER_REGISTER_CLAIM_EMAIL_AS_USERNAME_JSON =
            "lite-user-register-claim-email-as-username.json";
    protected static final String LITE_USER_REGISTER_CLAIM_EMAIL_AS_USERNAME_REVERT_JSON =
            "lite-user-register-claim-email-as-username-revert.json";
    protected static String swaggerDefinitionLiteUserRegister;
    protected static String API_PACKAGE_NAME_LITE_USER_REGISTER = "org.wso2.carbon.identity.api.user.governance";

    static {
        try {
            swaggerDefinitionLiteUserRegister = getAPISwaggerDefinition(API_PACKAGE_NAME_LITE_USER_REGISTER,
                    API_DEFINITION_NAME_LITE_USER_REGISTER);
        } catch (IOException e) {
            Assert.fail(String.format("Unable to read the swagger definition %s from %s",
                    API_DEFINITION_NAME_LITE_USER_REGISTER, API_PACKAGE_NAME_LITE_USER_REGISTER), e);
        }
    }

    private String serverBackendUrl;
    private ServerConfigurationManager serverConfigurationManager;

    @BeforeClass(alwaysRun = true)
    protected void restartServerAndInitiateLiteUserRegistration() throws Exception {

        super.init(TestUserMode.SUPER_TENANT_ADMIN);
        String carbonHome = Utils.getResidentCarbonHome();
        File defaultConfigFile = getDeploymentTomlFile(carbonHome);
        File emailLoginConfigFile = new File(getISResourceLocation() + File.separator + "user" + File.separator +
                ENABLE_EMAIL_USERNAME_DEPLOYMENT_CONFIG);
        serverConfigurationManager = new ServerConfigurationManager(isServer);
        serverConfigurationManager.applyConfigurationWithoutRestart(emailLoginConfigFile, defaultConfigFile, true);
        serverConfigurationManager.restartGracefully();

        super.init(TestUserMode.SUPER_TENANT_ADMIN);

        // Initialise required properties to update IDP properties.
        initUpdateIDPProperty();

        this.context = isServer;
        this.authenticatingUserName = context.getContextTenant().getTenantAdmin().getUserName();
        this.authenticatingCredential = context.getContextTenant().getTenantAdmin().getPassword();
        this.tenant = context.getContextTenant().getDomain();

        // Update username related claim.
        serverBackendUrl = isServer.getContextUrls().getWebAppURLHttps();
        String updateEmailAsUsernameClaimRequestBody = readResource(LITE_USER_REGISTER_CLAIM_EMAIL_AS_USERNAME_JSON);
        sendPutRequest(serverBackendUrl + API_USERNAME_CLAIM_PATH, updateEmailAsUsernameClaimRequestBody);
    }

    @AfterClass(alwaysRun = true)
    protected void restoreServerConfig() throws Exception {

        String revertEmailAsUsernameClaimRequestBody =
                readResource(LITE_USER_REGISTER_CLAIM_EMAIL_AS_USERNAME_REVERT_JSON);
        sendPutRequest(serverBackendUrl + API_USERNAME_CLAIM_PATH, revertEmailAsUsernameClaimRequestBody);
        serverConfigurationManager.restoreToLastConfiguration(false);
    }

    protected Response sendPutRequest(String endpointUri, String body) {

        return given().auth().preemptive().basic(authenticatingUserName, authenticatingCredential)
                .contentType(ContentType.JSON).header(HttpHeaders.ACCEPT, ContentType.JSON).body(body).put(endpointUri);
    }
}

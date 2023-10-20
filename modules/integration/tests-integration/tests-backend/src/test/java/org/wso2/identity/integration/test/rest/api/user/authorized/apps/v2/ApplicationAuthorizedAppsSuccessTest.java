/*
 * Copyright (c) 2021, WSO2 Inc. (http://www.wso2.com).
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

package org.wso2.identity.integration.test.rest.api.user.authorized.apps.v2;

import io.restassured.RestAssured;
import io.restassured.response.Response;
import org.apache.commons.lang.StringUtils;
import org.apache.http.HttpStatus;
import org.testng.annotations.AfterClass;
import org.testng.annotations.AfterMethod;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.DataProvider;
import org.testng.annotations.Factory;
import org.testng.annotations.Test;
import org.wso2.carbon.automation.engine.context.TestUserMode;

import javax.xml.xpath.XPathExpressionException;
import java.rmi.RemoteException;
import java.util.HashMap;
import java.util.List;

import static org.wso2.carbon.automation.engine.context.TestUserMode.SUPER_TENANT_ADMIN;

/**
 * Test class for authorized apps application REST APIs success paths.
 */
public class ApplicationAuthorizedAppsSuccessTest extends UserAuthorizedAppsBaseTest {

    private String clientIdApp1;
    private String appName1;
    private String appId1;
    private static final String APP_ID_PREFIX = "CLIENT_";
    private static final String APP_NAME_PREFIX = "APP_";
    private static final String APP_ID_SUFFIX_1 = "_1";
    private static final String CLIENT_SECRET = "TEST_CLIENT_SECRET";

    @BeforeClass(alwaysRun = true)
    public void init() throws XPathExpressionException, RemoteException {

        super.testInit(API_VERSION, swaggerDefinition, tenant);
        initUrls("me");
        registerApplication(appName1, clientIdApp1, CLIENT_SECRET);
        getTokenFromPasswordGrant(clientIdApp1, CLIENT_SECRET);
    }

    @BeforeMethod(alwaysRun = true)
    public void testInit() {

        RestAssured.basePath = basePath;
    }

    @AfterMethod(alwaysRun = true)
    public void testFinish() {

        RestAssured.basePath = StringUtils.EMPTY;
    }

    @DataProvider(name = "restAPIUserConfigProvider")
    public static Object[][] restAPIUserConfigProvider() {

        return new Object[][]{
                {SUPER_TENANT_ADMIN},
                {TestUserMode.TENANT_ADMIN}
        };
    }

    @Factory(dataProvider = "restAPIUserConfigProvider")
    public ApplicationAuthorizedAppsSuccessTest(TestUserMode userMode) throws Exception {

        super.init(userMode);
        this.context = isServer;
        this.authenticatingUserName = context.getContextTenant().getTenantAdmin().getUserName();
        this.authenticatingCredential = context.getContextTenant().getTenantAdmin().getPassword();
        this.tenant = context.getContextTenant().getDomain();

        this.clientIdApp1 = APP_ID_PREFIX + userMode + APP_ID_SUFFIX_1;
        this.appName1 = APP_NAME_PREFIX + userMode + APP_ID_SUFFIX_1;
    }

    @Test
    public void testListAuthorizedApps() throws Exception {

        Response response = getResponseOfGet(this.userAuthorizedAppsEndpointUri);
        response.then()
                .assertThat()
                .statusCode(HttpStatus.SC_OK)
                .log().ifValidationFails();
        List<HashMap<String, String>> jresponse = response.jsonPath().getList("$");
        for (HashMap application : jresponse) {
            if (this.appName1.equals(application.get("name"))) {
                this.appId1 = (String) application.get("id");
            }
        }
    }

    @Test(dependsOnMethods = {"testListAuthorizedApps"})
    public void testDeleteIssuedTokensByAppId() throws Exception {

        String path = String.format(this.userApplicationEndpointUri, appId1);
        getResponseOfDelete(path)
                .then()
                .assertThat()
                .statusCode(HttpStatus.SC_NO_CONTENT)
                .log().ifValidationFails();
    }

    @AfterClass(alwaysRun = true)
    @Override
    public void testConclude() throws Exception {

        deleteApplication(clientIdApp1);
        super.conclude();
    }
}

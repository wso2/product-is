/*
 * Copyright (c) 2020, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
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

package org.wso2.identity.integration.test.scim2.rest.api;

import io.restassured.RestAssured;
import io.restassured.response.ExtractableResponse;
import io.restassured.response.Response;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.http.HttpHeaders;
import org.apache.http.HttpStatus;
import org.testng.Assert;
import org.testng.annotations.AfterClass;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.DataProvider;
import org.testng.annotations.Factory;
import org.testng.annotations.Test;
import org.wso2.carbon.automation.engine.context.TestUserMode;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.rmi.RemoteException;
import java.util.ArrayList;
import java.util.LinkedHashMap;

import static org.wso2.identity.integration.test.scim2.SCIM2BaseTestCase.USERS_ENDPOINT;

/**
 * Test cases for updating users via SCIM PATCH operations in a read-only user store.
 */
public class SCIMUserUpdateReadOnlyTest extends SCIM2BaseTest {

    private static final Log log = LogFactory.getLog(SCIMUserUpdateReadOnlyTest.class);

    private String endpointURL;
    private String userId = null;
    private static final String SCIM_CONTENT_TYPE = "application/scim+json";

    @Factory(dataProvider = "restAPIUserConfigProvider")
    public SCIMUserUpdateReadOnlyTest(TestUserMode userMode) throws Exception {

        super.init(userMode);
        this.context = isServer;
        this.authenticatingUserName = context.getContextTenant().getTenantAdmin().getUserName();
        this.authenticatingCredential = context.getContextTenant().getTenantAdmin().getPassword();
        this.tenant = context.getContextTenant().getDomain();
    }

    @DataProvider(name = "restAPIUserConfigProvider")
    public static Object[][] restAPIUserConfigProvider() {

        return new Object[][]{
                {TestUserMode.SUPER_TENANT_ADMIN},
                {TestUserMode.TENANT_ADMIN}
        };
    }

    @BeforeClass(alwaysRun = true)
    public void init() throws RemoteException, InterruptedException {

        super.testInit(swaggerDefinition, tenant);
        Thread.sleep(20000);
    }

    @AfterClass(alwaysRun = true)
    public void testFinish() {

        super.conclude();
    }

    @BeforeClass(alwaysRun = true)
    public void testInit() {

        RestAssured.basePath = basePath;
        super.testInit();
    }

    @Test
    public void testGetUsers() {

        endpointURL = USERS_ENDPOINT;
        Response response = getResponseOfGet(endpointURL, SCIM_CONTENT_TYPE);
        if (HttpStatus.SC_INTERNAL_SERVER_ERROR == response.getStatusCode()) {
            log.info(">>> Content: >>>" + response.getBody().prettyPrint());
        }
        ExtractableResponse<Response> extractableResponse = response.then()
                .log().ifValidationFails()
                .assertThat()
                .statusCode(HttpStatus.SC_OK)
                .and()
                .assertThat()
                .header(HttpHeaders.CONTENT_TYPE, SCIM_CONTENT_TYPE)
                .extract();
        Assert.assertNotNull(extractableResponse);

        Object resourcesAttribute = extractableResponse.path("Resources");
        userId = (String) ((LinkedHashMap) ((ArrayList) resourcesAttribute).get(0)).get("id");
        endpointURL = USERS_ENDPOINT + "/" + userId;
    }

    /*
     TODO: 1/22/20 Enable the below test once the following issues have been resolved.
     https://github.com/wso2/product-is/issues/7341
     https://github.com/wso2/product-is/issues/7342
    @Test(dependsOnMethods = "testGetUsers")
    public void testPatchUserIdentityClaim() throws IOException {

        String body = readResource("scim2-patch-replace-identity-claim.json");
        Response response = getResponseOfPatch(endpointURL, body, SCIM_CONTENT_TYPE);
        ExtractableResponse extractableResponse = response.then()
                .log().ifValidationFails()
                .assertThat()
                .statusCode(HttpStatus.SC_CREATED)
                .header(HttpHeaders.LOCATION, notNullValue())
                .and()
                .assertThat()
                .header(HttpHeaders.CONTENT_TYPE, SCIM_CONTENT_TYPE)
                .extract();

        Assert.assertNotNull(extractableResponse);
        SCIMUtils.validateSchemasAttribute(extractableResponse.path("schemas"));
        SCIMUtils.validateMetaAttribute(extractableResponse.path("meta"), response, endpointURL);
    }
    */
}

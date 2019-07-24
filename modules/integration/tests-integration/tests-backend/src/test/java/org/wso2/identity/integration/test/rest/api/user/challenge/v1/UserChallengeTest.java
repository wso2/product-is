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

import io.restassured.http.ContentType;
import org.apache.http.HttpHeaders;
import org.testng.annotations.AfterClass;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.DataProvider;
import org.testng.annotations.Factory;
import org.testng.annotations.Test;
import org.wso2.carbon.automation.engine.context.AutomationContext;
import org.wso2.carbon.automation.engine.context.TestUserMode;
import org.wso2.identity.integration.test.rest.api.user.common.RESTAPIUserTestBase;

import java.io.IOException;
import javax.xml.xpath.XPathExpressionException;

import static io.restassured.RestAssured.given;

/**
 * Test REST API for managing user's challenge questions
 */
public class UserChallengeTest extends RESTAPIUserTestBase {

    private static final String API_DEFINITION_NAME = "challenge.yaml";
    private static final String API_VERSION = "v1";
    private static String API_PACKAGE_NAME = "org.wso2.carbon.identity.rest.api.user.challenge.v1";

    @BeforeClass(alwaysRun = true)
    public void testInit() throws IOException, XPathExpressionException {

        super.testInit(API_VERSION, API_PACKAGE_NAME, API_DEFINITION_NAME, tenant);
    }

    @AfterClass(alwaysRun = true)
    public void testConclude() {
        super.conclude();
    }

    private String adminUsername;
    private String adminPassword;
    private String tenant;

    @Factory(dataProvider = "restAPIUserConfigProvider")
    public UserChallengeTest(TestUserMode userMode) throws Exception {

        context = new AutomationContext("IDENTITY", userMode);
        this.adminUsername = context.getContextTenant().getTenantAdmin().getUserName();
        this.adminPassword = context.getContextTenant().getTenantAdmin().getPassword();
        this.tenant = context.getContextTenant().getDomain();
    }

    @DataProvider(name = "restAPIUserConfigProvider")
    public static Object[][] restAPIUserConfigProvider() {
        return new Object[][]{
                {TestUserMode.SUPER_TENANT_ADMIN},
                {TestUserMode.TENANT_ADMIN}
        };
    }

    @Test
    public void testGetAvailableChallenges() {

        given().auth().preemptive().basic(adminUsername, adminPassword)
                .contentType(ContentType.JSON)
                .header(HttpHeaders.ACCEPT, ContentType.JSON)
                .log().all()
                .when()
                .filter(validationFilter)
                .when()
                .get("/me/challenges")
                .then()
                .assertThat()
                .statusCode(200);
    }


}

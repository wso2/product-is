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

package org.wso2.identity.integration.test.rest.api.user.association.v1;

import io.restassured.RestAssured;
import org.apache.axis2.AxisFault;
import org.apache.commons.lang.StringUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.http.HttpStatus;
import org.testng.annotations.AfterClass;
import org.testng.annotations.AfterMethod;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.DataProvider;
import org.testng.annotations.Factory;
import org.testng.annotations.Test;
import org.wso2.carbon.automation.engine.context.TestUserMode;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.Base64;
import javax.xml.xpath.XPathExpressionException;

import static org.hamcrest.CoreMatchers.hasItems;
import static org.hamcrest.core.Is.is;

/**
 * Test REST API for managing user associations.
 */
public class UserMeSuccessTestBase extends UserAssociationTestBase {

    private static final Log log = LogFactory.getLog(UserMeSuccessTestBase.class);
    private static final String TEST_USER = "TestUser";
    private static final String TEST_USER_PW = "Test123";
    private TestUserMode userMode;

    @Factory(dataProvider = "restAPIUserConfigProvider")
    public UserMeSuccessTestBase(TestUserMode userMode) throws Exception {
        super.init(userMode);
        this.userMode = userMode;
        this.context = isServer;
        this.authenticatingUserName = context.getContextTenant().getTenantAdmin().getUserName();
        this.authenticatingCredential = context.getContextTenant().getTenantAdmin().getPassword();
        this.tenant = context.getContextTenant().getDomain();
    }

    @BeforeClass(alwaysRun = true)
    public void init() throws XPathExpressionException, AxisFault {

        super.testInit(API_VERSION, swaggerDefinition, tenant);
        initUrls("me");

        try {
            createUser(TEST_USER, TEST_USER_PW, null);
        } catch (Exception e) {
            log.error("Error while creating the user :" + TEST_USER, e);
        }
    }

    @AfterClass(alwaysRun = true)
    public void testConclude() {

        super.conclude();

        try {
            deleteUser(TEST_USER);
        } catch (Exception e) {
            log.error("Error while deleting the user :" + TEST_USER, e);
        }
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
                {TestUserMode.SUPER_TENANT_ADMIN},
                {TestUserMode.TENANT_ADMIN}
        };
    }

    @Test
    public void testCreateAssociation() throws IOException {

        String body;
        if (TestUserMode.SUPER_TENANT_ADMIN.equals(userMode)) {
            body = readResource("association-creation.json");
        } else {
            body = readResource("association-creation-tenant.json").replace("TENANT", tenant);
        }
        getResponseOfPost(this.userAssociationEndpointURI, body)
                .then()
                .assertThat()
                .statusCode(HttpStatus.SC_CREATED)
                .log().ifValidationFails();
    }

    @Test(dependsOnMethods = {"testCreateAssociation"})
    public void testGetAssociations() {

        getResponseOfGet(this.userAssociationEndpointURI)
                .then()
                .assertThat()
                .statusCode(HttpStatus.SC_OK)
                .log().ifValidationFails()
                .body("size()", is(1))
                .body("userId", hasItems(Base64.getUrlEncoder().encodeToString(("PRIMARY/" + TEST_USER).getBytes(
                        StandardCharsets.UTF_8))));
    }

    @Test(dependsOnMethods = {"testGetAssociations"})
    public void testRemoveAssociations() {

        getResponseOfDelete(this.userAssociationEndpointURI)
                .then()
                .log().ifValidationFails()
                .assertThat()
                .statusCode(HttpStatus.SC_NO_CONTENT);
    }

    protected void createUser(String username, String password, String[] roles) throws Exception {

        log.info("Creating User " + username);
        remoteUSMServiceClient.addUser(username, password, roles, null, null, true);
    }

    protected void deleteUser(String username) throws Exception {

        log.info("Deleting User " + username);
        remoteUSMServiceClient.deleteUser(username);
    }
}

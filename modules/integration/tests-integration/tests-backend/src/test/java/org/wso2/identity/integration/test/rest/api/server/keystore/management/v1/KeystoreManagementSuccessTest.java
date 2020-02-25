/*
 * Copyright (c) 2019, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
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
package org.wso2.identity.integration.test.rest.api.server.keystore.management.v1;

import io.restassured.RestAssured;
import io.restassured.response.Response;
import org.apache.commons.lang.StringUtils;
import org.apache.http.HttpStatus;
import org.testng.Assert;
import org.testng.annotations.AfterClass;
import org.testng.annotations.AfterMethod;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.DataProvider;
import org.testng.annotations.Factory;
import org.testng.annotations.Test;
import org.wso2.carbon.automation.engine.context.TestUserMode;
import org.wso2.carbon.utils.multitenancy.MultitenantConstants;

import java.io.IOException;
import java.net.URISyntaxException;
import java.util.HashMap;
import java.util.List;

/**
 * Tests for successful cases of the Keystore Management REST API.
 */
public class KeystoreManagementSuccessTest extends KeystoreManagementBaseTest {

    private static final String SUPER_TENANT_PUBLIC_CERT_ALIAS = "wso2carbon";

    @Factory(dataProvider = "restAPIUserConfigProvider")
    public KeystoreManagementSuccessTest(TestUserMode userMode) throws Exception {

        super.init(userMode);
        this.context = isServer;
        this.authenticatingUserName = context.getContextTenant().getTenantAdmin().getUserName();
        this.authenticatingCredential = context.getContextTenant().getTenantAdmin().getPassword();
        this.tenant = context.getContextTenant().getDomain();
    }

    @BeforeClass(alwaysRun = true)
    public void init() throws IOException {

        super.testInit(API_VERSION, swaggerDefinition, tenant);
    }

    @AfterClass(alwaysRun = true)
    public void testConclude() {

        if (!StringUtils.equals(MultitenantConstants.SUPER_TENANT_DOMAIN_NAME, tenant)) {
            getResponseOfDelete(KEYSTORE_MANAGEMENT_API_BASE_PATH +
                            KEYSTORE_MANAGEMENT_API_CERTIFICATE_PATH + PATH_SEPARATOR + "newcert");
        }
        super.conclude();
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
    public void testGetAllCertificateAliases() throws IOException, URISyntaxException {

        Response response = getResponseOfGet(KEYSTORE_MANAGEMENT_API_BASE_PATH +
                KEYSTORE_MANAGEMENT_API_CERTIFICATE_PATH);
        validateHttpStatusCode(response, HttpStatus.SC_OK);

        List<String> aliasList = response.jsonPath().getList("alias");
        if (StringUtils.equals(MultitenantConstants.SUPER_TENANT_DOMAIN_NAME, tenant)) {
            Assert.assertTrue(aliasList.contains(SUPER_TENANT_PUBLIC_CERT_ALIAS), "Public certificate alias " +
                    SUPER_TENANT_PUBLIC_CERT_ALIAS + " is not returned in the response.");
        } else {
            Assert.assertTrue(aliasList.contains(tenant), "Public certificate alias " + tenant +
                    " is not returned in the response.");
        }
    }

    @Test
    public void testGetCertificate() {

        String alias;
        if (StringUtils.equals(MultitenantConstants.SUPER_TENANT_DOMAIN_NAME, tenant)) {
            alias = SUPER_TENANT_PUBLIC_CERT_ALIAS;
        } else {
            alias = tenant;
        }

        Response response = getResponseOfGet(KEYSTORE_MANAGEMENT_API_BASE_PATH +
                KEYSTORE_MANAGEMENT_API_CERTIFICATE_PATH + PATH_SEPARATOR + alias, "application/pkix-cert");
        validateHttpStatusCode(response, HttpStatus.SC_OK);
        Assert.assertNotNull(response.asString());
    }

    @Test
    public void testGetAllClientCertificateAliases() throws IOException {

        if (StringUtils.equals(MultitenantConstants.SUPER_TENANT_DOMAIN_NAME, tenant)) {
            Response response = getResponseOfGet(KEYSTORE_MANAGEMENT_API_BASE_PATH +
                    KEYSTORE_MANAGEMENT_API_CLIENT_CERTIFICATE_PATH);
            validateHttpStatusCode(response, HttpStatus.SC_OK);

            List<String> aliasList = response.jsonPath().getList("alias");
            Assert.assertTrue(aliasList.contains(SUPER_TENANT_PUBLIC_CERT_ALIAS), "Public certificate alias " +
                    SUPER_TENANT_PUBLIC_CERT_ALIAS + " is not returned in the response.");
        }

    }

    @Test
    public void testGetClientCertificate() {

        if (StringUtils.equals(MultitenantConstants.SUPER_TENANT_DOMAIN_NAME, tenant)) {
            Response response = getResponseOfGet(KEYSTORE_MANAGEMENT_API_BASE_PATH +
                            KEYSTORE_MANAGEMENT_API_CERTIFICATE_PATH + PATH_SEPARATOR + SUPER_TENANT_PUBLIC_CERT_ALIAS,
                    "application/pkix-cert");
            validateHttpStatusCode(response, HttpStatus.SC_OK);
            Assert.assertNotNull(response.asString());
        }
    }

    @Test
    public void testGetPublicCertificate() {

        Response response = getResponseOfGetWithoutAuthentication(KEYSTORE_MANAGEMENT_API_BASE_PATH +
                KEYSTORE_MANAGEMENT_API_CERTIFICATE_PATH +
                KEYSTORE_MANAGEMENT_API_PUBLIC_CERTIFICATE_PATH, "application/pkix-cert");
        validateHttpStatusCode(response, HttpStatus.SC_OK);
        Assert.assertNotNull(response.asString());
    }

    @Test
    public void testAddCertificate() throws IOException {

        if (!StringUtils.equals(MultitenantConstants.SUPER_TENANT_DOMAIN_NAME, tenant)) {
            Response response = getResponseOfJSONPost(KEYSTORE_MANAGEMENT_API_BASE_PATH +
                            KEYSTORE_MANAGEMENT_API_CERTIFICATE_PATH, readResource("cert-request-body1.json"),
                    new HashMap<>());
            validateHttpStatusCode(response, HttpStatus.SC_CREATED);
            Assert.assertNotNull(response.getCookie("Location"));
        }
    }

    @Test(dependsOnMethods = "testAddCertificate")
    public void testDeleteCertificate() {

        if (!StringUtils.equals(MultitenantConstants.SUPER_TENANT_DOMAIN_NAME, tenant)) {
            Response response =
                    getResponseOfDelete(KEYSTORE_MANAGEMENT_API_BASE_PATH +
                            KEYSTORE_MANAGEMENT_API_CERTIFICATE_PATH + PATH_SEPARATOR + "newcert");
            validateHttpStatusCode(response, HttpStatus.SC_NO_CONTENT);
        }
    }
}

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
import org.json.JSONException;
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
import java.util.HashMap;

/**
 * Tests for failure cases of the Keystore Management REST API.
 */
public class KeystoreManagementFailureTest extends KeystoreManagementBaseTest {

    private static final String INVALID_ALIAS = "test-alias";

    @Factory(dataProvider = "restAPIUserConfigProvider")
    public KeystoreManagementFailureTest(TestUserMode userMode) throws Exception {

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
    public void testGetAllClientCertificateAliasesForTenant() {

        if (!StringUtils.equals(MultitenantConstants.SUPER_TENANT_DOMAIN_NAME, tenant)) {
            Response response = getResponseOfGet(KEYSTORE_MANAGEMENT_API_BASE_PATH +
                    KEYSTORE_MANAGEMENT_API_CLIENT_CERTIFICATE_PATH);
            validateHttpStatusCode(response, HttpStatus.SC_NOT_FOUND);
        }
    }

    @Test
    public void testAddCertificateForSuperTenant() throws IOException {

        if (StringUtils.equals(MultitenantConstants.SUPER_TENANT_DOMAIN_NAME, tenant)) {
            Response response = getResponseOfJSONPost(KEYSTORE_MANAGEMENT_API_BASE_PATH +
                            KEYSTORE_MANAGEMENT_API_CERTIFICATE_PATH, readResource("cert-request-body1.json"),
                    new HashMap<>());
            validateHttpStatusCode(response, HttpStatus.SC_METHOD_NOT_ALLOWED);
        }
    }

    @Test
    public void testDeleteCertificateForSuperTenant() {

        if (StringUtils.equals(MultitenantConstants.SUPER_TENANT_DOMAIN_NAME, tenant)) {
            Response response =
                    getResponseOfDelete(KEYSTORE_MANAGEMENT_API_BASE_PATH +
                            KEYSTORE_MANAGEMENT_API_CERTIFICATE_PATH + PATH_SEPARATOR + "wso2carbon");
            validateHttpStatusCode(response, HttpStatus.SC_METHOD_NOT_ALLOWED);
        }
    }

    @Test
    public void testGetCertificateWithInvalidAlias() {

        Response response = getResponseOfGet(KEYSTORE_MANAGEMENT_API_BASE_PATH +
                KEYSTORE_MANAGEMENT_API_CERTIFICATE_PATH + PATH_SEPARATOR + INVALID_ALIAS, "application/pkix-cert");
        validateErrorResponse(response, HttpStatus.SC_BAD_REQUEST, "KSS-60010");
    }

    @Test
    public void testAddExistingCertificate() throws IOException {

        if (!StringUtils.equals(MultitenantConstants.SUPER_TENANT_DOMAIN_NAME, tenant)) {
            Response response = getResponseOfGet(KEYSTORE_MANAGEMENT_API_BASE_PATH +
                    KEYSTORE_MANAGEMENT_API_CERTIFICATE_PATH + PATH_SEPARATOR + tenant +
                    ENCODE_CERT_QUERY_PARAMETER + "true", "application/pkix-cert");
            validateHttpStatusCode(response, HttpStatus.SC_OK);

            String certificateRequest = String.format(readResource("cert-request-body2.json"),
                    response.asString());
            response = getResponseOfJSONPost(KEYSTORE_MANAGEMENT_API_BASE_PATH +
                    KEYSTORE_MANAGEMENT_API_CERTIFICATE_PATH, certificateRequest, new HashMap<>());
            validateErrorResponse(response, HttpStatus.SC_BAD_REQUEST, "KSS-60001");
        }
    }

    @Test
    public void testAddCertificateWithExistingAlias() throws JSONException, IOException {

        if (!StringUtils.equals(MultitenantConstants.SUPER_TENANT_DOMAIN_NAME, tenant)) {
            String certificateRequest = String.format(readResource("cert-request-body3.json"), tenant);
            Response response = getResponseOfJSONPost(KEYSTORE_MANAGEMENT_API_BASE_PATH +
                    KEYSTORE_MANAGEMENT_API_CERTIFICATE_PATH, certificateRequest, new HashMap<>());
            validateErrorResponse(response, HttpStatus.SC_BAD_REQUEST, "KSS-60002");
        }
    }

    @Test
    public void testGetAllCertificatesWithInvalidFilter() {

        Response response = getResponseOfGet(KEYSTORE_MANAGEMENT_API_BASE_PATH +
                KEYSTORE_MANAGEMENT_API_CERTIFICATE_PATH + FILTER_QUERY_PARAMETER + "alias");
        validateErrorResponse(response, HttpStatus.SC_BAD_REQUEST, "KSS-60003");
    }

    @Test
    public void testGetAllCertificatesWithInvalidFilterOpertation() {

        Response response = getResponseOfGet(KEYSTORE_MANAGEMENT_API_BASE_PATH +
                KEYSTORE_MANAGEMENT_API_CERTIFICATE_PATH + FILTER_QUERY_PARAMETER + "alias+xx+wso2");
        validateErrorResponse(response, HttpStatus.SC_BAD_REQUEST, "KSS-60004");
    }
}

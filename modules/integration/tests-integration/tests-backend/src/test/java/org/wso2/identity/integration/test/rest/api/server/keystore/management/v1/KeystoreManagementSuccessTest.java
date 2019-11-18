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

import com.fasterxml.jackson.core.JsonFactory;
import com.fasterxml.jackson.databind.ObjectMapper;
import io.restassured.RestAssured;
import io.restassured.response.Response;
import org.apache.commons.lang.StringUtils;
import org.apache.cxf.phase.PhaseInterceptorChain;
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
import org.wso2.identity.integration.test.rest.api.server.keystore.management.v1.model.CertificateResponse;

import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.Arrays;
import java.util.List;

/**
 *
 */
public class KeystoreManagementSuccessTest extends KeystoreManagementBaseTest {

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
    public void getAllCertificateAliases() throws IOException, URISyntaxException {

        Response response = getResponseOfGet(KEYSTORE_MANAGEMENT_API_BASE_PATH +
                KEYSTORE_MANAGEMENT_API_CERTIFICATE_PATH);
        response.then()
                .log().ifValidationFails()
                .assertThat()
                .statusCode(HttpStatus.SC_OK);
        ObjectMapper jsonWriter = new ObjectMapper(new JsonFactory());
        List<CertificateResponse> responseFound =
                Arrays.asList(jsonWriter.readValue(response.asString(), CertificateResponse[].class));
        Assert.assertNotNull(responseFound);
        if (StringUtils.equals(MultitenantConstants.SUPER_TENANT_DOMAIN_NAME, tenant)) {
            Assert.assertTrue(responseFound.stream().anyMatch(certificateResponse -> certificateResponse.getAlias()
                    .equals("wso2carbon")), "message");
        } else {
            Assert.assertTrue(responseFound.stream().anyMatch(certificateResponse -> certificateResponse.getAlias()
                    .equals(tenant)), "message");
        }
    }

    @Test
    public void getCertificate() {

        String alias = "wso2carbon";
        Response response = getResponseOfGet(KEYSTORE_MANAGEMENT_API_BASE_PATH +
                KEYSTORE_MANAGEMENT_API_CERTIFICATE_PATH + PATH_SEPARATOR + alias);
        response.then()
                .log().ifValidationFails()
                .assertThat()
                .statusCode(HttpStatus.SC_OK);

    }

    @Test
    public void getClientCertificateAliases() {


    }

    @Test
    public void getClientCertificate() {

    }

    @Test
    public void getPublicCertificate() {

    }

    @Test
    public void addCertificate() {

    }

    @Test
    public void deleteCertificate() {

    }
}

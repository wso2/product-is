/*
 * Copyright (c) 2025, WSO2 LLC. (http://www.wso2.com).
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

package org.wso2.identity.integration.test.rest.api.server.certificate.validation.management.v1.cacertificates;

import io.restassured.RestAssured;
import io.restassured.response.Response;
import org.apache.http.HttpStatus;
import org.testng.annotations.AfterClass;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Factory;
import org.testng.annotations.Test;
import org.wso2.carbon.automation.engine.context.TestUserMode;
import org.wso2.carbon.integration.common.utils.mgt.ServerConfigurationManager;
import org.wso2.identity.integration.test.rest.api.server.certificate.validation.management.v1.cacertificates.model.CACertificateAddRequest;
import org.wso2.identity.integration.test.util.Utils;

import java.io.File;

import static org.hamcrest.CoreMatchers.equalTo;
import static org.hamcrest.CoreMatchers.notNullValue;

/**
 * Tests for happy paths of the Certificate Validation Management REST API CA Certificates.
 */
public class CACertificatesSuccessTest extends CACertificatesTestBase {

    private static CACertificateAddRequest cacertificate;
    private static String testCACertificateId;
    private ServerConfigurationManager serverConfigurationManager;
    private File defaultConfigFile;

    @Factory(dataProvider = "restAPIUserConfigProvider")
    public CACertificatesSuccessTest(TestUserMode userMode) throws Exception {

        super.init(userMode);
        this.context = isServer;
        this.authenticatingUserName = context.getContextTenant().getTenantAdmin().getUserName();
        this.authenticatingCredential = context.getContextTenant().getTenantAdmin().getPassword();
        this.tenant = context.getContextTenant().getDomain();
    }

    @BeforeClass(alwaysRun = true)
    @Override
    public void init() throws Exception {

        super.testInit(API_VERSION, swaggerDefinition, tenant);
        super.init();
        String carbonHome = Utils.getResidentCarbonHome();
        defaultConfigFile = getDeploymentTomlFile(carbonHome);
        File challengeQuestionsConfigFile = new File(
                getISResourceLocation() + File.separator + "certificate-validation-mgt" + File.separator +
                        ADD_CA_CERTIFICATE_VALIDATION_CONFIG);
        serverConfigurationManager = new ServerConfigurationManager(isServer);
        serverConfigurationManager.applyConfigurationWithoutRestart(challengeQuestionsConfigFile, defaultConfigFile,
                true);
        serverConfigurationManager.restartGracefully();
        serverConfigurationManager.restoreToLastConfiguration(false);
    }

    @AfterClass(alwaysRun = true)
    @Override
    public void testConclude() throws Exception {

        super.conclude();
    }

    @BeforeMethod(alwaysRun = true)
    @Override
    public void testInit() {

        RestAssured.basePath = basePath;
    }

    @Test
    public void testAddCACertificate() {

        cacertificate = new CACertificateAddRequest()
                .certificate(TEST_CERTIFICATE);

        String body = toJSONString(cacertificate);
        Response responseOfPost = getResponseOfPost(CERTIFICATE_VALIDATION_API_BASE_PATH +
                CA_CERTIFICATES_PATH, body);
        responseOfPost.then()
                .log().ifValidationFails()
                .assertThat()
                .statusCode(HttpStatus.SC_CREATED)
                .body("id", notNullValue())
                .body("serialNumber", equalTo(TEST_SERIAL_NUMBER))
                .body("crlUrls", notNullValue())
                .body("ocspUrls", notNullValue())
                .body("issuerDN", equalTo(TEST_ISSUER_DN));

        testCACertificateId = responseOfPost.getBody().jsonPath().getString("id");
    }

    @Test(dependsOnMethods = {"testAddCACertificate"})
    public void testGetCACertificates() {

        Response responseOfGet = getResponseOfGet(CERTIFICATE_VALIDATION_API_BASE_PATH + CA_CERTIFICATES_PATH);
        responseOfGet.then()
                .log().ifValidationFails()
                .assertThat()
                .statusCode(HttpStatus.SC_OK)
                .body(CERTIFICATES_KEY, notNullValue())
                .body(CERTIFICATES_KEY + ".size()", equalTo(1));
    }

    @Test(dependsOnMethods = {"testGetCACertificates"})
    public void testGetCACertificateByCertificateId() {

        Response responseOfGet = getResponseOfGet(CERTIFICATE_VALIDATION_API_BASE_PATH +
                CA_CERTIFICATES_PATH + "/" + testCACertificateId);
        responseOfGet.then()
                .log().ifValidationFails()
                .assertThat()
                .statusCode(HttpStatus.SC_OK)
                .body("id", equalTo(testCACertificateId))
                .body("serialNumber", equalTo(TEST_SERIAL_NUMBER))
                .body("crlUrls", notNullValue())
                .body("ocspUrls", notNullValue())
                .body("issuerDN", equalTo(TEST_ISSUER_DN));
    }

    @Test(dependsOnMethods = {"testGetCACertificateByCertificateId"})
    public void testUpdateCACertificate() {

        cacertificate = new CACertificateAddRequest()
                .certificate(TEST_CERTIFICATE);

        String body = toJSONString(cacertificate);
        Response responseOfPut = getResponseOfPut(CERTIFICATE_VALIDATION_API_BASE_PATH +
                CA_CERTIFICATES_PATH + "/" + testCACertificateId, body);

        responseOfPut.then()
                .log().ifValidationFails()
                .assertThat()
                .statusCode(HttpStatus.SC_OK)
                .body("id", equalTo(testCACertificateId))
                .body("serialNumber", equalTo(TEST_SERIAL_NUMBER))
                .body("crlUrls", notNullValue())
                .body("ocspUrls", notNullValue())
                .body("issuerDN", equalTo(TEST_ISSUER_DN));
    }

    @Test(dependsOnMethods = {"testUpdateCACertificate"})
    public void testDeleteAction() {

        getResponseOfDelete(CERTIFICATE_VALIDATION_API_BASE_PATH + CA_CERTIFICATES_PATH + "/" +
                testCACertificateId).then()
                .log().ifValidationFails()
                .assertThat()
                .statusCode(HttpStatus.SC_NO_CONTENT);

        Response responseOfGet = getResponseOfGet(CERTIFICATE_VALIDATION_API_BASE_PATH +
                CA_CERTIFICATES_PATH);
        responseOfGet.then()
                .log().ifValidationFails()
                .assertThat().statusCode(HttpStatus.SC_OK)
                .body(CERTIFICATES_KEY + ".size()", equalTo(0));
    }
}

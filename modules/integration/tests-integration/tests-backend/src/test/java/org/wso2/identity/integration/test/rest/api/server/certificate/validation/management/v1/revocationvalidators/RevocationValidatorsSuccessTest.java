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

package org.wso2.identity.integration.test.rest.api.server.certificate.validation.management.v1.revocationvalidators;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import io.restassured.RestAssured;
import io.restassured.response.Response;
import org.apache.http.HttpStatus;
import org.testng.annotations.AfterClass;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Factory;
import org.testng.annotations.Test;
import org.wso2.carbon.automation.engine.context.TestUserMode;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

import static org.hamcrest.CoreMatchers.equalTo;
import static org.hamcrest.CoreMatchers.notNullValue;

/**
 * Tests for happy paths of the Certificate Validation Management REST API Revocation Validators.
 */
public class RevocationValidatorsSuccessTest extends RevocationValidatorsTestBase {

    @Factory(dataProvider = "restAPIUserConfigProvider")
    public RevocationValidatorsSuccessTest(TestUserMode userMode) throws Exception {

        super.init(userMode);
        this.context = isServer;
        this.authenticatingUserName = context.getContextTenant().getTenantAdmin().getUserName();
        this.authenticatingCredential = context.getContextTenant().getTenantAdmin().getPassword();
        this.tenant = context.getContextTenant().getDomain();
    }

    @BeforeClass(alwaysRun = true)
    @Override
    public void init() throws IOException {

        super.testInit(API_VERSION, swaggerDefinition, tenant);
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
    public void testGetRevocationValidators() {

        Response responseOfGet = getResponseOfGet(CERTIFICATE_VALIDATION_API_BASE_PATH + REVOCATION_VALIDATORS_PATH);
        responseOfGet.then()
                .log().ifValidationFails()
                .assertThat()
                .statusCode(HttpStatus.SC_OK)
                .body(VALIDATORS_KEY, notNullValue())
                .body(VALIDATORS_KEY + ".size()", equalTo(2))
                .body(VALIDATORS_KEY, org.hamcrest.Matchers.hasItems(OCSP_VALIDATOR, CRL_VALIDATOR));
    }

    @Test
    public void testGetFullChainValidationValidator() {

        Response response = getResponseOfGet(
                CERTIFICATE_VALIDATION_API_BASE_PATH + REVOCATION_VALIDATORS_PATH + "/" + OCSP_VALIDATOR
        );
        response.then()
                .log().ifValidationFails()
                .assertThat()
                .statusCode(HttpStatus.SC_OK)
                .body(ENABLE_KEY, equalTo(ENABLED_VALUE))
                .body(PRIORITY_KEY, equalTo(PRIORITY_VALUE))
                .body(FULL_CHAIN_VALIDATION_KEY, equalTo(FULL_CHAIN_VALIDATION_VALUE))
                .body(RETRY_COUNT_KEY, equalTo(RETRY_COUNT_VALUE));
    }

    @Test
    public void testUpdateFullChainValidationValidator() throws JsonProcessingException {

        String endpoint = CERTIFICATE_VALIDATION_API_BASE_PATH + REVOCATION_VALIDATORS_PATH + "/" + OCSP_VALIDATOR;

        // Prepare request body
        Map<String, Object> requestBody = new HashMap<>();
        requestBody.put(ENABLE_KEY, ENABLED_VALUE);
        requestBody.put(PRIORITY_KEY, PRIORITY_VALUE);
        requestBody.put(FULL_CHAIN_VALIDATION_KEY, FULL_CHAIN_VALIDATION_VALUE);
        requestBody.put(RETRY_COUNT_KEY, 3); // Use 3 as per the test requirement

        ObjectMapper objectMapper = new ObjectMapper();
        String requestBodyJson = objectMapper.writeValueAsString(requestBody);

        Response response = getResponseOfPut(endpoint, requestBodyJson);
        response.then()
                .log().ifValidationFails()
                .assertThat()
                .statusCode(HttpStatus.SC_OK)
                .body(ENABLE_KEY, equalTo(ENABLED_VALUE))
                .body(PRIORITY_KEY, equalTo(PRIORITY_VALUE))
                .body(FULL_CHAIN_VALIDATION_KEY, equalTo(FULL_CHAIN_VALIDATION_VALUE))
                .body(RETRY_COUNT_KEY, equalTo(3));
    }
}

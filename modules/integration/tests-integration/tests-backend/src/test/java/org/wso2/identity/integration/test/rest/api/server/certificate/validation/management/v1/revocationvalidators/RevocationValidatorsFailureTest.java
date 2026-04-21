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
import org.apache.commons.lang.StringUtils;
import org.apache.http.HttpStatus;
import org.testng.annotations.AfterClass;
import org.testng.annotations.AfterMethod;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Factory;
import org.testng.annotations.Test;
import org.wso2.carbon.automation.engine.context.TestUserMode;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

import static org.hamcrest.CoreMatchers.equalTo;

/**
 * Tests for negative paths of the Certificate Validation Management REST API Revocation Validators.
 */
public class RevocationValidatorsFailureTest extends RevocationValidatorsTestBase {

    @Factory(dataProvider = "restAPIUserConfigProvider")
    public RevocationValidatorsFailureTest(TestUserMode userMode) throws Exception {

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

    @AfterMethod(alwaysRun = true)
    public void testFinish() {

        RestAssured.basePath = StringUtils.EMPTY;
    }

    @Test
    public void testGetValidatorByInvalidName() {

        String invalidValidatorName = "non_existing_validator";
        Response response = getResponseOfGet(
                CERTIFICATE_VALIDATION_API_BASE_PATH + REVOCATION_VALIDATORS_PATH + "/" + invalidValidatorName
        );
        response.then()
                .log().ifValidationFails()
                .assertThat()
                .statusCode(HttpStatus.SC_NOT_FOUND)
                .body("description",
                        equalTo("Invalid validator name " + invalidValidatorName + " in the tenant " + this.tenant +
                                "."));
    }

    @Test
    public void testUpdateValidatorWithInvalidKeys() throws JsonProcessingException {

        String endpoint = CERTIFICATE_VALIDATION_API_BASE_PATH + REVOCATION_VALIDATORS_PATH + "/" + OCSP_VALIDATOR;

        // Invalid values: negative priority and retryCount
        Map<String, Object> requestBody = new HashMap<>();
        requestBody.put("wrong_key", true);
        requestBody.put(PRIORITY_KEY, 1);
        requestBody.put(FULL_CHAIN_VALIDATION_KEY, true);
        requestBody.put(RETRY_COUNT_KEY, 5);

        ObjectMapper objectMapper = new ObjectMapper();
        String requestBodyJson = objectMapper.writeValueAsString(requestBody);

        Response response = getResponseOfPut(endpoint, requestBodyJson);
        response.then()
                .log().ifValidationFails()
                .assertThat()
                .statusCode(HttpStatus.SC_BAD_REQUEST)
                .body("description", equalTo("Provided request body content is not in the expected format."));
    }
}

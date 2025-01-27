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

package org.wso2.identity.integration.test.rest.api.server.claim.management.v1;

import io.restassured.RestAssured;
import io.restassured.response.Response;
import org.apache.commons.lang.StringUtils;
import org.apache.http.HttpHeaders;
import org.apache.http.HttpStatus;
import org.testng.Assert;
import org.testng.annotations.AfterClass;
import org.testng.annotations.AfterMethod;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;
import org.wso2.identity.integration.test.rest.api.server.common.RESTAPIServerTestBase;

import java.io.IOException;

import static org.hamcrest.core.IsNull.notNullValue;

public class ClaimProfilesWithRecoveryAPITest extends RESTAPIServerTestBase {

    private static final String API_DEFINITION_NAME = "claim-management.yaml";
    static final String API_VERSION = "v1";
    private static String API_PACKAGE_NAME = "org.wso2.carbon.identity.rest.api.server.claim.management.v1";

    public static final String CLAIM_DIALECTS_ENDPOINT_URI = "/claim-dialects";
    public static final String LOCAL_CLAIMS_ENDPOINT_URI = "/local/claims";

    protected static String claimMgtSwaggerDefinition;

    static {
        try {
            claimMgtSwaggerDefinition = getAPISwaggerDefinition(API_PACKAGE_NAME, API_DEFINITION_NAME);
        } catch (IOException e) {
            Assert.fail(String.format("Unable to read the swagger definition %s from %s", API_DEFINITION_NAME,
                    API_PACKAGE_NAME), e);
        }
    }

    public void testSelfRegistrationClaimProfile() throws Exception {

        addLocalClaim("claim-management-add-local-claim-with-profile-self-registration.json");
        addLocalClaim("claim-management-add-local-claim.json");
    }

    private void addLocalClaim(String fileName) throws IOException {

        String body = readResource(fileName);
        Response response = getResponseOfPost(CLAIM_DIALECTS_ENDPOINT_URI + LOCAL_CLAIMS_ENDPOINT_URI, body);
        response.then()
                .log().ifValidationFails()
                .assertThat()
                .statusCode(HttpStatus.SC_CREATED)
                .header(HttpHeaders.LOCATION, notNullValue());
    }
}

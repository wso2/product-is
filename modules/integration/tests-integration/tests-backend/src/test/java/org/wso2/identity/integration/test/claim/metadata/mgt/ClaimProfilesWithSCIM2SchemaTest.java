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

package org.wso2.identity.integration.test.claim.metadata.mgt;

import io.restassured.response.Response;
import org.apache.http.HttpHeaders;
import org.apache.http.HttpStatus;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;
import org.wso2.identity.integration.common.utils.ISIntegrationTest;
import org.wso2.identity.integration.test.restclients.ClaimManagementRestClient;
import org.wso2.identity.integration.test.restclients.SCIM2RestClient;

import java.io.IOException;

import static org.hamcrest.core.IsNull.notNullValue;
import static org.wso2.identity.integration.test.rest.api.common.RESTTestBase.readResource;

public class ClaimProfilesWithSCIM2SchemaTest extends ISIntegrationTest {

    ClaimManagementRestClient claimManagementRestClient;
    SCIM2RestClient scim2RestClient;

//    public void testSelfRegistrationClaimProfile() throws Exception {
//
//        addLocalClaim("claim-management-add-local-claim-with-profile-self-registration.json");
//        addLocalClaim("claim-management-add-local-claim.json");
//    }

    @BeforeMethod
    public void setUp() {

        scim2RestClient = new SCIM2RestClient(serverURL, tenantInfo);
        claimManagementRestClient = new ClaimManagementRestClient(serverURL, tenantInfo);
    }

    @Test
    public void testAddLocalClaim() throws IOException {

//        String body = readResource("claim-management-add-local-claim.json");
//        Response response = getResponseOfPost(CLAIM_DIALECTS_ENDPOINT_URI + LOCAL_CLAIMS_ENDPOINT_URI, body);
//        response.then()
//                .log().ifValidationFails()
//                .assertThat()
//                .statusCode(HttpStatus.SC_CREATED)
//                .header(HttpHeaders.LOCATION, notNullValue());
    }
}

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
package org.wso2.identity.integration.test.rest.api.server.application.management.v1;

import io.restassured.response.Response;
import org.apache.http.HttpHeaders;
import org.apache.http.HttpStatus;
import org.json.JSONObject;
import org.testng.annotations.Factory;
import org.testng.annotations.Test;
import org.wso2.carbon.automation.engine.context.TestUserMode;

import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.nullValue;
import static org.hamcrest.core.IsNull.notNullValue;
import static org.wso2.identity.integration.test.rest.api.server.application.management.v1.Utils.assertNotBlank;
import static org.wso2.identity.integration.test.rest.api.server.application.management.v1.Utils.extractApplicationIdFromLocationHeader;

/**
 * Tests for Application PATCH operation.
 */
public class ApplicationPatchTest extends ApplicationManagementBaseTest {

    private static final String APP_NAME = "testPatchApplication";
    public static final String UPDATED_APP_NAME = "testUpdateNameApplication";
    private String appId;

    @Factory(dataProvider = "restAPIUserConfigProvider")
    public ApplicationPatchTest(TestUserMode userMode) throws Exception {

        super(userMode);
    }

    @Test
    public void testCreateApplication() throws Exception {

        JSONObject createRequest = new JSONObject();
        createRequest.put("name", APP_NAME);
        String payload = createRequest.toString();

        Response responseOfPost = getResponseOfPost(APPLICATION_MANAGEMENT_API_BASE_PATH, payload);
        responseOfPost.then()
                .log().ifValidationFails()
                .assertThat()
                .statusCode(HttpStatus.SC_CREATED)
                .header(HttpHeaders.LOCATION, notNullValue());

        appId = getApplicationId(responseOfPost);
        assertNotBlank(appId);

        String path = APPLICATION_MANAGEMENT_API_BASE_PATH + "/" + appId;
        Response responseOfGet = getResponseOfGet(path);
        responseOfGet.then()
                .log().ifValidationFails()
                .assertThat()
                .statusCode(HttpStatus.SC_OK)
                .body("name", equalTo(APP_NAME));
    }

    @Test(dependsOnMethods = "testCreateApplication")
    public void testRenameApplication() throws Exception {

        JSONObject patchRequest = new JSONObject();
        patchRequest.put("name", UPDATED_APP_NAME);

        String path = APPLICATION_MANAGEMENT_API_BASE_PATH + "/" + appId;
        getResponseOfPatch(path, patchRequest.toString()).then()
                .assertThat()
                .statusCode(HttpStatus.SC_OK);

        getApplication(appId).then()
                .body("name", equalTo(UPDATED_APP_NAME));
    }

    @Test(dependsOnMethods = "testRenameApplication")
    public void testUpdateBasicInformation() throws Exception {

        String description = "This is my application.";
        String imageUrl = "https://localhost/image.png";
        String accessUrl = "https://app.test.com/login";

        JSONObject patchRequest = new JSONObject();
        patchRequest.put("description", description);
        patchRequest.put("imageUrl", imageUrl);
        patchRequest.put("accessUrl", accessUrl);

        String path = APPLICATION_MANAGEMENT_API_BASE_PATH + "/" + appId;
        getResponseOfPatch(path, patchRequest.toString()).then()
                .assertThat()
                .statusCode(HttpStatus.SC_OK);

        getApplication(appId).then()
                .body("description", equalTo(description))
                .body("imageUrl", equalTo(imageUrl))
                .body("accessUrl", equalTo(accessUrl));
    }

    @Test(dependsOnMethods = "testUpdateBasicInformation")
    public void testUpdateAdvancedConfiguration() throws Exception {

        // Let's assert the default values of the advanced configs first.
        getApplication(appId).then()
                .body("advancedConfigurations.find{ it.key == 'saas' }.value", equalTo(false))
                .body("advancedConfigurations.find{ it.key == 'discoverableByEndUsers' }.value", equalTo(false))
                .body("advancedConfigurations.find{ it.key == 'skipLoginConsent' }.value", equalTo(false))
                .body("advancedConfigurations.find{ it.key == 'skipLogoutConsent' }.value", equalTo(false))
                .body("advancedConfigurations.find{ it.key == 'returnAuthenticatedIdpList' }.value", equalTo(false))
                .body("advancedConfigurations.find{ it.key == 'enableAuthorization' }.value", equalTo(false))
                .body("advancedConfigurations.find{ it.key == 'certificate' }.value", nullValue());

        // Do the PATCH update request.
        String patchRequest = readResource("patch-application-advanced-configuration.json");
        String path = APPLICATION_MANAGEMENT_API_BASE_PATH + "/" + appId;
        getResponseOfPatch(path, patchRequest.toString()).then()
                .assertThat()
                .statusCode(HttpStatus.SC_OK);

        // Assert that the PATCH update of advanced configs was successful.
        getApplication(appId).then()
                .body("advancedConfigurations.find{ it.key == 'saas' }.value", equalTo(true))
                .body("advancedConfigurations.find{ it.key == 'discoverableByEndUsers' }.value", equalTo(true))
                .body("advancedConfigurations.find{ it.key == 'skipLoginConsent' }.value", equalTo(true))
                .body("advancedConfigurations.find{ it.key == 'skipLogoutConsent' }.value", equalTo(true))
                .body("advancedConfigurations.find{ it.key == 'returnAuthenticatedIdpList' }.value", equalTo(true))
                .body("advancedConfigurations.find{ it.key == 'enableAuthorization' }.value", equalTo(true));

        // Do the PATCH update request to set PEM certificate.
        String patchCertificatePEMRequest = readResource("patch-application-advanced-configuration-certificate-pem" +
                ".json");
        getResponseOfPatch(path, patchCertificatePEMRequest).then()
                .assertThat()
                .statusCode(HttpStatus.SC_OK);

        // Assert that the PATCH update of certificate type PEM was successful.
        getApplication(appId).then()
                .body("advancedConfigurations.find{ it.key == 'certificate' }.value", notNullValue())
                .body("advancedConfigurations.certificate.find{ it.key == 'type' }.value", equalTo("PEM"))
                .body("advancedConfigurations.certificate.find{ it.key == 'value' }.value", equalTo(
                        "LS0tLS1CRUdJTiBDRVJUSUZJQ0FURS0tLS0tCk1JSUZhekNDQTFPZ0F3SUJBZ0lVWmRBalp4QVBCeFpad3g" +
                                "wRndrMkJtallaUzNRd0RRWUpLb1pJaHZjTkFRRUwKQlFBd1JURUxNQWtHQTFVRUJoTUNRVlV4RXpBUkJnTl" +
                                "ZCQWdNQ2xOdmJXVXRVM1JoZEdVeElUQWZCZ05WQkFvTQpHRWx1ZEdWeWJtVjBJRmRwWkdkcGRITWdVSFI1S" +
                                "UV4MFpEQWVGdzB5TVRFd01EZ3dOekU1TWpsYUZ3MHpNVEV3Ck1EWXdOekU1TWpsYU1FVXhDekFKQmdOVkJB" +
                                "WVRBa0ZWTVJNd0VRWURWUVFJREFwVGIyMWxMVk4wWVhSbE1TRXcKSHdZRFZRUUtEQmhKYm5SbGNtNWxkQ0J" +
                                "YYVdSbmFYUnpJRkIwZVNCTWRHUXdnZ0lpTUEwR0NTcUdTSWIzRFFFQgpBUVVBQTRJQ0R3QXdnZ0lLQW9JQ0" +
                                "FRQzFYaENBN0JWWFVxM29ueVdBaTBSMFR5NW83VkJlcm9hWGxUU1V4VTRtClRWVEhqWjk3Sjdtd2RCaEVKa" +
                                "1k0Z1hpTkRuK0ZpSWVFUC9LUGlPeVBNK0Y4bWg4NDFwdWFDQ0pOOXpsNzZJK2EKOTVia1FUUC9iZnFuejRM" +
                                "TzBDOWYycTVPNm9BUmdhS2FERWhncEwyTGM4ZmVwbEZuRnQyREExYjJUVnd0VVNaUwp4WVQ5UTB6cGNDMEt" +
                                "LakFkNjlOdEZ5M1N1RTRVVTJIVW8xa3ZMMDc1UW8rT09aRUxWNFZWRDhtRFI3dCtsN1VzCnZFSzh5YkpOZk" +
                                "JqT2pLYXJSZGF3TEkzVnRLbXUxWWI2aGZlcHdaakh2cmM2WXMzRVQzNmhYcWpSbWZuQ0xyMDQKOU41amprY" +
                                "lI0OENkdFExcHNQcERydWpQN243K0sxTTZYMUVKQ21sc3dnYmlNS09waE95ZzNoem5Gd0tlYVZZcwpxQkJV" +
                                "VVdvQ3FKbWM0ck84ek0zQTdHQjNBMlBGbkgrbm44WWRUeW8wbFI2a0gvTjZRWFJwb2xrTStqQW5EV0FsCkR" +
                                "6SVBDdElHZHhsQlF4UExyL1g5UnMyVUpobk5FWW1Xako1YU5KNy9DRHl5ZWtyS05jNVZ3Vk9UdVRaWXlDY2" +
                                "cKemtTbGUxMHcvM1dUWE14UnBBYTBidVhyeDFoNm16UHB0NHg4eXBzQkZQdFJ2RUthamZKSDArYUxkZjFZd" +
                                "2dGdwpFaEFsbWV5RmpDdmxaSExNSCtFV0JCRllVb2xGa2NXajMraTJpcEVYMy9nRVlJWGZBcFV2anV0TVV2" +
                                "WFVWZ3ZHCmdrdmhkWlRIbm4zYlBXT0NTbjVQbXFXblVvWHNlcTFQaUZQMHV3V2c4N045N3VvZXhFQVpraXF" +
                                "DS0YzQ2g3SVcKNlFJREFRQUJvMU13VVRBZEJnTlZIUTRFRmdRVWRoUmc2eEdaTzZ2S2oyS2JpcTM5TVJHTW" +
                                "ltOHdId1lEVlIwagpCQmd3Rm9BVWRoUmc2eEdaTzZ2S2oyS2JpcTM5TVJHTWltOHdEd1lEVlIwVEFRSC9CQ" +
                                "VV3QXdFQi96QU5CZ2txCmhraUc5dzBCQVFzRkFBT0NBZ0VBR1l6eDVsZDY5Y3ljd3FqN0dtVzdCK3d2QmVz" +
                                "dEdPZmozS0FJUk9LYjdJYUEKdkVxOXhvTFp5V0NQcXNqNy90V0I2dS90L0wzQU9iNzhJc2ZIY200UnRxN1o" +
                                "raXNBNFRjdTNrQ3o2Qjg2dWx0WgpiTGo2bEh1eElhelB0M3VvcDRWY0Z4a0ZqaitnbzhLeWFhUHZrM2VrY2" +
                                "s2TmxURjhLMGU1cTc2eXYybW5SZ2U2CnZ2NXVIME9Fc3ZQbU1kTUcwMXZIOXJWYUd3UkgyRkJESGg0R256R" +
                                "2tJc2M0emtITHYvVUhzOWJGWWNXdzBQOGIKa0xkWWZVa2ZTaFRDWHg2NnYvNmxqdXh2bXA1T1JXbFlNa013" +
                                "QlJiZ3JhV21sUTBwNkd1NlFSdWNMOStoR2lJegpnUlBlSzRNQzZ5dWIyRWx3ZHY3YnFMZ3I0dVlGRWlzVlN" +
                                "FTG5lVzBHbXVqbDMrRmlpOWdZVSs1cTE3RUtMcmUyCm8weWdObzgwVDk3MHVPTUxjR0hHRlU5TTVINS9zOG" +
                                "llT01oeVY3RmR1TkkzZSthN2NTSm5FY21Ib203OGxJeWcKUVFTNHNXU1ViOUMvaW9Da2VOZmVGaXZZUXJ2S" +
                                "WtnR0N0Vi9VL2lJeWRDMTd1dzNjVWRPd0NLYk83RGRmNi9yUApnQ09YQWVydkRkcFhDK05icnRsaFZmcmcy" +
                                "TGdiVlYvMFYwTlhzaWJoUGJsVis3R0x1N3hNS3A5cEtKdXgydFVQCkJsSGhHZWdEbkoxUTVPWEVFZWVLVzV" +
                                "HVHhhS2ZvMnhydS9XZTlKbFFNcVNHUU5wZG5KcHQrSmJHeS9sa092eGMKNVZxc2V4dmFzQWtDaGxMV0N3ZW" +
                                "tBdFYxR0FsRm9jTFovdGNaY1MvR3d4b0V1bXU5TExqMmtVRm9wS0hERG9zPQotLS0tLUVORCBDRVJUSUZJQ" +
                                "0FURS0tLS0t"));

        // Do the PATCH update request to set PEM certificate.
        String patchCertificateJWKSRequest = readResource("patch-application-advanced-configuration-certificate-jwks" +
                ".json");
        getResponseOfPatch(path, patchCertificateJWKSRequest).then()
                .assertThat()
                .statusCode(HttpStatus.SC_OK);

        // Assert that the PATCH update of certificate type PEM was successful.
        getApplication(appId).then()
                .body("advancedConfigurations.find{ it.key == 'certificate' }.value", notNullValue())
                .body("advancedConfigurations.certificate.find{ it.key == 'type' }.value", equalTo("JWKS"))
                .body("advancedConfigurations.certificate.find{ it.key == 'value' }.value", equalTo("test_jwks_uri"));
    }

    @Test(dependsOnMethods = "testUpdateAdvancedConfiguration")
    public void testDeleteApplicationById() throws Exception {

        getResponseOfDelete(APPLICATION_MANAGEMENT_API_BASE_PATH + "/" + appId)
                .then()
                .log().ifValidationFails()
                .assertThat()
                .statusCode(HttpStatus.SC_NO_CONTENT);

        // Verify that the application is not available.
        getResponseOfGet(APPLICATION_MANAGEMENT_API_BASE_PATH + "/" + appId)
                .then()
                .log().ifValidationFails()
                .assertThat()
                .statusCode(HttpStatus.SC_NOT_FOUND);
    }

    private String getApplicationId(Response createFirstAppResponse) {

        String location = createFirstAppResponse.getHeader(HttpHeaders.LOCATION);
        return extractApplicationIdFromLocationHeader(location);
    }

    private Response getApplication(String appId) {

        String path = APPLICATION_MANAGEMENT_API_BASE_PATH + "/" + appId;
        Response responseOfGet = getResponseOfGet(path);
        responseOfGet.then()
                .log().ifValidationFails()
                .assertThat()
                .statusCode(HttpStatus.SC_OK);

        return responseOfGet;
    }
}

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
import org.testng.annotations.DataProvider;
import org.testng.annotations.Factory;
import org.testng.annotations.Test;
import org.wso2.carbon.automation.engine.context.TestUserMode;

import static org.hamcrest.CoreMatchers.equalTo;
import static org.hamcrest.Matchers.hasSize;
import static org.hamcrest.core.IsNull.notNullValue;
import static org.wso2.identity.integration.test.rest.api.server.application.management.v1.Utils.assertNotBlank;
import static org.wso2.identity.integration.test.rest.api.server.application.management.v1.Utils.extractApplicationIdFromLocationHeader;

/**
 * Tests for happy paths of the managing SAML applications using Application Management REST API.
 */
public class ApplicationManagementSAMLSuccessTest extends ApplicationManagementBaseTest {

    private static final String META_DATA_FILE_ISSUER = "https://saml.wso2.com";
    private static final String MANUAL_CONFIG_ISSUER = "https://sp.wso2.com";

    private String samlAppPostRequest;
    private String samlAppPutRequest;
    private String createdAppId;
    private String expectedIssuer;

    @Factory(dataProvider = "restAPIUserConfigProvider")
    public ApplicationManagementSAMLSuccessTest(TestUserMode userMode, String samlAppPostRequest, String samlAppPutRequest,
                                                String expectedIssuer) throws Exception {

        super(userMode);
        this.samlAppPostRequest = samlAppPostRequest;
        this.expectedIssuer = expectedIssuer;
        this.samlAppPutRequest = samlAppPutRequest;
    }

    @DataProvider(name = "restAPIUserConfigProvider")
    public static Object[][] restAPIUserConfigProvider() {

        return new Object[][]{
                {TestUserMode.SUPER_TENANT_ADMIN, "create-saml-app-with-metadata-file.json",
                        "update-saml-app-with-metadata-file.json", META_DATA_FILE_ISSUER},
                {TestUserMode.SUPER_TENANT_ADMIN, "create-saml-app-with-manual-config.json",
                        "update-saml-app-with-manual-config.json", MANUAL_CONFIG_ISSUER},

                {TestUserMode.TENANT_ADMIN, "create-saml-app-with-metadata-file.json",
                        "update-saml-app-with-metadata-file.json", META_DATA_FILE_ISSUER},
                {TestUserMode.TENANT_ADMIN, "create-saml-app-with-manual-config.json",
                        "update-saml-app-with-manual-config.json", MANUAL_CONFIG_ISSUER}
        };
    }

    @Test
    public void testCreateSAMLAppWithMetadataFile() throws Exception {

        String body = readResource(samlAppPostRequest);
        Response responseOfPost = getResponseOfPost(APPLICATION_MANAGEMENT_API_BASE_PATH, body);
        responseOfPost.then()
                .log().ifValidationFails()
                .assertThat()
                .statusCode(HttpStatus.SC_CREATED)
                .header(HttpHeaders.LOCATION, notNullValue());

        String location = responseOfPost.getHeader(HttpHeaders.LOCATION);
        createdAppId = extractApplicationIdFromLocationHeader(location);
        assertNotBlank(createdAppId);
    }

    @Test(dependsOnMethods = "testCreateSAMLAppWithMetadataFile")
    public void testGetSAMLInboundDetails() throws Exception {

        String path = APPLICATION_MANAGEMENT_API_BASE_PATH + "/" + createdAppId + "/inbound-protocols/saml";

        Response responseOfGet = getResponseOfGet(path);
        responseOfGet.then()
                .log().ifValidationFails()
                .assertThat()
                .statusCode(HttpStatus.SC_OK)
                .body("issuer", equalTo(expectedIssuer));
    }

    @Test(dependsOnMethods = "testGetSAMLInboundDetails", description = "Test to verify the SAML inbound update")
    public void testUpdateSAMLInbound() throws Exception {

        String path = APPLICATION_MANAGEMENT_API_BASE_PATH + "/" + createdAppId + "/inbound-protocols/saml";

        String body = readResource(samlAppPutRequest);

        Response responseOfPut = getResponseOfPut(path, body);
        responseOfPut.then()
                .log().ifValidationFails()
                .assertThat()
                .statusCode(HttpStatus.SC_OK);

        // Verify the updated details
        Response responseOfGet = getResponseOfGet(path);
        responseOfGet.then()
                .log().ifValidationFails()
                .assertThat()
                .statusCode(HttpStatus.SC_OK).and()
                .body("defaultAssertionConsumerUrl", equalTo("https://saml.wso2.com/acs3")).and()
                .body("assertionConsumerUrls", hasSize(3)).and()
                .body("requestValidation.enableSignatureValidation", equalTo(false));
    }

    @Test(dependsOnMethods = "testUpdateSAMLInbound", description = "Test to verify the SAML inbound delete")
    public void testDeleteSAMLInbound() throws Exception {

        String path = APPLICATION_MANAGEMENT_API_BASE_PATH + "/" + createdAppId + "/inbound-protocols/saml";

        Response responseOfDelete = getResponseOfDelete(path);
        responseOfDelete.then()
                .log()
                .ifValidationFails()
                .assertThat()
                .statusCode(HttpStatus.SC_NO_CONTENT);

        // Make sure we don't have the SAML inbound details.
        getResponseOfGet(path).then().assertThat().statusCode(HttpStatus.SC_NOT_FOUND);
    }

    @Test(dependsOnMethods = "testDeleteSAMLInbound")
    public void testDeleteApplication() throws Exception {

        String path = APPLICATION_MANAGEMENT_API_BASE_PATH + "/" + createdAppId;

        Response responseOfDelete = getResponseOfDelete(path);
        responseOfDelete.then()
                .log()
                .ifValidationFails()
                .assertThat()
                .statusCode(HttpStatus.SC_NO_CONTENT);

        // Make sure we don't have deleted application details.
        getResponseOfGet(path).then().assertThat().statusCode(HttpStatus.SC_NOT_FOUND);
    }
}

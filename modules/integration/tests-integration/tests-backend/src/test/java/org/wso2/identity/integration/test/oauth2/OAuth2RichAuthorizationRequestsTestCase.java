/*
 * Copyright (c) 2025, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
 *
 * WSO2 Inc. licenses this file to you under the Apache License,
 * Version 2.0 (the "License"); you may not use this file except
 * in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied. See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */

package org.wso2.identity.integration.test.oauth2;

import org.apache.http.HttpResponse;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.testng.Assert;
import org.testng.annotations.AfterClass;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;
import org.wso2.carbon.automation.engine.context.TestUserMode;
import org.wso2.identity.integration.test.rest.api.server.api.resource.v1.model.APIResourcePatchModel;
import org.wso2.identity.integration.test.rest.api.server.api.resource.v1.model.AuthorizationDetailsType;
import org.wso2.identity.integration.test.rest.api.server.application.management.v1.model.ApplicationResponseModel;
import org.wso2.identity.integration.test.rest.api.server.application.management.v1.model.AuthorizedAPICreationModel;
import org.wso2.identity.integration.test.rest.api.server.application.management.v1.model.AuthorizedAPIPatchModel;
import org.wso2.identity.integration.test.rest.api.server.application.management.v1.model.AuthorizedAPIResponse;
import org.wso2.identity.integration.test.utils.DataExtractUtil;

import java.io.IOException;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import javax.servlet.http.HttpServletResponse;

import static org.wso2.identity.integration.test.utils.RichAuthorizationRequestsUtils.TEST_TYPE_INVALID;
import static org.wso2.identity.integration.test.utils.RichAuthorizationRequestsUtils.createTestAPIResource;
import static org.wso2.identity.integration.test.utils.RichAuthorizationRequestsUtils.getTestAuthorizationDetailsSchema;
import static org.wso2.identity.integration.test.utils.RichAuthorizationRequestsUtils.getTestAuthorizationDetailsType;

/**
 * Integration test class for testing the API resource flows and API authorization flows in applications.
 */
public class OAuth2RichAuthorizationRequestsTestCase extends OAuth2ServiceAbstractIntegrationTest {

    public static final String TEST_TYPE_1 = "type_1";
    public static final String TEST_TYPE_2 = "type_2";
    public static final String TEST_TYPE_3 = "type_3";
    public static final List<AuthorizationDetailsType> TEST_AUTHORIZATION_DETAILS_TYPES = Arrays
            .asList(getTestAuthorizationDetailsType(TEST_TYPE_1), getTestAuthorizationDetailsType(TEST_TYPE_2));

    private String applicationId;
    private String apiResourceId;
    private String authorizedApiResourceId;

    @BeforeClass(alwaysRun = true)
    public void testInit() throws Exception {

        super.init(TestUserMode.SUPER_TENANT_USER);
        super.setSystemproperties();

        this.apiResourceId = createTestAPIResource(super.restClient, TEST_AUTHORIZATION_DETAILS_TYPES);

        ApplicationResponseModel application = super.addApplication();
        Assert.assertNotNull(application, "OAuth App creation failed.");
        this.applicationId = application.getId();
    }

    @AfterClass(alwaysRun = true)
    public void atEnd() throws Exception {

        super.restClient.deleteAPIResource(this.apiResourceId);
        super.deleteApp(this.applicationId);
        super.restClient.closeHttpClient();
    }

    @Test(groups = "wso2.is", description = "Checks api resource flow with authorization details types")
    public void testRegisterApiResourceWithAuthorizationDetails() throws IOException {

        final List<AuthorizationDetailsType> types =
                super.restClient.getAPIResourceAuthorizationDetailsTypes(apiResourceId);
        Assert.assertNotNull(types);
        Assert.assertEquals(types.size(), TEST_AUTHORIZATION_DETAILS_TYPES.size());
    }

    @Test(groups = "wso2.is", description = "Checks metadata endpoint contains supported authorization details types",
            dependsOnMethods = {"testRegisterApiResourceWithAuthorizationDetails"})
    public void testOIDCMetadataResponseContent() throws IOException, JSONException {
        HttpResponse httpResponse = sendGetRequest(super.restClient.client, "https://localhost:9853/oauth2/token/.well-known/openid-configuration");
        String content = DataExtractUtil.getContentData(httpResponse);
        Assert.assertNotNull(content, "Response content is not received");

        JSONObject oidcMetadataEndpoints = new JSONObject(content);
        JSONArray authorizationDetailsTypesSupported = oidcMetadataEndpoints.getJSONArray("authorization_details_types_supported");
        Assert.assertNotNull(authorizationDetailsTypesSupported);
        TEST_AUTHORIZATION_DETAILS_TYPES.stream().map(AuthorizationDetailsType::getType)
                .forEach(type -> Assert.assertTrue(authorizationDetailsTypesSupported.toString().contains(type)));
    }

    @Test(groups = "wso2.is", description = "Checks api resource update with authorization details types",
            dependsOnMethods = {"testRegisterApiResourceWithAuthorizationDetails"})
    private void testUpdateApiResourceWithAuthorizationDetails() throws IOException {

        APIResourcePatchModel patchModel = new APIResourcePatchModel()
                .addRemovedAuthorizationDetailsTypesItem(TEST_TYPE_2);
        super.restClient.updateAPIResource(apiResourceId, patchModel);

        List<AuthorizationDetailsType> types = super.restClient.getAPIResourceAuthorizationDetailsTypes(apiResourceId);
        Assert.assertNotNull(types);
        Assert.assertEquals(types.size(), 1);
        Assert.assertEquals(types.get(0).getType(), TEST_TYPE_1);

        patchModel = new APIResourcePatchModel()
                .addAddedAuthorizationDetailsTypesItem(getTestAuthorizationDetailsType(TEST_TYPE_2));
        super.restClient.updateAPIResource(apiResourceId, patchModel);

        types = super.restClient.getAPIResourceAuthorizationDetailsTypes(apiResourceId);
        Assert.assertNotNull(types);
        Assert.assertEquals(types.size(), 2);
    }

    @Test(groups = "wso2.is", description = "Test authorization details type CRUD flows",
            dependsOnMethods = {"testUpdateApiResourceWithAuthorizationDetails"})
    public void testAuthorizationDetailsTypes() throws IOException {

        Assert.assertNull(super.restClient.getAuthorizationDetailsType(apiResourceId, "invalid_type_id"));

        List<AuthorizationDetailsType> types = super.restClient.addAuthorizationDetailsTypes(apiResourceId,
                Collections.singletonList(getTestAuthorizationDetailsType(TEST_TYPE_3)));
        Assert.assertNotNull(types);
        Assert.assertFalse(types.isEmpty());
        types.forEach(type -> {
            Assert.assertNotNull(type.getId());
            Assert.assertEquals(type.getType(), TEST_TYPE_3);
            Assert.assertNotNull(type.getName());
        });

        final String typeId = types.get(0).getId();
        AuthorizationDetailsType type = restClient.getAuthorizationDetailsType(apiResourceId, typeId);

        Assert.assertNotNull(type);
        Assert.assertEquals(type.getType(), TEST_TYPE_3);
        Assert.assertTrue(type.getName().contains(TEST_TYPE_3));

        final String type4 = "test_type_4";
        type.setId(null);
        type.setType(type4);
        type.setSchema(getTestAuthorizationDetailsSchema(type4));

        super.restClient.updateAuthorizationDetailsType(apiResourceId, typeId, type);
        AuthorizationDetailsType updatedType = super.restClient.getAuthorizationDetailsType(apiResourceId, typeId);

        Assert.assertNotNull(updatedType);
        Assert.assertEquals(type.getType(), type4);
        Assert.assertTrue(type.getName().contains(TEST_TYPE_3));
        Assert.assertTrue(type.getSchema().get("properties").toString().contains(type4));

        super.restClient.deleteAuthorizationDetailsType(apiResourceId, typeId);
        Assert.assertNull(super.restClient.getAuthorizationDetailsType(apiResourceId, typeId));
    }

    @Test(groups = "wso2.is", description = "Check authorization details types authorization",
            dependsOnMethods = {"testUpdateApiResourceWithAuthorizationDetails"})
    public void testAddAuthorizedAuthorizationDetailsTypes() throws IOException {

        AuthorizedAPICreationModel authorizedAPICreationModel = new AuthorizedAPICreationModel();
        authorizedAPICreationModel.setId(apiResourceId);
        authorizedAPICreationModel.addAuthorizationDetailsTypesItem(TEST_TYPE_1);

        int response = super.restClient.addAPIAuthorizationToApplication(applicationId, authorizedAPICreationModel);
        Assert.assertEquals(response, HttpServletResponse.SC_OK, "Failed to add API authorization to the application");

        List<AuthorizedAPIResponse> apiResponses = super.restClient.getAPIAuthorizationsFromApplication(applicationId);

        Assert.assertNotNull(apiResponses);
        apiResponses.forEach(apiResponse -> Assert.assertNotNull(apiResponse.getAuthorizedAuthorizationDetailsTypes()));
        apiResponses.forEach(apiResponse ->
                Assert.assertEquals(apiResponse.getAuthorizedAuthorizationDetailsTypes().size(), 1));
        apiResponses.forEach(apiResponse ->
                apiResponse.getAuthorizedAuthorizationDetailsTypes().forEach(type -> {
                    Assert.assertNotNull(type.getId());
                    Assert.assertEquals(type.getType(), TEST_TYPE_1);
                    Assert.assertNotNull(type.getName());
                })
        );

        this.authorizedApiResourceId = apiResponses.get(0).getId();
    }

    @Test(groups = "wso2.is", description = "Check authorization details types authorization update",
            dependsOnMethods = {"testAddAuthorizedAuthorizationDetailsTypes"})
    public void testUpdateAuthorizedAuthorizationDetailsTypes() throws IOException {

        AuthorizedAPIPatchModel patchModel = new AuthorizedAPIPatchModel();
        patchModel.addAddedAuthorizationDetailsTypesItem(TEST_TYPE_2);
        patchModel.addRemovedAuthorizationDetailsTypesItem(TEST_TYPE_1);

        int response = super.restClient
                .updateAPIAuthorizationsFromApplication(applicationId, authorizedApiResourceId, patchModel);
        Assert.assertEquals(response, HttpServletResponse.SC_OK, "Authorized API resource update failed");
        List<AuthorizedAPIResponse> apiResponses = super.restClient.getAPIAuthorizationsFromApplication(applicationId);

        Assert.assertNotNull(apiResponses);
        apiResponses.forEach(apiResponse -> Assert.assertNotNull(apiResponse.getAuthorizedAuthorizationDetailsTypes()));
        apiResponses.forEach(apiResponse ->
                Assert.assertEquals(apiResponse.getAuthorizedAuthorizationDetailsTypes().size(), 1));
        apiResponses.forEach(apiResponse ->
                apiResponse.getAuthorizedAuthorizationDetailsTypes().forEach(type -> {
                    Assert.assertNotNull(type.getId());
                    Assert.assertEquals(type.getType(), TEST_TYPE_2);
                    Assert.assertNotNull(type.getName());
                })
        );

        patchModel.addAddedAuthorizationDetailsTypesItem(TEST_TYPE_INVALID);
        response = super.restClient
                .updateAPIAuthorizationsFromApplication(applicationId, authorizedApiResourceId, patchModel);
        Assert.assertEquals(response, HttpServletResponse.SC_BAD_REQUEST);
    }

    @Test(groups = "wso2.is", description = "Check authorization details types authorization delete",
            dependsOnMethods = {"testUpdateAuthorizedAuthorizationDetailsTypes"})
    public void testDeleteAuthorizedAuthorizationDetailsTypes() throws IOException {

        super.restClient.deleteAPIAuthorizationsFromApplication(applicationId, authorizedApiResourceId);
        List<AuthorizedAPIResponse> apiResponses = super.restClient.getAPIAuthorizationsFromApplication(applicationId);

        Assert.assertTrue(apiResponses.isEmpty());
    }

    @Test(groups = "wso2.is", description = "Checks api resource deletion with authorization details types",
            dependsOnMethods = {"testDeleteAuthorizedAuthorizationDetailsTypes"})
    public void testDeleteApiResourceWithAuthorizationDetails() throws IOException {

        super.restClient.deleteAPIResource(apiResourceId);
        final List<AuthorizationDetailsType> types =
                super.restClient.getAPIResourceAuthorizationDetailsTypes(apiResourceId);

        Assert.assertNull(types);
    }
}

/*
 * Copyright (c) 2021, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
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
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */

package org.wso2.identity.integration.test.scim2.rest.api.customSchema;

import io.restassured.RestAssured;
import io.restassured.response.ExtractableResponse;
import io.restassured.response.Response;
import org.apache.commons.lang.StringUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.http.HttpHeaders;
import org.apache.http.HttpStatus;
import org.testng.Assert;
import org.testng.annotations.AfterClass;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.DataProvider;
import org.testng.annotations.Factory;
import org.testng.annotations.Test;
import org.wso2.carbon.automation.engine.context.AutomationContext;
import org.wso2.carbon.automation.engine.context.TestUserMode;
import org.wso2.carbon.identity.claim.metadata.mgt.stub.ClaimMetadataManagementServiceClaimMetadataException;
import org.wso2.carbon.identity.claim.metadata.mgt.stub.dto.ClaimDialectDTO;
import org.wso2.carbon.identity.claim.metadata.mgt.stub.dto.AttributeMappingDTO;
import org.wso2.carbon.identity.claim.metadata.mgt.stub.dto.ClaimPropertyDTO;
import org.wso2.carbon.identity.claim.metadata.mgt.stub.dto.ExternalClaimDTO;
import org.wso2.carbon.identity.claim.metadata.mgt.stub.dto.LocalClaimDTO;
import org.wso2.carbon.integration.common.utils.LoginLogoutClient;
import org.wso2.carbon.user.core.UserCoreConstants;
import org.wso2.identity.integration.common.clients.claim.metadata.mgt.ClaimMetadataManagementServiceClient;
import org.wso2.identity.integration.test.scim2.rest.api.SCIM2BaseTest;
import org.wso2.identity.integration.test.scim2.rest.api.SCIMUtils;

import java.rmi.RemoteException;
import java.util.Arrays;
import java.util.LinkedHashMap;
import java.util.List;

import static org.hamcrest.CoreMatchers.notNullValue;
import static org.testng.Assert.assertEquals;
import static org.testng.Assert.assertNotNull;
import static org.testng.Assert.assertNull;
import static org.wso2.identity.integration.test.scim2.SCIM2BaseTestCase.USERS_ENDPOINT;

/**
 * Test cases for SCIM2 /Users api relates operations with custom schema related claims.
 */
public class SCIM2CustomSchemaUserTestCase extends SCIM2BaseTest {

    private static final Log log = LogFactory.getLog(SCIM2CustomSchemaUserTestCase.class);

    private String userIdEndpointURL;
    private String userId = null;
    private TestUserMode mode;
    private String cookie;
    private ClaimMetadataManagementServiceClient claimMetadataManagementServiceClient = null;

    // Custom schema related constants.
    private static final String CUSTOM_SCHEMA_URI = "urn:scim:schemas:extension:custom:User";
    private static final String CUSTOM_SCHEMA_URI_WITH_ESCAPE_CHARS = "\"urn:scim:schemas:extension:custom:User\"";
    // Country claim related constants.
    private static final String COUNTRY_CLAIM_ATTRIBUTE_NAME = "country";
    private static final String COUNTRY_CLAIM_ATTRIBUTE_URI = CUSTOM_SCHEMA_URI + ":" + COUNTRY_CLAIM_ATTRIBUTE_NAME;
    private static final String COUNTRY_LOCAL_CLAIM_URI = "http://wso2.org/claims/country";
    private static final String COUNTRY_LOCAL_CLAIM_VALUE = "France";
    private static final String COUNTRY_LOCAL_CLAIM_VALUE_AFTER_REPLACE = "Sri Lanka";
    private static final String COUNTRY_LOCAL_CLAIM_VALUE_AFTER_PUT = "India";
    // Manager claim related constants.
    private static final String MANAGER_CLAIM_ATTRIBUTE_NAME = "manager";
    private static final String MANAGER_CLAIM_ATTRIBUTE_URI = "urn:scim:schemas:extension:custom:User:manager";
    private static final String MANAGER_EMAIL_CLAIM_ATTRIBUTE_NAME = "email";
    private static final String MANAGER_EMAIL_CLAIM_ATTRIBUTE_URI =
            MANAGER_CLAIM_ATTRIBUTE_URI + "." + MANAGER_EMAIL_CLAIM_ATTRIBUTE_NAME;
    private static final String MANAGER_LOCAL_CLAIM_URI = "http://wso2.org/claims/manager";
    private static final String MANAGER_EMAIL_LOCAL_CLAIM_URI = "http://wso2.org/claims/emails.work";
    private static final String MANAGER_EMAIL_LOCAL_CLAIM_VALUE = "piraveena@gmail.com";
    private static final String MANAGER_EMAIL_LOCAL_CLAIM_VALUE_AFTER_REPLACE = "piraveenaReplace@gmail.com";
    private static final String MANAGER_EMAIL_LOCAL_CLAIM_VALUE_AFTER_ADD = "piraveenaAdd@gmail.com";

    @Factory(dataProvider = "restAPIUserConfigProvider")
    public SCIM2CustomSchemaUserTestCase(TestUserMode userMode) throws Exception {

        super.init(userMode);
        this.context = isServer;
        this.authenticatingUserName = context.getContextTenant().getTenantAdmin().getUserName();
        this.authenticatingCredential = context.getContextTenant().getTenantAdmin().getPassword();
        this.tenant = context.getContextTenant().getDomain();
        this.mode = userMode;
    }

    @DataProvider(name = "restAPIUserConfigProvider")
    public static Object[][] restAPIUserConfigProvider() {

        return new Object[][]{
                {TestUserMode.SUPER_TENANT_ADMIN},
                {TestUserMode.TENANT_ADMIN}
        };
    }

    @BeforeClass(alwaysRun = true)
    public void init() throws RemoteException, InterruptedException {

        super.testInit(swaggerDefinition, tenant);
        Thread.sleep(5000);
    }

    @AfterClass(alwaysRun = true)
    public void testFinish() {

        try {
            claimMetadataManagementServiceClient.removeExternalClaim(CUSTOM_SCHEMA_URI, COUNTRY_CLAIM_ATTRIBUTE_URI);
            claimMetadataManagementServiceClient.removeExternalClaim(CUSTOM_SCHEMA_URI,
                    MANAGER_EMAIL_CLAIM_ATTRIBUTE_URI);
            claimMetadataManagementServiceClient.removeExternalClaim(CUSTOM_SCHEMA_URI, MANAGER_CLAIM_ATTRIBUTE_URI);
            claimMetadataManagementServiceClient.removeLocalClaim(MANAGER_LOCAL_CLAIM_URI);
            claimMetadataManagementServiceClient.removeClaimDialect(CUSTOM_SCHEMA_URI);
        } catch (RemoteException | ClaimMetadataManagementServiceClaimMetadataException e) {
            log.error(e);
        }
        super.conclude();
    }

    @BeforeMethod(alwaysRun = true)
    public void testInit() {

        RestAssured.basePath = basePath;
    }

    @Test(description = "Creates simple attribute and complex attributes in urn:scim:schemas:extension:custom:User.")
    private void createClaims() throws Exception {

        AutomationContext context = new AutomationContext("IDENTITY", mode);
        backendURL = context.getContextUrls().getBackEndUrl();
        loginLogoutClient = new LoginLogoutClient(context);
        cookie = loginLogoutClient.login();
        claimMetadataManagementServiceClient = new ClaimMetadataManagementServiceClient(backendURL, cookie);

        //Set claims.
        setCustomDialect();
        setSimpleAttribute();
        setComplexAttribute();

        ExternalClaimDTO[] externalClaimDTOs =
                claimMetadataManagementServiceClient.getExternalClaims(CUSTOM_SCHEMA_URI);
        Assert.assertTrue(Arrays.stream(externalClaimDTOs)
                .anyMatch(claim -> StringUtils.equals(claim.getExternalClaimURI(), COUNTRY_CLAIM_ATTRIBUTE_URI)));
        Assert.assertTrue(Arrays.stream(externalClaimDTOs)
                .anyMatch(claim -> StringUtils.equals(claim.getExternalClaimURI(), MANAGER_CLAIM_ATTRIBUTE_URI)));
        Assert.assertTrue(Arrays.stream(externalClaimDTOs)
                .anyMatch(claim -> StringUtils.equals(claim.getExternalClaimURI(), MANAGER_EMAIL_CLAIM_ATTRIBUTE_URI)));
    }

    @Test(dependsOnMethods = "createClaims", description = "Create user with custom schema dialect.")
    public void testCreateUser() throws Exception {

        String body = readResource("scim2-custom-schema-add-user.json");
        Response response = getResponseOfPost(USERS_ENDPOINT, body, SCIM_CONTENT_TYPE);
        ExtractableResponse<Response> extractableResponse = response.then()
                .log().ifValidationFails()
                .assertThat()
                .statusCode(HttpStatus.SC_CREATED)
                .header(HttpHeaders.LOCATION, notNullValue())
                .and()
                .assertThat()
                .header(HttpHeaders.CONTENT_TYPE, SCIM_CONTENT_TYPE)
                .extract();
        Assert.assertNotNull(extractableResponse);

        userId = extractableResponse.path("id");
        Assert.assertNotNull(userId, "The user did not get created.");
        log.info("Created a user with userId :" + userId);
        userIdEndpointURL = USERS_ENDPOINT + "/" + userId;

        SCIMUtils.validateSchemasAttributeOfUsersEndpoint(extractableResponse.path("schemas"));
        SCIMUtils.validateMetaAttributeOfUsersEndpoint(extractableResponse.path("meta"), response, userIdEndpointURL);

        Object customSchema = extractableResponse.path(CUSTOM_SCHEMA_URI_WITH_ESCAPE_CHARS);
        assertNotNull(customSchema);

        String country = ((LinkedHashMap) customSchema).get(COUNTRY_CLAIM_ATTRIBUTE_NAME).toString();
        assertEquals(country, COUNTRY_LOCAL_CLAIM_VALUE);

        LinkedHashMap manager = (LinkedHashMap) ((LinkedHashMap) customSchema).get(MANAGER_CLAIM_ATTRIBUTE_NAME);
        assertNotNull(manager);

        String managerEMail = manager.get(MANAGER_EMAIL_CLAIM_ATTRIBUTE_NAME).toString();
        assertEquals(managerEMail, MANAGER_EMAIL_LOCAL_CLAIM_VALUE);
    }

    @Test(dependsOnMethods = "testCreateUser", description = "Tests get users and check for claims with /Users api.")
    public void testGetUsers() throws Exception {

        Response response = getResponseOfGet(USERS_ENDPOINT, SCIM_CONTENT_TYPE);
        if (HttpStatus.SC_INTERNAL_SERVER_ERROR == response.getStatusCode()) {
            log.info(">>> Content: >>>" + response.getBody().prettyPrint());
        }
        response.then()
                .assertThat()
                .statusCode(HttpStatus.SC_OK)
                .log()
                .ifValidationFails()
                .and()
                .assertThat().header(HttpHeaders.CONTENT_TYPE, SCIM_CONTENT_TYPE);

        List<String> userResources = response.jsonPath().getList("Resources");
        String countryValue = null;
        Object customSchema = null;

        for (Object users : userResources) {
            LinkedHashMap userResourcesResponse = (LinkedHashMap) users;
            String userIdResponse = (String) userResourcesResponse.get("id");
            if (StringUtils.equalsIgnoreCase(userId, userIdResponse)) {
                customSchema = userResourcesResponse.get(CUSTOM_SCHEMA_URI);
                countryValue = ((LinkedHashMap) customSchema).get(COUNTRY_CLAIM_ATTRIBUTE_NAME).toString();
            }
        }
        assertNotNull(customSchema);
        assertEquals(countryValue, COUNTRY_LOCAL_CLAIM_VALUE);
    }

    @Test(dependsOnMethods = "testGetUsers", description = "Tests patch replace operation with custom schema " +
            "attributes with /Users api.")
    public void testPatchReplaceUserAttributes() throws Exception {

        String body = readResource("scim2-custom-schema-patch-replace-attribute.json");
        Response response = getResponseOfPatch(userIdEndpointURL, body, SCIM_CONTENT_TYPE);
        ExtractableResponse<Response> extractableResponse = response.then()
                .log().ifValidationFails()
                .assertThat()
                .statusCode(HttpStatus.SC_OK)
                .and()
                .assertThat()
                .header(HttpHeaders.CONTENT_TYPE, SCIM_CONTENT_TYPE)
                .extract();
        Assert.assertNotNull(extractableResponse);

        Object customSchema = extractableResponse.path(CUSTOM_SCHEMA_URI_WITH_ESCAPE_CHARS);
        assertNotNull(customSchema);

        String country = ((LinkedHashMap) customSchema).get(COUNTRY_CLAIM_ATTRIBUTE_NAME).toString();
        assertEquals(country, COUNTRY_LOCAL_CLAIM_VALUE_AFTER_REPLACE);

        LinkedHashMap manager = (LinkedHashMap) ((LinkedHashMap) customSchema).get(MANAGER_CLAIM_ATTRIBUTE_NAME);
        assertNotNull(manager);

        String managerEMail = manager.get(MANAGER_EMAIL_CLAIM_ATTRIBUTE_NAME).toString();
        assertEquals(managerEMail, MANAGER_EMAIL_LOCAL_CLAIM_VALUE_AFTER_REPLACE);
    }

    @Test(dependsOnMethods = "testPatchReplaceUserAttributes", description = "Tests patch remove operation for custom" +
            " schema attributes with /Users api.")
    public void testPatchRemoveUserAttributes() throws Exception {

        String body = readResource("scim2-custom-schema-patch-remove-attribute.json");
        Response response = getResponseOfPatch(userIdEndpointURL, body, SCIM_CONTENT_TYPE);
        ExtractableResponse<Response> extractableResponse = response.then()
                .log().ifValidationFails()
                .assertThat()
                .statusCode(HttpStatus.SC_OK)
                .and()
                .assertThat()
                .header(HttpHeaders.CONTENT_TYPE, SCIM_CONTENT_TYPE)
                .extract();
        Assert.assertNotNull(extractableResponse);

        Object customSchema = extractableResponse.path(CUSTOM_SCHEMA_URI_WITH_ESCAPE_CHARS);
        assertNotNull(customSchema);

        Object country = ((LinkedHashMap) customSchema).get(COUNTRY_CLAIM_ATTRIBUTE_NAME);
        assertNull(country);

        LinkedHashMap manager = (LinkedHashMap) ((LinkedHashMap) customSchema).get(MANAGER_CLAIM_ATTRIBUTE_NAME);
        assertNotNull(manager);
        String managerEMail = manager.get(MANAGER_EMAIL_CLAIM_ATTRIBUTE_NAME).toString();
        assertEquals(managerEMail, MANAGER_EMAIL_LOCAL_CLAIM_VALUE_AFTER_REPLACE);
    }

    @Test(dependsOnMethods = "testPatchRemoveUserAttributes", description = "Tests put operation with custom schema " +
            "attributes using /Users api.")
    public void testPutUserAttributes() throws Exception {

        String body = readResource("scim2-custom-schema-put-user.json");
        Response response = getResponseOfPut(userIdEndpointURL, body, SCIM_CONTENT_TYPE);
        ExtractableResponse<Response> extractableResponse = response.then()
                .log().ifValidationFails()
                .assertThat()
                .statusCode(HttpStatus.SC_OK)
                .and()
                .assertThat()
                .header(HttpHeaders.CONTENT_TYPE, SCIM_CONTENT_TYPE)
                .extract();
        Assert.assertNotNull(extractableResponse);

        Object customSchema = extractableResponse.path(CUSTOM_SCHEMA_URI_WITH_ESCAPE_CHARS);
        assertNotNull(customSchema);

        String country = ((LinkedHashMap) customSchema).get(COUNTRY_CLAIM_ATTRIBUTE_NAME).toString();
        assertEquals(country, COUNTRY_LOCAL_CLAIM_VALUE_AFTER_PUT);

        LinkedHashMap manager = (LinkedHashMap) ((LinkedHashMap) customSchema).get(MANAGER_CLAIM_ATTRIBUTE_NAME);
        assertNull(manager);
    }

    @Test(dependsOnMethods = "testPutUserAttributes", description = "Tests patch add operation with custom schema " +
            "attributes using /Users api.")
    public void testPatchAddUserAttributes() throws Exception {

        String body = readResource("scim2-custom-schema-patch-add-attribute.json");
        Response response = getResponseOfPatch(userIdEndpointURL, body, SCIM_CONTENT_TYPE);
        ExtractableResponse<Response> extractableResponse = response.then()
                .log().ifValidationFails()
                .assertThat()
                .statusCode(HttpStatus.SC_OK)
                .and()
                .assertThat()
                .header(HttpHeaders.CONTENT_TYPE, SCIM_CONTENT_TYPE)
                .extract();
        Assert.assertNotNull(extractableResponse);

        Object customSchema = extractableResponse.path(CUSTOM_SCHEMA_URI_WITH_ESCAPE_CHARS);
        assertNotNull(customSchema);

        LinkedHashMap manager = (LinkedHashMap) ((LinkedHashMap) customSchema).get(MANAGER_CLAIM_ATTRIBUTE_NAME);
        assertNotNull(manager);

        String managerEMail = manager.get(MANAGER_EMAIL_CLAIM_ATTRIBUTE_NAME).toString();
        assertEquals(managerEMail, MANAGER_EMAIL_LOCAL_CLAIM_VALUE_AFTER_ADD);
    }

    @Test(dependsOnMethods = "testPatchAddUserAttributes", description = "Tests delete user operation using /Users " +
            "api.")
    public void testDeleteUser() throws Exception {

        getResponseOfDelete(userIdEndpointURL, SCIM_CONTENT_TYPE).then()
                .log().ifValidationFails()
                .assertThat()
                .statusCode(HttpStatus.SC_NO_CONTENT);

        getResponseOfGet(userIdEndpointURL, SCIM_CONTENT_TYPE).then().assertThat().statusCode(HttpStatus.SC_NOT_FOUND);
    }

    private void setCustomDialect() throws Exception {

        ClaimDialectDTO claimDialectDTO = new ClaimDialectDTO();
        claimDialectDTO.setClaimDialectURI(CUSTOM_SCHEMA_URI);
        claimMetadataManagementServiceClient.addClaimDialect(claimDialectDTO);
    }

    private void setSimpleAttribute() throws Exception {

        // Create country claim- simple attribute
        ExternalClaimDTO externalClaimDTO = new ExternalClaimDTO();
        externalClaimDTO.setExternalClaimDialectURI(CUSTOM_SCHEMA_URI);
        externalClaimDTO.setExternalClaimURI(COUNTRY_CLAIM_ATTRIBUTE_URI);
        externalClaimDTO.setMappedLocalClaimURI(COUNTRY_LOCAL_CLAIM_URI);
        claimMetadataManagementServiceClient.addExternalClaim(externalClaimDTO);
    }

    private void setComplexAttribute() throws Exception {

        // Create manager claim complex attribute
        LocalClaimDTO localClaimDTO = new LocalClaimDTO();
        localClaimDTO.setLocalClaimURI(MANAGER_LOCAL_CLAIM_URI);

        AttributeMappingDTO[] attributeMappingDTO = new AttributeMappingDTO[1];
        AttributeMappingDTO attributeMappingDTO1 = new AttributeMappingDTO();
        attributeMappingDTO1.setAttributeName(MANAGER_CLAIM_ATTRIBUTE_NAME);
        attributeMappingDTO1.setUserStoreDomain(UserCoreConstants.PRIMARY_DEFAULT_DOMAIN_NAME);
        attributeMappingDTO[0] = attributeMappingDTO1;
        localClaimDTO.setAttributeMappings(attributeMappingDTO);

        ClaimPropertyDTO[] claimDialectDTOs = new ClaimPropertyDTO[4];
        ClaimPropertyDTO claimPropertyDTO1 = new ClaimPropertyDTO();
        claimPropertyDTO1.setPropertyName("datatype");
        claimPropertyDTO1.setPropertyValue("complex");

        ClaimPropertyDTO claimPropertyDTO2 = new ClaimPropertyDTO();
        claimPropertyDTO2.setPropertyName("subattributes");
        claimPropertyDTO2.setPropertyValue(MANAGER_EMAIL_LOCAL_CLAIM_URI);

        ClaimPropertyDTO claimPropertyDTO3 = new ClaimPropertyDTO();
        claimPropertyDTO3.setPropertyName("Description");
        claimPropertyDTO3.setPropertyValue("Manager");

        ClaimPropertyDTO claimPropertyDTO4 = new ClaimPropertyDTO();
        claimPropertyDTO4.setPropertyName("Name");
        claimPropertyDTO4.setPropertyValue("Manager");

        claimDialectDTOs[0] = claimPropertyDTO1;
        claimDialectDTOs[1] = claimPropertyDTO2;
        claimDialectDTOs[2] = claimPropertyDTO3;
        claimDialectDTOs[3] = claimPropertyDTO4;
        localClaimDTO.setClaimProperties(claimDialectDTOs);
        claimMetadataManagementServiceClient.addLocalClaim(localClaimDTO);

        ExternalClaimDTO managerExternalClaimDTO = new ExternalClaimDTO();
        managerExternalClaimDTO.setExternalClaimDialectURI(CUSTOM_SCHEMA_URI);
        managerExternalClaimDTO.setExternalClaimURI(MANAGER_CLAIM_ATTRIBUTE_URI);
        managerExternalClaimDTO.setMappedLocalClaimURI(MANAGER_LOCAL_CLAIM_URI);
        claimMetadataManagementServiceClient.addExternalClaim(managerExternalClaimDTO);

        ExternalClaimDTO managerEmailExternalClaimDTO = new ExternalClaimDTO();
        managerEmailExternalClaimDTO.setExternalClaimDialectURI(CUSTOM_SCHEMA_URI);
        managerEmailExternalClaimDTO.setExternalClaimURI(MANAGER_EMAIL_CLAIM_ATTRIBUTE_URI);
        managerEmailExternalClaimDTO.setMappedLocalClaimURI(MANAGER_EMAIL_LOCAL_CLAIM_URI);
        claimMetadataManagementServiceClient.addExternalClaim(managerEmailExternalClaimDTO);
    }
}

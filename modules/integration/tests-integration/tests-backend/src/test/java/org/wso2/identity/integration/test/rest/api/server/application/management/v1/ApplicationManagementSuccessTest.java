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

import com.fasterxml.jackson.core.JsonFactory;
import com.fasterxml.jackson.databind.ObjectMapper;
import io.restassured.RestAssured;
import io.restassured.response.Response;
import org.apache.commons.lang.StringUtils;
import org.apache.http.HttpHeaders;
import org.apache.http.HttpStatus;
import org.testng.Assert;
import org.testng.annotations.AfterClass;
import org.testng.annotations.AfterMethod;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.DataProvider;
import org.testng.annotations.Factory;
import org.testng.annotations.Test;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.wso2.carbon.automation.engine.context.TestUserMode;
import org.wso2.carbon.identity.application.mgt.ApplicationConstants;
import org.wso2.carbon.utils.multitenancy.MultitenantConstants;
import org.wso2.identity.integration.test.rest.api.server.application.management.v1.model.ApplicationListResponse;
import org.wso2.identity.integration.test.rest.api.server.application.management.v1.model.ApplicationModel;
import org.xml.sax.SAXException;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.net.URISyntaxException;
import java.nio.charset.StandardCharsets;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;

import static org.hamcrest.CoreMatchers.equalTo;
import static org.hamcrest.core.IsNull.notNullValue;

/**
 * Tests for happy paths of the Application Management REST API.
 */
public class ApplicationManagementSuccessTest extends ApplicationManagementBaseTest {

    private static final String USER_PORTAL = "User Portal";
    private static final String APPLICATION_IMPORT_PATH = "/import";
    private static final String APPLICATION_EXPORT_PATH = "/export";
    private static final String APPLICATION_IMPORT_APP_NAME_SUPER_TENANT = "SampleApp";
    private static final String APPLICATION_IMPORT_APP_NAME_TENANT = "SampleAppTenant";

    private static final String CREATED_APP_NAME = "My SAMPLE APP";
    private String createdAppId;
    private String importedAppId = null;

    @Factory(dataProvider = "restAPIUserConfigProvider")
    public ApplicationManagementSuccessTest(TestUserMode userMode) throws Exception {

        super(userMode);
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
    public void testGetAllApplications() throws IOException {

        Response response = getResponseOfGet(APPLICATION_MANAGEMENT_API_BASE_PATH);
        response.then()
                .log().ifValidationFails()
                .assertThat()
                .statusCode(HttpStatus.SC_OK);
        ObjectMapper jsonWriter = new ObjectMapper(new JsonFactory());
        ApplicationListResponse listResponse = jsonWriter.readValue(response.asString(), ApplicationListResponse.class);

        Assert.assertNotNull(listResponse);
        Assert.assertTrue(listResponse.getApplications()
                        .stream()
                        .anyMatch(appBasicInfo -> appBasicInfo.getName().equals(ApplicationConstants.LOCAL_SP)),
                "Default resident service provider '" + ApplicationConstants.LOCAL_SP + "' is not listed by the API");

        if (StringUtils.equals(MultitenantConstants.SUPER_TENANT_DOMAIN_NAME, tenant)) {
            // Check whether the default "User Portal" app exists.
            Assert.assertTrue(listResponse.getApplications()
                            .stream()
                            .anyMatch(appBasicInfo -> appBasicInfo.getName().equals(USER_PORTAL)),
                    "Default application 'User Portal' is not listed by the API.");
        }
    }

    @Test
    public void testImportApplications() throws IOException, URISyntaxException {

        String importFilePath;
        if (StringUtils.equals(MultitenantConstants.SUPER_TENANT_DOMAIN_NAME, tenant)) {
            importFilePath = this.getClass().getResource("sample-sp-import-super-tenant.xml").getPath();
        } else {
            importFilePath = this.getClass().getResource("sample-sp-import-tenant.xml").getPath();
        }
        String response = getResponseOfPostWithFile(APPLICATION_MANAGEMENT_API_BASE_PATH + APPLICATION_IMPORT_PATH,
               importFilePath , "file");

        ObjectMapper jsonWriter = new ObjectMapper(new JsonFactory());
        ApplicationModel applicationModel = jsonWriter.readValue(response, ApplicationModel.class);
        // Assert created application name
        Assert.assertNotNull(applicationModel);
        if (StringUtils.equals(MultitenantConstants.SUPER_TENANT_DOMAIN_NAME, tenant)) {
            Assert.assertEquals(applicationModel.getName(), APPLICATION_IMPORT_APP_NAME_SUPER_TENANT);
        } else {
            Assert.assertEquals(applicationModel.getName(), APPLICATION_IMPORT_APP_NAME_TENANT);
        }

        Assert.assertNotNull(applicationModel.getId());
        // Extract app id using a regex
        importedAppId = applicationModel.getId();
    }

    @Test(dependsOnMethods = {"testImportApplications"})
    public void testExportApplications() throws IOException, SAXException, ParserConfigurationException {

        Response response = getResponseOfGet(APPLICATION_MANAGEMENT_API_BASE_PATH +
                PATH_SEPARATOR + importedAppId + APPLICATION_EXPORT_PATH, "application/octet-stream");

        // Extract application name from the response XML
        DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
        DocumentBuilder builder = factory.newDocumentBuilder();
        ByteArrayInputStream input = new ByteArrayInputStream(response.asString().getBytes(StandardCharsets.UTF_8));
        Document doc = builder.parse(input);
        doc.getDocumentElement().normalize();
        Element eElement = (Element) doc.getDocumentElement().getChildNodes();
        String appName = eElement.getElementsByTagName("ApplicationName").item(0).getTextContent();

        if (StringUtils.equals(MultitenantConstants.SUPER_TENANT_DOMAIN_NAME, tenant)) {
            Assert.assertEquals(appName, APPLICATION_IMPORT_APP_NAME_SUPER_TENANT,
                    "Application export response doesn't match.");
        } else {
            Assert.assertEquals(appName, APPLICATION_IMPORT_APP_NAME_TENANT,
                    "Application export response doesn't match.");
        }
    }

    @Test
    public void createApplication() throws Exception {

        String body = readResource("create-basic-application.json");
        Response responseOfPost = getResponseOfPost(APPLICATION_MANAGEMENT_API_BASE_PATH, body);
        responseOfPost.then()
                .log().ifValidationFails()
                .assertThat()
                .statusCode(HttpStatus.SC_CREATED)
                .header(HttpHeaders.LOCATION, notNullValue());

        String location = responseOfPost.getHeader(HttpHeaders.LOCATION);
        createdAppId = location.substring(location.lastIndexOf("/") + 1);
    }

    @Test(dependsOnMethods = {"createApplication"})
    public void testGetApplicationById() throws Exception {

        getResponseOfGet(APPLICATION_MANAGEMENT_API_BASE_PATH + "/" + createdAppId)
                .then()
                .log().ifValidationFails()
                .assertThat()
                .statusCode(HttpStatus.SC_OK)
                .body("name", equalTo(CREATED_APP_NAME));
    }

    @Test(dependsOnMethods = {"testGetApplicationById"})
    public void testDeleteApplicationById() throws Exception {

        getResponseOfDelete(APPLICATION_MANAGEMENT_API_BASE_PATH + "/" + createdAppId)
                .then()
                .log().ifValidationFails()
                .assertThat()
                .statusCode(HttpStatus.SC_NO_CONTENT);

        // Verify that the application is not available.
        getResponseOfGet(APPLICATION_MANAGEMENT_API_BASE_PATH + "/" + createdAppId)
                .then()
                .log().ifValidationFails()
                .assertThat()
                .statusCode(HttpStatus.SC_NOT_FOUND);
    }
}

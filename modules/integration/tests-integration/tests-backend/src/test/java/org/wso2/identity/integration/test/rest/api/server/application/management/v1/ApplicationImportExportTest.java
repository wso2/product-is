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
import org.apache.commons.lang.StringUtils;
import org.apache.http.HttpHeaders;
import org.apache.http.HttpStatus;
import org.testng.Assert;
import org.testng.annotations.Factory;
import org.testng.annotations.Test;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.wso2.carbon.automation.engine.context.TestUserMode;
import org.wso2.carbon.utils.multitenancy.MultitenantConstants;
import org.xml.sax.SAXException;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;

import static org.hamcrest.core.IsNull.notNullValue;
import static org.wso2.identity.integration.test.rest.api.server.application.management.v1.Utils.assertNotBlank;
import static org.wso2.identity.integration.test.rest.api.server.application.management.v1.Utils.extractApplicationIdFromLocationHeader;

/**
 * Tests for application import/export functionality using the Application Management REST API.
 */
public class ApplicationImportExportTest extends ApplicationManagementBaseTest {

    private static final String APPLICATION_IMPORT_PATH = "/import";
    private static final String APPLICATION_EXPORT_PATH = "/export";

    private static final String APPLICATION_IMPORT_APP_NAME_SUPER_TENANT = "SampleApp";
    private static final String UPDATED_JWKS_URL_APP_SUPER_TENANT = "https://sampleapp.wso2.com/jwks";

    private static final String APPLICATION_IMPORT_APP_NAME_TENANT = "SampleAppTenant";
    private static final String UPDATED_JWKS_URL_APP_TENANT = "https://sampleapptenant.wso2.com/jwks";

    private String importedAppId = null;

    private String importFilePath;
    private String importUpdateFilePath;
    private String importedApplicationName;
    private String updatedJwksUri;

    @Factory(dataProvider = "restAPIUserConfigProvider")
    public ApplicationImportExportTest(TestUserMode userMode) throws Exception {

        super(userMode);

        if (StringUtils.equals(MultitenantConstants.SUPER_TENANT_DOMAIN_NAME, tenant)) {
            importFilePath = this.getClass().getResource("sample-sp-import-super-tenant.xml").getPath();
            importUpdateFilePath = this.getClass().getResource("sample-sp-import-super-tenant_updated.xml").getPath();
            importedApplicationName = APPLICATION_IMPORT_APP_NAME_SUPER_TENANT;
            updatedJwksUri = UPDATED_JWKS_URL_APP_SUPER_TENANT;
        } else {
            importFilePath = this.getClass().getResource("sample-sp-import-tenant.xml").getPath();
            importedApplicationName = APPLICATION_IMPORT_APP_NAME_TENANT;
            importUpdateFilePath = this.getClass().getResource("sample-sp-import-tenant_updated.xml").getPath();
            updatedJwksUri = UPDATED_JWKS_URL_APP_TENANT;
        }
    }

    @Test
    public void testCreateWithImport() throws Exception {

        String endpoint = APPLICATION_MANAGEMENT_API_BASE_PATH + APPLICATION_IMPORT_PATH;
        Response responseOfUpload = getResponseOfMultipartFilePost(endpoint, importFilePath);
        responseOfUpload.then()
                .log().ifValidationFails()
                .assertThat()
                .statusCode(HttpStatus.SC_CREATED)
                .header(HttpHeaders.LOCATION, notNullValue());

        String location = responseOfUpload.getHeader(HttpHeaders.LOCATION);
        importedAppId = extractApplicationIdFromLocationHeader(location);
        assertNotBlank(importedAppId);
    }

    @Test(dependsOnMethods = {"testCreateWithImport"})
    public void testExportApplication() throws Exception {

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

        Assert.assertEquals(appName, importedApplicationName, "Application export response doesn't match.");
    }

    @Test(dependsOnMethods = {"testExportApplication"})
    public void testUpdateApplicationWithImport() throws Exception {

        String endpoint = APPLICATION_MANAGEMENT_API_BASE_PATH + APPLICATION_IMPORT_PATH;
        Response responseOfUpload = getResponseOfMultipartFilePut(endpoint, importUpdateFilePath);
        responseOfUpload.then()
                .log().ifValidationFails()
                .assertThat()
                .statusCode(HttpStatus.SC_OK)
                .header(HttpHeaders.LOCATION, notNullValue());
        String location = responseOfUpload.getHeader(HttpHeaders.LOCATION);
        importedAppId = extractApplicationIdFromLocationHeader(location);
        assertNotBlank(importedAppId);
    }

    @Test(dependsOnMethods = {"testUpdateApplicationWithImport"})
    public void testExportUpdatedApplication() throws Exception {

        Response response = getResponseOfGet(APPLICATION_MANAGEMENT_API_BASE_PATH +
                PATH_SEPARATOR + importedAppId + APPLICATION_EXPORT_PATH, "application/octet-stream");

        // Extract application name from the response XML
        DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
        DocumentBuilder builder = factory.newDocumentBuilder();
        ByteArrayInputStream input = new ByteArrayInputStream(response.asString().getBytes(StandardCharsets.UTF_8));
        Document doc = builder.parse(input);
        doc.getDocumentElement().normalize();
        Element eElement = (Element) doc.getDocumentElement().getChildNodes();

        // Assert app name.
        String appName = eElement.getElementsByTagName("ApplicationName").item(0).getTextContent();
        Assert.assertEquals(appName, importedApplicationName, "Application export response doesn't match.");

        // Assert whether the JWKS URI was updated.
        String exportedJwksUri = eElement.getElementsByTagName("JwksUri").item(0).getTextContent();
        Assert.assertEquals(exportedJwksUri, updatedJwksUri, "JWKS URI was not updated.");
    }

    @Test(dependsOnMethods = {"testExportUpdatedApplication"})
    public void testDeleteApp() throws Exception {

        String path = APPLICATION_MANAGEMENT_API_BASE_PATH + "/" + importedAppId;

        Response responseOfDelete = getResponseOfDelete(path);
        responseOfDelete.then()
                .log()
                .ifValidationFails()
                .assertThat()
                .statusCode(HttpStatus.SC_NO_CONTENT);

        getResponseOfGet(path).then().assertThat().statusCode(HttpStatus.SC_NOT_FOUND);
        importedAppId = null;
    }
}

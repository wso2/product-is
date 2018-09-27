/*
 * Copyright (c) 2018, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
 *
 *  WSO2 Inc. licenses this file to you under the Apache License,
 *  Version 2.0 (the "License"); you may not use this file except
 *  in compliance with the License.
 *  You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing,
 *  software distributed under the License is distributed on an
 *  "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 *  KIND, either express or implied.  See the License for the
 *  specific language governing permissions and limitations
 *  under the License.
 */

package org.wso2.identity.integration.test.application.mgt;

import org.apache.axis2.context.ConfigurationContext;
import org.apache.axis2.context.ConfigurationContextFactory;
import org.testng.Assert;
import org.testng.annotations.AfterClass;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;
import org.wso2.carbon.identity.application.common.model.xsd.ServiceProvider;
import org.wso2.carbon.identity.application.common.model.xsd.SpTemplate;
import org.wso2.identity.integration.common.clients.application.mgt.ApplicationManagementServiceClient;
import org.wso2.identity.integration.common.utils.ISIntegrationTest;

import java.io.File;
import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.Scanner;

import static org.testng.Assert.assertEquals;
import static org.testng.Assert.assertFalse;
import static org.testng.Assert.assertNotNull;
import static org.testng.Assert.assertNull;
import static org.testng.Assert.assertTrue;
import static org.testng.Assert.fail;

public class ApplicationTemplateMgtTestCase extends ISIntegrationTest {

    private ApplicationManagementServiceClient applicationManagementServiceClient;
    private final String applicationName1 = "TestServiceProvider1";
    private final String applicationName2 = "TestServiceProvider2";
    private final String templateName1 = "TestTemplate1";
    private final String templateName2 = "TestTemplate2";
    private final String templateName3 = "TestTemplate3";
    private final String templateName4 = "TestTemplate4";
    private final String templateName5 = "TestTemplate5";
    private final String templateDesc1 = "This is a template with custom claim configurations for travelocity";
    private final String templateDesc2 = "This is a template with TOTP authentication";
    private String templateContent1;
    private String templateContent2;
    private String templateContent3;
    private String templateContent4;
    private String templateContent5;

    @BeforeClass(alwaysRun = true)
    public void testInit() throws Exception {

        super.init();
        ConfigurationContext configContext = ConfigurationContextFactory.createConfigurationContextFromFileSystem(null
                , null);
        applicationManagementServiceClient = new ApplicationManagementServiceClient(sessionCookie, backendURL, configContext);
        templateContent1 = readResource("spTemplate/" + templateName1 + ".xml");
        templateContent2 = readResource("spTemplate/" + templateName2 + ".xml");
        templateContent3 = readResource("spTemplate/" + templateName3 + ".xml");
        templateContent4 = readResource("spTemplate/" + templateName4 + ".xml");
        templateContent5 = readResource("spTemplate/" + templateName5 + ".xml");
    }

    @AfterClass(alwaysRun = true)
    public void atEnd() {

        applicationManagementServiceClient = null;
    }

    @Test(alwaysRun = true, description = "Testing create service provider template")
    public void testCreateApplicationTemplate() {

        try {
            SpTemplate spTemplate1 = getSpTemplate(templateName1, templateDesc1, templateContent1);
            applicationManagementServiceClient.createApplicationTemplate(spTemplate1);

            SpTemplate receivedSpTemplate1 = applicationManagementServiceClient.getApplicationTemplate
                    (templateName1);
            assertNotNull(receivedSpTemplate1, "Failed to retrieve a Service Provider template");
            assertEquals(receivedSpTemplate1.getName(), templateName1,
                    "Failed to create a Service Provider template");
            assertEquals(receivedSpTemplate1.getDescription(), templateDesc1, "Failed to create a Service Provider " +
                    "template");

            SpTemplate spTemplate2 = getSpTemplate(templateName2, templateDesc2, templateContent2);
            applicationManagementServiceClient.createApplicationTemplate(spTemplate2);
            SpTemplate receivedSpTemplate2 = applicationManagementServiceClient.getApplicationTemplate
                    (templateName2);
            assertNotNull(receivedSpTemplate2, "Failed to retrieve a Service Provider template");
            assertEquals(receivedSpTemplate2.getName(), templateName2,
                    "Failed to create a Service Provider template");
            assertEquals(receivedSpTemplate2.getDescription(), templateDesc2, "Failed to create a Service Provider " +
                    "template");

            SpTemplate spTemplate3 = getSpTemplate(templateName5, null, templateContent5);
            applicationManagementServiceClient.createApplicationTemplate(spTemplate3);
            SpTemplate receivedSpTemplate3 = applicationManagementServiceClient.getApplicationTemplate
                    (templateName5);
            assertNotNull(receivedSpTemplate3, "Failed to retrieve a Service Provider template");
            assertEquals(receivedSpTemplate3.getName(), templateName5,
                    "Failed to create a Service Provider template");
            assertEquals(receivedSpTemplate3.getDescription(), null, "Failed to create a Service Provider " +
                    "template");
        } catch (Exception e) {
            fail("Error while trying to read Service provider Template", e);
        }
    }

    @Test(alwaysRun = true, description = "Testing create service provider template with unsupported config")
    public void testCreateApplicationTemplateWithUnsupportedConfig() {

        try {
            SpTemplate spTemplate = getSpTemplate(templateName3, null, templateContent3);
            applicationManagementServiceClient.createApplicationTemplate(spTemplate);

            SpTemplate receivedSpTemplate = applicationManagementServiceClient.getApplicationTemplate
                    (templateName3);
            assertNull(receivedSpTemplate, "Successfully created a Service Provider template with invalid " +
                    "config");
        } catch (Exception e) {
            assertTrue(true);
        }
    }

    @Test(alwaysRun = true, description = "Testing create service provider template with invalid config")
    public void testCreateApplicationTemplateWithInvalidConfig() {

        try {
            SpTemplate spTemplate = getSpTemplate(templateName4, null, templateContent4);
            applicationManagementServiceClient.createApplicationTemplate(spTemplate);

            SpTemplate receivedSpTemplate = applicationManagementServiceClient.getApplicationTemplate
                    (templateName4);
            assertNull(receivedSpTemplate, "Successfully created a Service Provider template with invalid " +
                    "config");
        } catch (Exception e) {
            assertTrue(true);
        }
    }

    @Test(alwaysRun = true, description = "Testing update Service Provider Template",
            dependsOnMethods = "testCreateApplicationTemplate")
    public void testUpdateApplicationTemplate() {

        String updatedTemplateDesc = "This is a template with TOTP and X509 authentication";

        try {
            SpTemplate spTemplate = getSpTemplate(templateName2, updatedTemplateDesc, templateContent2);
            applicationManagementServiceClient.updateApplicationTemplate(templateName2, spTemplate);

            SpTemplate receivedSpTemplate = applicationManagementServiceClient.getApplicationTemplate
                    (templateName2);
            assertNotNull(receivedSpTemplate, "Failed to retrieve a Service Provider template");
            assertEquals(receivedSpTemplate.getDescription(), updatedTemplateDesc,
                    "Failed to update a Service Provider template");
        } catch (Exception e) {
            fail("Error while trying to update Service Provider Template", e);
        }
    }

    @Test(alwaysRun = true, description = "Testing load all the service provider templates",
            dependsOnMethods = "testCreateApplicationTemplate")
    public void testGetAllApplicationTemplates() {

        try {
            SpTemplate[] templates = applicationManagementServiceClient.getAllApplicationTemplateInfo();
            assertNotNull(templates, "Failed to retrieve all the Service Provider templates");
            assertEquals(3, templates.length, "Loading incorrect number of templates");
            for (SpTemplate spTemplateDTO : templates) {
                if (!templateName1.equals(spTemplateDTO.getName()) && !templateName2.equals(spTemplateDTO.getName())
                        && !templateName5.equals(spTemplateDTO.getName())) {
                    fail("Retrieved incorrect template name: " + spTemplateDTO.getName());
                } else {
                    assertTrue(true, "Retrieved correct template name: " + spTemplateDTO.getName());
                }
            }
        } catch (Exception e) {
            fail("Error while trying to get Service provider Templates", e);
        }
    }

    @Test(alwaysRun = true, description = "Testing create service provider as template",
            dependsOnMethods = "testGetAllApplicationTemplates")
    public void testCreateServiceProviderAsTemplate() {

        String templateName = "TestTemplateFromSP";
        String templateDesc = "This is a template to allow saas for all the applications";

        try {

            createApplication(applicationName1, "This is a test Service Provider");
            assertEquals(applicationManagementServiceClient.getApplication(applicationName1).getApplicationName(),
                    applicationName1, "Failed to create a Service Provider");

            ServiceProvider serviceProvider = applicationManagementServiceClient.getApplication
                    (applicationName1);
            SpTemplate spTemplate = getSpTemplate(templateName, templateDesc, null);
            applicationManagementServiceClient.createApplicationTemplateFromSP(serviceProvider, spTemplate);

            SpTemplate receivedSpTemplate = applicationManagementServiceClient.getApplicationTemplate
                    (templateName);
            assertNotNull(receivedSpTemplate, "Failed to retrieve a Service Provider template");
            assertEquals(receivedSpTemplate.getName(), templateName,
                    "Failed to create a Service Provider template from SP");
            assertNotNull(receivedSpTemplate.getContent(), "Failed to create a Service Provider template " +
                    "from SP");
        } catch (Exception e) {
            fail("Error while trying to create Service provider Template from a already configured " +
                    "Service provider", e);
        }
    }

    @Test(alwaysRun = true, description = "Testing create Service Provider with a template",
            dependsOnMethods = "testCreateApplicationTemplate")
    public void createApplicationWithTemplate() {

        try {
            createApplicationWithTemplate(applicationName2, "This is a test Service Provider to be created using " +
                    "a template", templateName1);
            assertEquals(applicationManagementServiceClient.getApplication(applicationName2).getApplicationName(),
                    applicationName2, "Failed to create a Service Provider using a template");
        } catch (Exception e) {
            Assert.fail("Error while trying to create a Service Provider using a template", e);
        }
    }

    @Test(alwaysRun = true, description = "Testing delete Service Provider Template",
            dependsOnMethods = {"testGetAllApplicationTemplates", "testUpdateApplicationTemplate"})
    public void testDeleteApplicationTemplate() {

        try {
            applicationManagementServiceClient.deleteApplicationTemplate(templateName1);
            assertFalse(applicationManagementServiceClient.isExistingApplicationTemplate(templateName1),
                    "Failed to delete a Service Provider template");
        } catch (Exception e) {
            fail("Error while trying to delete Service Provider Template", e);
        }
    }

    private SpTemplate getSpTemplate(String templateName, String templateDesc, String templateContent) {

        SpTemplate spTemplate = new SpTemplate();
        spTemplate.setName(templateName);
        spTemplate.setDescription(templateDesc);
        spTemplate.setContent(templateContent);
        return spTemplate;
    }

    private void createApplication(String name, String description) {

        try {
            ServiceProvider serviceProvider = new ServiceProvider();
            serviceProvider.setApplicationName(name);
            serviceProvider.setDescription(description);
            serviceProvider.setSaasApp(true);
            applicationManagementServiceClient.createApplication(serviceProvider);
        } catch (Exception e) {
            fail("Error while trying to create Service Provider", e);
        }
    }

    private void createApplicationWithTemplate(String name, String description, String template) {

        try {
            ServiceProvider serviceProvider = new ServiceProvider();
            serviceProvider.setApplicationName(name);
            serviceProvider.setDescription(description);
            applicationManagementServiceClient.createApplicationWithTemplate(serviceProvider, template);
        } catch (Exception e) {
            fail("Error while trying to create Service Provider with a template", e);
        }
    }

    private String readResource(String path) {

        StringBuilder result = new StringBuilder();
        Scanner scanner = null;
        try {
            //Get file from resources folder
            ClassLoader classLoader = getClass().getClassLoader();
            if (classLoader != null && path != null && classLoader.getResource(path) != null) {
                URI filepath = new URI(classLoader.getResource(path).toString());
                File file = new File(filepath);
                scanner = new Scanner(file);
                while (scanner.hasNextLine()) {
                    String line = scanner.nextLine();
                    result.append(line).append("\n");
                }
            }
        } catch (IOException e) {
            log.error("Error occurred when reading the file.", e);
        } catch (URISyntaxException e) {
            log.error("URI syntax error.", e);
        } finally {
            if (scanner != null) {
                scanner.close();
            }
        }
        return result.toString().replaceAll("\\n\\r|\\n|\\r|\\t|\\s{2,}", "").replaceAll(": ", ":");
    }
}

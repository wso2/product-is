/*
 * Copyright (c) 2018, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
 *
 *  WSO2 Inc. licenses this spFile to you under the Apache License,
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
import org.w3c.dom.Document;
import org.wso2.identity.integration.common.clients.application.mgt.ApplicationManagementServiceClient;
import org.wso2.identity.integration.common.utils.ISIntegrationTest;
import org.xml.sax.InputSource;

import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.IOException;
import java.io.StringReader;
import java.net.URI;
import java.net.URISyntaxException;
import java.nio.charset.Charset;
import java.util.HashMap;
import java.util.Map;
import java.util.Scanner;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;

public class ImportExportServiceProviderTest extends ISIntegrationTest {

    private ConfigurationContext configContext;
    private ApplicationManagementServiceClient applicationManagementServiceClient;
    private static Pattern NAMESPACE_PATTERN = Pattern.compile("xmlns:(ns\\d+)=\"(.*?)\"");
    private String applicationName = "TestServer";

    @BeforeClass(alwaysRun = true)
    public void testInit() throws Exception {

        super.init();
        configContext = ConfigurationContextFactory.createConfigurationContextFromFileSystem(null
                , null);
        applicationManagementServiceClient = new ApplicationManagementServiceClient(sessionCookie, backendURL,
                configContext);

    }

    @AfterClass(alwaysRun = true)
    public void atEnd() throws Exception {

        try {
            applicationManagementServiceClient.deleteApplication(applicationName);
        } catch (Exception e) {
            Assert.fail("Error while trying to delete Service Provider", e);
        }
        applicationManagementServiceClient = null;
    }

    @Test(description = "Upload application from XML file")
    public void testImportExportApplication() {

        String fileName = applicationName + ".xml";
        String content = readResource("spFile/" + fileName);
        try {
            applicationManagementServiceClient.importApplication(content, fileName);
            Assert.assertEquals(applicationManagementServiceClient.getApplication(applicationName).getApplicationName(),
                    applicationName, "Failed to import a Service Provider");
        } catch (Exception e) {
            Assert.fail("Error while trying to importing a Service Provider", e);
        }

        try {
            String spContent = applicationManagementServiceClient.exportApplication(applicationName, true);
            DocumentBuilder builder = DocumentBuilderFactory.newInstance().newDocumentBuilder();
            InputSource src = new InputSource();
            src.setCharacterStream(new StringReader(spContent));
            Document doc = builder.parse(src);

            Assert.assertEquals(applicationName, doc.getElementsByTagName("ApplicationName").item(0).getTextContent()
                    , "Failed to export a Service Provider");
        } catch (Exception e) {
            Assert.fail("Error while trying to exporting the Service Provider", e);
        }

    }

    private String readResource(String path) {

        StringBuilder result = new StringBuilder();
        Scanner scanner = null;
        try {
            //Get file from resources folder
            ClassLoader classLoader = getClass().getClassLoader();
            URI filepath = new URI(classLoader.getResource(path).toString());

            File file = new File(filepath);

            scanner = new Scanner(file);

            while (scanner.hasNextLine()) {
                String line = scanner.nextLine();
                result.append(line).append("\n");
            }

        } catch (IOException e) {
            log.error("Error occured when reading the file.", e);
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

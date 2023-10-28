/*
 * Copyright (c) 2023, WSO2 LLC. (http://www.wso2.com).
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

package org.wso2.identity.integration.test.utils;

import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NodeList;

import java.io.ByteArrayInputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;

/**
 * Utility class for Carbon related operations.
 */
public class CarbonUtils {

    private static final String ENABLE_LEGACY_AUTHZ_RUNTIME_CONFIG = "EnableLegacyAuthzRuntime";
    private static Boolean isLegacyRuntimeEnabled;

    private CarbonUtils() {

    }

    /**
     * Check whether the legacy authz runtime is enabled.
     *
     * @return True if legacy authz runtime is enabled.
     * @throws Exception If an error occurs while getting the config.
     */
    public static boolean isLegacyAuthzRuntimeEnabled() throws Exception {

        if (isLegacyRuntimeEnabled == null) {
            isLegacyRuntimeEnabled = initializeLegacyAuthzRuntimeEnabled();
        }
        return isLegacyRuntimeEnabled;
    }

    /**
     * Set the value of EnableLegacyAuthzRuntime in carbon.xml.
     *
     * @return True if the EnableLegacyAuthzRuntime is set to true.
     * @throws Exception If an error occurs while intializing the config.
     */
    private static boolean initializeLegacyAuthzRuntimeEnabled() throws Exception {

        String carbonXMLContent;
        // Read the carbon.xml file.
        try {
            carbonXMLContent = readCarbonXML();
        } catch (Exception e) {
            throw new Exception("Error while reading the carbon.xml file.", e);
        }
        // Parse the carbon.xml file.
        try {
            return parseLegacyAuthzRuntimeConfig(carbonXMLContent);
        } catch (Exception e) {
            throw new Exception("Error while parsing the carbon.xml file.", e);
        }
    }

    /**
     * Read the carbon.xml file.
     *
     * @return Carbon XML content.
     * @throws Exception If an error occurs while reading the carbon.xml file.
     */
    private static String readCarbonXML() throws Exception {
        
        String carbonHome = System.getProperty("carbon.home");
        String carbonXMLFilePath = carbonHome + "/repository/conf/carbon.xml";
        Path filePath = Paths.get(carbonXMLFilePath);
        return new String(Files.readAllBytes(filePath));
    }

    /**
     * Parse the carbon.xml file and get the value of EnableLegacyAuthzRuntime.
     *
     * @param xmlContent Carbon XML content.
     * @return True if the EnableLegacyAuthzRuntime is set to true.
     * @throws Exception If an error occurs while parsing the carbon.xml file.
     */
    private static boolean parseLegacyAuthzRuntimeConfig(String xmlContent) throws Exception {

        DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
        DocumentBuilder builder = factory.newDocumentBuilder();
        Document document = builder.parse(new ByteArrayInputStream(xmlContent.getBytes()));

        // Get the root element
        Element root = document.getDocumentElement();
        // Find the element with the EnableLegacyAuthzRuntime tag.
        NodeList nodeList = root.getElementsByTagName(ENABLE_LEGACY_AUTHZ_RUNTIME_CONFIG);

        if (nodeList.getLength() > 0) {
            // Get the value of EnableLegacyAuthzRuntime
            String enableLegacyAuthzRuntimeValue = nodeList.item(0).getTextContent();
            return Boolean.parseBoolean(enableLegacyAuthzRuntimeValue);
        }
        return true;
    }
}

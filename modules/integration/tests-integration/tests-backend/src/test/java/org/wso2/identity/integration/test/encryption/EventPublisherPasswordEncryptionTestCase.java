/*
 *     Copyright (c) 2020, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
 *
 *     WSO2 Inc. licenses this file to you under the Apache License,
 *     Version 2.0 (the "License"); you may not use this file except
 *     in compliance with the License.
 *     You may obtain a copy of the License at
 *
 *       http://www.apache.org/licenses/LICENSE-2.0
 *
 *    Unless required by applicable law or agreed to in writing,
 *    software distributed under the License is distributed on an
 *    "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 *    KIND, either express or implied.  See the License for the
 *    specific language governing permissions and limitations
 *    under the License.
 */
package org.wso2.identity.integration.test.encryption;

import org.json.JSONObject;
import org.testng.Assert;
import org.testng.annotations.AfterClass;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;
import org.w3c.dom.Document;
import org.w3c.dom.NodeList;
import org.wso2.carbon.integration.common.utils.mgt.ServerConfigurationManager;
import org.wso2.identity.integration.common.utils.ISIntegrationTest;
import org.wso2.identity.integration.test.util.Utils;
import org.xml.sax.SAXException;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.xpath.XPath;
import javax.xml.xpath.XPathConstants;
import javax.xml.xpath.XPathExpression;
import javax.xml.xpath.XPathExpressionException;
import javax.xml.xpath.XPathFactory;
import java.io.File;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.Base64;

public class EventPublisherPasswordEncryptionTestCase extends ISIntegrationTest {

    private ServerConfigurationManager serverConfigurationManager;
    private String eventPublisherDeploymentDirectory;
    private String eventPublisherFileName = "IsAnalytics-Publisher-wso2event-AuthenticationData.xml";

    @BeforeClass(alwaysRun = true)
    public void testInit() throws Exception {

        super.init();
        serverConfigurationManager = new ServerConfigurationManager(isServer);

        eventPublisherDeploymentDirectory =
                Utils.getResidentCarbonHome() + File.separator + "repository" + File.separator +
                        "deployment" + File.separator + "server" + File.separator + "eventpublishers";
    }

    @Test(groups = "wso2.is",
            description = "Test encryption of password in user store configuration xml",
            priority = 1)
    public void testCustomPasswordEncryptionOfEventPublishers() throws Exception {

        File eventPublisherFile = new File(eventPublisherDeploymentDirectory + File.separator + eventPublisherFileName);
        decryptEventPublisherPassword(eventPublisherFile);
    }

    private void decryptEventPublisherPassword(File eventPublisherFile) throws Exception {

        DocumentBuilderFactory documentBuilderFactory = DocumentBuilderFactory.newInstance();
        documentBuilderFactory.setNamespaceAware(true);
        DocumentBuilder builder;
        Document doc;
        try {
            builder = documentBuilderFactory.newDocumentBuilder();
            doc = builder.parse(eventPublisherFile);
            XPathFactory xpathFactory = XPathFactory.newInstance();
            XPath xpath = xpathFactory.newXPath();
            NodeList data;
            data = getEncryptedPayload(doc, xpath);
            if (data.getLength() > 0) {
                for (int i = 0; i < data.getLength(); i++) {
                    String newEncryptedPassword = data.item(i).getNodeValue();
                    String decodedPasswordData = new String(Base64.getDecoder().decode(newEncryptedPassword),
                            StandardCharsets.UTF_8);
                    JSONObject jsonObject = new JSONObject(decodedPasswordData);
                    String tValue = jsonObject.getString("t");
                    Assert.assertEquals(tValue, "AES/GCM/NoPadding", "Password encryption has used an unexpected " +
                            "algorithm");
                }
            }
        } catch (ParserConfigurationException | SAXException | IOException | XPathExpressionException e) {
            throw new Exception(
                    "Error occurred while decrypting event publisher password for " + eventPublisherFileName);
        }
    }

    private static NodeList getEncryptedPayload(Document doc, XPath xpath)
            throws XPathExpressionException {

        XPathExpression expr = xpath
                .compile("//*[local-name()='property'][@*[local-name()='encrypted']='true']/text()");
        return (NodeList) expr.evaluate(doc, XPathConstants.NODESET);
    }

    @AfterClass(alwaysRun = true)
    public void cleanup() throws Exception {

        serverConfigurationManager.restoreToLastConfiguration();
    }
}

/*
* Copyright (c) 2018, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
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
package org.wso2.carbon.is.migration.service.v550.migrator;

import org.apache.commons.lang.StringUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.w3c.dom.Document;
import org.w3c.dom.NodeList;
import org.wso2.carbon.identity.core.migrate.MigrationClientException;
import org.wso2.carbon.is.migration.service.Migrator;
import org.wso2.carbon.is.migration.util.EncryptionUtil;
import org.wso2.carbon.is.migration.util.Constant;

import java.io.File;
import java.util.Objects;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;
import javax.xml.xpath.XPath;
import javax.xml.xpath.XPathConstants;
import javax.xml.xpath.XPathExpression;
import javax.xml.xpath.XPathExpressionException;
import javax.xml.xpath.XPathFactory;

/**
 * Migrator class to migrate event publishers
 */
public class EventPublisherMigrator extends Migrator {

    private static final Log log = LogFactory.getLog(EventPublisherMigrator.class);

    @Override
    public void migrate() throws MigrationClientException {

        migrateEventPublishers();
    }

    private void migrateEventPublishers() throws MigrationClientException {

        log.info(Constant.MIGRATION_LOG + "Migration starting on event publisher files. ");
        File publisherPath = readFiles(System.getProperty(Constant.CARBON_HOME) + Constant.EVENT_PUBLISHER_PATH);
        migrateData(publisherPath);
        log.info(Constant.MIGRATION_LOG + "Migrating event publishers was successful. ");
    }

    private void migrateData(File folder) throws MigrationClientException {
        DocumentBuilderFactory documentBuilderFactory = DocumentBuilderFactory.newInstance();
        documentBuilderFactory.setNamespaceAware(true);
        DocumentBuilder builder;
        Document doc;
        try {
            for (File fileEntry : Objects.requireNonNull(folder.listFiles())) {
                builder = documentBuilderFactory.newDocumentBuilder();
                doc = builder.parse(fileEntry);
                XPathFactory xpathFactory = XPathFactory.newInstance();
                XPath xpath = xpathFactory.newXPath();
                NodeList data = getEncryptedPayload(doc, xpath);
                if (data.getLength() > 0) {
                    for (int i = 0; i < data.getLength(); i++) {
                        String newEncryptedPassword = EncryptionUtil.getNewEncryptedValue(data.item(i).getNodeValue());
                        if (StringUtils.isNotEmpty(newEncryptedPassword)) {
                            data.item(i).setNodeValue(newEncryptedPassword);
                        }
                    }
                    Transformer xformer = TransformerFactory.newInstance().newTransformer();
                    xformer.transform(new DOMSource(doc),
                            new StreamResult(new File(fileEntry.getAbsolutePath()).getPath()));
                }
            }

        } catch (Exception e) {
            throw new MigrationClientException(
                    "Error occurred while migrating data in folder : " + folder.getAbsolutePath() + " . ", e);
        }

    }

    private File readFiles(String path) {
        return new File(path);
    }

    private NodeList getEncryptedPayload(Document doc, XPath xpath) throws Exception {

        try {
            XPathExpression expr = xpath
                    .compile("//*[local-name()='property'][@*[local-name()='encrypted']='true']/text()");
            return (NodeList) expr.evaluate(doc, XPathConstants.NODESET);
        } catch (XPathExpressionException e) {
            throw new MigrationClientException(
                    "Error has occurred while retriving the payload from file : " + doc.getDocumentURI(), e);
        }

    }
}



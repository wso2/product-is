/*
 * Copyright (c) 2016, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
 *
 * WSO2 Inc. licenses this file to you under the Apache License,
 * Version 2.0 (the "License"); you may not use this file except
 * in compliance with the License.
 * You may obtain a copy of the License at
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */

package org.wso2.carbon.is.migration.service.v540.util;

import org.apache.axiom.om.OMElement;
import org.apache.axiom.om.impl.builder.StAXOMBuilder;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.osgi.framework.BundleContext;
import org.wso2.carbon.is.migration.util.Utility;
import org.wso2.carbon.user.core.UserStoreException;
import org.wso2.carbon.user.core.claim.Claim;
import org.wso2.carbon.user.core.claim.ClaimMapping;
import org.wso2.carbon.user.core.claim.inmemory.ClaimConfig;

import java.io.BufferedInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import javax.xml.namespace.QName;
import javax.xml.stream.XMLStreamException;

public class FileBasedClaimBuilder {

    public static final String LOCAL_NAME_DIALECTS = "Dialects";
    public static final String LOCAL_NAME_DIALECT = "Dialect";
    public static final String LOCAL_NAME_CLAIM = "Claim";
    public static final String LOCAL_NAME_CLAIM_URI = "ClaimURI";
    public static final String LOCAL_NAME_DESCRIPTION = "Description";
    public static final String LOCAL_NAME_ATTR_ID = "AttributeID";
    public static final String ATTR_DIALECT_URI = "dialectURI";
    private static final String CLAIM_CONFIG = "claim-config.xml";

    private static Log log = LogFactory.getLog(org.wso2.carbon.user.core.claim.inmemory.FileBasedClaimBuilder.class);
    private static BundleContext bundleContext;
    private static InputStream inStream = null;
    private int tenantId;

    public FileBasedClaimBuilder(int tenantId) {
        this.tenantId = tenantId;
    }


    /**
     * Initiate claim reading from claim config xml file, which defined local claims(wso2 claims) and the additional
     * claims by default to the system at the first start up
     *
     * @return claimConfig object which contains claims and their meta data as properties in a property holder map
     * stored as key value pairs. Number of properties for a claim may vary from claim to claim in the way of defined in
     * the claim config file.
     * @throws UserStoreException
     */
    public static ClaimConfig buildClaimMappingsFromConfigFile(String filePath) throws IOException, XMLStreamException,
                                                                        UserStoreException {

        OMElement dom;
        Map<String, ClaimMapping> claims = new HashMap<>();
        Map<String, Map<String, String>> propertyHolder = new HashMap<>();

        String dialectUri;

        Claim claim;
        ClaimMapping claimMapping;
        ClaimConfig claimConfig;

        dom = getRootElement(filePath);
        Iterator dialectsIterator = dom.getChildrenWithName(new QName(LOCAL_NAME_DIALECTS));

        //Go through Dialects
        while (dialectsIterator.hasNext()) {
            OMElement dialects = (OMElement) dialectsIterator.next();
            Iterator dialectIterator = dialects.getChildrenWithName(new QName(LOCAL_NAME_DIALECT));

            //Go through Dialect
            while (dialectIterator.hasNext()) {
                OMElement dialect = (OMElement) dialectIterator.next();
                dialectUri = dialect.getAttributeValue(new QName(ATTR_DIALECT_URI));
                Iterator claimsIterator = dialect.getChildrenWithName(new QName(LOCAL_NAME_CLAIM));

                //Go through Claims
                while (claimsIterator.hasNext()) {
                    String claimUri = null;
                    String attributeId = null;
                    OMElement claimElement = (OMElement) claimsIterator.next();
                    validateSchema(claimElement);

                    claim = new Claim();
                    claim.setDialectURI(dialectUri);

                    Iterator metadataIterator = claimElement.getChildElements();
                    Map<String, String> properties = new HashMap<>();

                    //Go through META-DATA
                    while (metadataIterator.hasNext()) {
                        OMElement metadata = (OMElement) metadataIterator.next();
                        String key = metadata.getQName().toString();
                        String value = metadata.getText();
                        if (key.equals(LOCAL_NAME_CLAIM_URI)) {
                            claim.setClaimUri(value);
                            claimUri = value;
                        }
                        if (key.equals(LOCAL_NAME_ATTR_ID)) {
                            attributeId = value;
                        }
                        properties.put(key, value);
                        properties.put(LOCAL_NAME_DIALECT, dialectUri);
                    }

                    propertyHolder.put(claimUri, properties);
                    claimMapping = new ClaimMapping(claim, attributeId);
                    claims.put(claimUri, claimMapping);
                }
            }
        }
        claimConfig = new ClaimConfig(claims, propertyHolder);
        return claimConfig;
    }


    /**
     * Do schema validation.
     *
     * @param claimElement
     * @throws UserStoreException
     */
    private static void validateSchema(OMElement claimElement) throws UserStoreException {
        String message;

        if (claimElement.getFirstChildWithName(new QName(LOCAL_NAME_CLAIM_URI)) == null) {
            message = "In valid schema <ClaimUri> element not present";
            if (log.isDebugEnabled()) {
                log.debug(message);
            }
            throw new UserStoreException(message);
        }


        if (claimElement.getFirstChildWithName(new QName(LOCAL_NAME_ATTR_ID)) == null) {
            message = "In valid schema <AttributeId> element not present";
            if (log.isDebugEnabled()) {
                log.debug(message);
            }
            throw new UserStoreException(message);
        }
    }

    /**
     * Get elements from the claim config xml files to be read.
     *
     * @return elements from the config file
     * @throws XMLStreamException
     * @throws IOException
     * @throws UserStoreException
     */
    private static OMElement getRootElement(String filePath) throws XMLStreamException, IOException,
                                                     UserStoreException {
        StAXOMBuilder builder = null;

        File claimConfigFile = new File(filePath);
        InputStream inStream = new BufferedInputStream(new FileInputStream(claimConfigFile));

        String warningMessage = "";
        if (inStream == null) {
            URL url;
            if (bundleContext != null) {
                if ((url = bundleContext.getBundle().getResource(CLAIM_CONFIG)) != null) {
                    inStream = url.openStream();
                } else {
                    warningMessage = "Bundle context could not find resource " + CLAIM_CONFIG +
                                     " or user does not have sufficient permission to access the resource.";
                }
            } else {
                if ((url = org.wso2.carbon.user.core.claim.inmemory.FileBasedClaimBuilder.class.getClassLoader()
                        .getResource(CLAIM_CONFIG)) != null) {
                    inStream = url.openStream();
                } else {
                    warningMessage = "ClaimBuilder could not find resource " + CLAIM_CONFIG +
                                     " or user does not have sufficient permission to access the resource.";
                }
            }
        }

        if (inStream == null) {
            String message = "Claim configuration not found. Cause - " + warningMessage;
            if (log.isDebugEnabled()) {
                log.debug(message);
            }
            throw new FileNotFoundException(message);
        }

        builder = new StAXOMBuilder(inStream);
        OMElement documentElement = builder.getDocumentElement();

        return documentElement;
    }
}
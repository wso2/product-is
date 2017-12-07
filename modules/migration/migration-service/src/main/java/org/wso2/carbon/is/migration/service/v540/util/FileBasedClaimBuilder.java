/*
* Copyright (c) 2017, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
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

package org.wso2.carbon.is.migration.service.v540.util;

import org.apache.axiom.om.OMElement;
import org.apache.axiom.om.impl.builder.StAXOMBuilder;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.wso2.carbon.user.core.UserCoreConstants;
import org.wso2.carbon.user.core.UserStoreException;
import org.wso2.carbon.user.core.claim.Claim;
import org.wso2.carbon.user.core.claim.ClaimKey;
import org.wso2.carbon.user.core.claim.ClaimMapping;
import org.wso2.carbon.user.core.claim.inmemory.ClaimConfig;

import java.io.BufferedInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import javax.xml.namespace.QName;
import javax.xml.stream.XMLStreamException;

/**
 * Util class to build File based claims.
 */
public class FileBasedClaimBuilder {

    private static final String LOCAL_NAME_DIALECTS = "Dialects";

    private static final String LOCAL_NAME_DIALECT = "Dialect";

    private static final String LOCAL_NAME_CLAIM = "Claim";

    private static final String LOCAL_NAME_CLAIM_URI = "ClaimURI";

    private static final String LOCAL_NAME_ATTR_ID = "AttributeID";

    private static final String ATTR_DIALECT_URI = "dialectURI";

    private FileBasedClaimBuilder() {

    }

    /**
     * Initiate claim reading from claim config xml file, which defined local claims(wso2 claims) and the additional
     * claims by default to the system at the first start up
     *
     * @return claimConfig object which contains claims and their meta data as properties in a property holder map
     * stored as key value pairs. Number of properties for a claim may vary from claim to claim in the way of defined in
     * the claim config file.
     * @throws UserStoreException UserStoreException
     */
    public static ClaimConfig buildClaimMappingsFromConfigFile(String filePath) throws IOException, XMLStreamException,
            UserStoreException {

        OMElement dom;
        Map<ClaimKey, ClaimMapping> claims = new HashMap<>();
        Map<ClaimKey, Map<String, String>> propertyHolder = new HashMap<>();

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
                    //Unique key for claim.
                    ClaimKey claimKey = new ClaimKey();
                    claimKey.setClaimUri(claimUri);
                    claimKey.setDialectUri(dialectUri);

                    propertyHolder.put(claimKey, properties);
                    claimMapping = new ClaimMapping();
                    claimMapping.setClaim(claim);
                    setMappedAttributes(claimMapping, attributeId);
                    claims.put(claimKey, claimMapping);
                }
            }
        }
        claimConfig = new ClaimConfig();
        claimConfig.setClaimMap(claims);
        claimConfig.setPropertyHolderMap(propertyHolder);
        return claimConfig;
    }

    /**
     * Do schema validation.
     *
     * @param claimElement claim element
     * @throws UserStoreException UserStoreException
     */
    private static void validateSchema(OMElement claimElement) throws UserStoreException {

        if (claimElement.getFirstChildWithName(new QName(LOCAL_NAME_CLAIM_URI)) == null) {
            throw new UserStoreException("In valid schema <ClaimUri> element not present");
        }
        if (claimElement.getFirstChildWithName(new QName(LOCAL_NAME_ATTR_ID)) == null) {
            throw new UserStoreException("In valid schema <AttributeId> element not present");
        }
    }

    /**
     * Get elements from the claim config xml files to be read.
     *
     * @return elements from the config file
     * @throws XMLStreamException XMLStreamException
     * @throws IOException        IOException
     */
    private static OMElement getRootElement(String filePath) throws XMLStreamException, IOException {

        File claimConfigFile = new File(filePath);
        InputStream inStream = new BufferedInputStream(new FileInputStream(claimConfigFile));
        return new StAXOMBuilder(inStream).getDocumentElement();
    }

    /**
     * Set mapped attributes for a claim mapping.
     *
     * @param claimMapping    claim mapping
     * @param mappedAttribute mapped attribute
     */
    private static void setMappedAttributes(ClaimMapping claimMapping, String mappedAttribute) {

        if (mappedAttribute != null) {
            String[] attributes = mappedAttribute.split(";");
            Map<String, String> attrMap = new HashMap<>();

            for (String attribute : attributes) {
                int index;
                if ((index = attribute.indexOf(UserCoreConstants.DOMAIN_SEPARATOR)) > 1 && attribute.indexOf
                        (UserCoreConstants.DOMAIN_SEPARATOR) == attribute.lastIndexOf(UserCoreConstants
                        .DOMAIN_SEPARATOR)) {
                    String domain = attribute.substring(0, index);
                    String attrName = attribute.substring(index + 1);
                    attrMap.put(domain.toUpperCase(), attrName);
                } else {
                    claimMapping.setMappedAttribute(attribute);
                }
            }

            if (attrMap.size() > 0) {
                claimMapping.setMappedAttributes(attrMap);
            }
        }
    }
}

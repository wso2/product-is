/*
 * Copyright (c) 2016, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
 *
 *  WSO2 Inc. licenses this file to you under the Apache License,
 *  Version 2.0 (the "License"); you may not use this file except
 *  in compliance with the License.
 *  You may obtain a copy of the License at
 *
 *    http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */
package org.wso2.identity.integration.common.clients.sso.saml.query;

import org.apache.axiom.om.OMElement;
import org.apache.axiom.om.util.AXIOMUtil;
import org.apache.axis2.AxisFault;
import org.apache.axis2.addressing.EndpointReference;
import org.apache.axis2.client.Options;
import org.apache.axis2.client.ServiceClient;
import org.apache.axis2.context.ConfigurationContext;
import org.apache.axis2.context.ConfigurationContextFactory;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.joda.time.DateTime;
import org.opensaml.saml.common.SAMLVersion;
import org.opensaml.saml.saml1.core.NameIdentifier;
import org.opensaml.saml.saml2.core.AssertionIDRef;
import org.opensaml.saml.saml2.core.AssertionIDRequest;
import org.opensaml.saml.saml2.core.AttributeQuery;
import org.opensaml.saml.saml2.core.Issuer;
import org.opensaml.saml.saml2.core.NameID;
import org.opensaml.saml.saml2.core.NameIDType;
import org.opensaml.saml.saml2.core.Subject;
import org.opensaml.saml.saml2.core.SubjectConfirmation;
import org.opensaml.saml.saml2.core.SubjectConfirmationData;
import org.opensaml.saml.saml2.core.impl.AssertionIDRefBuilder;
import org.opensaml.saml.saml2.core.impl.AssertionIDRequestBuilder;
import org.opensaml.saml.saml2.core.impl.AttributeQueryBuilder;
import org.opensaml.saml.saml2.core.impl.IssuerBuilder;
import org.opensaml.saml.saml2.core.impl.NameIDBuilder;
import org.opensaml.saml.saml2.core.impl.SubjectBuilder;
import org.opensaml.saml.saml2.core.impl.SubjectConfirmationBuilder;
import org.opensaml.saml.saml2.core.impl.SubjectConfirmationDataBuilder;
import org.opensaml.xmlsec.signature.SignableXMLObject;

import javax.xml.stream.XMLStreamException;
import java.util.List;
import java.util.UUID;

/**
 * This class is used as the client application of the SAMLQuery Profile Attribute query
 */
public class SAMLQueryClient {

    private static final Log log = LogFactory.getLog(SAMLQueryClient.class);

    private static final String SERVICE_NAME = "services/SAMLQueryService";

    private static final String SOAP_ACTION = "http://wso2.org/identity/saml/query";

    private static final String AUTH_CONTEXT_CLASS_REF = "urn:oasis:names:tc:SAML:2.0:ac:classes:Password";

    private static final String DIGEST_METHOD_ALGO = "http://www.w3.org/2000/09/xmldsig#rsa-sha1";

    private static final String SIGNING_ALGO = "http://www.w3.org/2000/09/xmldsig#sha1";

    private String endPoint;

    private ClientSignKeyDataHolder signKeyDataHolder;

    /**
     * Constructor method
     * @param endPoint SOAP service endpoint
     * @param signKeyDataHolder Client sign key data holder instance
     */
    public SAMLQueryClient(String endPoint, ClientSignKeyDataHolder signKeyDataHolder) {

        if (endPoint.endsWith("/")) {
            this.endPoint = endPoint + SERVICE_NAME;
        } else {
            this.endPoint = endPoint + "/" + SERVICE_NAME;
        }
        this.signKeyDataHolder = signKeyDataHolder;

    }

    /**
     * This method is used to create AssertionIDRequest message
     * @param issuer Issuer of the request message
     * @param assertionId Requested Assertion id
     * @return Response response message from SAMLQuery service
     */
    public String executeIDRequest(String issuer, String assertionId) {

        String id = "_" + UUID.randomUUID().toString();
        DateTime issueInstant = new DateTime();
        Issuer issuerElement = new IssuerBuilder().buildObject();
        issuerElement.setValue(issuer);
        issuerElement.setFormat(NameIDType.ENTITY);
        AssertionIDRef assertionIDRef = new AssertionIDRefBuilder().buildObject();
        assertionIDRef.setAssertionID(assertionId);
        AssertionIDRequest idRequest = new AssertionIDRequestBuilder().buildObject();
        idRequest.setVersion(SAMLVersion.VERSION_20);
        idRequest.setID(id);
        idRequest.setIssueInstant(issueInstant);
        idRequest.setIssuer(issuerElement);
        idRequest.getAssertionIDRefs().add(assertionIDRef);
        return executeClient(idRequest);

    }

    /**
     * This method is used to create and execute AttributeQuery Request
     * @param issuer Issuer of the request
     * @param subject Subject of the assertion
     * @param attributes Requested assertions
     * @return Response response message from SAMLQuery service
     */
    public String executeAttributeQuery(String issuer, String subject, List<String> attributes) {

        String id = "_" + UUID.randomUUID().toString();
        DateTime issueInstant = new DateTime();
        DateTime notOnOrAfter =
                new DateTime(issueInstant.getMillis() + (long) 60 * 1000);

        Issuer issuerElement = new IssuerBuilder().buildObject();
        issuerElement.setValue(issuer);
        issuerElement.setFormat(NameIDType.ENTITY);

        Subject subjectElement = new SubjectBuilder().buildObject();
        NameID nameID = new NameIDBuilder().buildObject();
        SubjectConfirmation subjectConfirmation = new SubjectConfirmationBuilder().buildObject();
        SubjectConfirmationData subjectConfirmationData =
                new SubjectConfirmationDataBuilder().buildObject();
        nameID.setValue(subject);
        nameID.setFormat(NameIdentifier.EMAIL);
        subjectConfirmationData.setNotOnOrAfter(notOnOrAfter);
        subjectConfirmation.setSubjectConfirmationData(subjectConfirmationData);
        subjectConfirmation.setMethod(SubjectConfirmation.METHOD_BEARER);
        subjectElement.getSubjectConfirmations().add(subjectConfirmation);
        subjectElement.setNameID(nameID);

        AttributeQuery attributeQuery = new AttributeQueryBuilder().buildObject();
        attributeQuery.setVersion(SAMLVersion.VERSION_20);
        attributeQuery.setID(id);
        attributeQuery.setIssueInstant(issueInstant);
        attributeQuery.setIssuer(issuerElement);
        attributeQuery.setSubject(subjectElement);
        return executeClient(attributeQuery);

    }

    /**
     * This method is used to execute test request
     * @param xmlObject Request message
     * @return Response response message from SAMLQuery service
     */
    private String executeClient(SignableXMLObject xmlObject) {

        ConfigurationContext configurationContext = null;
        ServiceClient serviceClient = null;

        try {
            configurationContext = ConfigurationContextFactory.
                    createConfigurationContextFromFileSystem(null, null);
            serviceClient = new ServiceClient(configurationContext, null);

            Options options = new Options();
            options.setTo(new EndpointReference(endPoint));
            options.setAction(SOAP_ACTION);
            serviceClient.setOptions(options);

            QueryClientUtils.doBootstrap();
            xmlObject = QueryClientUtils.setXMLSignature(xmlObject, DIGEST_METHOD_ALGO, SIGNING_ALGO, signKeyDataHolder);
            String body = QueryClientUtils.marshall(xmlObject);
            OMElement result = serviceClient.sendReceive(AXIOMUtil.stringToOM(body));
            return result.toString();

        } catch (AxisFault axisFault) {
            log.error("Unable to initiate service client", axisFault);
        } catch (XMLStreamException e) {
            log.error("Unable to parse XML element", e);
        }

        return null;
    }

}

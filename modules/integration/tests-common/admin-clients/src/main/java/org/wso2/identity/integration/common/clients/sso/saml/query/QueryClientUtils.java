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

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.xml.security.Init;
import org.apache.xml.security.c14n.Canonicalizer;
import org.opensaml.core.config.InitializationException;
import org.opensaml.core.config.InitializationService;
import org.opensaml.core.xml.XMLObject;
import org.opensaml.core.xml.config.XMLObjectProviderRegistrySupport;
import org.opensaml.core.xml.io.Marshaller;
import org.opensaml.core.xml.io.MarshallerFactory;
import org.opensaml.core.xml.io.MarshallingException;
import org.opensaml.core.xml.io.Unmarshaller;
import org.opensaml.core.xml.io.UnmarshallerFactory;
import org.opensaml.core.xml.io.UnmarshallingException;
import org.opensaml.saml.common.SAMLObjectContentReference;
import org.opensaml.saml.config.SAMLConfigurationInitializer;
import org.opensaml.saml.saml2.core.Response;
import org.opensaml.security.x509.X509Credential;
import org.opensaml.xmlsec.signature.KeyInfo;
import org.opensaml.xmlsec.signature.SignableXMLObject;
import org.opensaml.xmlsec.signature.Signature;
import org.opensaml.xmlsec.signature.X509Certificate;
import org.opensaml.xmlsec.signature.X509Data;
import org.opensaml.xmlsec.signature.support.SignatureException;
import org.opensaml.xmlsec.signature.support.Signer;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.bootstrap.DOMImplementationRegistry;
import org.w3c.dom.ls.DOMImplementationLS;
import org.w3c.dom.ls.LSOutput;
import org.w3c.dom.ls.LSSerializer;
import org.xml.sax.SAXException;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.UnsupportedEncodingException;
import java.nio.charset.StandardCharsets;
import java.security.cert.CertificateEncodingException;
import java.util.ArrayList;
import java.util.List;

import static org.opensaml.core.xml.util.XMLObjectSupport.buildXMLObject;

/**
 * This class is used as util to generate Query request messages
 */
public class QueryClientUtils {

    private static boolean isBootstrapped = false;

    private static final Log log = LogFactory.getLog(QueryClientUtils.class);

    /**
     * Initializes the OpenSAML library modules, if not initialized yet.
     *
     */
    public static void doBootstrap() {
        try {
            if (!isBootstrapped) {
                InitializationService.initialize();
                SAMLConfigurationInitializer initializer = new SAMLConfigurationInitializer();
                initializer.init();
                isBootstrapped = true;
            }
        } catch (InitializationException e) {
            log.error("Unable to bootstrap the opensaml", e);
        }
    }


    /**
     * This method is used to serialize response message
     *
     * @param xmlObject well formed XML object
     * @return String serialized response
     */
    public static String marshall(XMLObject xmlObject)  {

        ByteArrayOutputStream byteArrayOutputStrm = null;
        try {
            doBootstrap();
            System.setProperty("javax.xml.parsers.DocumentBuilderFactory",
                    "org.apache.xerces.jaxp.DocumentBuilderFactoryImpl");
            MarshallerFactory marshallerFactory = XMLObjectProviderRegistrySupport.getMarshallerFactory();
            Marshaller marshaller = marshallerFactory.getMarshaller(xmlObject);
            if(marshaller != null) {
                Element element = marshaller.marshall(xmlObject);
                byteArrayOutputStrm = new ByteArrayOutputStream();
                DOMImplementationRegistry registry = DOMImplementationRegistry.newInstance();
                DOMImplementationLS impl = (DOMImplementationLS) registry.getDOMImplementation("LS");
                LSSerializer writer = impl.createLSSerializer();
                LSOutput output = impl.createLSOutput();
                output.setByteStream(byteArrayOutputStrm);
                writer.write(element, output);
                return byteArrayOutputStrm.toString("UTF-8");
            } else {
                log.error("Error can not find marshaller");
            }
        } catch (InstantiationException e) {
            log.error("Unable to initiate DOM implementation registry",e);
        } catch (MarshallingException e) {
            log.error("Unable to marshall element",e);
        } catch (IllegalAccessException e) {
            log.error("Illegal access on DOM registry ",e);
        } catch (UnsupportedEncodingException e) {
            log.error("Unsupported encoding scheme",e);
        } catch (ClassNotFoundException e) {
            log.error("Class not found",e);
        } finally {
            if (byteArrayOutputStrm != null) {
                try {
                    byteArrayOutputStrm.close();
                } catch (IOException e) {
                    log.error("Error while closing the stream", e);
                }
            }
        }

        return null;
    }

    /**
     * This method is used to set XML signature
     * @param signableXMLObject XML element or message
     * @param signatureAlgorithm Signature Algorithm
     * @param digestAlgorithm Digest Algorithm
     * @param cred X509Credential instance
     * @return SignableXMLObject XML element or message
     */
    public static SignableXMLObject setXMLSignature(SignableXMLObject signableXMLObject, String signatureAlgorithm, String
            digestAlgorithm, X509Credential cred) {

        Signature signature = (Signature) buildXMLObject(Signature.DEFAULT_ELEMENT_NAME);
        signature.setSigningCredential(cred);
        signature.setSignatureAlgorithm(signatureAlgorithm);
        signature.setCanonicalizationAlgorithm(Canonicalizer.ALGO_ID_C14N_EXCL_OMIT_COMMENTS);
        
        try {
            KeyInfo keyInfo = (KeyInfo) buildXMLObject(KeyInfo.DEFAULT_ELEMENT_NAME);
            X509Data data = (X509Data) buildXMLObject(X509Data.DEFAULT_ELEMENT_NAME);
            X509Certificate cert = (X509Certificate) buildXMLObject(X509Certificate.DEFAULT_ELEMENT_NAME);
            String value = org.apache.xml.security.utils.Base64.encode(cred.getEntityCertificate().getEncoded());
            cert.setValue(value);
            data.getX509Certificates().add(cert);
            keyInfo.getX509Datas().add(data);
            signature.setKeyInfo(keyInfo);
        } catch (CertificateEncodingException e) {
            log.error("Error occurred while retrieving encoded cert", e);
        }

        signableXMLObject.setSignature(signature);
        ((SAMLObjectContentReference) signature.getContentReferences().get(0)).setDigestAlgorithm(digestAlgorithm);

        List<Signature> signatureList = new ArrayList<>();
        signatureList.add(signature);

        MarshallerFactory marshallerFactory = XMLObjectProviderRegistrySupport.getMarshallerFactory();
        Marshaller marshaller = marshallerFactory.getMarshaller(signableXMLObject);

        if (marshaller != null) {
            try {
                marshaller.marshall(signableXMLObject);
            } catch (MarshallingException e) {
                log.error("Unable to marshall the request", e);
            }
        }

        Init.init();

        try {
            Signer.signObjects(signatureList);
        } catch (SignatureException e) {
            log.error("Error occurred while signing request", e);
        }

        return signableXMLObject;
    }

    /**
     * This method is used to extract assertion id from Assertion
     * @param xmlResponse Response message with assertions
     * @return String AssertionID
     */
    public static String getAssertionId (String xmlResponse) {

        Response response = (Response) unmarshall(xmlResponse);
        return response.getAssertions().get(0).getID();
    }

    /**
     * This method is used to unmarshall request message
     * @param xmlString Request message in text format
     * @return XMLObject Request message as XML
     */
    private static XMLObject unmarshall(String xmlString) {
        InputStream inputStream;
        try {
            doBootstrap();
            DocumentBuilderFactory documentBuilderFactory = DocumentBuilderFactory.newInstance();
            documentBuilderFactory.setNamespaceAware(true);
            documentBuilderFactory.setXIncludeAware(false);
            documentBuilderFactory.setExpandEntityReferences(false);
            DocumentBuilder docBuilder = documentBuilderFactory.newDocumentBuilder();
            inputStream = new ByteArrayInputStream(xmlString.trim().getBytes(StandardCharsets.UTF_8));
            Document document = docBuilder.parse(inputStream);
            Element element = document.getDocumentElement();
            UnmarshallerFactory unmarshallerFactory = XMLObjectProviderRegistrySupport.getUnmarshallerFactory();
            Unmarshaller unmarshaller = unmarshallerFactory.getUnmarshaller(element);
            return unmarshaller.unmarshall(element);
        } catch (UnmarshallingException e) {
            log.error("Unable to unmarshall request message",e);
        } catch (SAXException e) {
            log.error("Unable to parse input stream",e);
        } catch (ParserConfigurationException e) {
            log.error("Unable to initiate document builder",e);
        } catch (IOException e) {
            log.error("Unable to read xml stream",e);
        }

        return null;
    }



}
